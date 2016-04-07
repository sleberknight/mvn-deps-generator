package mvn.depgenerator;

import mvn.depgenerator.util.Json;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class DependencySearcher {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String MAVEN_SEARCH_URL = "http://search.maven.org";
    private static final String MAVEN_SEARCH_PATH = "solrsearch/select";
    private static final int ROWS = 1000;
    private static final String TYPE = "json";

    private static final Client CLIENT = ClientBuilder.newClient();

    public static void close() {
        CLIENT.close();
    }

    public static List<MvnDependency> getMvnDependencies(String group, String version) {
        WebTarget baseTarget = getBaseTarget();
        ResponseInfo responseInfo = search(baseTarget, group, version);
        validateResponse(responseInfo);

        Map<String, Object> responseMap = Json.toMap(responseInfo.responseText);
        return MvnDependencies.docs(responseMap).stream()
                .map(MvnDependency::from)
                .collect(toList());
    }

    public static List<MvnGroupDependency> getMvnDependencies(String group) {
        WebTarget baseTarget = getBaseTarget();
        ResponseInfo responseInfo = search(baseTarget, group);
        validateResponse(responseInfo);

        Map<String, Object> responseMap = Json.toMap(responseInfo.responseText);
        return MvnDependencies.docs(responseMap).stream()
                .map(MvnGroupDependency::from)
                .collect(toList());
    }

    public static String getPomDependencyXml(List<MvnDependency> mvnDeps) {
        return mvnDeps.stream()
                .map(MvnDependency::toXml)
                .collect(joining(LINE_SEPARATOR));
    }

    private static void validateResponse(ResponseInfo responseInfo) {
        if (responseInfo.statusCode != Response.Status.OK.getStatusCode()) {
            String error = String.format("%s (status code: %s)", responseInfo.responseText, responseInfo.statusCode);
            throw new RuntimeException(error);
        }
    }

    private static WebTarget getBaseTarget() {
        return CLIENT.target(MAVEN_SEARCH_URL).path(MAVEN_SEARCH_PATH);
    }

    private static ResponseInfo search(WebTarget baseTarget, String group) {
        WebTarget target = buildTarget(baseTarget, group);
        return search(target);
    }

    private static ResponseInfo search(WebTarget baseTarget, String group, String version) {
        WebTarget target = buildTarget(baseTarget, group, version);
        return search(target);
    }

    private static ResponseInfo search(WebTarget target) {
        Response response = target.request().get();
        return new ResponseInfo(response.getStatusInfo(), response.readEntity(String.class));
    }

    private static WebTarget buildTarget(WebTarget baseTarget, String group, String version) {
        String solrQuery = buildSolrQuery(group, version);
        return buildWebTarget(baseTarget, solrQuery);
    }

    private static WebTarget buildTarget(WebTarget baseTarget, String group) {
        String solrQuery = buildSolrQuery(group);
        return buildWebTarget(baseTarget, solrQuery);
    }

    private static WebTarget buildWebTarget(WebTarget baseTarget, String solrQuery) {
        return baseTarget.queryParam("q", solrQuery)
                .queryParam("rows", ROWS)
                .queryParam("wt", TYPE);
    }

    private static String buildSolrQuery(String group, String version) {
        return String.format("g:\"%s\" AND v:\"%s\"", group, version);
    }

    private static String buildSolrQuery(String group) {
        return String.format("g:\"%s\"", group);
    }

}

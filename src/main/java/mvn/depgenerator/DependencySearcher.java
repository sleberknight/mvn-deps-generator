package mvn.depgenerator;

import mvn.depgenerator.util.Json;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class DependencySearcher {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String MAVEN_SEARCH_URL = "http://search.maven.org";
    private static final String MAVEN_SEARCH_PATH = "solrsearch/select";
    private static final int ROWS = 1000;
    private static final String TYPE = "json";

    public static List<MvnDependency> getMvnDependencies(String group, String version) {
        return doWithClient(ClientBuilder.newClient(), (client) -> {
            WebTarget baseTarget = client.target(MAVEN_SEARCH_URL).path(MAVEN_SEARCH_PATH);
            ResponseInfo responseInfo = search(baseTarget, group, version);
            if (responseInfo.statusCode != Response.Status.OK.getStatusCode()) {
                String error = String.format("%s (status code: %s)", responseInfo.responseText, responseInfo.statusCode);
                throw new RuntimeException(error);
            }

            Map<String, Object> responseMap = Json.toMap(responseInfo.responseText);
            return MvnDependency.docs(responseMap).stream()
                    .map(MvnDependency::from)
                    .collect(toList());
        });
    }

    public static String getPomDependencyXml(List<MvnDependency> mvnDeps) {
        return mvnDeps.stream()
                .map(MvnDependency::toXml)
                .collect(joining(LINE_SEPARATOR));
    }

    private static <T> T doWithClient(Client client, Function<Client, T> function) {
        try {
            return function.apply(client);
        } finally {
            client.close();
        }
    }

    private static ResponseInfo search(WebTarget baseTarget, String group, String version) {
        WebTarget target = buildTarget(baseTarget, group, version);
        Response response = target.request().get();
        return new ResponseInfo(response.getStatusInfo(), response.readEntity(String.class));
    }

    private static WebTarget buildTarget(WebTarget baseTarget, String group, String version) {
        String solrQuery = buildSolrQuery(group, version);
        return baseTarget.queryParam("q", solrQuery)
                .queryParam("rows", ROWS)
                .queryParam("wt", TYPE);
    }

    private static String buildSolrQuery(String group, String version) {
        return String.format("g:\"%s\" AND v:\"%s\"", group, version);
    }

}

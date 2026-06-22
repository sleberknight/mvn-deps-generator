package mvn.depgenerator;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import lombok.experimental.UtilityClass;
import mvn.depgenerator.util.Json;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@UtilityClass
public class DependencySearcher {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String MAVEN_SEARCH_HOST = "central.sonatype.com";
    private static final String MAVEN_SEARCH_PATH = "/solrsearch/select";
    private static final int ROWS = 1000;
    private static final String TYPE = "json";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static void close() {
        CLIENT.close();
    }

    public static List<MvnDependency> getMvnDependencies(String group, String version) {
        ResponseInfo responseInfo = search(buildSolrQuery(group, version));
        validateResponse(responseInfo);

        Map<String, Object> responseMap = Json.toMap(responseInfo.responseText);
        return MvnDependencies.docs(responseMap).stream()
                .map(MvnDependency::from)
                .collect(toList());
    }

    public static List<MvnGroupDependency> getMvnDependencies(String group) {
        ResponseInfo responseInfo = search(buildSolrQuery(group));
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
        if (responseInfo.statusCode != 200) {
            String error = String.format("%s (status code: %s)", responseInfo.responseText, responseInfo.statusCode);
            throw new RuntimeException(error);
        }
    }

    private static ResponseInfo search(String solrQuery) {
        try {
            String queryString = String.format("q=%s&rows=%d&wt=%s", solrQuery, ROWS, TYPE);
            URI uri = new URI("https", MAVEN_SEARCH_HOST, MAVEN_SEARCH_PATH, queryString, null);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return new ResponseInfo(response.statusCode(), response.body());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Maven Central search failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Maven Central search was interrupted", e);
        }
    }

    private static String buildSolrQuery(String group, String version) {
        return String.format("g:%s AND v:%s", group, version);
    }

    private static String buildSolrQuery(String group) {
        return String.format("g:%s", group);
    }

}

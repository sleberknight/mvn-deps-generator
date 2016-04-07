package mvn.depgenerator;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

import static mvn.depgenerator.util.MapsHelper.immutableStringList;
import static mvn.depgenerator.util.MapsHelper.integerValue;
import static mvn.depgenerator.util.MapsHelper.longValue;
import static mvn.depgenerator.util.MapsHelper.string;

/**
 * Represent structure for searches that include only the group and optionally artifact (no version number).
 * <p>
 * <p>
 * Response structure:
 * <pre>
 * {
 *   "responseHeader":{ ... },
 *   "response":{
 *     "numFound":28,
 *     "start":0,
 *     "docs":[
 *       {...},
 *       {...}
 *     ]
 *   }
 * }
 * </pre>
 * </p>
 * <p>
 * Doc structure (each object in the "docs" array):
 * <pre>
 * {
 *   "id":"io.dropwizard:dropwizard-testing",
 *   "g":"io.dropwizard",
 *   "a":"dropwizard-testing",
 *   "latestVersion":"1.0.0-rc2",
 *   "repositoryId":"central",
 *   "p":"jar",
 *   "timestamp":1459326409000,
 *   "versionCount":28,
 *   "text":["io.dropwizard",dropwizard-testing","-sources.jar","-javadoc.jar",".jar",".pom"]
 *   "ec":["-javadoc.jar","-sources.jar",".jar",".pom"]
 * }
 * </pre>
 */
@Getter
@ToString
@EqualsAndHashCode
@Builder
public class MvnGroupDependency {

    private final String id;
    private final String group;
    private final String artifact;
    private final String latestVersion;
    private final String repositoryId;
    private final String packaging;
    private final Long timestamp;
    private final Integer versionCount;
    private final ImmutableList<String> text;
    private final ImmutableList<String> ec;

    public static MvnGroupDependency from(Map<String, Object> doc) {
        return MvnGroupDependency.builder()
                .id(string(doc, "id"))
                .group(string(doc, "g"))
                .artifact(string(doc, "a"))
                .latestVersion(string(doc, "latestVersion"))
                .repositoryId(string(doc, "repositoryId"))
                .packaging(string(doc, "p"))
                .timestamp(longValue(doc, "timestamp"))
                .versionCount(integerValue(doc, "versionCount"))
                .text(immutableStringList(doc, "text"))
                .ec(immutableStringList(doc, "ec"))
                .build();
    }

}

package mvn.depgenerator;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

import static mvn.depgenerator.util.MapsHelper.immutableStringList;
import static mvn.depgenerator.util.MapsHelper.longValue;
import static mvn.depgenerator.util.MapsHelper.string;

/**
 * Represent structure for searches that include the group and version number (and optionally artifact).
 *
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
 *   "id":"io.dropwizard:dropwizard-core:0.9.2",
 *   "g":"io.dropwizard",
 *   "a":"dropwizard-core",
 *   "v":"0.9.2",
 *   "p":"jar",
 *   "timestamp":1453280664000,
 *   "tags":["dropwizard","performance","developing","java","friendly","restful","applications","high","framework"],
 *   "ec":["-javadoc.jar","-sources.jar",".jar",".pom"]
 * }
 * </pre>
 */
@Getter
@ToString
@EqualsAndHashCode
@Builder
public class MvnDependency {

    private final String id;
    private final String group;
    private final String artifact;
    private final String version;
    private final String packaging;
    private final Long timestamp;
    private final ImmutableList<String> tags;
    private final ImmutableList<String> ec;

    public String toXml() {
        return String.format("<dependency>\n"
                        + "  <groupId>%s</groupId>\n"
                        + "  <artifactId>%s</artifactId>\n"
                        + "  <version>%s</version>\n"
                        + "  <type>%s</type>\n"
                        + "</dependency>\n",
                group, artifact, version, packaging
        );
    }

    public static MvnDependency from(Map<String, Object> doc) {
        return MvnDependency.builder()
                .id(string(doc, "id"))
                .group(string(doc, "g"))
                .artifact(string(doc, "a"))
                .version(string(doc, "v"))
                .packaging(string(doc, "p"))
                .timestamp(longValue(doc, "timestamp"))
                .tags(immutableStringList(doc, "tags"))
                .ec(immutableStringList(doc, "ec"))
                .build();
    }

    public static MvnDependency from(MvnGroupDependency groupDependency) {
        return MvnDependency.builder()
                .id(groupDependency.getId())
                .group(groupDependency.getGroup())
                .artifact(groupDependency.getArtifact())
                .version(groupDependency.getLatestVersion())
                .packaging(groupDependency.getPackaging())
                .timestamp(groupDependency.getTimestamp())
                .tags(ImmutableList.of())
                .ec(groupDependency.getEc())
                .build();
    }

}

package mvn.depgenerator;

import java.util.List;

import static java.util.stream.Collectors.joining;

public class DependencyGenerator {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String SEPARATOR = String.format("%s%s", LINE_SEPARATOR, System.lineSeparator());

    public static void main(String[] args) {
        String group = null;
        String version = null;
        if (args.length == 2) {
            group = args[0];
            version = args[1];
        } else {
            System.out.printf("Usage: java %s <groupId> <version>%n", DependencyGenerator.class.getSimpleName());
            System.out.println();
            System.out.printf("Example: java %s io.dropwizard 0.9.2%n", DependencyGenerator.class.getSimpleName());
            System.exit(1);
        }

        System.out.printf("Group: %s%n", group);
        System.out.printf("Version: %s%s", version, SEPARATOR);

        List<MvnDependency> mvnDeps = DependencySearcher.getMvnDependencies(group, version);
        System.out.printf("Num dependencies: %d%s", mvnDeps.size(), SEPARATOR);

        String artifactIds = mvnDeps.stream().map(MvnDependency::getArtifact).sorted().collect(joining(System.lineSeparator()));
        System.out.printf("Artifacts:%n%s%s", artifactIds, SEPARATOR);

        String depsXml = DependencySearcher.getPomDependencyXml(mvnDeps);
        System.out.println("POM dependency XML:");
        System.out.println(depsXml);
    }

}

package mvn.depgenerator;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class DependencyGenerator {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String SEPARATOR = String.format("%s%s", LINE_SEPARATOR, LINE_SEPARATOR);

    public static void main(String[] args) {
        String group = null;
        Optional<String> version = Optional.empty();
        if (args.length == 1) {
            group = args[0];
        } else if (args.length == 2) {
            group = args[0];
            version = Optional.of(args[1]);
        } else {
            String className = DependencyGenerator.class.getSimpleName();
            System.out.printf("Usage: java %s <groupId> [version]%n", className);
            System.out.println();
            System.out.printf("Example with group:%njava %s io.dropwizard%n%n", className);
            System.out.printf("Example with group and version:%njava %s io.dropwizard 0.9.2%n", className);
            System.exit(1);
        }

        try {
            if (version.isPresent()) {
                generateDeps(group, version.get());
            } else {
                generateDeps(group);
            }
        } finally {
            DependencySearcher.close();
        }
    }

    private static void generateDeps(String group, String version) {
        System.out.printf("Group: %s%n", group);
        System.out.printf("Version: %s%s", version, SEPARATOR);

        List<MvnDependency> mvnDeps = DependencySearcher.getMvnDependencies(group, version);
        System.out.printf("Num dependencies: %d%s", mvnDeps.size(), SEPARATOR);

        String artifactIds = mvnDeps.stream()
                .map(MvnDependency::getArtifact)
                .sorted()
                .collect(joining(LINE_SEPARATOR));
        System.out.printf("Artifacts:%n%s%s", artifactIds, SEPARATOR);

        String depsXml = DependencySearcher.getPomDependencyXml(mvnDeps);
        System.out.println("POM dependency XML:");
        System.out.println(depsXml);
    }

    private static void generateDeps(String group) {
        System.out.printf("Group: %s%s", group, SEPARATOR);

        List<MvnGroupDependency> mvnGroupDeps = DependencySearcher.getMvnDependencies(group);
        System.out.printf("Num dependencies: %d%s", mvnGroupDeps.size(), SEPARATOR);

        List<MvnDependency> mvnDeps = mvnGroupDeps.stream().map(MvnDependency::from).collect(toList());

        String artifactAndVersions = mvnDeps.stream()
                .map(DependencyGenerator::artifactAndVersion)
                .sorted()
                .collect(joining(LINE_SEPARATOR));
        System.out.printf("Artifacts:%n%s%s", artifactAndVersions, SEPARATOR);

        String depsXml = DependencySearcher.getPomDependencyXml(mvnDeps);
        System.out.println("POM dependency XML:");
        System.out.println(depsXml);
    }

    private static String artifactAndVersion(MvnDependency mvnDep) {
        return mvnDep.getArtifact() + ":" + mvnDep.getVersion();
    }

}

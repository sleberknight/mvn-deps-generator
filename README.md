# Maven Dependency Generator

A small utility to generate a bunch of Maven dependencies that can be pasted into your POM file in order to quickly
grab a bunch of related dependencies. Uses [Maven Central Search](http://search.maven.org/) API to perform the searches.

## Building

Package into a JAR file:

```
mvn clean package
```

This creates a shaded (using the Maven Shade plugin) JAR that can be easily run from the command line. The built JAR
resides in the `target` directory.

## Using

Assuming you've built the JAR, run it with no args to get the usage:

```
$ java -jar target/mvn-deps-generator-0.1.0.jar
Usage: java DependencyGenerator <groupId> [version]

Example with group:
java DependencyGenerator io.dropwizard

Example with group and version:
java DependencyGenerator io.dropwizard 0.9.2
```

### Generate dependencies for a specific group

If you specify only the group, the generator finds all the _latest_ dependencies for the given group.
For example:

```
$ java -jar target/mvn-deps-generator-0.1.0.jar io.dropwizard
```

will find the latest dependencies in the `io.dropwizard` group. This means some dependencies may actually
have different versions.

### Generate dependencies having a specific group and version

You can specific both a group and version, in which case all dependencies will be generated for that
specific group and version. For example:

```
$ java -jar target/mvn-deps-generator-0.1.0.jar io.dropwizard 0.9.2
```

finds all `io.dropwizard` dependencies that have version `0.9.2` only.
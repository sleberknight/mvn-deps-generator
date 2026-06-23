# Maven Dependency Generator

A small utility to generate a bunch of Maven dependencies that can be pasted into your POM file to quickly
grab a bunch of related dependencies. Uses [Maven Central Search](https://central.sonatype.com/search) API
to perform the searches.

## Requirements

- Java 25 or later
- GraalVM 25 (only required for native binary builds)

## Installing

The install script builds and installs the tool in one step. By default it installs a JAR-based wrapper
to `~/.local/bin`. Pass `-n` to build and install a self-contained GraalVM native binary instead.

```
# Install JAR-based wrapper (requires Java 25 at runtime)
./etc/install.sh

# Install native binary (no JVM required at runtime)
./etc/install.sh -n

# Uninstall
./etc/install.sh -u
```

The script auto-detects a suitable Java 25 (or GraalVM 25 for native builds) via SDKMAN if your
current JDK is not Java 25. To override the install directory:

```
INSTALL_DIR=/usr/local/bin ./etc/install.sh
```

## Building manually

Build the shaded JAR:

```
mvn clean package
```

Build the GraalVM native binary:

```
mvn -Pnative clean package
```

Both artifacts land in the `target` directory.

## Using

Run with no arguments to see usage:

```
$ mvn-deps-generator
Usage: mvn-deps-generator <groupId> [version]

Example with group:
mvn-deps-generator io.dropwizard

Example with group and version:
mvn-deps-generator io.dropwizard 5.0.2
```

### Generate dependencies for a specific group

If you specify only the group, the generator finds all the _latest_ dependencies for the given group.
For example:

```
$ mvn-deps-generator io.dropwizard
```

This will find the latest dependencies in the `io.dropwizard` group. This means some dependencies may
actually have different versions.

### Generate dependencies having a specific group and version

You can specify both a group and a version, in which case all dependencies will be generated for that
specific group and version. For example:

```
$ mvn-deps-generator io.dropwizard 5.0.2
```

This finds all `io.dropwizard` dependencies that have version `5.0.2` only.

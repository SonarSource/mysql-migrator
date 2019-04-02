SonarQube MySQL Database Migrator
=================================

Copy SonarQube database from MySQL to non-MySQL.

### Using

Download the latest release and unzip it. Run with `-help` to get usage instructions.

Required parameters:

- `-source PATH` where `PATH` is the path to the configuration file of the source database.
- `-target PATH` where `PATH` is the path to the configuration file of the target database.

The configuration files use the same format as `sonar.properties` file in a SonarQube installation, for example:

    sonar.jdbc.url = jdbc:mysql://localhost:3306/sonar? useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useConfigs=maxPerformance&useSSL=false
    sonar.jdbc.username = sonar
    sonar.jdbc.password = sonar

You could even use directly the path to a `sonar.properties` file of a SonarQube instance.

**Warning:** do not run the migration on live SonarQube instances.

The source database must be a MySQL database, and the target database must be a non-MySQL database.

The program performs various sanity checks before copying data, and prints diagnostics about its progress. It stops on the first error. Read the error messages carefully.

### Using with Oracle target

The Oracle database driver is not included in the distribution, you must provide it yourself.

TODO

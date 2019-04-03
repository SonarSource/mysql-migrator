SonarQube MySQL Database Migrator
=================================

You can use the SonarQube MySQL Database Migrator to copy your SonarQube database from MySQL to a non-MySQL database.

### Prerequisites

- SonarQube version 6.7LTS to 7.8
- Currently using MySQL

### Migrating your database

**Warning:** do not run migration on live SonarQube instances.

To copy your SonarQube database from MySQL to a non-MySQL database, follow these steps: 

1. Download the latest release of the migrator and unzip it. 

2. Start then stop your SonarQube version on your non-MySQL database (for example, if you're currently using SonarQube 7.3, run then stop SonarQube 7.3 on your non-MySQL database).

3. Run the migrator with `-help` to get usage instructions.

Required parameters:

- `-source PATH` where `PATH` is the path to the configuration file of the source database.
- `-target PATH` where `PATH` is the path to the configuration file of the target database.

**Note:** The source database must be a MySQL database, and the target database must be a non-MySQL database.

The configuration files use the same format as `sonar.properties` file in a SonarQube installation, and you can even directly use the path to a `sonar.properties` file of a SonarQube instance. Your configuration files should be formatted as follows:

    sonar.jdbc.url = jdbc:mysql://localhost:3306/sonar? useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useConfigs=maxPerformance&useSSL=false
    sonar.jdbc.username = sonar
    sonar.jdbc.password = sonar

The migrator performs various sanity checks before copying data and prints diagnostics about its progress. It stops on the first error. Read the error messages carefully.

### Using with Oracle target

The Oracle database driver is not included in the distribution, you must provide it yourself.

TODO

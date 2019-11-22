SonarQube MySQL Database Migrator
=================================

You can use the SonarQube MySQL Database Migrator to copy your SonarQube database from your MySQL (source) database to a non-MySQL (target) database.

### Prerequisites

- SonarQube version 6.7LTS to 7.8  
   **Note:** Your source and target databases must be using the same version of SonarQube.
- Currently using MySQL

### Migrating your database

**Warning:** do not run migration on live SonarQube instances.

To copy your SonarQube database from your source database to the target database, follow these steps: 

1. [Download][download-zip] the latest release of the migrator and unzip it. 

2. Start then stop the same version of SonarQube that you're running on your source database on your target database. For example, if you're running SonarQube 7.3 on your source database, run then stop SonarQube 7.3 on your target database.

3. Delete the Elasticsearch data folder in the target SonarQube instance: `data/es5` on SonarQube 6.7-7.6 and `data/es6` on SonarQube 7.7 and above.

4. Run the migrator with `-help` to get usage instructions.

Required parameters:

- `-source PATH` where `PATH` is the path to the configuration file of your SonarQube instance running MySQL.
- `-target PATH` where `PATH` is the path to the configuration file of your SonarQube instance running a non-MySQL database.

**Note:** The source database must be a MySQL database, and the target database must be a non-MySQL database.

The configuration files use the same format as `sonar.properties` file in a SonarQube installation, and you can even directly use the path to a `sonar.properties` file of a SonarQube instance. Your configuration files should be formatted as follows:

    sonar.jdbc.url = jdbc:mysql://localhost:3306/sonar?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useConfigs=maxPerformance&useSSL=false
    sonar.jdbc.username = sonar
    sonar.jdbc.password = sonar

The migrator performs various sanity checks before copying data and prints diagnostics about its progress. It stops on the first error. Read the error messages carefully.

### Migrating with a Microsoft SQL Server target

Java 8 must be installed on the machine executing the migration, and the `JAVA_HOME` environment variable should be set to its location. 

### Migrating with an Oracle target

The Oracle database driver is not included in the distribution, so you must provide it yourself.

Please download the [JDBC driver for Oracle][oracle-driver] and copy it to the `mysql-migrator/lib`
directory in the unzipped folder of the migration tool. The file must be named `oracle.jar` exactly.

With the Java driver in place, follow the steps above to migrate just like any other database.

[oracle-driver]: https://www.oracle.com/technetwork/database/features/jdbc/jdbc-ucp-122-3110062.html
[download-zip]: https://binaries.sonarsource.com/Distribution/mysql-migrator/mysql-migrator-1.1.0.119.zip

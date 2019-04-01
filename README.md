SonarQube MySQL Database Migrator
=================================

Copy SonarQube database from MySQL to non-MySQL.

### Building

    ./gradlew install

This creates runnable scripts in `./build/install/mysql-migrator/bin`.

### Running

    ./build/install/mysql-migrator/bin/mysql-migrator -help
    ./build/install/mysql-migrator/bin/mysql-migrator -source path/to/config -target path/to/config

The configuration files support configuring database connection in the same format as `sonar.properties` file in a SonarQube installation, for example:

    sonar.jdbc.url = jdbc:mysql://localhost:3306/sonar? useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useConfigs=maxPerformance&useSSL=false
    sonar.jdbc.username = sonar
    sonar.jdbc.password = sonar

You could even use directly the path to a `sonar.properties` file of a SonarQube instance.

**Warning:** do not run the migration on live SonarQube instances.

### Shipping

    ./gradlew distZip
    unzip build/distributions/mysql-migrator-1.0-SNAPSHOT.zip
    ./mysql-migrator-0.0.1-SNAPSHOT/bin/mysql-migrator -help
    ./mysql-migrator-0.0.1-SNAPSHOT/bin/mysql-migrator -source path/to/config -target path/to/config

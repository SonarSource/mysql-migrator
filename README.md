SonarQube MySQL Database Migrator
=================================

Copy SonarQube database from MySQL to non-MySQL.

### Running

    ./gradlew install
    build/install/mysql-migrator/bin/mysql-migrator -help
    build/install/mysql-migrator/bin/mysql-migrator -source path/to/config -target path/to/config

### Shipping

    ./gradlew distZip
    unzip build/distributions/mysql-migrator-1.0-SNAPSHOT.zip
    ./mysql-migrator-0.0.1-SNAPSHOT/bin/mysql-migrator -help
    ./mysql-migrator-0.0.1-SNAPSHOT/bin/mysql-migrator -source path/to/config -target path/to/config

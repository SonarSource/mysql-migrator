SonarQube MySQL Database Migrator
=================================

This document is for developers, see the README.md for end-users.

### Building

    ./gradlew install

This creates runnable scripts in `./build/install/mysql-migrator/bin`.

### Running

    ./build/install/mysql-migrator/bin/mysql-migrator -help
    ./build/install/mysql-migrator/bin/mysql-migrator -source path/to/config -target path/to/config

### Shipping

    ./gradlew distZip
    unzip build/distributions/mysql-migrator-1.0-SNAPSHOT.zip
    ./mysql-migrator-1.0-SNAPSHOT/bin/mysql-migrator -help
    ./mysql-migrator-1.0-SNAPSHOT/bin/mysql-migrator -source path/to/config -target path/to/config

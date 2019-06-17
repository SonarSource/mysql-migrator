SonarQube MySQL Database Migrator
=================================

This document is for developers, see the README.md for end-users.

## Building

    ./gradlew build install

This creates runnable scripts in `./build/install/mysql-migrator/bin`.

## Running

    ./build/install/mysql-migrator/bin/mysql-migrator -help
    ./build/install/mysql-migrator/bin/mysql-migrator -source path/to/config -target path/to/config

## Running ITs

To run ITs locally against different databases, use the helper script `./it/localrun.sh`:

    ./it/localrun.sh path/to/source.properties path/to/target.properties

Beware: the configuration files for ITs require more than just the migration.
Orchestrator needs permission to drop and create databases and users.
For this purpose, depending on the database type, you may need to specify additional properties.

### MySQL

Example configuration file:

    sonar.jdbc.url = jdbc:mysql://localhost:3306/sonar? useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useConfigs=maxPerformance&useSSL=false
    sonar.jdbc.username = sonar
    sonar.jdbc.password = sonar
    sonar.jdbc.rootUsername = root
    sonar.jdbc.rootPassword = rootsonar

Note that JDBC requires network connection, so when testing with a local MySQL, make sure it's configured accordingly (no `skip-networking`).

You also need to enable password authentication for the `root` user of the database.
This was tricky on Ubuntu 2018:

    sudo mysql
    update user set plugin='mysql_native_password' where user='root' and host='localhost';
    flush privileges;
    ALTER USER 'root'@'localhost' IDENTIFIED BY 'rootsonar';
    flush privileges;

### Postgresql

Example configuration file:

    sonar.jdbc.url = jdbc:postgresql://localhost/sonar
    sonar.jdbc.username = sonar
    sonar.jdbc.password = sonar
    sonar.jdbc.rootUrl = jdbc:postgresql://localhost/postgres
    sonar.jdbc.rootUsername = postgres
    sonar.jdbc.rootPassword = rootsonar

Set password for the root user:

    sudo -u postgres psql
    \password postgres

Running the ITs targeting a local postgres database takes about 5 minutes.

### SQL Server

Example configuration file:

    sonar.jdbc.url = jdbc:sqlserver://server:port;databaseName=sonar;SelectMethod=Cursor
    sonar.jdbc.username = sonar
    sonar.jdbc.password = sonar
    sonar.jdbc.rootUrl = jdbc:sqlserver://server:port;SelectMethod=Cursor
    sonar.jdbc.rootUsername = admin
    sonar.jdbc.rootPassword = adminsonar

Running the ITs targeting a SQL Server database on the local network takes about 20 minutes.

### Oracle

Example configuration file:

    sonar.jdbc.url = jdbc:oracle:thin:server:port/db
    sonar.jdbc.username = sonar
    sonar.jdbc.password = sonar
    sonar.jdbc.rootUrl = jdbc:oracle:thin:server:port/db
    sonar.jdbc.rootUsername = system
    sonar.jdbc.rootPassword = systemsonar
    sonar.jdbc.driverFile = /tmp/ojdbc8-12.2.0.1.0.jar

The `sonar.jdbc.driverFile` is required at the moment, by Orchestrator. (It would be great to eliminate this need...)

Running the ITs targeting a Oracle database on the local network takes about 15 minutes.

## Shipping

    ./gradlew distZip
    unzip build/distributions/mysql-migrator-1.0-SNAPSHOT.zip
    ./mysql-migrator-1.0-SNAPSHOT/bin/mysql-migrator -help
    ./mysql-migrator-1.0-SNAPSHOT/bin/mysql-migrator -source path/to/config -target path/to/config

## Adding support for SonarQube 7.x versions

One of the sanity checks before copying a database is to verify if the
source and target databases have the expected tables for the given SonarQube version.
The expected tables per version are hardcoded in `TableListProvider`.

When a new 7.x version is released, for example 7.8,
that version becomes the new recommended version for the 7.x series,
and therefore the hardcoded version and table lists must be updated.

Example steps, for adding 7.8:

    # set shell variable
    tag=7.8

    # baseline check before implementation: observe that in local tests that ITs are failing
    ./gradlew clean build install
    SQ_RUNTIME=$tag ./it/localrun.sh tmp/mysql.properties tmp/postgresql.properties

    # cd to a clone of sonarqube or sonar-enterprise that has $tag

    # run the extract-tables-and-version.sh helper script
    ~/dev/mysql-migrator/scripts/extract-tables-and-version.sh $tag

    # add the generated .version and .tables files to version control

    # run the helper script to generate the java code for the version and tables
    ./scripts/tables-and-version/gen-java.sh

    # copy-paste the relevant lines into TablesAndVersionRegistry and reformat nicely

    # verify in local test
    ./gradlew clean build install
    SQ_RUNTIME=$tag ./it/localrun.sh tmp/mysql.properties tmp/postgresql.properties

## Adding support for SonarQube patch versions

When a patch version is released, for example 7.x.y for a base version 7.x,
that version becomes the new recommended version for the 7.x series,
and therefore the hardcoded version and table lists must be updated,
*if the patch has added new database migrations*.

Follow the same steps as when adding a new 7.x release (see previous section).

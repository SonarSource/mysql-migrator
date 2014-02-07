/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

public class StartApp {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  private StartApp() {
    // empty because only use the static method main
  }

  public static void main(String[] args) {
    String starLine = "****************";
    Database databaseSource = new Database();
    Database databaseDest = new Database();
    ConnecterDatas connecterDatasSource = new ConnecterDatas(args[0], args[1], args[2], args[3]);
    ConnecterDatas connecterDatasDest = new ConnecterDatas(args[4], args[5], args[6], args[7]);


    /* VERIFY CONNECTION */
    LOGGER.info(starLine + " CONFIGURATION VERIFICATIONS " + starLine);
    ConnectionVerifier connectionVerifier = new ConnectionVerifier();
    connectionVerifier.databaseIsReached(connecterDatasSource);
    LOGGER.info("Database SOURCE  has been reached at :         " + connecterDatasSource.getUrl());
    connectionVerifier.databaseIsReached(connecterDatasDest);
    LOGGER.info("Database DESTINATION has been reached at :     " + connecterDatasDest.getUrl());


    /* VERIFY VERSIONS OF SONARQUBE */
    VersionVerifier vvSource = new VersionVerifier();
    int maxVersionIdSource = vvSource.lastVersionId(connecterDatasSource);
    VersionVerifier vvDest = new VersionVerifier();
    int maxVersionIdDestination = vvDest.lastVersionId(connecterDatasDest);
    if (maxVersionIdSource != maxVersionIdDestination && maxVersionIdDestination != 0) {
      throw new DbException("Version of schema migration are not the same between source (" + maxVersionIdSource + ") and destination (" + maxVersionIdDestination + ").", new Exception());
    } else if (maxVersionIdDestination == 0) {
      LOGGER.info("WARNING !! The versions of SonarQube schema migration destination is (" + maxVersionIdDestination + ") when source is (" + maxVersionIdSource + ").");
    } else {
      LOGGER.info("WELL DONE !! The versions of SonarQube schema migration are the same between source (" + maxVersionIdSource + ") and destination (" + maxVersionIdDestination + ").");
    }
    LOGGER.info(starLine + " SEARCH TABLES FROM SOURCE " + starLine);


    /* GET METADATA FROM SOURCE AND FROM DESTINATION */
    MetadataGetter metadataGetterSource = new MetadataGetter(connecterDatasSource, databaseSource);
    metadataGetterSource.execute();

    LOGGER.info(starLine + " SEARCH TABLES FROM DESTINATION " + starLine);

    MetadataGetter metadataGetterDest = new MetadataGetter(connecterDatasDest, databaseDest);
    metadataGetterDest.execute();

    LOGGER.info(starLine + " DELETE TABLES FROM DESTINATION " + starLine);

    /* DELETE TABLE CONTENT OF DATABASE DESTINATION */
    Deleter deleter = new Deleter(connecterDatasDest, databaseSource);
    deleter.execute(databaseDest);

    LOGGER.info(starLine + " COPY DATA " + starLine);

    /* COPY DATA FROM SOURCE TO DESTINATION */
    Reproducer reproducer = new Reproducer(connecterDatasSource, connecterDatasDest, databaseSource, databaseDest);
    reproducer.execute(databaseDest);

    LOGGER.info(starLine + " SEARCH FOR MISTAKES " + starLine);

    /* FIND AND DISPLAY TABLES PRESENT IN DESTINATION BUT NOT IN SOURCE */
    DatabaseComparer dbComparer = new DatabaseComparer(databaseSource);
    for (int indexTable = 0; indexTable < databaseSource.getNbTables(); indexTable++) {
      String tableNameDest = databaseDest.getTableName(indexTable);
      if (!dbComparer.tableExistsInDestinationDatabase(tableNameDest)) {
        LOGGER.warn("TABLE " + tableNameDest + " IN DESTINATION WAS NOT COPIED BECAUSE IT IS NOT PRESENT IN DATABASE SOURCE.");
      }
    }

    LOGGER.info("** THE COPY HAS FINISHED SUCCESSFULLY **");
    LOGGER.info(starLine + starLine + starLine);

  }
}

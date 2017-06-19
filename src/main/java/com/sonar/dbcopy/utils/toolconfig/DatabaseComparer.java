/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.data.Table;
import org.slf4j.LoggerFactory;

public class DatabaseComparer {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  public void displayAllTablesFoundIfExists(Database dbSource, Database dbDestination) {
    int nbMissingTableInDest = 0;

    // DISPLAY SOURCE TABLES
    for (int indexTable = 0; indexTable < dbSource.getNbTables(); indexTable++) {
      Table tableSource = dbSource.getTable(indexTable);
      String tableSourceName = tableSource.getName();
      LOGGER.info("");
      LOGGER.info("FOUND TABLE : {}", tableSourceName);
      LOGGER.info("   SOURCE:");
      displayTableContent(tableSource);
      // DISPLAY DESTINATION TABLE IF EXISTS
      Table tableDestToFind = dbDestination.getTableByName(tableSourceName);
      LOGGER.info("   DESTINATION:");
      if (tableDestToFind == null) {
        LOGGER.warn("TABLE {} is not present in the DESTINATION database.", dbSource.getTableName(indexTable));
        nbMissingTableInDest++;
      } else {
        displayTableContent(tableDestToFind);
      }
    }

    // FIND TABLE IN DESTINATION THAT DOES NOT EXIST IN SOURCE AND DISPLAY IT
    if (dbSource.getNbTables() == dbDestination.getNbTables() && nbMissingTableInDest == 0) {
      // DO NOTHING IN THE CASE OF DATABASES ARE IDENTICAL (SAME NB OF TABLES AND NO MISSING TABLE)
    } else {
      // DISPLAY TABLE THAT EXISTS IN DESTINATION BUT NOT IN SOURCE
      for (int indexTable = 0; indexTable < dbDestination.getNbTables(); indexTable++) {
        Table tableSourceToFind = dbSource.getTableByName(dbDestination.getTableName(indexTable));
        if (tableSourceToFind == null) {
          LOGGER.info("");
          LOGGER.info("FOUND TABLE : {}", dbDestination.getTableName(indexTable));
          LOGGER.warn("TABLE {} is not present in the SOURCE database.", dbDestination.getTableName(indexTable));
          LOGGER.info("   DESTINATION:");
          displayTableContent(dbDestination.getTable(indexTable));
        }
      }
    }
  }

  private static void displayTableContent(Table tableToDisplay) {
    LOGGER.info("         COLUMNS : ({})", tableToDisplay.getColumnNamesAsString());
    LOGGER.info("           TYPES : ({})", tableToDisplay.getTypesAsString());
  }

  public void displayMissingTableInDb(Database completeDb, Database dbToEvaluate, String sourceOrDestination) {
    Table realTable;
    Table missingTableToFind;
    for (int indexTable = 0; indexTable < completeDb.getNbTables(); indexTable++) {
      realTable = completeDb.getTable(indexTable);
      missingTableToFind = dbToEvaluate.getTableByName(realTable.getName());
      if (missingTableToFind == null) {
        LOGGER.warn("TABLE {} is not present in the {} database and have not been copied.", completeDb.getTableName(indexTable), sourceOrDestination);
      }
    }
  }

  public void displayDiffNumberRows(Database dbSource, Database dbDest) {
    for (int indexTable = 0; indexTable < dbSource.getNbTables(); indexTable++) {
      Table tableSource = dbSource.getTable(indexTable);
      Table tableDest = dbDest.getTableByName(tableSource.getName());
      if (tableDest != null && tableDest.getNbRows() != tableSource.getNbRows()) {
        LOGGER.warn("TABLE " + tableSource.getName() + " has " + tableSource.getNbRows() + " ROWS in SOURCE while " + tableDest.getNbRows() + " in DESTINATION");
      }
    }
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
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
      LOGGER.info("");
      LOGGER.info("FOUND TABLE : " + tableSource.getName());
      LOGGER.info("   SOURCE:");
      displayTableContent(tableSource);
      // DISPLAY DESTINATION TABLE IF EXISTS
      Table tableDestToFind = findTableByNameInDb(dbDestination, dbSource.getTableName(indexTable));
      if (tableDestToFind == null) {
        LOGGER.warn("   DESTINATION:  WARNING !! TABLE " + dbSource.getTableName(indexTable) + " is not present in the DESTINATION database.");
        nbMissingTableInDest++;
      } else {
        LOGGER.info("   DESTINATION:");
        displayTableContent(tableDestToFind);
      }
    }

    // FIND TABLE IN DESTINATION THAT DOES NOT EXIST IN SOURCE AND DISPLAY IT
    if (dbSource.getNbTables() == dbDestination.getNbTables() && nbMissingTableInDest == 0) {
      // DO NOTHING IN THE CASE OF DATABASES ARE IDENTICAL (SAME NB OF TABLES AND NO MISSING TABLE)
    } else {
      // DISPLAY TABLE THAT EXISTS IN DESTINATION BUT NOT IN SOURCE
      for (int indexTable = 0; indexTable < dbDestination.getNbTables(); indexTable++) {
        Table tableSourceToFind = findTableByNameInDb(dbSource, dbDestination.getTableName(indexTable));
        if (tableSourceToFind == null) {
          LOGGER.info("");
          LOGGER.info("FOUND TABLE : " + dbDestination.getTableName(indexTable));
          LOGGER.warn("   SOURCE:  WARNING!! TABLE " + dbDestination.getTableName(indexTable) + " is not present in the SOURCE database.");
          LOGGER.info("   DESTINATION:");
          displayTableContent(dbDestination.getTable(indexTable));
        }
      }
    }
  }

  public Table findTableByNameInDb(Database dbToExplore, String tableNameToFind) {
    Table tableToFindIfExists = null;
    for (int indexTable = 0; indexTable < dbToExplore.getNbTables(); indexTable++) {
      if (tableNameToFind.equals(dbToExplore.getTableName(indexTable))) {
        tableToFindIfExists = dbToExplore.getTable(indexTable);
      }
    }
    return tableToFindIfExists;
  }

  private void displayTableContent(Table tableToDisplay) {
    ListColumnsAsString lcas = new ListColumnsAsString(tableToDisplay);
    LOGGER.info("         COLUMNS : (" + lcas.makeColumnString() + ")");
    LOGGER.info("           TYPES : (" + lcas.makeStringOfTypes() + ")");
  }

  public void displayMissingTableInDb(Database fullDb, Database missDb, String missDbStatus) {
    Table realTable, missTableToFind;
    for (int indexTable = 0; indexTable < fullDb.getNbTables(); indexTable++) {
      realTable = fullDb.getTable(indexTable);
      missTableToFind = findTableByNameInDb(missDb, realTable.getName());
      if (missTableToFind == null) {
        LOGGER.warn("TABLE " + fullDb.getTableName(indexTable) + " is not present in the " + missDbStatus + " database. Have a look to the logs \"FOUND TABLES\" upper.");
      }
    }
  }

  public void displayDiffNumberRows(Database dbSource, Database dbDest) {
    for (int indexTable = 0; indexTable < dbSource.getNbTables(); indexTable++) {
      Table tableSource = dbSource.getTable(indexTable);
      Table tableDest = dbDest.getTableByName(tableSource.getName());
      if (tableDest.getNbRows() != tableSource.getNbRows()) {
        LOGGER.warn("TABLE " + tableSource.getName() + " add " + tableSource.getNbRows() + " ROWS in SOURCE while " + tableDest.getNbRows() + " in DESTINATION");
      }
    }
  }
}

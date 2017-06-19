/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.Connecter;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Deleter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterData cdDest;
  private Database databaseSource;

  public Deleter(ConnecterData cdDest, Database databaseSource) {
    this.cdDest = cdDest;
    this.databaseSource = databaseSource;
  }

  public void execute(Database databaseDest) {
    LOGGER.info("START DELETING...");
    Closer closer = new Closer("Deleter");
    String tableNameSource = null;
    Statement statementToDelete = null;
    Connection connectionDest = null;
    int nbTablesDeleted = 0;
    try {
      connectionDest = new Connecter().doConnection(cdDest);
      statementToDelete = connectionDest.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

      // DELETING DESTINATION TABLE FROM SOURCE DATABASE TABLE LIST ONLY WHEN IT IS PRESENT IN DESTINATION
      for (int indexTable = 0; indexTable < this.databaseSource.getNbTables(); indexTable++) {
        tableNameSource = this.databaseSource.getTableName(indexTable);
        if (databaseDest.getTableByName(tableNameSource) != null) {
          statementToDelete.execute(String.format("TRUNCATE TABLE %s", tableNameSource));
          nbTablesDeleted++;
        } else {
          LOGGER.warn("Can't DELETE  TABLE :" + tableNameSource + " because it doesn't exist in destination database. ");
        }
      }
      LOGGER.info("  " + nbTablesDeleted + " TABLES DELETED IN DESTINATION.");
      closer.closeStatement(statementToDelete);

    } catch (SQLException e) {
      throw new SqlDbCopyException("Deleting datas from destination failed for TABLE : " + tableNameSource + " .", e);
    } finally {
      closer.closeStatement(statementToDelete);
      closer.closeConnection(connectionDest);
    }
  }
}

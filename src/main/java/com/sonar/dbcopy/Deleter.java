/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Deleter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterDatas cdDest;
  private Database databaseSource;

  public Deleter(ConnecterDatas cdDest, Database dbS) {
    this.cdDest = cdDest;
    this.databaseSource = dbS;
  }

  public void execute(Database databaseDest) {
    Closer closer = new Closer("Deleter");
    DatabaseComparer dbComparer = new DatabaseComparer(databaseDest);
    String tableName = null;
    Statement statementToDelete = null;
    Connection connectionDest = null;
    try {
      connectionDest = new Connecter().doConnection(cdDest);
      statementToDelete = connectionDest.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

      for (int indexTable = 0; indexTable < this.databaseSource.getNbTables(); indexTable++) {
        tableName = this.databaseSource.getTableName(indexTable);
        if (dbComparer.tableExistsInDestinationDatabase(tableName)) {
          statementToDelete.execute("DELETE FROM " + tableName);
          LOGGER.info("DELETE: " + indexTable + "   " + tableName);
        } else {
          LOGGER.error("WARNING !! Can't DELETE  TABLE :" + tableName + " because it doesn't exist in destination database. ");
        }
      }
      closer.closeStatement(statementToDelete);

    } catch (SQLException e) {
      throw new DbException("Deleting datas from destination failed for TABLE : " + tableName + " .", e);
    } finally {
      closer.closeStatement(statementToDelete);
      closer.closeConnection(connectionDest);
      LOGGER.info("Everything is closed in Deleter.");
    }
  }
}

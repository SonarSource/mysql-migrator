/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.data.ConnecterDatas;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.Connecter;
import com.sonar.dbcopy.utils.toolconfig.DbException;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Deleter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterDatas cdDest;
  private Database databaseSource;

  public Deleter(ConnecterDatas cdDest, Database databaseSource) {
    this.cdDest = cdDest;
    this.databaseSource = databaseSource;
  }

  public void execute(Database databaseDest) {
    LOGGER.info("START DELETING...");
    Closer closer = new Closer("Deleter");
    String tableNameSource = null;
    Statement statementToDelete = null;
    Connection connectionDest = null;
    try {
      connectionDest = new Connecter().doConnection(cdDest);
      statementToDelete = connectionDest.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

      // DELETING DESTINATION TABLE FROM SOURCE DATABASE TABLE LIST ONLY WHEN IT IS PRESENT IN DESTINATION
      for (int indexTable = 0; indexTable < this.databaseSource.getNbTables(); indexTable++) {
        tableNameSource = this.databaseSource.getTableName(indexTable);
        if (databaseDest.getTableByName(tableNameSource) != null) {
          statementToDelete.execute("TRUNCATE TABLE " + tableNameSource);
        } else {
          LOGGER.error("WARNING !! Can't DELETE  TABLE :" + tableNameSource + " because it doesn't exist in destination database. ");
        }
      }
      closer.closeStatement(statementToDelete);

    } catch (SQLException e) {
      throw new DbException("Deleting datas from destination failed for TABLE : " + tableNameSource + " .", e);
    } finally {
      closer.closeStatement(statementToDelete);
      closer.closeConnection(connectionDest);
    }
  }
}

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
  private ConnecterDatas dcDest;
  private Database database;

  public Deleter(ConnecterDatas dcDest, Database db) {
    this.dcDest = dcDest;
    this.database = db;
  }

  public void execute() {
    Statement statementToDelete = null;
    Connection connectionDest = null;
    try {
      connectionDest = new Connecter().doConnection(dcDest);
      statementToDelete = connectionDest.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

      for (int indexTable = 0; indexTable < this.database.getNbTables(); indexTable++) {
        statementToDelete.execute("DELETE FROM " + this.database.getTableName(indexTable));
        LOGGER.info("DELETE: " + indexTable + "   " + this.database.getTableName(indexTable));
      }
      statementToDelete.close();

    } catch (SQLException e) {
      throw new DbException("Deleting datas from destination failed.", e);
    } finally {
      try {
        if(statementToDelete!=null){
          statementToDelete.close();
        }
      } catch (SQLException e) {
        LOGGER.error("Statement destination can not be closed or is already closed in Deleter." + e);
      }
      try {
        if(connectionDest!=null){
          connectionDest.close();
        }
      } catch (SQLException e) {
        LOGGER.error("Connection destination can not be closed or is already closed in Deleter." + e);
      }
      LOGGER.info("Everything is closed in Deleter.");
    }
  }
}

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
  private ConnecterDatas dc;
  private Database database;

  public Deleter(ConnecterDatas dc, Database db) {
    this.dc = dc;
    this.database = db;
  }

  public void execute() {
    Statement statementToDelete = null;
    Connection connectionDest = null;
    try {
      connectionDest = new Connecter().doDestinationConnection(this.dc);
      statementToDelete = connectionDest.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

      for (int indexTable = 0; indexTable < this.database.getNbTables(); indexTable++) {
        statementToDelete.execute("DELETE FROM " + this.database.getTableName(indexTable));
        LOGGER.info("DELETED                                                : " + indexTable + "   " + this.database.getTableName(indexTable));
      }
      statementToDelete.close();

    } catch (SQLException e) {
      throw new DbException("Deleting datas from destination failed.", e);
    } finally {
      LOGGER.info(" | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -| ");

      try {
        statementToDelete.close();
      } catch (Exception e) {
        LOGGER.info("Statement to delete datas from database destination can not be closed or is already closed.       | " + e);
      }

      try {
        connectionDest.close();
      } catch (Exception e) {
        LOGGER.info("ConnectionDest  to delete datas from database destination can not be closed or is already closed. | " + e);
      }

      LOGGER.info(" | Everything is closed in Deleter.                                                               | ");
      LOGGER.info(" | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -| ");
    }
  }
}

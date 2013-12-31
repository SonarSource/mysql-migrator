/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopybythread;

import com.sonar.dbcopyutils.Database;
import com.sonar.dbcopyutils.DbException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableToDelete implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private DataConnecterByThread dc;
  private Database database;

  public RunnableToDelete(DataConnecterByThread dc, Database db) {
    this.dc = dc;
    this.database = db;
  }

  public void run() {
    Statement statementToDelete = null;
    Connection connectionDest = null;
    try {
      connectionDest = new ConnecterByThread().doDestinationConnection(this.dc);
      statementToDelete = connectionDest.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

      for (int indexTable = 0; indexTable < this.database.getNbTables(); indexTable++) {
        while(!database.getTable(indexTable).getIsBuilt()){
          //waiting for table building end
          System.out.println("WAITING FOR TABLE");  //TODO TO DELETE
        }
        statementToDelete.execute("DELETE FROM " + this.database.getTableName(indexTable));
        LOGGER.log(Level.INFO,"DELETED   ******   ******* : "+indexTable+"   "+ this.database.getTableName(indexTable));
      }
      statementToDelete.close();
    } catch (SQLException e) {
      throw new DbException("Deleting datas from destination failed.", e);
    } finally {
      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -| ");

      try {
        statementToDelete.close();
        LOGGER.log(Level.INFO, " | StatementToDelete is closed.                                                                   | ");
      } catch (Exception e) {
        LOGGER.log(Level.INFO, "Statement to delete datas from database destination can not be closed or is already closed.       | "+e);
      }

      try {
        connectionDest.close();
        LOGGER.log(Level.INFO, " | ConnectionDest is closed.                                                                      | ");
      } catch (Exception e) {
        LOGGER.log(Level.INFO, "ConnectionDest  to delete datas from database destination can not be closed or is already closed. | "+e);
      }

      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -| ");

    }
  }
}

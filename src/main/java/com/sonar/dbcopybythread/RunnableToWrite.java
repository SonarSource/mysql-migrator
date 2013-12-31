/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopybythread;

import com.sonar.dbcopyutils.Column;
import com.sonar.dbcopyutils.Database;
import com.sonar.dbcopyutils.DbException;
import com.sonar.dbcopyutils.ListColumnsAsString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableToWrite implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private DataConnecterByThread dc;
  private Database database;

  public RunnableToWrite(DataConnecterByThread dc, Database database) {
    this.dc = dc;
    this.database = database;
  }

  public void run() {
    Connection connectionDest = null;
    PreparedStatement preparedStatement = null;
    String tableName = null;
    Object objectToInsert = null;
    int counterOfRows;
    int commit;

    try {
      connectionDest = new ConnecterByThread().doDestinationConnection(dc);
      connectionDest.setAutoCommit(false);

      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        counterOfRows =0;
        commit=0;
    /* GET INFORMATION FROM EACH TABLE  */
        tableName = database.getTableName(indexTable);
        int nbColInTable = database.getNbColumnsInTable(indexTable);
        int nbRowsInTable = database.getTable(indexTable).getNbRows();
        List<Column> columns = database.getTable(indexTable).getColumns();

    /* MAKE STRINGS TO PUT IN SQL INSERT REQUEST */
        ListColumnsAsString lcas = new ListColumnsAsString(columns);
        String columnsAsString = lcas.makeColumnString();
        String questionMarkString = lcas.makeQuestionMarkString();
        String sqlRequest = "INSERT INTO " + tableName + " (" + columnsAsString + ") VALUES(" + questionMarkString + ");";

    /* MAKE STATEMENTS  */
        preparedStatement = connectionDest.prepareStatement(sqlRequest);

    /* ADD EACH OBJECT BY ROW */
        for (int indexRow = 0; indexRow < database.getTable(indexTable).getNbRows(); indexRow++) {
          counterOfRows++;
          for (int indexColumn = 0; indexColumn < database.getNbColumnsInTable(indexTable); indexColumn++) {
            while (objectToInsert == null) {
              objectToInsert = database.getColumn(indexTable, indexColumn).pullData();
            }
            if (objectToInsert.equals("null")) {
              preparedStatement.setObject(indexColumn + 1, null);
            } else {
              preparedStatement.setObject(indexColumn + 1, objectToInsert);
            }
            objectToInsert = null;
          }// for indexColumn
          preparedStatement.executeUpdate();
          if (counterOfRows > 10000 * commit) {
            LOGGER.log(Level.INFO, "*******   ******   WRITTING... : "+indexTable+"   "+tableName+" LINES "+ counterOfRows +" / "+nbRowsInTable);
            connectionDest.commit();
            commit++;
          }
        }//for indexRow

        preparedStatement.close();
        LOGGER.log(Level.INFO, "*******   ******   WRITTEN : "+indexTable+"   "+tableName);
      }//for indextable
      connectionDest.commit();

    } catch (Exception e) {
      throw new DbException("Problem to set autocommit in RunnableToWrite ", e);
    } finally {
      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -| ");

      try {
        preparedStatement.close();
        LOGGER.log(Level.INFO, " | PreparedStatement to write in destination is closed.                             | ");
      } catch (SQLException e) {
        LOGGER.log(Level.INFO, "PreparedStatement to write in destination can not be closed or is already closed.   |");
      }

      try {
        connectionDest.close();
        LOGGER.log(Level.INFO, " | ConnectionDest is closed.                                                        | ");
      } catch (SQLException e) {
        LOGGER.log(Level.INFO, "ConnectionDest  to write in destination can not be closed or is already closed.     |");
      }

      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -|");

    }
  }
}

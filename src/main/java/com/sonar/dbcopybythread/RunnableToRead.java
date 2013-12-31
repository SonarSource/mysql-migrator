/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopybythread;

import com.sonar.dbcopyutils.Column;
import com.sonar.dbcopyutils.Database;
import com.sonar.dbcopyutils.DbException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableToRead implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private DataConnecterByThread dc;
  private Database database;

  public RunnableToRead(DataConnecterByThread dc, Database database) {
    this.dc = dc;
    this.database = database;
  }

  public void run() {
    Connection connectionSource = null;
    Statement sourceStatement = null;
    ResultSet resultSet = null;
    boolean datarecorded;
    int lineWritten, blockByTenThousand;
    try {
      connectionSource = new ConnecterByThread().doSourceConnection(dc);
      connectionSource.setAutoCommit(false);

      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
       lineWritten=0;
       blockByTenThousand=0;
    /* GET INFORMATION FROM EACH TABLE  */
        String tableName = database.getTableName(indexTable);
        int nbColInTable = database.getNbColumnsInTable(indexTable);
        int nbRowsInTable = database.getTable(indexTable).getNbRows();
        List<Column> columns = database.getTable(indexTable).getColumns();

    /* MAKE STATEMENTS  */
        sourceStatement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

    /* GET RESULTSET FROM SOURCE */
        //TODO Integer.MIN_VALUE to modify (?) for Oracle et sqlserver
        sourceStatement.setFetchSize(Integer.MIN_VALUE);
        resultSet = sourceStatement.executeQuery("SELECT * FROM " + tableName);

    /* ITERATE ON SOURCE RESULTSET */
        while (resultSet.next()) {
          lineWritten++;
          for (int indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
            Object objectGetted = resultSet.getObject(indexColumn + 1);
            datarecorded = false;
            while (!datarecorded) {
              if (objectGetted == null) {
                datarecorded = database.getColumn(indexTable, indexColumn).putData("null");
              } else {
                datarecorded = database.getColumn(indexTable, indexColumn).putData(objectGetted);
              }
              if(lineWritten>10000* blockByTenThousand){
                LOGGER.log(Level.INFO, "*******   READING...   ******* : "+indexTable+"   "+tableName+" LINES "+lineWritten+" / "+nbRowsInTable);
                blockByTenThousand++;
              }
            }
          }
        }
        resultSet.close();
        sourceStatement.close();
        LOGGER.log(Level.INFO, "*******   READED   ******* : "+indexTable+"   "+tableName);

      }
    } catch (Exception e) {
      throw new DbException("problem when reading datas from source in Reproducer by thread", e);
    } finally {
      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - | ");
      try {
        resultSet.close();
        LOGGER.log(Level.INFO, " | ResultSet to read datas from source is closed.                                  | ");
      } catch (SQLException e) {
        LOGGER.log(Level.INFO, " | ResultSet to read datas from source can't be closed or is already closed.       | ");
      }

      try {
        sourceStatement.close();
        LOGGER.log(Level.INFO, " | SourceStatement to read datas from source is closed.                            | ");
      } catch (SQLException e) {
        LOGGER.log(Level.INFO, " | SourceStatement to read datas from source can't be closed or is already closed. | ");
      }

      try {
        connectionSource.close();
        LOGGER.log(Level.INFO, " | ConnectionSource to read datas from source is closed.                           | ");
      } catch (SQLException e) {
        LOGGER.log(Level.INFO, " | ConnectionSource to read datas from source can't be closed or is already closed.| ");
      }

      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - | ");
    }
  }
}

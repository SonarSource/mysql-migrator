/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataReproducer {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private Statement sourceStatement;
  private PreparedStatement destPrepStatement;
  private ResultSet resultSet;
  //private String debug;

  public DataReproducer() {
  }

  public void doCopy(Connection sourceConnection, Connection destConnection, Database database) throws SQLException {
    sourceConnection.setAutoCommit(false);
    destConnection.setAutoCommit(false);
    //debug="";
    try {
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
    /* GET INFORMATION FROM EACH TABLE  */
        String tableName = database.getTableName(indexTable);
        int nbColInTable = database.getNbColumnsInTable(indexTable);
        int nbRowsInTable = database.getTable(indexTable).getNbRows();
        List<Column> columns = database.getTable(indexTable).getColumns();

    /* MAKE STRINGS TO PUT IN SQL INSERT REQUEST */
        ListColumnsAsString lcas = new ListColumnsAsString(columns);
        String columnsAsString = lcas.makeColumnString();
        String questionMarkString = lcas.makeQuestionMarkString();
        String sqlRequest = "INSERT INTO " + tableName + " (" + columnsAsString + ") VALUES(" + questionMarkString + ");";

    /* MAKE STATEMENTS  */
        sourceStatement = sourceConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        destPrepStatement = destConnection.prepareStatement(sqlRequest);

    /* GET RESULTSET FROM SOURCE */
        //TODO Integer.MIN_VALUE to modify (?) for Oracle et sqlserver
        sourceStatement.setFetchSize(1000);
        resultSet = sourceStatement.executeQuery("SELECT * FROM " + tableName);

    /* ITERATE ON SOURCE RESULTSET */
        int counter = 0, commit = 1;
        while (resultSet.next()) {
          //debug= "";
          for (int indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
            Object objectGetted = resultSet.getObject(indexColumn + 1);
            destPrepStatement.setObject(indexColumn + 1, objectGetted);
            //debug+=" *** "+String.valueOf(objectGetted);
          }
    /* INSERT DATAS OF THE ROW */
          destPrepStatement.executeUpdate();
          counter++;
          if (counter >= 10000 * commit) {
            destConnection.commit();
            LOGGER.log(Level.INFO, "-- COMMIT (every 10 000 rows) -- IN TABLE : " + tableName + " --  ROW " + counter + " /" + nbRowsInTable);
            commit++;
          }
        }
    /* INSERT IN THE TABLE FINISHED */
        resultSet.close();
        sourceStatement.close();
        destPrepStatement.close();
        LOGGER.log(Level.INFO, "************* DATAS COPIED IN " + tableName + " TABLE n° " + indexTable + ". *************");
      }
    } catch (Exception e) {
      throw new DbException("A problem happened during the copy.",e);

    } finally {
      try {
        if(resultSet!=null && !resultSet.isClosed()){
          resultSet.close();
        }
        if(sourceStatement!=null && !sourceStatement.isClosed()){
          sourceStatement.close();
        }
        //sourceConnection.close();
      } catch (SQLException e) {
        throw new DbException("Impossible to close object connected to database source in DataReproducer",e);
      }

    }

  }

  public void closeResultSet() {
    try {
      if (resultSet != null && !resultSet.isClosed()) {
        resultSet.close();
      }
    } catch (SQLException e) {
      throw new DbException("Closing resultset in DataReproducer failed.", e);
    }
  }

  public void closeSourceStatement() {
    try {
      if (sourceStatement != null && !sourceStatement.isClosed()) {
        sourceStatement.close();
      }
    } catch (SQLException e) {
      throw new DbException("Closing statement from source in DataReproducer failed.", e);
    }
  }

  public void closeDestPrepareStatement() {
    try {
      if (destPrepStatement != null && !destPrepStatement.isClosed()) {
        destPrepStatement.close();
      }
    } catch (SQLException e) {
      throw new DbException("Closing statement from destination in DataReproducer failed.", e);
    }
  }
}


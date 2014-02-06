/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.*;

public class LoopWriter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private int nbColInTable, indexTable, nbRowsInTable;
  private String tableName, sqlInsertRequest;

  public LoopWriter(int nbColInTable, int indexTable, String tableName, int nbRowsInTable, String sqlInsertRequest) {
    this.nbColInTable = nbColInTable;
    this.indexTable = indexTable;
    this.tableName = tableName;
    this.nbRowsInTable = nbRowsInTable;
    this.sqlInsertRequest = sqlInsertRequest;
  }

  public void readAndWrite(Connection connectionSource, Connection connectionDestination) {
    Closer closer = new Closer("LoopWriter");
    Statement sourceStatement = null;
    PreparedStatement destinationStatement = null;
    ResultSet resultSetSource = null;
    int lineWritten = 0, nbCommit = 0, nbLog = 0, indexColumn = 0;
    Object objectGetted = null;

    try {
      sourceStatement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      sourceStatement.setFetchSize(1);
      destinationStatement = connectionDestination.prepareStatement(sqlInsertRequest);
      resultSetSource = sourceStatement.executeQuery("SELECT * FROM " + tableName);

      while (resultSetSource.next()) {
        lineWritten++;
        for (indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
          objectGetted = resultSetSource.getObject(indexColumn + 1);

          if (objectGetted == null) {
            destinationStatement.setObject(indexColumn + 1, null);
          } else if (objectGetted.getClass().equals(oracle.sql.TIMESTAMP.class)) {
            Timestamp objTimeStamp = resultSetSource.getTimestamp(indexColumn+1);
            destinationStatement.setTimestamp(indexColumn + 1, objTimeStamp);
          } else {
            destinationStatement.setObject(indexColumn + 1, objectGetted);
          }
        }
        destinationStatement.addBatch();

        if (lineWritten > 1000 * nbLog) {
          LOGGER.info("COPYING... : " + indexTable + "   " + tableName + " LINES " + lineWritten + " / " + nbRowsInTable);
          nbLog++;
        }
//      COMMIT EACH 10 ROWS
        if (lineWritten > 10 * nbCommit) {
          destinationStatement.executeBatch();
          connectionDestination.commit();
          nbCommit++;
        }
      }
//      MUST COMMIT LAST ROWS
      destinationStatement.executeBatch();
      connectionDestination.commit();
      closer.closeStatement(destinationStatement);

    } catch (SQLException e) {
      throw new DbException("Problem in LoopWriter for the TABLE (" + tableName + ") in COLUMN (" + indexColumn + ") at ROW (" + lineWritten + ") for OBJECT (" + objectGetted + ")", e);
    } finally {
      closer.closeResultSet(resultSetSource);
      closer.closeStatement(sourceStatement);
      closer.closeStatement(destinationStatement);
      LOGGER.info("EveryThing is closed in LoopWriter.");
    }
  }
}

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
    int lineWritten = 0, nbCommit = 0;
    Object objectGetted;

    try {
      sourceStatement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      sourceStatement.setFetchSize(1);
      destinationStatement = connectionDestination.prepareStatement(sqlInsertRequest);
      resultSetSource = sourceStatement.executeQuery("SELECT * FROM " + tableName);

      while (resultSetSource.next()) {
        lineWritten++;
        for (int indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
          objectGetted = resultSetSource.getObject(indexColumn + 1);
          if (objectGetted == null) {
            destinationStatement.setObject(indexColumn + 1, null);
          } else {
            destinationStatement.setObject(indexColumn + 1, objectGetted);
          }
        }
        destinationStatement.addBatch();

        if (lineWritten > 1000 * nbCommit) {
          destinationStatement.executeBatch();
          connectionDestination.commit();
          LOGGER.info("COPYING... : " + indexTable + "   " + tableName + " LINES " + lineWritten + " / " + nbRowsInTable);
          nbCommit++;
        }
      }
      destinationStatement.executeBatch();
      connectionDestination.commit();
      closer.closeStatement(destinationStatement);

    } catch (SQLException e) {
      throw new DbException("Problem in LoopWriter", e);
    } finally {
      closer.closeResultSet(resultSetSource);
      closer.closeStatement(sourceStatement);
      closer.closeStatement(destinationStatement);
      LOGGER.info("EveryThing is closed in LoopWriter.");
    }
  }
}

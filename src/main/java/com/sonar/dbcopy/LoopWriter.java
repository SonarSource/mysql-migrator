/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoopWriter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private int nbColInTable, indexTable, nbRowsInTable;
  private String tableName;

  public LoopWriter(int nbColInTable, int indexTable, String tableName, int nbRowsInTable) {
    this.nbColInTable = nbColInTable;
    this.indexTable = indexTable;
    this.tableName = tableName;
    this.nbRowsInTable = nbRowsInTable;
  }

  public void readAndWrite(ResultSet resultSetSource, PreparedStatement destinationStatement, Connection connectionDestination) throws SQLException {
    int lineWritten = 0, nbCommit = 0;
    Object objectGetted;

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
          /* ADD BATCH FOR EACH ROW */
      destinationStatement.addBatch();

      /* LOG , EXECUTE BATCH AND COMMIT EVERY 1000 ROWS */
      if (lineWritten > 1000 * nbCommit) {
        destinationStatement.executeBatch();
        connectionDestination.commit();
        LOGGER.info("COPYING... : " + indexTable + "   " + tableName + " LINES " + lineWritten + " / " + nbRowsInTable);
        nbCommit++;
      }
    }
    /* EXECUTE BATCH AND COMMIT FOR ULTIMATE ROWS */
    destinationStatement.executeBatch();
    connectionDestination.commit();
  }
}

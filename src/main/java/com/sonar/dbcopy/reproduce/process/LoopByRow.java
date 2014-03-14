/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.reproduce.reader.ReaderTool;
import com.sonar.dbcopy.reproduce.writer.WriterTool;
import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.DbException;
import com.sonar.dbcopy.utils.toolconfig.ListColumnsAsString;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class LoopByRow {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private String logExceptionContext;
  private Table sourceTable, destTable;

  public LoopByRow(Table sourceTable, Table destTable) {
    this.sourceTable = sourceTable;
    this.destTable = destTable;
  }

  public void executeCopy(ResultSet resultSetSource, PreparedStatement preparedStatementDest, ReaderTool readerTool, WriterTool writerTool) {
    Closer closer = new Closer("LoopByRow");
    int lineWritten = 0, indexColumn, lastID = 0, lastIDOfPreviousBlock = 0;
    int nbColInTable = sourceTable.getNbColumns();
    String tableName = sourceTable.getName();
    int nbRowsInTable = sourceTable.getNbRows();

    ListColumnsAsString lcasSource = new ListColumnsAsString(sourceTable);
    ListColumnsAsString lcasDest = new ListColumnsAsString(destTable);
    try {
      while (resultSetSource.next()) {
        lastID = getIfRowHasIntegerIdAndPreserveCopyFromException(resultSetSource);

        logExceptionContext = "TABLE (" + tableName + ") " +
          " which had COLUMNS: " + lcasSource.makeColumnString() + " with TYPES: " + lcasSource.makeStringOfTypes() + " IN SOURCE " +
          " and COLUMNS: " + lcasDest.makeColumnString() + " with TYPES: " + lcasDest.makeStringOfTypes() + " IN DESTINATION " +
          " at ROW (" + lineWritten + ") for - id - between (" + lastIDOfPreviousBlock + ") and (" + lastID + ").";

        for (indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
          Object objectGetted = resultSetSource.getObject(indexColumn + 1);

          /* ******* COPY : CASES WITH PROBLEM ARE TREATED WITH COPIERTOOL ******* */
          if (objectGetted == null) {
            writerTool.writeWhenNull(indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.TIMESTAMP) {
            writerTool.writeTimestamp(readerTool.readTimestamp(resultSetSource, indexColumn), indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.DECIMAL && destTable.getType(indexColumn) == Types.BIT) {
            writerTool.writeBoolean(readerTool.readBoolean(resultSetSource, indexColumn), indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.BLOB) {
            writerTool.writeBlob(readerTool.readBlob(resultSetSource, indexColumn), indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.CLOB) {
            writerTool.writeClob(readerTool.readClob(resultSetSource, indexColumn), indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.VARCHAR) {
            writerTool.writeVarchar(readerTool.readVarchar(resultSetSource, indexColumn), indexColumn);
          } else {
            writerTool.writeObject(readerTool.readObject(resultSetSource, indexColumn), indexColumn);
          }
        }
        preparedStatementDest.addBatch();

        if (lineWritten % 100000 == 0) {
          LOGGER.info("COPYING... : " + tableName + " LINES " + lineWritten + " / " + nbRowsInTable);
        }

        // COMMIT EACH 10 ROWS
        if (lineWritten % 10 == 0) {
          executeBatchAndPreserveCopyFromException(preparedStatementDest);
          lastIDOfPreviousBlock = lastID;
        }
        lineWritten++;
      }
      executeBatchAndPreserveCopyFromException(preparedStatementDest);

    } catch (SQLException e) {
      throw new DbException("Problem when reading and writing data in LoopByRow for the " + logExceptionContext, e);
    } finally {
      closer.closeResultSet(resultSetSource);
      closer.closeStatement(preparedStatementDest);
    }
  }

  private void executeBatchAndPreserveCopyFromException(PreparedStatement preparedStatementDest) {
    try {
      preparedStatementDest.executeBatch();
      preparedStatementDest.getConnection().commit();
    } catch (SQLException e) {
      LOGGER.error("ERROR: LINES NOT COPIED !! IN " + logExceptionContext, e);
    }
  }

  private int getIfRowHasIntegerIdAndPreserveCopyFromException(ResultSet resultSetSource){
    int idToReturn = 0;
    try {
      idToReturn = resultSetSource.getInt(1);
    } catch (SQLException e) {
      LOGGER.info("First column is not and Integer.", e);
    }
    return idToReturn;
  }
}


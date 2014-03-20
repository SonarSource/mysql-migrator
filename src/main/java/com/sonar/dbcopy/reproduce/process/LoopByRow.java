/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.reproduce.reader.ReaderTool;
import com.sonar.dbcopy.reproduce.writer.WriterTool;
import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.ListColumnsAsString;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class LoopByRow {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private String logRowsForExecuteBatch, logCurrentRowForAddBatch, tableContentSource, tableContentDest;
  private Table sourceTable, destTable;
  private ReaderTool readerTool;
  private WriterTool writerTool;
  private PreparedStatement preparedStatementDest;

  public LoopByRow(Table sourceTable, Table destTable) {
    this.sourceTable = sourceTable;
    this.destTable = destTable;
    ListColumnsAsString lcasSource = new ListColumnsAsString(sourceTable);
    ListColumnsAsString lcasDest = new ListColumnsAsString(destTable);
    tableContentSource = "SOURCE COLUMNS      ( " + lcasSource.makeColumnString() + " ) with TYPES (" + lcasSource.makeStringOfTypes() + " ).";
    tableContentDest = "DESTINATION COLUMNS ( " + lcasDest.makeColumnString() + " ) with TYPES (" + lcasDest.makeStringOfTypes() + " ).";
  }

  public void executeCopy(ResultSet resultSetSource, PreparedStatement preparedStatementDest, ReaderTool readerTool, WriterTool writerTool) throws SQLException {
    this.readerTool = readerTool;
    this.writerTool = writerTool;
    this.preparedStatementDest = preparedStatementDest;
    long lineWritten = 0, lastID = 0, lastIDOfPreviousBlock = 0;
    String tableName = sourceTable.getName();
    int nbRowsInTable = sourceTable.getNbRows();


    logLinesCopied(tableName, lineWritten, nbRowsInTable);
    while (resultSetSource.next()) {

      // RECORD CONTEXT
      lastID = getIdIfPKAndPreserveCopyFromException(resultSetSource);
      logRowsForExecuteBatch = " at ROW (" + lineWritten + ") WITH - id - BETWEEN (" + lastIDOfPreviousBlock + ") AND (" + lastID + ").";
      logCurrentRowForAddBatch = " at ROW (" + lineWritten + ") WITH  id = (" + lastID + ").";

      // LOG EACH 50 000 LINES WRITTEN
      lineWritten++;
      if (lineWritten % 50000 == 0) {
        logLinesCopied("", lineWritten, nbRowsInTable);
      }

      // COPY
      getAndSetEachRowDependingOnType(resultSetSource);
      // ADD BATCH
      addBatchAndPreserveCopyFromException();

      // COMMIT EACH 10 ROWS
      if (lineWritten % 10 == 0) {
        executeBatchAndPreserveCopyFromException();
        commitAndPreserveCopyFromException();
        lastIDOfPreviousBlock = lastID;
      }
    }
    // COMMIT LAST ROWS
    executeBatchAndPreserveCopyFromException();
    commitAndPreserveCopyFromException();

    //LOG LAST COPIED LINE
    logLinesCopied(tableName, lineWritten, nbRowsInTable);
  }

  private void getAndSetEachRowDependingOnType(ResultSet resultSetSource) {
    try {
      for (int indexColumn = 0; indexColumn < sourceTable.getNbColumns(); indexColumn++) {
        Object objectGetted = resultSetSource.getObject(indexColumn + 1);

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
    } catch (SQLException e) {
      this.displayContextLog(e, logCurrentRowForAddBatch);
    }
  }

  private void addBatchAndPreserveCopyFromException() {
    try {
      preparedStatementDest.addBatch();
    } catch (SQLException e) {
      this.displayContextLog(e, logCurrentRowForAddBatch);
    }
  }

  private void executeBatchAndPreserveCopyFromException() {
    try {
      preparedStatementDest.executeBatch();
    } catch (SQLException e) {
      this.displayContextLog(e, logRowsForExecuteBatch);
    }
  }

  private void commitAndPreserveCopyFromException() {
    try {
      preparedStatementDest.getConnection().commit();
    } catch (SQLException e) {
      this.displayContextLog(e, logRowsForExecuteBatch);
    }
  }

  private long getIdIfPKAndPreserveCopyFromException(ResultSet resultSetSource) {
    long idToReturn = 0;
    try {
      idToReturn = resultSetSource.getLong(1);
    } catch (SQLException e) {
      LOGGER.info("First column is not a Long.", e);
    }
    return idToReturn;
  }

  private void logLinesCopied(String tableName, long lineWritten, int nbRowsInTable) {
    String space = "                                      ";
    if (tableName.length() < 30) {
      space = space.substring(0, 30 - tableName.length());
    }
    LOGGER.info(tableName + space + lineWritten + " / " + nbRowsInTable);
  }

  private void displayContextLog(SQLException e, String logRow) {
    LOGGER.error("ERROR IN TABLE: " + sourceTable.getName());
    LOGGER.error(tableContentSource);
    LOGGER.error(tableContentDest);
    LOGGER.error("LINES NOT COPIED " + logRow);
    LOGGER.error(e.getMessage()); // GIVE THE EXACT ROW WITH WHERE SQL REQUEST FAILED, BUT SOMETIMES IT'S TOO LONG WHEN IT DISPLAYS ENTIRE FILES
    LOGGER.error("NEXT EXCEPTION: " + e.getNextException());

  }


}


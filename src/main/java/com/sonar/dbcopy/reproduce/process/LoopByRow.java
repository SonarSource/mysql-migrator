/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.reproduce.reader.ReaderTool;
import com.sonar.dbcopy.reproduce.writer.WriterTool;
import com.sonar.dbcopy.utils.data.Table;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class LoopByRow {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private String logRowsForExecuteBatch;
  private String logCurrentRowForAddBatch;
  private String tableContentSource;
  private String tableContentDest;
  private Table sourceTable;
  private Table destTable;
  private int commitSize;
  private ReaderTool readerTool;
  private WriterTool writerTool;
  private PreparedStatement preparedStatementDest;
  private Object lastID;

  public LoopByRow(Table sourceTable, Table destTable, int commitSize) {
    this.sourceTable = sourceTable;
    this.destTable = destTable;
    this.commitSize = commitSize;
    tableContentSource = "SOURCE COLUMNS      ( " + sourceTable.getColumnNamesAsString() + " ) with TYPES (" + sourceTable.getTypesAsString() + " ).";
    tableContentDest = "DESTINATION COLUMNS ( " + destTable.getColumnNamesAsString() + " ) with TYPES (" + destTable.getTypesAsString() + " ).";
  }

  public void executeCopy(ResultSet resultSetSource, PreparedStatement preparedStatementDest, ReaderTool readerTool, WriterTool writerTool) throws SQLException {
    this.readerTool = readerTool;
    this.writerTool = writerTool;
    this.preparedStatementDest = preparedStatementDest;
    long lineWritten = 0;
    Object lastIDOfPreviousBlock = "Initial";
    lastID = "initial";
    String tableName = sourceTable.getName();
    int nbRowsInTable = sourceTable.getNbRows();


    logLinesCopied(tableName, lineWritten, nbRowsInTable);
    while (resultSetSource.next()) {

      // RECORD CONTEXT
      lastID = getIdIfPkButPreserveCopyFromException(resultSetSource);
      logRowsForExecuteBatch = " at ROW (" + lineWritten + ") WITH - id - BETWEEN (" + lastIDOfPreviousBlock + ") AND (" + lastID + ").";
      logCurrentRowForAddBatch = " at ROW (" + lineWritten + ") WITH  id = (" + lastID + ").";

      // LOG EACH 50 000 LINES WRITTEN
      lineWritten++;
      if (lineWritten % 50_000 == 0) {
        logLinesCopied("", lineWritten, nbRowsInTable);
      }

      // COPY
      getAndSetEachRowDependingOnType(resultSetSource);
      // ADD BATCH
      addBatchButPreserveCopyFromException();

      // COMMIT EACH <COMMIT SIZE> ROWS
      if (lineWritten % commitSize == 0) {
        executeBatchButPreserveCopyFromException();
        commitButPreserveCopyFromException();
        lastIDOfPreviousBlock = lastID;
      }
    }
    // COMMIT LAST ROWS
    executeBatchButPreserveCopyFromException();
    commitButPreserveCopyFromException();

    //LOG LAST COPIED LINE
    logLinesCopied(tableName, lineWritten, nbRowsInTable);
  }

  private void getAndSetEachRowDependingOnType(ResultSet resultSetSource) {
    int indexColumn = 0;
    try {
      for (indexColumn = 0; indexColumn < sourceTable.getNbColumns(); indexColumn++) {
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
      this.displayContextLog(e, logCurrentRowForAddBatch, "read and write at col: " + indexColumn + " and id=" + lastID);
    }
  }

  private void addBatchButPreserveCopyFromException() {
    try {
      preparedStatementDest.addBatch();
    } catch (SQLException e) {
      this.displayContextLog(e, logCurrentRowForAddBatch, "add batch at id=" + lastID);
    }
  }

  private void executeBatchButPreserveCopyFromException() {
    try {
      preparedStatementDest.executeBatch();
    } catch (SQLException e) {
      this.displayContextLog(e, logRowsForExecuteBatch, "execute batch");
    }
  }

  private void commitButPreserveCopyFromException() {
    try {
      preparedStatementDest.getConnection().commit();
    } catch (SQLException e) {
      this.displayContextLog(e, logRowsForExecuteBatch, "commit");
    }
  }

  private static Object getIdIfPkButPreserveCopyFromException(ResultSet resultSetSource) {
    try {
      return resultSetSource.getLong(1);
    } catch (SQLException notLong) {
      try {
        return resultSetSource.getString(1);
      } catch(SQLException notStringEither) {
        LOGGER.info("First column is neither Long nor String.", notStringEither);
        return null;
      }
    }
  }

  private static void logLinesCopied(String tableName, long lineWritten, int nbRowsInTable) {
    String space = "                                      ";
    if (tableName.length() < 30) {
      space = space.substring(0, 30 - tableName.length());
    }
    LOGGER.info("{}{}{} / {}", tableName, space, lineWritten, nbRowsInTable);
  }

  private void displayContextLog(SQLException e, String logRow, String kindOfError) {
    LOGGER.error("IN TABLE: " + sourceTable.getName() + " when " + kindOfError + ".");
    LOGGER.error(tableContentSource);
    LOGGER.error(tableContentDest);
    LOGGER.error("LINES NOT COPIED " + logRow);
    LOGGER.error(e.getMessage());
    if (e.getNextException() != null) {
      LOGGER.error("NEXT EXCEPTION: " + e.getNextException());
    }
  }
}


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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class LoopByRow {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private String logExceptionContext, tableContentSource, tableContentDest;
  private Table sourceTable, destTable;

  public LoopByRow(Table sourceTable, Table destTable) {
    this.sourceTable = sourceTable;
    this.destTable = destTable;
  }

  public void executeCopy(ResultSet resultSetSource, PreparedStatement preparedStatementDest, ReaderTool readerTool, WriterTool writerTool) {
    Closer closer = new Closer("LoopByRow");
    int indexColumn;
    long lineWritten = 0, lastID = 0, lastIDOfPreviousBlock = 0;
    int nbColInTable = sourceTable.getNbColumns();
    String tableName = sourceTable.getName();
    int nbRowsInTable = sourceTable.getNbRows();

    ListColumnsAsString lcasSource = new ListColumnsAsString(sourceTable);
    ListColumnsAsString lcasDest = new ListColumnsAsString(destTable);
    try {
      LOGGER.info(tableName);
      while (resultSetSource.next()) {
        // PREPARE LOG AND DISPLAY INFO EACH 50000 ROWS
        lastID = getIfRowHasIntegerIdAndPreserveCopyFromException(resultSetSource);
        logExceptionContext = " at ROW (" + lineWritten + ") WITH - id - BETWEEN (" + lastIDOfPreviousBlock + ") AND (" + lastID + ").";
        tableContentSource = "SOURCE COLUMNS      ( " + lcasSource.makeColumnString() + " ) with TYPES (" + lcasSource.makeStringOfTypes() + " ).";
        tableContentDest = "DESTINATION COLUMNS ( " + lcasDest.makeColumnString() + " ) with TYPES (" + lcasDest.makeStringOfTypes() + " ).";

        lineWritten++;
        if (lineWritten % 50000 == 0) {
          //TABLE NAME IS DISPLAYED ONLY AT THE LINE 0 JUST BEFORE THE START OF while
          logLinesCopied(lineWritten, nbRowsInTable);
        }


        for (indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
          Object objectGetted = resultSetSource.getObject(indexColumn + 1);
        /* ******* COPY : CASES WITH PROBLEM ARE TREATED WITH READER AND WRITER TOOLS ******* */
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

        // COMMIT EACH 10 ROWS
        if (lineWritten % 10 == 0) {
          executeBatchAndPreserveCopyFromException(preparedStatementDest);
          lastIDOfPreviousBlock = lastID;
        }
      }
      executeBatchAndPreserveCopyFromException(preparedStatementDest);
      logLinesCopied(lineWritten, nbRowsInTable);

    } catch (SQLException e) {
      LOGGER.error(" * * * * * * * * * * * * * * * * * * * * * * ");
      LOGGER.error("ERROR IN TABLE: " + tableName);
      LOGGER.error(tableContentSource);
      LOGGER.error(tableContentDest);
      LOGGER.error("LINES NOT COPIED " + logExceptionContext);
      LOGGER.error(e.getMessage());
      LOGGER.error("NEXT EXCEPTION: " + e.getNextException());
      throw new DbException("Problem in LoopByRow when reading data.", e);

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
      LOGGER.error("****************************************************");
      LOGGER.error("ERROR IN TABLE: " + sourceTable.getName());
      LOGGER.error(tableContentSource);
      LOGGER.error(tableContentDest);
      LOGGER.error("LINES NOT COPIED " + logExceptionContext);
      LOGGER.error(e.getMessage());
      LOGGER.error("NEXT EXCEPTION: " + e.getNextException());
      rollBackConnection(preparedStatementDest);
    }
  }

  private long getIfRowHasIntegerIdAndPreserveCopyFromException(ResultSet resultSetSource) {
    long idToReturn = 0;
    try {
      idToReturn = resultSetSource.getLong(1);
    } catch (SQLException e) {
      LOGGER.info("First column is not a Long.", e);
    }
    return idToReturn;
  }

  private void logLinesCopied(long lineWritten, int nbRowsInTable) {
    LOGGER.info("   " + lineWritten + " / " + nbRowsInTable);

  }

  private void rollBackConnection(PreparedStatement preparedStatementDest) {
    try {
      preparedStatementDest.getConnection().rollback();
    } catch (SQLException e) {
      throw new DbException("Unable to rollback the connection", e);
    }
  }
}


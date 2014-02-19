/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce;

import com.sonar.dbcopy.reproduce.tools.*;
import com.sonar.dbcopy.utils.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.Closer;
import com.sonar.dbcopy.utils.DbException;
import com.sonar.dbcopy.utils.objects.Table;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;

public class LoopToReadAndWrite {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  private int nbColInTable, indexTable, nbRowsInTable;
  private String tableName, sqlInsertRequest;
  private Table sourceTable, destTable;
  private Connection connectionSource, connectionDestination;

  private String logExceptionContext;

  private CopierTool copierTool;
  private boolean sourceIsOracle, sourceIsSqlServer;
  private boolean destinationIsPostgresql, destinationIsOracle, destinationIsSqlServer, destinationIsMySql;

  public LoopToReadAndWrite(Table sourceTable, Table destTable, int indexTable, String sqlInsertRequest) {
    this.sourceTable = sourceTable;
    this.destTable = destTable;
    this.nbColInTable = sourceTable.getNbColumns();
    this.indexTable = indexTable;
    this.tableName = sourceTable.getName();
    this.nbRowsInTable = sourceTable.getNbRows();
    this.sqlInsertRequest = sqlInsertRequest;
  }

  public void prepareStatementAndStartCopy(Connection connectionSource, Connection connectionDestination) {
    this.connectionSource = connectionSource;
    this.connectionDestination = connectionDestination;

    Closer closer = new Closer("LoopToReadAndWrite");
    Statement sourceStatement = null;
    PreparedStatement destinationStatement = null;
    ResultSet resultSetSource = null;

    try {

      sourceStatement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      sourceStatement.setFetchSize(1);
      resultSetSource = sourceStatement.executeQuery("SELECT * FROM " + tableName);
      destinationStatement = connectionDestination.prepareStatement(sqlInsertRequest);

      // BUILD RIGHT COPIERTOOL
      whichEditorIsSourceAndDestination();
      buildCopierTool(destinationStatement);

      // DO THE COPY WITH THE RIGHT COPIERTOOL  IN LOOP : FOR EACH ROW AND EACH COLUMN
      readAndWrite(resultSetSource, destinationStatement);

      // MUST COMMIT LAST ROWS
      destinationStatement.executeBatch();
      connectionDestination.commit();
      closer.closeStatement(destinationStatement);

    } catch (SQLException e) {
      throw new DbException("Problems when create statements in LoopToReadAndWrite to do copy at TABLE : " + tableName, e);
    } finally {
      closer.closeResultSet(resultSetSource);
      closer.closeStatement(sourceStatement);
      closer.closeStatement(destinationStatement);
      LOGGER.info("EveryThing is closed in LoopWriter.");
    }
  }

  private void readAndWrite(ResultSet resultSetSource, PreparedStatement destinationStatement) {
    int lineWritten = 0, nbCommit = 0, nbLog = 0, indexColumn;
    Object objectGetted;

    try {
      while (resultSetSource.next()) {
        lineWritten++;
        for (indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
          objectGetted = resultSetSource.getObject(indexColumn + 1);

          logExceptionContext = "Problem when reading and writing data in LoopToReadAndWrite for the TABLE (" + tableName + ") " +
            "in COLUMN SOURCE (name:" + sourceTable.getColumnName(indexColumn) + ",type:" + sourceTable.getType(indexColumn) + ")" +
            " and COLUMN DEST(name:" + destTable.getColumnName(indexColumn) + ",type:" + destTable.getType(indexColumn) + ") " +
            "at ROW (" + lineWritten + ") for OBJECT SOURCE(" + objectGetted + ") ";

          /* ******* COPY : CASES WITH PROBLEM ARE TREATED WITH COPIERTOOL ******* */
          if (objectGetted == null) {
            copierTool.copyWhenNull(indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.TIMESTAMP) {
            copierTool.copyTimestamp(resultSetSource, indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.DECIMAL && destTable.getType(indexColumn) == Types.BIT) {
            copierTool.copyBoolean(resultSetSource, indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.BLOB) {
            copierTool.copyBlob(resultSetSource, indexColumn);
          } else if (sourceTable.getType(indexColumn) == Types.CLOB) {
            copierTool.copyClob(resultSetSource, indexColumn);
          } else {
            copierTool.copy(resultSetSource, indexColumn);
          }
        }
        destinationStatement.addBatch();

        if (lineWritten > 1000 * nbLog) {
          LOGGER.info("COPYING... : " + indexTable + "   " + tableName + " LINES " + lineWritten + " / " + nbRowsInTable);
          nbLog++;
        }
        // COMMIT EACH 10 ROWS
        if (lineWritten > 10 * nbCommit) {
          destinationStatement.executeBatch();
          connectionDestination.commit();
          nbCommit++;
        }
      }
    } catch (SQLException e) {
      throw new DbException(logExceptionContext, e);
    } catch (IOException e) {
      throw new DbException(logExceptionContext, e);
    }
  }


  public void buildCopierTool(PreparedStatement destinationStatement) {
    if (sourceIsOracle && destinationIsSqlServer) {
      copierTool = new OracleToSqlServer(destinationStatement);
    } else if (sourceIsOracle && destinationIsPostgresql) {
      copierTool = new OracleToPostgresql(destinationStatement);
    } else if (sourceIsOracle && destinationIsMySql) {
      copierTool = new OracleToMySql(destinationStatement);
    } else if (sourceIsSqlServer && destinationIsOracle) {
      copierTool = new SqlServerToOracle(destinationStatement);
    } else if (sourceIsSqlServer && destinationIsPostgresql) {
      copierTool = new SqlServerToPostgresql(destinationStatement);
    } else {
      copierTool = new DefaultCopierTool(destinationStatement);
    }
  }

  private void whichEditorIsSourceAndDestination() throws SQLException {
    CharacteristicsRelatedToEditor chRelToEd = new CharacteristicsRelatedToEditor();
    DatabaseMetaData metaSource = connectionSource.getMetaData();
    DatabaseMetaData metaDest = connectionDestination.getMetaData();

    sourceIsOracle = chRelToEd.isOracle(metaSource);
    sourceIsSqlServer = chRelToEd.isSqlServer(metaSource);

    destinationIsPostgresql = chRelToEd.isPostgresql(metaDest);
    destinationIsOracle = chRelToEd.isOracle(metaDest);
    destinationIsSqlServer = chRelToEd.isSqlServer(metaDest);
    destinationIsMySql = chRelToEd.isMySql(metaDest);
  }

}

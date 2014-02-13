/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;

public class LoopWriter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private int nbColInTable, indexTable, nbRowsInTable;
  private String tableName, sqlInsertRequest;
  private Table sourceTable, destTable;
  private Connection connectionSource, connectionDestination;
  private boolean sourceIsOracle, sourceIsSqlServer, destinationIsPostgresql, destinationIsOracle, destinationIsSqlServer;
  private String logExceptionContext;

  public LoopWriter(Table sourceTable, Table destTable, int indexTable, String sqlInsertRequest) {
    this.sourceTable = sourceTable;
    this.destTable = destTable;
    this.nbColInTable = sourceTable.getNbColumns();
    this.indexTable = indexTable;
    this.tableName = sourceTable.getName();
    this.nbRowsInTable = sourceTable.getNbRows();
    this.sqlInsertRequest = sqlInsertRequest;
  }

  public void readAndWrite(Connection connectionSource, Connection connectionDestination) {
    this.connectionSource = connectionSource;
    this.connectionDestination = connectionDestination;

    Closer closer = new Closer("LoopWriter");
    Statement sourceStatement = null;
    PreparedStatement destinationStatement = null;
    ResultSet resultSetSource = null;
    int lineWritten = 0, nbCommit = 0, nbLog = 0, indexColumn = 0;
    Object objectGetted = null;

    try {
      whichIsSourceAndDestination();

      sourceStatement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      sourceStatement.setFetchSize(1);
      destinationStatement = connectionDestination.prepareStatement(sqlInsertRequest);
      resultSetSource = sourceStatement.executeQuery("SELECT * FROM " + tableName);

      while (resultSetSource.next()) {
        lineWritten++;
        for (indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
          logExceptionContext = "Problem when converting data in LoopWriter for the TABLE (" + tableName + ") " +
            "in COLUMN SOURCE (name:" + sourceTable.getColumnName(indexColumn) + ",type:" + sourceTable.getType(indexColumn) + ")" +
            " and COLUMN DEST(name:" + destTable.getColumnName(indexColumn) + ",type:" + destTable.getType(indexColumn) + ") " +
            "at ROW (" + lineWritten + ") for OBJECT SOURCE(" + objectGetted + ") ";

          objectGetted = resultSetSource.getObject(indexColumn + 1);

/* COPY : HERE IS TREATED CASES WITH PROBLEM */
          /* WHEN OBJECTS GETTED ARE NULL */
          if (objectGetted == null) {
            destinationStatement.setObject(indexColumn + 1, null);
          }
          /* SOURCE: ORACLE(TIMESTAMP) , DESTINATION ? (TIMESTAMP) */
          else if (sourceIsOracle && objectGetted.getClass().equals(oracle.sql.TIMESTAMP.class)) {
            Timestamp objTimestamp = resultSetSource.getTimestamp(indexColumn + 1);
            destinationStatement.setTimestamp(indexColumn + 1, objTimestamp);
          }

          /* SOURCE: ORACLE(DECIMAL as boolean) , DESTINATION: POSTGRESQL(BIT as boolean) */
          else if (sourceIsOracle && destinationIsPostgresql && sourceTable.getType(indexColumn) == Types.DECIMAL && destTable.getType(indexColumn) == Types.BIT) {
            int integerObj = resultSetSource.getInt(indexColumn + 1);
            boolean booleanToInsertInPostgresql = false;
            if (integerObj == 1) {
              booleanToInsertInPostgresql = true;
            }
            destinationStatement.setBoolean(indexColumn + 1, booleanToInsertInPostgresql);
          }

          /* SOURCE: ORACLE (BLOB), DESTINATION: POSTGRESQL(BINARY) */
          /* SOURCE: SQLSERVER(BLOB), DESTINATION:POSTGRESQL(BINARY) */
          /* SOURCE: ORACLE(CLOB) , DESTINATION: SQLSERVER(CLOB) */
          /* SOURCE: ORACLE(BLOB) , DESTINATION: SQLSERVER(BLOB) */
          else if ((sourceIsOracle && destinationIsPostgresql && sourceTable.getType(indexColumn) == Types.BLOB && destTable.getType(indexColumn) == Types.BINARY)
            ||
            (sourceIsSqlServer && destinationIsPostgresql && sourceTable.getType(indexColumn) == Types.BLOB && destTable.getType(indexColumn) == Types.BINARY)
            ||
            (sourceIsOracle && destinationIsSqlServer && sourceTable.getType(indexColumn) == Types.CLOB && destTable.getType(indexColumn) == Types.CLOB)
            ||
            (sourceIsOracle && destinationIsSqlServer && sourceTable.getType(indexColumn) == Types.BLOB && destTable.getType(indexColumn) == Types.BLOB)) {

            InputStream inputStreamObj = resultSetSource.getBinaryStream(indexColumn + 1);
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = inputStreamObj.read(buffer)) != -1) {
              output.write(buffer, 0, bytesRead);
            }
            byte[] bytesToInsert = output.toByteArray();
            destinationStatement.setBytes(indexColumn + 1, bytesToInsert);
            inputStreamObj.close();
          }

          /* SOURCE: SQLSERVER (CLOB) , DESTINATION: ORACLE(CLOB) */
          else if (sourceIsSqlServer && destinationIsOracle && sourceTable.getType(indexColumn) == Types.CLOB && destTable.getType(indexColumn) == Types.CLOB) {
            Clob clobObj = resultSetSource.getClob(indexColumn + 1);
            Reader reader = clobObj.getCharacterStream();
            destinationStatement.setClob(indexColumn + 1, reader);
            reader.close();
          }

          /* SOURCE: SQLSERVER(BLOB) , DESTINATION: ORACLE (BLOB) */
          else if (sourceIsSqlServer && destinationIsOracle && sourceTable.getType(indexColumn) == Types.BLOB && destTable.getType(indexColumn) == Types.BLOB) {
            Blob blobObj = resultSetSource.getBlob(indexColumn + 1);
            InputStream inputStream = blobObj.getBinaryStream();
            destinationStatement.setBlob(indexColumn + 1, inputStream);
            inputStream.close();
          }

/* ALL OTHER CASES WHEN EVERYTHING GOES WELL */
          else {
            destinationStatement.setObject(indexColumn + 1, objectGetted);
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
      // MUST COMMIT LAST ROWS
      destinationStatement.executeBatch();
      connectionDestination.commit();
      closer.closeStatement(destinationStatement);

    } catch (IOException e) {
      throw new DbException(logExceptionContext, e);
    } catch (SQLException e) {
      throw new DbException(logExceptionContext, e);
    } finally {
      closer.closeResultSet(resultSetSource);
      closer.closeStatement(sourceStatement);
      closer.closeStatement(destinationStatement);
      LOGGER.info("EveryThing is closed in LoopWriter.");
    }
  }

  private void whichIsSourceAndDestination() throws SQLException {
    CharacteristicsRelatedToEditor chRelToEd = new CharacteristicsRelatedToEditor();
    DatabaseMetaData metaSource = connectionSource.getMetaData();
    DatabaseMetaData metaDest = connectionDestination.getMetaData();

    sourceIsOracle = chRelToEd.isOracle(metaSource);
    sourceIsSqlServer = chRelToEd.isSqlServer(metaSource);

    destinationIsPostgresql = chRelToEd.isPostgresql(metaDest);
    destinationIsOracle = chRelToEd.isOracle(metaDest);
    destinationIsSqlServer = chRelToEd.isSqlServer(metaDest);
  }
}

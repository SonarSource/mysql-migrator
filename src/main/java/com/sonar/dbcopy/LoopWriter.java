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
import java.sql.*;

public class LoopWriter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private int nbColInTable, indexTable, nbRowsInTable;
  private String tableName, sqlInsertRequest;
  private Table sourceTable, destTable;


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
    Closer closer = new Closer("LoopWriter");
    Statement sourceStatement = null;
    PreparedStatement destinationStatement = null;
    ResultSet resultSetSource = null;
    CharacteristicsRelatedToEditor chRelToEd = new CharacteristicsRelatedToEditor();
    int lineWritten = 0, nbCommit = 0, nbLog = 0, indexColumn = 0;
    Object objectGetted = null;

    try {
      DatabaseMetaData metaSource = connectionSource.getMetaData();
      boolean sourceIsOracle = chRelToEd.isOracle(metaSource);
      boolean sourceIsMsSql = chRelToEd.isSqlServer(metaSource);

      sourceStatement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      sourceStatement.setFetchSize(1);
      destinationStatement = connectionDestination.prepareStatement(sqlInsertRequest);
      resultSetSource = sourceStatement.executeQuery("SELECT * FROM " + tableName);

      while (resultSetSource.next()) {
        lineWritten++;
        for (indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
          objectGetted = resultSetSource.getObject(indexColumn + 1);

          /* COPY : HERE IS TREATED CASES WITH PROBLEM */

          /* WHEN OBJECTS GETTED ARE NULL */
          if (objectGetted == null) {
            destinationStatement.setObject(indexColumn + 1, null);
          }

          /* WHEN ORACLE IS SOURCE AND OBJECTGETTED TYPE IS TIMESTAMP */
          else if (sourceIsOracle && objectGetted.getClass().equals(oracle.sql.TIMESTAMP.class)) {
            Timestamp objTimestamp = resultSetSource.getTimestamp(indexColumn + 1);
            destinationStatement.setTimestamp(indexColumn + 1, objTimestamp);
          }

          /* WHEN ORACLE IS SOURCE : IF SOURCE COLUMN TYPE IS ORACLE BOOLEAN (Types.DECIMAL) AND DESTINATION COLUMN TYPE IS POSTGRESQL BOOLEAN (Types.BIT) */
          else if (sourceIsOracle && sourceTable.getType(indexColumn) == Types.DECIMAL && destTable.getType(indexColumn) == Types.BIT) {
            int integerObj = resultSetSource.getInt(indexColumn + 1);
            boolean booleanToInsertInPostgresql = false;
            if (integerObj == 1) {
              booleanToInsertInPostgresql = true;
            }
            destinationStatement.setBoolean(indexColumn + 1, booleanToInsertInPostgresql);
          }

          /* WHEN ORACLE OR MSSQL ARE SOURCE : IF SOURCE COLUMN IS BLOB (Types.BLOB) AND DESTINATION COLUMN IS POSTEGRESQL BYTEA (Types.BINARY) >> IT IS NECESSARY TO REWRITE Inpustream IN Byte[] */
          else if (sourceTable.getType(indexColumn) == Types.BLOB && destTable.getType(indexColumn) == Types.BINARY) {
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

    } catch (IOException ioe) {
      throw new DbException("Problem when converting BLOB in LoopWriter for the TABLE (" + tableName + ") in COLUMN (" + indexColumn + ") at ROW (" + lineWritten + ") for OBJECT (" + objectGetted + ")", ioe);
    } catch (SQLException e) {
      LOGGER.error(e.toString());
      LOGGER.error(e.getNextException().toString());
      LOGGER.error("-----------------");
      throw new DbException("Problem in LoopWriter for the TABLE (" + tableName + ") in COLUMN (" + indexColumn + ") at ROW (" + lineWritten + ") for OBJECT (" + objectGetted + ")", e);
    } finally

    {
      closer.closeResultSet(resultSetSource);
      closer.closeStatement(sourceStatement);
      closer.closeStatement(destinationStatement);
      LOGGER.info("EveryThing is closed in LoopWriter.");
    }
  }
}

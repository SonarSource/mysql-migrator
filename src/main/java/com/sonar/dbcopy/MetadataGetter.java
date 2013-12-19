/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetadataGetter {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private Statement statementSource;
  private ResultSet resultSetTables, resultSetCol, resultSetRows;

  public MetadataGetter() {
  }

  public void getSchemaOfDatabaseSource(Connection connectionSource, Database database) {
    try {
      statementSource = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
    } catch (SQLException e) {
      throw new DbException("Creation of statement source to get schema failed", e);
    }

    try {
      /* WARNING TO GET TABLES FROM METADATA IN DEPENDS ON THE DATABASE VENDOR */
      String schema;
      DatabaseMetaData metaData = connectionSource.getMetaData();
      if("jdbc:po".equals(metaData.getURL().substring(0,7))){
        schema="public";
      }else if("jdbc:h2".equals(metaData.getURL().substring(0,7))){
        schema=null;
      }else if("jdbc:my".equals(metaData.getURL().substring(0,7))){
        schema=null;
      }else if("jdbc:or".equals(metaData.getURL().substring(0,7))){
        schema=null;
      }else if("jdbc:sq".equals(metaData.getURL().substring(0,7))){
        schema=null;
      }else{
        schema=null;
      }
      /* GET TABLES FROM SCHEMA */
      resultSetTables = metaData.getTables(connectionSource.getCatalog(), schema , "%", new String[]{"TABLE"});

      if (!resultSetTables.isBeforeFirst()) {
        throw new DbException("*** ERROR : NO DATA FOUND IN DATABASE SOURCE ***", new Exception());
      } else {
        while (resultSetTables.next()) {
          database.addTable(resultSetTables.getString("TABLE_NAME"));
          LOGGER.log(Level.INFO, "TABLE "+resultSetTables.getString("TABLE_NAME")+" FOUND");
        }
      }
      resultSetTables.close();

      /* GET COLUMNS FROM TABLES */
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        resultSetCol = metaData.getColumns(null, null, database.getTableName(indexTable), "%");
        while (resultSetCol.next()) {
          database.getTable(indexTable).addColumn(resultSetCol.getString("COLUMN_NAME"));
        }
        resultSetCol.close();
      }

      /* GET NB ROWS BY TABLE */
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        resultSetRows = statementSource.executeQuery("SELECT COUNT(*) FROM "+database.getTableName(indexTable));
        while (resultSetRows.next()) {
          database.getTable(indexTable).setNbRows(resultSetRows.getInt(1));
        }
        resultSetRows.close();
      }


    } catch (SQLException e) {
      throw new DbException("Problem to get schema from database source.", e);
    } finally {
      try {
        LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - | ");
        resultSetRows.close();
        LOGGER.log(Level.INFO, " | ResultSetRows is closed.                        | ");
        resultSetTables.close();
        LOGGER.log(Level.INFO, " | ResultSetTables is closed.                      | ");
        resultSetCol.close();
        LOGGER.log(Level.INFO, " | ResultSetCol is closed.                         | ");
        statementSource.close();
        LOGGER.log(Level.INFO, " | StatementSource is closed.                      | ");
        LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - | ");
      } catch (SQLException e) {
        throw new DbException("Problem to close Metadata objects.", e);
      }

    }
  }
/*
  public void closeStatementAndResultSet() {
    try {
      LOGGER.log(Level.INFO, " - - - - - - CLOSE METADATA OBJECTS - - - - - - - - ");
      if (resultSetCol!=null) {
        resultSetCol.close();
      }
      LOGGER.log(Level.INFO, "ResultSetCol from source in metadata getter has been closed.");

      if (!resultSetTables.isClosed()) {
        resultSetTables.close();
      }
      LOGGER.log(Level.INFO, "ResultSetTables from source in metadata getter has been closed.");

      statementSource.close();
      LOGGER.log(Level.INFO, "Statement from source in metadata getter has been closed.");
      LOGGER.log(Level.INFO, " - - - - - - - - - - - - - - - - - - - - - - - - - - ");
    } catch (SQLException e) {
      throw new DbException("Closing of  statement source or resultset from metadata getter failed.", e);
    }
  }   */
}

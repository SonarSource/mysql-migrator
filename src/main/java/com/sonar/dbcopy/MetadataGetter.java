/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.*;

public class MetadataGetter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private Database database;
  private ConnecterDatas cd;

  public MetadataGetter(ConnecterDatas cd, Database db) {
    this.cd = cd;
    this.database = db;
  }

  public void execute() {
    Connection connectionSource = new Connecter().doConnection(this.cd);
    Statement statementSource = null;
    ResultSet resultSetTables = null, resultSetCol = null, resultSetRows = null;
    try {
      statementSource = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

      /* WARNING !! TO GET TABLES FROM METADATA IN DEPENDS ON THE DATABASE VENDOR */
      DatabaseMetaData metaData = connectionSource.getMetaData();
      SchemaRelatedToVendor schemaRelatedToVendor = new SchemaRelatedToVendor();
      String schema = schemaRelatedToVendor.getSchema(metaData.getURL().substring(0, 7));

      /* GET TABLES FROM SCHEMA AND ADD TO DATABASE */
      resultSetTables = metaData.getTables(connectionSource.getCatalog(), schema, "%", new String[]{"TABLE"});
      this.addTables(resultSetTables);
      resultSetTables.close();

      /* GET COLUMNS FROM TABLES */
      this.addColumns(metaData);

      /* GET NB ROWS BY TABLE */
      this.addNbRowInTables(statementSource);

    } catch (SQLException e) {
      throw new DbException("Problem to get schema from database source.", e);
    } finally {
      try {
        if (resultSetTables != null) {
          resultSetTables.close();
        }
      } catch (SQLException e) {
        LOGGER.error("ResultSetTables can't be closed or is already closed in MetadataGetter." + e);
      }
      try {
        if (statementSource != null) {
          statementSource.close();
        }
      } catch (SQLException e) {
        LOGGER.error("StatementSource can't be closed or is already closed in MetadataGetter." + e);
      }
      try {
        if (connectionSource != null) {
          connectionSource.close();
        }
      } catch (SQLException e) {
        LOGGER.error("ConnectionSource can't be closed or is already closed in MetadataGetter." + e);
      }
      LOGGER.info("Everything is closed in MetadataGetter.");
    }
  }

  private void addTables(ResultSet resultSetTables) throws SQLException {
    if (!resultSetTables.isBeforeFirst()) {
      throw new DbException("*** ERROR : NO DATA FOUND IN DATABASE SOURCE ***", new Exception());
    } else {
      while (resultSetTables.next()) {
        String tableName = resultSetTables.getString("TABLE_NAME");
        database.addTable(tableName);
        LOGGER.info("FOUND : " + tableName + ".");
      }
    }
  }

  private void addColumns(DatabaseMetaData metaData) {
    ResultSet resultSetCol = null;
    try {
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        resultSetCol = metaData.getColumns(null, null, database.getTableName(indexTable), "%");
        while (resultSetCol.next()) {
          database.getTable(indexTable).addColumn(resultSetCol.getString("COLUMN_NAME"));
        }
        resultSetCol.close();
      }
    } catch (SQLException e) {
      throw new DbException("Problem to get schema from database source.", e);
    } finally {
      try {
        if (resultSetCol != null) {
          resultSetCol.close();
        }
      } catch (SQLException e) {
        LOGGER.error("ResultSetRows can't be closed or is already closed in MetadataGetter." + e);
      }
    }
  }

  private void addNbRowInTables(Statement statementSource) {
    ResultSet resultSetRows = null;
    try {
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        resultSetRows = statementSource.executeQuery("SELECT COUNT(*) FROM " + database.getTableName(indexTable));
        while (resultSetRows.next()) {
          database.getTable(indexTable).setNbRows(resultSetRows.getInt(1));
        }
        resultSetRows.close();
      }
    } catch (SQLException e) {
      throw new DbException("Problem to get schema from database source.", e);
    } finally {
      try {
        if (resultSetRows != null) {
          resultSetRows.close();
        }
      } catch (SQLException e) {
        LOGGER.error("ResultSetRows can't be closed or is already closed in MetadataGetter." + e);
      }
    }
  }
}

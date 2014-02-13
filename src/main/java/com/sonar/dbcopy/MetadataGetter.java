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
  private Closer closer;
  private DatabaseMetaData metaData;
  private CharacteristicsRelatedToEditor chRelToEd;

  public MetadataGetter(ConnecterDatas cd, Database db) {
    this.cd = cd;
    this.database = db;
  }

  public void execute() {
    closer = new Closer("MetadataGetter");

    Connection connectionSource = new Connecter().doConnection(this.cd);
    Statement statementSource = null;
    ResultSet resultSetTables = null;
    try {
      statementSource = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

      /* WARNING !! TO GET TABLES FROM METADATA IT DEPENDS ON THE VALUE OF SCHEMA EDITOR */
      metaData = connectionSource.getMetaData();

      chRelToEd = new CharacteristicsRelatedToEditor();
      String schema = chRelToEd.getSchema(metaData);

      /* GET TABLES FROM SCHEMA AND ADD TO DATABASE */
      resultSetTables = metaData.getTables(connectionSource.getCatalog(), schema, "%", new String[]{"TABLE"});
      this.addTables(resultSetTables);
      closer.closeResultSet(resultSetTables);

      /* GET COLUMNS FROM TABLES */
      this.addColumns();

      /* GET NB ROWS BY TABLE */
      this.addNbRowInTables(statementSource);

    } catch (SQLException e) {
      throw new DbException("Problem to get schema from database source.", e);
    } finally {
      closer.closeResultSet(resultSetTables);
      closer.closeStatement(statementSource);
      closer.closeConnection(connectionSource);
      LOGGER.info("Everything is closed in MetadataGetter.");
    }
  }

  private void addTables(ResultSet resultSetTables) throws SQLException {
    if (!resultSetTables.isBeforeFirst()) {
      throw new DbException("*** ERROR : CAN'T FIND ANY TABLE IN DATABASE SOURCE ***", new Exception("resultset is empty"));
    } else {
      while (resultSetTables.next()) {
        String tableName = resultSetTables.getString("TABLE_NAME").toLowerCase();
        database.addTable(tableName);
      }
    }
  }

  private void addColumns() {
    ResultSet resultSetCol = null;
    int indexTable = 0;
    int indexColumn;
    try {
      for (indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        indexColumn = 0;
        String tableNameWithAdaptedCase = chRelToEd.transfromCaseOfTableName(metaData, database.getTableName(indexTable));

        //ORACLE NEEDS UPERCASE TO EXECUTE getColumns()
        resultSetCol = metaData.getColumns(null, null, tableNameWithAdaptedCase, "%");
        while (resultSetCol.next()) {
          String columnNameToInsert = resultSetCol.getString("COLUMN_NAME").toLowerCase();
          int columnType = resultSetCol.getInt("DATA_TYPE");
          database.getTable(indexTable).addColumn(indexColumn, columnNameToInsert, columnType);
          indexColumn++;
        }
        closer.closeResultSet(resultSetCol);
      }
    } catch (SQLException e) {
      throw new DbException("Problem to add columns in TABLE : " + database.getTableName(indexTable) + ".", e);
    } finally {
      closer.closeResultSet(resultSetCol);
    }
  }

  private void addNbRowInTables(Statement statementSource) {
    ResultSet resultSetRows = null;
    int indexTable = 0;
    try {
      for (indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        resultSetRows = statementSource.executeQuery("SELECT COUNT(*) FROM " + database.getTableName(indexTable));
        while (resultSetRows.next()) {
          database.getTable(indexTable).setNbRows(resultSetRows.getInt(1));
        }
        closer.closeResultSet(resultSetRows);
      }
    } catch (SQLException e) {
      throw new DbException("Problem to add number of rows in TABLE : " + database.getTableName(indexTable) + ".", e);
    } finally {
      closer.closeResultSet(resultSetRows);
    }
  }
}

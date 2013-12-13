/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;

public class MetadataGetter {

  private Statement statementSource;
  private ResultSet resultSetTables;

  public MetadataGetter() {
  }

  public void getSchemaOfDatabaseSource(Connection connectionSource, Database database) {
    try {
      statementSource = connectionSource.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    } catch (SQLException e) {
      throw new DbException("Creation of statement source to get schema failed", e);
    }

    try {
      StringMakerAccordingToProvider stringMaker = new StringMakerAccordingToProvider();

      /* GET ALL TABLES WITH NB OF COLUMNS FROM SOURCE */
      resultSetTables = statementSource.executeQuery(stringMaker.getSqlRequest(connectionSource));

      // TODO to get schema tables with metadata
      //       DatabaseMetaData metaData = connectionSource.getMetaData();
      //       ResultSet resultSetTables = metaData.getTables(null, "public", "%", null);

      if (!resultSetTables.isBeforeFirst()) {
        throw new DbException("*** ERROR : DATAS NOT FOUND IN DATABASE SOURCE ***", new Exception());
      } else {
        while (resultSetTables.next()) {
          database.addTable(resultSetTables.getString(1));
        }
      }
      resultSetTables.close();

      /* GET ALL COLUMNS FROM EACH TABLE ALREADY FOUND */
      for (int indexTable = 0; indexTable < database.getTables().size(); indexTable++) {

        Table tableToModify = database.getTable(indexTable);

        ResultSet resultSet = statementSource.executeQuery("SELECT * FROM " + tableToModify.getName());
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int nbColumns = resultSetMetaData.getColumnCount();

        for (int indexColumn = 1; indexColumn <= nbColumns; indexColumn++) {
          String columnName = resultSetMetaData.getColumnName(indexColumn);
          tableToModify.addColumn(columnName);
        }
        resultSet.close();
      }
    } catch (SQLException e) {
      throw new DbException("Problem to get schema from database source.", e);
    } finally {
      closeStatementAndResultSet();
    }
  }

  public void closeStatementAndResultSet() {
    try {
      if (!resultSetTables.isClosed()) {
        resultSetTables.close();
      }
      statementSource.close();
    } catch (SQLException e) {
      throw new DbException("Closing of  statement source or resultset from metadata getter failed.", e);
    }
  }
}

/*
 * Copyright (C) 2013-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.Connecter;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class MetadataGetter {

  private Database database;
  private ConnecterData cd;
  private Closer closer;
  private DatabaseMetaData metaData;
  private CharacteristicsRelatedToEditor chRelToEd;

  public MetadataGetter(ConnecterData cd, Database db) {
    this.cd = cd;
    this.database = db;
  }

  public void execute(String[] tablesToCopy) {
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
      if (tablesToCopy == null) {
        this.fillTablesListFromDb(resultSetTables);
      } else {
        this.fillTablesListFromCommandLine(resultSetTables, tablesToCopy);
      }
      closer.closeResultSet(resultSetTables);

      /* GET COLUMNS FROM TABLES */
      this.fetchColumns();

      /* GET NB ROWS BY TABLE */
      this.fetchNbRowInTables(statementSource);

    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem to get schema from database source.", e);
    } finally {
      closer.closeResultSet(resultSetTables);
      closer.closeStatement(statementSource);
      closer.closeConnection(connectionSource);
    }
  }

  private void fillTablesListFromDb(ResultSet resultSetTables) throws SQLException {
    if (!resultSetTables.isBeforeFirst()) {
      throw new MessageException("Can not find tables in database source.");
    } else {
      while (resultSetTables.next()) {
        String tableName = resultSetTables.getString("TABLE_NAME").toLowerCase();
        database.addToTablesList(tableName);
      }
    }
  }

  private void fillTablesListFromCommandLine(ResultSet resultSetTables, String[] tablesToCopy) throws SQLException {
    if (!resultSetTables.isBeforeFirst()) {
      throw new MessageException("Can not find any table in database.");
    } else {
      while (resultSetTables.next()) {
        boolean tablehasBeenrequired = false;
        String tableNameFoundInDb = resultSetTables.getString("TABLE_NAME").toLowerCase();
        for (int indexTablesRequired = 0; indexTablesRequired < tablesToCopy.length; indexTablesRequired++) {
          if (tablesToCopy[indexTablesRequired].equals(tableNameFoundInDb)) {
            tablehasBeenrequired = true;
          }
        }
        if (tablehasBeenrequired) {
          database.addToTablesList(tableNameFoundInDb);
        }
      }
      if (database.getNbTables() != tablesToCopy.length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tablesToCopy.length; ++i) {
          stringBuilder.append(tablesToCopy[i] + " ");
        }
        String allTablesRequired = stringBuilder.toString();
        throw new MessageException("It seems that some table(s) you required in ( " + allTablesRequired + ") do not exist. Verify the name in the database.");
      }
    }
  }

  private void fetchColumns() {
    ResultSet resultSetCol = null;
    int indexTable = 0;
    int indexColumn;
    try {
      for (indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        indexColumn = 0;
        String tableNameWithAdaptedCase = chRelToEd.transfromCaseOfTableName(metaData, database.getTableName(indexTable));

        //ORACLE NEEDS UPERCASE TO EXECUTE getColumns()
        String schema = chRelToEd.getSchema(metaData);
        resultSetCol = metaData.getColumns(null, schema, tableNameWithAdaptedCase, "%");
        while (resultSetCol.next()) {
          String columnNameToInsert = resultSetCol.getString("COLUMN_NAME").toLowerCase();
          int columnType = resultSetCol.getInt("DATA_TYPE");
          database.getTable(indexTable).addColumn(indexColumn, columnNameToInsert, columnType);
          indexColumn++;
        }
        closer.closeResultSet(resultSetCol);
        database.getTable(indexTable).makeStringsUsedForTable();
      }
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem fetching the columns from TABLE : " + database.getTableName(indexTable) + ".", e);
    } finally {
      closer.closeResultSet(resultSetCol);
    }
  }

  private void fetchNbRowInTables(Statement statementSource) {
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
      throw new SqlDbCopyException("Problem fetching the number of rows in TABLE : " + database.getTableName(indexTable) + ".", e);
    } finally {
      closer.closeResultSet(resultSetRows);
    }
  }
}

/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2013-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import java.util.Arrays;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataGetter {

  private static final Logger LOGGER = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

  private Database database;
  private ConnecterData cd;
  private Closer closer;
  private DatabaseMetaData metaData;
  private Connection connectionSource;

  public MetadataGetter(ConnecterData cd, Database db) {
    this.cd = cd;
    this.database = db;
  }

  public void execute(String[] tablesToCopy) {
    closer = new Closer("MetadataGetter");

    connectionSource = new Connecter().doConnection(this.cd);
    Statement statementSource = null;
    ResultSet resultSetTables = null;
    try {
      statementSource = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

      /* WARNING !! TO GET TABLES FROM METADATA IT DEPENDS ON THE VALUE OF SCHEMA EDITOR */
      metaData = connectionSource.getMetaData();

      String schema = connectionSource.getSchema();

      LOGGER.info("Catalog: {}", connectionSource.getCatalog());
      LOGGER.info("Schema: {}", schema);

      /* GET TABLES FROM SCHEMA AND ADD TO DATABASE */
      resultSetTables = metaData.getTables(connectionSource.getCatalog(), schema, "%", new String[] {"TABLE"});
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
        String tableName = resultSetTables.getString("TABLE_NAME").toLowerCase(Locale.ENGLISH);
        LOGGER.info("Table to migrate: {}", tableName);
        database.addToTablesList(tableName);
      }
    }
  }

  private void fillTablesListFromCommandLine(ResultSet resultSetTables, String[] tablesToCopy) throws SQLException {
    if (!resultSetTables.isBeforeFirst()) {
      throw new MessageException("Can not find any table in database.");
    } else {
      while (resultSetTables.next()) {
        String tableNameFoundInDb = resultSetTables.getString("TABLE_NAME").toLowerCase(Locale.ENGLISH);
        boolean tablehasBeenrequired = Arrays.asList(tablesToCopy).contains(tableNameFoundInDb );
        if (tablehasBeenrequired) {
          database.addToTablesList(tableNameFoundInDb);
        }
      }
      if (database.getNbTables() != tablesToCopy.length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String aTablesToCopy : tablesToCopy) {
          stringBuilder.append(aTablesToCopy).append(" ");
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
        String tableNameWithAdaptedCase = CharacteristicsRelatedToEditor.transfromCaseOfTableName(metaData
            , database.getTableName(indexTable));

        // ORACLE NEEDS UPERCASE TO EXECUTE getColumns()
        String schema = connectionSource.getSchema();
        resultSetCol = metaData.getColumns(null, schema, tableNameWithAdaptedCase, "%");
        while (resultSetCol.next()) {
          String columnNameToInsert = resultSetCol.getString("COLUMN_NAME").toLowerCase(Locale.ENGLISH);
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

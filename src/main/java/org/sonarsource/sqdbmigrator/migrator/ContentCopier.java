/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2019 SonarSource SA
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
package org.sonarsource.sqdbmigrator.migrator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarsource.sqdbmigrator.migrator.Migrator.MigrationException;

public class ContentCopier {

  private static final Logger LOG = LoggerFactory.getLogger(ContentCopier.class);

  private static final int DEFAULT_BATCH_SIZE = 5000;

  void execute(Database source, Database target, TableListProvider tableListProvider) {
    execute(source, target, tableListProvider, DEFAULT_BATCH_SIZE);
  }

  void execute(Database source, Database target, TableListProvider tableListProvider, int batchSize) {
    tableListProvider.get(source).forEach(tableName -> {
      LOG.info("copying table {} ...", tableName);

      try {
        target.truncateTable(tableName);

        boolean tableHasIdColumn = source.tableHasIdColumn(tableName);

        if (tableHasIdColumn) {
          target.setIdentityInsert(tableName, true);
        }

        copyTable(source, target, tableName, batchSize);

        if (tableHasIdColumn) {
          target.setIdentityInsert(tableName, false);
          resetSequence(target, tableName);
        }

        ensureRowCountsMatch(source, target, tableName);
      } catch (SQLException e) {
        throw new MigrationException("Error while copying rows of table '%s': %s", tableName, e.getMessage());
      }
    });
  }

  private void copyTable(Database source, Database target, String tableName, int batchSize) throws SQLException {
    String selectSql = String.format("select * from %s", tableName);
    try (Statement preparedStatement = source.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs = preparedStatement.executeQuery(selectSql)) {

      int columnCount = rs.getMetaData().getColumnCount();

      String insertSql = String.format("insert into %s values (%s)", tableName, formatPlaceholders(columnCount));
      int count = 0;
      try (PreparedStatement insertStatement = target.getConnection().prepareStatement(insertSql)) {
        while (rs.next()) {
          copyColumns(rs, insertStatement);
          insertStatement.addBatch();

          count++;
          if (count % batchSize == 0) {
            insertStatement.executeBatch();
            target.getConnection().commit();
            count = 0;
          }
        }

        if (count > 0) {
          insertStatement.executeBatch();
          target.getConnection().commit();
        }
      }
    }
  }

  private static void copyColumns(ResultSet rs, PreparedStatement insertStatement) throws SQLException {
    int columnCount = rs.getMetaData().getColumnCount();
    for (int index = 1; index <= columnCount; index++) {
      insertStatement.setObject(index, rs.getObject(index));
    }
  }

  private static String formatPlaceholders(int count) {
    StringBuilder sb = new StringBuilder(count * 2 - 1);
    sb.append("?");
    IntStream.range(1, count).forEach(i -> sb.append(",?"));
    return sb.toString();
  }

  private static void resetSequence(Database database, String tableName) {
    try {
      long maxIdPlusOne = 1 + database.selectMaxId(tableName);
      database.resetSequence(tableName, maxIdPlusOne);
    } catch (SQLException e) {
      throw new MigrationException("Could not reset sequence for table %s: %s", tableName, e.getMessage());
    }
  }

  private static void ensureRowCountsMatch(Database source, Database target, String tableName) throws SQLException {
    long rowCountInSource = source.countRows(tableName);
    long rowCountInTarget = target.countRows(tableName);
    if (rowCountInSource != rowCountInTarget) {
      throw new MigrationException("Row counts don't match in source and target: %s != %s",
        tableName, rowCountInSource, rowCountInTarget);
    }
  }
}

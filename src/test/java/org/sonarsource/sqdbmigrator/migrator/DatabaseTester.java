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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.rules.ExternalResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.sonarsource.sqdbmigrator.TestUtils.randomAlphabetic;

public class DatabaseTester extends ExternalResource {

  private static final AtomicInteger idCounter = new AtomicInteger();

  private final Database database;

  private DatabaseTester(Database database) {
    this.database = database;
  }

  @Override
  protected void after() {
    try {
      database.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public static DatabaseTester newTester() {
    String dbname = randomAlphabetic(10) + idCounter.incrementAndGet();
    String url = "jdbc:h2:mem:" + dbname;
    ConnectionConfig config = new ConnectionConfig(url, null, null);
    try {
      return new DatabaseTester(Database.create(config));
    } catch (SQLException e) {
      throw new AssertionError("Could not create in-memory database: " + e.getMessage());
    }
  }

  public DatabaseTester createSchemaMigrations() throws SQLException {
    String createSchemaMigrationsSql = "create table schema_migrations (version varchar)";
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(createSchemaMigrationsSql)) {
      assertThat(preparedStatement.executeUpdate()).isEqualTo(0);
      return this;
    }
  }

  public DatabaseTester addVersion(int version) throws SQLException {
    return addVersion(String.valueOf(version));
  }

  public DatabaseTester addVersion(String version) throws SQLException {
    String insertVersionSql = "insert into schema_migrations (version) values (?)";
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(insertVersionSql)) {
      preparedStatement.setString(1, version);
      assertThat(preparedStatement.executeUpdate()).isEqualTo(1);
      return this;
    }
  }

  public DatabaseTester createTable(String createTableSql) throws SQLException {
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(createTableSql)) {
      assertThat(preparedStatement.executeUpdate()).isEqualTo(0);
      return this;
    }
  }

  DatabaseTester addRow(String tableName, List<String> names, List<Object> values) throws SQLException {
    assertThat(names.size()).isEqualTo(values.size());

    String namesCsv = String.join(", ", names);
    StringBuilder placeHolders = new StringBuilder(values.size() * 2 + 1).append("?");
    for (int i = 1; i < values.size(); i++) {
      placeHolders.append(",?");
    }

    String sql = String.format("insert into %s (%s) values (%s)",
      tableName, namesCsv, placeHolders);

    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql)) {
      for (int i = 0; i < values.size(); i++) {
        preparedStatement.setObject(i + 1, values.get(i));
      }
      assertThat(preparedStatement.executeUpdate()).isEqualTo(1);
      return this;
    }
  }

  DatabaseTester addRow(String tableName, List<Object> values) throws SQLException {
    StringBuilder placeHolders = new StringBuilder(values.size() * 2 + 1).append("?");
    for (int i = 1; i < values.size(); i++) {
      placeHolders.append(",?");
    }

    String sql = String.format("insert into %s values (%s)",
      tableName, placeHolders);

    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql)) {
      for (int i = 0; i < values.size(); i++) {
        preparedStatement.setObject(i + 1, values.get(i));
      }
      assertThat(preparedStatement.executeUpdate()).isEqualTo(1);
      return this;
    }
  }

  int queryForInt(String sql) throws SQLException {
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql); ResultSet rs = preparedStatement.executeQuery()) {
      return rs.next() ? rs.getInt(1) : 0;
    }
  }

  List<String> queryForStrings(String sql) throws SQLException {
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql); ResultSet rs = preparedStatement.executeQuery()) {
      List<String> strings = new ArrayList<>();
      while (rs.next()) {
        strings.add(rs.getString(1));
      }
      return strings;
    }
  }

  List<Object> queryForObjects(String sql) throws SQLException {
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql); ResultSet rs = preparedStatement.executeQuery()) {
      List<Object> objects = new ArrayList<>();
      while (rs.next()) {
        objects.add(rs.getObject(1));
      }
      return objects;
    }
  }

  List<byte[]> queryForBytes(String sql) throws SQLException {
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql); ResultSet rs = preparedStatement.executeQuery()) {
      List<byte[]> objects = new ArrayList<>();
      while (rs.next()) {
        objects.add(rs.getBytes(1));
      }
      return objects;
    }
  }

  public void dumpContent(String tableName) throws SQLException {
    String sql = "select * from " + tableName;
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql);
      ResultSet rs = preparedStatement.executeQuery()) {
      int columnCount = rs.getMetaData().getColumnCount();
      StringBuilder columnNames = new StringBuilder();
      for (int index = 1; index <= columnCount; index++) {
        columnNames.append(rs.getMetaData().getColumnName(index)).append(",");
      }
      System.out.println(columnNames.toString());

      while (rs.next()) {
        StringBuilder sb = new StringBuilder();
        for (int index = 1; index <= columnCount; index++) {
          sb.append(rs.getObject(index)).append(",");
        }
        System.out.println(sb.toString());
      }
    }
  }

  public Database getDatabase() {
    return database;
  }
}

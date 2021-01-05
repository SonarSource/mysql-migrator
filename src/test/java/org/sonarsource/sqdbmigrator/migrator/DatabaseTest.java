/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2021 SonarSource SA
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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Locale;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.sonarsource.sqdbmigrator.migrator.Database.DatabaseException;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;

@RunWith(DataProviderRunner.class)
public class DatabaseTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester databaseTester = newTester();

  @Test
  public void create_throws_for_unsupported_driver() throws SQLException {
    expectedException.expect(DatabaseException.class);
    expectedException.expectMessage("Unsupported database: jdbc:foo:bar");
    Database.create(new ConnectionConfig("jdbc:foo:bar", null, null));
  }

  @Test
  @UseDataProvider("defunctUrlsWithSupportedDrivers")
  public void create_attempts_connection_using_supported_drivers(String url) throws SQLException {
    expectedException.expect(SQLException.class);
    expectedException.expectMessage("No suitable driver found");
    Database.create(new ConnectionConfig(url, null, null));
  }

  @DataProvider
  public static Object[][] defunctUrlsWithSupportedDrivers() {
    return new Object[][] {
      {"jdbc:mysql:nonexistent"},
      {"jdbc:postgresql://nonexistent"},
      {"jdbc:sqlserver:nonexistent"},
    };
  }

  @Test
  public void create_throws_with_additional_help_about_oracle_driver_installation() throws SQLException {
    assumeThat(isOracleDriverAvailable()).isFalse();
    expectedException.expect(DatabaseException.class);
    expectedException.expectMessage("No suitable driver found for jdbc:oracle:bar\n" +
      "Make sure the JDBC Oracle driver is copied to the lib folder. The file must be named 'oracle.jar'");
    Database.create(new ConnectionConfig("jdbc:oracle:bar", null, null));
  }

  private boolean isOracleDriverAvailable() {
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @Test
  @UseDataProvider("usernamePasswordNullAndNonNullCombinations")
  public void create_database_with_or_without_credentials(String username, String password) throws SQLException {
    String url = "jdbc:h2:mem:foo";
    ConnectionConfig config = new ConnectionConfig(url, username, password);
    try (Database unused = Database.create(config)) {
      // nothing to do
    }
  }

  @DataProvider
  public static Object[][] usernamePasswordNullAndNonNullCombinations() {
    return new Object[][] {
      {null, null},
      {"foo", null},
      {null, "bar"},
      {"foo", "bar"},
    };
  }

  @Test
  @UseDataProvider("createTableSqlWithAndWithoutId")
  public void tableHasIdColumn(String createTableSql, boolean hasIdColumn) throws SQLException {
    Database database = databaseTester.createTable(createTableSql).getDatabase();
    assertThat(database.tableHasIdColumn("foo")).isEqualTo(hasIdColumn);
  }

  @DataProvider
  public static Object[][] createTableSqlWithAndWithoutId() {
    return new Object[][] {
      {"create table foo (foo varchar, id varchar primary key, baz varchar)", true},
      {"create table foo (foo varchar, id varchar, baz varchar)", false},
      {"create table foo (foo varchar, bar varchar, baz varchar)", false},
      {"create table foo (foo varchar, bar varchar primary key, baz varchar)", false},
    };
  }

  @Test
  public void queryForLong_returns_0_for_no_results() throws SQLException {
    Database database = databaseTester.createTable("create table foo (id int)")
      .getDatabase();
    assertThat(database.queryForLong("select id from foo")).isZero();
  }

  @Test
  @UseDataProvider("nullOrZero")
  public void queryForLong_returns_0_for_null_or_zero_value(@Nullable Integer value) throws SQLException {
    Database database = databaseTester.createTable("create table foo (id int)")
      .addRow("foo", Collections.singletonList(value))
      .getDatabase();
    assertThat(database.queryForLong("select id from foo")).isZero();
  }

  @DataProvider
  public static Object[][] nullOrZero() {
    return new Object[][] {
      {null},
      {0},
    };
  }

  @Test
  @UseDataProvider("mixedCaseFoo")
  public void canonicalTableName_uses_uppercase_by_default(String tableName) {
    assertThat(databaseTester.getDatabase().canonicalTableName(tableName)).isEqualTo("FOO");
  }

  @DataProvider
  public static Object[][] mixedCaseFoo() {
    return new Object[][] {
      {"foo"},
      {"FOO"},
      {"fOO"},
      {"Foo"},
    };
  }
}

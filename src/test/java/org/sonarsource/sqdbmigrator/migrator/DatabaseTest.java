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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.sql.SQLException;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.postgresql.util.PSQLException;
import org.sonarsource.sqdbmigrator.migrator.Database.DatabaseException;

import static org.assertj.core.api.Assertions.assertThat;
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
  @UseDataProvider("validUrlsAndExpectedExceptions")
  public void create_attempts_connection_using_supported_drivers(String url, Class<? extends Exception> exceptionType, String expectedMessage) throws SQLException {
    expectedException.expect(exceptionType);
    expectedException.expectMessage(expectedMessage);
    Database.create(new ConnectionConfig(url, null, null));
  }

  @DataProvider
  public static Object[][] validUrlsAndExpectedExceptions() {
    return new Object[][] {
      {"jdbc:mysql://nonexistent/nonexistent", SQLException.class, "Communications link failure"},
      {"jdbc:postgresql://nonexistent/nonexistent", PSQLException.class, "The connection attempt failed"},
      // FIXME slow test
      // {"jdbc:sqlserver://nonexistent/nonexistent", SQLServerException.class, "The TCP/IP connection to the host nonexistent/nonexistent, port 1433 has failed"},
    };
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
    };
  }

  @Test
  public void queryForLong_returns_0_for_null_value() throws SQLException {
    Database database = databaseTester.createTable("create table foo (id int)")
      .addRow("foo", Collections.singletonList(null))
      .getDatabase();
    assertThat(database.queryForLong("select id from foo")).isZero();
  }
}

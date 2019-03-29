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

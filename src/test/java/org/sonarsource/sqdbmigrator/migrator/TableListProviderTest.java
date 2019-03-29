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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;

public class TableListProviderTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester databaseTester = newTester();

  private final TableListProvider underTest = new TableListProvider();

  @Test
  public void throw_if_there_are_no_tables_to_migrate() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Could not find any tables. Expected a non-empty list to migrate.");
    underTest.get(databaseTester.getDatabase());
  }

  @Test
  public void get_tables_in_the_database() throws SQLException {
    List<String> tableNames = Arrays.asList("foo", "bar", "baz");
    for (String tableName : tableNames) {
      databaseTester.createTable(String.format("create table %s (name varchar)", tableName));
    }
    assertThat(underTest.get(databaseTester.getDatabase())).containsExactlyInAnyOrderElementsOf(tableNames);
  }
}

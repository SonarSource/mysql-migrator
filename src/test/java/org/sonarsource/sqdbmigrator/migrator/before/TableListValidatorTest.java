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
package org.sonarsource.sqdbmigrator.migrator.before;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.DatabaseTester;
import org.sonarsource.sqdbmigrator.migrator.TableListProvider;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;

public class TableListValidatorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester sourceTester = newTester();

  @Rule
  public final DatabaseTester targetTester = newTester();

  private final TableListValidator underTest = new TableListValidator();

  @Test
  public void do_not_throw_when_all_expected_tables_exist() {
    List<String> tables = Arrays.asList("foo", "bar");
    Database source = createTables(sourceTester, tables);
    Database target = createTables(targetTester, tables);
    TableListProvider tableListProvider = newTableListProvider(tables);
    underTest.execute(source, target, tableListProvider);
  }

  @Test
  public void throw_when_tables_are_missing_in_source() {
    List<String> tables = Arrays.asList("foo", "bar", "baz");
    Database source = createTables(sourceTester, singletonList("bar"));
    Database target = createTables(targetTester, tables);
    TableListProvider tableListProvider = newTableListProvider(tables);

    expectedException.expect(PreMigrationChecks.PreMigrationException.class);
    expectedException.expectMessage("Some expected tables are missing in source database: foo, baz");
    underTest.execute(source, target, tableListProvider);
  }

  @Test
  public void throw_when_tables_are_missing_in_target() {
    List<String> tables = Arrays.asList("foo", "bar", "baz");
    Database source = createTables(sourceTester, tables);
    Database target = createTables(targetTester, singletonList("bar"));
    TableListProvider tableListProvider = newTableListProvider(tables);

    expectedException.expect(PreMigrationChecks.PreMigrationException.class);
    expectedException.expectMessage("Some expected tables are missing in target database: foo, baz");
    underTest.execute(source, target, tableListProvider);
  }

  @Test
  public void throw_when_tables_are_missing_in_source_and_target() {
    List<String> tables = Arrays.asList("foo", "bar", "baz");
    Database source = createTables(sourceTester, singletonList("foo"));
    Database target = createTables(targetTester, singletonList("bar"));
    TableListProvider tableListProvider = newTableListProvider(tables);

    expectedException.expect(PreMigrationChecks.PreMigrationException.class);
    expectedException.expectMessage("Some expected tables are missing in source database: bar, baz");
    underTest.execute(source, target, tableListProvider);
  }

  @Test
  public void throw_when_cannot_get_tables_from_source() throws SQLException {
    Database source = mock(Database.class);
    when(source.getTables()).thenThrow(SQLException.class);
    Database target = createTables(targetTester, singletonList("foo"));
    TableListProvider tableListProvider = mock(TableListProvider.class);

    expectedException.expect(PreMigrationChecks.PreMigrationException.class);
    expectedException.expectMessage("Could not get list of tables from source database:");
    underTest.execute(source, target, tableListProvider);
  }

  @Test
  public void throw_when_cannot_get_tables_from_target() throws SQLException {
    Database source = createTables(sourceTester, singletonList("foo"));
    Database target = mock(Database.class);
    when(target.getTables()).thenThrow(SQLException.class);
    TableListProvider tableListProvider = mock(TableListProvider.class);

    expectedException.expect(PreMigrationChecks.PreMigrationException.class);
    expectedException.expectMessage("Could not get list of tables from target database:");
    underTest.execute(source, target, tableListProvider);
  }

  private Database createTables(DatabaseTester databaseTester, List<String> tables) {
    tables.forEach(tableName -> {
      try {
        databaseTester.createTable(String.format("create table %s (name varchar)", tableName));
      } catch (SQLException e) {
        fail(e.getMessage());
      }
    });
    return databaseTester.getDatabase();
  }

  private TableListProvider newTableListProvider(List<String> tables) {
    return new TableListProvider() {
      @Override
      public List<String> get(Database database) {
        return tables;
      }
    };
  }
}

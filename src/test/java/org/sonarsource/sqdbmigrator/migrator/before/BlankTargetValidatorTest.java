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
package org.sonarsource.sqdbmigrator.migrator.before;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.DatabaseTester;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks.PreMigrationException;

import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;

@RunWith(DataProviderRunner.class)
public class BlankTargetValidatorTest {

  private static final List<String> EXPECTED_TABLE_NAMES = Arrays.asList("projects", "project_measures", "issues", "users");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester databaseTester = newTester();

  private final BlankTargetValidator underTest = new BlankTargetValidator();

  @Test
  public void pass_when_expected_record_counts_match() throws SQLException {
    Database database = databaseTester.getDatabase();
    createExpectedTables();
    createExpectedRecords();

    underTest.execute(database);
  }

  @Test
  @UseDataProvider("expectedTableNames")
  public void fail_when_expected_table_missing(String tableName) throws SQLException {
    Database database = databaseTester.getDatabase();
    createExpectedTables();
    database.executeUpdate("drop table " + tableName);

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage(String.format("Could not get record count in target table '%s':", tableName));
    underTest.execute(database);
  }

  @Test
  @UseDataProvider("expectedTableNames")
  public void fail_when_there_are_more_records_than_expected(String tableName) throws SQLException {
    Database database = databaseTester.getDatabase();
    createExpectedTables();
    createExpectedRecords();
    createRecord(tableName);

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage(String.format("Unexpected record count in target table '%s'", tableName));
    underTest.execute(database);
  }

  @Test
  public void fail_when_there_are_less_records_than_expected() throws SQLException {
    String tableName = "users";

    Database database = databaseTester.getDatabase();
    createExpectedTables();
    createExpectedRecords();
    database.executeUpdate("delete from " + tableName);

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage(String.format("Unexpected record count in target table '%s'", tableName));
    underTest.execute(database);
  }

  @DataProvider
  public static Object[][] expectedTableNames() {
    return EXPECTED_TABLE_NAMES.stream()
      .map(tableName -> new Object[] {tableName})
      .toArray(Object[][]::new);
  }

  private void createExpectedTables() throws SQLException {
    for (String tableName : EXPECTED_TABLE_NAMES) {
      databaseTester.createTable(String.format("create table %s (name varchar)", tableName));
    }
  }

  private void createExpectedRecords() throws SQLException {
    createRecord("users");
  }

  private void createRecord(String tableName) throws SQLException {
    databaseTester.getDatabase().executeUpdate(String.format("insert into %s values ('foo')", tableName));
  }
}

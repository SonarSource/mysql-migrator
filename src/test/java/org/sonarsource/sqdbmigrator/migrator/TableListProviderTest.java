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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.sonarsource.sqdbmigrator.migrator.Migrator.MigrationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;
import static org.sonarsource.sqdbmigrator.migrator.TablesAndVersionRegistry.TABLES_PER_VERSION;

@RunWith(DataProviderRunner.class)
public class TableListProviderTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester databaseTester = newTester();

  private final TableListProvider underTest = new TableListProvider();

  @Test
  @UseDataProvider("unknownVersionSamples")
  public void fail_if_database_version_is_not_in_the_known_list(int unknownVersion) throws SQLException {
    databaseTester
      .createSchemaMigrations()
      .addVersion(1)
      .addVersion(unknownVersion);

    expectedException.expect(MigrationException.class);
    expectedException.expectMessage("Unknown schema version; cannot match to a SonarQube release: " + unknownVersion);
    underTest.get(databaseTester.getDatabase());
  }

  @DataProvider
  public static Object[][] unknownVersionSamples() {
    return new Object[][] {
      {2},
      {1922},
      {1924},
    };
  }

  @Test
  @UseDataProvider("knownVersions")
  public void find_list_if_tables_when_version_is_in_the_known_list(int knownVersion) throws SQLException {
    databaseTester
      .createSchemaMigrations()
      .addVersion(1)
      .addVersion(knownVersion);

    assertThat(underTest.get(databaseTester.getDatabase()))
      .hasSizeGreaterThan(1)
      .contains("projects");
  }

  @DataProvider
  public static Object[][] knownVersions() {
    return TABLES_PER_VERSION.keySet().stream()
      .map(k -> new Object[] {k})
      .toArray(Object[][]::new);
  }
}

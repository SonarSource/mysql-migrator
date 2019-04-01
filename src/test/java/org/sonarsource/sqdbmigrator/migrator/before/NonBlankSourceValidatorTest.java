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

import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.sql.SQLException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.DatabaseTester;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks.PreMigrationException;

import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;

@RunWith(DataProviderRunner.class)
public class NonBlankSourceValidatorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester databaseTester = newTester();

  private final NonBlankSourceValidator underTest = new NonBlankSourceValidator();

  @Test
  public void pass_when_source_database_has_projects() throws SQLException {
    Database database = databaseTester.getDatabase();
    database.executeUpdate("create table projects (name varchar)");
    database.executeUpdate("insert into projects values ('foo')");

    underTest.execute(database);
  }

  @Test
  public void fail_when_cannot_get_record_count_from_projects() {
    Database database = databaseTester.getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Could not get record count in projects table of the source database:");
    underTest.execute(database);
  }

  @Test
  public void fail_when_source_database_doesnt_have_projects() throws SQLException {
    Database database = databaseTester.getDatabase();
    database.executeUpdate("create table projects (name varchar)");

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("There are no records in the projects table of the source database. Did you mix up -source and -target parameters? Aborting migration.");
    underTest.execute(database);
  }
}

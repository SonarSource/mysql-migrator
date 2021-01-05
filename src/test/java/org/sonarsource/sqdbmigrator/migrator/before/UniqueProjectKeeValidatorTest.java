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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.DatabaseTester;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks.PreMigrationException;

import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;

public class UniqueProjectKeeValidatorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester databaseTester = newTester();

  private final UniqueProjectKeeValidator underTest = new UniqueProjectKeeValidator();

  @Test
  public void pass_when_source_database_doesnt_have_duplicate_project_kees() throws SQLException {
    Database database = databaseTester.getDatabase();
    database.executeUpdate("create table projects (kee varchar)");
    database.executeUpdate("insert into projects values ('foo')");
    database.executeUpdate("insert into projects values ('bar')");

    underTest.execute(database);
  }

  @Test
  public void fail_when_cannot_verify_unique_project_kees() {
    Database database = databaseTester.getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Could not verify uniqueness of projects.kee values:");
    underTest.execute(database);
  }

  @Test
  public void fail_when_source_database_has_duplicate_project_kees() throws SQLException {
    Database database = databaseTester.getDatabase();
    database.executeUpdate("create table projects (kee varchar)");
    database.executeUpdate("insert into projects values ('foo')");
    database.executeUpdate("insert into projects values ('foo')");

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Duplicate kee values detected in projects table. Please clean the table first. Aborting migration.");
    underTest.execute(database);
  }
}

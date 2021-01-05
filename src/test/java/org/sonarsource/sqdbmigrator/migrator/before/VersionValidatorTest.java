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

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;

public class VersionValidatorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester sourceTester = newTester();

  @Rule
  public final DatabaseTester targetTester = newTester();

  private final VersionValidator underTest = new VersionValidator();

  @Test
  public void do_not_throw_when_versions_match() throws SQLException {
    Database source = sourceTester.createSchemaMigrations().addVersion(123).getDatabase();
    Database target = targetTester.createSchemaMigrations().addVersion(123).getDatabase();
    underTest.execute(source, target);
  }

  @Test
  public void throw_when_schema_migrations_table_does_not_exist_in_source() throws SQLException {
    Database source = sourceTester.getDatabase();
    Database target = targetTester.createSchemaMigrations().addVersion(123).getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Could not determine SonarQube version of the source database. Could not select version from schema_migrations. Table \"SCHEMA_MIGRATIONS\" not found");
    underTest.execute(source, target);
  }

  @Test
  public void throw_when_schema_migrations_table_does_not_exist_in_target() throws SQLException {
    Database source = sourceTester.createSchemaMigrations().addVersion(123).getDatabase();
    Database target = targetTester.getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Could not determine SonarQube version of the target database. Could not select version from schema_migrations. Table \"SCHEMA_MIGRATIONS\" not found");
    underTest.execute(source, target);
  }

  @Test
  public void throw_when_schema_migrations_table_is_empty_in_source() throws SQLException {
    Database source = sourceTester.createSchemaMigrations().getDatabase();
    Database target = targetTester.createSchemaMigrations().addVersion(123).getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Could not determine SonarQube version of the source database. The schema_migrations table must not be empty");
    underTest.execute(source, target);
  }

  @Test
  public void throw_when_schema_migrations_table_is_empty_in_target() throws SQLException {
    Database source = sourceTester.createSchemaMigrations().addVersion(123).getDatabase();
    Database target = targetTester.createSchemaMigrations().getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Could not determine SonarQube version of the target database. The schema_migrations table must not be empty");
    underTest.execute(source, target);
  }

  @Test
  public void throw_when_source_version_is_lower_than_target() throws SQLException {
    Database source = sourceTester.createSchemaMigrations().addVersion(123).getDatabase();
    Database target = targetTester.createSchemaMigrations().addVersion(456).getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Versions in source and target database don't match: 123 != 456");
    underTest.execute(source, target);
  }

  @Test
  public void throw_when_target_version_is_lower_than_source() throws SQLException {
    Database source = sourceTester.createSchemaMigrations().addVersion(456).getDatabase();
    Database target = targetTester.createSchemaMigrations().addVersion(123).getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Versions in source and target database don't match: 456 != 123");
    underTest.execute(source, target);
  }

  @Test
  public void throw_when_version_is_malformed() throws SQLException {
    Database source = sourceTester.createSchemaMigrations().addVersion("foo").getDatabase();
    Database target = targetTester.createSchemaMigrations().addVersion(123).getDatabase();

    expectedException.expect(PreMigrationException.class);
    expectedException.expectMessage("Malformed version: 'foo'; expected integer value");
    underTest.execute(source, target);
  }

  @Test
  public void selectVersion_finds_highest_numeric_version() throws SQLException {
    Database database = sourceTester.createSchemaMigrations()
      .addVersion(12)
      .addVersion(123)
      .addVersion(19)
      .getDatabase();
    assertThat(underTest.selectVersion(database, "dummy")).isEqualTo(123);
  }
}

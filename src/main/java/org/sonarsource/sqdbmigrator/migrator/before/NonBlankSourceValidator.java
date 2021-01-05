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
import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks.PreMigrationException;

public class NonBlankSourceValidator {
  void execute(Database database) {
    try {
      long recordCount = database.countRows("projects");
      if (recordCount == 0) {
        throw new PreMigrationException("There are no records in the projects table of the source database. " +
          "Did you mix up -source and -target parameters? Aborting migration.");
      }
    } catch (SQLException e) {
      throw new PreMigrationException("Could not get record count in projects table of the source database: %s", e.getMessage());
    }
  }
}

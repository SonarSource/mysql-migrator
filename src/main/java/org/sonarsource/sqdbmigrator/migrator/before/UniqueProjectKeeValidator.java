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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks.PreMigrationException;

/**
 * This validator is for the sake of old MySQL databases:
 * in SonarQube 5.6 the projects table didn't have a unique index,
 * and upgrading to 6.7 silently does not create the unique index
 * when duplicate values exist.
 */
public class UniqueProjectKeeValidator {
  void execute(Database database) {
    String sql = "select kee, count(1) as ct from projects group by kee having ct > 1";
    try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql);
      ResultSet rs = preparedStatement.executeQuery()) {
      if (rs.next()) {
        throw new PreMigrationException("Duplicate kee values detected in projects table. Please clean the table first. Aborting migration.");
      }
    } catch (SQLException e) {
      throw new PreMigrationException("Could not verify uniqueness of projects.kee values: %s", e.getMessage());
    }
  }
}

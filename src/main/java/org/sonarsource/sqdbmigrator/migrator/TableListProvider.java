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
import java.util.List;
import org.sonarsource.sqdbmigrator.migrator.Migrator.MigrationException;

public class TableListProvider {
  public List<String> get(Database database) {
    List<String> tables;
    try {
      tables = database.getTables();
    } catch (SQLException e) {
      throw new MigrationException("Could not determine list of tables to copy: %s", e.getMessage());
    }

    if (tables.isEmpty()) {
      throw new IllegalStateException("Could not find any tables. Expected a non-empty list to migrate.");
    }

    return tables;
  }
}

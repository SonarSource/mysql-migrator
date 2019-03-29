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

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.TableListProvider;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks.PreMigrationException;

public class TableListValidator {
  void execute(Database source, Database target, TableListProvider tableListProvider) {
    List<String> tables = tableListProvider.get(source);

    ensureTablesExist(source, "source", tables);
    ensureTablesExist(target, "target", tables);
  }

  private void ensureTablesExist(Database database, String label, List<String> tables) {
    List<String> missing = findMissingTables(database, label, tables);
    if (!missing.isEmpty()) {
      throw new PreMigrationException("Some expected tables are missing in %s database: %s", label, String.join(", ", missing));
    }
  }

  private List<String> findMissingTables(Database database, String label, List<String> tables) {
    try {
      Set<String> databaseTables = new HashSet<>(database.getTables());
      return tables.stream().filter(name -> !databaseTables.contains(name)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new PreMigrationException("Could not get list of tables from %s database: %s", label, e.getMessage());
    }
  }
}

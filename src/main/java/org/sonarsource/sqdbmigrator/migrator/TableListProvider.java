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

import java.util.List;
import org.sonarsource.sqdbmigrator.migrator.Migrator.MigrationException;

import static org.sonarsource.sqdbmigrator.migrator.TablesAndVersionRegistry.TABLES_PER_VERSION;

public class TableListProvider {

  public List<String> get(Database database) {
    int schemaVersion = database.getSchemaVersion();

    List<String> tables = TABLES_PER_VERSION.get(schemaVersion);
    if (tables == null) {
      throw new MigrationException("Unknown schema version; cannot match to a SonarQube release: " + schemaVersion);
    }

    return tables;
  }
}

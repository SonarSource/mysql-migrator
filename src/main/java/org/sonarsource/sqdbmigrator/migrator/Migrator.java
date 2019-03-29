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
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks;

public class Migrator {

  private final ConnectionConfig sourceConfig;
  private final ConnectionConfig targetConfig;
  private final TableListProvider tableListProvider;
  private final PreMigrationChecks preMigrationChecks;
  private final ContentCopier contentCopier;

  public Migrator(ConnectionConfig sourceConfig, ConnectionConfig targetConfig,
    TableListProvider tableListProvider, PreMigrationChecks preMigrationChecks, ContentCopier contentCopier) {
    this.sourceConfig = sourceConfig;
    this.targetConfig = targetConfig;
    this.tableListProvider = tableListProvider;
    this.preMigrationChecks = preMigrationChecks;
    this.contentCopier = contentCopier;
  }

  public void execute() throws SQLException {
    try (Database source = Database.create(sourceConfig); Database target = Database.create(targetConfig)) {
      preMigrationChecks.execute(source, target, tableListProvider);
      contentCopier.execute(source, target, tableListProvider, 5000);
      // TODO print stats, see issue #8
    }
  }

  static class MigrationException extends RuntimeException {
    MigrationException(String format, Object... args) {
      super(String.format(format, args));
    }
  }
}

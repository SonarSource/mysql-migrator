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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks;

public class Migrator {

  private final System2 system2;
  private final ConnectionConfig sourceConfig;
  private final ConnectionConfig targetConfig;
  private final TableListProvider tableListProvider;
  private final PreMigrationChecks preMigrationChecks;
  private final ContentCopier contentCopier;
  private final StatsRecorder statsRecorder;

  public Migrator(System2 system2, ConnectionConfig sourceConfig, ConnectionConfig targetConfig,
    TableListProvider tableListProvider, PreMigrationChecks preMigrationChecks, ContentCopier contentCopier,
    StatsRecorder statsRecorder) {
    this.system2 = system2;
    this.sourceConfig = sourceConfig;
    this.targetConfig = targetConfig;
    this.tableListProvider = tableListProvider;
    this.preMigrationChecks = preMigrationChecks;
    this.contentCopier = contentCopier;
    this.statsRecorder = statsRecorder;
  }

  public void execute() throws SQLException {
    try (Database source = Database.create(sourceConfig); Database target = Database.create(targetConfig)) {
      long started = new Date().getTime();
      preMigrationChecks.execute(source, target, tableListProvider);
      contentCopier.execute(source, target, tableListProvider, statsRecorder, 5000);
      long completed = new Date().getTime();

      system2.printlnOut(statsRecorder.formatAsTable());
      system2.printlnOut(String.format("Migration successful in %s seconds", formatSeconds(completed - started)));
    }
  }

  private static String formatSeconds(long millis) {
    return new DecimalFormat("#0.0", new DecimalFormatSymbols(Locale.US)).format(millis / 1000D);
  }

  static class MigrationException extends RuntimeException {
    MigrationException(String format, Object... args) {
      super(String.format(format, args));
    }
  }
}

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
import org.junit.Test;
import org.sonarsource.sqdbmigrator.migrator.Migrator.MigrationException;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class MigratorTest {

  @Test
  public void execute_all_steps() throws SQLException {
    System2 system2 = mock(System2.class);
    ConnectionConfig sourceConfig = new ConnectionConfig("jdbc:h2:mem:source", null, null);
    ConnectionConfig targetConfig = new ConnectionConfig("jdbc:h2:mem:target", null, null);
    TableListProvider tableListProvider = mock(TableListProvider.class);
    PreMigrationChecks preMigrationChecks = mock(PreMigrationChecks.class);
    ContentCopier contentCopier = mock(ContentCopier.class);
    StatsRecorder statsRecorder = mock(StatsRecorder.class);

    new Migrator(system2, sourceConfig, targetConfig, tableListProvider, preMigrationChecks, contentCopier, statsRecorder).execute();

    verify(preMigrationChecks).execute(any(), any(), same(tableListProvider));
    verify(contentCopier).execute(any(), any(), same(tableListProvider), any(), anyInt());
  }

  @Test
  public void do_not_reach_contentCopier_if_pre_migration_checks_failed() throws SQLException {
    System2 system2 = mock(System2.class);
    ConnectionConfig sourceConfig = new ConnectionConfig("jdbc:h2:mem:source", null, null);
    ConnectionConfig targetConfig = new ConnectionConfig("jdbc:h2:mem:target", null, null);
    TableListProvider tableListProvider = mock(TableListProvider.class);
    PreMigrationChecks failingPreMigrationChecks = mock(PreMigrationChecks.class);
    doThrow(new MigrationException("something went wrong")).when(failingPreMigrationChecks).execute(any(), any(), same(tableListProvider));
    ContentCopier contentCopier = mock(ContentCopier.class);
    StatsRecorder statsRecorder = mock(StatsRecorder.class);

    try {
      new Migrator(system2, sourceConfig, targetConfig, tableListProvider, failingPreMigrationChecks, contentCopier, statsRecorder).execute();
      fail("expected to throw");
    } catch (MigrationException ignored) {
      // nothing to do
    }
    verifyZeroInteractions(contentCopier);
  }
}

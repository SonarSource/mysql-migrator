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

import org.junit.Test;
import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.TableListProvider;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks.PreMigrationException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class PreMigrationChecksTest {

  @Test
  public void execute_all_validators() {
    Database source = mock(Database.class);
    Database target = mock(Database.class);
    TableListProvider tableListProvider = mock(TableListProvider.class);

    VersionValidator versionValidator = mock(VersionValidator.class);
    TableListValidator tableListValidator = mock(TableListValidator.class);
    BlankTargetValidator blankTargetValidator = mock(BlankTargetValidator.class);
    NonBlankSourceValidator nonBlankSourceValidator = mock(NonBlankSourceValidator.class);
    UniqueProjectKeeValidator uniqueProjectKeeValidator = mock(UniqueProjectKeeValidator.class);

    new PreMigrationChecks(versionValidator, tableListValidator, blankTargetValidator, nonBlankSourceValidator, uniqueProjectKeeValidator).execute(source, target, tableListProvider);
    verify(versionValidator).execute(source, target);
    verify(tableListValidator).execute(source, target, tableListProvider);
    verify(blankTargetValidator).execute(target);
    verify(nonBlankSourceValidator).execute(source);
    verify(uniqueProjectKeeValidator).execute(source);
  }

  @Test
  public void do_not_reach_table_list_validation_if_version_validation_failed() {
    Database source = mock(Database.class);
    Database target = mock(Database.class);
    TableListProvider tableListProvider = mock(TableListProvider.class);

    VersionValidator failingVersionValidator = mock(VersionValidator.class);
    doThrow(new PreMigrationException("something went wrong")).when(failingVersionValidator).execute(source, target);

    TableListValidator tableListValidator = mock(TableListValidator.class);

    try {
      new PreMigrationChecks(failingVersionValidator, tableListValidator, null, null, null).execute(source, target, tableListProvider);
      fail("expected to throw");
    } catch (PreMigrationException ignored) {
      // nothing to do
    }
    verifyZeroInteractions(tableListValidator);
  }
}

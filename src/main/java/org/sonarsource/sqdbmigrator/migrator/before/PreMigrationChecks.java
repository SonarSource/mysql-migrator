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

import org.sonarsource.sqdbmigrator.migrator.Database;
import org.sonarsource.sqdbmigrator.migrator.TableListProvider;

public class PreMigrationChecks {

  private final VersionValidator versionValidator;
  private final TableListValidator tableListValidator;
  private final BlankTargetValidator blankTargetValidator;
  private final NonBlankSourceValidator nonBlankSourceValidator;
  private final UniqueProjectKeeValidator uniqueProjectKeeValidator;

  public PreMigrationChecks(VersionValidator versionValidator, TableListValidator tableListValidator,
    BlankTargetValidator blankTargetValidator, NonBlankSourceValidator nonBlankSourceValidator,
    UniqueProjectKeeValidator uniqueProjectKeeValidator) {
    this.versionValidator = versionValidator;
    this.tableListValidator = tableListValidator;
    this.blankTargetValidator = blankTargetValidator;
    this.nonBlankSourceValidator = nonBlankSourceValidator;
    this.uniqueProjectKeeValidator = uniqueProjectKeeValidator;
  }

  public void execute(Database source, Database target, TableListProvider tableListProvider) {
    versionValidator.execute(source, target);

    tableListValidator.execute(source, target, tableListProvider);

    blankTargetValidator.execute(target);

    nonBlankSourceValidator.execute(source);

    uniqueProjectKeeValidator.execute(source);
  }

  static class PreMigrationException extends RuntimeException {
    PreMigrationException(String format, Object... args) {
      super(String.format(format, args));
    }
  }
}

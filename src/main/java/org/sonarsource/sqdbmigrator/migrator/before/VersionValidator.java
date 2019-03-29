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

public class VersionValidator {
  void execute(Database source, Database target) {
    int sourceVersion = selectVersion(source, "source");
    int targetVersion = selectVersion(target, "target");

    if (sourceVersion != targetVersion) {
      throw new PreMigrationException("Versions in source and target database don't match: %s != %s", sourceVersion, targetVersion);
    }
  }

  int selectVersion(Database database, String label) {
    String sql = "select version from schema_migrations";
    String versionString = null;
    int version = -1;

    try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
         ResultSet rs = statement.executeQuery()) {
      // note: for lack of a standard way to sort int-valued strings, manually computing max value
      while (rs.next()) {
        versionString = rs.getString(1);
        version = Math.max(version, Integer.parseInt(versionString));
      }
    } catch (SQLException e) {
      throw new PreMigrationException("Could not determine SonarQube version of the %s database. %s", label, e.getMessage());
    } catch (NumberFormatException e) {
      throw new PreMigrationException("Malformed version: '%s'; expected integer value", versionString);
    }

    if (version < 0) {
      throw new PreMigrationException("schema_migrations table must not be empty in %s database", label);
    }

    return version;
  }
}

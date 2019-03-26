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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Migrator {

  private final System2 system2;
  private final ConnectionConfig sourceConfig;
  private final ConnectionConfig targetConfig;

  public Migrator(System2 system2, ConnectionConfig sourceConfig, ConnectionConfig targetConfig) {
    this.system2 = system2;
    this.sourceConfig = sourceConfig;
    this.targetConfig = targetConfig;
  }

  public void execute() throws SQLException {
    execute(sourceConfig);
    execute(targetConfig);
  }

  private void execute(ConnectionConfig config) throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", config.username);
    properties.setProperty("password", config.password);
    try (Connection unused = DriverManager.getConnection(config.url, properties)) {
      system2.printlnOut("Connection test successful!");
    } catch (SQLException e) {
      system2.printlnErr("Connection test failed!");
      throw e;
    }
  }
}

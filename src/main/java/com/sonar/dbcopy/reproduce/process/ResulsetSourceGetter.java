/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2013-2019 SonarSource SA
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
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResulsetSourceGetter {

  private String tableName;

  public ResulsetSourceGetter(String tableName) {
    this.tableName = tableName;
  }

  public Statement createAndReturnStatementSource(Connection connectionSource) {
    try {
      return connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when creating statement on TABLE source: " + tableName, e);
    }
  }

  public ResultSet createAndReturnResultSetSource(Statement statementSource) {
    try {
      return statementSource.executeQuery("SELECT * FROM " + tableName);
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when executing the sql select request on TABLE source: " + tableName, e);
    }
  }
}


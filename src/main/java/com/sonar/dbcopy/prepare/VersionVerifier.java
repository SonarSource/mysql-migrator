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
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class VersionVerifier {

  private int maxVersionId = 0;

  public int lastVersionId(ConnecterData cd) {
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    Closer closer = new Closer("ConnectionVerifier");
    try {
      Class.forName(cd.getDriver());
      connection = DriverManager.getConnection(cd.getUrl(), cd.getUser(), cd.getPwd());
      statement = connection.createStatement();

      String tableNameSchemaMigration = CharacteristicsRelatedToEditor.transfromCaseOfTableName(connection.getMetaData(),
          "schema_migrations");
      resultSet = statement.executeQuery("SELECT version FROM " + tableNameSchemaMigration);

      while (resultSet.next()) {
        String versionString = resultSet.getString(1);
        int versionInteger = Integer.parseInt(versionString);
        if (versionInteger > maxVersionId) {
          maxVersionId = versionInteger;
        }
      }
      return maxVersionId;

    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when verifying version database. Please build your destination database with SonarQube at the same SonarQube source version.", e);
    } catch (ClassNotFoundException e) {
      throw new MessageException("Driver " + cd.getDriver() + " does not exist. Class not found: " + e.getMessage());
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
      closer.closeConnection(connection);
    }
  }
}

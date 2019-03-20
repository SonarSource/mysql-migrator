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

import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DestinationStatementBuilder {

  public PreparedStatement getDestinationStatement(Connection connectionDestination, Table tableSource) {
    PreparedStatement preparedStatementDest;
    try {
      String tableSourceName = CharacteristicsRelatedToEditor.transfromCaseOfTableName(
          connectionDestination.getMetaData()
          , tableSource.getName());

      String sqlInsertRequest = "INSERT INTO " + tableSourceName + " (" + tableSource.getColumnNamesAsString() + ") VALUES(" + tableSource.getQuestionMarksAsString() + ")";

      preparedStatementDest = connectionDestination.prepareStatement(sqlInsertRequest);
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when buiding destination prepared statement", e);
    }
    return preparedStatementDest;
  }
}


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
package com.sonar.dbcopy.reproduce.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DefaultWriter implements WriterTool {
  private PreparedStatement destinationStatement;

  public DefaultWriter(PreparedStatement destinationStatement) {
    this.destinationStatement = destinationStatement;
  }

  @Override
  public void writeTimestamp(Timestamp timestamp, int indexColumn) throws SQLException {
    destinationStatement.setTimestamp(indexColumn + 1, timestamp);
  }

  @Override
  public void writeBlob(byte[] byteArray, int indexColumn) throws SQLException {
    destinationStatement.setBytes(indexColumn + 1, byteArray);
  }

  @Override
  public void writeClob(String stringAsclob, int indexColumn) throws SQLException {
    destinationStatement.setString(indexColumn + 1, stringAsclob);
  }

  @Override
  public void writeBoolean(boolean bool, int indexColumn) throws SQLException {
    destinationStatement.setBoolean(indexColumn + 1, bool);
  }

  @Override
  public void writeObject(Object object, int indexColumn) throws SQLException {
    destinationStatement.setObject(indexColumn + 1, object);
  }

  @Override
  public void writeVarchar(String string, int indexColumn) throws SQLException {
    destinationStatement.setString(indexColumn + 1, string);
  }

  @Override
  public void writeWhenNull(int indexColumn) throws SQLException {
    destinationStatement.setObject(indexColumn + 1, null);
  }
}

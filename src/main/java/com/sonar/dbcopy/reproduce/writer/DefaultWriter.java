/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DefaultWriter {
  private PreparedStatement destinationStatement;

  public DefaultWriter(PreparedStatement destinationStatement) {
    this.destinationStatement = destinationStatement;
  }

  public void writeTimestamp(Timestamp timestamp, int indexColumn) throws SQLException {
    destinationStatement.setTimestamp(indexColumn + 1, timestamp);
  }

  public void writeBlob(byte[] byteArray, int indexColumn) throws SQLException {
    destinationStatement.setBytes(indexColumn + 1, byteArray);
  }

  public void writeClob(String stringAsclob, int indexColumn) throws SQLException {
    destinationStatement.setString(indexColumn + 1, stringAsclob);
  }

  public void writeBoolean(boolean bool, int indexColumn) throws SQLException {
    destinationStatement.setBoolean(indexColumn + 1, bool);
  }

  public void writeObject(Object object, int indexColumn) throws SQLException {
    destinationStatement.setObject(indexColumn + 1, object);
  }

  public void writeVarchar(String string, int indexColumn) throws SQLException {
    destinationStatement.setString(indexColumn + 1, string);
  }

  public void writeWhenNull(int indexColumn) throws SQLException {
    destinationStatement.setObject(indexColumn + 1, null);
  }

}

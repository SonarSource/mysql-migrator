/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MySqlWriter implements WriterTool {

  private PreparedStatement destinationStatement;

  public MySqlWriter(PreparedStatement destinationStatement) {
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
  public void writeClob(byte[] byteArray, int indexColumn) throws SQLException {
    destinationStatement.setBytes(indexColumn + 1, byteArray);
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


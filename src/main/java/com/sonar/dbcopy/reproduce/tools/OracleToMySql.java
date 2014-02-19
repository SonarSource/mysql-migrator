/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class OracleToMySql implements CopierTool {

  private PreparedStatement destinationStatement;

  public OracleToMySql(PreparedStatement destinationStatement) {
    this.destinationStatement = destinationStatement;
  }

  @Override
  public void copyTimestamp(ResultSet resultSetSource, int indexColumn) throws SQLException {
    Timestamp objTimestamp = resultSetSource.getTimestamp(indexColumn + 1);
    destinationStatement.setTimestamp(indexColumn + 1, objTimestamp);
  }

  @Override
  public void copyBlob(ResultSet resultSetSource, int indexColumn) throws SQLException {
    copy(resultSetSource, indexColumn);

  }

  @Override
  public void copyClob(ResultSet resultSetSource, int indexColumn) throws SQLException {
    copy(resultSetSource, indexColumn);

  }

  @Override
  public void copyBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException {
    copy(resultSetSource, indexColumn);

  }

  @Override
  public void copy(ResultSet resultSetSource, int indexColumn) throws SQLException {
    Object object = resultSetSource.getObject(indexColumn + 1);
    destinationStatement.setObject(indexColumn + 1, object);
  }

  @Override
  public void copyWhenNull(int indexColumn) throws SQLException {
    destinationStatement.setObject(indexColumn + 1, null);
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;

public class SqlServerToOracle implements CopierTool {

  private PreparedStatement destinationStatement;

  public SqlServerToOracle(PreparedStatement destinationStatement) {
    this.destinationStatement = destinationStatement;
  }

  @Override
  public void copyTimestamp(ResultSet resultSetSource, int indexColumn) throws SQLException {
    copy(resultSetSource, indexColumn);
  }

  @Override
  public void copyBlob(ResultSet resultSetSource, int indexColumn) throws IOException, SQLException {
    Blob blobObj = resultSetSource.getBlob(indexColumn + 1);
    InputStream inputStream = blobObj.getBinaryStream();
    destinationStatement.setBlob(indexColumn + 1, inputStream);
    inputStream.close();
  }

  @Override
  public void copyClob(ResultSet resultSetSource, int indexColumn) throws IOException, SQLException {
    Clob clobObj = resultSetSource.getClob(indexColumn + 1);
    Reader reader = clobObj.getCharacterStream();
    destinationStatement.setClob(indexColumn + 1, reader);
    reader.close();
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

  @Override
  public void copyVarchar(ResultSet resultSetSource, int indexColumn) throws SQLException {
    String stringToinsert = resultSetSource.getString(indexColumn + 1);
    stringToinsert = stringToinsert.replace("\u0000", "");
    destinationStatement.setString(indexColumn + 1, stringToinsert);
  }
}

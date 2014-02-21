/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class OracleToPostgresql implements CopierTool {

  private PreparedStatement destinationStatement;

  public OracleToPostgresql(PreparedStatement destinationStatement) {
    this.destinationStatement = destinationStatement;
  }

  @Override
  public void copyTimestamp(ResultSet resultSetSource, int indexColumn) throws SQLException {
    Timestamp objTimestamp = resultSetSource.getTimestamp(indexColumn + 1);
    destinationStatement.setTimestamp(indexColumn + 1, objTimestamp);
  }

  @Override
  public void copyBlob(ResultSet resultSetSource, int indexColumn) throws IOException, SQLException {
    InputStream inputStreamObj = resultSetSource.getBinaryStream(indexColumn + 1);
    byte[] buffer = new byte[8192];
    int bytesRead;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    while ((bytesRead = inputStreamObj.read(buffer)) != -1) {
      output.write(buffer, 0, bytesRead);
    }
    byte[] bytesToInsert = output.toByteArray();
    destinationStatement.setBytes(indexColumn + 1, bytesToInsert);
    inputStreamObj.close();
  }

  @Override
  public void copyClob(ResultSet resultSetSource, int indexColumn) throws SQLException {
    copy(resultSetSource, indexColumn);
  }

  @Override
  public void copyBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException {
    int integerObj = resultSetSource.getInt(indexColumn + 1);
    boolean booleanToInsertInPostgresql = false;
    if (integerObj == 1) {
      booleanToInsertInPostgresql = true;
    }
    destinationStatement.setBoolean(indexColumn + 1, booleanToInsertInPostgresql);
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

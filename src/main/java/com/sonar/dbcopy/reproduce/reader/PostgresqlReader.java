/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.reader;

import com.sonar.dbcopy.utils.toolconfig.SqlDbException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PostgresqlReader implements ReaderTool {
  @Override
  public Timestamp readTimestamp(ResultSet resultSetSource, int indexColumn) throws SQLException {
    return resultSetSource.getTimestamp(indexColumn + 1);
  }

  @Override
  public byte[] readBlob(ResultSet resultSetSource, int indexColumn) throws SQLException {
    InputStream inputStreamObj = resultSetSource.getBinaryStream(indexColumn + 1);
    byte[] buffer = new byte[8192];
    int bytesRead;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      while ((bytesRead = inputStreamObj.read(buffer)) != -1) {
        output.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      throw new SqlDbException("Problem to get bytes when reading Blob", e);
    }
    return output.toByteArray();
  }

  @Override
  public String readClob(ResultSet resultSetSource, int indexColumn) throws SQLException {
    return resultSetSource.getString(indexColumn + 1);
  }

  @Override
  public boolean readBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException {
    return resultSetSource.getBoolean(indexColumn + 1);
  }

  @Override
  public Object readObject(ResultSet resultSetSource, int indexColumn) throws SQLException {
    return resultSetSource.getObject(indexColumn + 1);
  }

  @Override
  public String readVarchar(ResultSet resultSetSource, int indexColumn) throws SQLException {
    String stringToinsert = resultSetSource.getString(indexColumn + 1);
    return stringToinsert.replace("\u0000", "");
  }
}


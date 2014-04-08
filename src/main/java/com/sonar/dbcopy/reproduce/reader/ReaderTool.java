/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.reader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public interface ReaderTool {

  public Timestamp readTimestamp(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public byte[] readBlob(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public String readClob(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public boolean readBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public Object readObject(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public String readVarchar(ResultSet resultSetSource, int indexColumn) throws SQLException;
}


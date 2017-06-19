/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.writer;

import java.sql.SQLException;
import java.sql.Timestamp;

public interface WriterTool {

  public void writeTimestamp(Timestamp timestamp, int indexColumn) throws SQLException;

  public void writeBlob(byte[] byteArray, int indexColumn) throws SQLException;

  public void writeClob(String stringAsclob, int indexColumn) throws SQLException;

  public void writeBoolean(boolean bool, int indexColumn) throws SQLException;

  public void writeObject(Object object, int indexColumn) throws SQLException;

  public void writeVarchar(String string, int indexColumn) throws SQLException;

  public void writeWhenNull(int indexColumn) throws SQLException;
}


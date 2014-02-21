/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.tools;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface CopierTool {

  public void copyTimestamp(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public void copyBlob(ResultSet resultSetSource, int indexColumn) throws SQLException, IOException;

  public void copyClob(ResultSet resultSetSource, int indexColumn) throws SQLException, IOException;

  public void copyBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public void copy(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public void copyWhenNull(int indexColumn) throws SQLException;

  public void copyVarchar(ResultSet resultSetSource , int indexColumn) throws SQLException ;

  }

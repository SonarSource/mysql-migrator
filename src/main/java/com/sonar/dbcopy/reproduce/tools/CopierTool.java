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

  /* Dont forget to build object with a PreparedStatement from destination
  *
  * private PreparedStatement destStatement;
  * public CopierTool(PreparedStatement destStatement){
  *   this.destStatement = destStatement;
  * }
   *
  * */

  public void copyTimestamp(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public void copyBlob(ResultSet resultSetSource, int indexColumn) throws SQLException, IOException;

  public void copyClob(ResultSet resultSetSource, int indexColumn) throws SQLException, IOException;

  public void copyBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public void copy(ResultSet resultSetSource, int indexColumn) throws SQLException;

  public void copyWhenNull(int indexColumn) throws SQLException;

}

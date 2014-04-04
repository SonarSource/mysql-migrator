/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.reader;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresqlReader extends DefaultReader implements ReaderTool {

  @Override
  public boolean readBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException {
    return resultSetSource.getBoolean(indexColumn + 1);
  }
}


/*
 * Copyright (C) 2013-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.reader;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlServerReader extends DefaultReader {

  @Override
  public boolean readBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException {
    byte byteToConvert = resultSetSource.getByte(indexColumn + 1);
    boolean boolToReturn = true;
    if (byteToConvert == 0) {
      boolToReturn = false;
    }
    return boolToReturn;
  }
}


/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.reader;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleReader extends DefaultReader {

  @Override
  public boolean readBoolean(ResultSet resultSetSource, int indexColumn) throws SQLException {
    int integerObj = resultSetSource.getInt(indexColumn + 1);
    boolean booleanToInsert = false;
    if (integerObj == 1) {
      booleanToInsert = true;
    }
    return booleanToInsert;
  }
}

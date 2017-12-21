/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class ModifySqlServerOption {

  public void modifyIdentityInsert(Connection connection, String tableName, String onOroff) throws SQLException {
    Closer closer = new Closer("ModifySqlServerOption");
    ResultSet resultSet = null;
    Statement statement = null;
    try {
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      resultSet = databaseMetaData.getPrimaryKeys(null, null, tableName);

      if (resultSet.isBeforeFirst()) {
        closer.closeResultSet(resultSet);

        statement = connection.createStatement();
        String request = String.format("SET IDENTITY_INSERT %s %s ;", tableName, onOroff);
        statement.execute(request);
        closer.closeStatement(statement);
      }
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
  }
}

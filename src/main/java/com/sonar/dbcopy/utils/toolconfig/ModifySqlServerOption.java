/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import java.sql.*;

public class ModifySqlServerOption {

  public void modifyIdentityInsert(Connection connection, String tableName, String onOroff) {
    Closer closer = new Closer("ModifySqlServerOption");
    ResultSet resultSet = null;
    Statement statement = null;
    try {
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      resultSet = databaseMetaData.getPrimaryKeys(null, null, tableName);

      if (resultSet.isBeforeFirst()) {
        closer.closeResultSet(resultSet);

        statement = connection.createStatement();
        String request = "SET IDENTITY_INSERT " + tableName + " " + onOroff + " ;";
        statement.execute(request);
        closer.closeStatement(statement);
      }
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem to SET IDENTITY_INSERT at " + onOroff + " in database Sqlserver for TABLE : " + tableName, e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
  }
}

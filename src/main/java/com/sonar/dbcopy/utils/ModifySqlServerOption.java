/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils;

import java.sql.*;

public class ModifySqlServerOption {

  public void modifyIdentityInsert(Connection connection, String tableName, String onOroff) {
    Closer closer = new Closer("ModifySqlServerOption");
    ResultSet rs = null;
    Statement statement = null;
    try {
      DatabaseMetaData dm = connection.getMetaData();
      rs = dm.getPrimaryKeys(null, null, tableName);

      if (rs.isBeforeFirst()) {
        closer.closeResultSet(rs);

        statement = connection.createStatement();
        String request = "SET IDENTITY_INSERT " + tableName + " " + onOroff + " ;";
        statement.execute(request);
        closer.closeStatement(statement);
      }
    } catch (SQLException e) {
      throw new DbException("Problem to SET IDENTITY_INSERT at " + onOroff + " in database Sqlserver for TABLE : " + tableName, e);
    } finally {
      closer.closeResultSet(rs);
      closer.closeStatement(statement);
    }

  }
}

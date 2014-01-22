/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.*;

public class ModifySqlServerOption {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  public void modifyIdentityInsert(Connection connection, String tableName, String onOroff) throws SQLException {
    Closer closer = new Closer("ModifySqlServerOption");

    DatabaseMetaData dm = connection.getMetaData();
    ResultSet rs = dm.getPrimaryKeys(null, null, tableName);

    if (rs.isBeforeFirst()) {
      closer.closeResultSet(rs);

      Statement statement = connection.createStatement();
      String request = "SET IDENTITY_INSERT " + tableName + " " + onOroff + " ;";
      statement.execute(request);
      closer.closeStatement(statement);
    }

  }
}

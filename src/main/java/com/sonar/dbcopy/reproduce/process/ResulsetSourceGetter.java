/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResulsetSourceGetter {

  private String tableName;

  public ResulsetSourceGetter(String tableName) {
    this.tableName = tableName;
  }

  public Statement createAndReturnStatementSource(Connection connectionSource) {
    try {
      return connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when creating statement on TABLE source: " + tableName, e);
    }
  }

  public ResultSet createAndReturnResultSetSource(Statement statementSource) {
    try {
      return statementSource.executeQuery("SELECT * FROM " + tableName);
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when executing the sql select request on TABLE source: " + tableName, e);
    }
  }
}


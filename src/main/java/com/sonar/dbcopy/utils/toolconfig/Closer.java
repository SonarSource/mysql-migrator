/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Closer {

  private String callerClass;

  public Closer(String callerClass) {
    this.callerClass = callerClass;
  }

  public void closeResultSet(ResultSet r) {
    try {
      if (r != null && !r.isClosed()) {
        r.close();
      }
    } catch (SQLException e) {
      throwSqlDbCopyException(e);
    }
  }

  public void closeStatement(Statement s) {
    try {
      if (s != null && !s.isClosed()) {
        s.close();
      }
    } catch (SQLException e) {
      throwSqlDbCopyException(e);
    }
  }

  public void closeConnection(Connection c) {
    try {
      if (c != null && !c.isClosed()) {
        c.close();
      }
    } catch (SQLException e) {
      throwSqlDbCopyException(e);
    }
  }

  private void throwSqlDbCopyException(Exception e) {
    throw new SqlDbCopyException("Connection from " + callerClass + " had problem to be closed.", e);
  }
}

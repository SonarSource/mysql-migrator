/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

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
      throw new DbException("Resultset from " + callerClass + " had problem to be closed.", e);
    }
  }

  public void closeStatement(Statement s) {
    try {
      if (s != null && !s.isClosed()) {
        s.close();
      }
    } catch (SQLException e) {
      throw new DbException("Statement from " + callerClass + " had problem to be closed.", e);
    }
  }

  public void closeConnection(Connection c) {
    try {
      if (c != null && !c.isClosed()) {
        c.close();
      }
    } catch (SQLException e) {
      throw new DbException("Connection from " + callerClass + " had problem to be closed.", e);
    }
  }
}

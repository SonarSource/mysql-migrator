/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;

public class SimpleConnection {

  private Connection connection;

  public SimpleConnection() {
  }
  /* SETTERS */
  public Connection openConnection(String driver, String url,String user,String pwd) {
    try {
      Class.forName(driver);
      connection = DriverManager.getConnection(url, user, pwd);
    } catch (SQLException e){
      throw new DbException("Open connection failed.",e);
    } catch (ClassNotFoundException e){
      throw new DbException("impossible to get the jdbc Driver.",e);
    }
    return connection;
  }
  /* CLOSERS */
  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new DbException("Closing of connection failed.",e);
    }
  }
}

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
  public Connection openConnection(String driver, String url,String user,String pwd) throws SQLException, ClassNotFoundException {
    Class.forName(driver);
    connection = DriverManager.getConnection(url, user, pwd);
    return connection;
  }
  /* CLOSERS */
  public void closeConnection() throws SQLException {
    connection.close();
  }

}

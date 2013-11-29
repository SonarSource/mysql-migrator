/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;

public class SimpleConnection {
  private Connection connection;
  private Statement statement;

  public SimpleConnection() { }
  /* SETTERS */
  public Statement doStatement() throws ClassNotFoundException, SQLException {
    statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    return statement;
  }
  public void doConnection(String driver, String url,String user,String pwd) throws ClassNotFoundException, SQLException {
    Class.forName(driver);
    connection =DriverManager.getConnection(url, user, pwd);
  }
  /* GETTERS */
  public Connection getConnection(){
    return connection;
  }
  public Statement getStatement(){
    return statement;
  }
  /* CLOSER */
  public void closeStatement()throws SQLException {
    statement.close();
  }
  public void closeConnection() throws SQLException {
    connection.close();
  }

}

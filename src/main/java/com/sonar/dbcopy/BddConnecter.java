/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BddConnecter {

  private SimpleConnection sourceConnection;
  private Statement sourceStatement;
  private Connection connectionToPreparedStatement;
  private SimpleConnection destConnection;

  public BddConnecter(){ }

  /* SOURCE CONNECTION */
  public void doSourceConnectionAndStatement(String driver, String urlSource, String user, String pwd)throws SQLException, ClassNotFoundException{
    sourceConnection = new SimpleConnection();
    sourceConnection.doConnection(driver, urlSource, user, pwd);
    sourceStatement = sourceConnection.doStatement();
   }
  /* DESTINATION CONNECTION */
  public void doOnlyDestinationConnection(String driver, String urlDest, String user, String pwd)throws SQLException, ClassNotFoundException{
    destConnection = new SimpleConnection();
    destConnection.doConnection(driver, urlDest, user, pwd);
    connectionToPreparedStatement=destConnection.getConnection();
  }
  /* GETTERS */
  public Statement getStatementSource(){
    return sourceStatement;
  }
  public Connection getConnectionDest(){
    return connectionToPreparedStatement;
  }
  /* CLOSE METHODS */
  public void closeSourceConnection()throws SQLException{
    sourceConnection.closeStatement();
    sourceConnection.closeConnection();
  }
  public void closeDestConnection()throws SQLException{
    destConnection.closeConnection();
  }
}

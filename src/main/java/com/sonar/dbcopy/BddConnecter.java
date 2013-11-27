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

  private ConnectionParameters sourceConnectionParameters,destConnectionParameters;
  private SimpleConnection sourceConnection;
  protected Statement sourceStatement;
  private Connection connectionToPreparedStatement;
  private SimpleConnection destConnection;

  public BddConnecter(){
      sourceConnectionParameters = new ConnectionParameters("source");
      destConnectionParameters = new ConnectionParameters("destination");
  }

  /* SOURCE CONNECTION */
  public void doSourceConnectionAndStatement()throws SQLException, ClassNotFoundException{
    sourceConnection = new SimpleConnection();
    sourceConnection.addParamConnection(sourceConnectionParameters);
    sourceConnection.doConnection();
    sourceStatement = sourceConnection.getStatement();
   }
  /* DESTINATION CONNECTION */
  public void doOnlyDestinationConnection ()throws SQLException, ClassNotFoundException{
    destConnection = new SimpleConnection();
    destConnection.addParamConnection(destConnectionParameters);
    destConnection.doConnection();
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

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.Connection;

public class BddConnecter {

  private SimpleConnection simpleSourceConnection,simpleDestConnection;
  private Connection sourceConnection, destConnection;

  public BddConnecter(){
  }

  /* DO CONNECTION */
  public void doSourceConnection(String driver, String urlSource, String user, String pwd){
    simpleSourceConnection = new SimpleConnection();
    sourceConnection = simpleSourceConnection.openConnection(driver, urlSource, user, pwd);
   }
  /* DESTINATION CONNECTION */
  public void doDestinationConnection(String driver, String urlDest, String user, String pwd) {
    simpleDestConnection = new SimpleConnection();
    destConnection = simpleDestConnection.openConnection(driver, urlDest, user, pwd);
  }
  /* GETTERS */
  public Connection getSourceConnection(){
    return sourceConnection;
  }
  public Connection getDestConnection(){
    return destConnection;
  }
  public SimpleConnection getSimpleSourceConnection(){
    return simpleSourceConnection;
  }
  public SimpleConnection getSimpleDestConnection(){
    return simpleDestConnection;
  }
  /* CLOSE METHODS */
  public void closeSourceConnection(){
    simpleSourceConnection.closeConnection();
  }
  public void closeDestConnection(){
    simpleDestConnection.closeConnection();
  }
}

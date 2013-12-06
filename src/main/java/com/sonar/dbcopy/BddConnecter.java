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

  private SimpleConnection sourceSimpleConnection,destSimpleConnection;
  private Connection sourceConnection, destConnection;

  public BddConnecter(){
  }

  /* DO CONNECTION */
  public void doSourceConnection(String driver, String urlSource, String user, String pwd)throws SQLException, ClassNotFoundException{
    SimpleConnection sourceSimpleConnection = new SimpleConnection();
    sourceConnection = sourceSimpleConnection.openConnection(driver, urlSource, user, pwd);
   }
  /* DESTINATION CONNECTION */
  public void doDestinationConnection(String driver, String urlDest, String user, String pwd)throws SQLException, ClassNotFoundException{
    SimpleConnection destSimpleConnection = new SimpleConnection();
    destConnection = destSimpleConnection.openConnection(driver, urlDest, user, pwd);
  }
  /* GETTERS */
   public Connection getSourceConnection(){
     return sourceConnection;
   }
  public Connection getDestConnection(){
    return destConnection;
  }
  /* CLOSE METHODS */
  public void closeSourceConnection()throws SQLException{
    sourceConnection.close();
  }
  public void closeDestConnection()throws SQLException{
    destConnection.close();
  }
}

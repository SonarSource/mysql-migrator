/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.SQLException;

public class Main {

  public static void main(String[] args) {
     /*
      DRIVER :    args[0] = org.postgresql.Driver
      URLSOURCE : args[1] = jdbc:postgresql://localhost:5432/sonar
      URLDEST :   args[2] = jdbc:postgresql://localhost:5432/sonarToWrite
      USER :      args[3] = sonar
      PASSWORD :  args[4] = sonar
      */
    try{
      /* BUILD DB OBJECT */
      BddBuider bddBuider = new BddBuider();

      /* DO CONNECTION */
      BddConnecter bddConnecter = new BddConnecter();
      bddConnecter.doSourceConnectionAndStatement(args[0],args[1],args[3],args[4]);
      bddConnecter.doOnlyDestinationConnection(args[0],args[2],args[3],args[4]);

      /* DO COPY */
      new BddDataReproducer(bddConnecter,bddBuider.getBdd());

      /* DO VERIFYING */
      // TODO VERIFY THAT CONTENTS ARE THE SAME BETWEEN SOURCE AND  DESTINATION DATABASES

      /* DO CLOSE CONNECTION */
      bddConnecter.closeSourceConnection();
      bddConnecter.closeDestConnection();
     }
    catch (SQLException sqle){
      System.err.println("Exception SQL : ");
      while (sqle != null) {
        String message = sqle.getMessage();
        String sqlState = sqle.getSQLState();
        int errorCode = sqle.getErrorCode();
        System.out.println("Message = "+message);
        System.out.println("SQLState = "+sqlState);
        System.out.println("ErrorCode = "+errorCode);
        sqle.printStackTrace();
        sqle = sqle.getNextException();
      }
    }
    catch (ClassNotFoundException cnfe){
      System.err.println("ClassNotFoundException : ");
      cnfe.printStackTrace();
    }
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class Main {

  private Main(){
    // Utility classes, which are a collection of static members, should not have a public constructor
  }

  public static void main(String[] args) throws Exception {
   /*
   FOR POSTGRESQL:
   ---------------
    DRIVER :    args[0] = org.postgresql.Driver
    URLSOURCE : args[1] = jdbc:postgresql://localhost:5432/sonar
    URLDEST :   args[2] = jdbc:postgresql://localhost:5432/sonarToWrite
    USER :      args[3] = sonar
    PASSWORD :  args[4] = sonar
    */

    /* BUILD DB OBJECT */
    BddBuider bddBuider = new BddBuider();
    bddBuider.addtableToBdd();

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
}

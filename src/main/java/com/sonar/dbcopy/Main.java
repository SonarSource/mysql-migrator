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
/*
    arg[i]
    0=> org.postgresql.Driver
    1=> jdbc:postgresql://localhost:5432/sonar
    2=> sonar
    3=> sonar
    4=> com.mysql.jdbc.Driver
    5=> jdbc:mysql://localhost:13306/sonar
    6=> sonar
    7=> sonar
*/


  public static void main(String[] args) throws Exception {


    /* BUILD DB OBJECT */
    BddBuider bddBuider = new BddBuider();
    //bddBuider.addTableToBdd();

    /* DO CONNECTION */
    BddConnecter bddConnecter = new BddConnecter();
    bddConnecter.doSourceConnection(args[0],args[1],args[2],args[3]);
    bddConnecter.doDestinationConnection(args[4],args[5],args[6],args[7]);
    //TODO only for tries => to remove
    //bddConnecter.doSourceConnection("com.mysql.jdbc.Driver","jdbc:mysql://localhost:13306/sonar",args[2],args[3]);
    //bddConnecter.doDestinationConnection("org.postgresql.Driver","jdbc:postgresql://localhost:5432/sonarToWrite",args[6],args[7]);

    /* BUID SCHEMA DB DEST */
    MetadataGetter metadataGetter = new MetadataGetter();
    metadataGetter.getSchemaOfBddSource(bddConnecter.getSourceConnection(),bddBuider.getBdd());
    //metadataGetter.addSchemaToBddDest(bddConnecter.getDestConnection(),bddBuider.getBdd());

    /* DO COPY */
    new BddDataReproducer(bddConnecter,bddBuider.getBdd());

    /* DO VERIFYING */
    // TODO VERIFY THAT CONTENTS ARE THE SAME BETWEEN SOURCE AND  DESTINATION DATABASES
    // TODO DON'T FORGET TO REMOVE CONNECTION CLOSERS IN BDD REPRODUCER IF CLOSING IS DONE HERE
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class Main {

  private Main() {
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

    Database database = new Database();

    Connecter connecter = new Connecter();
    connecter.doSourceConnection(args[0], args[1], args[2], args[3]);
    connecter.doDestinationConnection(args[4], args[5], args[6], args[7]);

    MetadataGetter metadataGetter = new MetadataGetter();
    metadataGetter.getSchemaOfDatabaseSource(connecter.getConnectionSource(), database);

    new Reproducer(connecter, database);
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.io.IOException;
import java.sql.SQLException;

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

  public static void main(String[] args) throws IOException {

    Database database = new Database();

    Connecter connecter = new Connecter();
    /** TRY WITH mySql port 13306 AS SOURCE (NEMO) AND posgresql:5432 AS SOURCE */
    connecter.doSourceConnection(args[4], args[5], args[6], args[7]);
    connecter.doDestinationConnection(args[0], args[1], args[2], args[3]);

    /** TRY WITH postgresql port 5432 AS SOURCE AND mySql:13306 AS DEST */
    //connecter.doSourceConnection(args[0], args[1], args[2], args[3]);
    //connecter.doDestinationConnection(args[4], args[5], args[6], args[7]);

    MetadataGetter metadataGetter = new MetadataGetter();
    metadataGetter.getSchemaOfDatabaseSource(connecter.getConnectionSource(), database);

    new Reproducer(connecter, database);

    connecter.closeSource();
    connecter.closeDestination();
  }
}

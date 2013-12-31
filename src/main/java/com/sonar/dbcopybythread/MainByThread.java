/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopybythread;

import com.sonar.dbcopyutils.Database;

public class MainByThread {

  private MainByThread() {
  }

  /*
      arg[i]
      0=> org.postgresql.Driver
      1=> jdbc:postgresql://localhost:15432/sonar
      2=> sonar
      3=> sonar
      4=> com.mysql.jdbc.Driver
      5=> jdbc:mysql://localhost:13306/sonar
      6=> sonar
      7=> sonar

      //SELECT * FROM pg_stat_activity
  */

  public static void main(String[] args){

    Database database = new Database();
    /** CONNECTION WITH mySql port 13306 AS SOURCE (NEMO) AND posgresql:5432 AS DEST */
    DataConnecterByThread dataConnecterByThread = new DataConnecterByThread(args[4], args[5], args[6], args[7], args[0], args[1], args[2], args[3]);
    /** TRY WITH postgresql port 15432 AS SOURCE AND mySql:13306 AS DEST */
    //DataConnecterByThread dataConnecterByThread = new DataConnecterByThread(args[0], args[1], args[2], args[3],args[4], args[5], args[6], args[7]);

    new BuildAndDelete(dataConnecterByThread,database);

    new ReproducerByThread(dataConnecterByThread, database);

  }
}

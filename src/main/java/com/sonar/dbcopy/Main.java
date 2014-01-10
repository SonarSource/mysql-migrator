/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class Main {

  public static void main(String[] args) {

    Database database = new Database();
    ConnecterDatas connecterDatas = new ConnecterDatas(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);

    BuildAndDelete buildAndDelete = new BuildAndDelete();
    buildAndDelete.execute(connecterDatas, database);


    ReproducerByTable reproducerByTable = new ReproducerByTable(connecterDatas, database);
    reproducerByTable.execute();
  }
}

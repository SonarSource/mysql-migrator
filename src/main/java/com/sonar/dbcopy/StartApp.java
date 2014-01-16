/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class StartApp {

  public StartApp() {
  }

  public static void main(String[] args) {

    Database database = new Database();

    ConnecterDatas connecterDatasSource = new ConnecterDatas(args[0], args[1], args[2], args[3]);
    ConnecterDatas connecterDatasDest = new ConnecterDatas(args[4], args[5], args[6], args[7]);


    MetadataGetter metadataGetter = new MetadataGetter(connecterDatasSource, database);
    metadataGetter.execute();

    Deleter deleter = new Deleter(connecterDatasDest, database);
    deleter.execute();

    ReproducerByTable reproducerByTable = new ReproducerByTable(connecterDatasSource, connecterDatasDest, database);
    reproducerByTable.execute();
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

public class StartApp {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  private StartApp() {
    // empty because only use the static method main
  }

  public static void main(String[] args) {

    Database database = new Database();

    ConnecterDatas connecterDatasSource = new ConnecterDatas(args[0], args[1], args[2], args[3]);
    ConnecterDatas connecterDatasDest = new ConnecterDatas(args[4], args[5], args[6], args[7]);

    /* VERIFY CONNECTION */
    ConnectionVerifier connectionVerifier = new ConnectionVerifier();
    connectionVerifier.databaseIsReached(connecterDatasSource);
    LOGGER.info("Database source has been reached.");
    connectionVerifier.databaseIsReached(connecterDatasDest);
    LOGGER.info("Database destination has been reached.");

    MetadataGetter metadataGetter = new MetadataGetter(connecterDatasSource, database);
    metadataGetter.execute();

    Deleter deleter = new Deleter(connecterDatasDest, database);
    deleter.execute();

    Reproducer reproducer = new Reproducer(connecterDatasSource, connecterDatasDest, database);
    reproducer.execute();
  }
}

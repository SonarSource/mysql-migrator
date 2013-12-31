/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopybythread;

import com.sonar.dbcopyutils.Database;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildAndDelete {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public void execute(DataConnecterByThread dc, Database database) {

    Thread threadToMetadata = new Thread(new RunnableToGetMetadata(dc, database));
    threadToMetadata.start();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      LOGGER.log(Level.INFO, "Waiting for end of thread failed.");
    }

    new Thread(new RunnableToDelete(dc, database)).start();
  }
}

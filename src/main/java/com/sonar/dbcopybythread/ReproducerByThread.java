/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopybythread;

import com.sonar.dbcopyutils.Database;
import com.sonar.dbcopyutils.DbException;

import java.util.logging.Logger;

public class ReproducerByThread {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public void execute(DataConnecterByThread dataConnecterByThread, Database database) {

    /* THREAD TO read CONTENT OF SOURCE */
    Thread threadToread = new Thread(new RunnableToRead(dataConnecterByThread, database));
    threadToread.start();
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      throw new DbException("Thread.sleep didn't work find", e);
    }
    /* THREAD TO write CONTENT OF DESTINATION */
    Thread threadToWrite = new Thread(new RunnableToWrite(dataConnecterByThread, database));
    threadToWrite.start();
  }

}

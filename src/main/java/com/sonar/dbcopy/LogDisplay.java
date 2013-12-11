/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogDisplay {

  private Logger logger;

  public LogDisplay() throws IOException {
    this.logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  }

  public void displayInformationLog (String message){
    logger.log(Level.INFO, message);
  }
}


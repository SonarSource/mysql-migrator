/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

public class SqlDbException extends RuntimeException {

  public SqlDbException(String message, Exception e) {
    super(message, e);
  }
}

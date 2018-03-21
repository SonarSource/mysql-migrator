/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

public class SqlDbCopyException extends RuntimeException {

  public SqlDbCopyException(String message, Exception e) {
    super(message, e);
  }
}

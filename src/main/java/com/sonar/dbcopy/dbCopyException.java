/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class DbCopyException extends RuntimeException {

  public DbCopyException(String message, Exception e){
    super(message, e);
  }
}

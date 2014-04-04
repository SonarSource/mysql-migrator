/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

public class MessageException extends RuntimeException {

  public MessageException(String message) {
    super(message);
  }

  /**
   * Does not fill in the stack trace
   *
   * @see java.lang.Throwable#fillInStackTrace()
   */
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }

  @Override
  public String toString() {
    return getMessage();
  }
}

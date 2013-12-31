/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopyutils;

public class Sequence {

  private String name;

  public Sequence(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

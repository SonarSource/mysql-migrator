/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class SonarSequence {

  protected String sequenceName;

  public SonarSequence(String sequenceName){
    this.sequenceName = sequenceName;
  }
}

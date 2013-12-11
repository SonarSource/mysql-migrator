/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class Sequence {

  private String sequenceName;

  public Sequence(String sequenceName){
    this.sequenceName = sequenceName;
  }

  /* GETTERS */
  public String getSequencename(){
    return sequenceName;
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.List;

public class SonarBDD {

  private String bddName;
  private List<SonarTable> tables_of_bdd;

  public SonarBDD(String bddName){
    this.bddName = bddName;
  }

  /* GETTERS */
  public List<SonarTable>  getBDDTables(){
    return tables_of_bdd;
  }
  public String getBddNameName(){
    return this.bddName;
  }
  /* SETTERS */
  public void setBddTables (List<SonarTable> tables_of_bdd){
    this.tables_of_bdd = tables_of_bdd;
  }
  public void setBddName (String bddName){
    this.bddName = bddName;
  }
}

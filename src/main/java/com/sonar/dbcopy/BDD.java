/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.List;

public class Bdd {

  private String bddName;
  private List<Table> tables_of_bdd;

  public Bdd(String bddName){
    this.bddName = bddName;
  }

  /* GETTERS */
  public List<Table> getBddTables(){
    return tables_of_bdd;
  }
  public String getBddName(){
    return this.bddName;
  }
  public Object getDataFromColumnFromTable(int indexTable, int indexColumn , int indexData){
    return getColumnFromTable(indexTable,indexColumn).getDataList().get(indexData);
  }
  public Column getColumnFromTable(int indexTable, int indexColumn ){
    return  tables_of_bdd.get(indexTable).getColumns().get(indexColumn);
  }

  /* SETTERS */
  public void setBddTables (List<Table> tables_of_bdd){
    this.tables_of_bdd = tables_of_bdd;
  }
  public void setBddName (String bddName){
    this.bddName = bddName;
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.ArrayList;
import java.util.List;

public class SonarTable {

  private String tableName;
  private int nbRows;
  private List<SonarColumn> columns;

  public SonarTable(String tableName){
    this.tableName = tableName;
    this.nbRows=0;
    columns=new ArrayList<SonarColumn>();
  }

  public SonarColumn addOneColumnToTable(String columnName, String tableName){
    SonarColumn columnToAdd = new SonarColumn(columnName,tableName);
    columns.add(columnToAdd);
    return columnToAdd;
  }
  public void setTableName(String tableName){
    this.tableName = tableName;
  }
  public void setNbRows(int nbRows){
    this.nbRows = nbRows;
  }
  public String getTableName(){
    return this.tableName;
  }
  public int getNbRows(){
    return nbRows;
  }
  public List<SonarColumn> getColumns(){
    return this.columns;
  }
}

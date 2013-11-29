/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.ArrayList;
import java.util.List;

public class Table {

  private String tableName;
  private int nbRows;
  private List<Column> columns;

  public Table(String tableName){
    this.tableName = tableName;
    this.nbRows=0;
    columns=new ArrayList<Column>();
  }

  /* SETTERS */
  public Column addOneColumnToTable(String columnName){
    Column columnToAdd = new Column(columnName);
    columns.add(columnToAdd);
    return columnToAdd;
  }
  public void setNbRows(int nbRows){
    this.nbRows = nbRows;
  }
  /* GETTERS */
  public String getTableName(){
    return this.tableName;
  }
  public int getNbRows(){
    return nbRows;
  }
  public List<Column> getColumns(){
    return this.columns;
  }
}
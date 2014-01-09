/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopyutils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Table {

  private String tableName;
  private int nbRows;
  private List<Column> columns;
  private boolean isBuilt;

  public Table(String tableName) {
    this.tableName = tableName;
    this.nbRows = 0;
    columns = new ArrayList<Column>();
    isBuilt = false;
  }

  public Column addColumn(String columnName) {
    Column columnToAdd = new Column(columnName);
    columns.add(columnToAdd);
    return columnToAdd;
  }

  public String getName() {
    return this.tableName;
  }

  public int getNbRows() {
    return nbRows;
  }

  public void setNbRows(int nbRows) {
    this.nbRows = nbRows;
  }

  public boolean getIsBuilt() {
    return isBuilt;
  }

  public void setIsBuilt(boolean bool) {
    this.isBuilt = bool;
  }

  public List<Column> getColumns() {
    return this.columns;
  }

  public void destructContentTable() {
    for (int indexColumn = 0; indexColumn < columns.size(); indexColumn++) {
      this.getColumns().get(indexColumn).removeQueue();
    }
  }
}

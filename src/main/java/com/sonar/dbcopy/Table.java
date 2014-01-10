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
  private List<String> columns;
  private boolean isBuilt;


  public Table(String tableName) {
    this.tableName = tableName;
    this.nbRows = 0;
    columns = new ArrayList<String>();
    isBuilt = false;

  }

  public int getNbColumns() {
    return columns.size();
  }

  public void addColumn(String columnName) {
    columns.add(columnName);
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

  public List<String> getColumns() {
    return this.columns;
  }

  public String getColumnName(int indexColumn) {
    return columns.get(indexColumn);
  }
}

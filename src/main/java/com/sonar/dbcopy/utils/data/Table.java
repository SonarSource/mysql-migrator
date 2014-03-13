/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils.data;

import java.util.ArrayList;
import java.util.List;

public class Table {

  private String tableName;
  private int nbRows;
  private List<String> columns;
  private List<Integer> types;

  public Table(String tableName) {
    this.tableName = tableName;
    this.nbRows = 0;
    columns = new ArrayList<String>();
    types = new ArrayList<Integer>();
  }

  public int getNbColumns() {
    return columns.size();
  }

  public void addColumn(int index, String columnName, Integer type) {
    String columnNameToInsert = null;
    Integer typeToInsert = null;

    if (columnName == null) {
      columnNameToInsert = "null";
    } else {
      columnNameToInsert = columnName;
    }

    if (type == null) {
      typeToInsert = 0;
    } else {
      typeToInsert = type;
    }

    columns.add(index, columnNameToInsert);
    types.add(index, typeToInsert);
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

  public String getColumnName(int indexColumn) {
    return columns.get(indexColumn);
  }

  public Integer getType(int indexColumn) {
    return types.get(indexColumn);
  }
}

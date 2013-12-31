/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopyutils;

import java.util.ArrayList;
import java.util.List;

public class Database {

  private List<Table> tablesList;

  public Database() {
    tablesList = new ArrayList<Table>();
  }

  public Table addTable(String tableName) {
    Table table = new Table(tableName);
    tablesList.add(table);
    return table;
  }

  public void addData(int indexTable, int indexColumn, Object data) {
    this.getColumn(indexTable, indexColumn).addData(data);
  }

  public Object getData(int indexTable, int indexColumn, int indexRow) {
    return getColumn(indexTable, indexColumn).getDataList().get(indexRow);
  }

  public Column getColumn(int indexTable, int indexColumn) {
    return tablesList.get(indexTable).getColumns().get(indexColumn);
  }

  public List<Table> getTables() {
    return tablesList;
  }

  public void setTables(List<Table> databaseTables) {
    this.tablesList = databaseTables;
  }

  public String getTableName(int indexTable) {
    return tablesList.get(indexTable).getName();
  }

  public int getNbColumnsInTable(int indexTable) {
    return tablesList.get(indexTable).getColumns().size();
  }

  public int getNbTables() {
    return tablesList.size();
  }

  public Table getTable(int indexTable) {
    return tablesList.get(indexTable);
  }
}

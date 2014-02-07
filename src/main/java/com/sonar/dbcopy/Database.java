/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.ArrayList;
import java.util.List;

public class Database {

  private List<Table> tablesList;

  public Database() {
    tablesList = new ArrayList<Table>();
  }

  public void addTable(String tableName) {
    Table table = new Table(tableName);
    tablesList.add(table);
  }

  public String getTableName(int indexTable) {
    return tablesList.get(indexTable).getName();
  }

  public int getNbTables() {
    return tablesList.size();
  }

  public Table getTable(int indexTable) {
    return tablesList.get(indexTable);
  }

  public int getNbColumnInTable(int indexTable) {
    return tablesList.get(indexTable).getNbColumns();
  }

  public Table getTableByName(String tableNameToReturn) {
    Table tableToReturn = null;
    for (int indexTable = 0; indexTable < tablesList.size(); indexTable++) {
      if (getTableName(indexTable).equals(tableNameToReturn)) {
        tableToReturn = tablesList.get(indexTable);
      }
    }
    return tableToReturn;
  }
}

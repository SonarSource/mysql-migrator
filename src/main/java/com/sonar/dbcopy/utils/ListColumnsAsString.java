/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils;

import com.sonar.dbcopy.utils.objects.Table;

public class ListColumnsAsString {

  private Table table;
  private int nbColumns;

  public ListColumnsAsString(Table table) {
    this.table = table;
    nbColumns = table.getNbColumns();
  }

  public String makeColumnString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int indexColumn = 0; indexColumn < nbColumns; indexColumn++) {
      stringBuilder.append(",");
      stringBuilder.append(table.getColumnName(indexColumn));
    }
    String columnsAsString = stringBuilder.toString();
    columnsAsString = columnsAsString.substring(1);

    return columnsAsString;
  }

  public String makeQuestionMarkString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("?");
    for (int indexColumn = 0; indexColumn < nbColumns - 1; indexColumn++) {
      stringBuilder.append(",?");
    }
    return stringBuilder.toString();
  }

  public String makeStringOfTypes() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int indexColumn = 0; indexColumn < nbColumns; indexColumn++) {
      stringBuilder.append("," + table.getType(indexColumn));
    }
    String typesAsString = stringBuilder.toString();
    typesAsString = typesAsString.substring(1);

    return typesAsString;
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.List;

public class ListColumnsAsString {

  private List<Column> columnList;
  private int nbColumns;

  public ListColumnsAsString(List<Column> list) {
    columnList = list;
    nbColumns = columnList.size();
  }

  public String makeString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int indexColumn = 0; indexColumn < nbColumns; indexColumn++) {
      stringBuilder.append(",");
      stringBuilder.append(columnList.get(indexColumn).getColumnName());
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

  public int getNbColumns() {
    return nbColumns;
  }
}

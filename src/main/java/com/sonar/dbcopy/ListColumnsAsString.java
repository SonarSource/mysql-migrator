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

  public ListColumnsAsString(List<Column> list){
    columnList = list;
    nbColumns = columnList.size();
  }

  public String makeString(){
    StringBuffer buf = new StringBuffer();
    for (int indexColumn = 0; indexColumn < nbColumns; indexColumn++) {
      buf.append(",");
      buf.append(columnList.get(indexColumn).getColumnName());
    }
    String columnsAsString = buf.toString();
    columnsAsString = columnsAsString.substring(1);
    return columnsAsString;
  }
  public String makeQuestionMarkString(){
    StringBuffer buf = new StringBuffer();
    buf.append("?");
    for (int indexColumn = 0; indexColumn < nbColumns-1; indexColumn++) {
      buf.append(",?");
    }
    String questionMarkString = buf.toString();
    return questionMarkString;
  }
  public int getNbColumns(){
    return nbColumns;
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.List;

public class ListColumnsAsString {

  private List<Column> columnList;
  private String columnsAsString, questionMarkString;
  private int nbColumns;

  public ListColumnsAsString(List<Column> list){
    columnList = list;
    nbColumns = columnList.size();
  }

  public String makeString(){
    columnsAsString = new String();
    for(int indexColumn=0;indexColumn<nbColumns;indexColumn++){
      columnsAsString+=","+columnList.get(indexColumn).getColumnName();
    }
    columnsAsString = columnsAsString.substring(1);
    return columnsAsString;
  }
  public String makeQuestionMarkString(){
      questionMarkString ="?";
      for(int indexColumn=0;indexColumn<nbColumns-1;indexColumn++){
          questionMarkString+=",?";
      }
      return questionMarkString;
  }
  public int getNbColumns(){
    return nbColumns;
  }
}

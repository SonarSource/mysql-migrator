/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.data;

import java.util.ArrayList;
import java.util.List;

public class Table {

  private String tableName;
  private int nbRows;
  private List<String> columns;
  private List<Integer> types;
  private String typesAsString;
  private String questionMarksAsString;
  private String columnNamesAsString;

  public Table(String tableName) {
    this.tableName = tableName;
    this.nbRows = 0;
    columns = new ArrayList<>();
    types = new ArrayList<>();
  }

  public int getNbColumns() {
    return columns.size();
  }

  public void addColumn(int index, String columnName, Integer type) {
    String columnNameToInsert;
    Integer typeToInsert;

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

  public void makeStringsUsedForTable() {
    StringBuilder stringBuilderForTypes = new StringBuilder();
    StringBuilder stringBuilderForQuestionMarks = new StringBuilder();
    StringBuilder stringBuilderForColumnNames = new StringBuilder();

    for (int indexColumn = 0; indexColumn < getNbColumns(); indexColumn++) {
      stringBuilderForTypes.append(",");
      stringBuilderForTypes.append(getStringType(types.get(indexColumn)));
      stringBuilderForColumnNames.append(",");
      stringBuilderForColumnNames.append(getColumnName(indexColumn));
      stringBuilderForQuestionMarks.append(",?");
    }
    columnNamesAsString = stringBuilderForColumnNames.toString();
    columnNamesAsString = columnNamesAsString.substring(1);

    typesAsString = stringBuilderForTypes.toString();
    typesAsString = typesAsString.substring(1);

    questionMarksAsString = stringBuilderForQuestionMarks.toString();
    questionMarksAsString = questionMarksAsString.substring(1);
  }

  public String getQuestionMarksAsString() {
    return questionMarksAsString;
  }

  public String getColumnNamesAsString() {
    return columnNamesAsString;
  }

  public String getTypesAsString() {
    return typesAsString;
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

  public String getStringType(int typeAsInt) {
    String stringToReturn = null;
    Integer[] typesIntegerTab = {2003, -5, -2, -7,
      2004, 16, 1, 2005, 70, 91, 3, 2001, 8, 6, 4, 2000, -16, -4, -1, -15,
      2011, 0, 2, -9, 1111, 7, 2006, -8, 5, 2009, 2002, 92, 93, -6, -3, 12};
    String[] typesStringTab = {"ARRAY", "BIGINT", "BINARY", "BIT",
      "BLOB", "BOOLEAN", "CHAR", "CLOB", "DATALINK", "DATE", "DECIMAL", "DISTINCT", "DOUBLE",
      "FLOAT", "INTEGER", "JAVA_OBJECT", "LONGNVARCHAR", "LONGVARBINARY", "LONGVARCHAR", "NCHAR",
      "NCLOB", "NULL", "NUMERIC", "NVARCHAR", "OTHER", "REAL", "REF", "ROWID",
      "SMALLINT", "SQLXML", "STRUCT", "TIME", "TIMESTAMP", "TINYINT", "VARBINARY", "VARCHAR"};
    for (int indexInteger = 0; indexInteger < typesIntegerTab.length; indexInteger++) {
      if (typesIntegerTab[indexInteger] == typeAsInt) {
        stringToReturn = typesStringTab[indexInteger];
      }
    }
    return stringToReturn;
  }
}

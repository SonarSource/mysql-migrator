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
  private String typesAsString, questionMarksAsString, columnNamesAsString;

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


  private String getStringType(int typesAsInt) {
    String typesAsStringToreturn;
    switch (typesAsInt) {
      case 2003:
        typesAsStringToreturn = "ARRAY";
        break;
      case -5:
        typesAsStringToreturn = "BIGINT";
        break;
      case -2:
        typesAsStringToreturn = "BINARY";
        break;
      case -7:
        typesAsStringToreturn = "BIT";
        break;
      case 2004:
        typesAsStringToreturn = "BLOB";
        break;
      case 16:
        typesAsStringToreturn = "BOOLEAN";
        break;
      case 1:
        typesAsStringToreturn = "CHAR";
        break;
      case 2005:
        typesAsStringToreturn = "CLOB";
        break;
      case 70:
        typesAsStringToreturn = "DATALINK";
        break;
      case 91:
        typesAsStringToreturn = "DATE";
        break;
      case 3:
        typesAsStringToreturn = "DECIMAL";
        break;
      case 2001:
        typesAsStringToreturn = "DISTINCT";
        break;
      case 8:
        typesAsStringToreturn = "DOUBLE";
        break;
      case 6:
        typesAsStringToreturn = "FLOAT";
        break;
      case 4:
        typesAsStringToreturn = "INTEGER";
        break;
      case 2000:
        typesAsStringToreturn = "JAVA_OBJECT";
        break;
      case -16:
        typesAsStringToreturn = "LONGNVARCHAR";
        break;
      case -4:
        typesAsStringToreturn = "LONGVARBINARY";
        break;
      case -1:
        typesAsStringToreturn = "LONGVARCHAR";
        break;
      case -15:
        typesAsStringToreturn = "NCHAR";
        break;
      case 2011:
        typesAsStringToreturn = "NCLOB";
        break;
      case 0:
        typesAsStringToreturn = "NULL";
        break;
      case 2:
        typesAsStringToreturn = "NUMERIC";
        break;
      case -9:
        typesAsStringToreturn = "NVARCHAR";
        break;
      case 1111:
        typesAsStringToreturn = "OTHER";
        break;
      case 7:
        typesAsStringToreturn = "REAL";
        break;
      case 2006:
        typesAsStringToreturn = "REF";
        break;
      case -8:
        typesAsStringToreturn = "ROWID";
        break;
      case 5:
        typesAsStringToreturn = "SMALLINT";
        break;
      case 2009:
        typesAsStringToreturn = "SQLXML";
        break;
      case 2002:
        typesAsStringToreturn = "STRUCT";
        break;
      case 92:
        typesAsStringToreturn = "TIME";
        break;
      case 93:
        typesAsStringToreturn = "TIMESTAMP";
        break;
      case -6:
        typesAsStringToreturn = "TINYINT";
        break;
      case -3:
        typesAsStringToreturn = "VARBINARY";
        break;
      case 12:
        typesAsStringToreturn = "VARCHAR";
        break;
      default:
        typesAsStringToreturn = "null";
    }
    return typesAsStringToreturn;
  }
}

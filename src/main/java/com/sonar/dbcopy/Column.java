/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.ArrayList;
import java.util.List;

public class Column {

  private String name;
  private String type;
  private int sizeOfType;
  private String canBeNull;
  //TODO autoincrementwould be needed on Oracle
  private boolean isAutoIncrement;
  private Sequence sequenceIfId = null;
  private List<Object> dataList;

  public Column(String name) {
    this.name = name;
    dataList = new ArrayList<Object>();
  }

  public List<Object> getDataList() {
    return this.dataList;
  }

  public Object getData(int index) {
    return dataList.get(index);
  }

  public String getName() {
    return this.name;
  }

  public String getType() {
    return this.type;
  }

  public int getTypeSize() {
    return sizeOfType;
  }

  // return "NOT NULL" if it can't be null  otherwise ""
  public String getCanBeNull() {
    return this.canBeNull;
  }

  public boolean isAutoIncrement() {
    return isAutoIncrement;
  }

  public Sequence getSequence() {
    return this.sequenceIfId;
  }

  public void setIsAutoIncrement(boolean isAutoIncrement) {
    this.isAutoIncrement = isAutoIncrement;
  }

  public void addSequenceOnId(String tableName) {
    this.sequenceIfId = new Sequence(tableName + "_id_seq");
  }

  public void addData(Object object) {
    this.dataList.add(object);
  }

  // isNullable : columnNoNulls=0, columnNullable=1 or columnNullableUnknown=2
  public void addCharacteristic(String type, int size, int isNullable) {
    this.type = type;
    this.sizeOfType = size;
    if (isNullable == 0) {
      this.canBeNull = "NOT NULL";
    } else {
      this.canBeNull = "";
    }
  }
}

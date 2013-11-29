/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.ArrayList;
import java.util.List;

public class Column {

  /* ATTRIBUTS */
  private String columnName;
  private String columnType;
  private int sizeOfType;
  private boolean canBeNull;
  private boolean anIndexIsPresent;
  private Sequence sequenceIfId = null;
  private List<Object> objectTableData;

  /* CONSTRUCTEUR */
  public Column(String columnName){
    this.columnName = columnName;
    objectTableData = new ArrayList<Object>();
  }

  /* GETTERS */
  public List<Object> getDataList(){
    return this.objectTableData;
  }
  public Object getDataWithIndex(int index){
    return objectTableData.get(index);
  }
  public String getColumnName(){
    return this.columnName;
  }
  public String getColumnType(){
    return this.columnType;
  }
  /* SETTERS */
  public void setColumnName(String columnName){
    this.columnName = columnName;
  }
  public void setColumnType(String columnType){
    this.columnType = columnType;
  }
  public void addSequenceOnId(String tableName){
    this.sequenceIfId = new Sequence(tableName+"_id_seq");
  }
  public void addColumnType(String type){
    this.columnType = type;
  }
  public void addDataObjectInColumn(Object object){
    this.objectTableData.add(object);
  }
  public void addCharacteristicOfColumn(String type, int size, boolean canBeNull, boolean anIndexIsPresent){
    this.columnType = type;
    this.sizeOfType = size;
    this.canBeNull = canBeNull;
    this.anIndexIsPresent = anIndexIsPresent;
  }
}
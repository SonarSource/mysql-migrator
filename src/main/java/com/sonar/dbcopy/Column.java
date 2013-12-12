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
  private String canBeNull;
  //TODO when autoincrement syntax depends on provider !!
  private boolean isAutoIncrement;
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
  public int getColumnTypeSize(){
    return sizeOfType;
  }
  public String getCanBeNull(){
    return this.canBeNull;
  }
  public boolean getIsAutoIncrement(){
    return isAutoIncrement;
  }
  public Sequence getSequence(){
    return this.sequenceIfId;
  }

  /* SETTERS */
  public void addSequenceOnId(String tableName){
    this.sequenceIfId = new Sequence(tableName+"_id_seq");
  }
  public void addDataObjectInColumn(Object object){
    this.objectTableData.add(object);
  }
  public void addCharacteristicOfColumn(String type, int size, int canBeNull){
    this.columnType = type;
    this.sizeOfType = size;
    if (canBeNull==0){
      this.canBeNull = "NOT NULL";
    } else{
      this.canBeNull="";
    }
  }
  public void setIsAutoIncrement(boolean isAutoIncrement){
    this.isAutoIncrement = isAutoIncrement;
  }
}

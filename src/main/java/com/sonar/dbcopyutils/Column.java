/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopyutils;

import com.sonar.simplify.DbException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Column {

  private String name;
  private String type;
  private int sizeOfType;
  private String canBeNull;
  //TODO autoincrementwould be needed on Oracle
  private boolean isAutoIncrement;
  private Sequence sequenceIfId = null;
  public BlockingQueue<Object> queue;

  public Column(String name) {
    this.name = name;
    queue = new ArrayBlockingQueue<Object>(4);
  }

  //----- MULTITHREADING -----//
  public boolean putData(Object object) {
    try {
      if(queue.remainingCapacity()>1){
        queue.put(object);
        return true;
      }else{
       return false;
      }
    } catch (InterruptedException e) {
      throw new DbException("Problem to put in LinkedBlockingQueue", e);
    }
  }

  public Object pullData() {
    return queue.poll();
  }

  public void removeQueue(){
    this.queue.clear();
  }
  //----- END MULTITHREADING -----//

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

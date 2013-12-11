/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class ColumnTest {

  private Column column;

  @Before
  public void create_instance(){
    column = new Column("columnNameToVerify");
    column.addDataObjectInColumn("stringDataToVerify");
    column.addCharacteristicOfColumn("integer",4,0);
    column.setIsAutoIncrement(true);
    column.addSequenceOnId("tableName");
  }
  @Test
  public void testColumnCreation() throws Exception {
    assertNotNull(column);
    assertEquals("columnNameToVerify", column.getColumnName());
    assertNotNull(column.getDataList());
  }
  @Test
  public void testInsertionOfDataTypeString() throws Exception {
    assertEquals("stringDataToVerify",column.getDataWithIndex(0));
  }
  @Test
  public void testGettersForColumnAttributes() throws Exception {
    assertEquals("integer",column.getColumnType());
    assertEquals(4,column.getColumnTypeSize());
    assertTrue(column.getIsAutoIncrement());
    assertEquals("NOT NULL",column.getCanBeNull());
  }
  @Test
  public void testAddSequenceOnId(){
    assertNotNull(column.getSequence());
    assertEquals("tableName_id_seq",column.getSequence().getSequencename());
  }

 }

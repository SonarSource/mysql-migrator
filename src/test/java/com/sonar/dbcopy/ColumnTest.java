/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import com.sonar.dbcopyutils.Column;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class ColumnTest {

  private Column column;

  @Before
  public void create_instance() {
    column = new Column("columnNameToVerify");
    column.addData("stringDataToVerify");
    column.addCharacteristic("integer", 4, 0);
    column.setIsAutoIncrement(true);
    column.addSequenceOnId("tableName");
  }

  @Test
  public void testColumnCreation() throws Exception {
    assertNotNull(column);
    assertEquals("columnNameToVerify", column.getName());
    assertNotNull(column.getDataList());
  }

  @Test
  public void testInsertionOfDataTypeString() throws Exception {
    assertEquals("stringDataToVerify", column.getData(0));
  }

  @Test
  public void testGettersForColumnAttributes() throws Exception {
    assertEquals("integer", column.getType());
    assertEquals(4, column.getTypeSize());
    assertTrue(column.isAutoIncrement());
    assertEquals("NOT NULL", column.getCanBeNull());
  }

  @Test
  public void testAddSequenceOnId() {
    assertNotNull(column.getSequence());
    assertEquals("tableName_id_seq", column.getSequence().getName());
  }

}

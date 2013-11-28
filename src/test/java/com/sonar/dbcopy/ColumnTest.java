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

public class ColumnTest {

  private Column column;

  @Before
  public void create_instance(){
    column = new Column("columnNameToVerify");
  }
  @Test
  public void verifyColumnAndDataArrayCreation() throws Exception {
    assertNotNull(column);
    assertNotNull(column.getDataList());
  }
  @Test
  public void verifyInsertionOfDataTypeString() throws Exception {
    column.addDataObjectInColumn("stringDataToVerify");
    assertEquals("stringDataToVerify",column.getDataWithIndex(0));
  }
  @Test
  public void verifyColumnName() throws Exception {
    assertEquals("columnNameToVerify", column.getColumnName());
  }
 }

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

public class TableTest {

  private Table table;

  @Before
  public void createInstance(){
    table = new Table("tableNameToVerify");
    table.addOneColumnToTable("columnNameToVerify");
  }
  @Test
  public void verifyTableCreation(){
    assertNotNull(table);
  }
  @Test
  public void verifyColumnAddToTable() throws Exception {
    assertNotNull(table.getColumns().get(0));
  }
  @Test
  public void verifyNbRowsIsNull() throws Exception {
    assertEquals(0,table.getNbRows());
  }
  @Test
  public void verifyGetTableName() throws Exception {
    assertEquals("tableNameToVerify",table.getTableName());
  }
  @Test
  public void verifyColumnName() throws Exception {
    assertEquals("columnNameToVerify",table.getColumns().get(0).getColumnName());
  }
}

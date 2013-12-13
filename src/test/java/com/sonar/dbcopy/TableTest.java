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
  public void createInstance() {
    table = new Table("tableNameToVerify");
    table.addColumn("columnNameToVerify");
  }

  @Test
  public void testTableCreation() {
    assertNotNull(table);
  }

  @Test
  public void testColumnAddToTable() throws Exception {
    assertNotNull(table.getColumns().get(0));
  }

  @Test
  public void testNbRowsIsNull() throws Exception {
    assertEquals(0, table.getNbRows());
  }

  @Test
  public void testGetTableName() throws Exception {
    assertEquals("tableNameToVerify", table.getName());
  }

  @Test
  public void testColumnName() throws Exception {
    assertEquals("columnNameToVerify", table.getColumns().get(0).getName());
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TableTest {

  private Table table;

  @Before
  public void setUp() throws Exception {
    table = new Table("table1");
    table.setNbRows(5);
    table.addColumn("col0");
    table.addColumn("col1");
  }

  @Test
  public void testGetNbColumns() throws Exception {
    assertEquals(2,table.getNbColumns());
  }

  @Test
  public void testAddAndGetColumn() throws Exception {
    assertEquals("col0",table.getColumnName(0));
    assertEquals("col1",table.getColumnName(1));
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals("table1",table.getName());
  }

  @Test
  public void testSetAndGetNbRows() throws Exception {
    assertEquals(5,table.getNbRows());
  }
}

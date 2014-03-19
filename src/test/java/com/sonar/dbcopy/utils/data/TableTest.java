/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils.data;

import org.junit.Before;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertEquals;

public class TableTest {

  private Table table;

  @Before
  public void setUp() throws Exception {
    table = new Table("table1");
    table.setNbRows(5);
    table.addColumn(0,"col0", Types.VARCHAR);
    table.addColumn(1,"col1", Types.INTEGER);
    table.addColumn(2,null,null);
  }

  @Test
  public void testGetNbColumns() throws Exception {
    assertEquals(3,table.getNbColumns());
  }

  @Test
  public void testAddAndGetColumn() throws Exception {
    assertEquals("col0",table.getColumnName(0));
    assertEquals("col1",table.getColumnName(1));
    assertEquals("null",table.getColumnName(2));
  }

  @Test
  public void testGetName() throws Exception {
    assertEquals("table1",table.getName());
  }

  @Test
  public void testSetAndGetNbRows() throws Exception {
    assertEquals(5,table.getNbRows());
  }

  @Test
  public void testSetAndGetTypes(){
    assertEquals((long) Types.VARCHAR,(long) table.getType(0));
    assertEquals((long) Types.INTEGER,(long) table.getType(1));
    assertEquals(0 ,(long) table.getType(2));

  }
}

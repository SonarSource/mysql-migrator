/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class ListColumnsAsStringTest {

  private ListColumnsAsString lcas;

  @Before
  public void setUp(){
    Table table = new Table("tableName");
    table.addColumn("col0");
    table.addColumn("col1");
    table.addColumn("col2");
    lcas = new ListColumnsAsString(table);

  }

  @Test
  public void testMakeColumnString() throws Exception {
    assertEquals("col0,col1,col2", lcas.makeColumnString());
  }

  @Test
  public void testMakeQuestionMarkString() throws Exception {
    assertEquals("?,?,?", lcas.makeQuestionMarkString());
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertEquals;


public class ListColumnsAsStringTest {

  private ListColumnsAsString lcas;

  @Before
  public void setUp(){
    Table table = new Table("tableName");
    table.addColumn(0,"col0", Types.VARCHAR);
    table.addColumn(1,"col1",Types.VARCHAR);
    table.addColumn(2,"col2",Types.VARCHAR);
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

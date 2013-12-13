/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class ListColumnsAsStringTest {

  private List<Column> columnList;
  private ListColumnsAsString listColumnsAsString;

  @Before
  public void createInstance() {
    columnList = new ArrayList<Column>();
    columnList.add(new Column("columnName1"));
    columnList.add(new Column("columnName2"));
    columnList.add(new Column("columnName3"));

    listColumnsAsString = new ListColumnsAsString(columnList);
  }

  @Test
  public void testListColumnAsStringCreation() {
    assertNotNull(listColumnsAsString);
  }

  @Test
  public void testMakeString() throws Exception {
    assertEquals("columnName1,columnName2,columnName3", listColumnsAsString.makeColumnString());
  }

  @Test
  public void testMakeQuestionMarkString() throws Exception {
    assertEquals("?,?,?", listColumnsAsString.makeQuestionMarkString());
  }

  @Test
  public void testGetNbColumns() throws Exception {
    assertEquals(3, listColumnsAsString.getNbColumns());
  }
}

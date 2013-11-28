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
import java.util.ArrayList;
import java.util.List;

public class ListColumnsAsStringTest {

  private List<Column> columnList;
  private ListColumnsAsString listColumnsAsString;

  @Before
  public void createInstance(){
    columnList = new ArrayList<Column>();
    columnList.add(new Column("columnName1"));
    columnList.add(new Column("columnName2"));
    columnList.add(new Column("columnName3"));

    listColumnsAsString = new ListColumnsAsString(columnList);
  }

  @Test
  public void verifyListColumnAsStringCreation(){
    assertNotNull(listColumnsAsString);
  }
  @Test
  public void verifyMakeString() throws Exception {
    assertEquals("columnName1,columnName2,columnName3",listColumnsAsString.makeString());
  }

  @Test
  public void verifyMakeQuestionMarkString() throws Exception {
    assertEquals("?,?,?",listColumnsAsString.makeQuestionMarkString());
  }
  @Test
  public void verifyGetNbColumns() throws Exception {
    assertEquals(3,listColumnsAsString.getNbColumns());
  }
}

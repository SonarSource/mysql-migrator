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

public class BddBuiderTest {

  private BddBuider bddBuider;

  @Before
  public void createInstance() {
    bddBuider = new BddBuider();
    bddBuider.addTableToBdd();
  }

  @Test
  public void testJavaBddCreation() throws Exception {
    assertNotNull(bddBuider.getBdd());
    assertEquals("javaBdd", bddBuider.getBdd().getBddName());
  }

  @Test
  public void testTablesCreation() throws Exception {
    assertEquals(53, bddBuider.getBdd().getBddTables().size());
    assertEquals("widgets", bddBuider.getBdd().getBddTables().get(52).getTableName());
  }
}

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

public class BddTest {

  private Bdd Bdd;
  private List<Table> listTable;

  @Before
  public void createInstance() {
    Bdd = new Bdd("bddNameToVerify");
    listTable = new ArrayList<Table>();
    Bdd.setBddTables(listTable);
  }
  @Test
  public void verifyBddCreation() throws Exception {
    assertNotNull(Bdd);
    assertEquals("bddNameToVerify", Bdd.getBddName());
  }
  @Test
  public void verifyGetAndSetBddTables() throws Exception {
    listTable = new ArrayList<Table>();
    Bdd.setBddTables(listTable);
    assertNotNull(Bdd.getBddTables());
  }
}

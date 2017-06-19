/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatabaseTest {

  private Database database;

  @Before
  public void setUp() throws Exception {
    database = new Database();
    database.addToTablesList("table1");
    database.addToTablesList("table2");
  }

  @Test
  public void testAddAndGetTable() throws Exception {
    assertNotNull(database);
    assertNotNull(database.getTable(0));
    assertNotNull(database.getTable(0));
  }

  @Test
  public void testGetNbTables() throws Exception {
    assertEquals(2, database.getNbTables());
  }

  @Test
  public void testGetTableName() throws Exception {
    assertEquals("table1", database.getTableName(0));
    assertEquals("table2", database.getTableName(1));
  }
}

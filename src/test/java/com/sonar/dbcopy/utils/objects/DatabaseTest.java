/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils.objects;

import com.sonar.dbcopy.utils.objects.Database;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatabaseTest {

  private Database database;

  @Before
  public void setUp() throws Exception {
    database = new Database();
    database.addTable("table1");
    database.addTable("table2");
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

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

  private Bdd bddJavaFromUtils;

  @Before
  public void createInstance() {
    DatabaseUtils databaseUtils = new DatabaseUtils();
    databaseUtils.makeBddJavaObject();
    databaseUtils.addTablesToBddJavaObject();
    databaseUtils.addColumnsToBddJavaObject();
    databaseUtils.addDatasToBddJavaObject();
    bddJavaFromUtils = databaseUtils.getJavaBddFromUtils();
  }

  @Test
  public void testGetBddTables() {
    assertEquals("sonar", bddJavaFromUtils.getBddName());
  }

  @Test
  public void testGetBddName() {
    assertNotNull(bddJavaFromUtils.getBddTables());
  }

  @Test
  public void testGetColumnFromTable() {
    assertEquals("ID", bddJavaFromUtils.getColumnFromTable(1, 0).getColumnName());
  }

  @Test
  public void testGetDataFromColumnFromTable() {
    assertEquals("This is a second string for test", bddJavaFromUtils.getDataFromColumnFromTable(0, 1, 1));
  }

  @Test
  public void testAddTable() {
    bddJavaFromUtils.addTable("third_table_to_verify");
    assertNotNull(bddJavaFromUtils.getBddTables().get(2));
    assertEquals("third_table_to_verify", bddJavaFromUtils.getBddTables().get(2).getTableName());
  }

  @Test
  public void testSetBddTables() {
    List<Table> newList = new ArrayList<Table>();
    Table newTable = new Table("new_table_to_verify");
    newList.add(newTable);
    bddJavaFromUtils.setBddTables(newList);

    assertNotNull(bddJavaFromUtils.getBddTables());
    assertEquals("new_table_to_verify", bddJavaFromUtils.getBddTables().get(0).getTableName());
  }

}

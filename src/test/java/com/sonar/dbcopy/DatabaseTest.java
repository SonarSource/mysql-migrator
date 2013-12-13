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

public class DatabaseTest {

  private Database databaseJavaFromUtils;

  @Before
  public void createInstance() {
    DatabaseUtils databaseUtils = new DatabaseUtils();
    databaseUtils.makeDatabaseJavaObject();
    databaseUtils.addTablesToDatabaseJavaObject();
    databaseUtils.addColumnsToDatabaseJavaObject();
    databaseUtils.addDatasToDatabaseJavaObject();
    databaseJavaFromUtils = databaseUtils.getJavaDatabaseFromUtils();
  }

  @Test
  public void testGetDatabaseTables() {
    assertNotNull(databaseJavaFromUtils.getTables());
  }

  @Test
  public void testGetColumnFromTable() {
    assertEquals("ID", databaseJavaFromUtils.getColumn(1, 0).getName());
  }

  @Test
  public void testGetDataFromColumnFromTable() {
    assertEquals("This is a second string for test", databaseJavaFromUtils.getData(0, 1, 1));
  }

  @Test
  public void testAddTable() {
    databaseJavaFromUtils.addTable("third_table_to_verify");
    assertNotNull(databaseJavaFromUtils.getTables().get(2));
    assertEquals("third_table_to_verify", databaseJavaFromUtils.getTableName(2));
  }

  @Test
  public void testSetDatabaseTables() {
    List<Table> newList = new ArrayList<Table>();
    Table newTable = new Table("new_table_to_verify");
    newList.add(newTable);
    databaseJavaFromUtils.setTables(newList);

    assertNotNull(databaseJavaFromUtils.getTables());
    assertEquals("new_table_to_verify", databaseJavaFromUtils.getTableName(0));
  }

}

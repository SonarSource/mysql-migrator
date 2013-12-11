/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class MetadataGetterTest {

  private MetadataGetter metadataGetter;
  private DatabaseUtils databaseUtils;
  private Connection connection;
  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    metadataGetter = new MetadataGetter();

    databaseUtils = new DatabaseUtils();
    databaseUtils.makeDatabaseH2Withtables("sonar");
    connection = databaseUtils.getConnectionFromH2();

    databaseUtils.makeBddJavaObjectWithTable();
  }

  @Test
  public void verifyGetSchemaOfBddSource() throws Exception {
    Bdd bdd = databaseUtils.getJavaBddFromUtils();
    metadataGetter.getSchemaOfBddSource(connection,bdd);
    assertEquals("TABLE_FOR_TEST",bdd.getBddTables().get(1).getTableName());
    assertEquals("EMPTY_TABLE_FOR_TEST",bdd.getBddTables().get(0).getTableName());
    assertEquals(2,bdd.getBddTables().size());

    assertEquals("COLUMNINTEGER",bdd.getBddTables().get(1).getColumns().get(0).getColumnName());
    assertEquals("INTEGER",bdd.getBddTables().get(1).getColumns().get(0).getColumnType());
    assertEquals(10,bdd.getBddTables().get(1).getColumns().get(0).getColumnTypeSize());
    assertEquals("NOT NULL",bdd.getBddTables().get(1).getColumns().get(0).getCanBeNull());
    assertFalse(bdd.getBddTables().get(1).getColumns().get(0).getIsAutoIncrement());
   }

  @Test
  public void verifyAddSchemaToBddDest() throws Exception {

  }
  @After
  public void  closeEveryThing() throws SQLException, ClassNotFoundException {
    connection.close();
  }
}

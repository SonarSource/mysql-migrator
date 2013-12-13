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

    databaseUtils.makeDatabaseJavaObject();
  }

  @Test
  public void testGetSchemaOfDatabaseSource() throws Exception {
    Database database = databaseUtils.getJavaDatabaseFromUtils();
    metadataGetter.getSchemaOfDatabaseSource(connection, database);
    assertEquals("TABLE_FOR_TEST", database.getTables().get(1).getName());
    assertEquals("EMPTY_TABLE_FOR_TEST", database.getTables().get(0).getName());
    assertEquals(2, database.getTables().size());
    assertEquals("COLUMNINTEGER", database.getTables().get(1).getColumns().get(0).getName());
  }

  @After
  public void closeEveryThing() throws SQLException, ClassNotFoundException {
    connection.close();
  }
}

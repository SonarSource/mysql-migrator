/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import static junit.framework.Assert.*;

public class DataGetterTest {

  private DataGetter dataGetter;
  private DatabaseUtils databaseUtilsSource;
  private Database databaseFromUtils;
  private Connection connectionFromUtils;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException, IOException {
    databaseUtilsSource = new DatabaseUtils();

    /* MAKE DB JAVA OBJECT TO RECORD DATAS */
    databaseUtilsSource.makeDatabaseJavaObject();
    databaseUtilsSource.addTablesToDatabaseJavaObject();
    databaseUtilsSource.addColumnsToDatabaseJavaObject();
    databaseFromUtils = databaseUtilsSource.getJavaDatabaseFromUtils();

    /* MAKE FILLED H2 DATABASE TO BE THE SOURCE */
    databaseUtilsSource.makeDatabaseH2Withtables("sonarToRead");
    databaseUtilsSource.insertDatasInH2Tables();
    connectionFromUtils = databaseUtilsSource.getConnectionFromH2();

    /* BUILD DataGetter OBJECT */
    dataGetter = new DataGetter();
    dataGetter.createStatement(connectionFromUtils);
    dataGetter.recordDatas(databaseFromUtils);

  }

  @Test
  public void testRecordDatas() throws SQLException, ClassNotFoundException {

    assertNotNull(dataGetter.getStatementSource());

    /* VERIFY FIRST TABLE FROM JAVA DB */
    assertEquals("table_for_test", databaseFromUtils.getTableName(0));
    assertEquals("COLUMNINTEGER", databaseFromUtils.getColumn(0, 0).getName());
    assertEquals("COLUMNSTRING", databaseFromUtils.getColumn(0, 1).getName());
    assertEquals("COLUMNTIMESTAMP", databaseFromUtils.getColumn(0, 2).getName());
    assertEquals(2, databaseFromUtils.getTable(0).getNbRows());

    assertEquals(5, databaseFromUtils.getData(0, 0, 0));
    assertEquals(8, databaseFromUtils.getData(0, 0, 1));
    assertEquals("This is a second string for test", databaseFromUtils.getData(0, 1, 0));
    assertEquals("This is a first string for test", databaseFromUtils.getData(0, 1, 1));
    assertEquals(new Timestamp(456789), databaseFromUtils.getData(0, 2, 0));
    assertEquals(new Timestamp(123456), databaseFromUtils.getData(0, 2, 1));

    /* VERIFY SECOND TABLE */
    assertEquals("empty_table_for_test", databaseFromUtils.getTables().get(1).getName());
    assertEquals("ID", databaseFromUtils.getColumn(1, 0).getName());
    assertEquals("COLSTRING", databaseFromUtils.getColumn(1, 1).getName());
    assertEquals("COLTIMESTAMP", databaseFromUtils.getColumn(1, 2).getName());
    assertEquals(0, databaseFromUtils.getTable(1).getNbRows());

    dataGetter.getStatementSource().close();

    assertTrue(dataGetter.getStatementSource().isClosed());
  }

  @After
  public void closeEveryThing() throws SQLException {
    connectionFromUtils.close();
  }
}

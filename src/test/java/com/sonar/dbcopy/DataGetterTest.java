/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class DataGetterTest {

  private DataGetter dataGetter;
  private DatabaseUtils databaseUtilsSource;
  private Bdd bddFromUtils;
  private Connection connectionFromUtils;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    databaseUtilsSource = new DatabaseUtils();

    /* MAKE BDD JAVA OBJECT TO RECORD DATAS */
    databaseUtilsSource.makeBddJavaObject();
    bddFromUtils = databaseUtilsSource.getJavaBddFromUtils();

    /* MAKE FILLED H2 DATABASE TO BE THE SOURCE */
    databaseUtilsSource.makeDatabaseH2Withtables("sonarToRead");
    databaseUtilsSource.insertDatasInH2Tables();
    connectionFromUtils = databaseUtilsSource.getConnectionFromH2();

    /* BUILD DataGetter OBJECT */
    dataGetter = new DataGetter();
    dataGetter.createStatement(connectionFromUtils);
    dataGetter.writeDataInJavaBdd(bddFromUtils.getBddTables());

  }

  @Test
  public void verifywriteDataInJavaBdd() throws SQLException, ClassNotFoundException {

    assertNotNull(dataGetter.getStatementSource());

    /* VERIFY FIRST TABLE FROM JAVA BDD*/
    assertEquals("table_for_test",bddFromUtils.getBddTables().get(0).getTableName());
    assertEquals("COLUMNINTEGER",bddFromUtils.getColumnFromTable(0,0).getColumnName());
    assertEquals("COLUMNSTRING",bddFromUtils.getColumnFromTable(0,1).getColumnName());
    assertEquals("COLUMNTIMESTAMP",bddFromUtils.getColumnFromTable(0,2).getColumnName());
    assertEquals(2,bddFromUtils.getBddTables().get(0).getNbRows());

    assertEquals(1,bddFromUtils.getDataFromColumnFromTable(0,0,0));
    assertEquals(2,bddFromUtils.getDataFromColumnFromTable(0,0,1));
    assertEquals("This is a first string for test",bddFromUtils.getDataFromColumnFromTable(0,1,0));
    assertEquals("This is a second string for test",bddFromUtils.getDataFromColumnFromTable(0,1,1));
    assertEquals(new Timestamp(123456),bddFromUtils.getDataFromColumnFromTable(0,2,0));
    assertEquals(new Timestamp(456789),bddFromUtils.getDataFromColumnFromTable(0,2,1));

    /* VERIFY SECOND TABLE */
    assertEquals("empty_table_for_test",bddFromUtils.getBddTables().get(1).getTableName());
    assertEquals("ID",bddFromUtils.getColumnFromTable(1,0).getColumnName());
    assertEquals("COLSTRING",bddFromUtils.getColumnFromTable(1,1).getColumnName());
    assertEquals("COLTIMESTAMP",bddFromUtils.getColumnFromTable(1,2).getColumnName());
    assertEquals(0,bddFromUtils.getBddTables().get(1).getNbRows());

    dataGetter.getStatementSource().close();

    assertTrue(dataGetter.getStatementSource().isClosed());
  }
  @After
  public void  closeEveryThing() throws SQLException {
    connectionFromUtils.close();
  }
}

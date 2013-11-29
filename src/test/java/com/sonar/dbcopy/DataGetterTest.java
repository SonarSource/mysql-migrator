/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import static junit.framework.Assert.assertEquals;

public class DataGetterTest {

  private DataGetter dataGetter;
  private DatabaseUtils databaseUtilsSource;
  private Bdd bddFromUtils;
  private Statement statementFromUtils;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    /* MAKE FILLED DATABASE TO BE THE SOURCE */
    databaseUtilsSource = new DatabaseUtils();
    databaseUtilsSource.makeDatabaseH2("sonarToRead");
    databaseUtilsSource.insertDatasInH2();

    /* MAKE BDD JAVA OBJECT TO RECORD DATAS */
    bddFromUtils = databaseUtilsSource.makeBddJavaObject();
  }

  @Test
  public void testDoRequest() throws SQLException, ClassNotFoundException {
    /* CONNECT TO BDD H2 TO READ IT */
    statementFromUtils = databaseUtilsSource.getStatementFromH2();
    dataGetter = new DataGetter();
    dataGetter.doRequest(statementFromUtils,bddFromUtils.getBddTables());

    /* VERIFY FIRST TABLE */
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

    statementFromUtils.close();
  }
}

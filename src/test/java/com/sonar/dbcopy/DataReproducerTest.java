/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.Assert.assertEquals;

public class DataReproducerTest {

  private Utils utilsSource, utilsDest;
  private DataReproducer dataReproducer;
  private ResultSet sourceResultSet, destResultSet;
  private Statement sourceStatement, destStatement;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    utilsSource = new Utils();
    utilsSource.makeDatabaseH2Withtables("source");
    utilsSource.insertDatasInH2Tables();


    utilsDest = new Utils();
    utilsDest.makeDatabaseH2Withtables("destination");

    utilsSource.makeDatabaseJavaObject();
    utilsSource.addTablesToDatabaseJavaObject();
    utilsSource.addColumnsToDatabaseJavaObject();

    dataReproducer = new DataReproducer();
    dataReproducer.doCopy(utilsSource.getConnectionFromH2(), utilsDest.getConnectionFromH2(), utilsSource.getJavaDatabaseFromUtils());

    sourceStatement = utilsSource.getConnectionFromH2().createStatement();
    destStatement = utilsDest.getConnectionFromH2().createStatement();

  }

  @Test
  public void testDoCopy() throws Exception {
    sourceResultSet = sourceStatement.executeQuery("SELECT * FROM table_for_test");
    destResultSet = destStatement.executeQuery("SELECT * FROM table_for_test");

    // BE CAREFULL WITH Utils FOR MAX indexRow (=2) AND THE NUMBER OF COLUMNS USED IN getObject(?)
    for (int indexRow = 1; indexRow < 2; indexRow++) {
      sourceResultSet.next();
      destResultSet.next();

      int i = sourceResultSet.getInt(1);
      int j = destResultSet.getInt(1);

      assertEquals(sourceResultSet.getObject(1), destResultSet.getObject(1));
      assertEquals(sourceResultSet.getObject(2), destResultSet.getObject(2));
      assertEquals(sourceResultSet.getObject(3), destResultSet.getObject(3));
    }
  }

  @After
  public void testCloseResultSet() throws Exception {
    sourceResultSet.close();
    sourceStatement.close();
    utilsSource.getConnectionFromH2().close();

    destResultSet.close();
    destStatement.close();
    utilsDest.getConnectionFromH2().close();
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.toolconfig.DbException;
import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterDatas;
import com.sonar.dbcopy.utils.data.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DeleterTest {

  private Deleter deleter;
  private Database databaseSource;
  private Connection connectionSource;

  @Before
  public void setUp() {
    boolean threeTablesInJavaDb= false;

    Utils utils = new Utils();

    ConnecterDatas cdSource = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:filledDatabase;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");

    databaseSource = utils.makeDatabase(threeTablesInJavaDb);  // build dabase with metadatas
    Database databaseDest = utils.makeDatabase(threeTablesInJavaDb);

    connectionSource = utils.makeFilledH2("filledDatabase",threeTablesInJavaDb); // build a filled  H2 databaseSource

    new Deleter(cdSource, databaseSource).execute(databaseDest);


  }

  @Test
  public void testExecute() {
    Statement statement = null;
    ResultSet resultSet = null, resultSetTables = null;
    try {
      /* FIRST VERIFYING THE METADATAS OF TABLES DELETED (2 TABLES) ARE STILL PRESENT */
      DatabaseMetaData metaData = connectionSource.getMetaData();
      resultSetTables = metaData.getTables(connectionSource.getCatalog(), null, "%", new String[]{"TABLE"});
      resultSetTables.next();
      assertEquals(databaseSource.getTableName(1).toUpperCase(), resultSetTables.getString(3).toUpperCase());
      resultSetTables.next();
      assertEquals(databaseSource.getTableName(0).toUpperCase(), resultSetTables.getString(3).toUpperCase());
      resultSetTables.close();

      /* SECONDLY VERIFYING THAT TABLE_FOR_TEST DOESN'T HAVE ANY ROW OF DATA */
      statement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
      resultSet = statement.executeQuery("SELECT * FROM table_for_test");
      while (resultSet.next()) {
        assertNull(resultSet.getObject(1));
      }

    } catch (SQLException e) {
      throw new DbException("Problem when testing deleter.", e);
    } finally {
      try {
        if (resultSetTables != null) {
          resultSetTables.close();
        }
        resultSet.close();
      } catch (SQLException e) {
        throw new DbException("Problem To close resultset when testing deleter.", e);
      }
      try {
        statement.close();
      } catch (SQLException e) {
        throw new DbException("Problem To close statement when testing deleter.", e);
      }
    }
  }

  @After
  public void tearDown() {
    try {
      connectionSource.close();
    } catch (SQLException e) {
      throw new DbException("Problem To close connection source when testing deleter.", e);
    }
  }
}
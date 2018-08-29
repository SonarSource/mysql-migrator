/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

public class DeleterTest {

  private Deleter deleter;
  private Database databaseSource, databaseToBeDeleted, databaseToBeDeletedWithMissingTable;
  private Connection connectionToBeDeleted, connectionToBeDeletedWithMissingTable;
  private Utils utils;
  private Closer closer;
  private ConnecterData cdToBeDeleted, cdToBeDeletedWithMissingTable;

  @Before
  public void setUp() {
    closer = new Closer("DeleterTest");
    boolean aThirdTableIsAddedInDb = true;
    utils = new Utils();
    databaseSource = utils.makeDatabase(aThirdTableIsAddedInDb);
    databaseToBeDeleted = utils.makeDatabase(aThirdTableIsAddedInDb);
    databaseToBeDeletedWithMissingTable = utils.makeDatabase(false);

    // build a filled  H2 databaseSource
    cdToBeDeleted = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:DeleterTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    connectionToBeDeleted = utils.makeFilledH2("DeleterTestDB", aThirdTableIsAddedInDb);

    cdToBeDeletedWithMissingTable = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:DeleterTestWithMissingTableDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    connectionToBeDeletedWithMissingTable = utils.makeFilledH2("DeleterTestWithMissingTableDB", false);
  }

  @Test
  public void testExecute() {
    new Deleter(cdToBeDeleted, databaseSource).execute(databaseToBeDeleted);

    Statement statement = null;
    ResultSet resultSet = null, resultSetTables = null;
    try {
      /* FIRST VERIFYING THE METADATAS OF TABLES DELETED (2 TABLES) ARE STILL PRESENT */
      DatabaseMetaData metaData = connectionToBeDeleted.getMetaData();
      resultSetTables = metaData.getTables(connectionToBeDeleted.getCatalog(), null, "%", new String[]{"TABLE"});
      resultSetTables.next();
      assertEquals(databaseSource.getTableName(1).toUpperCase(), resultSetTables.getString(3).toUpperCase());
      resultSetTables.next();
      assertEquals(databaseSource.getTableName(2).toUpperCase(), resultSetTables.getString(3).toUpperCase());
      resultSetTables.next();
      assertEquals(databaseSource.getTableName(0).toUpperCase(), resultSetTables.getString(3).toUpperCase());
      closer.closeResultSet(resultSetTables);

      /* SECONDLY VERIFYING THAT TABLE_FOR_TEST DOESN'T HAVE ANY ROW OF DATA */
      statement = connectionToBeDeleted.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
      resultSet = statement.executeQuery("SELECT * FROM table_for_test");
      while (resultSet.next()) {
        assertNull(resultSet.getObject(1));
      }
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem in DeleterTest.", e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeResultSet(resultSetTables);
      closer.closeStatement(statement);
    }
  }

  @Test
  public void exceptionsTest() throws SQLException {
    new Deleter(cdToBeDeletedWithMissingTable, databaseSource).execute(databaseToBeDeletedWithMissingTable);
    DatabaseMetaData metaData = null;
    ResultSet resultSetTables = null;

    metaData = connectionToBeDeletedWithMissingTable.getMetaData();
    resultSetTables = metaData.getTables(connectionToBeDeletedWithMissingTable.getCatalog(), null, "%", new String[]{"TABLE"});
    resultSetTables.next();
    assertEquals(databaseSource.getTableName(1).toUpperCase(), resultSetTables.getString(3).toUpperCase());
    resultSetTables.next();
    assertEquals(databaseSource.getTableName(0).toUpperCase(), resultSetTables.getString(3).toUpperCase());
    resultSetTables.next();
    try {
      resultSetTables.getString(3);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SQLException.class).hasMessage("No data is available [2000-197]");
    } finally {
      closer.closeResultSet(resultSetTables);
    }
  }

  @After
  public void tearDown() {
    closer.closeConnection(connectionToBeDeleted);
    closer.closeConnection(connectionToBeDeletedWithMissingTable);
  }
}

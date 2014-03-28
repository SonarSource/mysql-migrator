/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.reader;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ReaderToolTest {

  private ArrayList<ReaderTool> readerToolList;
  private Connection connection;
  private Statement statement;
  private ResultSet resultSet;
  private Closer closer;

  @Before
  public void setUp() throws Exception {
    closer = new Closer("ReaderTootTest");
    readerToolList = new ArrayList<ReaderTool>();
    readerToolList.add(new H2Reader());
    readerToolList.add(new MySqlReader());
    readerToolList.add(new OracleReader());
    readerToolList.add(new PostgresqlReader());
    readerToolList.add(new SqlServerReader());

    Utils utils = new Utils();
    connection = utils.makeFilledH2("source", false);
    statement = connection.createStatement();
    resultSet = statement.executeQuery("SELECT * FROM table_for_test");
  }

  @After
  public void tearDown() throws Exception {
    closer.closeResultSet(resultSet);
    closer.closeStatement(statement);
    closer.closeConnection(connection);
  }

  @Test
  public void testReadTimestamp() throws Exception {
    // TO UNDERSTAND indexColumn GO AND SEE utils.makeFilledH2
    int indexColumn = 2;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        Assert.assertEquals(resultSet.getTimestamp(indexColumn + 1), readerToolList.get(indexReader).readTimestamp(resultSet, indexColumn));
      }
    }
  }

  @Test
  public void testReadBlob() throws Exception {
    // TO UNDERSTAND indexColumn GO AND SEE utils.makeFilledH2
    int indexColumn = 3;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        // ONLY COMPARE LENGTH FOR BLOB TO MAKE IT EASIER
        Assert.assertEquals(resultSet.getBlob(indexColumn + 1).length(), readerToolList.get(indexReader).readBlob(resultSet, indexColumn).length);
      }
    }
  }

  @Test
  public void testReadClob() throws Exception {
    // TO UNDERSTAND indexColumn GO AND SEE utils.makeFilledH2
    int indexColumn = 4;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        // ONLY COMPARE LENGTH FOR CLOB TO MAKE IT EASIER
        Assert.assertEquals(resultSet.getClob(indexColumn + 1).length(), readerToolList.get(indexReader).readBlob(resultSet, indexColumn).length);
      }
    }
  }

  @Test
  public void testReadBoolean() throws Exception {
    // TO UNDERSTAND indexColumn GO AND SEE utils.makeFilledH2
    int indexColumn = 5;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        Assert.assertEquals(resultSet.getBoolean(indexColumn + 1), readerToolList.get(indexReader).readBoolean(resultSet, indexColumn));
      }
    }
  }

  @Test
  public void testReadObject() throws Exception {
    // TO UNDERSTAND indexColumn GO AND SEE utils.makeFilledH2
    int indexColumn = 0;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        Assert.assertEquals(resultSet.getObject(indexColumn + 1), readerToolList.get(indexReader).readObject(resultSet, indexColumn));
      }
    }
  }

  @Test
  public void testReadVarchar() throws Exception {
    // TO UNDERSTAND indexColumn GO AND SEE utils.makeFilledH2
    int indexColumn = 1;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        Assert.assertEquals(resultSet.getString(indexColumn + 1), readerToolList.get(indexReader).readVarchar(resultSet, indexColumn));
      }
    }
  }
}

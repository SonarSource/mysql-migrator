/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.Utils;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CloserTest {

  private Connection connection;
  private Statement statement;
  private ResultSet resultSet;
  private Closer closer;

  @Before
  public void setUp() throws SQLException {
    closer = new Closer("CloserTest");
    Utils utils = new Utils();
    connection = utils.makeFilledH2("CloserTestDB",false);
    statement = connection.createStatement();
    resultSet = statement.executeQuery("SELECT * FROM table_for_test");
  }

  @Test
  public void testAll() {
    assertNotNull(connection);
    assertNotNull(statement);
    assertNotNull(resultSet);

    closer.closeResultSet(resultSet);
    closer.closeStatement(statement);
    closer.closeConnection(connection);

    try {
      assertTrue(resultSet.isClosed());
      assertTrue(statement.isClosed());
      assertTrue(connection.isClosed());
    } catch (Exception e) {
      throw new SqlDbException("Problem in CloserTest", e);
    }
    closer.closeResultSet(resultSet);
    closer.closeStatement(statement);
    closer.closeConnection(connection);
  }
}

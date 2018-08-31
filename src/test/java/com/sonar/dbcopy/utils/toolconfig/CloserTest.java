/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.Utils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class CloserTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
      throw new SqlDbCopyException("Problem in CloserTest", e);
    }
    closer.closeResultSet(resultSet);
    closer.closeStatement(statement);
    closer.closeConnection(connection);
  }

  @Test
  public void closeConnection_throw_SQLDbCopyException_instead_of_SQLException() throws SQLException {
    Connection connection = mock(Connection.class);
    doThrow(new SQLException()).when(connection).close();

    expectedException.expect(SqlDbCopyException.class);
    closer.closeConnection(connection);
  }

  @Test
  public void closeStatement_throw_SQLDbCopyException_instead_of_SQLException() throws SQLException {
    Statement statement = mock(Statement.class);
    doThrow(new SQLException()).when(statement).close();

    expectedException.expect(SqlDbCopyException.class);
    closer.closeStatement(statement);
  }

  @Test
  public void closeResultset_throw_SQLDbCopyException_instead_of_SQLException() throws SQLException {
    ResultSet resultSet = mock(ResultSet.class);
    doThrow(new SQLException()).when(resultSet).close();

    expectedException.expect(SqlDbCopyException.class);
    closer.closeResultSet(resultSet);
  }
}

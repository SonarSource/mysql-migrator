/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import junit.framework.Assert;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static junit.framework.Assert.assertNotNull;

public class SimpleConnectionTest {

  private SimpleConnection simpleConnection;
  private Connection connection;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    JdbcConnectionPool.create("jdbc:h2:mem:sonar;DB_CLOSE_DELAY=-1", "sonar", "sonar");

    simpleConnection = new SimpleConnection();
    connection = simpleConnection.openConnection("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");
  }

  @Test
  public void verifyMethodsAboutConnectionInSimpleConnectionClass() throws Exception {
    assertNotNull(simpleConnection);
    assertNotNull(connection);

    simpleConnection.closeConnection();
    Assert.assertTrue(connection.isClosed());
  }

  @After
  public void closeEveryThing() throws SQLException, ClassNotFoundException {
    connection.close();
  }
}

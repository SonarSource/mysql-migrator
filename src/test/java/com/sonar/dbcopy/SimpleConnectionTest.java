/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcConnectionPool;

public class SimpleConnectionTest {

  private SimpleConnection simpleConnection;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    JdbcConnectionPool.create("jdbc:h2:mem:sonar;DB_CLOSE_DELAY=-1", "sonar", "sonar");

    simpleConnection = new SimpleConnection();
    simpleConnection.doConnection("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");

  }

  @Test
  public void testMethodsAboutConnectionAndStatementInSimpleConnectionClass() throws Exception {
    assertNotNull(simpleConnection);
    assertNotNull(simpleConnection.getConnection());

    simpleConnection.doStatement();
    assertNotNull(simpleConnection.getStatement());

    simpleConnection.closeStatement();
    assertTrue(simpleConnection.getStatement().isClosed());

    simpleConnection.closeConnection();
    Assert.assertTrue(simpleConnection.getConnection().isClosed());

  }

}

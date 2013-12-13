/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class ConnecterTest {

  private Connecter connecter;
  private DatabaseUtils databaseUtils;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    databaseUtils = new DatabaseUtils();
    databaseUtils.makeDatabaseH2Withtables("sonar");

    connecter = new Connecter();
    connecter.doSourceConnection("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");
    connecter.doDestinationConnection("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");

  }

  @Test
  public void testDatabaseConnecter() throws Exception {
    assertNotNull(connecter.getConnectionSource());
    assertNotNull(connecter.getConnectionDest());

    connecter.closeSource();
    connecter.closeDestination();

    assertTrue(connecter.getConnectionSource().isClosed());
    assertTrue(connecter.getConnectionDest().isClosed());
  }

  @After
  public void closeEveryThing() throws SQLException, ClassNotFoundException {
    databaseUtils.getConnectionFromH2().close();
  }
}

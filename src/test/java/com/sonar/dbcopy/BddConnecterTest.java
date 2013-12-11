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

public class BddConnecterTest {

  private BddConnecter bddConnecter;
  private DatabaseUtils databaseUtils;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    databaseUtils = new DatabaseUtils();
    databaseUtils.makeDatabaseH2Withtables("sonar");

    bddConnecter = new BddConnecter();
    bddConnecter.doSourceConnection("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");
    bddConnecter.doDestinationConnection("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");

  }
  @Test
  public void testBddConnecter() throws Exception {
    assertNotNull(bddConnecter.getSourceConnection());
    assertNotNull(bddConnecter.getDestConnection());

    bddConnecter.closeSourceConnection();
    bddConnecter.closeDestConnection();

    assertTrue(bddConnecter.getSourceConnection().isClosed());
    assertTrue(bddConnecter.getDestConnection().isClosed());
  }
  @After
  public void  closeEveryThing() throws SQLException, ClassNotFoundException {
    databaseUtils.getConnectionFromH2().close();
  }
}

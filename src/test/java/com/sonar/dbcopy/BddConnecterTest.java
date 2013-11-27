/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Before;
import org.junit.Test;
import java.sql.SQLException;
import static junit.framework.Assert.assertNotNull;

public class BddConnecterTest {

  private BddConnecter bddConnecter;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    JdbcConnectionPool.create("jdbc:h2:mem:sonar;DB_CLOSE_DELAY=-1", "sonar", "sonar");
    bddConnecter = new BddConnecter();
    bddConnecter.doSourceConnectionAndStatement("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");
    bddConnecter.doOnlyDestinationConnection("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");

  }
  @Test
  public void verifyBddConnecterCreation() throws Exception {
    assertNotNull(bddConnecter.getStatementSource());
    assertNotNull(bddConnecter.getConnectionDest());
  }

}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.toolconfig.DbException;
import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConnectionVerifierTest {

  private ConnectionVerifier connectionVerifier;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    utils.makeH2("ConnectionVerifierTestDB");
    connectionVerifier = new ConnectionVerifier();
  }

  @Test
  public void testDatabaseIsReached() throws Exception {
    ConnecterData availableCd = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:ConnectionVerifierTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    ConnectionVerifier connectionVerifier = new ConnectionVerifier();
    connectionVerifier.databaseIsReached(availableCd);
  }

  @Test
  public void testDatabaseIsNotReached() {
    ConnecterData notAvailableURL = new ConnecterData("org.h2.Driver", ":h2:mem:ConnectionVerifierTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    try {
      connectionVerifier.databaseIsReached(notAvailableURL);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("*** DATABASE CAN'T BE REACHED AT ADDRESS :h2:mem:ConnectionVerifierTestDB;DB_CLOSE_ON_EXIT=-1; ***");
    }

    ConnecterData notAvailableDriver = new ConnecterData("org..Driver", "jdbc:h2:mem:ConnectionVerifierTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    try {
      connectionVerifier.databaseIsReached(notAvailableDriver);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("*** DRIVER org..Driver CAN'T BE REACHED ***");
    }

  }
}

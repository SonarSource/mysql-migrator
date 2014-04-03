/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.toolconfig.MessageDbException;
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
    ConnecterData notAvailableURL = new ConnecterData("org.h2.Driver", "wrongUrl", "sonar", "sonar");
    try {
      connectionVerifier.databaseIsReached(notAvailableURL);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(MessageDbException.class).hasMessage("ERROR: Database can not be reached at url wrongUrl. Verify url, user name and password. No suitable driver found for wrongUrl");
    }

    ConnecterData notAvailableDriver = new ConnecterData("wrong.Driver", "jdbc:h2:mem:ConnectionVerifierTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    try {
      connectionVerifier.databaseIsReached(notAvailableDriver);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(MessageDbException.class).hasMessage("ERROR: Driver wrong.Driver does not exists : wrong.Driver");
    }

  }
}

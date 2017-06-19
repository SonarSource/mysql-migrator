/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConnecterTest {

  private Connection connectionWorking, connectionFailingDriver, connectionFailingUrl;

  @Before
  public void setUp() {
    Utils utils = new Utils();
    utils.makeEmptyH2("ConnecterTestDB", false);
  }

  @Test
  public void testDoConnection() {
    ConnecterData connecterData = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:ConnecterTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    Connecter connecter = new Connecter();
    connectionWorking = connecter.doConnection(connecterData);
    assertThat(connectionWorking).isInstanceOf(Connection.class);
  }

  @Test
  public void testDoConnectionFailedForWrongDriver() {
    ConnecterData connecterData = new ConnecterData("wrongDriver", "jdbc:h2:mem:ConnecterTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    try {
      Connecter connecter = new Connecter();
      connectionFailingDriver = connecter.doConnection(connecterData);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SqlDbCopyException.class).hasMessage("Impossible to get the jdbc DRIVER wrongDriver.");
    }
  }

  @Test
  public void testDoConnectionFailedForWrongUrl() {
    ConnecterData connecterData = new ConnecterData("org.h2.Driver", "wrongUrl", "sonar", "sonar");
    try {
      Connecter connecter = new Connecter();
      connectionFailingUrl = connecter.doConnection(connecterData);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SqlDbCopyException.class).hasMessage("Open connection failed with URL :wrongUrl .");
    }
  }

  @After
  public void tearDown() {
    Closer closer = new Closer("ConnecterTest");
    closer.closeConnection(connectionWorking);
    closer.closeConnection(connectionFailingDriver);
    closer.closeConnection(connectionFailingUrl);
  }
}

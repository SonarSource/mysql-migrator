/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConnecterTest {


  @Before
  public void setUp() {
    Utils utils = new Utils();
    utils.makeEmptyH2("databaseToConnect",false);
  }

  @Test
  public void testDoConnection() {
    ConnecterData connecterData = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:databaseToConnect;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    Connecter connecter = new Connecter();
    assertThat(connecter.doConnection(connecterData)).isInstanceOf(Connection.class);
  }

  @Test
  public void testDoConnectionFailedForWrongDriver() {
    ConnecterData connecterData = new ConnecterData("wrongDriver", "jdbc:h2:mem:databaseToConnect;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    try {
      Connecter connecter = new Connecter();
      Connection connection = connecter.doConnection(connecterData);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("Impossible to get the jdbc DRIVER wrongDriver.");
    }
  }

  @Test
  public void testDoConnectionFailedForWrongUrl() {
    ConnecterData connecterData = new ConnecterData("org.h2.Driver", "wrongUrl", "sonar", "sonar");
    try {
      Connecter connecter = new Connecter();
      Connection connection = connecter.doConnection(connecterData);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("Open connection failed with URL :wrongUrl .");
    }
  }
}

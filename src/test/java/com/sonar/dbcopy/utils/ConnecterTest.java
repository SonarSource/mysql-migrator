/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils;

import com.sonar.dbcopy.Utils;
import com.sonar.dbcopy.utils.objects.ConnecterDatas;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConnecterTest {


  @Before
  public void setUp() {
    Utils utils = new Utils();
    utils.makeEmptyH2("databaseToConnect");
  }

  @Test
  public void testDoConnection() {
    ConnecterDatas connecterDatas = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:databaseToConnect;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    Connecter connecter = new Connecter();
    assertThat(connecter.doConnection(connecterDatas)).isInstanceOf(Connection.class);
  }

  @Test
  public void testDoConnectionFailed() {
    ConnecterDatas connecterDatas = new ConnecterDatas("wrongDriver", "jdbc:h2:mem:databaseToConnect;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    try {
      Connecter connecter = new Connecter();
      Connection connection = connecter.doConnection(connecterDatas);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("Impossible to get the jdbc DRIVER wrongDriver.");
    }
  }
}

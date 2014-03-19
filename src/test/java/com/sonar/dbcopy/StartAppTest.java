/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import com.sonar.dbcopy.utils.Utils;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

public class StartAppTest {

  @Before
  public void setUp() {
    Utils utils = new Utils();
    Connection connectionSource = utils.makeFilledH2("source", true);
    utils.addContentInThirdTable(connectionSource, 1);

    Connection connectionDest = utils.makeEmptyH2("destination", true);
    utils.addContentInThirdTable(connectionDest, 1);
  }

  @Test
  public void testMain() throws Exception {

    String[] args = {"org.h2.Driver", "jdbc:h2:mem:source;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar", "org.h2.Driver", "jdbc:h2:mem:destination;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar"};
    StartApp startApp = new StartApp();
    startApp.main(args);

  }
}

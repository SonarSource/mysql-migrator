/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.DbException;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class StartAppTest {

  @Before
  public void setUp() {
    Utils utils = new Utils();
    Connection connectionSource = utils.makeFilledH2("StartAppTestSourceDB", true);
    utils.addContentInThirdTable(connectionSource, 1);

    Connection connectionDest = utils.makeEmptyH2("StartAppTestDestinationDB", true);
    utils.addContentInThirdTable(connectionDest, 1);

    Connection connectionWithDifferentVersion = utils.makeEmptyH2("StartAppTestDestinationWithWrongVersionDB", true);
    utils.addContentInThirdTable(connectionWithDifferentVersion, 2);
  }

  @Test
  public void testMain() throws Exception {

    String[] args = {
      "-driverSrc", "org.h2.Driver",
      "-urlSrc", "jdbc:h2:mem:StartAppTestSourceDB;DB_CLOSE_ON_EXIT=-1;",
      "-userSrc", "sonar",
      "-pwdSrc", "sonar",
      "-driverDest", "org.h2.Driver",
      "-urlDest", "jdbc:h2:mem:StartAppTestDestinationDB;DB_CLOSE_ON_EXIT=-1;",
      "-userDest", "sonar",
      "-pwdDest", "sonar"
    };
    StartApp startApp = new StartApp();
    startApp.main(args);

    String[] argsBadVersion = {
      "-driverSrc", "org.h2.Driver",
      "-urlSrc", "jdbc:h2:mem:StartAppTestSourceDB;DB_CLOSE_ON_EXIT=-1;",
      "-userSrc", "sonar",
      "-pwdSrc", "sonar",
      "-driverDest", "org.h2.Driver",
      "-urlDest", "jdbc:h2:mem:StartAppTestDestinationWithWrongVersionDB;DB_CLOSE_ON_EXIT=-1;",
      "-userDest", "sonar",
      "-pwdDest", "sonar"
    };
    try {
      startApp.main(argsBadVersion);
      fail();
    } catch (DbException e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("Version of schema migration are not the same between source (1) and destination (2).");
    }

  }
}

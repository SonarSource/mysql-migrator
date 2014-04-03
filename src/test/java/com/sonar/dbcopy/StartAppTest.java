/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.MessageDbException;
import org.junit.After;
import org.junit.Test;

import java.sql.Connection;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class StartAppTest {

  private Connection connectionSourceV1, connectionSourceV2, connectionDestV1, connectiondestV3;

  @Test
  public void testMain() throws Exception {

    Utils utils = new Utils();
    connectionSourceV1 = utils.makeFilledH2("StartAppTest_Source_Version_1_DB", true);
    utils.addContentInThirdTable(connectionSourceV1, 1);

    connectionDestV1 = utils.makeEmptyH2("StartAppTest_Destination_Version_1_DB", true);
    utils.addContentInThirdTable(connectionDestV1, 1);

    Closer closer = new Closer("startAppTestMain");
    closer.closeConnection(connectionSourceV1);
    closer.closeConnection(connectionDestV1);

    String[] args = {
      "-driverSrc", "org.h2.Driver",
      "-urlSrc", "jdbc:h2:mem:StartAppTest_Source_Version_1_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userSrc", "sonar",
      "-pwdSrc", "sonar",
      "-driverDest", "org.h2.Driver",
      "-urlDest", "jdbc:h2:mem:StartAppTest_Destination_Version_1_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userDest", "sonar",
      "-pwdDest", "sonar"
    };
    StartApp startApp = new StartApp();
    startApp.main(args);
  }

  @Test
  public void testMainWithDifferentVersion() throws Exception {
    Utils utils = new Utils();
    connectionSourceV2 = utils.makeFilledH2("StartAppTest_Source_Version_2_DB", true);
    utils.addContentInThirdTable(connectionSourceV2, 2);

    connectiondestV3 = utils.makeEmptyH2("StartAppTest_Destination_Version_3_DB", true);
    utils.addContentInThirdTable(connectiondestV3, 3);

    Closer closer = new Closer("startAppTestMainWithDifferentVersion");
    closer.closeConnection(connectionSourceV2);
    closer.closeConnection(connectiondestV3);

    String[] argsBadVersion = {
      "-driverSrc", "org.h2.Driver",
      "-urlSrc", "jdbc:h2:mem:StartAppTest_Source_Version_2_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userSrc", "sonar",
      "-pwdSrc", "sonar",
      "-driverDest", "org.h2.Driver",
      "-urlDest", "jdbc:h2:mem:StartAppTest_Destination_Version_3_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userDest", "sonar",
      "-pwdDest", "sonar"
    };
    try {
      StartApp startApp = new StartApp();
      startApp.main(argsBadVersion);
      fail();
    } catch (MessageDbException e) {
      assertThat(e).isInstanceOf(MessageDbException.class).hasMessage("ERROR : Version of schema migration are not the same between source (2) and destination (3).");
    }

  }

  @After
  public void tearDown() {
    Closer closer = new Closer("startAppTesttearDown");
    closer.closeConnection(connectionSourceV1);
    closer.closeConnection(connectionDestV1);
    closer.closeConnection(connectionSourceV2);
    closer.closeConnection(connectiondestV3);
  }

}

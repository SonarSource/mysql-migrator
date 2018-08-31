/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;
import static org.fest.assertions.Assertions.assertThat;

public class VersionVerifierTest {

  private Connection connectionSource, connectionDest;
  private Utils utils;

  @Before
  public void setUp() {
    utils = new Utils();
    connectionSource = utils.makeFilledH2("VersionVerifierTestSourceDB", true);
    connectionDest = utils.makeEmptyH2("VersionVerifierTestDestinationDB", true);
    utils.addContentInThirdTable(connectionSource, 1);
    utils.addContentInThirdTable(connectionDest, 1);
  }

  @After
  public void tearDown() {
    Closer closer = new Closer("VersionVerifierTest");
    closer.closeConnection(connectionSource);
    closer.closeConnection(connectionDest);
  }

  @Test
  public void testLastVersionId() {
    ConnecterData cdSource = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:VersionVerifierTestSourceDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    ConnecterData cdDest = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:VersionVerifierTestDestinationDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");

    VersionVerifier versionVerifier = new VersionVerifier();
    assertEquals(versionVerifier.lastVersionId(cdSource), versionVerifier.lastVersionId(cdDest));

    utils.addContentInThirdTable(connectionDest, 2);
    assertEquals(2, versionVerifier.lastVersionId(cdDest));
  }

  @Test
  public void testLastVersionIdExceptions() {
    ConnecterData cdWithWrongUrl = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:wrongDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    ConnecterData cdWithWrongDriver = new ConnecterData("not_a_driver", "jdbc:h2:mem:VersionVerifierTestDestinationDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    VersionVerifier versionVerifier = new VersionVerifier();

    try {
      versionVerifier.lastVersionId(cdWithWrongUrl);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SqlDbCopyException.class).hasMessage("Problem when verifying version database. Please build your destination database with SonarQube at the same SonarQube source version.");
    }

    try {
      versionVerifier.lastVersionId(cdWithWrongDriver);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(MessageException.class).hasMessage("Driver not_a_driver does not exist. Class not found: not_a_driver");
    }
  }
}

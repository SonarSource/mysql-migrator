/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.DbException;
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
    connectionSource = utils.makeFilledH2("source", true);
    connectionDest = utils.makeEmptyH2("destination", true);
    utils.addContentInThirdTable(connectionSource, 1);
    utils.addContentInThirdTable(connectionDest, 1);


  }

  @Test
  public void testLastVersionId() throws Exception {
    ConnecterData cdSource = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:source;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    ConnecterData cdDest = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:destination;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");

    VersionVerifier versionVerifier = new VersionVerifier();
    assertEquals(versionVerifier.lastVersionId(cdSource), versionVerifier.lastVersionId(cdDest));

    utils.addContentInThirdTable(connectionDest, 2);
    assertEquals(2, versionVerifier.lastVersionId(cdDest));
  }

  @Test
  public void testLastVersionIdExceptions() throws Exception {
    ConnecterData cdWithWrongUrl = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:wrongDataBase;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    ConnecterData cdWithWrongDriver = new ConnecterData("not_a_driver", "jdbc:h2:mem:destination;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    VersionVerifier versionVerifier = new VersionVerifier();

    try {
      versionVerifier.lastVersionId(cdWithWrongUrl);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("Problem in VersionVerifier.");
    }

    try {
      versionVerifier.lastVersionId(cdWithWrongDriver);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("*** DRIVER not_a_driver CAN'T BE REACHED ***");
    }
  }
}

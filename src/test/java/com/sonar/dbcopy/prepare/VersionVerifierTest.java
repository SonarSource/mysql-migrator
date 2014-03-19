/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterDatas;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

import static junit.framework.Assert.assertEquals;

public class VersionVerifierTest {

  private Connection connectionSource, connectionDest;
  private Utils utils;
  @Before
  public void setUp() {
    utils = new Utils();
    connectionSource = utils.makeFilledH2("source",true);
    connectionDest=utils.makeEmptyH2("destination",true);
    utils.addContentInThirdTable(connectionSource,1);
    utils.addContentInThirdTable(connectionDest,1);


  }

  @Test
  public void testLastVersionId() throws Exception {
    ConnecterDatas cdSource = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:source;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    ConnecterDatas cdDest = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:destination;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");

    VersionVerifier versionVerifier = new VersionVerifier();
    assertEquals(versionVerifier.lastVersionId(cdSource), versionVerifier.lastVersionId(cdDest));

    utils.addContentInThirdTable(connectionDest,2);
    assertEquals(2, versionVerifier.lastVersionId(cdDest));
  }
}

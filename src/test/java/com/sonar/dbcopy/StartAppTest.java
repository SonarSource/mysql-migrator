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
    Connection connectionSource = utils.makeFilledH2("source", true);
    utils.addContentInThirdTable(connectionSource, 1);

    Connection connectionDest = utils.makeEmptyH2("destination", true);
    utils.addContentInThirdTable(connectionDest, 1);

    Connection connectionVersionDifferent = utils.makeEmptyH2("destinationWithBadVersion", true);
    utils.addContentInThirdTable(connectionVersionDifferent, 2);
  }

  @Test
  public void testMain() throws Exception {

    String[] args = {"org.h2.Driver", "jdbc:h2:mem:source;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar", "org.h2.Driver", "jdbc:h2:mem:destination;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar"};
    StartApp startApp = new StartApp();
    startApp.main(args);

    String[] argsBadVersion = {"org.h2.Driver", "jdbc:h2:mem:source;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar", "org.h2.Driver", "jdbc:h2:mem:destinationWithBadVersion;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar"};
    try{
      startApp.main(argsBadVersion);
      fail();

    } catch (DbException e){
      assertThat(e).isInstanceOf(DbException.class).hasMessage("Version of schema migration are not the same between source (1) and destination (2).");
    }

  }
}

/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StartAppTest {

  @Rule
  public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private Connection connectionSourceV1, connectionSourceV2, connectionDestV1, connectiondestV3, connectionSourceToption, connectionDestToption;

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

    // no driveDest to check the computation of the driver from the URL.
    String[] args = {
      "-driverSrc", "org.h2.Driver",
      "-urlSrc", "jdbc:h2:mem:StartAppTest_Source_Version_1_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userSrc", "sonar",
      "-pwdSrc", "sonar",
      "-urlDest", "jdbc:h2:mem:StartAppTest_Destination_Version_1_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userDest", "sonar",
      "-pwdDest", "sonar"
    };
    StartApp.main(args);

    assertThat(systemOutRule.getLog().contains("THE COPY HAS FINISHED SUCCESSFULLY") ).isTrue();
  }

  @Test
  public void testMainWithTableOption() throws Exception {

    Utils utils = new Utils();
    connectionSourceToption = utils.makeFilledH2("StartAppTest_Source_T_option_DB", true);
    utils.addContentInThirdTable(connectionSourceToption, 1);
    utils.addContentInThirdTable(connectionSourceToption, 2);

    connectionDestToption = utils.makeEmptyH2("StartAppTest_Destination_T_option_DB", true);
    utils.addContentInThirdTable(connectionDestToption, 2);

    // no driveSrc to check the computation of the driver from the URL.
    String[] args = {
      "-urlSrc", "jdbc:h2:mem:StartAppTest_Source_T_option_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userSrc", "sonar",
      "-pwdSrc", "sonar",
      "-driverDest", "org.h2.Driver",
      "-urlDest", "jdbc:h2:mem:StartAppTest_Destination_T_option_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userDest", "sonar",
      "-pwdDest", "sonar",
      "-T", "table_for_test"
    };
    StartApp.main(args);

    Statement statementDestToption = connectionDestToption.createStatement();
    ResultSet resultSet = statementDestToption.executeQuery("SELECT * FROM schema_migrations");
    // THE TEST WORKS IF THE TABLE schema_migrations DON'T HAVE A ROW WITH version=1 WHICH WOULD COME FROM SOURCE
    while (resultSet.next()) {
      assertEquals(2, resultSet.getInt(1));
    }

    // AND THE TABLE  table_for_test MUST BE FULL AND HAVE A RESULTSET WHITH DATA
    resultSet = statementDestToption.executeQuery("SELECT * FROM table_for_test");
    assertThat(resultSet).isNotNull();
    while (resultSet.next()) {
      assertThat(resultSet.getInt(1)).isNotNull();
    }

    Closer closer = new Closer("startAppTestMain");
    closer.closeConnection(connectionSourceToption);
    closer.closeConnection(connectionDestToption);

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
      StartApp.main(argsBadVersion);
      fail();
    } catch (MessageException e) {
      assertThat(e).isInstanceOf(MessageException.class).hasMessage("Version of schema migration are not the same between source (2) and destination (3).");
    }
  }

  @Test
  public void testHelp() throws Exception {

    String[] helpArgument = {"-help"};
    StartApp.main(helpArgument);

    assertThat(systemOutRule.getLog().startsWith("usage:") ).isTrue();

    // test a same line does not contain Src and dest, this would be a mix of options
    assertThat(systemOutRule.getLog().matches("^ -\\w+Src.*destination$") ).isFalse();
    assertThat(systemOutRule.getLog().matches("^ -\\w+Dest.*source$") ).isFalse();

  }

  @Test
  public void testVersion() throws Exception {

    String[] versionArgument = {"-version"};
    StartApp.main(versionArgument);

    assertThat (systemOutRule.getLog().contains("Java ")).isTrue();
  }

  @Test
  public void testMissingArgument() throws Exception {
    thrown.expect(MessageException.class);

    String[] aloneArgument = {"-urlSrc"};
    StartApp.main(aloneArgument);
    fail();
  }

  @Test
  public void testBadArgument() throws Exception {
    thrown.expect(MessageException.class);

    String[] badArgument = {"badArgument"};
    StartApp.main(badArgument);
    fail();
  }

  @Test
  public void testBadCommitSize() throws Exception {
    thrown.expect(MessageException.class);

    String[] badArgument = {
      "-urlSrc", "jdbc:h2:mem:StartAppTest_Source_Version_2_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userSrc", "sonar",
      "-pwdSrc", "sonar",
      "-urlDest", "jdbc:h2:mem:StartAppTest_Destination_Version_3_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userDest", "sonar",
      "-pwdDest", "sonar",
      "-commitSize", "notAnInteger"
    };
    StartApp.main(badArgument);
    fail();
  }

  @After
  public void tearDown() {
    Closer closer = new Closer("startAppTesttearDown");
    closer.closeConnection(connectionSourceToption);
    closer.closeConnection(connectionDestToption);
    closer.closeConnection(connectionSourceV1);
    closer.closeConnection(connectionDestV1);
    closer.closeConnection(connectionSourceV2);
    closer.closeConnection(connectiondestV3);
  }

}

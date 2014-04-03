/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.SqlDbException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;


public class SequenceReseterTest {

  private Connection connection;

  @Before
  public void setUp() {
    Utils utils = new Utils();
    connection = utils.makeFilledH2("SequenceReseterTestDB",false);
  }

  @Test
  public void testExecuteWithoutJdbcDriver() throws Exception {
    ConnecterData connecterData = new ConnecterData("Not a Driver", "jdbc:h2:mem:SequenceReseterTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    SequenceReseter sequenceReseter = new SequenceReseter("table_for_test", connecterData);
    try {
      sequenceReseter.execute();
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SqlDbException.class).hasMessage("Impossible to get the jdbc DRIVER Not a Driver.");
    }
  }

  @Test
  public void testExecute() throws Exception {
    // ONLY VERIFY THERE IS NO EXCEPTION BUT DON'T KNOW IF IT WORKS BECAUSE IT NEEDS ORACLE CONNECTION
    ConnecterData connecterData = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:SequenceReseterTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    SequenceReseter sequenceReseter = new SequenceReseter("table_for_test", connecterData);
    sequenceReseter.execute();
  }

  @After
  public void tearDown() throws SQLException {
    connection.close();
  }
}

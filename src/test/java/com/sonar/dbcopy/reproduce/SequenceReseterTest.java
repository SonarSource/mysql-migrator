/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce;

import com.sonar.dbcopy.reproduce.process.SequenceReseter;
import com.sonar.dbcopy.utils.data.ConnecterDatas;
import com.sonar.dbcopy.utils.toolconfig.DbException;
import com.sonar.dbcopy.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;


public class SequenceReseterTest {

  private Connection connection;

  @Before
  public void setUp() {
    Utils utils = new Utils();
    connection = utils.makeFilledH2("source");
  }

  @Test
  public void testExecute() throws Exception {
    // NON COMPLIANT
    ConnecterDatas connecterDatas = new ConnecterDatas("","jdbc:h2:mem:sonar;DB_CLOSE_ON_EXIT=-1;","sonar","sonar");
//    SequenceReseter sequenceReseter = new SequenceReseter("table_for_test", connecterDatas);
    try {
//      sequenceReseter.execute();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("Problem to reset autoincrement with last id in SequenceReseter.");
    }
  }

  @After
  public void tearDown() throws SQLException {
    connection.close();
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import com.sonar.dbcopy.reproduce.SequenceReseter;
import com.sonar.dbcopy.utils.DbException;
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
    SequenceReseter sequenceReseter = new SequenceReseter("table_for_test", connection);
    try {
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("Problem to reset autoincrement with last id in SequenceReseter.");
    }
  }

  @After
  public void tearDown() throws SQLException {
    connection.close();
  }
}

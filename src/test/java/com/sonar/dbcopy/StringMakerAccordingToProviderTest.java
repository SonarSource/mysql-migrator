/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static junit.framework.Assert.assertEquals;

public class StringMakerAccordingToProviderTest {

  private Connection connection;
  private DatabaseUtils databaseUtils;
  private StringMakerAccordingToProvider provider;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    databaseUtils = new DatabaseUtils();
    databaseUtils.makeDatabaseH2Withtables("sonar");
    connection = databaseUtils.getConnectionFromH2();
    provider = new StringMakerAccordingToProvider();
  }

  @Test
  public void getSqlRequest() throws SQLException {
    String response = provider.getSqlRequest(connection);
    assertEquals("SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = 'PUBLIC' ORDER BY 1", response);
  }
}
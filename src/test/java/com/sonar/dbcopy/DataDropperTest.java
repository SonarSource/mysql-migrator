/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;

import static junit.framework.Assert.*;
import static org.fest.assertions.Assertions.assertThat;

public class DataDropperTest {

  private Connection connection;
  private Database database;
  private DataDropper dataDropper;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {
    DatabaseUtils databaseUtils = new DatabaseUtils();
    databaseUtils.makeDatabaseH2Withtables("sonar");
    databaseUtils.insertDatasInH2Tables();
    connection = databaseUtils.getConnectionFromH2();

    databaseUtils.makeDatabaseJavaObject();
    databaseUtils.addTablesToDatabaseJavaObject();
    databaseUtils.addColumnsToDatabaseJavaObject();
    databaseUtils.addDatasToDatabaseJavaObject();
    database = databaseUtils.getJavaDatabaseFromUtils();

    dataDropper = new DataDropper();

    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT * FROM table_for_test");
    resultSet.next();
    assertEquals(8, resultSet.getObject(1));
    assertEquals("This is a first string for test",resultSet.getObject(2));
    assertEquals(new Timestamp(123456),resultSet.getObject(3));
  }

  @Test
  public void testDeleteDatas() throws SQLException, IOException {
    dataDropper.deleteDatas(connection,database);

    assertNotNull(connection);
    Statement statement = connection.createStatement();
    assertNotNull(statement);
    ResultSet resultSet = statement.executeQuery("SELECT * FROM table_for_test");
    assertThat(resultSet.next()).isFalse();
  }
}
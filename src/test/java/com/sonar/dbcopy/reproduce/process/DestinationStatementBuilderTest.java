/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

import static junit.framework.TestCase.fail;
import static org.fest.assertions.Assertions.assertThat;


public class DestinationStatementBuilderTest {

  private Connection connection;
  private Database database;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    connection = utils.makeEmptyH2("DestinationStatementBuilderTestDB", false);
    database = utils.makeDatabase(false);

  }

  @After
  public void tearDown() throws Exception {
    Closer closer = new Closer("DestinationStatementBuilderTest");
    closer.closeConnection(connection);
  }

  @Test
  public void testGetDestinationStatement() throws Exception {
    DestinationStatementBuilder destinationStatementBuilder = new DestinationStatementBuilder();

    PreparedStatement preparedStatement = destinationStatementBuilder.getDestinationStatement(connection, database.getTable(0));
    assertThat(preparedStatement).isInstanceOf(PreparedStatement.class);
    // THE REQUEST RETURNED BY preparedStatement.toString() HAVE A NUMBER LIKE prep145:
    // SO WE MUST SPLIT THE STRING IN 2 PARTS SEPARATED BY " "  AND USE request[1] IN ASSERTEQUALS
    String[] request = preparedStatement.toString().split(" ", 2);
    Assert.assertEquals("INSERT INTO TABLE_FOR_TEST (id,columnstring,columntimestamp,columnblob,columnclob,columnboolean,columntobenull) VALUES(?,?,?,?,?,?,?)", request[1]);

    Table tableWrong = new Table("no_existent_table");
    tableWrong.addColumn(0, "first", Types.INTEGER);
    tableWrong.addColumn(1, "second", Types.VARCHAR);
    try {
      preparedStatement = destinationStatementBuilder.getDestinationStatement(connection, tableWrong);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SqlDbCopyException.class).hasMessage("Problem when buiding destination prepared statement");
    }
  }
}

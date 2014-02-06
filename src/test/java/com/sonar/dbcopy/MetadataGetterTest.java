/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class MetadataGetterTest {

  private MetadataGetter mdg;
  private Database database;
  private Connection connectionForFilled, connectionWithoutTable;

  @Before
  public void setUp() throws SQLException, ClassNotFoundException {
    Utils utils = new Utils();
    database = new Database();
    connectionForFilled = utils.makeFilledH2("filledDatabase");
    connectionWithoutTable = utils.makeH2("withoutTables");
  }

  @Test
  public void testFilledDatabase() throws Exception {
    ConnecterDatas cdSource = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:filledDatabase;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    mdg = new MetadataGetter(cdSource, database);
    mdg.execute();

    assertNotNull(mdg);
    assertEquals(2, database.getNbTables());

    assertEquals("empty_table_for_test", database.getTableName(0));
    assertEquals("id", database.getTable(0).getColumnName(0));
    assertEquals("colstring", database.getTable(0).getColumnName(1));
    assertEquals("coltimestamp", database.getTable(0).getColumnName(2));

    assertEquals("table_for_test", database.getTableName(1));
    assertEquals(2, database.getTable(1).getNbRows());
    assertEquals(3, database.getTable(1).getNbColumns());
    assertEquals("columninteger", database.getTable(1).getColumnName(0));
    assertEquals("columnstring", database.getTable(1).getColumnName(1));
    assertEquals("columntimestamp", database.getTable(1).getColumnName(2));
  }

  @Test
  public void testWithoutTablesDataBase() throws Exception {
    ConnecterDatas cdSource = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:withoutTables;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    try {
      mdg = new MetadataGetter(cdSource, database);
      mdg.execute();
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(DbException.class).hasMessage("*** ERROR : CAN'T FIND ANY TABLE IN DATABASE SOURCE ***");
    }
  }

  @After
  public void tearDown() {
    try {
      connectionForFilled.close();
    } catch (SQLException e) {
      throw new DbException("Impossible to close H2 connection.", e);
    }
  }
}

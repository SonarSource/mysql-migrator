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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetadataGetterTest {

  private MetadataGetter mdg;
  private Database database;
  private Connection connectionSource;

  @Before
  public void setUp() {
    Utils utils = new Utils();
    database = new Database();
    connectionSource = utils.makeH2Source();

    ConnecterDatas cdSource = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:source;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    mdg = new MetadataGetter(cdSource, database);
    mdg.execute();
  }

  @Test
  public void testExecute() throws Exception {
    assertNotNull(mdg);
    assertEquals(2, database.getNbTables());

    assertEquals("EMPTY_TABLE_FOR_TEST", database.getTableName(0));
    assertEquals("ID", database.getTable(0).getColumnName(0));
    assertEquals("COLSTRING", database.getTable(0).getColumnName(1));
    assertEquals("COLTIMESTAMP", database.getTable(0).getColumnName(2));

    assertEquals("TABLE_FOR_TEST", database.getTableName(1));
    assertEquals(2, database.getTable(1).getNbRows());
    assertEquals(3, database.getTable(1).getNbColumns());
    assertEquals("COLUMNINTEGER", database.getTable(1).getColumnName(0));
    assertEquals("COLUMNSTRING", database.getTable(1).getColumnName(1));
    assertEquals("COLUMNTIMESTAMP", database.getTable(1).getColumnName(2));
  }

  @After
  public void tearDown() {
    try {
      connectionSource.close();
    } catch (SQLException e) {
      throw new DbException("Impossible to close H2 connection.", e);
    }
  }
}

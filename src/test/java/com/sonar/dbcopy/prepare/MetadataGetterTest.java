/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.MessageDbException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

public class MetadataGetterTest {

  private MetadataGetter mdg;
  private Connection connectionForFilled, connectionWithoutTable;
  private Closer closer;

  @Before
  public void setUp() throws SQLException, ClassNotFoundException {
    Utils utils = new Utils();
    connectionForFilled = utils.makeFilledH2("MetadataGetterTestFilledDB", false);
    connectionWithoutTable = utils.makeH2("MetadataGetterTestwithoutTablesDB");
    closer = new Closer("MetadataGetterTest");
  }

  @Test
  public void testFilledDatabase() throws Exception {
    ConnecterData cdSource = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:MetadataGetterTestFilledDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    Database database = new Database();

    mdg = new MetadataGetter(cdSource, database);
    mdg.execute(null);

    assertNotNull(mdg);
    assertEquals(2, database.getNbTables());

    assertEquals("empty_table_for_test", database.getTableName(0));
    assertEquals(0, database.getTable(0).getNbRows());
    assertEquals(3, database.getTable(0).getNbColumns());
    assertEquals("id", database.getTable(0).getColumnName(0));
    assertEquals("colstring", database.getTable(0).getColumnName(1));
    assertEquals("coltimestamp", database.getTable(0).getColumnName(2));

    assertEquals("table_for_test", database.getTableName(1));
    assertEquals(2, database.getTable(1).getNbRows());
    assertEquals(7, database.getTable(1).getNbColumns());
    assertEquals("id", database.getTable(1).getColumnName(0));
    assertEquals("columnstring", database.getTable(1).getColumnName(1));
    assertEquals("columntimestamp", database.getTable(1).getColumnName(2));
    assertEquals("columnblob", database.getTable(1).getColumnName(3));
    assertEquals("columnclob", database.getTable(1).getColumnName(4));
    assertEquals("columnboolean", database.getTable(1).getColumnName(5));
    assertEquals("columntobenull", database.getTable(1).getColumnName(6));

  }

  @Test
  public void testWithoutTablesDataBase() throws Exception {
    ConnecterData cdSource = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:MetadataGetterTestwithoutTablesDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    Database database = new Database();

    try {
      mdg = new MetadataGetter(cdSource, database);
      mdg.execute(null);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(MessageDbException.class).hasMessage("ERROR : can not find tables in database source.");
    }
  }

  @Test
  public void testFilledDatabaseWithOnlyOneTable() throws Exception {
    ConnecterData cdSource = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:MetadataGetterTestFilledDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    Database database = new Database();
    String[] tablesRequiredAsStringTab = {"table_for_test"};
    mdg = new MetadataGetter(cdSource, database);
    mdg.execute(tablesRequiredAsStringTab);

    assertNotNull(mdg);
    assertEquals(1, database.getNbTables());

    assertEquals("table_for_test", database.getTableName(0));
    assertEquals(2, database.getTable(0).getNbRows());
    assertEquals(7, database.getTable(0).getNbColumns());
    assertEquals("id", database.getTable(0).getColumnName(0));
    assertEquals("columnstring", database.getTable(0).getColumnName(1));
    assertEquals("columntimestamp", database.getTable(0).getColumnName(2));
    assertEquals("columnblob", database.getTable(0).getColumnName(3));
    assertEquals("columnclob", database.getTable(0).getColumnName(4));
    assertEquals("columnboolean", database.getTable(0).getColumnName(5));
    assertEquals("columntobenull", database.getTable(0).getColumnName(6));
    try {
      assertEquals("empty_table_for_test", database.getTableName(1));
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IndexOutOfBoundsException.class).hasMessage("Index: 1, Size: 1");
    }
  }

  @Test
  public void testFilledDatabaseWithAWrongTable() throws Exception {
    ConnecterData cdSource = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:MetadataGetterTestFilledDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    Database database = new Database();
    String[] tablesRequiredAsStringTab = {"non_existent_table"};
    mdg = new MetadataGetter(cdSource, database);

    try {
      mdg.execute(tablesRequiredAsStringTab);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(MessageDbException.class).hasMessage("ERROR: It seems that some table(s) you required in ( non_existent_table ) do not exist. Verify the name in the database.");
    }
  }

  @After
  public void tearDown() {
    closer.closeConnection(connectionForFilled);
    closer.closeConnection(connectionWithoutTable);
  }
}

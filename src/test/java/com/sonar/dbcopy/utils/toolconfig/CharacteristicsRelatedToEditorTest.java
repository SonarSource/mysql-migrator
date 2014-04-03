/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class CharacteristicsRelatedToEditorTest {

  private CharacteristicsRelatedToEditor chRelToEd;
  private DatabaseMetaData metaData;
  private Connection connection;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    connection = utils.makeFilledH2("CharacteristicsRelatedToEditorTestDB", false);
    metaData = connection.getMetaData();
    chRelToEd = new CharacteristicsRelatedToEditor();
  }

  @After
  public void tearDown() throws Exception {
    Closer closer = new Closer("CharacteristicsRelatedToEditorTest");
    closer.closeConnection(connection);
  }

  @Test
  public void testGetSchema() throws SQLException {
    assertEquals("PUBLIC", chRelToEd.getSchema(metaData));
  }

  @Test
  public void testGiveTableNameRelatedToVendor() throws SQLException {
    assertEquals("TABLE", chRelToEd.transfromCaseOfTableName(metaData, "table"));
  }
}

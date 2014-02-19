/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import com.sonar.dbcopy.utils.CharacteristicsRelatedToEditor;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class CharacteristicsRelatedToEditorTest {

  private CharacteristicsRelatedToEditor chRelToEd;
  private DatabaseMetaData metaData;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    Connection connection = utils.makeFilledH2("sonar");
    metaData = connection.getMetaData();
    chRelToEd = new CharacteristicsRelatedToEditor();
  }

  @Test
  public void testGetSchema() throws SQLException {
    assertEquals(null, chRelToEd.getSchema(metaData));
  }

  @Test
  public void testGiveTableNameRelatedToVendor() throws SQLException {
    assertEquals("TABLE", chRelToEd.transfromCaseOfTableName(metaData, "table"));
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class StringRelatedToVendorTest {

  private StringRelatedToVendor stringRelatedToVendor;
  private DatabaseMetaData metaData;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    Connection connection = utils.makeFilledH2("sonar");
    metaData = connection.getMetaData();
    stringRelatedToVendor = new StringRelatedToVendor(metaData);

  }

  @Test
  public void testGetSchema() throws SQLException {
    //assertEquals("public", stringRelatedToVendor.getSchema("jdbc:po"));
    assertEquals(null, stringRelatedToVendor.getSchema());
    //assertEquals(null, stringRelatedToVendor.getSchema("jdbc:my"));
    //assertEquals(null, stringRelatedToVendor.getSchema("jdbc:or"));
    //assertEquals(null, stringRelatedToVendor.getSchema("jdbc:sq"));
  }

  @Test
  public void testGiveTableNameRelatedToVendor () throws SQLException{
    assertEquals("TABLE", stringRelatedToVendor.giveTableNameRelatedToVendor("table"));
  }
}

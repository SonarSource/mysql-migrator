/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SchemaRelatedToVendorTest {

  private SchemaRelatedToVendor schemaRelatedToVendor;

  @Before
  public void setUp() throws Exception {
    schemaRelatedToVendor = new SchemaRelatedToVendor();
  }

  @Test
  public void testGetSchema() {
    assertEquals("public", schemaRelatedToVendor.getSchema("jdbc:po"));
    assertEquals(null, schemaRelatedToVendor.getSchema("jdbc:h2"));
    assertEquals(null, schemaRelatedToVendor.getSchema("jdbc:my"));
    assertEquals(null, schemaRelatedToVendor.getSchema("jdbc:or"));
    assertEquals(null, schemaRelatedToVendor.getSchema("jdbc:sq"));
  }
}

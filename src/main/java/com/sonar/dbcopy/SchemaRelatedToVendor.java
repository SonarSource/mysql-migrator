/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

public class SchemaRelatedToVendor {

  public String getSchema(String vendorUrl){
    String schema;
    if ("jdbc:po".equals(vendorUrl)) {
      schema = "public";
    } else if ("jdbc:h2".equals(vendorUrl)) {
      schema = null;
    } else if ("jdbc:my".equals(vendorUrl)) {
      schema = null;
    } else if ("jdbc:or".equals(vendorUrl)) {
      schema = null;
    } else if ("jdbc:sq".equals(vendorUrl)) {
      schema = null;
    } else {
      schema = null;
    }
    return schema;
  }
}

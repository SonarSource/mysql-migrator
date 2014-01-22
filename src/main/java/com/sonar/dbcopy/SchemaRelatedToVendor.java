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
    } else {
      schema = null;
    }
    return schema;
  }
}

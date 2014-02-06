/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class StringRelatedToVendor {

  private String vendorUrl;
  private DatabaseMetaData metaData;

  public StringRelatedToVendor(DatabaseMetaData metaData) throws SQLException {
    this.metaData = metaData;
    this.vendorUrl = metaData.getURL().substring(0, 7);
  }

  public String getSchema() throws SQLException {
    String schema;
    if ("jdbc:po".equals(vendorUrl)) {
      schema = "public";
    } else if ("jdbc:or".equals(vendorUrl)) {
      schema = metaData.getUserName().toUpperCase();
    } else {
      schema = null;
    }
    return schema;
  }

  public String giveTableNameRelatedToVendor(String tableNameToChangeCase) throws SQLException {
    String tableNameToReturn;
    if ("jdbc:or".equals(vendorUrl) || "jdbc:h2".equals(vendorUrl)) {
      tableNameToReturn = tableNameToChangeCase.toUpperCase();
    } else {
      tableNameToReturn = tableNameToChangeCase.toLowerCase();
    }
    return tableNameToReturn;
  }
}

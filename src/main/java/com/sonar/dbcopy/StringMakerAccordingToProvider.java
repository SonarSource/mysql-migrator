/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.Connection;
import java.sql.SQLException;

public class StringMakerAccordingToProvider {

  private static final String STRINGMYSQL = "SHOW TABLES";
  private static final String STRINGPOSTGRESQL = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY 1";
  private static final String STRINGSQLSERVER = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY 1";
  private static final String STRINGORACLE = "SELECT * FROM dba_tables";
  private static final String STRINGH2 = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = 'PUBLIC' ORDER BY 1";


  public String getSqlRequest(Connection connection) throws SQLException {
    String provider = connection.getMetaData().getDriverName();
    provider = provider.substring(0, 5).toLowerCase();

    String sqlRequestToReturn;

    if ("mysql".equals(provider)) {
      sqlRequestToReturn = STRINGMYSQL;
    } else if ("postg".equals(provider)) {
      sqlRequestToReturn = STRINGPOSTGRESQL;
    } else if ("sqlse".equals(provider)) {
      sqlRequestToReturn = STRINGSQLSERVER;
    } else if ("oracl".equals(provider)) {
      sqlRequestToReturn = STRINGORACLE;
    } else if ("h2 jd".equals(provider)) {
      sqlRequestToReturn = STRINGH2;
    } else {
      throw new SQLException("*** ERROR : Provider " + provider + " is not reconized to get schema database.", new Exception());
    }
    return sqlRequestToReturn;
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.Connection;
import java.sql.SQLException;

public class StringMakerAccordingToProvider {

  public String getSqlRequest(Connection connection) throws SQLException {
    String provider =connection.getMetaData().getDriverName();
    provider = provider.substring(0,5).toLowerCase();

    String sqlRequestToReturn;

    if(provider.equals("mysql")){
      sqlRequestToReturn = "SHOW TABLES";

    }
    else if (provider.equals("postg")){
      sqlRequestToReturn = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY 1";

    }
    else if (provider.equals("sqlse")){
      sqlRequestToReturn = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY 1";
    }
    else if (provider.equals("oracl")){
      sqlRequestToReturn = "select * from dba_tables";
    }
    else if (provider.equals("h2 jd")){
      sqlRequestToReturn = "select table_name from INFORMATION_SCHEMA.TABLES where table_schema = 'PUBLIC' ORDER BY 1";
    }
    else{
      throw new SQLException("*** ERROR : Provider "+provider+" is not reconized to get schema database.",new Exception());
    }


    return sqlRequestToReturn;
  }
}

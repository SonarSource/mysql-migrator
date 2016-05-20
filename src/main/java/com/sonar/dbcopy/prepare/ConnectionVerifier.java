/*
 * Copyright (C) 2013-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionVerifier {

  public void databaseIsReached(ConnecterData cd) {
    Connection connection = null;
    Closer closer = new Closer("ConnectionVerifier");
    try {
      Class.forName(cd.getDriver());
      connection = DriverManager.getConnection(cd.getUrl(), cd.getUser(), cd.getPwd());

      DatabaseMetaData metaData = connection.getMetaData();
      if (metaData == null) {
        throw new SQLException();
      }
    } catch (SQLException e) {
      throw new SqlDbCopyException("Database can not be reached at url " + cd.getUrl() + ". Verify url, user name and password.",e);
    } catch (ClassNotFoundException e) {
      throw new MessageException("Driver " + cd.getDriver() + " does not exists : " + e.getMessage());
    } finally {
      closer.closeConnection(connection);
    }
  }
}

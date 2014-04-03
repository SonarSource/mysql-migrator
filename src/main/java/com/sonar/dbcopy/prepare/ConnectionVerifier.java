/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.MessageDbException;

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
      throw new MessageDbException("ERROR: Database can not be reached at url " + cd.getUrl()+ ". Verify url, user name and password. "+e.getMessage());
    } catch (ClassNotFoundException e) {
      throw new MessageDbException("ERROR: Driver " + cd.getDriver() + " does not exists : "+e.getMessage());
    } finally {
      closer.closeConnection(connection);
    }
  }
}

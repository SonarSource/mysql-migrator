/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionVerifier {

  public void databaseIsReached(ConnecterDatas cd) {
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
      throw new DbException("*** DATABASE CAN'T BE REACHED AT ADDRESS " + cd.getUrl() + " ***", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("*** DRIVER " + cd.getDriver() + " CAN'T BE REACHED ***", e);
    } finally {
      closer.closeConnection(connection);
    }
  }
}

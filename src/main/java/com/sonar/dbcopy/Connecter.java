/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connecter {

  public Connection doSourceConnection(ConnecterDatas dc) {
    try {
      Class.forName(dc.getDriverSource());
      Connection sourceConnection = DriverManager.getConnection(dc.getUrlSource(), dc.getUserSource(), dc.getPwdSource());
      return sourceConnection;
    } catch (SQLException e) {
      throw new DbException("Open source connection failed.", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("Impossible to get the jdbc Driver Source.", e);
    }
  }

  public Connection doDestinationConnection(ConnecterDatas dc) {
    try {
      Class.forName(dc.getDriverDest());
      Connection destConnection = DriverManager.getConnection(dc.getUrlDest(), dc.getUserDest(), dc.getPwdDest());
      return destConnection;
    } catch (SQLException e) {
      throw new DbException("Open source connection failed.", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("Impossible to get the jdbc Driver Source.", e);
    }
  }
}

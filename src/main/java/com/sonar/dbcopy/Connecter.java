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

  private Connection sourceConnection, destConnection;

  public Connecter() {
  }

  public void doSourceConnection(String driverSource, String urlSource, String user, String pwd) {
    if (sourceConnection != null) {
      throw new IllegalStateException("Source Connection was already created.");
    } else {
      try {
        Class.forName(driverSource);
        sourceConnection = DriverManager.getConnection(urlSource, user, pwd);
      } catch (SQLException e) {
        throw new DbException("Open source connection failed.", e);
      } catch (ClassNotFoundException e) {
        throw new DbException("Impossible to get the jdbc Driver Source.", e);
      }
    }
  }

  public void doDestinationConnection(String driverDest, String urlDest, String user, String pwd) {
    if (destConnection != null) {
      throw new IllegalStateException("Destination Connection was already created.");
    } else {
      try {
        Class.forName(driverDest);
        destConnection = DriverManager.getConnection(urlDest, user, pwd);
      } catch (SQLException e) {
        throw new DbException("Open destination connection failed.", e);
      } catch (ClassNotFoundException e) {
        throw new DbException("Impossible to get the jdbc Driver destination.", e);
      }
    }
  }

  public Connection getConnectionSource() {
    return sourceConnection;
  }

  public Connection getConnectionDest() {
    return destConnection;
  }

  public void closeSource() {
    try {
      sourceConnection.close();
    } catch (SQLException e) {
      throw new DbException("Closing of connection failed.", e);
    }
  }

  public void closeDestination() {
    try {
      destConnection.close();
    } catch (SQLException e) {
      throw new DbException("Closing of connection failed.", e);
    }
  }

}

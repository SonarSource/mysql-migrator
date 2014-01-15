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

  public Connection doConnection(ConnecterDatas dc) {
    try {
      Class.forName(dc.getDriver());
      return DriverManager.getConnection(dc.getUrl(), dc.getUser(), dc.getPwd());
    } catch (SQLException e) {
      throw new DbException("Open source connection failed.", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("Impossible to get the jdbc Driver Source.", e);
    }
  }
}

/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.data.ConnecterData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connecter {

  public Connection doConnection(ConnecterData cd) {
    try {
      Class.forName(cd.getDriver());
      return DriverManager.getConnection(cd.getUrl(), cd.getUser(), cd.getPwd());
    } catch (SQLException e) {
      throw new SqlDbCopyException("Open connection failed with URL :" + cd.getUrl() + " .", e);
    } catch (ClassNotFoundException e) {
      throw new SqlDbCopyException("Impossible to get the jdbc DRIVER " + cd.getDriver() + ".", e);
    }
  }
}

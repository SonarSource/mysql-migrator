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

  public Connection doConnection(ConnecterDatas cd) {
    try {
      Class.forName(cd.getDriver());
      return DriverManager.getConnection(cd.getUrl(), cd.getUser(), cd.getPwd());
    } catch (SQLException e) {
      throw new DbException("Open connection failed with URL :"+cd.getUrl()+" .", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("Impossible to get the jdbc DRIVER "+cd.getDriver()+".", e);
    }
  }
}

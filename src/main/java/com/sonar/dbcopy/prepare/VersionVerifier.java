/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.Closer;
import com.sonar.dbcopy.utils.DbException;
import com.sonar.dbcopy.utils.objects.ConnecterDatas;

import java.sql.*;

public class VersionVerifier {

  private int maxVersionId = 0;

  public int lastVersionId(ConnecterDatas cd) {
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    Closer closer = new Closer("ConnectionVerifier");
    try {
      Class.forName(cd.getDriver());
      connection = DriverManager.getConnection(cd.getUrl(), cd.getUser(), cd.getPwd());
      statement = connection.createStatement();
      resultSet = statement.executeQuery("SELECT version FROM schema_migrations");

      while (resultSet.next()) {
        String versionString = resultSet.getString(1);
        int versionInteger = Integer.parseInt(versionString);
        if (versionInteger > maxVersionId) {
          maxVersionId = versionInteger;
        }
      }
      return maxVersionId;

    } catch (SQLException e) {
      throw new DbException("", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("*** DRIVER " + cd.getDriver() + " CAN'T BE REACHED ***", e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
      closer.closeConnection(connection);
    }
  }
}

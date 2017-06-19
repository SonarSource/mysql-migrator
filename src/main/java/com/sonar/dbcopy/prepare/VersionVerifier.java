/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class VersionVerifier {

  private int maxVersionId = 0;

  public int lastVersionId(ConnecterData cd) {
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    Closer closer = new Closer("ConnectionVerifier");
    CharacteristicsRelatedToEditor chRelToEd = new CharacteristicsRelatedToEditor();
    try {
      Class.forName(cd.getDriver());
      connection = DriverManager.getConnection(cd.getUrl(), cd.getUser(), cd.getPwd());
      statement = connection.createStatement();

      String tableNameSchemaMigration = chRelToEd.transfromCaseOfTableName(connection.getMetaData(), "schema_migrations");
      resultSet = statement.executeQuery("SELECT version FROM " + tableNameSchemaMigration);

      while (resultSet.next()) {
        String versionString = resultSet.getString(1);
        int versionInteger = Integer.parseInt(versionString);
        if (versionInteger > maxVersionId) {
          maxVersionId = versionInteger;
        }
      }
      return maxVersionId;

    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when verifying version database. Please build your destination database with SonarQube at the same SonarQube source version.", e);
    } catch (ClassNotFoundException e) {
      throw new MessageException("Driver " + cd.getDriver() + " does not exist. Class not found: " + e.getMessage());
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
      closer.closeConnection(connection);
    }
  }
}

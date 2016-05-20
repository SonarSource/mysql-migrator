/*
 * Copyright (C) 2013-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DestinationStatementBuilder {

  public PreparedStatement getDestinationStatement(Connection connectionDestination, Table tableSource) {
    PreparedStatement preparedStatementDest;
    CharacteristicsRelatedToEditor chRelToEd = new CharacteristicsRelatedToEditor();
    try {
      String tableSourceName = chRelToEd.transfromCaseOfTableName(connectionDestination.getMetaData(), tableSource.getName());

      String sqlInsertRequest = "INSERT INTO " + tableSourceName + " (" + tableSource.getColumnNamesAsString() + ") VALUES(" + tableSource.getQuestionMarksAsString() + ")";

      preparedStatementDest = connectionDestination.prepareStatement(sqlInsertRequest);
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when buiding destination prepared statement", e);
    }
    return preparedStatementDest;
  }
}


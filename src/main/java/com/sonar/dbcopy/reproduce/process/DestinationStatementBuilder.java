/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.DbException;
import com.sonar.dbcopy.utils.toolconfig.ListColumnsAsString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DestinationStatementBuilder {

  public PreparedStatement getDestinationStatement(Connection connectionDestination, Table tableSource) {
    PreparedStatement preparedStatementDest = null;
    String tableSourceName = tableSource.getName();

    ListColumnsAsString lcas = new ListColumnsAsString(tableSource);
    String sqlInsertRequest = "INSERT INTO " + tableSourceName + " (" + lcas.makeColumnString() + ") VALUES(" + lcas.makeQuestionMarkString() + ")";

    try {
      preparedStatementDest = connectionDestination.prepareStatement(sqlInsertRequest);
    } catch (SQLException e) {
      throw new DbException("Problem when buiding destination prepared statement", e);
    }
    return preparedStatementDest;
  }
}

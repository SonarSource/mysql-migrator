/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataDropper {
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public void deleteDatas(Connection connectionDest, Database database) throws IOException, SQLException {
    Statement statementToDelete = connectionDest.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
    try {
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        statementToDelete.execute("DELETE FROM " + database.getTableName(indexTable));
        LOGGER.log(Level.INFO, "TABLES  n° "+indexTable+": " + database.getTableName(indexTable) + " DELETED IN DESTINATION.");
      }
    } catch (SQLException e) {
      throw new DbException("Deleting datas from destination failed.", e);
    } finally {
      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - | ");
      statementToDelete.close();
      LOGGER.log(Level.INFO, " | StatementToDelete is closed.                    | ");
      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - | ");

    }
  }
}

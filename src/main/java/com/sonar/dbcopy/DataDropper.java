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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataDropper {
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public void deleteDatas(Connection connectionDest,List<Table> tableList) throws IOException, SQLException {
    Statement statementToDelete = connectionDest.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    try {
      for(int indexTable=0;indexTable<tableList.size();indexTable++){
        statementToDelete.execute("DELETE FROM "+tableList.get(indexTable).getTableName());
        LOGGER.log(Level.INFO,"TABLES " + tableList.get(indexTable) + " DELETED ON DESTINATION.");
      }
    } catch (SQLException e){
      throw new DbException("Deleting dats from destination failed.",e);
    } finally {
      statementToDelete.close();
    }
  }
}

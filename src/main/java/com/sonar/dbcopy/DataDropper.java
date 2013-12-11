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

public class DataDropper {

    public void deleteDatas(Connection connectionDest,List<Table> tableList) throws IOException, SQLException {
      LogDisplay logDisplay = new LogDisplay();

      Statement statementToDrop = connectionDest.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

      for(int indexTable=0;indexTable<tableList.size();indexTable++){
        statementToDrop.execute("DELETE FROM "+tableList.get(indexTable).getTableName());
        logDisplay.displayInformationLog("TABLES "+tableList.get(indexTable)+" DELETED ON DESTINATION.");
      }
      statementToDrop.close();
  }
}

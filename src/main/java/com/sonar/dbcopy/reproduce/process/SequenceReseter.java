/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.Connecter;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

public class SequenceReseter {

  private String sqlRequestToReset;
  private String tableName;
  private ConnecterData cdDest;

  public SequenceReseter(String tableName, ConnecterData cdDest) {
    this.tableName = tableName;
    this.cdDest = cdDest;
  }

  public void execute() {

    Closer closer = new Closer("SequenceReseter");
    Connection connectionDest = null;
    Statement statementDest = null;
    ResultSet resultSetDest = null;
    Connecter connecter = new Connecter();

    try {
      connectionDest = connecter.doConnection(cdDest);
      DatabaseMetaData metaDest = connectionDest.getMetaData();

      String tableNameWithGoodCase = CharacteristicsRelatedToEditor.transfromCaseOfTableName(metaDest, tableName);
      boolean destinationIsOracle = CharacteristicsRelatedToEditor.isOracle(metaDest);

      resultSetDest = metaDest.getPrimaryKeys(null, null, tableNameWithGoodCase);

      // IF REULTSET IS NOT "beforeFirst" THAT MEANS THERE IS A PRIMARY KEY
      if (resultSetDest.isBeforeFirst() && hasIdColumn(resultSetDest)) {
        closer.closeResultSet(resultSetDest);
        long idMaxPlusOne = CharacteristicsRelatedToEditor.getIdMaxPlusOne(connectionDest, tableName);
        sqlRequestToReset = CharacteristicsRelatedToEditor.makeAlterSequencesRequest(metaDest, tableName, idMaxPlusOne);

        // THEN RESET SEQUENCE REQUEST IS EXECUTED
        statementDest = connectionDest.createStatement();
        if (destinationIsOracle) {
          statementDest.execute(CharacteristicsRelatedToEditor.makeDropSequenceRequest(tableName));
        }
        statementDest.execute(sqlRequestToReset);
      }
    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem to execute the adjustment of autoincrement  with last id +1 :" + sqlRequestToReset + " at TABLE : " + tableName + ".", e);
    } finally {
      closer.closeResultSet(resultSetDest);
      closer.closeStatement(statementDest);
      closer.closeConnection(connectionDest);
    }
  }

  private static boolean hasIdColumn(ResultSet resultSetDest) {
    try {
      resultSetDest.findColumn("id");
      return true;
    } catch(SQLException noSuchColumn) {
      return false;
    }
  }
}


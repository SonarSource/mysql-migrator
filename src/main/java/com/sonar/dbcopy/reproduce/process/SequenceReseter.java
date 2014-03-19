/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.data.ConnecterDatas;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.Connecter;
import com.sonar.dbcopy.utils.toolconfig.DbException;

import java.sql.*;

public class SequenceReseter {

  private String sqlRequestToReset, tableName;
  private ConnecterDatas cdDest;

  public SequenceReseter(String tableName, ConnecterDatas cdDest) {
    this.tableName = tableName;
    this.cdDest = cdDest;
  }

  public void execute() {

    Closer closer = new Closer("SequenceReseter");
    Connection connectionDest = null;
    Statement statementDest = null;
    ResultSet resultSetDest = null;
    CharacteristicsRelatedToEditor relToEditor = new CharacteristicsRelatedToEditor();
    Connecter connecter = new Connecter();

    try {
      connectionDest = connecter.doConnection(cdDest);
      DatabaseMetaData metaDest = connectionDest.getMetaData();

      String tableNameWithGoodCase = relToEditor.transfromCaseOfTableName(metaDest, tableName);
      boolean destinationIsOracle = relToEditor.isOracle(metaDest);

      resultSetDest = metaDest.getPrimaryKeys(null, null, tableNameWithGoodCase);

      // IF REULTSET IS NOT "beforeFirst" THAT MEANS THERE IS A PRIMARY KEY
      if (resultSetDest.isBeforeFirst()) {
        closer.closeResultSet(resultSetDest);
        long idMaxPlusOne = relToEditor.getIdMaxPlusOne(connectionDest, tableName);
        sqlRequestToReset = relToEditor.makeAlterSequencesRequest(metaDest, tableName, idMaxPlusOne);

        // THEN RESET SEQUENCE REQUEST IS EXECUTED
        statementDest = connectionDest.createStatement();
        if (destinationIsOracle) {
          statementDest.execute(relToEditor.makeDropSequenceRequest(tableName));
        }
        statementDest.execute(sqlRequestToReset);
      }
    } catch (SQLException e) {
      throw new DbException("Problem to execute the adjustment of autoincrement  with last id +1 :" + sqlRequestToReset + " at TABLE : " + tableName + ".", e);
    } finally {
      closer.closeResultSet(resultSetDest);
      closer.closeStatement(statementDest);
      closer.closeConnection(connectionDest);
    }
  }
}


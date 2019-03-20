/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2013-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.Connecter;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

public class SequenceReseter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SequenceReseter.class);

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
        sqlRequestToReset = CharacteristicsRelatedToEditor.makeAlterSequencesRequest(connectionDest, metaDest, tableName, idMaxPlusOne);

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
      while(resultSetDest.next()) {
        if ("id".equalsIgnoreCase(resultSetDest.getString("COLUMN_NAME"))) {
          return true;
        }
      }
      return false;
    } catch(SQLException noSuchColumn) {
      LOGGER.error("Problem checking presence of ID column", noSuchColumn);
      return false;
    }
  }
}


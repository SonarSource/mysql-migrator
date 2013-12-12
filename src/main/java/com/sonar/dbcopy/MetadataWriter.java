/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MetadataWriter {

  public void addSchemaToBddDest(Connection connectionDest, Bdd bdd) throws SQLException {
    List<Table> tableList = bdd.getBddTables();
    Statement statementDest = connectionDest.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    for (int indexTable = 0; indexTable < tableList.size(); indexTable++) {
      Table tableToCreate = tableList.get(indexTable);
      String tableName = tableToCreate.getTableName();
      int nb = tableToCreate.getColumns().size();

      statementDest.execute("DROP TABLE IF EXISTS " + tableName);
      statementDest.execute("CREATE TABLE IF NOT EXISTS " + tableName + " ();");

      for (int indexColumn = 0; indexColumn < tableToCreate.getColumns().size(); indexColumn++) {

        /* GET CHARACTERISTICS FROM JAVA BDD */
        Column column = tableToCreate.getColumns().get(indexColumn);
        String colunmName = column.getColumnName();
        String columnType = column.getColumnType();
        int columnTypeSize = column.getColumnTypeSize();
        String canBeNull = column.getCanBeNull();

        /* CONCATENATION OF SQL STRING */
        String sqlAlterTableString = "ALTER TABLE " + tableName +
          " ADD COLUMN " + colunmName + " " + columnType;
        if (columnTypeSize != 0) {
          sqlAlterTableString += " (" + columnTypeSize + ") ";
        }
        sqlAlterTableString += " " + canBeNull;
        if ("id".equals(colunmName)) {
          sqlAlterTableString += " PRIMARY KEY";
        }
        sqlAlterTableString += " ;";

        /* EXCUTION OF SQL QUERY */
        statementDest.executeUpdate(sqlAlterTableString);
      }
    }
    statementDest.close();
  }
}

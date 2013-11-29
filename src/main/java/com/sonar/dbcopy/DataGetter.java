/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;
import java.util.List;

public class DataGetter {

  public DataGetter(){

  }

  public void doRequest(Statement statement, List <Table> tablesOfBdd ) throws SQLException {

    /* FOR EACH TABLE */
    for(int tableIndex=0;tableIndex<tablesOfBdd.size();tableIndex++){
      /* GET TABLE AND NAMETABLE */
      Table table = tablesOfBdd.get(tableIndex);
      String tableName = table.getTableName();

      /* GET RESULTSET FROM SQL REQUEST : SELECT * FROM tableName ORDER BY 1 */
      ResultSet resultSet =  statement.executeQuery("SELECT * FROM " +tableName+" ORDER BY 1;");

      /* GET NUMBER OF TABLE ROWS */
      int  nbRowsInTable = resultSet.last() ? resultSet.getRow() : 0;
      resultSet.beforeFirst();
      table.setNbRows(nbRowsInTable);

      /* GET THE METADATA OF THIS TABLE */
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      int columnNb = resultSetMetaData.getColumnCount();

      /* FOR EACH COLUMN OF THE TABLE */
      for(int columnIndex=1;columnIndex<=columnNb;columnIndex++){

        /* ADD THE COLUMN NAME AND COLUMN TYPE OF THE TABLE TO SET  */
        String columnName = resultSetMetaData.getColumnName(columnIndex);
        String  columnType = resultSetMetaData.getColumnTypeName(columnIndex);

        Column columnToSet = table.addOneColumnToTable(columnName);
        columnToSet.addColumnType(columnType);

        /* FOR EACH ROW OF THE CURRENT COLUMN , ADD IT IN THE JAVA BDD COLUMN OBJECT */
        while (resultSet.next()) {
          Object objectGetted =  resultSet.getObject(columnIndex);
          columnToSet.addDataObjectInColumn(objectGetted);
        }
        /* REPLACE CURSOR AT THE BEGINNING OF RESULTSET */
        resultSet.beforeFirst();
      }
      resultSet.close();
    }
    statement.close();
  }
}

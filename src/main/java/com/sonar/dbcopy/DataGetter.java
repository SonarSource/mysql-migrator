/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;
import java.util.List;

public class DataGetter {

  private Statement statement;
  private Bdd bdd;
  private ResultSet resultSet;
  private Column columnToSet;

  public DataGetter(Statement statement, Bdd bdd){
    this.statement = statement;
    this.bdd = bdd;
  }

  public void doRequest() throws SQLException {

    List <Table> tables_of_bdd = bdd.getBddTables();
    for(int tableIndex=0;tableIndex<tables_of_bdd.size();tableIndex++){
      /* GET TABLE AND NAMETABLE */
      Table table = tables_of_bdd.get(tableIndex);
      String tableName = table.getTableName();

      /* GET RESULTSET FROM SQL REQUEST : SELECT * FROM tableName ORDER BY 1 */
      resultSet =  this.statement.executeQuery("SELECT * FROM " +tableName+" ORDER BY 1;");

      /* GET NUMBER OF TABLE ROWS */
      int  nbRowsInTable = resultSet.last() ? resultSet.getRow() : 0;
      resultSet.beforeFirst();
      table.setNbRows(nbRowsInTable);

      /* GET THE METADATA OF THIS TABLE */
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      int columnNb = resultSetMetaData.getColumnCount();

      /* GET DATA FROM RESULTSET AND RECORD THEM IN Bdd */
      for(int columnIndex=1;columnIndex<=columnNb;columnIndex++){

        /* ADD THE COLUMN NAME AND COLUMN TYPE OF THE TABLE TO SET  */
        String columnName = resultSetMetaData.getColumnName(columnIndex);
        String  columnType = resultSetMetaData.getColumnTypeName(columnIndex);

        columnToSet = table.addOneColumnToTable(columnName);
        columnToSet.addColumnType(columnType);

        /* GET EACH LINE OF THE CURRENT COLUMN AND ADD IT IN THE JAVA Bdd COLUMN OBJECT */
        while (resultSet.next()) {
          Object objectGetted =  resultSet.getObject(columnIndex);
          columnToSet.addDataObjectInColumn(objectGetted);
        }
        /* REPLACE CURSOR AT THE BEGINNING OF RESULTSET */
        resultSet.beforeFirst();
      }
    }
    /* DEBUG AFFICHAGE Bdd */
    //DebugTableContent debug = new DebugTableContent(Bdd);
    //debug.AfficheColumnsAndRows();
  }
}

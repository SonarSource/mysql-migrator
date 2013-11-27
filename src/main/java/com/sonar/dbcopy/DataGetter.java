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
  private SonarBDD sonarBDD;
  private ResultSet resultSet;
  protected SonarColumn sonarColumnToSet;

  public DataGetter(Statement statement, SonarBDD sonarBDD){
    this.statement = statement;
    this.sonarBDD = sonarBDD;
  }

  public void doRequest() throws SQLException {
    List <SonarTable> tables_of_bdd = sonarBDD.getBDDTables();
    for(int tableIndex=0;tableIndex<tables_of_bdd.size();tableIndex++){
      /* GET TABLE AND NAMETABLE */
      SonarTable sonarTable= tables_of_bdd.get(tableIndex);
      String sonarTableName = sonarTable.getTableName();

      /* GET RESULTSET FROM SQL REQUEST : SELECT * FROM tableName ORDER BY 1 */
      resultSet =  statement.executeQuery("SELECT * FROM " + sonarTable.getTableName()+" ORDER BY 1;");

      /* GET NUMBER OF TABLE ROWS */
      int  nbRowsInTable = resultSet.last() ? resultSet.getRow() : 0;
      resultSet.beforeFirst();
      sonarTable.setNbRows(nbRowsInTable);

      /* GET THE METADATA OF THIS TABLE */
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      int columnNb = resultSetMetaData.getColumnCount();

      /* GET DATA FROM RESULTSET AND RECORD THEM IN SonarBDD */
      for(int columnIndex=1;columnIndex<=columnNb;columnIndex++){

        /* ADD THE COLUMN NAME AND COLUMN TYPE OF THE TABLE TO SET  */
        String columnName = resultSetMetaData.getColumnName(columnIndex);
        String  columnType = resultSetMetaData.getColumnTypeName(columnIndex);

        sonarColumnToSet = sonarTable.addOneColumnToTable(columnName,sonarTableName);
        sonarColumnToSet.addColumnType(columnType);

        /* GET EACH LINE OF THE CURRENT COLUMN AND ADD IT IN THE JAVA BDD COLUMN OBJECT */
        while (resultSet.next()) {
          Object objectGetted =  resultSet.getObject(columnIndex);
          sonarColumnToSet.addDataObjectInTable(objectGetted);
        }
        /* REPLACE CURSOR AT THE BEGINNING OF RESULTSET */
        resultSet.beforeFirst();
      }
    }
    /* DEBUG AFFICHAGE sonarBDD */
    //DebugTableContent debug = new DebugTableContent(sonarBDD);
    //debug.AfficheColumnsAndRows();
  }
}

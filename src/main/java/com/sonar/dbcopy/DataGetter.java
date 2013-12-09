/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;
import java.util.List;

public class DataGetter {

  private Statement sourceStatement;

  public DataGetter(){
  }

  public void createStatement(Connection sourceConnection) throws SQLException {
    sourceStatement = sourceConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
  }

  public void writeDataInJavaBdd(List <Table> tablesOfBdd ) throws SQLException {

    /* FOR EACH TABLE */
    for(int tableIndex=0;tableIndex<tablesOfBdd.size();tableIndex++){
      /* GET TABLE AND NAMETABLE */
      Table table = tablesOfBdd.get(tableIndex);
      String tableName = table.getTableName();

      /* GET RESULTSET FROM SQL REQUEST : SELECT * FROM tableName ORDER BY 1 */
      ResultSet resultSet =  sourceStatement.executeQuery("SELECT * FROM " +tableName+" ORDER BY 1;");

      /* GET NUMBER OF TABLE ROWS */
      int  nbRowsInTable = resultSet.last() ? resultSet.getRow() : 0;
      resultSet.beforeFirst();
      table.setNbRows(nbRowsInTable);

      /* GET THE METADATA OF THIS TABLE */
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      int columnNb = resultSetMetaData.getColumnCount();

      /* FOR EACH COLUMN OF THE TABLE */
      for(int columnIndex=0;columnIndex<columnNb;columnIndex++){

        /* ADD THE COLUMN NAME AND COLUMN TYPE OF THE TABLE TO SET  */
        // TODO : DELETE FOLLOWING BECAUSE BEFORE IT WAS DONE CREATING A NEW COLUMN BUT NOW THE BDD IS ALREADY BUILT BECAUSE OF THE SCHEMA COPY

        // TODO String columnName = resultSetMetaData.getColumnName(columnIndex);
        // TODO String  columnType = resultSetMetaData.getColumnTypeName(columnIndex);

        // TODO Column columnToSet = table.addOneColumnToTable(columnName);
        // TODO columnToSet.addColumnType(columnType);

        Column columnToSet = table.getColumns().get(columnIndex);
        // TODO if (columnName.equals(columnToSet.getColumnName())){System.out.println("go: "+columnName);}


        /* FOR EACH ROW OF THE CURRENT COLUMN , ADD IT IN THE JAVA BDD COLUMN OBJECT */
        while (resultSet.next()) {
          Object objectGetted =  resultSet.getObject(columnIndex+1);
          columnToSet.addDataObjectInColumn(objectGetted);
        }
        /* REPLACE CURSOR AT THE BEGINNING OF RESULTSET */
        resultSet.beforeFirst();
      }
      resultSet.close();
    }
  }

  public Statement getStatementSource(){
    return sourceStatement;
  }

  public void closeStatement() throws SQLException {
    sourceStatement.close();
  }
}

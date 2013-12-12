/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.*;

public class MetadataGetter {

  private  Statement statementSource;
  private ResultSet  resultSetTables;

  public MetadataGetter (){
  }

  public void getSchemaOfBddSource(Connection connectionSource, Bdd bdd){
    try {
      statementSource = connectionSource.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    } catch (SQLException e) {
      throw new DbException("Creation of statement source to get schema failed",e);
    }

    try {
      StringMakerAccordingToProvider stringMaker = new StringMakerAccordingToProvider();
      /* GET ALL TABLES WITH NB OF COLUMNS FRON SOURCE */

      resultSetTables = statementSource.executeQuery(stringMaker.getSqlRequest(connectionSource));
      //DatabaseMetaData metaData = connectionSource.getMetaData();
      //ResultSet resultSetTables = metaData.getTables(null, "public", "%", null);
      if(!resultSetTables.isBeforeFirst()){
        throw new DbException("*** ERROR : DATAS NOT FOUND IN DATABASE SOURCE ***", new Exception());
      } else{
        while (resultSetTables.next()){
          bdd.addTable(resultSetTables.getString(1));
        }
      }
      resultSetTables.close();

      /* GET ALL COLUMNS , TYPE , SIZE OF TYPE , IF CAN'T BE NULL FROM EACH TABLE ALREADY FOUND */
      for(int indexTable=0; indexTable<bdd.getBddTables().size();indexTable++){

        Table tableToModify = bdd.getBddTables().get(indexTable);

        ResultSet resultSet = statementSource.executeQuery("SELECT * FROM "+tableToModify.getTableName());
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int nbColumns = resultSetMetaData.getColumnCount();

        for(int indexColumn=1;indexColumn<=nbColumns;indexColumn++){
          String columnName = resultSetMetaData.getColumnName(indexColumn);
          String columnType = resultSetMetaData.getColumnTypeName(indexColumn);
          int sizeOfType = resultSetMetaData.getPrecision(indexColumn);
          // isNullable : columnNoNulls=0, columnNullable=1 or columnNullableUnknown=2
          int canBeNull = resultSetMetaData.isNullable(indexColumn);
          boolean isAutoIncrement = resultSetMetaData.isAutoIncrement(indexColumn);

          Column columnToAdd = tableToModify.addOneColumnToTable(columnName);
          columnToAdd.addCharacteristicOfColumn(columnType,sizeOfType,canBeNull);
          columnToAdd.setIsAutoIncrement(isAutoIncrement);
        }
        resultSet.close();
      }
    } catch (SQLException e) {
      throw new DbException("Problem to get schema from database source.",e);
    } finally {
      closeStatementAndResultSet();
    }
  }

  public void closeStatementAndResultSet(){
    try {
      if(!resultSetTables.isClosed()){
        resultSetTables.close();
      }
      statementSource.close();
    } catch (SQLException e) {
      throw new DbException("Closing of  statement source or resultset from metadata getter failed.",e);
    }
  }
}

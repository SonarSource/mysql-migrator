/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DataPutInBase {

  private LogDisplay logDisplay;
  private PreparedStatement statementDest;

  public DataPutInBase() throws IOException {
    logDisplay = new LogDisplay();
  }

  public void insertDatasFromJavaDatabaseToDestinationDatabase (Connection connectionDest,List<Table> listOfTables) throws SQLException {

    for(int indexTable=0;indexTable<listOfTables.size();indexTable++){

      /* GET ( TABLE, TABLENAME, ROW NB OF TABLE, COLUMNS AND NB OF COLUMNS ) FROM JAVA BDD OBJECT */
      Table table = listOfTables.get(indexTable);
      String tableName =  table.getTableName();
      int nbRowsInTable = table.getNbRows();
      List<Column> columns = table.getColumns();
      int nbColumns = columns.size();

      /* MAKE STRINGS TO PUT IN SQL INSERT REQUEST */
      ListColumnsAsString lcas = new ListColumnsAsString(columns);
      String columnsAsString = lcas.makeString();
      String questionMarkString = lcas.makeQuestionMarkString();
      String sqlRequest = "INSERT INTO "+tableName+" ("+columnsAsString+") VALUES("+questionMarkString+");";

      /* PREPARE STATEMENT BEFORE SENDING */
      statementDest = connectionDest.prepareStatement(sqlRequest);
      try {
        /* ADD DATA IN PREPARED STATEMENT FOR EACH COLUMN AND ROW BY ROW */
        for(int indexRow=0;indexRow<nbRowsInTable;indexRow++){
          for(int indexColumn=0;indexColumn<nbColumns;indexColumn++){
            Object objectToInsert = columns.get(indexColumn).getDataWithIndex(indexRow);
            statementDest.setObject(indexColumn+1,objectToInsert);
          }
          /* EXECUTE STATEMENT FOR EACH ROW */
          statementDest.executeUpdate();
        }
      } catch (SQLException e){
        throw new DbException("Problem when inserting datas in database destination.",e);
      }finally {
        closeStatementAndresultSet();
      }
      logDisplay.displayInformationLog("DATAS ADDED IN "+tableName+" TABLE");
    }
  }
  private void closeStatementAndresultSet(){
    try {
      statementDest.close();
    } catch (SQLException e){
      throw new DbException("Closing statement destination in DataPutInBase failed.",e);
    }
  }
}

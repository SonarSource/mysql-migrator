/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DataPutInBase {

  private Bdd bdd;
  private Connection connectionDest;
  private List<Table> listOfTables;

  public DataPutInBase(Connection connection, Bdd bdd){
    this.connectionDest = connection;
    this.bdd = bdd;
    this.listOfTables = this.bdd.getBddTables();
  }
  public void doInsertIntoTables ()throws SQLException{
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
      String sql = "INSERT INTO "+tableName+" ("+columnsAsString+") VALUES("+questionMarkString+");";

      /* PREPARE STATEMENT BEFORE SENDING */
      PreparedStatement statementDest = connectionDest.prepareStatement(sql);
      for(int indexRow=0;indexRow<nbRowsInTable;indexRow++){
        for(int indexColumn=0;indexColumn<nbColumns;indexColumn++){
          Object objectToInsert = columns.get(indexColumn).getDataWithIndex(indexRow);
          statementDest.setObject(indexColumn+1,objectToInsert);
        }
        /* EXECUTE STATEMENT FOR EACH ROW*/
        statementDest.executeUpdate();
      }
      statementDest.close();
    }
    connectionDest.close();
  }
}
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

  private Bdd Bdd;
  private Connection connectionDest;
  private List<Table> listOfTables;

  public DataPutInBase(Connection connection, Bdd Bdd){
      this.connectionDest = connection;
      this.Bdd = Bdd;
      this.listOfTables = this.Bdd.getBddTables();
  }
  public void doInsertIntoTables ()throws SQLException{
    for(int indexTable=0;indexTable<listOfTables.size();indexTable++){

      /* GET ( TABLE, TABLENAME, NB OF TABLE'S ROWS, COLUMNS AND NB OF COLUMS ) FROM JAVA Bdd OBJECT */
      Table table = listOfTables.get(indexTable);
      String tableName =  table.getTableName();
      System.out.println("Table "+tableName+" is copying...");
      int nbRowsInTable = table.getNbRows();
      List<Column> columns = table.getColumns();
      int nbColumns = columns.size();

      /* MAKE STRINGS TO PUT IN SQL INSERT REQUEST */
      ListColumnsAsString lcas = new ListColumnsAsString(columns);
      String columnsAsString = lcas.makeString();
      String questionMarkString = lcas.makeQuestionMarkString();
      String sql = "INSERT INTO "+tableName+" ("+columnsAsString+") VALUES("+questionMarkString+");";
      //sql must be : INSERT INTO tableName (column1,column2,...) VALUES (?,?,...);

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
      System.out.println("DONE");
    }
  }
}

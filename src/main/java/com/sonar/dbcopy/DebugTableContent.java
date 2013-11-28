/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class DebugTableContent {     /* THAT CLASS SHOULD BE DELETE AT THE END OF THE PROJECT  */

  private Bdd Bdd;

  public DebugTableContent(Bdd bdd){
    this.Bdd = bdd;
  }

  public void showTableColumnDatas(){
    for(int indexTable=0; indexTable< Bdd.getBddTables().size();indexTable++){
      System.out.println("*** TABLE : "+ Bdd.getBddTables().get(indexTable).getTableName());
      for(int indexColumn=0; indexColumn< Bdd.getBddTables().get(indexTable).getColumns().size();indexColumn++){
        System.out.println("****** COLUMN : "+ Bdd.getBddTables().get(indexTable).getColumns().get(indexColumn).getColumnName());
        for(int indexData=0;indexData< Bdd.getBddTables().get(indexTable).getColumns().get(indexColumn).getDataList().size();indexData++){
          System.out.print(Bdd.getBddTables().get(indexTable).getColumns().get(indexColumn).getDataList().get(indexData)+" : ");
          System.out.println();
        }
      }
    }
  }
  public void showColumnsAndNbRowsByTable(){
    for(int indexTable=0; indexTable< Bdd.getBddTables().size();indexTable++){
      System.out.println("*** TABLE : "+ Bdd.getBddTables().get(indexTable).getTableName()+"--- NB ROWS = "+ Bdd.getBddTables().get(indexTable).getNbRows());
      for(int indexColumn=0; indexColumn< Bdd.getBddTables().get(indexTable).getColumns().size();indexColumn++){
        System.out.println("****** COLUMN : "+ Bdd.getBddTables().get(indexTable).getColumns().get(indexColumn).getColumnName()+" -> TYPE = "+ Bdd.getBddTables().get(indexTable).getColumns().get(indexColumn).getColumnType());
      }
    }
  }
}

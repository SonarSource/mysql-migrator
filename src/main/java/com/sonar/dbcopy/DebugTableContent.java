/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class DebugTableContent {     /* THAT CLASS SHOULD BE DELETE AT THE END OF THE PROJECT  */

  private SonarBDD sonarBDD;

  public DebugTableContent(SonarBDD bdd){
    this.sonarBDD = bdd;
  }

  public void affichage(){
    for(int indexTable=0; indexTable<sonarBDD.getBDDTables().size();indexTable++){
      System.out.println("*** TABLE : "+sonarBDD.getBDDTables().get(indexTable).getTableName());
      for(int indexColumn=0; indexColumn<sonarBDD.getBDDTables().get(indexTable).getColumns().size();indexColumn++){
        System.out.println("****** COLUMN : "+sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getColumnName());
        for(int indexData=0;indexData<sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getDataList().size();indexData++){
          System.out.print(sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getDataList().get(indexData)+" : ");
          System.out.println();
        }
      }
    }
  }
  public void AfficheColumnsAndRows(){
    for(int indexTable=0; indexTable<sonarBDD.getBDDTables().size();indexTable++){
      System.out.println("*** TABLE : "+sonarBDD.getBDDTables().get(indexTable).getTableName()+"--- NB ROWS = "+sonarBDD.getBDDTables().get(indexTable).getNbRows());
      for(int indexColumn=0; indexColumn<sonarBDD.getBDDTables().get(indexTable).getColumns().size();indexColumn++){
        System.out.println("****** COLUMN : "+sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getColumnName()+" -> TYPE = "+sonarBDD.getBDDTables().get(indexTable).getColumns().get(indexColumn).getColumnType());
      }
    }
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MetadataGetter {

private List<Table> tableList;

  public MetadataGetter (){
    tableList =new ArrayList<Table>();
  }

  public void getSchemaOfBddSource(Connection connectionSource, Bdd bdd) throws SQLException {
    Statement statementSource = connectionSource.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

    ResultSet resultSetTables = statementSource.executeQuery("SELECT table_name,count(column_name) FROM information_schema.columns WHERE table_schema='public' GROUP BY table_name ORDER BY 1");

    while (resultSetTables.next()){
      Table tableToAdd = bdd.addTable(resultSetTables.getString(1));
    }
    //resultSetTables.close();

    for(int indexTable=0; indexTable<bdd.getBddTables().size();indexTable++){

        Table tableToModify = bdd.getBddTables().get(indexTable);
        ResultSet resultSetColumns = statementSource.executeQuery("SELECT column_name,data_type,character_maximum_length,is_nullable from information_schema.columns where table_name='"+tableToModify.getTableName()+"' ORDER BY ordinal_position");
        while (resultSetColumns.next()){
          Column columnToAdd = tableToModify.addOneColumnToTable(resultSetColumns.getString(1));
          columnToAdd.addCharacteristicOfColumn(resultSetColumns.getString(2),resultSetColumns.getInt(3),resultSetColumns.getString(4));
        }
        resultSetColumns.close();
      }
    statementSource.close();
  }

  public void addSchemaToBddDest(Connection connectionDest, Bdd bdd) throws SQLException {
    List<Table> tableList =  bdd.getBddTables();
    Statement statementDest = connectionDest.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    for(int indexTable=0;indexTable<tableList.size();indexTable++){
      Table tableToCreate = tableList.get(indexTable);
      String tableName = tableToCreate.getTableName();
      int nb = tableToCreate.getColumns().size();

      statementDest.execute("DROP TABLE IF EXISTS "+tableName);
      statementDest.execute("CREATE TABLE IF NOT EXISTS "+tableName+" ();");

      for (int indexColumn=0;indexColumn<tableToCreate.getColumns().size();indexColumn++){

        Column column = tableToCreate.getColumns().get(indexColumn);
        String colunmName = column.getColumnName();
        String columnType = column.getColumnType();
        int columnTypeSize = column.getColumnTypeSize();
        String canBeNull = column.getCanBeNull();

        String sqlAlterTableString = "ALTER TABLE "+tableName+
          " ADD COLUMN "+colunmName+" "+columnType;
        if (columnTypeSize!=0){
          sqlAlterTableString+=" ("+columnTypeSize+") ";
        }
        sqlAlterTableString+=" "+canBeNull+" ;";

        statementDest.executeUpdate(sqlAlterTableString);
      }
    }
    statementDest.close();
  }
}

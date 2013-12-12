/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.io.IOException;
import java.sql.SQLException;

public class BddDataReproducer {

  private DataGetter dataGetter;

  public BddDataReproducer(BddConnecter bddConnecter,Bdd bdd)throws IOException{

    /* GET DATAS FROM SOURCE */
    try {
      dataGetter = new DataGetter();
      dataGetter.createStatement(bddConnecter.getSourceConnection());
      dataGetter.writeDataInJavaBdd(bdd.getBddTables());
    } catch (SQLException e) {
      throw new DbException("Problem when getting datas from database source",e);
    } finally {
      dataGetter.closeSourceStatement();
    }

    /* DELETE CONTENT OF DESTINATION */
    try {
      DataDropper dataDropper =new DataDropper();
      dataDropper.deleteDatas(bddConnecter.getDestConnection(),bdd.getBddTables());
    } catch (SQLException e) {
      throw new DbException("Problem when deleting datas from database destination",e);
    } finally {
      bddConnecter.getSimpleSourceConnection().closeConnection();
    }

    /* ADD DATAS TO DESTINATION */
    try{
      DataPutInBase dataPutInBase = new DataPutInBase();
      dataPutInBase.insertDatasFromJavaDatabaseToDestinationDatabase(bddConnecter.getDestConnection(),bdd.getBddTables());
    } catch (SQLException e){
      throw new DbException("Problem when adding datas in database destination",e);
    } finally {
      bddConnecter.getSimpleDestConnection().closeConnection();
    }
  }
}

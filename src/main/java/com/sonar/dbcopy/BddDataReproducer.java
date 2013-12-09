/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.SQLException;

public class BddDataReproducer {

  private DataGetter dataGetter;
  private DataPutInBase dataPutInBase;

  public BddDataReproducer(BddConnecter bddConnecter,Bdd bdd) throws SQLException {
      dataGetter = new DataGetter();
      dataGetter.createStatement(bddConnecter.getSourceConnection());
      dataGetter.writeDataInJavaBdd(bdd.getBddTables());

      dataPutInBase = new DataPutInBase();
      dataPutInBase.insertDatasFromJavaDatabaseToDestinationDatabase(bddConnecter.getDestConnection(),bdd.getBddTables());
  }
  /* GETTERS */
  public DataGetter getDataGetter(){
    return this.dataGetter;
  }
  public DataPutInBase getDataPutInBase(){
    return this.dataPutInBase;
  }
}

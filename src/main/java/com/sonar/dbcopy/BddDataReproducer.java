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
  private Bdd Bdd;

  public BddDataReproducer(BddConnecter bddConnecter,Bdd Bdd) throws SQLException {
    this.Bdd = Bdd;
    try{
      dataGetter = new DataGetter(bddConnecter.getStatementSource(), this.Bdd);
      dataGetter.doRequest();

      dataPutInBase = new DataPutInBase(bddConnecter.getConnectionDest(), this.Bdd);
      dataPutInBase.doInsertIntoTables();
    }
    catch (Exception e){e.getStackTrace();}
  }
  /* GETTERS */
  public DataGetter getDataGetter(){
    return this.dataGetter;
  }
  public DataPutInBase getDataPutInBase(){
    return this.dataPutInBase;
  }
}

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
  private Bdd bdd;

  public BddDataReproducer(BddConnecter bddConnecter,Bdd Bdd) throws SQLException {
    this.bdd = Bdd;
    dataGetter = new DataGetter(bddConnecter.getStatementSource(), this.bdd);
    dataGetter.doRequest();

    dataPutInBase = new DataPutInBase(bddConnecter.getConnectionDest(), this.bdd);
    dataPutInBase.doInsertIntoTables();
  }
  /* GETTERS */
  public DataGetter getDataGetter(){
    return this.dataGetter;
  }
  public DataPutInBase getDataPutInBase(){
    return this.dataPutInBase;
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.sql.SQLException;

public class BddDataReproducer {

  protected DataGetter dataGetter;
  protected DataPutInBase dataPutInBase;

  public BddDataReproducer(BddConnecter bddConnecter,BddBuider bddBuider) throws SQLException {
    try{
      dataGetter = new DataGetter(bddConnecter.getStatementSource(), bddBuider.sonarBDD);
      dataGetter.doRequest();

      dataPutInBase = new DataPutInBase(bddConnecter.getConnectionDest(), bddBuider.sonarBDD);
      dataPutInBase.doInsertIntoTables();
    }
    catch (Exception e){e.getStackTrace();}
  }
}

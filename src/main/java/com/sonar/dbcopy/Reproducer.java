/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.io.IOException;
import java.sql.SQLException;

public class Reproducer {

  private DataGetter dataGetter;

  public Reproducer(Connecter connecter, Database database) throws IOException {

    /* GET DATAS FROM SOURCE */
    try {
        dataGetter = new DataGetter();
        dataGetter.createStatement(connecter.getConnectionSource());
        dataGetter.recordDatas(database);

      /* DELETE CONTENT OF DESTINATION */
        DataDropper dataDropper = new DataDropper();
        dataDropper.deleteDatas(connecter.getConnectionDest(), database);


      /* ADD DATAS TO DESTINATION */
        DataPutInBase dataPutInBase = new DataPutInBase();
        dataPutInBase.insertDatasToDestination(connecter.getConnectionDest(), database);

      /* VERIFY IF DATAS ARE THE SAME */
      // TODO
      //DataVerifier dataVerifier = new DataVerifier();
      //dataVerifier.doVerification(connecter,database);

    } catch (SQLException e) {
      throw new DbException("Problem when adding datas in database destination", e);
    } finally {
      dataGetter.closeSourceStatement();
      connecter.closeSource();
      connecter.closeDestination();
    }
  }
}

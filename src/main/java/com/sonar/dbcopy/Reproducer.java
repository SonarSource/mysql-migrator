/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reproducer {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private DataReproducer dataReproducer;


  public Reproducer(Connecter connecter, Database database) throws IOException {
    try {
    /* DELETE CONTENT OF DESTINATION */
      DataDropper dataDropper = new DataDropper();
      dataDropper.deleteDatas(connecter.getConnectionDest(), database);
    /* COPY DATAS */
      dataReproducer = new DataReproducer();
      dataReproducer.doCopy(connecter.getConnectionSource(), connecter.getConnectionDest(), database);
    /* VERIFY IF DATAS ARE THE SAME */
      // TODO
      //DataVerifier dataVerifier = new DataVerifier();
      //dataVerifier.doVerification(connecter,database);

    } catch (SQLException e) {
      throw new DbException("Problem when adding datas in database destination", e);
    } finally {
      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - -  | ");
      dataReproducer.closeResultSet();
      LOGGER.log(Level.INFO, " | ResultSet from source has been closed.           | ");
      dataReproducer.closeSourceStatement();
      LOGGER.log(Level.INFO, " | Statement from source has been closed.           | ");
      dataReproducer.closeDestPrepareStatement();
      LOGGER.log(Level.INFO, " | PreStatement from destination has been closed.   | ");
      connecter.closeSource();
      LOGGER.log(Level.INFO, " | Connection from source has been closed.          | ");
      connecter.closeDestination();
      LOGGER.log(Level.INFO, " | Connection from destination has been closed.     | ");
      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - -  | ");


    }
  }
}

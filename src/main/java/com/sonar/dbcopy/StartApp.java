/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import com.sonar.dbcopy.prepare.Arguments;
import com.sonar.dbcopy.prepare.ConnectionVerifier;
import com.sonar.dbcopy.prepare.MetadataGetter;
import com.sonar.dbcopy.prepare.VersionVerifier;
import com.sonar.dbcopy.prepare.Deleter;
import com.sonar.dbcopy.reproduce.process.LoopByTable;
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.DatabaseComparer;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import org.slf4j.LoggerFactory;

public class StartApp {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  protected StartApp() {
    // empty because only use the static method main
  }

  public static void main(String[] args) {
    String starLine = "****************";
    String[] tablesToCopy;
    Arguments arguments = new Arguments();
    arguments.doParsing(args);
    boolean argumentsAreFilled = arguments.allRequiredOptionsAreFilled();

    if (arguments.commandLineIsHelp()) {
      arguments.getHelp();
    } else if (argumentsAreFilled) {

      Database databaseSource = new Database();
      Database databaseDest = new Database();

      ConnecterData connecterDataSource = new ConnecterData(
        arguments.getOptionContent(Arguments.OptionNames.driverSrc),
        arguments.getOptionContent(Arguments.OptionNames.urlSrc),
        arguments.getOptionContent(Arguments.OptionNames.userSrc),
        arguments.getOptionContent(Arguments.OptionNames.pwdSrc)
      );
      ConnecterData connecterDataDest = new ConnecterData(
        arguments.getOptionContent(Arguments.OptionNames.driverDest),
        arguments.getOptionContent(Arguments.OptionNames.urlDest),
        arguments.getOptionContent(Arguments.OptionNames.userDest),
        arguments.getOptionContent(Arguments.OptionNames.pwdDest)
      );


    /* VERIFY CONNECTION */
      ConnectionVerifier connectionVerifier = new ConnectionVerifier();
      connectionVerifier.databaseIsReached(connecterDataSource);
      LOGGER.info(starLine + " CONFIGURATION VERIFICATIONS " + starLine);
      LOGGER.info("Database SOURCE  has been reached at :         " + connecterDataSource.getUrl());
      connectionVerifier.databaseIsReached(connecterDataDest);
      LOGGER.info("Database DESTINATION has been reached at :     " + connecterDataDest.getUrl());

    /* VERIFY VERSIONS OF SONARQUBE */
      VersionVerifier vvSource = new VersionVerifier();
      int maxVersionIdSource = vvSource.lastVersionId(connecterDataSource);
      VersionVerifier vvDest = new VersionVerifier();
      int maxVersionIdDestination = vvDest.lastVersionId(connecterDataDest);
      if (maxVersionIdSource != maxVersionIdDestination && maxVersionIdDestination != 0 && maxVersionIdSource != 0) {
        throw new MessageException("Version of schema migration are not the same between source (" + maxVersionIdSource + ") and destination (" + maxVersionIdDestination + ").");
      } else if (maxVersionIdDestination == 0) {
        LOGGER.warn("The versions of SonarQube schema migration source is (" + maxVersionIdSource + ") when destination is (" + maxVersionIdDestination + ").");
      } else {
        LOGGER.info("The versions of SonarQube schema migration are the same between source (" + maxVersionIdSource + ") and destination (" + maxVersionIdDestination + ").");
      }

    /* GET METADATA FROM SOURCE AND FROM DESTINATION */
      LOGGER.info(starLine + " SEARCH TABLES " + starLine);
      tablesToCopy = arguments.getTablesToCopy();

      LOGGER.info("START GETTING METADATA IN SOURCE...");
      MetadataGetter metadataGetterSource = new MetadataGetter(connecterDataSource, databaseSource);
      metadataGetterSource.execute(tablesToCopy);
      LOGGER.info("   " + databaseSource.getNbTables() + " TABLES GETTED.");
      LOGGER.info("START GETTING METADATA IN DESTINATION...");
      MetadataGetter metadataGetterDest = new MetadataGetter(connecterDataDest, databaseDest);
      metadataGetterDest.execute(tablesToCopy);
      LOGGER.info("   " + databaseDest.getNbTables() + " TABLES GETTED.");


    /* DISPLAY TABLES FOUND */
      LOGGER.info(starLine + " FOUND TABLES " + starLine);
      DatabaseComparer dbComparer = new DatabaseComparer();
      dbComparer.displayAllTablesFoundIfExists(databaseSource, databaseDest);


    /* DELETE TABLE CONTENT OF DATABASE DESTINATION */
      LOGGER.info(starLine + " DELETE TABLES FROM DESTINATION " + starLine);
      Deleter deleter = new Deleter(connecterDataDest, databaseSource);
      deleter.execute(databaseDest);

    /* COPY DATA FROM SOURCE TO DESTINATION */
      LOGGER.info(starLine + " COPY DATA " + starLine);
      LoopByTable loopByTable = new LoopByTable(connecterDataSource, connecterDataDest, databaseSource, databaseDest);
      loopByTable.execute();

    /* FIND AND DISPLAY TABLES PRESENT IN DESTINATION BUT NOT IN SOURCE */
      LOGGER.info(starLine + " SEARCH FOR MISTAKES " + starLine);
      dbComparer.displayMissingTableInDb(databaseSource, databaseDest, "DESTINATION");
      dbComparer.displayMissingTableInDb(databaseDest, databaseSource, "SOURCE");
      dbComparer.displayDiffNumberRows(databaseSource, databaseDest);

      LOGGER.info(starLine + starLine + starLine);
      LOGGER.info("** THE COPY HAS FINISHED SUCCESSFULLY **");
      LOGGER.info(starLine + starLine + starLine);
    } else {
      throw new MessageException("Some required parameters are missing. Type '-help' to know which parameters are required.\n"+arguments.giveArgumentsDebugString());
    }
  }
}


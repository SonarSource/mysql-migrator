/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2013-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

  protected StartApp() {
    // empty because only use the static method main
  }

  public static void main(String[] args) {
    String starLine = "****************";
    String[] tablesToCopy;
    Arguments arguments = new Arguments();
    arguments.doParsing(args);
    arguments.processOptions();
    boolean argumentsAreFilled = arguments.allRequiredOptionsAreFilled();

    if (arguments.commandLineIsHelp()) {
      arguments.printHelpStringAndExit();
    } else if (arguments.commandLineIsVersion()) {
      arguments.printVersionString();
      arguments.printJVMVersion();
      arguments.printOSVersion();
    } else if (argumentsAreFilled) {

      Database databaseSource = new Database();
      Database databaseDest = new Database();

      ConnecterData connecterDataSource = new ConnecterData(
              arguments.getOptionContent(Arguments.OptionNames.DRIVER_SRC),
              arguments.getOptionContent(Arguments.OptionNames.URL_SRC),
              arguments.getOptionContent(Arguments.OptionNames.USER_SRC),
              arguments.getOptionContent(Arguments.OptionNames.PWD_SRC)
      );
      ConnecterData connecterDataDest = new ConnecterData(
              arguments.getOptionContent(Arguments.OptionNames.DRIVER_DEST),
              arguments.getOptionContent(Arguments.OptionNames.URL_DEST),
              arguments.getOptionContent(Arguments.OptionNames.USER_DEST),
              arguments.getOptionContent(Arguments.OptionNames.PWD_DEST)
      );

      int commitSize;
      try {
        commitSize = Integer.parseInt(arguments.getOptionContent(Arguments.OptionNames.COMMIT_SIZE));
      } catch(IllegalArgumentException notAnInteger) {
        throw new MessageException("COMMIT_SIZE must be a valid integer");
      }

    /* VERIFY CONNECTION */
      ConnectionVerifier connectionVerifier = new ConnectionVerifier();
      connectionVerifier.databaseIsReached(connecterDataSource);
      LOGGER.info("{} CONFIGURATION VERIFICATIONS {}", starLine, starLine);
      LOGGER.info("Database SOURCE has been reached at :          {}", connecterDataSource.getUrl());
      connectionVerifier.databaseIsReached(connecterDataDest);
      LOGGER.info("Database DESTINATION has been reached at :     {}", connecterDataDest.getUrl());

    /* VERIFY VERSIONS OF SONARQUBE */
      VersionVerifier vvSource = new VersionVerifier();
      int maxVersionIdSource = vvSource.lastVersionId(connecterDataSource);
      VersionVerifier vvDest = new VersionVerifier();
      int maxVersionIdDestination = vvDest.lastVersionId(connecterDataDest);
      if (maxVersionIdSource != maxVersionIdDestination && maxVersionIdDestination != 0 && maxVersionIdSource != 0) {
        throw new MessageException("Version of schema migration are not the same between source (" + maxVersionIdSource + ") and destination (" + maxVersionIdDestination + ").");
      } else if (maxVersionIdDestination == 0) {
        LOGGER.warn("The versions of SonarQube schema migration source is ({}) when destination is ({}).", maxVersionIdSource, maxVersionIdDestination);
      } else {
        LOGGER.info("The versions of SonarQube schema migration are the same between source ({}) and destination ({}).", maxVersionIdSource, maxVersionIdDestination);
      }

    /* GET METADATA FROM SOURCE AND FROM DESTINATION */
      LOGGER.info("{} SEARCH TABLES {}", starLine, starLine);
      tablesToCopy = arguments.getTablesToCopy();

      LOGGER.info("START GETTING METADATA IN SOURCE...");
      MetadataGetter metadataGetterSource = new MetadataGetter(connecterDataSource, databaseSource);
      metadataGetterSource.execute(tablesToCopy);
      LOGGER.info("   {} TABLES GETTED.", databaseSource.getNbTables());
      LOGGER.info("START GETTING METADATA IN DESTINATION...");
      MetadataGetter metadataGetterDest = new MetadataGetter(connecterDataDest, databaseDest);
      metadataGetterDest.execute(tablesToCopy);
      LOGGER.info("   {} TABLES GETTED.", databaseDest.getNbTables());


    /* DISPLAY TABLES FOUND */
      LOGGER.info("{} FOUND TABLES {}", starLine, starLine);
      DatabaseComparer dbComparer = new DatabaseComparer();
      dbComparer.displayAllTablesFoundIfExists(databaseSource, databaseDest);


    /* DELETE TABLE CONTENT OF DATABASE DESTINATION */
      LOGGER.info("{} DELETE TABLES FROM DESTINATION {}", starLine, starLine);
      Deleter deleter = new Deleter(connecterDataDest, databaseSource);
      deleter.execute(databaseDest);

    /* COPY DATA FROM SOURCE TO DESTINATION */
      LOGGER.info("{} COPY DATA {}", starLine, starLine);
      LoopByTable loopByTable = new LoopByTable(connecterDataSource, connecterDataDest, databaseSource, databaseDest,
        commitSize);
      loopByTable.execute();

    /* FIND AND DISPLAY TABLES PRESENT IN DESTINATION BUT NOT IN SOURCE */
      LOGGER.info("{} SEARCH FOR MISTAKES {}", starLine, starLine);
      dbComparer.displayMissingTableInDb(databaseSource, databaseDest, "DESTINATION");
      dbComparer.displayMissingTableInDb(databaseDest, databaseSource, "SOURCE");
      dbComparer.displayDiffNumberRows(databaseSource, databaseDest);

      LOGGER.info("{}{}{}", starLine, starLine, starLine);
      LOGGER.info("** THE COPY HAS FINISHED SUCCESSFULLY **");
      LOGGER.info("{}{}{}", starLine, starLine, starLine);
    } else {
      throw new MessageException("Some required parameters are missing. Type '-help' to know which parameters are required.\n" + arguments.giveArgumentsDebugString());
    }
  }
}


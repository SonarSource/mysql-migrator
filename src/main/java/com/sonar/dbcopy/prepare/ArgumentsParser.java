/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;

public class ArgumentsParser {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private CommandLine commandLine;
  private Options options;
  private String driverSrc, driverDest, urlSrc, urlDest, userSrc, userDest, pwdSrc, pwdDest;
  private String[] tablesToCopy;

  public ArgumentsParser() {
    options = new Options();

    Option help = new Option("help", false, "           Print this message");
    options.addOption(help);

    String[] optionNames = {"urlSrc", "driverSrc", "userSrc", "pwdSrc", "driverDest", "urlDest", "userDest", "pwdDest"};
    String[] optionDescription= {
      "REQUIRED:  url for database source",
      "OPTIONAL:  driver for database source",
      "REQUIRED:  user name for database source",
      "REQUIRED:  password for database source",

      "OPTIONAL:  driver for database destination",
      "REQUIRED:  url for database destination",
      "REQUIRED:  user name for database destination",
      "REQUIRED:  password for database destination"
    };
    String[] optionArgument = {
      "url",
      "jdbc driver",
      "login",
      "password",

      "jdbc driver",
      "url",
      "login",
      "password"
    };

    for (int indexForString = 0; indexForString < optionNames.length; indexForString++) {
      Option option = OptionBuilder
        .withValueSeparator(' ')
        .withDescription(optionDescription[indexForString])
        .create(optionNames[indexForString]);

      if (!"driver".equals(optionNames[indexForString].substring(0, 6))) {
        option.isRequired();
      }
      option.setArgs(1);
      option.setArgName(optionArgument[indexForString]);
      options.addOption(option);
    }

    Option option = OptionBuilder
      .withValueSeparator(' ')
      .withValueSeparator(',')
      .withDescription("OPTIONAL:  table to copy separated by space or , (others will not be deleted)")
      .create("T");
    option.setArgs(100);
    option.setArgName("table1Name,table2Name ...");
    options.addOption(option);
  }

  public void doParsing(String[] args) {
    CommandLineParser commandLineParser = new GnuParser();
    CharacteristicsRelatedToEditor chRelToEd = new CharacteristicsRelatedToEditor();

    try {
      commandLine = commandLineParser.parse(options, args);

      if (!commandLine.hasOption("help")) {
        // SOME OPTIONS ARE REQUIRED , NO NEED "IF" CONDITION
        urlSrc = commandLine.getOptionValue("urlSrc");
        urlDest = commandLine.getOptionValue("urlDest");

        userSrc = commandLine.getOptionValue("userSrc");
        userDest = commandLine.getOptionValue("userDest");

        pwdSrc = commandLine.getOptionValue("pwdSrc");
        pwdDest = commandLine.getOptionValue("pwdDest");

        // GET OPTION driverSrc if EXISTS
        if (commandLine.hasOption("driverSrc")) {
          driverSrc = commandLine.getOptionValue("driverSrc");
        } else {
          // IF NO OPTION DRIVER , DEDUCE IT FROM URL
          driverSrc = chRelToEd.giveDriverWithUrlFromUser(urlSrc);
        }

        // GET OPTION driverDest IF EXISTS
        if (commandLine.hasOption("driverDest")) {
          driverDest = commandLine.getOptionValue("driverDest");
        } else {
          // IF NO OPTION DRIVER , DEDUCE IT FROM URL
          driverDest = chRelToEd.giveDriverWithUrlFromUser(urlDest);
        }

        // GET OPTION -T  IF EXISTS
        if (commandLine.getOptionValues("T") != null && commandLine.getOptionValues("T").length != 0) {
          int nbTablesrequired = commandLine.getOptionValues("T").length;
          tablesToCopy = new String[nbTablesrequired];
          for (int i = 0; i < nbTablesrequired; i++) {
            tablesToCopy[i] = commandLine.getOptionValues("T")[i];
          }
        }
      }


    } catch (ParseException e) {
      LOGGER.error(e.getMessage());
    }
  }

  public String[] getTablesToCopy() {
    return tablesToCopy;
  }

  public String getDriverSrc() {
    return driverSrc;
  }

  public String getDriverDest() {
    return driverDest;
  }

  public String getUrlSrc() {
    return urlSrc;
  }

  public String getUrlDest() {
    return urlDest;
  }

  public String getUserSrc() {
    return userSrc;
  }

  public String getUserDest() {
    return userDest;
  }

  public String getPwdSrc() {
    return pwdSrc;
  }

  public String getPwdDest() {
    return pwdDest;
  }

  public void getHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("help", options);
  }

  public boolean commandLineIsHelp() {
    return commandLine.hasOption("help");
  }
}

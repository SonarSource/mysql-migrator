/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.prepare;

import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;

public class ArgumentsParser {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private CommandLine commandLine;
  private Options options;

  public ArgumentsParser() {
    options = new Options();

    Option help = new Option("help", false, "Print this message");
    options.addOption(help);

    String[] optionNames = {"driverSrc", "urlSrc", "userSrc", "pwdSrc", "driverDest", "urlDest", "userDest", "pwdDest"};
    String[] optionDescription = {
      "The driver source is required like  org.postgresql.Driver for example.",
      "The url source is required like  jdbc:postgresql://localhost:15432/sonar for example.",
      "The user name recorded in database source is required.",
      "The password recorded in database source for the user name used in userSrc option is required.",

      "The driver destination is required like  com.mysql.jdbc.Driver for example.",
      "The url destination is required like   jdbc:mysql://localhost:13306/sonar?autoReconnect=true for example.",
      "The user name recorded in database destination is required.",
      "The password recorded in database destination for the user name used in userDest option is required."
    };

    for (int indexForString = 0; indexForString < optionNames.length; indexForString++) {
      Option option = OptionBuilder
        .hasArg()
        .isRequired()
        .withValueSeparator(' ')
        .withDescription(optionDescription[indexForString])
        .create(optionNames[indexForString]);
      options.addOption(option);
    }

    Option option = OptionBuilder
      .hasArgs()
      .withValueSeparator(' ')
      .withDescription("Use values to copy only tables mentionned after command line option -T.")
      .create("T");
    options.addOption(option);
  }

  public void doParsing(String[] args) {
    CommandLineParser commandLineParser = new GnuParser();
    try {
      commandLine = commandLineParser.parse(options, args);
    } catch (ParseException e) {
      LOGGER.error(e.getMessage());
      getHelp();
    }
  }

  public String[] getOptionTables() {
    return commandLine.getOptionValues("T");
  }

  public String getOptionValue(String optionName) {
    return commandLine.getOptionValue(optionName);
  }

  public void getHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("ant", options);
  }

  public CommandLine getCommandLine() {
    return commandLine;
  }
}

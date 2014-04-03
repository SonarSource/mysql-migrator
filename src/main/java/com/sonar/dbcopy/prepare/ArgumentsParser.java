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
      "OPTIONNAL: driver source",
      "REQUIRED:  url source",
      "REQUIRED:  user name source",
      "REQUIRED:  password source",

      "OPTIONNAL: driver destination",
      "REQUIRED:  url destination",
      "REQUIRED:  user name destination",
      "REQUIRED:  password destination"
    };

    for (int indexForString = 0; indexForString < optionNames.length; indexForString++) {
      Option option = OptionBuilder
        .hasArg()
        .withValueSeparator(' ')
        .withDescription(optionDescription[indexForString])
        .create(optionNames[indexForString]);
      if (!"driver".equals(optionNames[indexForString].substring(0, 6))) {
        option.isRequired();
      }
      options.addOption(option);
    }

    Option option = OptionBuilder
      .hasArgs()
      .withValueSeparator(' ')
      .withValueSeparator(',')
      .withDescription("OPTIONAL: table names to copy")
      .create("T");
    options.addOption(option);
  }

  public void doParsing(String[] args) {
    CommandLineParser commandLineParser = new GnuParser();
    try {
      commandLine = commandLineParser.parse(options, args);
    } catch (ParseException e) {
      LOGGER.error(" ** ERROR ** " + e.getMessage());
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
    formatter.printHelp("help", options);
  }

  public CommandLine getCommandLine() {
    return commandLine;
  }
}

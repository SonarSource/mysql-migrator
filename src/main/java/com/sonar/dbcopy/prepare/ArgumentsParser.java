/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ArgumentsParser {

  private CommandLine commandLine;
  private Options options;
  private static String[] OPTION_NAMES = {"urlSrc", "driverSrc", "userSrc", "pwdSrc", "urlDest", "driverDest", "userDest", "pwdDest"};

  private Map<String, String> optionContent, description, argumentHelpName;
  private String[] tablesToCopy;

  public ArgumentsParser() {
    options = new Options();

    Option help = new Option("help", false, "           Print this message");
    options.addOption(help);

    this.buildHashMap();

    for (int indexForString = 0; indexForString < OPTION_NAMES.length; indexForString++) {
      Option option = OptionBuilder
        .withValueSeparator(' ')
        .withDescription(description.get(OPTION_NAMES[indexForString]))
        .create(OPTION_NAMES[indexForString]);

      option.setArgs(1);
      option.setArgName(argumentHelpName.get(OPTION_NAMES[indexForString]));
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
        for (int indexArg = 0; indexArg < OPTION_NAMES.length; indexArg++) {
          if ("driverSrc".equals(OPTION_NAMES[indexArg]) && !commandLine.hasOption("driverSrc") && commandLine.hasOption("urlSrc")) {
            optionContent.put(OPTION_NAMES[indexArg], chRelToEd.giveDriverWithUrlFromUser(commandLine.getOptionValue("urlSrc")));

          } else if ("driverDest".equals(OPTION_NAMES[indexArg]) && !commandLine.hasOption("driverDest") && commandLine.hasOption("urlDest")) {
            optionContent.put(OPTION_NAMES[indexArg], chRelToEd.giveDriverWithUrlFromUser(commandLine.getOptionValue("urlDest")));

          } else {
            optionContent.put(OPTION_NAMES[indexArg], commandLine.getOptionValue(OPTION_NAMES[indexArg]));
          }
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
      throw new MessageException(e.getMessage());
    }
  }

  public String getOptionContent(String optionName) {
    return optionContent.get(optionName);
  }

  public String[] getTablesToCopy() {
    return tablesToCopy;
  }

  public void getHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("help", options);
  }

  public boolean commandLineIsHelp() {
    return commandLine.hasOption("help");
  }

  public boolean allRequiredOptionsAreFilled() {
    boolean optionsAreFilled = true;
    for (int i = 0; i < OPTION_NAMES.length; i++) {
      if (optionContent.get(OPTION_NAMES[i]) == null) {
        optionsAreFilled = false;
      }
    }
    return optionsAreFilled;
  }

  public String giveArgumentsDebugString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < OPTION_NAMES.length; i++) {
      stringBuilder.append(OPTION_NAMES[i] + " : " + optionContent.get(OPTION_NAMES[i]) + "\n");
    }
    return stringBuilder.toString();
  }

  public void buildHashMap() {
    optionContent = new HashMap<String, String>(8);
    description = new HashMap<String, String>(8);
    argumentHelpName = new HashMap<String, String>(8);

    optionContent.put(OPTION_NAMES[0], null);
    optionContent.put(OPTION_NAMES[1], null);
    optionContent.put(OPTION_NAMES[2], null);
    optionContent.put(OPTION_NAMES[3], null);
    optionContent.put(OPTION_NAMES[4], null);
    optionContent.put(OPTION_NAMES[5], null);
    optionContent.put(OPTION_NAMES[6], null);
    optionContent.put(OPTION_NAMES[7], null);

    description.put(OPTION_NAMES[0], "OPTIONAL:  driver for database source");
    description.put(OPTION_NAMES[1], "REQUIRED:  url for database source");
    description.put(OPTION_NAMES[2], "REQUIRED:  user name for database source");
    description.put(OPTION_NAMES[3], "REQUIRED:  password for database source");
    description.put(OPTION_NAMES[4], "REQUIRED:  url for database destination");
    description.put(OPTION_NAMES[5], "OPTIONAL:  driver for database destination");
    description.put(OPTION_NAMES[6], "REQUIRED:  user name for database destination");
    description.put(OPTION_NAMES[7], "REQUIRED:  password for database destination");

    argumentHelpName.put(OPTION_NAMES[0], "url");
    argumentHelpName.put(OPTION_NAMES[1], "jdbc driver");
    argumentHelpName.put(OPTION_NAMES[2], "login");
    argumentHelpName.put(OPTION_NAMES[3], "password");
    argumentHelpName.put(OPTION_NAMES[4], "url");
    argumentHelpName.put(OPTION_NAMES[5], "jdbc driver");
    argumentHelpName.put(OPTION_NAMES[6], "login");
    argumentHelpName.put(OPTION_NAMES[7], "password");

  }
}

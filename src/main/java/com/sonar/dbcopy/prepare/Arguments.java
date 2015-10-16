/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import java.util.HashMap;
import java.util.Map;

public class Arguments {
  public enum OptionNames {

    driverSrc("driverSrc", "OPTIONAL:  driver for database source", "jdbc driver"),
    urlSrc("urlSrc", "REQUIRED:  url for database source", "url"),
    userSrc("userSrc", "REQUIRED:  user name for database source", "login"),
    pwdSrc("pwdSrc", "REQUIRED:  password for database source", "password"),
    urlDest("urlDest", "REQUIRED:  url for database destination", "url"),
    driverDest("driverDest", "OPTIONAL:  driver for database destination", "jdbc driver"),
    userDest("userDest", "REQUIRED:  user name for database destination", "login"),
    pwdDest("pwdDest", "REQUIRED:  password for database destination", "password");
    // note that the help option is processed apart

    private String name;
    private String description;
    private String helperString;

    OptionNames(String name, String description, String helperString) {
      this.name = name;
      this.description = description;
      this.helperString = helperString;
    }

    @Override
    public String toString() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public String getHelperString() {
      return helperString;
    }

  }

  private CommandLine commandLine;
  private Options options;
  private Map<String, String> optionContent;
  private String[] tablesToCopy;

  public Arguments() {
    options = new Options();

    Option help = new Option("help", false, "           Print this message");
    options.addOption(help);

    this.allocatedEmptyOptionContent();

    for (OptionNames oneOption : OptionNames.values()) {
      OptionBuilder.withValueSeparator(' ');
      OptionBuilder.withDescription(oneOption.getDescription());
      Option option = OptionBuilder.create(oneOption.toString());

      option.setArgs(1);
      option.setArgName(oneOption.getHelperString());
      options.addOption(option);
    }

    OptionBuilder.withValueSeparator(' ');
    OptionBuilder.withValueSeparator(',');
    OptionBuilder.withDescription("OPTIONAL:  table to copy separated by space or , (others will not be deleted)");
    Option option = OptionBuilder.create("T");
    option.setArgs(Integer.MAX_VALUE);
    option.setArgName("table1Name,table2Name ...");
    options.addOption(option);
  }

  public void doParsing(String[] args) {
    CommandLineParser commandLineParser = new GnuParser();
    CharacteristicsRelatedToEditor chRelToEd = new CharacteristicsRelatedToEditor();

    try {
      commandLine = commandLineParser.parse(options, args);
    } catch (ParseException e) {
      throw new MessageException(e.getMessage());
    }

    if (!commandLine.hasOption("help")) {
      //copy content for all the options
      for (OptionNames oneOption : OptionNames.values()) {
        optionContent.put(oneOption.toString(), commandLine.getOptionValue(oneOption.toString()));
      }
      // process driver option if it is given in URL instead.
      if (!commandLine.hasOption("driverSrc") && commandLine.hasOption("urlSrc")) {
        optionContent.put(OptionNames.driverSrc.toString(), chRelToEd.giveDriverWithUrlFromUser(commandLine.getOptionValue("urlSrc")));
      }
      if (!commandLine.hasOption("driverDest") && commandLine.hasOption("urlDest")) {
        optionContent.put(OptionNames.driverDest.toString(), chRelToEd.giveDriverWithUrlFromUser(commandLine.getOptionValue("urlDest")));
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
  }

  public String getOptionContent(OptionNames optionName) {
    return optionContent.get(optionName.toString());
  }

  public String[] getTablesToCopy() {
    return tablesToCopy;
  }

  public void getHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setOptionComparator(null);
    formatter.printHelp("help", options);
  }

  public boolean commandLineIsHelp() {
    return commandLine.hasOption("help");
  }

  public boolean allRequiredOptionsAreFilled() {
    for (OptionNames oneOption : OptionNames.values()) {
      if (optionContent.get(oneOption.toString()) == null) {
        return false;
      }
    }
    return true;
  }

  public String giveArgumentsDebugString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (OptionNames oneOption : OptionNames.values()) {
      stringBuilder.append(oneOption.toString() + " : " + optionContent.get(oneOption.toString()) + "\n");
    }
    return stringBuilder.toString();
  }

  public void allocatedEmptyOptionContent() {

    optionContent = new HashMap<String, String>(OptionNames.values().length);
    for (OptionNames oneOption : OptionNames.values()) {
      optionContent.put(oneOption.toString(), null);
    }
  }
}

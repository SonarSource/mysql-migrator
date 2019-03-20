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
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.toolconfig.MessageException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import javax.annotation.Nullable;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor.detectDriverNameFromJdbcUrl;

public class Arguments {

  private static final int DEFAULT_COMMIT_SIZE = 5000;
  private static final Set<String> SUPPORTED_DBS = new LinkedHashSet<>(Arrays.asList("postgresql", "oracle", "sqlserver"));

  private final Options options;
  private final Map<String, String> rawOptionValues = new HashMap<>();
  private final List<String> errors = new ArrayList<>();
  private CommandLine commandLine;

  @Nullable
  private Integer commitSize;
  
  static final String SOURCE_URL_OPTION = "urlSrc"; 
  static final String SOURCE_USER_OPTION = "userSrc"; 
  static final String SOURCE_PASSWORD_OPTION = "pwdSrc"; 
  static final String SOURCE_DRIVER_OPTION = "driverSrc"; 
  static final String DEST_URL_OPTION = "urlDest";
  static final String DEST_USER_OPTION = "userDest";
  static final String DEST_PASSWORD_OPTION = "pwdDest";
  static final String DEST_DRIVER_OPTION = "driverDest";
  static final String COMMIT_SIZE_OPTION = "commitSize"; 
  static final String HELP_OPTION = "help"; 
  static final String VERSION_OPTION = "version"; 

  public enum OptionDescriptor {
    URL_SRC(SOURCE_URL_OPTION, "url for MySQL database source, for example: jdbc:mysql://localhost:3306/sonar", "jdbc url"),
    USER_SRC(SOURCE_USER_OPTION, "user name for database source", "login"),
    PWD_SRC(SOURCE_PASSWORD_OPTION, "password for database source", "password"),
    DRIVER_SRC(SOURCE_DRIVER_OPTION, "driver for database source", "jdbc driver", false),
    URL_DEST(DEST_URL_OPTION, "url for database destination", "jdbc url"),
    USER_DEST(DEST_USER_OPTION, "user name for database destination", "login"),
    PWD_DEST(DEST_PASSWORD_OPTION, "password for database destination", "password"),
    DRIVER_DEST(DEST_DRIVER_OPTION, "driver for database destination", "jdbc driver", false),
    COMMIT_SIZE(COMMIT_SIZE_OPTION, String.format("number of rows to commit (default: %s)", DEFAULT_COMMIT_SIZE), "commit size", false),
    HELP(HELP_OPTION, "Print this message", null, false),
    VERSION(VERSION_OPTION, "Display version information", null, false);

    final String name;
    private final String description;
    
    @Nullable
    private final String helperString;
    final boolean required;

    OptionDescriptor(String name, String description, @Nullable String helperString) {
      this(name, description, helperString, true);
    }

    OptionDescriptor(String name, String description, @Nullable String helperString, boolean required) {
      this.name = name;
      this.description = description;
      this.helperString = helperString;
      this.required = required;
    }
  }

  public Arguments() {
    options = new Options();

    for (OptionDescriptor descriptor : OptionDescriptor.values()) {
      if (descriptor.helperString != null) {
        Option option = new Option(descriptor.name, true, descriptor.description);
        option.setValueSeparator(' ');
        option.setArgs(1);
        option.setArgName(descriptor.helperString);
        options.addOption(option);
      } else {
        options.addOption(new Option(descriptor.name, false, descriptor.description));
      }
    }
  }

  public void parseArgs(String[] args) {
    try {
      validateCommandLine(args);

      if (commandLine.hasOption(HELP_OPTION) || commandLine.hasOption(VERSION_OPTION)) {
        return;
      }

      validateExcessArguments();

      populateRawOptionValues();

      validateCommitSize();

      validateRequiredOptions();

      validateJdbcUrl(rawOptionValues.get(SOURCE_URL_OPTION), Collections.singleton("mysql"));
      validateJdbcUrl(rawOptionValues.get(DEST_URL_OPTION), SUPPORTED_DBS);

      inferDriverNames();
    } catch (ValidationError e) {
      this.errors.addAll(e.errors);
    }
  }

  private void validateCommandLine(String[] args) throws ValidationError {
    try {
      commandLine = new DefaultParser().parse(options, args);
    } catch (ParseException e) {
      throw new ValidationError(e.getMessage());
    }
  }

  private void validateExcessArguments() throws ValidationError {
    if (!commandLine.getArgList().isEmpty()) {
      throw new ValidationError("Unknown arguments: " + String.join(", ", commandLine.getArgList()));
    }
  }

  private void populateRawOptionValues() {
    for (OptionDescriptor option : OptionDescriptor.values()) {
      String value = commandLine.getOptionValue(option.name);
      if (value != null) {
        rawOptionValues.put(option.name, value);
      }
    }
  }

  private void validateCommitSize() throws ValidationError {
    if (rawOptionValues.containsKey(COMMIT_SIZE_OPTION)) {
      String value = rawOptionValues.get(COMMIT_SIZE_OPTION);
      try {
        commitSize = Integer.parseInt(value);
      } catch (IllegalArgumentException e) {
        throw new ValidationError(String.format("The value of -%s must be a valid integer, got '%s'", COMMIT_SIZE_OPTION, value));
      }
    } else {
      commitSize = DEFAULT_COMMIT_SIZE;
    }
  }

  private void validateRequiredOptions() throws ValidationError {
    List<String> missingOptions = new ArrayList<>();

    for (OptionDescriptor option : OptionDescriptor.values()) {
      if (option.required && !rawOptionValues.containsKey(option.name)) {
        missingOptions.add("Missing required option: -" + option.name);
      }
    }

    if (!missingOptions.isEmpty()) {
      throw new ValidationError(missingOptions);
    }
  }

  private void validateJdbcUrl(String arg, Set<String> supportedDbs) throws ValidationError {
    String[] parts = arg.split(":");
    if (parts.length < 3 || !parts[0].equals("jdbc")) {
      throw new ValidationError("Not a valid Jdbc Url: " + arg);
    }

    // FIXME h2 should not be allowed either, keeping for now for testing
    if (!supportedDbs.contains(parts[1]) && !parts[1].equals("h2")) {
      throw new ValidationError("Unsupported database: " + parts[1] + ". Supported databases are: " + supportedDbs);
    }
  }

  private void inferDriverNames() {
    if (!rawOptionValues.containsKey(SOURCE_DRIVER_OPTION) && rawOptionValues.containsKey(SOURCE_URL_OPTION)) {
      rawOptionValues.put(SOURCE_DRIVER_OPTION, detectDriverNameFromJdbcUrl(rawOptionValues.get(SOURCE_URL_OPTION)));
    }

    if (!rawOptionValues.containsKey(DEST_DRIVER_OPTION) && rawOptionValues.containsKey(DEST_URL_OPTION)) {
      rawOptionValues.put(DEST_DRIVER_OPTION, detectDriverNameFromJdbcUrl(rawOptionValues.get(DEST_URL_OPTION)));
    }
  }

  List<String> getErrors() {
    return errors;
  }

  public boolean helpRequested() {
    return commandLine.hasOption(HELP_OPTION);
  }

  public boolean versionRequested() {
    return commandLine.hasOption(VERSION_OPTION);
  }

  public void printHelpStringAndExit() {
    final int usualNbOfColumnsInTerminal = 80;

    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(usualNbOfColumnsInTerminal);
    formatter.printHelp(HELP_OPTION, options);
  }

  /**
   * Read Sonar DB Copy version from the MANIFEST.MF
   * this MANIFEST.MF file being filled by Maven
   *
   * @throws MessageException
   */
  public void printVersionString() {
    try {
      URL url = ((URLClassLoader) getClass().getClassLoader()).findResource("META-INF/MANIFEST.MF");
      Manifest manifest = new Manifest(url.openStream());
      String title = manifest.getMainAttributes().getValue("Implementation-Title");
      String version = manifest.getMainAttributes().getValue("Implementation-Version");

      System.out.println(String.format("%s %s", title, version));

    } catch (IOException exception) {
      throw new MessageException(exception);
    }
  }

  public void printOSVersion() {
    String printed = String.format("%s %s %s ", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
    System.out.println(printed);
  }

  public void printJVMVersion() {
    String printed = String.format("Java %s %s ", System.getProperty("java.version"), System.getProperty("java.vendor"));

    String bits = System.getProperty("sun.arch.data.model");
    if ("32".equals(bits) || "64".equals(bits)) {
      printed += String.format(" (%s-bit)", bits);
    }
    System.out.println(printed);
  }

  public boolean isValid() {
    return errors.isEmpty();
  }

  public void printErrors() {
    this.errors.forEach(System.err::println);
  }

  public String getSourceDriver() {
    return rawOptionValues.get(OptionDescriptor.DRIVER_SRC.name);
  }

  public String getSourceUrl() {
    return rawOptionValues.get(OptionDescriptor.URL_SRC.name);
  }

  public String getSourceUser() {
    return rawOptionValues.get(OptionDescriptor.USER_SRC.name);
  }

  public String getSourcePassword() {
    return rawOptionValues.get(OptionDescriptor.PWD_SRC.name);
  }

  public String getTargetDriver() {
    return rawOptionValues.get(OptionDescriptor.DRIVER_DEST.name);
  }

  public String getTargetUrl() {
    return rawOptionValues.get(OptionDescriptor.URL_DEST.name);
  }

  public String getTargetUser() {
    return rawOptionValues.get(OptionDescriptor.USER_DEST.name);
  }

  public String getTargetPassword() {
    return rawOptionValues.get(OptionDescriptor.PWD_DEST.name);
  }

  public int getCommitSize() {
    return commitSize;
  }

  private static class ValidationError extends Exception {
    private final List<String> errors;

    private ValidationError(List<String> errors) {
      this.errors = errors;
    }

    public ValidationError(String message) {
      this(Collections.singletonList(message));
    }
  }
}

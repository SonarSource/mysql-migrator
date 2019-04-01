/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2019 SonarSource SA
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
package org.sonarsource.sqdbmigrator.cli;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import org.sonarsource.sqdbmigrator.argsparser.ArgumentsParser;
import org.sonarsource.sqdbmigrator.argsparser.GlobalValidators;
import org.sonarsource.sqdbmigrator.argsparser.Validator;
import org.sonarsource.sqdbmigrator.argsparser.Validators;
import org.sonarsource.sqdbmigrator.migrator.ConnectionConfig;
import org.sonarsource.sqdbmigrator.migrator.ContentCopier;
import org.sonarsource.sqdbmigrator.migrator.Migrator;
import org.sonarsource.sqdbmigrator.migrator.StatsRecorder;
import org.sonarsource.sqdbmigrator.migrator.System2;
import org.sonarsource.sqdbmigrator.migrator.TableListProvider;
import org.sonarsource.sqdbmigrator.migrator.before.BlankTargetValidator;
import org.sonarsource.sqdbmigrator.migrator.before.PreMigrationChecks;
import org.sonarsource.sqdbmigrator.migrator.before.TableListValidator;
import org.sonarsource.sqdbmigrator.migrator.before.VersionValidator;

import static java.util.Collections.singleton;

public class MySQLMigrator {

  private static final String JDBC_URL_PROPERTY = "sonar.jdbc.url";
  private static final String JDBC_USERNAME_PROPERTY = "sonar.jdbc.username";
  private static final String JDBC_PASSWORD_PROPERTY = "sonar.jdbc.password";

  static final Set<String> SUPPORTED_TARGET_DRIVER_NAMES = new LinkedHashSet<>(Arrays.asList("postgresql", "oracle", "sqlserver"));

  private final System2 system2;
  private final MainExecutor mainExecutor;

  private MySQLMigrator() {
    this(new System2(), (sys, sourceConfig, targetConfig) -> {
      PreMigrationChecks preMigrationChecks = new PreMigrationChecks(new VersionValidator(), new TableListValidator(),
        new BlankTargetValidator());
      Migrator migrator = new Migrator(sys, sourceConfig, targetConfig, new TableListProvider(),
        preMigrationChecks, new ContentCopier(), new StatsRecorder());
      migrator.execute();
    });
  }

  MySQLMigrator(System2 system2, MainExecutor mainExecutor) {
    this.system2 = system2;
    this.mainExecutor = mainExecutor;
  }

  public static void main(String[] args) {
    new MySQLMigrator().run(args);
  }

  void run(String[] args) {
    Validator<ConnectionConfig> sourceConfigValidator = Validators.create(path -> configFromPath(path, "source", singleton("mysql")));
    Validator<ConnectionConfig> targetConfigValidator = Validators.create(path -> configFromPath(path, "target", SUPPORTED_TARGET_DRIVER_NAMES));

    ArgumentsParser parser = ArgumentsParser.newBuilder()
      .setUsageLine("Usage: mysql-migrator [-help] [OPTIONS...]")
      .addOption("-source", "path/to/source/sonar.properties",
        "Path to sonar.properties of the source SonarQube instance", sourceConfigValidator)
      .addOption("-target", "path/to/target/sonar.properties",
        "Path to sonar.properties of the target SonarQube instance", targetConfigValidator)
      .addGlobalValidator(GlobalValidators.allPresent("-source", "-target"))
      .build();

    ArgumentsParser.Result result = parser.parseArgs(args);

    if (result.isHelpRequested()) {
      system2.printlnOut(parser.usageString());
      system2.exit(0);
    } else if (!result.isValid()) {
      system2.printlnErr(result.errorString());
      system2.exit(1);
    } else {
      ConnectionConfig sourceConfig = sourceConfigValidator.value();
      ConnectionConfig targetConfig = targetConfigValidator.value();
      try {
        mainExecutor.execute(system2, sourceConfig, targetConfig);
        system2.exit(0);
      } catch (Exception e) {
        system2.printlnErr(e.getMessage());
        system2.exit(1);
      }
    }
  }

  private static Properties loadProperties(String configPath) throws IOException {
    try (Reader reader = new InputStreamReader(new FileInputStream(configPath), StandardCharsets.UTF_8)) {
      Properties properties = new Properties();
      properties.load(reader);
      return properties;
    }
  }

  private static ConnectionConfig configFromPath(String path, String label, Set<String> supportedDrivers) {
    Properties properties;
    try {
      properties = loadProperties(path);
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not read configuration file: " + e.getMessage());
    }

    String url = properties.getProperty(JDBC_URL_PROPERTY);
    if (url == null) {
      throw new IllegalArgumentException("Missing required configuration: " + JDBC_URL_PROPERTY);
    }

    validateJdbcDriverClassName(url, label, supportedDrivers);
    String username = properties.getProperty(JDBC_USERNAME_PROPERTY);
    String password = properties.getProperty(JDBC_PASSWORD_PROPERTY);
    return new ConnectionConfig(url, username, password);
  }

  private static void validateJdbcDriverClassName(String url, String label, Set<String> supportedDriverNames) {
    if (!url.startsWith("jdbc:")) {
      throw new IllegalArgumentException("Expected JDBC URL to start with 'jdbc:', got: " + url);
    }

    String[] parts = url.split(":");
    if (parts.length < 3) {
      throw new IllegalArgumentException("Expected JDBC URL in the form 'jdbc:driverClassName:url', got: " + url);
    }

    String driverName = parts[1];
    if (!supportedDriverNames.contains(driverName)) {
      throw new IllegalArgumentException(String.format("Unsupported %s driver: %s; supported drivers: %s",
        label, driverName, String.join(", ", supportedDriverNames)));
    }
  }

  interface MainExecutor {
    void execute(System2 system2, ConnectionConfig sourceConfig, ConnectionConfig targetConfig) throws SQLException;
  }
}

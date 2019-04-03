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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sonarsource.sqdbmigrator.migrator.ConnectionConfig;
import org.sonarsource.sqdbmigrator.migrator.System2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(DataProviderRunner.class)
public class MySQLMigratorTest {

  private static final String USAGE_MESSAGE = "Usage: mysql-migrator [-help] [OPTIONS...]\n" +
    "\n" +
    "Options:\n" +
    "\n" +
    "-source path/to/source/sonar.properties\n" +
    "  Path to sonar.properties of the source SonarQube instance\n" +
    "-target path/to/target/sonar.properties\n" +
    "  Path to sonar.properties of the target SonarQube instance\n" +
    "-help\n" +
    "  Print this help\n";

  private final System2 system2 = mock(System2.class);
  private final MySQLMigrator.MainExecutor mainExecutor = mock(MySQLMigrator.MainExecutor.class);

  private final MySQLMigrator underTest = new MySQLMigrator(system2, mainExecutor);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void exit_with_error_when_args_empty() {
    underTest.run(new String[0]);
    verify(system2).printlnErr("Missing required options: -source, -target");
    verify(system2).exit(1);
  }

  @Test
  @UseDataProvider("argsWithMissingRequiredParams")
  public void exit_with_error_when_required_args_missing(String[] args, String expectedMessage) {
    underTest.run(args);
    verify(system2).printlnErr(expectedMessage);
    verify(system2).exit(1);
  }

  @DataProvider
  public static Object[][] argsWithMissingRequiredParams() {
    return new Object[][]{
      {new String[]{"-source", "path"}, "Missing required option: -target"},
      {new String[]{"-target", "path"}, "Missing required option: -source"},
    };
  }

  @Test
  public void exit_with_error_when_unexpected_args_present() {
    underTest.run(new String[]{"unexpected"});
    verify(system2).printlnErr("Unexpected arguments: unexpected");
    verify(system2).exit(1);
  }

  @Test
  @UseDataProvider("validOptionNamesRequiringParameter")
  public void exit_with_error_when_option_parameter_missing(String name) {
    underTest.run(new String[]{name});
    verify(system2).printlnErr("Option " + name + " requires a parameter");
    verify(system2).exit(1);
  }

  @DataProvider
  public static Object[][] validOptionNamesRequiringParameter() {
    return new Object[][]{
      {"-source"},
      {"-target"},
    };
  }

  @Test
  @UseDataProvider("argsIncludingHelp")
  public void exit_with_help_if_requested_and_skip_validation(String[] args) {
    underTest.run(args);
    verify(system2).printlnOut(USAGE_MESSAGE);
    verify(system2).exit(0);
  }

  @DataProvider
  public static Object[][] argsIncludingHelp() {
    return new Object[][]{
      {new String[]{"-help"}},
      {new String[]{"foo", "bar", "-help"}},
      {new String[]{"-foo", "bar", "-help"}},
      {new String[]{"-foo", "-bar", "-help"}},
      {new String[]{"-source", "path", "-help"}},
      {new String[]{"-target", "path", "-help"}},
      {new String[]{"-source", "path", "-target", "path", "-help"}},
    };
  }

  @Test
  public void exit_with_error_when_source_config_file_nonexistent() throws IOException {
    String nonexistentPath = temporaryFolder.newFolder().toPath().resolve("nonexistent").toString();
    underTest.run(new String[]{"-source", nonexistentPath, "-target", newValidTargetPath()});
    verify(system2).printlnErr("Could not read configuration file: " + nonexistentPath + " (No such file or directory)");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_target_config_file_nonexistent() throws IOException {
    String nonexistentPath = temporaryFolder.newFolder().toPath().resolve("nonexistent").toString();
    underTest.run(new String[]{"-source", newValidSourcePath(), "-target", nonexistentPath});
    verify(system2).printlnErr("Could not read configuration file: " + nonexistentPath + " (No such file or directory)");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_source_jdbc_url_missing() throws IOException {
    String path = temporaryFolder.newFile().toString();
    underTest.run(new String[]{"-source", path, "-target", newValidTargetPath()});
    verify(system2).printlnErr("Missing required configuration: sonar.jdbc.url");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_target_jdbc_url_missing() throws IOException {
    String path = temporaryFolder.newFile().toString();
    underTest.run(new String[]{"-source", newValidSourcePath(), "-target", path});
    verify(system2).printlnErr("Missing required configuration: sonar.jdbc.url");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_source_url_doesnt_start_with_jdbc() throws IOException {
    String path = newConfigFileWithoutCredentials("foo");
    underTest.run(new String[]{"-source", path, "-target", newValidTargetPath()});
    verify(system2).printlnErr("Expected JDBC URL to start with 'jdbc:', got: foo");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_target_url_doesnt_start_with_jdbc() throws IOException {
    String path = newConfigFileWithoutCredentials("foo");
    underTest.run(new String[]{"-source", newValidSourcePath(), "-target", path});
    verify(system2).printlnErr("Expected JDBC URL to start with 'jdbc:', got: foo");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_source_url_malformed() throws IOException {
    String path = newConfigFileWithoutCredentials("jdbc:");
    underTest.run(new String[]{"-source", path, "-target", newValidTargetPath()});
    verify(system2).printlnErr("Expected JDBC URL in the form 'jdbc:driverClassName:url', got: jdbc:");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_target_url_malformed() throws IOException {
    String path = newConfigFileWithoutCredentials("jdbc:");
    underTest.run(new String[]{"-source", newValidSourcePath(), "-target", path});
    verify(system2).printlnErr("Expected JDBC URL in the form 'jdbc:driverClassName:url', got: jdbc:");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_source_driver_unsupported() throws IOException {
    String path = newConfigFileWithoutCredentials("jdbc:foo:bar");
    underTest.run(new String[]{"-source", path, "-target", newValidTargetPath()});
    verify(system2).printlnErr("Unsupported source driver: foo; supported drivers: mysql");
    verify(system2).exit(1);
  }

  @Test
  public void exit_with_error_when_target_driver_unsupported() throws IOException {
    String path = newConfigFileWithoutCredentials("jdbc:foo:bar");
    underTest.run(new String[]{"-source", newValidSourcePath(), "-target", path});
    verify(system2).printlnErr("Unsupported target driver: foo; supported drivers: postgresql, oracle, sqlserver");
    verify(system2).exit(1);
  }

  @Test
  @UseDataProvider("supportedTargetDriverNames")
  public void run_with_successfully_parsed_configuration_without_credentials(String driverName) throws Exception {
    String sourceUrl = "jdbc:mysql:bar";
    String sourcePath = newConfigFileWithoutCredentials(sourceUrl);
    String targetUrl = "jdbc:" + driverName + ":bar";
    String targetPath = newConfigFileWithoutCredentials(targetUrl);
    underTest.run(new String[]{"-source", sourcePath, "-target", targetPath});

    ArgumentCaptor<ConnectionConfig> sourceArgumentCaptor = ArgumentCaptor.forClass(ConnectionConfig.class);
    ArgumentCaptor<ConnectionConfig> targetArgumentCaptor = ArgumentCaptor.forClass(ConnectionConfig.class);
    verify(mainExecutor).execute(same(system2), sourceArgumentCaptor.capture(), targetArgumentCaptor.capture());

    ConnectionConfig sourceConfig = sourceArgumentCaptor.getValue();
    assertThat(sourceConfig.url).isEqualTo(sourceUrl);
    assertThat(sourceConfig.username).isNull();
    assertThat(sourceConfig.password).isNull();

    ConnectionConfig targetConfig = targetArgumentCaptor.getValue();
    assertThat(targetConfig.url).isEqualTo(targetUrl);
    assertThat(targetConfig.username).isNull();
    assertThat(targetConfig.password).isNull();

    verify(system2).exit(0);
  }

  @Test
  @UseDataProvider("supportedTargetDriverNames")
  public void run_with_successfully_parsed_configuration(String driverName) throws Exception {
    String sourceUrl = "jdbc:mysql:bar";
    String sourcePath = newConfigFile(sourceUrl, "muser", "mpass");
    String targetUrl = "jdbc:" + driverName + ":bar";
    String targetPath = newConfigFile(targetUrl, "puser", "ppass");
    underTest.run(new String[]{"-source", sourcePath, "-target", targetPath});

    ArgumentCaptor<ConnectionConfig> sourceArgumentCaptor = ArgumentCaptor.forClass(ConnectionConfig.class);
    ArgumentCaptor<ConnectionConfig> targetArgumentCaptor = ArgumentCaptor.forClass(ConnectionConfig.class);
    verify(mainExecutor).execute(same(system2), sourceArgumentCaptor.capture(), targetArgumentCaptor.capture());

    ConnectionConfig sourceConfig = sourceArgumentCaptor.getValue();
    assertThat(sourceConfig.url).isEqualTo(sourceUrl);
    assertThat(sourceConfig.username).isEqualTo("muser");
    assertThat(sourceConfig.password).isEqualTo("mpass");

    ConnectionConfig targetConfig = targetArgumentCaptor.getValue();
    assertThat(targetConfig.url).isEqualTo(targetUrl);
    assertThat(targetConfig.username).isEqualTo("puser");
    assertThat(targetConfig.password).isEqualTo("ppass");

    verify(system2).exit(0);
  }

  @DataProvider
  public static Object[][] supportedTargetDriverNames() {
    return MySQLMigrator.SUPPORTED_TARGET_DRIVER_NAMES.stream()
      .map(name -> new Object[]{name})
      .toArray(Object[][]::new);
  }

  @Test
  public void exit_with_error_when_execution_fails() throws Exception {
    String message = "failed because...";
    doThrow(new RuntimeException(message)).when(mainExecutor).execute(same(system2), any(), any());

    underTest.run(new String[]{"-source", newValidSourcePath(), "-target", newValidTargetPath()});

    verify(system2).printlnErr(message);
    verify(system2).exit(1);
  }

  @Test
  public void print_stdout_and_stderr_of_mainExecutor() throws Exception {
    doAnswer(invocation -> {
      system2.printlnOut("some output");
      system2.printlnErr("some error");
      return null;
    }).when(mainExecutor).execute(same(system2), any(), any());

    underTest.run(new String[]{"-source", newValidSourcePath(), "-target", newValidTargetPath()});

    verify(system2).printlnOut("some output");
    verify(system2).printlnErr("some error");
    verify(system2).exit(0);
  }

  private String newConfigFile(String url, String username, String password) throws IOException {
    Path targetPath = temporaryFolder.newFile().toPath();
    String content = String.format("sonar.jdbc.url = %s\n" +
        "sonar.jdbc.username = %s\n" +
        "sonar.jdbc.password = %s\n",
      url, username, password);
    Files.write(targetPath, content.getBytes());
    return targetPath.toString();
  }

  private String newConfigFileWithoutCredentials(String url) throws IOException {
    Path targetPath = temporaryFolder.newFile().toPath();
    String content = "sonar.jdbc.url = " + url;
    Files.write(targetPath, content.getBytes());
    return targetPath.toString();
  }

  private String newValidSourcePath() throws IOException {
    return newConfigFileWithoutCredentials("jdbc:mysql:foo");
  }

  private String newValidTargetPath() throws IOException {
    return newConfigFileWithoutCredentials("jdbc:postgresql:foo");
  }
}

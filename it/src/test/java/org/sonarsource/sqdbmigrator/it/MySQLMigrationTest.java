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
package org.sonarsource.sqdbmigrator.it;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.config.Configuration;
import com.sonar.orchestrator.db.DefaultDatabase;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issue.SearchWsRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class MySQLMigrationTest {

  private static final Logger LOG = LoggerFactory.getLogger(MySQLMigrationTest.class);

  private static final String SONAR_RUNTIME_VERSION = getSystemPropertyOrFail("sonar.runtimeVersion");

  private static final String SOURCE_ORCHESTRATOR_CONFIG = getSystemPropertyOrFail("orchestrator.configUrl.source");
  private static final String TARGET_ORCHESTRATOR_CONFIG = getSystemPropertyOrFail("orchestrator.configUrl.target");

  private static final String PROJECT_PATH = "projects/xoo-sample";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final Orchestrator source = newOrchestratorBuilder()
    .setOrchestratorProperty("orchestrator.configUrl", SOURCE_ORCHESTRATOR_CONFIG)
    .build();

  private Orchestrator target = newTargetOrchestratorBuilder().build();

  private static OrchestratorBuilder newOrchestratorBuilder() {
    return Orchestrator.builderEnv()
      .setSonarVersion(System.getProperty("sonar.runtimeVersion", SONAR_RUNTIME_VERSION))
      // ES bootstrap checks need to be disable in order to run on dockers images on Cirrus.
      // It will prevent such error at 7.8+ startup : "max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]".
      // See SONAR-11264 for more details on the checks.
      .setServerProperty("sonar.es.bootstrap.checks.disable", "true")
      .addPlugin(MavenLocation.of("org.sonarsource.sonarqube", "sonar-xoo-plugin", SONAR_RUNTIME_VERSION));
  }

  private static OrchestratorBuilder newTargetOrchestratorBuilder() {
    return newOrchestratorBuilder()
      .setOrchestratorProperty("orchestrator.configUrl", TARGET_ORCHESTRATOR_CONFIG);
  }

  @Before
  public void before() {
    // sanity checks
    assertThat(source.getConfiguration().getString("sonar.jdbc.url")).isNotNull();
    assertThat(source.getConfiguration().getString("sonar.jdbc.username")).isNotNull();
    assertThat(source.getConfiguration().getString("sonar.jdbc.password")).isNotNull();
    assertThat(target.getConfiguration().getString("sonar.jdbc.url")).isNotNull();
    assertThat(target.getConfiguration().getString("sonar.jdbc.username")).isNotNull();
    assertThat(target.getConfiguration().getString("sonar.jdbc.password")).isNotNull();
  }

  @After
  public void after() {
    source.stop();
    target.stop();
  }

  @Test
  public void migrate_from_source_to_target() throws IOException, InterruptedException {
    source.start();

    WsClient sourceWsClient = newWsClient(source);

    // no issues at first
    assertThat(getIssueCount(sourceWsClient)).isZero();

    // analyze project, creating issues
    source.executeBuildQuietly(
      SonarScanner.create(new File(PROJECT_PATH + "/1")));

    long sourceIssuesAfter1stAnalysis = getIssueCount(sourceWsClient);

    assertThat(sourceIssuesAfter1stAnalysis).isGreaterThan(0);

    // analyze again, adding one issue
    source.executeBuildQuietly(
      SonarScanner.create(new File(PROJECT_PATH + "/2")));

    long sourceIssuesAfter2ndAnalysis = getIssueCount(sourceWsClient);

    assertThat(sourceIssuesAfter2ndAnalysis).isGreaterThan(sourceIssuesAfter1stAnalysis);

    source.stop();
    target.start();

    WsClient targetWsClient = newWsClient(target);

    // target starts with no issues
    assertThat(getIssueCount(targetWsClient)).isZero();

    // stop target to execute migration offline
    target.stop();

    assertThat(runMigration().exitStatus).isZero();

    // migration won't run again: projects exist in target
    ProcessResult processResult = runMigration();
    assertThat(processResult.exitStatus).isGreaterThan(0);
    assertThat(processResult.output).contains("Unexpected record count in target table 'projects'");

    // verify matching stats
    Stats sourceStats = computeStats(source);
    Stats targetStats = computeStats(target);
    assertThat(targetStats.projects).isEqualTo(sourceStats.projects);
    assertThat(targetStats.projectMeasures).isEqualTo(sourceStats.projectMeasures);
    assertThat(targetStats.issues).isEqualTo(sourceStats.issues);
    assertThat(targetStats.users).isEqualTo(sourceStats.users);

    target = newTargetOrchestratorBuilder()
      .setOrchestratorProperty("orchestrator.keepDatabase", "true")
      .build();
    target.start();

    // recreate target WS client (port number has changed after restart)
    targetWsClient = newWsClient(target);

    // verify matching issues using SQ WS too
    long targetIssuesAfterMigration = getIssueCount(targetWsClient);

    assertThat(targetIssuesAfterMigration).isEqualTo(sourceIssuesAfter2ndAnalysis);

    // analyze same project and verify new issues are successfully added
    target.executeBuildQuietly(
      SonarScanner.create(new File(PROJECT_PATH + "/3")));

    long targetIssuesAfterAnalysis = getIssueCount(targetWsClient);

    assertThat(targetIssuesAfterAnalysis).isGreaterThan(targetIssuesAfterMigration);

    // analyze another project and verify new project and new issues are successfully added
    target.executeBuildQuietly(
      SonarScanner.create(new File(PROJECT_PATH + "/3"))
        .setProperties("sonar.projectKey", "project2"));
    assertThat(getIssueCount(targetWsClient)).isEqualTo(2 * targetIssuesAfterAnalysis);
  }

  @Test
  public void fail_migration_when_sanity_checks_fail() throws IOException, InterruptedException, SQLException {
    ensureEmptyDatabase(source);
    ensureEmptyDatabase(target);

    // fail when source is empty
    ProcessResult processResult = runMigration();
    assertThat(processResult.exitStatus).isGreaterThan(0);
    assertThat(processResult.output).contains("Could not determine SonarQube version of the source database");

    // fail when target is empty
    ensureInitialSonarQubeDatabase(source);
    processResult = runMigration();
    assertThat(processResult.exitStatus).isGreaterThan(0);
    assertThat(processResult.output).contains("Could not determine SonarQube version of the target database");

    // fail when schema versions are same but invalid
    ensureInitialSonarQubeDatabase(target);
    String insertInvalidVersionSql = "insert into schema_migrations values ('999999')";
    runStatementsOnDatabase(source.getConfiguration(), insertInvalidVersionSql);
    runStatementsOnDatabase(target.getConfiguration(), insertInvalidVersionSql);
    processResult = runMigration();
    assertThat(processResult.exitStatus).isGreaterThan(0);
    assertThat(processResult.output).contains("Unknown schema version; cannot match to a SonarQube release: 999999");

    // fail when schema versions are different
    String deleteInvalidVersionSql = "delete from schema_migrations where version = '999999'";
    runStatementsOnDatabase(source.getConfiguration(), deleteInvalidVersionSql);
    processResult = runMigration();
    assertThat(processResult.exitStatus).isGreaterThan(0);
    assertThat(processResult.output).contains("Versions in source and target database don't match");

    // fail if versions match, and valid, but no projects in source
    runStatementsOnDatabase(target.getConfiguration(), deleteInvalidVersionSql);
    processResult = runMigration();
    assertThat(processResult.exitStatus).isGreaterThan(0);
    assertThat(processResult.output).contains("There are no records in the projects table of the source database.");

    // fail if duplicate keys exist in source
    analyzeProjectAndStopServer(source);
    // create duplicate projects.kee values
    String dropIndexSql = "drop index projects_kee on projects";
    String insertDummyKeeSql = "insert into projects (kee, uuid, project_uuid, root_uuid, uuid_path, organization_uuid, private) " +
      "values ('kee1', 'uuid1', 'project_uuid1', 'root_uuid1', 'uuid_path1', 'organization_uuid1', false)";
    runStatementsOnDatabase(source.getConfiguration(), dropIndexSql, insertDummyKeeSql, insertDummyKeeSql);

    processResult = runMigration();
    assertThat(processResult.exitStatus).isGreaterThan(0);
    assertThat(processResult.output).contains("Duplicate kee values detected in projects table.");

    // cleanup; all good, migration succeeds
    runStatementsOnDatabase(source.getConfiguration(), "delete from projects where kee = 'kee1'");
    assertThat(runMigration().exitStatus).isZero();
  }

  private void analyzeProjectAndStopServer(Orchestrator orchestrator) {
    orchestrator.start();

    WsClient sourceWsClient = newWsClient(orchestrator);

    // analyze project, creating issues
    orchestrator.executeBuildQuietly(
      SonarScanner.create(new File(PROJECT_PATH + "/1")));

    long sourceIssues = getIssueCount(sourceWsClient);

    assertThat(sourceIssues).isGreaterThan(0);

    orchestrator.stop();
  }

  private void runStatementsOnDatabase(Configuration configuration, String... sqls) throws SQLException {
    DefaultDatabase database = new DefaultDatabase(configuration);
    database.getClient().setDropAndCreate(false);
    database.start();
    Connection connection = database.openConnection();
    for (String sql : sqls) {
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.executeUpdate();
      }
    }
    database.closeQuietly(connection);
  }

  private long getIssueCount(WsClient wsClient) {
    return wsClient
      .issues()
      .search(new SearchWsRequest())
      .getTotal();
  }

  private void ensureEmptyDatabase(Orchestrator source) {
    new DefaultDatabase(source.getConfiguration()).start();
  }

  private void ensureInitialSonarQubeDatabase(Orchestrator orchestrator) {
    orchestrator.start();
    orchestrator.stop();
  }

  private ProcessResult runMigration() throws IOException, InterruptedException {
    return ProcessResult.run("../build/install/mysql-migrator/bin/mysql-migrator",
      "-source", newConfigFile(source.getConfiguration()),
      "-target", newConfigFile(target.getConfiguration()));
  }

  private static class ProcessResult {
    private final int exitStatus;
    private final String output;

    private ProcessResult(int exitStatus, String output) {
      this.exitStatus = exitStatus;
      this.output = output;
    }

    static ProcessResult run(String... args) throws IOException, InterruptedException {
      Process process = new ProcessBuilder().command(args)
        .redirectErrorStream(true)
        .start();

      StringBuilder sb = new StringBuilder();
      try (BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        do {
          String line = stdOut.readLine();
          if (line != null) {
            LOG.info(line);
            sb.append(line).append("\n");
          }
        } while (process.isAlive());
      }

      int exitStatus = process.waitFor();
      return new ProcessResult(exitStatus, sb.toString());
    }
  }

  private String newConfigFile(Configuration configuration) throws IOException {
    Path targetPath = temporaryFolder.newFile().toPath();
    String content = String.format("sonar.jdbc.url = %s\n" +
      "sonar.jdbc.username = %s\n" +
      "sonar.jdbc.password = %s\n",
      configuration.getString("sonar.jdbc.url"),
      configuration.getString("sonar.jdbc.username"),
      configuration.getString("sonar.jdbc.password"));
    Files.write(targetPath, content.getBytes());
    return targetPath.toString();
  }

  private static WsClient newWsClient(Orchestrator orchestrator) {
    return WsClientFactories.getDefault().newClient(
      HttpConnector.newBuilder()
        .url(orchestrator.getServer().getUrl())
        .build());
  }

  private Stats computeStats(Orchestrator orchestrator) {
    DefaultDatabase database = new DefaultDatabase(orchestrator.getConfiguration());
    database.getClient().setDropAndCreate(false);
    database.start();
    return Stats.from(database);
  }

  private static class Stats {
    private final int projects;
    private final int projectMeasures;
    private final int issues;
    private final int users;

    private Stats(int projects, int projectMeasures, int issues, int users) {
      this.users = users;
      this.projects = projects;
      this.issues = issues;
      this.projectMeasures = projectMeasures;
    }

    static Stats from(DefaultDatabase database) {
      int projects = database.countSql("select count(*) from projects");
      int projectMeasures = database.countSql("select count(*) from project_measures");
      int issues = database.countSql("select count(*) from issues");
      int users = database.countSql("select count(*) from users");
      return new Stats(projects, projectMeasures, issues, users);
    }
  }

  private static String getSystemPropertyOrFail(String name) {
    String value = System.getProperty(name);
    if (value == null || value.isEmpty()) {
      fail(name + " system property must be defined");
    }
    return value;
  }
}

/*
 * Copyright (C) 2017-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.its;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.config.Configuration;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issue.SearchWsRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class DbCopyIntegrationTest {

  private static final String DB_COPY_VERSION_PROPERTY = "sonar.dbCopyVersion";
  private static final String ORCHESTRATOR_PROPERTIES_SOURCE = "orchestrator.configUrl.source";
  private static final String ORCHESTRATOR_PROPERTIES_DESTINATION = "orchestrator.configUrl.destination";

  private String dbCopyVersion;
  private Orchestrator sourceOrchestrator;
  private Orchestrator destinationOrchestrator;

  @Before
  public void setUp() {

    dbCopyVersion = getSystemPropertyOrFail(DB_COPY_VERSION_PROPERTY);

    sourceOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addSystemProperties()
        .setProperty("orchestrator.configUrl", getSystemPropertyOrFail(ORCHESTRATOR_PROPERTIES_SOURCE))
        .addEnvVariables()
        .build()
    )
      .addPlugin("java")
      .build();
    destinationOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addSystemProperties()
        .setProperty("orchestrator.configUrl", getSystemPropertyOrFail(ORCHESTRATOR_PROPERTIES_DESTINATION))
        .addEnvVariables()
        .build()
    )
      .addPlugin("java")
      .build();
  }

  @After
  public void tearDown() {
    sourceOrchestrator.stop();
    destinationOrchestrator.stop();
  }

  @Test
  public void shouldCopyDatabase() throws Exception {

    // Start source SQ
    sourceOrchestrator.start();

    WsClient sourceWsClient = WsClientFactories.getDefault().newClient(
      HttpConnector.newBuilder()
        .url(sourceOrchestrator.getServer().getUrl())
        .build());

    assertThat(sourceWsClient
      .issues()
      .search(new SearchWsRequest())
      .getTotal()).isZero();

    // First analysis
    sourceOrchestrator.executeBuildQuietly(
      MavenBuild.create()
        .setPom(new File("projects/java-sample/1/pom.xml"))
        .setCleanPackageSonarGoals());

    long sourceInitialTotal = sourceWsClient
      .issues()
      .search(new SearchWsRequest())
      .getTotal();

    // Second analysis, should close one additional issue
    sourceOrchestrator.executeBuildQuietly(
      MavenBuild.create()
        .setPom(new File("projects/java-sample/2/pom.xml"))
        .setCleanPackageSonarGoals());

    long sourceFinalTotal = sourceWsClient
      .issues()
      .search(new SearchWsRequest())
      .getTotal();

    // Check that issues exist
    assertThat(sourceFinalTotal).isGreaterThan(sourceInitialTotal);

    // Stop source SQ
    sourceOrchestrator.stop();


    // Start destination SQ
    destinationOrchestrator.start();

    // Check no issues in destination
    WsClient destinationWsClient = WsClientFactories.getDefault().newClient(
      HttpConnector.newBuilder()
        .url(destinationOrchestrator.getServer().getUrl())
        .build());
    assertThat(destinationWsClient
      .issues()
      .search(new SearchWsRequest())
      .getTotal()).isZero();

    // And stop it immediately
    destinationOrchestrator.stop();

    // Execute copy
    Process dbCopyProcess = new ProcessBuilder().command(
      "java",
      "-jar", String.format("../target/sonar-db-copy-%s-jar-with-dependencies.jar", dbCopyVersion),
      "-urlSrc", sourceOrchestrator.getConfiguration().getString("sonar.jdbc.url"),
      "-userSrc", sourceOrchestrator.getConfiguration().getString("sonar.jdbc.username"),
      "-pwdSrc", sourceOrchestrator.getConfiguration().getString("sonar.jdbc.password"),
      "-urlDest", destinationOrchestrator.getConfiguration().getString("sonar.jdbc.url"),
      "-userDest", destinationOrchestrator.getConfiguration().getString("sonar.jdbc.username"),
      "-pwdDest", destinationOrchestrator.getConfiguration().getString("sonar.jdbc.password"))
      .inheritIO()
      .start();

    BufferedReader stdOut = new BufferedReader(new InputStreamReader(dbCopyProcess.getInputStream()));
    do {
      stdOut.readLine();
    } while(dbCopyProcess.isAlive());

    assertThat(dbCopyProcess.waitFor()).isZero();

    // Re-start destination SQ
    destinationOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addSystemProperties()
        .setProperty("orchestrator.configUrl", getSystemPropertyOrFail(ORCHESTRATOR_PROPERTIES_DESTINATION))
        // Prevent DB reset
        .setProperty("orchestrator.keepDatabase", "true")
        .addEnvVariables()
        .build()
    )
      .addPlugin("java")
      .build();
    destinationOrchestrator.start();

    // Check one issue in destination
    destinationWsClient = WsClientFactories.getDefault().newClient(
      HttpConnector.newBuilder()
        .url(destinationOrchestrator.getServer().getUrl())
        .build());
    long destinationFinalTotal = destinationWsClient
      .issues()
      .search(new SearchWsRequest())
      .getTotal();

    // Check that issues exist
    assertThat(destinationFinalTotal).isEqualTo(sourceFinalTotal);
  }

  private static String getSystemPropertyOrFail(String orchestratorPropertiesSource) {
    String propertyValue = System.getProperty(orchestratorPropertiesSource);
    if (StringUtils.isEmpty(propertyValue)) {
      throw fail(orchestratorPropertiesSource + " system property must be defined");
    }
    return propertyValue;
  }
}

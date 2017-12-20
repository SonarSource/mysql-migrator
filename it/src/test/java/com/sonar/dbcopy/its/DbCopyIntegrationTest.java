/*
 * Copyright (C) 2017-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.its;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.config.Configuration;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
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
    ).build();
    destinationOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addSystemProperties()
        .setProperty("orchestrator.configUrl", getSystemPropertyOrFail(ORCHESTRATOR_PROPERTIES_DESTINATION))
        .addEnvVariables()
        .build()
    ).build();
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
    // TODO Perform 2 analyses to have some history
    // sourceOrchestrator.executeBuildQuietly(MavenBuild.create(...));
    // sourceOrchestrator.executeBuildQuietly(MavenBuild.create(...));
    // Stop source SQ
    sourceOrchestrator.stop();

    // Start destination SQ
    destinationOrchestrator.start();
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
    ).build();
    destinationOrchestrator.start();
    // TODO Check data has been copied
    // destinationOrchestrator.getServer().newHttpCall("").execute();
  }

  private static String getSystemPropertyOrFail(String orchestratorPropertiesSource) {
    String propertyValue = System.getProperty(orchestratorPropertiesSource);
    if (StringUtils.isEmpty(propertyValue)) {
      throw fail(orchestratorPropertiesSource + " system property must be defined");
    }
    return propertyValue;
  }
}

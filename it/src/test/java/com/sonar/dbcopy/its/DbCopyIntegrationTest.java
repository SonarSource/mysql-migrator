/*
 * Copyright (C) 2017-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.its;

import com.sonar.dbcopy.StartApp;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.config.Configuration;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Fail.fail;

public class DbCopyIntegrationTest {

  private static final String ORCHESTRATOR_PROPERTIES_SOURCE = "orchestrator.configUrl.source";
  private static final String ORCHESTRATOR_PROPERTIES_DESTINATION = "orchestrator.configUrl.destination";

  private Orchestrator sourceOrchestrator;
  private Orchestrator destinationOrchestrator;

  @Before
  public void setUp() {
    failOnMissingSystemProperty(ORCHESTRATOR_PROPERTIES_SOURCE);
    failOnMissingSystemProperty(ORCHESTRATOR_PROPERTIES_DESTINATION);

    sourceOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addSystemProperties()
        .setProperty("orchestrator.configUrl", System.getProperty(ORCHESTRATOR_PROPERTIES_SOURCE))
        .build()
    ).build();
    destinationOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addSystemProperties()
        .setProperty("orchestrator.configUrl", System.getProperty(ORCHESTRATOR_PROPERTIES_DESTINATION))
        .build()
    ).build();
  }

  @After
  public void tearDown() {
    sourceOrchestrator.stop();
    destinationOrchestrator.stop();
  }

  @Test
  public void shouldCopyDatabase() {
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
    StartApp.main(new String[] {
      "-urlSrc", sourceOrchestrator.getConfiguration().getString("sonar.jdbc.url"),
      "-userSrc", sourceOrchestrator.getConfiguration().getString("sonar.jdbc.username"),
      "-pwdSrc", sourceOrchestrator.getConfiguration().getString("sonar.jdbc.password"),
      "-urlDest", destinationOrchestrator.getConfiguration().getString("sonar.jdbc.url"),
      "-userDest", destinationOrchestrator.getConfiguration().getString("sonar.jdbc.username"),
      "-pwdDest", destinationOrchestrator.getConfiguration().getString("sonar.jdbc.password")
    });

    // Re-start destination SQ
    destinationOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addSystemProperties()
        .setProperty("orchestrator.configUrl", System.getProperty(ORCHESTRATOR_PROPERTIES_DESTINATION))
        // Prevent DB reset
        .setProperty("orchestrator.keepDatabase", "true")
        .build()
      ).build();
    destinationOrchestrator.start();
    // TODO Check data has been copied
    // destinationOrchestrator.getServer().newHttpCall("").execute();
  }

  private static void failOnMissingSystemProperty(String orchestratorPropertiesSource) {
    if (StringUtils.isEmpty(System.getProperty(orchestratorPropertiesSource))) {
      throw fail(orchestratorPropertiesSource + " system property must be defined");
    }
  }
}

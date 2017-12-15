/*
 * Copyright (C) 2017-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.its;

import com.sonar.dbcopy.StartApp;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.config.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.util.Map;
import java.util.Properties;

import static org.fest.assertions.Fail.fail;

public class DbCopyIntegrationTest {

  private static final String ORCHESTRATOR_PROPERTIES_SOURCE = "ORCHESTRATOR_PROPERTIES_SOURCE";
  private static final String ORCHESTRATOR_PROPERTIES_DESTINATION = "ORCHESTRATOR_PROPERTIES_DESTINATION";
  private static final String SUFFIX_SOURCE = "_source";
  private static final String SUFFIX_DESTINATION = "_destination";

  private Orchestrator sourceOrchestrator;
  private Orchestrator destinationOrchestrator;
  private Properties orchestratorPropertiesSource;
  private Properties orchestratorPropertiesDestination;

  @Before
  public void setUp() throws Exception {
    Map<String, String> systemEnvironment = System.getenv();
    failOnMissingEnvironmentVariable(systemEnvironment, ORCHESTRATOR_PROPERTIES_SOURCE);
    failOnMissingEnvironmentVariable(systemEnvironment, ORCHESTRATOR_PROPERTIES_DESTINATION);

    String orchestratorPropertiesSourcePath = systemEnvironment.get(ORCHESTRATOR_PROPERTIES_SOURCE);
    orchestratorPropertiesSource = new Properties();
    orchestratorPropertiesSource.load(new FileReader(orchestratorPropertiesSourcePath));
    appendSuffixToProperty(orchestratorPropertiesSource, "sonar.jdbc.username", SUFFIX_SOURCE);
    // TODO /!\ The line below will not work with all databases /!\
    appendSuffixToProperty(orchestratorPropertiesSource, "sonar.jdbc.url", SUFFIX_SOURCE);

    String orchestratorPropertiesDestinationPath = systemEnvironment.get(ORCHESTRATOR_PROPERTIES_DESTINATION);
    orchestratorPropertiesDestination = new Properties();
    orchestratorPropertiesDestination.load(new FileReader(orchestratorPropertiesDestinationPath));
    appendSuffixToProperty(orchestratorPropertiesDestination, "sonar.jdbc.username", SUFFIX_DESTINATION);
    // TODO /!\ The line below will not work with all databases /!\
    appendSuffixToProperty(orchestratorPropertiesDestination, "sonar.jdbc.url", "_destination");

    sourceOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addProperties(orchestratorPropertiesSource)
        .build()
    ).build();
    destinationOrchestrator = Orchestrator.builder(
      Configuration.builder()
        .addProperties(orchestratorPropertiesDestination)
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
      "-urlSrc", orchestratorPropertiesSource.getProperty("sonar.jdbc.url"),
      "-userSrc", orchestratorPropertiesSource.getProperty("sonar.jdbc.username"),
      "-pwdSrc", orchestratorPropertiesSource.getProperty("sonar.jdbc.password"),
      "-urlDest", orchestratorPropertiesDestination.getProperty("sonar.jdbc.url"),
      "-userDest", orchestratorPropertiesDestination.getProperty("sonar.jdbc.username"),
      "-pwdDest", orchestratorPropertiesDestination.getProperty("sonar.jdbc.password")
    });

    // Re-start destination SQ
    orchestratorPropertiesDestination.setProperty("orchestrator.keepDatabase", "true");
    destinationOrchestrator = Orchestrator.builder(Configuration.builder().addProperties(orchestratorPropertiesDestination).build()).build();
    destinationOrchestrator.start();
    // TODO Check data has been copied
    // destinationOrchestrator.getServer().newHttpCall("").execute();
  }

  private static void failOnMissingEnvironmentVariable(Map<String, String> systemEnvironment, String orchestratorPropertiesSource) {
    if (!systemEnvironment.containsKey(orchestratorPropertiesSource)) {
      throw fail(orchestratorPropertiesSource + " environment variable must be defined");
    }
  }

  private static void appendSuffixToProperty(Properties properties, String propertyName, String suffix) {
    properties.setProperty(propertyName, properties.getProperty(propertyName) + suffix);
  }
}

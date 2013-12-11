/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class LogDisplayTest {

  private LogDisplay logDisplay;
  @Before
  public void createInstance() throws IOException {
    logDisplay = new LogDisplay();
  }
  @Test
  public void testDiplayErrorLog() throws Exception {
    //assertEquals(logDisplay.displayInformationLog(),"mymessage")
  }

  @Test
  public void testDisplayInformationLog() throws Exception {

  }
}

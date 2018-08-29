/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ConnecterDataTest {

  private ConnecterData connecterData;

  @Before
  public void setUp() {
    connecterData = new ConnecterData("driver", "url", "user", "pwd");
  }

  @Test
  public void testGetUrlSource() {
    assertEquals("url", connecterData.getUrl());
  }

  @Test
  public void testGetDriverSource() {
    assertEquals("driver", connecterData.getDriver());
  }

  @Test
  public void testGetUserSource() {
    assertEquals("user", connecterData.getUser());
  }

  @Test
  public void testGetPwdSource() {
    assertEquals("pwd", connecterData.getPwd());
  }

}

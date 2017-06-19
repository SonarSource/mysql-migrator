/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.data;

public class ConnecterData {

  private String driver;
  private String url;
  private String user;
  private String pwd;

  public ConnecterData(String driver, String url, String user, String pwd) {
    this.driver = driver;
    this.url = url;
    this.user = user;
    this.pwd = pwd;
  }

  public String getUrl() {
    return url;
  }

  public String getDriver() {
    return driver;
  }

  public String getUser() {
    return user;
  }

  public String getPwd() {
    return pwd;
  }
}


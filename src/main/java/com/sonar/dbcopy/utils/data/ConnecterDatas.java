/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils.data;

public class ConnecterDatas {

  private String driver, url, user, pwd;

  public ConnecterDatas(String driver, String url, String user, String pwd) {
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


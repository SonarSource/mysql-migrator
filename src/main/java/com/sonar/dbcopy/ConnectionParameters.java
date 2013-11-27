/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.Scanner;

public class ConnectionParameters {

  public int bdd, port;
  private String target;
  private String baseName;

  public ConnectionParameters(String target){
    this.target=target;
    setBdd();
    setPort();
    setBaseName();
  }

  public void setBdd(){
    Scanner scanner = new Scanner(System.in);
    bdd=2;
    while(bdd<1 || bdd>5){
      System.out.println("Veuillez saisir la base "+target+" : ");
      System.out.println("1- ORACLE");
      System.out.println("2- POSTGRESQL");
      System.out.println("3- MYSQL");
      System.out.println("4- SQLSERVER");
      bdd = scanner.nextInt();
    }
  }
  public void setPort (){
      Scanner scanner = new Scanner(System.in);
      port = 5432;
      while (port<1024 || port>65535){
          System.out.println("Veuillez saisir le port "+target+": ");
          port = scanner.nextInt();
      }
  }
  public void setBaseName(){
      Scanner scanner = new Scanner(System.in);
      System.out.println("Veuillez saisir le nom de la base "+target+": ");
      baseName = scanner.nextLine();
  }
  public String getBaseName(){
      return baseName;
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopybythread;

import com.sonar.dbcopyutils.Database;
import com.sonar.dbcopyutils.DbException;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableToGetMetadata implements Runnable{

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private Database database;
  private  DataConnecterByThread dc;

  public RunnableToGetMetadata(DataConnecterByThread dc, Database db) {
    this.dc = dc;
    this.database = db;
  }

  public void run() {
    Connection connectionSource = new ConnecterByThread().doSourceConnection(this.dc);
    Statement statementSource;
    ResultSet resultSetTables = null, resultSetCol = null, resultSetRows = null;

    try {
      statementSource = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
    } catch (SQLException e) {
      throw new DbException("Creation of statement source to get schema failed", e);
    }

    try {
      /* WARNING TO GET TABLES FROM METADATA IN DEPENDS ON THE DATABASE VENDOR */
      String schema;
      DatabaseMetaData metaData = connectionSource.getMetaData();
      if("jdbc:po".equals(metaData.getURL().substring(0,7))){
        schema="public";
      }else if("jdbc:h2".equals(metaData.getURL().substring(0,7))){
        schema=null;
      }else if("jdbc:my".equals(metaData.getURL().substring(0,7))){
        schema=null;
      }else if("jdbc:or".equals(metaData.getURL().substring(0,7))){
        schema=null;
      }else if("jdbc:sq".equals(metaData.getURL().substring(0,7))){
        schema=null;
      }else{
        schema=null;
      }
      /* GET TABLES FROM SCHEMA */
      resultSetTables = metaData.getTables(connectionSource.getCatalog(), schema , "%", new String[]{"TABLE"});

      if (!resultSetTables.isBeforeFirst()) {
        throw new DbException("*** ERROR : NO DATA FOUND IN DATABASE SOURCE ***", new Exception());
      } else {
        while (resultSetTables.next()) {
          database.addTable(resultSetTables.getString("TABLE_NAME"));
          LOGGER.log(Level.INFO, "TABLE "+resultSetTables.getString("TABLE_NAME")+" FOUND");
        }
      }
      resultSetTables.close();

      /* GET COLUMNS FROM TABLES */
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        resultSetCol = metaData.getColumns(null, null, database.getTableName(indexTable), "%");
        while (resultSetCol.next()) {
          database.getTable(indexTable).addColumn(resultSetCol.getString("COLUMN_NAME"));
        }
        resultSetCol.close();
      }

      /* GET NB ROWS BY TABLE */
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        resultSetRows = statementSource.executeQuery("SELECT COUNT(*) FROM "+database.getTableName(indexTable));
        while (resultSetRows.next()) {
          database.getTable(indexTable).setNbRows(resultSetRows.getInt(1));
        }
        database.getTable(indexTable).setIsBuilt(true);
        resultSetRows.close();
      }


    } catch (SQLException e) {
      throw new DbException("Problem to get schema from database source.", e);
    } finally {
      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - | ");
      try {
        resultSetRows.close();
        LOGGER.log(Level.INFO, " | ResultSetRows is closed.                        | ");
      } catch (SQLException e) {
        LOGGER.log(Level.INFO, " | ResultSetRows can't be closed.                  | ");
      }

      try {
        resultSetTables.close();
        LOGGER.log(Level.INFO, " | ResultSetTables is closed.                      | ");
      }catch (SQLException e){
        LOGGER.log(Level.INFO, " | ResultSetTables can't be closed.                | ");
      }

      try {
        resultSetCol.close();
        LOGGER.log(Level.INFO, " | ResultSetCol is closed.                         | ");
      }catch (SQLException e){
        LOGGER.log(Level.INFO, " | ResultSetCol can't be closed.                   | ");
      }

      try {
        statementSource.close();
        LOGGER.log(Level.INFO, " | StatementSource is closed.                      | ");
      }catch (SQLException e){
        LOGGER.log(Level.INFO, " | StatementSource can't be closed.                | ");
      }

      try {
        connectionSource.close();
        LOGGER.log(Level.INFO, " | ConnectionSource is closed.                     | ");
      }catch (SQLException e){
        LOGGER.log(Level.INFO, " | ConnectionSource can't be closed.               | ");
      }

      LOGGER.log(Level.INFO, " | - - - - - - - - - - - - - - - - - - - - - - - - | ");
    }
  }
}

/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.reproduce.reader.*;
import com.sonar.dbcopy.reproduce.writer.*;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.SqlDbException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ToolBuilder {

  private Connection connectionSource, connectionDestination;
  private CharacteristicsRelatedToEditor chRelToEd;

  public ToolBuilder(Connection connectionSource, Connection connectionDestination) {
    this.connectionSource = connectionSource;
    this.connectionDestination = connectionDestination;
    chRelToEd = new CharacteristicsRelatedToEditor();
  }

  public ReaderTool buildReaderTool() {
    ReaderTool readerTool = null;
    try {
      DatabaseMetaData metaSource = connectionSource.getMetaData();
      boolean sourceIsOracle = chRelToEd.isOracle(metaSource);
      boolean sourceIsSqlServer = chRelToEd.isSqlServer(metaSource);
      boolean sourceIsPostgresql = chRelToEd.isPostgresql(metaSource);
      boolean sourecIsMySql = chRelToEd.isMySql(metaSource);
      boolean sourceIsH2 = chRelToEd.isH2(metaSource);

      if (sourceIsOracle) {
        readerTool = new OracleReader();
      } else if (sourceIsSqlServer) {
        readerTool = new SqlServerReader();
      } else if (sourceIsPostgresql) {
        readerTool = new PostgresqlReader();
      } else if (sourecIsMySql) {
        readerTool = new MySqlReader();
      } else if (sourceIsH2) {
        readerTool = new H2Reader();
      }
      return readerTool;
    } catch (SQLException e) {
      throw new SqlDbException("Problem when building reader tool", e);
    }
  }

  public WriterTool buildWriterTool(PreparedStatement destinationStatement) {
    WriterTool writerTool = null;
    try {
      DatabaseMetaData metaDest = connectionDestination.getMetaData();
      boolean destinationIsPostgresql = chRelToEd.isPostgresql(metaDest);
      boolean destinationIsOracle = chRelToEd.isOracle(metaDest);
      boolean destinationIsSqlServer = chRelToEd.isSqlServer(metaDest);
      boolean destinationIsMySql = chRelToEd.isMySql(metaDest);
      boolean destinationIsH2 = chRelToEd.isH2(metaDest);

      if (destinationIsOracle) {
        writerTool = new OracleWriter(destinationStatement);
      } else if (destinationIsSqlServer) {
        writerTool = new SqlServerWriter(destinationStatement);
      } else if (destinationIsPostgresql) {
        writerTool = new PostgresqlWriter(destinationStatement);
      } else if (destinationIsMySql) {
        writerTool = new MySqlWriter(destinationStatement);
      } else if (destinationIsH2) {
        writerTool = new H2Writer(destinationStatement);
      }
      return writerTool;
    } catch (SQLException e) {
      throw new SqlDbException("Problem when building writer tool", e);
    }
  }
}


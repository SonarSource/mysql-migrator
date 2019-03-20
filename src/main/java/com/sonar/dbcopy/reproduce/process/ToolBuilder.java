/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2013-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.reproduce.reader.OracleReader;
import com.sonar.dbcopy.reproduce.reader.PostgresqlReader;
import com.sonar.dbcopy.reproduce.reader.ReaderTool;
import com.sonar.dbcopy.reproduce.reader.SqlServerReader;
import com.sonar.dbcopy.reproduce.reader.MySqlReader;
import com.sonar.dbcopy.reproduce.reader.H2Reader;
import com.sonar.dbcopy.reproduce.writer.PostgresqlWriter;
import com.sonar.dbcopy.reproduce.writer.SqlServerWriter;
import com.sonar.dbcopy.reproduce.writer.WriterTool;
import com.sonar.dbcopy.reproduce.writer.OracleWriter;
import com.sonar.dbcopy.reproduce.writer.MySqlWriter;
import com.sonar.dbcopy.reproduce.writer.H2Writer;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ToolBuilder {

  private Connection connectionSource;
  private Connection connectionDestination;

  public ToolBuilder(Connection connectionSource, Connection connectionDestination) {
    this.connectionSource = connectionSource;
    this.connectionDestination = connectionDestination;
  }

  public ReaderTool buildReaderTool() {
    ReaderTool readerTool = null;
    try {
      DatabaseMetaData metaSource = connectionSource.getMetaData();
      boolean sourceIsOracle = CharacteristicsRelatedToEditor.isOracle(metaSource);
      boolean sourceIsSqlServer = CharacteristicsRelatedToEditor.isSqlServer(metaSource);
      boolean sourceIsPostgresql = CharacteristicsRelatedToEditor.isPostgresql(metaSource);
      boolean sourecIsMySql = CharacteristicsRelatedToEditor.isMySql(metaSource);
      boolean sourceIsH2 = CharacteristicsRelatedToEditor.isH2(metaSource);

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
      throw new SqlDbCopyException("Problem when building reader tool", e);
    }
  }

  public WriterTool buildWriterTool(PreparedStatement destinationStatement) {
    WriterTool writerTool = null;
    try {
      DatabaseMetaData metaDest = connectionDestination.getMetaData();
      boolean destinationIsPostgresql = CharacteristicsRelatedToEditor.isPostgresql(metaDest);
      boolean destinationIsOracle = CharacteristicsRelatedToEditor.isOracle(metaDest);
      boolean destinationIsSqlServer = CharacteristicsRelatedToEditor.isSqlServer(metaDest);
      boolean destinationIsMySql = CharacteristicsRelatedToEditor.isMySql(metaDest);
      boolean destinationIsH2 = CharacteristicsRelatedToEditor.isH2(metaDest);

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
      throw new SqlDbCopyException("Problem when building writer tool", e);
    }
  }
}


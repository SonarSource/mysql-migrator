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

import com.sonar.dbcopy.reproduce.reader.ReaderTool;
import com.sonar.dbcopy.reproduce.writer.WriterTool;
import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class PrepareCopyTable {


  private Table tableSource;
  private Table tableDestination;
  private int commitSize;

  public PrepareCopyTable(Table tableSource, Table tableDestination, int commitSize) {
    this.tableSource = tableSource;
    this.tableDestination = tableDestination;
    this.commitSize = commitSize;
  }

  public void makeToolsAndStartCopy(Connection connectionSource, Connection connectionDest) {
    Closer closer = new Closer("PrepareCopyTable");
    Statement statementSource = null;
    ResultSet resultSetSource = null;
    PreparedStatement preparedStatementDest = null;

    try {
      // BUILD READER TOOL
      ToolBuilder toolBuilder = new ToolBuilder(connectionSource, connectionDest);
      ReaderTool readerTool = toolBuilder.buildReaderTool();

      // GET RESULSET FROM SOURCE
      ResulsetSourceGetter resulsetSourceGetter = new ResulsetSourceGetter(tableSource.getName());
      statementSource = resulsetSourceGetter.createAndReturnStatementSource(connectionSource);
      // SET FETCH SIZE OF MYSQL SOURCE STATEMENT
      if (CharacteristicsRelatedToEditor.isMySql(connectionSource.getMetaData())) {
        statementSource.setFetchSize(Integer.MIN_VALUE);
      } else {
        statementSource.setFetchSize(10);
      }
      resultSetSource = resulsetSourceGetter.createAndReturnResultSetSource(statementSource);

      // BUILD PREPARED STATEMENT FROM DESTINATION
      DestinationStatementBuilder destinationStatementBuilder = new DestinationStatementBuilder();
      preparedStatementDest = destinationStatementBuilder.getDestinationStatement(connectionDest, tableSource);

      // BUILD WRITER TOOL
      WriterTool writerTool = toolBuilder.buildWriterTool(preparedStatementDest);

      LoopByRow loopByRow = new LoopByRow(tableSource, tableDestination, commitSize);
      loopByRow.executeCopy(resultSetSource, preparedStatementDest, readerTool, writerTool);

    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when fetching size of mysql statement source to Integer.MIN_VALUE at TABLE: " + tableSource.getName(), e);
    } finally {
      closer.closeResultSet(resultSetSource);
      closer.closeStatement(statementSource);
      closer.closeStatement(preparedStatementDest);
    }
  }
}


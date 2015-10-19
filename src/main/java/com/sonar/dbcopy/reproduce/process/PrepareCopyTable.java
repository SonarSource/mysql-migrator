/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
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


  private Table tableSource, tableDestination;

  public PrepareCopyTable(Table tableSource, Table tableDestination) {
    this.tableSource = tableSource;
    this.tableDestination = tableDestination;
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
      CharacteristicsRelatedToEditor chRTEd = new CharacteristicsRelatedToEditor();
      if (chRTEd.isMySql(connectionSource.getMetaData())) {
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

      LoopByRow loopByRow = new LoopByRow(tableSource, tableDestination);
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


/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2021 SonarSource SA
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
package org.sonarsource.sqdbmigrator.migrator;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Record and format nicely stats collected during a database migration.
 */
public class StatsRecorder {

  private static final String NEWLINE = "\n";

  private static final String TABLES_LABEL = "Tables";
  private static final String RECORDS_LABEL = "Records";
  private static final String DURATION_LABEL = "Seconds";

  private final int[] columnWidths = {TABLES_LABEL.length(), RECORDS_LABEL.length(), DURATION_LABEL.length()};

  private final List<Record> records = new ArrayList<>();

  void add(String tableName, long recordsCopied, long started, long completed) {
    String recordsCopiedString = String.valueOf(recordsCopied);
    String seconds = formatSeconds(completed - started);

    columnWidths[0] = Math.max(columnWidths[0], tableName.length());
    columnWidths[1] = Math.max(columnWidths[1], recordsCopiedString.length());
    columnWidths[2] = Math.max(columnWidths[2], seconds.length());
    records.add(new Record(tableName, recordsCopiedString, seconds));
  }

  private static String formatToWidth(int width, String s) {
    return formatToWidth(width, s, false);
  }

  private static String formatToWidth(int width, String s, boolean alignLeft) {
    String format = String.format("%%%s%ds  ", alignLeft ? "-" : "", width);
    return String.format(format, s);
  }


  private static String formatSeconds(long millis) {
    return new DecimalFormat("#0.0", new DecimalFormatSymbols(Locale.US)).format(millis / 1000D);
  }

  private static String dashes(int count) {
    char[] chars = new char[count];
    Arrays.fill(chars, '-');
    return new String(chars);
  }

  String formatAsTable() {
    StringBuilder sb = new StringBuilder();
    sb.append(formatToWidth(columnWidths[0], TABLES_LABEL, true))
      .append(formatToWidth(columnWidths[1], RECORDS_LABEL, true))
      .append(formatToWidth(columnWidths[2], DURATION_LABEL, true))
      .append(NEWLINE)
      .append(formatToWidth(columnWidths[0], dashes(columnWidths[0])))
      .append(formatToWidth(columnWidths[1], dashes(columnWidths[1])))
      .append(formatToWidth(columnWidths[2], dashes(columnWidths[2])))
      .append(NEWLINE);

    records.forEach(record -> {
      sb.append(formatToWidth(columnWidths[0], record.tableName, true))
        .append(formatToWidth(columnWidths[1], record.recordsCopied))
        .append(formatToWidth(columnWidths[2], record.duration))
        .append(NEWLINE);
    });
    return sb.toString();
  }

  private static class Record {
    private final String tableName;
    private final String recordsCopied;
    private final String duration;

    Record(String tableName, String recordsCopied, String duration) {
      this.tableName = tableName;
      this.recordsCopied = recordsCopied;
      this.duration = duration;
    }
  }
}

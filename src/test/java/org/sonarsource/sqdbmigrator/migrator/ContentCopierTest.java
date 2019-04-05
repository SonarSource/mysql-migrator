/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2019 SonarSource SA
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

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.sonarsource.sqdbmigrator.migrator.Migrator.MigrationException;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.sonarsource.sqdbmigrator.migrator.DatabaseTester.newTester;

@RunWith(DataProviderRunner.class)
public class ContentCopierTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public final DatabaseTester sourceTester = newTester();

  @Rule
  public final DatabaseTester targetTester = newTester();

  private final ContentCopier underTest = new ContentCopier();
  private final StatsRecorder statsRecorder = mock(StatsRecorder.class);

  @Test
  @UseDataProvider("batchSizesAround5")
  public void copy_all_rows_regardless_of_batchSize_boundaries(int batchSize) throws SQLException {
    String tableName = "foo";
    String createTableSql = String.format("create table %s (name varchar)", tableName);

    sourceTester.createTable(createTableSql)
      .addRow(tableName, Collections.singletonList("foo1"))
      .addRow(tableName, Collections.singletonList("foo2"))
      .addRow(tableName, Collections.singletonList("foo3"))
      .addRow(tableName, Collections.singletonList("foo4"))
      .addRow(tableName, Collections.singletonList("foo5"));

    targetTester.createTable(createTableSql);

    underTest.execute(sourceTester.getDatabase(), targetTester.getDatabase(), newTableListProvider(tableName), statsRecorder, batchSize);

    assertThat(sourceTester.getDatabase().countRows(tableName))
      .isEqualTo(5)
      .isEqualTo(targetTester.getDatabase().countRows(tableName));

    String selectNamesSql = String.format("select name from %s", tableName);
    assertThat(sourceTester.queryForStrings(selectNamesSql))
      .isEqualTo(Arrays.asList("foo1", "foo2", "foo3", "foo4", "foo5"))
      .isEqualTo(targetTester.queryForStrings(selectNamesSql));
  }

  @DataProvider
  public static Object[][] batchSizesAround5() {
    return new Object[][]{
      {4},
      {5},
      {6},
    };
  }

  @Test
  public void copy_all_rows_with_all_supported_column_types() throws SQLException {
    // supported types = the unique types that appear in schema-h2 table definitions file in SonarQube

    String tableName = "t1";
    String createTableSql = String.format(
      "create table %s (\n" +
        "  c_bigint BIGINT NOT NULL,\n" +
        "  c_binary BINARY NOT NULL,\n" +
        "  c_blob BLOB NOT NULL,\n" +
        "  c_boolean BOOLEAN NOT NULL,\n" +
        "  c_clob CLOB NOT NULL,\n" +
        "  c_double DOUBLE NOT NULL,\n" +
        "  c_int INT NOT NULL,\n" +
        "  c_integer INTEGER NOT NULL,\n" +
        "  c_timestamp TIMESTAMP NOT NULL,\n" +
        "  c_tinyint TINYINT NOT NULL,\n" +
        "  c_varchar VARCHAR(100) NOT NULL\n" +
        ")",
      tableName);

    Date d1 = new Date();
    Date d2 = new Date();

    byte[] bytes1 = "foo".getBytes();
    byte[] bytes2 = "bar".getBytes();

    sourceTester.createTable(createTableSql)
      .addRow(tableName, Arrays.asList(BigInteger.TEN, bytes1, bytes1, true, "foo clob", 1.23D, 12, 34, d1, 1, "foo"))
      .addRow(tableName, Arrays.asList(11L, bytes2, bytes2, true, "bar clob", 1.23D, 12, 34, d2, 2, "bar"));

    targetTester.createTable(createTableSql);

    underTest.execute(sourceTester.getDatabase(), targetTester.getDatabase(), newTableListProvider(tableName), statsRecorder);

    assertThat(sourceTester.getDatabase().countRows(tableName))
      .isEqualTo(2)
      .isEqualTo(targetTester.getDatabase().countRows(tableName));

    String selectClobsSql = String.format("select c_clob from %s", tableName);
    assertThat(sourceTester.queryForStrings(selectClobsSql))
      .isEqualTo(Arrays.asList("foo clob", "bar clob"))
      .isEqualTo(targetTester.queryForStrings(selectClobsSql));

    String selectTimestampsSql = String.format("select c_timestamp from %s", tableName);
    assertThat(sourceTester.queryForObjects(selectTimestampsSql))
      .extracting(o -> ((Timestamp) o).getTime())
      .isEqualTo(Arrays.asList(d1.getTime(), d2.getTime()));
    assertThat(targetTester.queryForObjects(selectTimestampsSql))
      .extracting(o -> ((Timestamp) o).getTime())
      .isEqualTo(Arrays.asList(d1.getTime(), d2.getTime()));

    String selectBlobSql = String.format("select c_blob from %s", tableName);
    List<byte[]> sourceBytes = sourceTester.queryForBytes(selectBlobSql);
    assertThat(sourceBytes).hasSize(2);
    assertThat(sourceBytes.get(0)).isEqualTo(bytes1);
    assertThat(sourceBytes.get(1)).isEqualTo(bytes2);

    List<byte[]> targetBytes = targetTester.queryForBytes(selectBlobSql);
    assertThat(targetBytes).hasSize(2);
    assertThat(targetBytes.get(0)).isEqualTo(bytes1);
    assertThat(targetBytes.get(1)).isEqualTo(bytes2);
  }

  @Test
  public void update_sequence_in_target() throws SQLException {
    String tableName = "t1";
    String createTableSql = String.format(
      "create table %s (\"ID\" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "  \"NAME\" VARCHAR(128) NOT NULL\n" +
        ")",
      tableName);

    sourceTester.createTable(createTableSql)
      .addRow(tableName, singletonList("name"), singletonList("foo"))
      .addRow(tableName, singletonList("name"), singletonList("bar"))
      .addRow(tableName, singletonList("name"), singletonList("baz"));

    targetTester.createTable(createTableSql);

    underTest.execute(sourceTester.getDatabase(), targetTester.getDatabase(), newTableListProvider(tableName), statsRecorder);

    targetTester
      .addRow(tableName, singletonList("name"), singletonList("foo2"))
      .addRow(tableName, singletonList("name"), singletonList("bar2"));

    assertThat(targetTester.getDatabase().countRows(tableName)).isEqualTo(5);

    String selectMaxIdSql = String.format("select max(id) from %s", tableName);
    assertThat(targetTester.queryForInt(selectMaxIdSql)).isEqualTo(5);
  }

  @Test
  public void copy_correctly_even_when_column_order_doesnt_match() throws SQLException {
    String tableName = "foo";
    String createSourceTableSql = String.format("create table %s (name1 varchar, name2 varchar)", tableName);
    String createTargetTableSql = String.format("create table %s (name2 varchar, name1 varchar)", tableName);

    sourceTester.createTable(createSourceTableSql)
      .addRow(tableName, Arrays.asList("foo1", "foo2"))
      .addRow(tableName, Arrays.asList("bar1", "bar2"));

    targetTester.createTable(createTargetTableSql);

    underTest.execute(sourceTester.getDatabase(), targetTester.getDatabase(), newTableListProvider(tableName), statsRecorder);

    assertThat(sourceTester.getDatabase().countRows(tableName))
      .isEqualTo(2)
      .isEqualTo(targetTester.getDatabase().countRows(tableName));

    String selectName1Sql = String.format("select name1 from %s", tableName);
    assertThat(sourceTester.queryForStrings(selectName1Sql))
      .isEqualTo(Arrays.asList("foo1", "bar1"))
      .isEqualTo(targetTester.queryForStrings(selectName1Sql));

    String selectName2Sql = String.format("select name2 from %s", tableName);
    assertThat(sourceTester.queryForStrings(selectName2Sql))
      .isEqualTo(Arrays.asList("foo2", "bar2"))
      .isEqualTo(targetTester.queryForStrings(selectName2Sql));
  }

  @Test
  public void throw_if_something_goes_wrong_during_copy() throws SQLException {
    Database target = mock(Database.class);
    doThrow(SQLException.class).when(target).truncateTable(anyString());

    expectedException.expect(MigrationException.class);
    underTest.execute(mock(Database.class), target, newTableListProvider("foo"), statsRecorder);
  }

  @Test
  public void throw_if_something_goes_wrong_during_reset_sequence() throws SQLException {
    String tableName = "foo";
    String createTableSql = String.format(
      "create table %s (\"ID\" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
        "  \"NAME\" VARCHAR(128) NOT NULL\n" +
        ")",
      tableName);
    Database source = sourceTester.createTable(createTableSql).getDatabase();

    Database target = mock(Database.class, withSettings().defaultAnswer(Answers.RETURNS_MOCKS.get()));
    doThrow(SQLException.class).when(target).resetSequence(same(tableName), anyInt());

    expectedException.expect(MigrationException.class);
    underTest.execute(source, target, newTableListProvider(tableName), statsRecorder);
  }

  private TableListProvider newTableListProvider(String tableName) {
    return new TableListProvider() {
      @Override
      public List<String> get(Database database) {
        return singletonList(tableName);
      }
    };
  }
}

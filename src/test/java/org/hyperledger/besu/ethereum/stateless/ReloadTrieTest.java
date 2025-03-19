/*
 * Copyright Hyperledger Besu Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.hyperledger.besu.ethereum.stateless;

import org.hyperledger.besu.ethereum.stateless.factory.StoredNodeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ReloadTrieTest {

  private static VerkleTrieBatchHasher batchProcessor;
  private static StoredNodeFactory<Bytes> nodeFactory;
  private static NodeUpdaterMock nodeUpdater;

  private static Stream<Arguments> provideGenesisAndStateRootExpected() {
    return Stream.of(
        Arguments.of(
            createDataMap(
                "/gen-devnet-7.csv",
                "0x514a0e5715b0b6c635ac140a5f25b8665af36cf31836344d27d9645fc57eab76")),
        Arguments.of(
            createDataMap(
                "/devnet-7-block-1.csv",
                "0x5ac6bdb5f3dc47cfa7665c981f795a1116459dfeecde174433d14d710c433cc4")),
        Arguments.of(
            createDataMap(
                "/devnet-7-block-2.csv",
                "0x2a63a94a9cb5dc948f98bd9a18cd4db0214adbc7cef2297a5f84362b14cb6513")),
        Arguments.of(
            createDataMap(
                "/devnet-7-block-3.csv",
                "0x4818200283aff9e7fd8a788c28bd6e54a6545a6f3af9e8d75a784c43a4b75fbe")),
        Arguments.of(
            createDataMap(
                "/devnet-7-block-4.csv",
                "0x25d529a8a345af4db9e0987fbaea65a17632835180e3a1fb8eeed93743996dc9")),
        Arguments.of(
            createDataMap(
                "/devnet-7-block-5.csv",
                "0x19eefd036a31289e338bec949d777ad56c6e7e12e769399a1b1322e601f282cf")),
        Arguments.of(
            createDataMap(
                "/devnet-7-block-6.csv",
                "0x0338f6cf3d3087bd33b1d47960f39c2a75a5bc060cf56fcb31ab6f0203bd6866")),
        Arguments.of(
            createDataMap(
                "/devnet-7-block-7.csv",
                "0x3a897a8bbc000434f83a9a53e80916fb75bc232497284ccdc3e5c9643f9fc7fc")));
  }

  private static Map<String, String> createDataMap(String filePath, String expectedRoot) {
    Map<String, String> data = new HashMap<>();
    data.put("filePath", filePath);
    data.put("expectedRoot", expectedRoot);
    return data;
  }

  @BeforeAll
  public static void setUp() {
    nodeUpdater = new NodeUpdaterMock();
    final NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    batchProcessor = new VerkleTrieBatchHasher();
    nodeFactory = new StoredNodeFactory<>(nodeLoader, value -> value);
  }

  @ParameterizedTest
  @MethodSource("provideGenesisAndStateRootExpected")
  public void reloadTrieTest(final Map<String, String> data) throws IOException {
    final String genesisCSVFile = data.get("filePath");
    final String expectedRoot = data.get("expectedRoot");

    StoredBatchedVerkleTrie<Bytes32, Bytes> trie =
        new StoredBatchedVerkleTrie<>(batchProcessor, nodeFactory);
    InputStream input = ReloadTrieTest.class.getResourceAsStream(genesisCSVFile);
    try (Reader reader = new InputStreamReader(input, "UTF-8");
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT); ) {
      for (CSVRecord csvRecord : csvParser) {
        Bytes32 key = Bytes32.fromHexString(csvRecord.get(0));
        Bytes value = Bytes.fromHexString(csvRecord.get(1));
        trie.put(key, value);
      }
    }
    trie.commit(nodeUpdater);

    Assertions.assertThat(trie.getRootHash())
        .isEqualByComparingTo(Bytes32.fromHexString(expectedRoot));
  }
}

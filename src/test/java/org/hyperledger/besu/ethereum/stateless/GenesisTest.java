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

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.ethereum.stateless.factory.StoredNodeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GenesisTest {

  private static Stream<Arguments> provideGenesisAndStateRootExpected() {
    return Stream.of(
        Arguments.of(
            "/gen-devnet-2.csv",
            "0x5e8519756841faf0b2c28951c451b61a4b407b70a5ce5b57992f4bec973173ff"),
        Arguments.of(
            "/gen-devnet-3.csv",
            "0x382960711d9ccf58b9db20122e2253eb9bfa99d513f8c9d4e85b55971721f4de"),
        Arguments.of(
            "/gen-devnet-6.csv",
            "0x1fbf85345a3cbba9a6d44f991b721e55620a22397c2a93ee8d5011136ac300ee"),
        Arguments.of(
            "/gen-devnet-7.csv",
            "0x514a0e5715b0b6c635ac140a5f25b8665af36cf31836344d27d9645fc57eab76"));
  }

  @ParameterizedTest
  @MethodSource("provideGenesisAndStateRootExpected")
  public void putGenesis(String genesisCSVFile, String expectedStateRootHash) throws IOException {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes> nodeFactory = new StoredNodeFactory<>(nodeLoader, value -> value);
    StoredBatchedVerkleTrie<Bytes32, Bytes> trie =
        new StoredBatchedVerkleTrie<>(batchProcessor, nodeFactory);
    InputStream input = GenesisTest.class.getResourceAsStream(genesisCSVFile);
    try (Reader reader = new InputStreamReader(input, "UTF-8");
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT); ) {
      for (CSVRecord csvRecord : csvParser) {
        Bytes32 key = Bytes32.fromHexString(csvRecord.get(0));
        Bytes value = Bytes.fromHexString(csvRecord.get(1));
        trie.put(key, value);
      }
    }
    trie.commit(nodeUpdater);

    assertThat(trie.getRootHash())
        .isEqualByComparingTo(Bytes32.fromHexString(expectedStateRootHash));
  }
}

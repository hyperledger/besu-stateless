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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

public class DotExporterTest {

  /**
   * Reads the content of a file from the resources folder.
   *
   * @param fileName The name of the file in the resources folder.
   * @return The content of the file as a String.
   * @throws IOException If an I/O error occurs.
   */
  private String getResources(final String fileName) throws IOException {

    var classLoader = DotDisplayTest.class.getClassLoader();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                classLoader.getResourceAsStream(fileName), StandardCharsets.UTF_8))) {

      return reader.lines().collect(Collectors.joining("\n"));
    }
  }

  @Test
  public void testToDotTrieOneValueNoRepeatingEdgesExport() throws IOException {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<>();
    Bytes32 key =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    trie.put(key, value);

    trie.dotTreeToFile("src/test/resources/VerkleTrie.gv");

    final String fileName = "expectedTreeOneValueNoNulls.txt";
    final String expectedTree = getResources(fileName);

    final String actualFromFile = getResources("VerkleTrie.gv");

    assertEquals(expectedTree, actualFromFile);
  }
}

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

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StemPrunableNodeRegistryTest – stem pruning behaviour")
class StemPrunableNodeRegistryTest {

  private static final Bytes32 KEY =
      Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
  private static final Bytes32 VALUE =
      Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
  private static final Bytes STEM = KEY.slice(0, 31); // first 31 bytes

  private NodeUpdaterMock nodeUpdater;
  private StoredVerkleTrie<Bytes32, Bytes32> trie;

  @BeforeEach
  void setUp() {
    nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory = new StoredNodeFactory<>(nodeLoader, v -> (Bytes32) v);
    trie = new StoredVerkleTrie<>(nodeFactory);
  }

  @Test
  @DisplayName("prunes stem after key is removed and committed")
  void shouldPruneStemAfterRemoveAndCommit() {
    // put + commit
    trie.put(KEY, VALUE);
    trie.commit(nodeUpdater);
    assertThat(stemExists()).isTrue();
    assertThat(trie.getRootHash()).isNotEqualTo(Bytes32.ZERO);

    // remove + second commit
    trie.remove(KEY);
    trie.commit(nodeUpdater);

    assertThat(stemExists()).isFalse();
    assertThat(trie.getRootHash()).isEqualTo(Bytes32.ZERO);
  }

  @Test
  @DisplayName("keeps stem when key is removed then re-added before commit")
  void shouldKeepStemWhenKeyRemovedAndReaddedBeforeSameCommit() {
    // put + commit
    trie.put(KEY, VALUE);
    trie.commit(nodeUpdater);
    assertThat(stemExists()).isTrue();

    // remove + put (same transaction) + commit
    trie.remove(KEY);
    trie.put(KEY, VALUE);
    trie.commit(nodeUpdater);

    assertThat(stemExists()).isTrue();
    assertThat(trie.getRootHash()).isNotEqualTo(Bytes32.ZERO);
  }

  @Test
  @DisplayName("keeps stem when key is re-added in a later transaction")
  void shouldKeepStemWhenKeyReaddedAfterSeparateCommit() {
    // put + commit
    trie.put(KEY, VALUE);
    trie.commit(nodeUpdater);
    assertThat(stemExists()).isTrue();

    // remove + commit  ➜ stem prunable
    trie.remove(KEY);
    trie.commit(nodeUpdater);
    assertThat(stemExists()).isFalse();

    // put again + commit ➜ stem revived
    trie.put(KEY, VALUE);
    trie.commit(nodeUpdater);

    assertThat(stemExists()).isTrue();
    assertThat(trie.getRootHash()).isNotEqualTo(Bytes32.ZERO);
  }

  /**
   * @return {@code true} if the current in-memory DB still contains the stem.
   */
  private boolean stemExists() {
    return nodeUpdater.storage.keySet().stream().anyMatch(k -> k.equals(STEM));
  }
}

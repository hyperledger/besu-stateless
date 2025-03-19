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

import org.hyperledger.besu.ethereum.stateless.factory.NodeFactory;

import org.apache.tuweni.bytes.Bytes;

/**
 * Implements a Verkle Trie that batches node hashing by level.
 *
 * @param <K> The type of keys in the Verkle Trie.
 * @param <V> The type of values in the Verkle Trie.
 */
public class StoredBatchedVerkleTrie<K extends Bytes, V extends Bytes>
    extends SimpleBatchedVerkleTrie<K, V> {
  /** NodeFactory that load nodes from storage */
  protected final NodeFactory<V> nodeFactory;

  /**
   * Constructs a new trie with a node factory and batch processor.
   *
   * @param batchProcessor The processor for batching node hashing.
   * @param nodeFactory The {@link NodeFactory} to retrieve node.
   */
  public StoredBatchedVerkleTrie(
      final VerkleTrieBatchHasher batchProcessor, final NodeFactory<V> nodeFactory) {
    super(nodeFactory.retrieve(Bytes.EMPTY, null), batchProcessor);
    this.nodeFactory = nodeFactory;
  }
}

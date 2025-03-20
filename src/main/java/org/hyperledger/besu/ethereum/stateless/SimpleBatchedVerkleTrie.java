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

import static com.google.common.base.Preconditions.checkNotNull;

import org.hyperledger.besu.ethereum.stateless.node.Node;
import org.hyperledger.besu.ethereum.stateless.visitor.CommitVisitor;
import org.hyperledger.besu.ethereum.stateless.visitor.PutVisitor;
import org.hyperledger.besu.ethereum.stateless.visitor.RemoveVisitor;
import org.hyperledger.besu.ethereum.trie.NodeUpdater;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * A simple implementation of a Verkle Trie.
 *
 * @param <K> The type of keys in the Verkle Trie.
 * @param <V> The type of values in the Verkle Trie.
 */
public class SimpleBatchedVerkleTrie<K extends Bytes, V extends Bytes>
    extends SimpleVerkleTrie<K, V> {

  private final VerkleTrieBatchHasher batchProcessor;

  public SimpleBatchedVerkleTrie(final VerkleTrieBatchHasher batchProcessor) {
    super();
    this.batchProcessor = batchProcessor;
    this.batchProcessor.addNodeToBatch(Optional.of(Bytes.EMPTY), this.root);
  }

  public SimpleBatchedVerkleTrie(
      final Node<V> providedRoot, final VerkleTrieBatchHasher batchProcessor) {
    super(providedRoot);
    this.batchProcessor = batchProcessor;
    this.batchProcessor.addNodeToBatch(root.getLocation(), root);
  }

  public SimpleBatchedVerkleTrie(
      final Optional<Node<V>> maybeRoot, final VerkleTrieBatchHasher batchProcessor) {
    super(maybeRoot);
    this.batchProcessor = batchProcessor;
    this.batchProcessor.addNodeToBatch(root.getLocation(), root);
  }

  @Override
  public Optional<V> put(final K key, final V value) {
    checkNotNull(key);
    checkNotNull(value);
    PutVisitor<V> visitor = new PutVisitor<>(value, Optional.of(batchProcessor));
    this.root = root.accept(visitor, key);
    return visitor.getOldValue();
  }

  @Override
  public void remove(final K key) {
    checkNotNull(key);
    this.root = root.accept(new RemoveVisitor<V>(Optional.of(batchProcessor)), key);
  }

  @Override
  public void commit(final NodeUpdater nodeUpdater) {
    batchProcessor.calculateStateRoot();
    root = root.accept(new CommitVisitor<V>(nodeUpdater), Bytes.EMPTY);
  }

  @Override
  public Bytes32 getRootHash() {
    batchProcessor.calculateStateRoot();
    return root.getHash().get();
  }
}

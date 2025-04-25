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

import org.hyperledger.besu.ethereum.stateless.exporter.DotExporter;
import org.hyperledger.besu.ethereum.stateless.node.InternalNode;
import org.hyperledger.besu.ethereum.stateless.node.Node;
import org.hyperledger.besu.ethereum.stateless.pruning.StemPrunableNodeRegistry;
import org.hyperledger.besu.ethereum.stateless.visitor.CommitVisitor;
import org.hyperledger.besu.ethereum.stateless.visitor.GetVisitor;
import org.hyperledger.besu.ethereum.stateless.visitor.HashVisitor;
import org.hyperledger.besu.ethereum.stateless.visitor.PutVisitor;
import org.hyperledger.besu.ethereum.stateless.visitor.RemoveVisitor;
import org.hyperledger.besu.ethereum.trie.NodeUpdater;

import java.io.IOException;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * A simple implementation of a Verkle Trie. The batched version is recommended for better
 * performance
 *
 * @see SimpleBatchedVerkleTrie
 * @param <K> The type of keys in the Verkle Trie.
 * @param <V> The type of values in the Verkle Trie.
 */
public class SimpleVerkleTrie<K extends Bytes, V extends Bytes> implements VerkleTrie<K, V> {

  protected StemPrunableNodeRegistry stemPrunableNodeRegistry;
  protected Node<V> root;

  /** Creates a new Verkle Trie with a null node as the root. */
  public SimpleVerkleTrie() {
    this(new InternalNode<V>(Bytes.EMPTY));
  }

  /**
   * Creates a new Verkle Trie with the specified node as the root.
   *
   * @param root The root node of the Verkle Trie.
   */
  public SimpleVerkleTrie(final Optional<Node<V>> root) {
    this(root.orElse(new InternalNode<V>(Bytes.EMPTY)));
  }

  /**
   * Creates a new Verkle Trie with the specified node as the root.
   *
   * @param root The root node of the Verkle Trie.
   */
  public SimpleVerkleTrie(final Node<V> root) {
    this.root = root;
    this.stemPrunableNodeRegistry = new StemPrunableNodeRegistry();
  }

  /**
   * Retrieves the root node of the Verkle Trie.
   *
   * @return The root node of the Verkle Trie.
   */
  public Node<V> getRoot() {
    return root;
  }

  /**
   * Gets the value associated with the specified key from the Verkle Trie.
   *
   * @param key The key to retrieve the value for.
   * @return An optional containing the value if found, or an empty optional if not found.
   */
  @Override
  public Optional<V> get(final K key) {
    checkNotNull(key);
    return root.accept(new GetVisitor<V>(), key).getValue();
  }

  /**
   * Inserts a key-value pair into the Verkle Trie.
   *
   * @param key The key to insert.
   * @param value The value to associate with the key.
   */
  @Override
  public Optional<V> put(final K key, final V value) {
    checkNotNull(key);
    checkNotNull(value);
    final PutVisitor<V> visitor =
        new PutVisitor<V>(value, stemPrunableNodeRegistry, Optional.empty());
    this.root = root.accept(visitor, key);
    return visitor.getOldValue();
  }

  /**
   * Removes a key-value pair from the Verkle Trie.
   *
   * @param key The key to remove.
   */
  @Override
  public void remove(final K key) {
    checkNotNull(key);
    this.root = root.accept(new RemoveVisitor<V>(stemPrunableNodeRegistry, Optional.empty()), key);
  }

  /**
   * Computes and returns the root hash of the Verkle Trie.
   *
   * @return The root hash of the Verkle Trie.
   */
  @Override
  public Bytes32 getRootHash() {
    root = root.accept(new HashVisitor<V>(), Bytes.EMPTY);
    return root.getHash().get();
  }

  /**
   * Returns a string representation of the Verkle Trie.
   *
   * @return A string in the format "SimpleVerkleTrie[RootHash]".
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getRootHash() + "]";
  }

  /**
   * Commits the Verkle Trie using the provided node updater.
   *
   * @param nodeUpdater The node updater for storing the changes in the Verkle Trie.
   */
  @Override
  public void commit(final NodeUpdater nodeUpdater) {
    root = root.accept(new HashVisitor<V>(), Bytes.EMPTY);
    // Prune all stems marked as removable by setting their value to null in the node updater.
    stemPrunableNodeRegistry
        .getPrunableStems()
        .forEach(stem -> nodeUpdater.store(stem, null, null));
    stemPrunableNodeRegistry.clear();
    // Commit all updated nodes
    root = root.accept(new CommitVisitor<V>(nodeUpdater), Bytes.EMPTY);
  }

  /**
   * Returns the DOT representation of the entire Verkle Trie.
   *
   * @param showNullNodes if true displays NullNodes and NullLeafNodes; if false does not.
   * @return The DOT representation of the Verkle Trie.
   */
  public String toDotTree(Boolean showNullNodes) {
    return String.format(
        "digraph VerkleTrie {\n%s\n}",
        getRoot().toDot(showNullNodes).replaceAll("^\\n+|\\n+$", ""));
  }

  /**
   * Returns the DOT representation of the entire Verkle Trie.
   *
   * <p>The representation does not contain NullNodes and NullLeafNodes.
   *
   * @return The DOT representation of the Verkle Trie.
   */
  public String toDotTree() {
    StringBuilder result = new StringBuilder("digraph VerkleTrie {\n");
    Node<V> root = getRoot();
    result.append(root.toDot());
    return result.append("}").toString();
  }

  /**
   * Exports the Verkle Trie DOT representation to a '.gv' file located in the current directory.
   * The default file name is "VerkleTree.gv".
   *
   * @throws IOException if an I/O error occurs.
   */
  public void dotTreeToFile() throws IOException {
    DotExporter.exportToDotFile(toDotTree());
  }

  /**
   * /** Exports the Verkle Trie DOT representation to a '.gv' file located at the specified path.
   *
   * @param path The location where the DOT file will be saved.
   * @throws IOException if ann I/O error occurs.
   */
  public void dotTreeToFile(String path) throws IOException {
    DotExporter.exportToDotFile(toDotTree(), path);
  }
}

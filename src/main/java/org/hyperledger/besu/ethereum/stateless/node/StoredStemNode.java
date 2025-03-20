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
package org.hyperledger.besu.ethereum.stateless.node;

import org.hyperledger.besu.ethereum.stateless.factory.NodeFactory;
import org.hyperledger.besu.ethereum.stateless.visitor.NodeVisitor;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * Represents a regular node that can possibly be stored in storage.
 *
 * <p>StoredNodes wrap regular nodes and loads them lazily from storage as needed.
 *
 * @param <V> The type of the node's value.
 */
public class StoredStemNode<V> extends StoredNode<V> {
  final Bytes stem;

  /**
   * Constructs a new StoredNode at location.
   *
   * @param nodeFactory The node factory for creating nodes from storage.
   * @param location The location in the tree.
   * @param stem The stem
   */
  public StoredStemNode(final NodeFactory<V> nodeFactory, final Bytes location, final Bytes stem) {
    super(nodeFactory, location);
    this.stem = stem;
  }

  /**
   * Constructs a new StoredNode at location.
   *
   * @param nodeFactory The node factory for creating nodes from storage.
   * @param location The location in the tree.
   * @param stem The stem
   * @param hash The hash value of the node.
   */
  public StoredStemNode(
      final NodeFactory<V> nodeFactory,
      final Bytes location,
      final Bytes stem,
      final Bytes32 hash) {
    super(nodeFactory, location, hash);
    this.stem = stem;
  }

  /**
   * Accept a visitor to perform operations on the node.
   *
   * @param visitor The visitor to accept.
   * @return The result of the visitor's operation.
   */
  @Override
  public Node<V> accept(NodeVisitor<V> visitor) {
    final Node<V> node = load();
    return node.accept(visitor);
  }

  public Bytes getStem() {
    return stem;
  }

  /**
   * Get a string representation of the node.
   *
   * @return A string representation of the node.
   */
  @Override
  public String print() {
    return String.format("(Stored Stem) %s", hash.orElse(null));
  }

  @Override
  Optional<Node<V>> retrieve() {
    return nodeFactory.retrieve(stem, hash.orElse(null));
  }
}

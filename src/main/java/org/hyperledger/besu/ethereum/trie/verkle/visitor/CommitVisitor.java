/*
 * Copyright Besu Contributors
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
package org.hyperledger.besu.ethereum.trie.verkle.visitor;

import org.hyperledger.besu.ethereum.trie.NodeUpdater;
import org.hyperledger.besu.ethereum.trie.verkle.node.InternalNode;
import org.hyperledger.besu.ethereum.trie.verkle.node.LeafNode;
import org.hyperledger.besu.ethereum.trie.verkle.node.Node;
import org.hyperledger.besu.ethereum.trie.verkle.node.NullLeafNode;
import org.hyperledger.besu.ethereum.trie.verkle.node.NullNode;
import org.hyperledger.besu.ethereum.trie.verkle.node.StemNode;

import org.apache.tuweni.bytes.Bytes;

/**
 * A visitor class responsible for committing changes to nodes in a Trie tree.
 *
 * <p>It iterates through the nodes and stores the changes in the Trie structure.
 *
 * @param <V> The type of node values.
 */
public class CommitVisitor<V> implements PathNodeVisitor<V> {

  /** The NodeUpdater used to store changes in the Trie structure. */
  protected final NodeUpdater nodeUpdater;

  /**
   * Constructs a CommitVisitor with a provided NodeUpdater.
   *
   * @param nodeUpdater The NodeUpdater used to store changes in the Trie structure.
   */
  public CommitVisitor(final NodeUpdater nodeUpdater) {
    this.nodeUpdater = nodeUpdater;
  }

  /**
   * Visits a InternalNode to commit any changes in the node and its children.
   *
   * @param internalNode The internalNode being visited.
   * @param location The location in the Trie tree.
   * @return The visited internalNode.
   */
  @Override
  public Node<V> visit(final InternalNode<V> internalNode, final Bytes location) {
    if (!internalNode.isDirty()) {
      return internalNode;
    }
    for (int i = 0; i < InternalNode.maxChild(); ++i) {
      Bytes index = Bytes.of(i);
      final Node<V> child = internalNode.child((byte) i);
      child.accept(this, Bytes.concatenate(location, index));
    }
    nodeUpdater.store(location, null, internalNode.getEncodedValue());
    internalNode.markClean();
    return internalNode;
  }

  /**
   * Visits a stemNode to commit any changes in the node and its children.
   *
   * @param stemNode The stemNode being visited.
   * @param location The location in the Trie tree.
   * @return The visited stemNode.
   */
  @Override
  public Node<V> visit(final StemNode<V> stemNode, final Bytes location) {
    if (!stemNode.isDirty()) {
      return stemNode;
    }
    final Bytes stem = stemNode.getStem();
    for (int i = 0; i < StemNode.maxChild(); ++i) {
      Bytes index = Bytes.of(i);
      final Node<V> child = stemNode.child((byte) i);
      child.accept(this, Bytes.concatenate(stem, index));
    }
    nodeUpdater.store(location, null, stemNode.getEncodedValue());
    stemNode.markClean();
    return stemNode;
  }

  /**
   * Visits a LeafNode to commit any changes in the node.
   *
   * @param leafNode The LeafNode being visited.
   * @param location The location in the Trie tree.
   * @return The visited LeafNode.
   */
  @Override
  public Node<V> visit(final LeafNode<V> leafNode, final Bytes location) {
    if (!leafNode.isDirty()) {
      return leafNode;
    }
    nodeUpdater.store(location, null, leafNode.getEncodedValue());
    leafNode.markClean();
    return leafNode;
  }

  /**
   * Visits a NullNode, indicating no changes to commit.
   *
   * @param nullNode The NullNode being visited.
   * @param location The location in the Trie tree.
   * @return The NullNode indicating no changes.
   */
  @Override
  public Node<V> visit(final NullNode<V> nullNode, final Bytes location) {
    return nullNode;
  }

  /**
   * Visits a NullLeafNode, indicating no changes to commit.
   *
   * @param nullLeafNode The NullLeafNode being visited.
   * @param location The location in the Trie tree.
   * @return The NullLeafNode indicating no changes.
   */
  @Override
  public Node<V> visit(final NullLeafNode<V> nullLeafNode, final Bytes location) {
    return nullLeafNode;
  }
}

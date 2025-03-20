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
package org.hyperledger.besu.ethereum.stateless.visitor;

import org.hyperledger.besu.ethereum.stateless.node.InternalNode;
import org.hyperledger.besu.ethereum.stateless.node.LeafNode;
import org.hyperledger.besu.ethereum.stateless.node.Node;
import org.hyperledger.besu.ethereum.stateless.node.NullNode;
import org.hyperledger.besu.ethereum.stateless.node.StemNode;

/**
 * Defines a visitor interface for nodes in the Verkle Trie.
 *
 * @param <V> The type of value associated with nodes.
 */
public interface NodeVisitor<V> {

  /**
   * Visits an internal node.
   *
   * @param internalNode The internal node to visit.
   * @return The result of visiting the internal node.
   */
  default Node<V> visit(InternalNode<V> internalNode) {
    return internalNode;
  }

  /**
   * Visits a stem node.
   *
   * @param stemNode The stem node to visit.
   * @return The result of visiting the branch node.
   */
  default Node<V> visit(StemNode<V> stemNode) {
    return stemNode;
  }

  /**
   * Visits a leaf node.
   *
   * @param leafNode The leaf node to visit.
   * @return The result of visiting the leaf node.
   */
  default Node<V> visit(LeafNode<V> leafNode) {
    return leafNode;
  }

  /**
   * Visits a null node.
   *
   * @param nullNode The null node to visit.
   * @return The result of visiting the null node.
   */
  default Node<V> visit(NullNode<V> nullNode) {
    return nullNode;
  }
}

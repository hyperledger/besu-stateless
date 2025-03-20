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

import org.apache.tuweni.bytes.Bytes;

/**
 * Visit the trie with a path parameter.
 *
 * <p>The path parameter indicates the path visited in the Trie. As Nodes are (mostly) immutable,
 * visiting a Node returns a (possibly) new Node that should replace the old one.
 */
public interface PathNodeVisitor<V> {

  /**
   * Visits a internal node with a specified path.
   *
   * @param internalNode The internal node to visit.
   * @param path The path associated with the visit.
   * @return The result of visiting the internal node.
   */
  default Node<V> visit(InternalNode<V> internalNode, Bytes path) {
    return internalNode;
  }

  /**
   * Visits a branch node with a specified path.
   *
   * @param stemNode The branch node to visit.
   * @param path The path associated with the visit.
   * @return The result of visiting the branch node.
   */
  default Node<V> visit(StemNode<V> stemNode, Bytes path) {
    return stemNode;
  }

  /**
   * Visits a leaf node with a specified path.
   *
   * @param leafNode The leaf node to visit.
   * @param path The path associated with the visit.
   * @return The result of visiting the leaf node.
   */
  default Node<V> visit(LeafNode<V> leafNode, Bytes path) {
    return leafNode;
  }

  /**
   * Visits a null node with a specified path.
   *
   * @param nullNode The null node to visit.
   * @param path The path associated with the visit.
   * @return The result of visiting the null node.
   */
  default Node<V> visit(NullNode<V> nullNode, Bytes path) {
    return nullNode;
  }
}

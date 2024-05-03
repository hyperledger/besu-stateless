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
package org.hyperledger.besu.ethereum.trie.verkle.node;

import org.hyperledger.besu.ethereum.trie.verkle.visitor.NodeVisitor;
import org.hyperledger.besu.ethereum.trie.verkle.visitor.PathNodeVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * An interface representing a node in the Verkle Trie.
 *
 * @param <V> The type of the node's value.
 */
public interface Node<V> {

  /** A constant representing a commitment's hash to NullNodes */
  Bytes32 EMPTY_HASH = Bytes32.ZERO;

  /** A constant representing a commitment to NullNodes */
  Bytes EMPTY_COMMITMENT = Bytes.EMPTY;

  /**
   * Accept a visitor to perform operations on the node based on a provided path.
   *
   * @param visitor The visitor to accept.
   * @param path The path associated with a node.
   * @return The result of visitor's operation.
   */
  Node<V> accept(PathNodeVisitor<V> visitor, Bytes path);

  /**
   * Accept a visitor to perform operations on the node.
   *
   * @param visitor The visitor to accept.
   * @return The result of the visitor's operation.
   */
  Node<V> accept(NodeVisitor<V> visitor);

  /**
   * Get the location of the node.
   *
   * @return An optional containing the location of the node if available.
   */
  default Optional<Bytes> getLocation() {
    return Optional.empty();
  }

  /**
   * Get the value associated with the node.
   *
   * @return An optional containing the value of the node if available.
   */
  default Optional<V> getValue() {
    return Optional.empty();
  }

  /**
   * Get the hash associated with the node.
   *
   * @return An optional containing the hash of the node if available.
   */
  default Optional<Bytes32> getHash() {
    return Optional.empty();
  }

  /**
   * Get the value associated with the node.
   *
   * @return An optional containing the value of the node if available.
   */
  default Optional<V> getCommittedValue() {
    return Optional.empty();
  }

  /**
   * Get the commitment associated with the node.
   *
   * @return An optional containing the hash of the node if available.
   */
  default Optional<Bytes> getCommitment() {
    return Optional.empty();
  }

  /**
   * Get the encoded value of the node.
   *
   * @return The encoded value of the node.
   */
  default Bytes getEncodedValue() {
    return Bytes.EMPTY;
  }

  /**
   * Get the children nodes of this node.
   *
   * @return A list of children nodes.
   */
  default List<Node<V>> getChildren() {
    return Collections.emptyList();
  }

  /** Marks the node as needs to be persisted */
  void markDirty();

  /** Marks the node as no longer requiring persistence. */
  void markClean();

  /**
   * Is this node not persisted and needs to be?
   *
   * @return True if the node needs to be persisted.
   */
  boolean isDirty();

  /**
   * Get a string representation of the node.
   *
   * @return A string representation of the node.
   */
  String print();

  /**
   * Generates DOT representation for the Node.
   *
   * @param showNullNodes If true, prints NullNodes and NullLeafNodes; if false, prints only unique
   *     edges.
   * @return DOT representation of the Node.
   */
  String toDot(Boolean showNullNodes);

  /**
   * Generates DOT representation for the Node.
   *
   * <p>Representation does not contain repeating edges.
   *
   * @return DOT representation of the Node.
   */
  default String toDot() {
    return toDot(false);
  }

  /**
   * Retrieves the low value part of a given optional value.
   *
   * @param value The optional value.
   * @return The low value.
   */
  static Bytes32 getLowValue(Optional<?> value) {
    // Low values have a flag at bit 128.
    return value
        .map(
            (v) ->
                Bytes32.rightPad(
                    Bytes.concatenate(Bytes32.rightPad((Bytes) v).slice(0, 16), Bytes.of(1))))
        .orElse(Bytes32.ZERO);
  }

  /**
   * Retrieves the high value part of a given optional value.
   *
   * @param value The optional value.
   * @return The high value.
   */
  static Bytes32 getHighValue(Optional<?> value) {
    return value
        .map((v) -> Bytes32.rightPad(Bytes32.rightPad((Bytes) v).slice(16, 16)))
        .orElse(Bytes32.ZERO);
  }
}

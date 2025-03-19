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

import org.hyperledger.besu.ethereum.stateless.visitor.NodeVisitor;
import org.hyperledger.besu.ethereum.stateless.visitor.PathNodeVisitor;

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
public abstract class Node<V> {

  /** A constant representing a commitment's hash to NullNodes */
  public static Bytes32 EMPTY_HASH = Bytes32.ZERO;

  /** A constant representing a commitment to NullNodes */
  public static Bytes EMPTY_COMMITMENT =
      Bytes.fromHexString(
          "0x0000000000000000000000000000000000000000000000000000000000000000"
              + "0100000000000000000000000000000000000000000000000000000000000000");

  Optional<?> previous;
  boolean dirty;
  boolean persisted;

  public Node(final boolean dirty, final boolean persisted) {
    this.dirty = dirty;
    this.persisted = persisted;
    this.previous = Optional.empty();
  }

  public Node(final Optional<Bytes> previous, final boolean dirty, final boolean persisted) {
    this.previous = previous;
    this.dirty = dirty;
    this.persisted = persisted;
  }

  /**
   * Accept a visitor to perform operations on the node based on a provided path.
   *
   * @param visitor The visitor to accept.
   * @param path The path associated with a node.
   * @return The result of visitor's operation.
   */
  public abstract Node<V> accept(PathNodeVisitor<V> visitor, Bytes path);

  /**
   * Accept a visitor to perform operations on the node.
   *
   * @param visitor The visitor to accept.
   * @return The result of the visitor's operation.
   */
  public abstract Node<V> accept(NodeVisitor<V> visitor);

  /**
   * Get the location of the node.
   *
   * @return An optional containing the location of the node if available.
   */
  public Optional<Bytes> getLocation() {
    return Optional.empty();
  }

  /**
   * Replace node's Location
   *
   * @param newLocation The new location for the Node
   * @return The updated Node
   */
  public abstract Node<V> replaceLocation(Bytes newLocation);

  /**
   * Get the value associated with the node.
   *
   * @return An optional containing the value of the node if available.
   */
  public Optional<V> getValue() {
    return Optional.empty();
  }

  /**
   * Get the hash associated with the node.
   *
   * @return An optional containing the hash of the node if available.
   */
  public Optional<Bytes32> getHash() {
    return Optional.empty();
  }

  /**
   * Get the commitment associated with the node.
   *
   * @return An optional containing the hash of the node if available.
   */
  public Optional<Bytes> getCommitment() {
    return Optional.empty();
  }

  /**
   * Get the encoded value of the node.
   *
   * @return The encoded value of the node.
   */
  public Bytes getEncodedValue() {
    return Bytes.EMPTY;
  }

  /**
   * Get the children nodes of this node.
   *
   * @return A list of children nodes.
   */
  public List<Node<V>> getChildren() {
    return Collections.emptyList();
  }

  /**
   * Retrieves the previous state of this node, if it exists.
   *
   * <p>This method is used to obtain the state of the node before the current one.
   *
   * @return An {@link Optional} containing the previous state of this node if it exists; otherwise,
   *     an empty {@link Optional}.
   */
  public Optional<?> getPrevious() {
    return previous;
  }

  /**
   * Sets the previous state of this node.
   *
   * <p>This method allows updating the node's previous state. It is typically used during the
   * process of node modification to keep a record of the node's state prior to the current changes.
   *
   * @param previous An {@link Optional} containing the new previous state to be set for this node.
   */
  public void setPrevious(final Optional<?> previous) {
    this.previous = previous;
  }

  /** Marks the node as needs to be persisted */
  public void markDirty() {
    dirty = true;
    persisted = false;
  }

  /** Marks this node as needing an update of its scalar and commitment. */
  public void markClean() {
    dirty = false;
  }

  /** Marks the node as no longer requiring persistence. */
  public void markPersisted() {
    persisted = true;
  }

  /**
   * Is this node needing an update of its scalar and commitment?
   *
   * @return True if the node needs to be updated.
   */
  public boolean isDirty() {
    return dirty;
  }

  /**
   * Is this node not persisted and needs to be?
   *
   * @return True if the node needs to be persisted.
   */
  public boolean isPersisted() {
    return persisted;
  }

  /**
   * Get a string representation of the node.
   *
   * @return A string representation of the node.
   */
  public abstract String print();

  /**
   * Generates DOT representation for the Node.
   *
   * @param showNullNodes If true, prints NullNodes and NullLeafNodes; if false, prints only unique
   *     edges.
   * @return DOT representation of the Node.
   */
  public abstract String toDot(Boolean showNullNodes);

  /**
   * Gets the name for the DOT representation for the Node.
   *
   * @return name for the DOT representation for the Node.
   */
  public String getName() {
    return getClass().getSimpleName();
  }

  /**
   * Generates DOT representation for the Node.
   *
   * <p>Representation does not contain repeating edges.
   *
   * @return DOT representation of the Node.
   */
  public String toDot() {
    return toDot(false);
  }

  /**
   * Retrieves the low value part of a given optional value.
   *
   * @param value The optional value.
   * @return The low value.
   */
  public static Bytes32 getLowValue(Optional<?> value) {
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
  public static Bytes32 getHighValue(Optional<?> value) {
    return value
        .map((v) -> Bytes32.rightPad(Bytes32.rightPad((Bytes) v).slice(16, 16)))
        .orElse(Bytes32.ZERO);
  }

  /**
   * Encode a commitment
   *
   * @param value A commitment value
   * @return The encoded commitment
   */
  public static Bytes encodeCommitment(Bytes value) {
    return value != EMPTY_COMMITMENT ? value.trimTrailingZeros() : Bytes.EMPTY;
  }

  /**
   * Encode a scalar
   *
   * @param scalar A scalar value
   * @return The encoded scalar
   */
  public static Bytes encodeScalar(Bytes32 scalar) {
    return scalar != EMPTY_HASH ? scalar.trimTrailingZeros() : Bytes.EMPTY;
  }
}

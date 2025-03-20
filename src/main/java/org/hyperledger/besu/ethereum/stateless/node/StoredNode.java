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
import org.hyperledger.besu.ethereum.stateless.visitor.PathNodeVisitor;

import java.util.List;
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
public abstract class StoredNode<V> extends Node<V> {
  Bytes location;
  final Optional<Bytes32> hash;
  final NodeFactory<V> nodeFactory;
  Optional<Node<V>> loadedNode;

  /**
   * Constructs a new StoredNode at location.
   *
   * @param nodeFactory The node factory for creating nodes from storage.
   * @param location The location in the tree.
   */
  public StoredNode(final NodeFactory<V> nodeFactory, final Bytes location) {
    super(false, true);
    this.location = location;
    this.hash = Optional.empty();
    this.nodeFactory = nodeFactory;
    loadedNode = Optional.empty();
  }

  /**
   * Constructs a new StoredNode at location.
   *
   * @param nodeFactory The node factory for creating nodes from storage.
   * @param location The location in the tree.
   * @param hash The hash value of the node.
   */
  public StoredNode(final NodeFactory<V> nodeFactory, final Bytes location, final Bytes32 hash) {
    super(false, true);
    this.location = location;
    this.hash = Optional.of(hash);
    this.nodeFactory = nodeFactory;
    loadedNode = Optional.empty();
  }

  /**
   * Accept a visitor to perform operations on the node based on a provided path.
   *
   * @param visitor The visitor to accept.
   * @param path The path associated with a node.
   * @return The result of visitor's operation.
   */
  @Override
  public Node<V> accept(PathNodeVisitor<V> visitor, Bytes path) {
    Node<V> node = load();
    return node.accept(visitor, path);
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

  /**
   * Get the location of the node.
   *
   * @return An optional containing the location of the node if available.
   */
  @Override
  public Optional<Bytes> getLocation() {
    return Optional.of(location);
  }

  /**
   * Replace node's Location
   *
   * @param newLocation The new location for the Node
   * @return The updated Node
   */
  @Override
  public StoredNode<V> replaceLocation(Bytes newLocation) {
    location = newLocation;
    return this;
  }

  /**
   * Get the value associated with the node.
   *
   * @return An optional containing the value of the node if available.
   */
  @Override
  public Optional<V> getValue() {
    throw new RuntimeException("Should load storedNode before getValue");
  }

  /**
   * Get the hash associated with the node.
   *
   * @return An optional containing the hash of the node if available.
   */
  @Override
  public Optional<Bytes32> getHash() {
    return hash;
  }

  /**
   * Get the commitment associated with the node.
   *
   * @return An optional containing the commitment of the node if available.
   */
  @Override
  public Optional<Bytes> getCommitment() {
    throw new RuntimeException("Should load StoredNode before getCommitment");
  }

  @Override
  public void markDirty() {
    // no op
  }

  /**
   * Get the encoded value of the node.
   *
   * @return The encoded value of the node.
   */
  @Override
  public Bytes getEncodedValue() {
    throw new RuntimeException("Should load StoredNode before getEncodedValue");
  }

  /**
   * Get the children nodes of this node.
   *
   * @return A list of children nodes.
   */
  @Override
  public List<Node<V>> getChildren() {
    throw new RuntimeException("Should load StoredNode before getChildren");
  }

  /**
   * Get a string representation of the node.
   *
   * @return A string representation of the node.
   */
  @Override
  public String print() {
    return String.format("Stored %s %s", location, hash);
  }

  /**
   * Generates DOT representation for the StoredNode.
   *
   * @return DOT representation of the StoredNode.
   */
  @Override
  public String toDot(Boolean showNullNodes) {
    String result =
        getName()
            + getLocation().orElse(Bytes.EMPTY)
            + " [label=\"SD: "
            + getLocation().orElse(Bytes.EMPTY)
            + "\"]\n";
    return result;
  }

  Optional<Node<V>> retrieve() {
    return nodeFactory.retrieve(location, hash.orElse(null));
  }

  Node<V> load() {
    if (loadedNode.isEmpty()) {
      loadedNode = retrieve();
    }
    if (loadedNode.isPresent()) {
      return loadedNode.get();
    } else if (location.size() == 32) {
      return NullNode.newNullLeafNode();
    } else {
      return NullNode.nullNode();
    }
  }
}

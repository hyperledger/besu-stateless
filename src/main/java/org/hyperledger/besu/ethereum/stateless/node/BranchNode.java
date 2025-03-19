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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * Represents a branch node in the Verkle Trie.
 *
 * @param <V> The type of the node's value.
 */
@SuppressWarnings("DoubleBraceInitialization")
public abstract class BranchNode<V> extends Node<V> {
  protected Optional<Bytes> location; // Location in the tree
  protected Optional<Bytes32> hash; // Vector commitment's hash
  protected Optional<Bytes> commitment; // Vector commitment serialized
  private final List<Node<V>> children; // List of children nodes

  /**
   * Constructs a new BranchNode with optional location, optional hash, optional commitment and
   * children.
   *
   * @param location The optional location in the tree.
   * @param hash The optional vector commitment of children's commitments.
   * @param commitment Node's optional vector commitment.
   * @param children The list of children nodes.
   */
  public BranchNode(
      final Optional<Bytes> location,
      final Optional<Bytes32> hash,
      final Optional<Bytes> commitment,
      final List<Node<V>> children) {
    super(false, true);
    assert (children.size() == maxChild());
    this.location = location;
    this.hash = hash;
    this.commitment = commitment;
    this.children = children;
  }

  /**
   * Constructs a new BranchNode with location, hash, path, and children.
   *
   * @param location The location in the tree.
   * @param hash Node's vector commitment's hash.
   * @param commitment Node's vector commitment.
   * @param children The list of children nodes.
   */
  public BranchNode(
      final Bytes location,
      final Bytes32 hash,
      final Bytes commitment,
      final List<Node<V>> children) {
    this(Optional.of(location), Optional.of(hash), Optional.of(commitment), children);
  }

  /**
   * Constructs a new BranchNode with optional location and path, initializing children to
   * NullNodes.
   *
   * @param location The optional location in the tree.
   */
  public BranchNode(final Bytes location) {
    this(
        Optional.of(location),
        Optional.of(EMPTY_HASH),
        Optional.empty(),
        new ArrayList<>() {
          {
            for (int i = 0; i < maxChild(); i++) {
              add(NullNode.nullNode());
            }
          }
        });
  }

  /**
   * Get the maximum number of children nodes (256 for byte indexes).
   *
   * @return The maximum number of children nodes.
   */
  public static int maxChild() {
    return 256;
  }

  @Override
  public void markClean() {
    super.markClean();
    getChildren().forEach(Node::markClean);
  }

  /**
   * Accepts a visitor for path-based operations on the node.
   *
   * @param visitor The path node visitor.
   * @param path The path associated with a node.
   * @return The result of the visitor's operation.
   */
  @Override
  public abstract Node<V> accept(PathNodeVisitor<V> visitor, Bytes path);

  /**
   * Accepts a visitor for generic node operations.
   *
   * @param visitor The node visitor.
   * @return The result of the visitor's operation.
   */
  @Override
  public abstract Node<V> accept(final NodeVisitor<V> visitor);

  /**
   * Get the child node at a specified index.
   *
   * @param childIndex The index of the child node.
   * @return The child node.
   */
  public Node<V> child(final byte childIndex) {
    return children.get(Byte.toUnsignedInt(childIndex));
  }

  /**
   * Replaces the child node at a specified index with a new node.
   *
   * @param index The index of the child node to replace.
   * @param childNode The new child node.
   */
  public void replaceChild(final byte index, final Node<V> childNode) {
    children.set(Byte.toUnsignedInt(index), childNode);
  }

  /**
   * Get the vector commitment's hash of child commitment hashes.
   *
   * @return An optional containing the vector commitment.
   */
  @Override
  public Optional<Bytes32> getHash() {
    return hash;
  }

  /**
   * Get the vector commitment's hash of child commitment hashes.
   *
   * @return An optional containing the vector commitment.
   */
  @Override
  public Optional<Bytes> getCommitment() {
    return commitment;
  }

  /**
   * Get the location in the tree.
   *
   * @return An optional containing the location if available.
   */
  @Override
  public Optional<Bytes> getLocation() {
    return location;
  }

  /**
   * Get the RLP-encoded value of the node.
   *
   * @return The RLP-encoded value.
   */
  @Override
  public abstract Bytes getEncodedValue();

  /**
   * Get the list of children nodes.
   *
   * @return The list of children nodes.
   */
  @Override
  public List<Node<V>> getChildren() {
    return children;
  }

  /**
   * Allows returning the previous hash of this node.
   *
   * @return previous hash of this node
   */
  @Override
  public Optional<Bytes32> getPrevious() {
    return previous.map(Bytes32.class::cast);
  }

  /**
   * Generates a string representation of the branch node and its children.
   *
   * @return A string representing the branch node and its children.
   */
  @Override
  public String print() {
    final StringBuilder builder = new StringBuilder();
    builder.append("Branch:");
    for (int i = 0; i < maxChild(); i++) {
      final Node<V> child = child((byte) i);
      if (!(child instanceof NullNode)) {
        final String branchLabel = "[" + Integer.toHexString(i) + "] ";
        final String childRep = child.print().replaceAll("\n\t", "\n\t\t");
        builder.append("\n\t").append(branchLabel).append(childRep);
      }
    }
    return builder.toString();
  }

  /**
   * Generates DOT representation for the BranchNode.
   *
   * @return DOT representation of the BranchNode.
   */
  @Override
  public String toDot(Boolean showNullNodes) {
    StringBuilder result =
        new StringBuilder()
            .append(getName())
            .append(getLocation().orElse(Bytes.EMPTY))
            .append(" [label=\"B: ")
            .append(getLocation().orElse(Bytes.EMPTY))
            .append("\n")
            .append("Commitment: ")
            .append(getCommitment().orElse(Bytes32.ZERO))
            .append("\"]\n");

    for (Node<V> child : getChildren()) {
      String edgeString =
          getName()
              + getLocation().orElse(Bytes.EMPTY)
              + " -> "
              + child.getName()
              + child.getLocation().orElse(Bytes.EMPTY)
              + "\n";

      if (showNullNodes || !result.toString().contains(edgeString)) {
        result.append(edgeString);
      }
      result.append(child.toDot(showNullNodes));
    }

    return result.toString();
  }
}

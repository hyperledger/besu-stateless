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

import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.trie.verkle.visitor.NodeVisitor;
import org.hyperledger.besu.ethereum.trie.verkle.visitor.PathNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * Represents a stem node in the Verkle Trie.
 *
 * <p>StemNodes are nodes storing the stem of the key, and is the root of the suffix to value trie.
 *
 * @param <V> The type of the node's value.
 */
public class StemNode<V> extends BranchNode<V> {

  /** A constant representing a StemNode's extension Marker */
  public static Bytes32 MARKER = Bytes32.fromHexString("0x01");

  private final Bytes stem;
  private Optional<Bytes32> leftHash;
  private Optional<Bytes> leftCommitment;
  private Optional<Bytes32> rightHash;
  private Optional<Bytes> rightCommitment;
  private Optional<Bytes> encodedValue = Optional.empty();

  /**
   * Constructs a new StemNode with non-optional parameters.
   *
   * @param location The location in the tree.
   * @param stem Node's stem.
   * @param hash Node's vector commitment's hash.
   * @param commitment Node's vector commitment.
   * @param leftHash Hash of vector commitment to left values.
   * @param leftCommitment Vector commitment to left values.
   * @param rightHash Hash of vector commitment to right values.
   * @param rightCommitment Vector commitment to right values.
   * @param children The list of children nodes.
   */
  public StemNode(
      final Bytes location,
      final Bytes stem,
      final Bytes32 hash,
      final Bytes commitment,
      final Bytes32 leftHash,
      final Bytes leftCommitment,
      final Bytes32 rightHash,
      final Bytes rightCommitment,
      final List<Node<V>> children) {
    super(location, hash, commitment, children);
    this.stem = extractStem(stem);
    this.leftHash = Optional.of(leftHash);
    this.leftCommitment = Optional.of(leftCommitment);
    this.rightHash = Optional.of(rightHash);
    this.rightCommitment = Optional.of(rightCommitment);
    this.previous = Optional.of(hash);
  }

  /**
   * Constructs a new StemNode with optional parameters.
   *
   * @param location Optional location in the tree.
   * @param stem Node's stem.
   * @param hash Optional node's vector commitment's hash.
   * @param previousHash Optional node's vector previous commitment's hash.
   * @param commitment Optional node's vector commitment.
   * @param leftHash Optional hash of vector commitment to left values.
   * @param leftCommitment Optional vector commitment to left values.
   * @param rightHash Optional hash of vector commitment to right values.
   * @param rightCommitment Optional vector commitment to right values.
   * @param children The list of children nodes.
   */
  public StemNode(
      final Optional<Bytes> location,
      final Bytes stem,
      final Optional<Bytes32> hash,
      final Optional<Bytes32> previousHash,
      final Optional<Bytes> commitment,
      final Optional<Bytes32> leftHash,
      final Optional<Bytes> leftCommitment,
      final Optional<Bytes32> rightHash,
      final Optional<Bytes> rightCommitment,
      final List<Node<V>> children) {
    super(location, hash, commitment, children);
    this.stem = extractStem(stem);
    this.leftHash = leftHash;
    this.leftCommitment = leftCommitment;
    this.rightHash = rightHash;
    this.rightCommitment = rightCommitment;
    this.previous = previousHash;
  }

  /**
   * Constructs a new BranchNode with non-optional parameters.
   *
   * @param location The location in the tree.
   * @param stem Node's stem.
   */
  public StemNode(final Bytes location, final Bytes stem) {
    super(location);
    for (int i = 0; i < maxChild(); i++) {
      NullNode<V> nullLeafNode = NullNode.newNullLeafNode();
      replaceChild((byte) i, nullLeafNode);
    }
    this.stem = extractStem(stem);
    this.leftHash = Optional.empty();
    this.leftCommitment = Optional.empty();
    this.rightHash = Optional.empty();
    this.rightCommitment = Optional.empty();
    this.previous = Optional.empty();
  }

  /**
   * Accepts a visitor for path-based operations on the node.
   *
   * @param visitor The path node visitor.
   * @param path The path associated with a node.
   * @return The result of the visitor's operation.
   */
  @Override
  public Node<V> accept(final PathNodeVisitor<V> visitor, final Bytes path) {
    return visitor.visit(this, path);
  }

  /**
   * Accepts a visitor for generic node operations.
   *
   * @param visitor The node visitor.
   * @return The result of the visitor's operation.
   */
  @Override
  public Node<V> accept(final NodeVisitor<V> visitor) {
    return visitor.visit(this);
  }

  /**
   * Get the stem.
   *
   * @return the stem.
   */
  public Bytes getStem() {
    return stem;
  }

  /**
   * Get Node's extension.
   *
   * @return the extension path.
   */
  public Optional<Bytes> getPathExtension() {
    return getLocation().map((loc) -> stem.slice(loc.size()));
  }

  /**
   * Get the leftHash.
   *
   * @return the leftHash.
   */
  public Optional<Bytes32> getLeftHash() {
    return leftHash;
  }

  /**
   * Get the stem.
   *
   * @return the stem.
   */
  public Optional<Bytes> getLeftCommitment() {
    return leftCommitment;
  }

  /**
   * Get the rightHash.
   *
   * @return the rightHash.
   */
  public Optional<Bytes32> getRightHash() {
    return rightHash;
  }

  /**
   * Get the stem.
   *
   * @return the stem.
   */
  public Optional<Bytes> getRightCommitment() {
    return rightCommitment;
  }

  /**
   * Replace node's Location
   *
   * @param newLocation The new location for the Node
   * @return The updated Node
   */
  @Override
  public StemNode<V> replaceLocation(Bytes newLocation) {
    List<Node<V>> newChildren = new ArrayList<>(maxChild());
    for (int i = 0; i < maxChild(); i++) {
      final Bytes index = Bytes.of(i);
      final Bytes childLocation = Bytes.concatenate(newLocation, index);
      final Node<V> vNode = child((byte) i).replaceLocation(childLocation);
      vNode.markDirty();
      newChildren.add(vNode);
    }
    return new StemNode<V>(
        Optional.of(newLocation),
        stem,
        hash,
        Optional.empty(),
        commitment,
        leftHash,
        leftCommitment,
        rightHash,
        rightCommitment,
        newChildren);
  }

  /**
   * Creates a new node by replacing all its commitments
   *
   * @param hash Node's vector commitment hash
   * @param leftHash Node's left vector commitment hash
   * @param rightHash Node's right vector commitment hash
   * @return StemNode with new commitments.
   */
  public StemNode<V> replaceHash(
      final Bytes32 hash,
      final Bytes commitment,
      final Bytes32 leftHash,
      final Bytes leftCommitment,
      final Bytes32 rightHash,
      final Bytes rightCommitment) {
    this.hash = Optional.ofNullable(hash);
    this.commitment = Optional.ofNullable(commitment);
    this.leftHash = Optional.ofNullable(leftHash);
    this.leftCommitment = Optional.ofNullable(leftCommitment);
    this.rightHash = Optional.ofNullable(rightHash);
    this.rightCommitment = Optional.ofNullable(rightCommitment);
    return this;
  }

  /**
   * Get the RLP-encoded value of the node.
   *
   * @return The RLP-encoded value.
   */
  @Override
  public Bytes getEncodedValue() {
    if (encodedValue.isPresent()) {
      return encodedValue.get();
    }
    BytesValueRLPOutput values = new BytesValueRLPOutput();
    values.startList();
    values.writeByte((byte) (getLocation().get().size() & 0xff));
    values.writeBytes(encodeCommitment(getCommitment().get()));
    values.writeBytes(encodeCommitment(getLeftCommitment().get()));
    values.writeBytes(encodeCommitment(getRightCommitment().get()));
    values.writeBytes(encodeScalar(getLeftHash().get()));
    values.writeBytes(encodeScalar(getRightHash().get()));
    values.writeList(getChildren(), (child, writer) -> writer.writeBytes(child.getEncodedValue()));
    values.endList();
    Bytes result = values.encoded();
    this.encodedValue = Optional.of(result);
    return result;
  }

  /**
   * Generates a string representation of the stem node and its children.
   *
   * @return A string representing the stem node and its children.
   */
  @Override
  public String print() {
    final StringBuilder builder = new StringBuilder();
    builder.append(String.format("Stem: %s", stem));
    for (int i = 0; i < maxChild(); i++) {
      final Node<V> child = child((byte) i);
      if (!(child instanceof NullNode)) {
        final String label = String.format("[%02x] ", i);
        final String childRep = child.print().replaceAll("\n  ", "\n    ");
        builder.append("\n  ").append(label).append(childRep);
      }
    }
    return builder.toString();
  }

  private Bytes extractStem(final Bytes stemValue) {
    return stemValue.slice(0, 31);
  }

  @Override
  public String toDot(Boolean showNullNodes) {
    StringBuilder result =
        new StringBuilder()
            .append(getName())
            .append(getLocation().orElse(Bytes.EMPTY))
            .append(" [label=\"S: ")
            .append(getLocation().orElse(Bytes.EMPTY))
            .append("\nStem: ")
            .append(getStem())
            .append("\nLeftCommitment: ")
            .append(getLeftCommitment().orElse(Bytes32.ZERO))
            .append("\nRightCommitment: ")
            .append(getRightCommitment().orElse(Bytes32.ZERO))
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

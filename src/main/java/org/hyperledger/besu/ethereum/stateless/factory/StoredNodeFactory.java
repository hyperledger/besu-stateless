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
package org.hyperledger.besu.ethereum.stateless.factory;

import org.hyperledger.besu.ethereum.rlp.BytesValueRLPInput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import org.hyperledger.besu.ethereum.stateless.node.InternalNode;
import org.hyperledger.besu.ethereum.stateless.node.LeafNode;
import org.hyperledger.besu.ethereum.stateless.node.Node;
import org.hyperledger.besu.ethereum.stateless.node.NullNode;
import org.hyperledger.besu.ethereum.stateless.node.StemNode;
import org.hyperledger.besu.ethereum.stateless.node.StoredInternalNode;
import org.hyperledger.besu.ethereum.stateless.node.StoredStemNode;
import org.hyperledger.besu.ethereum.trie.NodeLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes;

/** Node types that are saved to storage. */
enum NodeType {
  ROOT,
  INTERNAL,
  STEM,
  LEAF
}

/**
 * A factory for creating Verkle Trie nodes based on stored data.
 *
 * @param <V> The type of values stored in Verkle Trie nodes.
 */
public class StoredNodeFactory<V> implements NodeFactory<V> {
  private final NodeLoader nodeLoader;
  private final Function<Bytes, V> valueDeserializer;
  private final Boolean areCommitmentsCompressed;

  /**
   * Creates a new StoredNodeFactory with the given node loader and value deserializer.
   *
   * @param nodeLoader The loader for retrieving stored nodes.
   * @param valueDeserializer The function to deserialize values from Bytes.
   */
  public StoredNodeFactory(NodeLoader nodeLoader, Function<Bytes, V> valueDeserializer) {
    this.nodeLoader = nodeLoader;
    this.valueDeserializer = valueDeserializer;
    this.areCommitmentsCompressed = false;
  }

  /**
   * Creates a new StoredNodeFactory with the given node loader and value deserializer.
   *
   * @param nodeLoader The loader for retrieving stored nodes.
   * @param valueDeserializer The function to deserialize values from Bytes.
   * @param areCommitmentsCompressed Are commitments stored compressed (32bytes).
   */
  public StoredNodeFactory(
      NodeLoader nodeLoader,
      Function<Bytes, V> valueDeserializer,
      Boolean areCommitmentsCompressed) {
    this.nodeLoader = nodeLoader;
    this.valueDeserializer = valueDeserializer;
    this.areCommitmentsCompressed = areCommitmentsCompressed;
  }

  /**
   * Retrieves a Verkle Trie node from stored data based on the location and hash.
   *
   * @param location Node's location
   * @param hash Node's hash
   * @return An optional containing the retrieved node, or an empty optional if the node is not
   *     found.
   */
  @Override
  public Optional<Node<V>> retrieve(final Bytes location, final Bytes32 hash) {
    /*
     * Currently, Root and Leaf are distinguishable by location.
     * To distinguish internal from stem, we further need values.
     * Currently, they are distinguished by values length.
     */
    Optional<Node<V>> result;
    Optional<Bytes> maybeEncodedValues = nodeLoader.getNode(location, hash);
    if (maybeEncodedValues.isEmpty()) {
      return Optional.empty();
    }
    Bytes encodedValues = maybeEncodedValues.get();

    if (location.size() == 0) {
      result = Optional.of(decodeRootNode(encodedValues));
    } else if (location.size() > 0 && location.size() < 31) {
      result = Optional.of(decodeInternalNode(location, encodedValues, hash));
    } else if (location.size() == 31) {
      result = Optional.of(decodeStemNode(location, encodedValues, hash));
    } else {
      result = Optional.empty();
    }
    return result;
  }

  private Bytes decodeCommitment(Bytes commitment) {
    if (areCommitmentsCompressed && !commitment.isEmpty()) {
      // TODO: uncompress commitment
    }
    if (commitment.isEmpty()) {
      commitment = Node.EMPTY_COMMITMENT;
    }
    MutableBytes comm = MutableBytes.create(64);
    comm.set(0, commitment);
    return (Bytes) comm;
  }

  /**
   * Creates a rootNode using the provided location, hash, and path.
   *
   * @param encodedValues List of Bytes values retrieved from storage.
   * @return A internalNode instance.
   */
  InternalNode<V> decodeRootNode(Bytes encodedValues) {
    RLPInput input = new BytesValueRLPInput(encodedValues, false);
    input.enterList();
    Bytes32 hash = Bytes32.rightPad(input.readBytes());
    Bytes commitment = decodeCommitment(input.readBytes());
    List<Bytes> stemExtensions = input.readList(in -> in.readBytes());
    List<Bytes32> scalars = input.readList(in -> Bytes32.rightPad(in.readBytes()));
    input.leaveList();
    return createInternalNode(Bytes.EMPTY, hash, commitment, stemExtensions, scalars);
  }

  /**
   * Creates a internalNode using the provided location, hash, and path.
   *
   * @param location The location of the internalNode.
   * @param encodedValues List of Bytes values retrieved from storage.
   * @param hash Node's hash value.
   * @return A internalNode instance.
   */
  InternalNode<V> decodeInternalNode(Bytes location, Bytes encodedValues, Bytes32 hash) {
    RLPInput input = new BytesValueRLPInput(encodedValues, false);
    input.enterList();
    Bytes commitment = decodeCommitment(input.readBytes());
    List<Bytes> stemExtensions = input.readList(in -> in.readBytes());
    List<Bytes32> scalars = input.readList(in -> Bytes32.rightPad(in.readBytes()));
    input.leaveList();
    return createInternalNode(location, hash, commitment, stemExtensions, scalars);
  }

  private InternalNode<V> createInternalNode(
      Bytes location,
      Bytes32 hash,
      Bytes commitment,
      List<Bytes> stemExtensions,
      List<Bytes32> scalars) {
    Map<Byte, Bytes> indices = new HashMap<>();
    for (Bytes extension : stemExtensions) {
      indices.put(extension.get(0), extension);
    }
    int nChild = InternalNode.maxChild();
    List<Node<V>> children = new ArrayList<>(nChild);
    for (int i = 0; i < nChild; i++) {
      if (scalars.get(i).compareTo(Bytes32.ZERO) == 0) {
        children.add(NullNode.nullNode());
      } else {
        if (indices.containsKey((byte) i)) {
          children.add(
              new StoredStemNode<V>(
                  this,
                  Bytes.concatenate(location, Bytes.of(i)),
                  Bytes.concatenate(location, indices.get((byte) i)),
                  scalars.get(i)));
        } else {
          children.add(
              new StoredInternalNode<V>(
                  this, Bytes.concatenate(location, Bytes.of(i)), scalars.get(i)));
        }
      }
    }
    return new InternalNode<V>(location, hash, commitment, children);
  }

  /**
   * Creates a StemNode using the provided stem, hash and encodedValues
   *
   * @param stem The stem of the BranchNode.
   * @param encodedValues List of Bytes values retrieved from storage.
   * @param hash Node's hash value.
   * @return A BranchNode instance.
   */
  StemNode<V> decodeStemNode(Bytes stem, Bytes encodedValues, Bytes32 hash) {
    RLPInput input = new BytesValueRLPInput(encodedValues, false);
    input.enterList();

    int depth = input.readByte();
    Bytes commitment = decodeCommitment(input.readBytes());
    Bytes leftCommitment = decodeCommitment(input.readBytes());
    Bytes rightCommitment = decodeCommitment(input.readBytes());
    Bytes32 leftScalar = Bytes32.rightPad(input.readBytes());
    Bytes32 rightScalar = Bytes32.rightPad(input.readBytes());
    List<Bytes> values = input.readList(in -> in.readBytes());

    // create StemNode
    final Bytes location = stem.slice(0, depth);
    final int nChild = StemNode.maxChild();
    List<Node<V>> children = new ArrayList<>(nChild);
    for (int i = 0; i < nChild; i++) {
      if (values.get(i) == Bytes.EMPTY) {
        children.add(NullNode.newNullLeafNode());
      } else {
        children.add(
            createLeafNode(
                Bytes.concatenate(location, Bytes.of(i)), Bytes32.rightPad(values.get(i))));
      }
    }
    return new StemNode<V>(
        location,
        stem,
        hash,
        commitment,
        leftScalar,
        leftCommitment,
        rightScalar,
        rightCommitment,
        children);
  }

  /**
   * Creates a LeafNode using the provided location, path, and value.
   *
   * @param key The key of the LeafNode.
   * @param encodedValue Leaf value retrieved from storage.
   * @return A LeafNode instance.
   */
  LeafNode<V> createLeafNode(Bytes key, Bytes encodedValue) {
    V value = valueDeserializer.apply(encodedValue);
    return new LeafNode<V>(Optional.of(key), value);
  }
}

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
package org.hyperledger.besu.ethereum.stateless;

import static org.hyperledger.besu.ethereum.stateless.node.Node.getHighValue;
import static org.hyperledger.besu.ethereum.stateless.node.Node.getLowValue;

import org.hyperledger.besu.ethereum.stateless.hasher.TrieCommitmentHasher;
import org.hyperledger.besu.ethereum.stateless.node.BranchNode;
import org.hyperledger.besu.ethereum.stateless.node.InternalNode;
import org.hyperledger.besu.ethereum.stateless.node.LeafNode;
import org.hyperledger.besu.ethereum.stateless.node.Node;
import org.hyperledger.besu.ethereum.stateless.node.NullNode;
import org.hyperledger.besu.ethereum.stateless.node.StemNode;
import org.hyperledger.besu.ethereum.stateless.node.StoredNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * Processes batches of trie nodes for efficient hashing.
 *
 * <p>This class manages the batching and hashing of trie nodes to optimize performance.
 */
public class VerkleTrieBatchHasher {

  private static final Logger LOG = LogManager.getLogger(VerkleTrieBatchHasher.class);

  private static final ExecutorService executor =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
  private static final int MAX_BATCH_SIZE = 2000; // Maximum number of nodes in a batch
  private static final Bytes[] EMPTY_ARRAY_TEMPLATE = new Bytes[0];
  private final TrieCommitmentHasher hasher = new TrieCommitmentHasher(); // Hasher for node hashing
  private final Map<Bytes, Node<?>> updatedNodes =
      new HashMap<>(); // Map to hold nodes for batching

  /**
   * Adds a node for future batching. If the node is a NullNode or NullLeafNode and the location is
   * not empty, it removes the node from the batch.
   *
   * @param maybeLocation The location of the node.
   * @param node The node to add.
   */
  public void addNodeToBatch(final Optional<Bytes> maybeLocation, final Node<?> node) {
    maybeLocation.ifPresent(
        location -> {
          if (node instanceof NullNode<?> && !location.isEmpty()) {
            updatedNodes.remove(location);
          } else {
            updatedNodes.put(location, node);
          }
        });
  }

  /**
   * Returns the map of nodes currently added for future batching.
   *
   * @return Map of nodes to be batched.
   */
  public Map<Bytes, Node<?>> getNodesToBatch() {
    return updatedNodes;
  }

  /**
   * Processes the nodes in batches. Sorts the nodes by their location and hashes them in batches.
   * Clears the batch after processing.
   */
  public void calculateStateRoot() {
    if (updatedNodes.isEmpty()) {
      return;
    }

    final List<Map.Entry<Bytes, Node<?>>> sortedNodesByLocation =
        new ArrayList<>(updatedNodes.entrySet());
    sortedNodesByLocation.sort(
        (entry1, entry2) -> Integer.compare(entry2.getKey().size(), entry1.getKey().size()));

    int currentDepth = -1; // Tracks the depth of the current batch

    final List<Node<?>> nodesInSameLevel = new ArrayList<>();
    for (Map.Entry<Bytes, Node<?>> entry : sortedNodesByLocation) {
      final Bytes location = entry.getKey();
      final Node<?> node = entry.getValue();
      if (node instanceof BranchNode<?>) {
        if (location.size() != currentDepth || nodesInSameLevel.size() > MAX_BATCH_SIZE) {
          if (!nodesInSameLevel.isEmpty()) {
            processBatch(nodesInSameLevel);
            nodesInSameLevel.clear();
          }
          if (location.isEmpty()) {
            // We will end up updating the root node. Once all the batching is finished,
            // we will update the previous states of the nodes by setting them to the new
            // ones.
            calculateRootInternalNodeHash((InternalNode<?>) node);
            updatedNodes.forEach(
                (__, n) -> {
                  if (n instanceof BranchNode<?>) {
                    n.setPrevious(n.getHash());
                  } else if (n instanceof LeafNode<?>) {
                    n.setPrevious(n.getValue());
                  }
                  n.markClean();
                });
            updatedNodes.clear();
            return;
          }
          currentDepth = location.size();
        }
        if (node.isDirty() || node.getHash().isEmpty() || node.getCommitment().isEmpty()) {
          nodesInSameLevel.add(node);
        }
      }
    }

    throw new IllegalStateException("root node not found");
  }

  private void processBatch(List<Node<?>> nodes) {
    LOG.atTrace().log("Start hashing {} batch of nodes", nodes.size());

    try {
      LOG.atTrace().log("Creating commitments for stem nodes and internal nodes");
      // Phase 1: submit and collect initial commitments
      List<Future<Bytes>> initialCommitFutures = new ArrayList<>();
      for (Node<?> node : nodes) {
        if (node instanceof StemNode<?> stem) {
          initialCommitFutures.add(executor.submit(() -> getStemNodeLeftCommitment(stem)));
          initialCommitFutures.add(executor.submit(() -> getStemNodeRightCommitment(stem)));
        } else if (node instanceof InternalNode<?> internal) {
          initialCommitFutures.add(executor.submit(() -> getInternalNodeCommitment(internal)));
        }
      }
      List<Bytes> initialCommitments = new ArrayList<>(initialCommitFutures.size());
      for (Future<Bytes> future : initialCommitFutures) {
        initialCommitments.add(future.get());
      }

      LOG.atTrace()
          .log(
              "Executing batch hashing for {} commitments of stem (left/right) and internal nodes.",
              initialCommitments.size());
      // Compute first batch of hashes
      Iterator<Bytes32> hashIterator =
          hasher.hashMany(initialCommitments.toArray(EMPTY_ARRAY_TEMPLATE)).iterator();
      Iterator<Bytes> commitIterator = initialCommitments.iterator();

      // Phase 2: prepare stem nodes and collect stem commitments
      LOG.atTrace()
          .log("Creating commitments for stem nodes and refreshing hashes of internal nodes");
      List<Future<Bytes>> stemCommitFutures = new ArrayList<>();
      for (Node<?> node : nodes) {
        if (node instanceof StemNode<?> stem) {
          Bytes32[] hashes =
              new Bytes32[] {
                Bytes32.rightPad(Bytes.of(1)), // extension marker
                Bytes32.rightPad(stem.getStem()), // stem value
                hashIterator.next(),
                hashIterator.next() // child hashes
              };
          stem.replaceHash(
              null, null, hashes[2], commitIterator.next(), hashes[3], commitIterator.next());
          stemCommitFutures.add(executor.submit(() -> hasher.commit(hashes)));
        } else if (node instanceof InternalNode<?> internal) {
          internal.replaceHash(hashIterator.next(), commitIterator.next());
        }
      }
      List<Bytes> stemCommitments = new ArrayList<>(stemCommitFutures.size());
      for (Future<Bytes> future : stemCommitFutures) {
        stemCommitments.add(future.get());
      }

      LOG.atTrace()
          .log("Executing batch hashing for {} commitments of stem nodes.", stemCommitments.size());
      // Finalize stem hashes
      Iterator<Bytes32> finalHashIterator =
          hasher.hashMany(stemCommitments.toArray(EMPTY_ARRAY_TEMPLATE)).iterator();
      Iterator<Bytes> finalCommitIterator = stemCommitments.iterator();
      for (Node<?> node : nodes) {
        if (node instanceof StemNode<?> stem) {
          stem.replaceHash(
              finalHashIterator.next(), finalCommitIterator.next(),
              stem.getLeftHash().orElseThrow(), stem.getLeftCommitment().orElseThrow(),
              stem.getRightHash().orElseThrow(), stem.getRightCommitment().orElseThrow());
        }
      }
      LOG.atTrace().log("Finished refreshing hashes of stem nodes");

    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  private void calculateRootInternalNodeHash(final InternalNode<?> internalNode) {
    final Bytes commitment = getRootNodeCommitments(internalNode).get(0);
    final Bytes32 hash = hasher.compress(commitment);
    internalNode.replaceHash(hash, commitment);
  }

  private Bytes getStemNodeLeftCommitment(StemNode<?> stemNode) {
    int halfSize = StemNode.maxChild() / 2;

    List<Byte> indices = new ArrayList<>();
    List<Bytes> oldValues = new ArrayList<>();
    List<Bytes> newValues = new ArrayList<>();

    for (int idx = 0; idx < halfSize; idx++) {
      Node<?> node = stemNode.child((byte) idx);
      Optional<Bytes> oldValue = node.getPrevious().map(Bytes.class::cast);

      if (!(node instanceof StoredNode<?>) && (oldValue.isEmpty() || node.isDirty())) {
        indices.add((byte) (2 * idx));
        indices.add((byte) (2 * idx + 1));
        oldValues.add(getLowValue(oldValue));
        oldValues.add(getHighValue(oldValue));
        newValues.add(getLowValue(node.getValue()));
        newValues.add(getHighValue(node.getValue()));
      }
    }

    if (indices.isEmpty()) {
      return stemNode.getLeftCommitment().get();
    } else {
      return hasher.commitPartialUpdate(
          stemNode.getLeftCommitment(), indices, oldValues, newValues);
    }
  }

  private Bytes getStemNodeRightCommitment(StemNode<?> stemNode) {
    int size = StemNode.maxChild();
    int halfSize = size / 2;

    List<Byte> indices = new ArrayList<>();
    List<Bytes> oldValues = new ArrayList<>();
    List<Bytes> newValues = new ArrayList<>();

    for (int idx = halfSize; idx < size; idx++) {
      Node<?> node = stemNode.child((byte) idx);
      Optional<Bytes> oldValue = node.getPrevious().map(Bytes.class::cast);

      if (!(node instanceof StoredNode<?>) && (oldValue.isEmpty() || node.isDirty())) {
        indices.add((byte) (2 * idx));
        indices.add((byte) (2 * idx + 1));
        oldValues.add(getLowValue(oldValue));
        oldValues.add(getHighValue(oldValue));
        newValues.add(getLowValue(node.getValue()));
        newValues.add(getHighValue(node.getValue()));
      }
    }

    if (indices.isEmpty()) {
      return stemNode.getRightCommitment().get();
    } else {
      return hasher.commitPartialUpdate(
          stemNode.getRightCommitment(), indices, oldValues, newValues);
    }
  }

  private Bytes getInternalNodeCommitment(InternalNode<?> internalNode) {
    int size = InternalNode.maxChild();

    final List<Byte> indices = new ArrayList<>();
    final List<Bytes> oldValues = new ArrayList<>();
    final List<Bytes> newValues = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      final Node<?> node = internalNode.child((byte) i);
      Optional<Bytes> oldValue = node.getPrevious().map(Bytes.class::cast);
      // We should not recalculate a node if it is persisted and has not undergone an
      // update since
      // its last save.
      // If a child does not have a previous value, it means that it is a new node and
      // we must
      // therefore recalculate it.
      if (!(node instanceof StoredNode<?>) && (oldValue.isEmpty() || node.isDirty())) {
        indices.add((byte) i);
        oldValues.add(oldValue.orElse(Bytes.EMPTY));
        newValues.add(node.getHash().get());
      }
    }
    return hasher.commitPartialUpdate(internalNode.getCommitment(), indices, oldValues, newValues);
  }

  private List<Bytes> getRootNodeCommitments(InternalNode<?> internalNode) {
    int size = InternalNode.maxChild();
    final List<Bytes> commitmentsHashes = new ArrayList<>();
    final List<Bytes32> newValues = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      final Node<?> node = internalNode.child((byte) i);
      newValues.add(node.getHash().get());
    }
    commitmentsHashes.add(hasher.commit(newValues.toArray(new Bytes32[] {})));
    return commitmentsHashes;
  }
}

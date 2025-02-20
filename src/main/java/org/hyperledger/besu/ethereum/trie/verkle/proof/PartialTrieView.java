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
package org.hyperledger.besu.ethereum.trie.verkle.proof;

import org.hyperledger.besu.ethereum.trie.verkle.node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class PartialTrieView {
  private final Map<Bytes, NodeView> nodeViews;
  private final Map<Bytes, StemView> stemViews;

  public PartialTrieView(Map<Bytes, NodeView> nodeViews, Map<Bytes, StemView> stemViews) {
    this.nodeViews = nodeViews;
    this.stemViews = stemViews;
  }

  public Map<Bytes, NodeView> getNodeViews() {
    return nodeViews;
  }

  public Map<Bytes, StemView> getStemViews() {
    return stemViews;
  }

  public static PartialTrieView fromExecutionWitness(ExecutionWitness executionWitness)
      throws IllegalArgumentException {
    Map<Bytes, NodeView> nodeViews = new HashMap<>();
    Map<Bytes, StemView> stemViews = new HashMap<>();
    Iterator<Bytes32> iterCommitments =
        executionWitness.verificationHint().commitments().iterator();
    Iterator<Bytes> iterOtherStems = executionWitness.verificationHint().otherStems().iterator();

    // Process root Node
    Bytes stem;
    Bytes path = Bytes.EMPTY;
    Bytes32 commitment = executionWitness.previousStateRoot();
    nodeViews.put(path, NodeView.fromCommitment(commitment));

    // Process Stems
    for (ExecutionWitness.StemInfo stemInfo : executionWitness.stemInfos()) {
      // Process Internal Nodes
      for (int i = 1; i < stemInfo.depth(); i++) {
        path = stemInfo.stem().slice(0, i);
        NodeView current = nodeViews.get(path);
        if (current == null) {
          if (!iterCommitments.hasNext()) {
            throw new IllegalArgumentException("Not enough Commitments");
          }
          commitment = iterCommitments.next();
          nodeViews.put(path, NodeView.fromCommitment(commitment));
        }
      }

      // Process Last Node
      // 0: Internal Node with Emtpy value
      // 1: Stem Node with different stem
      // 2. Stem Node with value
      path = stemInfo.stem().slice(0, stemInfo.depth());
      switch (stemInfo.extensionType()) {
        case 0: // Empty Internal Node
          nodeViews.put(path, NodeView.fromCommitment(Node.EMPTY_COMMITMENT_COMPRESSED));
          break;
        case 1: // Other Stem
          if (!iterCommitments.hasNext()) {
            throw new IllegalArgumentException("Not enough Commitments");
          }
          commitment = iterCommitments.next();
          if (!iterOtherStems.hasNext()) {
            throw new IllegalArgumentException("Not enough OtherStems");
          }
          stem = iterOtherStems.next();
          if (stem.commonPrefix(path) != path) {
            throw new IllegalArgumentException("OtherStem does not match path prefix");
          }
          StemView stemView = stemViews.get(stem);
          if (stemView == null) {
            stemViews.put(stem, StemView.fromEmpty());
          }
          break;
        case 2: // Stem is Present
          stem = stemInfo.stem();
          if (!iterCommitments.hasNext()) {
            throw new IllegalArgumentException("Not enough Commitments");
          }
          commitment = iterCommitments.next();
          nodeViews.put(path, new NodeView(commitment, stem));

          List<ExecutionWitness.SuffixDiff> diffs = stemInfo.suffixDiffs();
          Optional<Bytes32> leftCommitment = Optional.empty();
          Optional<Bytes32> rightCommitment = Optional.empty();
          if (diffs.get(0).suffix() < 128) {
            if (!iterCommitments.hasNext()) {
              throw new IllegalArgumentException(); // Missing commitments to fill the tree
            }
            leftCommitment = Optional.of(iterCommitments.next());
          }
          if (diffs.getLast().suffix() >= 128) {
            if (!iterCommitments.hasNext()) {
              throw new IllegalArgumentException(); // Missing commitments to fill the tree
            }
            rightCommitment = Optional.of(iterCommitments.next());
          }
          stemViews.put(stem, new StemView(leftCommitment, rightCommitment, diffs));
          break;
        default:
          throw new IllegalArgumentException("Illegal ExtensionType");
      }
    } // End for each stemInfo

    if (iterCommitments.hasNext()) {
      throw new IllegalArgumentException("Too many commitments");
    }
    if (iterOtherStems.hasNext()) {
      throw new IllegalArgumentException("Too many otherStems");
    }
    return new PartialTrieView(nodeViews, stemViews);
  }

  public record NodeView(Bytes32 commitment, Bytes stem) {
    public static NodeView fromCommitment(Bytes32 commitment) {
      return new NodeView(commitment, Bytes.EMPTY);
    }
  }
  ;

  public record StemView(
      Optional<Bytes32> leftCommiment,
      Optional<Bytes32> rightCommitment,
      List<ExecutionWitness.SuffixDiff> suffixDiffs) {
    public static StemView fromEmpty() {
      return new StemView(
          Optional.empty(), Optional.empty(), new ArrayList<ExecutionWitness.SuffixDiff>());
    }
  }
  ;
}

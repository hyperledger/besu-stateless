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

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public record ExecutionWitness(
    Bytes32 previousStateRoot,
    List<StemInfo> stemInfos,
    VerificationHint verificationHint,
    IPAMultiProof proof) {

  public record SuffixDiff(int suffix, Bytes value) {}
  ;

  public record StemInfo(Bytes stem, int depth, int extensionType, List<SuffixDiff> suffixDiffs) {}
  ;

  public record VerificationHint(List<Bytes32> commitments, List<Bytes> otherStems) {}
  ;

  public record IPAMultiProof(
      Bytes32 multiCommitment,
      List<Bytes32> leftCommitments,
      List<Bytes32> rightCommitments,
      Bytes32 finalEvaluation) {

    public int getIPADepth() {
      return leftCommitments.size();
    }

    public Bytes toBytes() {
      List<Bytes> allBytes = new ArrayList<Bytes>(getIPADepth() * 2 + 1);
      allBytes.addAll(leftCommitments);
      allBytes.addAll(rightCommitments);
      allBytes.add(finalEvaluation);
      return Bytes.concatenate(allBytes);
    }
  }
  ;

  // public static ExecutionWitness fromParameters(ExecutionWitnessParameters
  // params) {
  // Logic to validate an executionWitness from json parameters.
  // }
}

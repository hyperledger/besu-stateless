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
package org.hyperledger.besu.ethereum.stateless.proof;

import java.util.List;
import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import verkle.cryptography.LibIpaMultipoint;

/**
 * This class validates the Verkle proof against the specified pre-state root.
 *
 * <p>This method checks if the provided execution witness data correctly corresponds to the given
 * pre-state root.
 */
public class ProofVerifier {

  public boolean verifyVerkleProof(
      final List<Bytes> keys,
      final List<Bytes> currentValues,
      final List<Bytes32> commitmentsByPath,
      final List<Bytes32> cl,
      final List<Bytes32> cr,
      final List<Bytes> otherStems,
      final Bytes d,
      final Bytes depthsExtensionPresentStems,
      final Bytes finalEvaluation,
      final Bytes prestateRoot) {
    return LibIpaMultipoint.verifyPreStateRoot(
        toArray(keys),
        toArray(currentValues, unused -> Bytes.EMPTY.toArrayUnsafe()),
        toArray(commitmentsByPath),
        toArray(cl),
        toArray(cr),
        toArray(otherStems),
        d.toArrayUnsafe(),
        depthsExtensionPresentStems.toArrayUnsafe(),
        finalEvaluation.toArrayUnsafe(),
        prestateRoot.toArrayUnsafe());
  }

  private <T extends Bytes> byte[][] toArray(final List<T> elts) {
    return toArray(elts, unused -> null);
  }

  private <T extends Bytes> byte[][] toArray(
      final List<T> elts, final Function<Void, byte[]> defaultValue) {
    final byte[][] elements = new byte[elts.size()][];
    for (int i = 0; i < elts.size(); i++) {
      elements[i] =
          ((elts.get(i) == null) ? defaultValue.apply(null) : elts.get(i).toArrayUnsafe());
    }
    return elements;
  }
}

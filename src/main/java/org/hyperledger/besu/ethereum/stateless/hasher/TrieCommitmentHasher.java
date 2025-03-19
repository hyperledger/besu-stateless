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
package org.hyperledger.besu.ethereum.stateless.hasher;

import org.hyperledger.besu.ethereum.stateless.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import verkle.cryptography.LibIpaMultipoint;

/**
 * This class provides methods to commit to vectors of values, perform partial updates on
 * commitments, compress commitments, and convert them to scalars. It serves as a utility for
 * cryptographic operations in the context of Verkle trie data structures.
 */
public class TrieCommitmentHasher implements Hasher {

  /**
   * Commits to a vector of values.
   *
   * @param inputs an array of {@link Bytes32} representing serialized scalars to commit to.
   * @return a {@link Bytes} object representing the uncompressed serialized commitment.
   */
  public Bytes commit(Bytes32[] inputs) {
    return Bytes.wrap(LibIpaMultipoint.commit(Bytes.concatenate(inputs).toArray()));
  }

  /**
   * Commits to the root node of a trie.
   *
   * @param inputs an array of {@link Bytes} inputs to commit.
   * @return a {@link Bytes32} object representing the compressed serialized commitment (32 bytes).
   */
  public Bytes32 commitRoot(final Bytes[] inputs) {
    return Bytes32.wrap(LibIpaMultipoint.commitAsCompressed(Bytes.concatenate(inputs).toArray()));
  }

  /**
   * Commits to a partially updated vector of values.
   *
   * <p>This method updates an existing commitment with new scalar values, optionally using a prior
   * commitment.
   *
   * @param commitment an {@link Optional} containing an existing commitment, or empty for a new
   *     one.
   * @param indices a {@link List} of indices representing the positions to update.
   * @param oldScalars a {@link List} of {@link Bytes} representing old scalar values.
   * @param newScalars a {@link List} of {@link Bytes} representing new scalar values.
   * @return a {@link Bytes} object representing the updated uncompressed serialized commitment.
   */
  public Bytes commitPartialUpdate(
      final Optional<Bytes> commitment,
      final List<Byte> indices,
      final List<Bytes> oldScalars,
      final List<Bytes> newScalars) {
    final Bytes cmnt = commitment.orElse(Node.EMPTY_COMMITMENT);
    final byte[] idx = new byte[indices.size()];
    for (int i = 0; i < indices.size(); i++) {
      idx[i] = indices.get(i);
    }
    return Bytes.wrap(
        LibIpaMultipoint.updateSparse(
            cmnt.toArray(),
            idx,
            prepareScalars(oldScalars.toArray(new Bytes[0])).toArray(),
            prepareScalars(newScalars.toArray(new Bytes[0])).toArray()));
  }

  /**
   * Convert a commitment to its serialized compressed form.
   *
   * @param commitment Uncompressed serialized commitment.
   * @return Serialized scalar (32 bytes).
   */
  public Bytes32 compress(final Bytes commitment) {
    return Bytes32.wrap(LibIpaMultipoint.compress(commitment.toArray()));
  }

  /**
   * Convert a commitment to its corresponding scalar.
   *
   * @param commitment Uncompressed serialized commitment.
   * @return Serialized scalar (32 bytes).
   */
  public Bytes32 hash(final Bytes commitment) {
    return Bytes32.wrap(LibIpaMultipoint.hashMany(commitment.toArray()));
  }

  /**
   * Map a vector of commitments to their corresponding scalars.
   *
   * <p>The vectorized version is highly optimized, making use of Montgomery's batch inversion
   * trick.
   *
   * @param commitments Uncompressed serialized commitments.
   * @return Serialized scalars (32 bytes each).
   */
  public List<Bytes32> hashMany(final Bytes[] commitments) {
    final Bytes hashMany =
        Bytes.wrap(LibIpaMultipoint.hashMany(Bytes.concatenate(commitments).toArray()));
    final List<Bytes32> hashes = new ArrayList<>();
    for (int i = 0; i < commitments.length; i++) {
      hashes.add(Bytes32.wrap(hashMany.slice(i * Bytes32.SIZE, Bytes32.SIZE)));
    }
    return hashes;
  }
}

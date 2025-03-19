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

import org.hyperledger.besu.ethereum.stateless.hasher.cache.CacheStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import verkle.cryptography.LibIpaMultipoint;

/**
 * StemHasher is responsible for computing stems. It utilizes caching strategies for stems and
 * address commitments to optimize performance and reduce redundant computations.
 */
public class StemHasher implements Hasher {

  private static final int CHUNK_SIZE = 16;

  /** The fixed size of the stem, defined as 31 bytes. */
  private static final int STEM_SIZE = 31;

  /** Default array used when no scalars are provided. */
  private static final byte[] EMPTY_SCALARS_BUFFER = new byte[Bytes32.SIZE * 2];

  private final CacheStrategy<Bytes32, Bytes> stemCache;
  private final CacheStrategy<Bytes, Bytes> addressCommitmentCache;

  /**
   * Constructs a StemHasher with caching strategies for stems and address commitments.
   *
   * @param stemCache The caching strategy for stems.
   * @param addressCommitmentCache The caching strategy for address commitments.
   */
  public StemHasher(
      CacheStrategy<Bytes32, Bytes> stemCache, CacheStrategy<Bytes, Bytes> addressCommitmentCache) {
    this.stemCache = stemCache;
    this.addressCommitmentCache = addressCommitmentCache;
  }

  /**
   * Computes the stem for a specific address and storage index.
   *
   * @param address The address from which the stem is derived.
   * @param index The storage index used in the computation.
   * @return The computed stem as a {@link Bytes} object.
   */
  public Bytes computeStem(final Bytes address, final Bytes32 index) {
    // Compute the commitment for the provided address
    final Bytes addressCommitment = computeAddressCommitment(address);
    // Retrieve the stem from cache or compute it if missing
    return stemCache.get(index, key -> computeHashes(addressCommitment, List.of(key)).get(key));
  }

  /**
   * Computes stems for a specific address and a list of storage indexes.
   *
   * <p>This method utilizes caching to avoid redundant computations and dynamically generates
   * missing stems for indexes that are not already cached.
   *
   * @param address The address to compute stems for.
   * @param trieKeyIndexes A list of storage indexes for which stems need to be computed.
   * @return A map associating each index with its corresponding stem.
   */
  public Map<Bytes32, Bytes> manyStems(final Bytes address, final List<Bytes32> trieKeyIndexes) {
    // Compute the address commitment
    final Bytes addressCommitment = computeAddressCommitment(address);
    final Map<Bytes32, Bytes> result = new HashMap<>();
    final List<Bytes32> missingKeys = new ArrayList<>();

    // Check cache for existing stems and identify missing keys
    trieKeyIndexes.forEach(
        key -> {
          Bytes value = stemCache.getIfPresent(key);
          if (value != null) {
            result.put(key, value);
          } else {
            missingKeys.add(key);
          }
        });

    // Generate missing stems if any keys are missing
    if (!missingKeys.isEmpty()) {
      final Map<Bytes32, Bytes> generatedStems = computeHashes(addressCommitment, missingKeys);
      result.putAll(generatedStems);
      stemCache.putAll(generatedStems);
    }

    return result;
  }

  /**
   * Computes the address commitment for a specific address, leveraging a caching strategy.
   *
   * @param address The address for which the commitment is computed.
   * @return The address commitment as a {@link Bytes} object.
   */
  private Bytes computeAddressCommitment(final Bytes address) {
    return addressCommitmentCache.get(address, this::computeAddressCommitmentInternal);
  }

  /**
   * Performs the internal computation of the address commitment for a given address.
   *
   * @param address The address to process.
   * @return The computed address commitment as a {@link Bytes} object.
   */
  private Bytes computeAddressCommitmentInternal(final Bytes address) {
    Bytes32 paddedAddress = Bytes32.leftPad(address);
    Bytes32[] chunks = generatePartialAddressChunks(paddedAddress);
    return Bytes.wrap(LibIpaMultipoint.commit(Bytes.concatenate(chunks).toArray()));
  }

  /**
   * Generalized method to compute hashes for one or more storage indexes.
   *
   * @param addressCommitment The address commitment to use in the hash computation.
   * @param indexes A list of storage indexes to compute hashes for.
   * @return A map associating each index with its computed hash.
   */
  private Map<Bytes32, Bytes> computeHashes(
      final Bytes addressCommitment, final List<Bytes32> indexes) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      for (Bytes32 index : indexes) {
        // Generate chunks for each index
        Bytes[] indexChunks = generatePartialIndexChunks(index);
        outputStream.writeBytes(
            LibIpaMultipoint.updateSparse(
                addressCommitment.toArrayUnsafe(),
                new byte[] {3, 4}, // Index low + index high
                EMPTY_SCALARS_BUFFER,
                prepareScalars(indexChunks).toArrayUnsafe()));
      }
      // Hash all accumulated data in the output stream
      Bytes hashedData = Bytes.wrap(LibIpaMultipoint.hashMany(outputStream.toByteArray()));
      Map<Bytes32, Bytes> stems = new HashMap<>();
      // Extract individual stems from the hashed data
      for (int i = 0; i < indexes.size(); i++) {
        stems.put(indexes.get(i), hashedData.slice(i * Bytes32.SIZE, STEM_SIZE));
      }
      return stems;
    } catch (IOException e) {
      throw new RuntimeException("Failed to compute trie-key hashes", e);
    }
  }

  /**
   * Generates partial chunks for an address padded to 32 bytes.
   *
   * @param address The address to split into chunks.
   * @return An array of partial chunks as {@link Bytes32}.
   */
  private Bytes32[] generatePartialAddressChunks(final Bytes address) {
    // Pad the address so that it is 32 bytes
    final Bytes32 paddedAddress = Bytes32.leftPad(address);
    return new Bytes32[] {
      Bytes32.rightPad(Bytes.of(2, 64)), // Set the first chunk which is always 2 + 256 * 64
      // Slice input into 16 byte segments
      // Pad each chunk to make it 32 bytes
      Bytes32.rightPad(paddedAddress.slice(0, CHUNK_SIZE)),
      Bytes32.rightPad(paddedAddress.slice(CHUNK_SIZE, CHUNK_SIZE))
    };
  }

  /**
   * Generates partial chunks for a given index, converted to little-endian format.
   *
   * @param index The index to split into chunks.
   * @return An array of partial chunks as {@link Bytes32}.
   */
  private Bytes32[] generatePartialIndexChunks(final Bytes32 index) {
    // Reverse the index so that it is in little endian format
    Bytes32 indexLE = Bytes32.wrap(index.reverse());
    return new Bytes32[] {
      Bytes32.rightPad(indexLE.slice(0, CHUNK_SIZE)), // Index low
      Bytes32.rightPad(indexLE.slice(CHUNK_SIZE, CHUNK_SIZE)) // Index high
    };
  }
}

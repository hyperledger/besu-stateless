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
package org.hyperledger.besu.ethereum.stateless.adapter;

import static org.hyperledger.besu.ethereum.stateless.util.Parameters.BASIC_DATA_LEAF_KEY;
import static org.hyperledger.besu.ethereum.stateless.util.Parameters.CODE_HASH_LEAF_KEY;

import org.hyperledger.besu.ethereum.stateless.hasher.StemHasher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

/**
 * This class provides methods for generating trie keys in the Verkle Trie structure, specifically
 * for operations requiring a StemHasher instance.
 */
public class TrieKeyFactory {

  private final StemHasher hasher;

  /**
   * Creates a KeyStemGenerator with the provided hasher.
   *
   * @param hasher The hasher used for stem and key generation.
   */
  public TrieKeyFactory(final StemHasher hasher) {
    this.hasher = hasher;
  }

  /**
   * Retrieves the hasher used for stem generation.
   *
   * @return The {@link StemHasher} instance used for stem generation operations.
   */
  public StemHasher getHasher() {
    return hasher;
  }

  /**
   * Generates the header stem for a given address.
   *
   * @param address The address.
   * @return The generated header stem.
   */
  public Bytes getHeaderStem(final Bytes address) {
    return hasher.computeStem(address, BASIC_DATA_LEAF_KEY);
  }

  /**
   * Generates the storage stem using the provided address and storage key.
   *
   * @param address The address.
   * @param storageKey The storage key.
   * @return The generated storage stem.
   */
  public Bytes getStorageStem(final Bytes address, final Bytes32 storageKey) {
    final Bytes32 trieIndex = TrieKeyUtils.getStorageKeyTrieIndex(storageKey);
    return hasher.computeStem(address, trieIndex);
  }

  /**
   * Generates the code chunk stem using the provided address and chunk ID.
   *
   * @param address The address.
   * @param chunkId The chunk ID.
   * @return The generated code chunk stem.
   */
  public Bytes getCodeChunkStem(final Bytes address, final UInt256 chunkId) {
    final Bytes32 trieIndex = TrieKeyUtils.getCodeChunkKeyTrieIndex(chunkId.toBytes());
    return hasher.computeStem(address, trieIndex);
  }

  public Map<Bytes32, Bytes> manyStems(
      final Bytes address,
      final List<Bytes32> headerKeys,
      final List<Bytes32> storageKeys,
      final List<Bytes32> codeChunkIds) {

    final Set<Bytes32> trieIndex = new HashSet<>();

    if (!headerKeys.isEmpty()) {
      trieIndex.add(UInt256.ZERO);
    }

    trieIndex.addAll(TrieKeyUtils.getStorageKeyTrieIndexes(storageKeys));

    for (Bytes32 codeChunkId : codeChunkIds) {
      trieIndex.add(TrieKeyUtils.getCodeChunkKeyTrieIndex(codeChunkId));
    }

    return getHasher().manyStems(address, new ArrayList<>(trieIndex));
  }

  /**
   * Generates a basic data key for a given address.
   *
   * @param address The address.
   * @return The generated basic data key.
   */
  public Bytes32 basicDataKey(final Bytes address) {
    return headerKey(address, BASIC_DATA_LEAF_KEY);
  }

  /**
   * Generates the storage key by combining the storage stem and suffix.
   *
   * @param address The address.
   * @param storageKey The storage key.
   * @return The generated storage key.
   */
  public Bytes32 storageKey(final Bytes address, final Bytes32 storageKey) {
    final Bytes stem = getStorageStem(address, storageKey);
    final Bytes suffix = TrieKeyUtils.getStorageKeySuffix(storageKey);
    return Bytes32.wrap(Bytes.concatenate(stem, suffix));
  }

  /**
   * Generates a code hash key for a given address.
   *
   * @param address The address.
   * @return The generated code hash key.
   */
  public Bytes32 codeHashKey(final Bytes address) {
    return headerKey(address, CODE_HASH_LEAF_KEY);
  }

  /**
   * Generates a code chunk key for a given address and chunkId.
   *
   * @param address The address.
   * @param chunkId The chunk ID.
   * @return The generated code chunk key.
   */
  public Bytes32 codeChunkKey(final Bytes address, final UInt256 chunkId) {
    final Bytes stem = getCodeChunkStem(address, chunkId);
    final Bytes suffix = TrieKeyUtils.getCodeChunkKeySuffix(chunkId.toBytes());
    return Bytes32.wrap(Bytes.concatenate(stem, suffix));
  }

  /**
   * Generates a header key for a given address and leafKey.
   *
   * @param address The address.
   * @param leafKey The leaf key.
   * @return The generated header key.
   */
  public Bytes32 headerKey(final Bytes address, final UInt256 leafKey) {
    final Bytes stem = getHeaderStem(address);
    return Bytes32.wrap(Bytes.concatenate(stem, TrieKeyUtils.getLastByte(leafKey.toBytes())));
  }
}

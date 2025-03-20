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

import static org.hyperledger.besu.ethereum.stateless.util.Parameters.CODE_OFFSET;
import static org.hyperledger.besu.ethereum.stateless.util.Parameters.HEADER_STORAGE_OFFSET;
import static org.hyperledger.besu.ethereum.stateless.util.Parameters.HEADER_STORAGE_SIZE;
import static org.hyperledger.besu.ethereum.stateless.util.Parameters.MAIN_STORAGE_OFFSET_SHIFT_LEFT_VERKLE_NODE_WIDTH;
import static org.hyperledger.besu.ethereum.stateless.util.Parameters.VERKLE_NODE_WIDTH;
import static org.hyperledger.besu.ethereum.stateless.util.Parameters.VERKLE_NODE_WIDTH_LOG2;

import org.hyperledger.besu.ethereum.stateless.util.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

/** Utility class providing static methods for key operations in the Verkle Trie structure. */
public class TrieKeyUtils {

  private static final int CHUNK_SIZE = 31;

  private TrieKeyUtils() {}

  public static Bytes32 getAccountKeyTrieIndex() {
    return Parameters.BASIC_DATA_LEAF_KEY;
  }

  public static Bytes32 getStorageKeyTrieIndex(final Bytes32 storageKey) {
    final UInt256 uintStorageKey = UInt256.fromBytes(storageKey);
    if (uintStorageKey.compareTo(HEADER_STORAGE_SIZE) < 0) {
      return uintStorageKey.add(HEADER_STORAGE_OFFSET).divide(VERKLE_NODE_WIDTH);
    } else {
      // We divide by VerkleNodeWidthLog2 to make space and prevent any potential overflow
      // Then, we increment, a step that is safeguarded against overflow.
      return Bytes32.wrap(
          uintStorageKey
              .shiftRight(VERKLE_NODE_WIDTH_LOG2.intValue())
              .add(MAIN_STORAGE_OFFSET_SHIFT_LEFT_VERKLE_NODE_WIDTH));
    }
  }

  public static List<Bytes32> getStorageKeyTrieIndexes(final List<Bytes32> storageSlotKeys) {
    return storageSlotKeys.stream()
        .map(TrieKeyUtils::getStorageKeyTrieIndex)
        .map(Bytes32::wrap)
        .toList();
  }

  public static Bytes32 getCodeChunkKeyTrieIndex(final Bytes32 chunkId) {
    return Bytes32.wrap(CODE_OFFSET.add(UInt256.fromBytes(chunkId)).divide(VERKLE_NODE_WIDTH));
  }

  public static List<Bytes32> getCodeChunkKeyTrieIndexes(final Bytes code) {
    return IntStream.range(0, TrieKeyUtils.getNbChunk(code))
        .mapToObj(UInt256::valueOf)
        .collect(Collectors.toUnmodifiableList());
  }

  public static Bytes getStorageKeySuffix(final Bytes32 storageKey) {
    final UInt256 uintStorageKey = UInt256.fromBytes(storageKey);
    if (uintStorageKey.compareTo(HEADER_STORAGE_SIZE) < 0) {
      return getLastByte(uintStorageKey.add(HEADER_STORAGE_OFFSET).mod(VERKLE_NODE_WIDTH));
    } else {
      return getLastByte(storageKey);
    }
  }

  public static Bytes getCodeChunkKeySuffix(final Bytes32 chunkId) {
    return getLastByte(CODE_OFFSET.add(UInt256.fromBytes(chunkId)).mod(VERKLE_NODE_WIDTH));
  }

  public static Bytes getLastByte(final Bytes base) {
    return base.slice(Bytes32.SIZE - 1);
  }

  public static int getNbChunk(final Bytes bytecode) {
    return bytecode.isEmpty() ? 0 : (1 + ((bytecode.size() - 1) / CHUNK_SIZE));
  }

  public static List<UInt256> chunkifyCode(final Bytes bytecode) {
    if (bytecode.isEmpty()) {
      return new ArrayList<>();
    }

    // Chunking variables
    final int CHUNK_SIZE = 31;
    final int nChunks = getNbChunk(bytecode);
    final int padSize = nChunks * CHUNK_SIZE - bytecode.size();
    final Bytes code = Bytes.concatenate(bytecode, Bytes.repeat((byte) 0, padSize));
    final List<UInt256> chunks = new ArrayList<>(nChunks);

    // OpCodes for PUSH's
    final int PUSH_OFFSET = 95;
    final int PUSH1 = PUSH_OFFSET + 1;
    final int PUSH32 = PUSH_OFFSET + 32;

    // Iterator data
    int chunkPos = 0; // cursor position to start of current chunk
    int posInChunk = 0; // cursor position relative to the current chunk
    int nPushData = 0; // number of bytes in current push data

    // Create chunk iteratively
    for (int chunkId = 0; chunkId < nChunks; ++chunkId) {
      chunkPos = chunkId * CHUNK_SIZE;
      posInChunk = nPushData;
      while (posInChunk < CHUNK_SIZE) {
        int opCode = Byte.toUnsignedInt(code.get(chunkPos + posInChunk));
        posInChunk += 1;
        if (PUSH1 <= opCode && opCode <= PUSH32) {
          posInChunk += opCode - PUSH_OFFSET;
        }
      }
      chunks.add(
          UInt256.fromBytes(
              Bytes.concatenate(
                  Bytes.of(Math.min(nPushData, 31)), code.slice(chunkPos, CHUNK_SIZE))));
      nPushData = posInChunk - CHUNK_SIZE;
    }

    return chunks;
  }
}

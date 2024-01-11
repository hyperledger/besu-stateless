/*
 * Copyright Besu Contributors
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
package org.hyperledger.besu.ethereum.trie.verkle.hasher;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/** Defines an interface for a Verkle Trie node hashing strategy. */
public interface Hasher {

  /**
   * Calculates the commitment hash for an array of inputs.
   *
   * @param inputs An array of values to be hashed.
   * @return The commitment hash calculated from the inputs.
   */
  public Bytes32 commit(Bytes32[] inputs);

  public Bytes32 commitRoot(Bytes32[] inputs);
  /**
   * Calculates the hash for an address and index.
   *
   * @param address Account address.
   * @param index Index in storage.
   * @return The trie-key hash
   */
  public Bytes32 trieKeyHash(Bytes address, Bytes32 index);
}

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
package org.hyperledger.besu.ethereum.stateless.pruning;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

/**
 * Registry responsible for tracking {@code StemNode} stems that are eligible for pruning.
 *
 * <p>When a {@code StemNode} becomes obsolete (e.g., replaced by a {@code NullNode}), its stem can
 * be marked as prunable and later removed during a cleanup pass. This class centralizes that
 * tracking mechanism.
 */
public class StemPrunableNodeRegistry {

  private final List<Bytes> prunableStems = new ArrayList<>();

  /** Creates a new registry for tracking prunable stem nodes. */
  public StemPrunableNodeRegistry() {}

  /**
   * Marks a stem as prunable, indicating that the corresponding {@code StemNode} can be safely
   * removed if no longer needed.
   *
   * @param key the stem key to mark as prunable
   */
  public void markPrunableStem(final Bytes key) {
    prunableStems.add(key);
  }

  /**
   * Removes a stem from the prunable set, usually because it has been revived or re-added to the
   * trie.
   *
   * @param key the stem key to remove from pruning consideration
   */
  public void removePrunableStem(final Bytes key) {
    prunableStems.remove(key);
  }

  /**
   * Returns the list of all stems currently marked as prunable.
   *
   * @return list of stems eligible for pruning
   */
  public List<Bytes> getPrunableStems() {
    return prunableStems;
  }

  /** Clears all tracked prunable stems. Typically used after a batch prune operation. */
  public void clear() {
    prunableStems.clear();
  }
}

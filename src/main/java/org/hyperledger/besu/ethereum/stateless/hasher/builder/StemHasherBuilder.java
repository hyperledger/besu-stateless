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
package org.hyperledger.besu.ethereum.stateless.hasher.builder;

import org.hyperledger.besu.ethereum.stateless.hasher.StemHasher;
import org.hyperledger.besu.ethereum.stateless.hasher.cache.CacheStrategy;
import org.hyperledger.besu.ethereum.stateless.hasher.cache.NoOpCacheStrategy;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class StemHasherBuilder {

  private CacheStrategy<Bytes32, Bytes> stemCache;
  private CacheStrategy<Bytes, Bytes> addressCommitmentCache;

  /**
   * Creates a new StemHasherBuilder.
   *
   * @return A new instance of StemHasherBuilder.
   */
  public static StemHasherBuilder builder() {
    return new StemHasherBuilder();
  }

  /**
   * Sets the caching strategy for stems.
   *
   * @param stemCache The caching strategy for stems.
   * @return The builder instance for method chaining.
   */
  public StemHasherBuilder withStemCache(CacheStrategy<Bytes32, Bytes> stemCache) {
    this.stemCache = stemCache;
    return this;
  }

  /**
   * Sets the caching strategy for address commitments.
   *
   * @param addressCommitmentCache The caching strategy for address commitments.
   * @return The builder instance for method chaining.
   */
  public StemHasherBuilder withAddressCommitmentCache(
      CacheStrategy<Bytes, Bytes> addressCommitmentCache) {
    this.addressCommitmentCache = addressCommitmentCache;
    return this;
  }

  /**
   * Builds the PedersenHasher with the configured caching strategies.
   *
   * @return A new instance of PedersenHasher.
   */
  public StemHasher build() {
    if (stemCache == null) {
      stemCache = new NoOpCacheStrategy<>();
    }
    if (addressCommitmentCache == null) {
      addressCommitmentCache = new NoOpCacheStrategy<>();
    }

    return new StemHasher(stemCache, addressCommitmentCache);
  }
}

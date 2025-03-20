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
package org.hyperledger.besu.ethereum.stateless.hasher.cache;

/**
 * Interface for caching strategies.
 *
 * @param <K> The type of keys stored in the cache.
 * @param <V> The type of values stored in the cache.
 */
public interface CacheStrategy<K, V> {

  /**
   * Retrieves a value from the cache, or computes it using the provided function if absent.
   *
   * @param key The key to look up in the cache.
   * @param computeFunction The function to compute the value if not present in the cache.
   * @return The cached or computed value.
   */
  V get(K key, java.util.function.Function<K, V> computeFunction);

  /**
   * Stores a key-value pair in the cache.
   *
   * @param key The key to store.
   * @param value The value to store.
   */
  void put(K key, V value);

  /**
   * Retrieves a value from the cache if it exists.
   *
   * @param key The key to look up.
   * @return The cached value, or null if not present.
   */
  V getIfPresent(K key);

  /**
   * Stores all key-value pairs in the cache.
   *
   * @param values The map of key-value pairs to store.
   */
  void putAll(java.util.Map<K, V> values);
}

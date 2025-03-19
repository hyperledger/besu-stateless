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

import java.util.Map;
import java.util.function.Function;

/**
 * No-op caching strategy that performs no caching.
 *
 * @param <K> The type of keys.
 * @param <V> The type of values.
 */
public class NoOpCacheStrategy<K, V> implements CacheStrategy<K, V> {

  @Override
  public V get(K key, Function<K, V> computeFunction) {
    return computeFunction.apply(key);
  }

  @Override
  public void put(K key, V value) {
    // No-op
  }

  @Override
  public V getIfPresent(K key) {
    return null; // Always return null
  }

  @Override
  public void putAll(Map<K, V> values) {
    // No-op
  }
}

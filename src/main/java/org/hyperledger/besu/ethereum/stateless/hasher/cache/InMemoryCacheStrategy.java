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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * In-memory caching strategy using the Caffeine library.
 *
 * @param <K> The type of keys stored in the cache.
 * @param <V> The type of values stored in the cache.
 */
public class InMemoryCacheStrategy<K, V> implements CacheStrategy<K, V> {

  private final Cache<K, V> cache;

  /**
   * Constructs a Caffeine cache with the specified maximum size.
   *
   * @param maxSize The maximum size of the cache.
   */
  public InMemoryCacheStrategy(int maxSize) {
    this.cache = Caffeine.newBuilder().maximumSize(maxSize).build();
  }

  public InMemoryCacheStrategy(int maxSize, final Map<K, V> preloaded) {
    this(maxSize);
    cache.putAll(preloaded);
  }

  @Override
  public V get(K key, java.util.function.Function<K, V> computeFunction) {
    return cache.get(key, computeFunction);
  }

  @Override
  public void put(K key, V value) {
    cache.put(key, value);
  }

  @Override
  public V getIfPresent(K key) {
    return cache.getIfPresent(key);
  }

  @Override
  public void putAll(Map<K, V> values) {
    cache.putAll(values);
  }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.ethereum.stateless.hasher.builder.StemHasherBuilder;
import org.hyperledger.besu.ethereum.stateless.hasher.cache.CacheStrategy;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked"})
class StemHasherTest {

  private CacheStrategy<Bytes32, Bytes> mockStemCache;
  private CacheStrategy<Bytes, Bytes> mockAddressCommitmentCache;
  private StemHasher stemHasher;

  @BeforeEach
  void setUp() {
    mockStemCache = mock(CacheStrategy.class);
    mockAddressCommitmentCache = mock(CacheStrategy.class);
    stemHasher = new StemHasher(mockStemCache, mockAddressCommitmentCache);
  }

  @Test
  public void testGetStem() {

    final StemHasher hasher = StemHasherBuilder.builder().build();

    byte[] addr = new byte[32];
    for (int i = 0; i < 16; i++) {
      addr[1 + 2 * i] = (byte) 0xff;
    }

    final Bytes address = Bytes.wrap(addr);

    BigInteger n = BigInteger.ONE;
    n = n.shiftLeft(129);
    n = n.add(BigInteger.valueOf(3));
    final Bytes32 index = UInt256.valueOf(n).toBytes();
    final Bytes tk = hasher.computeStem(address, index);
    final String got = tk.toHexString();
    final String exp = "0x6ede905763d5856cd2d67936541e82aa78f7141bf8cd5ff6c962170f3e9dc2";

    assertEquals(exp, got);
  }

  @Test
  void testComputeStem_CacheHit() {
    final Bytes32 index = Bytes32.random();
    final Bytes address = Bytes.random(32);
    final Bytes cachedValue = Bytes.random(31);

    when(mockStemCache.get(eq(index), any())).thenReturn(cachedValue);

    final Bytes result = stemHasher.computeStem(address, index);

    assertEquals(cachedValue, result);
    verify(mockStemCache).get(eq(index), any());
  }

  @Test
  void testComputeStem_CacheMiss() {
    final Bytes32 index = Bytes32.random();
    final Bytes address = Bytes.random(32);

    when(mockAddressCommitmentCache.get(eq(address), any()))
        .thenAnswer(
            invocation ->
                ((java.util.function.Function<Bytes, Bytes>) invocation.getArgument(1))
                    .apply(address));

    when(mockStemCache.get(eq(index), any()))
        .thenAnswer(
            invocation ->
                ((java.util.function.Function<Bytes32, Bytes>) invocation.getArgument(1))
                    .apply(index));

    final Bytes result = stemHasher.computeStem(address, index);

    assertNotNull(result);
    verify(mockStemCache).get(eq(index), any());
    verify(mockAddressCommitmentCache).get(eq(address), any());
  }

  @Test
  void testManyStems_AllKeysInCache() {
    final Bytes address = Bytes.random(32);
    final UInt256 index1 = UInt256.fromBytes(Bytes32.random());
    final UInt256 index2 = UInt256.fromBytes(Bytes32.random());
    final Bytes value1 = Bytes.random(31);
    final Bytes value2 = Bytes.random(31);

    when(mockStemCache.getIfPresent(index1)).thenReturn(value1);
    when(mockStemCache.getIfPresent(index2)).thenReturn(value2);

    final Map<Bytes32, Bytes> result = stemHasher.manyStems(address, List.of(index1, index2));

    assertEquals(2, result.size());
    assertEquals(value1, result.get(index1));
    assertEquals(value2, result.get(index2));
    verify(mockStemCache).getIfPresent(index1);
    verify(mockStemCache).getIfPresent(index2);
  }

  @Test
  void testManyStems_MissingKeys() {
    final Bytes address = Bytes.random(32);
    final UInt256 index1 = UInt256.fromBytes(Bytes32.leftPad(Bytes.of(1)));
    final UInt256 index2 = UInt256.fromBytes(Bytes32.leftPad(Bytes.of(2)));
    final UInt256 index3 = UInt256.fromBytes(Bytes32.leftPad(Bytes.of(3)));
    final Bytes value1 = Bytes.random(31);

    when(mockStemCache.getIfPresent(index1)).thenReturn(value1);
    when(mockStemCache.getIfPresent(index2)).thenReturn(null);
    when(mockStemCache.getIfPresent(index3)).thenReturn(null);
    when(mockAddressCommitmentCache.get(eq(address), any()))
        .thenAnswer(
            invocation ->
                ((java.util.function.Function<Bytes, Bytes>) invocation.getArgument(1))
                    .apply(address));
    when(mockStemCache.get(eq(index2), any()))
        .thenAnswer(
            invocation ->
                ((java.util.function.Function<Bytes32, Bytes>) invocation.getArgument(1))
                    .apply(index2));
    when(mockStemCache.get(eq(index3), any()))
        .thenAnswer(
            invocation ->
                ((java.util.function.Function<Bytes32, Bytes>) invocation.getArgument(1))
                    .apply(index3));

    final Map<Bytes32, Bytes> result =
        stemHasher.manyStems(address, List.of(index1, index2, index3));

    assertEquals(3, result.size());
    verify(mockStemCache).getIfPresent(index1);
    verify(mockStemCache).getIfPresent(index2);
    verify(mockStemCache).getIfPresent(index3);
  }
}

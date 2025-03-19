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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.hyperledger.besu.ethereum.stateless.hasher.StemHasher;
import org.hyperledger.besu.ethereum.stateless.hasher.cache.CacheStrategy;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@SuppressWarnings("unchecked")
public class StemHasherBuilderTest {

  private CacheStrategy<Bytes32, Bytes> mockStemCache;
  private CacheStrategy<Bytes, Bytes> mockAddressCommitmentCache;

  @BeforeEach
  public void setUp() {
    mockStemCache = mock(CacheStrategy.class);
    mockAddressCommitmentCache = mock(CacheStrategy.class);
  }

  @Test
  public void testBuilderWithStemCache() {
    final StemHasherBuilder builder = StemHasherBuilder.builder().withStemCache(mockStemCache);

    assertNotNull(builder);
  }

  @Test
  public void testBuilderWithAddressCommitmentCache() {
    final StemHasherBuilder builder =
        StemHasherBuilder.builder().withAddressCommitmentCache(mockAddressCommitmentCache);

    assertNotNull(builder);
  }

  @Test
  public void testBuildWithCustomCaches() {
    final StemHasherBuilder builder =
        StemHasherBuilder.builder()
            .withStemCache(mockStemCache)
            .withAddressCommitmentCache(mockAddressCommitmentCache);

    final StemHasher stemHasher = builder.build();

    assertNotNull(stemHasher);

    // Verify that the custom caches are used
    stemHasher.computeStem(Bytes.EMPTY, Bytes32.ZERO);
    verify(mockStemCache).get(Mockito.any(), Mockito.any());
    verify(mockAddressCommitmentCache).get(Mockito.any(), Mockito.any());
  }
}

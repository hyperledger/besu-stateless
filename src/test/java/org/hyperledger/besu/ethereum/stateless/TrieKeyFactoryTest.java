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
package org.hyperledger.besu.ethereum.stateless;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.ethereum.stateless.adapter.TrieKeyFactory;
import org.hyperledger.besu.ethereum.stateless.adapter.TrieKeyUtils;
import org.hyperledger.besu.ethereum.stateless.hasher.builder.StemHasherBuilder;
import org.hyperledger.besu.ethereum.stateless.hasher.cache.InMemoryCacheStrategy;
import org.hyperledger.besu.ethereum.stateless.util.Parameters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TrieKeyFactoryTest {
  Bytes address = Bytes.fromHexString("0x00112233445566778899aabbccddeeff00112233");
  TrieKeyFactory adapter = new TrieKeyFactory(StemHasherBuilder.builder().build());

  @Test
  public void testStorageKey() {
    UInt256 storageKey = UInt256.valueOf(32);
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034460");
    assertThat(adapter.storageKey(address, storageKey)).isEqualTo(expected);
  }

  @Test
  public void testStorageKeyMainStorage() {
    UInt256 storageKey = UInt256.valueOf(64);
    Bytes32 expected =
        Bytes32.fromHexString("0x6127e4b0c266bee72914ce7261d0e4595c414c1ef439d9b0eb7d13cda5dc7640");
    assertThat(adapter.storageKey(address, storageKey)).isEqualTo(expected);
  }

  @Test
  public void testStorageKeyForMainStorageWithOverflow() {
    Bytes32 storageKey =
        Bytes32.fromHexString("0xff0d54412868ab2569622781556c0b41264d9dae313826adad7b60da4b441e67");
    Bytes32 expected =
        Bytes32.fromHexString("0xe4674a8f2ed2b61006311280d5bf4bccb24c69ed5c2c7c4fe71133748e28a267");
    assertThat(adapter.storageKey(address, storageKey)).isEqualTo(expected);
  }

  @Test
  public void testCodeChunkKey() {
    UInt256 chunkId = UInt256.valueOf(24);
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034498");
    assertThat(adapter.codeChunkKey(address, chunkId)).isEqualTo(expected);
  }

  @Test
  public void testCodeChunkKey2() {
    Bytes addr = Bytes.fromHexString("0x6f22ffbc56eff051aecf839396dd1ed9ad6bba9d");
    UInt256 chunkId =
        UInt256.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000080");
    Bytes32 expected =
        Bytes32.fromHexString("0x64465862f6244f410f93da62f24f4219a6e99fc3d0ad603da813b4be8e5c9500");
    assertThat(adapter.codeChunkKey(addr, chunkId)).isEqualTo(expected);
  }

  @Test
  public void testBasicDataKey() {
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034400");
    assertThat(adapter.basicDataKey(address)).isEqualTo(expected);
  }

  @Test
  public void testCodeHashKey() {
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034401");
    assertThat(adapter.codeHashKey(address)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource({"JsonChunkifyData", "JsonContractCodeDataWithPush32On31stByte"})
  public void TestChunkifyCode(TestChunkifyData testData) {
    Bytes bytecode = Bytes.fromHexString(testData.bytecode);
    List<UInt256> result = TrieKeyUtils.chunkifyCode(bytecode);
    assertThat(testData.chunks.size()).isEqualTo(result.size());
    Bytes32 value;
    for (int i = 0; i < testData.chunks.size(); ++i) {
      value = Bytes32.fromHexString(testData.chunks.get(i));
      assertThat(value).isEqualByComparingTo(result.get(i));
    }
  }

  @ParameterizedTest
  @MethodSource("JsonContractCodeData")
  public void TestContractCode(TestCodeData testData) {
    Bytes addr = Bytes.fromHexString(testData.address);
    Bytes bytecode = Bytes.fromHexString(testData.bytecode);
    List<UInt256> chunks = TrieKeyUtils.chunkifyCode(bytecode);
    assertThat(chunks.size()).as("Same number of chunks").isEqualTo(testData.chunks.size());
    for (int i = 0; i < chunks.size(); ++i) {
      Bytes32 key = adapter.codeChunkKey(addr, UInt256.valueOf(i));
      Bytes32 expectedKey = Bytes32.fromHexString(testData.chunks.get(i).key);
      assertThat(key).as(String.format("Key %s", i)).isEqualTo(expectedKey);
      Bytes32 value = chunks.get(i);
      Bytes32 expectedValue = Bytes32.fromHexString(testData.chunks.get(i).value);
      assertThat(value).as(String.format("Value %s", i)).isEqualTo(expectedValue);
    }
  }

  @Test
  public void testAccountKeys() {
    final List<Bytes32> expectedIndexes = new ArrayList<>();
    expectedIndexes.add(Parameters.BASIC_DATA_LEAF_KEY);
    expectedIndexes.add(Parameters.CODE_HASH_LEAF_KEY);

    final Map<Bytes32, Bytes> generatedStems =
        adapter.manyStems(address, expectedIndexes, new ArrayList<>(), new ArrayList<>());
    final TrieKeyFactory cachedTrieKeyAdapter =
        new TrieKeyFactory(
            StemHasherBuilder.builder()
                .withStemCache(new InMemoryCacheStrategy<>(100, generatedStems))
                .withAddressCommitmentCache(new InMemoryCacheStrategy<>(100))
                .build());
    assertThat(cachedTrieKeyAdapter.basicDataKey(address))
        .isEqualTo(
            Bytes32.fromHexString(
                "0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034400"));
    assertThat(cachedTrieKeyAdapter.codeHashKey(address))
        .isEqualTo(
            Bytes32.fromHexString(
                "0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034401"));
  }

  @Test
  public void testAccountKeysWithStorage() {
    final List<Bytes32> expectedIndexes = new ArrayList<>();
    expectedIndexes.add(Parameters.BASIC_DATA_LEAF_KEY);
    expectedIndexes.add(Parameters.CODE_HASH_LEAF_KEY);

    final UInt256 storage = UInt256.valueOf(64);
    final UInt256 storage2 =
        UInt256.fromHexString(
            "0xff0d54412868ab2569622781556c0b41264d9dae313826adad7b60da4b441e67"); // test overflow
    expectedIndexes.add(storage);
    expectedIndexes.add(storage2);

    final Map<Bytes32, Bytes> generatedStems =
        adapter.manyStems(address, expectedIndexes, List.of(storage, storage2), new ArrayList<>());

    final TrieKeyFactory cachedTrieKeyAdapter =
        new TrieKeyFactory(
            StemHasherBuilder.builder()
                .withStemCache(new InMemoryCacheStrategy<>(100, generatedStems))
                .withAddressCommitmentCache(new InMemoryCacheStrategy<>(100))
                .build());
    assertThat(cachedTrieKeyAdapter.basicDataKey(address))
        .isEqualTo(
            Bytes32.fromHexString(
                "0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034400"));
    assertThat(cachedTrieKeyAdapter.codeHashKey(address))
        .isEqualTo(
            Bytes32.fromHexString(
                "0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034401"));
    assertThat(cachedTrieKeyAdapter.storageKey(address, storage))
        .isEqualTo(
            Bytes32.fromHexString(
                "0x6127e4b0c266bee72914ce7261d0e4595c414c1ef439d9b0eb7d13cda5dc7640"));
    assertThat(cachedTrieKeyAdapter.storageKey(address, storage2))
        .isEqualTo(
            Bytes32.fromHexString(
                "0xe4674a8f2ed2b61006311280d5bf4bccb24c69ed5c2c7c4fe71133748e28a267"));
  }

  @Test
  public void testAccountKeysWithCode() {
    final List<Bytes32> expectedIndexes = new ArrayList<>();
    expectedIndexes.add(Parameters.BASIC_DATA_LEAF_KEY);
    expectedIndexes.add(Parameters.CODE_HASH_LEAF_KEY);

    final UInt256 chunkId = UInt256.valueOf(24);
    expectedIndexes.add(chunkId);

    final Map<Bytes32, Bytes> generatedStems =
        adapter.manyStems(address, expectedIndexes, new ArrayList<>(), List.of(chunkId));
    final TrieKeyFactory cachedTrieKeyAdapter =
        new TrieKeyFactory(
            StemHasherBuilder.builder()
                .withStemCache(new InMemoryCacheStrategy<>(100, generatedStems))
                .withAddressCommitmentCache(new InMemoryCacheStrategy<>(100))
                .build());
    assertThat(cachedTrieKeyAdapter.basicDataKey(address))
        .isEqualTo(
            Bytes32.fromHexString(
                "0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034400"));
    assertThat(cachedTrieKeyAdapter.codeHashKey(address))
        .isEqualTo(
            Bytes32.fromHexString(
                "0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034401"));
    assertThat(cachedTrieKeyAdapter.codeChunkKey(address, chunkId))
        .isEqualTo(
            Bytes32.fromHexString(
                "0x46b95e4e504b92d984c91d6f17eba4b60b904fb370818f0b6e74bc3ae5034498"));
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static List<TestChunkifyData> JsonChunkifyData() throws IOException {
    InputStream inputStream = TrieKeyFactoryTest.class.getResourceAsStream("/chunkifyCode.json");
    return objectMapper.readValue(inputStream, new TypeReference<List<TestChunkifyData>>() {});
  }

  public static List<TestChunkifyData> JsonContractCodeDataWithPush32On31stByte()
      throws IOException {
    InputStream inputStream =
        TrieKeyFactoryTest.class.getResourceAsStream("/chukifyCodePush32on31stByte.json");
    return objectMapper.readValue(inputStream, new TypeReference<List<TestChunkifyData>>() {});
  }

  static class TestChunkifyData {
    public String bytecode;
    public ArrayList<String> chunks;
  }

  static class KeyValueData {
    public String key;
    public String value;
  }

  static class TestCodeData {
    public String address;
    public String bytecode;
    public ArrayList<KeyValueData> chunks;
  }

  public static List<TestCodeData> JsonContractCodeData() throws IOException {
    InputStream inputStream = TrieKeyFactoryTest.class.getResourceAsStream("/contractCode.json");
    return objectMapper.readValue(inputStream, new TypeReference<List<TestCodeData>>() {});
  }
}

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
package org.hyperledger.besu.ethereum.trie.verkle;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.ethereum.trie.verkle.adapter.TrieKeyAdapter;
import org.hyperledger.besu.ethereum.trie.verkle.hasher.PedersenHasher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TrieKeyAdapterTest {
  Bytes address = Bytes.fromHexString("0x00112233445566778899aabbccddeeff00112233");
  TrieKeyAdapter adapter = new TrieKeyAdapter(new PedersenHasher());

  @Test
  public void testStorageKey() {
    UInt256 storageKey = UInt256.valueOf(32);
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0xc224b9edaff18b27ce2604ad0f33a162d19840fd25290436dedcce42ad15d260");
    assertThat(adapter.storageKey(address, storageKey)).isEqualTo(expected);
  }

  @Test
  public void testStorageKeyForMainStorage() {
    UInt256 storageKey = UInt256.valueOf(64);
    // use shift left operation for the moment , should be pow in the future
    Bytes32 expected =
        Bytes32.fromHexString("0x3d08fd033c8f1e8b95f28b95a854a0e948062cb7ecb87587e54dcd826e577640");
    assertThat(adapter.storageKey(address, storageKey)).isEqualTo(expected);
  }

  @Test
  public void testCodeChunkKey() {
    UInt256 chunkId = UInt256.valueOf(24);
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0xc224b9edaff18b27ce2604ad0f33a162d19840fd25290436dedcce42ad15d298");
    assertThat(adapter.codeChunkKey(address, chunkId)).isEqualTo(expected);
  }

  @Test
  public void testCodeChunkKey2() {
    Bytes addr = Bytes.fromHexString("0x6f22ffbc56eff051aecf839396dd1ed9ad6bba9d");
    UInt256 chunkId =
        UInt256.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000080");
    Bytes32 expected =
        Bytes32.fromHexString("0x07361aa490296db032a91d3a5a69d3c3ef0e477baf8fced7f6775e4739614200");
    assertThat(adapter.codeChunkKey(addr, chunkId)).isEqualTo(expected);
  }

  @Test
  public void testVersionKey() {
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0xc224b9edaff18b27ce2604ad0f33a162d19840fd25290436dedcce42ad15d200");
    assertThat(adapter.versionKey(address)).isEqualTo(expected);
  }

  @Test
  public void testBalanceKey() {
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0xc224b9edaff18b27ce2604ad0f33a162d19840fd25290436dedcce42ad15d201");
    assertThat(adapter.balanceKey(address)).isEqualTo(expected);
  }

  @Test
  public void testNonceKey() {
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0xc224b9edaff18b27ce2604ad0f33a162d19840fd25290436dedcce42ad15d202");
    assertThat(adapter.nonceKey(address)).isEqualTo(expected);
  }

  @Test
  public void testCodeKeccakKey() {
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0xc224b9edaff18b27ce2604ad0f33a162d19840fd25290436dedcce42ad15d203");
    assertThat(adapter.codeKeccakKey(address)).isEqualTo(expected);
  }

  @Test
  public void testCodeSizeKey() {
    // Need to change this once commit is fixed
    Bytes32 expected =
        Bytes32.fromHexString("0xc224b9edaff18b27ce2604ad0f33a162d19840fd25290436dedcce42ad15d204");
    assertThat(adapter.codeSizeKey(address)).isEqualTo(expected);
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static List<TestChunkifyData> JsonChunkifyData() throws IOException {
    InputStream inputStream = TrieKeyAdapterTest.class.getResourceAsStream("/chunkifyCode.json");
    return objectMapper.readValue(inputStream, new TypeReference<List<TestChunkifyData>>() {});
  }

  public static List<TestChunkifyData> JsonContractCodeDataWithPush32On31Byte() throws IOException {
    InputStream inputStream =
        TrieKeyAdapterTest.class.getResourceAsStream("/chukifyCodePush32on31stByte.json");
    return objectMapper.readValue(inputStream, new TypeReference<List<TestChunkifyData>>() {});
  }

  static class TestChunkifyData {
    public String bytecode;
    public ArrayList<String> chunks;
  }

  @ParameterizedTest
  @MethodSource({"JsonChunkifyData", "JsonContractCodeDataWithPush32On31Byte"})
  public void TestChunkifyCode(TestChunkifyData testData) {
    Bytes bytecode = Bytes.fromHexString(testData.bytecode);
    List<Bytes32> result = adapter.chunkifyCode(bytecode);
    assertThat(testData.chunks.size()).isEqualTo(result.size());
    Bytes32 value;
    // @SuppressWarnings("ModifiedButNotUsed")
    // List<Bytes32> expected = new ArrayList<Bytes32>(testData.chunks.size());
    for (int i = 0; i < testData.chunks.size(); ++i) {
      value = Bytes32.fromHexString(testData.chunks.get(i));
      assertThat(value).isEqualByComparingTo(result.get(i));
      // expected.add(value);
    }
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
    InputStream inputStream = TrieKeyAdapterTest.class.getResourceAsStream("/contractCode.json");
    return objectMapper.readValue(inputStream, new TypeReference<List<TestCodeData>>() {});
  }

  @ParameterizedTest
  @MethodSource("JsonContractCodeData")
  public void TestContractCode(TestCodeData testData) {
    Bytes addr = Bytes.fromHexString(testData.address);
    Bytes bytecode = Bytes.fromHexString(testData.bytecode);
    List<Bytes32> chunks = adapter.chunkifyCode(bytecode);
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
}

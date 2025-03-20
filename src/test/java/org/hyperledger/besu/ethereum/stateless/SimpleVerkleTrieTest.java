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

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

public class SimpleVerkleTrieTest {

  @Test
  public void testEmptyTrie() {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(Bytes32.ZERO);
  }

  @Test
  public void testOneValue() {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    trie.put(key, value);
    assertThat(trie.get(key))
        .as("Get one value should be the inserted value")
        .isEqualTo(Optional.of(value));
    Bytes32 expectedRootHash =
        Bytes32.fromHexString("0x70985156c77f266a97c35d5a051b6c48b3ed64669105ea0a6a248220bedaece4");
    assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(expectedRootHash);
  }

  @Test
  public void testDeleteAlreadyDeletedValue() {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    trie.put(key, value);
    trie.remove(key);
    trie.remove(key);
    assertThat(trie.getRootHash()).isEqualTo(Bytes32.ZERO);
  }

  @Test
  public void testTwoValuesAtSameStem() throws Exception {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key3 =
        Bytes32.fromHexString("0xde112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    trie.put(key1, value1);
    trie.put(key2, value2);
    assertThat(trie.get(key1).get()).as("Get first value").isEqualByComparingTo(value1);
    assertThat(trie.get(key2).get()).as("Get second value").isEqualByComparingTo(value2);
    assertThat(trie.get(key3)).as("Get non-key returns empty").isEmpty();

    Bytes32 expectedRootHash =
        Bytes32.fromHexString("0x0e6714b82adb7af3d814aac5386878295949ded4eae3e33e08844af49fe042ba");
    assertThat(trie.getRootHash()).as("Get root hash").isEqualByComparingTo(expectedRootHash);
  }

  @Test
  public void testTwoValuesAtDifferentIndex() throws Exception {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    assertThat(trie.get(key1).get()).as("Get first value").isEqualByComparingTo(value1);
    assertThat(trie.get(key2).get()).as("Get second value").isEqualByComparingTo(value2);
    Bytes32 expectedRootHash =
        Bytes32.fromHexString("0x60d94a57c00df92c007416aac3d85cac54cd979408b0865088fc296611c65efc");
    assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(expectedRootHash);
  }

  @Test
  public void testTwoValuesWithDivergentStemsAtDepth2() throws Exception {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    assertThat(trie.get(key1)).as("Retrieve first value").isEqualTo(Optional.of(value1));
    assertThat(trie.get(key2)).as("Retrieve second value").isEqualTo(Optional.of(value2));
    Bytes32 expectedRootHash =
        Bytes32.fromHexString("0x4fd3848fecececd160dad21e25eef54c40e39813814968f63971c0cb458e95ab");
    assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(expectedRootHash);
  }

  @Test
  public void testDeleteTwoValuesAtSameStem() throws Exception {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000001");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000002");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.remove(key1);
    assertThat(trie.get(key1)).as("Make sure value is deleted").isEqualTo(Optional.empty());
    trie.remove(key2);
    assertThat(trie.get(key2)).as("Make sure value is deleted").isEqualTo(Optional.empty());
  }

  @Test
  public void testDeleteTwoValuesAtDifferentIndex() throws Exception {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.remove(key1);
    assertThat(trie.get(key1)).as("Make sure value is deleted").isEqualTo(Optional.empty());
    trie.remove(key2);
    assertThat(trie.get(key2)).as("Make sure value is deleted").isEqualTo(Optional.empty());
  }

  @Test
  public void testDeleteTwoValuesWithDivergentStemsAtDepth2() throws Exception {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.remove(key1);
    assertThat(trie.get(key1)).as("Make sure value is deleted").isEqualTo(Optional.empty());
    trie.remove(key2);
    assertThat(trie.get(key2)).as("Make sure value is deleted").isEqualTo(Optional.empty());
  }

  @Test
  public void testDeleteThreeValues() throws Exception {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0200000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key3 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddff");
    Bytes32 value3 =
        Bytes32.fromHexString("0x0300000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.put(key3, value3);
    trie.remove(key3);
    assertThat(trie.get(key3)).as("Make sure value is deleted").isEqualTo(Optional.empty());
    assertThat(trie.get(key2)).as("Retrieve second value").isEqualTo(Optional.of(value2));
    trie.remove(key2);
    assertThat(trie.get(key2)).as("Make sure value is deleted").isEqualTo(Optional.empty());
    assertThat(trie.get(key1)).as("Retrieve first value").isEqualTo(Optional.of(value1));
    trie.remove(key1);
    assertThat(trie.get(key1)).as("Make sure value is deleted").isEqualTo(Optional.empty());
  }

  @Test
  public void testDeleteThreeValuesWithFlattening() throws Exception {
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0200000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key3 =
        Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddff");
    Bytes32 value3 =
        Bytes32.fromHexString("0x0300000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.put(key3, value3);
    trie.remove(key1);
    assertThat(trie.get(key1)).as("First value has been deleted").isEqualTo(Optional.empty());
    assertThat(trie.get(key2)).as("Second value").isEqualTo(Optional.of(value2));
    trie.remove(key2);
    assertThat(trie.get(key2)).as("Second value has been deleted").isEqualTo(Optional.empty());
    assertThat(trie.get(key3)).as("Third value").isEqualTo(Optional.of(value3));
    trie.remove(key3);
    assertThat(trie.get(key3)).as("Third value has been deleted").isEqualTo(Optional.empty());
  }

  @Test
  public void testDeleteAllValuesWithDivergentStemsAtDepth2() throws Exception {
    SimpleVerkleTrie<Bytes, Bytes> trie = new SimpleVerkleTrie<>();
    assertThat(trie.getRootHash()).isEqualTo(Bytes32.ZERO);
    Bytes32 key0 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45641");
    Bytes32 value0 =
        Bytes32.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000001");
    Bytes32 key1 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45601");
    Bytes32 value1 =
        Bytes32.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000001");
    Bytes32 key2 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45602");
    Bytes32 value2 = Bytes32.fromHexString("0x01");
    Bytes32 key3 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45600");
    Bytes32 value3 = Bytes32.fromHexString("0x00");
    Bytes32 key4 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45603");
    Bytes32 value4 =
        Bytes32.fromHexString("0xf84a97f1f0a956e738abd85c2e0a5026f8874e3ec09c8f012159dfeeaab2b156");
    Bytes32 key5 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45604");
    Bytes32 value5 = Bytes32.fromHexString("0x03");
    Bytes32 key6 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45680");
    Bytes32 value6 =
        Bytes32.fromHexString("0x0000010200000000000000000000000000000000000000000000000000000000");
    trie.put(key0, value0);
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.put(key3, value3);
    trie.put(key4, value4);
    trie.put(key5, value5);
    trie.put(key6, value6);
    trie.remove(key0);
    trie.remove(key4);
    trie.remove(key5);
    trie.remove(key6);
    trie.remove(key3);
    trie.remove(key1);
    trie.remove(key2);
    assertThat(trie.getRootHash()).isEqualTo(Bytes32.ZERO);
  }

  @Test
  public void testDeleteManyValuesWithDivergentStemsAtDepth2() throws Exception {
    SimpleVerkleTrie<Bytes, Bytes> trie = new SimpleVerkleTrie<>();
    assertThat(trie.getRootHash()).isEqualTo(Bytes32.ZERO);
    Bytes32 key0 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45641");
    Bytes32 value0 =
        Bytes32.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000001");
    Bytes32 key1 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45601");
    Bytes32 value1 =
        Bytes32.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000001");
    Bytes32 key2 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45602");
    Bytes32 value2 = Bytes32.fromHexString("0x01");
    Bytes32 key3 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45600");
    Bytes32 value3 = Bytes32.fromHexString("0x00");
    Bytes32 key4 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45603");
    Bytes32 value4 =
        Bytes32.fromHexString("0xf84a97f1f0a956e738abd85c2e0a5026f8874e3ec09c8f012159dfeeaab2b156");
    Bytes32 key5 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45604");
    Bytes32 value5 = Bytes32.fromHexString("0x03");
    Bytes32 key6 =
        Bytes32.fromHexString("0x1e4abaeaa58259f4784e086ddbaa74a9d3975efb2e4380595f0eed5692c45680");
    Bytes32 value6 =
        Bytes32.fromHexString("0x0000010200000000000000000000000000000000000000000000000000000000");
    trie.put(key0, value0);
    trie.put(key1, value1);
    trie.put(key2, value2);
    final Bytes32 expectedIntermediateRootHash = trie.getRootHash();
    trie.put(key3, value3);
    final Bytes32 expectedIntermediateRootHash2 = trie.getRootHash();
    trie.put(key4, value4);
    trie.put(key5, value5);
    trie.put(key6, value6);
    trie.remove(key4);
    trie.remove(key5);
    trie.remove(key6);
    assertThat(trie.getRootHash()).isEqualTo(expectedIntermediateRootHash2);
    trie.remove(key3);
    assertThat(trie.getRootHash()).isEqualTo(expectedIntermediateRootHash);
    trie.remove(key1);
    trie.remove(key0);
    trie.remove(key2);
    assertThat(trie.getRootHash()).isEqualTo(Bytes32.ZERO);
  }
}

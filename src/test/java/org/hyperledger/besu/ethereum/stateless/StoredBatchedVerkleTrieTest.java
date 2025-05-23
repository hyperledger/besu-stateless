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

import org.hyperledger.besu.ethereum.stateless.factory.StoredNodeFactory;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

public class StoredBatchedVerkleTrieTest {

  @Test
  public void testEmptyTrie() {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    trie.commit(nodeUpdater);

    StoredBatchedVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
  }

  @Test
  public void testOneValue() {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    Bytes32 key =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    trie.put(key, value);
    trie.commit(nodeUpdater);

    StoredBatchedVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key).orElse(null)).as("Retrieved value").isEqualTo(value);
  }

  @Test
  public void testDeleteAlreadyDeletedValue() {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    Bytes32 key =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    trie.put(key, value);
    trie.remove(key);
    trie.remove(key);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(Bytes32.ZERO);
  }

  @Test
  public void testTwoValuesAtSameStem() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    Bytes32 key2 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.commit(nodeUpdater);

    StoredBatchedVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
  }

  @Test
  public void testTwoValuesAtDifferentIndex() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
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
    trie.commit(nodeUpdater);

    StoredBatchedVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
  }

  @Test
  public void testTwoValuesWithDivergentStemsAtDepth2() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
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
    trie.commit(nodeUpdater);

    StoredBatchedVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
  }

  @Test
  public void testDeleteThreeValues() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
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
    trie.commit(nodeUpdater);

    StoredBatchedVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
    assertThat(storedTrie.get(key3).orElse(null)).isEqualTo(value3);
  }

  @Test
  public void testDeleteThreeValuesWithFlattening() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
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
    trie.commit(nodeUpdater);

    StoredBatchedVerkleTrie<Bytes32, Bytes32> storedTrie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    assertThat(storedTrie.getRootHash()).isEqualTo(trie.getRootHash());
    assertThat(storedTrie.get(key1).orElse(null)).isEqualTo(value1);
    assertThat(storedTrie.get(key2).orElse(null)).isEqualTo(value2);
    assertThat(storedTrie.get(key3).orElse(null)).isEqualTo(value3);
  }

  @Test
  public void testDeleteManyValuesWithDivergentStemsAtDepth2() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);

    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredBatchedVerkleTrie<Bytes, Bytes> trie =
        new StoredBatchedVerkleTrie<>(
            batchProcessor, new StoredNodeFactory<>(nodeLoader, value -> value));

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

    trie.commit(nodeUpdater);
    StoredBatchedVerkleTrie<Bytes, Bytes> trie2 =
        new StoredBatchedVerkleTrie<>(
            batchProcessor, new StoredNodeFactory<>(nodeLoader, value -> value));
    assertThat(trie2.getRootHash()).isEqualTo(trie.getRootHash());
    trie2.remove(key0);
    trie2.remove(key4);
    trie2.remove(key5);
    trie2.remove(key6);
    trie2.remove(key3);
    trie2.remove(key1);
    trie2.remove(key2);
    assertThat(trie2.getRootHash()).isEqualTo(Bytes32.ZERO);
  }

  @Test
  public void testAddAndRemoveKeysWithMultipleTreeReloads() throws Exception {
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    VerkleTrieBatchHasher batchProcessor = new VerkleTrieBatchHasher();
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);

    trie.put(
        Bytes32.fromHexString("0x1123356d04d4bd662ba38c44cbd79d4108521284d80327fa533e0baab1af9fff"),
        Bytes32.fromHexString(
            "0x4ff50e1454f9a9f56871911ad5b785b7f9966cce3cb12eb0e989332ae2279213"));
    trie.commit(nodeUpdater);
    Bytes32 expectedRootHash = trie.getRootHash();

    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie2 =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    trie2.put(
        Bytes32.fromHexString("0x117b67dd491b9e11d9cde84ef3c02f11ddee9e18284969dc7d496d43c300e500"),
        Bytes32.fromHexString(
            "0x4ff50e1454f9a9f56871911ad5b785b7f9966cce3cb12eb0e989332ae2279213"));
    trie2.commit(nodeUpdater);

    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie3 =
        new StoredBatchedVerkleTrie<Bytes32, Bytes32>(batchProcessor, nodeFactory);
    trie3.remove(
        Bytes32.fromHexString(
            "0x117b67dd491b9e11d9cde84ef3c02f11ddee9e18284969dc7d496d43c300e500"));
    trie3.commit(nodeUpdater);

    assertThat(trie3.getRootHash()).isEqualTo(expectedRootHash);
  }

  @Test
  void addRemoveMultipleKeysInBranchOverThreeBlocks() {
    final NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    final NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);

    final Bytes32 key_0xedb2_BASIC =
        Bytes32.fromHexString("0xedb24b771623f488b0a739020fa9601fc2769b8f62e6de227fcafa23a3651300");
    final Bytes32 value_0xedb2_BASIC =
        Bytes32.fromHexString("0x00000000000000000000000000000000ffffffffffffffffffffffffffffffff");

    final Bytes32 key_0xedb2_CODEHASH =
        Bytes32.fromHexString("0xedb24b771623f488b0a739020fa9601fc2769b8f62e6de227fcafa23a3651301");
    final Bytes32 value_0xedb2_CODEHASH =
        Bytes32.fromHexString("0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470");

    final Bytes32 key_0xed1a_BASIC =
        Bytes32.fromHexString("0xed1a40ec02fddf80cefb80cc58452ce8d585d1a589c6ac88021887e76ef5c500");
    final Bytes32 value_OLD_0xed1a_BASIC =
        Bytes32.fromHexString("0x000000000000000000000000000000000000000f000000000000000000000000");
    final Bytes32 value_NEW_0xed1a_BASIC =
        Bytes32.fromHexString("0x000000000000000000000000000000010000000effffffffffffffffffb36a87");

    final Bytes32 key_0xed1a_CODEHASH =
        Bytes32.fromHexString("0xed1a40ec02fddf80cefb80cc58452ce8d585d1a589c6ac88021887e76ef5c501");
    final Bytes32 value_0xed1a_CODEHASH =
        Bytes32.fromHexString("0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470");

    final Bytes32 key_0xedd9_BASIC =
        Bytes32.fromHexString("0xedd901af49dc1bb5f62c3ab906060dc972e44b6a251333f85690cd1495eda700");
    final Bytes32 value_0xedd9_BASIC =
        Bytes32.fromHexString("0x0000000000000000000000000000000000000000000000000000000000000001");

    final Bytes32 key_0xedd9_CODEHASH =
        Bytes32.fromHexString("0xedd901af49dc1bb5f62c3ab906060dc972e44b6a251333f85690cd1495eda701");
    final Bytes32 value_0xedd9_CODEHASH =
        Bytes32.fromHexString("0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470");

    // add block 0
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredBatchedVerkleTrie<Bytes32, Bytes32> trie =
        new StoredBatchedVerkleTrie<>(new VerkleTrieBatchHasher(), nodeFactory);

    trie.put(key_0xedb2_BASIC, value_0xedb2_BASIC);
    trie.put(key_0xedb2_CODEHASH, value_0xedb2_CODEHASH);
    trie.put(key_0xed1a_BASIC, value_OLD_0xed1a_BASIC);
    trie.put(key_0xed1a_CODEHASH, value_0xed1a_CODEHASH);
    trie.commit(nodeUpdater);

    Bytes32 block0RootHash =
        Bytes32.fromHexString("0x40786ff866d586a069d72de805185594c988743336350fdcbcd82b8ca07327ff");
    assertThat(trie.getRootHash()).isEqualTo(block0RootHash);

    // block 1
    nodeFactory = new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    trie = new StoredBatchedVerkleTrie<>(new VerkleTrieBatchHasher(), nodeFactory);

    trie.put(key_0xed1a_BASIC, value_NEW_0xed1a_BASIC);
    trie.put(key_0xedd9_BASIC, value_0xedd9_BASIC);
    trie.put(key_0xedd9_CODEHASH, value_0xedd9_CODEHASH);
    trie.commit(nodeUpdater);

    Bytes32 block2RootHash =
        Bytes32.fromHexString("0x121a999ce1f99adc775bc3b862e63ebc91144ed3e37f0ec0bc1b6b8d467cf5b1");
    assertThat(trie.getRootHash()).isEqualTo(block2RootHash);

    // rollback 1 -> 0
    nodeFactory = new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    trie = new StoredBatchedVerkleTrie<>(new VerkleTrieBatchHasher(), nodeFactory);

    trie.put(key_0xed1a_BASIC, value_OLD_0xed1a_BASIC);
    trie.remove(key_0xedd9_BASIC);
    trie.remove(key_0xedd9_CODEHASH);
    trie.commit(nodeUpdater);

    assertThat(trie.getRootHash()).isEqualTo(block0RootHash);
  }
}

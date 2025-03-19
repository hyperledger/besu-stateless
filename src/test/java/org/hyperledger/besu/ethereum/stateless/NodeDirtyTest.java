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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hyperledger.besu.ethereum.stateless.factory.StoredNodeFactory;
import org.hyperledger.besu.ethereum.stateless.node.InternalNode;
import org.hyperledger.besu.ethereum.stateless.node.Node;
import org.hyperledger.besu.ethereum.stateless.node.NullNode;
import org.hyperledger.besu.ethereum.stateless.node.StemNode;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

public class NodeDirtyTest {
  @Test
  public void testOneValueSimpleVerkleTrie() {
    List<Boolean> isDirtyExpectedList = List.of(false, false, false);
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<>();
    Bytes32 key =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    trie.put(key, value);
    assertTrue(trie.getRoot().isDirty());
    trie.commit(nodeUpdater);
    assertFalse(trie.getRoot().isDirty());
    List<Node<Bytes32>> nodes = collectNodes(trie.getRoot());
    int index = 0;
    for (Node<Bytes32> node : nodes) {
      assertEquals(isDirtyExpectedList.get(index), node.isDirty());
      ++index;
    }
  }

  @Test
  public void testOneValueStoredVerkleTrie() {
    List<Boolean> isDirtyExpectedList = List.of(false, false, false);
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    NodeLoaderMock nodeLoader = new NodeLoaderMock(nodeUpdater.storage);
    StoredNodeFactory<Bytes32> nodeFactory =
        new StoredNodeFactory<>(nodeLoader, value -> (Bytes32) value);
    StoredVerkleTrie<Bytes32, Bytes32> trie = new StoredVerkleTrie<>(nodeFactory);
    Bytes32 key =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
    trie.put(key, value);
    assertTrue(trie.getRoot().isDirty());
    trie.commit(nodeUpdater);
    assertFalse(trie.getRoot().isDirty());
    List<Node<Bytes32>> nodes = collectNodes(trie.getRoot());
    int index = 0;
    for (Node<Bytes32> node : nodes) {
      assertEquals(isDirtyExpectedList.get(index), node.isDirty());
      ++index;
    }
  }

  @Test
  public void testTwoValues() {
    List<Boolean> isDirtyExpectedList = List.of(false, false, false, false, false);
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<>();
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
    assertTrue(trie.getRoot().isDirty());
    trie.commit(nodeUpdater);
    assertFalse(trie.getRoot().isDirty());
    List<Node<Bytes32>> nodes = collectNodes(trie.getRoot());
    int index = 0;
    for (Node<Bytes32> node : nodes) {
      assertEquals(isDirtyExpectedList.get(index), node.isDirty());
      ++index;
    }
  }

  @Test
  public void testThreeValues() {

    List<Boolean> isDirtyExpectedList = List.of(true, false, false, true, true, true, true);
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<>();

    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key2 =
        Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key3 =
        Bytes32.fromHexString("0x2233445566778899aabbccddeeff00112233445566778899aabbccddeeff0011");
    Bytes32 value3 =
        Bytes32.fromHexString("0x0010000000000000000000000000000000000000000000000000000000000000");

    trie.put(key1, value1);
    assertTrue(trie.getRoot().isDirty());
    trie.commit(nodeUpdater);
    assertFalse(trie.getRoot().isDirty());
    trie.put(key2, value2);
    trie.put(key3, value3);
    assertTrue(trie.getRoot().isDirty());
    List<Node<Bytes32>> nodes = collectNodes(trie.getRoot());
    int index = 0;
    for (Node<Bytes32> node : nodes) {
      assertEquals(isDirtyExpectedList.get(index), node.isDirty());
      ++index;
    }
  }

  @Test
  public void testFourValues() {

    List<Boolean> isDirtyExpectedList =
        List.of(true, false, false, true, true, true, true, false, false);
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<>();

    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key2 =
        Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key3 =
        Bytes32.fromHexString("0x2233445566778899aabbccddeeff00112233445566778899aabbccddeeff0011");
    Bytes32 value3 =
        Bytes32.fromHexString("0x0010000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key4 =
        Bytes32.fromHexString("0x445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233");
    Bytes32 value4 =
        Bytes32.fromHexString("0x0001000000000000000000000000000000000000000000000000000000000000");

    trie.put(key1, value1);
    trie.put(key2, value2);
    assertTrue(trie.getRoot().isDirty());
    trie.commit(nodeUpdater);
    assertFalse(trie.getRoot().isDirty());
    trie.put(key4, value4);
    trie.put(key3, value3);
    assertTrue(trie.getRoot().isDirty());
    List<Node<Bytes32>> nodes = collectNodes(trie.getRoot());
    int index = 0;
    for (Node<Bytes32> node : nodes) {
      assertEquals(isDirtyExpectedList.get(index), node.isDirty());
      ++index;
    }
  }

  @Test
  public void testFiveValues() {

    List<Boolean> isDirtyExpectedList =
        List.of(true, false, false, false, false, false, false, true, true, false, false);
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<>();

    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key2 =
        Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key3 =
        Bytes32.fromHexString("0x2233445566778899aabbccddeeff00112233445566778899aabbccddeeff0011");
    Bytes32 value3 =
        Bytes32.fromHexString("0x0010000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key4 =
        Bytes32.fromHexString("0x445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233");
    Bytes32 value4 =
        Bytes32.fromHexString("0x0001000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key5 =
        Bytes32.fromHexString("0x66778899aabbccddeeff00112233445566778899aabbccddeeff001122334455");
    Bytes32 value5 =
        Bytes32.fromHexString("0x0000100000000000000000000000000000000000000000000000000000000000");

    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.put(key3, value3);
    trie.put(key4, value4);
    assertTrue(trie.getRoot().isDirty());
    trie.commit(nodeUpdater);
    assertFalse(trie.getRoot().isDirty());
    trie.put(key5, value5);
    assertTrue(trie.getRoot().isDirty());
    List<Node<Bytes32>> nodes = collectNodes(trie.getRoot());
    int index = 0;
    for (Node<Bytes32> node : nodes) {
      assertEquals(isDirtyExpectedList.get(index), node.isDirty());
      ++index;
    }
  }

  @Test
  public void testSixValues() {

    List<Boolean> isDirtyExpectedList =
        List.of(
            true, false, false, false, false, false, false, true, true, true, true, false, false);
    NodeUpdaterMock nodeUpdater = new NodeUpdaterMock();
    SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<>();

    Bytes32 key1 =
        Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
    Bytes32 value1 =
        Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key2 =
        Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
    Bytes32 value2 =
        Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key3 =
        Bytes32.fromHexString("0x2233445566778899aabbccddeeff00112233445566778899aabbccddeeff0011");
    Bytes32 value3 =
        Bytes32.fromHexString("0x0010000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key4 =
        Bytes32.fromHexString("0x445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233");
    Bytes32 value4 =
        Bytes32.fromHexString("0x0001000000000000000000000000000000000000000000000000000000000000");

    Bytes32 key5 =
        Bytes32.fromHexString("0x66778899aabbccddeeff00112233445566778899aabbccddeeff001122334455");
    Bytes32 value5 =
        Bytes32.fromHexString("0x0000100000000000000000000000000000000000000000000000000000000000");

    Bytes32 key6 =
        Bytes32.fromHexString("0x8899aabbccddeeff00112233445566778899aabbccddeeff0011223344556677");
    Bytes32 value6 =
        Bytes32.fromHexString("0x0000010000000000000000000000000000000000000000000000000000000000");

    trie.put(key1, value1);
    trie.put(key2, value2);
    trie.put(key3, value3);
    trie.put(key4, value4);
    assertTrue(trie.getRoot().isDirty());
    trie.commit(nodeUpdater);
    assertFalse(trie.getRoot().isDirty());
    trie.put(key5, value5);
    trie.put(key6, value6);
    assertTrue(trie.getRoot().isDirty());
    List<Node<Bytes32>> nodes = collectNodes(trie.getRoot());
    int index = 0;
    for (Node<Bytes32> node : nodes) {
      assertEquals(isDirtyExpectedList.get(index), node.isDirty());
      ++index;
    }
  }

  /**
   * Collects non-null nodes from the given trie node and add them to the list.
   *
   * @param node The trie node to collect nodes from.
   * @param path The path to the current node.
   * @param nodes The list to which collectyed nodes are added.
   * @return The list of collected nodes.
   */
  private List<Node<Bytes32>> collectNodes(
      Node<Bytes32> node, String path, List<Node<Bytes32>> nodes) {
    if (node instanceof NullNode) {
      return nodes;
    }

    nodes.add(node);

    if (node instanceof InternalNode<Bytes32> internalNode) {
      for (int i = 0; i < InternalNode.maxChild(); i++) {
        Bytes index = Bytes.of(i);
        Node<Bytes32> child = internalNode.child((byte) i);
        collectNodes(child, path + index.toString(), nodes);
      }
    } else if (node instanceof StemNode<Bytes32> stemNode) {
      for (int i = 0; i < StemNode.maxChild(); i++) {
        Bytes index = Bytes.of(i);
        Node<Bytes32> child = stemNode.child((byte) i);
        collectNodes(child, path + index.toString(), nodes);
      }
    }
    return nodes;
  }

  /**
   * Collects non-null nodes from the given trie node using root path.
   *
   * @param node The trie node to collect nodes from.
   * @return The list of collected nodes.
   */
  private List<Node<Bytes32>> collectNodes(Node<Bytes32> node) {
    return collectNodes(node, "", new ArrayList<>());
  }
}

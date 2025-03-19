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
package org.hyperledger.besu.ethereum.stateless.node;

import org.hyperledger.besu.ethereum.stateless.factory.NodeFactory;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * Represents a regular node that can possibly be stored in storage.
 *
 * <p>StoredNodes wrap regular nodes and loads them lazily from storage as needed.
 *
 * @param <V> The type of the node's value.
 */
public class StoredInternalNode<V> extends StoredNode<V> {

  /**
   * Constructs a new StoredNode at location.
   *
   * @param nodeFactory The node factory for creating nodes from storage.
   * @param location The location in the tree.
   */
  public StoredInternalNode(final NodeFactory<V> nodeFactory, final Bytes location) {
    super(nodeFactory, location);
  }

  /**
   * Constructs a new StoredNode at location.
   *
   * @param nodeFactory The node factory for creating nodes from storage.
   * @param location The location in the tree.
   * @param hash The hash value of the node.
   */
  public StoredInternalNode(
      final NodeFactory<V> nodeFactory, final Bytes location, final Bytes32 hash) {
    super(nodeFactory, location, hash);
  }

  /**
   * Get a string representation of the node.
   *
   * @return A string representation of the node.
   */
  @Override
  public String print() {
    final Node<V> node = load();
    return String.format("(Stored Internal) %s", node.print());
  }
}

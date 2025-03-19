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
package org.hyperledger.besu.ethereum.stateless.factory;

import org.hyperledger.besu.ethereum.stateless.node.Node;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * An interface representing a factory for creating nodes in the Verkle Trie.
 *
 * @param <V> The type of the nodes to be created.
 */
public interface NodeFactory<V> {

  /**
   * Retrieve a node with the given location and hash.
   *
   * @param location The location of the node.
   * @param hash The hash of the node.
   * @return An optional containing the retrieved node, or empty if not found.
   */
  Optional<Node<V>> retrieve(final Bytes location, final Bytes32 hash);
}

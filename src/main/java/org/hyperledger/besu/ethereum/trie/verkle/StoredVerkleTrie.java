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

import org.hyperledger.besu.ethereum.trie.verkle.factory.NodeFactory;
import org.hyperledger.besu.ethereum.trie.verkle.node.NullNode;

import org.apache.tuweni.bytes.Bytes;

public class StoredVerkleTrie<K extends Bytes, V extends Bytes> extends SimpleVerkleTrie<K, V> {
  protected final NodeFactory<V> nodeFactory;

  /**
   * Create a trie.
   *
   * @param nodeFactory The {@link NodeFactory} to retrieve node.
   */
  public StoredVerkleTrie(final NodeFactory<V> nodeFactory) {
    super(nodeFactory.retrieve(Bytes.EMPTY, null).orElse(NullNode.instance()));
    this.nodeFactory = nodeFactory;
  }
}

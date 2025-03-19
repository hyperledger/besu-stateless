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

import org.hyperledger.besu.ethereum.trie.NodeLoader;

import java.util.Map;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class NodeLoaderMock implements NodeLoader {

  public Map<Bytes, Bytes> storage;

  public NodeLoaderMock(Map<Bytes, Bytes> storage) {
    this.storage = storage;
  }

  @Override
  public Optional<Bytes> getNode(Bytes location, Bytes32 hash) {
    return Optional.ofNullable(storage.get(location));
  }
}

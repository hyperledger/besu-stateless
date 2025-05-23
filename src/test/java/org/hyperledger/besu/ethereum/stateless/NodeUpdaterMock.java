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

import org.hyperledger.besu.ethereum.trie.NodeUpdater;

import java.util.HashMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class NodeUpdaterMock implements NodeUpdater {

  public Map<Bytes, Bytes> storage;

  public NodeUpdaterMock() {
    this.storage = new HashMap<Bytes, Bytes>();
  }

  public NodeUpdaterMock(Map<Bytes, Bytes> storage) {
    this.storage = storage;
  }

  @Override
  public void store(Bytes location, Bytes32 hash, Bytes value) {
    if (value == null) {
      storage.remove(location);
    } else {
      storage.put(location, value);
    }
  }
}

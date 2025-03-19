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
package org.hyperledger.besu.ethereum.stateless.hasher;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.MutableBytes;

/** Defines an interface for a Verkle Trie node hashing strategy. */
public interface Hasher {

  default Bytes prepareScalars(final Bytes[] inputs) {
    return rightPadInput(32, inputs);
  }

  default Bytes rightPadInput(final int size, final Bytes[] inputs) {
    MutableBytes result = MutableBytes.create(inputs.length * size);
    for (int i = 0; i < inputs.length; i++) {
      int offset = i * size;
      if (inputs[i].size() > size) {
        throw new IllegalArgumentException("Input size exceeds the specified padding size");
      }
      result.set(offset, inputs[i]);
    }
    return result;
  }
}

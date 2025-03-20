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
package org.hyperledger.besu.ethereum.stateless.util;

import static org.hyperledger.besu.ethereum.stateless.util.SuffixTreeDescriptor.BALANCE_BYTE_SIZE;
import static org.hyperledger.besu.ethereum.stateless.util.SuffixTreeDescriptor.BALANCE_OFFSET;
import static org.hyperledger.besu.ethereum.stateless.util.SuffixTreeDescriptor.CODE_SIZE_BYTE_SIZE;
import static org.hyperledger.besu.ethereum.stateless.util.SuffixTreeDescriptor.CODE_SIZE_OFFSET;
import static org.hyperledger.besu.ethereum.stateless.util.SuffixTreeDescriptor.NONCE_BYTE_SIZE;
import static org.hyperledger.besu.ethereum.stateless.util.SuffixTreeDescriptor.NONCE_OFFSET;
import static org.hyperledger.besu.ethereum.stateless.util.SuffixTreeDescriptor.VERSION_BYTE_SIZE;
import static org.hyperledger.besu.ethereum.stateless.util.SuffixTreeDescriptor.VERSION_OFFSET;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

public class SuffixTreeDecoder {

  public static Bytes[] decodeBasicDataLeaf(final Bytes32 value) {
    Bytes[] decodedFields = new Bytes[4];

    decodedFields[0] = extractField(value, VERSION_OFFSET, VERSION_BYTE_SIZE);
    decodedFields[1] = extractField(value, CODE_SIZE_OFFSET, CODE_SIZE_BYTE_SIZE);
    decodedFields[2] = extractField(value, NONCE_OFFSET, NONCE_BYTE_SIZE);
    decodedFields[3] = extractField(value, BALANCE_OFFSET, BALANCE_BYTE_SIZE);

    return decodedFields;
  }

  public static Byte decodeVersion(final Bytes32 value) {
    return value.get(VERSION_OFFSET);
  }

  public static int decodeCodeSize(final Bytes32 value) {
    return extractField(value, CODE_SIZE_OFFSET, CODE_SIZE_BYTE_SIZE).toInt();
  }

  public static long decodeNonce(final Bytes32 value) {
    return extractField(value, NONCE_OFFSET, NONCE_BYTE_SIZE).toLong();
  }

  public static UInt256 decodeBalance(final Bytes32 value) {
    return UInt256.fromBytes(extractField(value, BALANCE_OFFSET, BALANCE_BYTE_SIZE));
  }

  private static Bytes extractField(final Bytes32 value, final int offset, int size) {
    return value.slice(offset, size);
  }
}

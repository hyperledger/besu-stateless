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
import org.apache.tuweni.bytes.MutableBytes32;

public class SuffixTreeEncoder {

  /**
   * Sets the new version and encodes it inside `value`.
   *
   * @param value on which to set the version part
   * @param version value for the new version
   * @return updated value with the new version set
   */
  public static Bytes32 setVersionInValue(Bytes32 value, final Bytes version) {
    checkSize(version, VERSION_BYTE_SIZE);
    final MutableBytes32 mutableValue = value.mutableCopy();
    mutableValue.set(VERSION_OFFSET, version);
    return mutableValue;
  }

  /**
   * Sets the new code size and encodes it inside `value`.
   *
   * @param value on which to set the code size part
   * @param codeSize value for the new code size
   * @return updated value with the new code size set
   */
  public static Bytes32 setCodeSizeInValue(final Bytes32 value, final Bytes codeSize) {
    checkSize(codeSize, CODE_SIZE_BYTE_SIZE);
    final MutableBytes32 mutableValue = value.mutableCopy();
    mutableValue.set(CODE_SIZE_OFFSET, codeSize);
    return mutableValue;
  }

  /**
   * Sets the new nonce and encodes it inside `value`.
   *
   * @param value on which to set the nonce part
   * @param nonce value for the new nonce
   * @return updated value with the new nonce set
   */
  public static Bytes32 setNonceInValue(Bytes32 value, final Bytes nonce) {
    checkSize(nonce, NONCE_BYTE_SIZE);
    final MutableBytes32 mutableValue = value.mutableCopy();
    mutableValue.set(NONCE_OFFSET, nonce);
    return mutableValue;
  }

  /**
   * Sets the new balance and encodes it inside `value`.
   *
   * @param value on which to set the balance part
   * @param balance value for the new balance
   * @return updated value with the new balance set
   */
  public static Bytes32 setBalanceInValue(Bytes32 value, final Bytes balance) {
    checkSize(balance, BALANCE_BYTE_SIZE);
    final MutableBytes32 mutableValue = value.mutableCopy();
    mutableValue.set(BALANCE_OFFSET, balance);
    return mutableValue;
  }

  private static void checkSize(final Bytes value, final int requiredSize) {
    if (value.size() != requiredSize) {
      throw new IllegalArgumentException("value should have size=" + requiredSize);
    }
  }
}

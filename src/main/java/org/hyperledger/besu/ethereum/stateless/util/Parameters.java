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

import org.apache.tuweni.units.bigints.UInt256;

public class Parameters {
  public static final UInt256 BASIC_DATA_LEAF_KEY = UInt256.valueOf(0);
  public static final UInt256 CODE_HASH_LEAF_KEY = UInt256.valueOf(1);
  public static final UInt256 HEADER_STORAGE_OFFSET = UInt256.valueOf(64);
  public static final UInt256 CODE_OFFSET = UInt256.valueOf(128);
  public static final UInt256 VERKLE_NODE_WIDTH = UInt256.valueOf(256);
  public static final UInt256 VERKLE_NODE_WIDTH_LOG2 = UInt256.valueOf(8);
  public static final UInt256 MAIN_STORAGE_OFFSET_SHIFT_LEFT_VERKLE_NODE_WIDTH =
      UInt256.ONE.shiftLeft(UInt256.valueOf(8 * 31).subtract(VERKLE_NODE_WIDTH_LOG2).intValue());
  public static final UInt256 HEADER_STORAGE_SIZE = CODE_OFFSET.subtract(HEADER_STORAGE_OFFSET);
}

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

public class SuffixTreeDescriptor {

  protected static final int VERSION_OFFSET = 0;
  protected static final int VERSION_BYTE_SIZE = 1;
  protected static final int CODE_SIZE_OFFSET = 5;
  protected static final int CODE_SIZE_BYTE_SIZE = 3;
  protected static final int NONCE_OFFSET = 8;
  protected static final int NONCE_BYTE_SIZE = 8;
  protected static final int BALANCE_OFFSET = 16;
  protected static final int BALANCE_BYTE_SIZE = 16;
}

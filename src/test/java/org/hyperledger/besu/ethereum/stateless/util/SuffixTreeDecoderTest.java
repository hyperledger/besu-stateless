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

import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SuffixTreeDecoderTest {

  public static Stream<Arguments> decodeValues() {
    return Stream.of(
        Arguments.of(
            Bytes32.ZERO,
            new Bytes[] {
              Bytes.of(0x00), // Version
              Bytes.repeat((byte) 0x00, 3), // Code Size
              Bytes.repeat((byte) 0x00, 8), // Nonce
              Bytes.repeat((byte) 0x00, 16) // Balance
            }),
        Arguments.of(
            Bytes32.fromHexString(
                "0x0A00000000000000000000000000000000000000000000000000000000000000"),
            new Bytes[] {
              Bytes.of(10), // Version
              Bytes.repeat((byte) 0x00, 3), // Code Size
              Bytes.repeat((byte) 0x00, 8), // Nonce
              Bytes.repeat((byte) 0x00, 16) // Balance
            }),
        Arguments.of(
            Bytes32.fromHexString(
                "0x0000000000000036000000000000000000000000000000000000000000000000"),
            new Bytes[] {
              Bytes.of(0x00), // Version
              Bytes.of(0x00, 0x00, 54), // Code Size
              Bytes.repeat((byte) 0x00, 8), // Nonce
              Bytes.repeat((byte) 0x00, 16) // Balance
            }),
        Arguments.of(
            Bytes32.fromHexString(
                "0x0000000000000000000000000000001400000000000000000000000000000000"),
            new Bytes[] {
              Bytes.of(0x00), // Version
              Bytes.repeat((byte) 0x00, 3), // Code Size
              Bytes.fromHexString("0x0000000000000014"), // Nonce
              Bytes.repeat((byte) 0x00, 16) // Balance
            }),
        Arguments.of(
            Bytes32.fromHexString(
                "0x0000000000000000000000000000000000000000000000000000000000000A00"),
            new Bytes[] {
              Bytes.of(0x00), // Version
              Bytes.repeat((byte) 0x00, 3), // Code Size
              Bytes.repeat((byte) 0x00, 8), // Nonce
              Bytes.fromHexString("0x00000000000000000000000000000a00") // Balance
            }),
        Arguments.of(
            Bytes32.fromHexString(
                "0xAB343123BD1231214BC13213EF23434FF213124423247124FF12312EE12AC234"),
            new Bytes[] {
              Bytes.fromHexString("0xAB"), // Version
              Bytes.fromHexString("0x123121"), // Code Size
              Bytes.fromHexString("0x4bc13213ef23434f"), // Nonce
              Bytes.fromHexString("0xf213124423247124ff12312ee12ac234") // Balance
            }));
  }

  @ParameterizedTest
  @MethodSource("decodeValues")
  void decodeBasicDataLeafTest(final Bytes32 basicDataLeafValue, final Bytes[] expected) {
    Bytes[] decodedValues = SuffixTreeDecoder.decodeBasicDataLeaf(basicDataLeafValue);
    for (int i = 0; i < expected.length; i++) {
      Assertions.assertThat(expected[i]).isEqualTo(decodedValues[i]);
    }
  }

  @ParameterizedTest
  @MethodSource("decodeValues")
  void decodeFieldByFieldBasicDataLeafTest(
      final Bytes32 basicDataLeafValue, final Bytes[] expected) {
    Assertions.assertThat(expected[0].get(SuffixTreeDescriptor.VERSION_OFFSET))
        .isEqualTo(SuffixTreeDecoder.decodeVersion(basicDataLeafValue));
    Assertions.assertThat(expected[1].toInt())
        .isEqualTo(SuffixTreeDecoder.decodeCodeSize(basicDataLeafValue));
    Assertions.assertThat(expected[2].toLong())
        .isEqualTo(SuffixTreeDecoder.decodeNonce(basicDataLeafValue));
    Assertions.assertThat(UInt256.fromBytes(expected[3]))
        .isEqualTo(SuffixTreeDecoder.decodeBalance(basicDataLeafValue));
  }
}

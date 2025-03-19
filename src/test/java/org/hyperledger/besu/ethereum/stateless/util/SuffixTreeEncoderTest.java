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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.ValueSource;

public class SuffixTreeEncoderTest {

  private static class StringToBytes32Converter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context)
        throws ArgumentConversionException {
      if (!(source instanceof CharSequence)) {
        throw new ArgumentConversionException("source must be a String");
      }
      String hexString = (String) source;
      return Bytes32.fromHexString(hexString);
    }
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "0x0000000000000000000000000000000000000000000000000000000000000000",
        "0x0100000000000000000000000000000000000000000000000000000000000000"
      })
  void setVersionInValue(
      @ConvertWith(StringToBytes32Converter.class) final Bytes32 basicDataLeafValue) {
    Bytes version = Bytes.of(10);
    assertEquals(
        Bytes.fromHexString("0x0A00000000000000000000000000000000000000000000000000000000000000"),
        SuffixTreeEncoder.setVersionInValue(basicDataLeafValue, version));
  }

  @Test
  void setVersionInValue_differentSizeThrows() {
    Bytes version = Bytes.ofUnsignedShort(10);
    assertThrows(
        IllegalArgumentException.class,
        () -> SuffixTreeEncoder.setVersionInValue(Bytes32.ZERO, version));
  }

  @Test
  void setVersionInValue_fullLeaf() {
    final Bytes32 basicDataLeafValue =
        Bytes32.fromHexString("0xAB343123BD1231214BC13213EF23434FF213124423247124FF12312EE12AC234");
    Bytes version = Bytes.of(10);
    assertEquals(
        Bytes.fromHexString("0x0A343123BD1231214BC13213EF23434FF213124423247124FF12312EE12AC234"),
        SuffixTreeEncoder.setVersionInValue(basicDataLeafValue, version));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "0x0000000000000000000000000000000000000000000000000000000000000000",
        "0x00000000000000AC000000000000000000000000000000000000000000000000"
      })
  void setCodeSizeInValue(
      @ConvertWith(StringToBytes32Converter.class) final Bytes32 basicDataLeafValue) {
    Bytes codeSize = Bytes.repeat((byte) 0, 3).or(Bytes.of(54));
    assertEquals(
        Bytes.fromHexString("0x0000000000000036000000000000000000000000000000000000000000000000"),
        SuffixTreeEncoder.setCodeSizeInValue(basicDataLeafValue, codeSize));
  }

  @Test
  void setCodeSizeInValue_differentSizeThrows() {
    Bytes codeSize = Bytes.ofUnsignedLong(54);
    assertThrows(
        IllegalArgumentException.class,
        () -> SuffixTreeEncoder.setCodeSizeInValue(Bytes32.ZERO, codeSize));
  }

  @Test
  void setCodeSizeInValue_fullLeaf() {
    final Bytes32 basicDataLeafValue =
        Bytes32.fromHexString("0xAB343123BD1231214BC13213EF23434FF213124423247124FF12312EE12AC234");
    Bytes codeSize = Bytes.repeat((byte) 0, 3).or(Bytes.of(54));
    assertEquals(
        Bytes.fromHexString("0xAB343123BD0000364BC13213EF23434FF213124423247124FF12312EE12AC234"),
        SuffixTreeEncoder.setCodeSizeInValue(basicDataLeafValue, codeSize));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "0x0000000000000000000000000000000000000000000000000000000000000000",
        "0x0000000000000000000000000000000100000000000000000000000000000000"
      })
  void setNonceInValue(
      @ConvertWith(StringToBytes32Converter.class) final Bytes32 basicDataLeafValue) {
    Bytes nonce = Bytes.repeat((byte) 0, 8).or(Bytes.of(20));
    assertEquals(
        Bytes.fromHexString("0x0000000000000000000000000000001400000000000000000000000000000000"),
        SuffixTreeEncoder.setNonceInValue(basicDataLeafValue, nonce));
  }

  @Test
  void setNonceInValue_differentSizeThrows() {
    Bytes nonce = UInt256.valueOf(54);
    assertThrows(
        IllegalArgumentException.class,
        () -> SuffixTreeEncoder.setNonceInValue(Bytes32.ZERO, nonce));
  }

  @Test
  void setNonceInValue_fullLeaf() {
    final Bytes32 basicDataLeafValue =
        Bytes32.fromHexString("0xAB343123BD1231214BC13213EF23434FF213124423247124FF12312EE12AC234");
    Bytes nonce = Bytes.repeat((byte) 0, 8).or(Bytes.of(20));
    assertEquals(
        Bytes.fromHexString("0xAB343123BD1231210000000000000014F213124423247124FF12312EE12AC234"),
        SuffixTreeEncoder.setNonceInValue(basicDataLeafValue, nonce));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "0x0000000000000000000000000000000000000000000000000000000000000000",
        "0x00000000000000000000000000000000000000000000000000000000000000F2"
      })
  void setBalanceInValue(
      @ConvertWith(StringToBytes32Converter.class) final Bytes32 basicDataLeafValue) {
    Bytes balance = Bytes.repeat((byte) 0, 16).or(Bytes.fromHexString("0x3635C9ADC5DEA00000"));
    assertEquals(
        Bytes.fromHexString("0x00000000000000000000000000000000000000000000003635C9ADC5DEA00000"),
        SuffixTreeEncoder.setBalanceInValue(basicDataLeafValue, balance));
  }

  @Test
  void setBalanceInValue_differentSizeThrows() {
    Bytes balance = Bytes.repeat((byte) 0, 32).or(Bytes.fromHexString("0x3635C9ADC5DEA00000"));
    assertThrows(
        IllegalArgumentException.class,
        () -> SuffixTreeEncoder.setBalanceInValue(Bytes32.ZERO, balance));
  }

  @Test
  void setBalanceInValue_fullLeaf() {
    final Bytes32 basicDataLeafValue =
        Bytes32.fromHexString("0xAB343123BD1231214BC13213EF23434FF213124423247124FF12312EE12AC234");
    Bytes balance = Bytes.repeat((byte) 0, 16).or(Bytes.fromHexString("0x3635C9ADC5DEA00000"));
    assertEquals(
        Bytes.fromHexString("0xAB343123BD1231214BC13213EF23434f000000000000003635C9ADC5DEA00000"),
        SuffixTreeEncoder.setBalanceInValue(basicDataLeafValue, balance));
  }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import verkle.cryptography.LibIpaMultipoint;

public class TrieCommitmentHasherTest {

  private TrieCommitmentHasher hasher;

  @BeforeEach
  public void setUp() {
    hasher = new TrieCommitmentHasher();
  }

  @Test
  public void testCommit() {
    final Bytes32[] inputs = {Bytes32.ZERO, Bytes32.ZERO};
    final Bytes expectedCommitment =
        Bytes.wrap(LibIpaMultipoint.commit(Bytes.concatenate(inputs).toArray()));

    final Bytes commitment = hasher.commit(inputs);

    assertNotNull(commitment);
    assertEquals(expectedCommitment, commitment);
  }

  @Test
  public void testCommitRoot() {
    final Bytes[] inputs = {Bytes.EMPTY, Bytes.EMPTY};
    final Bytes32 expectedCommitment =
        Bytes32.wrap(LibIpaMultipoint.commitAsCompressed(Bytes.concatenate(inputs).toArray()));

    final Bytes32 commitment = hasher.commitRoot(inputs);

    assertNotNull(commitment);
    assertEquals(expectedCommitment, commitment);
  }

  @Test
  public void testCommitPartialUpdate() {

    final Bytes expectedCommitment =
        Bytes.wrap(
            LibIpaMultipoint.commit(
                Bytes.concatenate(Bytes32.ZERO, Bytes32.leftPad(Bytes.fromHexString("0x01")))
                    .toArray()));

    final List<Byte> indices = List.of((byte) 1);
    final List<Bytes> oldScalars = List.of(Bytes32.ZERO, Bytes32.ZERO);
    final List<Bytes> newScalars = List.of(Bytes32.leftPad(Bytes.fromHexString("0x01")));
    final Optional<Bytes> commitment =
        Optional.of(
            Bytes.wrap(
                LibIpaMultipoint.commit(Bytes.concatenate(Bytes32.ZERO, Bytes32.ZERO).toArray())));
    final Bytes updatedCommitment =
        hasher.commitPartialUpdate(commitment, indices, oldScalars, newScalars);

    assertNotNull(updatedCommitment);
    assertEquals(expectedCommitment, updatedCommitment);
  }

  @Test
  public void testCompress() {
    final Bytes commitment =
        Bytes.wrap(
            LibIpaMultipoint.commit(Bytes.concatenate(Bytes32.ZERO, Bytes32.ZERO).toArray()));
    final Bytes32 expectedCompressed =
        Bytes32.wrap(LibIpaMultipoint.compress(commitment.toArray()));

    final Bytes32 compressed = hasher.compress(commitment);

    assertNotNull(compressed);
    assertEquals(expectedCompressed, compressed);
  }

  @Test
  public void testHash() {
    final Bytes commitment =
        Bytes.wrap(
            LibIpaMultipoint.commit(Bytes.concatenate(Bytes32.ZERO, Bytes32.ZERO).toArray()));
    final Bytes32 expectedHash = Bytes32.wrap(LibIpaMultipoint.hashMany(commitment.toArray()));

    final Bytes32 hash = hasher.hash(commitment);

    assertNotNull(hash);
    assertEquals(expectedHash, hash);
  }

  @Test
  public void testHashMany() {
    final Bytes[] commitments = {
      Bytes.wrap(LibIpaMultipoint.commit(Bytes.concatenate(Bytes32.ZERO, Bytes32.ZERO).toArray())),
      Bytes.wrap(
          LibIpaMultipoint.commit(
              Bytes.concatenate(
                      Bytes32.leftPad(Bytes.fromHexString("0x01")),
                      Bytes32.leftPad(Bytes.fromHexString("0x01")))
                  .toArray()))
    };
    final List<Bytes32> expectedHashes =
        List.of(
            Bytes32.wrap(LibIpaMultipoint.hash(commitments[0].toArray())),
            Bytes32.wrap(LibIpaMultipoint.hash(commitments[1].toArray())));

    final List<Bytes32> hashes = hasher.hashMany(commitments);

    assertNotNull(hashes);
    assertEquals(expectedHashes, hashes);
  }
}

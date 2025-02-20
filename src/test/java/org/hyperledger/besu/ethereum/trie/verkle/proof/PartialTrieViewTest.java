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
package org.hyperledger.besu.ethereum.trie.verkle.proof;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

public class PartialTrieViewTest {

  @Test
  public void test0x62() {
    List<ExecutionWitness.SuffixDiff> diffs = new ArrayList<>();
    diffs.add(new ExecutionWitness.SuffixDiff(97, null));
    List<ExecutionWitness.StemInfo> stemInfos = new ArrayList<>();
    stemInfos.add(
        new ExecutionWitness.StemInfo(
            Bytes.fromHexString("0xab8fbede899caa6a95ece66789421c7777983761db3cfb33b5e47ba10f413b"),
            2,
            2,
            diffs));
    List<Bytes> otherStems = new ArrayList<>();
    List<Bytes32> commitments = new ArrayList<>();
    commitments.add(
        Bytes32.fromHexString(
            "0x4900c9eda0b8f9a4ef9a2181ced149c9431b627797ab747ee9747b229579b583"));
    commitments.add(
        Bytes32.fromHexString(
            "0x491dff71f13c89dac9aea22355478f5cfcf0af841b68e379a90aa77b8894c00e"));
    commitments.add(
        Bytes32.fromHexString(
            "0x525d67511657d9220031586db9d41663ad592bbafc89bc763273a3c2eb0b19dc"));
    ExecutionWitness witness =
        new ExecutionWitness(
            Bytes32.fromHexString(
                "0x2cf2ab8fed2dcfe2fa77da044ab16393dbdabbc65deea5fdf272107a039f2c60"),
            stemInfos,
            new ExecutionWitness.VerificationHint(commitments, otherStems),
            null);

    PartialTrieView view = PartialTrieView.fromExecutionWitness(witness);
    assertThat(view != null).isTrue();
  }
}

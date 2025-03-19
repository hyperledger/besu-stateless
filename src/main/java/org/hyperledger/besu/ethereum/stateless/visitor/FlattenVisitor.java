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
package org.hyperledger.besu.ethereum.stateless.visitor;

import org.hyperledger.besu.ethereum.stateless.VerkleTrieBatchHasher;
import org.hyperledger.besu.ethereum.stateless.node.InternalNode;
import org.hyperledger.besu.ethereum.stateless.node.Node;
import org.hyperledger.besu.ethereum.stateless.node.NullNode;
import org.hyperledger.besu.ethereum.stateless.node.StemNode;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

/**
 * Class representing a visitor for flattening a node in a Trie tree.
 *
 * <p>Flattening a node means that it is merged with its parent, adding one level to the extension
 * path. Per current specs, only StemNodes can have extensions, so only StemNodes can potentially be
 * flattened.
 *
 * @param <V> The type of node values.
 */
public class FlattenVisitor<V> implements NodeVisitor<V> {

  private final Optional<VerkleTrieBatchHasher> batchProcessor;

  public FlattenVisitor(final Optional<VerkleTrieBatchHasher> batchProcessor) {
    this.batchProcessor = batchProcessor;
  }

  @Override
  public Node<V> visit(InternalNode<V> internalNode) {
    return NullNode.nullNode();
  }

  @Override
  public Node<V> visit(StemNode<V> stemNode) {
    final Bytes location = stemNode.getLocation().get();
    final Bytes newLocation = location.slice(0, location.size() - 1);
    // Should not flatten root node
    if (newLocation.isEmpty()) {
      return NullNode.nullNode();
    }
    final StemNode<V> updateStemNode = stemNode.replaceLocation(newLocation);
    updateStemNode.markDirty();
    batchProcessor.ifPresent(
        processor -> {
          final NullNode<V> nullNode = NullNode.nullNode();
          nullNode.markDirty();
          processor.addNodeToBatch(stemNode.getLocation(), nullNode);
          processor.addNodeToBatch(updateStemNode.getLocation(), updateStemNode);
          for (int i = 0; i < StemNode.maxChild(); i++) {
            Byte index = Bytes.of(i).get(0);
            boolean isNullLeafNode =
                stemNode.child(index) instanceof NullNode<V> nullLeafNode && nullLeafNode.isLeaf();
            if (!isNullLeafNode) {
              final NullNode<V> childNullNode = NullNode.newNullLeafNode();
              childNullNode.markDirty();
              processor.addNodeToBatch(stemNode.child(index).getLocation(), childNullNode);
              processor.addNodeToBatch(
                  updateStemNode.child(index).getLocation(), updateStemNode.child(index));
            }
          }
        });
    return updateStemNode;
  }
}

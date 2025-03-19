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
package org.hyperledger.besu.ethereum.stateless.exporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility class for exporting Verkle Trie representations to DOT files. */
public class DotExporter {

  private static final Logger LOG = LogManager.getLogger(DotExporter.class);
  private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("\\.(dot|gv)$");
  private static final String DEFAULT_FILE_NAME = "./VerkleTrie.gv";

  /**
   * Exports the Verkle Trie DOT representation to a '.gv' file located in the current directory.
   * The default file name is "VerkleTrie.gv".
   *
   * @param verkleTrieDotString The DOT representation of the Verkle Trie.
   * @throws IOException If an I/O error occurs during the export process.
   */
  public static void exportToDotFile(String verkleTrieDotString) throws IOException {
    exportToDotFile(verkleTrieDotString, DEFAULT_FILE_NAME);
  }

  /**
   * Exports the Verkle Trie DOT representation to a '.gv' file located at the specified path.
   *
   * @param verkleTrieDotString The DOT representation of the Verkle Trie.
   * @param filePath The location where the DOT file will be saved.
   * @throws IOException If an I/O error occurs during the export process.
   */
  public static void exportToDotFile(String verkleTrieDotString, String filePath)
      throws IOException {
    try {
      if (filePath == null || filePath.isEmpty()) {
        filePath = DEFAULT_FILE_NAME;
      } else {
        Matcher matcher = FILE_EXTENSION_PATTERN.matcher(filePath);
        if (!matcher.find()) {
          throw new IllegalArgumentException("Invalid file extension. Use .dot or .gv extension.");
        }
      }

      Path path = Paths.get(filePath);

      Files.createDirectories(path.getParent());

      try (BufferedWriter writer =
          new BufferedWriter(new FileWriter(path.toString(), StandardCharsets.UTF_8))) {
        writer.write(verkleTrieDotString);
      }

    } catch (AccessDeniedException e) {
      LOG.error(
          "Access denied. Check write permissions for the file. Details: {}", e.getMessage(), e);
      throw e;
    } catch (FileSystemException e) {
      LOG.error(
          "File system issue. Check disk space and file system restrictions. Details: {}",
          e.getMessage(),
          e);
      throw e;
    } catch (IOException e) {
      LOG.error("Error writing DOT file: {}. Details: {}", e.getMessage(), e);
      throw e;
    }
  }
}

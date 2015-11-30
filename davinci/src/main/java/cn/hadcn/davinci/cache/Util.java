/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.hadcn.davinci.cache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Junk drawer of utility methods. */
final class Util {
  static final Charset US_ASCII = Charset.forName("US-ASCII");
  static final Charset UTF_8 = Charset.forName("UTF-8");

  private Util() {
  }

  static String readFully(Reader reader) throws IOException {
    try {
      StringWriter writer = new StringWriter();
      char[] buffer = new char[1024];
      int count;
      while ((count = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, count);
      }
      return writer.toString();
    } finally {
      reader.close();
    }
  }

  /**
   * Deletes the contents of {@code dir}. Throws an IOException if any file
   * could not be deleted, or if {@code dir} is not a readable directory.
   */
  static void deleteContents(File dir) throws IOException {
    File[] files = dir.listFiles();
    if (files == null) {
      throw new IOException("not a readable directory: " + dir);
    }
    for (File file : files) {
      if (file.isDirectory()) {
        deleteContents(file);
      }
      if (!file.delete()) {
        throw new IOException("failed to delete file: " + file);
      }
    }
  }

  static void closeQuietly(/*Auto*/Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (RuntimeException rethrown) {
        throw rethrown;
      } catch (Exception ignored) {
      }
    }
  }

  /**
   * Creates a unique cache key based on a url value
   * file name in linux is limited
   * @param uri
   * 		uri to be used in key creation
   * @return
   * 		cache key value
   */
  public static String generateKey(String uri) {
    String regEx = "[^a-z0-9_-]";
    Pattern p = Pattern.compile(regEx);
    Matcher m = p.matcher(uri);
    String key = m.replaceAll("").trim();
    int length = key.length();
    if ( length <= 120 ) {  //limited by DisLruCache
      return key;
    } else {
      return key.substring(0, 120);
    }
  }
}

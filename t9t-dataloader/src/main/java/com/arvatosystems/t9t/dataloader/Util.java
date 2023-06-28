/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.dataloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class Util {

    /**
     * <p>
     * Checks if a String is whitespace, empty ("") or null.
     * </p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    // -----------------------------------------------------------------------
    /**
     * <p>
     * Removes control characters (char &lt;= 32) from both ends of this String, handling <code>null</code> by returning an empty String ("").
     * </p>
     *
     * <pre>
     * StringUtils.clean(null)          = ""
     * StringUtils.clean("")            = ""
     * StringUtils.clean("abc")         = "abc"
     * StringUtils.clean("    abc    ") = "abc"
     * StringUtils.clean("     ")       = ""
     * </pre>
     *
     * @param str the String to clean, may be null
     * @return the trimmed text, never <code>null</code>
     */
    public static String clean(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * @param fileNameFirstValid a list of file names
     * @return the first valid file (exist and is_file)
     */
    public static File getFile(String... fileNameFirstValid) {
        for (String fileName : fileNameFirstValid) {
            if (fileName == null)
                continue;
            File file = new File(fileName);
            if (!file.exists() || !file.isFile())
                continue; // this file is not available, try next
            return file;
        }
        return null;
    }

    public static InputStream determineInputStream(String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileName);

        InputStream inputStream = null;
        if (hasFileExtension(fileName, "gz")) {
            inputStream = new GZIPInputStream(fileInputStream);
        } else if (hasFileExtension(fileName, "zip")) {
            inputStream = new ZipInputStream(fileInputStream);
        } else {
            inputStream = fileInputStream;
        }
        return inputStream;

    }

    public static String createArchiveFilename(String fileName) {
        StringBuilder stringBuilder = new StringBuilder(fileName);
        // stringBuilder.replace(fileName.lastIndexOf("."), fileName.lastIndexOf(".") + 1, "." + extension + ".");
        stringBuilder.replace(fileName.lastIndexOf("."), fileName.length(), "." + new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()) + ".archive");
        return stringBuilder.toString();
    }

    private static boolean hasFileExtension(String fileName, String extension) {
        return fileName.toLowerCase().lastIndexOf("." + extension.toLowerCase()) > 0;
    }

    public static <T> Set<T> minus(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new LinkedHashSet<T>();
        for (T x : setA)
            if (!setB.contains(x))
                tmp.add(x);
        return tmp;
    }

    public static String setToString(Set<?> set) {
        StringBuffer buf = new StringBuffer();

        for (Object x : set) {
            buf.append(x.toString()).append(",");
        }

        buf.deleteCharAt(buf.length() - 1); // cut last ,
        return buf.toString();
    }

    /**
     * Surround the result of <tt>String.valueOf(aObject)</tt> with single quotes.
     */
    public static String quote(Object aObject, char quote) {
        return quote + String.valueOf(aObject) + quote;
    }

    public static Set<String> setFromIterator(Iterator<?> it) {
        final Set<String> s = new HashSet<String>();
        while (it.hasNext())
            s.add(it.next().toString());
        return s;
    }

}

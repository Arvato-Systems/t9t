/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.dataloader.visitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unzips all encountered JAR files to workDir/jars/{jar.filename} and applies itself upon the extracted directory.
 * <p/>
 * This way JARs inside JARs will be automatically extracted.
 *
 * @author Franz Becker
 *
 */
public class JarUnzipper extends SimpleFileVisitor<Path> {

    final IOFileFilter jarFilter;
    final Logger logger = LoggerFactory.getLogger(getClass());

    private String workDir;

    protected JarUnzipper(Path workPath) {
        createDir(workPath);
        this.workDir = workPath.toFile().toString();
        this.jarFilter = new AndFileFilter(FileFileFilter.FILE, new SuffixFileFilter(".jar", IOCase.INSENSITIVE));
    }

    public JarUnzipper(String workDir) {
        this(Paths.get(workDir, "jars"));
    }

    public String getUnzipDir() {
        return workDir;
    }

    /**
     * Checks if the passed roots contain jar files.
     *
     * @param roots the roots argument
     * @return {@code true} if the passed roots only consist of jar files, {@code false} if no jar file was included
     * @throws IllegalArgumentException if the passed roots have a mix of folders and jar files
     */
    public boolean containsJarFiles(List<String> roots) {
        boolean containsJar = false;
        boolean containsNonJar = false;
        for (String root : roots) {
            File file = new File(root);
            if (jarFilter.accept(file)) {
                containsJar = true;
            } else {
                containsNonJar = true;
            }
        }
        if (containsJar && containsNonJar) {
            String message = "Found a mix of folders and jars in the passed roots, this is not allowed! Roots were: " + roots.toString();
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        return containsJar;
    }

    /**
     * If a jar file is encountered it is unzipped and {@code this} is applied recursively on the destination folder.
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (jarFilter.accept(path.toFile())) {
            logger.info("Unzipping jar: {}", path.getFileName().toString());
            Path destPath = unzip(path);
            logger.debug("Checking for jars recursively within {}", destPath);
            Files.walkFileTree(destPath, new JarUnzipper(destPath));
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Unzips a given JAR file and returns the destination path.
     *
     * @param path the JAR file to extract
     * @return the destination directory, where the JAR has been unzipped
     */
    protected Path unzip(Path path) throws IOException { // throws ArchiverException, IOException {
        File file = path.toFile();

        Path destPath = Paths.get(workDir, FilenameUtils.getBaseName(file.getPath()));
        String outputDir = createDir(destPath);

        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDir, entry.getName());
                entryDestination.getParentFile().mkdirs();
                if (entry.isDirectory())
                    entryDestination.mkdirs();
                else {
                    try (InputStream in = jarFile.getInputStream(entry);
                            OutputStream out = new FileOutputStream(entryDestination);) {
                        IOUtils.copy(in, out);
                    }
                }
            }
        }
        return destPath;
    }

    protected String createDir(Path path) {
        File file = path.toFile();
        file.mkdir();
        if (!file.exists() || !file.isDirectory()) {
            String message = "Could not create directory: " + file.getPath();
            logger.error(message);
            throw new RuntimeException(message);
        }
        return file.getPath();
    }

}

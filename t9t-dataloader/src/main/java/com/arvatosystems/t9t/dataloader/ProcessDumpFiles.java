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
package com.arvatosystems.t9t.dataloader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ProcessDumpFiles extends SimpleFileVisitor<Path> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDumpFiles.class);

    public static boolean export;
    public static String database;
    public static String dbUrl;
    public static String dbUser;
    public static String dbPassword;
    public static String dbDriver;
    public static String dumpFilesFileNameAvailable;
    public static boolean loadDemoData;
    public static String logDumpFile;
    public static String excludedFoldersForDumpFilesSearch = DbSetup.SEP + "bin";
    public static ArrayList<String> loadOnlyFromElements;
    public static String currentProject;

    @Override
    public FileVisitResult visitFile(Path aFile, BasicFileAttributes aAttrs) throws IOException {
        boolean skipFile = false;
        if (excludedFoldersForDumpFilesSearch != null) {
            String[] excludedFolders = excludedFoldersForDumpFilesSearch.split(";");
            for (String excludeFolder : excludedFolders) {
                skipFile |= aFile.toString().contains(excludeFolder);
            }
            logger.info("Following Folders are excluded from dumfile search: ".concat(Arrays.toString(excludedFolders)));
        }

        if (loadOnlyFromElements != null) {
            logger.info("skipFile currently =" + skipFile + " - Load only files from following project: ".concat(loadOnlyFromElements.toString() + ", current project: " + currentProject));
            logger.info("check whether following file should be loaded: " + aFile.toString());

            for (String loadElement : loadOnlyFromElements) {
                if (!loadElement.contains(currentProject))
                    skipFile = true;

                if (!aFile.toString().contains(loadElement.concat("-")))
                    skipFile = true;
            }
        }

        // only consider those dumpfiles for the current project processed in this run.
        if (!aFile.toString().contains(currentProject.concat("-")))
            skipFile = true;


        if ((aFile.toString().contains("sql" + DbSetup.SEP + "DML") || aFile.toString().contains(database + DbSetup.SEP + "DML"))
                && (!skipFile)
                && aFile.getFileName().toString().endsWith(".dump")) {
            if ((loadDemoData && aFile.toString().contains("test" + DbSetup.SEP + "sql" + DbSetup.SEP + "DML"))
                    || (aFile.toString().contains(database + DbSetup.SEP + "DML"))
                    || aFile.toString().contains("main" + DbSetup.SEP + "sql" + DbSetup.SEP + "DML")) {
                logger.debug("\nProcessing file:" + aFile);
                try {
                    new DataLoader(database, aFile.toString(), false, dbUrl, dbUser, dbPassword, dbDriver, false, export).process();
                    try (Writer writer = new FileWriter(new File(dumpFilesFileNameAvailable).getAbsoluteFile(), true)) {
                        writer.append(aFile.getFileName().toString() + System.getProperty("line.separator"));
                    }
                    try (Writer writer = new FileWriter(new File(logDumpFile).getAbsoluteFile(), true)) {
                        writer.append(aFile.getFileName().toString() + (export ? "(export)" : "(import)") + System.getProperty("line.separator"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("DDL was not successful");
                    throw new RuntimeException(e);
                }
            }
        } else {
            logger.info("File skipped");
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(
            Path aDir, BasicFileAttributes aAttrs
            ) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}

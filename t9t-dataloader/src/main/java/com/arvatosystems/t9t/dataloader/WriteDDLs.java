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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class WriteDDLs extends SimpleFileVisitor<Path> {

    private static final Logger logger = LoggerFactory.getLogger(WriteDDLs.class);

    public static String dropFkStatementsFileName;
    public static String dropTableFileName;
    public static String dropSequenceFileName;
    public static String dropViewFileName;
    public static String dropFunctionFileName;
    public static String dropTriggerFileName;
    public static String createOtherStatementsFileName;
    public static String createTableFileName;
    public static String createSequenceFileName;
    public static String createViewFileName;
    public static String createFunctionFileName;
    public static String createTriggerFileName;
    public static String dumpFilesFileNameMax;
    public static String dbType;
    public static String excludedFoldersForSqlSearch = "t arget;bin;src-gen";
    public static boolean skipTablespace;
    public static boolean skipGrants;
    public static String database;
    private String projectFilter;

    public WriteDDLs(String project) {
        projectFilter = project;
    }

    protected boolean isGeneratedSqlFile(String path) {
        final String dbTypePathFragment = dbType + DbSetup.SEP;

        boolean fileNameMatches = (path.contains(dbType + DbSetup.SEP + "Table")
                || path.contains(dbTypePathFragment + "View")
                || path.contains(dbTypePathFragment + "Function")
                || path.contains(dbTypePathFragment + "Trigger")
                || path.contains(dbTypePathFragment + "Sequence")
                || path.contains(dbTypePathFragment + "ForeignKey")
                || path.contains(dbTypePathFragment + "ForeignKey")
                || path.contains(database + DbSetup.SEP + "DDL")
                );
        boolean fileExtensionMatches = path.endsWith(".sql");

        return fileNameMatches && fileExtensionMatches;
    }

    @Override
    public FileVisitResult visitFile(Path aFile, BasicFileAttributes aAttrs) throws IOException {
        final String path = aFile.toFile().getPath();

        boolean skipFile = false;

        boolean fileIsInProjectFilter = true;

        if (excludedFoldersForSqlSearch != null) {
            String[] excludedFolders = excludedFoldersForSqlSearch.split(";");
            for (String excludeFolder : excludedFolders) {
                skipFile |= aFile.toString().contains(excludeFolder);
            }
        }

        if (!projectFilter.isEmpty()) {
            if (!aFile.toString().contains(DbSetup.SEP.concat(projectFilter.concat("-")))) {
                fileIsInProjectFilter = false;
            }
        }

        logger.debug("Checking file:" + aFile + ". contains " + dbType + DbSetup.SEP + "Table" + ": "
                + aFile.toString().contains(dbType + DbSetup.SEP + "Table")
                + ". skipFile: " + skipFile + ". fileIsInProjectFilter: " + fileIsInProjectFilter + " . Current project: " + projectFilter);
        if (isGeneratedSqlFile(path)
                && (!aFile.toString().contains("bin"))
                && (!aFile.toString().contains("src-gen"))
                && (!aFile.getFileName().toString().endsWith("Tablespaces.sql"))
                && (!skipFile)
                && (fileIsInProjectFilter)) {
            logger.info("Processing file:" + aFile);

            try (BufferedReader reader = new BufferedReader(new FileReader(aFile.toString()))) {
                StringBuilder createTableStatements = new StringBuilder();
                StringBuilder createOtherStatements = new StringBuilder();
                StringBuilder createViewStatements = new StringBuilder();
                StringBuilder createSequenceStatements = new StringBuilder();
                StringBuilder createFunctionStatements = new StringBuilder();
                StringBuilder createTriggerStatements = new StringBuilder();
                StringBuilder dropFkStatements = new StringBuilder();
                StringBuilder dropTableStatements = new StringBuilder();
                StringBuilder dropViewStatements = new StringBuilder();
                StringBuilder dropSequenceStatements = new StringBuilder();
                StringBuilder dropFunctionStatements = new StringBuilder();
                StringBuilder dropTriggerStatements = new StringBuilder();
                StringBuilder dumpFiles = new StringBuilder();
                String line = reader.readLine();
                String objectName = "";
                String CMDEND = ";";

                boolean triggerContainsFunction = false;

                while (line != null) {
                    if (aFile.toString().contains(dbType + DbSetup.SEP + "Table")) {
                        /*
                         * Table section
                         */
                        if (line.toUpperCase().contains("CREATE TABLE ")) {
                            createTableStatements.append("-- " + aFile.toString() + DbSetup.LF);
                            // drop table statement
                            objectName = line.substring(line.indexOf("CREATE TABLE ") + 13, line.indexOf("("));
                            dropTableStatements.append("DROP TABLE ");
                            dropTableStatements.append(objectName.replaceAll(" ", ""));
                            if (dbType.equals("POSTGRES"))
                                dropTableStatements.append(" CASCADE");
                            if (dbType.equals("ORACLE"))
                                dropTableStatements.append(" CASCADE CONSTRAINTS");

                            dropTableStatements.append(CMDEND + DbSetup.LF);
                            // log every cfg + dat table as dump file
                            if (!line.toLowerCase().contains("_his_") && !line.toLowerCase().contains("_int_")) {
                                dumpFiles.append(line.substring(line.indexOf("CREATE TABLE ") + 13, line.indexOf("(")).replaceAll("\\s", "") + ".dump");
                                dumpFiles.append(DbSetup.LF);
                            }
                        }
                        // create table statement
                        if (skipTablespace && line.toUpperCase().contains(") TABLESPACE ")) {
                            createTableStatements.append(")");
                            createTableStatements.append(CMDEND + DbSetup.LF);
                        } else if (skipTablespace && line.toUpperCase().contains(") USING INDEX TABLESPACE")) {
                            createTableStatements.append(") USING INDEX");
                            createTableStatements.append(CMDEND + DbSetup.LF);
                        } else if (skipGrants && line.toUpperCase().contains(") GRANT ")) {
                            // do nothing
                        }
                        else {
                            createTableStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                        }
                    } else if (aFile.toString().contains(dbType + DbSetup.SEP + "View")) {
                        /*
                         * View section
                         */
                        if (line.toUpperCase().contains("CREATE OR REPLACE VIEW ")) {
                            createViewStatements.append("-- " + aFile.toString() + DbSetup.LF);
                            // drop view statement
                            objectName = removeUnallowedChars(line.substring(line.indexOf("CREATE OR REPLACE VIEW ") + 23, line.indexOf(" AS")));
                            dropViewStatements.append("DROP VIEW ");
                            dropViewStatements.append(objectName);
                            if (dbType.equals("POSTGRES"))
                                dropViewStatements.append(" CASCADE");
                            dropViewStatements.append(CMDEND + DbSetup.LF);
                        }
                        // create view statement
                        createViewStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                    } else if (aFile.toString().contains(dbType + DbSetup.SEP + "Function")) {
                        /*
                         * Function section
                         */
                        if (line.toUpperCase().contains("CREATE OR REPLACE FUNCTION ") || line.toUpperCase().contains("CREATE FUNCTION ")) {
                            createFunctionStatements.append("-- " + aFile.toString() + DbSetup.LF);
                            // drop view statement
                            String sub = "";
                            if (line.toUpperCase().contains("CREATE OR REPLACE FUNCTION "))
                                sub =  line.substring(line.indexOf("CREATE OR REPLACE FUNCTION ") + 27, line.indexOf(")")+1);
                                if (dbType.equals("ORACLE"))
                                    sub =  line.substring(line.indexOf("CREATE OR REPLACE FUNCTION ") + 27, line.indexOf("("));
                            if (line.toUpperCase().contains("CREATE FUNCTION "))
                                sub =  line.substring(line.indexOf("CREATE FUNCTION ") + 16, line.indexOf(")")+1);
                                if (dbType.equals("ORACLE"))
                                    sub =  line.substring(line.indexOf("CREATE OR REPLACE FUNCTION ") + 27, line.indexOf("("));
                            objectName = removeUnallowedCharsInFunction(sub);
                            if (dbType.equals("POSTGRES")) {
                                dropFunctionStatements.append("DROP FUNCTION IF EXISTS ");
                            } else if (dbType.equals("ORACLE")) {
                                dropFunctionStatements.append("DROP FUNCTION ");
                            }
                            dropFunctionStatements.append(objectName);
                            if (!objectName.contains("(") && !dbType.equals("ORACLE"))
                                dropFunctionStatements.append("()");
                            if (dbType.equals("POSTGRES"))
                                dropFunctionStatements.append(" CASCADE");
                            dropFunctionStatements.append(CMDEND + DbSetup.LF);
                        }
                        // create function statement
                        createFunctionStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                    } else if (aFile.toString().contains(dbType + DbSetup.SEP + "Trigger")) {

                        /*
                         * Trigger section
                         */
                        if (dbType.equals("ORACLE")) {
                            if (line.toUpperCase().contains("CREATE OR REPLACE TRIGGER " )) {
                                createTriggerStatements.append("-- " + aFile.toString() + DbSetup.LF);
                                // drop view statement
                                String sub = line.substring(line.indexOf("CREATE OR REPLACE TRIGGER ") + 26, line.length());
                                objectName = removeUnallowedCharsInFunction(sub);
                                dropTriggerStatements.append("DROP TRIGGER ");
                                dropTriggerStatements.append(objectName);
                                dropTriggerStatements.append(CMDEND + DbSetup.LF);
                            }
                            // create function statement
                            createTriggerStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                        } else if (dbType.equals("POSTGRES")) {
                            if (line.toUpperCase().contains("CREATE TRIGGER " )) {
                                triggerContainsFunction = false;

                                createTriggerStatements.append("-- " + aFile.toString() + DbSetup.LF);
                                // drop view statement
                                String sub = line.substring(line.indexOf("CREATE TRIGGER ") + 15, line.length());
                                objectName = removeUnallowedCharsInFunction(sub);
                                dropTriggerStatements.append("-- " + aFile.toString() + DbSetup.LF);
                                dropTriggerStatements.append("DROP TRIGGER IF EXISTS ");
                                dropTriggerStatements.append(objectName);
                                dropTriggerStatements.append(" ON " + objectName.replace("_tr", ""));
                                dropTriggerStatements.append(" CASCADE;");
                                dropTriggerStatements.append(CMDEND + DbSetup.LF);

                            } else if (line.toUpperCase().contains("CREATE FUNCTION ") || line.toUpperCase().contains("CREATE OR REPLACE FUNCTION ")) {
                                triggerContainsFunction = true;

                                createFunctionStatements.append("-- " + aFile.toString() + DbSetup.LF);
                                // drop view statement
                                String sub = "";
                                if (line.toUpperCase().contains("CREATE OR REPLACE FUNCTION "))
                                    sub =  line.substring(line.indexOf("CREATE OR REPLACE FUNCTION ") + 27, line.indexOf(")")+1);
                                if (line.toUpperCase().contains("CREATE FUNCTION "))
                                    sub =  line.substring(line.indexOf("CREATE FUNCTION ") + 16, line.indexOf(")")+1);
                                objectName = removeUnallowedCharsInFunction(sub);
                                dropFunctionStatements.append("DROP FUNCTION IF EXISTS ");
                                dropFunctionStatements.append(objectName);
                                if (!objectName.contains("("))
                                    dropFunctionStatements.append("()");
                                if (dbType.equals("POSTGRES"))
                                    dropFunctionStatements.append(" CASCADE");
                                dropFunctionStatements.append(CMDEND + DbSetup.LF);

                            }


                            // create trigger statement unless trigger currently does not contain a function
                            if (!triggerContainsFunction) {
                                createTriggerStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                            } else {
                                createFunctionStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                            }
                        }
                    } else if (aFile.toString().contains(dbType + DbSetup.SEP + "Sequence")) {
                        /*
                         * Sequence section
                         */
                        if (line.toUpperCase().contains("CREATE SEQUENCE ")) {
                            createSequenceStatements.append("-- " + aFile.toString() + DbSetup.LF);
                            // drop sequence statement
                            objectName = removeUnallowedChars(line.substring(line.indexOf("CREATE SEQUENCE ") + 16, line.indexOf(";")));
                            // objectName = objectName.replace("NOCACHE", "");
                            dropSequenceStatements.append("DROP SEQUENCE ");
                            dropSequenceStatements.append(objectName);
                            dropSequenceStatements.append(CMDEND + DbSetup.LF);
                        }
                        // create sequence statement
                        if (dbType.equals("MSSQLSERVER")) {
                            createSequenceStatements.append(line.replace(";", CMDEND).replace("NOCACHE", "") + DbSetup.LF);
                        } else {
                            createSequenceStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                        }
                    } else if (aFile.toString().contains(dbType + DbSetup.SEP + "ForeignKey")) {
                        /*
                         * Foreign Key section
                         */
                        if (aFile.toString().contains("drop")) {
                            // drop FK statement
                            dropFkStatements.append("-- " + aFile.toString() + DbSetup.LF);
                            dropFkStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                        } else {
                            // create FK statement
                            createOtherStatements.append("-- " + aFile.toString() + DbSetup.LF);
                            createOtherStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                        }
                    } else {
                        // create any other ddl statement
                        createOtherStatements.append("-- " + aFile.toString() + DbSetup.LF);
                        createOtherStatements.append(line.replace(";", CMDEND) + DbSetup.LF);
                    }
                    line = reader.readLine();
                }
                writeIt(dropFkStatements, dropFkStatementsFileName);
                writeIt(dropTableStatements, dropTableFileName);
                writeIt(dropViewStatements, dropViewFileName);
                writeIt(dropFunctionStatements, dropFunctionFileName);
                writeIt(dropTriggerStatements, dropTriggerFileName);
                writeIt(dropSequenceStatements, dropSequenceFileName);
                writeIt(createOtherStatements, createOtherStatementsFileName);
                writeIt(createTableStatements, createTableFileName);
                writeIt(createViewStatements, createViewFileName);
                writeIt(createFunctionStatements, createFunctionFileName);
                writeIt(createTriggerStatements, createTriggerFileName);
                writeIt(createSequenceStatements, createSequenceFileName);
                writeIt(dumpFiles, dumpFilesFileNameMax);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    private String removeUnallowedChars(String objectName) {
        if (objectName.contains(" ")) {
            objectName = objectName.substring(0, objectName.indexOf(" "));
        }
        return objectName;
    }

    private String removeUnallowedCharsInFunction(String objectName) {
        return objectName.trim();
    }

    private void writeIt(StringBuilder sqlStatements, String fileName) throws IOException {
        try (Writer writer = new FileWriter(new File(fileName).getAbsoluteFile(), true)) {
            writer.append(sqlStatements.toString());
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path aDir, BasicFileAttributes aAttrs) throws IOException {
        logger.debug("Processing directory: " + aDir);
        return FileVisitResult.CONTINUE;
    }

}

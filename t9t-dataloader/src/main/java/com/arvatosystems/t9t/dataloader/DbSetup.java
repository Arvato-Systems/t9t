/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import com.arvatosystems.t9t.dataloader.visitor.JarUnzipper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSetup {

    private static final Logger logger = LoggerFactory.getLogger(DbSetup.class);

    public static final String SEP = System.getProperty("file.separator");
    public static final String LF = System.getProperty("line.separator");
    private static final boolean IS_UNIX = SEP.equals("/");

    private static final Options commandLineOptions = new Options();

    static {
        commandLineOptions.addOption("d", "dbType", true, "REQUIRED: database type (currently POSTGRES, MSSQLSERVER, SAPHANA or ORACLE)");
        commandLineOptions.addOption("s", "sourceDatabase", true, "The source database");
        commandLineOptions.addOption("t", "targetDatabase", true, "The target database");
        commandLineOptions.addOption("w", "workdir", true, "working directory (default is c:\\tmp\\ on MSWin and /tmp/ on *nix)");
        commandLineOptions.addOption("createDDL", false, "Create files containing drop / create table statements and list of max possible dumpfiles");
        commandLineOptions.addOption("exportDumpFiles", false, "Export the table data into dump files");
        commandLineOptions.addOption("importDumpFiles", false, "Import the dump files into the tables");
        commandLineOptions.addOption("runDropTable", false, "Drops all tables from drop statements file");
        commandLineOptions.addOption("runCreateTable", false, "Creates all tables from create statements file");
        commandLineOptions.addOption("runDropSequence", false, "Drop the existing Sequences");
        commandLineOptions.addOption("runCreateSequence", false, "Create the Sequences");
        commandLineOptions.addOption("runCreateFunction", false, "Create the Functions");
        commandLineOptions.addOption("runCreateTrigger", false, "Create the Triggers");
        commandLineOptions.addOption("runDropView", false, "Drop the existing Views");
        commandLineOptions.addOption("runDropFunction", false, "Drop the existing Functions");
        commandLineOptions.addOption("runDropTrigger", false, "Drop the existing Triggers");
        commandLineOptions.addOption("runCreateView", false, "Create the Views");
        commandLineOptions.addOption("skipTablespace", false, "Do not use the Tablespace of the sql statements");
        commandLineOptions.addOption("skipGrants", false, "Do not use the Grants of the sql statements");
        commandLineOptions.addOption("loadDemoData", false, "Load dump data of folders src/test/sql/DML");
        commandLineOptions.addOption("excludedFoldersForSqlSearch", true,
                "Lists folders which should be skipped while visiting sql files. Folder names should be separated by ;");
        commandLineOptions.addOption("excludedFoldersForDumpFilesSearch", true,
                "Lists folders which should be skipped while visiting dump files. Folder names should be separated by ;");
        commandLineOptions.addOption("loadOnlyFromElements", true, "Define semicolon separated list which dump files should be included, e.g. fortytwo;a42");
        commandLineOptions.addOption("projectFilterIncludingOrder", true, "Define semicolon separated list defining the sqls from those projects to be loaded. This also determines the order. E.g. t9t;a28;fortytwo;a42");
        commandLineOptions.addOption("dbUrl", true, "JDBC URL");
        commandLineOptions.addOption("dbUser", true, "Database username");
        commandLineOptions.addOption("dbPassword", true, "Database password. Support plain text and obfuscate password");
    }

    public static void main(String[] args) throws Exception {

        boolean createDDL = false;
        boolean exportDumpFiles = false;
        boolean runDropTable = false;
        boolean runCreateTable = false;
        boolean importDumpFiles = false;
        boolean runDropView = false;
        boolean runCreateView = false;
        boolean runDropSequence = false;
        boolean runCreateSequence = false;
        boolean runCreateFunction = false;
        boolean runDropFunction = false;
        boolean runCreateTrigger = false;
        boolean runDropTrigger = false;
        ProcessDumpFiles.loadDemoData = false;
        WriteDDLs.skipTablespace = false;
        WriteDDLs.skipGrants = false;
        StringBuilder workdirBuffer = new StringBuilder();
        String workdir = null;
        String sourceDatabase = null;
        String targetDatabase = null;
        String logDDLFile = null;

        String[] filenames = null;
        List<String> roots = new ArrayList<String>();

        // Initialize list. Reason: If no projects are given as argument list, the sqls from all jars are included
        ArrayList<String> projectFilterIncludingOrderList = new ArrayList<String>();
        projectFilterIncludingOrderList.add(new String(""));

        // Initialize list. Reason: If no projects are given as argument list, the dumpfiles from all jars are included
        ArrayList<String> loadOnlyFromElementsList = new ArrayList<String>();
//        loadOnlyFromElementsList.add(new String(""));

        /*
         * Check command line options
         */
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(commandLineOptions, args);

            if (cmd.hasOption("sourceDatabase")) {
                sourceDatabase = cmd.getOptionValue("sourceDatabase");
            }
            if (cmd.hasOption("targetDatabase")) {
                targetDatabase = cmd.getOptionValue("targetDatabase");
            }
            if (cmd.hasOption("dbType")) {
                WriteDDLs.dbType = cmd.getOptionValue("dbType");
            }
            if (cmd.hasOption("excludedFoldersForSqlSearch")) {
                WriteDDLs.excludedFoldersForSqlSearch = cmd.getOptionValue("excludedFoldersForSqlSearch");
            }
            if (cmd.hasOption("dbUrl")) {
                ProcessDumpFiles.dbUrl = cmd.getOptionValue("dbUrl");
                ProcessDumpFiles.dbDriver = DbUtils.getDbDriver(ProcessDumpFiles.dbUrl);
            }
            if (cmd.hasOption("dbUser")) {
                ProcessDumpFiles.dbUser = cmd.getOptionValue("dbUser");
            }
            if (cmd.hasOption("dbPassword")) {
                ProcessDumpFiles.dbPassword = cmd.getOptionValue("dbPassword");
            }
            if (cmd.hasOption("dbDriver")) {
                ProcessDumpFiles.dbDriver = cmd.getOptionValue("dbDriver");
            }
            if (cmd.hasOption("workdir")) {
                workdirBuffer.append(cmd.getOptionValue("workdir"));
                if (!workdirBuffer.toString().endsWith(SEP)) {
                    workdirBuffer.append(SEP);
                }
            } else {
                workdirBuffer.append(IS_UNIX ? "/tmp/" : "c:\\tmp\\");
            }
            workdir = workdirBuffer.toString();

            WriteDDLs.dumpFilesFileNameMax = workdir + "dump_files_max.txt";
            WriteDDLs.createOtherStatementsFileName = workdir + "create_other_objects.sql";
            WriteDDLs.createTableFileName = workdir + "create_table_objects.sql";
            WriteDDLs.createViewFileName = workdir + "create_view_objects.sql";
            WriteDDLs.createFunctionFileName = workdir + "create_function_objects.sql";
            WriteDDLs.createTriggerFileName = workdir + "create_trigger_objects.sql";
            WriteDDLs.createSequenceFileName = workdir + "create_sequence_objects.sql";
            WriteDDLs.dropTableFileName = workdir + "drop_table_objects.sql";
            WriteDDLs.dropFkStatementsFileName = workdir + "drop_fk_objects.sql";
            WriteDDLs.dropViewFileName = workdir + "drop_view_objects.sql";
            WriteDDLs.dropFunctionFileName = workdir + "drop_function_objects.sql";
            WriteDDLs.dropTriggerFileName = workdir + "drop_trigger_objects.sql";
            WriteDDLs.dropSequenceFileName = workdir + "drop_sequence_objects.sql";
            ProcessDumpFiles.dumpFilesFileNameAvailable = workdir + "dump_files_available.txt";
            ProcessDumpFiles.logDumpFile = workdir + "dumpFile.log";
            logDDLFile = workdir + "executedDDL.log";

            if (cmd.hasOption("excludedFoldersForDumpFilesSearch")) {
                ProcessDumpFiles.excludedFoldersForDumpFilesSearch = cmd.getOptionValue("excludedFoldersForDumpFilesSearch");
            }

            if (cmd.hasOption("createDDL")) {
                createDDL = true;
            }
            if (cmd.hasOption("exportDumpFiles")) {
                exportDumpFiles = true;
            }
            if (cmd.hasOption("runDropTable")) {
                runDropTable = true;
            }
            if (cmd.hasOption("runCreateTable")) {
                runCreateTable = true;
            }
            if (cmd.hasOption("runCreateFunction")) {
                runCreateFunction = true;
            }
            if (cmd.hasOption("runCreateTrigger")) {
                runCreateTrigger = true;
            }
            if (cmd.hasOption("importDumpFiles")) {
                importDumpFiles = true;
            }
            if (cmd.hasOption("runDropSequence")) {
                runDropSequence = true;
            }
            if (cmd.hasOption("runCreateSequence")) {
                runCreateSequence = true;
            }
            if (cmd.hasOption("runDropView")) {
                runDropView = true;
            }
            if (cmd.hasOption("runDropFunction")) {
                runDropFunction = true;
            }
            if (cmd.hasOption("runDropTrigger")) {
                runDropTrigger = true;
            }
            if (cmd.hasOption("runCreateView")) {
                runCreateView = true;
            }
            if (cmd.hasOption("loadDemoData")) {
                ProcessDumpFiles.loadDemoData = true;
            }
            if (cmd.hasOption("skipTablespace")) {
                WriteDDLs.skipTablespace = true;
            }
            if (cmd.hasOption("skipGrants")) {
                WriteDDLs.skipGrants = true;
            }


            if (cmd.hasOption("projectFilterIncludingOrder")) {
                String elements = cmd.getOptionValue("projectFilterIncludingOrder");
                if (elements != null) {
                    String[] s = elements.toLowerCase().trim().split("\\s*;\\s*");
                    projectFilterIncludingOrderList = new ArrayList<String>(Arrays.asList(s));
                }
            }


            if (cmd.hasOption("loadOnlyFromElements")) {
                String elements = cmd.getOptionValue("loadOnlyFromElements");
                if (elements != null) {
                    String[] s = elements.toLowerCase().trim().split("\\s*;\\s*");
                    loadOnlyFromElementsList = new ArrayList<String>(Arrays.asList(s));
                }
            }

            // fetch the rest, assuming it's a file list
            filenames = cmd.getArgs();
            if (filenames.length > 0) {
                for (int i = 0; i < filenames.length; i++) {
                    roots.add(filenames[i]);
                }
            }

        } catch (ParseException e) {
            logger.error(e.getMessage());
            logger.error("Command line parsing problem. Usage is as follows:");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(DbSetup.class.getName(), commandLineOptions);
            throw new RuntimeException(e);
        }


        if (projectFilterIncludingOrderList.isEmpty()) {
            callDataloader("", targetDatabase, workdir, createDDL, runDropTable, runCreateTable, importDumpFiles, roots, loadOnlyFromElementsList
                    , exportDumpFiles, logDDLFile, sourceDatabase, runDropView, runDropFunction, runCreateView, runCreateFunction, runDropSequence, runCreateSequence, runDropTrigger, runCreateTrigger);
        } else {
            for (String project : projectFilterIncludingOrderList) {
                callDataloader(project, targetDatabase, workdir, createDDL, runDropTable, runCreateTable, importDumpFiles, roots, loadOnlyFromElementsList
                    , exportDumpFiles, logDDLFile, sourceDatabase, runDropView, runDropFunction, runCreateView, runCreateFunction, runDropSequence, runCreateSequence, runDropTrigger, runCreateTrigger);
            }
        }
    }

    private static void callDataloader(String project, String targetDatabase, String workdir, boolean createDDL, boolean runDropTable, boolean runCreateTable, boolean importDumpFiles, List<String> roots, ArrayList<String> loadOnlyFromElementsList
            , boolean exportDumpFiles, String logDDLFile, String sourceDatabase, boolean runDropView, boolean runDropFunction, boolean runCreateView, boolean runCreateFunction, boolean runDropSequence, boolean runCreateSequence
            , boolean runDropTrigger, boolean runCreateTrigger) throws Exception {
        /*
         * Call the dataloader for each project
         */
        logger.info("---------------------------------------------");
        if (!project.equals(""))
            logger.info("In current run include projects : " + project);
        logger.info("DbSetup options:");
        logger.info("db URL:" + ProcessDumpFiles.dbUrl);
        logger.info("db user:" + ProcessDumpFiles.dbUser);
        logger.info("targetDatabase:" + targetDatabase);
        logger.info("dbType:" + WriteDDLs.dbType);
        logger.info("workdir:" + workdir);
        logger.info("createDDL:" + createDDL);
        logger.info("runDropTable:" + runDropTable);
        logger.info("runCreateTable:" + runCreateTable);
        logger.info("importDumpFiles:" + importDumpFiles);
        logger.info("loadDemoData:" + ProcessDumpFiles.loadDemoData);
        logger.info("roots:" + roots);
        logger.info("LoadOnlyFromElements: " + loadOnlyFromElementsList);
        logger.info("---------------------------------------------");

        /*
         * create new empty files for the DDL statements
         */
        if (createDDL) {
            createEmptyFile(WriteDDLs.createTableFileName);
            createEmptyFile(WriteDDLs.createViewFileName);
            createEmptyFile(WriteDDLs.createFunctionFileName);
            createEmptyFile(WriteDDLs.createTriggerFileName);
            createEmptyFile(WriteDDLs.createSequenceFileName);
            createEmptyFile(WriteDDLs.createOtherStatementsFileName);
            createEmptyFile(WriteDDLs.dropTableFileName);
            createEmptyFile(WriteDDLs.dropViewFileName);
            createEmptyFile(WriteDDLs.dropTriggerFileName);
            createEmptyFile(WriteDDLs.dropFunctionFileName);
            createEmptyFile(WriteDDLs.dropSequenceFileName);
            createEmptyFile(WriteDDLs.dropFkStatementsFileName);
            createEmptyFile(WriteDDLs.dumpFilesFileNameMax);
            createEmptyFile(ProcessDumpFiles.logDumpFile);
            createEmptyFile(ProcessDumpFiles.dumpFilesFileNameAvailable);
            createEmptyFile(logDDLFile);

            WriteDDLs.database = targetDatabase;

            /*
             * Extract JAR files (only if just jars were passed)
             */
            JarUnzipper unzipper = new JarUnzipper(workdir);
            if (unzipper.containsJarFiles(roots)) {
                logger.info("JAR files were passed, those will be unzipped.");
                for (String root : roots) {
                    Files.walkFileTree(Paths.get(root), unzipper);
                }
                roots.clear();
                roots.add(unzipper.getUnzipDir());
                logger.info("New roots: {}", roots);
            }

            /*
             * Write DDL statements for all repositories
             */
            try {
                for (String root : roots) {
                    FileVisitor<Path> fileVisitor = new WriteDDLs(project);
                    Files.walkFileTree(Paths.get(root), fileVisitor);
                }
            } catch (IOException e) {
                logger.error("write DDL statements for all repositories was not successful");
                throw new RuntimeException(e);
            }
        }

        /*
         * Export dump files for all repositories
         */
        if (exportDumpFiles) {
            createEmptyFile(ProcessDumpFiles.dumpFilesFileNameAvailable);
            try {
                for (String root : roots) {
                    ProcessDumpFiles.export = true;
                    ProcessDumpFiles.database = sourceDatabase;
                    ProcessDumpFiles.loadOnlyFromElements = loadOnlyFromElementsList;
                    FileVisitor<Path> fileVisitor = new ProcessDumpFiles();
                    Files.walkFileTree(Paths.get(root), fileVisitor);
                }
            } catch (IOException e) {
                logger.error("export of dump file " + ProcessDumpFiles.dumpFilesFileNameAvailable + " was not successful");
                throw new RuntimeException(e);
            }
        }

        /*
         * drop sequences from database
         */
        if (runDropSequence) {
            executeDDLs(WriteDDLs.dropSequenceFileName.toString(), targetDatabase, logDDLFile);
        }

        /*
         * create sequences in database
         */
        if (runCreateSequence) {
            executeDDLs(WriteDDLs.createSequenceFileName.toString(), targetDatabase, logDDLFile);
        }

        if (runDropTrigger) {
            executeDDLs(WriteDDLs.dropTriggerFileName.toString(), targetDatabase, logDDLFile);
        }

        /*
         * drop views from database
         */
        if (runDropView || runDropTable) {
            executeDDLs(WriteDDLs.dropViewFileName.toString(), targetDatabase, logDDLFile);
        }


        if (runDropView || runDropFunction) {
            executeDDLs(WriteDDLs.dropFunctionFileName.toString(), targetDatabase, logDDLFile);
        }


        /*
         * drop constraints and tables from database
         */
        if (runDropTable) {
            executeDDLs(WriteDDLs.dropFkStatementsFileName.toString(), targetDatabase, logDDLFile);
            executeDDLs(WriteDDLs.dropTableFileName.toString(), targetDatabase, logDDLFile);
        }

        /*
         * create tables in database
         */
        if (runCreateTable) {
            executeDDLs(WriteDDLs.createTableFileName.toString(), targetDatabase, logDDLFile);
        }

        if (runCreateFunction) {
            executeDDLs(WriteDDLs.createFunctionFileName.toString(), targetDatabase, logDDLFile);
        }

        /*
         * create views in database
         */
        if (runCreateView) {
            executeDDLs(WriteDDLs.createViewFileName.toString(), targetDatabase, logDDLFile);
        }


        if (runCreateTrigger) {
            executeDDLs(WriteDDLs.createTriggerFileName.toString(), targetDatabase, logDDLFile);
        }

        /*
         * Import dump files for all repositories
         */
        if (importDumpFiles) {
            if (!runDropTable) {
                executeDDLs(WriteDDLs.dropFkStatementsFileName.toString(), targetDatabase, logDDLFile);
            }
            try {
                for (String root : roots) {
                    ProcessDumpFiles.database = targetDatabase;
                    ProcessDumpFiles.export = false;
                    ProcessDumpFiles.loadOnlyFromElements = loadOnlyFromElementsList;
                    ProcessDumpFiles.currentProject = project;
                    FileVisitor<Path> fileVisitor = new ProcessDumpFiles();
                    Files.walkFileTree(Paths.get(root), fileVisitor);
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("import of dump files for all repositories");
                throw new RuntimeException(e);
            }
        }

        /*
         * create FKs in database
         */
        if (runCreateTable || importDumpFiles) {
            executeDDLs(WriteDDLs.createOtherStatementsFileName.toString(), targetDatabase, logDDLFile);
        }

        logger.info("DbSetup END");

    }



    private static void executeDDLs(String filename, String targetDatabase, String logDDLFile) throws Exception {

        logger.debug("--- filename:" + filename);
        logger.debug("--- targetDatabase:" + targetDatabase);
        logger.debug("--- logDDLFile:" + logDDLFile);
        logger.debug("--- dbUrl" + ProcessDumpFiles.dbUrl);
        logger.debug("--- dbUser" + ProcessDumpFiles.dbUser);
        logger.debug("--- dbDriver" + ProcessDumpFiles.dbDriver);

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                StringBuilder sqlStatements = new StringBuilder();
                boolean functionEnded = false;
                boolean statementEnded = false;

                String line = reader.readLine();

                Configurator.createInstance(ProcessDumpFiles.dbUrl, ProcessDumpFiles.dbUser, ProcessDumpFiles.dbPassword, ProcessDumpFiles.dbDriver, targetDatabase);
                DbConnection dbConnection = new DbConnection(targetDatabase);
                Statement sql = dbConnection.getOpenConnection().createStatement();

                // create functions/triggers requires special handling
                if (filename.contains(WriteDDLs.createFunctionFileName) || filename.contains(WriteDDLs.createTriggerFileName)) {

                    if (WriteDDLs.dbType.toUpperCase().equals("POSTGRES")) {
                        while (line != null) {

                            if (filename.contains(WriteDDLs.createFunctionFileName) || filename.contains(WriteDDLs.createTriggerFileName)) {
                                if ((sqlStatements.toString().toUpperCase().contains("CREATE OR REPLACE FUNCTION") || sqlStatements.toString().toUpperCase().contains("CREATE FUNCTION") || sqlStatements.toString().toUpperCase().contains("CREATE OR REPLACE TRIGGER") || sqlStatements.toString().toUpperCase().contains("CREATE TRIGGER")) && line.toUpperCase().contains("LANGUAGE PLPGSQL")) {
                                    statementEnded = true;
                                    sqlStatements.append(line);
                                    sqlStatements.append(System.getProperty("line.separator"));
                                } else if (sqlStatements.toString().toUpperCase().startsWith("DROP TRIGGER IF EXISTS")) {
                                    statementEnded = true;
                                } else if (sqlStatements.toString().toUpperCase().startsWith("CREATE TRIGGER") && (sqlStatements.toString().toUpperCase().contains("CREATE OR REPLACE FUNCTION") || sqlStatements.toString().toUpperCase().contains("CREATE FUNCTION"))) {
                                    statementEnded = true;
                                }

                                // new function/trigger in same file is found
                                if (statementEnded) {
                                    try {
                                        logger.debug("DDl:" + sqlStatements.toString());
                                        sql.execute(sqlStatements.toString().replace("/", ""));
                                        try (Writer writer = new FileWriter(new File(logDDLFile).getAbsoluteFile(), true)) {
                                            writer.append(sqlStatements.toString());
                                        }
                                        sqlStatements = new StringBuilder();
                                        statementEnded = false;
                                    } catch (Exception e) {
                                        logger.error(e.toString());
                                        statementEnded = false;
                                    }

                                }
                            }
                            if (!line.toUpperCase().contains("LANGUAGE PLPGSQL")) {
                                sqlStatements.append(line);
                                sqlStatements.append(System.getProperty("line.separator"));
                            }
                            line = reader.readLine();
                        }

                        //execute last statement (otherwise last function in file is not executed)
                        try {
                            logger.debug("DDl:" + sqlStatements.toString());
                            sql.execute(sqlStatements.toString().replace("/", ""));
                            functionEnded = false;
                        } catch (Exception e) {
                            functionEnded = false;
                            logger.error(e.toString());
                        }
                    } else {
                        while (line != null) {
                            if (filename.contains(WriteDDLs.createFunctionFileName) || filename.contains(WriteDDLs.createTriggerFileName)) {
                                if ((line.toUpperCase().startsWith("CREATE OR REPLACE FUNCTION") || line.toUpperCase().startsWith("CREATE FUNCTION") || line.toUpperCase().startsWith("CREATE OR REPLACE TRIGGER")) && sqlStatements.toString().toUpperCase().contains(";"))
                                    functionEnded = true;

                                // new function/trigger in same file is found
                                if (functionEnded == true && (sqlStatements.toString().toUpperCase().contains("CREATE OR REPLACE FUNCTION") || sqlStatements.toString().toUpperCase().contains("CREATE FUNCTION") || sqlStatements.toString().toUpperCase().contains("CREATE OR REPLACE TRIGGER"))) {
                                    try {
                                        logger.debug("DDl:" + sqlStatements.toString());
                                        sql.execute(sqlStatements.toString().replace("/", ""));
                                        try (Writer writer = new FileWriter(new File(logDDLFile).getAbsoluteFile(), true)) {
                                            writer.append(sqlStatements.toString());
                                        }
                                        sqlStatements = new StringBuilder();
                                        functionEnded = false;
                                    } catch (Exception e) {
                                        logger.error(e.toString());
                                        functionEnded = false;
                                    }

                                }
                            }
                            sqlStatements.append(line);
                            sqlStatements.append(System.getProperty("line.separator"));
                            line = reader.readLine();
                        }

                        //execute last statement (otherwise last function in file is not executed)
                        try {
                            logger.debug("DDl:" + sqlStatements.toString());
                            sql.execute(sqlStatements.toString().replace("/", ""));
                            functionEnded = false;
                        } catch (Exception e) {
                            functionEnded = false;
                            logger.error(e.toString());
                        }
                    }

                } else {
                    // Execute anything else than creating functions
                    while (line != null) {
                        sqlStatements.append(line);
                        sqlStatements.append(System.getProperty("line.separator"));
                        if (line.contains(";")) {
                            logger.debug("DDl:" + sqlStatements.toString());
                            try {
                                sql.execute(sqlStatements.toString().replace(";", "").replace("/", ""));
                            } catch (Exception e) {
                                if (sqlStatements.toString().contains("CREATE SMALLFILE")) {
                                    if (!e.getMessage().toLowerCase().contains("nicht ausreichende berechtigungen")
                                            && !e.getMessage().toLowerCase().contains("insufficient privileges")) {
                                        reader.close();
                                        throw e;
                                    }
                                } else if (!e.getMessage().toLowerCase().contains(" does not exist")
                                        && !e.getMessage().toLowerCase().contains(" nicht vorhanden")
                                        && !e.getMessage().toLowerCase().contains(" existiert nicht")
                                        && !e.getMessage().toLowerCase().contains(" is not a constraint")) {
                                    reader.close();
                                    throw e;
                                }
                            }
                            try (Writer writer = new FileWriter(new File(logDDLFile).getAbsoluteFile(), true)) {
                                writer.append(sqlStatements.toString());
                            }
                            sqlStatements = new StringBuilder();
                        }
                        line = reader.readLine();
                    }
                }
                sql.close();
                dbConnection.closeConnection();
            }
        } catch (FileNotFoundException e) {
            logger.warn("No DDL File available for:" + filename);
            //throw new RuntimeException(e);
        }
    }

    private static void createEmptyFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            // Delete file if it exists
            if (!file.delete()) {
                String message = "Could not delete file: " + file.getPath();
                logger.error(message);
                throw new RuntimeException(message);
            }
        }
        // Create the file
        if (!file.createNewFile()) {
            String message = "Could not create file: " + file.getPath();
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

}

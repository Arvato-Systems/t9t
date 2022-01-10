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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSV Parser: Java CSV parsing with Apache Commons CSV parser.
 *
 * @see <a href="http://www.xinotes.org/notes/note/1378/">http://www.xinotes.org/notes/note/1378/</a>
 *
 */
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    public static final String DBCONFING_FILENAME = "dbconfig.properties";
    public static final String HOMECFG_FILENAME = System.getProperty("user.home") + System.getProperty("file.separator") + DBCONFING_FILENAME;

    private static final Options commandLineOptions = new Options();

    private static final String SECTION_PREFIX = "@@";
    private static final String SECTION_CONFIG = "@@CONFIG";
    private static final String SECTION_DEFAULTS = "@@DEFAULTS";
    private static final String SECTION_PATTERNS = "@@PATTERNS";
    private static final String SECTION_DATA = "@@DATA";

    private static final String CONFING_TABLE_NAME = "table";
    private static final String CONFING_WHERE_CLAUSE = "where";
    private static final String CONFING_ORDERBY_CLAUSE = "order_by";

    private int countTotal = 0;
    private String dbName;
    private InputStream inputStream;
    private String exportFileName;
    private Writer writer;
    private Properties configuration = new OrderedProperties();
    private Properties defaultColumns = new OrderedProperties();
    private Properties patternColumns = new OrderedProperties();
    private boolean verbose = false;
    private boolean deleteOnly = false;
    private boolean export = false;

    static {
        File dbConfigFile = Util.getFile(null, DBCONFING_FILENAME, HOMECFG_FILENAME);
        String availableDbNames = availableDbNames(dbConfigFile);

        commandLineOptions.addOption("db", true, "REQUIRED: The database name" + availableDbNames);
        commandLineOptions.addOption("f", "file", true, "REQUIRED: File name of the xml import/export file.\nsupported are .dump / .gz / .zip ");
        commandLineOptions.addOption("v", false, "Verbose output");
        commandLineOptions.addOption("del", false, "Delete the old data ONYL - no import");
        commandLineOptions.addOption("export", false, "Export the data");
        commandLineOptions.addOption("dbUrl", true, "JDBC URL");
        commandLineOptions.addOption("dbUser", true, "Database username");
        commandLineOptions.addOption("dbPassword", true, "Database password. Support plain text and obfuscate password");
    }

    private static String availableDbNames(File dbConfigFile) {
        StringBuffer buf = new StringBuffer();
        if (dbConfigFile != null) {
            Properties p = new OrderedProperties();
            try {
                p.load(new FileInputStream(dbConfigFile));
                buf.append(": Available: >");
                for (Object object : p.keySet()) {
                    String key = (String) object;
                    if (key.contains(".uri")) {
                        buf.append(key.replaceFirst(".uri", "")).append(",");
                    }
                }
                buf.deleteCharAt(buf.length() - 1);
                buf.append("<");
            } catch (Exception e) {
                logger.info("Problems to determine configurations: {}" + e.getMessage());
            }
        }
        return buf.toString();
    }

    public DataLoader(String dbName, String fileName, boolean verbose, String dbUrl, String dbUser, String dbPassword, String dbDriver, boolean deleteOnly, boolean export) throws IOException {
        File importFile = Util.getFile(fileName);
        if (importFile == null) {
            throw new FileNotFoundException("File " + fileName + " is not existig or is not a file");
        }
        this.dbName = dbName;
        this.verbose = verbose;
        this.deleteOnly = deleteOnly;
        this.export = export;

        if (export) {
            File newFile = new File(Util.createArchiveFilename(fileName));
            boolean wasSuccesful = importFile.renameTo(newFile);
            if (!wasSuccesful) {
                logger.error("rename was not successful");
                throw new RuntimeException();
            }
            this.inputStream = Util.determineInputStream(newFile.getAbsolutePath());

            this.exportFileName = fileName;
            this.writer = new FileWriter(new File(this.exportFileName).getAbsoluteFile());
        }
        else {
            this.inputStream = Util.determineInputStream(fileName);
        }

        if (dbUrl == null || dbUser == null || dbPassword == null || dbDriver == null) {
            throw new IllegalArgumentException("Missing DB configuration");
        }

        Configurator.createInstance(dbUrl, dbUser, dbPassword, dbDriver, dbName);

    }

    public void process() throws Exception {

        String line;
        try (ConfigReader configReader = new ConfigReader(new InputStreamReader(this.inputStream, Charset.forName("UTF-8")))) {
            while ((line = configReader.readLine()) != null) {
                line = readConfiguration(line, configReader);
                if (line.startsWith(SECTION_DATA)) {
                    configReader.stopStoreing();
                    if (export) {
                        long startTime = System.currentTimeMillis();
                        DbConnection dbConnection = new DbConnection(this.dbName);
                        logger.debug("Save: Write DATA in section {}", SECTION_DATA);
                        logger.debug("Save: Export file is {}", this.exportFileName);

                        try (BufferedWriter bufferedWriter = new BufferedWriter(this.writer)) {
                            // save configuration
                            bufferedWriter.write(configReader.getStoreConfig());
                            Set<String> selectColumns = DbUtils.getSelectColumnsWithoutExclued(dbConnection, configuration.getProperty(CONFING_TABLE_NAME),
                                    defaultColumns.keySet());
                            CSVPrinter printer = initCsvPrinter(bufferedWriter);
                            bufferedWriter.write(Util.setToString(selectColumns) + "\n");
                            writeCsvDate(dbConnection, printer, selectColumns);

                            logger.debug("Save: Processed {} entries. Time: {} ms.", countTotal, System.currentTimeMillis() - startTime);
                        }
                        this.writer.close();

                    } else {
                        long startTime = System.currentTimeMillis();

                        DbConnection dbConnection = new DbConnection(this.dbName);
                        // delete old data
                        DbUtils.deleteOldData(dbConnection, configuration.getProperty(CONFING_TABLE_NAME), configuration.getProperty(CONFING_WHERE_CLAUSE));

                        // load new data
                        if (!this.deleteOnly) {
                            logger.debug("Load: Read DATA in section {}", SECTION_DATA);
                            CSVParser parser = initCsvParser(configReader);
                            loadCsvDate(dbConnection, parser);
                            logger.debug("Load: Processed {} entries. Time: {} ms.", countTotal, System.currentTimeMillis() - startTime);
                        }
                    }
                }
            }
        }
        this.inputStream.close();

    }

    private String readConfiguration(String line, BufferedReader bufferedReader) throws IOException {
        StringBuffer readedLine = new StringBuffer(); // it is just a data
        // container for look back
        // in the stream

        if (line.startsWith(SECTION_CONFIG)) {
            logger.debug("Config: Read configuration in section {}", SECTION_CONFIG);
            this.configuration = getConfigProps(bufferedReader, readedLine);
            line = readedLine.toString();
            checkConfiguration(this.configuration);
            line = readConfiguration(line, bufferedReader); // call self, if
            // sections are not
            // in expected order
        }
        if (line.startsWith(SECTION_DEFAULTS)) {
            logger.debug("Config: Read configuration in section {}", SECTION_DEFAULTS);
            this.defaultColumns = getConfigProps(bufferedReader, readedLine);
            line = readedLine.toString();
            checkDefaultColumns(this.defaultColumns);
            line = readConfiguration(line, bufferedReader); // call self, if
            // sections are not
            // in expected order
        }
        if (line.startsWith(SECTION_PATTERNS)) {
            logger.debug("Config: Read configuration in section {}", SECTION_PATTERNS);
            this.patternColumns = getConfigProps(bufferedReader, readedLine);
            line = readedLine.toString();
            line = readConfiguration(line, bufferedReader); // call self, if
            // sections are not
            // in expected order
        }
        return line;
    }

    private void writeCsvDate(DbConnection dbConnection, CSVPrinter printer, Set<String> selectColumns) throws SQLException, IOException {
        String select = DbUtils.getSqlSelect(dbConnection, configuration.getProperty(CONFING_TABLE_NAME), configuration.getProperty(CONFING_WHERE_CLAUSE),
                configuration.getProperty(CONFING_ORDERBY_CLAUSE), selectColumns);
        logger.debug("Save: Using statment: {}", select);

        try (PreparedStatement preparedStatement = dbConnection.getOpenConnection().prepareStatement(select);
             ResultSet resultSet = preparedStatement.executeQuery();
        ) {
            String[] values = new String[selectColumns.size()];
            while (resultSet.next()) {
                countTotal++;
                int i = 0;
                for (String column : selectColumns) {
                    values[i++] = DbTypeConverter.convertToString(resultSet, column);
                }

                printer.println(values);
                printer.flush();
            }
        }
        dbConnection.closeConnection();
    }

    private void loadCsvDate(DbConnection dbConnection, CSVParser parser) throws Exception {

        Map<String, Integer> sqlTypes = DbUtils.getDbTypesForTable(dbConnection, configuration.getProperty(CONFING_TABLE_NAME));

        String[] columnNames = parser.getLine(); // first line with column names
        String insert = DbUtils.getSqlInsert(configuration.getProperty(CONFING_TABLE_NAME), sqlTypes.keySet(), columnNames, defaultColumns);
        logger.debug("Load: Using statment: {}", insert);

        String[] values = parser.getLine();
        while (values != null) {
            countTotal++;
            if (verbose) {
                printValues(parser.getLineNumber(), values);
            }
            try (PreparedStatement preparedStatement = dbConnection.getOpenConnection().prepareStatement(insert)) {
                dbConnection.setAutoCommit(true);
                int valueColumnCount = 1; // DB world ... it starts from 1 not from
                // 0

                // default columns
                for (Enumeration<?> en = defaultColumns.propertyNames(); en.hasMoreElements();) {
                    String key = (String) en.nextElement();
                    String value = defaultColumns.getProperty(key);
                    if (!sqlTypes.keySet().contains(key)) {
                        continue;
                    }
                    if (value.startsWith("@RAW")) {
                        continue; // is not set as a bind variable
                    } else if (value.startsWith("@JAVA")) {
                        // changed for HANA:
                        // Integer sqlType = sqlTypes.get(key);
                        // preparedStatement.setObject(valueColumnCount, DbTypeConverter.getDefault(sqlType));
                        preparedStatement.setDate(valueColumnCount, new Date(System.currentTimeMillis()));
                        valueColumnCount++;
                    } else {
                        Integer sqlType = sqlTypes.get(key);
                        String pattern = this.patternColumns.getProperty(value);
                        preparedStatement.setObject(valueColumnCount, DbTypeConverter.convert(sqlType, value, pattern));
                        valueColumnCount++;
                    }

                }

                // csv columns
                for (int i = 0; i < values.length; i++) {
                    logger.debug("Process column {} ", columnNames[i].toString());
                    Integer sqlType = sqlTypes.get(columnNames[i]);
                    String pattern = this.patternColumns.getProperty(columnNames[i]);
                    if ("null".equals(values[i])) { // remap to null
                        preparedStatement.setObject(valueColumnCount, DbTypeConverter.convert(sqlType, null, null));
                    } else {
                        preparedStatement.setObject(valueColumnCount, DbTypeConverter.convert(sqlType, values[i], pattern));
                    }
                    valueColumnCount++;
                }

                DbUtils.executeBatch(dbConnection, preparedStatement);
            }

            values = parser.getLine();
        }

        dbConnection.closeConnection();

    }

    private CSVParser initCsvParser(BufferedReader br) {
        CSVStrategy strategy = CSVStrategy.DEFAULT_STRATEGY;
        strategy.setDelimiter(',');
        strategy.setEncapsulator('\'');
        CSVParser parser = new CSVParser(br, strategy);
        return parser;
    }

    private CSVPrinter initCsvPrinter(BufferedWriter bw) {
        CSVStrategy strategy = CSVStrategy.DEFAULT_STRATEGY;
        strategy.setDelimiter(',');
        strategy.setEncapsulator('\'');
        return new CSVPrinter(bw, strategy);
    }

    private static void printValues(int lineNumber, String[] as) {
        logger.debug("Line " + lineNumber + " has " + as.length + " values:");
        for (String s : as) {
            logger.debug("\t|" + s + "|");
        }
        logger.debug("");
    }

    private Properties getConfigProps(BufferedReader br, StringBuffer readedLine) throws IOException {
        StringBuffer buf = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            // clean buffer and save the last line for further processing
            // outside
            readedLine.delete(0, readedLine.length()).append(line);
            if (line.startsWith(SECTION_PREFIX)) {
                break; // check if another TAG begins
            }
            if (!Util.isBlank(line)) {
                line = line.replaceFirst("--.*", ""); // remove potential
                // comments
                buf.append(Util.clean(line)).append("\n"); // clean line and
                // append + \n
            }
        }

        OrderedProperties properties = new OrderedProperties();
        properties.load(new StringReader(buf.toString()));
        properties.setRAW(buf.toString());

        return properties;

    }

    private void checkConfiguration(Properties configuration) {
        if (!configuration.containsKey(CONFING_TABLE_NAME)) {
            if (configuration instanceof OrderedProperties) {
                logger.error("RAW Config:\n{}", ((OrderedProperties) configuration).getRAW());
            }
            configuration.list(System.out);
            throw new IllegalArgumentException("No 'table' existing in " + SECTION_CONFIG + " section");
        }
        logger.debug("Config: Using table:     {}", configuration.getProperty(CONFING_TABLE_NAME));

        if (!configuration.containsKey(CONFING_WHERE_CLAUSE)) {
            logger.warn("******* No 'where' clause existing in {} section. Be aware that all data will be deleted", SECTION_CONFIG);
            if (configuration instanceof OrderedProperties) {
                logger.error("RAW Config:\n{}", ((OrderedProperties) configuration).getRAW());
            }
        }
        logger.debug("Config: Using where:     {}", configuration.getProperty(CONFING_WHERE_CLAUSE, "n/a"));

        if (configuration.containsKey(CONFING_ORDERBY_CLAUSE)) {
            logger.debug("Config: Using order_by:  {}", configuration.getProperty(CONFING_ORDERBY_CLAUSE, "n/a"));
        }
        if (verbose) {
            logger.debug("RAW Config:\n{}", ((OrderedProperties) configuration).getRAW());
        }
    }

    private void checkDefaultColumns(Properties defaultColumns) {
        if (defaultColumns.isEmpty()) {
            logger.info("No default columns configured in {} section.", SECTION_DEFAULTS);
        } else {
            for (Enumeration<?> en = defaultColumns.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                String workingValue = defaultColumns.getProperty(key);
                String old = null;
                if (workingValue.startsWith("${ENV.")) {
                    String changed = workingValue.replaceFirst("^\\$\\{ENV\\.", "").replaceFirst("}$", "");
                    changed = System.getenv(changed);
                    if (!System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                        // fix for non-windows OS
                        changed = System.getProperty("user.name");
                    }
                    defaultColumns.setProperty(key, changed);
                    old = old == null ? new String(workingValue) : old;
                    workingValue = defaultColumns.getProperty(key);
                }
                if (workingValue.startsWith("${")) {
                    String changed = workingValue.replaceFirst("^\\$\\{", "").replaceFirst("}$", "");
                    changed = System.getProperty(changed);
                    defaultColumns.setProperty(key, changed);
                    old = old == null ? new String(workingValue) : old;
                    workingValue = defaultColumns.getProperty(key);
                }
                if (workingValue.startsWith("'")) {
                    String changed = workingValue.replaceFirst("^'", "").replaceFirst("'$", "");
                    defaultColumns.setProperty(key, changed);
                    old = old == null ? new String(workingValue) : old;
                    workingValue = defaultColumns.getProperty(key);
                }
                old = old != null ? new String("[original: " + old + "]") : ""; // make
                // it
                // nice
                logger.debug("Config: Using default column:     {} = {} {}", key, defaultColumns.getProperty(key), old);
            }
        }
    }

    public static void main(String[] args) {
        String version = Configurator.getMavenVersion(DataLoader.class);
        logger.info("DataLoader - Version: " + version);

        String dbName = null;
        String[] filenames = null;
        boolean verbose = false;
        boolean deleteOnly = false;
        boolean export = false;
        String dbUrl = null;
        String dbUser = null;
        String dbPassword = null;
        String dbDriver = null;

        try {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(commandLineOptions, args);

            if (cmd.hasOption('f')) {
                filenames = new String[] { cmd.getOptionValue('f') };
            }
            else {
                // fetch the rest, assuming it's a file list
                filenames = cmd.getArgs();
            }

            if (cmd.hasOption("db")) {
                dbName = cmd.getOptionValue("db");
            }

            if (cmd.hasOption("dbUrl")) {
                dbUrl = cmd.getOptionValue("dbUrl");
                dbDriver = DbUtils.getDbDriver(dbUrl);
            }

            if (cmd.hasOption("dbUser")) {
                dbUser = cmd.getOptionValue("dbUser");
            }

            if (cmd.hasOption("dbPassword")) {
                dbPassword = cmd.getOptionValue("dbPassword");
            }

            if (cmd.hasOption('v')) {
                verbose = true;
            }

            if (cmd.hasOption("del")) {
                deleteOnly = true;
            }

            if (cmd.hasOption("export")) {
                export = true;
            }

            if (dbName == null || filenames.length == 0) {
                File dbConfig = Util.getFile(dbUrl, DBCONFING_FILENAME, HOMECFG_FILENAME); // why do we do this 3 times? should respect DRY principle
                String availableDbNames = availableDbNames(dbConfig);
                if (dbConfig != null) {
                    logger.debug("Get configurations from: {}", dbConfig.getAbsolutePath());
                }
                if (!availableDbNames.isEmpty()) {
                    logger.info("Found database names{}", availableDbNames);
                }
                throw new ParseException("");
            }
        } catch (ParseException e) {
            logger.error(e.getMessage());
            logger.error("Command line parsing problem. Usage is as follows:");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(DataLoader.class.getName(), commandLineOptions);
            throw new RuntimeException(e);
        }

        logger.info("Start DataLoader {} with: Type:{} - Config:{}", export ? "EXPORT" : "", dbName, dbUrl != null ? dbUrl : "n/a");
        if (deleteOnly && !export) {
            logger.info("*****************************************");
            logger.info("  Old data will be deleted - no import");
            logger.info("*****************************************");
        } else if (deleteOnly) {
            logger.info("  DELETE will be skipped - Export only!!");
        }

        try {
            if (filenames.length > 0) {
                List<File> files = new ArrayList<File>();
                for (String filename : filenames) {
                    files.add(Util.getFile(filename));
                }
                for (File fileToProcess : files) {
                    logger.debug("Config: Process dump file:       {}", fileToProcess.getAbsolutePath());
                    new DataLoader(dbName, fileToProcess.getAbsolutePath(), verbose, dbUrl, dbUser, dbPassword, dbDriver, deleteOnly, export).process();
                }
            }
        } catch (Exception e) {
            if (verbose) {
                logger.error("Unexpected error!", e);
            } else {
                logger.error("Unexpected error! - " + e.getMessage());
            }
            logger.error("Unexpected error! - " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    class ConfigReader extends BufferedReader {

        private StringBuffer config = new StringBuffer();
        private boolean stop = false;

        public ConfigReader(Reader in) {
            super(in);
        }

        @Override
        public String readLine() throws IOException {
            String line = super.readLine();
            store(line);
            return line;
        }

        private void store(String line) {
            if (!stop) {
                config.append(line).append("\n");
            }
        }

        private void stopStoreing() {
            this.stop = true;
        }

        private String getStoreConfig() {
            return this.config.toString();
        }
    }

}

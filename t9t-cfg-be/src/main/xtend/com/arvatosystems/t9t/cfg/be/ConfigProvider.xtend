/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.cfg.be

import com.arvatosystems.t9t.base.T9tException
import de.jpaw.annotations.AddLogger
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import java.io.File
import java.io.PrintWriter
import java.io.StringReader
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer
import java.util.UUID

@AddLogger
class ConfigProvider {
    static final String DEFAULT_CFG_FILENAME = System.getProperty("user.home") + "/.t9tconfig.xml"
    static final ConcurrentMap<String, String> customParameters = new ConcurrentHashMap<String, String>(16);
    static final char EQUALS_SIGN = '=';
    static final Map<String, UplinkConfiguration> uplinks = new ConcurrentHashMap
    static final Map<String, EncryptionConfiguration> encryptions = new ConcurrentHashMap

    static val postgresConfig = new T9tServerConfiguration => [
        persistenceUnitName         = "t9t-DS"    // hibernate / resourceLocal / postgres
        databaseConfiguration       = new RelationalDatabaseConfiguration => [
            username                = "fortytwo"
            password                = "secret/changeMe"  // this is not a real password!
            databaseBrand           = DatabaseBrandType.POSTGRES
            jdbcDriverClass         = "org.postgresql.Driver"
            jdbcConnectString       = "jdbc:postgresql://127.0.0.1:5432/fortytwo"
            migrations              = #[ "classpath:t9t-sql-migration/POSTGRES/Migration=fw_t9t" ]
        ]
        noSqlConfiguration          = new NoSqlDatabaseConfiguration => [
            strategy                = "JDBC"  // fall back to JDBC instead some specific noSQL db
//            hostname                = "localhost"
//            port                    = 3000
//            schemaName              = "test"
        ]
        keyPrefetchConfiguration    = new KeyPrefetchConfiguration => [
            strategy                = "lazySequenceJDBC"
            defaultKeyChunkSize     = 100
            prefetchedKeyChunkSize  = 20
            numKeyFetchThreads      = 1
            useSequencePerTable     = Boolean.TRUE
        ]
        logWriterConfiguration      = new LogWriterConfiguration => [
            strategy                = "asynchronous"
            queueSize               = 1000  // currently ignore
        ]
        awsConfiguration            = new AWSConfiguration => [
            snsEndpoint             = "https://sns.eu-central-1.amazonaws.com"
            sqsEndpoint             = "https://sqs.eu-central-1.amazonaws.com"
        ]
        searchConfiguration         = new SearchConfiguration => [
            strategy                = "SOLR"
            defaultUrl              = "http://localhost:8880/solr6"
        ]
        asyncMsgConfiguration       = new AsyncTransmitterConfiguration => [
            strategy                = "noop"  // "LTQ" (LinkedTransferQueue) is default implementation for local queues
            maxMessageAtStartup     =   100
            timeoutIdleGreen        =   500
            timeoutIdleRed          =  5000
            timeoutShutdown         =  1000
            timeoutExternal         =  1000
            waitAfterExtError       = 10000
            waitAfterDbErrors       = 60000
        ]
    ]

    static T9tServerConfiguration myConfiguration = postgresConfig

    def private static getContext() {
        return JAXBContext.newInstance("com.arvatosystems.t9t.cfg.be")
    }

    def static configToFile(T9tServerConfiguration cfg, String filename) {
        val m = context.createMarshaller
        val writer = new PrintWriter(filename, "UTF-8")
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        m.marshal(cfg, writer)
        writer.close
    }

    def static configFromFile(String filename) {
//        val reader = new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8)
        val u = context.createUnmarshaller
        val cfg = u.unmarshal(new File(filename))
        return cfg as T9tServerConfiguration
    }

    def static configFromString(String data) {
        val u = context.createUnmarshaller
        val cfg = u.unmarshal(new StringReader(data))
        return cfg as T9tServerConfiguration
    }

    def static T9tServerConfiguration getConfiguration() {
        return myConfiguration
    }

    def static boolean isMocked(String what) {
        if (myConfiguration.mocks === null) {
           return false
        }
        return myConfiguration.mocks.contains(what)
    }

    /** Returns true of the shadow database should be used for queries of a specific module, if possible. */
    def static boolean useShadowDatabase(String moduleName) {
        val applConfig = myConfiguration.applicationConfiguration;
        if (applConfig === null || applConfig.useShadowDatabaseForModule === null) {
            return false;
        }
        return applConfig.useShadowDatabaseForModule.contains(moduleName)
    }

    def private static void mergeConfigurations(T9tServerConfiguration a, T9tServerConfiguration b, Consumer<T9tServerConfiguration> customizer) {
        myConfiguration = new T9tServerConfiguration => [
            serverIdSelf            = a.serverIdSelf            ?: b.serverIdSelf
            stagingType             = a.stagingType             ?: b.stagingType
            persistenceUnitName     = a.persistenceUnitName     ?: b.persistenceUnitName
            databaseConfiguration   = a.databaseConfiguration   ?: b.databaseConfiguration
            secondaryDatabaseConfig = a.secondaryDatabaseConfig ?: b.secondaryDatabaseConfig ?: databaseConfiguration
            shadowDatabaseConfig    = a.shadowDatabaseConfig    ?: b.shadowDatabaseConfig
            noSqlConfiguration      = a.noSqlConfiguration      ?: b.noSqlConfiguration
            keyPrefetchConfiguration= a.keyPrefetchConfiguration?: b.keyPrefetchConfiguration
            logWriterConfiguration  = a.logWriterConfiguration  ?: b.logWriterConfiguration
            applicationConfiguration= a.applicationConfiguration?: b.applicationConfiguration
            serverConfiguration     = a.serverConfiguration     ?: b.serverConfiguration
            bpm2Configuration       = a.bpm2Configuration       ?: b.bpm2Configuration
            awsConfiguration        = a.awsConfiguration        ?: b.awsConfiguration
            azureConfiguration      = a.azureConfiguration      ?: b.azureConfiguration
            uplinkConfiguration     = a.uplinkConfiguration     ?: b.uplinkConfiguration
            searchConfiguration     = a.searchConfiguration     ?: b.searchConfiguration
            kafkaConfiguration      = a.kafkaConfiguration      ?: b.kafkaConfiguration
            asyncMsgConfiguration   = a.asyncMsgConfiguration   ?: b.asyncMsgConfiguration
            ldapConfiguration       = a.ldapConfiguration       ?: b.ldapConfiguration
            oidConfiguration        = a.oidConfiguration        ?: b.oidConfiguration
            encryptionConfiguration = a.encryptionConfiguration ?: b.encryptionConfiguration
            passwordResetApiKey     = a.passwordResetApiKey     ?: b.passwordResetApiKey
            noDbBackendApiKey       = a.noDbBackendApiKey       ?: b.noDbBackendApiKey
            noDbBackendPermittedRequests = a.noDbBackendPermittedRequests ?: b.noDbBackendPermittedRequests
            jwtValidityApiKey       = a.jwtValidityApiKey       ?: b.jwtValidityApiKey
            jwtValidityUserPassword = a.jwtValidityUserPassword ?: b.jwtValidityUserPassword
            jwtValidityOpenId       = a.jwtValidityOpenId       ?: b.jwtValidityOpenId
            mocks                   = a.mocks                   ?: b.mocks
            runInCluster            = a.runInCluster            ?: b.runInCluster
            disableScheduler        = a.disableScheduler        ?: b.disableScheduler
            sessionLogSysout        = a.sessionLogSysout        ?: b.sessionLogSysout
            importEnvironment       = a.importEnvironment       ?: b.importEnvironment
            eventEnvironment        = a.eventEnvironment        ?: b.eventEnvironment
            schedulerEnvironment    = a.schedulerEnvironment    ?: b.schedulerEnvironment
            z                       = a.z                       ?: b.z
            startupApiKey           = a.startupApiKey           ?: b.startupApiKey
        ]
        // replace string fields from environment (for containers)
        val envVarResolver = new CfgFromEnvironmentProvider
        myConfiguration.treeWalkString(envVarResolver, true)

        // replace UUIDs from environment
        getEnvUuid("SERVER_NODB_PW_RESET_API_KEY", [ myConfiguration.passwordResetApiKey = it ]);
        getEnvUuid("SERVER_NODB_BACKEND_API_KEY",  [ myConfiguration.noDbBackendApiKey   = it ]);
        getEnvUuid("SERVER_STARTUP_API_KEY",       [ myConfiguration.startupApiKey       = it ]);
        if (myConfiguration.kafkaConfiguration !== null) {
            getEnvUuid("SERVER_KAFKA_IMPORT_API_KEY", [ myConfiguration.kafkaConfiguration.defaultImportApiKey = it ]);
            getEnvUuid("SERVER_KAFKA_CLUSTER_MANAGER_API_KEY", [ myConfiguration.kafkaConfiguration.clusterManagerApiKey = it ]);
        }
        if (myConfiguration.uplinkConfiguration !== null) {
            for (uplink: myConfiguration.uplinkConfiguration) {
                getEnvUuid("SERVER_UPLINK_API_KEY_" + uplink.key.toUpperCase, [ uplink.apiKey = it ]);
            }
        }

        // preprocess any custom fields into map for later easier access
        if (myConfiguration.z !== null) {
            customParameters.clear
            myConfiguration.z.forEach[
                val equalsPos = indexOf(EQUALS_SIGN)
                if (equalsPos > 0) {
                    // store key/value pair
                    customParameters.put(substring(0, equalsPos).trim,
                        envVarResolver.convert(substring(equalsPos+1).trim, null)
                    )
                } else {
                    LOGGER.warn("Custom (z field) entry {} has no '=' delimiter, ignoring entry", it)
                }
            ]
            LOGGER.info("Read {} custom parameters from config file", customParameters.size)
        }
        if (customizer !== null) {
            customizer.accept(myConfiguration)
        }
        // may need to set some defaults
        if (myConfiguration.applicationConfiguration !== null) {
            val it = myConfiguration.applicationConfiguration
            if (autonomousPoolSize             === null) autonomousPoolSize = 4;
            if (localAsyncPoolSize             === null) localAsyncPoolSize = 4;
            if (numberOfRetriesOptimisticLock  === null) numberOfRetriesOptimisticLock = 2;
            if (numberOfRetriesDatabaseConnect === null) numberOfRetriesDatabaseConnect = 3;
            if (pauseIncreaseFactor            === null) pauseIncreaseFactor = 1.5;
        }
        // index the uplink entries
        uplinks.clear();
        if (myConfiguration.uplinkConfiguration !== null) {
            for (uplink: myConfiguration.uplinkConfiguration) {
                uplinks.put(uplink.key, uplink)
            }
        }
        // index the encryption entries
        encryptions.clear();
        if (myConfiguration.encryptionConfiguration !== null) {
            for (encryption: myConfiguration.encryptionConfiguration) {
                encryptions.put(encryption.encryptionId, encryption)
            }
        }
        myConfiguration.freeze  // it won't be changed afterwards
    }

    def static String getCustomParameter(String key) {
        return customParameters.get(key)
    }

    /** Returns the uplink configuration configured for the specific key, or return null if none exists. */
    def static UplinkConfiguration getUplink(String key) {
        return uplinks.get(key)
    }

    /** Returns the uplink configuration configured for the specific key, or throws an exception if none exists. */
    def static UplinkConfiguration getUplinkOrThrow(String key) {
        val UplinkConfiguration uplinkCfg = uplinks.get(key)
        if (uplinkCfg === null) {
            LOGGER.error("Missing uplink configuration for key {}", key);
            throw new T9tException(T9tException.MISSING_UPLINK_CONFIGURATION, key);
        }
        return uplinkCfg
    }

    /** Returns the encryption configuration configured for the specific ID, or return null if none exists. */
    def static EncryptionConfiguration getEncryption(String id) {
        return encryptions.get(id)
    }

    /** Returns the uplink configuration configured for the specific key, or throws an exception if none exists. */
    def static EncryptionConfiguration getEncryptionOrThrow(String id) {
        val EncryptionConfiguration encryptionCfg = encryptions.get(id)
        if (encryptionCfg === null) {
            LOGGER.error("Missing encryption configuration for ID {}", id);
            throw new T9tException(T9tException.MISSING_ENCRYPTION_CONFIGURATION, id);
        }
        return encryptionCfg
    }

    def static void readConfiguration(String filename) {
        readConfiguration(filename, null)
    }

    // read the configuration from the provided file, or fallback to the home cfg file if none has been specified
    def static void readConfiguration(String filename, Consumer<T9tServerConfiguration> customizer) {
        if (filename === null) {
            LOGGER.info("No configuration filename specified, using default {}", DEFAULT_CFG_FILENAME)
            try {
                val cfgFromFile = configFromFile(DEFAULT_CFG_FILENAME)
                mergeConfigurations(cfgFromFile, postgresConfig, customizer)
            } catch (Exception e) {
                // ignore issues because we are working on the default
                LOGGER.info("Falling back to default configuration due to {}", e.message ?: e.class.simpleName)
                return
            }
        } else {
            LOGGER.info("Using configuration file {}", filename)
            try {
                val cfgFromFile = configFromFile(filename)
                mergeConfigurations(cfgFromFile, postgresConfig, customizer)
            } catch (Exception e) {
                // fail on ignore issues because the cfg file was requested specifically
                LOGGER.info("{}: Cannot read configuration due to {}", e.class.simpleName, e.message)
                System.exit(1)
            }
        }
    }

    def private static void getEnvUuid(String envname, Consumer<UUID> setter) {
        val value = System.getenv(envname);
        if (value !== null && !value.isEmpty) {
            try {
                setter.accept(UUID.fromString(value));
            } catch (Exception e) {
                LOGGER.error("Misconfigured environment variable {}: {} is not a valid UUID", envname, value);
            }
        }
    }
}

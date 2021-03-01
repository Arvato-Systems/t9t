Schema Loader Commandline Interface
===================================

This module provides a fully packaged schema loader for command line usage.

All necessary JAR files are included in the provided package.
The only exception are database driver, which manually need to be added using the java property
`-Dloader.path=myJDBCDriver.jar` or the `schemaClasspath` configuration.

## Configuration
Configuration properties can by provided on seperate ways, where for each property a different way
could be chosen. The available configuration sources are order with a priority such that a configuration
on a higher level source overwrites a configuration on a lower level source:

1. Commandline arguments (e.g. --migration-log-table=FW_MIGRA)
2. JVM properties (e.g. -Dmigration-log-table=FW_MIGRA)
3. Property or YML file provided with JVM properts -DpropertyFile=myConfig.yml
4. Environment variables (e.g. MIGRATION_LOG_TABLE=FW_MIGRA)
5. Property file schemaLoader.properties in JVM working dir
6. YML file schemaLoader.yml in JVM working dir
7. Property file schemaLoader.properties in user home
8. YML file schemaLoader.properties in user home
9. Property file schemaLoader.properties in classpath

Configuration properties can be written in many ways:

* db-config.default.type
* db_config.default.type
* dbConfig.default.type
* DB\_CONFIG\_DEFAULT\_TYPE

For a detailed list of all available configuration parameters see README.md of lib module.

### Example YAML

```
db-config:
  default:
    type: POSTGRES
    url: jdbc:postgresql://localhost:5432/fortytwo
    username: fortytwo
    password: changeMe
    driver-class: org.postgresql.Driver

install:
  create:
    - Role
    - User
    - Tablespace
    - Table
    - ForeignKey
    - Sequence
    - Function
    - View
  drop:
    - View
    - Trigger
    - Function
    - Table
    - Sequence

migration:
  pre-drop:
    - View
    - Trigger
    - Function
  post-create:
    - View
    - Function
    - Trigger
```

## Example start

```
java -Dloader.path=myJdbcDriver.jar -jar schema-loader-cmdline.jar --migration-log-table=FW_MIGRA
```

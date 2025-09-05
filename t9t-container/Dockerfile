
# === Globale ARGs ===
ARG JAVA_IMAGE=amazoncorretto
ARG JAVA_TAG=21-alpine-full

ARG JPROFILER_VERSION=15_0_3
ARG JPROFILER_HOME=/opt/jprofiler

ARG JMX_EXPORTER_VERSION=1.0.1

# === T9T JAVA Stage (common ENVs) ===
FROM ${JAVA_IMAGE}:${JAVA_TAG} AS t9t-java
ENV MIN_RAM_P=95 \
    MAX_RAM_P=95 \
    INIT_RAM_P=95 \
    USERNAME=javauser \
    LOGBACK_PATH=./logback.xml \
    APP=/app \
    JAR=/app/app.jar \
    log.debug=false \
    log.level.jettyserver=INFO \
    log.level.jpaw=ERROR \
    log.level.arvatosystems=ERROR \
    log.level.root=ERROR
# Install system packages
RUN apk add --no-cache dumb-init bash curl msttcorefonts-installer fontconfig && \
    update-ms-fonts
# Install JProfiler
ARG JPROFILER_VERSION
ARG JPROFILER_HOME
RUN wget https://download.ej-technologies.com/jprofiler/jprofiler_linux-x64_${JPROFILER_VERSION}.tar.gz -O /tmp/jprofiler.tar.gz && \
    mkdir -p ${JPROFILER_HOME} && \
    tar -xzf /tmp/jprofiler.tar.gz -C ${JPROFILER_HOME} --strip-components=1 && \
    rm /tmp/jprofiler.tar.gz
# Using JProfiler with
# ENV AGENT_PATH=agentpath:${JPROFILER_HOME}/bin/linux-x64/libjprofilerti.so=port=8849 \

# Add group and user for application
RUN addgroup --system ${USERNAME} && \
    adduser -S -s /bin/false -G ${USERNAME} ${USERNAME}
# Create application directories
RUN mkdir -p "${APP}" && \
    chmod -R u+w "${APP}" && \
    chown -R ${USERNAME}:${USERNAME} "${APP}"
USER ${USERNAME}
WORKDIR ${APP}

# === T9T UI Stage ===
FROM t9t-java AS t9t-zkui
ARG JMX_EXPORTER_VERSION
ENV APP=/app \
    CONF=/conf \
    BASE_PROPERTIES_PATH=/conf/base.properties \
    HOST=server-main \
    PORT=8024 \
    METRICS="--metrics"
COPY --chown=${USERNAME}:${USERNAME} ./ui/conf ${CONF}
EXPOSE ${JMX_EXPORTER_PORT} 8080 8849
CMD dumb-init java ${AGENT_PATH} \
    -Dlogback.configurationFile=${LOGBACK_PATH} \
    -DbasePropertyFile=${BASE_PROPERTIES_PATH} \
    -XX:MinRAMPercentage=${MIN_RAM_P} \
    -XX:MaxRAMPercentage=${MAX_RAM_P} \
    -XX:InitialRAMPercentage=${INIT_RAM_P} \
    -XX:+PrintFlagsFinal \
    -jar ${JAR}

# === T9T SETUP Stage ===
FROM t9t-java AS t9t-setup
ENV SETUP=/setup \
    DB_URL=jdbc:postgresql://db:5432/fortytwo \
    DB_USER=fortytwo \
    # DB_PASSWORD=changeMe \
    ACTION=install
USER root
RUN mkdir -p "${SETUP}" && \
    touch ${SETUP}/schemaLoader.yml && \
    chown -R 1001:1001 ${SETUP}
RUN touch ${SETUP}/schemaLoader.yml
COPY --chown=${USERNAME}:${USERNAME} ./setup/bin ${SETUP}
COPY --chown=${USERNAME}:${USERNAME} ./target/dependency/postgresql.jar ${SETUP}/postgresql.jar
COPY --chown=${USERNAME}:${USERNAME} ./target/dependency/schema-loader-cmdline.jar ${SETUP}/schema-loader-cmdline.jar
COPY --chown=${USERNAME}:${USERNAME} ./target/dependency/t9t-sql.jar ${SETUP}/t9t-sql.jar
USER ${USERNAME}

WORKDIR ${SETUP}
USER ${USERNAME}
CMD ["bash", "setup.sh"]

# === T9T SERVER Stage ===
FROM t9t-java AS t9t-server
ENV APP=/app \
    CONF=/conf \
    CONF_DEFAULT=/conf_default \
    MIG=/app/migration \
    FORTYTWO=/app/fortytwo \
    CAMEL=/app/fortytwo/camel \
    LOGBACK_PATH=/conf/logback.xml \
    HAZELCAST_PATH=/conf/hazelcast.xml \
    CONFIG_PATH=/conf/t9tconfig.xml \
    T9T_RESTAPI_UNAUTHEDPING=Y \
    T9T_RESTAPI_KAFKA=Y \
    T9T_KAFKA_BOOTSTRAP_SERVERS="/dev/null" \
    T9T_KAFKA_REQUEST_TOPIC="requestToMAIN" \
    MAIN_CLASS=com.arvatosystems.t9t.vertx.cluster.VertxCluster \
    POSTGRES_MAIN_URL=jdbc:aws-wrapper:postgresql://db:5432/main \
    POSTGRES_MAIN_USER=fortytwo \
    # POSTGRES_MAIN_PASSWORD=changeMe
    # KEYSTORE_PASSWORD=BNpGofE5p02EoWhEXdMg
    METRICS="--metrics" \
    CORS="--cors" \
    MIGRATE="--migrateDb"

USER root
RUN mkdir -p "${APP}" "${CAMEL}" "${CONF}" "${CONF_DEFAULT}" "${FORTYTWO}" "${MIG}"
RUN chmod -R u+w "${APP}"
RUN chown -R ${USERNAME}:${USERNAME} "${APP}" "${CONF}" "${CONF_DEFAULT}" "${FORTYTWO}" "${MIG}"
USER ${USERNAME}

# Add default config
COPY --chown=${USERNAME}:${USERNAME} ./server/conf ${CONF}

EXPOSE 8025 8024 5701 8849
VOLUME ${FORTYTWO}
CMD dumb-init java ${AGENT_PATH} \
    -Dlogback.configurationFile=${LOGBACK_PATH} \
    -XX:MinRAMPercentage=${MIN_RAM_P} \
    -XX:MaxRAMPercentage=${MAX_RAM_P} \
    -XX:InitialRAMPercentage=${INIT_RAM_P} \
    -XX:+PrintFlagsFinal \
    -Dhazelcast.jmx=true \
    -Dvertx.hazelcast.config=${HAZELCAST_PATH} \
    -cp ${JAR} \
    -Dloader.main=${MAIN_CLASS} \
    org.springframework.boot.loader.launch.PropertiesLauncher \
    --cfg=${CONFIG_PATH} \
    ${METRICS} \
    ${CORS} \
    ${MIGRATE} \
    ${RESTPORT}

# === T9T GATEWAY Stage ===
FROM t9t-java AS t9t-gateway-rest
ENV HOST=server-main \
  PORT=8024 \
  T9T_RESTAPI_SWAGGER="Y" \
  T9T_RESTAPI_UNAUTHEDPING="Y" \
  T9T_RESTAPI_SERVLET_LOGGING_FILTER="N" \
  T9T_RESTAPI_KAFKA="Y" \
  T9T_KAFKA_BOOTSTRAP_SERVERS="/dev/null" \
  T9T_KAFKA_REQUEST_TOPIC="requestToMAIN" \
  MAIN_CLASS=com.arvatosystems.t9t.jetty.init.JettyServer\
  METRICS="--metrics"
CMD dumb-init java ${AGENT_PATH} \
    -Dlogback.configurationFile=${LOGBACK_PATH} \
    -XX:MinRAMPercentage=${MIN_RAM_P} \
    -XX:MaxRAMPercentage=${MAX_RAM_P} \
    -XX:InitialRAMPercentage=${INIT_RAM_P} \
    -XX:+PrintFlagsFinal \
    -jar ${JAR} \
    ${METRICS}
EXPOSE 8090
  
#######################################
### PRODUCTIVE IMAGES FROM HERE --> ###
#######################################

# === MCP GATEWAY Stage ===
FROM t9t-java AS t9t-gateway-mcp
ENV \
  HOST=server-main \
  PORT=8024 \
  MAIN_CLASS=com.arvatosystems.t9t.mcp.gateway.McpJettyServer \
  JETTY_HTTP_PORT=9094 \
  JETTY_THREADPOOL_MINTHREADS=4 \
  JETTY_THREADPOOL_MAXTHREADS=20 \
  JETTY_THREADPOOL_IDLETIMEOUT=5000 \
  JETTY_CONNECTION_IDLETIMEOUT=300000 \
  JETTY_STOPTIMEOUT=5000 \
  JETTY_CONTEXTPATH=/
  # T9T_MCP_APIKEY=c674390c-8165-47c8-a3d4-1b4f1cd9876a \
CMD dumb-init java ${AGENT_PATH} \
    -Dlogback.configurationFile=${LOGBACK_PATH} \
    -XX:MinRAMPercentage=${MIN_RAM_P} \
    -XX:MaxRAMPercentage=${MAX_RAM_P} \
    -XX:InitialRAMPercentage=${INIT_RAM_P} \
    -XX:+PrintFlagsFinal \
    -jar ${JAR} \
    ${METRICS}
EXPOSE 9094
COPY --chown=${USERNAME}:${USERNAME} ./target/dependency/t9t-gateway-mcp.jar ${JAR}

# === T9T SERVER MAIN Stage ===
FROM t9t-server AS t9t-server-main
COPY --chown=${USERNAME}:${USERNAME} ./target/dependency/t9t-server.jar ${JAR}

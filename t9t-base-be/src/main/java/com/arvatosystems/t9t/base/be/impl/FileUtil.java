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
package com.arvatosystems.t9t.base.be.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.SimplePatternEvaluator;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FileUtil implements IFileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static final String USER_HOME = "userHome";
    private static final String USER_NAME = "userName";

    protected final T9tServerConfiguration serverConfiguration = Jdp.getRequired(T9tServerConfiguration.class);

    public static final String SEP = System.getProperty("file.separator") == null ? "/" : System.getProperty("file.separator");
    public static final String TENANT_ID = "tenantId";
    public static final String TENANT_ID_TAG = "${tenantId}";

    private final String userHome;
    private final String userName;
    private final Map<String, Object> patternReplacements;

    /**
     * The root of files stored or read by this application from the file system.
     * It is determined by the following priority:
     *    a) system property t9t.pathprefix
     *    b) environment variable T9T_PATHPREFIX
     *    c) if the configuration file defines serverConfiguration.filesystemPathRoot, then this is used
     *    d) $HOME/serverConfiguration.filesystemContext
     *    e) $HOME/fortytwo (hardcoded fallback)
     */
    private final String filePrefix;

    public FileUtil() {
        userHome = System.getProperty("user.home");
        if (userHome == null || userHome.length() == 0) {
            LOGGER.error("FATAL: system property user.home does not exist - incomplete JRE?");
            throw new T9tException(T9tException.UNKNOWN_SYSTEM_PROPERTY_USER_HOME);
        }

        userName = System.getProperty("user.name");
        if (userName == null || userName.length() == 0) {
            LOGGER.error("FATAL: system property user.name does not exist - incomplete JRE?");
            throw new T9tException(T9tException.UNKNOWN_SYSTEM_PROPERTY_USER_NAME);
        }

        patternReplacements = new HashMap<>(2);
        patternReplacements.put(USER_HOME, userHome);
        patternReplacements.put(USER_NAME, userName);

        final String source;
        String prefix = System.getProperty("t9t.pathprefix");
        if (prefix != null) {
            source = "system property t9t.pathprefix";
        } else {
            prefix = System.getenv("T9T_PATHPREFIX");
            if (prefix != null) {
                source = "environment variable T9T_PATHPREFIX";
            } else {
                prefix = serverConfiguration.getServerConfiguration() == null ? null : serverConfiguration.getServerConfiguration().getFilesystemPathRoot();
                if (prefix != null) {
                    source = "configuration file entry filesystemPathRoot";
                } else {
                    prefix = serverConfiguration.getServerConfiguration() == null ? null : serverConfiguration.getServerConfiguration().getFilesystemContext();
                    if (prefix != null) {
                        source = "configuration file entry filesystemContext";
                    } else {
                        prefix = "fortytwo";
                        source = "hardcoded fallback";
                    }
                    // anything in this section needs USER_HOME
                    prefix = userHome + SEP + prefix;
                }
            }
        }
        LOGGER.info("Using file system root path {} determined by {}", prefix, source);
        filePrefix = prefix + SEP;
    }

    @Override
    public boolean needGzipExtension(String relativePath, boolean compressed) {
        return compressed && !relativePath.toLowerCase().endsWith(GZIP_EXTENSION);
    }

    @Override
    public String getAbsolutePathForTenant(final String tenantId, final String relativePath, final boolean compressed) {
        final String absolutePath = getAbsolutePathForTenant(tenantId, relativePath);
        return needGzipExtension(relativePath, compressed) ? absolutePath + GZIP_EXTENSION : absolutePath;
    }

    /**
     * Builds absolute path from predefined path prefix, tenantId and given relative path.
     *
     * @param relativePath
     *            relative path
     * @return absolute path
     * @throws T9tException
     *             thrown when building absolute path fails
     */
    @Override
    public String getAbsolutePathForTenant(final String tenantId, final String relativePath) {
        final StringBuilder sb = new StringBuilder().append(getFilePathPrefix());

        if (!getFilePathPrefix().endsWith(SEP)) {
            sb.append(SEP);
        }
        sb.append(tenantId).append(SEP);

        if (relativePath != null && relativePath.length() > 0) {
            sb.append(SimplePatternEvaluator.evaluate(relativePath, patternReplacements));
        }

        return sb.toString();
    }

    /**
     * Builds absolute path from predefined path prefix and given relative path.
     *
     * @param relativePath
     *            relative path
     * @return absolute path
     * @throws T9tException
     *             thrown when building absolute path fails
     */
    @Override
    public String getAbsolutePath(final String relativePath) {
        final StringBuilder sb = new StringBuilder().append(getFilePathPrefix());

        if (!getFilePathPrefix().endsWith(SEP)) {
            sb.append(SEP);
        }

        if (relativePath != null && relativePath.length() > 0) {
            sb.append(SimplePatternEvaluator.evaluate(relativePath, patternReplacements));
        }

        return sb.toString();
    }

    /**
     * Returns predefined file prefix. Can be used to override the default method.
     *
     * @return file prefix
     * @throws T9tException
     *             thrown when file prefix was not defined
     */
    @Override
    public String getFilePathPrefix() {
        return filePrefix;
    }

    /**
     * Creates folders for the given location if they don't exist already.
     *
     * @param pathToFile
     *            absolute path to the file
     * @return true if folders were created, false otherwise
     */
    @Override
    public boolean createFileLocation(final String pathToFile) {
        final File targetFile = new File(pathToFile);
        final File targetFolder = targetFile.getParentFile();

        return !targetFolder.exists() && targetFolder.mkdirs();
    }

    @Override
    public String buildPath(final String... pathElements) {
        final StringBuilder sb = new StringBuilder();
        if (pathElements != null) {
            String previousPathElement = "";
            for (final String pathElement : pathElements) {
                if (!previousPathElement.endsWith(SEP) && !pathElement.startsWith(SEP)) {
                    sb.append(SEP);
                }
                sb.append(pathElement);
                previousPathElement = pathElement;
            }
        }

        return sb.toString();
    }
}

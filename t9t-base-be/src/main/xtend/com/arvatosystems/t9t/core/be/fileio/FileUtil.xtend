/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.core.be.fileio;

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.services.IFileUtil
import com.arvatosystems.t9t.base.services.SimplePatternEvaluator
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.io.File
import java.util.Map

@AddLogger
@Singleton
public class FileUtil implements IFileUtil {
    @Inject T9tServerConfiguration      serverConfiguration

    public static final String SEP = System.getProperty("file.separator") ?: "/";
    public static final String TENANT_ID = "tenantId";
    public static final String TENANT_ID_TAG = "${tenantId}";
    private static final String USER_HOME = "userHome";
    private static final String USER_NAME = "userName";

    private final String userHome = System.getProperty("user.home") => [
        if (nullOrEmpty) {
            LOGGER.error("FATAL: system property user.home does not exist - incomplete JRE?")
            throw new T9tException(T9tException.UNKNOWN_SYSTEM_PROPERTY_USER_HOME);
        }
    ]
    private final String userName = System.getProperty("user.name") => [
        if (nullOrEmpty) {
            LOGGER.error("FATAL: system property user.name does not exist - incomplete JRE?")
            throw new T9tException(T9tException.UNKNOWN_SYSTEM_PROPERTY_USER_NAME);
        }
    ]
    private final Map<String,Object> PATTERN_REPLACEMENTS = #{
        USER_HOME -> userHome,
        USER_NAME -> userName
    }

    /** The root of files stored or read by this application from the file system.
     * It is determined by the following priority:
     *    a) system property t9t.pathprefix
     *    b) environment variable T9T_PATHPREFIX
     *    c) if the configuration file defines serverConfiguration.filesystemPathRoot, then this is used
     *    d) $HOME/serverConfiguration.filesystemContext
     *    e) $HOME/fortytwo (hardcoded fallback)
     */
    private final String filePrefix = {
        var String prefix
        var String source
        prefix = System.getProperty("t9t.pathprefix")
        if (prefix !== null) {
            source = "system property t9t.pathprefix"
        } else {
            prefix = System.getenv("T9T_PATHPREFIX")
            if (prefix !== null) {
                source = "environment variable T9T_PATHPREFIX"
            } else {
                prefix = serverConfiguration.serverConfiguration?.filesystemPathRoot
                if (prefix !== null) {
                    source = "configuration file entry filesystemPathRoot"
                } else {
                    prefix = serverConfiguration.serverConfiguration?.filesystemContext
                    if (prefix !== null) {
                        source = "configuration file entry filesystemContext"
                    } else {
                        prefix = "fortytwo"
                        source = "hardcoded fallback"
                    }
                    // anything in this section needs USER_HOME
                    prefix = userHome + SEP + prefix
                }
            }
        }
        LOGGER.info("Using file system root path {} determined by {}", prefix, source)
        prefix + SEP
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
    override public String getAbsolutePathForTenant(String tenantId, String relativePath) {
        val sb = new StringBuilder().append(getFilePathPrefix());

        if (!getFilePathPrefix().endsWith(SEP)) {
            sb.append(SEP);
        }
        sb.append(tenantId).append(SEP);

        if (!relativePath.nullOrEmpty)
            sb.append(SimplePatternEvaluator.evaluate(relativePath, PATTERN_REPLACEMENTS));

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
    override public String getAbsolutePath(String relativePath) {
        val sb = new StringBuilder().append(getFilePathPrefix());

        if (!getFilePathPrefix().endsWith(SEP)) {
            sb.append(SEP);
        }

        if (!relativePath.nullOrEmpty) {
            sb.append(SimplePatternEvaluator.evaluate(relativePath, PATTERN_REPLACEMENTS));
        }

        return sb.toString();
    }

    override public String buildPath(String... pathElements) {
        val sb = new StringBuilder();

        if (pathElements !== null) {
            var String previousPathElement = "";
            for (String pathElement : pathElements) {
                if (!previousPathElement.endsWith(SEP) && !pathElement.startsWith(SEP)) {
                    sb.append(SEP);
                }
                sb.append(pathElement);
                previousPathElement = pathElement;
            }
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
    override public String getFilePathPrefix() {
        return filePrefix;
    }

    /**
     * Creates folders for the given location if they don't exist already.
     *
     * @param pathToFile
     *            absolute path to the file
     * @return true if folders were created, false otherwise
     */
    override public boolean createFileLocation(String pathToFile) {
        val targetFile = new File(pathToFile);
        val targetFolder = targetFile.getParentFile();

        return !targetFolder.exists() && targetFolder.mkdirs();
    }
}

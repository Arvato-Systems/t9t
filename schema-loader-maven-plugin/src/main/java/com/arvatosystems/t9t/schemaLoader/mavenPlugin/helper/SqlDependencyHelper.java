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
package com.arvatosystems.t9t.schemaLoader.mavenPlugin.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.arvatosystems.t9t.schemaLoader.mavenPlugin.config.SqlArtifact;

public class SqlDependencyHelper {

    public static final String SQL_CLASSIFIER = "sql";

    private Log logger;

    public SqlDependencyHelper(Log logger) {
       this.logger = logger;
    }

    /**
     * Filters the references jars (in project dependencies) with classifier {@value #SQL_CLASSIFIER}
     * with a list of explicitely listed jars that shall be included.
     *
     * @param project project the maven project
     * @param sqlArtifacts the explicitely listed jars that shall be included
     * @returna list of jars of every dependency with classifier {@value #SQL_CLASSIFIER} filtered by sqlArtifact
     * @throws MojoExecutionException if an {@link Artifact} was not resolved or its file is {@code null}
     */
    public List<URL> getJarList(MavenProject project, List<SqlArtifact> sqlArtifacts) throws MojoExecutionException {
        try {
            Set<Artifact> artifacts = getArtifactsWithSqlClassifier(project);
            List<URL> jarList = new LinkedList<>();

            for (SqlArtifact sqlArtifact : sqlArtifacts) {
                Artifact artifact = findArtifact(sqlArtifact, artifacts);
                jarList.add(artifact.getFile().toURI().toURL());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved filtered jars: " + jarList.toString());
            }
            return jarList;
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Invalid jar file URL: " + e.getMessage());
        }
    }

    /**
     * Returns all referenced jars (in project dependencies) with classifier {@value #SQL_CLASSIFIER}.
     *
     * @param project the maven project
     * @return a list of jars of every dependency with classifier {@value #SQL_CLASSIFIER}
     * @throws MojoExecutionException if an {@link Artifact} was not resolved or its file is {@code null}
     */
    public List<URL> getJarList(MavenProject project) throws MojoExecutionException {
        try {
            Set<Artifact> artifacts = getArtifactsWithSqlClassifier(project);
            List<URL> jarList = new LinkedList<>();

            for (Artifact artifact : artifacts) {
                checkResolved(artifact);
                jarList.add(artifact.getFile().toURI().toURL());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved jars: " + jarList.toString());
            }
            return jarList;
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Invalid jar file URL: " + e.getMessage());
        }
    }

    protected Artifact findArtifact(SqlArtifact sqlArtifact, Set<Artifact> artifacts) throws MojoExecutionException {
        for (Artifact artifact : artifacts) {
            if (sqlArtifact.isMatch(artifact)) {
                checkResolved(artifact);
                return artifact;
            }
        }
        String message = "Could not find artifact in dependencies. " + sqlArtifact.toString();
        logger.error(message);
        throw new MojoExecutionException(message);
    }

    protected void checkResolved(Artifact artifact) throws MojoExecutionException {
        if (!artifact.isResolved()) {
            String message = "Artifact was not resolved. " + artifact.toString();
            logger.error(message);
            throw new MojoExecutionException(message);
        }
        if (artifact.getFile() == null) {
            String message = "Artifact file is null. " + artifact.toString();
            logger.error(message);
            throw new MojoExecutionException(message);
        }
    }

    protected Set<Artifact> getArtifactsWithSqlClassifier(MavenProject project) {
        logger.debug("Filtering dependency artifacts by classifier=" + SQL_CLASSIFIER);
        Set<Artifact> result = new HashSet<>();
        Set<?> artifacts = project.getDependencyArtifacts();
        for (Iterator<?> artifactIterator = artifacts.iterator(); artifactIterator.hasNext();) {
            Artifact artifact = (Artifact) artifactIterator.next();
            if (artifact.hasClassifier() && artifact.getClassifier().equals(SQL_CLASSIFIER)) {
                result.add(artifact);
            }
        }
        return result;
    }

}

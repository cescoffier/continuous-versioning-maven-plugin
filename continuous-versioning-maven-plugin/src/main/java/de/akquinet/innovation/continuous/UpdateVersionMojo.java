/*
 * Copyright 2012 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.akquinet.innovation.continuous;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.versions.SetMojo;

import java.util.List;

/**
 * @goal update-version
 * @aggregator
 * @requiresProject true
 * @requiresDirectInvocation true
 */
public class UpdateVersionMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project;

    /**
     * The maven session.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    MavenSession session;

    /**
     * The version of the dependency/module to update.
     *
     * @parameter expression="${oldVersion}" default-value="${project.version}"
     * @since 1.2
     */
    String oldVersion;

    /**
     * @component allo
     * @since 1.0-alpha-1
     */
    protected MavenProjectBuilder projectBuilder;

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @since 1.0-alpha-1
     */
    protected ArtifactRepository localRepository;

    /**
     * Controls whether a backup pom should be created (default is true).
     *
     * @parameter expression="${generateBackupPoms}"
     * @since 1.0-alpha-3
     */
    private Boolean generateBackupPoms;

    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    protected List reactor;

    /**
     * @component
     * @since 1.0-alpha-1
     */
    protected org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    public void execute() throws MojoExecutionException, MojoFailureException {

        String newVersion = collectNewVersion();

        MyVersionSetMojo mojo = new MyVersionSetMojo(newVersion);
        mojo.execute();
    }

    private String collectNewVersion() throws MojoExecutionException {
        String version = (String) project.getProperties().get("unique.version");
        if (version == null) {
            version = System.getProperty("unique.version");
        }

        if (version == null) {
            getLog().error("Cannot find the unique version");
            throw new MojoExecutionException("Cannot collect the new version");
        }

        return version;

    }

    private class MyVersionSetMojo extends SetMojo {
        public MyVersionSetMojo(String nv) {
            this.projectBuilder = UpdateVersionMojo.this.projectBuilder;
            this.artifactFactory = UpdateVersionMojo.this.artifactFactory;
            this.localRepository = UpdateVersionMojo.this.localRepository;
            this.reactorProjects = UpdateVersionMojo.this.reactor;
            this.session = UpdateVersionMojo.this.session;
            this.settings = UpdateVersionMojo.this.settings;

            Reflect.set(this, "newVersion", String.class, nv);
            Reflect.set(this, "project", MavenProject.class, project);
            Reflect.set(this, "oldVersion", String.class, oldVersion);

            Reflect.set(this, "groupId", String.class, project.getGroupId());
            Reflect.set(this, "artifactId", String.class, project.getArtifactId());

            // Inconsistencies in the boolean management....
            // Booleans
            Reflect.set(this, "generateBackupPoms", Boolean.class, Boolean.TRUE);
            Reflect.set(this, "updateMatchingVersions", Boolean.class, Boolean.TRUE);
            // booleans
            Reflect.set(this, "processDependencies", Boolean.TYPE, true);
            Reflect.set(this, "processParent", Boolean.TYPE, true);
            Reflect.set(this, "processPlugins", Boolean.TYPE, true);
            Reflect.set(this, "processProject", Boolean.TYPE, true);

        }
    }

}

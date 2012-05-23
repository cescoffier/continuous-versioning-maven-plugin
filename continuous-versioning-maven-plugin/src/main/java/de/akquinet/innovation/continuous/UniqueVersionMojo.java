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

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.plugin.ChangeLogMojo;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @goal compute-unique-version
 * @aggregator
 * @requiresProject true
 * @requiresDirectInvocation true
 */
public class UniqueVersionMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project;

    /**
     * @parameter expression="${component.org.apache.maven.scm.manager.ScmManager}"
     * @required
     * @readonly
     */
    ScmManager manager;

    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    Settings settings;

    /**
     * @parameter default-value=true
     */
    boolean fillReleaseProperties;

    /**
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    List<MavenProject> reactor;

    public void execute() throws MojoExecutionException {
        Scm scm = project.getScm();
        if (scm == null) {
            getLog().error("No SCM data - ignoring unique version computation");
            return;
        }

        getLog().info("SCM Url : " + scm.getConnection().toString());

        MyChangelogMojo changelogMojo = new MyChangelogMojo(scm);


        try {
            ScmRepository repository = changelogMojo.getScmRepository();
            ScmProvider provider = changelogMojo.getScmManager().getProviderByRepository( repository );

            ChangeLogScmResult result = provider.changeLog(repository, changelogMojo.getFileSet(),
                    null,
                    null, 0,
                    (ScmBranch) null,
                    null);
            changelogMojo.checkResult(result);

            ChangeLogSet changeLogSet = result.getChangeLog();
            getLog().info("Commit count : " + changeLogSet.getChangeSets().size());

            if (getLog().isDebugEnabled()) {
                getLog().debug("==== Changelog ====");
                for (Object change : changeLogSet.getChangeSets()) {
                    ChangeSet changeSet = (ChangeSet) change;
                    getLog().debug(changeSet.toString());
                    getLog().debug("--------");
                }
                getLog().debug("==== End Changelog ====");
            }

            String newVersion = computeVersion(project.getVersion(), changeLogSet.getChangeSets().size());
            exportProperties(newVersion);

            if (fillReleaseProperties) {
                updateReleaseProperties(newVersion);
            }
        } catch (ScmException e) {
            getLog().error("Cannot run the unique version computation", e);
        } catch (IOException e) {
            getLog().error("Cannot run the unique version computation", e);
        }

    }

    public String computeVersion(String version, int id) {
        String newVersion = version;
        if (version.contains("-SNAPSHOT")) {
            // SNAPSHOT case
            newVersion = version.replace("-SNAPSHOT", "_" + id);
        } else if (version.matches(".*_[0-9]*")) {
            // Version already unique.
            newVersion = version.replaceAll("_[0-9]*", "_" + id);
        } else {
            // No marker.
            newVersion += "_" + id;
        }

        return newVersion;
    }

    public void exportProperties(String newVersion) {
        project.getProperties().put("unique.version", newVersion);
        System.setProperty("unique.version", newVersion);
    }

    public void updateReleaseProperties(String newVersion) throws IOException {
        Properties properties = new Properties();
        File releaseFile = new File(project.getBasedir(), "release.properties");
        if (releaseFile.exists()) {
            FileInputStream fis = new FileInputStream(releaseFile);
            properties.load(fis);
            IOUtils.closeQuietly(fis);
        }

        properties.put("scm.tag", project.getArtifactId() + "-" + newVersion);
        String oldVersion = project.getVersion();
        // For all projects set:
        //project.rel.org.myCompany\:projectA=1.2
        //project.dev.org.myCompany\:projectA=1.3-SNAPSHOT
        for (MavenProject project : reactor) {
            properties.put("project.rel." + project.getGroupId() + ":" + project.getArtifactId(), newVersion);
            properties.put("project.dev." + project.getGroupId() + ":" + project.getArtifactId(), oldVersion);
        }

        FileOutputStream fos = new FileOutputStream(releaseFile);
        properties.store(fos, "File edited by the continuous-version-maven-plugin");
        IOUtils.closeQuietly(fos);
    }

    private class MyChangelogMojo extends ChangeLogMojo {

         public MyChangelogMojo(Scm scm) {
             setConnectionUrl(scm.getConnection());
             setLog(getLog());
             setWorkingDirectory(project.getBasedir());
             setConnectionType("connection");

             Reflect.set(this, "manager", ScmManager.class, manager);
             Reflect.set(this, "settings", Settings.class, settings);
         }

         public void setConnectionType(String type) {
            super.setConnectionType(type);
         }
    }
}

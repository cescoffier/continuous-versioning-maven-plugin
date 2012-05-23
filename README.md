Project Continuous
==================

The _project continuous_ is intended to provide tools to support continuous delivery with Maven.

continous-versioning-maven-plugin
=================================

The _continous-versioning-maven-plugin_ is a Maven plugin generating a unique version number based on the SCM metadata.
It supports Git and Subversion. The generated unique version number is computed in order to respect version ordering, and
 it intended to be used to create _fast releases_ in continuous delivery pipeline.

On a project using the 1.0.0-SNAPSHOT version, the computed version will be 1.0.0_xxx, where _xxx_ is the computed
 version.

Usage
-----

In your pom add:

    <build>
        <plugins>
            <plugin>
                <groupId>de.akquinet.innovation.continuous</groupId>
  		        <artifactId>continuous-versioning-maven-plugin</artifactId>
		        <version>VERSION</version>
            </plugin>
        </plugins>
    </build>

You must also have the _SCM_ metadata configured in your pom.

Fast-Release
-------------

The plugin also initiates the computation of the _release.properties_ file used by the Maven Release Plugin. To build a
_fast release_ using the computed version, just use:

    mvn continuous:compute-unique-version release:prepare release:perform



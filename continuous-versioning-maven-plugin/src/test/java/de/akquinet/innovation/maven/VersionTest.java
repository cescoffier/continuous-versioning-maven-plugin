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
package de.akquinet.innovation.maven;

import de.akquinet.innovation.continuous.UniqueVersionMojo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 27.04.12
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class VersionTest {

    @Test
    public void testVersionOrdering() {
        DefaultArtifactVersion v0 = new DefaultArtifactVersion("2.0.0");
        DefaultArtifactVersion v1 = new DefaultArtifactVersion("1.0.0");
        DefaultArtifactVersion v2 = new DefaultArtifactVersion("1.0.0-SNAPSHOT");
        DefaultArtifactVersion v3 = new DefaultArtifactVersion("1.0.0_3");

        assertThat(v0.compareTo(v1)).isEqualTo(1);
        assertThat(v1.compareTo(v2)).isEqualTo(1);
        assertThat(v1.compareTo(v3)).isEqualTo(1);
        assertThat(v2.compareTo(v3)).isEqualTo(1);
    }

    @Test
    public void testComputation() {
        UniqueVersionMojo mojo = new UniqueVersionMojo();
        assertThat(mojo.computeVersion("1.0.0-SNAPSHOT", 25)).isEqualTo("1.0.0_25");
        assertThat(mojo.computeVersion("1.0.0", 25)).isEqualTo("1.0.0_25");

        assertThat(mojo.computeVersion("1.0.0_24", 25)).isEqualTo("1.0.0_25");
    }
}

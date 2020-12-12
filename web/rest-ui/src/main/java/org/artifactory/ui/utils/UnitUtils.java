/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.artifactory.api.artifact.*;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.maven.MavenService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.maven.MavenModelUtils;
import org.artifactory.mime.NamingUtils;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Chen Keinan
 */
public class UnitUtils {

    private static final Logger log = LoggerFactory.getLogger(UnitUtils.class);

    /**
     * get maven artifact info from jar
     *
     * @param file       - file
     * @param pomFromJar - pom content
     * @param originalFileName
     * @return maven artifact info
     */
    public static UnitInfo getMavenArtifactInfo(File file, String pomFromJar, String originalFileName) {
        if (pomFromJar == null) {
            if (file.getName().endsWith(".jar")) {
                return MavenModelUtils.artifactInfoFromFile(file, originalFileName);
            } else {
                return null;
            }
        }
        MavenArtifactInfo mavenArtifactInfo = null;
        if (StringUtils.isNotBlank(pomFromJar)) {
            InputStream pomInputStream = IOUtils.toInputStream(pomFromJar);
            try {
                mavenArtifactInfo = MavenModelUtils.mavenModelToArtifactInfo(pomInputStream);
            } catch (IOException | XmlPullParserException e) {
                log.error(e.toString());
            }
            if (mavenArtifactInfo != null) {
                mavenArtifactInfo.setType(PathUtils.getExtension(file.getName()));
                return mavenArtifactInfo;
            }
        }
        return null;
    }

    public static String getPomContent(File file, MavenArtifactInfo mavenArtifactInfo) {
        String pom = MavenModelUtils.getPomFileAsStringFromJar(file);
        if(StringUtils.isBlank(pom)){
            Model mavenModel = MavenModelUtils.toMavenModel(mavenArtifactInfo);
            pom = MavenModelUtils.mavenModelToString(mavenModel);
        }

        return pom;
    }

    /**
     * populate unit info , 1st check if maven artifact info , if not return basic Artifact info
     *
     * @param uniqueFile - current file
     * @param uploadArtifactInfo -
     * @param mavenService -
     */
    public static void populateUnitInfo(File uniqueFile, String originalFileName,
            UploadArtifactInfo uploadArtifactInfo,
                                                 MavenService mavenService, RepoType repoType) {
        UnitInfo artifactInfo = null;
        uploadArtifactInfo.cleanData();
        if (uniqueFile.getName().endsWith(".pom")) {
            MavenArtifactInfo pomInfo = getArtifactInfoFromPom(uniqueFile,true);
            String pomFromPom = UnitUtils.getPomContent(uniqueFile, pomInfo);
            uploadArtifactInfo.setUnitConfigFileContent(pomFromPom);
            artifactInfo = pomInfo;
        }
        if (originalFileName.endsWith(".deb")) {
            artifactInfo = new DebianArtifactInfo(originalFileName);
        }
        if (originalFileName.endsWith(".box")) {
            artifactInfo = new VagrantArtifactInfo(originalFileName);
        }
        //if Maven
        if (NamingUtils.isJarVariant(originalFileName)) {
            MavenArtifactInfo mavenArtifactInfo = mavenService.getMavenArtifactInfo(uniqueFile, originalFileName);
            String pomFromJar = UnitUtils.getPomContent(uniqueFile, mavenArtifactInfo);
            uploadArtifactInfo.setUnitConfigFileContent(pomFromJar);
            artifactInfo = getMavenArtifactInfo(uniqueFile, pomFromJar, originalFileName);
        }
        if (RepoType.CRAN.equals(repoType)) {
            artifactInfo = new CranUnitInfo();
        }
        if(artifactInfo == null){
            artifactInfo = new ArtifactInfo(originalFileName);
        }

        uploadArtifactInfo.setUnitInfo(artifactInfo);
    }

    /**
     * get artifact info from pom
     *
     * @param file - current file
     * @return artifact info
     */
    private static MavenArtifactInfo getArtifactInfoFromPom(File file, boolean isPom) {
        MavenArtifactInfo mavenArtifactInfo = null;
        try {
            InputStream pomInputStream = new FileInputStream(file);
            mavenArtifactInfo = MavenModelUtils.mavenModelToArtifactInfo(pomInputStream);
            if (isPom) {
                mavenArtifactInfo.setType("pom");
            }
        } catch (FileNotFoundException | XmlPullParserException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
        return mavenArtifactInfo;
    }
}

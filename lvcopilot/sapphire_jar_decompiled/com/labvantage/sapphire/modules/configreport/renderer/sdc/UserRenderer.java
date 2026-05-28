/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.ro.UserRO;
import com.labvantage.sapphire.modules.configreport.util.UserUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.xml.PropertyListCollection;

public class UserRenderer
extends UserUtil {
    @Override
    public void createXMLReport() throws SapphireException {
        if (this.sdcRO != null && this.sdcRO.currentSDI != null) {
            FileOutputStream file;
            super.createXMLReport();
            DataSet pps = ((UserRO)this.sdcRO).getAllUserProfileProperties();
            String xmlFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
            xmlFileName = xmlFileName.replace(".xml", "_profileprops.xml");
            try {
                file = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlFileName);
            }
            try {
                file.write(pps.toXML().getBytes());
                file.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create profileprops file");
            }
            this.createSDCAccessMatrixXMLReport();
        }
    }

    @Override
    public ConfigReportContent getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User: ");
        UserRO currRO = (UserRO)sdcRO;
        if (this.includeDiffReport) {
            if (sdcRO == null || sdcRO.currentSDI == null) {
                currRO = (UserRO)refSdcRO;
                configReportContent.startSDISection(refSdcRO.currentSDI, refSdcRO.getDescription());
            } else if (refSdcRO == null || refSdcRO.currentSDI == null) {
                configReportContent.startSDISection(sdcRO.currentSDI, sdcRO.getDescription());
            } else {
                configReportContent.startSDISectionDiff(sdcRO.currentSDI, sdcRO.getDescription(), refSdcRO.getDescription());
            }
        } else {
            configReportContent.startSDISection(sdcRO.currentSDI, sdcRO.getDescription());
        }
        if (this.reportSummary) {
            ConfigReportContent matrix;
            if (this.includeDiffReport) {
                if (sdcRO == null || sdcRO.currentSDI == null) {
                    configReportContent.startSubSection("User", "The following is the summary of the User definition:");
                    configReportContent.appendSubSection(this.renderUserInfo(refSdcRO), "User", this.diffOnly);
                    configReportContent.startSubSection("User Profile", "");
                    configReportContent.appendSubSection(this.renderUserProfile(refSdcRO), "User Profile", this.diffOnly);
                    matrix = this.renderUserSDCAccessMatrix(refSdcRO);
                    if (matrix.length() > 0) {
                        configReportContent.startSubSection("SDC Access Matrix", "");
                        configReportContent.appendSubSection(matrix, "SDC Access Matrix", this.diffOnly);
                    }
                    if ((matrix = this.renderUserDeptAccessMatrix(refSdcRO)).length() > 0) {
                        configReportContent.startSubSection("Department Access Matrix", "");
                        configReportContent.appendSubSection(matrix, "Department Access Matrix", this.diffOnly);
                    }
                    configReportContent.endSubSection("", "Summary");
                } else if (this.refRO == null || refSdcRO.currentSDI == null) {
                    configReportContent.startSubSection("User", "The following is the summary of the User definition:");
                    configReportContent.appendSubSection(this.renderUserInfo(sdcRO), "User", this.diffOnly);
                    configReportContent.startSubSection("User Profile", "");
                    configReportContent.appendSubSection(this.renderUserProfile(sdcRO), "User Profile", this.diffOnly);
                    matrix = this.renderUserSDCAccessMatrix(sdcRO);
                    if (matrix.length() > 0) {
                        configReportContent.startSubSection("SDC Access Matrix", "");
                        configReportContent.appendSubSection(matrix, "SDC Access Matrix", this.diffOnly);
                    }
                    if ((matrix = this.renderUserDeptAccessMatrix(sdcRO)).length() > 0) {
                        configReportContent.startSubSection("Department Access Matrix", "");
                        configReportContent.appendSubSection(matrix, "Department Access Matrix", this.diffOnly);
                    }
                    configReportContent.endSubSection("", "Summary");
                } else {
                    configReportContent.startSubSection("User", "The following is the summary of the User definition:");
                    configReportContent.appendSubSection(this.renderUserInfoDiff(), "User", this.diffOnly);
                    configReportContent.startSubSection("User Profile", "");
                    configReportContent.appendSubSection(this.renderUserProfileDiff(), "User Profile", this.diffOnly);
                    matrix = this.renderUserSDCAccessMatrixDiff();
                    if (matrix.length() > 0) {
                        configReportContent.startSubSection("SDC Access Matrix", "");
                        configReportContent.appendSubSection(matrix, "SDC Access Matrix", this.diffOnly);
                    }
                    if ((matrix = this.renderUserDeptAccessMatrixDiff()).length() > 0) {
                        configReportContent.startSubSection("Department Access Matrix", "");
                        configReportContent.appendSubSection(matrix, "Department Access Matrix", this.diffOnly);
                    }
                    configReportContent.endSubSection("", "Summary");
                }
            } else {
                configReportContent.startSubSection("User", "The following is the summary of the User definition:");
                configReportContent.appendSubSection(this.renderUserInfo(sdcRO), "User", this.diffOnly);
                configReportContent.startSubSection("User Profile", "");
                configReportContent.appendSubSection(this.renderUserProfile(sdcRO), "User Profile", this.diffOnly);
                matrix = this.renderUserSDCAccessMatrix(sdcRO);
                if (matrix.length() > 0) {
                    configReportContent.startSubSection("SDC Access Matrix", "");
                    configReportContent.appendSubSection(matrix, "SDC Access Matrix", this.diffOnly);
                }
                if ((matrix = this.renderUserDeptAccessMatrix(sdcRO)).length() > 0) {
                    configReportContent.startSubSection("Department Access Matrix", "");
                    configReportContent.appendSubSection(matrix, "Department Access Matrix", this.diffOnly);
                }
                configReportContent.endSubSection("", "Summary");
            }
        }
        if (this.reportDetails) {
            configReportContent.startSection("Detailed Definition");
            configReportContent.appendSpecialContent(this.renderDetails(true, "password", this.getTranslationProcessor()), this.diffOnly);
            if (!this.includeDiffReport || refSdcRO == null || refSdcRO.currentSDI == null) {
                configReportContent.startSubHeading("User Profile Properties", "");
                configReportContent.renderListTable(currRO.getAllUserProfileProperties(), this.getTranslationProcessor());
                configReportContent.startSubHeading("SDCSecurity Matrix for User", "");
                configReportContent.renderMatrix(currRO.getSDCAccessMatrix(), 5);
                if (currRO.getDeptAccessMatrix().getRowCount() != 0) {
                    configReportContent.startSubHeading("Department Matrix for each User SDC Operation", "");
                    configReportContent.renderMatrix(currRO.getDeptAccessMatrix(), 5);
                }
            } else {
                String[] keycols = new String[]{"propertyid"};
                configReportContent.startSubHeading("User Profile Properties", "");
                PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("userprofileproperties");
                configReportContent.renderDiffListTable(currRO.getAllUserProfileProperties(), ((UserRO)refSdcRO).getAllUserProfileProperties(), keycols, ignoreDiffs, this.getTranslationProcessor());
                String[] keycols2 = new String[]{"sdcid"};
                configReportContent.startSubHeading("SDCSecurity Matrix for User", "");
                configReportContent.renderDiffMatrix(currRO.getSDCAccessMatrix(), ((UserRO)refSdcRO).getSDCAccessMatrix(), keycols2);
                if (currRO.getDeptAccessMatrix().getRowCount() != 0) {
                    configReportContent.startSubHeading("Department Matrix for each User SDC Operation", "");
                    configReportContent.renderDiffMatrix(currRO.getDeptAccessMatrix(), ((UserRO)refSdcRO).getDeptAccessMatrix(), keycols2);
                }
            }
            configReportContent.endSection();
        }
        if (this.reportCategories) {
            configReportContent.startSubHeading("Categories", "");
            if (!this.includeDiffReport) {
                configReportContent.renderCategories(sdcRO.getCategories());
            } else if (sdcRO == null || sdcRO.currentSDI == null) {
                configReportContent.renderCategories(refSdcRO.getCategories());
            } else if (refSdcRO == null || refSdcRO.currentSDI == null) {
                configReportContent.renderCategories(sdcRO.getCategories());
            } else {
                configReportContent.renderCategoriesDiff(sdcRO.getCategories(), refSdcRO.getCategories());
            }
        }
        configReportContent.endSection();
        this.updateSectionChangeInfo("User", ConfigReportContent.generateSDISectionTitle(currRO.currentSDI), configReportContent);
        return configReportContent;
    }
}


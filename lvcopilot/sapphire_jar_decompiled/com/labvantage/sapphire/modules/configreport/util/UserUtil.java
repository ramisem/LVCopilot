/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.ro.UserRO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UserUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderUserInfo(BaseSDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User Info: ");
        UserRO userRO = (UserRO)ro;
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.addRowItem("User ID", userRO.getUserId());
        configReportContent.addRowItem("Type", userRO.getUserType());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Full Name", userRO.getUserDesc());
        configReportContent.addRowItem("Password Expiry Date", userRO.getPasswordExpiryDt());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Force Change Password", userRO.getForceChangePassword());
        configReportContent.addRowItem("Locale", userRO.getLocale());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Time Zone", userRO.getTimeZone());
        configReportContent.addRowItem("Language", userRO.getLanguage());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Status", userRO.getStatus());
        configReportContent.addRowItem("Disabled Reason", userRO.getDisabledReason());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("GLP", userRO.getGLPFlag());
        configReportContent.addRowItem("Default Department", userRO.getDefaultDepartment());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Base Department", userRO.getBaseDepartment());
        configReportContent.addRowItem("Email", userRO.getEmail());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Default Job Type", userRO.getDefaultJobType());
        configReportContent.addRowItem("Last Logon Job Type", userRO.getLastLogOnJobType());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Security Type", userRO.getSecurityType());
        configReportContent.addRowItem("Authentication Type", userRO.getAuthenticationType());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Roles", userRO.getRoles(), 3);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Departments", userRO.getDepartments(), 3);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Modules", userRO.getModules(), 3);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Job Types", userRO.getJobTypes(), 3);
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderUserInfoDiff() {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User Info: ");
        UserRO userRO = (UserRO)this.sdcRO;
        configReportContent.startTable();
        UserRO refUserRO = (UserRO)this.refSdcRO;
        configReportContent.startRow();
        configReportContent.addDiffRowItem("User ID", userRO.getUserId(), refUserRO.getUserId());
        configReportContent.addDiffRowItem("Type", userRO.getUserType(), refUserRO.getUserType());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Full Name", userRO.getUserDesc(), refUserRO.getUserDesc());
        configReportContent.addDiffRowItem("Password Expiry Date", userRO.getPasswordExpiryDt(), refUserRO.getPasswordExpiryDt());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Force Change Password", userRO.getForceChangePassword(), refUserRO.getForceChangePassword());
        configReportContent.addDiffRowItem("Locale", userRO.getLocale(), refUserRO.getLocale());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Time Zone", userRO.getTimeZone(), refUserRO.getTimeZone());
        configReportContent.addDiffRowItem("Language", userRO.getLanguage(), refUserRO.getLanguage());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Status", userRO.getStatus(), refUserRO.getStatus());
        configReportContent.addDiffRowItem("Disabled Reason", userRO.getDisabledReason(), refUserRO.getDisabledReason());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("GLP", userRO.getGLPFlag(), refUserRO.getGLPFlag());
        configReportContent.addDiffRowItem("Default Department", userRO.getDefaultDepartment(), refUserRO.getDefaultDepartment());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Base Department", userRO.getBaseDepartment(), refUserRO.getBaseDepartment());
        configReportContent.addDiffRowItem("Email", userRO.getEmail(), refUserRO.getEmail());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Default Job Type", userRO.getDefaultJobType(), refUserRO.getDefaultJobType());
        configReportContent.addDiffRowItem("Last Logon Job Type", userRO.getLastLogOnJobType(), refUserRO.getLastLogOnJobType());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Security Type", userRO.getSecurityType(), refUserRO.getSecurityType());
        configReportContent.addDiffRowItem("Authentication Type", userRO.getAuthenticationType(), refUserRO.getAuthenticationType());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Roles", userRO.getRoles(), refUserRO.getRoles(), 3, false, this.getTranslationProcessor(), false);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Departments", userRO.getDepartments(), refUserRO.getDepartments(), 3, false, this.getTranslationProcessor(), true);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Modules", userRO.getModules(), refUserRO.getModules(), 3, false, this.getTranslationProcessor(), false);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Job Types", userRO.getJobTypes(), refUserRO.getJobTypes(), 3, false, this.getTranslationProcessor(), false);
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderUserProfile(BaseSDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User profile: ");
        UserRO userRO = (UserRO)ro;
        PropertyList pl = userRO.getUserProfile();
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.addRowItem("The default logon url is:", pl.getProperty("logonpageurl"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("BO URL:", pl.getProperty("bourl"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("BO Document Domain:", pl.getProperty("bodocumentdomain"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("BO Domain:", pl.getProperty("bodomain"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("BO Exchange Mode:", pl.getProperty("boexchangemode"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("BO Universe:", pl.getProperty("bouniverse"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("BO Username:", pl.getProperty("bousername"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("BO Authentication Type:", pl.getProperty("boauthenticationtype"));
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderUserProfileDiff() {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User profile:");
        UserRO userRO = (UserRO)this.sdcRO;
        PropertyList pl = userRO.getUserProfile();
        configReportContent.startTable();
        UserRO refUserRO = (UserRO)this.refSdcRO;
        PropertyList refpl = refUserRO.getUserProfile();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("The default logon url is:", pl.getProperty("logonpageurl"), refpl.getProperty("logonpageurl"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("BO URL:", pl.getProperty("bourl"), refpl.getProperty("bourl"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("BO Document Domain:", pl.getProperty("bodocumentdomain"), refpl.getProperty("bodocumentdomain"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("BO Domain:", pl.getProperty("bodomain"), refpl.getProperty("bodomain"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("BO Exchange Mode:", pl.getProperty("boexchangemode"), refpl.getProperty("boexchangemode"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("BO Universe:", pl.getProperty("bouniverse"), refpl.getProperty("bouniverse"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("BO Username:", pl.getProperty("bousername"), refpl.getProperty("bousername"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("BO Authentication Type:", pl.getProperty("boauthenticationtype"), refpl.getProperty("boauthenticationtype"));
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public DataSet getSummarySDCAccessMatrix(BaseSDCRO ro) {
        UserRO lvJobTypeRO = (UserRO)ro;
        DataSet dSet = lvJobTypeRO.getSDCAccessMatrix();
        if (dSet == null) {
            return null;
        }
        DataSet newMatrix = new DataSet();
        newMatrix.addColumn("SDC", 0);
        newMatrix.addColumn("Delete", 0);
        newMatrix.addColumn("Edit", 0);
        newMatrix.addColumn("List", 0);
        newMatrix.addColumn("ViewMaskedData", 0);
        for (int i = 0; i < dSet.getRowCount(); ++i) {
            int currRow = newMatrix.addRow();
            newMatrix.setString(currRow, "SDC", dSet.getValue(i, "Sdcid"));
            newMatrix.setString(currRow, "Delete", lvJobTypeRO.createSDCAccessRowData(dSet.getValue(i, "Operation Delete")));
            newMatrix.setString(currRow, "Edit", lvJobTypeRO.createSDCAccessRowData(dSet.getValue(i, "Operation Edit")));
            newMatrix.setString(currRow, "List", lvJobTypeRO.createSDCAccessRowData(dSet.getValue(i, "Operation List")));
            newMatrix.setString(currRow, "ViewMaskedData", lvJobTypeRO.createSDCAccessRowData(dSet.getValue(i, "Operation ViewMaskedData")));
        }
        return newMatrix;
    }

    public ConfigReportContent renderUserSDCAccessMatrix(BaseSDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User SDC Access:");
        configReportContent.renderMatrix(this.getSummarySDCAccessMatrix(ro), 4);
        return configReportContent;
    }

    public ConfigReportContent renderUserSDCAccessMatrixDiff() {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User SDC Access:");
        DataSet userSDCAccess = this.getSummarySDCAccessMatrix(this.sdcRO);
        if (userSDCAccess.getRowCount() == 0) {
            configReportContent.append("Not found.");
            return configReportContent;
        }
        DataSet refUserSDCAccess = this.getSummarySDCAccessMatrix(this.refSdcRO);
        String[] keycols = new String[]{"sdc"};
        configReportContent.renderDiffMatrix(userSDCAccess, refUserSDCAccess, keycols);
        return configReportContent;
    }

    public void createSDCAccessMatrixXMLReport() throws SapphireException {
        FileOutputStream file;
        DataSet pps = ((UserRO)this.sdcRO).getSDCAccessMatrix();
        String xmlFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
        xmlFileName = xmlFileName.replace(".xml", "_sdcaccess.xml");
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
            throw new SapphireException("Failed to create sdcaccess file");
        }
        pps = ((UserRO)this.sdcRO).getDeptAccessMatrix();
        xmlFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
        xmlFileName = xmlFileName.replace(".xml", "_deptaccess.xml");
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
            throw new SapphireException("Failed to create sdcaccess file");
        }
    }

    public DataSet getSummaryDeptAccessMatrix(BaseSDCRO ro) {
        UserRO userRO = (UserRO)ro;
        DataSet dSet = userRO.getDeptAccessMatrix();
        if (dSet == null) {
            return null;
        }
        DataSet newMatrix = new DataSet();
        newMatrix.addColumn("SDC", 0);
        newMatrix.addColumn("Delete", 0);
        newMatrix.addColumn("Edit", 0);
        newMatrix.addColumn("List", 0);
        newMatrix.addColumn("ViewMaskedData", 0);
        for (int i = 0; i < dSet.getRowCount(); ++i) {
            int currRow = newMatrix.addRow();
            newMatrix.setString(currRow, "SDC", dSet.getValue(i, "Sdcid"));
            newMatrix.setString(currRow, "Delete", dSet.getValue(i, "Operation Delete"));
            newMatrix.setString(currRow, "Edit", dSet.getValue(i, "Operation Edit"));
            newMatrix.setString(currRow, "List", dSet.getValue(i, "Operation List"));
            newMatrix.setString(currRow, "ViewMaskedData", dSet.getValue(i, "Operation ViewMaskedData"));
        }
        return newMatrix;
    }

    public ConfigReportContent renderUserDeptAccessMatrixDiff() {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User departmental access:");
        DataSet userSDCAccess = this.getSummaryDeptAccessMatrix(this.sdcRO);
        if (userSDCAccess == null || userSDCAccess.getRowCount() == 0) {
            return configReportContent;
        }
        DataSet refUserSDCAccess = this.getSummaryDeptAccessMatrix(this.refSdcRO);
        String[] keycols = new String[]{"sdc"};
        configReportContent.renderDiffMatrix(userSDCAccess, refUserSDCAccess, keycols);
        return configReportContent;
    }

    public ConfigReportContent renderUserDeptAccessMatrix(BaseSDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "User departmental access:");
        DataSet deptAccess = this.getSummaryDeptAccessMatrix(ro);
        if (deptAccess == null || deptAccess.getRowCount() == 0) {
            return configReportContent;
        }
        configReportContent.renderMatrix(deptAccess, 4);
        return configReportContent;
    }
}


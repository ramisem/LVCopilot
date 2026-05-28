/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.modules.configreport.ro.LV_JobTypeRO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;

public class LV_JobTypeUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderLV_JobTypeInfo(BaseSDCRO ro) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Job type info: ");
        LV_JobTypeRO jobTypeRO = (LV_JobTypeRO)ro;
        buffer.startTable();
        buffer.startRow();
        buffer.addRowItem("Job Type", jobTypeRO.getLV_JobTypeId(), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Require Re-authentication", jobTypeRO.getPrimaryValue("passwordrequiredflag"), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Description", jobTypeRO.getDescription(), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Default Logon URL", jobTypeRO.getDefaultLogonURL(), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Default Menu", jobTypeRO.getPrimaryValue("defaultmenu"), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Default Sidebar", jobTypeRO.getPrimaryValue("defaultsidebar"), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Default Department", jobTypeRO.getDefaultDepartment(), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Notes", jobTypeRO.getPrimaryValue("notes"), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Roles", jobTypeRO.getRoles(), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Departments", jobTypeRO.getDepartments(), 3);
        buffer.endRow();
        buffer.startRow();
        buffer.addRowItem("Users", jobTypeRO.getUsers(), 3);
        buffer.endRow();
        buffer.endTable();
        return buffer;
    }

    public ConfigReportContent renderLV_JobTypeInfoDiff() {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Job type info:");
        LV_JobTypeRO jobTypeRO = (LV_JobTypeRO)this.sdcRO;
        buffer.startTable();
        LV_JobTypeRO refjobTypeRO = null;
        if (this.refSdcRO != null) {
            refjobTypeRO = (LV_JobTypeRO)this.refSdcRO;
        }
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Job Type", jobTypeRO.getLV_JobTypeId(), refjobTypeRO.getLV_JobTypeId(), 3, this.getTranslationProcessor());
        } else {
            buffer.addDiffRowItem("Job Type", jobTypeRO.getLV_JobTypeId(), "");
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Require Re-authentication", jobTypeRO.getPrimaryValue("passwordrequiredflag"), refjobTypeRO.getPrimaryValue("passwordrequiredflag"), 3, this.getTranslationProcessor());
        } else {
            buffer.addDiffRowItem("Require Re-authentication", jobTypeRO.getPrimaryValue("passwordrequiredflag"), "", 3, this.getTranslationProcessor());
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Description", jobTypeRO.getDescription(), refjobTypeRO.getDescription(), 3, this.getTranslationProcessor());
        } else {
            buffer.addDiffRowItem("Description", jobTypeRO.getDescription(), "", 3, this.getTranslationProcessor());
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Default Logon URL", jobTypeRO.getDefaultLogonURL(), refjobTypeRO.getDefaultLogonURL(), 3, this.getTranslationProcessor());
        } else {
            buffer.addDiffRowItem("Default Logon URL", jobTypeRO.getDefaultLogonURL(), "", 3, this.getTranslationProcessor());
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Default Menu", jobTypeRO.getPrimaryValue("defaultmenu"), refjobTypeRO.getPrimaryValue("defaultmenu"), 3, this.getTranslationProcessor());
        } else {
            buffer.addDiffRowItem("Default Menu", jobTypeRO.getPrimaryValue("defaultmenu"), "", 3, this.getTranslationProcessor());
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Default Sidebar", jobTypeRO.getPrimaryValue("defaultsidebar"), refjobTypeRO.getPrimaryValue("defaultsidebar"), 3, this.getTranslationProcessor());
        } else {
            buffer.addDiffRowItem("Default Sidebar", jobTypeRO.getPrimaryValue("defaultsidebar"), "", 3, this.getTranslationProcessor());
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Default Department", jobTypeRO.getDefaultDepartment(), refjobTypeRO.getDefaultDepartment(), 3, this.getTranslationProcessor());
        } else {
            buffer.addDiffRowItem("Default Department", jobTypeRO.getDefaultDepartment(), "", 3, this.getTranslationProcessor());
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Notes", jobTypeRO.getPrimaryValue("notes"), refjobTypeRO.getPrimaryValue("notes"), 3, this.getTranslationProcessor());
        } else {
            buffer.addDiffRowItem("Notes", jobTypeRO.getDefaultDepartment(), "", 3, this.getTranslationProcessor());
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Roles", jobTypeRO.getRoles(), refjobTypeRO.getRoles(), 3, this.ignoreDiff("roles"), this.getTranslationProcessor(), false);
        } else {
            buffer.addDiffRowItem("Roles", jobTypeRO.getRoles(), "", 3, this.ignoreDiff("roles"), this.getTranslationProcessor(), false);
        }
        buffer.endRow();
        buffer.startRow();
        if (this.refSdcRO != null) {
            buffer.addDiffRowItem("Departments", jobTypeRO.getDepartments(), refjobTypeRO.getDepartments(), 3, this.ignoreDiff("departments"), this.getTranslationProcessor(), false);
        } else {
            buffer.addDiffRowItem("Departments", jobTypeRO.getDepartments(), "", 3, this.ignoreDiff("departments"), this.getTranslationProcessor(), false);
        }
        buffer.endRow();
        if (jobTypeRO.getUsers().length() > 0 || refjobTypeRO != null && refjobTypeRO.getUsers().length() > 0) {
            buffer.startRow();
            if (this.refSdcRO != null) {
                buffer.addDiffRowItem("Users", jobTypeRO.getUsers(), refjobTypeRO.getUsers(), 3, this.ignoreDiff("users"), this.getTranslationProcessor(), false);
            } else {
                buffer.addDiffRowItem("Users", jobTypeRO.getUsers(), "", 3, this.ignoreDiff("users"), this.getTranslationProcessor(), false);
            }
            buffer.endRow();
        }
        buffer.endTable();
        return buffer;
    }

    @Override
    public void createXMLReport() throws SapphireException {
        if (this.sdcRO != null && this.sdcRO.currentSDI != null) {
            FileOutputStream file;
            super.createXMLReport();
            DataSet pps = ((LV_JobTypeRO)this.sdcRO).getSDCAccessMatrix();
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
            pps = ((LV_JobTypeRO)this.sdcRO).getDeptAccessMatrix();
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
    }

    public ConfigReportContent renderLV_JobTypeSDCAccessMatrix(BaseSDCRO ro) {
        ConfigReportContent str = new ConfigReportContent(this.config, "Job type sdc access matrix: ");
        str.renderMatrix(this.getSummarySDCAccessMatrix(ro), 4);
        return str;
    }

    public ConfigReportContent renderLV_JobTypeDeptAccessMatrix(BaseSDCRO ro) {
        ConfigReportContent str = new ConfigReportContent(this.config, "Job type department access matrix: ");
        DataSet deptAccess = this.getSummaryDeptAccessMatrix(ro);
        if (deptAccess.getRowCount() == 0) {
            return str;
        }
        str.startSubHeading("Department Matrix for each Job Type SDC Operation", "");
        str.renderMatrix(deptAccess, 4);
        return str;
    }

    public ConfigReportContent renderLV_JobTypeSDCAccessMatrixDiff() {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Job type sdc access matrix: ");
        DataSet userSDCAccess = this.getSummarySDCAccessMatrix(this.sdcRO);
        if (userSDCAccess.getRowCount() == 0) {
            buffer.append("Not found.");
            return buffer;
        }
        DataSet refUserSDCAccess = this.getSummarySDCAccessMatrix(this.refSdcRO);
        String[] keycols = new String[]{"sdc"};
        buffer.renderDiffMatrix(userSDCAccess, refUserSDCAccess, keycols);
        return buffer;
    }

    public ConfigReportContent renderLV_JobTypeDeptAccessMatrixDiff() {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Job type department access matrix: ");
        DataSet userSDCAccess = this.getSummaryDeptAccessMatrix(this.sdcRO);
        if (userSDCAccess.getRowCount() == 0) {
            return buffer;
        }
        DataSet refUserSDCAccess = this.getSummaryDeptAccessMatrix(this.refSdcRO);
        String[] keycols = new String[]{"sdc"};
        buffer.startSubHeading("Department Matrix for each Job Type SDC Operation", "");
        buffer.renderDiffMatrix(userSDCAccess, refUserSDCAccess, keycols);
        return buffer;
    }

    public DataSet getSummarySDCAccessMatrix(BaseSDCRO ro) {
        if (ro == null) {
            return new DataSet();
        }
        LV_JobTypeRO lvJobTypeRO = (LV_JobTypeRO)ro;
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

    public DataSet getSummaryDeptAccessMatrix(BaseSDCRO ro) {
        LV_JobTypeRO lvJobTypeRO;
        DataSet dSet;
        if (ro == null) {
            new DataSet();
        }
        if ((dSet = (lvJobTypeRO = (LV_JobTypeRO)ro).getDeptAccessMatrix()) == null) {
            return null;
        }
        DataSet newMatrix = new DataSet();
        newMatrix.addColumn("SDC", 0);
        newMatrix.addColumn("Delete", 0);
        newMatrix.addColumn("Edit", 0);
        newMatrix.addColumn("List", 0);
        for (int i = 0; i < dSet.getRowCount(); ++i) {
            int currRow = newMatrix.addRow();
            newMatrix.setString(currRow, "SDC", dSet.getValue(i, "Sdcid"));
            newMatrix.setString(currRow, "Delete", dSet.getValue(i, "Operation Delete"));
            newMatrix.setString(currRow, "Edit", dSet.getValue(i, "Operation Edit"));
            newMatrix.setString(currRow, "List", dSet.getValue(i, "Operation List"));
        }
        return newMatrix;
    }
}


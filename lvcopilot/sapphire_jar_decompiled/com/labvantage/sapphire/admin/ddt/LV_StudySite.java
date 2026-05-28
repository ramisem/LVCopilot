/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_StudySite
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 75609 $";

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (BusinessRulesUtil.isStudySiteDependentParticipantExists(rsetid, this.database)) {
            this.throwError("DependentParticipant", "VALIDATION", this.getTranslationProcessor().translate("Some LV_StudySite(s) are found to be associated with LV_Participant(s)!"));
        }
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (this.isCMTImport()) {
            for (int i = 0; i < primary.size(); ++i) {
                if ("Y".equals(primary.getString(i, "clinicalflag"))) continue;
                primary.setString(i, "clinicalprotocolid", null);
            }
        }
        this.setProtocolId(sdiData);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.setProtocolId(sdiData);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.validateStudySiteName(sdiData);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.validateStudySiteName(sdiData);
    }

    private void validateStudySiteName(SDIData sdiData) throws SapphireException {
        if (this.isCMTImport()) {
            return;
        }
        DataSet primary = sdiData.getDataset("primary");
        if (primary.size() > 0) {
            HashSet<String> departmentSet = new HashSet<String>();
            String sstudyid = primary.getString(0, "sstudyid", "");
            boolean isProtocolStudy = Study.isProtocolDriven(this.getQueryProcessor(), sstudyid);
            String departmentid = primary.getString(0, "departmentid", "");
            departmentSet.add(departmentid);
            if (isProtocolStudy && departmentid.length() == 0) {
                this.throwError("Missing Department", "VALIDATION", this.getTranslationProcessor().translate("Study Site is missing Department."));
            }
            for (int i = 0; i < primary.size(); ++i) {
                String studysiteid = primary.getString(i, "s_studysiteid", "");
                String studysitename = primary.getString(i, "studysitedesc", "");
                if (!studysitename.equalsIgnoreCase("") && this.isStudySiteNameExist(studysiteid, studysitename)) {
                    this.throwError("UniqueStudySiteName", "VALIDATION", this.getTranslationProcessor().translate("Study Site name must be unique."));
                }
                if (!isProtocolStudy) continue;
                String _departmentid = primary.getString(i, "departmentid", "");
                departmentSet.add(_departmentid);
                if (i <= 0 || !_departmentid.equals(departmentid)) continue;
                this.throwError("Duplicate Department", "VALIDATION", this.getTranslationProcessor().translate("No two Study Sites can have same Department."));
            }
            if (isProtocolStudy) {
                for (String department : departmentSet) {
                    if (this.database.getPreparedCount("select count(s_studysiteid) from s_studysite where sstudyid = ? and departmentid = ?", new String[]{sstudyid, department}) <= 1) continue;
                    this.throwError("Duplicate Department", "VALIDATION", this.getTranslationProcessor().translate("No two Study Sites can have same Department."));
                }
            }
        }
    }

    private boolean isStudySiteNameExist(String studysiteid, String studysitename) throws SapphireException {
        String sql = "SELECT 1 FROM s_studysite where sstudyid=(select sstudyid from s_studysite where s_studysiteid=?) AND s_studysiteid!=? AND studysitedesc=? ";
        this.database.createPreparedResultSet(sql, new Object[]{studysiteid, studysiteid, studysitename});
        return this.database.getNext();
    }

    private void setProtocolId(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary.isValidColumn("s_studysiteid")) {
            for (int i = 0; i < primary.size(); ++i) {
                String studysiteid = primary.getString(i, "s_studysiteid");
                String studyid = primary.getString(i, "sstudyid", "");
                if (studyid.length() == 0) {
                    studyid = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "sstudyid", "s_studysiteid = ?", new String[]{studysiteid});
                }
                if (!Study.isProtocolDriven(this.getQueryProcessor(), studyid)) continue;
                String clinicalprotocolid = primary.getString(i, "clinicalprotocolid", "");
                if (clinicalprotocolid.length() == 0 || !studyid.equals(clinicalprotocolid)) {
                    primary.setString(i, "clinicalprotocolid", studyid);
                }
                if (this.isCMTImport() || !this.hasPrimaryValueChanged(primary, i, "clinicalprotocolrevision")) continue;
                clinicalprotocolid = primary.getString(i, "clinicalprotocolid");
                String clinicalprotocolrevision = primary.getString(i, "clinicalprotocolrevision", this.getOldPrimaryValue(primary, i, "clinicalprotocolrevision"));
                if (clinicalprotocolrevision.length() == 0) {
                    this.setError(this.getTranslationProcessor().translate("Blank Protocol Revision"), "VALIDATION", this.getTranslationProcessor().translate("Protocol Revision must not be blank"));
                    continue;
                }
                if (this.database.getPreparedCount("select count(s_clinicalprotocolid) from s_clinicalprotocol where s_clinicalprotocolid = ? and s_clinicalprotocolrevision = ? and versionstatus = 'C'", new String[]{clinicalprotocolid, clinicalprotocolrevision}) != 0) continue;
                this.setError(this.getTranslationProcessor().translate("Provisional Protocol Revision"), "VALIDATION", this.getTranslationProcessor().translate("Protocol Revision must have an Approved version") + " [" + clinicalprotocolrevision + "]");
            }
        }
    }
}


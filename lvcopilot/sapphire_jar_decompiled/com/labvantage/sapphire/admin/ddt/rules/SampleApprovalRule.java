/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.LV_SampleFamily;
import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public class SampleApprovalRule
extends BaseRule {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String __Subject_Missing = "Subject Id must not be null. ";
    public static final String __Patient_Missing = "Patient Id must not be null.";

    public SampleApprovalRule() {
    }

    public SampleApprovalRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public void processRule(String samplefamilyid, HashMap valMap) throws SapphireException {
        String subjectId = valMap.containsKey("subjectid") ? valMap.get("subjectid").toString() : LV_SampleFamily.getSubject(this.database, samplefamilyid);
        String externalSubjectId = valMap.containsKey("externalsubject") ? valMap.get("externalsubject").toString() : LV_SampleFamily.getExternalSubject(this.database, samplefamilyid);
        String studyId = valMap.containsKey("studyid") ? valMap.get("studyid").toString() : LV_SampleFamily.getStudyid(this.database, samplefamilyid);
        boolean hasRequireLinkToPatient = valMap.containsKey("subjectrequiredflag") ? Boolean.getBoolean(valMap.get("subjectrequiredflag").toString()) : Study.hasRequireLinkToPatient(this.database, studyId);
        boolean isApproved = LV_SampleFamily.isApproved(this.database, samplefamilyid);
        StringBuffer problems = new StringBuffer();
        if (samplefamilyid != null && samplefamilyid.trim().length() > 0) {
            this.startRule();
            if (isApproved) {
                if (subjectId == null || subjectId.length() < 1) {
                    problems.append(__Subject_Missing);
                }
                if (hasRequireLinkToPatient && (externalSubjectId == null || externalSubjectId.length() < 1)) {
                    problems.append(__Patient_Missing);
                }
            }
            if (problems.length() > 0) {
                throw new SapphireException(problems.toString());
            }
            this.endRule();
        }
    }
}


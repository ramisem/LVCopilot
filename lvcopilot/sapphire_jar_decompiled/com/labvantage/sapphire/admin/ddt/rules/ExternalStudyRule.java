/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public class ExternalStudyRule
extends BaseRule {
    public ExternalStudyRule() {
    }

    public ExternalStudyRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public void processRule(String studyid) throws SapphireException {
        if (studyid != null && studyid.length() > 0) {
            StringBuffer error = new StringBuffer();
            String studyType = Study.getStudyType(this.database, studyid);
            boolean subjectRequired = Study.hasRequireLinkToPatient(this.database, studyid);
            boolean collectionInfo = Study.hasCollectionInformation(this.database, studyid);
            boolean events = Study.hasClinicalEvents(this.database, studyid);
            boolean sites = Study.hasClinicalSites(this.database, studyid);
            if (studyType != null && studyType.equals("Externally Submitted")) {
                if (subjectRequired) {
                    error.append("Externally Submitted studies cannot require links to the patient.<BR>");
                }
                if (collectionInfo) {
                    error.append("Externally Submitted studies cannot require collection information.<BR>");
                }
                if (error.length() > 0) {
                    throw new SapphireException(error.toString());
                }
            }
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;

public class StudyHasProtocolRule
extends BaseBioBankRule {
    public StudyHasProtocolRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    @Override
    public String getRuleId() {
        return "Study Has Protocol Rule";
    }

    public void processRule(String studyid, boolean forceUpdate) throws SapphireException {
        if (this.isRuleActive() && StringUtil.getLen(studyid) > 0L) {
            String studyType = Study.getStudyType(this.database, studyid);
            String protocolName = Study.getProtocolName(this.database, studyid);
            if (studyType != null && studyType.equals("Clinical") && (protocolName == null || protocolName.trim().length() == 0)) {
                throw new SapphireException();
            }
        }
    }
}


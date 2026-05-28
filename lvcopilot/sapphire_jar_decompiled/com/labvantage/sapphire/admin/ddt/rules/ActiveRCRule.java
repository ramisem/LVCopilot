/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;

public class ActiveRCRule
extends BaseBioBankRule {
    public ActiveRCRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    @Override
    public String getRuleId() {
        return "Active RC Rule";
    }

    public void processRule(String sampleid, String status, String studyid) throws SapphireException {
        if (StringUtil.getLen(sampleid) > 0L && ("Allocated".equals(status) || "Received".equals(status)) && StringUtil.getLen(studyid) > 0L && !this.studyHasActiveRC(studyid)) {
            throw new SapphireException(this.getRuleId(), this.getTranslationProcessor().translate("Study must have an active Restriction Class") + " (" + studyid + ")");
        }
    }
}


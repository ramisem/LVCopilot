/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;

public class ConsCOCRule
extends BaseRule {
    public ConsCOCRule() {
    }

    public ConsCOCRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public String processRule(String studyid) throws SapphireException {
        this.processRule(studyid, true);
        return "";
    }

    public void processRule(String studyid, boolean forceUpdate) throws SapphireException {
        if (studyid != null && studyid.length() > 0) {
            this.startRule();
            boolean coc = Study.getConsCOC(this.database, studyid);
            boolean dr = Study.getConsDR(this.database, studyid);
            boolean newcoc = Study.hasRestClassWithCOC(this.database, studyid);
            boolean newdr = Study.hasRestClassWithDR(this.database, studyid);
            if (coc != newcoc || dr != newdr) {
                if (forceUpdate) {
                    Study.setConsCOCDR(this.connectionInfo, studyid, newcoc, newdr, forceUpdate);
                } else {
                    throw new SapphireException("Study: " + studyid + ". Flag for " + (coc != newcoc ? " ConcsCOC" : "") + (dr != newdr ? " ConsDR" : "") + " does not match values derived from restriction classes.");
                }
            }
            this.endRule();
        }
    }
}


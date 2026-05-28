/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;

public class ConnectByLoopRule
extends BaseRule {
    public ConnectByLoopRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public void processRule(String tableid, String keyid, String parentid, String keycolid1, String parentcolid) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: ConnectByLoopRule");
        this.validateParent(tableid, keyid, parentid, keycolid1, parentcolid);
        Trace.logInfo("END: ConnectByLoopRule. Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private void validateParent(String tableid, String keyid, String parentid, String keycolid, String parentcolid) throws SapphireException {
        if (parentid.equals(keyid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Connect By Loop Rule"), "VALIDATION", this.getTranslationProcessor().translate("A node can not be it's own parent"));
        }
        boolean errorFlag = false;
        StringBuilder sql = new StringBuilder();
        if (this.connectionInfo.isOracle()) {
            sql.append("select ").append(keycolid);
            sql.append(" from ").append(tableid);
            sql.append(" start with ").append(keycolid).append(" = ?");
            sql.append(" connect by prior ").append(keycolid).append(" = ").append(parentcolid);
        } else {
            sql.append("WITH TreeHeirarchy (").append(keycolid).append(")");
            sql.append(" AS (");
            sql.append(" SELECT t1.").append(keycolid).append(" FROM ").append(tableid).append(" AS t1 WHERE t1.").append(keycolid).append(" = ?");
            sql.append(" UNION ALL");
            sql.append(" SELECT t1.").append(keycolid).append(" FROM ").append(tableid).append(" AS t1 INNER JOIN TreeHeirarchy AS d ON t1.").append(parentcolid).append(" = d.").append(keycolid);
            sql.append(" )");
            sql.append("SELECT ").append(tableid).append(".").append(keycolid).append(" from ").append(tableid);
            sql.append(" where ").append(tableid).append(".").append(keycolid).append(" in (SELECT st.").append(keycolid).append(" FROM TreeHeirarchy st)");
        }
        DataSet child = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{keyid});
        if (child != null) {
            for (int i = 0; i < child.size(); ++i) {
                if (!parentid.equals(child.getValue(i, keycolid))) continue;
                errorFlag = true;
                break;
            }
        }
        if (errorFlag) {
            throw new SapphireException(this.getTranslationProcessor().translate("Connect By Loop Rule"), "VALIDATION", this.getTranslationProcessor().translate("Any descendent node can not be the parent of same node"));
        }
    }
}


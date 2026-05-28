/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class MaxTIAllowedRule
extends BaseRule {
    protected String LABVANTAGE_CVS_ID = "$Revision: 77330 $";

    public MaxTIAllowedRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public void processRule(List<String> storageunitids) throws SapphireException {
        if (storageunitids == null || storageunitids.size() == 0) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: MaxTIAllowedRule");
        HashSet<String> set = new HashSet<String>();
        set.addAll(storageunitids);
        DataSet ds = null;
        String rsetid = null;
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        if (set.size() > 750) {
            try {
                rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", OpalUtil.toDelimitedString(set, ";"), null, null);
                sql.append("select s.storageunitid, s.maxtiallowed, ");
                sql.append(" (select count(t.trackitemid) ticount from trackitem t where t.currentstorageunitid = s.storageunitid) ticount");
                sql.append(" from storageunit s, rsetitems r");
                sql.append(" where s.maxtiallowed != -1");
                sql.append(" and s.maxtiallowed is not null");
                sql.append(" and r.keyid1 = s.storageunitid");
                sql.append(" and r.sdcid = 'StorageUnitSDC'");
                sql.append(" and r.rsetid = ").append(safeSQL.addVar(rsetid));
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            catch (SapphireException e) {
                throw new SapphireException(this.getTranslationProcessor().translate("MaxTrackItemAllowedRule"), "FAILURE", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
            }
            finally {
                if (StringUtil.getLen(rsetid) > 0L) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            }
        } else {
            sql.append("select s.storageunitid, s.maxtiallowed, ");
            sql.append(" (select count(t.trackitemid) ticount from trackitem t where t.currentstorageunitid = s.storageunitid) ticount");
            sql.append(" from storageunit s");
            sql.append(" where s.maxtiallowed != -1");
            sql.append(" and s.maxtiallowed is not null");
            if (set.size() == 1) {
                sql.append(" and s.storageunitid = ").append(safeSQL.addVar(set.iterator().next()));
            } else {
                sql.append(" and s.storageunitid in ( ").append(safeSQL.addIn(OpalUtil.toDelimitedString(set, "','"))).append(" )");
            }
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                int maxtiallowed = ds.getInt(i, "maxtiallowed");
                int ticount = ds.getInt(i, "ticount");
                if (ticount <= maxtiallowed) continue;
                String error = "{{No space available in}} " + StorageUnitSDC.getStorageUnitSDIInfo(this.getQueryProcessor(), ds.getValue(i, "storageunitid"));
                throw new SapphireException(this.getTranslationProcessor().translate("MaxTrackItemAllowedRule"), "VALIDATION", this.getTranslationProcessor().translatePartial(error));
            }
        }
        Trace.logInfo("END: MaxTIAllowedRule. Took " + (System.currentTimeMillis() - start) + "ms.");
    }
}


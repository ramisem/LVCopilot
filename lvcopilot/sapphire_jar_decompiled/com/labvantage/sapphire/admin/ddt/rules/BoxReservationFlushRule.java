/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.Collection;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class BoxReservationFlushRule
extends BaseRule {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 90958 $";

    public BoxReservationFlushRule() {
    }

    public BoxReservationFlushRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public String getRuleId() {
        return "BoxReservationFlushRule";
    }

    public void processRule(Collection<String> boxList, boolean forceUpdate) throws SapphireException {
        DataSet ds;
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select reservestorageunit.storageunitid");
        sql.append(" from reservestorageunit, storageunit, storageunit parentstorageunit");
        sql.append(" where reservestorageunit.storageunitid = storageunit.storageunitid");
        sql.append("  and storageunit.parentid = parentstorageunit.storageunitid");
        sql.append("  and parentstorageunit.linksdcid = 'LV_Box'");
        if (boxList.size() > 1000) {
            String rsetid = this.getDAMProcessor().createRSet("LV_Box", OpalUtil.toDelimitedString(boxList, ";"), null, null);
            sql.append("  and parentstorageunit.linkkeyid1 in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            sql.append("  and parentstorageunit.linkkeyid1 in (").append(safeSQL.addIn(boxList)).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (OpalUtil.isNotEmpty(ds)) {
            if (forceUpdate) {
                for (int i = 0; i < ds.size(); ++i) {
                    String storageunitid = ds.getString(i, "storageunitid", "");
                    String trackitemid = ds.getString(i, "trackitemid", "");
                    if (storageunitid.length() <= 0 || trackitemid.length() <= 0) continue;
                    sql.setLength(0);
                    sql.append("delete from reservestorageunit");
                    sql.append(" where storageunitid = ? and trackitemid = ?");
                    this.database.executePreparedUpdate(sql.toString(), new Object[]{storageunitid, trackitemid});
                }
            } else {
                throw new SapphireException("BoxReservationFlushRule", "CONFIRM", this.getTranslationProcessor().translatePartial("{{All the reservations will be cleared for boxes}}"));
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class SampleReservationPerCDRule
extends BaseRule {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 54060 $";

    public SampleReservationPerCDRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public void processRule(String sampleid) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        if (sampleid != null && sampleid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("select s2.storageunitid, s2.linksdcid, s2.linkkeyid1, s2.linkkeyid2, s2.linkkeyid3,");
            sql.append(" (select t3.custodialdepartmentid from trackitem t3 where t3.linksdcid = s2.linksdcid and t3.linkkeyid1 = s2.linkkeyid1 ) custodialdepartmentid");
            sql.append(" from reservestorageunit r, storageunit s, storageunit s2");
            sql.append(" where s.parentid = s2.storageunitid");
            sql.append(" and r.storageunitid = s.storageunitid");
            sql.append(" and r.trackitemid = ( select t2.trackitemid ");
            sql.append(" from trackitem t2 ");
            sql.append(" where t2.linksdcid = 'Sample' ");
            sql.append(" and t2.linkkeyid1 = ").append(safeSQL.addVar(sampleid)).append(" )");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < ds.size(); ++i) {
                    String cd = ds.getValue(i, "custodialdepartmentid");
                    if (cd == null || cd.trim().length() <= 0) continue;
                    if (list.contains(cd)) {
                        throw new SapphireException("SampleReservationPerCDRule", "VALIDATION", this.getTranslationProcessor().translate("Sample has multiple reservations in Custodial Department") + " (" + cd + ")");
                    }
                    list.add(cd);
                }
            }
        }
    }
}


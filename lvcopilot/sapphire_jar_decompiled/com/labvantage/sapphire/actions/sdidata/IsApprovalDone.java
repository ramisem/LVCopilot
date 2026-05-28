/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.SDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class IsApprovalDone
extends BaseAction
implements sapphire.action.IsApprovalDone {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String aprovalstep;
        String dataset;
        String variantid;
        String paramlistversionid;
        SDI sdi = new SDI(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
        if (!sdi.isValid()) {
            throw new SapphireException("NO_SDCID", "No sdcid specified");
        }
        properties.setProperty("approvaldone", "No");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer select = new StringBuffer("SELECT\tapprovalflag ");
        select.append("FROM\tsdidataapproval ").append("WHERE\tsdcid = ").append(safeSQL.addVar(sdi.getSdcid())).append(" AND keyid1 = ").append(safeSQL.addVar(sdi.getKeyid1())).append(" AND keyid2 = ").append(safeSQL.addVar(sdi.getKeyid2())).append(" AND keyid3 = ").append(safeSQL.addVar(sdi.getKeyid3())).append(" AND ");
        String paramlistid = properties.getProperty("paramlistid");
        if (paramlistid != null && paramlistid.length() > 0) {
            select.append("paramlistid = " + safeSQL.addVar(paramlistid) + " AND ");
        }
        if ((paramlistversionid = properties.getProperty("paramlistversionid")) != null && paramlistversionid.length() > 0) {
            select.append("paramlistversionid = " + safeSQL.addVar(paramlistversionid) + " AND ");
        }
        if ((variantid = properties.getProperty("variantid")) != null && variantid.length() > 0) {
            select.append("variantid = " + safeSQL.addVar(variantid) + " AND ");
        }
        if ((dataset = properties.getProperty("dataset")) != null && dataset.length() > 0) {
            select.append("dataset = " + safeSQL.addVar(dataset) + " AND ");
        }
        if ((aprovalstep = properties.getProperty("approvalstep")) != null && aprovalstep.length() > 0) {
            select.append("approvalstep = " + safeSQL.addVar(aprovalstep) + " AND ");
        }
        select.append("mandatoryflag = 'Y' and approvalflag = 'U' ");
        this.database.createPreparedResultSet(select.toString(), safeSQL.getValues());
        if (this.database.getNext()) {
            properties.setProperty("approvaldone", "No");
        } else {
            properties.setProperty("approvaldone", "Yes");
        }
    }
}


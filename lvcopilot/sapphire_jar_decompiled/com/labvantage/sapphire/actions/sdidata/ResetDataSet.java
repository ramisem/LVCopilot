/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.util.StringHolder;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ResetDataSet
extends BaseAction
implements sapphire.action.ResetDataSet {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int rc = 1;
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        StringHolder rsetidHolder = new StringHolder();
        boolean applylock = properties.getProperty("applylock").equals("Y");
        DAMProcessor damProcessor = this.getDAMProcessor();
        rc = damProcessor.createRSetDS(sdcid, keyid1, keyid2, keyid3, "", "", "", "", false, true, false, rsetidHolder);
        if (rc == 2) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create Rset");
        }
        String rsetid = rsetidHolder.value;
        if (applylock && (rc = damProcessor.lockRSet(rsetidHolder)) == 2) {
            damProcessor.clearRSet(rsetid);
            throw new SapphireException("CREATE_LOCK_FAILURE", "Failed to create lock rset");
        }
        rsetid = rsetidHolder.value;
        String paramlistid = properties.getProperty("paramlistid");
        String paramlistversionid = properties.getProperty("paramlistversionid");
        String variantid = properties.getProperty("variantid");
        String dataset = properties.getProperty("dataset");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer whereclause = new StringBuffer(this.connectionInfo.isOracle() ? "( sdcid, keyid1, keyid2, keyid3 ) IN ( SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )" : "( \tsdcid IN ( SELECT sdcid FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") AND \tkeyid1 IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") AND \tkeyid2 IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") AND \tkeyid3 IN ( SELECT keyid3 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") )");
        if (paramlistid.length() > 0) {
            whereclause.append(" AND paramlistid = ");
            whereclause.append(safeSQL.addVar(paramlistid));
        }
        if (paramlistversionid.length() > 0) {
            whereclause.append(" AND paramlistversionid = ");
            whereclause.append(safeSQL.addVar(paramlistversionid));
        }
        if (variantid.length() > 0) {
            whereclause.append(" AND variantid = ");
            whereclause.append(safeSQL.addVar(variantid));
        }
        if (dataset.length() > 0) {
            whereclause.append(" AND dataset = ");
            whereclause.append(safeSQL.addVar(dataset));
        }
        StringBuffer update = new StringBuffer();
        update.append("UPDATE sdidataitem SET ");
        update.append("\t\tenteredtext = null, ");
        update.append("\t\tenteredunits = null, ");
        update.append("\t\tenteredvalue = null, ");
        update.append("\t\ttransformvalue = null, ");
        update.append("\t\ttransformdt = null, ");
        update.append("\t\ttransformtext = null, ");
        update.append("\t\tdisplayvalue = null, ");
        update.append("\t\tenteredqualifier = null, ");
        update.append("\t\tenteredoperator = null, ");
        update.append("\t\treleasedflag = 'N', ");
        update.append("\t\tvaluestatus = null ");
        update.append("WHERE ");
        update.append(whereclause.toString());
        this.logger.info(update.toString());
        try {
            this.database.executePreparedUpdate(update.toString(), safeSQL.getValues());
        }
        catch (SapphireException ex) {
            damProcessor.clearRSet(rsetid);
            throw new SapphireException("EXECUTE_STMT_FAILED", ErrorUtil.extractMessage("Failed to update sdidataitem. Reason: " + update, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
        }
        update = new StringBuffer("UPDATE sdidataitemspec SET ");
        update.append("\t\tcondition = null ");
        update.append("WHERE ");
        update.append(whereclause.toString());
        this.logger.info(update.toString());
        try {
            this.database.executePreparedUpdate(update.toString(), safeSQL.getValues());
        }
        catch (SapphireException e) {
            damProcessor.clearRSet(rsetid);
            throw new SapphireException("EXECUTE_STMT_FAILED", ErrorUtil.extractMessage("Failed to update sdidataitem. Reason: " + update, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        update = new StringBuffer("UPDATE sdidataitemlimits SET ");
        update.append("\t\tstatusflag = null ");
        update.append("WHERE ");
        update.append(whereclause.toString());
        this.logger.info(update.toString());
        try {
            this.database.executePreparedUpdate(update.toString(), safeSQL.getValues());
        }
        catch (SapphireException e) {
            damProcessor.clearRSet(rsetid);
            throw new SapphireException("EXECUTE_STMT_FAILED", ErrorUtil.extractMessage("Failed to update sdidataitem. Reason: " + update, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        update = new StringBuffer("UPDATE sdidataapproval SET ");
        update.append("\t\tapprovalflag = 'U' ");
        update.append("WHERE ");
        update.append(whereclause.toString());
        this.logger.info(update.toString());
        try {
            this.database.executePreparedUpdate(update.toString(), safeSQL.getValues());
        }
        catch (SapphireException e) {
            damProcessor.clearRSet(rsetid);
            throw new SapphireException("EXECUTE_STMT_FAILED", ErrorUtil.extractMessage("Failed to update sdidataitem. Reason: " + update, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        damProcessor.clearRSet(rsetid);
    }
}


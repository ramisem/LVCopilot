/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workitem;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SetSDIWIIComplete
extends BaseAction
implements sapphire.action.SetSDIWIIComplete {
    private static final String PROPERTY_SEPARATOR = "separator";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean applylock = properties.getProperty("applylock").equals("Y");
        String sdcid = properties.getProperty("sdcid");
        String rsetid = null;
        DAMProcessor dam = this.getDAMProcessor();
        rsetid = BaseSDIDataAction.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), this.database, this.connectionInfo, applylock);
        String separator = properties.getProperty(PROPERTY_SEPARATOR, ";");
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
        String[] workitemidprop = StringUtil.split(properties.getProperty("workitemid"), separator);
        String[] workiteminstanceprop = StringUtil.split(properties.getProperty("workiteminstance"), separator);
        String[] workitemitemidprop = StringUtil.split(properties.getProperty("workitemitemid"), separator);
        String[] paramlistidprop = StringUtil.split(properties.getProperty("paramlistid"), separator);
        String[] paramlistversionidprop = StringUtil.split(properties.getProperty("paramlistversionid"), separator);
        String[] variantidprop = StringUtil.split(properties.getProperty("variantid"), separator);
        String[] datasetprop = StringUtil.split(properties.getProperty("dataset"), separator);
        int workitemidpos = 0;
        int workiteminstancepos = 0;
        int workitemitemidpos = 0;
        int paramlistidpos = 0;
        int paramlistversionidpos = 0;
        int variantidpos = 0;
        int datasetpos = 0;
        if (keyid1prop.length > 0 && keyid1prop[0].length() > 0) {
            String complete = properties.getProperty("complete", "Y").toUpperCase();
            String sql = "update sdiworkitemitem set completeflag='" + SafeSQL.encodeForSQL(complete, this.database.isOracle()) + "' where sdcid='" + SafeSQL.encodeForSQL(sdcid, this.database.isOracle()) + "' and keyid1=? and keyid2=? and keyid3=?";
            int columncount = 3;
            if (workitemidprop.length == keyid1prop.length && workitemidprop[0].length() > 0) {
                sql = sql + " and workitemid=?";
                workitemidpos = ++columncount;
            }
            if (workiteminstanceprop.length == keyid1prop.length && workiteminstanceprop[0].length() > 0) {
                sql = sql + " and workiteminstance=?";
                workiteminstancepos = ++columncount;
            }
            if (workitemitemidprop.length == keyid1prop.length && workitemitemidprop[0].length() > 0) {
                sql = sql + " and workitemitemid=?";
                workitemitemidpos = ++columncount;
            }
            if (paramlistidprop.length == keyid1prop.length && paramlistidprop[0].length() > 0) {
                sql = sql + " and itemkeyid1=?";
                paramlistidpos = ++columncount;
            }
            if (paramlistversionidprop.length == keyid1prop.length && paramlistversionidprop[0].length() > 0) {
                sql = sql + " and itemkeyid2=?";
                paramlistversionidpos = ++columncount;
            }
            if (variantidprop.length == keyid1prop.length && variantidprop[0].length() > 0) {
                sql = sql + " and itemkeyid3=?";
                variantidpos = ++columncount;
            }
            if (datasetprop.length == keyid1prop.length && datasetprop[0].length() > 0) {
                sql = sql + " and iteminstance=?";
                datasetpos = ++columncount;
            }
            try {
                this.logger.info("Preparing statement: " + sql + " to be used for " + keyid1prop.length + " values");
                PreparedStatement updatestatus = this.database.prepareStatement(sql);
                for (int i = 0; i < keyid1prop.length; ++i) {
                    StringBuffer logparams = new StringBuffer();
                    String keyid1 = keyid1prop[i];
                    String keyid2 = keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                    String keyid3 = keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                    updatestatus.setString(1, keyid1);
                    updatestatus.setString(2, keyid2);
                    updatestatus.setString(3, keyid3);
                    logparams.append(keyid1).append(";").append(keyid2).append(";").append(keyid3);
                    if (workitemidpos > 0) {
                        updatestatus.setString(workitemidpos, workitemidprop[i]);
                        logparams.append(";").append(workitemidprop[i]);
                    }
                    if (workiteminstancepos > 0) {
                        updatestatus.setInt(workiteminstancepos, Integer.parseInt(workiteminstanceprop[i]));
                        logparams.append(";").append(workiteminstanceprop[i]);
                    }
                    if (workitemitemidpos > 0) {
                        updatestatus.setString(workitemitemidpos, workitemitemidprop[i]);
                        logparams.append(";").append(workitemitemidprop[i]);
                    }
                    if (paramlistidpos > 0) {
                        this.logger.info("plpos printing " + paramlistidpos);
                        updatestatus.setString(paramlistidpos, paramlistidprop[i]);
                        logparams.append(";").append(paramlistidprop[i]);
                    }
                    if (paramlistversionidpos > 0) {
                        updatestatus.setString(paramlistversionidpos, paramlistversionidprop[i]);
                    }
                    if (variantidpos > 0) {
                        this.logger.info("var pos printing " + variantidpos);
                        updatestatus.setString(variantidpos, variantidprop[i]);
                        logparams.append(";").append(variantidprop[i]);
                    }
                    if (datasetpos > 0) {
                        this.logger.info("dataset printing " + datasetpos);
                        updatestatus.setInt(datasetpos, Integer.parseInt(datasetprop[i]));
                        logparams.append(";").append(datasetprop[i]);
                    }
                    try {
                        int rows = updatestatus.executeUpdate();
                        this.logger.info("Updated (" + String.valueOf(rows) + ") using " + logparams.toString());
                        continue;
                    }
                    catch (Exception e) {
                        throw new SapphireException("EXECUTE_STMT_FAILED", ErrorUtil.extractMessage("Error Updating the workitemitemcompleteflag. Exception: " + e.getMessage() + " executing " + sql, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                    }
                }
                this.database.closeStatement();
                SafeSQL safeSQL = new SafeSQL();
                sql = this.connectionInfo.isOracle() ? (complete.equals("N") ? "UPDATE sdiworkitem swi SET \tcompleteflag = 'N' WHERE \t( sdcid, keyid1, keyid2, keyid3 ) IN ( SELECT sdcid, keyid1, keyid2, keyid3 \tFROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) AND \tNvl (completeflag,'x') <> 'N' AND \tEXISTS (\t\tSELECT\tnull FROM sdiworkitemitem swii \t\tWHERE \t\t\tNvl (mandatoryflag,'Y') || Nvl (completeflag,'N') = 'YN' AND \t\t\tswii.sdcid = swi.sdcid AND \t\t\tswii.keyid1 = swi.keyid1 AND \t\t\tswii.keyid2 = swi.keyid2 AND \t\t\tswii.keyid3 = swi.keyid3 AND \t\t\tswii.workitemid = swi.workitemid )" : "UPDATE sdiworkitem swi SET \tcompleteflag = 'Y' WHERE \t( sdcid, keyid1, keyid2, keyid3 ) IN ( SELECT sdcid, keyid1, keyid2, keyid3 \tFROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) AND \tNvl (completeflag,'x') <> 'Y' AND \tNOT EXISTS (\t\tSELECT null FROM sdiworkitemitem swii \t\tWHERE \t\t\tNvl (mandatoryflag,'Y') || Nvl (completeflag,'N') = 'YN' AND \t\t\tswii.sdcid = swi.sdcid AND \t\t\tswii.keyid1 = swi.keyid1 AND \t\t\tswii.keyid2 = swi.keyid2 AND \t\t\tswii.keyid3 = swi.keyid3 AND \t\t\tswii.workitemid = swi.workitemid )") : (complete.equals("N") ? "UPDATE sdiworkitem SET \tcompleteflag = 'N' WHERE \t( \t\tsdcid IN ( SELECT sdcid FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) AND \t\tkeyid1 IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) AND \t\tkeyid2 IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) AND \t\tkeyid3 IN ( SELECT keyid3 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " ) \t) AND \tIsNull (completeflag,'x') <> 'N' AND \tEXISTS (\t\tSELECT\tnull FROM sdiworkitemitem swii \t\tWHERE \t\t\tIsNull (mandatoryflag,'Y') = 'Y' AND IsNull (completeflag,'N') = 'N' AND \t\t\tswii.sdcid = sdiworkitem.sdcid AND \t\t\tswii.keyid1 = sdiworkitem.keyid1 AND \t\t\tswii.keyid2 = sdiworkitem.keyid2 AND \t\t\tswii.keyid3 = sdiworkitem.keyid3 AND \t\t\tswii.workitemid = sdiworkitem.workitemid )" : "UPDATE sdiworkitem SET \tcompleteflag = 'Y' WHERE \t( \t\tsdcid IN ( SELECT sdcid FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") AND \t\tkeyid1 IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") AND \t\tkeyid2 IN ( SELECT keyid2 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") AND \t\tkeyid3 IN ( SELECT keyid3 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") \t) AND \tIsNull (completeflag,'x') <> 'Y' AND \tNOT EXISTS (\t\tSELECT null FROM sdiworkitemitem swii \t\tWHERE \t\t\tIsNull (mandatoryflag,'Y') = 'Y' AND \t\t\tIsNull (completeflag,'N') = 'N' AND \t\t\tswii.sdcid = sdiworkitem.sdcid AND \t\t\tswii.keyid1 = sdiworkitem.keyid1 AND \t\t\tswii.keyid2 = sdiworkitem.keyid2 AND \t\t\tswii.keyid3 = sdiworkitem.keyid3 AND \t\t\tswii.workitemid = sdiworkitem.workitemid )");
                try {
                    this.database.executePreparedUpdate(sql, safeSQL.getValues());
                }
                catch (SapphireException e) {
                    throw new SapphireException("EXECUTE_STMT_FAILED", ErrorUtil.extractMessage("Failed to delete datalimits. Reason: " + sql, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                }
            }
            catch (Exception e) {
                if (rsetid != null) {
                    dam.clearRSet(rsetid);
                }
                throw new SapphireException("EXECUTE_STMT_FAILED", "Error creating prepared statement. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        if (rsetid != null) {
            dam.clearRSet(rsetid);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.spec;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.util.StringHolder;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RemoveSDISpec
extends BaseAction
implements sapphire.action.RemoveSDISpec {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int rc = 1;
        String rsetid = null;
        String sdcid = properties.getProperty("sdcid");
        String keyid1list = properties.getProperty("keyid1");
        String keyid2list = properties.getProperty("keyid2");
        String keyid3list = properties.getProperty("keyid3");
        String specid = properties.getProperty("specid");
        String specversionid = properties.getProperty("specversionid");
        if (sdcid.length() > 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            StringHolder rsetidHolder = new StringHolder();
            DAMProcessor damProcessor = this.getDAMProcessor();
            rc = damProcessor.createRSet(sdcid, keyid1list, keyid2list, keyid3list, rsetidHolder);
            if (rc == 1 && applylock) {
                StringHolder datasetstatusHolder = new StringHolder();
                rc = damProcessor.lockRSet(datasetstatusHolder);
            }
            if (rc != 1) return;
            try {
                String[] keyid3array;
                rsetid = rsetidHolder.value;
                String reason = properties.getProperty("auditreason");
                String activity = properties.getProperty("auditactivity");
                String signedFlag = properties.getProperty("auditsignedflag");
                String auditdt = properties.getProperty("auditdt");
                String tracelogid = properties.getProperty("tracelogid", "");
                if (tracelogid.length() == 0 && reason.length() > 0) {
                    this.logger.info("Generate the tracelog record");
                    PropertyList tracelogprops = new PropertyList();
                    tracelogprops.setProperty("sdcid", properties.getProperty("sdcid"));
                    tracelogprops.setProperty("description", "Deleted specifications");
                    tracelogprops.setProperty("auditreason", reason);
                    tracelogprops.setProperty("auditactivity", activity);
                    tracelogprops.setProperty("auditsignedflag", signedFlag);
                    tracelogprops.setProperty("auditdt", auditdt);
                    try {
                        ActionProcessor actionProcessor = this.getActionProcessor();
                        actionProcessor.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                        tracelogid = tracelogprops.getProperty("tracelogid");
                        properties.setProperty("tracelogid", tracelogid);
                    }
                    catch (Exception e) {
                        throw new SapphireException("DB_ACTION_FAILED", "Error calling LoggerUtil.traceLog. Exception: ", e);
                    }
                }
                SafeSQL sdiClauseSafeSQL = new SafeSQL();
                String[] specidarray = StringUtil.split(specid, ";");
                String[] versionarray = StringUtil.split(specversionid, ";");
                String[] keyid1array = StringUtil.split(keyid1list, ";");
                String[] keyid2array = keyid2list.length() > 0 ? StringUtil.split(keyid2list, ";") : null;
                String[] stringArray = keyid3array = keyid3list.length() > 0 ? StringUtil.split(keyid3list, ";") : null;
                if (specidarray.length != versionarray.length || specidarray.length != keyid1array.length || keyid2array != null && keyid1array.length != keyid2array.length || keyid3array != null && keyid1array.length != keyid3array.length) return;
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                for (int i = 0; i < specidarray.length; ++i) {
                    sdiClauseSafeSQL.reset();
                    String sdiClause = "sdcid=" + sdiClauseSafeSQL.addVar(sdcid) + " and keyid1 =" + sdiClauseSafeSQL.addVar(keyid1array[i]) + " and keyid2=" + sdiClauseSafeSQL.addVar(keyid2array != null && keyid2array[i].length() > 0 ? keyid2array[i] : "(null)") + " and keyid3=" + sdiClauseSafeSQL.addVar(keyid3array != null && keyid3array[i].length() > 0 ? keyid3array[i] : "(null)");
                    sql.setLength(0);
                    safeSQL.reset();
                    sql.append("DELETE FROM sdispecrule WHERE specid=").append(safeSQL.addVar(specidarray[i])).append(" and specversionid=").append(safeSQL.addVar(versionarray[i])).append(" and ").append(sdiClause);
                    this.logger.info("Executing " + sql.toString());
                    this.database.executePreparedUpdate(sql.toString(), SafeSQL.joinArrays(safeSQL.getValues(), sdiClauseSafeSQL.getValues()));
                    sql.setLength(0);
                    safeSQL.reset();
                    sql.append("DELETE FROM sdidataitemspec WHERE specid=").append(safeSQL.addVar(specidarray[i])).append(" and specversionid=").append(safeSQL.addVar(versionarray[i])).append(" and ").append(sdiClause);
                    this.logger.info("Executing " + sql.toString());
                    this.database.executePreparedUpdate(sql.toString(), SafeSQL.joinArrays(safeSQL.getValues(), sdiClauseSafeSQL.getValues()));
                    sql.setLength(0);
                    safeSQL.reset();
                    sql.append("DELETE FROM sdispec WHERE specid=").append(safeSQL.addVar(specidarray[i])).append(" and specversionid=").append(safeSQL.addVar(versionarray[i])).append(" and ").append(sdiClause);
                    this.logger.info("Executing " + sql.toString());
                    if (this.database.executePreparedUpdate(sql.toString(), SafeSQL.joinArrays(safeSQL.getValues(), sdiClauseSafeSQL.getValues())) <= 0) continue;
                    sql.setLength(0);
                    safeSQL.reset();
                    sql.append("UPDATE a_sdispec SET modtool = 'RemoveSDISpec'");
                    sql.append(tracelogid.length() > 0 ? ", tracelogid = " + safeSQL.addVar(tracelogid) : "");
                    sql.append(" WHERE specid=").append(safeSQL.addVar(specidarray[i]));
                    sql.append(" AND specversionid=").append(safeSQL.addVar(versionarray[i]));
                    sql.append(" AND ").append(sdiClause);
                    Object[] values = SafeSQL.joinArrays(safeSQL.getValues(), sdiClauseSafeSQL.getValues());
                    safeSQL.reset();
                    sql.append(" AND auditopflag = 'D' AND auditsequence = ( SELECT max( auditsequence ) FROM a_sdispec WHERE specid=").append(safeSQL.addVar(specid)).append(" AND specversionid=").append(safeSQL.addVar(specversionid)).append(" AND ").append(sdiClause).append(" AND auditopflag = 'D' )");
                    values = SafeSQL.joinArrays(values, safeSQL.getValues());
                    this.database.executePreparedUpdate(sql.toString(), SafeSQL.joinArrays(values, sdiClauseSafeSQL.getValues()));
                }
                return;
            }
            catch (SapphireException se) {
                throw new SapphireException("DB_DELETE_FAILED", "Failed to delete spec " + specid + " version " + specversionid + " from " + keyid1list + ": " + ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
            }
            finally {
                if (rsetid != null) {
                    damProcessor.clearRSet(rsetid);
                }
            }
        }
        if (rc != true) return;
        throw new SapphireException("INVALID_PROPERTY", "You need to specify an sdcid.");
    }
}


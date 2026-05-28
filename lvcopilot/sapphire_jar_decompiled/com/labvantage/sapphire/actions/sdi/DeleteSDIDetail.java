/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.PropertyList;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.BaseSDILinkAction;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.StringHolder;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyListCollection;

public class DeleteSDIDetail
extends BaseSDILinkAction
implements sapphire.action.DeleteSDIDetail {
    public static final String REPLACENULL = "__null";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(sapphire.xml.PropertyList properties) throws SapphireException {
        int rc = 1;
        for (String currentKey : properties.keySet()) {
            String currentValue;
            if (currentKey == null || currentKey.length() <= 0 || (currentValue = properties.getProperty(currentKey, "")).length() <= 0) continue;
            if (currentValue.equalsIgnoreCase(REPLACENULL)) {
                properties.setProperty(currentKey, "(null)");
                continue;
            }
            if (currentValue.indexOf(REPLACENULL) <= -1) continue;
            currentValue = StringUtil.replaceAll(currentValue, REPLACENULL, "(null)", false);
            properties.setProperty(currentKey, currentValue);
        }
        SDCProcessor sdcProcessor = null;
        DAMProcessor dam = null;
        String rsetid = null;
        PropertyList props = new PropertyList(properties);
        String sdcid = properties.getProperty("sdcid");
        String linkid = properties.getProperty("linkid");
        String separator = properties.getProperty("separator", ";");
        if (sdcid.length() <= 0) throw new SapphireException("INVALID_PROPERTY", "You need to specify an sdcid.");
        try {
            sdcProcessor = this.getSDCProcessor();
        }
        catch (Exception e) {
            throw new SapphireException("COMPONENT_ACCESS_FAILURE", "Failed to access SDCManager component", e);
        }
        try {
            int i;
            int i2;
            int linktablekeys;
            String detaillinkid = properties.getProperty("detaillinkid", "");
            boolean detailLink = detaillinkid.length() > 0;
            String tableId = "";
            PropertyList sdcpl = null;
            PropertyList linkpl = null;
            HashMap sdcProps = sdcProcessor.getSDCProperties(sdcid);
            HashMap linkProps = sdcProcessor.getLinkProperties(sdcid, linkid);
            if (sdcProps == null || linkProps == null) throw new SapphireException("INVALID_PROPERTY", "The sdc properties could not be loaded");
            sdcpl = new PropertyList(sdcProps);
            int sdckeys = Integer.parseInt(sdcpl.getProperty("keycolumns"));
            linkpl = new PropertyList(linkProps);
            sapphire.xml.PropertyList detailLinkpl = null;
            if (detailLink) {
                detailLinkpl = new sapphire.xml.PropertyList(sdcProcessor.getDetailLinkProperties(sdcid, linkid + ";" + detaillinkid));
                tableId = detailLinkpl.getProperty("linktableid");
                linktablekeys = Integer.parseInt(detailLinkpl.getProperty("keycolcount"));
            } else {
                tableId = linkpl.getProperty("linktableid");
                linktablekeys = Integer.parseInt(linkpl.getProperty("keycolcount"));
            }
            String[] keycols = new String[linktablekeys];
            String[] keyvals = new String[linktablekeys];
            ArrayList<String> linktablekeysonly = new ArrayList<String>();
            for (int j = 1; j <= linktablekeys; ++j) {
                boolean notsdckeys = true;
                for (i2 = 1; i2 <= sdckeys; ++i2) {
                    String linkKeyColVal;
                    String sdcKeyColVal = sdcpl.getProperty("keycolid" + String.valueOf(i2));
                    String string = linkKeyColVal = detailLink ? detailLinkpl.getProperty("keycolid" + String.valueOf(j)) : linkpl.getProperty("keycolid" + String.valueOf(j));
                    if (!sdcKeyColVal.equals(linkKeyColVal)) continue;
                    keycols[i2 - 1] = sdcKeyColVal;
                    String keyid = properties.getProperty("keyid" + String.valueOf(i2));
                    keyvals[i2 - 1] = keyid.length() > 0 ? keyid : properties.getProperty(keycols[i2 - 1]);
                    notsdckeys = false;
                }
                if (!notsdckeys) continue;
                String linkKeyColVal = detailLink ? detailLinkpl.getProperty("keycolid" + String.valueOf(j)) : linkpl.getProperty("keycolid" + String.valueOf(j));
                linktablekeysonly.add(linkKeyColVal);
            }
            boolean applylock = properties.getProperty("applylock").equals("Y");
            dam = this.getDAMProcessor();
            StringHolder rsetidHolder = new StringHolder();
            rc = applylock ? dam.createLockedRSet(sdcid, StringUtil.replaceAll(keyvals[0], separator, ";"), keyvals.length > 1 ? StringUtil.replaceAll(keyvals[1], separator, ";") : "", keyvals.length > 2 ? StringUtil.replaceAll(keyvals[2], separator, ";") : "", rsetidHolder) : dam.createRSet(sdcid, StringUtil.replaceAll(keyvals[0], separator, ";"), keyvals.length > 1 ? StringUtil.replaceAll(keyvals[1], separator, ";") : "", keyvals.length > 2 ? StringUtil.replaceAll(keyvals[2], separator, ";") : "", rsetidHolder);
            if (rc != 1) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create rset");
            }
            rsetid = rsetidHolder.value;
            for (i2 = 0; i2 < linktablekeysonly.size(); ++i2) {
                for (int j = 0; j < properties.size(); ++j) {
                    if (!linktablekeysonly.get(i2).equals(props.propertyid[j])) continue;
                    keycols[sdckeys + i2] = props.propertyid[j];
                    keyvals[sdckeys + i2] = props.propertyvalue[j];
                }
            }
            boolean valuesCorrect = true;
            String[] temp = StringUtil.split(keyvals[sdckeys], separator);
            int values = temp.length;
            String[][] keyvalues = new String[linktablekeys][temp.length];
            keyvalues[linktablekeys - sdckeys] = temp;
            for (i = 0; i < sdckeys; ++i) {
                keyvalues[i] = StringUtil.split(keyvals[i], separator);
            }
            for (i = sdckeys; i < linktablekeys; ++i) {
                keyvalues[i] = StringUtil.split(keyvals[i], separator);
                if (keyvalues[i].length == values) continue;
                valuesCorrect = false;
            }
            if (!valuesCorrect) throw new SapphireException("INVALID_PROPERTY", "The number of values in the key columns is not consistent.");
            BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, null, "PreDeleteDetail");
            Trace.startBusinessRule(sdcid + "." + "PreDeleteDetail", true);
            sdcPreRules.preDeleteDetail(rsetid, properties);
            Trace.endBusinessRule(sdcid + "." + "PreDeleteDetail", true);
            Trace.startBusinessRule(sdcid + "." + "PreDeleteDetail", false);
            for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                customRules.preDeleteDetail(rsetid, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PreDeleteDetail", false);
            sdcPreRules.endRule();
            StringBuffer sql = new StringBuffer();
            sql.append("DELETE FROM ");
            sql.append(tableId);
            sql.append(" WHERE");
            StringBuffer whereClause = new StringBuffer();
            for (int i3 = 0; i3 < values; ++i3) {
                if (i3 > 0) {
                    whereClause.append(" OR ");
                }
                whereClause.append("(");
                for (int j = 0; j < linktablekeys; ++j) {
                    if (j > 0) {
                        whereClause.append(" AND ");
                    }
                    whereClause.append(" ");
                    whereClause.append(keycols[j]);
                    whereClause.append("='");
                    whereClause.append(i3 < keyvalues[j].length ? keyvalues[j][i3].replaceAll("'", "''") : keyvalues[j][keyvalues[j].length - 1].replaceAll("'", "''"));
                    whereClause.append("'");
                }
                whereClause.append(")");
            }
            sql.append(whereClause);
            if (values > 0) {
                if (!detailLink) {
                    PropertyListCollection detailLinkProps = sdcProcessor.getDetailLinks(sdcid);
                    this.deleteDetailDetails(linkid, detailLinkProps, whereClause.toString());
                }
                this.logger.info("Processing: " + sql);
                this.database.executeSQL(sql.toString());
                if (properties.getProperty("tracelogid", "").length() == 0 && properties.getProperty("auditreason", "").length() > 0) {
                    properties.setProperty("tracelogid", this.getTracelogid(properties));
                }
                if (properties.getProperty("tracelogid", "").trim().length() != 0 && !sdcpl.getProperty("auditedflag").equalsIgnoreCase("N")) {
                    this.logger.info("Generate the tracelog records");
                    StringBuffer updWhereClause = new StringBuffer();
                    updWhereClause.append("(").append(whereClause).append(")");
                    String updateSQL = "";
                    try {
                        updateSQL = "UPDATE a_" + tableId + " SET tracelogid = '" + properties.getProperty("tracelogid", "").trim() + "', modtool = '" + "DeleteSDIDetail" + "'  WHERE " + updWhereClause.toString() + " AND tracelogid = 'DELETED'";
                        this.logger.info("Updating Audit table: " + updateSQL);
                        this.database.executeSQL(updateSQL);
                    }
                    catch (Exception e) {
                        this.logger.info("DeleteSDIDetail", "Error Updating the audit record. Exception: " + e.getMessage() + " executing " + updateSQL);
                    }
                }
            }
            BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, null, "PostDeleteDetail");
            Trace.startBusinessRule(sdcid + "." + "PostDeleteDetail", true);
            sdcPostRules.postDeleteDetail(rsetid, properties);
            Trace.endBusinessRule(sdcid + "." + "PostDeleteDetail", true);
            Trace.startBusinessRule(sdcid + "." + "PostDeleteDetail", false);
            for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                customRules.postDeleteDetail(rsetid, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PostDeleteDetail", false);
            sdcPostRules.endRule();
            if (rsetid == null) return;
            dam.clearRSet(rsetid);
        }
        catch (Exception e) {
            try {
                throw new SapphireException("DB_DELETE_FAILED", "Failed to delete SDI detail records: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            catch (Throwable throwable) {
                if (rsetid == null) throw throwable;
                dam.clearRSet(rsetid);
                throw throwable;
            }
        }
        return;
    }

    private void deleteDetailDetails(String linkId, PropertyListCollection detailLinksProps, String whereClause) throws SapphireException {
        if (detailLinksProps != null && detailLinksProps.size() > 0) {
            for (int i = 0; i < detailLinksProps.size(); ++i) {
                sapphire.xml.PropertyList detailLinkProp = (sapphire.xml.PropertyList)detailLinksProps.get(i);
                String linkType = detailLinkProp.getProperty("linktype");
                String parentLinkId = detailLinkProp.getProperty("linkid");
                if (!linkId.equalsIgnoreCase(parentLinkId) || !"D".equals(linkType)) continue;
                String detailLinkTableId = detailLinkProp.getProperty("linktableid");
                String sql = "DELETE FROM " + detailLinkTableId + " WHERE " + whereClause;
                this.database.executeSQL(sql.toString());
            }
        }
    }

    private String getTracelogid(sapphire.xml.PropertyList properties) throws SapphireException {
        String traceLogId = properties.getProperty("tracelogid", "");
        String auditReason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag");
        String sdcid = properties.getProperty("sdcid");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N") && traceLogId.length() == 0 && auditReason.length() > 0) {
            this.logger.info("Generate the tracelog records");
            String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
            String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                traceLogId = audit.addTraceLogEntry(auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data deleted", standard.equals("Y"));
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        return traceLogId;
    }
}


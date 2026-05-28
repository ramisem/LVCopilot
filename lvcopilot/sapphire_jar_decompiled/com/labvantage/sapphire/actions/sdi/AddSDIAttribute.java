/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class AddSDIAttribute
extends BaseSDIAttributeAction
implements sapphire.action.AddSDIAttribute {
    private String delim = ";";
    private boolean applylock = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        BaseSDIAttributeAction.AttributeType at;
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String sdcid = properties.getProperty("sdcid");
        String attributeid = properties.getProperty("attributeid");
        String attributesdcid = properties.getProperty("attributesdcid");
        String value = properties.getProperty("value");
        String type = properties.getProperty("type");
        String datatype = properties.getProperty("datatype");
        String mandatoryflag = properties.getProperty("mandatory");
        String updatableflag = properties.getProperty("updatable");
        String hiddenflag = properties.getProperty("hidden");
        String editorstyleid = properties.getProperty("editorstyle");
        String editsdcid = properties.getProperty("editsdcid");
        String editreftypeid = properties.getProperty("editreftypeid");
        String instructionflag = properties.getProperty("instructionflag");
        String insructiontext = properties.getProperty("instructiontext");
        String copydowncontext = properties.getProperty("copydowncontext");
        String attributetypeflag = properties.getProperty("attributetypeflag");
        String worksheetcontext = properties.getProperty("worksheetcontext");
        this.applylock = properties.getProperty("applylock").equals("Y");
        String traceLogId = properties.getProperty("tracelogid");
        String auditReason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag");
        if (sdcid.length() <= 0) throw new SapphireException("No sdcid provided.");
        if (sdcid.contains(this.delim)) {
            throw new SapphireException("Only one SDC can be maintained at one time.");
        }
        PropertyList primary = this.getSDCProcessor().getPropertyList(sdcid);
        if (primary == null) throw new SapphireException("Invalid SDC provided.");
        if (keyid1.length() <= 0) throw new SapphireException("No keyid1 provided.");
        if (properties.getProperty("keycolid2").length() > 0 && keyid2.length() == 0) {
            throw new SapphireException("No keyid2 provided.");
        }
        if (properties.getProperty("keycolid3").length() > 0 && keyid3.length() == 0) {
            throw new SapphireException("No keyid3 provided.");
        }
        if (type.equalsIgnoreCase("s") || type.equalsIgnoreCase("sdc") || type.equalsIgnoreCase("primary")) {
            throw new SapphireException("SDC/Primary attributes are created automatically by Add SDI.");
        }
        if (type.equalsIgnoreCase("t") || type.equalsIgnoreCase("temp") || type.equalsIgnoreCase("template")) {
            throw new SapphireException("Template attributes are created automatically by Add SDI.");
        }
        if (type.equalsIgnoreCase("l") || type.equalsIgnoreCase("linkdef") || type.equalsIgnoreCase("link") || type.equalsIgnoreCase("fk") || type.equalsIgnoreCase("foreign key")) {
            if (attributesdcid.length() <= 0) throw new SapphireException("For linked attributed you need to pass an attribute sdc.");
            at = BaseSDIAttributeAction.AttributeType.linkdef;
        } else {
            if (type.equalsIgnoreCase("t") || type.equalsIgnoreCase("temp") || type.equalsIgnoreCase("template")) {
                throw new SapphireException("Template attributes are created from add SDI.");
            }
            if (type.length() == 0 || type.equalsIgnoreCase("a") || type.equalsIgnoreCase("adhoc")) {
                at = BaseSDIAttributeAction.AttributeType.adhoc;
            } else {
                if (!type.contains(this.delim)) throw new SapphireException("Invalid type provided.");
                throw new SapphireException("Only one attribute type can be maintained at a time.");
            }
        }
        if (attributeid.length() <= 0) throw new SapphireException("No attribute id provided.");
        String rsetid = this.applylock ? this.getDAMProcessor().createLockedRSet(sdcid, keyid1, keyid2, keyid3) : this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
        if (rsetid.length() == 0) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for sdiattibute maintenance");
        }
        try {
            this.logger.debug("rset = " + rsetid);
            DataSet toadd = new DataSet(this.connectionInfo);
            HashMap<String, ArrayList<String>> skipped = new HashMap<String, ArrayList<String>>();
            DataSet existing = AddSDIAttribute.getExistingAttributes(rsetid, this.getQueryProcessor(), this.logger);
            AddSDIAttribute.createAttributeData(toadd, existing, primary, keyid1, keyid2, keyid3, attributeid, attributesdcid, datatype, at, value, mandatoryflag, updatableflag, hiddenflag, editorstyleid, editsdcid, editreftypeid, instructionflag, insructiontext, copydowncontext, attributetypeflag, worksheetcontext, properties.getProperty("usersequence", ""), skipped, false, rsetid, this.delim, this.getSDCProcessor(), this.getQueryProcessor(), new M18NUtil(this.connectionInfo), this.connectionInfo, this.logger);
            String user = this.connectionInfo.getSysuserId();
            Calendar now = DateTimeUtil.getNowCalendar();
            toadd.setString(-1, "createby", user);
            toadd.setString(-1, "createtool", "AddSDIAttribute");
            toadd.setDate(-1, "createdt", now);
            toadd.setString(-1, "modby", user);
            toadd.setString(-1, "modtool", "AddSDIAttribute");
            toadd.setDate(-1, "moddt", now);
            if (traceLogId.length() == 0 && auditReason.length() > 0) {
                this.logger.debug("Generate the tracelog records for adding sdiattributes");
                if (auditReason.length() > 0) {
                    AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    try {
                        traceLogId = audit.addTraceLogEntry(auditReason, auditActivity, auditSignedFlag, "", "", true);
                        toadd.setString(-1, "tracelogid", traceLogId);
                    }
                    catch (ServiceException e) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Operation AddSDIAttribute: Failed to add audit records"), e);
                    }
                }
            }
            if (Logger.isDebugEnabled()) {
                this.logger.debug("Save set built:");
                toadd.showData();
            }
            if (at != BaseSDIAttributeAction.AttributeType.linkdef && AddSDIAttribute.isRequiredComplete(primary, toadd, existing, rsetid, this.getQueryProcessor(), this.logger)) {
                this.logger.debug("AddSDIAttribute - REQUIRED COMPLETE");
                properties.setProperty("requiredcomplete", "Y");
            } else {
                properties.setProperty("requiredcomplete", "N");
            }
            SDIData sdiData = new SDIData();
            sdiData.setDataset("attribute", toadd);
            sdiData.setRsetid(rsetid);
            BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, primary, "PreAddAttribute");
            Trace.startBusinessRule(sdcid + "." + "PreAddAttribute", true);
            sdcPreRules.preAddAttribute(sdiData, properties);
            Trace.endBusinessRule(sdcid + "." + "PreAddAttribute", true);
            Trace.startBusinessRule(sdcid + "." + "PreAddAttribute", false);
            for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                customRules.preAddAttribute(sdiData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PreAddAttribute", false);
            sdcPreRules.endRule();
            if (toadd.size() > 0) {
                DataSetUtil.insert(this.database, toadd, "sdiattribute");
            }
            StringBuffer sb = new StringBuffer();
            if (skipped.containsKey("duplicates")) {
                for (String skip : skipped.get("duplicates")) {
                    if (sb.length() > 0) {
                        sb.append(this.delim);
                    }
                    sb.append(skip);
                }
            }
            properties.setProperty("skippedduplicateattributes", sb.toString());
            sb = new StringBuffer();
            if (skipped.containsKey("masterlist")) {
                for (String skip : skipped.get("masterlist")) {
                    if (sb.length() > 0) {
                        sb.append(this.delim);
                    }
                    sb.append(skip);
                }
            }
            properties.setProperty("skippedattributes", sb.toString());
            BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, primary, "PostAddAttribute");
            Trace.startBusinessRule(sdcid + "." + "PostAddAttribute", true);
            sdcPostRules.postAddAttribute(sdiData, properties);
            Trace.endBusinessRule(sdcid + "." + "PostAddAttribute", true);
            Trace.startBusinessRule(sdcid + "." + "PostAddAttribute", false);
            for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                customRules.postAddAttribute(sdiData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PostAddAttribute", false);
            sdcPostRules.endRule();
            if (toadd.size() <= 0 || !properties.getProperty("index", "Y").equals("Y")) return;
            Indexer.indexPrimaryAndAttributes(this.connectionInfo, sdcid, toadd.getColumnValues("keyid1", ";"), toadd.getColumnValues("keyid2", ";"), toadd.getColumnValues("keyid3", ";"));
            return;
        }
        finally {
            if (rsetid != null && rsetid.length() > 0) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }
}


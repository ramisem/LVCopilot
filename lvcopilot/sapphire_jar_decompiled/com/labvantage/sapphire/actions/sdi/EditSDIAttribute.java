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
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class EditSDIAttribute
extends BaseSDIAttributeAction
implements sapphire.action.EditSDIAttribute {
    private String delim = ";";
    private boolean applylock = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String sdcid = properties.getProperty("sdcid");
        String attributeid = properties.getProperty("attributeid");
        String attributesdcid = properties.getProperty("attributesdcid");
        String attributeinstance = properties.getProperty("attributeinstance");
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
        if (attributeid.length() <= 0) throw new SapphireException("No attribute id provided.");
        if (attributeinstance.length() <= 0) throw new SapphireException("No attribute instance provided.");
        if (attributesdcid.length() <= 0) throw new SapphireException("No attribute sdcid provided.");
        String rsetid = this.applylock ? this.getDAMProcessor().createLockedRSet(sdcid, keyid1, keyid2, keyid3) : this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3, false, 1);
        if (rsetid.length() == 0) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for sdiattibute maintenance");
        }
        try {
            BaseSDCRules sdcPostRules;
            this.logger.debug("rset = " + rsetid);
            DataSet existingattributes = EditSDIAttribute.getExistingAttributes(rsetid, this.getQueryProcessor(), this.logger);
            DataSet toupdate = EditSDIAttribute.updateAttributeData(properties, existingattributes, sdcid, this.getSDCProcessor(), keyid1, keyid2, keyid3, attributeid, attributesdcid, attributeinstance, "", this.delim, this.getQueryProcessor(), new M18NUtil(this.connectionInfo), this.connectionInfo, this.logger);
            toupdate.setString(-1, "modby", this.connectionInfo.getSysuserId());
            toupdate.setString(-1, "modtool", "EditSDIAttribute");
            toupdate.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
            if (traceLogId.length() == 0 && auditReason.length() > 0) {
                this.logger.debug("Generate the tracelog records for editing sdiattributes");
                if (auditReason.length() > 0) {
                    AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    try {
                        traceLogId = audit.addTraceLogEntry(auditReason, auditActivity, auditSignedFlag, "", "", true);
                        toupdate.setString(-1, "tracelogid", traceLogId);
                    }
                    catch (ServiceException e) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Operation EditSDIAttribute: Failed to add audit records"), e);
                    }
                }
            }
            if (Logger.isDebugEnabled()) {
                this.logger.debug("Save set built:");
                toupdate.showData();
            }
            if (sdcid.equalsIgnoreCase(attributesdcid) && EditSDIAttribute.isRequiredComplete(primary, toupdate, existingattributes, rsetid, this.getQueryProcessor(), this.logger)) {
                this.logger.debug("EditSDIAttribute - REQUIRED COMPLETE");
                properties.setProperty("requiredcomplete", "Y");
            } else {
                properties.setProperty("requiredcomplete", "N");
            }
            BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, primary, "PreEditAttribute");
            SDIData beforeEditImage = new SDIData();
            if (sdcPreRules.requiresBeforeEditSDIAttributeImage() || sdcPreRules.customRulesRequiresBeforeEditSDIAttributeImage()) {
                beforeEditImage.setRsetid(rsetid);
                beforeEditImage.setDataset("attribute", existingattributes);
            }
            sdcPreRules.setBeforeEditImage(beforeEditImage);
            SDIData editImage = new SDIData();
            editImage.setRsetid(rsetid);
            editImage.setDataset("attribute", toupdate);
            Trace.startBusinessRule(sdcid + "." + "PreEditAttribute", true);
            sdcPreRules.preEditAttribute(editImage, properties);
            Trace.endBusinessRule(sdcid + "." + "PreEditAttribute", true);
            Trace.startBusinessRule(sdcid + "." + "PreEditAttribute", false);
            for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                customRules.preEditAttribute(editImage, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PreEditAttribute", false);
            sdcPreRules.endRule();
            if (toupdate.size() > 0) {
                String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "attributeid", "attributeinstance", "attributesdcid"};
                DataSetUtil.update(this.database, toupdate, "sdiattribute", keycols);
            }
            if ((sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, primary, "PostEditAttribute")).requiresBeforeEditSDIAttributeImage() || sdcPostRules.customRulesRequiresBeforeEditSDIAttributeImage()) {
                sdcPostRules.setBeforeEditImage(beforeEditImage);
            }
            Trace.startBusinessRule(sdcid + "." + "PostEditAttribute", true);
            sdcPostRules.postEditAttribute(editImage, properties);
            Trace.endBusinessRule(sdcid + "." + "PostEditAttribute", true);
            Trace.startBusinessRule(sdcid + "." + "PostEditAttribute", false);
            for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                customRules.postEditAttribute(editImage, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PostEditAttribute", false);
            sdcPostRules.endRule();
            if (toupdate.size() <= 0 || !properties.getProperty("index", "Y").equals("Y")) return;
            Indexer.indexPrimaryAndAttributes(this.connectionInfo, sdcid, toupdate.getColumnValues("keyid1", ";"), toupdate.getColumnValues("keyid2", ";"), toupdate.getColumnValues("keyid3", ";"));
            return;
        }
        finally {
            if (rsetid != null && rsetid.length() > 0) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }
}


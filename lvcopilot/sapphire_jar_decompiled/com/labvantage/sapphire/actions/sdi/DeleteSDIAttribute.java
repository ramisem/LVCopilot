/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDIAttribute
extends BaseSDIAttributeAction
implements sapphire.action.DeleteSDIAttribute {
    private String delim = ";";

    private int find(String[] array, String value) {
        for (int i = 0; i < array.length; ++i) {
            if (!array[i].equals(value)) continue;
            return i;
        }
        return -1;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String[] attributesdcids;
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String sdcid = properties.getProperty("sdcid");
        String attributeid = properties.getProperty("attributeid");
        String attributesdcid = properties.getProperty("attributesdcid", sdcid);
        String attributeinstance = properties.getProperty("attributeinstance");
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
        if (keyid1.contains(this.delim) || keyid2.contains(this.delim) || keyid3.contains(this.delim)) {
            throw new SapphireException("Only one sdi can be maintained at one time.");
        }
        if (attributeid.length() <= 0) throw new SapphireException("No attribute id provided.");
        if (attributeinstance.length() <= 0) throw new SapphireException("No attribute instance provided.");
        if (attributesdcid.length() <= 0) throw new SapphireException("No attribute sdcid provided.");
        String[] attributeids = StringUtil.split(attributeid, this.delim);
        String[] attributeinstances = StringUtil.split(attributeinstance, this.delim);
        String[] t_attributesdcids = StringUtil.split(attributesdcid, this.delim);
        if (t_attributesdcids.length != attributeids.length) {
            attributesdcids = new String[attributeids.length];
            for (int i = 0; i < attributeids.length; ++i) {
                attributesdcids[i] = t_attributesdcids[0];
            }
        } else {
            attributesdcids = t_attributesdcids;
        }
        if (attributeids.length != attributeinstances.length || attributeids.length != attributesdcids.length) throw new SapphireException("Provided attribute ids do not match instances and/or attribute sdcids.");
        int[] numberinstances = new int[attributeinstances.length];
        for (int i = 0; i < attributeinstances.length; ++i) {
            try {
                numberinstances[i] = Integer.parseInt(attributeinstances[i]);
                continue;
            }
            catch (NumberFormatException e) {
                throw new SapphireException("Invalid instance id " + attributeinstances[i] + " provided.");
            }
        }
        String rsetid = "";
        boolean applylock = properties.getProperty("applylock").equals("Y");
        rsetid = applylock ? this.getDAMProcessor().createLockedRSet(sdcid, keyid1, keyid2, keyid3) : this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
        if (rsetid.length() == 0) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for delete");
        }
        DataSet predelete = DeleteSDIAttribute.getExistingAttributes(rsetid, this.getQueryProcessor(), this.logger);
        for (int i = 0; i < predelete.getRowCount(); ++i) {
            BaseSDCRules[] currentatt = predelete.getValue(i, "attributeid", "");
            int find = this.find(attributeids, (String)currentatt);
            if (find <= -1) continue;
            String currentattsdcid = predelete.getValue(i, "attributesdcid", "");
            String currentattinstance = predelete.getValue(i, "attributeinstance", "");
            if (!attributeinstances[find].equals(currentattinstance) || !attributesdcids[find].equals(currentattsdcid)) continue;
            predelete.deleteRow(i);
            --i;
        }
        if (DeleteSDIAttribute.isRequiredComplete(primary, null, predelete, rsetid, this.getQueryProcessor(), this.logger)) {
            this.logger.debug("DeleteSDIAttribute - REQUIRED COMPLETE");
            properties.setProperty("requiredcomplete", "Y");
        } else {
            properties.setProperty("requiredcomplete", "N");
        }
        BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, primary, "PreDeleteAttribute");
        Trace.startBusinessRule(sdcid + "." + "PreDeleteAttribute", true);
        sdcPreRules.preDeleteAttribute(rsetid, properties);
        Trace.endBusinessRule(sdcid + "." + "PreDeleteAttribute", true);
        Trace.startBusinessRule(sdcid + "." + "PreDeleteAttribute", false);
        for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
            customRules.preDeleteAttribute(rsetid, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PreDeleteAttribute", false);
        sdcPreRules.endRule();
        BaseSDIAttributeAction.removeAttributeData(primary, rsetid, keyid1, keyid2, keyid3, attributeids, attributesdcids, numberinstances, this.database, this.logger);
        BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, primary, "PostDeleteAttribute");
        Trace.startBusinessRule(sdcid + "." + "PostDeleteAttribute", true);
        sdcPostRules.postDeleteAttribute(rsetid, properties);
        Trace.endBusinessRule(sdcid + "." + "PostDeleteAttribute", true);
        Trace.startBusinessRule(sdcid + "." + "PostDeleteAttribute", false);
        for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
            customRules.postDeleteAttribute(rsetid, properties);
        }
        Trace.endBusinessRule(sdcid + "." + "PostDeleteAttribute", false);
        sdcPostRules.endRule();
        if (properties.getProperty("index", "Y").equals("Y")) {
            Indexer.indexPrimaryAndAttributes(this.connectionInfo, sdcid, keyid1, keyid2, keyid3);
        }
        if (rsetid.length() <= 0) return;
        this.getDAMProcessor().clearRSet(rsetid);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetSDIAttribute
extends BaseSDIAttributeAction
implements sapphire.action.GetSDIAttribute {
    private String delim = ";";

    private int find(String[] array, String value) {
        for (int i = 0; i < array.length; ++i) {
            if (!array[i].equals(value)) continue;
            return i;
        }
        return -1;
    }

    private void addToBuffers(StringBuffer sbattributeid, StringBuffer sbattributesdcid, StringBuffer sbattributeinstance, StringBuffer sbattributetype, StringBuffer sbvalue, StringBuffer sbsourcesdcid, StringBuffer sbdatatype, StringBuffer sbeditorstyle, StringBuffer sbeditorsdcid, StringBuffer sbeditorreftype, StringBuffer sbupdateable, StringBuffer sbhidden, StringBuffer sbmandatory, String currentattributeid, String currentattributesdcid, String currentattributeinstance, BaseSDIAttributeAction.AttributeType currentattributetype, DataSet existingattributes, int row, String sdcid, M18NUtil m18n) {
        if (sbattributeid.length() > 0) {
            sbattributeid.append(this.delim);
            sbattributesdcid.append(this.delim);
            sbattributeinstance.append(this.delim);
            sbattributetype.append(this.delim);
            sbvalue.append(this.delim);
            sbsourcesdcid.append(this.delim);
            sbdatatype.append(this.delim);
            sbeditorstyle.append(this.delim);
            sbeditorsdcid.append(this.delim);
            sbeditorreftype.append(this.delim);
            sbupdateable.append(this.delim);
            sbhidden.append(this.delim);
            sbmandatory.append(this.delim);
        }
        sbattributeid.append(currentattributeid);
        sbattributesdcid.append(currentattributesdcid);
        sbattributeinstance.append(currentattributeinstance);
        sbattributetype.append(GetSDIAttribute.getAttributeType(currentattributetype));
        sbsourcesdcid.append(existingattributes.getValue(row, "sourcesdcid", sdcid));
        sbvalue.append(StringUtil.replaceAll(GetSDIAttribute.getAttributeValue(existingattributes, row, m18n), "#semicolon#", ";"));
        sbdatatype.append(existingattributes.getValue(row, "datatype", "S"));
        if (currentattributetype == BaseSDIAttributeAction.AttributeType.linkdef) {
            sbeditorstyle.append(existingattributes.getValue(row, "editorstyleid", ""));
            sbeditorsdcid.append(existingattributes.getValue(row, "editsdcid", ""));
            sbeditorreftype.append(existingattributes.getValue(row, "editreftypeid", ""));
            String s = existingattributes.getValue(row, "updateableflag", "Y");
            sbupdateable.append(s.equalsIgnoreCase("N") ? "N" : "Y");
            s = existingattributes.getValue(row, "hiddenflag", "N");
            sbhidden.append(s.equalsIgnoreCase("Y") ? "Y" : "N");
            s = existingattributes.getValue(row, "mandatoryflag", "N");
            sbmandatory.append(s.equalsIgnoreCase("Y") ? "Y" : "N");
        }
    }

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
        String attributetype = properties.getProperty("type");
        this.delim = properties.getProperty("separator", this.delim);
        if (sdcid.length() <= 0) throw new SapphireException("No sdcid provided.");
        if (sdcid.contains(this.delim)) {
            throw new SapphireException("Only one SDC can be obtained at one time.");
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
            throw new SapphireException("Only one sdi can be obtained at one time.");
        }
        String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
        if (rsetid.length() == 0) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for sdiattibute maintenance");
        }
        try {
            String[] attributetypes;
            String[] attributesdcids;
            String[] attributeinstances;
            String[] attributeids;
            this.logger.debug("rset = " + rsetid);
            DataSet existingattributes = GetSDIAttribute.getExistingAttributes(rsetid, this.getQueryProcessor(), this.logger);
            if (attributeid.length() > 0) {
                this.logger.debug("Attribute ids provided for filtering.");
                attributeids = StringUtil.split(attributeid, this.delim);
                if (attributeinstance.length() > 0) {
                    attributeinstances = StringUtil.split(attributeinstance, this.delim);
                    if (attributeinstances.length != attributeids.length) {
                        this.logger.info("Attribute intances do not match attribute id's and therefore cannot be used.");
                        attributeinstances = null;
                    }
                } else {
                    this.logger.debug("No attribute intances provided for filtering.");
                    attributeinstances = null;
                }
                if (attributesdcid.length() > 0) {
                    attributesdcids = StringUtil.split(attributesdcid, this.delim);
                    if (attributesdcids.length != attributesdcids.length) {
                        this.logger.info("Attribute sdcids do not match attribute id's and therefore cannot be used.");
                        attributesdcids = null;
                    }
                } else {
                    this.logger.debug("No attribute sdcids provided for filtering.");
                    attributesdcids = null;
                }
            } else {
                this.logger.debug("No attribute ids provided for filtering.");
                attributeinstances = null;
                attributesdcids = null;
                attributeids = null;
            }
            if (attributetype.length() > 0) {
                attributetypes = StringUtil.split(attributetype, this.delim);
                if (attributeids == null || attributetypes.length != attributeids.length && attributetypes.length > 1) {
                    this.logger.info("Attribute types do not match attribute id's therefore use first type.");
                    attributetypes = new String[]{attributetypes[0]};
                }
            } else {
                this.logger.debug("No attribute types provided for filtering.");
                attributetypes = null;
            }
            StringBuffer sbattributeid = new StringBuffer();
            StringBuffer sbattributesdcid = new StringBuffer();
            StringBuffer sbattributeinstance = new StringBuffer();
            StringBuffer sbattributetype = new StringBuffer();
            StringBuffer sbvalue = new StringBuffer();
            StringBuffer sbdatatype = new StringBuffer();
            StringBuffer sbeditorstyle = new StringBuffer();
            StringBuffer sbeditorsdcid = new StringBuffer();
            StringBuffer sbeditorreftype = new StringBuffer();
            StringBuffer sbupdateable = new StringBuffer();
            StringBuffer sbmandatory = new StringBuffer();
            StringBuffer sbhidden = new StringBuffer();
            StringBuffer sbsourcesdcid = new StringBuffer();
            M18NUtil m18n = new M18NUtil(this.connectionInfo);
            for (int i = 0; i < existingattributes.getRowCount(); ++i) {
                String currentattributeid = existingattributes.getValue(i, "attributeid", "");
                String currentattributesdcid = existingattributes.getValue(i, "attributesdcid", "");
                String currentattributeinstance = existingattributes.getValue(i, "attributeinstance", "");
                BaseSDIAttributeAction.AttributeType currentattributetype = GetSDIAttribute.getAttributeTypeFromString(existingattributes.getValue(i, "attributesourcetype", ""));
                if (attributeids != null) {
                    int k = this.find(attributeids, currentattributeid);
                    if (k <= -1 || attributesdcids != null && !attributesdcids[k].equals(currentattributesdcid) || attributeinstances != null && !attributeinstances[k].equals(currentattributeinstance) || attributetypes != null && (attributetypes.length != 1 || GetSDIAttribute.getAttributeTypeFromString(attributetypes[0]) != currentattributetype) && GetSDIAttribute.getAttributeTypeFromString(attributetypes[k]) != currentattributetype) continue;
                    this.addToBuffers(sbattributeid, sbattributesdcid, sbattributeinstance, sbattributetype, sbvalue, sbsourcesdcid, sbdatatype, sbeditorstyle, sbeditorsdcid, sbeditorreftype, sbupdateable, sbhidden, sbmandatory, currentattributeid, currentattributesdcid, currentattributeinstance, currentattributetype, existingattributes, i, sdcid, m18n);
                    continue;
                }
                if (attributetypes != null) {
                    if (currentattributetype != GetSDIAttribute.getAttributeTypeFromString(attributetypes[0])) continue;
                    this.addToBuffers(sbattributeid, sbattributesdcid, sbattributeinstance, sbattributetype, sbvalue, sbsourcesdcid, sbdatatype, sbeditorstyle, sbeditorsdcid, sbeditorreftype, sbupdateable, sbhidden, sbmandatory, currentattributeid, currentattributesdcid, currentattributeinstance, currentattributetype, existingattributes, i, sdcid, m18n);
                    continue;
                }
                this.addToBuffers(sbattributeid, sbattributesdcid, sbattributeinstance, sbattributetype, sbvalue, sbsourcesdcid, sbdatatype, sbeditorstyle, sbeditorsdcid, sbeditorreftype, sbupdateable, sbhidden, sbmandatory, currentattributeid, currentattributesdcid, currentattributeinstance, currentattributetype, existingattributes, i, sdcid, m18n);
            }
            properties.setProperty("attributeid", sbattributeid.toString());
            properties.setProperty("attributesdcid", sbattributesdcid.toString());
            properties.setProperty("attributeinstance", sbattributeinstance.toString());
            properties.setProperty("sourcesdcid", sbsourcesdcid.toString());
            properties.setProperty("type", sbattributetype.toString());
            properties.setProperty("datatype", sbdatatype.toString());
            properties.setProperty("value", sbvalue.toString());
            properties.setProperty("editorstyle", sbeditorstyle.toString());
            properties.setProperty("editsdcid", sbeditorsdcid.toString());
            properties.setProperty("editreftypeid", sbeditorreftype.toString());
            properties.setProperty("updatable", sbupdateable.toString());
            properties.setProperty("mandatory", sbmandatory.toString());
            properties.setProperty("hidden", sbhidden.toString());
            return;
        }
        finally {
            if (rsetid != null && rsetid.length() > 0) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }
}


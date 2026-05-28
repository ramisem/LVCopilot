/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_AttributeDef
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        HashMap<String, Integer> userSequence = new HashMap<String, Integer>();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String basedonid = primary.getValue(i, "basedonid");
            if (userSequence.containsKey(basedonid)) {
                Integer useeSeq = (Integer)userSequence.get(basedonid);
                primary.setNumber(i, "usersequence", useeSeq);
                userSequence.put(basedonid, useeSeq + 1);
                continue;
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select max(usersequence) maxsequence from attributedef where basedonid=?", (Object[])new String[]{basedonid});
            Integer maxSequence = ds.getInt(0, "maxsequence");
            maxSequence = maxSequence + 1;
            userSequence.put(basedonid, maxSequence);
            primary.setNumber(i, "usersequence", maxSequence);
        }
        if (actionProps.containsKey("default")) {
            this.setDefaultValue(actionProps, primary);
        }
        this.setUpInstructions(primary);
        this.clearCache(primary, null);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (actionProps.containsKey("default")) {
            this.setDefaultValue(actionProps, primary);
        }
        this.setUpInstructions(primary);
        DataSet beforeEditImage = this.getBeforeEditImage().getDataset("primary");
        this.clearCache(primary, beforeEditImage);
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String sql = "SELECT at.attributedefid,at.basedonid FROM attributedef at,rsetitems ri  WHERE at.attributedefid = ri.keyid1 AND at.basedonid = ri.keyid2 AND ri.rsetid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
        this.checkExistingAttributes(ds);
        this.clearCache(ds, null);
    }

    private void checkExistingAttributes(DataSet attributes) throws SapphireException {
        attributes.sort("basedonid");
        ArrayList<DataSet> groups = attributes.getGroupedDataSets("basedonid");
        for (int g = 0; g < groups.size(); ++g) {
            String[] attributedefs;
            DataSet dsAttributes = groups.get(g);
            String basedonId = dsAttributes.getValue(0, "basedonid");
            DataSet inuse = BaseSDIAttributeAction.getExistingAttributes(basedonId, attributedefs = StringUtil.split(dsAttributes.getColumnValues("attributedefid", ";"), ";"), this.getQueryProcessor(), this.logger);
            if (inuse.getRowCount() <= 0) continue;
            this.logger.warn("Found " + inuse.getRowCount() + " attributes in use.");
            StringBuffer msg = new StringBuffer(this.getTranslationProcessor().translate("Could not delete attributes from master list because the following attributes are in use"));
            msg.append(":");
            for (int i = 0; i < inuse.getRowCount(); ++i) {
                msg.append("\n");
                if (i > 10) {
                    msg.append("").append(inuse.getRowCount() - 10).append("more found...");
                    break;
                }
                if (inuse.getValue(i, "attributesourcetype", "").equalsIgnoreCase("linkdef")) {
                    msg.append("Linked attribute");
                } else {
                    msg.append("Attribute");
                }
                msg.append(" ").append(inuse.getValue(i, "attributeid", "")).append(" on ");
                msg.append(inuse.getValue(i, "sdcid")).append(" ").append(inuse.getValue(i, "keyid1", ""));
                String k2 = inuse.getValue(i, "keyid2", "(null)");
                if (k2.equalsIgnoreCase("(null)")) continue;
                msg.append(" (").append(k2);
                String k3 = inuse.getValue(i, "keyid3", "(null)");
                if (!k3.equalsIgnoreCase("(null)")) {
                    msg.append(" ").append(k3);
                }
                msg.append(")");
            }
            throw new SapphireException(msg.toString());
        }
    }

    private void setDefaultValue(PropertyList properties, DataSet primary) {
        String[] defaults;
        String[] datatypes = StringUtil.split(properties.getProperty("datatype", "S"), ";");
        if (datatypes.length == (defaults = StringUtil.split(properties.getProperty("default", ""), ";")).length && datatypes.length == primary.getRowCount()) {
            StringBuffer defaulttexts = new StringBuffer();
            StringBuffer defaultnumerics = new StringBuffer();
            StringBuffer defaultclobs = new StringBuffer();
            StringBuffer defaultdates = new StringBuffer();
            M18NUtil usersM18N = new M18NUtil(this.connectionInfo);
            for (int i = 0; i < datatypes.length; ++i) {
                if (i > 0) {
                    defaulttexts.append(";");
                    defaultnumerics.append(";");
                    defaultclobs.append(";");
                    defaultdates.append(";");
                }
                String datatype = datatypes[i];
                String currentdefault = defaults[i];
                if (datatype.equalsIgnoreCase("C")) {
                    defaultclobs.append("(null)".equalsIgnoreCase(currentdefault) ? "" : currentdefault);
                    defaultdates.append("(null)");
                    defaulttexts.append("");
                    defaultnumerics.append("(null)");
                    continue;
                }
                if (datatype.equalsIgnoreCase("D") || datatype.equalsIgnoreCase("O")) {
                    if (currentdefault.length() == 0 || currentdefault.equalsIgnoreCase("(null)")) {
                        defaultdates.append("(null)");
                        defaulttexts.append("");
                    } else {
                        BaseSDIAttributeAction.processEnteredDate(currentdefault, defaulttexts, defaultdates, "(null)", usersM18N, datatype.equalsIgnoreCase("D"), this.logger);
                    }
                    defaultnumerics.append("(null)");
                    defaultclobs.append("");
                    continue;
                }
                if (datatype.equalsIgnoreCase("N")) {
                    if (currentdefault.length() == 0 || currentdefault.equalsIgnoreCase("(null)")) {
                        defaultnumerics.append("(null)");
                        defaulttexts.append("");
                    } else {
                        BaseSDIAttributeAction.processEnteredNumber(currentdefault, defaulttexts, defaultnumerics, usersM18N, true, this.logger);
                    }
                    defaultclobs.append("");
                    defaultdates.append("(null)");
                    continue;
                }
                defaulttexts.append("(null)".equalsIgnoreCase(currentdefault) ? "" : currentdefault);
                defaultdates.append("(null)");
                defaultclobs.append("");
                defaultnumerics.append("(null)");
            }
            String[] defaultTextValues = StringUtil.split(defaulttexts.toString(), ";");
            String[] defaultNumericValues = StringUtil.split(defaultnumerics.toString(), ";");
            String[] defaultClobValues = StringUtil.split(defaultclobs.toString(), ";");
            String[] defaultDateValues = StringUtil.split(defaultdates.toString(), ";");
            for (int i = 0; i < primary.getRowCount(); ++i) {
                primary.setString(i, "defaulttextvalue", defaultTextValues[i]);
                primary.setNumber(i, "defaultnumericvalue", defaultNumericValues[i]);
                primary.setClob(i, "defaultclobvalue", defaultClobValues[i]);
                primary.setDate(i, "defaultdatevalue", defaultDateValues[i]);
            }
        } else {
            this.logger.warn(this.getSdcid() + ": Datatypes and defaults do not match.");
        }
    }

    private void setUpInstructions(DataSet attributes) {
        if (attributes != null) {
            for (int i = 0; i < attributes.getRowCount(); ++i) {
                String instructionflag = attributes.getValue(i, "instructionflag", "");
                if (instructionflag.equalsIgnoreCase("O") || instructionflag.equalsIgnoreCase("A")) {
                    attributes.setValue(i, "datatype", "S");
                    attributes.setValue(i, "editorstyleid", instructionflag.equalsIgnoreCase("A") ? "Yes No Checkbox" : "");
                    attributes.setValue(i, "editsdcid", "");
                    attributes.setValue(i, "editreftypeid", "");
                    attributes.setValue(i, "defaultdatevalue", "");
                    attributes.setValue(i, "defaultclobvalue", "");
                    attributes.setValue(i, "defaulttextvalue", instructionflag.equalsIgnoreCase("A") ? "" : "");
                    attributes.setValue(i, "defaultnumericvalue", "");
                    continue;
                }
                if (instructionflag.equalsIgnoreCase("R")) continue;
                attributes.setValue(i, "instructiontext", "");
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void clearCache(DataSet ds, DataSet beforeEditImage) {
        ArrayList<String> sdcs = new ArrayList<String>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String cacheKey = ds.getValue(i, "basedonid");
            if (cacheKey.length() == 0 && beforeEditImage != null && beforeEditImage.getRowCount() > 0) {
                cacheKey = beforeEditImage.getValue(i, "basedonid");
            }
            if (cacheKey.length() <= 0 || sdcs.contains(cacheKey)) continue;
            CacheUtil.remove(this.connectionInfo.getDatabaseId(), "SDC", cacheKey.toLowerCase());
            sdcs.add(cacheKey);
        }
    }
}


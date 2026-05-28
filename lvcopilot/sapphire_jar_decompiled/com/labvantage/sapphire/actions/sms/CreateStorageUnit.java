/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sms;

import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateStorageUnit
extends BaseAction {
    public String LABVANTAGE_CVS_ID = "$Revision: 54516 $";
    public static final String YES = "Y";
    public static final String ID = "CreateStorageUnit";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_CREATESU = "createsu";
    public static final String PROPERTY_LINKSDCID = "linksdcid";
    public static final String PROPERTY_NEWKEYID1 = "newkeyid1";
    public static final String PROPERTY_LINKPROPNODEID = "linkpropnodeid";

    @Override
    public void processAction(PropertyList properties) {
        String createSU = properties.getProperty(PROPERTY_CREATESU);
        if (!YES.equalsIgnoreCase(createSU)) {
            return;
        }
        String linksdcid = properties.getProperty(PROPERTY_LINKSDCID);
        if (linksdcid.length() == 0) {
            this.setError("INVALID_PROPERTY", "Property \"linksdcid\" is empty");
            return;
        }
        String newKeyid1 = properties.getProperty(PROPERTY_NEWKEYID1);
        if (newKeyid1.trim().length() == 0) {
            this.setError("INVALID_PROPERTY", "Property \"newkeyid1\" is empty");
            return;
        }
        if ((newKeyid1 = this.filterKeys(linksdcid, newKeyid1)).length() == 0) {
            return;
        }
        int parentSize = 0;
        String[] keyids = StringUtil.split(newKeyid1, ";");
        if (keyids != null) {
            parentSize = keyids.length;
        }
        if (parentSize == 0) {
            return;
        }
        String linkpropnodeid = properties.getProperty(PROPERTY_LINKPROPNODEID);
        PropertyList props = this.parseProps(properties, keyids);
        props.setProperty("fromaction", "createstorageunits");
        StorageUnitUtil suUtil = new StorageUnitUtil(this.getConnectionId());
        suUtil.initializeSUProperties(props);
        PropertyList addSUSDIProps = suUtil.getStorageUnitProps();
        addSUSDIProps.setProperty("_linksdcid", linksdcid);
        addSUSDIProps.setProperty("_linkkeyid1", newKeyid1);
        addSUSDIProps.setProperty("_createlinkedsuflag", "N");
        addSUSDIProps.setProperty("_linkpropnodeid", linkpropnodeid);
        try {
            this.getActionProcessor().processAction("AddSDI", VERSIONID, addSUSDIProps);
            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
            properties.setProperty("storageunitid", addSUSDIProps.getProperty(PROPERTY_NEWKEYID1));
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
    }

    private void setUmbrellaSUNodeProps(PropertyList props) {
        String prefix = "saveSUH_";
        String boxPrefix = prefix + "0";
        props.setProperty(boxPrefix + "_id", "USUH");
        props.setProperty(boxPrefix + "_sutypehierarchyid", "");
        props.setProperty(boxPrefix + "_sutypeid", "");
        props.setProperty(boxPrefix + "_nodeid", "");
        props.setProperty(boxPrefix + "_propertytreeid", "");
        props.setProperty(boxPrefix + "_sunodecount", Integer.toString(1));
        props.setProperty(boxPrefix + "_suenvironment", "");
        props.setProperty(boxPrefix + "_sulevel", "-1");
        props.setProperty(boxPrefix + "_childidsequence", "2");
        props.setProperty(boxPrefix + "_childids", "[USUH_1]");
        props.setProperty(boxPrefix + "_parentid", "");
        props.setProperty(boxPrefix + "_label", "");
        props.setProperty(boxPrefix + "_sudesc", "");
        props.setProperty(boxPrefix + "_maxtiallowed", "");
        props.setProperty(boxPrefix + "_moveableflag", "");
    }

    private PropertyList parseProps(PropertyList properties, String[] keyids) {
        String suTypeHierarchyPrefix;
        PropertyList props = new PropertyList();
        int parentSize = keyids.length;
        this.setUmbrellaSUNodeProps(props);
        String prefix = "saveSUH_";
        String[] nodeid = StringUtil.split(properties.getProperty("nodeid"), ";");
        String[] pTreeid = StringUtil.split(properties.getProperty("propertytreeid"), ";");
        String[] suSize = StringUtil.split(properties.getProperty("size"), ";");
        String[] maxTiAllowed = StringUtil.split(properties.getProperty("maxtiallowed"), ";");
        String[] moveableFlag = StringUtil.split(properties.getProperty("moveableflag"), ";");
        int nodeCount = nodeid.length;
        String currentid = "USUH";
        String childid = "USUH_1";
        String suTypeHierarchyId = suTypeHierarchyPrefix = "SUTHierarchy";
        for (int count = 0; count < nodeCount; ++count) {
            String currentPrefix = prefix + (count + 1);
            String parentid = currentid;
            currentid = childid;
            childid = childid + "_1";
            if (count > 0) {
                suTypeHierarchyId = suTypeHierarchyPrefix + "_" + count;
            }
            String currentSUSize = count == 0 ? Integer.toString(parentSize) : suSize[count];
            props.setProperty(currentPrefix + "_id", currentid);
            props.setProperty(currentPrefix + "_sutypeid", "");
            props.setProperty(currentPrefix + "_sutypehierarchyid", suTypeHierarchyId);
            props.setProperty(currentPrefix + "_nodeid", nodeid[count]);
            props.setProperty(currentPrefix + "_propertytreeid", pTreeid[count]);
            props.setProperty(currentPrefix + "_sunodecount", currentSUSize);
            props.setProperty(currentPrefix + "_suenvironment", "");
            props.setProperty(currentPrefix + "_sulevel", Integer.toString(count));
            props.setProperty(currentPrefix + "_parentid", parentid);
            props.setProperty(currentPrefix + "_label", "");
            props.setProperty(currentPrefix + "_sudesc", "");
            props.setProperty(currentPrefix + "_maxtiallowed", maxTiAllowed[count]);
            props.setProperty(currentPrefix + "_moveableflag", moveableFlag[count]);
            if (nodeCount - count > 1) {
                props.setProperty(currentPrefix + "_childidsequence", "2");
                props.setProperty(currentPrefix + "_childids", "[" + childid + "]");
                continue;
            }
            props.setProperty(currentPrefix + "_childidsequence", VERSIONID);
            props.setProperty(currentPrefix + "_childids", "");
        }
        props.setProperty("saveSUHNodeCount", Integer.toString(++nodeCount));
        props.setProperty("saveSUH_0_id", "USUH");
        return props;
    }

    private String filterKeys(String sdcid, String keyid1) {
        String[] keys;
        StringBuilder sb = new StringBuilder();
        for (String key : keys = StringUtil.split(keyid1, ";")) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select count( storageunitid ) count from storageunit where linksdcid = ? and linkkeyid1 = ?", (Object[])new String[]{sdcid, keyid1});
            if (ds == null || ds.getInt(0, "count") != 0) continue;
            sb.append(key).append(";");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}


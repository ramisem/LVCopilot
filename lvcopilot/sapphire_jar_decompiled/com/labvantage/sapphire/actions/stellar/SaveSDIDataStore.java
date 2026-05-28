/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.stellar;

import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.actions.sdi.EditSDIAttachment;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.file.FileManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.DataStore;
import sapphire.util.JsonArray;
import sapphire.util.JsonObject;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIDataStore;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SaveSDIDataStore
extends BaseAction {
    public static final String COLUMNS = "columns";
    public static final String COLUMNID_CATEGORYLIST = "_categorylist";
    public static final String COLUMNID_SDIWORKITEMLIST = "_sdiworkitemlist";
    public static final String PROPERTY_REQUESTEDCOLUMNS = "requestedcolumns";
    public static final String PROPERTY_DETAILCHIPLISTREQUESTITEMS = "detailchiplistrequestitems";
    private HashMap<String, JsonArray> extraProps = new HashMap();
    private String saveMode = "full";
    String detailChipListRequestItems = "";
    private JsonObject audit = new JsonObject();
    protected HashMap<String, HashMap<String, JsonArray>> requestedSDIDataDatasetColumns;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            SDIDataStore sdiDataStore = new SDIDataStore(new JsonObject(properties.getProperty("sdidatastore")), this.connectionInfo);
            this.extraProps = (HashMap)properties.get("extraprops");
            this.saveMode = properties.getProperty("_savemode", "full");
            this.audit = properties.containsKey("audit") ? new JsonObject(properties.getProperty("audit")) : new JsonObject();
            this.requestedSDIDataDatasetColumns = (HashMap)properties.get(PROPERTY_REQUESTEDCOLUMNS);
            this.detailChipListRequestItems = properties.getProperty(PROPERTY_DETAILCHIPLISTREQUESTITEMS);
            if (properties.getProperty("templatekeyid1").length() > 0) {
                this.addExtraProp("", "AddSDI", "templatekeyid1", properties.getProperty("templatekeyid1"));
                this.addExtraProp("", "AddSDI", "templatekeyid2", properties.getProperty("templatekeyid2"));
                this.addExtraProp("", "AddSDI", "templatekeyid3", properties.getProperty("templatekeyid3"));
            }
            try {
                this.save(sdiDataStore);
                String keycolid1 = this.getSDCProcessor().getProperty(sdiDataStore.getSdcid(), "keycolid1");
                String keycolid2 = this.getSDCProcessor().getProperty(sdiDataStore.getSdcid(), "keycolid2");
                String keycolid3 = this.getSDCProcessor().getProperty(sdiDataStore.getSdcid(), "keycolid3");
                properties.put("returnkeyid1", sdiDataStore.getDataStore("primary").getColumnValues(keycolid1, 0, sdiDataStore.getDataStore("primary").getRowCount(), ";"));
                properties.put("returnkeyid2", keycolid2.length() > 0 ? sdiDataStore.getDataStore("primary").getColumnValues(keycolid2, 0, sdiDataStore.getDataStore("primary").getRowCount(), ";") : "");
                properties.put("returnkeyid3", keycolid3.length() > 0 ? sdiDataStore.getDataStore("primary").getColumnValues(keycolid3, 0, sdiDataStore.getDataStore("primary").getRowCount(), ";") : "");
            }
            catch (SapphireException e) {
                this.logger.error("Failed to execute save", e);
                throw e;
            }
        }
        catch (JSONException e) {
            throw new ActionException("Unable to parse sdiDataStore: " + e.getMessage(), e);
        }
    }

    public void save(SDIDataStore sdiDataStore) throws SapphireException {
        this.save("", sdiDataStore, "", "", "", "", null);
    }

    public void save(String sdiRequestName, SDIDataStore sdiDataStore, String parentSdcid, String soloParentKeyid1, String soloParentKeyid2, String soloParentKeyid3, String soloParentRowId) throws SapphireException {
        Object actionProps;
        PropertyList link;
        String linkid;
        String sdcid = sdiDataStore.getSdcid();
        String tracelogid = this.generateTracelogId(sdcid);
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        PropertyListCollection links = sdcProcessor.getLinks(sdcid);
        DataStore primary = sdiDataStore.getDataStore("primary");
        DataSet unlinkPrimaryDS = primary.getUnlinkDataSet();
        boolean doUnlinking = false;
        if (soloParentKeyid1.length() > 0 && (linkid = sdiDataStore.getLinkid()) != null && linkid.length() > 0 && (link = links.find("linkid", linkid)) != null) {
            String linkcolumnid1 = link.getProperty("sdccolumnid");
            String linkcolumnid2 = link.getProperty("sdccolumnid2");
            String linkcolumnid3 = link.getProperty("sdccolumnid3");
            primary.setString(-1, linkcolumnid1, soloParentKeyid1);
            primary.setString(-1, linkcolumnid2, soloParentKeyid2);
            primary.setString(-1, linkcolumnid3, soloParentKeyid3);
            if (!unlinkPrimaryDS.isEmpty()) {
                unlinkPrimaryDS.setString(-1, linkcolumnid1, "(null)");
                unlinkPrimaryDS.setString(-1, linkcolumnid2, "(null)");
                unlinkPrimaryDS.setString(-1, linkcolumnid3, "(null)");
                doUnlinking = true;
            }
        }
        if (soloParentKeyid1.equals("") && primary.size() == 1 && !primary.isRowInsert(0) && this.saveMode.equals("full")) {
            primary.setRowUpdate(0);
        }
        DataSet primaryDS = primary.getDataSet();
        DataSet insertPrimaryDS = primary.getInsertDataSet();
        DataSet updatePrimaryDS = primary.getUpdateDataSet();
        if (soloParentRowId != null && primaryDS.isValidColumn("__parentrowid") && !primaryDS.getColumnValues("__parentrowid", "").isEmpty()) {
            HashMap<String, String> parentRowIdFilterMap = new HashMap<String, String>();
            parentRowIdFilterMap.put("__parentrowid", soloParentRowId);
            primaryDS = primaryDS.getFilteredDataSet(parentRowIdFilterMap);
            insertPrimaryDS = insertPrimaryDS.getFilteredDataSet(parentRowIdFilterMap);
            updatePrimaryDS = updatePrimaryDS.getFilteredDataSet(parentRowIdFilterMap);
        }
        DataSet deletePrimaryDS = primary.getDeleteDataSet();
        HashMap<String, JsonArray> columnmap = this.requestedSDIDataDatasetColumns.get(sdiRequestName);
        if (columnmap == null) {
            throw new ActionException("Unable to verify columns");
        }
        HashSet<String> validateSet = this.getPrimryColumnValidateSet(columnmap);
        if (insertPrimaryDS.size() > 0) {
            String linkcolumnid1;
            PropertyList link2;
            actionProps = new PropertyList();
            ((PropertyList)actionProps).setProperty("sdcid", sdcid);
            if (tracelogid.length() > 0) {
                ((PropertyList)actionProps).setProperty("tracelogid", tracelogid);
            }
            this.addActionProperties((PropertyList)actionProps, insertPrimaryDS, true, validateSet);
            for (String datasetName : sdiDataStore.getDataSetNames()) {
                this.addExtraProp(sdiRequestName, "AddSDI", "exclude" + datasetName, "Y");
            }
            this.addExtraProps(sdiRequestName, (PropertyList)actionProps, "AddSDI", tracelogid);
            String linkid2 = sdiDataStore.getLinkid();
            if (linkid2 != null && linkid2.length() > 0 && (link2 = links.find("linkid", linkid2)) != null && ((HashMap)actionProps).containsKey(linkcolumnid1 = link2.getProperty("sdccolumnid")) && ((PropertyList)actionProps).getProperty(linkcolumnid1).contains(";")) {
                ((PropertyList)actionProps).setProperty("copies", Integer.toString(StringUtil.split(((PropertyList)actionProps).getProperty(linkcolumnid1), ";").length));
            }
            this.getActionProcessor().processAction("AddSDI", "1", (HashMap)actionProps);
            String[] newkeyid1 = StringUtil.split(((PropertyList)actionProps).getProperty("newkeyid1"), ";");
            String[] newkeyid2 = StringUtil.split(((PropertyList)actionProps).getProperty("newkeyid2"), ";");
            String[] newkeyid3 = StringUtil.split(((PropertyList)actionProps).getProperty("newkeyid3"), ";");
            if (newkeyid1.length != insertPrimaryDS.size()) {
                throw new SapphireException("Failed to perform save of new " + sdcid + ". Not enough new ids returned");
            }
            for (int i = 0; i < insertPrimaryDS.size(); ++i) {
                insertPrimaryDS.setString(i, keycolid1, newkeyid1[i]);
                if (newkeyid2.length == newkeyid1.length) {
                    insertPrimaryDS.setString(i, keycolid2, newkeyid2[i]);
                }
                if (newkeyid3.length != newkeyid1.length) continue;
                insertPrimaryDS.setString(i, keycolid3, newkeyid3[i]);
            }
        }
        if (updatePrimaryDS.size() > 0) {
            actionProps = new PropertyList();
            ((PropertyList)actionProps).setProperty("sdcid", sdcid);
            if (tracelogid.length() > 0) {
                ((PropertyList)actionProps).setProperty("tracelogid", tracelogid);
            }
            this.addActionProperties((PropertyList)actionProps, updatePrimaryDS, true, validateSet);
            ((PropertyList)actionProps).setProperty("keyid1", updatePrimaryDS.getColumnValues(keycolid1, ";"));
            ((PropertyList)actionProps).setProperty("keyid2", updatePrimaryDS.getColumnValues(keycolid2, ";"));
            ((PropertyList)actionProps).setProperty("keyid3", updatePrimaryDS.getColumnValues(keycolid3, ";"));
            this.addExtraProps(sdiRequestName, (PropertyList)actionProps, "EditSDI", tracelogid);
            this.getActionProcessor().processAction("EditSDI", "1", (HashMap)actionProps);
        }
        if (doUnlinking) {
            actionProps = new PropertyList();
            ((PropertyList)actionProps).setProperty("sdcid", sdcid);
            if (tracelogid.length() > 0) {
                ((PropertyList)actionProps).setProperty("tracelogid", tracelogid);
            }
            this.addActionProperties((PropertyList)actionProps, unlinkPrimaryDS, true, validateSet);
            ((PropertyList)actionProps).setProperty("keyid1", unlinkPrimaryDS.getColumnValues(keycolid1, ";"));
            ((PropertyList)actionProps).setProperty("keyid2", unlinkPrimaryDS.getColumnValues(keycolid2, ";"));
            ((PropertyList)actionProps).setProperty("keyid3", unlinkPrimaryDS.getColumnValues(keycolid3, ";"));
            this.addExtraProps(sdiRequestName, (PropertyList)actionProps, "EditSDI", tracelogid);
            try {
                this.getActionProcessor().processAction("EditSDI", "1", (HashMap)actionProps);
            }
            catch (ActionException e) {
                this.logger.error("Removing link failed. " + e.getMessage());
                throw new ActionException("Removing link failed.");
            }
        }
        if (deletePrimaryDS.size() > 0) {
            actionProps = new PropertyList();
            ((PropertyList)actionProps).setProperty("sdcid", sdcid);
            if (tracelogid.length() > 0) {
                ((PropertyList)actionProps).setProperty("tracelogid", tracelogid);
            }
            ((PropertyList)actionProps).setProperty("keyid1", deletePrimaryDS.getColumnValues(keycolid1, ";"));
            ((PropertyList)actionProps).setProperty("keyid2", deletePrimaryDS.getColumnValues(keycolid2, ";"));
            ((PropertyList)actionProps).setProperty("keyid3", deletePrimaryDS.getColumnValues(keycolid3, ";"));
            this.addExtraProps(sdiRequestName, (PropertyList)actionProps, "DeleteSDI", tracelogid);
            this.getActionProcessor().processAction("DeleteSDI", "1", (HashMap)actionProps);
        }
        for (String datasetName : sdiDataStore.getDataSetNames()) {
            DataStore dataStore = sdiDataStore.getDataStore(datasetName);
            if (datasetName.equals("primary")) continue;
            if (datasetName.equals("category")) {
                this.saveCategoryItems(sdiRequestName, sdcid, insertPrimaryDS, updatePrimaryDS, dataStore, tracelogid);
                continue;
            }
            if (datasetName.equals("sdiworkitem")) {
                this.saveTestMethods(sdiRequestName, sdcid, insertPrimaryDS, updatePrimaryDS, dataStore, tracelogid);
                continue;
            }
            if (datasetName.equals("attachment")) {
                this.saveAttachments(sdiRequestName, sdcid, insertPrimaryDS, updatePrimaryDS, dataStore, sdiDataStore, tracelogid);
                continue;
            }
            if (datasetName.equals("attribute")) {
                this.saveAttributes(sdiRequestName, sdcid, insertPrimaryDS, updatePrimaryDS, dataStore, sdiDataStore);
                continue;
            }
            PropertyList link3 = links.find("linktableid", datasetName);
            if (link3 == null || !link3.getProperty("linktype").equals("M") || !this.detailChipListRequestItems.contains(";" + datasetName + ";")) continue;
            this.saveDetailChipList(sdiRequestName, sdcid, link3, insertPrimaryDS, updatePrimaryDS, dataStore, tracelogid);
        }
        for (int p = 0; p < primaryDS.getRowCount(); ++p) {
            String parentKeyid1 = primaryDS.getString(p, keycolid1);
            String parentKeyid2 = primaryDS.getString(p, keycolid2);
            String parentKeyid3 = primaryDS.getString(p, keycolid3);
            if (parentKeyid1 == null) continue;
            for (String datasetName : sdiDataStore.getDataSetNames()) {
                PropertyList detailLink;
                DataStore dataStore = sdiDataStore.getDataStore(datasetName);
                if (datasetName.equals("primary") || datasetName.equals("category") || datasetName.equals("sdiworkitem") || datasetName.equals("attachment") || datasetName.equals("attribute")) continue;
                DataSet insertDS = dataStore.getInsertDataSet();
                DataSet updateDS = dataStore.getUpdateDataSet();
                DataSet deleteDS = dataStore.getDeleteDataSet();
                if (!primaryDS.getString(p, "__rowid", "").isEmpty() && dataStore.getDataSet().isValidColumn("__parentrowid") && !dataStore.getDataSet().getColumnValues("__parentrowid", "").isEmpty()) {
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    filterMap.put("__parentrowid", primaryDS.getString(p, "__rowid", ""));
                    insertDS = insertDS.getFilteredDataSet(filterMap);
                    updateDS = updateDS.getFilteredDataSet(filterMap);
                    deleteDS = deleteDS.getFilteredDataSet(filterMap);
                }
                if ((detailLink = links.find("linktableid", datasetName)) != null && detailLink.containsKey("keycolid1") && !this.detailChipListRequestItems.contains(";" + datasetName + ";")) {
                    this.saveMorDTypeDetails(sdiRequestName, sdcid, parentKeyid1, parentKeyid2, parentKeyid3, datasetName, insertDS, updateDS, deleteDS, detailLink, tracelogid);
                }
                if (!datasetName.equals("address")) continue;
                this.saveSDIAddress(sdiRequestName, sdcid, parentKeyid1, parentKeyid2, parentKeyid3, insertDS, updateDS, deleteDS, tracelogid);
            }
            for (String name : sdiDataStore.getSDIDataStoreNames()) {
                SDIDataStore nestedDataStore = sdiDataStore.getSDIDataStore(name);
                this.save(name, nestedDataStore, sdcid, parentKeyid1, parentKeyid2, parentKeyid3, primaryDS.getString(p, "__rowid", ""));
            }
        }
    }

    private HashSet<String> getPrimryColumnValidateSet(HashMap<String, JsonArray> columnmap) {
        JsonArray primaryColsToValidate = columnmap.get("primary");
        HashSet<String> validateList = new HashSet<String>();
        for (JsonObject col : primaryColsToValidate.toJsonObjectArray()) {
            validateList.add(col.getString("columnid"));
        }
        return validateList;
    }

    protected void addActionProperties(PropertyList props, DataSet ds, boolean escapeNulls, HashSet validateSet) {
        String[] columns;
        for (String columnid : columns = ds.getColumns()) {
            if (columnid.startsWith("_") || validateSet != null && !validateSet.contains(columnid)) continue;
            String values = ds.getColumnValues(columnid, ";");
            if (escapeNulls) {
                if (values.equals("")) {
                    values = "(null)";
                } else {
                    if (values.startsWith(";")) {
                        values = "(null)" + values;
                    }
                    if (values.endsWith(";")) {
                        values = values + "(null)";
                    }
                    values = StringUtil.replaceAll(values, ";;", ";(null);");
                }
            }
            props.setProperty(columnid, values);
        }
    }

    private void saveMorDTypeDetails(String sdiRequestName, String sdcid, String parentKeyid1, String parentKeyid2, String parentKeyid3, String datasetName, DataSet insertDS, DataSet updateDS, DataSet deleteDS, PropertyList detailLink, String tracelogid) throws ActionException {
        String columnid;
        int i;
        PropertyList actionProps;
        String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
        String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
        int keycolcount = Integer.parseInt(detailLink.getProperty("keycolcount"));
        if (insertDS.size() > 0) {
            insertDS.setString(-1, keycolid1, parentKeyid1);
            insertDS.setString(-1, keycolid2, parentKeyid2);
            insertDS.setString(-1, keycolid3, parentKeyid3);
            actionProps = new PropertyList();
            this.addActionProperties(actionProps, insertDS, true, null);
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("linkid", detailLink.getProperty("linkid"));
            for (i = 0; i < keycolcount; ++i) {
                columnid = detailLink.getProperty("keycolid" + (i + 1));
                actionProps.setProperty(columnid, insertDS.getColumnValues(columnid, ";"));
            }
            this.addExtraProps(sdiRequestName, actionProps, "AddSDIDetail", tracelogid);
            this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
        }
        if (updateDS.size() > 0) {
            actionProps = new PropertyList();
            this.addActionProperties(actionProps, updateDS, true, null);
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("linkid", detailLink.getProperty("linkid"));
            for (i = 0; i < keycolcount; ++i) {
                columnid = detailLink.getProperty("keycolid" + (i + 1));
                actionProps.setProperty(columnid, updateDS.getColumnValues(columnid, ";"));
            }
            this.addExtraProps(sdiRequestName, actionProps, "EditSDIDetail", tracelogid);
            this.getActionProcessor().processAction("EditSDIDetail", "1", actionProps);
        }
        if (deleteDS.size() > 0) {
            actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("linkid", detailLink.getProperty("linkid"));
            for (i = 0; i < keycolcount; ++i) {
                columnid = detailLink.getProperty("keycolid" + (i + 1));
                actionProps.setProperty(columnid, deleteDS.getColumnValues(columnid, ";"));
            }
            this.addExtraProps(sdiRequestName, actionProps, "DeleteSDIDetail", tracelogid);
            this.getActionProcessor().processAction("DeleteSDIDetail", "1", actionProps);
        }
    }

    private void saveSDIAddress(String sdiRequestName, String sdcid, String parentKeyid1, String parentKeyid2, String parentKeyid3, DataSet insertDS, DataSet updateDS, DataSet deleteDS, String tracelogid) throws ActionException {
        PropertyList actionProps;
        if (insertDS.size() > 0) {
            insertDS.setString(-1, "keyid1", parentKeyid1);
            insertDS.setString(-1, "keyid2", parentKeyid2);
            insertDS.setString(-1, "keyid3", parentKeyid3);
            actionProps = new PropertyList();
            this.addActionProperties(actionProps, insertDS, true, null);
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("propsmatch", "Y");
            this.addExtraProps(sdiRequestName, actionProps, "AddSDIAddress", tracelogid);
            this.getActionProcessor().processAction("AddSDIAddress", "1", actionProps);
        }
        if (updateDS.size() > 0) {
            actionProps = new PropertyList();
            this.addActionProperties(actionProps, updateDS, true, null);
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("propsmatch", "Y");
            this.addExtraProps(sdiRequestName, actionProps, "EditSDIAddress", tracelogid);
            this.getActionProcessor().processAction("EditSDIAddress", "1", actionProps);
        }
        if (deleteDS.size() > 0) {
            HashMap<String, BigDecimal> fil = new HashMap<String, BigDecimal>();
            fil.put("__rowstatus", new BigDecimal(0));
            deleteDS = deleteDS.getFilteredDataSet(fil);
            PropertyList actionProps2 = new PropertyList();
            this.addActionProperties(actionProps2, deleteDS, true, null);
            actionProps2.setProperty("sdcid", sdcid);
            this.addExtraProps(sdiRequestName, actionProps2, "DeleteSDIAddress", tracelogid);
            this.getActionProcessor().processAction("DeleteSDIAddress", "1", actionProps2);
        }
    }

    private void addExtraProps(String sdiRequestName, PropertyList actionProps, String actionid, String tracelogid) {
        actionProps.setProperty("_savemode", this.saveMode);
        actionProps.setProperty("tracelogid", tracelogid);
        if (this.extraProps != null) {
            JsonArray extra = this.extraProps.get(sdiRequestName);
            for (JsonObject extraProp : extra.toJsonObjectArray()) {
                if (!extraProp.getString("actionid").equals(actionid)) continue;
                actionProps.setProperty(extraProp.getString("propertyid"), extraProp.getString("propertyvalue"));
            }
        }
    }

    private void saveCategoryItems(String sdiRequestName, String sdcid, DataSet insertPrimaryDS, DataSet updatePrimaryDS, DataStore dataStore, String tracelogid) throws ActionException {
        String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        for (int i = 0; i < insertPrimaryDS.size(); ++i) {
            ArrayList<String> newCategories = new ArrayList<String>(Arrays.asList(StringUtil.split(insertPrimaryDS.getValue(i, COLUMNID_CATEGORYLIST), ";")));
            String newkeyid1 = insertPrimaryDS.getString(i, keycolid1);
            StringBuilder categoryList = new StringBuilder();
            for (String categoryid : newCategories) {
                if (categoryid.length() <= 0) continue;
                categoryList.append(";").append(categoryid);
            }
            if (categoryList.length() <= 0) continue;
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", newkeyid1);
            actionProps.setProperty("categoryid", categoryList.substring(1));
            actionProps.setProperty("propsmatch", "N");
            this.addExtraProps(sdiRequestName, actionProps, "AddCategoryItem", tracelogid);
            this.getActionProcessor().processAction("AddCategoryItem", "1", actionProps);
        }
        DataSet addCategories = new DataSet();
        DataSet deleteCategories = new DataSet();
        for (int i = 0; i < updatePrimaryDS.size(); ++i) {
            int row;
            ArrayList<String> newCategories = new ArrayList<String>(Arrays.asList(StringUtil.split(updatePrimaryDS.getValue(i, COLUMNID_CATEGORYLIST), ";")));
            String keyid1 = updatePrimaryDS.getValue(i, keycolid1);
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("keyid1", keyid1);
            DataSet currentCategories = dataStore.getFilteredDataSet(filter);
            for (int j = 0; j < currentCategories.size(); ++j) {
                String categoryid = currentCategories.getValue(j, "categoryid");
                if (newCategories.contains(categoryid)) {
                    newCategories.remove(categoryid);
                    continue;
                }
                row = deleteCategories.addRow();
                deleteCategories.setString(row, "keyid1", keyid1);
                deleteCategories.setString(row, "categoryid", categoryid);
            }
            for (String categoryid : newCategories) {
                if (categoryid.length() <= 0) continue;
                row = addCategories.addRow();
                addCategories.setString(row, "keyid1", keyid1);
                addCategories.setString(row, "categoryid", categoryid);
            }
        }
        if (!addCategories.isEmpty()) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", addCategories.getColumnValues("keyid1", ";"));
            actionProps.setProperty("categoryid", addCategories.getColumnValues("categoryid", ";"));
            actionProps.setProperty("propsmatch", "Y");
            this.addExtraProps(sdiRequestName, actionProps, "AddCategoryItem", tracelogid);
            this.getActionProcessor().processAction("AddCategoryItem", "1", actionProps);
        }
        if (!deleteCategories.isEmpty()) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", deleteCategories.getColumnValues("keyid1", ";"));
            actionProps.setProperty("categoryid", deleteCategories.getColumnValues("categoryid", ";"));
            actionProps.setProperty("propsmatch", "Y");
            this.addExtraProps(sdiRequestName, actionProps, "DeleteCategoryItem", tracelogid);
            this.getActionProcessor().processAction("DeleteCategoryItem", "1", actionProps);
        }
    }

    private void saveTestMethods(String sdiRequestName, String sdcid, DataSet insertPrimaryDS, DataSet updatePrimaryDS, DataStore dataStore, String tracelogid) throws ActionException {
        String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
        String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
        for (int i = 0; i < insertPrimaryDS.size(); ++i) {
            ArrayList<String> newWorkitems = new ArrayList<String>(Arrays.asList(StringUtil.split(insertPrimaryDS.getValue(i, COLUMNID_SDIWORKITEMLIST), ";")));
            String newkeyid1 = insertPrimaryDS.getString(i, keycolid1);
            String newkeyid2 = insertPrimaryDS.getString(i, keycolid2);
            String newkeyid3 = insertPrimaryDS.getString(i, keycolid3);
            StringBuilder workitemidList = new StringBuilder();
            StringBuilder workitemVersionIdList = new StringBuilder();
            for (String workitemid : newWorkitems) {
                if (workitemid.length() <= 0) continue;
                if (workitemid.contains("|")) {
                    String[] parts = StringUtil.split(workitemid, "|");
                    workitemidList.append(";").append(parts[0]);
                    workitemVersionIdList.append(";").append(parts[1]);
                    continue;
                }
                workitemidList.append(";").append(workitemid);
                workitemVersionIdList.append(";");
            }
            if (workitemidList.length() <= 0) continue;
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", newkeyid1);
            actionProps.setProperty("keyid2", newkeyid2);
            actionProps.setProperty("keyid3", newkeyid3);
            actionProps.setProperty("workitemid", workitemidList.substring(1));
            actionProps.setProperty("workitemversionid", workitemVersionIdList.substring(1));
            actionProps.setProperty("propsmatch", "N");
            actionProps.setProperty("applyworkitem", "N");
            this.addExtraProps(sdiRequestName, actionProps, "AddSDIWorkItem", tracelogid);
            this.getActionProcessor().processAction("AddSDIWorkItem", "1", actionProps);
        }
        DataSet addWorkitems = new DataSet();
        DataSet deleteWorkitems = new DataSet();
        for (int i = 0; i < updatePrimaryDS.size(); ++i) {
            ArrayList<String> newWorkitems = new ArrayList<String>(Arrays.asList(StringUtil.split(updatePrimaryDS.getValue(i, COLUMNID_SDIWORKITEMLIST), ";")));
            String keyid1 = updatePrimaryDS.getValue(i, keycolid1);
            String keyid2 = updatePrimaryDS.getValue(i, keycolid2);
            String keyid3 = updatePrimaryDS.getValue(i, keycolid3);
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("keyid1", keyid1);
            if (keycolid2.length() > 0) {
                filter.put("keyid2", keyid2);
            }
            if (keycolid3.length() > 0) {
                filter.put("keyid3", keyid3);
            }
            DataSet currentWorkitems = dataStore.getFilteredDataSet(filter);
            for (int j = 0; j < currentWorkitems.size(); ++j) {
                String workitemid = currentWorkitems.getValue(j, "workitemid");
                String workiteminstance = currentWorkitems.getValue(j, "workiteminstance");
                if (newWorkitems.contains(workitemid)) {
                    newWorkitems.remove(workitemid);
                    continue;
                }
                int row = deleteWorkitems.addRow();
                deleteWorkitems.setString(row, "keyid1", keyid1);
                deleteWorkitems.setString(row, "keyid2", keyid2);
                deleteWorkitems.setString(row, "keyid3", keyid3);
                deleteWorkitems.setString(row, "workitemid", workitemid);
                deleteWorkitems.setString(row, "workiteminstance", workiteminstance);
            }
            for (String workitemid : newWorkitems) {
                if (workitemid.length() <= 0) continue;
                int row = addWorkitems.addRow();
                addWorkitems.setString(row, "keyid1", keyid1);
                addWorkitems.setString(row, "keyid2", keyid2);
                addWorkitems.setString(row, "keyid3", keyid3);
                if (workitemid.contains("|")) {
                    String[] parts = StringUtil.split(workitemid, "|");
                    addWorkitems.setString(row, "workitemid", parts[0]);
                    addWorkitems.setString(row, "workitemversionid", parts[1]);
                    continue;
                }
                addWorkitems.setString(row, "workitemid", workitemid);
            }
        }
        if (!addWorkitems.isEmpty()) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", addWorkitems.getColumnValues("keyid1", ";"));
            if (keycolid2.length() > 0) {
                actionProps.setProperty("keyid2", addWorkitems.getColumnValues("keyid2", ";"));
            }
            if (keycolid3.length() > 0) {
                actionProps.setProperty("keyid3", addWorkitems.getColumnValues("keyid3", ";"));
            }
            actionProps.setProperty("workitemid", addWorkitems.getColumnValues("workitemid", ";"));
            actionProps.setProperty("workitemversionid", addWorkitems.getColumnValues("workitemversionid", ";"));
            actionProps.setProperty("propsmatch", "Y");
            actionProps.setProperty("applyworkitem", "N");
            this.addExtraProps(sdiRequestName, actionProps, "AddSDIWorkItem", tracelogid);
            this.getActionProcessor().processAction("AddSDIWorkItem", "1", actionProps);
        }
        if (!deleteWorkitems.isEmpty()) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", deleteWorkitems.getColumnValues("keyid1", ";"));
            if (keycolid2.length() > 0) {
                actionProps.setProperty("keyid2", deleteWorkitems.getColumnValues("keyid2", ";"));
            }
            if (keycolid3.length() > 0) {
                actionProps.setProperty("keyid3", deleteWorkitems.getColumnValues("keyid3", ";"));
            }
            actionProps.setProperty("workitemid", deleteWorkitems.getColumnValues("workitemid", ";"));
            actionProps.setProperty("workiteminstance", deleteWorkitems.getColumnValues("workiteminstance", ";"));
            this.addExtraProps(sdiRequestName, actionProps, "DeleteSDIWorkItem", tracelogid);
            this.getActionProcessor().processAction("DeleteSDIWorkItem", "1", actionProps);
        }
    }

    private void saveDetailChipList(String sdiRequestName, String sdcid, PropertyList linkProperties, DataSet insertPrimaryDS, DataSet updatePrimaryDS, DataStore dataStore, String tracelogid) throws ActionException {
        String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
        String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
        String linkid = linkProperties.getProperty("linkid");
        String columnid = "_" + linkid.toLowerCase();
        String linkSdcid = linkProperties.getProperty("linksdcid");
        if (linkSdcid.length() > 0) {
            String link_keycolid1 = this.getSDCProcessor().getProperty(linkSdcid, "keycolid1");
            String link_keycolid2 = this.getSDCProcessor().getProperty(linkSdcid, "keycolid2");
            String link_keycolid3 = this.getSDCProcessor().getProperty(linkSdcid, "keycolid3");
            for (int i = 0; i < insertPrimaryDS.size(); ++i) {
                ArrayList<String> newDetails = new ArrayList<String>(Arrays.asList(StringUtil.split(insertPrimaryDS.getValue(i, columnid), ";")));
                String newkeyid1 = insertPrimaryDS.getString(i, keycolid1);
                String newkeyid2 = insertPrimaryDS.getString(i, keycolid2);
                String newkeyid3 = insertPrimaryDS.getString(i, keycolid3);
                StringBuilder newKeyid1List = new StringBuilder();
                StringBuilder newKeyid2List = new StringBuilder();
                StringBuilder newKeyid3List = new StringBuilder();
                StringBuilder detailKeyid1List = new StringBuilder();
                StringBuilder detailKeyid2List = new StringBuilder();
                StringBuilder detailKeyid3List = new StringBuilder();
                for (String newDetail : newDetails) {
                    if (newDetail.length() <= 0) continue;
                    String[] parts = StringUtil.split(newDetail, "|");
                    newKeyid1List.append(";").append(newkeyid1);
                    newKeyid2List.append(";").append(newkeyid2);
                    newKeyid3List.append(";").append(newkeyid3);
                    detailKeyid1List.append(";").append(parts[0]);
                    detailKeyid2List.append(";").append(parts.length > 1 ? parts[2] : "");
                    detailKeyid3List.append(";").append(parts.length > 2 ? parts[3] : "");
                }
                if (detailKeyid1List.length() <= 0) continue;
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", sdcid);
                actionProps.setProperty("linkid", linkid);
                actionProps.setProperty("keyid1", newKeyid1List.substring(1));
                actionProps.setProperty("keyid2", newKeyid2List.substring(1));
                actionProps.setProperty("keyid3", newKeyid3List.substring(1));
                actionProps.setProperty(link_keycolid1, detailKeyid1List.substring(1));
                actionProps.setProperty(link_keycolid2, detailKeyid2List.substring(1));
                actionProps.setProperty(link_keycolid3, detailKeyid3List.substring(1));
                this.addExtraProps(sdiRequestName, actionProps, "AddSDIDetail_" + linkSdcid, tracelogid);
                this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
            }
            DataSet addDetails = new DataSet();
            DataSet deleteDetails = new DataSet();
            for (int i = 0; i < updatePrimaryDS.size(); ++i) {
                ArrayList<String> newDetails = new ArrayList<String>(Arrays.asList(StringUtil.split(updatePrimaryDS.getValue(i, columnid), ";")));
                String keyid1 = updatePrimaryDS.getValue(i, keycolid1);
                String keyid2 = updatePrimaryDS.getValue(i, keycolid2);
                String keyid3 = updatePrimaryDS.getValue(i, keycolid3);
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put(keycolid1, keyid1);
                if (keycolid2.length() > 0) {
                    filter.put(keycolid2, keyid2);
                }
                if (keycolid3.length() > 0) {
                    filter.put(keycolid3, keyid3);
                }
                DataSet currentDetails = dataStore.getFilteredDataSet(filter);
                for (int j = 0; j < currentDetails.size(); ++j) {
                    String detailkeyid1 = currentDetails.getValue(j, link_keycolid1);
                    String detailkeyid2 = currentDetails.getValue(j, link_keycolid2);
                    String detailkeyid3 = currentDetails.getValue(j, link_keycolid3);
                    if (newDetails.contains(detailkeyid1)) {
                        newDetails.remove(detailkeyid1);
                        continue;
                    }
                    int row = deleteDetails.addRow();
                    deleteDetails.setString(row, keycolid1, keyid1);
                    deleteDetails.setString(row, keycolid2, keyid2);
                    deleteDetails.setString(row, keycolid3, keyid3);
                    deleteDetails.setString(row, link_keycolid1, detailkeyid1);
                    deleteDetails.setString(row, link_keycolid2, detailkeyid2);
                    deleteDetails.setString(row, link_keycolid3, detailkeyid3);
                }
                for (String newDetail : newDetails) {
                    if (newDetail.length() <= 0) continue;
                    int row = addDetails.addRow();
                    addDetails.setString(row, keycolid1, keyid1);
                    addDetails.setString(row, keycolid2, keyid2);
                    addDetails.setString(row, keycolid3, keyid3);
                    String[] parts = StringUtil.split(newDetail, "|");
                    addDetails.setString(row, link_keycolid1, parts[0]);
                    addDetails.setString(row, link_keycolid2, parts.length > 1 ? parts[2] : "");
                    addDetails.setString(row, link_keycolid2, parts.length > 2 ? parts[3] : "");
                }
            }
            if (!addDetails.isEmpty()) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", sdcid);
                actionProps.setProperty("linkid", linkid);
                actionProps.setProperty(keycolid1, addDetails.getColumnValues(keycolid1, ";"));
                actionProps.setProperty(keycolid2, addDetails.getColumnValues(keycolid2, ";"));
                actionProps.setProperty(keycolid3, addDetails.getColumnValues(keycolid3, ";"));
                actionProps.setProperty(link_keycolid1, addDetails.getColumnValues(link_keycolid1, ";"));
                actionProps.setProperty(link_keycolid2, addDetails.getColumnValues(link_keycolid2, ";"));
                actionProps.setProperty(link_keycolid3, addDetails.getColumnValues(link_keycolid3, ";"));
                this.addExtraProps(sdiRequestName, actionProps, "AddSDIDetail_" + linkSdcid, tracelogid);
                this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
            }
            if (!deleteDetails.isEmpty()) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", sdcid);
                actionProps.setProperty("linkid", linkid);
                actionProps.setProperty(keycolid1, deleteDetails.getColumnValues(keycolid1, ";"));
                actionProps.setProperty(keycolid2, deleteDetails.getColumnValues(keycolid2, ";"));
                actionProps.setProperty(keycolid3, deleteDetails.getColumnValues(keycolid3, ";"));
                actionProps.setProperty(link_keycolid1, deleteDetails.getColumnValues(link_keycolid1, ";"));
                actionProps.setProperty(link_keycolid2, deleteDetails.getColumnValues(link_keycolid2, ";"));
                actionProps.setProperty(link_keycolid3, deleteDetails.getColumnValues(link_keycolid3, ";"));
                this.addExtraProps(sdiRequestName, actionProps, "DeleteSDIDetail_" + linkSdcid, tracelogid);
                this.getActionProcessor().processAction("DeleteSDIDetail", "1", actionProps);
            }
        }
    }

    private void saveAttributes(String sdiRequestName, String sdcid, DataSet insertPrimaryDS, DataSet updatePrimaryDS, DataStore dataStore, SDIDataStore parentSDIDataStore) throws ActionException {
        JsonArray attribtueColsToValidate;
        String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
        String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
        DataSet newAttributes = dataStore.getInsertDataSet();
        DataSet editAttributes = dataStore.getUpdateDataSet();
        DataSet deleteAttributes = dataStore.getDeleteDataSet();
        HashMap<String, JsonArray> columnmap = this.requestedSDIDataDatasetColumns.get(sdiRequestName);
        if (columnmap != null && (attribtueColsToValidate = columnmap.get("attribute")) != null && attribtueColsToValidate.size() > 0) {
            if (insertPrimaryDS.size() > 0) {
                this.handleSaveAttributesAsColumn(sdcid, insertPrimaryDS, newAttributes, editAttributes, attribtueColsToValidate, keycolid1, keycolid2, keycolid3, false);
            }
            if (updatePrimaryDS.size() > 0) {
                this.handleSaveAttributesAsColumn(sdcid, updatePrimaryDS, newAttributes, editAttributes, attribtueColsToValidate, keycolid1, keycolid2, keycolid3, true);
            }
        }
        for (int i = 0; i < dataStore.getDataSet().getRowCount(); ++i) {
            if (insertPrimaryDS.size() == 1) {
                String rowid = insertPrimaryDS.getString(0, "__rowid");
                if (rowid.length() == 0) {
                    dataStore.getDataSet().setValue(i, "keyid1", insertPrimaryDS.getString(0, keycolid1));
                    if (keycolid2.length() > 0) {
                        dataStore.getDataSet().setValue(i, "keyid2", insertPrimaryDS.getString(0, keycolid2));
                    }
                    if (keycolid3.length() <= 0) continue;
                    dataStore.getDataSet().setValue(i, "keyid3", insertPrimaryDS.getString(0, keycolid3));
                    continue;
                }
                if (!dataStore.getDataSet().getValue(i, "__rowid").equals(rowid)) continue;
                dataStore.getDataSet().setValue(i, "keyid1", insertPrimaryDS.getString(0, keycolid1));
                dataStore.getDataSet().setValue(i, "keyid2", keycolid2.length() > 0 ? insertPrimaryDS.getString(0, keycolid2) : "");
                dataStore.getDataSet().setValue(i, "keyid3", keycolid3.length() > 0 ? insertPrimaryDS.getString(0, keycolid3) : "");
                continue;
            }
            if (dataStore.getDataSet().getValue(i, "keyid1", "").length() != 0) continue;
            if (updatePrimaryDS.size() == 1) {
                dataStore.getDataSet().setValue(i, "keyid1", updatePrimaryDS.getString(0, keycolid1));
                if (keycolid2.length() > 0) {
                    dataStore.getDataSet().setValue(i, "keyid2", updatePrimaryDS.getString(0, keycolid2));
                }
                if (keycolid3.length() <= 0) continue;
                dataStore.getDataSet().setValue(i, "keyid3", updatePrimaryDS.getString(0, keycolid3));
                continue;
            }
            dataStore.getDataSet().setValue(i, "keyid1", parentSDIDataStore.getDataStore("primary").getString(0, keycolid1));
            if (keycolid2.length() > 0) {
                dataStore.getDataSet().setValue(i, "keyid2", parentSDIDataStore.getDataStore("primary").getString(0, keycolid2));
            }
            if (keycolid3.length() <= 0) continue;
            dataStore.getDataSet().setValue(i, "keyid3", parentSDIDataStore.getDataStore("primary").getString(0, keycolid3));
        }
        if (newAttributes.size() > 0) {
            try {
                newAttributes.removeColumn("__rowstatus");
                newAttributes.addColumn("__rowstatus", 0);
                newAttributes.setValue(-1, "__rowstatus", "I");
                HashMap<String, String> filteradhoc = new HashMap<String, String>();
                filteradhoc.put("attributesourcetype", "Adhoc");
                DataSet adhoc = newAttributes.getFilteredDataSet(filteradhoc);
                if (adhoc.getRowCount() > 0) {
                    BaseSDIAttributeAction.saveAttributeData(adhoc, this.getActionProcessor(), new M18NUtil(this.connectionInfo), this.audit.getString("reason", ""), this.audit.getString("activity"), this.audit.getBoolean("signed", false) ? "Y" : "N");
                }
                HashMap<String, String> filterlink = new HashMap<String, String>();
                filterlink.put("attributesourcetype", "Link");
                DataSet link = newAttributes.getFilteredDataSet(filterlink);
                if (link.getRowCount() > 0) {
                    SDIData existing;
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setRequestItem("attribute");
                    sdiRequest.setSDCid(sdcid);
                    sdiRequest.setKeyid1List(parentSDIDataStore.getDataStore("primary").getColumnValues(keycolid1, 0, parentSDIDataStore.getDataStore("primary").getRowCount(), ";"));
                    if (keycolid2.length() > 0) {
                        sdiRequest.setKeyid2List(parentSDIDataStore.getDataStore("primary").getColumnValues(keycolid2, 0, parentSDIDataStore.getDataStore("primary").getRowCount(), ";"));
                    }
                    if (keycolid3.length() > 0) {
                        sdiRequest.setKeyid3List(parentSDIDataStore.getDataStore("primary").getColumnValues(keycolid3, 0, parentSDIDataStore.getDataStore("primary").getRowCount(), ";"));
                    }
                    if ((existing = this.getSDIProcessor().getSDIData(sdiRequest)) != null && existing.getDataset("attribute") != null) {
                        DataSet existingAttributes = existing.getDataset("attribute").getFilteredDataSet(filterlink);
                        for (int r = 0; r < existingAttributes.getRowCount(); ++r) {
                            String sourcesdcid = existingAttributes.getString(r, "sourcesdcid", "");
                            String sourcekeyid1 = existingAttributes.getString(r, "sourcekeyid1", "");
                            String sourcekeyid2 = existingAttributes.getString(r, "sourcekeyid2", "");
                            String sourcekeyid3 = existingAttributes.getString(r, "sourcekeyid3", "");
                            String attributeid = existingAttributes.getString(r, "attributeid", "");
                            String keyid1 = existingAttributes.getString(r, "keyid1", "");
                            String keyid2 = existingAttributes.getString(r, "keyid2", "");
                            String keyid3 = existingAttributes.getString(r, "keyid3", "");
                            for (int n = 0; n < link.getRowCount(); ++n) {
                                String linksourcesdcid = link.getString(n, "sourcesdcid", "");
                                String linksourcekeyid1 = link.getString(n, "sourcekeyid1", "");
                                String linksourcekeyid2 = link.getString(n, "sourcekeyid2", "");
                                String linksourcekeyid3 = link.getString(n, "sourcekeyid3", "");
                                String linkattributeid = link.getString(n, "attributeid", "");
                                String linkkeyid1 = link.getString(n, "keyid1", "");
                                String linkkeyid2 = link.getString(n, "keyid2", "(null)");
                                String linkkeyid3 = link.getString(n, "keyid3", "(null)");
                                if (!linkattributeid.equals(attributeid) || !linksourcekeyid1.equals(sourcekeyid1) || !linksourcesdcid.equals(sourcesdcid) || !linksourcekeyid2.equalsIgnoreCase(sourcekeyid2) || !linksourcekeyid3.equalsIgnoreCase(sourcekeyid3) || !linkkeyid1.equals(keyid1) || !linkkeyid2.equals(keyid2) || !linkkeyid3.equals(keyid3)) continue;
                                link.setValue(n, "__rowstatus", "U");
                                link.setValue(n, "sdiattributeid", existingAttributes.getValue(r, "sdiattributeid", ""));
                                link.setValue(n, "attributeinstance", existingAttributes.getValue(r, "attributeinstance", ""));
                                link.setValue(n, "keyid1", existingAttributes.getValue(r, "keyid1", ""));
                                link.setValue(n, "keyid2", existingAttributes.getValue(r, "keyid2", ""));
                                link.setValue(n, "keyid3", existingAttributes.getValue(r, "keyid3", ""));
                            }
                        }
                    }
                    BaseSDIAttributeAction.saveAttributeData(link, this.getActionProcessor(), new M18NUtil(this.connectionInfo), this.audit.getString("reason", ""), this.audit.getString("activity"), this.audit.getBoolean("signed", false) ? "Y" : "N");
                }
            }
            catch (Exception e) {
                throw new ActionException("Failed to add new attributes", e);
            }
        }
        if (editAttributes.size() > 0) {
            editAttributes.removeColumn("__rowstatus");
            editAttributes.addColumn("__rowstatus", 0);
            editAttributes.setValue(-1, "__rowstatus", "U");
            try {
                BaseSDIAttributeAction.saveAttributeData(editAttributes, this.getActionProcessor(), new M18NUtil(this.connectionInfo), this.audit.getString("reason", ""), this.audit.getString("activity"), this.audit.getBoolean("signed", false) ? "Y" : "N");
            }
            catch (Exception e) {
                throw new ActionException("Failed to update attributes", e);
            }
        }
        if (deleteAttributes.size() > 0) {
            deleteAttributes.addColumn("_checkingempty", 0);
            for (int i = 0; i < deleteAttributes.getRowCount(); ++i) {
                if (deleteAttributes.getValue(i, "__rowstatus", "").length() <= 0 && deleteAttributes.getValue(i, "__lockstate", "").length() <= 0) continue;
                deleteAttributes.setValue(i, "_checkingempty", "Y");
            }
            HashMap<String, String> fil = new HashMap<String, String>();
            fil.put("_checkingempty", "Y");
            deleteAttributes = deleteAttributes.getFilteredDataSet(fil);
            deleteAttributes.removeColumn("__rowstatus");
            deleteAttributes.addColumn("__rowstatus", 0);
            deleteAttributes.setValue(-1, "__rowstatus", "D");
            try {
                BaseSDIAttributeAction.saveAttributeData(deleteAttributes, this.getActionProcessor(), new M18NUtil(this.connectionInfo), this.audit.getString("reason", ""), this.audit.getString("activity"), this.audit.getBoolean("signed", false) ? "Y" : "N");
            }
            catch (Exception e) {
                throw new ActionException("Failed to delete attributes", e);
            }
        }
    }

    private void handleSaveAttributesAsColumn(String sdcid, DataSet primary, DataSet addElementAttributes, DataSet editDatasetAttributes, JsonArray attribtueColsToValidate, String keycolid1, String keycolid2, String keycolid3, boolean editMode) {
        for (int i = 0; i < primary.size(); ++i) {
            String keyid1 = primary.getString(i, keycolid1);
            String keyid2 = keycolid2.length() > 0 ? primary.getString(i, keycolid2) : "";
            String keyid3 = keycolid3.length() > 0 ? primary.getString(i, keycolid3) : "";
            HashMap<String, Object> findSDIAtrributeMap = new HashMap<String, Object>();
            findSDIAtrributeMap.put("keyid1", keyid1);
            if (keyid2.length() > 0) {
                findSDIAtrributeMap.put("keyid2", keyid2);
            }
            if (keyid3.length() > 0) {
                findSDIAtrributeMap.put("keyid3", keyid3);
            }
            findSDIAtrributeMap.put("attributeinstance", new BigDecimal(1));
            findSDIAtrributeMap.put("attributesdcid", sdcid);
            HashSet<String> validateSet = new HashSet<String>();
            for (JsonObject col : attribtueColsToValidate.toJsonObjectArray()) {
                validateSet.add(col.getString("attributeid"));
            }
            if (editMode) {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT * FROM sdiattribute WHERE  attributeid IN (" + safeSQL.addIn(validateSet) + ") AND attributesdcid=" + safeSQL.addVar(sdcid) + " AND attributeinstance=1 AND sdcid=" + safeSQL.addVar(sdcid) + " AND keyid1=" + safeSQL.addVar(primary.getString(i, keycolid1));
                if (keycolid2.length() > 0) {
                    sql = sql + " AND keyid2=" + safeSQL.addVar(primary.getString(i, keycolid2));
                }
                if (keycolid3.length() > 0) {
                    sql = sql + " AND keyid3=" + safeSQL.addVar(primary.getString(i, keycolid3));
                }
                DataSet currentAttributes = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                for (int j = 0; j < currentAttributes.size(); ++j) {
                    findSDIAtrributeMap.put("attributeid", currentAttributes.getString(j, "attributeid"));
                    if (editDatasetAttributes.findRow(findSDIAtrributeMap) != -1) continue;
                    editDatasetAttributes.copyRow(currentAttributes, j, 1);
                }
            }
            for (String attributeid : validateSet) {
                int findRow;
                String valueCol;
                String dataType;
                boolean edited = false;
                findSDIAtrributeMap.put("attributeid", attributeid);
                int attributeType = primary.getColumnType("att_" + attributeid);
                if (attributeType == 1) {
                    dataType = "N";
                    valueCol = "numericvalue";
                } else if (attributeType == 2) {
                    dataType = "D";
                    valueCol = "datevalue";
                } else if (attributeType == 3) {
                    dataType = "C";
                    valueCol = "clobvalue";
                } else {
                    dataType = "S";
                    valueCol = "textvalue";
                }
                Object attributeValue = primary.getObject(i, "att_" + attributeid);
                if (editMode && (findRow = editDatasetAttributes.findRow(findSDIAtrributeMap)) >= 0) {
                    editDatasetAttributes.setObject(findRow, valueCol, attributeValue);
                    edited = true;
                }
                if (edited) continue;
                findRow = addElementAttributes.findRow(findSDIAtrributeMap);
                if (findRow >= 0) {
                    editDatasetAttributes.setObject(findRow, valueCol, attributeValue);
                    continue;
                }
                int newRow = addElementAttributes.addRow();
                addElementAttributes.setString(newRow, "sdcid", sdcid);
                addElementAttributes.setString(newRow, "keyid1", keyid1);
                addElementAttributes.setString(newRow, "keyid2", keyid2.length() > 0 ? keyid2 : "(null)");
                addElementAttributes.setString(newRow, "keyid3", keyid3.length() > 0 ? keyid3 : "(null)");
                addElementAttributes.setString(newRow, "attributeid", attributeid);
                addElementAttributes.setString(newRow, "attributesdcid", sdcid);
                addElementAttributes.setNumber(newRow, "attributeinstance", 0);
                addElementAttributes.setString(newRow, "attributesourcetype", "Adhoc");
                addElementAttributes.setString(newRow, "datatype", dataType);
                addElementAttributes.setObject(newRow, valueCol, attributeValue);
            }
        }
    }

    private void saveAttachments(String sdiRequestName, String sdcid, DataSet insertPrimaryDS, DataSet updatePrimaryDS, DataStore dataStore, SDIDataStore parentSDIDataStore, String tracelogid) throws ActionException {
        String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
        String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
        for (int i = 0; i < dataStore.getDataSet().getRowCount(); ++i) {
            if (insertPrimaryDS.size() == 1) {
                dataStore.getDataSet().setValue(-1, "keyid1", insertPrimaryDS.getString(0, keycolid1));
                if (keycolid2.length() > 0) {
                    dataStore.getDataSet().setValue(-1, "keyid2", insertPrimaryDS.getString(0, keycolid2));
                }
                if (keycolid3.length() <= 0) continue;
                dataStore.getDataSet().setValue(-1, "keyid3", insertPrimaryDS.getString(0, keycolid3));
                continue;
            }
            if (dataStore.getDataSet().getValue(i, "keyid1", "").length() != 0) continue;
            if (insertPrimaryDS.size() == 1) {
                dataStore.getDataSet().setValue(-1, "keyid1", insertPrimaryDS.getString(0, keycolid1));
                if (keycolid2.length() > 0) {
                    dataStore.getDataSet().setValue(-1, "keyid2", insertPrimaryDS.getString(0, keycolid2));
                }
                if (keycolid3.length() <= 0) continue;
                dataStore.getDataSet().setValue(-1, "keyid3", insertPrimaryDS.getString(0, keycolid3));
                continue;
            }
            if (updatePrimaryDS.size() == 1) {
                dataStore.getDataSet().setValue(-1, "keyid1", updatePrimaryDS.getString(0, keycolid1));
                if (keycolid2.length() > 0) {
                    dataStore.getDataSet().setValue(-1, "keyid2", updatePrimaryDS.getString(0, keycolid2));
                }
                if (keycolid3.length() <= 0) continue;
                dataStore.getDataSet().setValue(-1, "keyid3", updatePrimaryDS.getString(0, keycolid3));
                continue;
            }
            dataStore.getDataSet().setValue(-1, "keyid1", parentSDIDataStore.getDataStore("primary").getString(0, keycolid1));
            if (keycolid2.length() > 0) {
                dataStore.getDataSet().setValue(-1, "keyid2", parentSDIDataStore.getDataStore("primary").getString(0, keycolid2));
            }
            if (keycolid3.length() <= 0) continue;
            dataStore.getDataSet().setValue(-1, "keyid3", parentSDIDataStore.getDataStore("primary").getString(0, keycolid3));
        }
        DataSet newAttachments = dataStore.getInsertDataSet();
        DataSet editAttachments = dataStore.getUpdateDataSet();
        DataSet deleteAttachments = dataStore.getDeleteDataSet();
        if (newAttachments.size() > 0) {
            try {
                newAttachments.removeColumn("__rowstatus");
                newAttachments.addColumn("__rowstatus", 0);
                newAttachments.setValue(-1, "__rowstatus", "I");
                newAttachments.addColumn("typeflag", 0);
                newAttachments.setValue(-1, "typeflag", "F");
                FileManager.saveAttachmentData(sdcid, newAttachments, this.getConnectionId());
            }
            catch (Exception e) {
                throw new ActionException("Failed to add new attachments", e);
            }
        }
        if (editAttachments.size() > 0) {
            PropertyList actionProps = new PropertyList();
            for (int i = 0; i < editAttachments.size(); ++i) {
                actionProps.setProperty("sdcid", sdcid);
                actionProps.setProperty("keyid1", editAttachments.getString(i, "keyid1"));
                actionProps.setProperty("keyid2", editAttachments.getString(i, "keyid2"));
                actionProps.setProperty("keyid3", editAttachments.getString(i, "keyid3"));
                actionProps.setProperty("attachmentnum", editAttachments.getValue(i, "attachmentnum"));
                actionProps.setProperty("attachmentdesc", editAttachments.getValue(i, "attachmentdesc", "(null)"));
                actionProps.setProperty("attachmentclass", editAttachments.getValue(i, "attachmentclass", "(null)"));
                this.addExtraProps(sdiRequestName, actionProps, "EditSDIAttachment", tracelogid);
                this.getActionProcessor().processActionClass(EditSDIAttachment.class.getName(), actionProps);
            }
        }
        if (deleteAttachments.size() > 0) {
            for (int i = 0; i < deleteAttachments.size(); ++i) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", sdcid);
                if (deleteAttachments.getValue(i, "keyid1").length() <= 0) continue;
                actionProps.setProperty("keyid1", deleteAttachments.getString(i, "keyid1"));
                actionProps.setProperty("keyid2", deleteAttachments.getString(i, "keyid2"));
                actionProps.setProperty("keyid3", deleteAttachments.getString(i, "keyid3"));
                actionProps.setProperty("attachmentnum", deleteAttachments.getValue(i, "attachmentnum"));
                this.addExtraProps(sdiRequestName, actionProps, "DeleteSDIAttachment", tracelogid);
                this.getActionProcessor().processAction("DeleteSDIAttachment", "1", actionProps);
            }
        }
    }

    private void addExtraProp(String requestid, String actionid, String propertyid, String propertyvalue) {
        JsonArray props = this.extraProps.get(requestid);
        if (props == null) {
            props = new JsonArray();
            this.extraProps.put(requestid, props);
        }
        JsonObject prop = new JsonObject();
        props.put(prop);
        prop.put("actionid", actionid);
        prop.put("propertyid", propertyid);
        prop.put("propertyvalue", propertyvalue);
    }

    private String generateTracelogId(String sdcid) {
        boolean audited;
        String trace = "";
        if (this.audit.has("tracelogid") && this.audit.getString("tracelogid").length() > 0) {
            return this.audit.getString("tracelogid");
        }
        boolean bl = audited = !this.getSDCProcessor().getProperty(sdcid, "auditedflag").equalsIgnoreCase("N");
        if (audited) {
            String auditReason = this.audit.getString("reason", "");
            String auditActivity = this.audit.getString("activity");
            if (auditReason.length() > 0 || auditActivity.length() > 0) {
                String auditSignedFlag = this.audit.getBoolean("signed", false) ? "Y" : "N";
                long time = this.audit.getLong("time", 0L);
                String auditDate = "";
                M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                if (time > 0L) {
                    Calendar now = m18NUtil.getNowCalendar();
                    now.setTimeInMillis(time);
                    auditDate = m18NUtil.format(now);
                } else {
                    auditDate = m18NUtil.format(m18NUtil.getNowCalendar());
                }
                String auditDescription = this.audit.getString("description");
                SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
                sapphireConnection.setConnection(this.database.getConnection());
                AuditService auditService = new AuditService(sapphireConnection);
                try {
                    int tracelogid = Integer.parseInt(auditService.addTraceLogEntry(auditReason, auditActivity, auditSignedFlag, auditDate, auditDescription, false));
                    trace = String.valueOf(tracelogid);
                    if (auditActivity.length() > 0) {
                        auditService.setTracelogIdInDBSession(trace);
                    }
                }
                catch (Exception e) {
                    this.logger.error("Failed to generate trace log", e);
                }
            }
        }
        this.audit.put("tracelogid", trace);
        return trace;
    }
}


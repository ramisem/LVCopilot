/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.action;

import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import com.labvantage.sapphire.servlet.command.TagRequestPropertyHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.ActionConstants;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SaveSDI
extends BaseAction
implements ActionConstants {
    private Set<String> excludedFields = new HashSet<String>(Arrays.asList("createdt", "createby", "createtool", "moddt", "modby", "modtool", "auditsequence", "auditdeferflag", "tracelogid"));
    private String sdcid = "";
    private static final String WORKITEMID = "workitemid";
    private static final String WORKITEMVERSIONID = "workitemversionid";
    private static final String WORKITEMINSTANCE = "workiteminstance";
    private Set<String> changedKeys = new HashSet<String>();
    private String auditreason = "";
    private String auditsignedflag = "";
    private HashMap<String, PropertyList> sdcCache = new HashMap();

    private PropertyList getSDCProps(String sdcId) {
        PropertyList sdcProps = this.sdcCache.get(sdcId);
        if (sdcProps == null) {
            sdcProps = this.getSDCProcessor().getProperties(sdcId);
            this.sdcCache.put(sdcId, sdcProps);
        }
        return sdcProps;
    }

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String msg;
        JSONObject jFormdata;
        String sdcidIn = properties.getProperty("sdcid", "");
        if ("User".equals(sdcidIn)) {
            TagRequestPropertyHandler.isSDCOperationAllowed(this.getConnectionid(), sdcidIn, "EditSDI", properties, new HashMap());
        }
        String sFormdata = properties.getProperty("formdata");
        int newitemcount = Utils.s2i(properties.getProperty("newitemcount", "1"), 1);
        boolean doRelease = properties.getProperty("releaseonsave", "Y").equals("Y");
        boolean applyWorkItem = properties.getProperty("applyworkitem", "Y").equals("Y");
        this.auditreason = properties.getProperty("auditreason", "");
        this.auditsignedflag = properties.getProperty("auditsignedflag", "");
        String pageId = properties.getProperty("pageid", "");
        HttpSession session = (HttpSession)properties.get("session");
        PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
        this.sdcid = sdcidIn;
        PropertyList secondarySdcProps = this.getSecondarySDCProps(pageProps);
        String parentsdcid = "";
        String parentlinkid = "";
        String useParent = pageProps.getPropertyListNotNull("pagetype").getPropertyListNotNull("parent").getProperty("useparent", "Y");
        if (useParent.equals("Y")) {
            parentsdcid = pageProps.getPropertyListNotNull("pagetype").getPropertyListNotNull("parent").getProperty("parentsdcid", "");
            parentlinkid = pageProps.getPropertyListNotNull("pagetype").getPropertyListNotNull("parent").getProperty("parentlinkid", "");
        }
        PropertyListCollection postSaveActions = pageProps.getPropertyListNotNull("pagetype").getCollectionNotNull("postsaveactions");
        String saveAsTemplate = pageProps.getPropertyListNotNull("pagetype").getProperty("saveastemplate", "");
        String returncode = "OK";
        String newkeyid1 = "";
        String newkeyid2 = "";
        String newkeyid3 = "";
        String parentkeyid1 = "";
        String parentkeyid2 = "";
        int primarykeycols = Utils.s2i(this.getSDCProperty(this.sdcid, "keycolumns", "1"));
        String primarykeycol1 = this.getSDCProperty(this.sdcid, "keycolid1");
        String primarykeycol2 = "";
        String primarykeycol3 = "";
        if (primarykeycols > 1) {
            primarykeycol2 = this.getSDCProperty(this.sdcid, "keycolid2");
        }
        if (primarykeycols > 2) {
            primarykeycol3 = this.getSDCProperty(this.sdcid, "keycolid3");
        }
        String parentprimarykeycol1 = "";
        String parentprimarykeycol2 = "";
        int parentkeycols = 1;
        if (!parentsdcid.equals("")) {
            parentkeycols = Utils.s2i(this.getSDCProperty(parentsdcid, "keycolumns", "1"));
            parentprimarykeycol1 = this.getSDCProperty(parentsdcid, "keycolid1");
            if (parentkeycols > 1) {
                parentprimarykeycol2 = this.getSDCProperty(parentsdcid, "keycolid2");
            }
        }
        PropertyList parentAddSDI = new PropertyList();
        PropertyList parentEditSDI = new PropertyList();
        PropertyList secondaryAddSDI = new PropertyList();
        PropertyList primaryAddSDI = new PropertyList();
        PropertyList primaryEditSDI = new PropertyList();
        PropertyList addSDIWorkItem = new PropertyList();
        PropertyList editSDIWorkItem = new PropertyList();
        PropertyList deleteSDIWorkItem = new PropertyList();
        PropertyList addDataset = new PropertyList();
        PropertyList editDataset = new PropertyList();
        HashMap<String, PropertyList> deleteDataset = new HashMap<String, PropertyList>();
        PropertyList enterDataItem = new PropertyList();
        PropertyList editDataItem = new PropertyList();
        PropertyList extendDataSet = new PropertyList();
        PropertyList addReplicate = new PropertyList();
        PropertyList deleteDataItem = new PropertyList();
        PropertyList editDataApproval = new PropertyList();
        PropertyList addSDISpec = new PropertyList();
        PropertyList removeSDISpec = new PropertyList();
        ArrayList<PropertyList> addSDINotesList = new ArrayList<PropertyList>();
        PropertyList addSDIRole = new PropertyList();
        PropertyList deleteSDIRole = new PropertyList();
        PropertyList addCategory = new PropertyList();
        PropertyList deleteCategory = new PropertyList();
        PropertyList addSDIAddress = new PropertyList();
        PropertyList deleteSDIAddress = new PropertyList();
        HashMap<String, PropertyList> addSDIAttributeList = new HashMap<String, PropertyList>();
        HashMap<String, PropertyList> editSDIAttributeList = new HashMap<String, PropertyList>();
        HashMap<String, PropertyList> deleteSDIAttributeList = new HashMap<String, PropertyList>();
        PropertyList addControlCard = new PropertyList();
        PropertyList deleteControlCard = new PropertyList();
        LinkedHashMap<String, PropertyList> otherSaveOperations = new LinkedHashMap<String, PropertyList>();
        if (!sFormdata.equals("")) {
            try {
                jFormdata = new JSONObject(sFormdata);
            }
            catch (Exception e) {
                this.logger.error("Exception while parsing:  " + sFormdata, e);
                throw new SapphireException("Failed to parse form data from JSON object", e);
            }
        } else {
            jFormdata = new JSONObject();
        }
        try {
            JSONArray secondaryDataset;
            JSONArray primaryDataset = jFormdata.optJSONArray("primary");
            if (primaryDataset != null) {
                String newkeyids = this.collectPrimaryData(primaryAddSDI, primaryEditSDI, primaryDataset, primarykeycol1, primarykeycol2, primarykeycol3, parentlinkid);
                String[] arrNewkeyids = newkeyids.split("\\|");
                newkeyid1 = arrNewkeyids[0];
                if (arrNewkeyids.length > 1) {
                    newkeyid2 = arrNewkeyids[1];
                }
                if (arrNewkeyids.length > 2) {
                    newkeyid3 = arrNewkeyids[2];
                }
                this.appendChangedIds(newkeyid1, newkeyid2, newkeyid3);
            }
            if ((secondaryDataset = jFormdata.optJSONArray("secondary")) != null) {
                this.collectPrimaryData(secondaryAddSDI, secondaryAddSDI, secondaryDataset, secondarySdcProps.getProperty("keycol1", ""), secondarySdcProps.getProperty("keycol2", ""), secondarySdcProps.getProperty("keycol3", ""), "");
            }
            Iterator i1 = jFormdata.keys();
            while (i1.hasNext()) {
                PropertyList elementProps;
                JSONArray jDataSetRows;
                String key = (String)i1.next();
                if (key.equals("parent")) {
                    String parentLinkField = "";
                    DataSet linkData = this.getSDCProcessor().getLinksData(this.sdcid);
                    for (int i = 0; i < linkData.getRowCount(); ++i) {
                        if (!linkData.getString(i, "linkid", "").equalsIgnoreCase(parentlinkid)) continue;
                        parentLinkField = linkData.getString(i, "sdccolumnid", "");
                        break;
                    }
                    JSONArray parentDataset = jFormdata.optJSONArray(key);
                    String parentkeyids = this.collectPrimaryData(parentAddSDI, parentEditSDI, parentDataset, parentprimarykeycol1, parentprimarykeycol2, "", parentLinkField);
                    String[] arrNewkeyids = parentkeyids.split("\\|");
                    parentkeyid1 = arrNewkeyids[0];
                    if (arrNewkeyids.length <= 1) continue;
                    parentkeyid2 = arrNewkeyids[1];
                    continue;
                }
                if (key.equals("sdidataitem")) {
                    JSONArray jDataItemRows = jFormdata.optJSONArray(key);
                    this.collectDataItemData(jDataItemRows, enterDataItem, editDataItem, extendDataSet, addReplicate, deleteDataItem);
                    continue;
                }
                if (key.equals("sdidata")) {
                    jDataSetRows = jFormdata.optJSONArray(key);
                    this.collectDataSetData(jDataSetRows, addDataset, editDataset, deleteDataset);
                    continue;
                }
                if (key.equals("sdiworkitem")) {
                    jDataSetRows = jFormdata.optJSONArray(key);
                    this.collectWorkitemData(jDataSetRows, addSDIWorkItem, editSDIWorkItem, deleteSDIWorkItem);
                    continue;
                }
                if (key.equals("sdispec")) {
                    JSONArray jSpecRows = jFormdata.optJSONArray(key);
                    this.collectSpecData(jSpecRows, addSDISpec, removeSDISpec);
                    continue;
                }
                if (key.equals("sdidataapproval")) {
                    JSONArray jApprovalRows = jFormdata.optJSONArray(key);
                    this.collectApprovalData(jApprovalRows, editDataApproval);
                    continue;
                }
                if (key.equals("sdinotes")) {
                    String sSdinotes = jFormdata.optString(key);
                    this.collectSDINotes(sSdinotes, addSDINotesList);
                    continue;
                }
                if (key.equals("sdirole")) {
                    JSONArray jRoleRows = jFormdata.optJSONArray(key);
                    this.collectRoleData(jRoleRows, addSDIRole, deleteSDIRole);
                    continue;
                }
                if (key.equals("category")) {
                    JSONArray jCategoryRows = jFormdata.optJSONArray(key);
                    this.collectCategoryData(jCategoryRows, addCategory, deleteCategory);
                    continue;
                }
                if (key.equals("sdiaddress")) {
                    JSONArray jAddressRows = jFormdata.optJSONArray(key);
                    this.collectAddressData(jAddressRows, addSDIAddress, deleteSDIAddress);
                    continue;
                }
                if (key.equals("sdicontrolcard")) {
                    JSONArray jControlCardRows = jFormdata.optJSONArray(key);
                    this.collectControlCardData(jControlCardRows, addControlCard, deleteControlCard);
                    continue;
                }
                if (key.equals("attribute")) {
                    JSONArray jAttributeRows = jFormdata.optJSONArray(key);
                    this.collectAttributeData(jAttributeRows, addSDIAttributeList, editSDIAttributeList, deleteSDIAttributeList);
                    continue;
                }
                if (key.equals("sdiattachment") || key.equals("primary") || key.equals("secondary") || (elementProps = key.endsWith("m2m") ? pageProps.getPropertyList(key.substring(0, key.length() - 3)) : pageProps.getPropertyList(key)) == null) continue;
                String linktype = elementProps.getProperty("linktype", "");
                String linkSdcId = elementProps.getProperty("linksdcid", "");
                String linkColumnId = elementProps.getProperty("linkcolumnid", "");
                String linkColumnId2 = elementProps.getProperty("linkcolumnid2", "");
                String linkId = elementProps.getProperty("linkid", "");
                if (linkId.equals("")) {
                    linkId = elementProps.getPropertyListNotNull("detailcollection").getProperty("detaillink", "");
                }
                if (linktype.startsWith("reversefk:")) {
                    this.collectReverseFKData(key, linkSdcId, linkColumnId, linkColumnId2, jFormdata.optJSONArray(key), otherSaveOperations);
                    continue;
                }
                if (!linktype.startsWith("sdclink:")) continue;
                this.collectSDIDetailData(key, linkId, jFormdata.optJSONArray(key), otherSaveOperations, newkeyid1, newkeyid2);
            }
            if (!parentsdcid.equals("")) {
                if (!parentEditSDI.isEmpty()) {
                    parentEditSDI.setProperty("sdcid", parentsdcid);
                    parentEditSDI.setProperty("propsmatch", "Y");
                    this.processSystemAction("EditSDI", "1", parentEditSDI);
                    parentkeyid1 = parentEditSDI.getProperty("keyid1");
                    if (parentkeycols > 1) {
                        parentkeyid2 = parentEditSDI.getProperty("keyid2");
                    }
                }
                if (!parentAddSDI.isEmpty()) {
                    parentAddSDI.setProperty("sdcid", parentsdcid);
                    if (parentAddSDI.containsKey("keyid1")) {
                        parentAddSDI.setProperty("keyid1", parentAddSDI.getProperty("keyid1", "").replaceAll("\\[keyid1\\]", ""));
                    }
                    this.processSystemAction("AddSDI", "1", parentAddSDI);
                    parentkeyid1 = parentAddSDI.getProperty("newkeyid1");
                    if (parentkeycols > 1) {
                        parentkeyid2 = parentAddSDI.getProperty("newkeyid2");
                    }
                }
            }
            if (!secondaryAddSDI.isEmpty()) {
                if (secondarySdcProps.getProperty("sdcid").equals("Category")) {
                    secondaryAddSDI.setProperty("keyid2", this.sdcid);
                }
                secondaryAddSDI.setProperty("sdcid", secondarySdcProps.getProperty("sdcid"));
                this.processSystemAction("AddSDI", "1", secondaryAddSDI);
            }
            if (!primaryEditSDI.isEmpty()) {
                primaryEditSDI.setProperty("sdcid", this.sdcid);
                primaryEditSDI.setProperty("propsmatch", "Y");
                this.processSystemAction("EditSDI", "1", primaryEditSDI);
                newkeyid1 = primaryEditSDI.getProperty("keyid1");
                newkeyid2 = primaryEditSDI.getProperty("keyid2", "");
                newkeyid3 = primaryEditSDI.getProperty("keyid3", "");
            }
            if (!primaryAddSDI.isEmpty()) {
                primaryAddSDI.setProperty("sdcid", this.sdcid);
                if (newitemcount > 1) {
                    primaryAddSDI.setProperty("copies", "" + newitemcount);
                }
                if (saveAsTemplate.equals("Y")) {
                    primaryAddSDI.setProperty("overrideautokey", "Y");
                    primaryAddSDI.setProperty("templateflag", "Y");
                }
                if (!parentkeyid1.equals("")) {
                    DataSet linkData = this.getSDCLinksData(this.sdcid);
                    for (int i = 0; i < linkData.getRowCount(); ++i) {
                        if (!linkData.getString(i, "linkid", "").equalsIgnoreCase(parentlinkid)) continue;
                        String linkcolumnid = linkData.getString(i, "sdccolumnid", "");
                        String linkcolumnid2 = linkData.getString(i, "sdccolumnid2", "");
                        int numItems = Utils.s2i(primaryAddSDI.getProperty("copies"), 1);
                        Utils.appendToPL(primaryAddSDI, linkcolumnid, parentkeyid1, numItems);
                        if (linkcolumnid2.equals("")) break;
                        Utils.appendToPL(primaryAddSDI, linkcolumnid2, parentkeyid2, numItems);
                        break;
                    }
                }
                this.processSystemAction("AddSDI", "1", primaryAddSDI);
                newkeyid1 = primaryAddSDI.getProperty("newkeyid1");
                newkeyid2 = Utils.notNull(primaryAddSDI.getProperty("newkeyid2"));
                newkeyid3 = Utils.notNull(primaryAddSDI.getProperty("newkeyid3"));
            }
            this.saveWorkItemData(newitemcount, newkeyid1, newkeyid2, newkeyid3, addSDIWorkItem, editSDIWorkItem, deleteSDIWorkItem, applyWorkItem);
            this.saveParamListData(newitemcount, newkeyid1, newkeyid2, newkeyid3, addDataset, editDataset, deleteDataset);
            this.saveSpecData(newitemcount, newkeyid1, newkeyid2, newkeyid3, addSDISpec, removeSDISpec);
            this.saveApprovalData(newitemcount, newkeyid1, newkeyid2, newkeyid3, editDataApproval);
            this.saveResultData(newitemcount, doRelease, newkeyid1, newkeyid2, newkeyid3, enterDataItem, editDataItem, extendDataSet, addReplicate, deleteDataItem);
            this.saveRoleData(newkeyid1, newkeyid2, newkeyid3, addSDIRole, deleteSDIRole);
            this.saveCategoryData(newkeyid1, newkeyid2, newkeyid3, addCategory, deleteCategory);
            this.saveAddressData(newkeyid1, newkeyid2, newkeyid3, addSDIAddress, deleteSDIAddress);
            this.saveControlCardData(newkeyid1, newkeyid2, newkeyid3, addControlCard, deleteControlCard);
            this.saveAttributeData(newkeyid1, newkeyid2, newkeyid3, addSDIAttributeList, editSDIAttributeList, deleteSDIAttributeList);
            this.saveLinkData(newkeyid1, newkeyid2, newkeyid3, otherSaveOperations);
            this.saveSDINotesData(newkeyid1, newkeyid2, newkeyid3, addSDINotesList);
            this.runPostSaveActions(newkeyid1, newkeyid2, newkeyid3, postSaveActions);
            String currDate = this.getConnectionProcessor().isOra() ? "sysdate" : "getDate()";
            String sql = "update connection set lastaccesseddt=" + currDate + " where connectionid=?";
            this.getQueryProcessor().execPreparedUpdate(sql, new Object[]{this.getConnectionId()});
            msg = this.getTranslationProcessor().translate("Save OK");
        }
        catch (SapphireException e) {
            String timestamp = new SimpleDateFormat().format(new Date());
            String userid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId();
            this.logger.error("ERROR " + userid, e);
            returncode = "ERROR";
            msg = timestamp + " " + userid + "<br>" + this.getTranslationProcessor().translate("Error") + ":" + e.getMessage();
        }
        properties.setProperty("msg", msg);
        properties.setProperty("returncode", returncode);
        properties.setProperty("newkeyid1", newkeyid1);
        properties.setProperty("newkeyid2", newkeyid2);
        properties.setProperty("newkeyid3", newkeyid3);
    }

    private PropertyList getSecondarySDCProps(PropertyList pageProps) {
        PropertyList retVal = new PropertyList();
        for (Object key : pageProps.keySet()) {
            String elementId = (String)key;
            PropertyList elementConfig = pageProps.getPropertyList(elementId);
            if (elementConfig == null || !elementConfig.getProperty("type", "").equals("multimaint")) continue;
            PropertyList collectionConfig = elementConfig.getPropertyListNotNull("detailcollection");
            String detailCollectionType = collectionConfig.getProperty("detailcollectiontype", "sdidetail");
            String detailLink = collectionConfig.getProperty("detaillink", "");
            String detailCollectionItem = collectionConfig.getProperty("detailcollectionitem", "(none)");
            if (detailCollectionType.equals("many-to-many")) {
                String sql = "SELECT sdclink.linksdcid, sdc.tableid FROM sdclink JOIN sdc on sdclink.linksdcid=sdc.sdcid WHERE sdclink.sdcid=? and sdclink.linkid=?";
                Object[] params = new String[]{this.sdcid, detailLink};
                DataSet linkDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                String detailSdcId = linkDs.getString(0, "linksdcid", "");
                retVal.setProperty("sdcid", detailSdcId);
                retVal.setProperty("keycol1", this.getSDCProcessor().getProperty(detailSdcId, "keycolid1", ""));
                retVal.setProperty("keycol2", this.getSDCProcessor().getProperty(detailSdcId, "keycolid2", ""));
                continue;
            }
            if (!detailCollectionItem.equals("(none)") && !detailCollectionItem.equals("category")) {
                if (detailCollectionItem.equals("sdiworkitem")) {
                    retVal.setProperty("sdcid", "WorkItem");
                    retVal.setProperty("keycol1", WORKITEMID);
                    retVal.setProperty("keycol2", WORKITEMVERSIONID);
                    continue;
                }
                if (detailCollectionItem.equals("sdidata")) {
                    retVal.setProperty("sdcid", "ParamList");
                    retVal.setProperty("keycol1", "paramlistid");
                    retVal.setProperty("keycol2", "paramlistversionid");
                    retVal.setProperty("keycol3", "variantid");
                    continue;
                }
                if (detailCollectionItem.equals("sdispec")) {
                    retVal.setProperty("sdcid", "SpecSDC");
                    retVal.setProperty("keycol1", "specid");
                    retVal.setProperty("keycol2", "specversionid");
                    continue;
                }
                if (!detailCollectionItem.equals("sdirole")) continue;
                retVal.setProperty("sdcid", "Role");
                retVal.setProperty("keycol1", "roleid");
                continue;
            }
            if (!detailCollectionItem.equals("category")) continue;
            retVal.setProperty("sdcid", "Category");
            retVal.setProperty("keycol1", "categoryid");
        }
        return retVal;
    }

    private void saveControlCardData(String newkeyid1, String newkeyid2, String newkeyid3, PropertyList addControlCard, PropertyList deleteControlCard) throws ActionException {
        if (!addControlCard.isEmpty()) {
            addControlCard.setProperty("key1", addControlCard.getProperty("key1", "").replaceAll("\\[keyid1\\]", newkeyid1));
            addControlCard.setProperty("key2", addControlCard.getProperty("key2", "").replaceAll("\\[keyid2\\]", newkeyid2));
            addControlCard.setProperty("key3", addControlCard.getProperty("key3", "").replaceAll("\\[keyid3\\]", newkeyid3));
            if (addControlCard.getProperty("keyid2").replaceAll(";", "").equals("")) {
                addControlCard.remove("keyid2");
            }
            if (addControlCard.getProperty("keyid3").replaceAll(";", "").equals("")) {
                addControlCard.remove("keyid3");
            }
            addControlCard.setProperty("propsmatch", "Y");
            this.processSystemAction("AddSDI", "1", addControlCard);
        }
        if (!deleteControlCard.isEmpty()) {
            this.processSystemAction("DeleteSDI", "1", deleteControlCard);
        }
    }

    private void saveApprovalData(int newitemcount, String newkeyid1, String newkeyid2, String newkeyid3, PropertyList editDataApproval) throws ActionException {
        if (!editDataApproval.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(editDataApproval, newitemcount, true);
            }
            editDataApproval.setProperty("sdcid", this.sdcid);
            editDataApproval.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, editDataApproval);
            this.processSystemAction("EditDataApproval", "1", editDataApproval);
        }
    }

    private void runPostSaveActions(String newkeyid1, String newkeyid2, String newkeyid3, PropertyListCollection postSaveActions) throws SapphireException {
        for (int i = 0; i < postSaveActions.size(); ++i) {
            PropertyList postSaveActionProps = postSaveActions.getPropertyList(i);
            String actionid = postSaveActionProps.getProperty("actionid", "");
            String actionversionid = postSaveActionProps.getProperty("actionversionid", "1");
            PropertyList psActionProps = new PropertyList();
            psActionProps.setProperty("sdcid", this.sdcid);
            String keyid1 = this.extractChangedKeyids(1);
            String keyid2 = this.extractChangedKeyids(2);
            String keyid3 = this.extractChangedKeyids(3);
            if (keyid1.equals("")) break;
            psActionProps.setProperty("keyid1", keyid1.replaceAll("\\[keyid1\\]", newkeyid1));
            if (!keyid2.equals("")) {
                psActionProps.setProperty("keyid2", keyid2.replaceAll("\\[keyid2\\]", newkeyid2));
            }
            if (!keyid3.equals("")) {
                psActionProps.setProperty("keyid3", keyid3.replaceAll("\\[keyid3\\]", newkeyid3));
            }
            if (actionid.equals("")) continue;
            this.processSystemAction(actionid, actionversionid, psActionProps);
        }
    }

    private void saveSDINotesData(String newkeyid1, String newkeyid2, String newkeyid3, List<PropertyList> addSDINotesList) throws ActionException {
        if (!addSDINotesList.isEmpty()) {
            for (PropertyList addSDINote : addSDINotesList) {
                addSDINote.setProperty("sdcid", this.sdcid);
                addSDINote.setProperty("keyid1", addSDINote.getProperty("keyid1").replaceAll("\\[keyid1\\]", newkeyid1));
                addSDINote.setProperty("keyid2", addSDINote.getProperty("keyid2").replaceAll("\\[keyid2\\]", newkeyid2));
                addSDINote.setProperty("keyid3", addSDINote.getProperty("keyid3").replaceAll("\\[keyid3\\]", newkeyid3));
                this.processSystemAction("AddSDINote", "1", addSDINote);
            }
        }
    }

    private void saveLinkData(String newkeyid1, String newkeyid2, String newkeyid3, Map<String, PropertyList> otherSaveOperations) throws ActionException {
        for (Map.Entry<String, PropertyList> entry : otherSaveOperations.entrySet()) {
            String originalColumnValue2;
            String linkcolumn2;
            String originalColumnValue1;
            String key = entry.getKey();
            String[] temp = key.split(";");
            String action = temp[1];
            PropertyList props = entry.getValue();
            String linkcolumn1 = (String)props.remove("__linkcolumn1");
            if (linkcolumn1 != null && (originalColumnValue1 = props.getProperty(linkcolumn1)) != null && originalColumnValue1.contains("[parentkeyid1]")) {
                props.setProperty(linkcolumn1, originalColumnValue1.replaceAll("\\[parentkeyid1\\]", newkeyid1));
            }
            if ((linkcolumn2 = (String)props.remove("__linkcolumn2")) != null && (originalColumnValue2 = props.getProperty(linkcolumn2)) != null && originalColumnValue2.contains("[parentkeyid2]")) {
                props.setProperty(linkcolumn2, originalColumnValue2.replaceAll("\\[parentkeyid2\\]", newkeyid2));
            }
            if (action.equals("AddSDIDetail")) {
                this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, props);
            } else if (action.equals("AddSDI")) {
                this.replaceKeyIds("", "", "", props);
            }
            this.processSystemAction(action, "1", props);
        }
    }

    private void saveAttributeData(String newkeyid1, String newkeyid2, String newkeyid3, Map<String, PropertyList> addSDIAttributeList, Map<String, PropertyList> editSDIAttributeList, Map<String, PropertyList> deleteSDIAttributeList) throws ActionException {
        for (PropertyList addSDIAttribute : addSDIAttributeList.values()) {
            addSDIAttribute.setProperty("sdcid", this.sdcid);
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, addSDIAttribute);
            this.processSystemAction("AddSDIAttribute", "1", addSDIAttribute);
        }
        for (PropertyList editSDIAttribute : editSDIAttributeList.values()) {
            editSDIAttribute.setProperty("sdcid", this.sdcid);
            this.processSystemAction("EditSDIAttribute", "1", editSDIAttribute);
        }
        for (PropertyList deleteSDIAttribute : deleteSDIAttributeList.values()) {
            deleteSDIAttribute.setProperty("sdcid", this.sdcid);
            this.processSystemAction("DeleteSDIAttribute", "1", deleteSDIAttribute);
        }
    }

    private void saveAddressData(String newkeyid1, String newkeyid2, String newkeyid3, PropertyList addSDIAddress, PropertyList deleteSDIAddress) throws ActionException {
        if (!addSDIAddress.isEmpty()) {
            addSDIAddress.setProperty("sdcid", this.sdcid);
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, addSDIAddress);
            addSDIAddress.setProperty("propsmatch", "Y");
            this.processSystemAction("AddSDIAddress", "1", addSDIAddress);
        }
        if (!deleteSDIAddress.isEmpty()) {
            deleteSDIAddress.setProperty("sdcid", this.sdcid);
            this.processSystemAction("DeleteSDIAddress", "1", deleteSDIAddress);
        }
    }

    private void removeDuplicates(PropertyList actionProps, String detailColumn) {
        String[] keyid1Arr = actionProps.getProperty("keyid1").split(";");
        String[] detailColumnArr = actionProps.getProperty(detailColumn).split(";");
        HashSet<String> uniqueKeys = new HashSet<String>();
        StringBuilder uniqueKeyid1 = new StringBuilder();
        StringBuilder uniqueDetail = new StringBuilder();
        for (int i = 0; i < keyid1Arr.length; ++i) {
            String keyid1 = keyid1Arr[i];
            String detail = detailColumnArr[i];
            if (uniqueKeys.contains(keyid1 + ";" + detail)) continue;
            uniqueKeyid1.append(";").append(keyid1);
            uniqueDetail.append(";").append(detail);
            uniqueKeys.add(keyid1 + ";" + detail);
        }
        actionProps.setProperty("keyid1", uniqueKeyid1.substring(1));
        actionProps.setProperty(detailColumn, uniqueDetail.substring(1));
    }

    private void saveCategoryData(String newkeyid1, String newkeyid2, String newkeyid3, PropertyList addCategory, PropertyList deleteCategory) throws ActionException {
        if (!addCategory.isEmpty()) {
            addCategory.setProperty("sdcid", this.sdcid);
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, addCategory);
            this.removeDuplicates(addCategory, "categoryid");
            addCategory.setProperty("propsmatch", "Y");
            this.processSystemAction("AddCategoryItem", "1", addCategory);
        }
        if (!deleteCategory.isEmpty()) {
            deleteCategory.setProperty("sdcid", this.sdcid);
            deleteCategory.setProperty("propsmatch", "Y");
            this.processSystemAction("DeleteCategoryItem", "1", deleteCategory);
        }
    }

    private void saveRoleData(String newkeyid1, String newkeyid2, String newkeyid3, PropertyList addSDIRole, PropertyList deleteSDIRole) throws ActionException {
        if (!addSDIRole.isEmpty()) {
            addSDIRole.setProperty("sdcid", this.sdcid);
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, addSDIRole);
            this.removeDuplicates(addSDIRole, "roleid");
            addSDIRole.setProperty("propsmatch", "Y");
            this.processSystemAction("AddSDIRole", "1", addSDIRole);
        }
        if (!deleteSDIRole.isEmpty()) {
            deleteSDIRole.setProperty("sdcid", this.sdcid);
            deleteSDIRole.setProperty("propsmatch", "Y");
            this.processSystemAction("DeleteSDIRole", "1", deleteSDIRole);
        }
    }

    private void saveResultData(int newitemcount, boolean doRelease, String newkeyid1, String newkeyid2, String newkeyid3, PropertyList enterDataItem, PropertyList editDataItem, PropertyList extendDataSet, PropertyList addReplicate, PropertyList deleteDataItem) throws ActionException {
        if (!extendDataSet.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(extendDataSet, newitemcount, true);
            }
            extendDataSet.setProperty("sdcid", this.sdcid);
            extendDataSet.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, extendDataSet);
            this.processSystemAction("ExtendDataSet", "1", extendDataSet);
        }
        if (!addReplicate.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(addReplicate, newitemcount, true);
            }
            addReplicate.setProperty("sdcid", this.sdcid);
            addReplicate.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, addReplicate);
            this.processSystemAction("AddReplicate", "1", addReplicate);
        }
        if (!editDataItem.isEmpty() && doRelease) {
            if (newitemcount > 1) {
                Utils.multiplyItems(editDataItem, newitemcount, true);
            }
            editDataItem.setProperty("sdcid", this.sdcid);
            editDataItem.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, editDataItem);
            this.processSystemAction("EditDataItem", "1", editDataItem);
        }
        if (!enterDataItem.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(enterDataItem, newitemcount, true);
            }
            enterDataItem.setProperty("sdcid", this.sdcid);
            enterDataItem.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, enterDataItem);
            enterDataItem.setProperty("autorelease", doRelease ? "Y" : "N");
            this.processSystemAction("EnterDataItem", "1", enterDataItem);
        }
        if (!editDataItem.isEmpty() && !doRelease) {
            if (newitemcount > 1) {
                Utils.multiplyItems(editDataItem, newitemcount, true);
            }
            editDataItem.setProperty("sdcid", this.sdcid);
            editDataItem.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, editDataItem);
            this.processSystemAction("EditDataItem", "1", editDataItem);
        }
        if (!deleteDataItem.isEmpty()) {
            deleteDataItem.setProperty("sdcid", this.sdcid);
            deleteDataItem.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, deleteDataItem);
            this.processSystemAction("DeleteDataItem", "1", deleteDataItem);
        }
    }

    private void saveSpecData(int newitemcount, String newkeyid1, String newkeyid2, String newkeyid3, PropertyList addSDISpec, PropertyList removeSDISpec) throws ActionException {
        if (!removeSDISpec.isEmpty()) {
            removeSDISpec.setProperty("sdcid", this.sdcid);
            this.processSystemAction("RemoveSDISpec", "1", removeSDISpec);
        }
        if (!addSDISpec.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(addSDISpec, newitemcount, true);
            }
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, addSDISpec);
            String[] keyids = addSDISpec.getProperty("keyid1").split(";");
            String[] specids = addSDISpec.getProperty("specid").split(";");
            String[] specversionids = addSDISpec.getProperty("specversionid").split(";");
            LinkedHashMap<String, PropertyList> addSDISpecGroup = new LinkedHashMap<String, PropertyList>();
            for (int i = 0; i < keyids.length; ++i) {
                String keyid1 = keyids[i];
                String specid = specids[i];
                String specversionid = specversionids[i];
                PropertyList actionprops = (PropertyList)addSDISpecGroup.get(specid + ";" + specversionid);
                if (actionprops == null) {
                    actionprops = new PropertyList();
                    actionprops.setProperty("sdcid", this.sdcid);
                    actionprops.setProperty("specid", specid);
                    actionprops.setProperty("specversionid", specversionid);
                }
                Utils.appendToPL(actionprops, "keyid1", keyid1);
                addSDISpecGroup.put(specid + ";" + specversionid, actionprops);
            }
            for (Map.Entry entry : addSDISpecGroup.entrySet()) {
                PropertyList actionprops = (PropertyList)entry.getValue();
                this.processSystemAction("AddSDISpec", "1", actionprops);
            }
        }
    }

    private void saveParamListData(int newitemcount, String newkeyid1, String newkeyid2, String newkeyid3, PropertyList addDataset, PropertyList editDataset, Map<String, PropertyList> deleteDatasets) throws ActionException {
        if (!addDataset.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(addDataset, newitemcount, true);
            }
            addDataset.setProperty("sdcid", this.sdcid);
            addDataset.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, addDataset);
            this.processSystemAction("AddDataSet", "1", addDataset);
        }
        if (!editDataset.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(editDataset, newitemcount, true);
            }
            editDataset.setProperty("sdcid", this.sdcid);
            editDataset.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, editDataset);
            this.processSystemAction("EditDataSet", "1", editDataset);
        }
        if (!deleteDatasets.isEmpty()) {
            for (PropertyList deleteDataset : deleteDatasets.values()) {
                if (deleteDataset.isEmpty()) continue;
                deleteDataset.setProperty("sdcid", this.sdcid);
                this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, deleteDataset);
                this.processSystemAction("DeleteDataSet", "1", deleteDataset);
            }
        }
    }

    private void saveWorkItemData(int newitemcount, String newkeyid1, String newkeyid2, String newkeyid3, PropertyList addSDIWorkItem, PropertyList editSDIWorkItem, PropertyList deleteSDIWorkItem, boolean applyWorkItem) throws ActionException {
        if (!addSDIWorkItem.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(addSDIWorkItem, newitemcount, true);
            }
            addSDIWorkItem.setProperty("sdcid", this.sdcid);
            addSDIWorkItem.setProperty("propsmatch", "Y");
            addSDIWorkItem.setProperty("applyworkitem", applyWorkItem ? "Y" : "N");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, addSDIWorkItem);
            String[] arrKeyid1 = newkeyid1.split(";");
            for (int i = 0; addSDIWorkItem.getProperty("keyid1").contains("[keyid1#" + i + "]") && arrKeyid1.length > i; ++i) {
                addSDIWorkItem.setProperty("keyid1", addSDIWorkItem.getProperty("keyid1").replaceAll("\\[keyid1#" + i + "\\]", arrKeyid1[i]));
            }
            this.processSystemAction("AddSDIWorkItem", "1", addSDIWorkItem);
        }
        if (!editSDIWorkItem.isEmpty()) {
            if (newitemcount > 1) {
                Utils.multiplyItems(editSDIWorkItem, newitemcount, true);
            }
            editSDIWorkItem.setProperty("sdcid", this.sdcid);
            editSDIWorkItem.setProperty("propsmatch", "Y");
            this.replaceKeyIds(newkeyid1, newkeyid2, newkeyid3, editSDIWorkItem);
            this.processSystemAction("EditSDIWorkItem", "1", editSDIWorkItem);
        }
        if (!deleteSDIWorkItem.isEmpty()) {
            deleteSDIWorkItem.setProperty("sdcid", this.sdcid);
            deleteSDIWorkItem.setProperty("propsmatch", "Y");
            deleteSDIWorkItem.setProperty("cascadedeletes", "Y");
            deleteSDIWorkItem.setProperty("keyid1", deleteSDIWorkItem.getProperty("keyid1").replaceAll("\\[keyid1\\]", newkeyid1));
            this.processSystemAction("DeleteSDIWorkItem", "1", deleteSDIWorkItem);
        }
    }

    private void replaceKeyIds(String keyid1, String keyid2, String keyid3, PropertyList propertyList) {
        propertyList.setProperty("keyid1", propertyList.getProperty("keyid1").replaceAll("\\[keyid1\\]", keyid1));
        propertyList.setProperty("keyid2", propertyList.getProperty("keyid2").replaceAll("\\[keyid2\\]", keyid2));
        propertyList.setProperty("keyid3", propertyList.getProperty("keyid3").replaceAll("\\[keyid3\\]", keyid3));
        if (propertyList.getProperty("keyid2").replaceAll(";", "").equals("")) {
            propertyList.remove("keyid2");
        }
        if (propertyList.getProperty("keyid3").replaceAll(";", "").equals("")) {
            propertyList.remove("keyid3");
        }
    }

    protected void collectRoleData(JSONArray rows, PropertyList addSDIRole, PropertyList deleteSDIRole) {
        for (int i = 0; i < rows.length(); ++i) {
            JSONObject detailRow = rows.optJSONObject(i);
            String rowstatus = detailRow.optString("__rowstatus", "");
            String roleid = detailRow.optString("roleid");
            String keyid1 = detailRow.optString("keyid1", "[keyid1]");
            String keyid2 = detailRow.optString("keyid2");
            if (rowstatus.equals("A")) {
                Utils.appendToPL(addSDIRole, "roleid", roleid);
                Utils.appendToPL(addSDIRole, "keyid1", keyid1);
                if (!keyid2.equals("")) {
                    Utils.appendToPL(addSDIRole, "keyid2", keyid2);
                }
                Utils.appendToPL(addSDIRole, "privid", "list");
            }
            if (!rowstatus.equals("D")) continue;
            Utils.appendToPL(deleteSDIRole, "roleid", roleid);
            Utils.appendToPL(deleteSDIRole, "keyid1", keyid1);
            if (!keyid2.equals("")) {
                Utils.appendToPL(deleteSDIRole, "keyid2", keyid2);
            }
            Utils.appendToPL(deleteSDIRole, "privid", "list");
        }
    }

    protected void collectCategoryData(JSONArray rows, PropertyList addCategory, PropertyList deleteCategory) {
        for (int i = 0; i < rows.length(); ++i) {
            JSONObject detailRow = rows.optJSONObject(i);
            String rowstatus = detailRow.optString("__rowstatus", "");
            String roleid = detailRow.optString("categoryid");
            String keyid1 = detailRow.optString("keyid1", "[keyid1]");
            String keyid2 = detailRow.optString("keyid2");
            if (rowstatus.equals("A")) {
                Utils.appendToPL(addCategory, "categoryid", roleid);
                Utils.appendToPL(addCategory, "keyid1", keyid1);
                if (!keyid2.equals("")) {
                    Utils.appendToPL(addCategory, "keyid2", keyid2);
                }
            }
            if (!rowstatus.equals("D")) continue;
            Utils.appendToPL(deleteCategory, "categoryid", roleid);
            Utils.appendToPL(deleteCategory, "keyid1", keyid1);
            if (keyid2.equals("")) continue;
            Utils.appendToPL(deleteCategory, "keyid2", keyid2);
        }
    }

    protected void collectAddressData(JSONArray rows, PropertyList addSDIAddress, PropertyList deleteSDIAddress) {
        HashSet<String> prevAddresses = new HashSet<String>();
        for (int i = 0; i < rows.length(); ++i) {
            JSONObject detailRow = rows.optJSONObject(i);
            String rowstatus = detailRow.optString("__rowstatus", "");
            String addressid = detailRow.optString("addressid");
            String addresstype = detailRow.optString("addresstype");
            String contactfunction = detailRow.optString("contactfunction", "Standard");
            String usersequence = detailRow.optString("usersequence", "");
            String keyid1 = detailRow.optString("keyid1", "[keyid1]");
            String keyid2 = detailRow.optString("keyid2");
            String keyid3 = detailRow.optString("keyid3");
            if (rowstatus.equals("A") && !prevAddresses.contains(addressid + ";" + addresstype + ";" + keyid1 + ";" + keyid2 + ";" + keyid3)) {
                Utils.appendToPL(addSDIAddress, "addressid", addressid);
                Utils.appendToPL(addSDIAddress, "addresstype", addresstype);
                Utils.appendToPL(addSDIAddress, "contactfunction", contactfunction);
                Utils.appendToPL(addSDIAddress, "keyid1", keyid1);
                if (!keyid2.equals("")) {
                    Utils.appendToPL(addSDIAddress, "keyid2", keyid2);
                }
                if (!keyid3.equals("")) {
                    Utils.appendToPL(addSDIAddress, "keyid3", keyid3);
                }
                Utils.appendToPL(addSDIAddress, "usersequence", usersequence);
                prevAddresses.add(addressid + ";" + addresstype + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
            }
            if (!rowstatus.equals("D")) continue;
            Utils.appendToPL(deleteSDIAddress, "addressid", addressid);
            Utils.appendToPL(deleteSDIAddress, "addresstype", addresstype);
            Utils.appendToPL(deleteSDIAddress, "contactfunction", contactfunction);
            Utils.appendToPL(deleteSDIAddress, "keyid1", keyid1);
            if (!keyid2.equals("")) {
                Utils.appendToPL(deleteSDIAddress, "keyid2", keyid2);
            }
            if (keyid3.equals("")) continue;
            Utils.appendToPL(deleteSDIAddress, "keyid3", keyid3);
        }
    }

    protected void collectControlCardData(JSONArray rows, PropertyList addControlCard, PropertyList deleteControlCard) {
        int addRows = 0;
        for (int i = 0; i < rows.length(); ++i) {
            JSONObject detailRow = rows.optJSONObject(i);
            String rowstatus = detailRow.optString("__rowstatus", "");
            String keyid1 = detailRow.optString("keyid1", "[keyid1]");
            String keyid2 = detailRow.optString("keyid2");
            String keyid3 = detailRow.optString("keyid3");
            if (rowstatus.equals("A")) {
                String controlCardId = detailRow.optString("spc_controlcardid");
                String calcMethodId = detailRow.optString("calcmethodid", "");
                String bgCalcMethodFunc = detailRow.optString("bgcalcmethodfunc", "");
                addControlCard.setProperty("sdcid", "SPC_SDIControlCard");
                Utils.appendToPL(addControlCard, "spc_controlcardid", controlCardId);
                Utils.appendToPL(addControlCard, "calcmethodid", calcMethodId);
                Utils.appendToPL(addControlCard, "bgcalcmethodfunc", bgCalcMethodFunc);
                Utils.appendToPL(addControlCard, "sdc", this.sdcid);
                Utils.appendToPL(addControlCard, "key1", keyid1);
                if (!keyid2.equals("")) {
                    Utils.appendToPL(addControlCard, "key2", keyid2);
                }
                if (!keyid3.equals("")) {
                    Utils.appendToPL(addControlCard, "key3", keyid3);
                }
                ++addRows;
            }
            if (!rowstatus.equals("D")) continue;
            String sdiControlCardId = detailRow.optString("spc_sdicontrolcardid", "");
            deleteControlCard.setProperty("sdcid", "SPC_SDIControlCard");
            Utils.appendToPL(deleteControlCard, "keyid1", sdiControlCardId);
        }
        if (addRows > 0) {
            addControlCard.setProperty("copies", "" + addRows);
        }
    }

    protected void collectAttributeData(JSONArray rows, Map<String, PropertyList> addSDIAttributeList, Map<String, PropertyList> editSDIAttributeList, Map<String, PropertyList> deleteSDIAttributeList) {
        for (int i = 0; i < rows.length(); ++i) {
            JSONObject detailRow = rows.optJSONObject(i);
            String rowstatus = detailRow.optString("__rowstatus", "");
            String attributeid = detailRow.optString("attributeid");
            String attributeinstance = detailRow.optString("attributeinstance", "1");
            String attributesdcid = detailRow.optString("attributesdcid");
            String value = detailRow.optString("value__", "");
            String keyid1 = detailRow.optString("keyid1", "[keyid1]");
            String keyid2 = detailRow.optString("keyid2");
            String keyid3 = detailRow.optString("keyid3");
            if (rowstatus.equals("A")) {
                PropertyList addSDIAttribute = addSDIAttributeList.get(keyid1 + ";" + keyid2 + ";" + keyid3);
                if (addSDIAttribute == null) {
                    addSDIAttribute = new PropertyList();
                    addSDIAttribute.setProperty("keyid1", keyid1);
                    if (!keyid2.equals("")) {
                        addSDIAttribute.setProperty("keyid2", keyid2);
                    }
                    if (!keyid3.equals("")) {
                        addSDIAttribute.setProperty("keyid3", keyid3);
                    }
                }
                Utils.appendToPL(addSDIAttribute, "attributeid", attributeid);
                Utils.appendToPL(addSDIAttribute, "attributeinstance", attributeinstance);
                Utils.appendToPL(addSDIAttribute, "value", value);
                addSDIAttributeList.put(keyid1 + ";" + keyid2 + ";" + keyid3, addSDIAttribute);
            }
            if (rowstatus.equals("E")) {
                PropertyList editSDIAttribute = editSDIAttributeList.get(keyid1 + ";" + keyid2 + ";" + keyid3);
                if (editSDIAttribute == null) {
                    editSDIAttribute = new PropertyList();
                    editSDIAttribute.setProperty("keyid1", keyid1);
                    if (!keyid2.equals("")) {
                        editSDIAttribute.setProperty("keyid2", keyid2);
                    }
                    if (!keyid3.equals("")) {
                        editSDIAttribute.setProperty("keyid3", keyid3);
                    }
                }
                Utils.appendToPL(editSDIAttribute, "attributeid", attributeid);
                Utils.appendToPL(editSDIAttribute, "attributeinstance", attributeinstance);
                Utils.appendToPL(editSDIAttribute, "attributesdcid", attributesdcid);
                Utils.appendToPL(editSDIAttribute, "value", value);
                editSDIAttributeList.put(keyid1 + ";" + keyid2 + ";" + keyid3, editSDIAttribute);
            }
            if (!rowstatus.equals("D")) continue;
            PropertyList deleteSDIAttribute = deleteSDIAttributeList.get(keyid1 + ";" + keyid2 + ";" + keyid3);
            if (deleteSDIAttribute == null) {
                deleteSDIAttribute = new PropertyList();
                deleteSDIAttribute.setProperty("keyid1", keyid1);
                if (!keyid2.equals("")) {
                    deleteSDIAttribute.setProperty("keyid2", keyid2);
                }
                if (!keyid3.equals("")) {
                    deleteSDIAttribute.setProperty("keyid3", keyid3);
                }
            }
            Utils.appendToPL(deleteSDIAttribute, "attributeid", attributeid);
            Utils.appendToPL(deleteSDIAttribute, "attributeinstance", attributeinstance);
            Utils.appendToPL(deleteSDIAttribute, "attributesdcid", attributesdcid);
            Utils.appendToPL(deleteSDIAttribute, "value", value);
            deleteSDIAttributeList.put(keyid1 + ";" + keyid2 + ";" + keyid3, deleteSDIAttribute);
        }
    }

    private void collectSDIDetailData(String key, String detailLinkId, JSONArray jsonArray, Map<String, PropertyList> otherSaveOperations, String newkeyid1, String newkeyid2) {
        PropertyList detailAddSDI = new PropertyList();
        PropertyList detailEditSDI = new PropertyList();
        PropertyList detailDeleteSDI = new PropertyList();
        this.collectDetailData(detailAddSDI, detailEditSDI, detailDeleteSDI, jsonArray, newkeyid1, newkeyid2);
        if (!detailAddSDI.isEmpty()) {
            int copies = Integer.parseInt((String)detailAddSDI.remove("copies"));
            for (int i = 0; i < copies; ++i) {
                PropertyList addProps = new PropertyList();
                addProps.setProperty("sdcid", this.sdcid);
                addProps.setProperty("linkid", detailLinkId);
                for (Map.Entry entry : detailAddSDI.entrySet()) {
                    String propertyid = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    String[] arrValue = value.split(";");
                    addProps.setProperty(propertyid, arrValue[i]);
                }
                otherSaveOperations.put(key + ";AddSDIDetail;" + i, addProps);
            }
        }
        if (!detailEditSDI.isEmpty()) {
            detailEditSDI.setProperty("sdcid", this.sdcid);
            detailEditSDI.setProperty("linkid", detailLinkId);
            otherSaveOperations.put(key + ";EditSDIDetail", detailEditSDI);
        }
        if (!detailDeleteSDI.isEmpty()) {
            detailDeleteSDI.setProperty("sdcid", this.sdcid);
            detailDeleteSDI.setProperty("linkid", detailLinkId);
            otherSaveOperations.put(key + ";DeleteSDIDetail", detailDeleteSDI);
        }
    }

    private void collectReverseFKData(String key, String detailsdcid, String linkcolumnid, String linkcolumnid2, JSONArray dataCollection, Map<String, PropertyList> otherSaveOperations) {
        String detailkeycol1 = this.getSDCProperty(detailsdcid, "keycolid1");
        String detailkeycol2 = this.getSDCProperty(detailsdcid, "keycolid2", "");
        PropertyList detailAddSDI = new PropertyList();
        PropertyList detailEditSDI = new PropertyList();
        PropertyList detailDeleteSDI = new PropertyList();
        this.collectPrimaryData(detailAddSDI, detailEditSDI, dataCollection, detailkeycol1, detailkeycol2, "", "");
        this.collectDeleteRows(detailDeleteSDI, dataCollection, detailkeycol1, detailkeycol2);
        if (!detailAddSDI.isEmpty()) {
            detailAddSDI.setProperty("sdcid", detailsdcid);
            detailAddSDI.setProperty("propsmatch", "Y");
            detailAddSDI.setProperty("__linkcolumn1", linkcolumnid);
            detailAddSDI.setProperty("__linkcolumn2", linkcolumnid2);
            otherSaveOperations.put(key + ";AddSDI", detailAddSDI);
        }
        if (!detailEditSDI.isEmpty()) {
            detailEditSDI.setProperty("sdcid", detailsdcid);
            detailEditSDI.setProperty("propsmatch", "Y");
            otherSaveOperations.put(key + ";EditSDI", detailEditSDI);
        }
        if (!detailDeleteSDI.isEmpty()) {
            detailDeleteSDI.setProperty("sdcid", detailsdcid);
            otherSaveOperations.put(key + ";DeleteSDI", detailDeleteSDI);
        }
    }

    private void collectSDINotes(String sSdinotes, List<PropertyList> addSDINotes) {
        JSONObject notes;
        try {
            notes = new JSONObject(sSdinotes);
        }
        catch (JSONException e) {
            this.logger.error("Failed to parse SDINotes JSON: " + sSdinotes);
            return;
        }
        JSONArray dataset = notes.optJSONArray("dataset");
        for (int i = 0; i < dataset.length(); ++i) {
            JSONArray noteObj = dataset.optJSONArray(i);
            String keyid1 = noteObj.optString(1);
            String keyid2 = noteObj.optString(2);
            String keyid3 = noteObj.optString(3);
            if (keyid1.equals("")) {
                keyid1 = "[keyid1]";
            }
            if (keyid2.equals("")) {
                keyid2 = "[keyid2]";
            }
            if (keyid3.equals("")) {
                keyid3 = "[keyid3]";
            }
            String note = noteObj.optString(4);
            String notetypeflag = noteObj.optString(5);
            String commentnotifyflag = noteObj.optString(6);
            PropertyList addSDINote = new PropertyList();
            addSDINote.setProperty("keyid1", keyid1);
            addSDINote.setProperty("keyid2", keyid2);
            addSDINote.setProperty("keyid3", keyid3);
            addSDINote.setProperty("note", note);
            addSDINote.setProperty("notetype", notetypeflag);
            addSDINote.setProperty("commentnotifyflag", commentnotifyflag);
            addSDINotes.add(addSDINote);
        }
    }

    private void collectDetailData(PropertyList detailAddSDI, PropertyList detailEditSDI, PropertyList detailDeleteSDI, JSONArray detailRows, String masterkeyid1, String masterkeyid2) {
        int addRows = 0;
        List<String> addedRowColumns = this.getAllColumns(detailRows, "A");
        List<String> editedRowColumns = this.getAllColumns(detailRows, "E");
        List<String> deletedRowColumns = this.getAllColumns(detailRows, "D");
        for (int i = 0; i < detailRows.length(); ++i) {
            String value;
            JSONObject detailRow = detailRows.optJSONObject(i);
            String rowstatus = detailRow.optString("__rowstatus", "");
            if (masterkeyid1.equals("")) {
                masterkeyid1 = detailRow.optString("keyid1");
            }
            if (masterkeyid2.equals("")) {
                masterkeyid2 = detailRow.optString("keyid2");
            }
            if (rowstatus.equals("A")) {
                ++addRows;
                for (String fieldid : addedRowColumns) {
                    value = detailRow.has(fieldid + "__") ? detailRow.optString(fieldid + "__") : detailRow.optString(fieldid);
                    Utils.appendToPL(detailAddSDI, fieldid, value);
                }
                continue;
            }
            if (rowstatus.equals("E")) {
                for (String fieldid : editedRowColumns) {
                    value = detailRow.has(fieldid + "__") ? detailRow.optString(fieldid + "__") : detailRow.optString(fieldid);
                    Utils.appendToPL(detailEditSDI, fieldid, value);
                }
                continue;
            }
            if (!rowstatus.equals("D")) continue;
            for (String fieldid : deletedRowColumns) {
                value = detailRow.has(fieldid + "__") ? detailRow.optString(fieldid + "__") : detailRow.optString(fieldid);
                Utils.appendToPL(detailDeleteSDI, fieldid, value);
            }
        }
        if (addRows > 0) {
            detailAddSDI.setProperty("copies", "" + addRows);
        }
    }

    private void collectDeleteRows(PropertyList detailDeleteSDI, JSONArray detailRows, String detailkeycol1, String detailkeycol2) {
        for (int i = 0; i < detailRows.length(); ++i) {
            JSONObject detailRow = detailRows.optJSONObject(i);
            String rowstatus = detailRow.optString("__rowstatus", "");
            if (!rowstatus.equals("D") && !rowstatus.equals("VD")) continue;
            String keyid1 = detailRow.optString(detailkeycol1);
            String keyid2 = detailRow.optString(detailkeycol2, "");
            Utils.appendToPL(detailDeleteSDI, "keyid1", keyid1);
            if (detailkeycol2.equals("")) continue;
            Utils.appendToPL(detailDeleteSDI, "keyid2", keyid2);
        }
    }

    private void collectSpecData(JSONArray jSpecRows, PropertyList addSDISpec, PropertyList deleteSDISpec) {
        for (int i = 0; i < jSpecRows.length(); ++i) {
            JSONObject jDataset = jSpecRows.optJSONObject(i);
            String rowstatus = jDataset.optString("__rowstatus", "");
            String keyid1 = jDataset.optString("keyid1", "[keyid1]");
            String keyid2 = jDataset.optString("keyid2", "[keyid2]");
            String keyid3 = jDataset.optString("keyid3", "[keyid3]");
            String specid = jDataset.optString("specid", "");
            String specversionid = jDataset.optString("specversionid", "");
            if (rowstatus.equals("A")) {
                Utils.appendToPL(addSDISpec, "keyid1", keyid1);
                Utils.appendToPL(addSDISpec, "keyid2", keyid2);
                Utils.appendToPL(addSDISpec, "keyid3", keyid3);
                Utils.appendToPL(addSDISpec, "specid", specid);
                Utils.appendToPL(addSDISpec, "specversionid", specversionid);
            } else if (rowstatus.equals("D") || rowstatus.equals("VD")) {
                Utils.appendToPL(deleteSDISpec, "keyid1", keyid1);
                Utils.appendToPL(deleteSDISpec, "keyid2", keyid2);
                Utils.appendToPL(deleteSDISpec, "keyid3", keyid3);
                Utils.appendToPL(deleteSDISpec, "specid", specid);
                Utils.appendToPL(deleteSDISpec, "specversionid", specversionid);
            }
            this.appendChangedIds(keyid1, keyid2, keyid3);
        }
    }

    private void collectApprovalData(JSONArray jApprovalRows, PropertyList editDataApproval) {
        for (int i = 0; i < jApprovalRows.length(); ++i) {
            JSONObject jDataset = jApprovalRows.optJSONObject(i);
            String rowstatus = jDataset.optString("__rowstatus", "");
            if (!rowstatus.equals("E")) continue;
            String keyid1 = jDataset.optString("keyid1", "[keyid1]");
            String keyid2 = jDataset.optString("keyid2", "[keyid2]");
            String keyid3 = jDataset.optString("keyid3", "[keyid3]");
            Utils.appendToPL(editDataApproval, "keyid1", keyid1);
            Utils.appendToPL(editDataApproval, "keyid2", keyid2);
            Utils.appendToPL(editDataApproval, "keyid3", keyid3);
            Utils.appendToPL(editDataApproval, "paramlistid", jDataset.optString("paramlistid", ""));
            Utils.appendToPL(editDataApproval, "paramlistversionid", jDataset.optString("paramlistversionid", ""));
            Utils.appendToPL(editDataApproval, "variantid", jDataset.optString("variantid", ""));
            Utils.appendToPL(editDataApproval, "dataset", jDataset.optString("dataset", ""));
            Utils.appendToPL(editDataApproval, "approvalstep", jDataset.optString("approvalstep", ""));
            Utils.appendToPL(editDataApproval, "approvalflag", jDataset.optString("approvalflag__", "U"));
            this.appendChangedIds(keyid1, keyid2, keyid3);
        }
    }

    protected void collectWorkitemData(JSONArray jDataSetRows, PropertyList addSDIWorkItem, PropertyList editSDIWorkItem, PropertyList deleteSDIWorkItem) {
        int numEditSdiWorkitems = 0;
        List<String> changedColumns = this.getChangedColumns(jDataSetRows, "keyid1", "keyid2", "keyid3", WORKITEMID, WORKITEMVERSIONID, WORKITEMINSTANCE);
        for (int i = 0; i < jDataSetRows.length(); ++i) {
            JSONObject jDataset = jDataSetRows.optJSONObject(i);
            String rowstatus = jDataset.optString("__rowstatus", "");
            String keyid1 = jDataset.optString("keyid1", "[keyid1]");
            String keyid2 = jDataset.optString("keyid2", "[keyid2]");
            String keyid3 = jDataset.optString("keyid3", "[keyid3]");
            String workitemid = jDataset.optString(WORKITEMID, "");
            String workitemversionid = jDataset.optString(WORKITEMVERSIONID, "");
            String workiteminstance = jDataset.optString(WORKITEMINSTANCE, "");
            String usersequence = jDataset.optString("usersequence__", "");
            if (rowstatus.equals("A")) {
                Utils.appendToPL(addSDIWorkItem, "keyid1", keyid1);
                Utils.appendToPL(addSDIWorkItem, "keyid2", keyid2);
                Utils.appendToPL(addSDIWorkItem, "keyid3", keyid3);
                Utils.appendToPL(addSDIWorkItem, WORKITEMID, workitemid);
                Utils.appendToPL(addSDIWorkItem, WORKITEMVERSIONID, workitemversionid);
                Utils.appendToPL(addSDIWorkItem, WORKITEMINSTANCE, workiteminstance);
                Utils.appendToPL(addSDIWorkItem, "usersequence", usersequence);
            } else if (rowstatus.equals("D") || rowstatus.equals("VD")) {
                Utils.appendToPL(deleteSDIWorkItem, "keyid1", keyid1);
                Utils.appendToPL(deleteSDIWorkItem, "keyid2", keyid2);
                Utils.appendToPL(deleteSDIWorkItem, "keyid3", keyid3);
                Utils.appendToPL(deleteSDIWorkItem, WORKITEMID, workitemid);
                Utils.appendToPL(deleteSDIWorkItem, WORKITEMVERSIONID, workitemversionid);
                Utils.appendToPL(deleteSDIWorkItem, WORKITEMINSTANCE, workiteminstance);
            }
            boolean dataChanged = false;
            PropertyList tempMap = new PropertyList();
            for (String fieldid : changedColumns) {
                String fieldvalue;
                if (jDataset.has(fieldid + "__")) {
                    fieldvalue = jDataset.optString(fieldid + "__");
                    dataChanged = true;
                } else {
                    fieldvalue = jDataset.optString(fieldid);
                }
                if (fieldid.startsWith("sdiworkitem_")) {
                    fieldid = fieldid.substring(12);
                }
                tempMap.setProperty(fieldid, fieldvalue);
            }
            if ((rowstatus.equals("A") || rowstatus.equals("E")) && dataChanged) {
                tempMap.setProperty("keyid1", keyid1);
                tempMap.setProperty("keyid2", keyid2);
                tempMap.setProperty("keyid3", keyid3);
                tempMap.setProperty(WORKITEMID, workitemid);
                tempMap.setProperty(WORKITEMINSTANCE, workiteminstance);
                this.injectMapToAnother(tempMap, editSDIWorkItem, numEditSdiWorkitems++);
            }
            this.appendChangedIds(keyid1, keyid2, keyid3);
        }
    }

    protected void collectDataSetData(JSONArray jDataSetRows, PropertyList addDataset, PropertyList editDataset, Map<String, PropertyList> deleteDatasets) {
        List<String> changedColumns = this.getChangedColumns(jDataSetRows, "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset");
        int numEditDatasets = 0;
        for (int i = 0; i < jDataSetRows.length(); ++i) {
            JSONObject jDataset = jDataSetRows.optJSONObject(i);
            if (jDataset == null) {
                return;
            }
            String rowstatus = jDataset.optString("__rowstatus", "");
            String keyid1 = jDataset.optString("keyid1", "[keyid1]");
            String keyid2 = jDataset.optString("keyid2", "[keyid2]");
            String keyid3 = jDataset.optString("keyid3", "[keyid3]");
            String paramlistid = jDataset.optString("paramlistid", "");
            String paramlistversionid = jDataset.optString("paramlistversionid", "");
            String variantid = jDataset.optString("variantid", "");
            String dataset = jDataset.optString("dataset", "");
            if (rowstatus.equals("A")) {
                Utils.appendToPL(addDataset, "keyid1", keyid1);
                Utils.appendToPL(addDataset, "keyid2", keyid2);
                Utils.appendToPL(addDataset, "keyid3", keyid3);
                Utils.appendToPL(addDataset, "paramlistid", paramlistid);
                Utils.appendToPL(addDataset, "paramlistversionid", paramlistversionid);
                Utils.appendToPL(addDataset, "variantid", variantid);
                Utils.appendToPL(addDataset, "dataset", dataset);
            } else if (rowstatus.equals("D") || rowstatus.equals("VD")) {
                PropertyList deleteDataset = this.getPropertyList(deleteDatasets, paramlistid, paramlistversionid, variantid, dataset);
                Utils.appendToPL(deleteDataset, "keyid1", keyid1);
                Utils.appendToPL(deleteDataset, "keyid2", keyid2);
                Utils.appendToPL(deleteDataset, "keyid3", keyid3);
            }
            boolean dataChanged = false;
            PropertyList tempMap = new PropertyList();
            for (String fieldid : changedColumns) {
                String fieldvalue;
                if (jDataset.has(fieldid + "__")) {
                    fieldvalue = jDataset.optString(fieldid + "__");
                    dataChanged = true;
                } else {
                    fieldvalue = jDataset.optString(fieldid);
                }
                if (fieldid.startsWith("sdidata_")) {
                    fieldid = fieldid.substring(8);
                }
                tempMap.setProperty(fieldid, fieldvalue);
            }
            if ((rowstatus.equals("A") || rowstatus.equals("E")) && dataChanged) {
                tempMap.setProperty("keyid1", keyid1);
                tempMap.setProperty("keyid2", keyid2);
                tempMap.setProperty("keyid3", keyid3);
                tempMap.setProperty("paramlistid", paramlistid);
                tempMap.setProperty("paramlistversionid", paramlistversionid);
                tempMap.setProperty("variantid", variantid);
                tempMap.setProperty("dataset", dataset);
                this.injectMapToAnother(tempMap, editDataset, numEditDatasets++);
            }
            this.appendChangedIds(keyid1, keyid2, keyid3);
        }
    }

    private PropertyList getPropertyList(Map<String, PropertyList> map, String paramlistid, String paramlistversionid, String variantid, String dataset) {
        String key = String.join((CharSequence)";", paramlistid, paramlistversionid, variantid, dataset);
        if (!map.containsKey(key)) {
            PropertyList pl = new PropertyList();
            pl.setProperty("paramlistid", paramlistid);
            pl.setProperty("paramlistversionid", paramlistversionid);
            pl.setProperty("variantid", variantid);
            pl.setProperty("dataset", dataset);
            map.put(key, pl);
        }
        return map.get(key);
    }

    protected void collectDataItemData(JSONArray jDataItemRows, PropertyList enterDataItem, PropertyList editDataItem, PropertyList extendDataSet, PropertyList addReplicate, PropertyList deleteDataItem) {
        List<String> changedColumns = this.getChangedColumns(jDataItemRows, "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "enteredtext");
        int numEditDataItems = 0;
        for (int i = 0; i < jDataItemRows.length(); ++i) {
            String enteredtext;
            JSONObject jDataitem = jDataItemRows.optJSONObject(i);
            if (jDataitem == null) {
                return;
            }
            String keyid1 = jDataitem.optString("keyid1", "[keyid1]");
            String keyid2 = jDataitem.optString("keyid2", "[keyid2]");
            String keyid3 = jDataitem.optString("keyid3", "[keyid3]");
            String paramlistid = jDataitem.optString("paramlistid", "");
            String paramlistversionid = jDataitem.optString("paramlistversionid", "");
            String variantid = jDataitem.optString("variantid", "");
            String dataset = jDataitem.optString("dataset", "");
            String paramid = jDataitem.optString("paramid", "");
            String paramtype = jDataitem.optString("paramtype", "");
            String replicateid = jDataitem.optString("replicateid", "");
            String rowStatus = jDataitem.optString("__rowstatus", "");
            if (rowStatus.equals("A")) {
                if (replicateid.equals("1")) {
                    Utils.appendToPL(extendDataSet, "keyid1", keyid1);
                    Utils.appendToPL(extendDataSet, "keyid2", keyid2);
                    Utils.appendToPL(extendDataSet, "keyid3", keyid3);
                    Utils.appendToPL(extendDataSet, "paramlistid", paramlistid);
                    Utils.appendToPL(extendDataSet, "paramlistversionid", paramlistversionid);
                    Utils.appendToPL(extendDataSet, "variantid", variantid);
                    Utils.appendToPL(extendDataSet, "dataset", dataset);
                    Utils.appendToPL(extendDataSet, "paramid", paramid);
                    Utils.appendToPL(extendDataSet, "paramtype", paramtype);
                    Utils.appendToPL(extendDataSet, "replicateid", replicateid);
                    DataSet paramlistItemData = this.getParamlistItemData(paramlistid, paramlistversionid, variantid, paramid, paramtype);
                    Utils.appendToPL(extendDataSet, "displayunits", paramlistItemData.getValue(0, "displayunits", ""));
                    Utils.appendToPL(extendDataSet, "mandatoryflag", paramlistItemData.getValue(0, "mandatoryflag", "N"));
                    Utils.appendToPL(extendDataSet, "datatypes", paramlistItemData.getValue(0, "datatypes", "N"));
                    Utils.appendToPL(extendDataSet, "usersequence", paramlistItemData.getValue(0, "usersequence", "99"));
                } else {
                    Utils.appendToPL(addReplicate, "keyid1", keyid1);
                    Utils.appendToPL(addReplicate, "keyid2", keyid2);
                    Utils.appendToPL(addReplicate, "keyid3", keyid3);
                    Utils.appendToPL(addReplicate, "paramlistid", paramlistid);
                    Utils.appendToPL(addReplicate, "paramlistversionid", paramlistversionid);
                    Utils.appendToPL(addReplicate, "variantid", variantid);
                    Utils.appendToPL(addReplicate, "dataset", dataset);
                    Utils.appendToPL(addReplicate, "paramid", paramid);
                    Utils.appendToPL(addReplicate, "paramtype", paramtype);
                    Utils.appendToPL(addReplicate, "replicateid", replicateid);
                }
            }
            if (rowStatus.equals("D") || rowStatus.equals("VD")) {
                Utils.appendToPL(deleteDataItem, "keyid1", keyid1);
                Utils.appendToPL(deleteDataItem, "keyid2", keyid2);
                Utils.appendToPL(deleteDataItem, "keyid3", keyid3);
                Utils.appendToPL(deleteDataItem, "paramlistid", paramlistid);
                Utils.appendToPL(deleteDataItem, "paramlistversionid", paramlistversionid);
                Utils.appendToPL(deleteDataItem, "variantid", variantid);
                Utils.appendToPL(deleteDataItem, "dataset", dataset);
                Utils.appendToPL(deleteDataItem, "paramid", paramid);
                Utils.appendToPL(deleteDataItem, "paramtype", paramtype);
                Utils.appendToPL(deleteDataItem, "replicateid", replicateid);
            }
            if ((enteredtext = jDataitem.optString("enteredtext__", null)) != null) {
                Utils.appendToPL(enterDataItem, "keyid1", keyid1);
                Utils.appendToPL(enterDataItem, "keyid2", keyid2);
                Utils.appendToPL(enterDataItem, "keyid3", keyid3);
                Utils.appendToPL(enterDataItem, "paramlistid", paramlistid);
                Utils.appendToPL(enterDataItem, "paramlistversionid", paramlistversionid);
                Utils.appendToPL(enterDataItem, "variantid", variantid);
                Utils.appendToPL(enterDataItem, "dataset", dataset);
                Utils.appendToPL(enterDataItem, "paramid", paramid);
                Utils.appendToPL(enterDataItem, "paramtype", paramtype);
                Utils.appendToPL(enterDataItem, "replicateid", replicateid);
                Utils.appendToPL(enterDataItem, "enteredtext", enteredtext, false);
            }
            boolean dataChanged = false;
            PropertyList tempMap = new PropertyList();
            for (String fieldid : changedColumns) {
                String fieldvalue;
                if (jDataitem.has(fieldid + "__")) {
                    fieldvalue = jDataitem.optString(fieldid + "__");
                    dataChanged = true;
                } else {
                    fieldvalue = jDataitem.optString(fieldid);
                }
                if (fieldid.startsWith("sdidataitem_")) {
                    fieldid = fieldid.substring(12);
                }
                tempMap.setProperty(fieldid, fieldvalue);
            }
            if (dataChanged) {
                tempMap.setProperty("keyid1", keyid1);
                tempMap.setProperty("keyid2", keyid2);
                tempMap.setProperty("keyid3", keyid3);
                tempMap.setProperty("paramlistid", paramlistid);
                tempMap.setProperty("paramlistversionid", paramlistversionid);
                tempMap.setProperty("variantid", variantid);
                tempMap.setProperty("dataset", dataset);
                tempMap.setProperty("paramid", paramid);
                tempMap.setProperty("paramtype", paramtype);
                tempMap.setProperty("replicateid", replicateid);
                this.injectMapToAnother(tempMap, editDataItem, numEditDataItems++);
            }
            this.appendChangedIds(keyid1, keyid2, keyid3);
        }
    }

    private DataSet getParamlistItemData(String paramlistid, String paramlistversionid, String variantid, String paramid, String paramtype) {
        String sql = "SELECT displayunits, mandatoryflag, usersequence, datatypes FROM paramlistitem WHERE paramlistid=? AND paramlistversionid=? AND variantid=? AND paramid=? AND paramtype=? ";
        Object[] params = new String[]{paramlistid, paramlistversionid, variantid, paramid, paramtype};
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
    }

    protected String collectPrimaryData(PropertyList primaryAddSDI, PropertyList primaryEditSDI, JSONArray jPrimaryRows, String keycol1, String keycol2, String keycol3, String parentlinkid) {
        int numAddSDIs = 0;
        int numEditSDIs = 0;
        StringBuilder keyid1 = new StringBuilder();
        StringBuilder keyid2 = new StringBuilder();
        StringBuilder keyid3 = new StringBuilder();
        List<String> changedColumns = this.getChangedColumns(jPrimaryRows, keycol1, keycol2, keycol3);
        for (int i = 0; i < jPrimaryRows.length(); ++i) {
            JSONObject jPrimary = jPrimaryRows.optJSONObject(i);
            PropertyList tempMap = new PropertyList();
            String rowstatus = jPrimary.optString("__rowstatus", "");
            if (keyid1.length() > 0) {
                keyid1.append(";");
            }
            if (rowstatus.equals("A")) {
                keyid1.append(jPrimary.optString(keycol1 + "__", "[keyid1]"));
            } else {
                keyid1.append(jPrimary.optString(keycol1, "[keyid1]"));
            }
            if (!keycol2.equals("")) {
                if (keyid2.length() > 0) {
                    keyid2.append(";");
                }
                if (rowstatus.equals("A")) {
                    keyid2.append(jPrimary.optString(keycol2 + "__", "[keyid2]"));
                } else {
                    keyid2.append(jPrimary.optString(keycol2, "[keyid2]"));
                }
            }
            if (!keycol3.equals("")) {
                if (keyid3.length() > 0) {
                    keyid3.append(";");
                }
                if (rowstatus.equals("A")) {
                    keyid3.append(jPrimary.optString(keycol3 + "__", "[keyid3]"));
                } else {
                    keyid3.append(jPrimary.optString(keycol3, "[keyid3]"));
                }
            }
            boolean dataChanged = false;
            if (rowstatus.equals("")) continue;
            if (rowstatus.equals("A")) {
                tempMap.setProperty("moddt", "n");
                dataChanged = true;
            }
            for (String fieldid : changedColumns) {
                String fieldvalue;
                if (jPrimary.has(fieldid + "__")) {
                    fieldvalue = jPrimary.optString(fieldid + "__");
                    dataChanged = true;
                } else {
                    fieldvalue = jPrimary.optString(fieldid);
                }
                if (!parentlinkid.equals("") && fieldid.startsWith(parentlinkid.toLowerCase() + "_")) {
                    fieldid = fieldid.substring(parentlinkid.length() + 1);
                }
                tempMap.setProperty(fieldid, fieldvalue);
            }
            if (rowstatus.equals("A") && dataChanged) {
                tempMap.setProperty("keyid1", jPrimary.optString(keycol1 + "__", "[keyid1]"));
                if (!keycol2.equals("")) {
                    tempMap.setProperty("keyid2", jPrimary.optString(keycol2 + "__", "[keyid2]"));
                }
                if (!keycol3.equals("")) {
                    tempMap.setProperty("keyid3", jPrimary.optString(keycol3 + "__", "[keyid3]"));
                }
                this.injectMapToAnother(tempMap, primaryAddSDI, numAddSDIs++);
                continue;
            }
            if (!rowstatus.equals("E") || !dataChanged) continue;
            tempMap.setProperty("keyid1", jPrimary.optString(keycol1, "[keyid1]"));
            if (!keycol2.equals("")) {
                tempMap.setProperty("keyid2", jPrimary.optString(keycol2, "[keyid2]"));
            }
            if (!keycol3.equals("")) {
                tempMap.setProperty("keyid3", jPrimary.optString(keycol3, "[keyid3]"));
            }
            this.injectMapToAnother(tempMap, primaryEditSDI, numEditSDIs++);
        }
        String retval = keyid1.toString();
        if (keycol2.equals("")) {
            retval = retval + "|" + keyid2.toString();
        }
        if (keycol3.equals("")) {
            retval = retval + "|" + keyid3.toString();
        }
        return retval;
    }

    private List<String> getChangedColumns(JSONArray jPrimaryRows, String ... ignoreColumns) {
        ArrayList<String> changedColumns = new ArrayList<String>();
        HashSet<String> setIgnoreColumns = new HashSet<String>(Arrays.asList(ignoreColumns));
        for (int i = 0; i < jPrimaryRows.length(); ++i) {
            JSONObject jPrimary = jPrimaryRows.optJSONObject(i);
            Iterator iter = jPrimary.keys();
            while (iter.hasNext()) {
                String fieldid = (String)iter.next();
                if (!fieldid.endsWith("__") || this.excludedFields.contains(fieldid = fieldid.substring(0, fieldid.length() - 2)) || changedColumns.contains(fieldid) || setIgnoreColumns.contains(fieldid)) continue;
                changedColumns.add(fieldid);
            }
        }
        return changedColumns;
    }

    private List<String> getAllColumns(JSONArray jPrimaryRows, String rowstatus) {
        ArrayList<String> allColumns = new ArrayList<String>();
        for (int i = 0; i < jPrimaryRows.length(); ++i) {
            JSONObject jPrimary = jPrimaryRows.optJSONObject(i);
            Iterator iter = jPrimary.keys();
            if (!rowstatus.equals(jPrimary.optString("__rowstatus"))) continue;
            while (iter.hasNext()) {
                String fieldid = (String)iter.next();
                if (fieldid.startsWith("__")) continue;
                if (fieldid.endsWith("__")) {
                    fieldid = fieldid.substring(0, fieldid.length() - 2);
                }
                if (this.excludedFields.contains(fieldid) || allColumns.contains(fieldid)) continue;
                allColumns.add(fieldid);
            }
        }
        return allColumns;
    }

    private void injectMapToAnother(PropertyList tempMap, PropertyList mainMap, int originalItemCount) {
        Iterator iter = mainMap.keySet().iterator();
        HashSet originalKeySet = new HashSet();
        while (iter.hasNext()) {
            originalKeySet.add(iter.next());
        }
        for (Map.Entry entry : tempMap.entrySet()) {
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (mainMap.containsKey(key)) {
                Utils.appendToPL(mainMap, key, value);
                originalKeySet.remove(key);
                continue;
            }
            Utils.appendToPL(mainMap, key, "(null)", originalItemCount);
            Utils.appendToPL(mainMap, key, value);
        }
        for (String key : originalKeySet) {
            Utils.appendToPL(mainMap, key, "(null)");
        }
    }

    private void appendChangedIds(String keyid1, String keyid2, String keyid3) {
        String key = keyid1;
        if (!keyid2.equals("") && !keyid2.equals("(null)")) {
            key = key + "|" + keyid2;
        }
        if (!keyid3.equals("") && !keyid3.equals("(null)")) {
            key = key + "|" + keyid3;
        }
        this.changedKeys.add(key);
    }

    private String extractChangedKeyids(int num) {
        StringBuilder retval = new StringBuilder();
        for (String key : this.changedKeys) {
            String[] arr = key.split("\\|");
            if (arr.length <= num - 1) continue;
            if (retval.length() > 0) {
                retval.append(";");
            }
            retval.append(arr[num - 1]);
        }
        return retval.toString();
    }

    protected void processSystemAction(String actionid, String actionversionid, PropertyList properties) throws ActionException {
        String templateid;
        if (!this.auditreason.equals("")) {
            properties.setProperty("auditreason", this.auditreason);
        }
        if (!this.auditsignedflag.equals("")) {
            properties.setProperty("auditsignedflag", this.auditsignedflag);
        }
        if (properties.getProperty("keyid2", "").toUpperCase().contains("C") && this.getSDCProcessor().getProperty(properties.getProperty("sdcid", ""), "versionedflag").equals("Y")) {
            String[] keyid1Arr = properties.getProperty("keyid1", "").split(";");
            String[] keyid2Arr = properties.getProperty("keyid2", "").split(";");
            StringBuilder keyid2builder = new StringBuilder();
            for (int i = 0; i < keyid1Arr.length; ++i) {
                String keyid2;
                String keyid1 = keyid1Arr[i];
                String string = keyid2 = keyid2Arr.length > i ? keyid2Arr[i] : "";
                if (keyid2builder.length() > 0) {
                    keyid2builder.append(";");
                }
                if (keyid2.equals("C")) {
                    String latestVersion = Utils.getLatestVersion(keyid1, this.getSDCProps(this.sdcid), this.getQueryProcessor(), this.getConnectionProcessor().isOra());
                    keyid2builder.append(latestVersion);
                    continue;
                }
                keyid2builder.append(keyid2);
            }
            properties.setProperty("keyid2", keyid2builder.toString());
        }
        if (actionid.equals("AddSDI") && properties.containsKey("templateid") && (templateid = properties.getProperty("templateid", "")).contains(";")) {
            String[] arrTemplateids = templateid.split(";");
            boolean multiTemplate = false;
            String firstTemplate = arrTemplateids[0];
            for (String currTemplateid : arrTemplateids) {
                if (currTemplateid.equals(firstTemplate)) continue;
                multiTemplate = true;
                break;
            }
            if (multiTemplate) {
                this.addSDIsWithMultipleTemplates(properties);
                return;
            }
            properties.setProperty("templateid", arrTemplateids[0]);
        }
        try {
            this.getActionProcessor().processAction(actionid, actionversionid, properties);
        }
        catch (ActionException e) {
            this.logger.error("Error processing " + actionid + " with properties " + properties.toJSONString());
            throw e;
        }
    }

    private void addSDIsWithMultipleTemplates(PropertyList properties) throws ActionException {
        String[] arrTemplates = properties.getProperty("templateid").split(";");
        int itemCount = arrTemplates.length;
        ArrayList<String> distinctTemplates = new ArrayList<String>();
        for (String templateid : arrTemplates) {
            if (distinctTemplates.contains(templateid)) continue;
            distinctTemplates.add(templateid);
        }
        for (String templateId : distinctTemplates) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("templateid", templateId.equals("(null)") ? "" : templateId);
            int copies = 0;
            for (int i = 0; i < itemCount; ++i) {
                if (!arrTemplates[i].equals(templateId)) continue;
                for (String propertyId : properties.keySet()) {
                    if (propertyId.equals("templateid")) continue;
                    String[] valueArr = properties.getProperty(propertyId).split(";");
                    if (valueArr.length == 1) {
                        actionProps.setProperty(propertyId, valueArr[0]);
                        continue;
                    }
                    if (valueArr.length <= i) continue;
                    Utils.appendToPL(actionProps, propertyId, valueArr[i]);
                }
                ++copies;
            }
            actionProps.setProperty("copies", "" + copies);
            this.getActionProcessor().processAction("AddSDI", "1", actionProps);
        }
    }

    protected String getSDCProperty(String sdcid, String propertyid) {
        return this.getSDCProcessor().getProperty(sdcid, propertyid);
    }

    protected String getSDCProperty(String sdcid, String propertyid, String defaultvalue) {
        return this.getSDCProcessor().getProperty(sdcid, propertyid, defaultvalue);
    }

    protected DataSet getSDCLinksData(String sdcid) {
        return this.getSDCProcessor().getLinksData(sdcid);
    }
}


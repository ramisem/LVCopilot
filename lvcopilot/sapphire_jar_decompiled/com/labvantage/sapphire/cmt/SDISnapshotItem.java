/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class SDISnapshotItem
implements SnapshotItem {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private String sdcId;
    private String keyId1;
    private String keyId2 = "";
    private String keyId3 = "";
    private String policyNodeId;
    private SDISnapshotItem parent;
    protected SDISnapshot container;
    private boolean isIncludedForTransfer;
    private boolean isDeleted;
    private boolean isRenamed;
    private boolean isCurrentLink;
    private Map<SnapshotItem.LinkType, Map<String, List<SnapshotItem>>> linkItems = new HashMap<SnapshotItem.LinkType, Map<String, List<SnapshotItem>>>();
    private boolean loadedSuccessfully;
    private String statusMessage;
    private String linkId = "";
    private SnapshotItem.LinkType linkType;
    private PropertyList parentLinkProps = null;
    protected static Logger logger = new Logger(new LogContext("SDISnapshotItem", "(none)"));

    public SDISnapshotItem() {
    }

    public SDISnapshotItem(String xmlId) throws IllegalArgumentException {
        String[] idParts = StringUtil.split(xmlId, ";");
        if (idParts.length < 4) {
            throw new IllegalArgumentException("Invalid Id structure found: " + xmlId);
        }
        this.setSdcId(idParts[0]);
        this.setKeyId1(idParts[1]);
        this.setKeyId2(idParts[2]);
        this.setKeyId3(idParts[3]);
        this.setPolicyNodeId(idParts[4]);
    }

    public SDISnapshotItem(String sdcId, String keyId1, String keyId2, String keyId3) {
        this(sdcId, keyId1, keyId2, keyId3, "");
    }

    public SDISnapshotItem(String sdcId, String keyId1, String keyId2, String keyId3, String policyNodeId) {
        this.sdcId = sdcId;
        this.keyId1 = keyId1;
        this.keyId2 = keyId2;
        this.keyId3 = keyId3;
        this.policyNodeId = policyNodeId;
    }

    @Override
    public String getSDCId() {
        return this.sdcId;
    }

    public void setSdcId(String sdcId) {
        this.sdcId = sdcId;
    }

    @Override
    public String getKeyId1() {
        return this.keyId1;
    }

    public void setKeyId1(String keyId1) {
        this.keyId1 = keyId1;
    }

    @Override
    public String getKeyId2() {
        return this.keyId2;
    }

    public void setKeyId2(String keyId2) {
        this.keyId2 = keyId2;
    }

    @Override
    public String getKeyId3() {
        return this.keyId3;
    }

    public void setKeyId3(String keyId3) {
        this.keyId3 = keyId3;
    }

    @Override
    public String getPolicyNodeId() {
        return this.policyNodeId;
    }

    public void setPolicyNodeId(String policyNodeId) {
        this.policyNodeId = policyNodeId;
    }

    public boolean isDeleted() {
        return this.isDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    public boolean isRenamed() {
        return this.isRenamed;
    }

    public void setRenamed(boolean renamed) {
        this.isRenamed = renamed;
    }

    public boolean isCurrentLink() {
        return this.isCurrentLink;
    }

    public void setCurrentLink(boolean currentLink) {
        this.isCurrentLink = currentLink;
    }

    public String getLinkId() {
        return this.linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public SnapshotItem.LinkType getLinkType() {
        return this.linkType;
    }

    public void setLinkType(SnapshotItem.LinkType linkType) {
        this.linkType = linkType;
    }

    public SDISnapshotItem getParent() {
        return this.parent;
    }

    public void setParent(SDISnapshotItem parent) {
        this.parent = parent;
    }

    public boolean isLoadedSuccessfully() {
        return this.loadedSuccessfully;
    }

    public void setLoadedSuccessfully(boolean loadedSuccessfully) {
        this.loadedSuccessfully = loadedSuccessfully;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public boolean isIncludedForTransfer() {
        return this.isIncludedForTransfer;
    }

    public void setIncludedForTransfer(boolean includedForTransfer) {
        this.isIncludedForTransfer = includedForTransfer;
    }

    protected SDISnapshot getContainer() {
        return this.getTop().container;
    }

    @Override
    public SDISnapshot getSnapshot() {
        return this.getContainer();
    }

    public void setContainer(SDISnapshot container) {
        this.container = container;
    }

    public String[] getIdentifierColumns() throws SapphireException {
        CMTPolicy sourcePolicy;
        String identify;
        PropertyList transPolicyNodePL = this.getPolicyNodeProps();
        if (transPolicyNodePL != null && (identify = (sourcePolicy = new CMTPolicy(transPolicyNodePL)).getIndentifyColumn()) != null && identify.length() > 0) {
            String[] idcolumns = StringUtil.split(identify, ",");
            ArrayList<String> list = new ArrayList<String>();
            for (int c = 0; c < idcolumns.length; ++c) {
                String columnid = idcolumns[c];
                if (idcolumns[c].lastIndexOf(" ") > 0) {
                    columnid = idcolumns[c].substring(0, idcolumns[c].lastIndexOf(" "));
                }
                list.add(columnid);
            }
            String[] strList = new String[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                strList[i] = (String)list.get(i);
            }
            return strList;
        }
        return null;
    }

    public InputStream getAttachmentAsStream(int attachmentNum) throws SapphireException {
        SDIData sdiData;
        DataSet attachments;
        InputStream inStream = null;
        if (this.getContainer().getSnapshotPackage() != null && (attachments = (sdiData = this.getSDIData()).getDataset("attachment")) != null) {
            String fileName;
            HashMap<String, BigDecimal> findMap = new HashMap<String, BigDecimal>();
            findMap.put("attachmentnum", new BigDecimal(attachmentNum));
            int findRow = attachments.findRow(findMap);
            if (findRow > -1 && (fileName = attachments.getString(findRow, "__cmtattachmentid", "")) != null && fileName.length() > 0) {
                inStream = this.getContainer().getSnapshotPackage().getAttachmentFile(fileName);
            }
        }
        return inStream;
    }

    public long getAttachmentSize() throws SapphireException {
        SDIData sdiData;
        DataSet attachments;
        long attTotalSize = -1L;
        if (this.getContainer().getSnapshotPackage() != null && (attachments = (sdiData = this.getSDIData()).getDataset("attachment")) != null) {
            for (int i = 0; i < attachments.getRowCount(); ++i) {
                long attSize = attachments.getLong(i, "attachmentsize");
                if (attSize == -999999999L) continue;
                attTotalSize += attSize;
            }
        }
        return attTotalSize;
    }

    @Override
    public List<SnapshotItem> getLinkItems() {
        ArrayList<SnapshotItem> allLinks = new ArrayList<SnapshotItem>();
        for (Map<String, List<SnapshotItem>> linkTypeLinks : this.linkItems.values()) {
            for (List<SnapshotItem> linkTypeLinkIdLinks : linkTypeLinks.values()) {
                allLinks.addAll(linkTypeLinkIdLinks);
            }
        }
        return allLinks;
    }

    @Override
    public List<SnapshotItem> getLinkItemsByType(SnapshotItem.LinkType linkType) {
        ArrayList<SnapshotItem> linksByType = new ArrayList<SnapshotItem>();
        Map<String, List<SnapshotItem>> linkTypeLinks = this.linkItems.get((Object)linkType);
        if (linkTypeLinks != null) {
            for (List<SnapshotItem> linkTypeLinkIdLinks : linkTypeLinks.values()) {
                linksByType.addAll(linkTypeLinkIdLinks);
            }
        }
        return linksByType;
    }

    @Override
    public List<String> getLinkIdsByType(SnapshotItem.LinkType linkType) {
        ArrayList<String> linkTypeLinkIds = new ArrayList<String>();
        Map<String, List<SnapshotItem>> linkTypeLinks = this.linkItems.get((Object)linkType);
        if (linkTypeLinks != null) {
            linkTypeLinkIds.addAll(linkTypeLinks.keySet());
        }
        return linkTypeLinkIds;
    }

    @Override
    public List<SnapshotItem> getLinkItemsByLinkId(SnapshotItem.LinkType linkType, String linkId) {
        List<SnapshotItem> linkTypeLinkIdLinks;
        ArrayList<SnapshotItem> allLinks = new ArrayList<SnapshotItem>();
        Map<String, List<SnapshotItem>> linkTypeLinks = this.linkItems.get((Object)linkType);
        if (linkTypeLinks != null && (linkTypeLinkIdLinks = linkTypeLinks.get(linkId)) != null) {
            allLinks.addAll(linkTypeLinkIdLinks);
        }
        return allLinks;
    }

    private void addLink(SDISnapshotItem childItem, SnapshotItem.LinkType linkType, String linkId) {
        Map linkTypeMap = this.linkItems.getOrDefault((Object)linkType, new LinkedHashMap());
        List linkTypeLinks = linkTypeMap.getOrDefault(linkId, new ArrayList());
        linkTypeMap.put(linkId, linkTypeLinks);
        linkTypeLinks.add(childItem);
        this.linkItems.put(linkType, linkTypeMap);
        childItem.setParent(this);
        childItem.setLinkId(linkId);
        childItem.setLinkType(linkType);
    }

    public void addLink(SDISnapshotItem childItem, SnapshotItem.LinkType linkType, String linkId, PropertyList parentLinkProps) {
        this.addLink(childItem, linkType, linkId);
        childItem.parentLinkProps = parentLinkProps;
    }

    @Override
    public String toString() {
        return this.toString(false, false);
    }

    public String toString(boolean ignorePolicyNode) {
        return this.toString(ignorePolicyNode, false);
    }

    public String toString(boolean ignorePolicyNode, boolean prettyPrint) {
        if (prettyPrint) {
            return this.sdcId + "-" + this.keyId1 + (this.keyId2 != null && this.keyId2.length() > 0 ? ";" + this.keyId2 : "") + (this.keyId3 != null && this.keyId3.length() > 0 ? ";" + this.keyId3 : "") + (ignorePolicyNode ? "" : "-" + this.policyNodeId);
        }
        return this.sdcId + ";" + this.keyId1 + ";" + this.keyId2 + ";" + this.keyId3 + (ignorePolicyNode ? "" : ";" + this.policyNodeId);
    }

    @Override
    public boolean equals(Object o) {
        return this.equals(o, false);
    }

    public boolean equals(Object o, boolean ignorePolicyNode) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SDISnapshotItem)) {
            return false;
        }
        SDISnapshotItem that = (SDISnapshotItem)o;
        return Objects.equals((Object)this.getType(), (Object)that.getType()) && Objects.equals(this.sdcId, that.sdcId) && Objects.equals(this.keyId1, that.keyId1) && Objects.equals(this.keyId2, that.keyId2) && Objects.equals(this.keyId3, that.keyId3) && (ignorePolicyNode || Objects.equals(this.policyNodeId, that.policyNodeId));
    }

    public int hashCode() {
        return Objects.hash(this.sdcId, this.keyId1, this.keyId2, this.keyId3, this.policyNodeId);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("snapshotitemid", this.toString());
            jsonObject.put("includedfortransfer", this.isIncludedForTransfer());
            jsonObject.put("linkid", this.getLinkId());
            jsonObject.put("linktype", this.getLinkType() != null ? this.getLinkType().getCode() : "");
            jsonObject.put("status", this.isLoadedSuccessfully() ? "SUCCESS" : "FAILURE");
            if (!this.isLoadedSuccessfully()) {
                jsonObject.put("statusmessage", this.getStatusMessage());
            }
            jsonObject.put("isDeleted", this.isDeleted());
            jsonObject.put("isCurrentLink", this.isCurrentLink());
            if (this.parentLinkProps != null) {
                jsonObject.put("parentLinkProps", this.parentLinkProps.toJSONObject(false, true));
            }
            for (SnapshotItem.LinkType linkType : SnapshotItem.LinkType.values()) {
                JSONObject linkTypeJObj = new JSONObject();
                Map linkItemsMap = this.linkItems.getOrDefault((Object)linkType, new HashMap());
                for (String linkId : linkItemsMap.keySet()) {
                    JSONArray linksJArr = new JSONArray();
                    for (SnapshotItem linkItem : (List)linkItemsMap.get(linkId)) {
                        linksJArr.put(linkItem.toJSONObject());
                    }
                    linkTypeJObj.put(linkId, linksJArr);
                }
                jsonObject.put("links_" + linkType.getCode(), linkTypeJObj);
            }
        }
        catch (JSONException e) {
            logger.error("JSON Exception: ", e);
        }
        return jsonObject;
    }

    @Override
    public Snapshot.Type getType() {
        return Snapshot.Type.SDI;
    }

    protected void fromJSONInstance(JSONObject jsonObject) throws JSONException, SapphireException {
        String snapshotItemId = jsonObject.getString("snapshotitemid");
        String[] snapshotItemParts = StringUtil.split(snapshotItemId, ";");
        if (snapshotItemParts.length < 5) {
            throw new SapphireException("Invalid JSON format.");
        }
        this.setSdcId(snapshotItemParts[0]);
        this.setKeyId1(snapshotItemParts[1]);
        this.setKeyId2(snapshotItemParts[2]);
        this.setKeyId3(snapshotItemParts[3]);
        this.setPolicyNodeId(snapshotItemParts[4]);
        this.setIncludedForTransfer(jsonObject.getBoolean("includedfortransfer"));
        this.setLinkId(jsonObject.getString("linkid"));
        if (jsonObject.getString("linktype").length() > 0) {
            this.setLinkType(SnapshotItem.LinkType.getByCode(jsonObject.getString("linktype")));
        }
        this.setLoadedSuccessfully("SUCCESS".equals(jsonObject.getString("status")));
        try {
            this.setStatusMessage(jsonObject.getString("statusmessage"));
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        this.setDeleted(jsonObject.getBoolean("isDeleted"));
        try {
            this.setCurrentLink(jsonObject.getBoolean("isCurrentLink"));
        }
        catch (JSONException e) {
            this.setCurrentLink(false);
        }
        if (jsonObject.has("parentLinkProps")) {
            PropertyList props = new PropertyList();
            props.setJSONString(jsonObject.getJSONObject("parentLinkProps").toString(), true);
            this.parentLinkProps = props;
        }
        for (SnapshotItem.LinkType linkType : SnapshotItem.LinkType.values()) {
            JSONArray linkNames;
            JSONObject linkJSON = jsonObject.getJSONObject("links_" + linkType.getCode());
            if (linkJSON == null || (linkNames = linkJSON.names()) == null) continue;
            for (int i = 0; i < linkNames.length(); ++i) {
                String linkName = (String)linkNames.get(i);
                JSONArray linkValuesArr = linkJSON.getJSONArray(linkName);
                if (linkValuesArr == null) continue;
                for (int j = 0; j < linkValuesArr.length(); ++j) {
                    JSONObject linkItemJSON = (JSONObject)linkValuesArr.get(j);
                    boolean isLinkPTree = linkItemJSON.getString("snapshotitemid").startsWith("PropertyTree;");
                    SDISnapshotItem linkSnapshotItem = isLinkPTree ? new PropertyTreeSnapshotItem() : new SDISnapshotItem();
                    linkSnapshotItem.setParent(this);
                    this.addLink(linkSnapshotItem, linkType, linkName);
                    linkSnapshotItem.fromJSONInstance(linkItemJSON);
                }
            }
        }
    }

    public static SDISnapshotItem fromJSON(JSONObject jsonObject) {
        SDISnapshotItem sdiSnapshotItem = new SDISnapshotItem();
        try {
            sdiSnapshotItem.fromJSONInstance(jsonObject);
        }
        catch (JSONException e) {
            logger.error("JSON Exception: ", e);
        }
        catch (SapphireException e) {
            logger.error("Exception: ", e);
        }
        return sdiSnapshotItem;
    }

    public static SDISnapshotItem fromJSON(String jsonString) {
        SDISnapshotItem sdiSnapshotItem = new SDISnapshotItem();
        try {
            sdiSnapshotItem = SDISnapshotItem.fromJSON(new JSONObject(jsonString));
        }
        catch (JSONException e) {
            logger.error("JSON Exception: ", e);
        }
        return sdiSnapshotItem;
    }

    public boolean hasSameAncestor(boolean ignorePolicyNode) {
        for (SDISnapshotItem parent = this.getParent(); parent != null; parent = parent.getParent()) {
            if (!(ignorePolicyNode ? parent.equals(this, true) : parent.equals(this))) continue;
            return true;
        }
        return false;
    }

    private SDISnapshotItem getTop() {
        SDISnapshotItem current = this;
        for (SDISnapshotItem parent = current.getParent(); parent != null; parent = parent.getParent()) {
            current = parent;
        }
        return current;
    }

    public PropertyList getPolicyNodeProps() throws SapphireException {
        SDISnapshot container = this.getContainer();
        if (container == null) {
            throw new SapphireException("SnapshotItem is not linked to a Container Snapshot yet.");
        }
        return container.getPolicyNodeProps(this.getPolicyNodeId());
    }

    private void extractParentLinkProps() throws SapphireException {
        SDISnapshotItem parent = this.getParent();
        if (parent == null) {
            return;
        }
        PropertyList parentPolicyNodeProps = parent.getPolicyNodeProps();
        PropertyListCollection linkTypePropsCollection = CMTPolicy.getAssociatedSDIFilteredCollection(true, this.getLinkType(), parentPolicyNodeProps);
        for (int i = 0; i < linkTypePropsCollection.size(); ++i) {
            PropertyList props = linkTypePropsCollection.getPropertyList(i);
            if (!CMTPolicy.getAssociatedSDILinkInfo(props).equals(this.getLinkId())) continue;
            this.parentLinkProps = props;
            return;
        }
    }

    public PropertyList getParentLinkProps() throws SapphireException {
        if (this.parentLinkProps == null) {
            this.extractParentLinkProps();
        }
        return this.parentLinkProps;
    }

    @Override
    public SDIData getSDIData() {
        SDISnapshot container = this.getContainer();
        if (container == null) {
            return null;
        }
        return container.getSDIData(this);
    }

    public boolean isExists(Collection<SDISnapshotItem> collection, boolean ignorePolicyNode) {
        for (SDISnapshotItem sdiSnapshotItem : collection) {
            if (!this.equals(sdiSnapshotItem, ignorePolicyNode)) continue;
            return true;
        }
        return false;
    }

    public boolean isExistsDifferentPolicy(Collection<SDISnapshotItem> collection) {
        for (SDISnapshotItem snapshotItem : collection) {
            if (!this.equals(snapshotItem, true) || snapshotItem.getPolicyNodeId().equals(this.getPolicyNodeId())) continue;
            return true;
        }
        return false;
    }
}


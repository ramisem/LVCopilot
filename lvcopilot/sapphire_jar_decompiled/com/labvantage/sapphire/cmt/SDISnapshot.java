/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class SDISnapshot
implements Snapshot {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private static final String LOGNAME = "SDISnapshot";
    protected Logger logger = new Logger(new LogContext("SDISnapshot", "(none)"));
    private SDISnapshotItem primaryItem;
    private Map<SDISnapshotItem, SDIData> sdiDataMap = new HashMap<SDISnapshotItem, SDIData>();
    private Map<String, PropertyList> policyNodeMap = new HashMap<String, PropertyList>();
    private Map<SDISnapshotItem, SDISnapshot> transferSnapshots = new HashMap<SDISnapshotItem, SDISnapshot>();
    private boolean isDevMode = false;
    private String compCode = "";
    private SnapshotPackage snapshotPackage;
    private Map<String, String> idMap = new HashMap<String, String>();

    public SDISnapshot() {
    }

    public SDISnapshot(SDISnapshotItem requestedItem) {
        this.setSnapshotItem(requestedItem);
    }

    public String getSDCId() {
        return this.primaryItem.getSDCId();
    }

    public String getKeyId1() {
        return this.primaryItem.getKeyId1();
    }

    public String getKeyId2() {
        return this.primaryItem.getKeyId2();
    }

    public String getKeyId3() {
        return this.primaryItem.getKeyId3();
    }

    public String getPolicyNodeId() {
        return this.primaryItem.getPolicyNodeId();
    }

    public void setPolicyNodeMap(HashMap<String, PropertyList> policyNodeMap) {
        this.policyNodeMap = policyNodeMap;
    }

    public Map<String, PropertyList> getPolicyNodeMap() {
        return this.policyNodeMap;
    }

    public String getCompCode() {
        return this.compCode;
    }

    public void setCompCode(String compCode) {
        this.compCode = compCode;
    }

    public SnapshotPackage getSnapshotPackage() {
        return this.snapshotPackage;
    }

    public void setSnapshotPackage(SnapshotPackage snapshotPackage) {
        this.snapshotPackage = snapshotPackage;
    }

    public boolean isFromSnapshotPackage() {
        return this.snapshotPackage != null;
    }

    public boolean isSDCSnapshotIncluded() {
        boolean val = false;
        if (this.isFromSnapshotPackage()) {
            SnapshotPackage snapshotPackage = this.getSnapshotPackage();
            for (SnapshotItem snapshotItem : snapshotPackage.getSnapshotItems()) {
                SDISnapshotItem sdiSnapshotItem = (SDISnapshotItem)snapshotItem;
                if (!"SDC".equals(sdiSnapshotItem.getSDCId()) || !this.getSDCId().equals(sdiSnapshotItem.getKeyId1())) continue;
                val = true;
                break;
            }
        }
        return val;
    }

    @Override
    public SDIData getSDIData() {
        return this.sdiDataMap.get(this.primaryItem);
    }

    public SDIData getSDIData(SDISnapshotItem snapshotItem) {
        return this.sdiDataMap.get(snapshotItem);
    }

    public void addPolicyProps(String policyNodeId, PropertyList props) {
        this.policyNodeMap.put(policyNodeId, props);
    }

    public PropertyList getPolicyNodeProps(String policyNodeId) {
        if ("Sapphire Custom".equals(policyNodeId)) {
            return this.policyNodeMap.get("Sapphire Custom;" + this.getSDCId());
        }
        return this.policyNodeMap.get(policyNodeId);
    }

    public void addSDIData(SDISnapshotItem sdiSnapshotItem, SDIData sdiData) {
        this.sdiDataMap.put(sdiSnapshotItem, sdiData);
    }

    @Override
    public String toXML() {
        SDISnapshotItem reqSnapshotItem = this.getSnapshotItem();
        StringBuffer xml = new StringBuffer();
        xml.append("<snapshot type='" + (Object)((Object)this.getType()) + "' id='" + StringUtil.escapeXMLAttributeValue(reqSnapshotItem.toString()) + "'>\n");
        String indentStr = "  ";
        xml.append("<sdisnapshot sdcid='" + StringUtil.escapeXMLAttributeValue(reqSnapshotItem.getSDCId()) + "' keyid1='" + StringUtil.escapeXMLAttributeValue(reqSnapshotItem.getKeyId1()) + "' keyid2='" + StringUtil.escapeXMLAttributeValue(reqSnapshotItem.getKeyId2()) + "' keyid3='" + StringUtil.escapeXMLAttributeValue(reqSnapshotItem.getKeyId3()) + "' policynodeid='" + StringUtil.escapeXMLAttributeValue(reqSnapshotItem.getPolicyNodeId()) + "' devmode='" + (this.isDevMode() ? "Y" : "N") + "' compcode='" + StringUtil.escapeXMLAttributeValue(this.getCompCode()) + "'>\n");
        xml.append("<sditree>\n");
        try {
            xml.append(indentStr + "<![CDATA[" + reqSnapshotItem.toJSONObject().toString(4) + "]]>\n");
        }
        catch (Exception e) {
            this.logger.error("Error in serializing SDI Link tree." + e.getMessage(), e);
        }
        xml.append("</sditree>\n");
        xml.append("<sdidatalist>\n");
        for (SDISnapshotItem snapshotItem : this.sdiDataMap.keySet()) {
            xml.append(indentStr + "<sdidatalistitem id='" + StringUtil.escapeXMLAttributeValue(snapshotItem.toString()) + "'>\n");
            SDIData snapshotData = this.sdiDataMap.get(snapshotItem);
            xml.append(snapshotData.toXML(4, true, true));
            xml.append(indentStr + "</sdidatalistitem>\n");
        }
        xml.append("</sdidatalist>\n");
        xml.append("<policynodelist>\n");
        for (String nodeId : this.policyNodeMap.keySet()) {
            xml.append(indentStr + "<policynodelistitem id='" + StringUtil.escapeXMLAttributeValue(nodeId) + "'>\n");
            try {
                xml.append(indentStr + indentStr + "<![CDATA[" + this.policyNodeMap.get(nodeId).toJSONObject(false, true).toString(4) + "]]>\n");
            }
            catch (JSONException e) {
                this.logger.error("Error in serializing Policy Node." + e.getMessage(), e);
            }
            xml.append(indentStr + "</policynodelistitem>\n");
        }
        xml.append("</policynodelist>\n");
        xml.append("</sdisnapshot>\n");
        xml.append("</snapshot>\n");
        return xml.toString();
    }

    public String toHTML() {
        StringBuffer xml = new StringBuffer();
        String indentStr = "  ";
        xml.append("<div style=\"border:1px solid red\" id=\"" + this.getSnapshotItem().toString() + "\">\n");
        xml.append("<p>Primary:" + this.getSnapshotItem().toString() + "</p>");
        try {
            xml.append("<textarea style=\"width:600px;height:400px\">" + this.sdiDataMap.get(this.getSnapshotItem()).toJSONString() + "</textarea>");
        }
        catch (Exception e) {
            this.logger.error("Error in serializing SDI Link tree." + e.getMessage(), e);
        }
        xml.append("</div>\n");
        for (SDISnapshotItem snapshotItem : this.sdiDataMap.keySet()) {
            xml.append(indentStr + "<div style=\"border:1px solid green\" id='" + snapshotItem + "'>");
            xml.append("<p>Embedded :" + snapshotItem.toString() + "</p>");
            xml.append("<textarea style=\"width:600px;height:400px\">\n");
            xml.append(this.sdiDataMap.get(snapshotItem).toJSONString());
            xml.append(indentStr + "</textarea></div>\n");
        }
        return xml.toString();
    }

    @Override
    public Snapshot.Type getType() {
        return Snapshot.Type.SDI;
    }

    @Override
    public SDISnapshotItem getSnapshotItem() {
        return this.primaryItem;
    }

    public void setSnapshotItem(SDISnapshotItem requestedSDI) {
        this.primaryItem = requestedSDI;
        requestedSDI.setContainer(this);
    }

    public boolean isDevMode() {
        return this.isDevMode;
    }

    public void setDevMode(boolean isDevMode) {
        this.isDevMode = isDevMode;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SDISnapshot that = (SDISnapshot)o;
        return Objects.equals(this.primaryItem, that.primaryItem);
    }

    public int hashCode() {
        return Objects.hash(this.primaryItem);
    }

    public void addTransferSnapshot(SDISnapshot sdiSnapshot) {
        this.transferSnapshots.put(sdiSnapshot.getSnapshotItem(), sdiSnapshot);
    }

    public List<SDISnapshot> getTransferSnapshots() {
        return new ArrayList<SDISnapshot>(this.transferSnapshots.values());
    }

    public boolean hasAuditColumns() {
        DataSet primary = this.getSDIData().getDataset("primary");
        if (primary != null) {
            return primary.isValidColumn("createby");
        }
        return false;
    }

    public void setKeyValue(String key, String value) {
        this.idMap.put(key, value);
    }

    public String getKeyValue(String key) {
        return this.idMap.get(key);
    }

    public boolean containsKey(String key) {
        return this.idMap.containsKey(key);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt;

import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.cmt.Snapshot;

public class PropertyTreeSnapshotItem
extends SDISnapshotItem {
    private static final String LOGNAME = "SDISnapshot";
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    protected static Logger logger = new Logger(new LogContext("SDISnapshot", "(none)"));
    private String nodeId = "";
    private String renamedNodeId = "";

    public PropertyTreeSnapshotItem() {
        this.setSdcId("PropertyTree");
        this.setKeyId2("");
        this.setKeyId3("");
        this.setPolicyNodeId("Sapphire Custom");
    }

    public PropertyTreeSnapshotItem(String xmlId) throws IllegalArgumentException {
        super(xmlId);
        this.setKeyId2("");
        this.setKeyId3("");
        this.setPolicyNodeId("Sapphire Custom");
        String[] idParts = StringUtil.split(xmlId, ";");
        if (idParts.length < 4) {
            throw new IllegalArgumentException("Invalid Id structure found: " + xmlId);
        }
        this.setNodeId(idParts[5]);
    }

    public String getPropertyTreeId() {
        return this.getKeyId1();
    }

    public void setPropertyTreeId(String propertyTreeId) {
        this.setKeyId1(propertyTreeId);
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getRenamedNodeId() {
        return this.renamedNodeId;
    }

    public void setRenamedNodeId(String renamedNodeId) {
        this.renamedNodeId = renamedNodeId;
    }

    public String getExtendsNodeId() throws SapphireException {
        PropertyTreeSnapshot container = (PropertyTreeSnapshot)this.getContainer();
        if (container == null) {
            throw new SapphireException("SnapshotItem is not linked to a Container Snapshot yet.");
        }
        return container.getExtendsNodeId();
    }

    @Override
    public SDIData getSDIData() {
        SDISnapshot container = this.getContainer();
        if (container == null) {
            return null;
        }
        return container.getSDIData();
    }

    public PropertyTree getPropertyTree() throws SapphireException {
        PropertyTreeSnapshot container = (PropertyTreeSnapshot)this.getContainer();
        if (container == null) {
            throw new SapphireException("SnapshotItem is not linked to a Container Snapshot yet.");
        }
        return container.getPropertyTree();
    }

    @Override
    protected SDISnapshot getContainer() {
        return this.container;
    }

    @Override
    public Snapshot.Type getType() {
        return Snapshot.Type.PROPERTYTREE;
    }

    @Override
    public String toString() {
        return this.toString(false);
    }

    @Override
    public String toString(boolean ignorePolicyNode) {
        return super.toString(ignorePolicyNode) + ";" + this.nodeId;
    }

    @Override
    public boolean equals(Object o) {
        return this.equals(o, false);
    }

    @Override
    public boolean equals(Object o, boolean ignoreNode) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertyTreeSnapshotItem)) {
            return false;
        }
        PropertyTreeSnapshotItem that = (PropertyTreeSnapshotItem)o;
        return Objects.equals((Object)this.getType(), (Object)that.getType()) && Objects.equals(this.getSDCId(), that.getSDCId()) && Objects.equals(this.getKeyId1(), that.getKeyId1()) && (ignoreNode || Objects.equals(this.nodeId, that.nodeId));
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject superJSON = super.toJSONObject();
        try {
            superJSON.put("nodeid", this.getNodeId());
            if (this.getRenamedNodeId().length() > 0) {
                superJSON.put("renamedNodeId", this.getRenamedNodeId());
            }
        }
        catch (JSONException e) {
            logger.error("JSON Exception: ", e);
        }
        return superJSON;
    }

    public static PropertyTreeSnapshotItem fromJSON(JSONObject jsonObject) {
        PropertyTreeSnapshotItem sdiSnapshotItem = new PropertyTreeSnapshotItem();
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

    public static PropertyTreeSnapshotItem fromJSON(String jsonString) {
        PropertyTreeSnapshotItem sdiSnapshotItem = new PropertyTreeSnapshotItem();
        try {
            sdiSnapshotItem = PropertyTreeSnapshotItem.fromJSON(new JSONObject(jsonString));
        }
        catch (JSONException e) {
            logger.error("JSON Exception: ", e);
        }
        return sdiSnapshotItem;
    }

    @Override
    protected void fromJSONInstance(JSONObject jsonObject) throws JSONException, SapphireException {
        super.fromJSONInstance(jsonObject);
        this.setNodeId(jsonObject.getString("nodeid"));
        if (jsonObject.has("renamedNodeId")) {
            this.setNodeId(jsonObject.getString("renamedNodeId"));
        }
    }
}


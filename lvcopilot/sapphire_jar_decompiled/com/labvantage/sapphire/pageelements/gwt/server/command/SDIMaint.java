/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.gwt.server.JSONSaveRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIDataItemTable;
import com.labvantage.sapphire.pageelements.gwt.server.command.Table;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.DDTConstants;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.command.TagRequestPropertyHandler;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDIMaint
extends SDIData
implements JSONable,
DDTConstants {
    private PropertyList sdcProps;
    private int keycols = 1;
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";
    private PropertyListCollection sdcLinks;
    private PropertyListCollection detailLinks;

    public SDIMaint(PropertyList sdcProps, SDIData sdiData) {
        this.initSDCProps(sdcProps);
        this.initSDIData(sdiData, null);
    }

    public SDIMaint(PropertyList sdcProps, SDIData sdiData, Set datasetsToInclude) {
        this.initSDCProps(sdcProps);
        this.initSDIData(sdiData, datasetsToInclude);
    }

    private void initSDIData(SDIData sdiData, Set datasetsToInclude) {
        if (sdiData != null) {
            this.setRsetid(sdiData.getRsetid());
            this.setRequestStatus(sdiData.getRequestStatus());
            this.setQualifiedRows(sdiData.getQualifiedRows());
            this.setupKeyids(sdiData.getDataset("primary"));
            Set datasets = sdiData.getDatasets();
            for (String datasetname : datasets) {
                if (datasetsToInclude != null && !datasetsToInclude.contains(datasetname)) continue;
                Table table = new Table(sdiData.getKeys(datasetname), sdiData.getDataset(datasetname));
                this.setDataset(datasetname, table);
            }
        }
    }

    private void setupKeyids(DataSet primary) {
        if (primary != null) {
            this.keyid1 = primary.getColumnValues(this.sdcProps.getProperty("keycolid1"), ";");
            this.keyid2 = primary.getColumnValues(this.sdcProps.getProperty("keycolid2"), ";");
            this.keyid3 = primary.getColumnValues(this.sdcProps.getProperty("keycolid3"), ";");
        }
    }

    public SDIMaint(PropertyList sdcProps) {
        this.initSDCProps(sdcProps);
    }

    public SDIMaint(String jsonString) {
        try {
            this.parseJSONString(jsonString);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public SDIMaint(SDCProcessor sdcProcessor, String jsonString) {
        try {
            JSONObject jsonObject = this.parseJSONString(jsonString);
            this.initSDCProps(sdcProcessor.getPropertyList(jsonObject.getString("sdcid")));
            this.setupKeyids(this.getDataset("primary"));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public SDIMaint(PropertyList sdcProps, String jsonString) {
        this.initSDCProps(sdcProps);
        try {
            this.parseJSONString(jsonString);
            this.setupKeyids(this.getDataset("primary"));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private JSONObject parseJSONString(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(new JSONTokener(jsonString));
        this.setSdcid(jsonObject.getString("sdcid"));
        JSONObject datasets = jsonObject.getJSONObject("datasets");
        JSONArray tables = jsonObject.getJSONArray("tables");
        for (int i = 0; i < tables.length(); ++i) {
            String tablename = tables.getString(i);
            this.addTable(tablename, datasets.getJSONObject(tablename));
        }
        return jsonObject;
    }

    public void setSDCProps(PropertyList sdcProps) {
        this.initSDCProps(sdcProps);
        this.setupKeyids(this.getDataset("primary"));
    }

    private void initSDCProps(PropertyList sdcProps) {
        this.sdcProps = sdcProps;
        this.setSdcid(sdcProps.getProperty("sdcid"));
        this.keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        this.setPrimaryKeyCols(sdcProps.getProperty("keycolid1"), sdcProps.getProperty("keycolid2"), sdcProps.getProperty("keycolid3"));
        this.sdcLinks = sdcProps.getCollection("links");
        int links = this.sdcLinks.size();
        String[] linkids = new String[links];
        String[] linktables = new String[links];
        String[][] linktablekeys = new String[links][];
        for (int link = 0; link < links; ++link) {
            PropertyList linkProps = this.sdcLinks.getPropertyList(link);
            linkids[link] = linkProps.getProperty("linkid");
            linktables[link] = linkProps.getProperty("linktableid");
            int keycolcount = Integer.parseInt(linkProps.getProperty("keycolcount"));
            linktablekeys[link] = new String[keycolcount];
            for (int key = 1; key <= keycolcount; ++key) {
                linktablekeys[link][key - 1] = linkProps.getProperty("keycolid" + String.valueOf(key));
            }
        }
        this.setLinks(linkids, linktables);
        if (linktables != null) {
            for (int i = 0; i < linktables.length; ++i) {
                this.setLinkTableKeys(linktables[i], linktablekeys[i]);
            }
        }
        this.detailLinks = sdcProps.getCollection("detaillinks");
        int detaillinks = this.detailLinks.size();
        int sizeofArr = 0;
        for (int link = 0; link < detaillinks; ++link) {
            PropertyList linkProps = this.detailLinks.getPropertyList(link);
            if (!linkProps.getProperty("linktype").equals("D")) continue;
            ++sizeofArr;
        }
        String[] detailLinkIds = new String[sizeofArr];
        String[] detailDetailLinkIds = new String[sizeofArr];
        String[] detaillinktables = new String[sizeofArr];
        String[][] detaillinktablekeys = new String[sizeofArr][];
        int arrele = -1;
        for (int link = 0; link < detaillinks; ++link) {
            PropertyList linkProps = this.detailLinks.getPropertyList(link);
            if (!linkProps.getProperty("linktype").equals("D")) continue;
            detailLinkIds[++arrele] = linkProps.getProperty("linkid");
            detailDetailLinkIds[arrele] = linkProps.getProperty("detaillinkid");
            detaillinktables[arrele] = linkProps.getProperty("linktableid");
            int keycolcount = Integer.parseInt(linkProps.getProperty("keycolcount"));
            detaillinktablekeys[arrele] = new String[keycolcount];
            for (int key = 1; key <= keycolcount; ++key) {
                detaillinktablekeys[arrele][key - 1] = linkProps.getProperty("keycolid" + String.valueOf(key));
            }
        }
        if (detailDetailLinkIds != null) {
            this.setDetailLinks(detailLinkIds, detailDetailLinkIds, detaillinktables);
        }
        if (detaillinktables != null) {
            for (int i = 0; i < detaillinktables.length; ++i) {
                this.setDetailLinkTableKeys(detaillinktables[i], detaillinktablekeys[i]);
            }
        }
    }

    public void save(ActionService actionService, AuditService auditService, ErrorHandler errorHandler, PropertyHandler logger) throws SapphireException, ServiceException {
        this.save(actionService, auditService, errorHandler, logger, null);
    }

    public void save(ActionService actionService, AuditService auditService, ErrorHandler errorHandler, PropertyHandler logger, HashMap extraProps) throws SapphireException, ServiceException {
        HashMap sdiprops = new HashMap();
        PropertyList props = new PropertyList();
        PropertyList editprops = new PropertyList();
        PropertyList delprops = new PropertyList();
        PropertyList addprops = new PropertyList();
        PropertyList returnkeylist = new PropertyList();
        String traceLogIdStr = "";
        String sdcid = this.getSdcid();
        if (extraProps != null && extraProps.containsKey("auditreason")) {
            props.setProperty("auditreason", extraProps.get("auditreason") != null ? extraProps.get("auditreason").toString() : "");
            props.setProperty("auditactivity", extraProps.get("auditactivity") != null ? extraProps.get("auditactivity").toString() : "");
            props.setProperty("auditsignedflag", extraProps.get("auditsignedflag") != null ? extraProps.get("auditsignedflag").toString() : "");
            props.setProperty("buttonactivity", "");
        }
        String separator = ";";
        try {
            if (separator == null || separator.length() == 0) {
                separator = ";";
            }
        }
        catch (Exception e) {
            separator = ";";
        }
        Set datasets = this.getDatasets();
        for (String datasetname : datasets) {
            Table dataset = (Table)this.getDataset(datasetname);
            if (dataset == null || "dataitem".equals(datasetname) && (!"dataitem".equals(datasetname) || ((SDIDataItemTable)this.getDataset("dataitem")).getDataentry() != null)) continue;
            String[] keyCols = this.getKeys(datasetname);
            String linkid = this.getLinkid(datasetname);
            String detailLinkId = "";
            if (linkid == null || linkid.length() == 0) {
                linkid = this.getDetailLinkid(datasetname);
                detailLinkId = this.getDetailDetailLinkid(datasetname);
            }
            dataset.getSaveProps(sdcid, datasetname.equals("primary"), linkid, detailLinkId, keyCols, editprops, delprops, addprops, separator);
            traceLogIdStr = TagRequestPropertyHandler.saveDataset(sdiprops, errorHandler, traceLogIdStr, auditService, returnkeylist, props, editprops, delprops, addprops, null, linkid, detailLinkId, sdcid, this.sdcProps.getProperty("auditedflag"), this.sdcProps.getProperty("auditpromptflag"), actionService, linkid != null && linkid.length() > 0, detailLinkId != null && detailLinkId.length() > 0, datasetname, keyCols, separator, logger);
            addprops.clear();
            editprops.clear();
            delprops.clear();
        }
        if (this.getDataset("dataitem") != null && ((SDIDataItemTable)this.getDataset("dataitem")).getDataentry() != null) {
            try {
                ActionBlock deActionBlock = JSONSaveRequest.buildDataEntryActionBlock(this, extraProps, actionService.getConnectionId());
                if (deActionBlock != null) {
                    actionService.processActionBlock(deActionBlock);
                }
            }
            catch (Exception e) {
                throw new ServiceException(e);
            }
        }
    }

    public int getKeycols() {
        return this.keycols;
    }

    public String getKeyid1() {
        return this.keyid1;
    }

    public String getKeyid2() {
        return this.keyid2;
    }

    public String getKeyid3() {
        return this.keyid3;
    }

    public void addTable(String tableid, JSONObject jsonObject) {
        if ("dataitem".equals(tableid)) {
            SDIDataItemTable table = new SDIDataItemTable(this.getKeys(tableid), jsonObject);
            this.setDataset(tableid, table);
        } else {
            Table table = new Table(this.getKeys(tableid), jsonObject);
            this.setDataset(tableid, table);
        }
    }

    public Table createTable(String tableid) {
        Table table = new Table(this.getKeys(tableid));
        String columnidProperty = "columnid";
        String datatypeProperty = "datatype";
        PropertyListCollection columns = null;
        if (tableid.equals("primary") || tableid.equals(this.sdcProps.getProperty("tableid"))) {
            columns = this.sdcProps.getCollection("columns");
        } else {
            String linkid = this.getLinkid(tableid);
            if (linkid != null && linkid.length() > 0) {
                PropertyList link = this.sdcLinks.getPropertyList(linkid);
                columns = link.getCollection("linkcolumns");
                columnidProperty = "linkcolumnid";
                datatypeProperty = "linkdatatype";
            }
        }
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String datatype = column.getProperty(datatypeProperty);
            table.addColumn(column.getProperty(columnidProperty), datatype.equals("C") ? 0 : (datatype.equals("N") || datatype.equals("R") ? 1 : (datatype.equals("D") ? 2 : 3)));
        }
        this.setDataset(tableid, table);
        return table;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObj = new JSONObject();
        try {
            int keycolumns = Integer.parseInt(this.sdcProps.getProperty("keycolumns"));
            jsonObj.put("sdcid", this.getSdcid());
            jsonObj.put("keyid1", this.getKeyid1());
            jsonObj.put("keyid2", this.getKeyid2());
            jsonObj.put("keyid3", this.getKeyid3());
            jsonObj.put("rsetid", this.getRsetid());
            jsonObj.put("requeststatus", this.getRequestStatus());
            jsonObj.put("qualifiedrows", this.getQualifiedRows());
            Set ds = this.getDatasets();
            Iterator it = ds.iterator();
            JSONObject datasets = new JSONObject();
            JSONObject keys = new JSONObject();
            JSONArray tables = new JSONArray();
            while (it.hasNext()) {
                String name = it.next().toString();
                tables.put(name);
                JSONObject table = ((Table)this.getDataset(name)).toJSONObject();
                datasets.put(name, table);
                keys.put(name, JSONUtil.toJSONArray(this.getKeys(name)));
            }
            jsonObj.put("datasets", datasets);
            jsonObj.put("tables", tables);
            jsonObj.put("primarytable", this.sdcProps.getProperty("tableid"));
            jsonObj.put("keycolumns", keycolumns);
            jsonObj.put("keys", keys);
        }
        catch (Exception e) {
            Trace.logError("Failed to convert SDIMaint object to JSONObject. Reason: " + e.getMessage(), e);
        }
        return jsonObj;
    }

    public HashMap getTables() {
        return this.sdiData;
    }

    @Override
    public String toJSONString() {
        return this.toJSONObject().toString();
    }
}


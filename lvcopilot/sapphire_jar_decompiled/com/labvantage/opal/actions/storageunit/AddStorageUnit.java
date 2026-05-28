/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddStorageUnit
extends BaseAction
implements sapphire.action.AddStorageUnit {
    private Map<String, PropertyList> localCache = new HashMap<String, PropertyList>();

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        DataSet primaryds;
        String columnid;
        long start = System.currentTimeMillis();
        String storageunittype = actionProps.getProperty("storageunittype", "");
        int copies = Integer.parseInt(actionProps.getProperty("copies", "1"));
        if (OpalUtil.isEmpty(storageunittype)) {
            throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Missing Input"), this.getTranslationProcessor().translate("Missing required action input") + ": " + storageunittype);
        }
        PropertyList storageUnitTypeProps = StorageUnitUtil.getDefinition(this.getQueryProcessor(), storageunittype);
        String sdcid = storageUnitTypeProps.getPropertyListNotNull("template").getProperty("sdcid");
        String propertytreeid = storageUnitTypeProps.getProperty("propertytreeid");
        if (OpalUtil.isEmpty(propertytreeid)) {
            throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Missing Storage Unit Type"), this.getTranslationProcessor().translate("Storage unit type does not exist") + ": " + storageunittype);
        }
        if (actionProps.getProperty("tracelogid", "").length() == 0 && actionProps.getProperty("auditreason", "").length() == 0) {
            String auditreason = this.getTranslationProcessor().translate("New " + this.getSDCProcessor().getProperty(sdcid, "singular") + " Added");
            actionProps.setProperty("auditreason", auditreason);
        }
        DataSet ds = new DataSet();
        int sulevel = 0;
        for (int i = 0; i < copies; ++i) {
            String heirarchyid = "USUH_" + (i + 1);
            int row = ds.addRow();
            ds.setString(row, "propnodeid", propertytreeid + "|" + storageunittype);
            ds.setString(row, "propertytreeid", propertytreeid);
            ds.setString(row, "nodeid", storageunittype);
            ds.setString(row, "storageunittype", storageunittype);
            ds.setString(row, "sulevel", String.valueOf(sulevel));
            ds.setString(row, "maxtiallowed", actionProps.getProperty("maxtiallowed", storageUnitTypeProps.getProperty("maxtiallowed", "0")));
            ds.setString(row, "suparentid", "USUH");
            ds.setString(row, "moveableflag", storageUnitTypeProps.getProperty("moveable", "N"));
            ds.setString(row, "parentid", "");
            ds.setString(row, "suhierarchyid", heirarchyid);
            ds.setString(row, "storageunitlabel", "");
            ds.setString(row, "storageenvid", "");
            for (Object property : actionProps.keySet()) {
                if (!(property instanceof String) || !((String)property).startsWith("storageunit_")) continue;
                columnid = ((String)property).substring(12);
                String datatype = this.getSDCProcessor().getSDCColumnProperty("StorageUnitSDC", columnid, "datatype");
                if (!"C".equals(datatype) && !"N".equals(datatype)) continue;
                ds.setString(row, columnid, actionProps.getProperty((String)property, ""));
            }
            this.addMandatoryChild(storageUnitTypeProps, ds, sulevel + 1, heirarchyid);
        }
        PropertyList props = new PropertyList();
        for (int i = 0; i < ds.getColumnCount(); ++i) {
            String columnid2 = ds.getColumnId(i);
            props.setProperty(columnid2, ds.getColumnValues(columnid2, ";"));
        }
        props.setProperty("sdcid", "StorageUnitSDC");
        props.setProperty("fromaction", "createstorageunits");
        props.setProperty("applylock", "N");
        props.setProperty("propsmatch", "true");
        props.setProperty("copies", String.valueOf(ds.size()));
        if ("LV_Box".equals(actionProps.getProperty("primary_sdcid"))) {
            boolean setPrimaryData = false;
            JSONObject json = new JSONObject();
            for (Object column : actionProps.keySet()) {
                String property;
                columnid = (String)column;
                if (!columnid.startsWith("primary_") || (property = columnid.substring(8).toLowerCase()).equals("sdcid")) continue;
                try {
                    String value = actionProps.getProperty(columnid);
                    if (!OpalUtil.isNotEmpty(value)) continue;
                    json.put(property, actionProps.getProperty(columnid));
                    setPrimaryData = true;
                    if (!"templatekeyid1".equals(property)) continue;
                    props.setProperty("boxtemplateid", value);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (setPrimaryData) {
                props.setProperty("primaryboxdata", json.toString());
            }
        }
        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
        DataSet ds1 = new DataSet();
        ds1.addColumnValues("storageunitid", 0, props.getProperty("newkeyid1"), ";");
        ds1.addColumnValues("sulevel", 0, props.getProperty("sulevel"), ";");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("sulevel", "0");
        DataSet newStorageUnitDS = ds1.getFilteredDataSet(filter);
        String storageunitid = newStorageUnitDS.getColumnValues("storageunitid", ";");
        actionProps.setProperty("newkeyid1", storageunitid);
        actionProps.setProperty("storageunitid", storageunitid);
        String arraylayoutid = actionProps.getProperty("arraylayoutid");
        String arraylayoutversionid = actionProps.getProperty("arraylayoutversionid");
        if (arraylayoutid.length() > 0 && arraylayoutversionid.length() > 0 && !arraylayoutid.contains(";") && !arraylayoutversionid.contains(";")) {
            String arraytypeid = OpalUtil.getColumnValue(this.getQueryProcessor(), "arraylayout", "arraytypeid", "arraylayoutid=? and arraylayoutversionid=?", new String[]{arraylayoutid, arraylayoutversionid});
            if (("ASL " + storageunittype).equals(arraytypeid)) {
                props.clear();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", storageunitid);
                props.setProperty("arraylayoutid", arraylayoutid);
                props.setProperty("arraylayoutversionid", arraylayoutversionid);
                props.setProperty("__syncoperation", "Y");
                props.setProperty("tracelogid", actionProps.getProperty("tracelogid"));
                props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        if (OpalUtil.isNotEmpty(sdcid) && (primaryds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "StorageUnitSDC", "select linksdcid, linkkeyid1, (select trackitem.trackitemid from trackitem where trackitem.linksdcid = storageunit.linksdcid and trackitem.linkkeyid1 = storageunit.linkkeyid1) trackitemid from storageunit where storageunitid in ([]) and linksdcid = '" + sdcid + "' and linkkeyid1 is not null", storageunitid)) != null && primaryds.size() > 0) {
            actionProps.setProperty("linksdcid", sdcid);
            actionProps.setProperty("linkkeyid1", primaryds.getColumnValues("linkkeyid1", ";"));
            PropertyList trackitemProps = new PropertyList();
            PropertyList editProps = new PropertyList();
            DataSet sdcColumns = this.getSDCProcessor().getColumnData(sdcid);
            DataSet trackitemColumns = this.getSDCProcessor().getColumnData("TrackItemSDC");
            String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
            for (Object column : actionProps.keySet()) {
                String property;
                String columnid3 = (String)column;
                if (columnid3.startsWith("primary_")) {
                    property = columnid3.substring(8).toLowerCase();
                    if (property.equals(keycolid1) || property.equals("boxtype")) continue;
                    filter.clear();
                    filter.put("columnid", property);
                    if (sdcColumns.findRow(filter) == -1) continue;
                    editProps.setProperty(property, actionProps.getProperty(columnid3));
                    continue;
                }
                if (!columnid3.startsWith("trackitem_") || (property = columnid3.substring(10).toLowerCase()).equals("trackitemid")) continue;
                filter.clear();
                filter.put("columnid", property);
                if (trackitemColumns.findRow(filter) == -1) continue;
                trackitemProps.setProperty(property, actionProps.getProperty(columnid3));
            }
            if (!"LV_Box".equals(actionProps.getProperty("primary_sdcid")) && editProps.size() > 0) {
                editProps.setProperty("sdcid", sdcid);
                editProps.setProperty("keyid1", primaryds.getColumnValues("linkkeyid1", ";"));
                editProps.setProperty("tracelogid", actionProps.getProperty("tracelogid"));
                editProps.setProperty("auditreason", actionProps.getProperty("auditreason"));
                editProps.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                editProps.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                Trace.log("================================================================================");
                Trace.log("Editing SDC Props with audit reason: " + actionProps.getProperty("auditreason"));
                Trace.log("================================================================================");
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), editProps);
            }
            if (trackitemProps.size() > 0 && OpalUtil.isNotEmpty(primaryds.getColumnValues("trackitemid", ";"))) {
                trackitemProps.setProperty("sdcid", "TrackItemSDC");
                trackitemProps.setProperty("keyid1", primaryds.getColumnValues("trackitemid", ";"));
                trackitemProps.setProperty("tracelogid", actionProps.getProperty("tracelogid"));
                trackitemProps.setProperty("auditreason", actionProps.getProperty("auditreason"));
                trackitemProps.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                trackitemProps.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                Trace.log("================================================================================");
                Trace.log("Editing Trackitem Props with audit reason: " + actionProps.getProperty("auditreason"));
                Trace.log("================================================================================");
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), trackitemProps);
            }
        }
        Trace.log("================================================================================");
        Trace.log("AddStorageUnit took " + (System.currentTimeMillis() - start) / 1000L + " seconds to add " + copies + " copies.");
        Trace.log("================================================================================");
    }

    private void addMandatoryChild(PropertyList parentStorageUnitType, DataSet ds, int sulevel, String parentHeirarchyID) {
        PropertyList mandatoryChildTypeProps = this.getMandatoryChildTypeDefinition(parentStorageUnitType.getProperty("nodeid"));
        String childPropertyTreeID = mandatoryChildTypeProps.getProperty("propertytreeid");
        if (OpalUtil.isNotEmpty(childPropertyTreeID)) {
            String childstorageunittype = mandatoryChildTypeProps.getProperty("nodeid");
            int childCount = 0;
            try {
                childCount = Integer.parseInt(parentStorageUnitType.getProperty("size", "1"));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            for (int child = 0; child < childCount; ++child) {
                String childHeirarchyID = parentHeirarchyID + "_" + (child + 1);
                int row = ds.addRow();
                ds.setString(row, "propnodeid", childPropertyTreeID + "|" + childstorageunittype);
                ds.setString(row, "propertytreeid", childPropertyTreeID);
                ds.setString(row, "nodeid", childstorageunittype);
                ds.setString(row, "storageunittype", childstorageunittype);
                ds.setString(row, "sulevel", String.valueOf(sulevel));
                ds.setString(row, "maxtiallowed", mandatoryChildTypeProps.getProperty("maxtiallowed", "0"));
                ds.setString(row, "suparentid", parentHeirarchyID);
                ds.setString(row, "moveableflag", mandatoryChildTypeProps.getProperty("moveableflag", "N"));
                ds.setString(row, "parentid", "");
                ds.setString(row, "suhierarchyid", childHeirarchyID);
                ds.setString(row, "storageunitlabel", "");
                ds.setString(row, "storageenvid", "");
                this.addMandatoryChild(mandatoryChildTypeProps, ds, sulevel + 1, childHeirarchyID);
            }
        }
    }

    private PropertyList getMandatoryChildTypeDefinition(String nodeid) {
        if (!this.localCache.containsKey(nodeid)) {
            this.localCache.put(nodeid, StorageUnitUtil.getMandatoryChildTypeDefinition(this.getQueryProcessor(), nodeid));
        }
        return this.localCache.get(nodeid);
    }
}


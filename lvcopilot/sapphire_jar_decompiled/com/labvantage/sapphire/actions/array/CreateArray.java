/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.array.ApplyArrayMethod;
import com.labvantage.sapphire.actions.array.ArrayUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import java.util.List;
import java.util.regex.Pattern;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateArray
extends BaseAction
implements ArrayConstants,
sapphire.action.CreateArray {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet ds;
        String classification;
        DataSet primary;
        SDIData data;
        SDIRequest sdiRequest;
        SDIProcessor sdiProcessor;
        String inputArrayId = properties.getProperty("overridearrayid", "");
        String arrayTypeId = properties.getProperty("arraytypeid", "");
        String arrayTypeVersionId = properties.getProperty("arraytypeversionid", "");
        String arrayLayoutId = properties.getProperty("arraylayoutid", "");
        String arrayLayoutVersionId = properties.getProperty("arraylayoutversionid", "");
        String arrayMethodId = properties.getProperty("arraymethodid", "");
        String arrayMethodVersionId = properties.getProperty("arraymethodversionid", "");
        if (arrayLayoutId.isEmpty() && arrayTypeId.isEmpty() && arrayMethodId.isEmpty()) {
            throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Either ArrayMethod, ArrayLayout or ArrayType needs to be specified."));
        }
        if (!arrayMethodId.isEmpty()) {
            if (arrayMethodVersionId.isEmpty()) {
                arrayMethodVersionId = ArrayUtil.getArrayMethodCurrentVersion(this.getQueryProcessor(), arrayMethodId);
            }
            if (arrayLayoutId.isEmpty()) {
                String sql = "SELECT arraylayoutid, arraylayoutversionid, arraytypeid, arraytypeversionid FROM arraymethod WHERE arraymethodid=? AND arraymethodversionid=? ";
                SafeSQL safeSQL = new SafeSQL();
                safeSQL.addVar(arrayMethodId);
                safeSQL.addVar(arrayMethodVersionId);
                DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (ds2 != null && ds2.getRowCount() > 0) {
                    arrayLayoutId = ds2.getString(0, "arraylayoutid", "");
                    arrayLayoutVersionId = ds2.getString(0, "arraylayoutversionid", "1");
                    if (arrayLayoutId.isEmpty()) {
                        properties.setProperty("arraytypeid", ds2.getString(0, "arraytypeid", ""));
                        properties.setProperty("arraytypeversionid", ds2.getString(0, "arraytypeversionid", "1"));
                    }
                }
            }
            if (arrayTypeId.isEmpty()) {
                Trace.log("Fetching arraytype for: " + arrayLayoutId + ":" + arrayLayoutVersionId);
                sdiProcessor = this.getSDIProcessor();
                sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_ArrayLayout");
                sdiRequest.setRequestItem("primary");
                sdiRequest.setKeyid1List(arrayLayoutId);
                sdiRequest.setKeyid2List(arrayLayoutVersionId);
                data = sdiProcessor.getSDIData(sdiRequest);
                primary = data.getDataset("primary");
                arrayTypeId = primary.getValue(0, "arraytypeid");
                arrayTypeVersionId = primary.getValue(0, "arraytypeversionid");
            }
        }
        if (!arrayLayoutId.isEmpty()) {
            if (arrayLayoutVersionId.isEmpty()) {
                arrayLayoutVersionId = ArrayUtil.getArrayLayoutCurrentVersion(this.getQueryProcessor(), arrayLayoutId);
            }
            if (arrayTypeId.isEmpty()) {
                Trace.log("Fetching arraytype for: " + arrayLayoutId + ":" + arrayLayoutVersionId);
                sdiProcessor = this.getSDIProcessor();
                sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_ArrayLayout");
                sdiRequest.setRequestItem("primary");
                sdiRequest.setKeyid1List(arrayLayoutId);
                sdiRequest.setKeyid2List(arrayLayoutVersionId);
                data = sdiProcessor.getSDIData(sdiRequest);
                primary = data.getDataset("primary");
                arrayTypeId = primary.getValue(0, "arraytypeid");
                arrayTypeVersionId = primary.getValue(0, "arraytypeversionid");
            }
        }
        if (!arrayTypeId.isEmpty() && StringUtil.getLen(arrayTypeVersionId) == 0L) {
            arrayTypeVersionId = ArrayUtil.getArrayTypeCurrentVersion(this.getQueryProcessor(), arrayLayoutId);
        }
        if (StringUtil.getLen(classification = (ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT classification FROM arraytype WHERE arraytypeid = ? AND arraytypeversionid=?", (Object[])new String[]{arrayTypeId, arrayTypeVersionId})).getValue(0, "classification", "")) == 0L) {
            throw new SapphireException(this.getTranslationProcessor().translate("Classification field is mandatory"));
        }
        PropertyList arrayprops = properties.copy();
        arrayprops.setProperty("sdcid", "LV_Array");
        if (!inputArrayId.isEmpty()) {
            arrayprops.setProperty("overrideautokey", "Y");
            arrayprops.setProperty("keyid1", inputArrayId);
        }
        arrayprops.setProperty("arraydesc", properties.getProperty("arraydesc", ""));
        arrayprops.setProperty("arraytypeid", arrayTypeId);
        arrayprops.setProperty("arraytypeversionid", arrayTypeVersionId);
        arrayprops.setProperty("arraylayoutid", arrayLayoutId);
        arrayprops.setProperty("arraylayoutversionid", arrayLayoutVersionId);
        arrayprops.setProperty("copies", properties.getProperty("copies", "1"));
        arrayprops.setProperty("classification", classification);
        arrayprops.setProperty("arraystatus", "Created");
        arrayprops.setProperty("auditreason", properties.getProperty("auditreason", ""));
        arrayprops.setProperty("auditactivity", properties.getProperty("auditactivity", ""));
        arrayprops.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", ""));
        this.getActionProcessor().processAction("AddSDI", "1", arrayprops);
        String arrayids = arrayprops.getProperty("newkeyid1");
        String[] arrayidlist = StringUtil.split(arrayids, ";");
        DataSet arrayItemInsertDS = new DataSet();
        DataSet arrayZoneDS = new DataSet();
        DataSet arrayZoneFullArrayDS = new DataSet();
        DataSet arrayItemArrayZoneInsertDS = new DataSet();
        for (String arrayid : arrayidlist) {
            if (!arrayLayoutId.isEmpty()) {
                this.createArrayItemsFromLayout(arrayid, arrayLayoutId, arrayLayoutVersionId, arrayItemInsertDS);
            } else {
                this.createArrayItemsFromType(arrayid, arrayTypeId, arrayTypeVersionId, arrayItemInsertDS);
            }
            if (StringUtil.getLen(arrayLayoutId) > 0L && StringUtil.getLen(arrayLayoutVersionId) > 0L) {
                String select = "SELECT '" + arrayid + "' arrayid, arraylayoutzone, color, contentdirection, contentbound, dilutiondirection, dilutionsteps, dilutionfactor, dilutefirstflag, repeatdirection, repeatcount, loadingpriorityhorizontal, loadingpriorityvertical, usersequence, adhocmodeflag FROM arraylayoutzone WHERE arraylayoutid = ? AND arraylayoutversionid = ?";
                DataSet arraylayoutzoneentries = this.getQueryProcessor().getPreparedSqlDataSet(select, (Object[])new String[]{arrayLayoutId, arrayLayoutVersionId});
                if (arraylayoutzoneentries == null || arraylayoutzoneentries.getRowCount() <= 0) continue;
                for (int i = 0; i < arraylayoutzoneentries.size(); ++i) {
                    arrayZoneDS.copyRow(arraylayoutzoneentries, i, 1);
                }
                continue;
            }
            int row = arrayZoneFullArrayDS.addRow();
            arrayZoneFullArrayDS.setString(row, "arrayid", arrayid);
            arrayZoneFullArrayDS.setString(row, "zone", "(FullArray)");
        }
        DataSetUtil.insert(this.database, arrayItemInsertDS, "arrayitem");
        if (arrayZoneDS.size() > 0) {
            PropertyList addProps = new PropertyList();
            addProps.setProperty("sdcid", "LV_ArrayZone");
            addProps.setProperty("arrayid", arrayZoneDS.getColumnValues("arrayid", ";"));
            addProps.setProperty("zone", arrayZoneDS.getColumnValues("arraylayoutzone", ";"));
            addProps.setProperty("color", arrayZoneDS.getColumnValues("color", ";"));
            addProps.setProperty("contentdirection", arrayZoneDS.getColumnValues("contentdirection", ";"));
            addProps.setProperty("contentbound", arrayZoneDS.getColumnValues("contentbound", ";"));
            addProps.setProperty("dilutiondirection", arrayZoneDS.getColumnValues("dilutiondirection", ";"));
            addProps.setProperty("dilutionsteps", arrayZoneDS.getColumnValues("dilutionsteps", ";"));
            addProps.setProperty("dilutionfactor", arrayZoneDS.getColumnValues("dilutionfactor", ";"));
            addProps.setProperty("dilutefirstflag", arrayZoneDS.getColumnValues("dilutefirstflag", ";"));
            addProps.setProperty("repeatdirection", arrayZoneDS.getColumnValues("repeatdirection", ";"));
            addProps.setProperty("repeatcount", arrayZoneDS.getColumnValues("repeatcount", ";"));
            addProps.setProperty("loadingpriorityhorizontal", arrayZoneDS.getColumnValues("loadingpriorityhorizontal", ";"));
            addProps.setProperty("loadingpriorityvertical", arrayZoneDS.getColumnValues("loadingpriorityvertical", ";"));
            addProps.setProperty("usersequence", arrayZoneDS.getColumnValues("usersequence", ";"));
            addProps.setProperty("adhocmodeflag", arrayZoneDS.getColumnValues("adhocmodeflag", ";"));
            addProps.setProperty("copies", String.valueOf(arrayZoneDS.getRowCount()));
            addProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            addProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
            addProps.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processAction("AddSDI", "1", addProps);
            for (String arrayid : arrayidlist) {
                String sql = "SELECT array.arrayid, arraylayoutzoneitem.xpos, arraylayoutzoneitem.ypos, arraylayoutzoneitem.arraylayoutzone, arrayzone.arrayzoneid, arraylayoutzoneitem.contentstring, arraylayoutzoneitem.usersequence FROM arraylayoutzoneitem, array, arrayzone WHERE arraylayoutzoneitem.arraylayoutid = ?  AND arraylayoutzoneitem.arraylayoutversionid = ?  and array.ARRAYLAYOUTID = arraylayoutzoneitem.ARRAYLAYOUTID  and array.ARRAYLAYOUTVERSIONID = ARRAYLAYOUTZONEITEM.ARRAYLAYOUTVERSIONID  and array.arrayid = ?  and arrayzone.ARRAYID = array.arrayid  and arrayzone.zone = ARRAYLAYOUTZONEITEM.ARRAYLAYOUTZONE";
                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{arrayLayoutId, arrayLayoutVersionId, arrayid});
                if (!OpalUtil.isNotEmpty(dataset)) continue;
                for (int i = 0; i < dataset.size(); ++i) {
                    String xpos = dataset.getValue(i, "xpos");
                    String ypos = dataset.getValue(i, "ypos");
                    String arrayitemid = arrayid + "_" + xpos + "_" + ypos;
                    int row = arrayItemArrayZoneInsertDS.addRow();
                    arrayItemArrayZoneInsertDS.setString(row, "arrayitemid", arrayitemid);
                    arrayItemArrayZoneInsertDS.setString(row, "arrayzoneid", dataset.getString(i, "arrayzoneid"));
                    arrayItemArrayZoneInsertDS.setString(row, "contentstring", dataset.getString(i, "contentstring", ""));
                    arrayItemArrayZoneInsertDS.setNumber(row, "usersequence", dataset.getInt(i, "usersequence"));
                }
            }
            DataSetUtil.insert(this.database, arrayItemArrayZoneInsertDS, "arrayitemarrayzone");
        }
        if (OpalUtil.isNotEmpty(arrayZoneFullArrayDS)) {
            String[] arrayList;
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_ArrayZone");
            props.setProperty("arrayid", arrayZoneFullArrayDS.getColumnValues("arrayid", ";"));
            props.setProperty("zone", arrayZoneFullArrayDS.getColumnValues("zone", ";"));
            props.setProperty("copies", String.valueOf(arrayZoneFullArrayDS.size()));
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
            String arrayzoneid = props.getProperty("newkeyid1");
            arrayItemArrayZoneInsertDS.reset();
            for (String arrayid : arrayList = StringUtil.split(props.getProperty("arrayid"), ";")) {
                this.createArrayItemArrayZonesFromType(arrayid, arrayzoneid, arrayTypeId, arrayTypeVersionId, arrayItemArrayZoneInsertDS);
            }
        }
        if (!arrayMethodId.isEmpty()) {
            this.applyArrayMethod(properties, arrayids, arrayMethodId, arrayMethodVersionId);
        }
        properties.setProperty("arrayid", arrayids);
    }

    private String getItemLabel(String hLabel, String vLabel) {
        String itemLabel = "";
        itemLabel = Pattern.matches("[a-zA-Z]+", String.valueOf(hLabel)) && Pattern.matches("[0-9]+", String.valueOf(vLabel)) ? hLabel + vLabel : (Pattern.matches("[a-zA-Z]+", String.valueOf(vLabel)) && Pattern.matches("[0-9]+", String.valueOf(hLabel)) ? vLabel + hLabel : hLabel + "_" + vLabel);
        return itemLabel;
    }

    private void createArrayItemsFromLayout(String arrayid, String arrayLayoutId, String arrayLayoutVersionId, DataSet arrayItemInsertDS) throws SapphireException {
        DataSet ds = this.getConnectionProcessor().isOra() ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT xpos, ypos, horizontallabel, verticallabel, '" + arrayid + "' || '_' || xpos || '_' || ypos arrayitemid, usersequence FROM arraylayoutitem WHERE arraylayoutid = ? AND arraylayoutversionid = ?", (Object[])new String[]{arrayLayoutId, arrayLayoutVersionId}) : this.getQueryProcessor().getPreparedSqlDataSet("SELECT xpos, ypos, horizontallabel, verticallabel, '" + arrayid + "' + '_' + CAST(xpos AS VARCHAR(10))  + '_' + CAST(ypos AS VARCHAR(10)) arrayitemid, usersequence FROM arraylayoutitem WHERE arraylayoutid = ? AND arraylayoutversionid = ?", (Object[])new String[]{arrayLayoutId, arrayLayoutVersionId});
        try {
            for (int i = 0; i < ds.size(); ++i) {
                String hLabel = ds.getValue(i, "horizontallabel", "");
                String vLabel = ds.getValue(i, "verticallabel", "");
                int row = arrayItemInsertDS.addRow();
                arrayItemInsertDS.setString(row, "arrayid", arrayid);
                arrayItemInsertDS.setNumber(row, "xpos", ds.getInt(i, "xpos"));
                arrayItemInsertDS.setNumber(row, "ypos", ds.getInt(i, "ypos"));
                arrayItemInsertDS.setString(row, "horizontallabel", hLabel);
                arrayItemInsertDS.setString(row, "verticallabel", vLabel);
                arrayItemInsertDS.setString(row, "itemlabel", this.getItemLabel(hLabel, vLabel));
                arrayItemInsertDS.setString(row, "arrayitemid", ds.getValue(i, "arrayitemid", ""));
                arrayItemInsertDS.setNumber(row, "usersequence", ds.getInt(i, "usersequence", i));
            }
        }
        catch (Exception e) {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to insert arrayitems"), e);
        }
    }

    private void createArrayItemsFromType(String arrayid, String arraytypeid, String arraytypeversionid, DataSet arrayItemInsertDS) throws SapphireException {
        int i;
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arraytypeid);
        safeSQL.addVar(arraytypeversionid);
        DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet("SELECT numrows, numcolumns, horizontallabeltype, horizontallabeldirection, horizontallabelstart, verticallabeltype, verticallabeldirection, verticallabelstart FROM arraytype WHERE arraytypeid=? AND arraytypeversionid=? ", safeSQL.getValues());
        int rows = (int)Float.parseFloat(dataset.getValue(0, "numrows"));
        int cols = (int)Float.parseFloat(dataset.getValue(0, "numcolumns"));
        String horlbltype = dataset.getValue(0, "horizontallabeltype");
        String horlbldir = dataset.getValue(0, "horizontallabeldirection");
        String horlblstart = dataset.getValue(0, "horizontallabelstart");
        String verlbltype = dataset.getValue(0, "verticallabeltype");
        String verlbldir = dataset.getValue(0, "verticallabeldirection");
        String verlblstart = dataset.getValue(0, "verticallabelstart");
        DataSet arrayitem = new DataSet();
        arrayitem.addColumn("arrayid", 0);
        arrayitem.addColumn("xpos", 1);
        arrayitem.addColumn("ypos", 1);
        arrayitem.addColumn("horizontallabel", 0);
        arrayitem.addColumn("verticallabel", 0);
        arrayitem.addColumn("arrayitemid", 0);
        List colLabels = com.labvantage.sapphire.util.array.ArrayUtil.generateLabel(horlbltype, horlblstart, horlbldir, cols);
        List rowLabels = com.labvantage.sapphire.util.array.ArrayUtil.generateLabel(verlbltype, verlblstart, verlbldir, rows);
        for (i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                int rowindex = arrayitem.addRow();
                arrayitem.setString(rowindex, "arrayid", arrayid);
                arrayitem.setNumber(rowindex, "xpos", i);
                arrayitem.setNumber(rowindex, "ypos", j);
                arrayitem.setString(rowindex, "horizontallabel", String.valueOf(colLabels.get(j)));
                arrayitem.setString(rowindex, "verticallabel", String.valueOf(rowLabels.get(i)));
                arrayitem.setString(rowindex, "arrayitemid", arrayid + "_" + i + "_" + j);
                arrayitem.setString(rowindex, "itemlabel", this.getItemLabel(String.valueOf(colLabels.get(j)), String.valueOf(rowLabels.get(i))));
            }
        }
        try {
            for (i = 0; i < arrayitem.getRowCount(); ++i) {
                int row = arrayItemInsertDS.addRow();
                arrayItemInsertDS.setString(row, "arrayid", arrayid);
                arrayItemInsertDS.setNumber(row, "xpos", arrayitem.getInt(i, "xpos"));
                arrayItemInsertDS.setNumber(row, "ypos", arrayitem.getInt(i, "ypos"));
                arrayItemInsertDS.setString(row, "horizontallabel", arrayitem.getString(i, "horizontallabel"));
                arrayItemInsertDS.setString(row, "verticallabel", arrayitem.getString(i, "verticallabel"));
                arrayItemInsertDS.setString(row, "itemlabel", arrayitem.getString(i, "itemlabel"));
                arrayItemInsertDS.setString(row, "arrayitemid", arrayitem.getString(i, "arrayitemid"));
            }
        }
        catch (Exception e) {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to insert arrayitem"), e);
        }
    }

    private void createArrayItemArrayZonesFromType(String arrayid, String arrayzoneid, String arraytypeid, String arraytypeversionid, DataSet arrayItemArrayZoneInsertDS) {
        int i;
        DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet("SELECT numrows, numcolumns, horizontallabeltype, horizontallabeldirection, horizontallabelstart, verticallabeltype, verticallabeldirection, verticallabelstart FROM arraytype WHERE arraytypeid=? AND arraytypeversionid=?", (Object[])new String[]{arraytypeid, arraytypeversionid});
        int rows = (int)Float.parseFloat(dataset.getValue(0, "numrows"));
        int cols = (int)Float.parseFloat(dataset.getValue(0, "numcolumns"));
        DataSet aiaz = new DataSet();
        aiaz.addColumn("arrayitemid", 0);
        aiaz.addColumn("arrayzoneid", 1);
        aiaz.addColumn("contentstring", 1);
        for (i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                int rowindex = aiaz.addRow();
                aiaz.setString(rowindex, "arrayitemid", arrayid + "_" + i + "_" + j);
                aiaz.setString(rowindex, "arrayzoneid", arrayzoneid);
                aiaz.setString(rowindex, "contentstring", i * cols + j + 1 + ";0;0;0;0");
            }
        }
        for (i = 0; i < aiaz.getRowCount(); ++i) {
            int row = arrayItemArrayZoneInsertDS.addRow();
            arrayItemArrayZoneInsertDS.setString(row, "arrayitemid", aiaz.getString(i, "arrayitemid"));
            arrayItemArrayZoneInsertDS.setString(row, "arrayzoneid", aiaz.getString(i, "arrayzoneid"));
            arrayItemArrayZoneInsertDS.setString(row, "contentstring", aiaz.getString(i, "contentstring"));
        }
    }

    private void applyArrayMethod(PropertyList properties, String arrayids, String arrayMethodId, String arrayMethodVersionId) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("arrayid", arrayids);
        props.setProperty("arraymethodid", arrayMethodId);
        props.setProperty("arraymethodversionid", arrayMethodVersionId);
        props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
        props.setProperty("auditactivity", properties.getProperty("auditactivity"));
        props.setProperty("auditreason", properties.getProperty("auditreason"));
        this.getActionProcessor().processActionClass(ApplyArrayMethod.class.getName(), props);
    }
}


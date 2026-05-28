/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.pagetype.tism;

import com.labvantage.opal.actions.sql.ExecuteInsert;
import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.capa.RecordIncident;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.AddSDIAlias;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.services.ServiceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public final class TISMPropertyHandler
extends BasePropertyHandler {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 87425 $";
    public static final String OPERATION_FILE = "file";
    public static final String OPERATION_ACCEPTRESERVATION = "reserve";
    public static final String OPERATION_TAKECUSTODY = "custody";
    public static final String OPERATION_MISSING = "missing";
    private boolean forceUpdate = false;
    private String auditreason = "";
    private String auditactivity = "";
    private String auditsignedflag = "";
    private HashMap scannedmap = new HashMap();
    HashMap studyaliasmap = new HashMap();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        PropertyList trackitemlist;
        String sourcekeyid1;
        String sourcesdcid;
        block85: {
            sourcesdcid = (String)props.get("sourcesdcid");
            sourcekeyid1 = (String)props.get("sourcekeyid1");
            String extraprops = (String)props.get("__pr_extraprops");
            HashMap extraPropsMap = OpalUtil.parseExtraProps(extraprops);
            this.forceUpdate = extraPropsMap.containsKey("__sdcruleconfirm") ? "Y".equals(extraPropsMap.get("__sdcruleconfirm")) : "Y".equals(props.get("__tismsdcruleconfirm"));
            if (extraPropsMap.containsKey("auditreason")) {
                this.auditreason = (String)extraPropsMap.get("auditreason");
            }
            if (OpalUtil.isEmpty(this.auditreason)) {
                this.auditreason = "TISMDataEdit";
            }
            if (extraPropsMap.containsKey("auditactivity")) {
                this.auditactivity = (String)extraPropsMap.get("auditactivity");
            }
            if (extraPropsMap.containsKey("auditsignedflag")) {
                this.auditsignedflag = (String)extraPropsMap.get("auditsignedflag");
            }
            trackitemlist = new PropertyList();
            String extrascanprops = (String)props.get("extrascandata");
            HashMap<String, String> contentMap = new HashMap<String, String>();
            DataSet ds = new DataSet();
            if (StringUtil.getLen(extrascanprops) > 0L) {
                try {
                    JSONObject o = new JSONObject(extrascanprops);
                    for (int k = 0; k < o.length(); ++k) {
                        String key = String.valueOf(k);
                        if (!o.has(key)) continue;
                        int row = ds.addRow();
                        JSONObject _o = (JSONObject)o.get(key);
                        Iterator i = _o.keys();
                        while (i.hasNext()) {
                            String columnid = (String)i.next();
                            if ("property".equals(columnid)) {
                                JSONObject _p = new JSONObject(_o.getString(columnid));
                                Iterator p = _p.keys();
                                while (p.hasNext()) {
                                    columnid = (String)p.next();
                                    if (!ds.isValidColumn(columnid)) {
                                        ds.addColumn(columnid, 0);
                                    }
                                    ds.setValue(row, columnid, _p.getString(columnid));
                                }
                                continue;
                            }
                            if ("show".equals(columnid)) continue;
                            if (columnid.startsWith("content")) {
                                String value = _o.getString(columnid);
                                if (contentMap.containsKey(columnid)) {
                                    contentMap.put(columnid, (String)contentMap.get(columnid) + ";" + value);
                                    continue;
                                }
                                contentMap.put(columnid, value);
                                continue;
                            }
                            String c = "__" + columnid;
                            if (!ds.isValidColumn(c)) {
                                ds.addColumn(c, 0);
                            }
                            ds.setValue(row, c, _o.getString(columnid));
                        }
                    }
                    DataSet dispcripancyds = new DataSet();
                    dispcripancyds.addColumn("s_sampleid", 0);
                    dispcripancyds.addColumn("s_sampledetailid", 0);
                    dispcripancyds.addColumn("detailtype", 0);
                    dispcripancyds.addColumn("detailvalue", 0);
                    dispcripancyds.addColumn("detailsdcid", 0);
                    dispcripancyds.addColumn("detailkeyid1", 0);
                    dispcripancyds.addColumn("createdt", 2);
                    dispcripancyds.addColumn("createby", 0);
                    dispcripancyds.addColumn("createtool", 0);
                    if (ds == null || ds.size() <= 0) break block85;
                    PropertyList actionProps = new PropertyList();
                    int columnCount = ds.getColumnCount();
                    for (int i = 0; i < ds.size(); ++i) {
                        String capadeviation;
                        String discripancy;
                        Object trackitemid = ds.getValue(i, "__trackitemid");
                        if ("true".equals(ds.getValue(i, "__addsdi", "false"))) {
                            String studyalias;
                            this.forceUpdate = true;
                            actionProps.clear();
                            String studyid = ds.getValue(i, "studyid", "");
                            if (StringUtil.getLen(studyid) == 0L && StringUtil.getLen(studyalias = ds.getValue(i, "studyalias", "")) > 0L) {
                                ds.addColumn("studyid", 0);
                                ds.setValue(i, "studyid", this.getStudyIDByAlias(studyalias));
                            }
                            try {
                                void var20_50;
                                void var20_55;
                                String sdcid = ds.getValue(i, "__sdcid");
                                String scannedid = ds.getValue(i, "__scannedid");
                                actionProps.setProperty("sdcid", sdcid);
                                actionProps.setProperty("copies", "1");
                                for (int col = 0; col < columnCount; ++col) {
                                    String string = ds.getColumnId(col);
                                    if (string.startsWith("__")) continue;
                                    actionProps.setProperty(string, ds.getValue(i, string));
                                }
                                if ("Sample".equals(sdcid)) {
                                    actionProps.remove("studyid");
                                    actionProps.setProperty("sampletypeid", ds.getValue(i, "sampletypeid", ""));
                                    actionProps.setProperty("sstudyid", studyid);
                                    actionProps.setProperty("storagestatus", "Received");
                                    actionProps.setProperty("samplestatus", "Received");
                                    actionProps.setProperty("receiveddt", "n");
                                    actionProps.setProperty("receivedby", this.connectionInfo.getSysuserId());
                                    actionProps.setProperty("externalid", scannedid);
                                    actionProps.setProperty("externalidtype", "External");
                                }
                                actionProps.setProperty("__sdcruleconfirm", this.forceUpdate ? "Y" : "N");
                                actionProps.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Scanned new Sample" : this.auditreason);
                                actionProps.setProperty("auditactivity", this.auditactivity);
                                actionProps.setProperty("auditsignedflag", this.auditsignedflag);
                                this.getActionService().processActionClass(AddSDI.class.getName(), actionProps, this.getErrorHandler());
                                String newkeyid1 = actionProps.getProperty("newkeyid1");
                                ds.setValue(i, "keyid1", newkeyid1);
                                if ("Sample".equals(sdcid)) {
                                    String string = actionProps.getProperty("newtrackitemid");
                                } else {
                                    String string = OpalUtil.getColumnValue(this.getQueryProcessor(), "trackitem", "trackitemid", "linksdcid = ? and linkkeyid1 = ?", new String[]{sdcid, newkeyid1});
                                    if (StringUtil.getLen(scannedid) > 0L) {
                                        actionProps.clear();
                                        actionProps.setProperty("sdcid", sdcid);
                                        actionProps.setProperty("keyid1", newkeyid1);
                                        actionProps.setProperty("aliasid", scannedid);
                                        actionProps.setProperty("aliastype", "External");
                                        actionProps.setProperty("__sdcruleconfirm", this.forceUpdate ? "Y" : "N");
                                        actionProps.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Scanned new Sample" : this.auditreason);
                                        actionProps.setProperty("auditactivity", this.auditactivity);
                                        actionProps.setProperty("auditsignedflag", this.auditsignedflag);
                                        this.getActionService().processActionClass(AddSDIAlias.class.getName(), actionProps, this.getErrorHandler());
                                    }
                                }
                                if (OpalUtil.isEmpty((String)var20_55)) {
                                    actionProps.clear();
                                    actionProps.setProperty("sdcid", "TrackItemSDC");
                                    actionProps.setProperty("copies", "1");
                                    actionProps.setProperty("linksdcid", sdcid);
                                    actionProps.setProperty("linkkeyid1", newkeyid1);
                                    actionProps.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Added new Trackitem" : this.auditreason);
                                    actionProps.setProperty("auditactivity", this.auditactivity);
                                    actionProps.setProperty("auditsignedflag", this.auditsignedflag);
                                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), actionProps);
                                    String string = actionProps.getProperty("newkeyid1");
                                }
                                ds.setValue(i, "__keyid1", newkeyid1);
                                ds.setValue(i, "__trackitemid", (String)var20_50);
                                trackitemlist.setProperty((String)trackitemid, (String)var20_50);
                                trackitemid = var20_50;
                            }
                            catch (ServiceException e) {
                                throw new SapphireException("Action Failure", "VALIDATION", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                            }
                        }
                        DataSet _ds = new DataSet();
                        for (int col = 0; col < ds.getColumnCount(); ++col) {
                            String columnid = ds.getColumnId(col);
                            String value = ds.getValue(i, columnid);
                            if (value == null || value.trim().length() <= 0) continue;
                            if ("__thawed".equals(columnid)) {
                                if (!"true".equals(value)) continue;
                                if (_ds.size() == 0) {
                                    _ds.addRow();
                                }
                                _ds.addColumn("freezethawcount", 0);
                                _ds.setValue(0, "freezethawcount", ds.getValue(i, "__freezethawcount"));
                                continue;
                            }
                            if (columnid.startsWith("__")) continue;
                            if (_ds.size() == 0) {
                                _ds.addRow();
                            }
                            _ds.addColumn(columnid, 0);
                            _ds.setValue(0, columnid, value);
                        }
                        if (ds.isValidColumn("__discripancy") && StringUtil.getLen(discripancy = ds.getValue(i, "__discripancy")) > 0L) {
                            int row = dispcripancyds.addRow();
                            dispcripancyds.setValue(row, "s_sampleid", ds.getValue(i, "__keyid1"));
                            dispcripancyds.setValue(row, "s_sampledetailid", OpalUtil.getNextSequence("s_sampledetail", this.getSequenceProcessor()));
                            dispcripancyds.setValue(row, "detailtype", "Deviation");
                            dispcripancyds.setValue(row, "detailvalue", discripancy);
                            dispcripancyds.setDate(row, "createdt", DateTimeUtil.getNowCalendar());
                            dispcripancyds.setValue(row, "createby", this.getConnectionInfo().getSysuserId());
                            dispcripancyds.setValue(row, "createtool", "SampleDetailHandler");
                        }
                        if (ds.isValidColumn("__capadeviation") && StringUtil.getLen(capadeviation = ds.getValue(i, "__capadeviation")) > 0L && !"-1".equals(capadeviation)) {
                            PropertyList actionprops = new PropertyList();
                            actionprops.setProperty("sourcesdcid", "Sample");
                            actionprops.setProperty("sourcekeyid1", ds.getValue(i, "__keyid1"));
                            actionprops.setProperty("incidentcategory", "UnPlanned");
                            actionprops.setProperty("templateid", capadeviation);
                            actionProps.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Add new CAPA Deviation" : this.auditreason);
                            actionProps.setProperty("auditactivity", this.auditactivity);
                            actionProps.setProperty("auditsignedflag", this.auditsignedflag);
                            this.getActionProcessor().processActionClass(RecordIncident.class.getName(), actionprops);
                        }
                        this.scannedmap.put(trackitemid, _ds);
                    }
                    if (contentMap.size() > 0) {
                        DataSet contentDS = new DataSet();
                        for (String columnid : contentMap.keySet()) {
                            contentDS.addColumnValues(columnid, 0, (String)contentMap.get(columnid), ";");
                        }
                        DataSet ftds = new DataSet();
                        for (int i = 0; i < contentDS.size(); ++i) {
                            String contenttrackitemid;
                            if (!"Y".equals(contentDS.getString(i, "contentthawed")) || !OpalUtil.isNotEmpty(contenttrackitemid = contentDS.getString(i, "contenttrackitemid"))) continue;
                            int row = ftds.addRow();
                            ftds.setString(row, "trackitemid", contenttrackitemid);
                            ftds.setString(row, "freezethawcount", contentDS.getString(i, "contentftcount", "0"));
                        }
                        if (ftds.size() > 0) {
                            PropertyList actionprops = new PropertyList();
                            actionprops.setProperty("sdcid", "TrackItemSDC");
                            actionprops.setProperty("keyid1", ftds.getColumnValues("trackitemid", ";"));
                            actionprops.setProperty("freezethawcount", ftds.getColumnValues("freezethawcount", ";"));
                            actionProps.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Set Freeze Thaw count" : this.auditreason);
                            actionProps.setProperty("auditactivity", this.auditactivity);
                            actionProps.setProperty("auditsignedflag", this.auditsignedflag);
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), actionprops);
                        }
                    }
                    if (dispcripancyds.size() > 0) {
                        try {
                            PropertyList actionprops = new PropertyList();
                            actionprops.setProperty("tableid", "s_sampledetail");
                            actionprops.put("dataset", dispcripancyds);
                            actionProps.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Add Sample Discrepancy" : this.auditreason);
                            actionProps.setProperty("auditactivity", this.auditactivity);
                            actionProps.setProperty("auditsignedflag", this.auditsignedflag);
                            this.getActionProcessor().processActionClass(ExecuteInsert.class.getName(), actionprops);
                        }
                        catch (SapphireException actionprops) {}
                    }
                }
                catch (JSONException e) {
                    Trace.logError("Error", e);
                }
            }
        }
        DataSet file = new DataSet();
        DataSet takecustody = new DataSet();
        DataSet missing = new DataSet();
        StringBuilder sql = new StringBuilder();
        ArrayList<String> trackitems = new ArrayList<String>();
        ArrayList<String> clearReservationSQL = new ArrayList<String>();
        ArrayList<String> reserve = new ArrayList<String>();
        ArrayList dsTrackItems = null;
        try {
            String trackitemid;
            int i;
            void var20_58;
            String tismdata = (String)props.get("tismdata");
            tismdata = StringUtil.getLen(tismdata) > 0L ? StringUtil.replaceAll(tismdata, "&quot;", "\"") : "[]";
            JSONArray jsonArray = new JSONArray(tismdata);
            boolean bl = false;
            while (var20_58 < jsonArray.length()) {
                int ftcount = -1;
                JSONObject jsonObject = jsonArray.getJSONObject((int)var20_58);
                String trackitemid2 = jsonObject.getString("trackitemid");
                if (trackitemlist.containsKey(trackitemid2)) {
                    trackitemid2 = trackitemlist.getProperty(trackitemid2);
                }
                try {
                    ftcount = Integer.parseInt(jsonObject.getString("freezethawcount"));
                }
                catch (Exception e) {
                    ftcount = -1;
                }
                String storageunitid = jsonObject.getString("storageunitid");
                String action = jsonObject.getString("action");
                DataSet __ds = (DataSet)this.scannedmap.get(trackitemid2);
                if (__ds != null) {
                    String freezethawcount = __ds.getValue(0, "freezethawcount");
                    try {
                        ftcount = Integer.parseInt(freezethawcount);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                trackitems.add(trackitemid2);
                if (OPERATION_FILE.equals(action)) {
                    this.populateDataSet(file, trackitemid2, storageunitid, ftcount);
                } else if (OPERATION_ACCEPTRESERVATION.equals(action)) {
                    this.populateDataSet(file, trackitemid2, storageunitid, ftcount);
                    sql.setLength(0);
                    sql.append("delete from reservestorageunit");
                    sql.append(" where storageunitid = '").append(storageunitid).append("'");
                    sql.append(" and trackitemid = ( select t.trackitemid");
                    sql.append(" from trackitem t");
                    sql.append(" where t.trackitemid = '").append(trackitemid2).append("' )");
                    clearReservationSQL.add(sql.toString());
                } else if (OPERATION_TAKECUSTODY.equals(action)) {
                    this.populateDataSet(takecustody, trackitemid2, storageunitid, ftcount);
                    if ("Y".equals(storageunitid)) {
                        reserve.add(trackitemid2);
                    }
                } else if (OPERATION_MISSING.equals(action)) {
                    this.populateDataSet(missing, trackitemid2, storageunitid, ftcount);
                }
                ++var20_58;
            }
            HashMap<String, String> hashMap = new HashMap<String, String>();
            sql.setLength(0);
            if (trackitems.size() > 1000) {
                String rsetid = null;
                try {
                    rsetid = this.getDamProcessor().createRSet("TrackItemSDC", OpalUtil.toDelimitedString(trackitems, ";"), null, null);
                    sql.append("select ti.trackitemid, ti.freezethawcount, ti.linksdcid, ti.linkkeyid1");
                    sql.append(" from trackitem ti");
                    sql.append(" where ti.trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
                    dsTrackItems = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
                finally {
                    if (StringUtil.getLen(rsetid) > 0L) {
                        this.getDamProcessor().clearRSet(rsetid);
                    }
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                String ssql = "select trackitemid, freezethawcount, linksdcid, linkkeyid1 from trackitem where trackitemid in (" + safeSQL.addIn(trackitems) + ")";
                dsTrackItems = this.getQueryProcessor().getPreparedSqlDataSet(ssql, safeSQL.getValues());
            }
            if (dsTrackItems != null) {
                for (int i3 = 0; i3 < dsTrackItems.size(); ++i3) {
                    hashMap.put(((DataSet)dsTrackItems).getString(i3, "trackitemid"), ((DataSet)dsTrackItems).getValue(i3, "freezethawcount", "0"));
                }
            }
            for (i = 0; i < file.size(); ++i) {
                trackitemid = file.getString(i, "trackitemid", "");
                if (!"-1".equals(file.getString(i, "freezethawcount"))) continue;
                file.setString(i, "freezethawcount", (String)hashMap.get(trackitemid));
            }
            for (i = 0; i < takecustody.size(); ++i) {
                trackitemid = takecustody.getString(i, "trackitemid", "");
                takecustody.setString(i, "freezethawcount", (String)hashMap.get(trackitemid));
            }
            for (i = 0; i < missing.size(); ++i) {
                trackitemid = missing.getString(i, "trackitemid", "");
                missing.setString(i, "freezethawcount", (String)hashMap.get(trackitemid));
            }
        }
        catch (JSONException e) {
            this.logError(e.getMessage(), e);
        }
        try {
            this.reserveLocations(reserve);
            this.fileTrackItem(file);
            this.takeTrackItemCustody(takecustody);
            this.markMissing(missing);
            this.processPackage(sourcesdcid, sourcekeyid1, trackitems);
            for (Object e : clearReservationSQL) {
                this.getQueryProcessor().execSQL((String)e);
            }
            StringBuilder sb = new StringBuilder();
            if (trackitems.size() != 0) {
                if (trackitems.size() == 1) {
                    sb.append(this.getTranslationProcessor().translate("1 item has been successfully added to picklist."));
                } else {
                    sb.append(trackitems.size()).append(" ").append(this.getTranslationProcessor().translate("items have been successfully added to picklist."));
                }
            }
            sb.append("<input id='__picklistitems' style='display:none' value='").append(OpalUtil.toDelimitedString(trackitems, ";")).append("'>");
            List<Object> list = OpalUtil.isNotEmpty(sourcekeyid1) ? OpalUtil.toList(sourcekeyid1, ";") : new ArrayList();
            boolean newTrackItemsFound = false;
            if (OpalUtil.isNotEmpty((DataSet)dsTrackItems)) {
                ArrayList sdcDataset;
                if (OpalUtil.isNotEmpty(sourcesdcid)) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("linksdcid", sourcesdcid);
                    sdcDataset = ((DataSet)dsTrackItems).getFilteredDataSet(filter);
                } else {
                    sourcesdcid = ((DataSet)dsTrackItems).getString(0, "linksdcid", "");
                    sdcDataset = dsTrackItems;
                }
                if (OpalUtil.isNotEmpty((DataSet)sdcDataset)) {
                    for (int i = 0; i < sdcDataset.size(); ++i) {
                        String linkkeyid1 = ((DataSet)sdcDataset).getString(i, "linkkeyid1", "");
                        if (linkkeyid1.length() <= 0 || list.contains(linkkeyid1)) continue;
                        list.add(linkkeyid1);
                        newTrackItemsFound = true;
                    }
                }
            }
            if (newTrackItemsFound) {
                String key = OpalUtil.toDelimitedString(list, ";");
                if (key.startsWith(";")) {
                    key = key.substring(1);
                }
                sb.append("<input id='__picklistsourcesdcid' style='display:none' value='").append(sourcesdcid).append("'>");
                sb.append("<input id='__picklistsourcekeyid1' style='display:none' value='").append(key).append("'>");
            }
            this.getErrorHandler().add("TrackItemSDC", "", this.getTranslationProcessor().translate("Pick List"), "INFORMATION", sb.toString());
            if (this.getErrorHandler().hasInfoErrors()) {
                props.put("ERRORHANDLER", this.getErrorHandler());
            }
        }
        catch (ServiceException e) {
            if (this.getErrorHandler().size() > 0 && ErrorHandler.isErrorHandlerFormat(e.getMessage())) {
                props.put("ERRORHANDLER", this.getErrorHandler());
                ErrorDetail errorDetail = (ErrorDetail)this.getErrorHandler().get(0);
                if (errorDetail != null) {
                    throw new SapphireException(errorDetail.getErrorid(), errorDetail.getErrorType(), errorDetail.getMessage(), e);
                }
            }
            throw new SapphireException(e.getErrorid(), ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    private void reserveLocations(List<String> reserve) throws ServiceException {
        if (reserve != null && reserve.size() > 0) {
            DataSet dsinsert = new DataSet();
            dsinsert.addColumn("trackitemid", 0);
            dsinsert.addColumn("storageunitid", 0);
            dsinsert.addColumn("createby", 0);
            dsinsert.addColumn("createdt", 2);
            dsinsert.addColumn("createtool", 0);
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select ti.trackitemid, ti.linkkeyid1, ti.currentstorageunitid from trackitem ti where ti.trackitemid in (" + safeSQL.addIn(reserve) + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
                Calendar now = dtu.getCalendar("Now");
                for (int i = 0; i < ds.size(); ++i) {
                    String currentstorageunitid = ds.getValue(i, "currentstorageunitid");
                    if (StringUtil.getLen(currentstorageunitid) <= 0L) continue;
                    int row = dsinsert.addRow();
                    dsinsert.setValue(row, "trackitemid", ds.getValue(i, "trackitemid"));
                    dsinsert.setValue(row, "storageunitid", currentstorageunitid);
                    dsinsert.setValue(row, "createby", this.connectionInfo.getSysuserId());
                    dsinsert.setDate(row, "createdt", now);
                    dsinsert.setValue(row, "createtool", "TISM");
                }
                if (dsinsert.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("tableid", "reservestorageunit");
                    props.put("dataset", dsinsert);
                    props.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Sample location reserved" : this.auditreason);
                    props.setProperty("auditactivity", this.auditactivity);
                    props.setProperty("auditsignedflag", this.auditsignedflag);
                    this.getActionService().processActionClass(ExecuteInsert.class.getName(), props, this.getErrorHandler());
                }
            }
        }
    }

    private void markMissing(DataSet ds) throws ServiceException, SapphireException {
        if (ds != null && ds.size() > 0) {
            PropertyList props = new PropertyList();
            StringBuilder sql = new StringBuilder();
            ArrayList tids = null;
            if (ds.size() > 750) {
                String rsetid = this.getDamProcessor().createRSet("TrackItemSDC", ds.getColumnValues("trackitemid", ";"), null, null);
                if (StringUtil.getLen(rsetid) > 0L) {
                    sql.append("select trackitemid, linksdcid, linkkeyid1 from trackitem");
                    sql.append(" where trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ? ) order by linksdcid");
                    tids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select trackitemid, linksdcid, linkkeyid1 from trackitem");
                sql.append(" where trackitemid in (").append(safeSQL.addIn(ds.getColumnValues("trackitemid", "','"))).append(") order by linksdcid");
                tids = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (tids != null && tids.size() > 0) {
                ((DataSet)tids).sort("linksdcid");
                ArrayList<DataSet> list = ((DataSet)tids).getGroupedDataSets("linksdcid");
                if (list != null) {
                    for (Object e : list) {
                        DataSet d = (DataSet)e;
                        if (d == null || d.size() <= 0) continue;
                        String linksdcid = d.getString(0, "linksdcid");
                        if ("Sample".equals(linksdcid)) {
                            props.setProperty("sdcid", "Sample");
                            props.setProperty("keyid1", d.getColumnValues("linkkeyid1", ";"));
                            props.setProperty("disposaldt", "n");
                            props.setProperty("disposedby", this.getConnectionInfo().getSysuserId());
                            props.setProperty("storagestatus", "Disposed");
                            props.setProperty("storagedisposalstatus", "Missing");
                            props.setProperty("__sdcruleconfirm", this.forceUpdate ? "Y" : "N");
                            props.setProperty("propsmatch", "Y");
                            props.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Sample marked missing" : this.auditreason);
                            props.setProperty("auditactivity", this.auditactivity);
                            props.setProperty("auditsignedflag", this.auditsignedflag);
                            this.getActionService().processActionClass(EditSDI.class.getName(), props, this.getErrorHandler());
                            this.getErrorHandler().add("Sample", "", this.getTranslationProcessor().translate("Information"), "INFORMATION", this.getTranslationProcessor().translate("Samples have been marked missing successfully"));
                            continue;
                        }
                        props.clear();
                        props.setProperty("trackitemid", d.getColumnValues("trackitemid", ";"));
                        props.setProperty("trackitemstatus", "Missing");
                        props.setProperty("custodialuserid", "(null)");
                        props.setProperty("currentstorageunitid", "(null)");
                        props.setProperty("__sdcruleconfirm", this.forceUpdate ? "Y" : "N");
                        props.setProperty("auditreason", this.auditreason.equals("TISMDataEdit") ? "Sample marked missing" : this.auditreason);
                        props.setProperty("auditactivity", this.auditactivity);
                        props.setProperty("auditsignedflag", this.auditsignedflag);
                        this.getActionService().processActionClass(EditTrackItem.class.getName(), props, this.getErrorHandler());
                        this.getErrorHandler().add(this.getTranslationProcessor().translate("Mark Missing"), "", this.getTranslationProcessor().translate("Information"), "INFORMATION", this.getTranslationProcessor().translate("Items have been marked missing successfully"));
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processPackage(String sourcesdcid, String sourcekeyid1, List trackitems) throws ServiceException, SapphireException {
        if (trackitems.size() == 0 || !"StorageUnitSDC".equals(sourcesdcid)) {
            return;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select p.s_packageid, p.senderdepartmentid");
        sql.append(" from s_package p, storageunit su");
        sql.append(" where su.linksdcid = 'LV_Package'");
        sql.append(" and p.s_packageid = su.linkkeyid1");
        sql.append(" and su.storageunitid = ?");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{sourcekeyid1});
        if (ds != null && ds.size() > 0) {
            String packageid = ds.getString(0, "s_packageid");
            String senderdepartmentid = ds.getString(0, "senderdepartmentid", "");
            DataSet sampleds = null;
            SafeSQL safeSQL = new SafeSQL();
            sql.setLength(0);
            sql.append("select sf.s_samplefamilyid, sf.initialdepartmentid, sf.initialpackageid");
            sql.append(" from s_samplefamily sf, s_sample sa, trackitem ti");
            sql.append(" where sa.samplefamilyid = sf.s_samplefamilyid");
            sql.append(" and sa.s_sampleid = ti.linkkeyid1");
            sql.append(" and ti.linksdcid = 'Sample'");
            if (trackitems.size() < 750) {
                sql.append(" and ti.trackitemid in (").append(safeSQL.addIn(OpalUtil.toDelimitedString(trackitems, "','"))).append(")");
                sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            } else {
                String rsetid = null;
                try {
                    rsetid = this.getDamProcessor().createRSet("TrackItemSDC", OpalUtil.toDelimitedString(trackitems, ";"), null, null);
                    if (StringUtil.getLen(rsetid) > 0L) {
                        sql.append(" and ti.trackitemid in (select rs.keyid1 from rsetitems rs where rs.rsetid = ?)");
                        sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                    }
                }
                finally {
                    if (StringUtil.getLen(rsetid) > 0L) {
                        this.getDamProcessor().clearRSet(rsetid);
                    }
                }
            }
            if (sampleds != null && sampleds.size() > 0) {
                DataSet data = new DataSet();
                data.addColumn("s_samplefamilyid", 0);
                data.addColumn("initialdepartmentid", 0);
                data.addColumn("initialpackageid", 0);
                for (int i = 0; i < sampleds.size(); ++i) {
                    String samplefamilyid = sampleds.getString(i, "s_samplefamilyid");
                    String initialdepartmentid = sampleds.getString(i, "initialdepartmentid");
                    String initialpackageid = sampleds.getString(i, "initialpackageid");
                    if (StringUtil.getLen(samplefamilyid) <= 0L || StringUtil.getLen(initialdepartmentid) != 0L && StringUtil.getLen(initialpackageid) != 0L) continue;
                    int row = data.addRow();
                    data.setString(row, "s_samplefamilyid", samplefamilyid);
                    data.setString(row, "initialdepartmentid", senderdepartmentid);
                    data.setString(row, "initialpackageid", packageid);
                }
                if (data.size() > 0) {
                    PropertyList actionprops = new PropertyList();
                    actionprops.setProperty("sdcid", "LV_SampleFamily");
                    actionprops.setProperty("keyid1", data.getColumnValues("s_samplefamilyid", ";"));
                    actionprops.setProperty("initialdepartmentid", data.getColumnValues("initialdepartmentid", ";"));
                    actionprops.setProperty("initialpackageid", data.getColumnValues("initialpackageid", ";"));
                    actionprops.setProperty("propsmatch", "Y");
                    actionprops.setProperty("__sdcruleconfirm", this.forceUpdate ? "Y" : "N");
                    actionprops.setProperty("auditreason", this.auditreason);
                    actionprops.setProperty("auditactivity", this.auditactivity);
                    actionprops.setProperty("auditsignedflag", this.auditsignedflag);
                    this.getActionService().processActionClass(EditSDI.class.getName(), actionprops, this.getErrorHandler());
                }
            }
        }
    }

    private void fileTrackItem(DataSet ds) throws ServiceException, SapphireException {
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String trackitemid = ds.getValue(i, "trackitemid");
                if (!this.scannedmap.containsKey(trackitemid)) continue;
                DataSet _ds = (DataSet)this.scannedmap.get(trackitemid);
                for (int col = 0; col < _ds.getColumnCount(); ++col) {
                    String columnid = _ds.getColumnId(col);
                    if (!ds.isValidColumn(columnid)) {
                        ds.addColumn(columnid, 0);
                    }
                    ds.setValue(i, columnid, _ds.getValue(0, columnid));
                }
            }
            PropertyList props = new PropertyList();
            props.setProperty("trackitemid", ds.getColumnValues("trackitemid", ";"));
            props.setProperty("currentstorageunitid", ds.getColumnValues("storageunitid", ";"));
            props.setProperty("freezethawcount", ds.getColumnValues("freezethawcount", ";"));
            for (int col = 0; col < ds.getColumnCount(); ++col) {
                String columnid = ds.getColumnId(col);
                if (columnid.equals("trackitemid") || columnid.equals("storageunitid") || columnid.equals("freezethawcount")) continue;
                props.setProperty(columnid, ds.getColumnValues(columnid, ";"));
            }
            props.setProperty("__sdcruleconfirm", this.forceUpdate ? "Y" : "N");
            props.setProperty("__clinicalflag", "Y");
            props.setProperty("propsmatch", "Y");
            if ("TISMDataEdit".equals(this.auditreason)) {
                DataSet dataSet;
                String sdc = "";
                if (ds.size() <= 1000) {
                    SafeSQL safeSQL = new SafeSQL();
                    dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select distinct linksdcid from trackitem where trackitemid in (" + safeSQL.addIn(ds.getColumnValues("trackitemid", ";"), ";") + ")", safeSQL.getValues());
                } else {
                    String rsetid = this.getDamProcessor().createRSet("TrackItemSDC", ds.getColumnValues("trackitemid", ";"), null, null);
                    dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select distinct linksdcid from trackitem where trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
                    this.getDamProcessor().clearRSet(rsetid);
                }
                if (OpalUtil.isNotEmpty(dataSet)) {
                    String storagesdcid;
                    for (int i = 0; i < dataSet.size(); ++i) {
                        String linksdcid = dataSet.getString(i, "linksdcid", "");
                        if (linksdcid.length() <= 0) continue;
                        String sdcdisplayname = this.getSdcProcessor().getProperty(linksdcid, "singular");
                        sdc = sdc.length() == 0 ? sdcdisplayname : ", " + sdcdisplayname;
                    }
                    String targetsdcid = "storage location";
                    String currentstorageunitid = ds.getString(0, "storageunitid", "");
                    dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select (case when linksdcid is null then (select p.linksdcid from storageunit p where p.storageunitid = storageunit.parentid) else linksdcid end) storagesdcid from storageunit where storageunitid = ?", (Object[])new String[]{currentstorageunitid});
                    if (OpalUtil.isNotEmpty(dataSet) && (storagesdcid = dataSet.getString(0, "storagesdcid", "")).length() > 0) {
                        targetsdcid = this.getSdcProcessor().getProperty(storagesdcid, "singular");
                    }
                    this.auditreason = sdc + " filed in " + targetsdcid;
                }
            }
            props.setProperty("auditreason", this.auditreason);
            props.setProperty("auditactivity", this.auditactivity);
            props.setProperty("auditsignedflag", this.auditsignedflag);
            this.getActionService().processActionClass(EditTrackItem.class.getName(), props, this.getErrorHandler());
            this.getErrorHandler().add("TrackItemSDC", "", this.getTranslationProcessor().translate("Information"), "INFORMATION", this.getTranslationProcessor().translate("Operation successful"));
        }
    }

    private void takeTrackItemCustody(DataSet ds) throws ServiceException, SapphireException {
        if (ds != null && ds.size() > 0) {
            DataSet _ds = new DataSet();
            _ds.addColumn("currentstorageunitid", 0);
            _ds.addColumn("custodialuserid", 0);
            _ds.addColumn("custodytakendt", 0);
            _ds.addColumnValues("trackitemid", 0, ds.getColumnValues("trackitemid", ";"), ";");
            _ds.setValue(0, "currentstorageunitid", "(null)");
            _ds.setValue(0, "custodialuserid", this.connectionInfo.getSysuserId());
            _ds.setValue(0, "custodytakendt", "n");
            _ds.padColumns();
            PropertyList props = new PropertyList();
            props.setProperty("trackitemid", _ds.getColumnValues("trackitemid", ";"));
            props.setProperty("currentstorageunitid", _ds.getColumnValues("currentstorageunitid", ";"));
            props.setProperty("custodialuserid", _ds.getColumnValues("custodialuserid", ";"));
            props.setProperty("custodytakendt", _ds.getColumnValues("custodytakendt", ";"));
            props.setProperty("__sdcruleconfirm", this.forceUpdate ? "Y" : "N");
            props.setProperty("propsmatch", "Y");
            if ("TISMDataEdit".equals(this.auditreason)) {
                DataSet dataSet;
                String sdc = "";
                if (ds.size() <= 1000) {
                    SafeSQL safeSQL = new SafeSQL();
                    dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select distinct linksdcid from trackitem where trackitemid in (" + safeSQL.addIn(_ds.getColumnValues("trackitemid", ";"), ";") + ")", safeSQL.getValues());
                } else {
                    String rsetid = this.getDamProcessor().createRSet("TrackItemSDC", _ds.getColumnValues("trackitemid", ";"), null, null);
                    dataSet = this.getQueryProcessor().getPreparedSqlDataSet("select distinct linksdcid from trackitem where trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
                    this.getDamProcessor().clearRSet(rsetid);
                }
                if (OpalUtil.isNotEmpty(dataSet)) {
                    for (int i = 0; i < dataSet.size(); ++i) {
                        String linksdcid = dataSet.getString(i, "linksdcid", "");
                        if (linksdcid.length() <= 0) continue;
                        String sdcdisplayname = this.getSdcProcessor().getProperty(linksdcid, "singular");
                        sdc = sdc.length() == 0 ? sdcdisplayname : ", " + sdcdisplayname;
                    }
                    this.auditreason = "User taken custody of " + sdc;
                }
            }
            props.setProperty("auditreason", this.auditreason);
            props.setProperty("auditactivity", this.auditactivity);
            props.setProperty("auditsignedflag", this.auditsignedflag);
            this.getActionService().processActionClass(EditTrackItem.class.getName(), props, this.getErrorHandler());
            this.getErrorHandler().add("TrackItemSDC", "", this.getTranslationProcessor().translate("Information"), "INFORMATION", this.getTranslationProcessor().translate("You have successfully taken the custody"));
        }
    }

    private void populateDataSet(DataSet ds, String trackitemid, String storageunitid, int ftcount) {
        if (ds.getColumnCount() == 0) {
            ds.addColumn("storageunitid", 0);
            ds.addColumn("trackitemid", 0);
            ds.addColumn("freezethawcount", 0);
        }
        int row = ds.addRow();
        if (StringUtil.getLen(trackitemid) > 0L) {
            ds.setString(row, "storageunitid", storageunitid);
            ds.setString(row, "trackitemid", trackitemid);
            ds.setString(row, "freezethawcount", String.valueOf(ftcount));
        }
    }

    private String getStudyIDByAlias(String studyalias) {
        if (!this.studyaliasmap.containsKey(studyalias)) {
            this.studyaliasmap.put(studyalias, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "s_studyid", "studyalias = ?", new String[]{studyalias}));
        }
        return (String)this.studyaliasmap.get(studyalias);
    }
}


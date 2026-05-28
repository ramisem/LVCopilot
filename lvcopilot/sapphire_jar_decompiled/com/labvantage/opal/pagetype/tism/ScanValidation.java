/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.pagetype.tism;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.util.format.DateFormatter;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.ext.BaseTismScanParser;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ScanValidation
extends BaseAjaxRequest {
    public static final String STATUS_DISPOSED = "Disposed";
    public static final String YES = "Y";
    public static final String NO = "N";
    Map<String, HashMap<String, Object>> groovyMapCache = new HashMap<String, HashMap<String, Object>>();

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        if (!YES.equals(ajaxResponse.getRequestParameter("confirmed"))) {
            String msg = this.validateTargetStorageUnit(ajaxResponse);
            if (StringUtil.getLen(msg) > 0L) {
                ajaxResponse.addCallbackArgument("confirmflag", YES);
                ajaxResponse.addCallbackArgument("msg", msg);
                ajaxResponse.addCallbackArgument("scannedid", ajaxResponse.getRequestParameter("keyid1"));
            } else {
                this.validateScannedItem(ajaxResponse);
            }
        } else {
            this.validateScannedItem(ajaxResponse);
        }
        ajaxResponse.print();
    }

    private String validateTargetStorageUnit(AjaxResponse ajaxResponse) {
        String targetstorageunitid = ajaxResponse.getRequestParameter("targetstorageunitid");
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        String aliasCheck = ajaxResponse.getRequestParameter("aliascheck");
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        if (!YES.equals(aliasCheck)) {
            sql.append("select count(t.linkkeyid1) ticount");
            sql.append(" from trackitem t");
            sql.append(" where t.currentstorageunitid in ( select s.storageunitid from storageunit s");
            sql.append(" where s.parentid = ").append(safeSQL.addVar(targetstorageunitid)).append(" or s.storageunitid = ").append(safeSQL.addVar(targetstorageunitid)).append(" )");
            sql.append(" and t.linksdcid = ").append(safeSQL.addVar(sdcid)).append("");
            sql.append(" and t.linkkeyid1 = ").append(safeSQL.addVar(keyid1)).append("");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0 && 0 != ds.getInt(0, "ticount")) {
                return this.getTranslationProcessor().translate("Scanned item already exists in Target Storage Unit. File again?");
            }
        } else {
            sql.append("select a.keyid1 from sdialias a where a.sdcid = 'Sample' and a.aliasid = ").append(safeSQL.addVar(keyid1));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() <= 1) {
                safeSQL.reset();
                sql.setLength(0);
                sql.append("select count(t.linkkeyid1) ticount");
                sql.append(" from trackitem t");
                sql.append(" where t.currentstorageunitid in ( select s.storageunitid from storageunit s");
                sql.append(" where s.parentid = ").append(safeSQL.addVar(targetstorageunitid)).append(" or s.storageunitid = ").append(safeSQL.addVar(targetstorageunitid)).append(" )");
                sql.append(" and t.linksdcid = ").append(safeSQL.addVar(sdcid));
                sql.append(" and ( t.linkkeyid1 = ").append(safeSQL.addVar(keyid1)).append(" or t.linkkeyid1 = ( select a.keyid1 from sdialias a where a.sdcid = 'Sample' and a.aliasid = ").append(safeSQL.addVar(keyid1)).append(" ) )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0 && 0 != ds.getInt(0, "ticount")) {
                    return this.getTranslationProcessor().translate("Scanned item already exists in Target Storage Unit. File again?");
                }
            }
        }
        return "";
    }

    private void validateScannedItem(AjaxResponse ajaxResponse) throws ServletException {
        block94: {
            try {
                String[] keyArray;
                String defaultvalue;
                String columnid;
                HashMap map;
                DataSet ds;
                String keycolid1;
                String tableid;
                List<String> errors;
                String rsetid;
                String scannedValue;
                String msg;
                String extrascanshow;
                String extrascancolumns;
                String scanparseclass;
                String aliasCheck;
                String keyid1;
                String sdcid;
                String targetstorageunitid;
                String sourcekeyid1;
                String sourcesdcid;
                boolean inStorage;
                block97: {
                    DataSet temp;
                    StringBuilder sql;
                    QueryProcessor qp;
                    String aliasType;
                    block95: {
                        block96: {
                            inStorage = false;
                            sourcesdcid = ajaxResponse.getRequestParameter("sourcesdcid");
                            sourcekeyid1 = ajaxResponse.getRequestParameter("sourcekeyid1");
                            targetstorageunitid = ajaxResponse.getRequestParameter("targetstorageunitid");
                            sdcid = ajaxResponse.getRequestParameter("sdcid");
                            keyid1 = ajaxResponse.getRequestParameter("keyid1");
                            aliasCheck = ajaxResponse.getRequestParameter("aliascheck");
                            aliasType = ajaxResponse.getRequestParameter("aliastype", "");
                            scanparseclass = ajaxResponse.getRequestParameter("scanparseclass", "");
                            extrascancolumns = ajaxResponse.getRequestParameter("extrascancolumns");
                            extrascanshow = StringUtil.replaceAll(ajaxResponse.getRequestParameter("extrascanshow", ""), "||quot||", "\"");
                            msg = "";
                            scannedValue = keyid1;
                            qp = this.getQueryProcessor();
                            sql = new StringBuilder();
                            rsetid = null;
                            if (keyid1.split(";").length > 1000) {
                                rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, null, null);
                            }
                            errors = null;
                            if (YES.equals(aliasCheck)) break block95;
                            SafeSQL safeSQL = new SafeSQL();
                            if ("Sample".equals(sdcid)) {
                                tableid = "s_sample";
                                keycolid1 = "s_sampleid";
                                sql.append(" select 'Sample' sdcid, s.s_sampleid keyid1, s.storagestatus,");
                                sql.append(" t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn,");
                                sql.append(" ( select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid ) susdcid,");
                                sql.append(" ( select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid ) labelpath");
                                sql.append(" from s_sample s, trackitem t");
                                if (OpalUtil.isNotEmpty(rsetid)) {
                                    sql.append(" where s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                                } else {
                                    sql.append(" where s.s_sampleid in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
                                }
                                sql.append(" and t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid");
                                sql.append(" order by s.s_sampleid");
                            } else if ("TrackItemSDC".equals(sdcid)) {
                                tableid = "trackitem";
                                keycolid1 = "trackitemid";
                                sql.append(" select t.linksdcid sdcid, t.linkkeyid1 keyid1, t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn,");
                                sql.append(" ( select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid ) susdcid,");
                                sql.append(" ( select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid ) labelpath");
                                sql.append(" from trackitem t");
                                if (OpalUtil.isNotEmpty(rsetid)) {
                                    sql.append(" where t.trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                                } else {
                                    sql.append(" where t.trackitemid in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
                                }
                                sql.append(" order by t.trackitemid");
                            } else {
                                SDCProcessor sdcProcessor = this.getSDCProcessor();
                                keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                                tableid = sdcProcessor.getProperty(sdcid, "tableid");
                                String desccol = sdcProcessor.getProperty(sdcid, "desccol");
                                sql.setLength(0);
                                safeSQL.reset();
                                sql.append("select s.").append(keycolid1).append(", (select t.trackitemid from trackitem t where t.linksdcid = ").append(safeSQL.addVar(sdcid)).append(" and t.linkkeyid1 = s.").append(keycolid1).append(") trackitemid");
                                sql.append(" from ").append(tableid).append(" s");
                                if (OpalUtil.isNotEmpty(rsetid)) {
                                    sql.append(" where s.").append(keycolid1).append(" in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                                } else {
                                    sql.append(" where s.").append(keycolid1).append(" in (").append(safeSQL.addIn(keyid1, ";")).append(")");
                                }
                                DataSet dataset = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                                if (dataset != null) {
                                    HashSet<String> set = new HashSet<String>();
                                    for (int i = 0; i < dataset.size(); ++i) {
                                        if (!OpalUtil.isEmpty(dataset.getString(i, "trackitemid"))) continue;
                                        set.add(dataset.getString(i, keycolid1));
                                    }
                                    if (set.size() > 0) {
                                        PropertyList props = new PropertyList();
                                        props.setProperty("sdcid", "TrackItemSDC");
                                        props.setProperty("copies", String.valueOf(set.size()));
                                        props.setProperty("linksdcid", StringUtil.repeat(sdcid, set.size(), ";"));
                                        props.setProperty("linkkeyid1", OpalUtil.toDelimitedString(set, ";"));
                                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                                    }
                                }
                                sql.setLength(0);
                                safeSQL.reset();
                                sql.append(" select ").append(safeSQL.addVar(sdcid)).append(" sdcid, s.").append(keycolid1).append(" keyid1, s.").append(desccol).append(" description,");
                                sql.append(" t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn,");
                                sql.append(" ( select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid ) susdcid,");
                                sql.append(" ( select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid ) labelpath");
                                sql.append(" from ").append(tableid).append(" s, trackitem t");
                                if (OpalUtil.isNotEmpty(rsetid)) {
                                    sql.append(" where s.").append(keycolid1).append(" in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                                } else {
                                    sql.append(" where s.").append(keycolid1).append(" in (").append(safeSQL.addIn(keyid1, ";")).append(")");
                                }
                                sql.append(" and t.linksdcid = ").append(safeSQL.addVar(sdcid)).append(" and t.linkkeyid1 = s.").append(keycolid1);
                                sql.append(" order by s.").append(keycolid1);
                            }
                            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                            if (ds != null) break block96;
                            ds = new DataSet();
                            break block97;
                        }
                        if (ds.size() <= 0) break block97;
                        errors = StorageUnitUtil.validateStorageRestrictions(this.getQueryProcessor(), this.getDAMProcessor(), targetstorageunitid, ds.getString(0, "trackitemid"), this.getConnectionProcessor().getSapphireConnection());
                        break block97;
                    }
                    StringBuilder sql2 = new StringBuilder();
                    SafeSQL safeSQL = new SafeSQL();
                    SafeSQL safeSQL2 = new SafeSQL();
                    if ("Sample".equals(sdcid)) {
                        tableid = "s_sample";
                        keycolid1 = "s_sampleid";
                        sql.append(" select 'Sample' sdcid, s.s_sampleid keyid1, s.storagestatus, s.sampledesc,");
                        sql.append(" t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn,");
                        sql.append(" ( select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid ) susdcid,");
                        sql.append(" ( select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid ) labelpath,");
                        sql.append(" '' aliasid, '' aliastype");
                        sql.append(" from s_sample s, trackitem t");
                        if (OpalUtil.isNotEmpty(rsetid)) {
                            sql.append(" where s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                        } else {
                            sql.append(" where s.s_sampleid in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
                        }
                        sql.append(" and t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid");
                        sql.append(" order by s.s_sampleid");
                        sql2.append(" select sa.sdcid, sa.keyid1, '' description,");
                        sql2.append(" (select s_sample.storagestatus from s_sample where s_sample.s_sampleid = sa.keyid1) storagestatus,");
                        sql2.append(" t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn,");
                        sql2.append(" ( select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid ) susdcid,");
                        sql2.append(" ( select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid ) labelpath,");
                        sql2.append(" sa.aliasid, sa.aliastype");
                        sql2.append(" from sdialias sa, trackitem t");
                        sql2.append(" where sa.sdcid = 'Sample'");
                        if (OpalUtil.isNotEmpty(rsetid)) {
                            sql2.append(" and sa.aliasid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL2.addVar(rsetid)).append(" )");
                        } else {
                            sql2.append(" and sa.aliasid in ( ").append(safeSQL2.addIn(keyid1, ";")).append(" )");
                        }
                        sql2.append(" and t.linksdcid = 'Sample' and t.linkkeyid1 = sa.keyid1");
                        if (StringUtil.getLen(aliasType) > 0L) {
                            sql2.append(" and sa.aliastype = ").append(safeSQL2.addVar(aliasType));
                        }
                        sql2.append(" order by sa.keyid1");
                    } else if ("TrackItemSDC".equals(sdcid)) {
                        tableid = "trackitem";
                        keycolid1 = "trackitemid";
                        sql.append(" select t.linksdcid sdcid, t.linkkeyid1 keyid1, t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn, '' description,");
                        sql.append(" ( select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid ) susdcid,");
                        sql.append(" ( select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid ) labelpath");
                        sql.append(" from trackitem t");
                        if (OpalUtil.isNotEmpty(rsetid)) {
                            sql.append(" where t.trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                        } else {
                            sql.append(" where t.trackitemid in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
                        }
                        sql.append(" order by t.trackitemid");
                    } else {
                        SDCProcessor sdcProcessor = this.getSDCProcessor();
                        keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                        tableid = sdcProcessor.getProperty(sdcid, "tableid");
                        String desccol = sdcProcessor.getProperty(sdcid, "desccol");
                        sql.append(" select ").append(safeSQL.addVar(sdcid)).append(" sdcid, s.").append(keycolid1).append(" keyid1, s.").append(desccol).append(" description,");
                        sql.append(" t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn,");
                        sql.append(" ( select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid ) susdcid,");
                        sql.append(" ( select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid ) labelpath,");
                        sql.append(" '' aliasid, '' aliastype");
                        sql.append(" from ").append(tableid).append(" s, trackitem t");
                        if (OpalUtil.isNotEmpty(rsetid)) {
                            sql.append(" where s.").append(keycolid1).append(" in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                        } else {
                            sql.append(" where s.").append(keycolid1).append(" in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
                        }
                        sql.append(" and t.linksdcid = ").append(safeSQL.addVar(sdcid)).append(" and t.linkkeyid1 = s.").append(keycolid1);
                        sql.append(" order by s.").append(keycolid1);
                        sql2.append(" select sa.sdcid, sa.keyid1, '' description,");
                        sql2.append(" t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.freezethawflag, t.freezethawcount, t.freezethawcountmax, t.freezethawcountwarn,");
                        sql2.append(" ( select su.linksdcid from storageunit su where su.storageunitid = t.currentstorageunitid ) susdcid,");
                        sql2.append(" ( select su.labelpath from storageunit su where su.storageunitid = t.currentstorageunitid ) labelpath,");
                        sql2.append(" sa.aliasid, sa.aliastype");
                        sql2.append(" from sdialias sa, trackitem t");
                        sql2.append(" where sa.sdcid = ").append(safeSQL2.addVar(sdcid)).append("");
                        sql2.append(" and sa.aliasid =").append(safeSQL2.addVar(keyid1)).append("");
                        sql2.append(" and t.linksdcid = ").append(safeSQL2.addVar(sdcid)).append(" and t.linkkeyid1 = sa.keyid1");
                        if (StringUtil.getLen(aliasType) > 0L) {
                            sql2.append(" and sa.aliastype =").append(safeSQL2.addVar(aliasType)).append("");
                        }
                        sql2.append(" order by sa.keyid1");
                    }
                    ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds == null) {
                        ds = new DataSet();
                    }
                    if (sql2.length() > 0 && (temp = qp.getPreparedSqlDataSet(sql2.toString(), safeSQL2.getValues())) != null) {
                        for (int i = 0; i < temp.size(); ++i) {
                            ds.copyRow(temp, i, 1);
                        }
                    }
                }
                if (OpalUtil.isNotEmpty(rsetid)) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                if (OpalUtil.isNotEmpty(ds)) {
                    for (int i = 0; i < ds.size(); ++i) {
                        SDIRequest sdiRequest = new SDIRequest();
                        sdiRequest.setRequestItem("primary");
                        sdiRequest.setSDIList(ds.getString(i, "sdcid", ""), ds.getString(i, "keyid1"), ds.getString(i, "keyid2", null), ds.getString(i, "keyid3", null));
                        DataSet primary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
                        if (!OpalUtil.isEmpty(primary)) continue;
                        if (errors == null) {
                            errors = new ArrayList<String>();
                        }
                        String s = "<span style='color:red'>" + this.getTranslationProcessor().translate("ACCESS FAILURE") + "</span>";
                        s = s + "<hr>";
                        s = s + this.getTranslationProcessor().translate("User is not authorized to access the scanned item.");
                        s = s + "<br><br>";
                        s = s + this.getTranslationProcessor().translate("The application security has stopped the user from scanning this item.");
                        errors.add(s);
                    }
                }
                if (errors != null && errors.size() > 0) {
                    ajaxResponse.addCallbackArgument("confirm", NO);
                    ajaxResponse.addCallbackArgument("msg", OpalUtil.toDelimitedString(errors, "<br><br>"));
                    break block94;
                }
                String confirm = NO;
                if (ds != null && ds.size() > 0) {
                    boolean valid = true;
                    if (!YES.equals(ajaxResponse.getRequestParameter("ignorecurrentlocation")) && !YES.equals(ajaxResponse.getRequestParameter("confirmed"))) {
                        for (int i = 0; i < ds.size(); ++i) {
                            if (ds.getString(i, "labelpath", "").length() <= 0 || "LV_Package".equals(ds.getString(i, "susdcid"))) continue;
                            confirm = YES;
                            msg = "The selected item is already in Storage";
                            valid = false;
                            inStorage = true;
                            break;
                        }
                    }
                    if (valid) {
                        ds.addColumn("reservelocation", 0);
                        ds.addColumn("__ftstatusimg", 0);
                        for (int i = 0; i < ds.size(); ++i) {
                            if ("Sample".equals(sdcid) && STATUS_DISPOSED.equals(ds.getValue(i, "storagestatus"))) {
                                msg = "One or more of the scanned item is Disposed";
                                break;
                            }
                            ds.setValue(i, "labelpath", ds.getValue(i, "labelpath", ""));
                            ds.setValue(i, "custodialuserid", ds.getValue(i, "custodialuserid", ""));
                            ds.setValue(i, "freezethawflag", ds.getValue(i, "freezethawflag", NO));
                            ds.setNumber(i, "freezethawcount", ds.getInt(i, "freezethawcount", -1));
                            ds.setNumber(i, "freezethawcountmax", ds.getInt(i, "freezethawcountmax", 0));
                            ds.setNumber(i, "freezethawcountwarn", ds.getInt(i, "freezethawcountwarn", 0));
                            ds.setValue(i, "reservelocation", this.getTrackitemReservedLocations(ds.getValue(i, "trackitemid")));
                            int count = ds.getInt(i, "freezethawcount");
                            int warn = ds.getInt(i, "freezethawcountwarn");
                            int max = ds.getInt(i, "freezethawcountmax");
                            if (count > max) {
                                ds.setValue(i, "__ftstatusimg", "FreezeThawFail");
                                continue;
                            }
                            if (count == max) {
                                ds.setValue(i, "__ftstatusimg", "FreezeThawMax");
                                continue;
                            }
                            if (count >= warn && count < max) {
                                ds.setValue(i, "__ftstatusimg", "FreezeThawWarn");
                                continue;
                            }
                            if (count >= warn) continue;
                            ds.setValue(i, "__ftstatusimg", "FreezeThawPass");
                        }
                    }
                }
                PropertyList parselist = new PropertyList();
                if (StringUtil.getLen(scanparseclass) > 0L) {
                    Class<?> c = Class.forName(scanparseclass);
                    Constructor<?> constructor = c.getConstructor(String.class);
                    BaseTismScanParser tismScanParser = (BaseTismScanParser)constructor.newInstance(this.getConnectionId());
                    PropertyList list = tismScanParser.parseScannedString(sdcid, keyid1);
                    for (Object o : list.keySet()) {
                        String key = (String)o;
                        parselist.setProperty(key, list.getProperty(key));
                    }
                }
                PropertyList pl = new PropertyList();
                GroovyUtil grooyUtil = GroovyUtil.getInstance(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                if (ds != null && ds.size() > 0) {
                    keyid1 = ds.getValue(0, "keyid1");
                    if (extrascanshow.startsWith("$G{")) {
                        try {
                            pl.setProperty("show", grooyUtil.evaluateSecure(extrascanshow, this.getGroovyMap(sdcid, keyid1, tableid, keycolid1, sourcesdcid, sourcekeyid1, targetstorageunitid)));
                        }
                        catch (SapphireException e) {
                            msg = "Error while parsing the Groovy Expression: <br><i><b>" + extrascanshow + "</b></i><hr>" + e.getMessage();
                        }
                    } else {
                        pl.setProperty("show", YES.equals(extrascanshow) ? "true" : "false");
                    }
                    if (StringUtil.getLen(extrascancolumns) > 0L) {
                        JSONObject json = new JSONObject(extrascancolumns);
                        Iterator iterator = json.keys();
                        while (iterator.hasNext()) {
                            String show;
                            String key = (String)iterator.next();
                            JSONObject object = (JSONObject)json.get(key);
                            String __id = object.getString("otherpropid");
                            String string = show = object.has("show") ? object.getString("show") : YES;
                            if (show.startsWith("$G{")) {
                                try {
                                    HashMap map2 = this.getGroovyMap(sdcid, keyid1, tableid, keycolid1, sourcesdcid, sourcekeyid1, targetstorageunitid);
                                    pl.setProperty(__id, grooyUtil.evaluateSecure(show, map2));
                                }
                                catch (SapphireException e) {
                                    msg = "Error while parsing the Groovy Expression: <br><i><b>" + show + "</b></i><hr>" + e.getMessage();
                                }
                            } else {
                                pl.setProperty(__id, YES.equals(show) ? "true" : "false");
                            }
                            String columntype = object.getString("columntype");
                            if (columntype.startsWith("$G{")) {
                                try {
                                    map = this.getGroovyMap(sdcid, keyid1, tableid, keycolid1, sourcesdcid, sourcekeyid1, targetstorageunitid);
                                    pl.setProperty(__id + "_columntype", grooyUtil.evaluateSecure(columntype, map));
                                }
                                catch (SapphireException e) {
                                    msg = "Error while parsing the Groovy Expression: <br><i><b>" + columntype + "</b></i><hr>" + e.getMessage();
                                }
                            } else {
                                pl.setProperty(__id + "_columntype", columntype);
                            }
                            if (!object.has("otherpropid")) continue;
                            HashMap groovyMap = this.getGroovyMap(sdcid, keyid1, tableid, keycolid1, sourcesdcid, sourcekeyid1, targetstorageunitid);
                            columnid = object.getString("otherpropid");
                            if (object.has("defaultvalue")) {
                                defaultvalue = object.getString("defaultvalue");
                                if (defaultvalue == null || defaultvalue.trim().length() <= 0) continue;
                                if (defaultvalue.startsWith("$G{")) {
                                    defaultvalue = grooyUtil.evaluateSecure(defaultvalue, groovyMap);
                                }
                                if (object.has("dateformat")) {
                                    try {
                                        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                                        String dateformat = object.getString("dateformat");
                                        DateTimeUtil dtu = new DateTimeUtil(connectionInfo);
                                        Calendar date = dtu.getCalendar(defaultvalue);
                                        DateFormat df = ElementUtil.getDateFormat(connectionInfo, dateformat, true);
                                        defaultvalue = df.format(date.getTime());
                                    }
                                    catch (Exception connectionInfo) {
                                        // empty catch block
                                    }
                                }
                                parselist.setProperty(columnid, defaultvalue);
                                continue;
                            }
                            if (!"qtycurrent".equals(columnid)) continue;
                            HashMap map3 = (HashMap)groovyMap.get("trackitem");
                            String qtycurrent = (String)map3.get("qtycurrent");
                            String qtyunits = (String)map3.get("qtyunits");
                            parselist.setProperty(columnid, qtycurrent);
                            parselist.setProperty("qtyunits", qtyunits);
                        }
                    }
                } else {
                    boolean showAdditionalData;
                    if (extrascanshow.startsWith("$G{")) {
                        try {
                            pl.setProperty("show", grooyUtil.evaluateSecure(extrascanshow, this.getGroovyMap(sdcid, keyid1, tableid, keycolid1, sourcesdcid, sourcekeyid1, targetstorageunitid)));
                        }
                        catch (SapphireException e) {
                            msg = "Error while parsing the Groovy Expression: <br><i><b>" + extrascanshow + "</b></i><hr>" + e.getMessage();
                        }
                    } else {
                        pl.setProperty("show", YES.equals(extrascanshow) ? "true" : "false");
                    }
                    boolean bl = showAdditionalData = YES.equals(pl.getProperty("show")) || "true".equals(pl.getProperty("show"));
                    if (showAdditionalData && StringUtil.getLen(extrascancolumns) > 0L) {
                        JSONObject json = new JSONObject(extrascancolumns);
                        Iterator iterator = json.keys();
                        while (iterator.hasNext()) {
                            String show;
                            String key = (String)iterator.next();
                            JSONObject object = (JSONObject)json.get(key);
                            String __id = object.getString("otherpropid");
                            String string = show = object.has("show") ? object.getString("show") : YES;
                            if (show.startsWith("$G{")) {
                                try {
                                    map = this.getGroovyMap(sdcid, keyid1, tableid, keycolid1, sourcesdcid, sourcekeyid1, targetstorageunitid);
                                    pl.setProperty(__id, grooyUtil.evaluateSecure(show, map));
                                }
                                catch (SapphireException e) {
                                    msg = "Error while parsing the Groovy Expression: <br><i><b>" + show + "</b></i><hr>" + e.getMessage();
                                }
                            } else {
                                pl.setProperty(__id, YES.equals(show) ? "true" : "false");
                            }
                            String columntype = object.getString("columntype");
                            if (columntype.startsWith("$G{")) {
                                try {
                                    HashMap map4 = this.getGroovyMap(sdcid, keyid1, tableid, keycolid1, sourcesdcid, sourcekeyid1, targetstorageunitid);
                                    pl.setProperty(__id + "_columntype", grooyUtil.evaluateSecure(columntype, map4));
                                }
                                catch (SapphireException e) {
                                    msg = "Error while parsing the Groovy Expression: <br><i><b>" + columntype + "</b></i><hr>" + e.getMessage();
                                }
                            } else {
                                pl.setProperty(__id + "_columntype", columntype);
                            }
                            if (!object.has("otherpropid") || !object.has("defaultvalue")) continue;
                            columnid = object.getString("otherpropid");
                            defaultvalue = object.getString("defaultvalue");
                            if (defaultvalue == null || defaultvalue.trim().length() <= 0) continue;
                            if (defaultvalue.startsWith("$G{")) {
                                parselist.setProperty(columnid, grooyUtil.evaluateSecure(defaultvalue, this.getGroovyMap(sdcid, keyid1, tableid, keycolid1, sourcesdcid, sourcekeyid1, targetstorageunitid)));
                                continue;
                            }
                            parselist.setProperty(columnid, defaultvalue);
                        }
                    }
                }
                DataSet data = new DataSet();
                if (!"TrackItemSDC".equals(sdcid)) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    for (String key : keyArray = StringUtil.split(ajaxResponse.getRequestParameter("keyid1"), ";")) {
                        if (YES.equals(aliasCheck)) {
                            filter.put("aliasid", key);
                            DataSet tempds = ds.getFilteredDataSet(filter);
                            if (tempds != null && tempds.size() > 0) {
                                for (int i = 0; i < tempds.size(); ++i) {
                                    data.copyRow(tempds, i, 1);
                                }
                            }
                        }
                        filter.clear();
                        filter.put("keyid1", key);
                        int row = ds.findRow(filter);
                        if (row == -1) continue;
                        data.copyRow(ds, row, 1);
                    }
                } else {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    for (String key : keyArray = StringUtil.split(ajaxResponse.getRequestParameter("keyid1"), ";")) {
                        filter.put("trackitemid", key);
                        int row = ds.findRow(filter);
                        if (row == -1) continue;
                        data.copyRow(ds, row, 1);
                    }
                }
                ajaxResponse.addCallbackArgument("confirm", confirm);
                ajaxResponse.addCallbackArgument("msg", this.getTranslationProcessor().translate(msg));
                ajaxResponse.addCallbackArgument("scannedid", scannedValue);
                ajaxResponse.addCallbackArgument("ds", data);
                ajaxResponse.addCallbackArgument("alias", aliasCheck);
                ajaxResponse.addCallbackArgument("parseds", parselist);
                ajaxResponse.addCallbackArgument("show", pl);
                ajaxResponse.addCallbackArgument("instorage", inStorage ? "true" : "false");
            }
            catch (Exception e) {
                throw new ServletException((Throwable)e);
            }
        }
    }

    private String getTrackitemReservedLocations(String trackitemid) {
        String sb = "";
        StringBuffer sql = new StringBuffer();
        sql.append("select s.labelpath");
        sql.append(" from reservestorageunit r, storageunit s");
        sql.append(" where r.storageunitid = s.storageunitid");
        sql.append(" and r.trackitemid = ( select t.trackitemid ");
        sql.append(" from trackitem t ");
        sql.append(" where t.trackitemid = ?").append(" )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{trackitemid});
        if (ds != null && ds.size() > 0) {
            sb = ds.getColumnValues("labelpath", "<br>");
        }
        return sb;
    }

    private HashMap getGroovyMap(String sdcid, String keyid1, String tableid, String keycolid1, String sourcesdcid, String sourcekeyid1, String targetstorageunitid) {
        String key = sdcid + ";" + keyid1 + ";" + tableid + ";" + keycolid1 + ";" + sourcesdcid + ";" + sourcekeyid1 + ";" + targetstorageunitid;
        if (!this.groovyMapCache.containsKey(key)) {
            String scannedvaluefound = YES;
            HashMap<String, Object> groovyMap = new HashMap<String, Object>();
            StringBuilder sql = new StringBuilder();
            if ("TrackItemSDC".equals(sdcid)) {
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select * from trackitem where trackitemid = ?", (Object[])new String[]{keyid1});
                if (ds != null && ds.size() > 0) {
                    String sqtycurrent = ds.getValue(0, "qtycurrent", "");
                    HashMap map = this.setNullColumnsToBlank((HashMap)ds.get(0));
                    map.put("qtycurrent", sqtycurrent);
                    groovyMap.put("primary", map);
                } else {
                    scannedvaluefound = NO;
                    groovyMap.put("primary", this.getSDCColumnMap(sdcid));
                }
            } else {
                sql.append("select * from ").append(tableid).append(" where ").append(keycolid1).append(" = ?");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{keyid1});
                if (ds != null && ds.size() > 0) {
                    groovyMap.put("primary", this.setNullColumnsToBlank((HashMap)ds.get(0)));
                } else {
                    groovyMap.put("primary", this.getSDCColumnMap(sdcid));
                }
                sql.setLength(0);
                sql.append("select * from trackitem where linksdcid = ? and linkkeyid1 = ?");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{sdcid, keyid1});
                if (ds != null && ds.size() > 0) {
                    String sqtycurrent = ds.getValue(0, "qtycurrent", "");
                    HashMap map = this.setNullColumnsToBlank((HashMap)ds.get(0));
                    map.put("qtycurrent", sqtycurrent);
                    groovyMap.put("trackitem", map);
                } else {
                    scannedvaluefound = NO;
                    HashMap columnMap = this.getSDCColumnMap("TrackItemSDC");
                    columnMap.put("linksdcid", sdcid);
                    groovyMap.put("trackitem", columnMap);
                }
                if ("Sample".equals(sdcid)) {
                    sql.setLength(0);
                    sql.append("select * from s_study where s_studyid = ( select s.sstudyid from s_sample s where s.s_sampleid = ?").append(" )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{keyid1});
                    if (ds != null && ds.size() > 0) {
                        groovyMap.put("study", this.setNullColumnsToBlank((HashMap)ds.get(0)));
                    } else {
                        groovyMap.put("study", this.getSDCColumnMap("Study"));
                    }
                    sql.setLength(0);
                    sql.append("select * from s_samplefamily where s_samplefamilyid = ( select s_sample.samplefamilyid from s_sample where s_sampleid = ?").append(" )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{keyid1});
                    if (ds != null && ds.size() > 0) {
                        groovyMap.put("samplefamily", this.setNullColumnsToBlank((HashMap)ds.get(0)));
                    } else {
                        groovyMap.put("samplefamily", this.getSDCColumnMap("LV_SampleFamily"));
                    }
                    sql.setLength(0);
                    sql.append("select * from s_participant where s_participantid = ( select s_samplefamily.participantid from s_samplefamily where s_samplefamily.s_samplefamilyid = (select s_sample.samplefamilyid from s_sample where s_sample.s_sampleid = ?").append(") )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{keyid1});
                    if (ds != null && ds.size() > 0) {
                        groovyMap.put("participant", this.setNullColumnsToBlank((HashMap)ds.get(0)));
                    } else {
                        groovyMap.put("participant", this.getSDCColumnMap("LV_Participant"));
                    }
                }
            }
            groovyMap.put("sdcid", sdcid);
            groovyMap.put("keyid1", keyid1);
            groovyMap.put("scannedvalue", keyid1);
            groovyMap.put("tableid", tableid);
            groovyMap.put("scannedvaluefound", scannedvaluefound);
            groovyMap.put("queryprocessor", this.getQueryProcessor());
            groovyMap.put("dataset", new DataSet());
            groovyMap.put("sourcesdcid", sourcesdcid);
            groovyMap.put("sourcekeyid1", sourcekeyid1);
            groovyMap.put("sourcestorageunitid", "StorageUnitSDC".equals(sourcesdcid) ? sourcekeyid1 : "");
            groovyMap.put("targetstorageunitid", targetstorageunitid);
            groovyMap.put("user", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getUserAttributeMap());
            this.groovyMapCache.put(key, groovyMap);
        }
        return this.groovyMapCache.get(key);
    }

    private HashMap getSDCColumnMap(String sdcid) {
        HashMap columns = new HashMap();
        DataSet ds = this.getSDCProcessor().getColumnData(sdcid);
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                columns.put(((HashMap)ds.get(i)).get("columnid"), "");
            }
        }
        return columns;
    }

    private HashMap setNullColumnsToBlank(HashMap map) {
        if (map != null) {
            for (Object o1 : map.keySet()) {
                String key = (String)o1;
                Object o = map.get(key);
                String value = o instanceof Calendar ? DateFormatter.formatDateTime((Calendar)o) : String.valueOf(map.get(key));
                map.put(key, "null".equals(value) ? "" : value);
            }
        }
        return map;
    }
}


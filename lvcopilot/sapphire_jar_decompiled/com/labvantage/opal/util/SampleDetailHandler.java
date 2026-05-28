/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.util;

import com.labvantage.opal.actions.sql.ExecuteInsert;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.capa.RecordIncident;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SampleDetailHandler
extends BaseAjaxRequest {
    public static final String DETAIL_COMMENT = "Comment";
    public static final String DETAIL_TREATMENT = "Treatment";
    public static final String DETAIL_PROCEDURE = "Procedure";
    public static final String DETAIL_DEVIATION = "Deviation";
    public static final List detailList = new ArrayList();

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String detail = ajaxResponse.getRequestParameter("detail", "");
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        String value = ajaxResponse.getRequestParameter("value", "");
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String sysuserid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId();
        String msg = "";
        if (StringUtil.getLen(detail) > 0L && detailList.contains(detail)) {
            PropertyList props = new PropertyList();
            if (DETAIL_DEVIATION.equals(detail)) {
                try {
                    if (StringUtil.getLen(sampleid) > 0L) {
                        props.setProperty("sourcesdcid", "Sample");
                        props.setProperty("sourcekeyid1", StringUtil.replaceAll(sampleid, "%3B", ";"));
                        props.setProperty("incidentcategory", "UnPlanned");
                        props.setProperty("templateid", value);
                        this.getActionProcessor().processActionClass(RecordIncident.class.getName(), props);
                    } else if (StringUtil.getLen(trackitemid.trim()) > 0L) {
                        StringBuffer sql = new StringBuffer();
                        SafeSQL safeSQL = new SafeSQL();
                        sql.append("select trackitemid, linksdcid, linkkeyid1");
                        sql.append(" from trackitem");
                        sql.append(" where trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(")");
                        sql.append(" and linksdcid = 'Sample'");
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        if (ds != null && ds.size() > 0) {
                            DataSet _ds = new DataSet();
                            _ds.addColumn("sourcesdcid", 0);
                            _ds.addColumn("sourcekeyid1", 0);
                            for (int i = 0; i < ds.size(); ++i) {
                                int row = _ds.addRow();
                                _ds.setValue(row, "sourcesdcid", "TrackItemSDC");
                                _ds.setValue(row, "sourcekeyid1", ds.getString(i, "trackitemid"));
                                row = _ds.addRow();
                                _ds.setValue(row, "sourcesdcid", "Sample");
                                _ds.setValue(row, "sourcekeyid1", ds.getString(i, "linkkeyid1"));
                            }
                            props.setProperty("sourcesdcid", _ds.getColumnValues("sourcesdcid", ";"));
                            props.setProperty("sourcekeyid1", _ds.getColumnValues("sourcekeyid1", ";"));
                            props.setProperty("incidentcategory", "UnPlanned");
                            props.setProperty("templateid", value);
                            this.getActionProcessor().processActionClass(RecordIncident.class.getName(), props);
                        }
                    }
                }
                catch (ActionException e) {
                    Logger.logError(e.getMessage(), e);
                    msg = e.getMessage();
                }
            } else {
                DataSet ds = new DataSet();
                ds.addColumn("s_sampleid", 0);
                ds.addColumn("s_sampledetailid", 0);
                ds.addColumn("usersequence", 0);
                ds.addColumn("detailtype", 0);
                ds.addColumn("detailvalue", 0);
                ds.addColumn("detailsdcid", 0);
                ds.addColumn("detailkeyid1", 0);
                ds.addColumn("createdt", 2);
                ds.addColumn("createby", 0);
                ds.addColumn("createtool", 0);
                String[] s = StringUtil.split(StringUtil.replaceAll(sampleid, "%3B", ";"), ";");
                String[] v = StringUtil.split(value, ";");
                for (int i = 0; i < s.length; ++i) {
                    for (int j = 0; j < v.length; ++j) {
                        String detailvalue = v[j];
                        if (DETAIL_TREATMENT.equals(detail)) {
                            sdcid = "LV_Treatment";
                            detailvalue = keyid1 = v[j];
                        }
                        int row = ds.addRow();
                        ds.setValue(row, "s_sampleid", s[i]);
                        ds.setValue(row, "s_sampledetailid", OpalUtil.getNextSequence("s_sampledetail", this.getSequenceProcessor()));
                        ds.setValue(row, "usersequence", "" + this.getNextUserSequence("s_sampledetail", "s_sampleid", s[i]));
                        ds.setValue(row, "detailtype", detail);
                        ds.setValue(row, "detailvalue", detailvalue);
                        ds.setValue(row, "detailsdcid", sdcid);
                        ds.setValue(row, "detailkeyid1", keyid1);
                        ds.setDate(row, "createdt", DateTimeUtil.getNowCalendar());
                        ds.setValue(row, "createby", sysuserid);
                        ds.setValue(row, "createtool", "SampleDetailHandler");
                    }
                }
                if (ds.size() > 0) {
                    try {
                        props.setProperty("tableid", "s_sampledetail");
                        props.put("dataset", ds);
                        this.getActionProcessor().processActionClass(ExecuteInsert.class.getName(), props);
                    }
                    catch (SapphireException e) {
                        msg = e.getMessage();
                    }
                }
            }
        }
        ajaxResponse.addCallbackArgument("detail", detail);
        ajaxResponse.addCallbackArgument("msg", msg);
        ajaxResponse.print();
    }

    private int getNextUserSequence(String tablename, String keycolumname, String keycolumvalue) {
        int nextseq = 1;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select max(usersequence) usersequence from ").append(tablename).append(" where ").append(keycolumname).append("=").append(safeSQL.addVar(keycolumvalue));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            nextseq = ds.getInt(0, "usersequence", 0) + 1;
        }
        return nextseq;
    }

    static {
        detailList.add(DETAIL_COMMENT);
        detailList.add(DETAIL_TREATMENT);
        detailList.add(DETAIL_PROCEDURE);
        detailList.add(DETAIL_DEVIATION);
    }
}


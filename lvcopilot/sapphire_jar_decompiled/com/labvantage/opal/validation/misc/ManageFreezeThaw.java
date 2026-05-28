/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ManageFreezeThaw
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54732 $";
    HashMap candidateMap = new HashMap();

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        int i;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitems = ajaxResponse.getRequestParameter("trackitemid", "");
        ArrayList<String> warnList = new ArrayList<String>();
        DataSet ds = new DataSet();
        if (StringUtil.getLen(trackitems) > 0L) {
            trackitems = StringUtil.replaceAll(trackitems, "%3B", ";");
            String[] t = StringUtil.split(trackitems, ";");
            warnList.addAll(Arrays.asList(t));
            if (warnList.size() > 0) {
                try {
                    ds = this.getFreezeThawWarnTrackitems(warnList);
                    if (ds.size() > 0) {
                        DataSet warnds = new DataSet();
                        for (i = 0; i < ds.getColumnCount(); ++i) {
                            String columnid = ds.getColumnId(i);
                            warnds.addColumn(columnid, ds.getColumnType(columnid));
                        }
                        for (i = 0; i < ds.size(); ++i) {
                            if ("Check Out".equals(ds.getValue(i, "ftstatus"))) continue;
                            warnds.copyRow(ds, i, 1);
                        }
                        ds = warnds;
                        warnds.sort("linkkeyid1");
                    }
                }
                catch (SapphireException e) {
                    this.logger.error("Action Exception: " + e.toString(), e);
                }
            }
        } else {
            try {
                ArrayList<String> fileList = new ArrayList<String>();
                String tismdata = ajaxResponse.getRequestParameter("tismdata", "");
                if (StringUtil.getLen(tismdata) == 0L) {
                    tismdata = "[]";
                }
                JSONArray jsonArray = new JSONArray(tismdata);
                for (int i2 = 0; i2 < jsonArray.length(); ++i2) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i2);
                    String trackitemid = jsonObject.getString("trackitemid");
                    String storageunitid = jsonObject.getString("storageunitid");
                    String action = jsonObject.getString("action");
                    if ("file".equals(action)) {
                        if (!this.isFreezeThawCandidate(storageunitid)) continue;
                        fileList.add(trackitemid);
                        continue;
                    }
                    if ("reserve".equals(action)) {
                        if (!this.isFreezeThawCandidate(storageunitid)) continue;
                        fileList.add(trackitemid);
                        continue;
                    }
                    if (!"custody".equals(action)) continue;
                    warnList.add(trackitemid);
                }
                try {
                    ds = this.getRegularCheckInTrackitems(fileList);
                    DataSet initialds = this.getInitialCheckInTrackitems(fileList);
                    if (initialds.size() > 0) {
                        for (int i3 = 0; i3 < initialds.size(); ++i3) {
                            ds.copyRow(initialds, i3, 1);
                        }
                    }
                    DataSet warnds = this.getFreezeThawWarnTrackitems(warnList);
                    for (int i4 = 0; i4 < warnds.size(); ++i4) {
                        ds.copyRow(warnds, i4, 1);
                    }
                }
                catch (SapphireException e) {
                    this.logger.error("Action Exception: " + e.toString(), e);
                }
                ds.sort("linkkeyid1");
            }
            catch (JSONException e) {
                this.logger.error(e.getMessage(), e);
            }
        }
        if (ds != null && ds.size() > 0) {
            HashMap<String, Map> props = new HashMap<String, Map>();
            QueryProcessor qp = this.getQueryProcessor();
            for (i = 0; i < ds.size(); ++i) {
                Map sdcProps;
                String sdcid = ds.getValue(i, "linksdcid");
                if (StringUtil.getLen(sdcid) <= 0L) continue;
                if (props.containsKey(sdcid)) {
                    sdcProps = (Map)props.get(sdcid);
                } else {
                    sdcProps = this.getSDCProcessor().getSDCProperties(sdcid);
                    props.put(sdcid, sdcProps);
                }
                if (sdcProps == null) continue;
                String description = OpalUtil.getColumnValue(qp, (String)sdcProps.get("tableid"), (String)sdcProps.get("desccol"), sdcProps.get("keycolid1") + "=?", new String[]{ds.getValue(i, "linkkeyid1")});
                ds.setValue(i, "description", description);
                ds.setValue(i, "linksdcid", (String)sdcProps.get("singular"));
            }
        }
        ajaxResponse.addCallbackArgument("ds", ds);
        ajaxResponse.addCallbackArgument("trackitemid", trackitems);
        ajaxResponse.print();
    }

    private DataSet getFreezeThawWarnTrackitems(List trackitems) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        String rsetid = null;
        SafeSQL safeSQL = new SafeSQL();
        if (trackitems.size() <= 750) {
            sql.append("select trackitemid, linksdcid, linkkeyid1, freezethawflag, freezethawcount, freezethawcountmax, ");
            sql.append(" freezethawcountwarn, 'N' initialflag, 'checkout' ftaction, 'Check Out' ftstatus, '' description");
            sql.append(" from trackitem");
            sql.append(" where trackitemid in (").append(safeSQL.addIn(OpalUtil.toDelimitedString(trackitems, "','"))).append(")");
            sql.append(" and freezethawflag = 'Y'");
            sql.append(" and freezethawcountmax is not null");
            sql.append(" order by linkkeyid1");
        } else {
            rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", OpalUtil.toDelimitedString(trackitems, ";"), null, null);
            sql.append("select trackitemid, linksdcid, linkkeyid1, freezethawflag, freezethawcount, freezethawcountmax, ");
            sql.append(" freezethawcountwarn, 'N' initialflag, 'checkout' ftaction, 'Check Out' ftstatus, '' description");
            sql.append(" from trackitem");
            sql.append(" where trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            sql.append(" and freezethawflag = 'Y'");
            sql.append(" and freezethawcountmax is not null");
            sql.append(" order by linkkeyid1");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (StringUtil.getLen(rsetid) > 0L) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                int ftcount = ds.getInt(i, "freezethawcount");
                int warnftcount = ds.getInt(i, "freezethawcountwarn", 0);
                int maxftcount = ds.getInt(i, "freezethawcountmax", 0);
                if (ftcount > maxftcount) {
                    ds.setValue(i, "ftstatus", "EXCEEDED");
                    continue;
                }
                if (ftcount == maxftcount) {
                    ds.setValue(i, "ftstatus", "MAXREACHED");
                    continue;
                }
                if (ftcount < warnftcount) continue;
                ds.setValue(i, "ftstatus", "WARN");
            }
        }
        return ds;
    }

    private DataSet getInitialCheckInTrackitems(List trackitems) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        String rsetid = null;
        SafeSQL safeSQL = new SafeSQL();
        if (trackitems.size() <= 750) {
            sql.append("select trackitemid, linksdcid, linkkeyid1, freezethawflag, freezethawcount, freezethawcountmax, ");
            sql.append(" freezethawcountwarn, 'N' initialflag, 'checkin' ftaction, 'Initial Check-In' ftstatus, '' description");
            sql.append(" from trackitem");
            sql.append(" where trackitemid in (").append(safeSQL.addIn(OpalUtil.toDelimitedString(trackitems, "','"))).append(")");
            sql.append(" and freezethawflag = 'Y'");
            sql.append(" and ( freezethawcount = -1 or freezethawcount is null )");
            sql.append(" and freezethawcountmax is not null");
            sql.append(" order by linkkeyid1");
        } else {
            rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", OpalUtil.toDelimitedString(trackitems, ";"), null, null);
            sql.append("select trackitemid, linksdcid, linkkeyid1, freezethawflag, freezethawcount, freezethawcountmax, ");
            sql.append(" freezethawcountwarn, 'N' initialflag, 'checkin' ftaction, 'Initial Check-In' ftstatus, '' description");
            sql.append(" from trackitem");
            sql.append(" where trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            sql.append(" and freezethawflag = 'Y'");
            sql.append(" and ( freezethawcount = -1 or freezethawcount is null )");
            sql.append(" and freezethawcountmax is not null");
            sql.append(" order by linkkeyid1");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (StringUtil.getLen(rsetid) > 0L) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        return ds;
    }

    private DataSet getRegularCheckInTrackitems(List trackitems) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        String rsetid = null;
        SafeSQL safeSQL = new SafeSQL();
        if (trackitems.size() <= 750) {
            sql.append("select trackitemid, linksdcid, linkkeyid1, freezethawflag, freezethawcount, freezethawcountmax, ");
            sql.append(" freezethawcountwarn, 'N' initialflag, 'checkin' ftaction, 'OK' ftstatus, '' description");
            sql.append(" from trackitem");
            sql.append(" where trackitemid in (").append(safeSQL.addIn(OpalUtil.toDelimitedString(trackitems, "','"))).append(")");
            sql.append(" and freezethawflag = 'Y'");
            sql.append(" and freezethawcountmax is not null");
            sql.append(" and ( freezethawcount <> -1 )");
            sql.append(" order by linkkeyid1");
        } else {
            rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", OpalUtil.toDelimitedString(trackitems, ";"), null, null);
            sql.append("select trackitemid, linksdcid, linkkeyid1, freezethawflag, freezethawcount, freezethawcountmax, ");
            sql.append(" freezethawcountwarn, 'N' initialflag, 'checkin' ftaction, 'OK' ftstatus, '' description");
            sql.append(" from trackitem");
            sql.append(" where trackitemid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            sql.append(" and freezethawflag = 'Y'");
            sql.append(" and freezethawcountmax is not null");
            sql.append(" and ( freezethawcount <> -1 )");
            sql.append(" order by linkkeyid1");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (StringUtil.getLen(rsetid) > 0L) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                int ftcount = ds.getInt(i, "freezethawcount", 0);
                if (ftcount == 0) {
                    ds.setValue(i, "freezethawcount", "0");
                }
                int warnftcount = ds.getInt(i, "freezethawcountwarn", 0);
                int maxftcount = ds.getInt(i, "freezethawcountmax", 0);
                if (ftcount > maxftcount) {
                    ds.setValue(i, "ftstatus", "EXCEEDED");
                    continue;
                }
                if (ftcount == maxftcount) {
                    ds.setValue(i, "ftstatus", "MAXREACHED");
                    continue;
                }
                if (ftcount < warnftcount) continue;
                ds.setValue(i, "ftstatus", "WARN");
            }
        }
        return ds;
    }

    private boolean isFreezeThawCandidate(String storageunitid) {
        if (!this.candidateMap.containsKey(storageunitid)) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select s.storageunitid, s.ancestorid, s.storageenvid,");
            sql.append(" ( select env.freezethawcandidateflag from storageenv env where env.storageenvid = s.storageenvid ) ftflag1,");
            sql.append(" ( select env.freezethawcandidateflag from storageenv env where env.storageenvid = ( select su.storageenvid ");
            sql.append(" from storageunit su where su.storageunitid = s.ancestorid ) ) ftflag2");
            sql.append(" from storageunit s ");
            sql.append(" where s.storageunitid = ").append(safeSQL.addVar(storageunitid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() == 1) {
                if ("Y".equals(ds.getValue(0, "ftflag1")) || "Y".equals(ds.getValue(0, "ftflag2"))) {
                    this.candidateMap.put(storageunitid, "Y");
                } else {
                    this.candidateMap.put(storageunitid, "N");
                }
            }
        }
        return "Y".equals(this.candidateMap.get(storageunitid));
    }
}


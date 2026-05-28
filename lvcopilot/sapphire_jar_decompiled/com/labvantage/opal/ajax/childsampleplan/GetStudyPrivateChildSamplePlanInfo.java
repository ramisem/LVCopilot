/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.childsampleplan;

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetStudyPrivateChildSamplePlanInfo
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String rownum = ajaxResponse.getRequestParameter("rownum");
        String sdiworkitemid = ajaxResponse.getRequestParameter("sdiworkitemid");
        String r = "-1";
        String childsampleplanid = "";
        String childsampleplanversionid = "";
        StringBuilder sql = new StringBuilder();
        sql.append("select wi.embedchildsampleplanid, wi.embedchildsampleplanversionid, wi.supportembeddedchildplanflag, s.embedchildsampleplanid privateplanid, s.embedchildsampleplanversionid privateplanversionid");
        sql.append(" from workitem wi, sdiworkitem s");
        sql.append(" where wi.workitemid = s.workitemid");
        sql.append(" and wi.workitemversionid = s.workitemversionid");
        sql.append(" and s.sdiworkitemid = ?");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{sdiworkitemid});
        if (ds != null && ds.size() > 0) {
            String supportembeddedchildplanflag = ds.getString(0, "supportembeddedchildplanflag", "N");
            if ("Y".equals(supportembeddedchildplanflag)) {
                String privateplanid = ds.getString(0, "privateplanid", "");
                if (privateplanid.length() > 0) {
                    r = "1";
                    childsampleplanid = privateplanid;
                    childsampleplanversionid = ds.getString(0, "privateplanversionid");
                } else {
                    r = "0";
                }
            }
        } else {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("select workitemid, workitemversionid, embedchildsampleplanid privateplanid, embedchildsampleplanversionid privateplanversionid from sdiworkitem where sdiworkitemid = ?", (Object[])new String[]{sdiworkitemid});
            if (ds != null && ds.size() > 0) {
                String workitemid = ds.getString(0, "workitemid");
                String workitemversionid = ds.getString(0, "workitemversionid", "");
                String privateplanid = ds.getString(0, "privateplanid", "");
                String privateplanversionid = ds.getString(0, "privateplanversionid", "");
                if (OpalUtil.isNotEmpty(privateplanid)) {
                    r = "1";
                    childsampleplanid = privateplanid;
                    childsampleplanversionid = privateplanversionid;
                } else {
                    DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet("select embedchildsampleplanid, embedchildsampleplanversionid, supportembeddedchildplanflag, versionstatus from workitem where workitemid = ? and versionstatus in ( 'P', 'C' ) order by createdt desc", (Object[])new String[]{workitemid});
                    if (_ds != null && _ds.size() > 0) {
                        String supportembeddedchildplanflag = _ds.getValue(0, "supportembeddedchildplanflag", "N");
                        String embedchildsampleplanid = _ds.getValue(0, "embedchildsampleplanid", "");
                        String embedchildsampleplanversionid = _ds.getValue(0, "embedchildsampleplanversionid", "");
                        boolean fromcurrentversion = OpalUtil.isEmpty(workitemversionid);
                        for (int i = 0; i < _ds.size(); ++i) {
                            if (fromcurrentversion) {
                                if (!"C".equals(_ds.getString(i, "versionstatus"))) continue;
                                supportembeddedchildplanflag = _ds.getString(i, "supportembeddedchildplanflag", "N");
                                embedchildsampleplanid = _ds.getString(i, "embedchildsampleplanid", "");
                                embedchildsampleplanversionid = _ds.getString(i, "embedchildsampleplanversionid", "");
                                break;
                            }
                            if (!workitemversionid.equals(_ds.getString(i, "workitemversionid"))) continue;
                            supportembeddedchildplanflag = _ds.getString(i, "supportembeddedchildplanflag", "N");
                            embedchildsampleplanid = _ds.getString(i, "embedchildsampleplanid", "");
                            embedchildsampleplanversionid = _ds.getString(i, "embedchildsampleplanversionid", "");
                            break;
                        }
                        if ("Y".equals(supportembeddedchildplanflag)) {
                            r = "0";
                            childsampleplanid = embedchildsampleplanid;
                            childsampleplanversionid = embedchildsampleplanversionid;
                        }
                    }
                }
            }
        }
        ajaxResponse.addCallbackArgument("rownum", rownum);
        ajaxResponse.addCallbackArgument("response", r);
        ajaxResponse.addCallbackArgument("sdiworkitemid", sdiworkitemid);
        ajaxResponse.addCallbackArgument("childsampleplanid", childsampleplanid);
        ajaxResponse.addCallbackArgument("childsampleplanversionid", childsampleplanversionid);
        ajaxResponse.print();
    }
}


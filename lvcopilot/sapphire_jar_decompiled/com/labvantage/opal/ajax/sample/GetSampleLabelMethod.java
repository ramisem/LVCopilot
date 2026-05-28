/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.sample;

import com.labvantage.opal.util.OpalUtil;
import java.util.HashSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

@Deprecated
public class GetSampleLabelMethod
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "");
        String userprinter = this.getConfigurationProcessor().getProfileProperty("sysuser_config_accession_printer");
        String labelmethodid = "";
        String labelmethodversionid = "";
        if (OpalUtil.isNotEmpty(sampleid)) {
            StringBuilder sql = new StringBuilder();
            sql.append("select s.s_sampleid, s.sampletypeid, ");
            SafeSQL safeSQL = new SafeSQL();
            sql.append("  (select sp.labelmethodid from s_eventdefstspecimendef sp where sp.s_eventdefid = sf.eventdefid and sp.s_specimendefid = sf.specimendefid and sp.s_sampletypeid = sf.sampletypeid) labelmethodid,");
            sql.append("  (select sp.labelmethodversionid from s_eventdefstspecimendef sp where sp.s_eventdefid = sf.eventdefid and sp.s_specimendefid = sf.specimendefid and sp.s_sampletypeid = sf.sampletypeid) labelmethodversionid");
            if (this.getConnectionProcessor().getSapphireConnection().isOracle()) {
                sql.append(" ,(select csp.labelmethodid from s_childsampleplanitem csp, s_samplemap sm where csp.s_childsampleplanitemid = sm.childsampleplanitemid and csp.s_childsampleplanid = sm.childsampleplanid and csp.s_childsampleplanversionid = sm.childsampleplanversionid and sm.destsampleid = s.s_sampleid and rownum = 1) childlabelmethodid");
                sql.append(" ,(select csp.labelmethodversionid from s_childsampleplanitem csp, s_samplemap sm where csp.s_childsampleplanitemid = sm.childsampleplanitemid and csp.s_childsampleplanid = sm.childsampleplanid and csp.s_childsampleplanversionid = sm.childsampleplanversionid and sm.destsampleid = s.s_sampleid and rownum = 1) childlabelmethodversionid");
            } else {
                sql.append(" ,(select top(1) csp.labelmethodid from s_childsampleplanitem csp, s_samplemap sm where csp.s_childsampleplanitemid = sm.childsampleplanitemid and csp.s_childsampleplanid = sm.childsampleplanid and csp.s_childsampleplanversionid = sm.childsampleplanversionid and sm.destsampleid = s.s_sampleid) childlabelmethodid");
                sql.append(" ,(select top(1) csp.labelmethodversionid from s_childsampleplanitem csp, s_samplemap sm where csp.s_childsampleplanitemid = sm.childsampleplanitemid and csp.s_childsampleplanid = sm.childsampleplanid and csp.s_childsampleplanversionid = sm.childsampleplanversionid and sm.destsampleid = s.s_sampleid) childlabelmethodversionid");
            }
            sql.append("  from s_sample s, s_samplefamily sf");
            sql.append(" where sf.s_samplefamilyid = s.samplefamilyid");
            sql.append(" and s.s_sampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                HashSet<String> labelMethodSet = new HashSet<String>();
                HashSet<String> labelMethodVersionSet = new HashSet<String>();
                for (int i = 0; i < ds.size(); ++i) {
                    String methodid = ds.getString(i, "childlabelmethodid", ds.getString(i, "labelmethodid", ""));
                    if (!OpalUtil.isNotEmpty(methodid)) {
                        labelMethodSet.clear();
                        labelMethodVersionSet.clear();
                        break;
                    }
                    String methodversionid = ds.getString(i, "childlabelmethodversionid", ds.getString(i, "labelmethodversionid", "1"));
                    labelMethodSet.add(methodid);
                    labelMethodVersionSet.add(methodversionid);
                }
                if (labelMethodSet.size() > 0) {
                    labelmethodid = OpalUtil.toDelimitedString(labelMethodSet, ";");
                    labelmethodversionid = OpalUtil.toDelimitedString(labelMethodVersionSet, ";");
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("labelmethodid", labelmethodid);
        ajaxResponse.addCallbackArgument("labelmethodversionid", labelmethodversionid);
        ajaxResponse.addCallbackArgument("userprinter", userprinter);
        ajaxResponse.print();
    }
}


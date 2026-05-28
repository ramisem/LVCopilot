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
import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import java.util.HashSet;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class MultiSampleChild
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String clinicalflag;
        String message;
        AjaxResponse ajaxResponse;
        block14: {
            String[] childStudyArray;
            ajaxResponse = new AjaxResponse(request, response);
            message = "";
            String sampleid = "";
            clinicalflag = "";
            HashSet<String> studySet = new HashSet<String>();
            for (String studyid : childStudyArray = StringUtil.split(ajaxResponse.getRequestParameter("child_studyid", ""), ";")) {
                if (!OpalUtil.isNotEmpty(studyid)) continue;
                studySet.add(studyid);
            }
            if (studySet.size() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_studyid, clinicalflag from s_study where s_studyid in (" + safeSQL.addIn(studySet) + ")", safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    clinicalflag = ds.getString(0, "clinicalflag", "N");
                    for (int i = 1; i < ds.size(); ++i) {
                        if (ds.getString(i, "clinicalflag", "N").equals(clinicalflag)) continue;
                        message = this.getTranslationProcessor().translate("All child samples must be created in either Protocol Study or Non Protocol Study");
                        break;
                    }
                }
            }
            if (OpalUtil.isEmpty(message)) {
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("parent_sampleid", ajaxResponse.getRequestParameter("parent_sampleid", ""));
                    props.setProperty("parent_quantity", ajaxResponse.getRequestParameter("parent_quantity", ""));
                    props.setProperty("parent_depleteflag", ajaxResponse.getRequestParameter("parent_depleteflag", "N"));
                    props.setProperty("child_templateid", ajaxResponse.getRequestParameter("child_templateid", ""));
                    props.setProperty("child_copies", ajaxResponse.getRequestParameter("child_copies", ""));
                    props.setProperty("child_quantity", ajaxResponse.getRequestParameter("child_quantity", ""));
                    props.setProperty("child_unit", ajaxResponse.getRequestParameter("child_unit", ""));
                    props.setProperty("child_sampletypeid", ajaxResponse.getRequestParameter("child_sampletypeid", ""));
                    props.setProperty("child_preptypeid", ajaxResponse.getRequestParameter("child_preptypeid", ""));
                    props.setProperty("child_treatmentid", ajaxResponse.getRequestParameter("child_treatmentid", ""));
                    props.setProperty("syncparent", ajaxResponse.getRequestParameter("syncparent", "N"));
                    props.setProperty("mode", ajaxResponse.getRequestParameter("mode", "Aliquot"));
                    props.setProperty("child_studyid", ajaxResponse.getRequestParameter("child_studyid", ""));
                    props.setProperty("child_storagestatus", ajaxResponse.getRequestParameter("child_storagestatus", ""));
                    props.setProperty("child_diluentvolume", ajaxResponse.getRequestParameter("child_diluentvolume", ""));
                    props.setProperty("child_diluentvolumeunit", ajaxResponse.getRequestParameter("child_diluentvolumeunit", ""));
                    props.setProperty("parent_disposeflag", ajaxResponse.getRequestParameter("parent_disposalflag", "Y"));
                    props.setProperty("copydowncolumns", ajaxResponse.getRequestParameter("copydowncolumns", ""));
                    if (OpalUtil.isNotEmpty(ajaxResponse.getRequestParameter("child_concentration", ""))) {
                        props.setProperty("childcolumn_concentration", ajaxResponse.getRequestParameter("child_concentration", ""));
                        props.setProperty("childcolumn_concentrationunits", ajaxResponse.getRequestParameter("child_concentrationunits", ""));
                    }
                    try {
                        int i;
                        DataSet ds = new DataSet();
                        JSONArray columnDataArray = new JSONArray(ajaxResponse.getRequestParameter("child_columndata", new JSONArray().toString()));
                        for (i = 0; i < columnDataArray.length(); ++i) {
                            JSONObject jsonObject = columnDataArray.getJSONObject(i);
                            if (jsonObject == null) continue;
                            int row = ds.addRow();
                            Iterator iterator = jsonObject.keys();
                            while (iterator.hasNext()) {
                                Object o = iterator.next();
                                if (!(o instanceof String)) continue;
                                String columnid = (String)o;
                                String value = jsonObject.getString(columnid);
                                ds.setString(row, columnid, value);
                            }
                        }
                        if (ds.size() > 0) {
                            for (i = 0; i < ds.getColumnCount(); ++i) {
                                String columnid = ds.getColumnId(i);
                                String value = ds.getColumnValues(columnid, ";");
                                props.setProperty("childcolumn_" + columnid, value);
                            }
                        }
                        String child_aliasdata = ajaxResponse.getRequestParameter("child_aliasdata", "");
                        props.setProperty("child_aliasdata", child_aliasdata);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    props.setProperty("actionid", "MultiSampleChild");
                    props.setProperty("actionversionid", "1");
                    props.setProperty("processassysuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                    props.setProperty("__trackprogressid", ajaxResponse.getRequestParameter("__trackprogressid"));
                    props.setProperty("auditreason", ajaxResponse.getRequestParameter("auditreason", ""));
                    props.setProperty("auditsignedflag", ajaxResponse.getRequestParameter("auditsignedflag", ""));
                    props.setProperty("auditactivity", ajaxResponse.getRequestParameter("auditactivity", ""));
                    this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props);
                }
                catch (ActionException e) {
                    ErrorHandler errorHandler = e.getErrorHandler();
                    if (errorHandler == null) break block14;
                    message = ((ErrorDetail)errorHandler.get(0)).getMessage();
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("clinicalflag", clinicalflag);
        ajaxResponse.print();
    }
}


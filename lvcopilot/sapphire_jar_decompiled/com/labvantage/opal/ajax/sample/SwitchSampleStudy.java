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
import com.labvantage.sapphire.actions.sdi.EditSDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SwitchSampleStudy
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "").trim();
        String studyid = ajaxResponse.getRequestParameter("studyid", "").trim();
        String extraprops = ajaxResponse.getRequestParameter("extraprops", "").trim();
        String clinicalflag = "N";
        if (sampleid.length() > 0 && studyid.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            String studydefaultglp = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "defaultglpflag", "s_studyid = ?", new String[]{studyid});
            if (!"Y".equals(studydefaultglp)) {
                boolean glpswitched = false;
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid, glpflag from s_sample where s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ")", safeSQL.getValues());
                for (int i = 0; i < ds.size(); ++i) {
                    if (!"Y".equals(ds.getString(i, "glpflag"))) continue;
                    ds.setString(i, "glpflag", "N");
                    glpswitched = true;
                }
                if (glpswitched) {
                    props.setProperty("keyid1", ds.getColumnValues("s_sampleid", ";"));
                    props.setProperty("glpflag", ds.getColumnValues("glpflag", ";"));
                    props.setProperty("sstudyid", StringUtil.repeat(studyid, ds.size(), ";"));
                } else {
                    props.setProperty("keyid1", sampleid);
                    props.setProperty("sstudyid", studyid);
                }
            } else {
                props.setProperty("keyid1", sampleid);
                props.setProperty("sstudyid", studyid);
            }
            props.setProperty("auditreason", "Study switched on sample by " + this.getConnectionProcessor().getSapphireConnection().getSysuserId());
            props.setProperty("__sdcruleconfirm", "Y");
            try {
                if (extraprops.length() > 0) {
                    String[] eparray;
                    for (String s : eparray = StringUtil.split(extraprops, ";")) {
                        int index = s.indexOf("=");
                        if (index == -1) continue;
                        String property = s.substring(0, index);
                        String value = s.substring(index + 1);
                        if (!property.startsWith("audit")) continue;
                        props.setProperty(property, value);
                    }
                }
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                clinicalflag = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "clinicalflag", "s_studyid=?", new String[]{studyid});
            }
            catch (ActionException e) {
                message = this.getTranslationProcessor().translate("Error while switching study on sample") + ": " + e.getMessage();
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.addCallbackArgument("clinicalflag", clinicalflag);
        ajaxResponse.print();
    }
}


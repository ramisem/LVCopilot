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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateSamplePool
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 103132 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String sampleid;
        String error;
        AjaxResponse ajaxResponse;
        block4: {
            ajaxResponse = new AjaxResponse(request, response);
            error = "";
            sampleid = "";
            try {
                String[] v;
                String[] s;
                PropertyList props = new PropertyList();
                props.setProperty("sampleid", ajaxResponse.getRequestParameter("sampleid", ""));
                props.setProperty("quantity", ajaxResponse.getRequestParameter("quantity", ""));
                props.setProperty("poolcopies", ajaxResponse.getRequestParameter("poolcopies", "1"));
                props.setProperty("pooltemplateid", ajaxResponse.getRequestParameter("pooltemplateid", ""));
                props.setProperty("poolquantity", ajaxResponse.getRequestParameter("poolquantity", ""));
                props.setProperty("poolunitid", ajaxResponse.getRequestParameter("poolunitid", ""));
                props.setProperty("poolcontainertypeid", ajaxResponse.getRequestParameter("poolcontainertypeid", ""));
                props.setProperty("poolcustodialdepartmentid", ajaxResponse.getRequestParameter("poolcustodialdepartmentid", ""));
                props.setProperty("poolglpflag", ajaxResponse.getRequestParameter("poolglpflag", "N"));
                props.setProperty("childstoragestatus", ajaxResponse.getRequestParameter("childstoragestatus", ""));
                props.setProperty("poolmode", ajaxResponse.getRequestParameter("poolmode", "BioBank"));
                props.setProperty("enablequantitycalculation", ajaxResponse.getRequestParameter("enablequantitycalculation", "Y"));
                props.setProperty("childstudyid", ajaxResponse.getRequestParameter("childstudyid", "POOL"));
                props.setProperty("disposesampleid", ajaxResponse.getRequestParameter("disposesampleid", ""));
                props.setProperty("child_aliasdata", ajaxResponse.getRequestParameter("child_aliasdata", ""));
                props.setProperty("auditreason", ajaxResponse.getRequestParameter("auditreason", ""));
                props.setProperty("auditsignedflag", ajaxResponse.getRequestParameter("auditsignedflag", ""));
                props.setProperty("auditactivity", ajaxResponse.getRequestParameter("auditactivity", ""));
                String child_columnid = ajaxResponse.getRequestParameter("child_columnid", "");
                if (OpalUtil.isNotEmpty(child_columnid) && (s = StringUtil.split(child_columnid, ";")).length == (v = StringUtil.split(ajaxResponse.getRequestParameter("child_columnvalue", ""), ";")).length) {
                    int index = 0;
                    for (String columnid : s) {
                        props.setProperty("childcolumn_" + columnid, v[index++]);
                    }
                }
                this.getActionProcessor().processActionClass(com.labvantage.sapphire.actions.sms.CreateSamplePool.class.getName(), props);
                sampleid = props.getProperty("newkeyid1");
            }
            catch (ActionException e) {
                ErrorHandler errorHandler = e.getErrorHandler();
                if (errorHandler == null) break block4;
                error = ((ErrorDetail)errorHandler.get(0)).getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.addCallbackArgument("message", error);
        ajaxResponse.print();
    }
}


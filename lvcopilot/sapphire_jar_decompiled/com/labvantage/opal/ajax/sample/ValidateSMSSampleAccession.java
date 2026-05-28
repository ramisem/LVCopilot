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
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateSMSSampleAccession
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        String clinicalflag = "N";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "");
        String validatestoragestatus = ajaxResponse.getRequestParameter("storagestatus", "").trim();
        String fixsmssampledata = ajaxResponse.getRequestParameter("fixsmssampledata", "N");
        if (OpalUtil.isNotEmpty(sampleid)) {
            sampleid = StringUtil.replaceAll(sampleid, "%3B", ";");
            ArrayList ds = null;
            if (StringUtil.split(sampleid, ";").length > 1000) {
                String rsetid = null;
                try {
                    rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sample.sstudyid, s_sample.samplefamilyid, s_sample.storagestatus,  (select s_study.clinicalflag from s_study where s_study.s_studyid = s_sample.sstudyid) clinicalflag from s_sample where s_sample.s_sampleid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)", (Object[])new String[]{rsetid});
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
                finally {
                    if (rsetid != null) {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "select s_sample.sstudyid, s_sample.samplefamilyid, s_sample.storagestatus,  (select s_study.clinicalflag from s_study where s_study.s_studyid = s_sample.sstudyid) clinicalflag from s_sample where s_sample.s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ")";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
            if (ds != null && ds.size() > 0) {
                clinicalflag = ((DataSet)ds).getString(0, "clinicalflag", "N");
                for (int i = 0; i < ds.size(); ++i) {
                    if (i > 0 && !((DataSet)ds).getString(i, "clinicalflag", "N").equals(clinicalflag)) {
                        message = this.getTranslationProcessor().translate("All selected samples must either belong to a Non-Protocol study or a Protocol Study");
                        break;
                    }
                    if (((DataSet)ds).getString(i, "samplefamilyid", "").length() == 0) {
                        message = this.getTranslationProcessor().translate("One or more of the selected samples are missing Sample Family.");
                        break;
                    }
                    if ("Y".equals(fixsmssampledata)) continue;
                    String storagestatus = ((DataSet)ds).getString(i, "storagestatus");
                    if (validatestoragestatus.length() > 0) {
                        if (validatestoragestatus.equals(storagestatus)) continue;
                        message = this.getTranslationProcessor().translate("One or more selected sample(s) not in valid status") + " (" + validatestoragestatus + ")";
                        break;
                    }
                    if (!"Disposed".equals(storagestatus) && !"Archived".equals(storagestatus) && !"3rd Party Transfer".equals(storagestatus)) continue;
                    message = this.getTranslationProcessor().translate("One or more of the selected sample(s) has(ve) Disposed, Archived or 3rd Party Transfer status.");
                    break;
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.addCallbackArgument("pageid", ajaxResponse.getRequestParameter("Y".equals(clinicalflag) ? "protocolpageid" : "nonprotocolpageid"));
        ajaxResponse.print();
    }
}


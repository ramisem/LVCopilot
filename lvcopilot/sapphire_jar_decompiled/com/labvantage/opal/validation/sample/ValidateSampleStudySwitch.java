/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.sample;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.ddt.Sample;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ValidateSampleStudySwitch
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String errmessage;
        String message;
        AjaxResponse ajaxResponse;
        block19: {
            ajaxResponse = new AjaxResponse(request, response);
            StringBuilder sql = new StringBuilder();
            message = "";
            errmessage = "";
            String sampleid = ajaxResponse.getRequestParameter("keyid1");
            if (OpalUtil.isNotEmpty(sampleid)) {
                sampleid = StringUtil.replaceAll(sampleid, "%3B", ";");
                String rsetid = null;
                try {
                    rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
                    String studyid = ajaxResponse.getRequestParameter("studyid", "");
                    if (OpalUtil.isNotEmpty(studyid)) {
                        String studystatus = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "studystatus", "s_studyid = ?", new String[]{studyid});
                        if ("Completed".equals(studystatus)) {
                            errmessage = this.getTranslationProcessor().translate("The new study is already Completed. Changing study of a Sample to Completed study is not allowed.");
                        } else if ("Cancelled".equals(studystatus)) {
                            errmessage = this.getTranslationProcessor().translate("The new study is Cancelled. Changing study of a Sample to Cancelled study is not allowed.");
                        } else {
                            sampleid = StringUtil.replaceAll(sampleid, "%3B", ";");
                            String glpflag = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "defaultglpflag", "s_studyid = ?", new String[]{studyid});
                            if (!"Y".equals(glpflag)) {
                                sql.append("select s.s_sampleid, s.glpflag");
                                sql.append(" from s_sample s");
                                sql.append(" where s.s_sampleid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)");
                                sql.append(" and glpflag = 'Y'");
                                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                                if (ds != null && ds.size() > 0) {
                                    message = this.getTranslationProcessor().translate("The new study is non GLP and selected sample(s) will lose GLP status");
                                }
                            }
                        }
                        ajaxResponse.addCallbackArgument("studyid", studyid);
                        ajaxResponse.addCallbackArgument("sampleid", sampleid);
                        break block19;
                    }
                    sql.append("select s.s_sampleid, s.sstudyid, s.storagestatus,");
                    sql.append(" (select count(sm.destsampleid) from s_samplemap sm where sm.destsampleid = s.s_sampleid) childcount");
                    sql.append(" ,(select t.custodialdepartmentid from trackitem t where t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid) custodialdepartmentid");
                    sql.append(" from s_sample s");
                    sql.append(" where s.s_sampleid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ?)");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                    if (ds == null || ds.size() <= 0) break block19;
                    for (int i = 0; i < ds.size(); ++i) {
                        String custodialdepartmentid = ds.getString(i, "custodialdepartmentid", "");
                        if (!("Allocated".equals(ds.getString(i, "storagestatus", "")) || custodialdepartmentid.length() != 0 && this.getDepartmentList().contains(custodialdepartmentid))) {
                            message = this.getTranslationProcessor().translate("One of more of the selected sample(s) are not in your Custodial Domain.");
                        } else if (!Sample.validStudySwitchStatusList.contains(ds.getString(i, "storagestatus"))) {
                            message = this.getTranslationProcessor().translate("One of more of the selected sample(s) are not in valid status for Study switch.") + "<br><br>" + this.getTranslationProcessor().translate("You can only switch study on the samples with status of Allocated, Received or Temporary In Lab.");
                        } else {
                            if (ds.getInt(i, "childcount") <= 0) continue;
                            message = this.getTranslationProcessor().translate("One of more of the selected sample(s) is a child Sample. Please select the parent sample to switch study.");
                        }
                        break;
                    }
                }
                catch (SapphireException e) {
                    errmessage = this.getTranslationProcessor().translate("Unable to create RSET for selected samples.");
                }
                finally {
                    if (OpalUtil.isNotEmpty(rsetid)) {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("errmessage", errmessage);
        ajaxResponse.print();
    }
}


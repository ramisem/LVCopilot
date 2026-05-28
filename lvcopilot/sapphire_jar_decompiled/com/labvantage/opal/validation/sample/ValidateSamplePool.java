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

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.BaseAjaxValidation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateSamplePool
extends BaseAjaxValidation {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String sampleid = ajaxResponse.getRequestParameter("keyid1");
        if (OpalUtil.isNotEmpty(sampleid)) {
            sampleid = StringUtil.replaceAll(sampleid, "%3B", ";");
            try {
                String policyChildStorageStatus = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("childsamplestatus", "In Prep");
                boolean isGLPRuleActive = this.isBBRuleActive("GLP Rule");
                HashMap<String, List<String>> exceptions = new HashMap<String, List<String>>();
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                sql.append("select s.s_sampleid, s.glpflag, s.sampletypeid, s.storagestatus, s.confirmedby, t.custodialuserid, t.custodialdepartmentid");
                sql.append(" from s_sample s, trackitem t");
                sql.append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleid, ";")).append(")");
                sql.append(" and t.linksdcid = 'Sample'");
                sql.append(" and t.linkkeyid1 = s.s_sampleid");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    String compsampletypeid = ds.getValue(0, "sampletypeid");
                    String sysuserid = this.getSysUserId();
                    boolean allParentsAreGLP = true;
                    boolean allParentsInUserCustody = true;
                    for (int i = 0; i < ds.size(); ++i) {
                        sampleid = ds.getValue(i, "s_sampleid");
                        String glpflag = ds.getValue(i, "glpflag", "N");
                        String storagestatus = ds.getValue(i, "storagestatus");
                        String sampletypeid = ds.getValue(i, "sampletypeid");
                        String samplecustodian = ds.getValue(i, "custodialuserid");
                        String sampleCD = ds.getValue(i, "custodialdepartmentid");
                        if (!sampletypeid.equals(compsampletypeid)) {
                            throw new SapphireException(this.getTranslationProcessor().translate("All selected samples must have same Sample Type for pooling."));
                        }
                        if (!this.isDepartmentMember(sampleCD)) {
                            this.addExceptionMessage(exceptions, sampleid, this.getTranslationProcessor().translate("User is not a member of Sample's Custodial Department"));
                        }
                        if (!"Y".equals(glpflag)) {
                            allParentsAreGLP = false;
                        }
                        if (!sysuserid.equals(samplecustodian)) {
                            allParentsInUserCustody = false;
                        }
                        if ("In Circulation".equals(storagestatus) || "Temporary In Lab".equals(storagestatus)) continue;
                        this.addExceptionMessage(exceptions, sampleid, this.getTranslationProcessor().translate("All selected samples must be either \"In Circulation\" or \"Temporary In Lab\""));
                    }
                    if (allParentsAreGLP && !allParentsInUserCustody && isGLPRuleActive) {
                        String childStorageStatus = policyChildStorageStatus;
                        if ("inherit".equals(policyChildStorageStatus)) {
                            String parentStorageStatus = ds.getString(0, "storagestatus", "");
                            for (int i = 1; i < ds.size(); ++i) {
                                if (!parentStorageStatus.equals(ds.getString(i, "storagestatus", ""))) continue;
                                childStorageStatus = "In Prep";
                            }
                        }
                        if ("In Circulation".equals(childStorageStatus)) {
                            this.addExceptionMessage(exceptions, this.getTranslationProcessor().translate("Custody Error"), this.getTranslationProcessor().translate("Child sample(s) are being created as \"In Circulation\" so user must take the custody of parent GLP samples for this Operation"));
                        }
                    }
                    if (exceptions.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (Object o : exceptions.keySet()) {
                            String key = (String)o;
                            sb.append("<u>").append(key).append("</u><br>").append("<ul>");
                            List<String> list = exceptions.get(key);
                            for (String aList : list) {
                                sb.append("<li>").append((Object)aList).append("</li>");
                            }
                            sb.append("</ul>");
                        }
                        throw new SapphireException(sb.toString());
                    }
                }
            }
            catch (SapphireException e) {
                message = ErrorUtil.formatErrorMessage(e.getMessage());
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private void addExceptionMessage(HashMap<String, List<String>> exceptions, String sampleid, String message) {
        if (!exceptions.containsKey(sampleid)) {
            exceptions.put(sampleid, new ArrayList());
        }
        exceptions.get(sampleid).add(message);
    }
}


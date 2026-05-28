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

public class ValidateSampleChild
extends BaseAjaxValidation {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53421 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("keyid1");
        boolean validatesampletypeflag = "Y".equals(ajaxResponse.getRequestParameter("validatesampletype"));
        String message = "";
        if (StringUtil.getLen(sampleid) == 0L) {
            message = ErrorUtil.formatErrorMessage("Sampleid not found in request. Please make sure you selected samples. If problem persists, contact Administrator.");
        } else {
            try {
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                String sampleInClause = StringUtil.replaceAll(sampleid, ";", "','");
                sql.append("select s.s_sampleid, (select count(t2.trackitemid) from trackitem t2 where t2.linksdcid = 'Sample' and t2.linkkeyid1 = s.s_sampleid ) ticount");
                sql.append(" from s_sample s");
                sql.append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleInClause)).append(")");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    for (int i = 0; i < ds.size(); ++i) {
                        int count = ds.getInt(i, "ticount");
                        if (count == 0) {
                            throw new SapphireException("[" + ds.getValue(i, "s_sampleid") + "] " + this.getTranslationProcessor().translate("Sample does not have associated TrackItem."));
                        }
                        if (count <= 1) continue;
                        throw new SapphireException("[" + ds.getValue(i, "s_sampleid") + "] " + this.getTranslationProcessor().translate("Sample has multiple TrackItems."));
                    }
                }
                String policyChildStorageStatus = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("childsamplestatus", "In Prep");
                boolean isGLPRuleActive = this.isBBRuleActive("GLP Rule");
                HashMap<String, List<String>> exceptions = new HashMap<String, List<String>>();
                safeSQL.reset();
                sql.setLength(0);
                sql.append("select s.s_sampleid, s.glpflag, s.sampletypeid, s.storagestatus, s.confirmedby, t.custodialuserid, t.custodialdepartmentid");
                sql.append(" from s_sample s, trackitem t");
                sql.append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleInClause)).append(")");
                sql.append(" and t.linksdcid = 'Sample'");
                sql.append(" and t.linkkeyid1 = s.s_sampleid");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    String compsampletypeid = ds.getValue(0, "sampletypeid");
                    String sysuserid = this.getSysUserId();
                    for (int i = 0; i < ds.size(); ++i) {
                        sampleid = ds.getValue(i, "s_sampleid");
                        String glpflag = ds.getValue(i, "glpflag", "N");
                        String storagestatus = ds.getValue(i, "storagestatus");
                        String sampletypeid = ds.getValue(i, "sampletypeid");
                        String samplecustodian = ds.getValue(i, "custodialuserid");
                        String sampleCD = ds.getValue(i, "custodialdepartmentid");
                        if (validatesampletypeflag && !sampletypeid.equals(compsampletypeid)) {
                            throw new SapphireException(this.getTranslationProcessor().translate("Samples must have same Sample Type for this operation."));
                        }
                        if (!this.isDepartmentMember(sampleCD)) {
                            this.addExceptionMessage(exceptions, sampleid, this.getTranslationProcessor().translate("User is not a member of Sample's Custodial Department"));
                        }
                        if (isGLPRuleActive && "Y".equals(glpflag)) {
                            String childStorageStatus = policyChildStorageStatus;
                            if ("inherit".equals(policyChildStorageStatus)) {
                                childStorageStatus = storagestatus;
                            }
                            if ("In Circulation".equals(childStorageStatus) && !sysuserid.equals(samplecustodian)) {
                                this.addExceptionMessage(exceptions, sampleid, this.getTranslationProcessor().translate("Child sample(s) are being created as \"In Circulation\" so user must take the custody of parent GLP sample for this Operation"));
                            }
                        }
                        if ("In Circulation".equals(storagestatus) || "Temporary In Lab".equals(storagestatus)) continue;
                        this.addExceptionMessage(exceptions, sampleid, this.getTranslationProcessor().translate("Sample must be In Circulation or Temporary In Lab"));
                    }
                    if (exceptions.size() > 0) {
                        StringBuffer sb = new StringBuffer();
                        for (Object o : exceptions.keySet()) {
                            String key = (String)o;
                            sb.append("<u>").append(key).append("</u><br>").append("<ul>");
                            List list = (List)exceptions.get(key);
                            for (Object aList : list) {
                                sb.append("<li>").append(aList).append("</li>");
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


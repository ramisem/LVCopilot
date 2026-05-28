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

public class ValidateSampleConfirm
extends BaseAjaxValidation {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("keyid1");
        String message = "";
        if (StringUtil.getLen(sampleid) == 0L) {
            message = ErrorUtil.formatErrorMessage("Sampleid not found in request. Please make sure you selected samples. If problem persists, contact Administrator.");
        } else {
            try {
                SafeSQL safeSQL = new SafeSQL();
                String sysuserid = this.getSysUserId();
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
                boolean isGLPRuleActive = this.isBBRuleActive("GLP Rule");
                boolean isUserGLP = "Y".equals(OpalUtil.getColumnValue(this.getQueryProcessor(), "sysuser", "glpflag", "sysuserid = ?", new String[]{sysuserid}));
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
                    for (int i = 0; i < ds.size(); ++i) {
                        sampleid = ds.getValue(i, "s_sampleid");
                        String glpflag = ds.getValue(i, "glpflag", "N");
                        String storagestatus = ds.getString(i, "storagestatus");
                        String sampleCD = ds.getValue(i, "custodialdepartmentid");
                        if (!this.isDepartmentMember(sampleCD)) {
                            this.addExceptionMessage(exceptions, sampleid, this.getTranslationProcessor().translate("User is not a member of Sample's Custodial Department"));
                        }
                        if (isGLPRuleActive && "Y".equals(glpflag) && !isUserGLP) {
                            this.addExceptionMessage(exceptions, sampleid, this.getTranslationProcessor().translate("The Sample will lose GLP status as user is not GLP certified"));
                        }
                        if ("In Prep".equals(storagestatus)) continue;
                        this.addExceptionMessage(exceptions, sampleid, this.getTranslationProcessor().translate("Sample must be in the status of In Prep"));
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


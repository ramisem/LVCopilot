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
import com.labvantage.opal.validation.sample.BaseSampleValidation;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class RemoveGLPValidation
extends BaseSampleValidation {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        ErrorHandler errorHandler;
        String message;
        AjaxResponse ajaxResponse;
        block8: {
            ajaxResponse = new AjaxResponse(request, response);
            message = "";
            String keyid1 = ajaxResponse.getRequestParameter("keyid1");
            errorHandler = new ErrorHandler();
            try {
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer sql = new StringBuffer();
                sql.append("select s.s_sampleid, t.custodialuserid, t.custodialdepartmentid, s.storagestatus, s.glpflag");
                sql.append(" from trackitem t, s_sample s");
                sql.append(" where t.linksdcid = 'Sample'");
                sql.append(" and t.linkkeyid1 in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
                sql.append(" and s.s_sampleid = t.linkkeyid1");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    for (int i = 0; i < ds.size(); ++i) {
                        String sampleid = ds.getValue(i, "s_sampleid");
                        String storagestatus = ds.getValue(i, "storagestatus");
                        String glpflag = ds.getValue(i, "glpflag", "N");
                        String custodialuserid = ds.getValue(i, "custodialuserid");
                        String custodialdepartmentid = ds.getValue(i, "custodialdepartmentid");
                        if (!"Y".equals(glpflag)) {
                            throw new SapphireException("[" + sampleid + "] " + this.getTranslationProcessor().translate("Sample is not GLP"));
                        }
                        if ("Archived".equals(storagestatus) || "3rd Party Transfer".equals(storagestatus)) {
                            throw new SapphireException("[" + sampleid + "] " + this.getTranslationProcessor().translate("Sample does not have a valid storage status"));
                        }
                        if (StringUtil.getLen(custodialuserid) > 0L && !this.getSysUserId().equals(custodialuserid)) {
                            throw new SapphireException("[" + sampleid + "] " + this.getTranslationProcessor().translate("Sample is not in your Custody"));
                        }
                        if (StringUtil.getLen(custodialdepartmentid) <= 0L || this.isDepartmentMember(custodialdepartmentid)) continue;
                        throw new SapphireException("[" + sampleid + "] " + this.getTranslationProcessor().translate("Sample is not in your Custodial Department"));
                    }
                    break block8;
                }
                throw new SapphireException(this.getTranslationProcessor().translate("One or more of the selected sample does not have Trackitem information"));
            }
            catch (SapphireException e) {
                errorHandler.add("Validation", "", "Validation failed", "VALIDATION", e.getMessage());
            }
        }
        if (errorHandler.size() > 0) {
            message = ErrorUtil.formatErrorMessage(errorHandler);
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}


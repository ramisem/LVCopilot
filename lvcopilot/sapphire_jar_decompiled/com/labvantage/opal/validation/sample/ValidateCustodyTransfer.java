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

import com.labvantage.opal.validation.sample.BaseSampleValidation;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateCustodyTransfer
extends BaseSampleValidation {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53421 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        if ("Sample".equals(sdcid)) {
            keyid1 = StringUtil.replaceAll(keyid1, "%3B", ";");
            message = this.validateCustodian(keyid1);
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private String validateCustodian(String keyid1) {
        StringBuffer sb = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select linkkeyid1, custodialuserid, custodialdepartmentid from trackitem");
        sql.append(" where linksdcid = 'Sample'");
        sql.append(" and linkkeyid1 in (").append(safeSQL.addIn(keyid1, ";")).append(" )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String custodianid = ds.getValue(i, "custodialuserid");
                if (custodianid == null || custodianid.trim().length() == 0) {
                    sb.append(this.getTranslationProcessor().translate("One or more of the selected sample does not have any custodian."));
                    sb.append("\n");
                    sb.append(this.getTranslationProcessor().translate(" Transferring of custody is allowed only if Sample is under someone's custody."));
                    break;
                }
                if (this.getSysUserId().equals(custodianid)) {
                    sb.append(this.getTranslationProcessor().translate("You already have the custody of Sample")).append(" \"").append(ds.getValue(i, "linkkeyid1")).append("\".");
                    break;
                }
                String sampleCD = ds.getValue(i, "custodialdepartmentid");
                if (this.isDepartmentMember(sampleCD)) continue;
                sb.append(this.getTranslationProcessor().translate("You are not a member of Sample's Custodial Department"));
            }
        }
        return this.getTranslationProcessor().translatePartial(sb.toString());
    }
}


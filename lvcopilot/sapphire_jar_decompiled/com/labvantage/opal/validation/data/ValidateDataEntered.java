/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.data;

import com.labvantage.sapphire.SDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateDataEntered
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53251 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String keyid3;
        String keyid2;
        String keyid1;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String errorMessage = "";
        String elementId = ajaxResponse.getRequestParameter("elementid");
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        SDI sdi = new SDI(sdcid, keyid1 = ajaxResponse.getRequestParameter("keyid1"), keyid2 = ajaxResponse.getRequestParameter("keyid2"), keyid3 = ajaxResponse.getRequestParameter("keyid3"));
        if (sdi.isValid()) {
            String workitemid = ajaxResponse.getRequestParameter("workitemid");
            String workiteminstance = ajaxResponse.getRequestParameter("workiteminstance");
            if (workitemid != null && workitemid.trim().length() > 0) {
                String[] w1;
                String[] w0;
                if (workiteminstance != null && workiteminstance.trim().length() > 0 && (w0 = StringUtil.split(workitemid, ";")).length == (w1 = StringUtil.split(workiteminstance, ";")).length) {
                    for (int i = 0; i < w0.length; ++i) {
                        if (!ValidateDataEntered.isWorkItemInProgress(this.getQueryProcessor(), sdi, w0[i], w1[i])) continue;
                        errorMessage = this.getTranslationProcessor().translate("Test under progress, unable to delete");
                        break;
                    }
                }
            } else {
                String paramlistid = ajaxResponse.getRequestParameter("paramlistid");
                String paramlistversionid = ajaxResponse.getRequestParameter("paramlistversionid");
                String variantid = ajaxResponse.getRequestParameter("variantid");
                String dataset = ajaxResponse.getRequestParameter("dataset");
                String[] s0 = StringUtil.split(paramlistid, ";");
                String[] s1 = StringUtil.split(paramlistversionid, ";");
                String[] s2 = StringUtil.split(variantid, ";");
                String[] s3 = StringUtil.split(dataset, ";");
                if (s0.length == s1.length && s1.length == s2.length && s2.length == s3.length) {
                    for (int i = 0; i < s0.length; ++i) {
                        if (!ValidateDataEntered.isDataSetInProgress(this.getQueryProcessor(), sdi, s0[i], s1[i], s2[i], s3[i])) continue;
                        errorMessage = this.getTranslationProcessor().translate("Test under progress, unable to delete");
                        break;
                    }
                }
            }
        } else {
            errorMessage = "Unable to validate data. Invalid SDI";
        }
        ajaxResponse.addCallbackArgument("data", errorMessage.toString());
        ajaxResponse.addCallbackArgument("id", elementId.toString());
        ajaxResponse.print();
    }

    public static boolean isWorkItemInProgress(QueryProcessor qp, SDI sdi, String workitemid, String workiteminstance) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select sdidataitem.enteredtext");
        sql.append(" from sdidataitem, sdiworkitemitem");
        sql.append(" where sdidataitem.sdcid = sdiworkitemitem.sdcid");
        sql.append(" and sdidataitem.keyid1 = sdiworkitemitem.keyid1");
        sql.append(" and sdidataitem.keyid2 = sdiworkitemitem.keyid2");
        sql.append(" and sdidataitem.keyid3 = sdiworkitemitem.keyid3");
        sql.append(" and sdidataitem.paramlistid = sdiworkitemitem.itemkeyid1");
        sql.append(" and sdidataitem.paramlistversionid = sdiworkitemitem.itemkeyid2");
        sql.append(" and sdidataitem.variantid = sdiworkitemitem.itemkeyid3");
        sql.append(" and sdidataitem.dataset = sdiworkitemitem.iteminstance");
        sql.append(" and ( sdidataitem.enteredtext is not null and sdidataitem.enteredtext != '(null)' )");
        sql.append(" and sdiworkitemitem.sdcid = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" and sdiworkitemitem.itemsdcid = 'ParamList'");
        sql.append(" and sdiworkitemitem.keyid1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" and sdiworkitemitem.keyid2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" and sdiworkitemitem.keyid3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" and sdiworkitemitem.workitemid = ").append(safeSQL.addVar(workitemid));
        sql.append(" and sdiworkitemitem.workiteminstance = ").append(safeSQL.addVar(workiteminstance));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        ValidateDataEntered.removeBlankEnteredtextRows(ds);
        return ds != null && ds.size() > 0;
    }

    public static boolean isDataSetInProgress(QueryProcessor qp, SDI sdi, String paramlistid, String paramlistversionid, String variantid, String dataset) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select sdidataitem.enteredtext ");
        sql.append(" from sdidataitem, sdidata");
        sql.append(" where sdidataitem.sdcid = sdidata.sdcid");
        sql.append(" and sdidataitem.keyid1 = sdidata.keyid1");
        sql.append(" and sdidataitem.keyid2 = sdidata.keyid2");
        sql.append(" and sdidataitem.keyid3 = sdidata.keyid3");
        sql.append(" and sdidataitem.paramlistid = sdidata.paramlistid");
        sql.append(" and sdidataitem.paramlistversionid = sdidata.paramlistversionid");
        sql.append(" and sdidataitem.variantid = sdidata.variantid");
        sql.append(" and sdidataitem.dataset = sdidata.dataset");
        sql.append(" and ( sdidataitem.enteredtext is not null and sdidataitem.enteredtext != '(null)' )");
        sql.append(" and sdidata.sdcid = ").append(safeSQL.addVar(sdi.getSdcid()));
        sql.append(" and sdidata.keyid1 = ").append(safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" and sdidata.keyid2 = ").append(safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" and sdidata.keyid3 = ").append(safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" and sdidata.paramlistid = ").append(safeSQL.addVar(paramlistid));
        sql.append(" and sdidata.paramlistversionid = ").append(safeSQL.addVar(paramlistversionid));
        sql.append(" and sdidata.variantid = ").append(safeSQL.addVar(variantid));
        sql.append(" and sdidata.dataset = ").append(safeSQL.addVar(dataset));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        ValidateDataEntered.removeBlankEnteredtextRows(ds);
        return ds != null && ds.size() > 0;
    }

    public static void removeBlankEnteredtextRows(DataSet ds) {
        for (int i = ds.getRowCount() - 1; i >= 0; --i) {
            String enteredText = ds.getString(i, "enteredtext", "");
            if (enteredText.trim().length() != 0) continue;
            ds.deleteRow(i);
        }
    }
}


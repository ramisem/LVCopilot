/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.pkg;

import com.labvantage.opal.validation.BaseValidation;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class ValidateCDTPacking
extends BaseValidation {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53268 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String msg = "";
        String trackItemIds = request.getParameter("trackitemids");
        if (trackItemIds != null && trackItemIds.length() > 0) {
            try {
                ValidateCDTPacking.validateCDTPacking(this.getQueryProcessor(), this.getTranslationProcessor(), trackItemIds, this.getSysUserId());
            }
            catch (SapphireException e) {
                msg = e.getMessage();
            }
        } else {
            msg = "Track Item Id not found for the selected item(s)";
        }
        try {
            PrintWriter out = response.getWriter();
            out.print(msg);
            out.flush();
            out.close();
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    public static void validateCDTPacking(QueryProcessor qp, TranslationProcessor tp, String trackItemIds, String sysuserid) throws SapphireException {
        ArrayList<String> nocustody = new ArrayList<String>();
        ArrayList<String> inpackage = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT custodialuserid, currentstorageunitid, linksdcid, linkkeyid1 FROM trackitem ");
        sql.append(" WHERE trackitemid IN (").append(safeSQL.addIn(trackItemIds, ";")).append(" )");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String custodialuserid = ds.getValue(i, "custodialuserid");
                if (!sysuserid.equals(custodialuserid)) {
                    nocustody.add(ds.getValue(i, "linkkeyid1"));
                }
                String currentStorageUnitId = ds.getValue(i, "currentstorageunitid", "");
                String storageNode = "";
                if (currentStorageUnitId.length() <= 0 || (storageNode = StorageUnitSDC.getStorageNodeBySDC(qp, currentStorageUnitId, "LV_Package")).length() == 0) continue;
                inpackage.add(ds.getValue(i, "linkkeyid1"));
            }
        }
        StringBuffer sb = new StringBuffer();
        if (nocustody.size() > 0) {
            sb.append(tp.translate("Unable to perform CDT Transfer")).append("<br>");
            sb.append(tp.translate("The following items are not in your custody"));
            for (int i = 0; i < nocustody.size(); ++i) {
                sb.append("<br>").append(i + 1).append(". ").append(nocustody.get(i));
            }
        }
        if (inpackage.size() > 0) {
            if (sb.length() == 0) {
                sb.append(tp.translate("Unable to perform CDT Transfer")).append("<br>");
            } else {
                sb.append("<br><br>");
            }
            sb.append(tp.translate("The following items are already in a Package"));
            for (int i = 0; i < inpackage.size(); ++i) {
                sb.append("<br>").append(i + 1).append(". ").append(inpackage.get(i));
            }
        }
        if (sb.length() > 0) {
            throw new SapphireException(sb.toString());
        }
    }
}


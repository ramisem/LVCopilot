/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetSDIAttachments
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 58673 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block17: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            try {
                String sdcid = ajaxResponse.getRequestParameter("sdcid");
                String keyid1 = ajaxResponse.getRequestParameter("keyid1");
                String keyid2 = ajaxResponse.getRequestParameter("keyid2");
                String keyid3 = ajaxResponse.getRequestParameter("keyid3");
                boolean showds = ajaxResponse.getRequestParameter("showds").equalsIgnoreCase("Y");
                this.logger.debug("Starting GetSDIAttachments...");
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select sdcid, keyid1, keyid2, keyid3, attachmentnum, attachmentdesc");
                sql.append(" from sdiattachment");
                if (showds) {
                    sql.append(" where (sdcid = ").append(safeSQL.addVar(sdcid)).append(" ");
                    sql.append(" and keyid1 = ").append(safeSQL.addVar(keyid1)).append(" ");
                    if (StringUtil.getLen(keyid2) > 0L) {
                        sql.append(" and keyid2 = ").append(safeSQL.addVar(keyid2)).append(" ");
                    }
                    if (StringUtil.getLen(keyid3) > 0L) {
                        sql.append(" and keyid3 = ").append(safeSQL.addVar(keyid3)).append(" ");
                    }
                    sql.append(") ");
                    sql.append("or (sdcid = 'DataSet' ");
                    sql.append("and keyid1 IN (select distinct sdidataid from sdidata where ");
                    sql.append("sdcid=").append(safeSQL.addVar(sdcid)).append(" ");
                    sql.append("and keyid1=").append(safeSQL.addVar(keyid1)).append(" ");
                    if (StringUtil.getLen(keyid2) > 0L) {
                        sql.append(" and keyid2 = ").append(safeSQL.addVar(keyid2)).append(" ");
                    }
                    if (StringUtil.getLen(keyid3) > 0L) {
                        sql.append(" and keyid3 = ").append(safeSQL.addVar(keyid3)).append(" ");
                    }
                    sql.append(")) ");
                    sql.append("or (sdcid = 'DataItem' ");
                    sql.append("and keyid1 IN (select distinct sdidataitemid from sdidataitem where ");
                    sql.append("sdcid=").append(safeSQL.addVar(sdcid)).append(" ");
                    sql.append("and keyid1=").append(safeSQL.addVar(keyid1)).append(" ");
                    if (StringUtil.getLen(keyid2) > 0L) {
                        sql.append(" and keyid2 = ").append(safeSQL.addVar(keyid2)).append(" ");
                    }
                    if (StringUtil.getLen(keyid3) > 0L) {
                        sql.append(" and keyid3 = ").append(safeSQL.addVar(keyid3)).append(" ");
                    }
                    sql.append(")) ");
                } else {
                    sql.append(" where sdcid = ").append(safeSQL.addVar(sdcid)).append(" ");
                    sql.append(" and keyid1 = ").append(safeSQL.addVar(keyid1)).append(" ");
                    if (StringUtil.getLen(keyid2) > 0L) {
                        sql.append(" and keyid2 = ").append(safeSQL.addVar(keyid2)).append(" ");
                    }
                    if (StringUtil.getLen(keyid3) > 0L) {
                        sql.append(" and keyid3 = ").append(safeSQL.addVar(keyid3)).append(" ");
                    }
                }
                this.logger.debug("About to run SQL....");
                try {
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null) {
                        for (int i = 0; i < ds.getRowCount(); ++i) {
                            String attDesc = ds.getString(i, "attachmentdesc", "");
                            if (attDesc.length() != 0) continue;
                            ds.setValue(i, "attachmentdesc", this.getTranslationProcessor().translate("No Description"));
                        }
                        ajaxResponse.addCallbackArgument("count", ds.size());
                        ajaxResponse.addCallbackArgument("attachmentds", ds);
                        this.logger.debug("Found: " + ds.getRowCount() + " attachments.");
                        break block17;
                    }
                    this.logger.debug("No Attachments found");
                    ajaxResponse.addCallbackArgument("count", 0);
                }
                catch (Exception e) {
                    ajaxResponse.setError("Failed to execute attachment SQL.");
                }
            }
            finally {
                ajaxResponse.print();
                this.logger.debug("GetSDIAttachments Finished.");
            }
        }
    }
}


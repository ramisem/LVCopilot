/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.elements.coc;

import com.labvantage.opal.elements.coc.SDICOC;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.util.CocUtil;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class COCAjaxCheck
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53394 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "COCHandler");
        String itemselected = ajaxResponse.getRequestParameter("cocitemsselected");
        String sdcid = ajaxResponse.getRequestParameter("cocsdcid");
        if (itemselected == null || itemselected.length() == 0 || sdcid == null || sdcid.length() == 0) {
            ajaxResponse.setError(this.getTranslationProcessor().translate("Could not get selected items and/or SDC Id."));
        } else {
            String[] rows;
            String currentuser = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            String callback = ajaxResponse.getRequestParameter("coccallback");
            String cocform = ajaxResponse.getRequestParameter("cocform");
            String cocpage = ajaxResponse.getRequestParameter("cocpage");
            PropertyList data = new PropertyList();
            for (String keyid1 : rows = itemselected.split("%3B")) {
                String _keyid1;
                int i;
                ArrayList<String> list;
                DataSet ds;
                StringBuilder sql;
                data.setProperty("keyid1", keyid1);
                boolean allok = true;
                SafeSQL safeSQL = new SafeSQL();
                if (cocpage.toLowerCase().indexOf("cocaction=from") != -1) {
                    safeSQL.reset();
                    sql = new StringBuilder();
                    sql.append("select keyid1, fromcustodianid, tocustodianid");
                    sql.append(" from s_sdicoc");
                    sql.append(" where keyid1 in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
                    sql.append(" order by createdt desc");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null && ds.size() > 0) {
                        list = new ArrayList<String>();
                        for (i = 0; i < ds.size(); ++i) {
                            _keyid1 = ds.getString(i, "keyid1");
                            if (list.contains(_keyid1)) continue;
                            list.add(_keyid1);
                            if (!currentuser.equals(ds.getString(i, "tocustodianid"))) continue;
                            allok = false;
                            ajaxResponse.setError(this.getTranslationProcessor().translate("You are already Custodian of one or more selected item(s)"));
                            break;
                        }
                    }
                } else if (cocpage.toLowerCase().indexOf("cocaction=to") != -1) {
                    safeSQL.reset();
                    sql = new StringBuilder();
                    sql.append("select keyid1, fromcustodianid, tocustodianid");
                    sql.append(" from s_sdicoc");
                    sql.append(" where keyid1 in ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
                    sql.append(" order by createdt desc");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null && ds.size() > 0) {
                        list = new ArrayList();
                        for (i = 0; i < ds.size(); ++i) {
                            _keyid1 = ds.getString(i, "keyid1");
                            if (list.contains(_keyid1)) continue;
                            list.add(_keyid1);
                            if (!currentuser.equals(ds.getString(i, "fromcustodianid"))) continue;
                            allok = false;
                            ajaxResponse.setError(this.getTranslationProcessor().translate("You are not the Custodian of one or more selected item(s)"));
                            break;
                        }
                    }
                }
                if (!allok) continue;
                data.setProperty("sdcid", sdcid);
                data.setProperty("currentuser", currentuser);
                data.setProperty("manual", cocpage.toLowerCase().indexOf("manual=y") == -1 ? "N" : "Y");
                Object obj = CocUtil.getSDICOC(data, this.getQueryProcessor(), this.getSDCProcessor(), SQLFactory.getSqlGenerator(SQLFactory.getDBMS(this.getConnectionId()).equals("ORA")));
                if (obj instanceof String) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate(obj.toString()));
                    continue;
                }
                SDICOC sdiCOC = (SDICOC)obj;
                if (sdiCOC.size() > 0) {
                    ajaxResponse.addCallbackArgument("cocrequired", "Y");
                } else {
                    ajaxResponse.addCallbackArgument("cocrequired", "N");
                }
                ajaxResponse.addCallbackArgument("cocitemselected", itemselected);
                ajaxResponse.addCallbackArgument("cocsdcid", sdcid);
                ajaxResponse.addCallbackArgument("cocpage", cocpage);
                ajaxResponse.addCallbackArgument("cocform", cocform);
                ajaxResponse.addCallbackArgument("coccallback", callback);
            }
        }
        ajaxResponse.print();
    }
}


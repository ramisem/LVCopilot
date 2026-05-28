/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DashboardAjaxSave
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "DashboardHandler");
        JSONObject job = ajaxResponse.getCallProperties();
        if (job != null) {
            try {
                PropertyList pl = new PropertyList(job);
                WebAdminProcessor wap = new WebAdminProcessor(this.getConnectionId());
                String pagename = pl.getProperty("page");
                String productedition = wap.getDefaultPageEdition(pagename);
                QueryProcessor qp = new QueryProcessor(this.getConnectionId());
                DataSet existing = qp.getPreparedSqlDataSet("SELECT propertytreeid, elementid, valuetree FROM webpageuseroverride  WHERE webpageid = ? AND productedition = ? AND sysuserid = ?", (Object[])new String[]{pagename, productedition, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId()}, true);
                Iterator it = pl.keySet().iterator();
                while (it.hasNext()) {
                    String next = it.next().toString();
                    Object prop = pl.get(next);
                    if (!(prop instanceof PropertyList)) continue;
                    PropertyList element = (PropertyList)prop;
                    String propertytreeid = element.getProperty("propertytreeid", "");
                    element.deleteProperty("propertytreeid");
                    if (existing != null && existing.getRowCount() > 0) {
                        HashMap<String, String> find = new HashMap<String, String>(2);
                        find.put("propertytreeid", propertytreeid);
                        find.put("elementid", next);
                        int found = existing.findRow(find);
                        if (found > -1) {
                            PropertyList exist = new PropertyList();
                            exist.setPropertyList(existing.getClob(found, "valuetree"));
                            exist.setPropertyList(element.toXMLString(), true);
                            wap.saveUserOverrides(pagename, productedition, propertytreeid, next, exist);
                            continue;
                        }
                        wap.saveUserOverrides(pagename, productedition, propertytreeid, next, element);
                        continue;
                    }
                    wap.saveUserOverrides(pagename, productedition, propertytreeid, next, element);
                }
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not update user overrides."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.storageunit.StorageUnitRenderer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GetStorageUnitDisplay
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        String html = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String scannedvalue = ajaxResponse.getRequestParameter("scannedvalue", "").trim();
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "").trim();
        String defaultwhereclause = "storageunit.storageunitid = ?";
        defaultwhereclause = OpalUtil.isNotEmpty(sdcid) ? defaultwhereclause + " or (storageunit.linksdcid = '" + SafeSQL.encodeForSQL(sdcid, this.getConnectionProcessor().isOra()) + "' and storageunit.linkkeyid1 = ?)" : defaultwhereclause + " or storageunit.linkkeyid1 = ?";
        String scannedunits = ajaxResponse.getRequestParameter("scannedunits");
        String elementid = "";
        String storageunitid = "";
        String linksdcid = "";
        String linkkeyid1 = "";
        String trackitemid = "";
        String rsetid = "";
        String hasNoLayout = "N";
        if (OpalUtil.isNotEmpty(scannedvalue)) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunit.storageunitid, storageunit.linksdcid, storageunit.linkkeyid1, storageunit.propertytreeid, trackitem.trackitemid, trackitem.custodialdepartmentid, trackitem.custodialuserid from storageunit left outer join trackitem on trackitem.linksdcid = storageunit.linksdcid and trackitem.linkkeyid1 = storageunit.linkkeyid1 where " + defaultwhereclause, (Object[])new String[]{scannedvalue, scannedvalue});
            if (ds != null && ds.size() > 0) {
                String[] s;
                storageunitid = ds.getString(0, "storageunitid");
                linksdcid = ds.getString(0, "linksdcid");
                linkkeyid1 = ds.getString(0, "linkkeyid1");
                trackitemid = ds.getString(0, "trackitemid");
                String custodialdepartmentid = ds.getString(0, "custodialdepartmentid");
                String custodialuserid = ds.getString(0, "custodialuserid");
                String string = hasNoLayout = "No Layout".equals(ds.getString(0, "propertytreeid")) ? "Y" : "N";
                if (!"LV_Box".equals(linksdcid) && !"LV_Plate".equals(linksdcid)) {
                    message = this.getTranslationProcessor().translate("Scanned storage unit is not a Box");
                }
                for (String s1 : s = StringUtil.split(scannedunits, ";")) {
                    if (!storageunitid.equals(s1)) continue;
                    message = this.getTranslationProcessor().translate("This storage unit has been already scanned");
                }
                if (message.length() == 0) {
                    if (OpalUtil.isNotEmpty(custodialuserid)) {
                        if (!this.getConnectionProcessor().getSapphireConnection().getSysuserId().equals(custodialuserid)) {
                            message = this.getTranslationProcessor().translate("This item is not in your Custody") + " (" + this.getSDCProcessor().getProperty(linksdcid, "singular") + " " + linkkeyid1 + ")";
                        }
                    } else if (OpalUtil.isNotEmpty(custodialdepartmentid) && !this.getConnectionProcessor().getSapphireConnection().getDepartmentList().contains(custodialdepartmentid)) {
                        message = this.getTranslationProcessor().translate("This item is not in your Custodial Department") + " (" + this.getSDCProcessor().getProperty(linksdcid, "singular") + " " + linkkeyid1 + ")";
                    }
                }
                if (message.length() == 0) {
                    try {
                        PropertyList element = new PropertyList();
                        element.setJSONString(ajaxResponse.getRequestParameter("element"));
                        element.setProperty("property_storageunitid", storageunitid);
                        String displayHTML = StorageUnitRenderer.getDisplayHTML(element, this.getQueryProcessor(), this.getTranslationProcessor(), this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        html = "<div class='boxholder' storageunitid='" + storageunitid + "' linksdcid='" + linksdcid + "' linkkeyid1='" + linkkeyid1 + "' trackitemid='" + trackitemid + "'>" + displayHTML + "</div>";
                        elementid = element.getId();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                message = this.getTranslationProcessor().translate("No storage unit found for scanned value");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("html", html);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("elementid", elementid);
            jsonObject.put("containerid", ajaxResponse.getRequestParameter("containerid"));
            jsonObject.put("storageunitid", storageunitid);
            jsonObject.put("linksdcid", linksdcid);
            jsonObject.put("linkkeyid1", linkkeyid1);
            jsonObject.put("trackitemid", trackitemid);
            jsonObject.put("rsetid", rsetid);
            jsonObject.put("hasnolayout", hasNoLayout);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("data", jsonObject.toString());
        ajaxResponse.print();
    }
}


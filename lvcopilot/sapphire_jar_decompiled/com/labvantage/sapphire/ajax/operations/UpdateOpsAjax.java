/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.modules.datafile.ValidationEditorUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class UpdateOpsAjax
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "updateOpsHtml_Callback");
        try {
            String elementid = ajaxResponse.getRequestParameter("elementid");
            String fieldselected = ajaxResponse.getRequestParameter("fieldselected");
            String otherfield = ajaxResponse.getRequestParameter("otherfield");
            String fieldsCollJSON = ajaxResponse.getRequestParameter("fieldlist");
            PropertyListCollection fields = new PropertyListCollection();
            fields.setJSONString(fieldsCollJSON);
            int pos = fieldselected.indexOf(".value");
            if (pos > 0) {
                String fieldid = fieldselected.substring(1, pos);
                PropertyList match = fields.find("fieldid", fieldid);
                if (match != null) {
                    String otherdatatype;
                    String ofieldid;
                    PropertyList omatch;
                    String datatype = match.getProperty("datatype");
                    if (otherfield.length() > 0 && (omatch = fields.find("fieldid", ofieldid = otherfield.substring(1, otherfield.indexOf(".value")))) != null && !(otherdatatype = omatch.getProperty("datatype")).equals(datatype)) {
                        ajaxResponse.setError("Datatypes of the two fields are not the same.");
                        ajaxResponse.setCallProperties(elementid);
                        ajaxResponse.setErrorCallback("updateOpsErrorCallback");
                        ajaxResponse.print();
                        return;
                    }
                    String ddelementid = "";
                    ddelementid = elementid.contains("lhs") ? elementid.replace("dropdownlhs", "conditionop") : elementid.replace("dropdownrhs", "conditionop");
                    String dropdownhtml = ValidationEditorUtil.getComparisonOperatorDropdown(this.getTranslationProcessor(), "", datatype, ddelementid, "");
                    ajaxResponse.addCallbackArgument("ddelementid", ddelementid);
                    ajaxResponse.addCallbackArgument("dropdownhtml", dropdownhtml);
                }
            } else if (fieldselected.indexOf(".length") > 0) {
                String ddelementid = "";
                ddelementid = elementid.contains("lhs") ? elementid.replace("dropdownlhs", "conditionop") : elementid.replace("dropdownrhs", "conditionop");
                String dropdownhtml = ValidationEditorUtil.getComparisonOperatorDropdown(this.getTranslationProcessor(), "fieldlength", "number", ddelementid, "");
                ajaxResponse.addCallbackArgument("ddelementid", ddelementid);
                ajaxResponse.addCallbackArgument("dropdownhtml", dropdownhtml);
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        ajaxResponse.print();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.spreadsheet;

import com.labvantage.sapphire.pageelements.spreadsheet.SpreadsheetParser;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class SpreadsheetParserPropertiesAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "SSParserHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            int style;
            try {
                style = Integer.parseInt(ajaxResponse.getRequestParameter("style", "0"));
            }
            catch (NumberFormatException e3) {
                style = -1;
                ajaxResponse.setError(this.getTranslationProcessor().translate("Incorrect style provided."));
            }
            if (style > -1) {
                try {
                    PropertyList useprop = new PropertyList(new JSONObject(props));
                    String fieldid = ajaxResponse.getRequestParameter("fieldid", "");
                    try {
                        String propsHtml = SpreadsheetParser.getPropertiesHtml(fieldid, useprop, style, ajaxResponse.getRequestParameter("viewonly", "n").equalsIgnoreCase("y"), this.getConnectionId(), this.getTranslationProcessor());
                        ajaxResponse.addCallbackArgument("html", propsHtml);
                    }
                    catch (Exception e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain properties HTML."));
                    }
                }
                catch (Exception e2) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain propertylist from string provided."));
                }
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}


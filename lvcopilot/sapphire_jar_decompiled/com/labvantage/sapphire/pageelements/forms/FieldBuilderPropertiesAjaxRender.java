/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.forms;

import com.labvantage.sapphire.pageelements.forms.FieldBuilder;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class FieldBuilderPropertiesAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "FieldBuilderHandler");
        String props = ajaxResponse.getRequestParameter("formproperties", "");
        if (props.length() > 0) {
            try {
                PropertyList formprop = new PropertyList(new JSONObject(props));
                try {
                    PropertyList userConf = RequestContext.getInstance(request).getPropertyList("userconfig");
                    if (userConf != null && !ajaxResponse.getRequestParameter("propertyGrouping", "").equalsIgnoreCase(userConf.getProperty("propertybuilder_groupby", ""))) {
                        userConf.setProperty("propertybuilder_groupby", ajaxResponse.getRequestParameter("propertyGrouping", "cat"));
                    }
                    String propsHtml = FieldBuilder.getPropertiesHtml(formprop, ajaxResponse.getRequestParameter("viewonly", "n").equalsIgnoreCase("y"), this.getConnectionId(), RequestContext.getInstance(request).getPropertyList("userconfig"), this.getTranslationProcessor(), request.getSession(), ProcessingUtil.createBindingsMap(null, this.getQueryProcessor(), this.getSDCProcessor(), null, null));
                    ajaxResponse.addCallbackArgument("html", propsHtml);
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain properties HTML."));
                }
            }
            catch (Exception e2) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain propertylist from string provided."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}


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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetDropDownOptions
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext) throws ServletException {
        PropertyList columnProps;
        AjaxResponse ar = new AjaxResponse(httpServletRequest, httpServletResponse);
        String columnJsonString = ar.getRequestParameter("column");
        try {
            columnProps = new PropertyList(new JSONObject(columnJsonString));
        }
        catch (JSONException ex) {
            throw new IllegalArgumentException("Column JSON is not valid: " + columnJsonString, ex);
        }
        PropertyListCollection optionCollection = new PropertyListCollection();
        PropertyList emptyOption = new PropertyList();
        emptyOption.setProperty("refvalueid", "");
        emptyOption.setProperty("refdisplayvalue", "");
        optionCollection.add(emptyOption);
        String refTypeId = columnProps.getProperty("reftypeid");
        if (!refTypeId.isEmpty()) {
            DataSet refTypeDs = this.getQueryProcessor().getRefTypeDataSet(refTypeId);
            for (int i = 0; i < refTypeDs.getRowCount(); ++i) {
                PropertyList optionProps = new PropertyList();
                optionProps.setProperty("refvalueid", refTypeDs.getString(i, "refvalueid"));
                optionProps.setProperty("refdisplayvalue", this.getTranslationProcessor().translate(refTypeDs.getString(i, "refdisplayvalue", optionProps.getProperty("refvalueid"))));
                optionCollection.add(optionProps);
            }
        }
        ar.addCallbackArgument("options", optionCollection.toJSONString());
        ar.addCallbackArgument("column", columnProps);
        ar.print();
    }
}


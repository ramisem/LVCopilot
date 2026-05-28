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

import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FormBuilderPropertiesAjaxMerger
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 57024 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "FormBuilderHandler");
        String props = ajaxResponse.getRequestParameter("inputproperties", "");
        String fieldid = ajaxResponse.getRequestParameter("fieldid", "");
        if (fieldid.length() > 0) {
            DataSet fielddata;
            this.logger.debug("fieldid = " + fieldid);
            PropertyList oldprop = null;
            try {
                oldprop = props.length() > 0 ? new PropertyList(new JSONObject(props)) : new PropertyList();
            }
            catch (Exception e2) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain propertylist from string provided."));
            }
            if (oldprop != null && (fielddata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT fieldobject, fieldtype, editableflag, fieldlabel, defaulteditor FROM field WHERE fieldid = ?", new Object[]{fieldid}, true)) != null && fielddata.size() > 0) {
                String dataprops;
                oldprop.setProperty("datatype", fielddata.getValue(0, "fieldtype", ""));
                oldprop.setProperty("controlled", "Y");
                oldprop.setProperty("type", fielddata.getValue(0, "defaulteditor", "text"));
                if (fielddata.getValue(0, "editableflag", "N").equalsIgnoreCase("N")) {
                    oldprop.setProperty("locked", "Y");
                }
                if ((dataprops = fielddata.getClob(0, "fieldobject", "")).length() > 0) {
                    PropertyList newprops = new PropertyList();
                    try {
                        newprops.setPropertyList(dataprops);
                    }
                    catch (Exception e) {
                        this.logger.debug("Could not create propertylist from property data.");
                    }
                    if (newprops != null && newprops.size() > 0) {
                        Iterator it = newprops.keySet().iterator();
                        while (it.hasNext()) {
                            String key = it.next().toString();
                            Object value = newprops.get(key);
                            if (key.equalsIgnoreCase("fieldid") || key.equalsIgnoreCase("datatype") || key.equalsIgnoreCase("locked") || key.equalsIgnoreCase("controlled") || key.equalsIgnoreCase("type") || value == null) continue;
                            if (value instanceof PropertyListCollection) {
                                oldprop.setProperty(key, (PropertyListCollection)value);
                                continue;
                            }
                            if (value instanceof PropertyList) {
                                oldprop.setProperty(key, (PropertyList)value);
                                continue;
                            }
                            if (!(value instanceof String)) continue;
                            oldprop.setProperty(key, value.toString());
                        }
                    }
                }
                if (fielddata.getValue(0, "fieldlabel", "").length() > 0) {
                    if (!oldprop.containsKey("labels")) {
                        oldprop.setProperty("labels", new PropertyListCollection());
                    }
                    PropertyList label = new PropertyList();
                    label.setProperty("labelid", fieldid + "_label" + (oldprop.getCollection("labels").size() + 1));
                    label.setProperty("text", fielddata.getValue(0, "fieldlabel", ""));
                    oldprop.getCollection("labels").add(label);
                }
                props = oldprop.toJSONString();
                ajaxResponse.addCallbackArgument("fieldid", fieldid);
                ajaxResponse.addCallbackArgument("outputproperties", props);
                ajaxResponse.addCallbackArgument("refresh", ajaxResponse.getRequestParameter("refresh", "n").equalsIgnoreCase("y"));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No field Id provided."));
        }
        ajaxResponse.print();
    }
}


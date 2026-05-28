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

import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.tagext.SDITagUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FormBuilderSaveAjaxConverter
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 85686 $";

    private void processField(PropertyList field, TranslationProcessor tp) {
        String fieldid = field.getProperty("fieldid", "");
        String editorstyleid = field.getProperty("editorstyleid");
        if (editorstyleid.contains("|")) {
            field.setProperty("editorstyleid", editorstyleid.substring(0, editorstyleid.indexOf("|")));
        }
        if (fieldid.length() > 0) {
            String type = field.getProperty("type", "");
            if (type.length() > 0 && type.equalsIgnoreCase("lookup")) {
                PropertyList pagedir;
                String sdcid = field.getProperty("sdcid");
                String selector = field.getProperty("selectortype");
                StringBuffer mapcolidlist = new StringBuffer();
                StringBuffer fieldidlist = new StringBuffer();
                PropertyListCollection temp = new PropertyListCollection();
                PropertyListCollection cols = field.getCollection("lookupcolumns");
                if (cols != null) {
                    for (int i = 0; i < cols.size(); ++i) {
                        temp.add(cols.getPropertyList(i).clone());
                    }
                }
                if ((pagedir = SDITagUtil.getLookupPageDirectives(sdcid, fieldid, "", false, selector, true, "", "", "", "", "", temp, false, mapcolidlist, fieldidlist, tp, this.getSDCProcessor())) != null) {
                    field.setProperty("lookupoptions", pagedir.toJSONString());
                }
                field.setProperty("lookupreturncolumns", mapcolidlist.toString());
                field.setProperty("lookupreturnfields", fieldidlist.toString());
            } else {
                if (field.containsKey("lookupoptions")) {
                    field.remove("lookupoptions");
                }
                if (field.containsKey("lookupreturncolumns")) {
                    field.remove("lookupreturncolumns");
                }
                if (field.containsKey("lookupreturnfields")) {
                    field.remove("lookupreturnfields");
                }
            }
        }
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block13: {
            ajaxResponse = new AjaxResponse(request, response, "FormBuilderHandler");
            String props = ajaxResponse.getRequestParameter("formproperties", "");
            if (props.length() > 0) {
                String layout = ajaxResponse.getRequestParameter("formlayout", "");
                if (layout.length() > 0) {
                    boolean xmlMode = false;
                    String txmlMode = ajaxResponse.getRequestParameter("xmlmode", "false");
                    if (txmlMode.equalsIgnoreCase("true") || txmlMode.equalsIgnoreCase("Y")) {
                        xmlMode = true;
                    }
                    boolean formObjectMode = false;
                    String tfoMode = ajaxResponse.getRequestParameter("formobject", "false");
                    if (tfoMode.equalsIgnoreCase("true") || tfoMode.equalsIgnoreCase("Y")) {
                        formObjectMode = true;
                    }
                    try {
                        PropertyListCollection fields;
                        PropertyList formprop = new PropertyList(new JSONObject(props));
                        if (formprop.containsKey("fields") && (fields = formprop.getCollection("fields")) != null && fields.size() > 0) {
                            TranslationProcessor tp = this.getTranslationProcessor();
                            for (int i = 0; i < fields.size(); ++i) {
                                PropertyList field = fields.getPropertyList(i);
                                this.processField(field, tp);
                            }
                        }
                        StringBuffer xhtml = new StringBuffer();
                        try {
                            xhtml.append(layout);
                        }
                        catch (Exception e3) {
                            ajaxResponse.setError(this.getTranslationProcessor().translate("Could not convert layout to XML."));
                        }
                        String sdcid = ajaxResponse.getRequestParameter("sdcid");
                        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
                        String keyid2 = ajaxResponse.getRequestParameter("keyid2");
                        HTMLEditorControl.processImages(xhtml, true, this.getConnectionId());
                        if (formObjectMode) {
                            ajaxResponse.addCallbackArgument("formlayout", "");
                            ajaxResponse.addCallbackArgument("formproperties", "");
                            PropertyList fo = new PropertyList();
                            fo.setProperty("formlayout", xhtml.toString());
                            fo.setProperty("formproperties", formprop);
                            String sfo = xmlMode ? fo.toXMLString() : fo.toJSONString();
                            ajaxResponse.addCallbackArgument("formobject", sfo);
                            break block13;
                        }
                        props = xmlMode ? formprop.toXMLString() : formprop.toJSONString();
                        ajaxResponse.addCallbackArgument("formlayout", xhtml);
                        ajaxResponse.addCallbackArgument("formproperties", props);
                        ajaxResponse.addCallbackArgument("formobject", "");
                    }
                    catch (Exception e2) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain propertylist from string provided."));
                    }
                } else {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("No layout string provided."));
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
            }
        }
        ajaxResponse.print();
    }
}


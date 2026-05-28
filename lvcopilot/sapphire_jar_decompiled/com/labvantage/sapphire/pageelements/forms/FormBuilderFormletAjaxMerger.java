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

import java.util.HashMap;
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

public class FormBuilderFormletAjaxMerger
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53852 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "FormBuilderHandler");
        String props = ajaxResponse.getRequestParameter("formproperties", "");
        if (props.length() > 0) {
            String formletid = ajaxResponse.getRequestParameter("formletid", "");
            if (formletid.length() > 0) {
                String formletversion = ajaxResponse.getRequestParameter("formletversion", "1");
                boolean byreference = ajaxResponse.getRequestParameter("byreference", "N").equalsIgnoreCase("Y");
                boolean refresh = ajaxResponse.getRequestParameter("refresh", "N").equalsIgnoreCase("Y");
                ajaxResponse.addCallbackArgument("formletid", formletid);
                this.logger.debug("formletid = " + formletid);
                PropertyList formProps = null;
                try {
                    formProps = new PropertyList(new JSONObject(props));
                }
                catch (Exception e2) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain propertylist from string provided."));
                }
                if (formProps != null) {
                    DataSet formletdata;
                    if (formletversion.equalsIgnoreCase("C")) {
                        formletdata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT formletobjects, formletlayout, formletid, formletversionid, versionstatus FROM formlet WHERE formletid = ? AND ( versionstatus = 'C' OR versionstatus = 'P' )", new Object[]{formletid}, true);
                        if (formletdata.size() > 1) {
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put("versionstatus", "C");
                            DataSet filtered = formletdata.getFilteredDataSet(filter);
                            if (filtered.size() > 0) {
                                formletdata = filtered;
                            } else {
                                formletdata.sort("formletversionid d");
                            }
                        }
                    } else {
                        formletdata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT formletobjects, formletlayout, formletid, formletversionid, versionstatus FROM formlet WHERE formletid = ? AND formletversionid = ?", new Object[]{formletid, formletversion}, true);
                    }
                    if (formletdata != null && formletdata.size() > 0) {
                        String formletlayout = formletdata.getClob(0, "formletlayout", "");
                        String formletproperties = formletdata.getClob(0, "formletobjects", "");
                        if (formletproperties.length() > 0) {
                            PropertyList formletProps = new PropertyList();
                            try {
                                formletProps.setPropertyList(formletproperties);
                            }
                            catch (Exception e) {
                                formletProps = null;
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not use formlet properties."));
                            }
                            if (formletProps != null) {
                                String error = "";
                                error = this.addPropertyLists(formletProps, formProps, "fields", "fieldid", "Field", formletid, byreference);
                                if (!(error.length() != 0 && !refresh || (error = this.addPropertyLists(formletProps, formProps, "groups", "groupid", "Group", formletid, byreference)).length() != 0 && !refresh || (error = this.addPropertyLists(formletProps, formProps, "sections", "sectionid", "Section", formletid, byreference)).length() != 0 && !refresh)) {
                                    error = this.addPropertyLists(formletProps, formProps, "elements", "elementid", "Element", formletid, byreference);
                                }
                                if (error.length() > 0 && !refresh) {
                                    ajaxResponse.setError(this.getTranslationProcessor().translate(error));
                                }
                            }
                        }
                        ajaxResponse.addCallbackArgument("mergedproperties", formProps.toJSONString());
                        ajaxResponse.addCallbackArgument("formletlayout", formletlayout);
                        ajaxResponse.addCallbackArgument("byreference", byreference);
                        ajaxResponse.addCallbackArgument("refresh", refresh);
                        ajaxResponse.addCallbackArgument("update", !ajaxResponse.getRequestParameter("update", "Y").equalsIgnoreCase("N"));
                        ajaxResponse.addCallbackArgument("formletversion", formletversion);
                        ajaxResponse.addCallbackArgument("edit", !ajaxResponse.getRequestParameter("edit", "Y").equalsIgnoreCase("N"));
                        ajaxResponse.addCallbackArgument("sectionid", ajaxResponse.getRequestParameter("sectionid", "formlet_" + formletid));
                    } else {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("No formlet data available for formlet") + " " + formletid + " (" + formletversion + ").");
                    }
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No formlet Id provided."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No Property List string provided."));
        }
        ajaxResponse.print();
    }

    private String addPropertyLists(PropertyList formletProps, PropertyList formProps, String sCollectionName, String idProp, String sCollectionText, String formletid, boolean locked) {
        String error = "";
        PropertyListCollection formletCollection = formletProps.getCollection(sCollectionName);
        if (formletCollection != null && formletCollection.size() > 0) {
            PropertyListCollection formCollection = formProps.getCollection(sCollectionName);
            if (formCollection == null || formCollection.size() == 0) {
                formProps.setProperty(sCollectionName, formletCollection);
            } else {
                for (int i = 0; i < formletCollection.size(); ++i) {
                    PropertyList item = formletCollection.getPropertyList(i);
                    String itemid = item.getProperty(idProp, "");
                    if (formCollection.find(idProp, itemid) == null) {
                        if (locked) {
                            if (sCollectionName.equalsIgnoreCase("sections")) {
                                item.setProperty("formletlocked", "Y");
                            } else {
                                item.setProperty("locked", "Y");
                            }
                        }
                    } else {
                        error = sCollectionText + " " + itemid + " found in formlet " + formletid + " already exists in form.";
                        break;
                    }
                    formCollection.add(item);
                }
            }
        }
        return error;
    }
}


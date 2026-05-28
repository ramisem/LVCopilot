/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.pageelements.forms.FormBuilderGroovyAjaxRender;
import com.labvantage.sapphire.util.groovy.DBRead;
import com.labvantage.sapphire.util.groovy.GroovyLogger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class TaskDefGroovyAjaxRender
extends FormBuilderGroovyAjaxRender {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                PropertyList properties = new PropertyList(new JSONObject(props));
                String propertypath = ajaxResponse.getRequestParameter("objectid");
                String propertyid = ajaxResponse.getRequestParameter("propertyid");
                if (propertyid.length() > 0) {
                    String searchstring = ajaxResponse.getRequestParameter("searchstring");
                    String partstring = ajaxResponse.getRequestParameter("partstring");
                    boolean extendedObjects = true;
                    JSONArray ret = new JSONArray();
                    if (searchstring.length() == 0) {
                        if (properties.containsKey("steps")) {
                            // empty if block
                        }
                        this.addStandardObjects(extendedObjects, false, ret);
                    } else if (!searchstring.equals("steps")) {
                        if (searchstring.equals("user")) {
                            this.addUserObjects(ret);
                        } else if (searchstring.equals("database")) {
                            this.addPublicMethods(DBRead.class, ret);
                        } else if (searchstring.equals("logger")) {
                            this.addPublicMethods(GroovyLogger.class, ret);
                        } else if (searchstring.equals("m18n")) {
                            this.addPublicMethods(M18NUtil.class, ret);
                        } else if (searchstring.equals("actionProcessor")) {
                            this.addPublicMethods(ActionProcessor.class, ret);
                        } else if (searchstring.equals("queryProcessor")) {
                            this.addPublicMethods(QueryProcessor.class, ret);
                        } else if (searchstring.equals("sdcProcessor")) {
                            this.addPublicMethods(SDCProcessor.class, ret);
                        }
                    }
                    ajaxResponse.addCallbackArgument("objects", ret);
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


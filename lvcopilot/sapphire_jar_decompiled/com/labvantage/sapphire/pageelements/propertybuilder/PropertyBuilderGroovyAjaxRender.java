/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.propertybuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class PropertyBuilderGroovyAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54565 $";

    private JSONObject getAttributes(String name, String type, String help) {
        JSONObject temp = new JSONObject();
        try {
            temp.put("__name", name);
            temp.put("type", type);
            temp.put("help", help);
            return temp;
        }
        catch (Exception e) {
            return null;
        }
    }

    private JSONObject getAttributes(String name, String message) {
        JSONObject temp = new JSONObject();
        try {
            temp.put("__name", name);
            temp.put("type", "message");
            temp.put("help", message);
            return temp;
        }
        catch (Exception e) {
            return null;
        }
    }

    private void addObject(String name, JSONArray ret, String type, String help) {
        try {
            ret.put(this.getAttributes(name, type, help));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void addObject(String message, JSONArray ret) {
        try {
            ret.put(this.getAttributes("message", message));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "PropertyBuilderHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                PropertyList prop = new PropertyList(new JSONObject(props));
                String propertyid = ajaxResponse.getRequestParameter("propertyid");
                String collectionindex = ajaxResponse.getRequestParameter("collectionindex");
                String collectionpropertyid = ajaxResponse.getRequestParameter("collectionpropertyid");
                String objecttype = ajaxResponse.getRequestParameter("objecttype");
                if (propertyid.length() > 0) {
                    String searchstring = ajaxResponse.getRequestParameter("searchstring");
                    String partstring = ajaxResponse.getRequestParameter("partstring");
                    boolean extendedObjects = true;
                    JSONArray ret = new JSONArray();
                    if (searchstring.length() == 0) {
                        this.addObject("user", ret, "Map", "User connection info");
                        this.addObject("logger", ret, "Object", "Message logger object");
                        this.addObject("m18n", ret, "Object", "Multinationalization object");
                        if (extendedObjects) {
                            this.addObject("database", ret, "Object", "Database read access object");
                            this.addObject("actionProcessor", ret, "Object", "ActionProcessor object");
                            this.addObject("queryProcessor", ret, "Object", "QueryProcessor object");
                            this.addObject("sdcProcessor", ret, "Object", "SDCProcessor object");
                            this.addObject("processAction( actionid, actionProps )", ret, "Method", "Process action method");
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

    private DataSet getSpecLimitTypes(HttpServletRequest request, ServletContext servletContext) {
        DataSet specLimitTypes = (DataSet)servletContext.getAttribute("distinctspeclimittypes");
        if (specLimitTypes == null) {
            QueryProcessor qp = new QueryProcessor(RequestContext.getRequestContext(request).getConnectionId());
            specLimitTypes = qp.getSqlDataSet("SELECT DISTINCT limittypeid FROM speclimittype ORDER BY 1");
            servletContext.setAttribute("distinctspeclimittypes", (Object)specLimitTypes);
        }
        return specLimitTypes;
    }

    private void addPublicMethods(Class c, JSONArray ret) throws JSONException {
        Method[] m = c.getDeclaredMethods();
        TreeMap<String, Method> methods = new TreeMap<String, Method>();
        for (int i = 0; i < m.length; ++i) {
            if (m[i].getModifiers() != 1) continue;
            methods.put(m[i].getName() + this.getParameterList(m[i]), m[i]);
        }
        for (String method : methods.keySet()) {
            this.addObject(method, ret, ((Method)methods.get(method)).getReturnType().getSimpleName(), ((Method)methods.get(method)).toString());
        }
    }

    private String getParameterList(Method m) {
        StringBuffer pl = new StringBuffer();
        Class<?>[] c = m.getParameterTypes();
        Type[] t = m.getGenericParameterTypes();
        boolean closure = false;
        for (int i = 0; i < c.length; ++i) {
            if (c[i].getSimpleName().equals("Closure")) {
                pl.append(" ) { it -> it }");
                closure = true;
                continue;
            }
            pl.append(", ").append(c[i].getSimpleName());
        }
        return pl.length() > 0 ? "( " + pl.substring(1) + (closure ? "" : " )") : "()";
    }
}


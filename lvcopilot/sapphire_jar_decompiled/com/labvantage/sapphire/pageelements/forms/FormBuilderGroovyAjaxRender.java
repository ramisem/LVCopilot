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

import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.FormGroup;
import com.labvantage.sapphire.util.groovy.DBRead;
import com.labvantage.sapphire.util.groovy.GroovyLogger;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FormBuilderGroovyAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 65525 $";

    protected JSONObject getAttributes(String name, String type, String help) {
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

    protected JSONObject getAttributes(String name, String message) {
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

    protected void addObject(String name, JSONArray ret, String type, String help) {
        try {
            ret.put(this.getAttributes(name, type, help));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    protected void addObject(String message, JSONArray ret) {
        try {
            ret.put(this.getAttributes("message", message));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "FormBuilderHandler");
        String props = ajaxResponse.getRequestParameter("formproperties", "");
        if (props.length() > 0) {
            try {
                PropertyList formprop = new PropertyList(new JSONObject(props));
                String objectid = ajaxResponse.getRequestParameter("objectid");
                String propertyid = ajaxResponse.getRequestParameter("propertyid");
                String collectionindex = ajaxResponse.getRequestParameter("collectionindex");
                String collectionpropertyid = ajaxResponse.getRequestParameter("collectionpropertyid");
                String objecttype = ajaxResponse.getRequestParameter("objecttype");
                if (objectid.length() > 0 && propertyid.length() > 0) {
                    String searchstring = ajaxResponse.getRequestParameter("searchstring");
                    String partstring = ajaxResponse.getRequestParameter("partstring");
                    boolean extendedObjects = true;
                    if (propertyid.startsWith("visible") || propertyid.startsWith("mandatory") || propertyid.startsWith("readonly") || propertyid.startsWith("processingfield")) {
                        extendedObjects = false;
                    }
                    if (propertyid.equals("sql") || propertyid.equals("values") || propertyid.equals("reftype") || propertyid.equals("sdcid")) {
                        extendedObjects = false;
                    }
                    if (propertyid.equals("binding.specexpression")) {
                        extendedObjects = false;
                    }
                    JSONArray ret = new JSONArray();
                    if (searchstring.length() == 0) {
                        if (formprop.containsKey("fields")) {
                            this.addObject("fields", ret, "Map", "Map of fields");
                            boolean fieldinstance = false;
                            PropertyListCollection sections = formprop.getCollection("sections");
                            if (sections != null) {
                                for (int i = 0; !fieldinstance && i < sections.size(); ++i) {
                                    PropertyList section = sections.getPropertyList(i);
                                    if (!section.getProperty("repeatable").equals("Y")) continue;
                                    PropertyListCollection fields = section.getCollection("fields");
                                    for (int j = 0; !fieldinstance && j < fields.size(); ++j) {
                                        if (!fields.getPropertyList(j).getProperty("fieldid").equals(objectid)) continue;
                                        fieldinstance = true;
                                    }
                                }
                                if (fieldinstance) {
                                    this.addObject("fieldinstance", ret, "Map", "Map of section fields");
                                }
                            }
                        }
                        if (objecttype.equals("group") && !propertyid.startsWith("validation")) {
                            this.addObject("group", ret, "Object", "Group object");
                            this.addObject("groupfields", ret, "Map", "Map of fields in group");
                            this.addObject("members", ret, "List", "List of fields in group");
                        }
                        this.addStandardObjects(extendedObjects, true, ret);
                    } else if (searchstring.equals("fields")) {
                        PropertyListCollection fields = formprop.getCollection("fields");
                        if (fields != null) {
                            TreeSet<String> sortedFields = new TreeSet<String>();
                            for (int i = 0; i < fields.size(); ++i) {
                                sortedFields.add(fields.getPropertyList(i).getProperty("fieldid"));
                            }
                            for (String id : sortedFields) {
                                this.addObject(id, ret, "Object", "'Field " + id);
                            }
                        }
                    } else if (searchstring.equals("fieldinstance")) {
                        boolean done = false;
                        PropertyListCollection sections = formprop.getCollection("sections");
                        for (int i = 0; !done && i < sections.size(); ++i) {
                            PropertyList section = sections.getPropertyList(i);
                            if (!section.getProperty("repeatable").equals("Y")) continue;
                            PropertyListCollection fields = section.getCollection("fields");
                            for (int j = 0; !done && j < fields.size(); ++j) {
                                if (!fields.getPropertyList(j).getProperty("fieldid").equals(objectid)) continue;
                                for (int k = 0; k < fields.size(); ++k) {
                                    this.addObject(fields.getPropertyList(k).getProperty("fieldid"), ret, "String", fields.getPropertyList(k).getProperty("fieldid"));
                                }
                                done = true;
                            }
                        }
                    } else if (searchstring.equals("groupfields")) {
                        PropertyListCollection groups = formprop.getCollection("groups");
                        if (groups != null) {
                            for (int i = 0; i < groups.size(); ++i) {
                                PropertyListCollection fields;
                                if (!groups.getPropertyList(i).getProperty("groupid").equals(objectid) || (fields = groups.getPropertyList(i).getCollection("members")) == null) continue;
                                for (int j = 0; j < fields.size(); ++j) {
                                    PropertyList field = fields.getPropertyList(j);
                                    String id = field.getProperty("fieldid");
                                    this.addObject(id, ret, "Object", "'Field " + id);
                                }
                            }
                        }
                    } else if (searchstring.startsWith("fields.") || searchstring.startsWith("fieldinstance.") || searchstring.startsWith("groupfields.")) {
                        if (searchstring.endsWith(".binding")) {
                            this.addObject("paramlistid", ret, "String", "ParamListId");
                            this.addObject("paramlistversionid", ret, "String", "ParamListVersionId");
                            this.addObject("variantid", ret, "String", "VariantId");
                            this.addObject("paramid", ret, "String", "ParamId");
                            this.addObject("paramtype", ret, "String", "ParamType");
                            this.addObject("replicateid", ret, "String", "ReplicateId");
                        } else {
                            String fieldid = searchstring.substring(7);
                            if (fieldid.indexOf(".") == -1) {
                                PropertyList field = formprop.getCollection("fields").find("fieldid", fieldid);
                                if (field != null && field.getProperty("bindingmode").length() > 0) {
                                    this.addObject("binding", ret, "Object", "Bindings");
                                }
                                this.addPublicMethods(Field.class, ret);
                            } else {
                                this.addObject("Refer to Java API", ret);
                            }
                        }
                    } else if (searchstring.equals("group")) {
                        this.addPublicMethods(FormGroup.class, ret);
                    } else if (searchstring.equals("user")) {
                        this.addUserObjects(ret);
                    } else if (searchstring.equals("spec")) {
                        DataSet specLimitTypes = this.getSpecLimitTypes(request, servletContext);
                        for (int i = 0; i < specLimitTypes.size(); ++i) {
                            this.addObject(specLimitTypes.getValue(i, "limittypeid"), ret, "Map", specLimitTypes.getValue(i, "limittypeid"));
                        }
                    } else if (searchstring.startsWith("spec.")) {
                        DataSet specLimitTypes = this.getSpecLimitTypes(request, servletContext);
                        if (specLimitTypes.findRow("limittypeid", searchstring.substring(searchstring.indexOf(".") + 1)) > -1) {
                            this.addObject("specid", ret, "String", "specid");
                            this.addObject("specversionid", ret, "String", "specversionid");
                            this.addObject("operator1", ret, "String", "operator1");
                            this.addObject("value1", ret, "String", "value1");
                            this.addObject("operator2", ret, "String", "operator2");
                            this.addObject("value2", ret, "String", "value2");
                        }
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

    protected void addUserObjects(JSONArray ret) {
        this.addObject("sysuserid", ret, "String", "sysuserid");
        this.addObject("dbms", ret, "String", "dbms");
        this.addObject("rolelist", ret, "List", "rolelist");
        this.addObject("modulelist", ret, "List", "modulelist");
        this.addObject("defaultdepartment", ret, "String", "defaultdepartment");
        this.addObject("departmentlist", ret, "String", "departmentlist");
        this.addObject("lastlogonjobtype", ret, "String", "lastlogonjobtype");
        this.addObject("jobtypelist", ret, "String", "jobtypelist");
        this.addObject("language", ret, "String", "language");
        this.addObject("locale", ret, "String", "locale");
        this.addObject("timezone", ret, "String", "timezone");
        this.addObject("sysusername", ret, "String", "sysusername");
        this.addObject("databaseid", ret, "String", "databaseid");
        this.addObject("deviceid", ret, "String", "deviceid");
        this.addObject("guimode", ret, "String", "guimode");
        this.addObject("tool", ret, "String", "tool");
        this.addObject("usertype", ret, "String", "usertype");
    }

    protected DataSet getSpecLimitTypes(HttpServletRequest request, ServletContext servletContext) {
        DataSet specLimitTypes = (DataSet)servletContext.getAttribute("distinctspeclimittypes");
        if (specLimitTypes == null) {
            QueryProcessor qp = new QueryProcessor(RequestContext.getRequestContext(request).getConnectionId());
            specLimitTypes = qp.getSqlDataSet("SELECT DISTINCT limittypeid FROM speclimittype ORDER BY 1");
            servletContext.setAttribute("distinctspeclimittypes", (Object)specLimitTypes);
        }
        return specLimitTypes;
    }

    protected void addPublicMethods(Class c, JSONArray ret) throws JSONException {
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

    protected String getParameterList(Method m) {
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

    protected void addStandardObjects(boolean extendedObjects, boolean actionProcess, JSONArray ret) {
        this.addObject("user", ret, "Map", "User connection info");
        this.addObject("logger", ret, "Object", "Message logger object");
        this.addObject("m18n", ret, "Object", "Multinationalization object");
        if (extendedObjects) {
            this.addObject("database", ret, "Object", "Database read access object");
            this.addObject("actionProcessor", ret, "Object", "ActionProcessor object");
            this.addObject("queryProcessor", ret, "Object", "QueryProcessor object");
            this.addObject("sdcProcessor", ret, "Object", "SDCProcessor object");
            if (actionProcess) {
                this.addObject("processAction( actionid, actionProps )", ret, "Method", "Process action method");
            }
        }
    }
}


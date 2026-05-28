/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspFactory
 *  javax.servlet.jsp.PageContext
 */
package sapphire.servlet;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.admin.system.SQLRegister;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.PseudoPageContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AjaxResponse {
    static final String LABVANTAGE_CVS_ID = "$Revision: 83838 $";
    private static final String CALLBACK = "callback";
    private static final String ERRORCALLBACK = "errorcallback";
    private static final String CALLBACKARGS = "callbackArgs";
    private static final String CALLPROPERTIES = "callproperties";
    private static final String ERROR = "error";
    private PrintWriter out;
    private JSONObject jsonResponse = new JSONObject();
    private Logger logger;
    private PageContext pageContext = null;
    private int platform = -1;
    private Browser browser;

    private AjaxResponse(HttpServletResponse response) {
        try {
            this.out = response != null ? response.getWriter() : null;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public PageContext getPageContext(Servlet servlet, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
        if (this.pageContext == null) {
            try {
                Configuration config = Configuration.getInstance();
                this.platform = config.getPlatform();
                this.pageContext = this.platform == 3 ? new PseudoPageContext(servletContext, request, response) : JspFactory.getDefaultFactory().getPageContext(servlet, (ServletRequest)request, (ServletResponse)response, "", true, 0, false);
            }
            catch (Exception e) {
                this.pageContext = null;
            }
        }
        return this.pageContext;
    }

    public static void handleException(HttpServletRequest request, HttpServletResponse response, Throwable t) {
        AjaxResponse ajaxResponse;
        if (request.getMethod().equalsIgnoreCase("POST") && request.getContentType().equalsIgnoreCase("application/json")) {
            ajaxResponse = new AjaxResponse(request, response);
        } else {
            ajaxResponse = new AjaxResponse(response);
            try {
                ajaxResponse.setErrorCallback(request.getParameter(ERRORCALLBACK));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        ajaxResponse.setError(t.getMessage(), t);
        ajaxResponse.print();
    }

    public AjaxResponse(HttpServletRequest request, HttpServletResponse response) {
        this.browser = new Browser(request);
        this.getRequestParams(request, response, "");
    }

    public AjaxResponse(HttpServletRequest request, HttpServletResponse response, String callback) {
        this.browser = new Browser(request);
        this.getRequestParams(request, response, callback);
        request.setAttribute("__ajaxresponse", (Object)this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void getRequestParams(HttpServletRequest request, HttpServletResponse response, String callback) {
        block14: {
            try {
                this.logger = new Logger(new LogContext("AJAXRESPONSE", RequestContext.getRequestContext(request).getConnectionId()));
                PrintWriter printWriter = this.out = response != null ? response.getWriter() : null;
                if (request.getMethod().equalsIgnoreCase("POST") && request.getContentType().equalsIgnoreCase("application/json")) {
                    StringBuffer content = new StringBuffer();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader((InputStream)request.getInputStream(), "UTF-8"));){
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine).append("\n");
                        }
                    }
                    if (content.length() > 0) {
                        try {
                            PropertyList requestProps = new PropertyList(new JSONObject(content.toString()));
                            this.setCallback(requestProps.getProperty(CALLBACK, callback));
                            this.setErrorCallback(requestProps.getProperty(ERRORCALLBACK, ""));
                            if (requestProps.containsKey(CALLPROPERTIES)) {
                                this.setCallProperties(new JSONObject(requestProps.getProperty(CALLPROPERTIES)));
                            }
                            if (requestProps.containsKey("_viewport")) {
                                this.browser.setViewPort(requestProps.getProperty("_viewport"), request);
                            }
                            break block14;
                        }
                        catch (Exception e) {
                            this.logger.warn("Could not process content from ajax request.");
                            this.setCallback("");
                            this.setErrorCallback("");
                        }
                        break block14;
                    }
                    this.logger.warn("No content sent through to Ajax request.");
                    this.setCallback("");
                    this.setErrorCallback("");
                    break block14;
                }
                this.setCallback(request.getParameter(CALLBACK) != null ? request.getParameter(CALLBACK) : callback);
                this.setErrorCallback(request.getParameter(ERRORCALLBACK) != null ? request.getParameter(ERRORCALLBACK) : "");
                if (request.getParameter(CALLPROPERTIES) != null) {
                    this.setCallProperties(new JSONObject(request.getParameter(CALLPROPERTIES)));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void setCallback(String callback) {
        try {
            this.jsonResponse.put(CALLBACK, SafeHTML.encodeForJavaScript(callback));
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void setErrorCallback(String errorcallback) {
        try {
            this.jsonResponse.put(ERRORCALLBACK, SafeHTML.encodeForJavaScript(errorcallback));
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public boolean isCallbackSet() {
        return this.jsonResponse.optString(CALLBACK) != null && this.jsonResponse.optString(CALLBACK).length() > 0;
    }

    public void addCallbackArgument(String name, Object data) {
        try {
            this.jsonResponse.put(name, data);
            this.addCallbackArgumentName(name);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void addCallbackArgument(String name, int data) {
        try {
            this.jsonResponse.put(name, data);
            this.addCallbackArgumentName(name);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void addCallbackArgument(String name, long data) {
        try {
            this.jsonResponse.put(name, data);
            this.addCallbackArgumentName(name);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void addCallbackArgument(String name, double data) {
        try {
            this.jsonResponse.put(name, data);
            this.addCallbackArgumentName(name);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void addCallbackArgument(String name, Map data) {
        try {
            this.jsonResponse.put(name, data);
            this.addCallbackArgumentName(name);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void addCallbackArgument(String name, Collection data) {
        try {
            this.jsonResponse.put(name, data);
            this.addCallbackArgumentName(name);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void addCallbackArgument(String name, boolean data) {
        try {
            this.jsonResponse.put(name, data);
            this.addCallbackArgumentName(name);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void addCallbackArgument(String name, DataSet dataset) {
        try {
            JSONArray jsonDataSet = new JSONArray();
            String[] column = dataset.getColumns();
            for (int row = 0; row < dataset.size(); ++row) {
                JSONObject jo = new JSONObject();
                for (int col = 0; col < column.length; ++col) {
                    jo.put(column[col], dataset.getValue(row, column[col]));
                }
                jsonDataSet.put(jo);
            }
            this.jsonResponse.put(name, jsonDataSet);
            this.addCallbackArgumentName(name);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    private void addCallbackArgumentName(String name) {
        try {
            String callbackArgs = this.jsonResponse.optString(CALLBACKARGS);
            if (callbackArgs == null || callbackArgs.length() == 0) {
                callbackArgs = name;
            } else {
                String[] names = StringUtil.split(callbackArgs, ",");
                if (names.length == 1) {
                    callbackArgs = name + "," + callbackArgs;
                } else {
                    callbackArgs = names[0];
                    for (int i = 1; i < names.length - 1; ++i) {
                        callbackArgs = callbackArgs + "," + names[i];
                    }
                    callbackArgs = callbackArgs + "," + name + "," + names[names.length - 1];
                }
            }
            this.jsonResponse.put(CALLBACKARGS, callbackArgs);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public JSONObject getCallProperties() {
        if (this.jsonResponse.has(CALLPROPERTIES)) {
            try {
                return this.jsonResponse.getJSONObject(CALLPROPERTIES);
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public void setCallProperties(Object properties) {
        try {
            this.jsonResponse.put(CALLPROPERTIES, properties);
            this.addCallbackArgumentName(CALLPROPERTIES);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void setError(String error) {
        try {
            if (this.logger != null) {
                this.logger.error(error);
            } else {
                Logger.logError(error);
            }
            this.jsonResponse.put(ERROR, error);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public void setError(String error, Throwable t) {
        try {
            if (this.logger != null) {
                this.logger.error(error, t);
            } else {
                Logger.logError(error, t);
            }
            this.jsonResponse.put(ERROR, error);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    public String getRequestParameter(String propertyid) {
        return this.getRequestParameter(propertyid, "");
    }

    public String getRequestParameter(String propertyid, String defaultValue) {
        try {
            String value = this.jsonResponse.getJSONObject(CALLPROPERTIES).getString(propertyid);
            return value != null && value.length() > 0 ? (value.startsWith("{|}") ? EncryptDecrypt.decryptRSA(value.substring("{|}".length())) : value) : defaultValue;
        }
        catch (JSONException e) {
            return defaultValue;
        }
    }

    public Map getRequestParameters() {
        try {
            JSONObject callProperties = this.jsonResponse.getJSONObject(CALLPROPERTIES);
            HashMap<String, Object> map = new HashMap<String, Object>();
            Iterator it = callProperties.keys();
            while (it.hasNext()) {
                String property = (String)it.next();
                Object o = callProperties.get(property);
                if (o instanceof JSONObject) {
                    map.put(property, callProperties.get(property));
                    continue;
                }
                String s = callProperties.getString(property);
                map.put(property, s != null && s.length() > 0 && s.startsWith("{|}") ? EncryptDecrypt.decryptRSA(s.substring("{|}".length())) : s);
            }
            return map;
        }
        catch (JSONException e) {
            return null;
        }
    }

    public String toString() {
        return this.jsonResponse.toString();
    }

    public void print() {
        if (this.pageContext != null && this.platform != 3) {
            JspFactory.getDefaultFactory().releasePageContext(this.pageContext);
        }
        this.out.print(this);
    }

    public Browser getBrowser() {
        return this.browser;
    }

    public DataSet getRegisteredSQLDataSet(QueryProcessor queryProcessor, Map params) throws SapphireException {
        int sqlcode = -1;
        try {
            Object sqlObject = null;
            if (params.get("dynamicsqlcode") != null) {
                sqlObject = SQLRegister.getDynamicSQL((String)params.get("dynamicsqlcode"));
            } else {
                sqlcode = Integer.parseInt((String)params.get("sqlcode"));
                sqlObject = SQLRegister.getSQL(SecurityService.getDatabaseId(queryProcessor.getConnectionid()), sqlcode);
            }
            boolean isOracle = new ConnectionProcessor(queryProcessor.getConnectionid()).isOra();
            if (sqlObject instanceof String) {
                String sql = (String)sqlObject;
                sql = this.replaceTokens(sql, params, isOracle);
                Object o = params.get("bindvars");
                if (o == null) {
                    return queryProcessor.getSqlDataSet(sql);
                }
                if (o instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject)o;
                    Object[] bindVars = new Object[((JSONObject)o).length()];
                    Iterator it = jsonObject.keys();
                    while (it.hasNext()) {
                        String index = (String)it.next();
                        bindVars[Integer.parseInt((String)index)] = jsonObject.get(index);
                    }
                    return queryProcessor.getPreparedSqlDataSet(sql, bindVars);
                }
                return queryProcessor.getPreparedSqlDataSet(sql, new Object[]{(String)params.get("bindvars")});
            }
            if (sqlObject instanceof PropertyList) {
                PropertyList dropdowndefinition = (PropertyList)sqlObject;
                SDIProcessor sdilist = new SDIProcessor(queryProcessor.getConnectionid());
                SDIRequest sdirequest = new SDIRequest();
                String sdcid = dropdowndefinition.getProperty("sdcid");
                sdirequest.setSDCid(sdcid);
                String orderby = "";
                if (dropdowndefinition.getProperty("queryfrom") != null && dropdowndefinition.getProperty("queryfrom").length() > 0) {
                    sdirequest.setQueryFrom(dropdowndefinition.getProperty("queryfrom"));
                } else {
                    SDCProcessor sdcProcessor = new SDCProcessor(queryProcessor.getConnectionid());
                    String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                    sdirequest.setQueryFrom(tableid);
                    orderby = (String)sdcProcessor.getSDCProperties(dropdowndefinition.getProperty("sdcid")).get("keycolid1");
                }
                String queryWhere = dropdowndefinition.getProperty("querywhere");
                queryWhere = this.replaceTokens(queryWhere, params, isOracle);
                sdirequest.setQueryWhere(queryWhere);
                sdirequest.setQueryOrderBy(dropdowndefinition.getProperty("queryorderby", orderby));
                String retrivecolumns = null;
                retrivecolumns = dropdowndefinition.getProperty("valuecolumn").length() > 0 ? dropdowndefinition.getProperty("valuecolumn") + (dropdowndefinition.getProperty("displaycolumn").length() > 0 ? "," + dropdowndefinition.getProperty("displaycolumn") : "") : orderby;
                sdirequest.setRequestItem("primary[" + retrivecolumns + "]");
                sdirequest.setShowTemplates(true);
                SDIData sdidata = sdilist.getSDIData(sdirequest);
                DataSet primary = sdidata.getDataset("primary");
                return primary;
            }
            return new DataSet();
        }
        catch (Exception e) {
            throw new SapphireException("Error calling registered SQL (" + sqlcode + "). Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(queryProcessor.getConnectionid())), e);
        }
    }

    private String replaceTokens(String sql, Map params, boolean isOracle) {
        String[] tokens = StringUtil.getTokens(sql);
        if (tokens != null && tokens.length > 0) {
            for (int i = 0; i < tokens.length; ++i) {
                String bindvalue;
                String string = bindvalue = params.get(tokens[i]) != null ? (String)params.get(tokens[i]) : "";
                if (bindvalue.indexOf("'") == 0 && bindvalue.lastIndexOf("'") == bindvalue.length() - 1) {
                    bindvalue = bindvalue.substring(1, bindvalue.length() - 1);
                    String[] valuesInList = StringUtil.split(bindvalue, "','");
                    StringBuilder sb = new StringBuilder();
                    for (int v = 0; v < valuesInList.length; ++v) {
                        sb.append((v == 0 ? "" : "','") + SafeSQL.encodeForSQL(valuesInList[v], isOracle));
                    }
                    bindvalue = "'" + sb.toString() + "'";
                } else {
                    bindvalue = SafeSQL.encodeForSQL(bindvalue, isOracle);
                }
                sql = StringUtil.replaceAll(sql, "[" + tokens[i] + "]", bindvalue);
            }
        }
        return sql;
    }
}


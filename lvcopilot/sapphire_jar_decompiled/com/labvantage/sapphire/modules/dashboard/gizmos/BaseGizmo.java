/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.MiscUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.groovy.GroovyPolicyUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.xml.PropertyDefaultList;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public abstract class BaseGizmo
extends BaseElement {
    public static final String TITLE_PROPERTY = "title";
    public static final String TABID_PROPERTY = "tabid";
    public static final String RESIZABLE_PROPERTY = "resizable";
    public static final String VISIBLE_PROPERTY = "visible";
    public static final String REFRESHEVERY_PROPERTY = "refreshevery";
    public static final String REFRESHONRESIZE_PROPERTY = "refreshonresize";
    public static final String WIDTH_PROPERTY = "width";
    public static final String HEIGHT_PROPERTY = "height";
    public static final String GIZMOPROPS_PROPERTY = "gizmoprops";
    public static final String OPTIONS_COLLECTION = "options";
    public static final String PROPERTYID_PROPERTY = "propertyid";
    public static final String EDITABLE_PROPERTY = "editable";
    public static final String DEFAULTGIZMOIMAGE = "WEB-CORE/images/svg/gizmo-c.svg";
    public static final int DEFAULT_WIDTH = 200;
    public static final int DEFAULT_HEIGHT = 200;
    public static final int STRING_TYPE = 0;
    public static final int YESNO_TYPE = 1;
    private int timeout = -1;
    private int width = -1;
    private int height = -1;
    private Boolean resizable = null;
    private Boolean refreshOnResize = null;
    private String title = "";
    private String image = "";
    private String imageTitle = "";
    private Color color = null;
    private Boolean visible = null;
    private PropertyList parameters = null;
    private M18NUtil clientM18N = null;
    private HashMap groovyBindMap = null;
    private String previewjs = "";
    private String gizmoDefId = "";
    private WebAdminProcessor wap = null;
    private GizmoStyle gizmoStyle = GizmoStyle.FULL;
    private int count = -1;
    private boolean countEvaluated = false;
    private GizmoLocation gizmoLocation = GizmoLocation.OLDDASHBOARD;
    private PropertyList userProperties;
    protected HttpServletRequest request = null;
    private boolean previewMode = false;

    protected String getParameterSource(String paramid) {
        PropertyListCollection pi = this.getParameters().getCollectionNotNull("paramitems");
        PropertyList p = pi.find("paramid", paramid);
        if (p != null && p.size() > 0) {
            return p.getProperty("source", "");
        }
        return "";
    }

    protected String getParameter(String paramid, String defaultValue) {
        PropertyListCollection pi = this.getParameters().getCollectionNotNull("paramitems");
        PropertyList p = pi.find("paramid", paramid);
        if (p != null && p.size() > 0) {
            return p.getProperty("value", defaultValue);
        }
        return defaultValue;
    }

    protected void setParameter(String paramid, String value) {
        PropertyList p;
        PropertyListCollection pi = this.getParameters().getCollection("paramitems");
        if (pi == null) {
            pi = new PropertyListCollection();
            this.getParameters().setProperty("paramitems", pi);
        }
        if ((p = pi.find("paramid", paramid)) == null) {
            p = new PropertyList();
            p.setProperty("paramid", paramid);
            pi.add(p);
        }
        p.setProperty("value", value);
        if (p.getProperty("datatype").length() == 0) {
            p.setProperty("datatype", "S");
        }
        p.setProperty("source", "gizmo");
    }

    public boolean init() {
        return true;
    }

    public String getElementid() {
        return this.elementid;
    }

    public HashMap getGroovyBindMap() {
        if (this.groovyBindMap == null) {
            PropertyListCollection paramitems;
            this.groovyBindMap = new HashMap();
            this.groovyBindMap.put("element", this.element);
            this.groovyBindMap.put("elements", this.requestContext.getPropertyList().getPropertyList("elements"));
            this.groovyBindMap.put("pagedata", this.requestContext.getPropertyList().getPropertyList("pagedata"));
            this.groovyBindMap.put("user", this.connectionInfo.getUserAttributeMap());
            this.groovyBindMap.put("policy", new GroovyPolicyUtil(this.pageContext));
            PropertyList params = new PropertyList();
            PropertyListCollection propertyListCollection = paramitems = this.getParameters() != null ? this.getParameters().getCollection("paramitems") : null;
            if (paramitems != null) {
                for (int i = 0; i < paramitems.size(); ++i) {
                    PropertyList currParam = paramitems.getPropertyList(i);
                    params.setProperty(currParam.getProperty("paramid"), currParam.getProperty("value"));
                }
            }
            this.groovyBindMap.put("parameters", params);
        }
        return this.groovyBindMap;
    }

    private WebAdminProcessor getWebAdminProcessor() {
        if (this.wap == null) {
            this.wap = this.pageContext != null ? new WebAdminProcessor(this.pageContext) : new WebAdminProcessor(this.getConnectionId());
        }
        return this.wap;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
        if (this.browser == null) {
            this.browser = new Browser(request);
        }
    }

    public M18NUtil getM18N() {
        if (this.clientM18N == null) {
            this.clientM18N = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        }
        return this.clientM18N;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setGizmoStyle(GizmoStyle gizmoStyle) {
        this.gizmoStyle = gizmoStyle;
    }

    public GizmoStyle getGizmoStyle() {
        return this.gizmoStyle;
    }

    public GizmoLocation getGizmoLocation() {
        return this.gizmoLocation;
    }

    public void setGizmoLocation(GizmoLocation gizmoLocation) {
        this.gizmoLocation = gizmoLocation;
    }

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    public boolean isPreviewMode() {
        return this.previewMode;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public void setParameters(PropertyList parameters) {
        this.parameters = parameters;
    }

    public PropertyList getParameters() {
        if (this.parameters == null) {
            this.parameters = new PropertyList();
        }
        return this.parameters;
    }

    protected void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    protected void setResizable(boolean resizable) {
        this.resizable = resizable ? Boolean.TRUE : Boolean.FALSE;
    }

    protected void setVisible(boolean visible) {
        this.visible = visible ? Boolean.TRUE : Boolean.FALSE;
    }

    protected void setRefreshOnResize(boolean refreshOnResize) {
        this.refreshOnResize = refreshOnResize ? Boolean.TRUE : Boolean.FALSE;
    }

    public String getTitle() {
        String title = this.title.length() == 0 ? this.getGizmoDefId() : this.title;
        String t = "";
        t = this.getParameters() != null && this.getParameters().size() > 0 ? this.evaluateExpression(this.element.getPropertyList(GIZMOPROPS_PROPERTY) != null ? this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty(TITLE_PROPERTY, title) : title, I18NFormat.CLIENT) : this.evalExpression(this.element.getPropertyList(GIZMOPROPS_PROPERTY) != null ? this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty(TITLE_PROPERTY, title) : title);
        if (t.contains("<") && t.contains(">")) {
            t = DOMUtil.convertChars(t);
        }
        if (this.pageContext != null && this.getConnectionId() != null) {
            return this.getTranslationProcessor().translate(t);
        }
        return t;
    }

    public String getHelpText() {
        String t = this.evalExpression(this.element.getPropertyList(GIZMOPROPS_PROPERTY) != null ? this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty("helptext", this.getTitle()) : this.getTitle());
        if (this.pageContext != null && this.getConnectionId() != null) {
            return this.getTranslationProcessor().translate(t);
        }
        return t;
    }

    public String getTitleColor() {
        return this.evalExpression(this.element.getPropertyList(GIZMOPROPS_PROPERTY) != null ? this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty("titlecolor") : "");
    }

    public int getTimeout() {
        if (this.timeout > -1) {
            return this.timeout;
        }
        return 0;
    }

    protected String evaluateExpression(String expression, I18NFormat i18NFormat) {
        return BaseGizmo.evaluateExpression(this.getGizmoDefId(), expression, i18NFormat, this.parameters, this.getGroovyBindMap(), this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), false, false);
    }

    protected void evaluateExpression(PropertyList props, I18NFormat i18NFormat) {
        BaseGizmo.evaluateExpression(this.getGizmoDefId(), props, i18NFormat, this.parameters, this.getGroovyBindMap(), this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
    }

    private static String evaluateExpression(String gizmodefid, String expression, I18NFormat i18NFormat, PropertyList parameters, HashMap bindMap, ConnectionInfo connectionInfo, boolean ignore, boolean ignoreDatabaseExpressions) {
        PropertyListCollection params = null;
        if (parameters == null) {
            parameters = new PropertyList();
        }
        if (!parameters.containsKey("paramitems")) {
            params = new PropertyListCollection();
            parameters.setProperty("paramitems", params);
        } else {
            params = parameters.getCollection("paramitems");
        }
        String[] tokens = StringUtil.getTokens(expression);
        if (tokens != null && tokens.length > 0) {
            for (int i = 0; i < tokens.length; ++i) {
                if (ignoreDatabaseExpressions && tokens[i].startsWith("%") && tokens[i].endsWith("%")) continue;
                PropertyList parameter = params.find("paramid", tokens[i]);
                if (parameter != null) {
                    String value = parameter.getProperty("value");
                    String datatype = parameter.getProperty("datatype", "S");
                    boolean processed = false;
                    if (datatype.equalsIgnoreCase("S") || datatype.equalsIgnoreCase("C")) {
                        expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", value);
                        processed = true;
                    } else if (datatype.equalsIgnoreCase("D") || datatype.equalsIgnoreCase("O")) {
                        String parsedValue = "";
                        if (i18NFormat == I18NFormat.DATABASE) {
                            parsedValue = I18nUtil.convertToQueryDateString(value, connectionInfo, !datatype.equalsIgnoreCase("O"));
                        } else {
                            M18NUtil m18nClient = new M18NUtil(connectionInfo);
                            Calendar calendar = m18nClient.parseCalendar(value);
                            if (i18NFormat == I18NFormat.SERVER) {
                                M18NUtil m18nServer = new M18NUtil();
                                parsedValue = m18nServer.format(calendar, !datatype.equalsIgnoreCase("O"));
                            } else {
                                parsedValue = m18nClient.format(calendar, !datatype.equalsIgnoreCase("O"));
                            }
                        }
                        expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", parsedValue);
                        processed = true;
                    } else if (datatype.equalsIgnoreCase("N")) {
                        M18NUtil m18nClient;
                        StringBuffer one = new StringBuffer();
                        StringBuffer two = new StringBuffer();
                        if (i18NFormat == I18NFormat.DATABASE) {
                            expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", I18nUtil.convertToSysNumberString(value, connectionInfo));
                        } else if (i18NFormat == I18NFormat.SERVER) {
                            m18nClient = new M18NUtil(connectionInfo);
                            MiscUtil.MiscString.parseComplexNumber(value, one, two, m18nClient, false);
                            expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", two.toString());
                        } else {
                            m18nClient = new M18NUtil(connectionInfo);
                            MiscUtil.MiscString.parseComplexNumber(value, one, two, m18nClient, true);
                            expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", two.toString());
                        }
                        processed = true;
                    }
                    if (!processed) continue;
                    PropertyListCollection gizmos = parameter.getCollection("gizmos");
                    if (gizmos == null) {
                        gizmos = new PropertyListCollection();
                        parameter.setProperty("gizmos", gizmos);
                    }
                    if (gizmos.find("gizmodefid", gizmodefid) != null) continue;
                    PropertyList gizmo = new PropertyList();
                    gizmo.setProperty("gizmodefid", gizmodefid);
                    gizmos.add(gizmo);
                    continue;
                }
                if (ignore) continue;
                PropertyListCollection missingparams = parameters.getCollection("missing");
                if (missingparams == null) {
                    missingparams = new PropertyListCollection();
                    parameters.setProperty("missing", missingparams);
                }
                boolean found = false;
                for (Object o : missingparams) {
                    PropertyList pl = (PropertyList)o;
                    if (pl == null || !pl.getProperty("paramid").equals(tokens[i]) || !pl.getProperty("gizmodefid").equals(gizmodefid)) continue;
                    found = true;
                }
                if (found) continue;
                PropertyList missing = new PropertyList();
                missing.setProperty("paramid", tokens[i]);
                missing.setProperty("gizmodefid", gizmodefid);
                missingparams.add(missing);
            }
        }
        if (expression.startsWith("$G{") && expression.endsWith("}") && bindMap != null) {
            try {
                expression = GroovyUtil.getInstance(connectionInfo).evaluateSecure(expression, bindMap);
            }
            catch (Exception e) {
                Logger.logError("Failed to parse groovy expression.", e);
            }
        }
        return expression;
    }

    public static void evaluateExpression(String gizmodefid, PropertyList props, I18NFormat i18NFormat, PropertyList parameters, HashMap bindMap, ConnectionInfo connectionInfo) {
        BaseGizmo.evaluateExpression(gizmodefid, props, i18NFormat, parameters, bindMap, connectionInfo, null);
    }

    public static void setUpParameters(Map requestMap, PropertyList parameters, ConnectionInfo connectionInfo) {
        PropertyList cu;
        Map in = requestMap;
        PropertyListCollection items = parameters.getCollection("paramitems");
        if (items == null) {
            items = new PropertyListCollection();
            parameters.setProperty("paramitems", items);
        }
        if (in != null) {
            for (String key : in.keySet()) {
                if (!(in.get(key) instanceof String[]) || key.equalsIgnoreCase("command") || key.equalsIgnoreCase("page") || key.equalsIgnoreCase("ajaxclass")) continue;
                PropertyList param = new PropertyList();
                param.setProperty("paramid", key);
                param.setProperty("value", ((String[])in.get(key))[0].toString());
                param.setProperty("datatype", EditorStyleField.getEditorStyleDataType("C"));
                param.setProperty("source", "request");
                items.add(param);
            }
        }
        if ((cu = parameters.getCollection("paramitems").find("paramid", "currentuser")) == null && connectionInfo != null) {
            cu = new PropertyList();
            cu.setProperty("paramid", "currentuser");
            cu.setProperty("value", connectionInfo.getSysuserId());
            cu.setProperty("datatype", EditorStyleField.getEditorStyleDataType("C"));
            cu.setProperty("source", "user");
            parameters.getCollection("paramitems").add(cu);
        }
    }

    public static void evaluateExpression(String gizmodefid, PropertyList props, I18NFormat i18NFormat, PropertyList parameters, HashMap bindMap, ConnectionInfo connectionInfo, ArrayList<String> ignoreProps) {
        BaseGizmo.evaluateExpression(gizmodefid, props, i18NFormat, parameters, bindMap, connectionInfo, ignoreProps, false);
    }

    protected static void evaluateExpression(String gizmodefid, PropertyList props, I18NFormat i18NFormat, PropertyList parameters, HashMap bindMap, ConnectionInfo connectionInfo, ArrayList<String> ignoreProps, boolean ignoreDatabaseExpressions) {
        if (props != null) {
            for (Map.Entry dataentry : props.entrySet()) {
                if (dataentry.getValue() instanceof PropertyListCollection) {
                    for (int i = 0; i < ((PropertyListCollection)dataentry.getValue()).size(); ++i) {
                        BaseGizmo.evaluateExpression(gizmodefid, ((PropertyListCollection)dataentry.getValue()).getPropertyList(i), i18NFormat, parameters, bindMap, connectionInfo, ignoreProps, ignoreDatabaseExpressions);
                    }
                    continue;
                }
                if (dataentry.getValue() instanceof PropertyList) {
                    BaseGizmo.evaluateExpression(gizmodefid, (PropertyList)dataentry.getValue(), i18NFormat, parameters, bindMap, connectionInfo, ignoreProps, ignoreDatabaseExpressions);
                    continue;
                }
                if (!(dataentry.getValue() instanceof String)) continue;
                boolean ignore = false;
                if (ignoreProps != null && ignoreProps.contains(dataentry.getKey())) {
                    ignore = true;
                }
                props.setProperty((String)dataentry.getKey(), BaseGizmo.evaluateExpression(gizmodefid, dataentry.getValue().toString(), i18NFormat, parameters, bindMap, connectionInfo, ignore, ignoreDatabaseExpressions));
            }
        }
    }

    public int getWidth() {
        if (this.width > 0) {
            return this.width;
        }
        return 200;
    }

    public boolean getResizable() {
        return this.resizable == null || this.resizable != false;
    }

    public boolean getVisible() {
        return this.visible == null || this.visible != false;
    }

    public boolean getRefreshOnResize() {
        return this.refreshOnResize != null && this.refreshOnResize != false;
    }

    public int getHeight() {
        if (this.height > 0) {
            return this.height;
        }
        return 200;
    }

    public void setBaseProperties() {
        if (this.element != null) {
            PropertyList gizmoProps = this.element.getPropertyList(GIZMOPROPS_PROPERTY);
            if (gizmoProps != null) {
                String visible;
                if (this.title.length() == 0) {
                    this.title = gizmoProps.getProperty(TITLE_PROPERTY, this.gizmoDefId);
                }
                if (this.visible == null) {
                    visible = gizmoProps.getProperty(VISIBLE_PROPERTY, "Y");
                    this.visible = visible.equalsIgnoreCase("N") ? Boolean.FALSE : Boolean.TRUE;
                }
                if (this.resizable == null) {
                    String resizable = gizmoProps.getProperty(RESIZABLE_PROPERTY, "Y");
                    this.resizable = resizable.equalsIgnoreCase("N") ? Boolean.FALSE : Boolean.TRUE;
                }
                if (this.refreshOnResize == null) {
                    visible = gizmoProps.getProperty(REFRESHONRESIZE_PROPERTY, "N");
                    this.resizable = visible.equalsIgnoreCase("Y") ? Boolean.TRUE : Boolean.FALSE;
                }
                if (this.timeout < 0) {
                    try {
                        int refresh = Integer.parseInt(gizmoProps.getProperty(REFRESHEVERY_PROPERTY, "0"));
                        this.timeout = refresh > 0 ? refresh * 1000 : 0;
                    }
                    catch (Exception e) {
                        this.timeout = 0;
                    }
                }
                if (this.width < 0) {
                    try {
                        this.width = Integer.parseInt(gizmoProps.getProperty(WIDTH_PROPERTY, "200"));
                    }
                    catch (Exception e) {
                        this.width = 200;
                    }
                }
                if (this.height < 0) {
                    try {
                        this.height = Integer.parseInt(gizmoProps.getProperty(HEIGHT_PROPERTY, "200"));
                    }
                    catch (Exception e) {
                        this.height = 200;
                    }
                }
            } else {
                this.logger.warn("No gizmoprops property list found.");
                this.resizable = Boolean.TRUE;
                this.refreshOnResize = Boolean.FALSE;
                this.title = "";
                this.visible = Boolean.TRUE;
                this.timeout = 0;
                this.height = 200;
                this.width = 200;
            }
        } else {
            this.logger.warn("No element properties found.");
            this.resizable = Boolean.TRUE;
            this.refreshOnResize = Boolean.FALSE;
            this.title = "";
            this.visible = Boolean.TRUE;
            this.timeout = 0;
            this.height = 200;
            this.width = 200;
        }
    }

    protected void setUserProperties(PropertyList userProperties) {
        this.userProperties = userProperties;
    }

    public PropertyList getUserProperties() {
        return this.userProperties == null ? new PropertyList() : this.userProperties;
    }

    public String getGizmoDefId() {
        return this.gizmoDefId;
    }

    public static PropertyList mergeLocationHotspotProperty(PropertyList baseProps, String hotspotProps) {
        try {
            JSONArray hotspotsPropArray = new JSONArray(hotspotProps);
            JSONObject hotspots = new JSONObject(hotspotsPropArray.get(0).toString());
            String sdcId = hotspots.get("sdcid").toString();
            String locationkeyId1 = hotspots.get("keyid1").toString();
            baseProps.getPropertyList("image").setProperty("sdcid", sdcId);
            baseProps.getPropertyList("image").setProperty("keyid1", locationkeyId1);
            JSONArray hotspotsArray = new JSONArray(hotspots.has("hotspot") ? hotspots.getString("hotspot") : "[]");
            JSONObject hotspotsMaps = hotspotsArray.getJSONObject(0);
            if (hotspotsArray.length() > 0) {
                for (int i = 0; i < hotspotsMaps.length(); ++i) {
                    String coords = hotspotsArray.getJSONObject(i).get("coords").toString();
                    String sdcid = hotspotsArray.getJSONObject(i).has("sdcid") ? hotspotsArray.getJSONObject(i).get("sdcid").toString() : "";
                    String keyid1 = hotspotsArray.getJSONObject(i).has("keyid1") ? hotspotsArray.getJSONObject(i).get("keyid1").toString() : "";
                    String label = hotspotsArray.getJSONObject(i).has("label") ? hotspotsArray.getJSONObject(i).get("label").toString() : "";
                    PropertyList hotspot = new PropertyList();
                    hotspot.setProperty("sdcid", sdcid);
                    hotspot.setProperty("keyid1", keyid1);
                    hotspot.setProperty("coordinates", coords);
                    hotspot.setProperty("url", "");
                    hotspot.setProperty("tip", "");
                    hotspot.setProperty("label", label);
                    hotspot.setProperty("querytype", "");
                    if (baseProps.getCollection("map") == null) {
                        baseProps.setProperty("map", new PropertyListCollection());
                    }
                    baseProps.getCollection("map").add(hotspot);
                }
            }
        }
        catch (Exception e) {
            Trace.logWarn("Could not merge properties - " + e.getMessage());
        }
        return baseProps;
    }

    public static PropertyList getGizmoDefProperties(String gizmoDefId, String propertytreeid, String extendnodeid, String productValueTree, String valueTree, WebAdminProcessor wap, SapphireConnection sc) {
        PropertyList baseProps = BaseGizmo.getProperties(propertytreeid, extendnodeid, wap, sc);
        try {
            if (productValueTree != null && productValueTree.length() > 0) {
                baseProps.setPropertyList(productValueTree, true);
                baseProps.setAttribute("productvaluetree", "Y");
            }
            if (valueTree != null && valueTree.length() > 0) {
                baseProps.setPropertyList(valueTree, true);
                baseProps.setAttribute("valuetree", "Y");
            }
        }
        catch (Exception e) {
            Trace.logWarn("Could not merge properties - " + e.getMessage());
        }
        return baseProps;
    }

    public static DataSet getGizmoDef(String gizmoDefId, String connectionid) {
        DataSet out = null;
        SDIRequest sdiReq = new SDIRequest();
        sdiReq.setSDCid("LV_GizmoDef");
        sdiReq.setKeyid1List(gizmoDefId);
        sdiReq.setRequestItem("primary");
        sdiReq.setExtendedDataTypes(true);
        SDIData sdi = new SDIProcessor(connectionid).getSDIData(sdiReq);
        if (sdi != null) {
            out = sdi.getDataset("primary");
        }
        return out;
    }

    public static DataSet getGizmoDef(String gizmoDefId, String databaseid, String sysuserid, String connectionid) {
        DataSet out = null;
        String sessionid = databaseid + ";" + sysuserid + ";";
        String sessionidref = sessionid + connectionid;
        DataSet found = null;
        Object cache = CacheUtil.get(databaseid, "GizmoDefAll", sessionidref);
        if (cache == null || !(cache instanceof DataSet)) {
            CacheUtil.removeAllStartWith(databaseid, "GizmoDefAll", sessionid);
            SDIRequest sdiReq = new SDIRequest();
            sdiReq.setSDCid("LV_GizmoDef");
            sdiReq.setQueryFrom("gizmodef");
            sdiReq.setRequestItem("primary");
            sdiReq.setExtendedDataTypes(true);
            SDIData sdi = new SDIProcessor(connectionid).getSDIData(sdiReq);
            if (sdi != null) {
                found = sdi.getDataset("primary");
                CacheUtil.put(databaseid, "GizmoDefAll", sessionidref, found);
            }
        } else {
            found = (DataSet)cache;
        }
        if (found != null) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("gizmodefid", gizmoDefId);
            out = found.getFilteredDataSet(filter);
        }
        return out;
    }

    public static DataSet getLocImageMapDef(String locationId, String databaseid, String sysuserid, String connectionid) {
        DataSet out = null;
        String sessionid = databaseid + ";" + sysuserid + ";";
        String sessionidref = sessionid + connectionid;
        CacheUtil.removeAllStartWith(databaseid, "GizmoDefAll", sessionid);
        SDIRequest sdiReq = new SDIRequest();
        sdiReq.setSDCid("Location");
        sdiReq.setQueryFrom("s_location");
        sdiReq.setQueryWhere("S_LOCATIONID= '" + locationId + "'");
        sdiReq.setRequestItem("primary");
        sdiReq.setExtendedDataTypes(true);
        SDIData sdi = new SDIProcessor(connectionid).getSDIData(sdiReq);
        if (sdi != null) {
            out = sdi.getDataset("primary");
        }
        return out;
    }

    public void setGizmoDefId(String gizmoDefId) {
        this.gizmoDefId = gizmoDefId;
    }

    public abstract String getScript();

    public String getOptionsHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.pageContext != null ? this.getTranslationProcessor() : new TranslationProcessor(this.getConnectionId());
        PropertyList userProperties = this.getUserProperties();
        PropertyListCollection options = this.element.getPropertyList(GIZMOPROPS_PROPERTY).getCollection(OPTIONS_COLLECTION);
        if (options != null && options.size() > 0) {
            for (int index = 0; index < options.size(); ++index) {
                String propertyid = options.getPropertyList(index).getProperty(PROPERTYID_PROPERTY, "");
                String editable = options.getPropertyList(index).getProperty(EDITABLE_PROPERTY, "Y");
                if (propertyid.length() <= 0) continue;
                String[] tree = StringUtil.split(propertyid, ".");
                PropertyList pl = userProperties;
                if (tree.length <= 0 || tree[0].equalsIgnoreCase(GIZMOPROPS_PROPERTY)) continue;
                for (int arrayindex = 0; arrayindex < tree.length; ++arrayindex) {
                    if (arrayindex == tree.length - 1) {
                        pl.setProperty(tree[arrayindex], editable);
                        continue;
                    }
                    if (pl.containsKey(tree[arrayindex])) {
                        pl = pl.getPropertyList(tree[arrayindex]);
                        continue;
                    }
                    PropertyList old = pl;
                    pl = new PropertyList();
                    old.setProperty(tree[arrayindex], pl);
                }
            }
        }
        if (userProperties != null && userProperties.size() > 0) {
            WebAdminProcessor wp = this.getWebAdminProcessor();
            try {
                PropertyDefinitionList propertyDefinitionList = wp.getPropertyDefinitionList(this.element.getProperty("propertytreeid", ""));
                html.append("<table border=0 cellpadding=5 cellspacing=0 align=center>");
                html.append("<tr>");
                html.append("<td style=\"font-weight:bold;\">").append("Title").append("</td>");
                html.append("<td>").append("<input type=text id=\"user_gizmoprops_title\" value=\"");
                html.append(this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty(TITLE_PROPERTY, ""));
                html.append("\">").append("</td>");
                html.append("</tr>");
                html.append("<tr>");
                html.append("<td style=\"font-weight:bold;\">").append("Tab Id").append("</td>");
                html.append("<td>").append("<input type=text id=\"user_gizmoprops_tabid\" value=\"");
                html.append(this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty(TABID_PROPERTY, ""));
                html.append("\">").append("<button onclick=\"sapphire.page.getTop().dashboard.pickTab(user_gizmoprops_tabid)\" style=\"height:22px;\">...</button>").append("</td>");
                html.append("</tr>");
                for (PropertyDefinition propdef : propertyDefinitionList) {
                    String propid = propdef.getId();
                    if (propid.length() <= 0 || !userProperties.containsKey(propid)) continue;
                    this.renderProperty(html, propid, userProperties.get(propid), propertyDefinitionList, this.element, "user");
                }
                html.append("</table>");
            }
            catch (Exception e) {
                this.logger.error("Could not create property definition.");
                html.setLength(0);
                html.append(tp.translate("Could not obtain properties."));
            }
        } else {
            html.append(tp.translate("No properties to define."));
        }
        return html.toString();
    }

    private void renderProperty(StringBuffer html, String propid, Object property, PropertyDefinitionList propertyDefinitionList, PropertyList values, String prefix) {
        if (propid.length() > 0) {
            if (property instanceof String) {
                PropertyDefinition propertyDefinition = propertyDefinitionList.getPropertyDef(propid);
                if (propertyDefinition != null) {
                    String editorhtml;
                    String title = propertyDefinition.getTitle();
                    if (title.length() == 0) {
                        title = propid;
                    }
                    PropertyValue pvalue = new PropertyValue(propid, false, null);
                    pvalue.value = values.getProperty(propid, "");
                    String editorname = propertyDefinition.getEditor();
                    String edit = property.toString();
                    if (editorname != null && editorname.length() > 0 && edit.equalsIgnoreCase("Y")) {
                        try {
                            Class<?> c = Class.forName("com.labvantage.sapphire.admin.propertytree." + editorname);
                            TypeSimple editor = (TypeSimple)c.newInstance();
                            editorhtml = editor.getEditor(prefix + "_" + propid, pvalue, this.element, false, propertyDefinition.getAttributes(), this.pageContext, false);
                        }
                        catch (Exception e1) {
                            try {
                                Class<?> c = Class.forName(editorname);
                                TypeSimple editor = (TypeSimple)c.newInstance();
                                editorhtml = editor.getEditor(prefix + "_" + propid, pvalue, null, false, propertyDefinition.getAttributes(), this.pageContext, false);
                            }
                            catch (Exception e2) {
                                editorhtml = pvalue.value;
                                this.logger.warn("Unable to diplay SimpleEditor ('" + editorname + "') for property " + propid);
                            }
                        }
                    } else {
                        editorhtml = pvalue.value;
                    }
                    html.append("<tr>");
                    html.append("<td style=\"font-weight:bold;\">").append(title).append("</td>");
                    html.append("<td>").append(editorhtml).append("</td>");
                    html.append("</tr>");
                }
            } else if (property instanceof PropertyList) {
                PropertyList propertyList = (PropertyList)property;
                PropertyDefinition propdef = propertyDefinitionList.getPropertyDef(propid);
                html.append("<tr>");
                html.append("<td style=\"font-weight:bold;\" valign=\"top\">").append(propdef.getTitle()).append("</td>");
                html.append("<td>");
                html.append("<table border=0 cellpadding=5 cellspacing=0 align=center>");
                Object prop = values.get(propid);
                PropertyDefinitionList newdeflist = propertyDefinitionList.getPropertyDef(propid).getPropertyDefinitionList();
                if (prop instanceof PropertyList) {
                    for (PropertyDefinition newpropdef : newdeflist) {
                        String newpropid = newpropdef.getId();
                        if (newpropid.length() <= 0 || !propertyList.containsKey(newpropid)) continue;
                        this.renderProperty(html, newpropid, propertyList.get(newpropid), newdeflist, (PropertyList)prop, prefix + "_" + propid);
                    }
                } else if (prop instanceof PropertyListCollection) {
                    PropertyListCollection parentcollection = (PropertyListCollection)prop;
                    for (int index = 0; index < parentcollection.size(); ++index) {
                        PropertyList parentlist = parentcollection.getPropertyList(index);
                        for (PropertyDefinition newpropdef : newdeflist) {
                            String newpropid = newpropdef.getId();
                            if (newpropid.length() <= 0 || !propertyList.containsKey(newpropid)) continue;
                            this.renderProperty(html, newpropid, propertyList.get(newpropid), newdeflist, parentlist, prefix + "_" + propid + "_" + index + "");
                        }
                        html.append("<tr>");
                        html.append("<td colspan=2>&nbsp;</td>");
                        html.append("</tr>");
                    }
                }
                html.append("</table>");
                html.append("</td>");
                html.append("</tr>");
            }
        }
    }

    public static PropertyList getElementProperties(ServletRequest request, String pageid, String elementid) {
        PropertyList element = new PropertyList();
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        RequestProcessor rp = new RequestProcessor(requestContext.getConnectionId());
        try {
            PropertyList pagedata = rp.getWebPageProperties(pageid, requestContext);
            element = pagedata.getPropertyList(elementid);
            return element;
        }
        catch (Exception exception) {
            return element;
        }
    }

    public String getElementType() {
        return this.elementType;
    }

    public void setPreviewJS(String js) {
        this.previewjs = js;
    }

    public String getPreviewJS() {
        return this.getNavigateJS();
    }

    public String getNavigateJS() {
        String s = this.getURL();
        if (s.length() > 0) {
            if (s.toLowerCase().startsWith("javascript:")) {
                return s.substring(11);
            }
            if (s.startsWith("sapphire.lookup.") || s.startsWith("sapphire.ui.") || s.startsWith("sapphire.page.")) {
                return s;
            }
            return "sapphire.page.navigate('" + this.getURL() + "', 'Y', null, null, true, this);";
        }
        return this.previewjs.length() > 0 ? this.previewjs : "dashboard.gizmos.preview('" + this.elementid + "');";
    }

    public void setCount(int count) {
        this.count = count;
        this.countEvaluated = true;
    }

    public int getCount() {
        if (!this.countEvaluated) {
            this.count = this.evalCount();
            this.countEvaluated = true;
        }
        return this.count;
    }

    public int evalCount() {
        return -1;
    }

    public String getURL() {
        return "";
    }

    public String getIconHtml() {
        int size = 16;
        String h = this.getHelpText();
        h = SafeHTML.encodeForHTML(h, true);
        StringBuffer html = new StringBuffer();
        BaseGizmo.evaluateExpression(this.getGizmoDefId(), this.element, I18NFormat.CLIENT, this.getParameters(), null, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        if (this.gizmoStyle.showImage) {
            html.append("<span title=\"").append(h).append("\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(this.gizmoStyle.className.length() > 0 ? " class=\"" + this.gizmoStyle.className + "_img\"" : "").append(">");
            html.append(this.getIcon());
            if (this.gizmoStyle != GizmoStyle.SMALLTEXT) {
                html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS() + ";event.cancelBubble=true;", this.elementid));
            }
            html.append("</span>");
        }
        if (this.gizmoStyle.showTitle) {
            String titleColor = this.getTitleColor();
            html.append("<span title=\"").append(h).append("\" id=\"").append(this.elementid).append("_text\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(this.gizmoStyle.className.length() > 0 ? " class=\"" + this.gizmoStyle.className + "_txt\"" : "");
            html.append(titleColor.length() > 0 ? " style=\"color:" + titleColor + "\"" : "").append(">");
            String t = this.getTitle();
            t = SafeHTML.encodeForHTML(t, true);
            html.append("<span id=\"").append(this.elementid).append("_changetext\">").append(t).append("</span>");
            if (this.gizmoStyle == GizmoStyle.SMALLTEXT) {
                html.append("</span>");
                html.append("<span").append(this.gizmoStyle.className.length() > 0 ? " class=\"" + this.gizmoStyle.className + "_notify\"" : "").append(">");
                html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS(), this.elementid));
                html.append("</span>");
            } else {
                if (!this.gizmoStyle.showImage || this.gizmoStyle == GizmoStyle.SMALLTEXT) {
                    html.append(this.getNotifyHtml(this.browser, this.getCount(), this.getPreviewJS(), this.elementid));
                }
                html.append("</span>");
            }
        }
        return html.toString();
    }

    private static PropertyList getProperties(String gizmopropertytreeid, String gizmonodeid, WebAdminProcessor wap, SapphireConnection sapphireConnection) {
        Object cache = CacheUtil.get(sapphireConnection.getDatabaseId(), "Gizmo", gizmopropertytreeid + ";" + gizmonodeid);
        if (cache != null && cache instanceof PropertyList) {
            ((PropertyList)cache).setDbms(sapphireConnection.getDbms());
            ((PropertyList)cache).setDatabaseid(sapphireConnection.getDatabaseId());
            return ((PropertyList)cache).copy();
        }
        PropertyList props = null;
        try {
            PropertyTree p = wap.getPropertyTree(gizmopropertytreeid);
            if (p != null) {
                props = p.getNodePropertyList(gizmonodeid, true, true);
                CacheUtil.put(sapphireConnection.getDatabaseId(), "GizmoDefaults", gizmopropertytreeid, p.getPropertyDefaultList());
            }
        }
        catch (Exception e) {
            Trace.logWarn("Unable to load properties for policy " + gizmopropertytreeid + " and node " + gizmonodeid, e);
        }
        if (props == null) {
            Trace.logWarn("Unable to load properties for policy " + gizmopropertytreeid + " and node " + gizmonodeid);
            props = new PropertyList();
        }
        try {
            props.setProperty("objectname", wap.getPropertyTreeObject(gizmopropertytreeid));
        }
        catch (Exception exception) {
            // empty catch block
        }
        CacheUtil.put(sapphireConnection.getDatabaseId(), "Gizmo", gizmopropertytreeid + ";" + gizmonodeid, props.copy());
        return props;
    }

    public static sapphire.pageelements.BaseGizmo getInstance(String connectionid, String gizmoDefId, boolean applyUserOverrides) {
        return BaseGizmo.getInstance(connectionid, null, gizmoDefId, null, applyUserOverrides, false);
    }

    private static boolean getGizmoUserOverrides(String gizmoDefId, String sysuserid, PropertyList gizmodefProps, QueryProcessor queryProcessor) {
        String valutree;
        boolean out = false;
        DataSet data = queryProcessor.getSqlDataSet(20022, new Object[]{sysuserid, gizmoDefId}, true);
        if (data != null && data.size() > 0 && (valutree = data.getValue(0, "valuetree", "")).length() > 0) {
            try {
                if (gizmodefProps != null) {
                    gizmodefProps.setPropertyList(valutree, true);
                }
                out = true;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return out;
    }

    private static void setGizmoUserOverrides(String gizmoDefId, String sysuserid, PropertyList overrides, QueryProcessor queryProcessor) {
        Object out = null;
        DataSet data = queryProcessor.getSqlDataSet(20022, new Object[]{sysuserid, gizmoDefId}, true);
        if (data != null && data.size() > 0) {
            queryProcessor.execSQL(20024, new Object[]{overrides.toXMLString(), sysuserid, gizmoDefId});
        } else {
            queryProcessor.execSQL(20023, new Object[]{overrides.toXMLString(), sysuserid, gizmoDefId});
        }
    }

    public void saveGizmoDefinition() {
        this.saveGizmoDefinition(null);
    }

    public void saveGizmoDefinition(PropertyList propertyList) {
        if (propertyList == null) {
            propertyList = this.getElementProperties();
        }
        if (this.gizmoDefId.length() > 0) {
            boolean devmode = false;
            ConfigurationProcessor cp = new ConfigurationProcessor(this.pageContext);
            try {
                devmode = cp.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception exception) {
                // empty catch block
            }
            PropertyList editsdi = new PropertyList();
            editsdi.setProperty("sdcid", "LV_GizmoDef");
            editsdi.setProperty("keyid1", this.getGizmoDefId());
            if (devmode) {
                editsdi.setProperty("productvaluetree", propertyList.toXMLString());
            } else {
                editsdi.setProperty("valuetree", propertyList.toXMLString());
            }
            try {
                this.getActionProcessor().processAction("EditSDI", "1", editsdi);
            }
            catch (Exception e) {
                this.logger.error("Failed to update Gizmo Definition. " + e.getMessage());
            }
        }
    }

    public void resetUserOverrides() {
        if (this.gizmoDefId.length() > 0) {
            ConnectionProcessor cp = this.pageContext != null ? new ConnectionProcessor(this.pageContext) : new ConnectionProcessor(this.getConnectionId());
            SapphireConnection sc = cp.getSapphireConnection();
            String sessionid = this.gizmoDefId + ";" + sc.getSysuserId() + ";";
            this.getQueryProcessor().execSQL(20025, new Object[]{this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId(), this.gizmoDefId});
            CacheUtil.removeAllStartWith(sc.getDatabaseId(), "GizmoDefUser", sessionid);
        }
    }

    public void saveUserOverrides() {
        this.saveUserOverrides(null);
    }

    public void saveUserOverrides(PropertyList propertyList) {
        if (propertyList == null) {
            propertyList = this.getElementProperties();
        }
        if (this.gizmoDefId.length() > 0) {
            BaseGizmo.setGizmoUserOverrides(this.gizmoDefId, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId(), propertyList, this.getQueryProcessor());
            ConnectionProcessor cp = this.pageContext != null ? new ConnectionProcessor(this.pageContext) : new ConnectionProcessor(this.getConnectionId());
            SapphireConnection sc = cp.getSapphireConnection();
            String sessionid = this.gizmoDefId + ";" + sc.getSysuserId() + ";" + sc.getConnectionId();
            CacheUtil.remove(sc.getDatabaseId(), "GizmoDefUser", sessionid);
        }
    }

    public static sapphire.pageelements.BaseGizmo getInstance(PageContext pageContext, String gizmoDefId, PropertyList parameters, boolean applyUserOverrides, boolean delayInit) {
        return BaseGizmo.getInstance(null, pageContext, gizmoDefId, parameters, applyUserOverrides, delayInit);
    }

    public static sapphire.pageelements.BaseGizmo getInstance(PageContext pageContext, String gizmoDefId, boolean applyUserOverrides) {
        return BaseGizmo.getInstance(null, pageContext, gizmoDefId, null, applyUserOverrides, false);
    }

    public static sapphire.pageelements.BaseGizmo getTypeInstance(String connectionid, String propertytreeid, String extendnodeid) {
        return BaseGizmo.getTypeInstance(connectionid, null, propertytreeid, extendnodeid);
    }

    public static sapphire.pageelements.BaseGizmo getTypeInstance(PageContext pageContext, String propertytreeid, String extendnodeid) {
        return BaseGizmo.getTypeInstance(null, pageContext, propertytreeid, extendnodeid);
    }

    public static sapphire.pageelements.BaseGizmo getTypeInstance(String connectionid, PageContext pageContext, String propertytreeid, String extendnodeid) {
        return BaseGizmo.getTypeInstance(connectionid, pageContext, propertytreeid, extendnodeid, null, false);
    }

    public static PropertyList applyRootProperties(PropertyList props, String ptreeid, SapphireConnection sapphireConnection) {
        Object p = CacheUtil.get(sapphireConnection.getDatabaseId(), "GizmoDefaults", ptreeid);
        if (p != null && p instanceof PropertyDefaultList) {
            try {
                WebAdminProcessor wp = new WebAdminProcessor(sapphireConnection.getConnectionId());
                PropertyDefinitionList propertyDefinitionList = wp.getPropertyDefinitionList(ptreeid);
                props.setPropertyTreeDefaults((PropertyDefaultList)p, propertyDefinitionList);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return props;
    }

    public static sapphire.pageelements.BaseGizmo getTypeInstance(String connectionid, PageContext pageContext, String propertytreeid, String extendnodeid, PropertyList parameters, boolean delayInit) {
        sapphire.pageelements.BaseGizmo out = null;
        try {
            Object gizmoOb;
            ConnectionProcessor cp = pageContext != null ? new ConnectionProcessor(pageContext) : new ConnectionProcessor(connectionid);
            SapphireConnection sc = cp.getSapphireConnection();
            WebAdminProcessor wap = pageContext != null ? new WebAdminProcessor(pageContext) : new WebAdminProcessor(connectionid);
            PropertyList props = BaseGizmo.getProperties(propertytreeid, extendnodeid, wap, sc);
            String objectName = props.getProperty("objectname");
            if (objectName.length() == 0) {
                objectName = wap.getPropertyTreeObject(propertytreeid);
            }
            if ((gizmoOb = Class.forName(objectName).newInstance()) instanceof BaseGizmo) {
                out = (sapphire.pageelements.BaseGizmo)gizmoOb;
                out.setElementType(propertytreeid);
                out.setGizmoDefId(props.getProperty("gizmodefid"));
                props = BaseGizmo.applyRootProperties(props.copy(), propertytreeid, sc);
                out.setElementProperties(props);
                if (!delayInit) {
                    BaseGizmo.initalizeInstance(out, connectionid, sc, pageContext, parameters);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return out;
    }

    private static sapphire.pageelements.BaseGizmo getInstance(String connectionid, PageContext pageContext, String gizmoDefId, PropertyList parameters, boolean applyUserOverrides, boolean delayInit) {
        sapphire.pageelements.BaseGizmo out = null;
        ConnectionProcessor cp = pageContext != null ? new ConnectionProcessor(pageContext) : new ConnectionProcessor(connectionid);
        TranslationProcessor tp = null;
        SapphireConnection sc = cp.getSapphireConnection();
        boolean isLocationGizmo = false;
        String locationId = "";
        boolean isDBfetchRequired = true;
        if (parameters != null && !parameters.getCollection("paramitems").isEmpty()) {
            for (PropertyList p : parameters.getCollection("paramitems")) {
                if (!p.getProperty("paramid").toString().equalsIgnoreCase("location")) continue;
                isLocationGizmo = true;
                locationId = p.getProperty("value").toString();
            }
        }
        String sessionid = gizmoDefId + ";" + sc.getSysuserId() + ";" + sc.getConnectionId();
        DataSet ds_gizmodef = null;
        if (isLocationGizmo) {
            String accesserror = (String)CacheUtil.get(sc.getDatabaseId(), "GizmoDefAccess", sessionid);
            ds_gizmodef = BaseGizmo.getLocImageMapDef(locationId, sc.getDatabaseId(), sc.getSysuserId(), sc.getConnectionId());
            accesserror = ds_gizmodef.size() == 0 ? "Gizmo " + gizmoDefId + "does not exist or you have insufficient access" : "";
            if (accesserror.length() == 0) {
                String o;
                WebAdminProcessor wap = pageContext != null ? new WebAdminProcessor(pageContext) : new WebAdminProcessor(connectionid);
                PropertyList gizmodef = null;
                if (ds_gizmodef != null && ds_gizmodef.getRowCount() == 1) {
                    String propertytreeid = "imagemapgizmo";
                    String extendnodeid = "HotspotImage Location Custom";
                    String hotspotProps = ds_gizmodef.getClob(0, "hotspotpropertyclob");
                    PropertyList props = BaseGizmo.getGizmoDefProperties(gizmoDefId, propertytreeid, extendnodeid, null, null, wap, sc);
                    props = BaseGizmo.mergeLocationHotspotProperty(props, hotspotProps);
                    gizmodef = new PropertyList();
                    props.getPropertyList("image");
                    gizmodef.setProperty("gizmodefid", gizmoDefId);
                    gizmodef.setProperty("propertytreeid", propertytreeid);
                    gizmodef.setProperty("element", props);
                    try {
                        String objectName = props.getProperty("objectname");
                        if (objectName.length() == 0) {
                            objectName = wap.getPropertyTreeObject(propertytreeid);
                        }
                        gizmodef.setProperty("object", objectName);
                    }
                    catch (Exception objectName) {
                        // empty catch block
                    }
                }
                try {
                    String rolelist = sc.getRoleList();
                    String modulelist = sc.getModuleList();
                    Set<String> inactiveRoles = wap.getInactiveRoleList();
                    gizmodef.setDbms(sc.getDbms());
                    gizmodef.setDatabaseid(sc.getDatabaseId());
                    if (tp == null) {
                        tp = pageContext != null ? new TranslationProcessor(pageContext) : new TranslationProcessor(connectionid);
                    }
                    gizmodef = gizmodef.copy(sc.getLanguage(), tp, rolelist, modulelist, inactiveRoles);
                }
                catch (Exception rolelist) {
                    // empty catch block
                }
                gizmodef = gizmodef.copy();
                ConnectionInfo connectionInfo = cp.getConnectionInfo(sc.getConnectionId());
                PropertyList element = gizmodef.getPropertyList("element");
                element.setAttribute("useroverrides", "N");
                if (gizmodef != null && (o = gizmodef.getProperty("object", "")).length() > 0) {
                    try {
                        Object gizmoOb = Class.forName(o).newInstance();
                        if (gizmoOb instanceof BaseGizmo) {
                            out = (sapphire.pageelements.BaseGizmo)gizmoOb;
                            out.setGizmoDefId(gizmodef.getProperty("gizmodefid"));
                            out.elementType = gizmodef.getProperty("propertytreeid");
                            PropertyList finalList = BaseGizmo.applyRootProperties(gizmodef.getPropertyList("element").copy(), out.elementType, sc);
                            out.setElementProperties(finalList);
                            if (!delayInit) {
                                return BaseGizmo.initalizeInstance(out, connectionid, sc, pageContext, parameters);
                            }
                            return out;
                        }
                    }
                    catch (Exception e) {
                        out = null;
                    }
                }
            } else {
                Logger.logError(accesserror);
            }
        } else {
            String accesserror = (String)CacheUtil.get(sc.getDatabaseId(), "GizmoDefAccess", sessionid);
            if (accesserror == null) {
                ds_gizmodef = BaseGizmo.getGizmoDef(gizmoDefId, sc.getDatabaseId(), sc.getSysuserId(), sc.getConnectionId());
                if (ds_gizmodef.size() == 0) {
                    accesserror = "Gizmo " + gizmoDefId + "does not exist or you have insufficient access";
                } else {
                    try {
                        QueryProcessor qp = pageContext != null ? new QueryProcessor(pageContext) : new QueryProcessor(connectionid);
                        DataSet ds = qp.getPreparedSqlDataSet("GizmoCheck", "SELECT gizmodefid FROM gizmodef WHERE gizmodefid = ? AND ( EXISTS ( SELECT null FROM sdimodule WHERE sdimodule.sdcid='LV_GizmoDef' AND sdimodule.keyid1 = ? AND  sdimodule.moduleid in ( SELECT moduleid FROM modulesysuser WHERE sysuserid = ? UNION SELECT moduleid FROM module WHERE maxusers = 'S' ) ) OR NOT EXISTS( SELECT null FROM sdimodule WHERE sdimodule.sdcid='LV_GizmoDef' and sdimodule.keyid1 = ? ) )", new Object[]{gizmoDefId, gizmoDefId, sc.getSysuserId(), gizmoDefId});
                        accesserror = ds == null || ds.getRowCount() == 0 ? "You do not have access to the module of this Gizmo" : "";
                    }
                    catch (Exception e) {
                        Trace.logError("Could not check module access to gizmo " + gizmoDefId + ".");
                        accesserror = "User " + sc.getSysuserId() + " does not have access to gizmo ";
                    }
                }
                CacheUtil.put(sc.getDatabaseId(), "GizmoDefAccess", sessionid, accesserror);
            }
            if (accesserror.length() == 0) {
                String o;
                Object cache;
                PropertyList gizmodef = null;
                PropertyList gizmodef_overrides = null;
                if (applyUserOverrides && (cache = CacheUtil.get(sc.getDatabaseId(), "GizmoDefUser", sessionid)) != null) {
                    gizmodef_overrides = ((PropertyList)cache).copy();
                }
                if (gizmodef_overrides == null) {
                    cache = CacheUtil.get(sc.getDatabaseId(), "GizmoDefSecured", sessionid);
                    if (cache != null) {
                        gizmodef = ((PropertyList)cache).copy();
                    }
                    if (gizmodef == null) {
                        WebAdminProcessor wap;
                        Object c = CacheUtil.get(sc.getDatabaseId(), "GizmoDef", gizmoDefId);
                        WebAdminProcessor webAdminProcessor = wap = pageContext != null ? new WebAdminProcessor(pageContext) : new WebAdminProcessor(connectionid);
                        if (c != null && c instanceof PropertyList) {
                            gizmodef = (PropertyList)c;
                        } else {
                            if (ds_gizmodef == null) {
                                ds_gizmodef = BaseGizmo.getGizmoDef(gizmoDefId, sc.getDatabaseId(), sc.getSysuserId(), sc.getConnectionId());
                            }
                            if (ds_gizmodef != null && ds_gizmodef.getRowCount() == 1) {
                                String propertytreeid = ds_gizmodef.getValue(0, "propertytreeid");
                                String extendnodeid = ds_gizmodef.getValue(0, "extendnodeid");
                                String productValueTree = ds_gizmodef.getClob(0, "productvaluetree");
                                String valueTree = ds_gizmodef.getClob(0, "valuetree");
                                PropertyList props = BaseGizmo.getGizmoDefProperties(gizmoDefId, propertytreeid, extendnodeid, productValueTree, valueTree, wap, sc);
                                gizmodef = new PropertyList();
                                gizmodef.setProperty("gizmodefid", gizmoDefId);
                                gizmodef.setProperty("propertytreeid", propertytreeid);
                                gizmodef.setProperty("element", props);
                                try {
                                    String objectName = props.getProperty("objectname");
                                    if (objectName.length() == 0) {
                                        objectName = wap.getPropertyTreeObject(propertytreeid);
                                    }
                                    gizmodef.setProperty("object", objectName);
                                }
                                catch (Exception objectName) {
                                    // empty catch block
                                }
                                CacheUtil.put(sc.getDatabaseId(), "GizmoDef", gizmoDefId, gizmodef);
                                try {
                                    String rolelist = sc.getRoleList();
                                    String modulelist = sc.getModuleList();
                                    Set<String> inactiveRoles = wap.getInactiveRoleList();
                                    gizmodef.setDbms(sc.getDbms());
                                    gizmodef.setDatabaseid(sc.getDatabaseId());
                                    gizmodef = gizmodef.copy(rolelist, modulelist, inactiveRoles);
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        }
                        try {
                            String rolelist = sc.getRoleList();
                            String modulelist = sc.getModuleList();
                            Set<String> inactiveRoles = wap.getInactiveRoleList();
                            gizmodef.setDbms(sc.getDbms());
                            gizmodef.setDatabaseid(sc.getDatabaseId());
                            if (tp == null) {
                                tp = pageContext != null ? new TranslationProcessor(pageContext) : new TranslationProcessor(connectionid);
                            }
                            gizmodef = gizmodef.copy(sc.getLanguage(), tp, rolelist, modulelist, inactiveRoles);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        CacheUtil.put(sc.getDatabaseId(), "GizmoDefSecured", sessionid, gizmodef);
                    }
                    gizmodef = gizmodef.copy();
                    ConnectionInfo connectionInfo = cp.getConnectionInfo(sc.getConnectionId());
                    PropertyList element = gizmodef.getPropertyList("element");
                    if (BaseGizmo.getGizmoUserOverrides(gizmoDefId, connectionInfo.getSysuserId(), applyUserOverrides ? element : null, pageContext != null ? new QueryProcessor(pageContext) : new QueryProcessor(connectionid))) {
                        element.setAttribute("useroverrides", "Y");
                    } else {
                        element.setAttribute("useroverrides", "N");
                    }
                    if (applyUserOverrides) {
                        CacheUtil.put(sc.getDatabaseId(), "GizmoDefUser", sessionid, gizmodef);
                    }
                } else {
                    gizmodef = gizmodef_overrides;
                }
                if (gizmodef != null && (o = gizmodef.getProperty("object", "")).length() > 0) {
                    try {
                        Object gizmoOb = Class.forName(o).newInstance();
                        if (gizmoOb instanceof BaseGizmo) {
                            out = (sapphire.pageelements.BaseGizmo)gizmoOb;
                            out.setGizmoDefId(gizmodef.getProperty("gizmodefid"));
                            out.elementType = gizmodef.getProperty("propertytreeid");
                            PropertyList finalList = BaseGizmo.applyRootProperties(gizmodef.getPropertyList("element").copy(), out.elementType, sc);
                            out.setElementProperties(finalList);
                            if (!delayInit) {
                                return BaseGizmo.initalizeInstance(out, connectionid, sc, pageContext, parameters);
                            }
                            return out;
                        }
                    }
                    catch (Exception e) {
                        out = null;
                    }
                }
            } else {
                Logger.logError(accesserror);
            }
        }
        return out;
    }

    public static sapphire.pageelements.BaseGizmo initalizeInstance(sapphire.pageelements.BaseGizmo gizmo, String connectionid, SapphireConnection sc, PageContext pageContext, PropertyList parameters) {
        if (pageContext != null) {
            gizmo.setPageContext(pageContext);
            gizmo.setRequest((HttpServletRequest)pageContext.getRequest());
        } else {
            gizmo.setConnectionId(connectionid);
            gizmo.connectionInfo = new ConnectionInfo(sc);
        }
        gizmo.setParameters(parameters);
        gizmo.setBaseProperties();
        if (!gizmo.init()) {
            return null;
        }
        return gizmo;
    }

    public String getIcon() {
        return this.getImage(this.element.getProperty("imagetitle", "Gizmo"), this.gizmoStyle.size).getHtml();
    }

    protected Image getImage(String defaulttitle, int size) {
        String imageTitle;
        Image im;
        if (this.pageContext == null) {
            im = new Image();
            im.setConnectionId(this.getConnectionId());
        } else {
            im = new Image(this.pageContext);
        }
        String image = this.getImageSrc();
        String string = this.element != null ? this.element.getProperty("imagetitle", this.element.getPropertyList(GIZMOPROPS_PROPERTY) != null ? this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty(TITLE_PROPERTY, defaulttitle) : defaulttitle) : (imageTitle = defaulttitle);
        if (this.getTranslationProcessor() != null) {
            imageTitle = this.getTranslationProcessor().translate(imageTitle);
        }
        if (image.endsWith(".png") || image.endsWith(".svg") || image.startsWith("rc?command=")) {
            im.setImageSrc(image);
        } else {
            im.setImageId(image);
        }
        im.setElementid(this.elementid + "_image");
        im.setDimensions(size, size);
        im.setTitle(imageTitle);
        if (this.isPreviewMode()) {
            im.setColor(String.format("#%02x%02x%02x", Color.black.getRed(), Color.black.getGreen(), Color.black.getBlue()));
        } else if (this.color != null) {
            im.setColor(String.format("#%02x%02x%02x", this.color.getRed(), this.color.getGreen(), this.color.getBlue()));
        }
        return im;
    }

    public String getDefaultImageSrc() {
        return this.element.getProperty("image", DEFAULTGIZMOIMAGE);
    }

    public String getImageSrc() {
        return this.evalExpression(this.element != null ? (this.element.getPropertyList(GIZMOPROPS_PROPERTY) != null ? this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty("image", this.element.getProperty("image", this.getDefaultImageSrc())) : this.element.getProperty("image", this.getDefaultImageSrc())) : this.getDefaultImageSrc());
    }

    public String getNotifyHtml(Browser b, int count, String onclick) {
        return this.getNotifyHtml(b, count, onclick, null);
    }

    public String getCountColor() {
        return this.evalExpression(this.element != null ? (this.element.getPropertyList(GIZMOPROPS_PROPERTY) != null ? this.element.getPropertyList(GIZMOPROPS_PROPERTY).getProperty("notifycolor", this.element.getProperty("notifycolor")) : this.element.getProperty("notifycolor")) : "");
    }

    public String getNotifyHtml(Browser b, int count, String onclick, String elementid) {
        StringBuffer html = new StringBuffer();
        if (count > -1) {
            String color;
            html.append("<span ").append(elementid != null && elementid.length() > 0 ? "id=\"" + elementid + "_count\"" : "").append(" class=\"ws_notify ").append(count > 0 ? "ws_count_on" : "ws_count_off").append("\" ");
            if (onclick != null && onclick.length() > 0) {
                html.append("onclick=\"").append(onclick).append(";event.cancelBubble=true;").append("\"");
            }
            if ((color = this.getCountColor()).length() > 0) {
                html.append(" style=\"background-color:").append(color).append("\"");
            }
            html.append(">");
            html.append(count).append("</span>");
        }
        return html.toString();
    }

    public static int getSDICount(SDIProcessor sdiProcessor, String sdcid, String queryid, PropertyListCollection queryparams, String queryfrom, String querywhere) {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setCountRequest(true);
        if (queryid != null && queryid.length() > 0) {
            sdiRequest.setQueryid(queryid);
            if (queryparams != null) {
                String[] params = new String[queryparams.size()];
                for (int i = 0; i < queryparams.size(); ++i) {
                    params[i] = queryparams.getPropertyList(i).getProperty("paramvalue");
                }
                sdiRequest.setQueryParams(params);
            }
        } else if (queryfrom != null && queryfrom.length() > 0) {
            sdiRequest.setQueryFrom(queryfrom);
            sdiRequest.setQueryWhere(querywhere);
        } else {
            return -1;
        }
        return sdiProcessor.getSDICount(sdiRequest, false);
    }

    protected String evalExpression(String expression) {
        if (expression.startsWith("$G{") && expression.endsWith("}")) {
            try {
                HashMap<String, Object> bindingMap = GroovyUtil.getCommonBindingMap(this.connectionInfo, null, this.element, null, null);
                HashMap<String, Integer> gizmo = new HashMap<String, Integer>();
                gizmo.put("count", this.getCount());
                bindingMap.put("gizmo", gizmo);
                return GroovyUtil.getInstance(this.pageContext).evaluateSecure(expression.substring(3, expression.length() - "}".length()), bindingMap);
            }
            catch (Exception e) {
                this.logger.error("Failed to evaluate groovy expression '" + expression + "'. Reason: " + e.getMessage(), e);
                return expression;
            }
        }
        return expression;
    }

    protected void subscribeToParameter(String parameterid) {
        PropertyList parameters = this.getParameters();
        PropertyListCollection paramitems = parameters.getCollection("paramitems");
        if (paramitems != null) {
            PropertyList gizmo;
            PropertyListCollection gizmos;
            PropertyList param = paramitems.find("paramid", parameterid);
            if (param == null) {
                this.setParameter(parameterid, "");
                param = paramitems.find("paramid", parameterid);
            }
            if ((gizmos = param.getCollection("gizmos")) == null) {
                gizmos = new PropertyListCollection();
                param.setProperty("gizmos", gizmos);
            }
            if ((gizmo = gizmos.find("gizmodefid", this.getGizmoDefId())) == null) {
                gizmo = new PropertyList();
                gizmo.setProperty("gizmodefid", this.getGizmoDefId());
                gizmos.add(gizmo);
            }
        }
    }

    public static enum GizmoStyle {
        LARGE("Large Icon", 48, true, false, "gizmo_large"),
        LARGETEXT("Large Icon With Text", 48, true, true, "gizmo_large"),
        MEDIUM("Medium Icon", 32, true, false, "gizmo_med"),
        MEDIUMTEXT("Medium Icon With Text", 32, true, true, "gizmo_med"),
        SMALL("Small Icon", 16, true, false, "gizmo_small"),
        SMALLTEXT("Small Icon With Text", 16, true, true, "gizmo_small"),
        TEXT("Text Only", 0, false, true, "gizmo_text"),
        FULL("Full Render", 0, false, false, "");

        String text = "";
        String className = "";
        int size = 0;
        boolean showTitle = false;
        boolean showImage = false;

        private GizmoStyle(String text, int size, boolean showImage, boolean showTitle, String className) {
            this.text = text;
            this.size = size;
            this.showImage = showImage;
            this.showTitle = showTitle;
            this.className = className;
        }

        public static GizmoStyle getGizmoStyle(String text, boolean withTitle) {
            GizmoStyle out = LARGETEXT;
            for (GizmoStyle gs : GizmoStyle.values()) {
                if (!gs.text.equalsIgnoreCase(text)) continue;
                out = gs;
            }
            if (withTitle) {
                switch (out) {
                    case LARGE: {
                        out = LARGETEXT;
                        break;
                    }
                    case MEDIUM: {
                        out = MEDIUMTEXT;
                        break;
                    }
                    case SMALL: {
                        out = SMALLTEXT;
                    }
                }
            }
            return out;
        }
    }

    public static enum GizmoLocation {
        TOPBAR,
        SIDEBAR,
        PAGE,
        DASHBOARD,
        OLDDASHBOARD;

    }

    public static enum I18NFormat {
        DATABASE,
        SERVER,
        CLIENT;

    }
}


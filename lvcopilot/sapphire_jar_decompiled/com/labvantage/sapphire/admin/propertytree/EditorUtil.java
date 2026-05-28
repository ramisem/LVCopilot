/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 *  org.apache.commons.codec.digest.DigestUtils
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.propertytree.PropertyListEditor;
import com.labvantage.sapphire.admin.propertytree.PropertyTreeDisplayOptions;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.scheduler.SchedulerAdminProcessor;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.groovy.GroovyBindVariableRegister;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class EditorUtil {
    public static final String ANCESTOR_PREFIX = "{|";
    public static final String ANCESTOR_SUFFIX = "|}";
    public static final String GROOVY_PREFIX = "$G{";
    public static final String GROOVY_SUFFIX = "}";
    public static final String GUIMODE_PREFIX = "$R{";
    public static final String GUIMODE_SUFFIX = "}";
    public static final String VARIABLES_PREFIX = "[variables.";
    public static final String VARIABLES_SUFFIX = "]";
    private static final String DIRECTION_GROUPED = "grouped";
    private static final String DIRECTION_ACROSS = "across";
    public static final String COLLECTION_ITEM_PASTE = "collectionitempaste";
    public static final String COLLECTION_ITEM_PASTE_CONTEXT = "collectionitempastecontext";
    public static final String COLLECTION_ITEM_PASTE_CONTEXT_TYPE_PROPERTYTREE = "propertytree";
    public static final String COLLECTION_ITEM_PASTE_CONTEXT_TYPE_WEBPAGE = "webpage";
    public static final String COLLECTION_ITEM_PASTE_CONTEXT_TYPE_SCHEDULEPLANITEM = "scheduleplanitem";
    public static final String COLLECTION_ITEM_PASTE_CONTEXT_TYPE_STABILITYPLANITEM = "stabilityplanitem";
    public static final String COLLECTION_ITEM_PASTE_CONTEXT_TYPE_MAINTPAGE = "maintpage";
    public static final String NONEXIST_PREFIX = "?-";
    public static final String NONEXIST_SUFFIX = "-?";

    public static SDCProcessor getSDCProcessor(PageContext pageContext) {
        return new SDCProcessor(pageContext);
    }

    public static SDIProcessor getSDIProcessor(PageContext pageContext) {
        return new SDIProcessor(pageContext);
    }

    public static QueryProcessor getQueryProcessor(PageContext pageContext) {
        return new QueryProcessor(pageContext);
    }

    public static String getAllowedModuleList(String ptreeid, PageContext pageContext) {
        String databaseid;
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        String modulelist = "";
        if (requestContext != null && ptreeid != null && ptreeid.length() > 0 && (databaseid = requestContext.getProperty("databaseid")).length() > 0) {
            modulelist = (String)CacheUtil.get(databaseid, "ModuleListByPtreeid", ptreeid);
            if (modulelist == null) {
                String ptreetype = EditorUtil.getQueryProcessor(pageContext).getPreparedSqlDataSet("SELECT propertytreetype FROM propertytree WHERE propertytreeid=?", new Object[]{ptreeid}).getValue(0, "propertytreetype");
                modulelist = EditorUtil.getAllowedModuleList(ptreetype);
                CacheUtil.put(databaseid, "ModuleListByPtreeid", ptreeid, modulelist);
            }
            return modulelist;
        }
        return modulelist;
    }

    public static String getAllowedModuleList(String ptreetype) {
        String modulelist = "";
        if ("Element".equals(ptreetype) || "Layout".equals(ptreetype) || "Page Type".equals(ptreetype) || "Gizmo".equals(ptreetype) || "StellarDataSource".equals(ptreetype) || "StellarElement".equals(ptreetype) || "StellarGizmo".equals(ptreetype) || "StellarPageType".equals(ptreetype)) {
            modulelist = "WPDPro;WPDStd";
        } else if ("Authentication".equals(ptreetype) || "Password Validator".equals(ptreetype)) {
            modulelist = "Security";
        }
        return modulelist;
    }

    public static String displayEditor(String nodeid, PropertyDefinition propertyDefinition, PropertyList propertylist, PropertyList toppropertylist, String parentid, int propertylistindex, PageContext pageContext, PropertyTreeDisplayOptions options, TranslationProcessor tp, Browser browser) {
        boolean rememberCascadeAdvanced;
        StringBuffer output;
        block82: {
            String clipboard;
            output = new StringBuffer();
            String propertyid = propertyDefinition.getId();
            String fieldName = parentid + "_" + propertylistindex + "_" + propertyid;
            String editorName = propertyDefinition.getEditor();
            boolean advanced = options.cascadeAdvanced || propertyDefinition.isAdvanced();
            options.containsAdvanced |= advanced;
            rememberCascadeAdvanced = options.cascadeAdvanced;
            if (advanced) {
                options.cascadeAdvanced = true;
            }
            boolean pasteButton = false;
            if (options.collectionitempaste && (clipboard = (String)pageContext.getSession().getAttribute(COLLECTION_ITEM_PASTE)) != null && clipboard.length() > 0) {
                String pastePath = clipboard.substring(0, clipboard.indexOf(";"));
                pasteButton = pastePath.equals(fieldName);
            }
            String type = propertyDefinition.getType();
            String fromNode = propertylist.getPropertyTreeNodeId(propertyid);
            if (options.showDebug) {
                if (type.equals("simple") && fromNode != null && fromNode.length() > 0) {
                    output.append(fieldName + " (from " + fromNode + ")<br>");
                }
                Trace.log("EDITORUTIL", "DISPLAY EDITOR - Property:" + propertyid + ", Editor: " + editorName + ", Type: " + type);
            }
            if (type.equals("collection")) {
                try {
                    PropertyDefinitionList propertyDefinitionList = propertyDefinition.getPropertyDefinitionList();
                    String titlepropertyid = propertyDefinitionList.getTitlePropertyId();
                    PropertyListCollection collection = propertylist.getCollection(propertyid);
                    if (collection == null) {
                        collection = new PropertyListCollection();
                    }
                    collection.setFieldName(fieldName);
                    if (options.showDebug) {
                        Trace.log("EDITORUTIL", "COLLECTION EDITOR: " + collection.size() + " nodes found");
                    }
                    String labelsingular = propertyDefinitionList.getLabelSingular();
                    String labelplural = propertyDefinitionList.getLabelPlural();
                    if (labelsingular.equals("")) {
                        labelsingular = "item";
                    }
                    if (labelplural.equals("")) {
                        labelplural = labelsingular + "s";
                    }
                    boolean allowroles = propertyDefinitionList.isAllowRoles();
                    boolean appRolesOnly = propertyDefinitionList.isAppRolesOnly();
                    output.append("<input type=\"hidden\" id=\"" + fieldName + "__SELECTEDINDEX\" name=\"" + fieldName + "__SELECTEDINDEX\">");
                    output.append("<input type=\"hidden\" name=\"" + fieldName + "__PROPERTYLISTCOUNT\" value=\"" + collection.size() + "\" />");
                    if (options.showAdvanced || !advanced) {
                        String direction;
                        String string = direction = propertyDefinitionList.getDirecttion().length() > 0 ? propertyDefinitionList.getDirecttion() : DIRECTION_GROUPED;
                        if (direction.equals(DIRECTION_GROUPED)) {
                            int i;
                            boolean collapse = collection.size() > 15;
                            output.append("<table cellpadding=2 cellspacing=0 border=0><tr valign=\"top\"><td>");
                            if (collapse) {
                                output.append("<table cellpadding=0 cellspacing=0 border=0 style='width:100%;font-size:10px;color:#666;background:#efefef;border:1px solid #ccc;'><tr>");
                                output.append("<td style='padding:2px;width:100px;'>");
                                output.append("<input style='border:1px solid #ccc;padding:2px;width:100px;' onkeyup=\"lvEGCollectionSearch( this, '").append(fieldName).append("' );\" id=\"__filter_").append(fieldName).append("\">");
                                output.append("</td>");
                                output.append("<td style='padding:2px;display:none;color:#999;cursor:pointer;' onclick=\"lvEGCollectionSearchReset('").append(fieldName).append("');\"");
                                output.append("onmouseover=\"this.style.color='blue'\" onmouseout=\"this.style.color='#999'\" id='__span_").append(fieldName).append("' title='Reset Search'>");
                                output.append("[X]</td>");
                                output.append("<td style='text-align:right;padding:2px;cursor:pointer;' onclick=\"lvEGCollectionShowAll(this, '").append(fieldName).append("')\">Show All (").append(collection.size()).append(")</td>");
                                output.append("</tr></table>");
                                output.append("<div style='height:360px;overflow-y:auto;overflow-x:hidden;' id='__").append(fieldName).append("_collectionheader'>");
                            }
                            output.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"2\" id=\"__table_").append(fieldName).append("\" style='width:100%' twidth='0'>");
                            ArrayList<StringBuffer> propertylists = new ArrayList<StringBuffer>();
                            for (i = 0; i < collection.size(); ++i) {
                                propertylists.add(EditorUtil.getPropertyList(fieldName, i, collection, propertyDefinitionList, nodeid, toppropertylist, pageContext, "style=\"display: none\"", options, true));
                            }
                            for (int collectionItemCounter = 0; collectionItemCounter < collection.size(); ++collectionItemCounter) {
                                if (collapse) {
                                    PropertyList subpropertylist = (PropertyList)collection.get(collectionItemCounter);
                                    String id = subpropertylist.getProperty("id", "");
                                    String title = "";
                                    if (titlepropertyid.length() > 0) {
                                        title = subpropertylist.getProperty(titlepropertyid);
                                    }
                                    if (title.length() == 0) {
                                        title = labelsingular;
                                    }
                                    output.append("<tr>");
                                    output.append("<td style='display:none' class='searchdata'>");
                                    output.append(HttpUtil.htmlEncode(title).toLowerCase());
                                    if (!id.equals(title)) {
                                        output.append(" ").append(id.toLowerCase());
                                    }
                                    output.append(" ").append(HttpUtil.htmlEncode(subpropertylist.getProperty("columnid")).toLowerCase());
                                    output.append("</td>");
                                    output.append("<td style='width:16px;'><img style=\"display: none\" id=\"").append(fieldName).append("_").append(collectionItemCounter).append("__PROPERTYLISTPOINTER\" src=\"WEB-CORE/elements/images/pointer.gif\"></td>");
                                    output.append("<td align=right style='color:#666;width:20px;'>").append(collectionItemCounter + 1).append("</td>");
                                } else {
                                    output.append("<tr>");
                                    output.append("<td><img style=\"display: none\" id=\"").append(fieldName).append("_").append(collectionItemCounter).append("__PROPERTYLISTPOINTER\" src=\"WEB-CORE/elements/images/pointer.gif\"></td>");
                                }
                                output.append("<td width='*'>");
                                output.append(EditorUtil.getHeader(fieldName, collectionItemCounter, collection, propertyDefinitionList, nodeid, allowroles, appRolesOnly, labelsingular, "width:100%", options, pasteButton));
                                output.append("</td>");
                                output.append("</tr>");
                            }
                            output.append("</table>");
                            if (collapse) {
                                output.append("</div>");
                            }
                            output.append("<table cellspacing=\"0\" cellpadding=\"0\" style='padding:4px;vertical-align:top'><tr>");
                            if (!options.readonly && !options.lockAll) {
                                output.append(EditorUtil.getButtons(pageContext, fieldName, labelsingular, propertyDefinitionList.getAddMethod(), toppropertylist, propertylist, direction, collection.size() > 0, options.collectionitemcopy, pasteButton, tp, browser));
                            }
                            output.append("</tr></table>");
                            output.append("</td><td>");
                            for (i = 0; i < collection.size(); ++i) {
                                output.append((StringBuffer)propertylists.get(i));
                            }
                            output.append("</td></tr></table>");
                            PropertyValue pv = propertylist.getPropertyValue(propertyid);
                            int selectedindex = -1;
                            if (pv != null && pv.getAttribute("selectedindex").length() > 0) {
                                selectedindex = Integer.parseInt(pv.getAttribute("selectedindex"));
                            }
                            output.append("<script>");
                            output.append("var last__" + fieldName + ";");
                            output.append("function display__" + fieldName + "( index ) {");
                            output.append("  if ( last__" + fieldName + " != null ) {");
                            output.append("    document.getElementById( '" + fieldName + "_' + last__" + fieldName + " + '__PROPERTYLIST' ).style.display = 'none';");
                            output.append("    document.getElementById( '" + fieldName + "_' + last__" + fieldName + " + '__PROPERTYLISTPOINTER' ).style.display = 'none';");
                            output.append("  }");
                            output.append("  document.getElementById( '" + fieldName + "_' + index + '__PROPERTYLIST' ).style.display = 'block';");
                            output.append("  document.getElementById( '" + fieldName + "_' + index + '__PROPERTYLISTPOINTER' ).style.display = 'block';");
                            output.append("  document.getElementById( '" + fieldName + "__SELECTEDINDEX' ).value = index;");
                            output.append("  last__" + fieldName + " = index;");
                            output.append("}");
                            if (selectedindex == -1 && collection.size() > 0) {
                                selectedindex = 0;
                            }
                            if (selectedindex > collection.size() - 1) {
                                selectedindex = collection.size() - 1;
                            }
                            if (selectedindex >= 0) {
                                output.append("display__" + fieldName + "( " + selectedindex + ");");
                            }
                            output.append("</script>");
                        }
                        if (direction.equals(DIRECTION_ACROSS)) {
                            output.append("<table><tr valign=\"top\"><td>");
                            output.append("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\"><tr valign=\"top\">");
                            for (int collectionItemCounter = 0; collectionItemCounter < collection.size(); ++collectionItemCounter) {
                                output.append("<td>");
                                output.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
                                StringBuffer propertyList = EditorUtil.getPropertyList(fieldName, collectionItemCounter, collection, propertyDefinitionList, nodeid, toppropertylist, pageContext, "", options, true);
                                output.append(EditorUtil.getHeader(fieldName, collectionItemCounter, collection, propertyDefinitionList, nodeid, allowroles, appRolesOnly, labelsingular, "min-width:170px", options, pasteButton));
                                output.append("</td><td></td></tr><tr><td colspan=\"2\">");
                                output.append(propertyList);
                                output.append("</td></tr></table>");
                                output.append("</td>");
                            }
                            output.append("</tr><tr><td>");
                            if (!options.readonly && !options.lockAll) {
                                output.append("<table cellspacing=\"0\" cellpadding=\"0\" style='vertical-align:top'><tr>");
                                output.append(EditorUtil.getButtons(pageContext, fieldName, labelsingular, propertyDefinitionList.getAddMethod(), toppropertylist, propertylist, direction, collection.size() > 0, options.collectionitemcopy, pasteButton, tp, browser));
                                output.append("</tr></table>");
                            }
                            output.append("</td></tr></table>");
                            output.append("</td></tr></table>");
                        }
                        output.append("<table><tr valign=\"top\"><td>");
                        output.append("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\">");
                        for (int collectionItemCounter = 0; collectionItemCounter < collection.size(); ++collectionItemCounter) {
                            output.append("<tr><td>");
                            output.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
                            StringBuffer propertyList = EditorUtil.getPropertyList(fieldName, collectionItemCounter, collection, propertyDefinitionList, nodeid, toppropertylist, pageContext, "", options, true);
                            output.append(EditorUtil.getHeader(fieldName, collectionItemCounter, collection, propertyDefinitionList, nodeid, allowroles, appRolesOnly, labelsingular, "min-width:170px", options, pasteButton));
                            output.append("</td><td></td></tr><tr><td colspan=\"2\">");
                            output.append(propertyList);
                            output.append("</td></tr></table>");
                            output.append("</td></tr>");
                        }
                        output.append("<tr><td>");
                        if (!options.readonly && !options.lockAll) {
                            output.append("<table cellspacing=\"0\" cellpadding=\"0\" style='vertical-align:top'><tr>");
                            output.append(EditorUtil.getButtons(pageContext, fieldName, labelsingular, propertyDefinitionList.getAddMethod(), toppropertylist, propertylist, direction, collection.size() > 0, options.collectionitemcopy, pasteButton, tp, browser));
                            output.append("</tr></table>");
                        }
                        output.append("</td></tr></table>");
                        output.append("</td></tr></table>");
                    }
                    for (int i = 0; i < collection.size(); ++i) {
                        output.append(EditorUtil.getPropertyList(fieldName, i, collection, propertyDefinitionList, nodeid, toppropertylist, pageContext, "", options, false));
                    }
                }
                catch (Exception e) {
                    Logger.logError("EDITORUTIL", "ERROR: DISPLAY EDITOR: Unable to diplay Collection Editor ('" + editorName + "') for property " + propertyid + ": " + e.getMessage(), e);
                    output.append("<p style=\"color:red\">Exception: Unable to diplay Editor</p>");
                }
            } else if (type.equals("propertylist")) {
                try {
                    PropertyDefinitionList propertyDefinitionList = propertyDefinition.getPropertyDefinitionList();
                    String subpropertylistid = "";
                    PropertyList subpropertylist = propertylist.getPropertyList(propertyid);
                    if (subpropertylist == null) {
                        subpropertylist = new PropertyList();
                        subpropertylist.setUsePropertyValues(true);
                        subpropertylistid = parentid + "_0";
                    } else {
                        subpropertylistid = subpropertylist.getId();
                    }
                    PropertyListEditor editor = new PropertyListEditor();
                    if (editor == null) {
                        output.append("Property List Editor not found");
                        break block82;
                    }
                    if (options.showDebug) {
                        output.append("propertylistid=" + subpropertylistid + " (from " + fromNode + ")");
                    }
                    output.append("<input type=\"hidden\" name=\"" + fieldName + "_0__PROPERTYLISTID\" value=\"" + subpropertylistid + "\" />");
                    output.append("<input type=\"hidden\" name=\"" + fieldName + "_0__ANCESTOR\" value=\"N\" />");
                    if (options.showAdvanced || !advanced) {
                        output.append("<table cellpadding=\"0\" border=\"1\" style=\"margin: -2px; " + propertyDefinitionList.getTableStyle() + "\" cellspacing=\"0\">");
                    }
                    try {
                        output.append(editor.getEditor(nodeid, propertyDefinitionList, subpropertylist, toppropertylist, fieldName, 0, pageContext, options)).append("\n");
                    }
                    catch (Exception e) {
                        output.append("<tr><td>Editor " + editorName + " not found</td></tr>");
                        Trace.log("EDITORUTIL", "ERROR: DISPLAY EDITOR: Unable to diplay PropertyList Editor ('" + editorName + "') for property " + propertyid + ": " + e.getMessage());
                    }
                    if (options.showAdvanced || !advanced) {
                        output.append("</table>");
                    }
                }
                catch (Exception e) {
                    Trace.log("EDITORUTIL", "ERROR: DISPLAY EDITOR: Unable to diplay PropertyList Editor ('" + editorName + "') for property " + propertyid + ": " + e.getMessage());
                    output.append("Editor not found");
                }
            } else {
                String guimodeExpression;
                boolean ancestor = false;
                PropertyValue propertyValue = propertylist.getPropertyValue(propertyid);
                boolean locked = "Y".equals(propertyValue.getAttribute("locked"));
                boolean expressionable = propertyDefinition.isExpression();
                boolean hasExpression = expressionable && propertyValue.value.startsWith(GROOVY_PREFIX) && propertyValue.value.endsWith("}");
                String expression = hasExpression ? propertyValue.value.substring(GROOVY_PREFIX.length(), propertyValue.value.length() - "}".length()) : "";
                expression = DOMUtil.convertChars(expression);
                boolean isPassword = "Y".equals(propertyDefinition.getAttributes().get("password"));
                boolean resolutionable = propertyDefinition.isResolution();
                boolean hasGUIMode = resolutionable && propertyValue.value.startsWith(GUIMODE_PREFIX) && propertyValue.value.endsWith("}") && propertyValue.value.contains(":|:");
                String string = guimodeExpression = hasGUIMode ? propertyValue.value.substring(GUIMODE_PREFIX.length(), propertyValue.value.length() - "}".length()) : "";
                if (propertylist.getPropertyTreeNodeId(propertyid) == null || !nodeid.equals(propertylist.getPropertyTreeNodeId(propertyid))) {
                    if (!(propertyValue == null || propertyValue.value.length() <= 0 || propertyValue.value.startsWith(ANCESTOR_PREFIX) && propertyValue.value.endsWith(ANCESTOR_SUFFIX))) {
                        propertyValue.value = ANCESTOR_PREFIX + propertyValue.value + ANCESTOR_SUFFIX;
                    }
                    if (propertyValue != null && propertyValue.value.length() > 0 && propertyValue.value.startsWith(ANCESTOR_PREFIX) && propertyValue.value.endsWith(ANCESTOR_SUFFIX)) {
                        ancestor = true;
                    }
                }
                if (options.readonly) {
                    if (options.showAdvanced || !advanced) {
                        output.append("<span style=\"width: 200px " + (ancestor ? "; color:blue" : "") + "\">");
                        if (options.showExportFlag && propertyValue.value.length() > 0) {
                            output.append("<input simple=\"Y\" name=\"exportcheckbox\" id=\"EXPORT__" + fieldName + "\" type=\"checkbox\" onclick=\"simpleClicked( this );\" >");
                        }
                        if (!propertyValue.value.contains("<") || propertyValue.value.contains(">")) {
                            // empty if block
                        }
                        output.append(propertyValue.value.equals("") ? "&nbsp;" : HttpUtil.htmlEncode(isPassword ? "*************" : propertyValue.value));
                        output.append("</span>");
                    }
                } else if (!options.showAdvanced && advanced) {
                    if (isPassword && propertyValue.value.length() > 0) {
                        String hashcode = "HEX_" + DigestUtils.md5Hex((String)propertyValue.value);
                        pageContext.getSession().setAttribute(hashcode, (Object)propertyValue.value);
                        propertyValue.value = hashcode;
                    }
                    propertyValue.value = DOMUtil.convertChars(propertyValue.value);
                    output.append("<input type=\"hidden\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" value=\"" + propertyValue + "\"/>");
                    output.append("<input type=\"hidden\" name=\"" + fieldName + "__LOCKED\" value=\"" + (locked ? "Y" : "") + "\">");
                } else if (locked || options.lockAll) {
                    if (isPassword && propertyValue.value.length() > 0) {
                        String hashcode = "HEX_" + DigestUtils.md5Hex((String)propertyValue.value);
                        pageContext.getSession().setAttribute(hashcode, (Object)propertyValue.value);
                        propertyValue.value = hashcode;
                    }
                    propertyValue.value = DOMUtil.convertChars(propertyValue.value);
                    output.append("<input type=\"hidden\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" value=\"" + propertyValue + "\"/>");
                    output.append("<input type=\"hidden\" name=\"" + fieldName + "__LOCKED\" value=\"" + (locked ? "Y" : "") + "\">");
                    output.append("<div style=\"padding-top:3px;padding-left:4px;font-style:italic;white-space:nowrap;color:darkgray\" >Locked (" + (isPassword ? "*************" : propertyValue) + ")</div>");
                } else {
                    String transContext;
                    HashMap attributes;
                    TypeSimple editor = null;
                    try {
                        Class<?> c = Class.forName("com.labvantage.sapphire.admin.propertytree." + editorName);
                        editor = (TypeSimple)c.newInstance();
                    }
                    catch (Exception e1) {
                        try {
                            Class<?> c = Class.forName(editorName);
                            editor = (TypeSimple)c.newInstance();
                        }
                        catch (Exception e2) {
                            Trace.log("EDITORUTIL", "ERROR: DISPLAY EDITOR: Unable to diplay SimpleEditor ('" + editorName + "') for property " + propertyid);
                        }
                    }
                    if (editor == null) {
                        output.append("Editor not found");
                    } else {
                        if (hasExpression) {
                            propertyValue.value = "";
                        }
                        attributes = propertyDefinition.getAttributes();
                        attributes.put("nodeid", nodeid);
                        if (hasGUIMode && !hasExpression) {
                            ArrayList<Browser.GUIMode> modes = browser.getGUIModes();
                            output.append("<input id=\"" + fieldName + "\" name=\"" + fieldName + "\" type=\"hidden\" value=\"$R{" + guimodeExpression + "}\" />\n");
                            output.append("<table style=\"display:inline\" cellspacing=\"0\" cellpadding=\"0\">");
                            attributes.put("pastevalue", "N");
                            for (Browser.GUIMode mode : modes) {
                                String modeid = mode.getId();
                                String image = mode.getImageRef();
                                String value = EditorUtil.getGUIModeValue(guimodeExpression, modeid);
                                String string2 = propertyValue.value = value == null ? "" : value;
                                if (ancestor && propertyValue.value.length() > 0) {
                                    propertyValue.value = ANCESTOR_PREFIX + propertyValue.value + ANCESTOR_SUFFIX;
                                }
                                output.append("<tr><td align=\"center\"><img src=\"rc?command=image&image=" + image + "\" /></td><td>" + editor.getEditor(fieldName + "_" + modeid, propertyValue, toppropertylist, ancestor, attributes, pageContext, options.showDebug)).append("</td></tr>\n");
                            }
                            output.append("</table>");
                        } else {
                            output.append(editor.getEditor(fieldName, propertyValue, toppropertylist, ancestor, attributes, pageContext, options.showDebug)).append("\n");
                        }
                        String warningMessage = (String)attributes.get("__showwarning");
                        if (warningMessage != null && warningMessage.length() > 0) {
                            options.setPropertyListHasError(parentid + "_" + propertylistindex, warningMessage);
                        }
                    }
                    if (expressionable && !hasGUIMode) {
                        attributes = propertyDefinition.getAttributes();
                        String variablecode = "";
                        try {
                            variablecode = (String)attributes.get("groovyvariablecode");
                        }
                        catch (Exception title) {
                            // empty catch block
                        }
                        String variablelist = GroovyBindVariableRegister.getVariables(variablecode);
                        if (variablelist.indexOf("primary[columns]") >= 0) {
                            StringBuilder primarycolumns = new StringBuilder();
                            if (toppropertylist.getCollection("columns") != null) {
                                PropertyListCollection columns = toppropertylist.getCollection("columns");
                                for (int i = 0; i < columns.size(); ++i) {
                                    String columnid = columns.getPropertyList(i).getProperty("columnid");
                                    if (columnid.length() <= 0) continue;
                                    if (columnid.lastIndexOf(" ") > 0) {
                                        columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
                                    }
                                    primarycolumns.append(";primary." + columnid);
                                }
                            }
                            variablelist = StringUtil.replaceAll(variablelist, "primary[columns]", primarycolumns.substring(1));
                        }
                        String image = hasExpression ? (ancestor ? "WEB-CORE/modules/webadmin/images/ancestorexpression.gif" : "WEB-CORE/modules/webadmin/images/editexpression.gif") : "WEB-CORE/modules/webadmin/images/addexpression.gif";
                        output.append("&nbsp;&nbsp;<img id=\"" + fieldName + "__EXPICON\" onclick=\"if(typeof(sapphire)!='undefined')sapphire.ui.dialog.open('" + tp.translate("Expression Editor") + "','rc?command=file&file=WEB-CORE/pagetypes/actionblock/scripteditor.jsp&variablelist=" + variablelist + "&mode=groovy&fieldid=" + fieldName + "__EXPRESSION&scripttype=groovynoaction&sdcid=" + toppropertylist.getProperty("sdcid") + "', true, 800, 500);\" );\" style=\"cursor: pointer\" src=\"" + image + "\">");
                        String displayExpression = expression.length() < 100 ? expression : expression.substring(0, 99) + "...";
                        output.append("&nbsp;<span id=\"" + fieldName + "__EXPDISPLAY\" style=\"color:gray;font-style: italic\">" + displayExpression + "</span>");
                        output.append("<textarea onchange=\"document.getElementById( '" + fieldName + "__EXPDISPLAY' ).innerText=(this.value.length < 100 ? this.value : this.value.substring(0,99)+'...');propertyChange();document.getElementById( '" + fieldName + "__EXPRESSIONANCESTOR' ).value='N';if ( this.value.length > 0 ) document.getElementById( '" + fieldName + "' ).value='';document.getElementById( '" + fieldName + "__EXPICON' ).src='WEB-CORE/modules/webadmin/images/' + ( this.value.length>0?'editexpression.gif':'addexpression.gif');\" style=\"display: none\" name=\"" + fieldName + "__EXPRESSION\" id=\"" + fieldName + "__EXPRESSION\">" + (ancestor ? ANCESTOR_PREFIX : "") + expression + (ancestor ? ANCESTOR_SUFFIX : "") + "</textarea>");
                        output.append("<input type=\"text\" style=\"display: none\" name=\"" + fieldName + "__EXPRESSIONANCESTOR\" id=\"" + fieldName + "__EXPRESSIONANCESTOR\" value=\"" + (ancestor ? "Y" : "N") + "\"/>");
                    }
                    if (resolutionable && (!hasExpression || ancestor)) {
                        output.append("<input id=\"" + fieldName + "__RES\" name=\"" + fieldName + "__RES\" type=\"hidden\" value=\"" + (hasGUIMode ? "Y" : "N") + "\" />\n");
                        String image = hasGUIMode ? "WEB-CORE/modules/webadmin/images/deleteresolution.gif" : "WEB-CORE/modules/webadmin/images/addresolution.gif";
                        output.append("&nbsp;&nbsp;<img style=\"vertical-align:top;margin-top:2px\" id=\"" + fieldName + "__RESICON\" ");
                        output.append(" onclick=\"document.getElementById( '" + fieldName + "__RES' ).value='" + (hasGUIMode ? "N" : "Y") + "';doCommand('save')\"");
                        output.append(" style=\"cursor: pointer\" src=\"" + image + "\">");
                    }
                    String string3 = transContext = "Y".equals(propertyValue.getAttribute("translate")) ? "W" : propertyValue.getAttribute("translate");
                    if (transContext.length() > 0 && propertyValue.toString().length() > 0) {
                        SDITagUtil.getTranslateIcon("", transContext, fieldName, pageContext);
                        output.append(SDITagUtil.getTranslateIcon("", transContext, fieldName, pageContext));
                        output.append("(" + transContext + ")");
                    }
                    output.append("<input name=\"" + fieldName + "__LOCKED\" type=\"hidden\" value=\"" + (locked ? "Y" : "") + "\">");
                }
            }
        }
        options.cascadeAdvanced = rememberCascadeAdvanced;
        return output.toString();
    }

    public static String getGUIModeValue(String value, String modeid) {
        String returnValue = "";
        if (value != null && modeid.length() > 0 && value.contains(":|:")) {
            String[] parts = StringUtil.split(value, ":|:");
            for (int i = 0; i < parts.length; ++i) {
                if (!parts[i].startsWith(modeid + ":")) continue;
                returnValue = parts[i].substring(modeid.length() + 1);
            }
        }
        return returnValue;
    }

    private static StringBuffer getHeader(String fieldname, int collectionItemCounter, PropertyListCollection collection, PropertyDefinitionList propertyDefinitionList, String nodeid, boolean allowroles, boolean appRolesOnly, String labelsingular, String extraStyle, PropertyTreeDisplayOptions options, boolean pasteButton) {
        String propertyListError;
        boolean warn;
        StringBuffer output = new StringBuffer();
        PropertyList subpropertylist = (PropertyList)collection.get(collectionItemCounter);
        String uniqueIdPropertyid = propertyDefinitionList.getUniqueIdPropertyId();
        if (uniqueIdPropertyid.isEmpty()) {
            for (Object o : propertyDefinitionList) {
                PropertyDefinition propertyDef = (PropertyDefinition)o;
                if (!propertyDef.getEditor().equalsIgnoreCase("IdEditor")) continue;
                uniqueIdPropertyid = propertyDef.getId();
            }
        }
        boolean bl = warn = (propertyListError = options.getPropertyListError(fieldname + "_" + collectionItemCounter)) != null && propertyListError.length() > 0;
        if (uniqueIdPropertyid.length() > 0) {
            String currrentid = subpropertylist.getProperty(uniqueIdPropertyid);
            if (currrentid.length() == 0) {
                warn = true;
                propertyListError = "Missing id property value";
            } else {
                for (int j = 0; j < collection.size() && !warn; ++j) {
                    if (j == collectionItemCounter) continue;
                    PropertyList item = collection.getPropertyList(j);
                    warn = item.getProperty(uniqueIdPropertyid).equals(currrentid);
                    propertyListError = "Duplication id property value";
                }
            }
        }
        boolean ancestor = subpropertylist.getPropertyTreeNodeId() == null || !subpropertylist.getPropertyTreeNodeId().equals(nodeid);
        String titlePropertyid = propertyDefinitionList.getTitlePropertyId();
        String title = "";
        String tip = "";
        if (titlePropertyid.length() > 0) {
            int j;
            String[] parts;
            if (titlePropertyid.contains(" AND ")) {
                parts = StringUtil.split(titlePropertyid, " AND ");
                for (j = 0; j < parts.length; ++j) {
                    String value = subpropertylist.getProperty(parts[j].trim()).trim();
                    if (value.length() <= 0) continue;
                    title = title + (title.length() == 0 ? value : " (" + value + ")");
                }
            } else if (titlePropertyid.contains(" OR ")) {
                parts = StringUtil.split(titlePropertyid, " OR ");
                for (j = 0; j < parts.length; ++j) {
                    if (title.trim().length() != 0) continue;
                    title = subpropertylist.getProperty(parts[j].trim()).trim();
                }
            } else {
                title = subpropertylist.getProperty(titlePropertyid.contains(";") ? titlePropertyid.substring(0, titlePropertyid.indexOf(";")) : titlePropertyid);
            }
        }
        if (title.indexOf("<") >= 0) {
            title = "???";
        }
        if (title.length() == 0) {
            title = labelsingular + "_" + (collectionItemCounter + 1);
        }
        tip = title;
        if (title.length() > 30) {
            title = title.substring(0, 29) + "...";
        }
        boolean starred = EditorUtil.getPropertyBoolean(subpropertylist, propertyDefinitionList.getFlaggedPropertyId());
        boolean hidden = EditorUtil.getPropertyBoolean(subpropertylist, propertyDefinitionList.getHiddenPropertyId());
        if (starred) {
            title = title + " *";
        }
        if (warn) {
            title = title + " <img src=\"WEB-CORE/images/warning.gif\" title=\"" + propertyListError + "\">";
        }
        String subpropertylistid = subpropertylist.getId();
        output.append("<table style=\"" + extraStyle + (hidden ? ";text-decoration:line-through " + (ancestor ? "grey" : "") : "") + "\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id='__" + collectionItemCounter + "_" + fieldname + "_container'>");
        output.append("<td id=\"" + fieldname + "_" + collectionItemCounter + "__PROPERTYLISTTITLE\" style=\"border: 1px solid black\" class=\"propertylisttitle" + (ancestor ? "ancestor" : "") + "\">");
        output.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\"><tr>");
        output.append("<td title=\"" + tip + "\" width=\"100%\" nowrap id=\"" + fieldname + "_" + collectionItemCounter + "__PROPERTYLISTTITLETEXT\" class=\"propertylisttitle" + (ancestor ? "ancestor" : "") + "\"" + (DIRECTION_GROUPED.equals(propertyDefinitionList.getDirecttion()) || propertyDefinitionList.getDirecttion().length() == 0 ? " onclick=\"display__" + fieldname + "( " + collectionItemCounter + ")\" style=\"cursor: pointer\"" : "") + " align=\"left\">");
        if (options.showExportFlag) {
            output.append("<span align=\"right\">");
            output.append(" <input name=\"exportcheckbox\" id=\"EXPORT__" + fieldname + "_" + collectionItemCounter + "\" onclick=\"collectionItemClicked( this );\" type=\"checkbox\" >");
            output.append("</span>");
        } else if (!options.readonly && !options.lockAll) {
            output.append("<span align=\"right\">");
            String id = fieldname + "_" + collectionItemCounter + "_ITEMSELECTOR";
            output.append(" <input name=\"" + id + "\" id=\"" + id + "\" " + (pasteButton ? "onclick=\"togglePaste('" + fieldname + "')\"" : "") + " ancestor=\"" + (ancestor ? "Y" : "N") + "\" type=\"checkbox\" >");
            output.append("</span>");
        }
        output.append("&nbsp;" + title + "&nbsp;");
        if (options.showPropertylistids) {
            if (ancestor) {
                output.append("<br>&nbsp;(" + subpropertylistid + ")");
            } else {
                output.append("<br>&nbsp;<input name=\"" + fieldname + "_" + collectionItemCounter + "__NEWPROPERTYLISTID\" type=\"text\" style=\"background-color: lemonchiffon; font-weight: bold; border: 1px solid black; \" value=\"" + subpropertylistid + "\">");
            }
        }
        output.append("</td>");
        if (!options.readonly) {
            output.append("<td nowrap style=\"font-weight:  bold\" align=\"right\">");
            output.append("</td><td>");
            output.append("<td nowrap width=\"1%\" align=\"right\">");
            if (!options.lockAll) {
                if (!ancestor) {
                    // empty if block
                }
            } else {
                output.append("&nbsp;<img style=\"margin: 1\" src=\"WEB-CORE/elements/images/blankclose.gif\">");
            }
            output.append("</td>");
            if (!options.lockAll && allowroles) {
                String roleColor;
                String rolelist = subpropertylist.getAttribute("rolelist");
                String modulelist = subpropertylist.getAttribute("modulelist");
                String previousRoleList = subpropertylist.getPreviousRoleList();
                String previousModuleList = subpropertylist.getPreviousModuleList();
                String roleListNodeid = subpropertylist.getRoleListNodeid();
                String moduleListNodeid = subpropertylist.getModuleListNodeid();
                if (previousRoleList == null) {
                    previousRoleList = "";
                }
                if (previousModuleList == null) {
                    previousModuleList = "";
                }
                if (roleListNodeid == null) {
                    roleListNodeid = "";
                }
                if (moduleListNodeid == null) {
                    moduleListNodeid = "";
                }
                String string = rolelist.length() == 0 ? "" : (roleColor = roleListNodeid.equals(nodeid) ? "red" : "blue");
                String moduleColor = modulelist.length() == 0 ? "" : (moduleListNodeid.equals(nodeid) ? "red" : "blue");
                output.append("<input type=\"hidden\" name=\"" + fieldname + "_" + collectionItemCounter + "__MODULES\" id=\"" + fieldname + "_" + collectionItemCounter + "__MODULES\" value=\"" + modulelist + "\">");
                output.append("<input type=\"hidden\" name=\"" + fieldname + "_" + collectionItemCounter + "__ROLES\" id=\"" + fieldname + "_" + collectionItemCounter + "__ROLES\" value=\"" + rolelist + "\">");
                output.append("<td width=\u0001%\"><a href='' onClick='showRolesByFormPost( \"" + fieldname + "_" + collectionItemCounter + "\", \"" + rolelist + "\", " + (options.appRolesOnly || appRolesOnly) + " );sapphire.events.cancelEvent(event, false);'><img style='border-color: black; border-width: 1px' " + (rolelist.length() > 0 ? " title=\"" + rolelist + "\"" : "") + " src='WEB-CORE/elements/images/padlock" + roleColor + ".gif'></a></td>");
                if (options.showModules) {
                    output.append("<td width=\u0001%\"><a href='' onClick='showModules( \"" + fieldname + "_" + collectionItemCounter + "\", \"" + modulelist + "\" );sapphire.events.cancelEvent(event, false);'><img style='border-color: black; border-width: 1px' " + (modulelist.length() > 0 ? " title=\"" + modulelist + "\"" : "") + " src='WEB-CORE/elements/images/module" + moduleColor + ".gif'></a></td>");
                }
            }
        }
        output.append("</tr></table></td></tr></table>");
        return output;
    }

    private static boolean getPropertyBoolean(PropertyList subpropertylist, String hiddenPropertyId) {
        String[] parts;
        boolean hidden = false;
        String hiddenPropertyid = hiddenPropertyId;
        if (hiddenPropertyid.length() > 0 && (parts = StringUtil.split(hiddenPropertyid, "=")).length == 2) {
            String value = subpropertylist.getProperty(parts[0]);
            String targetvalue = parts[1];
            if (targetvalue.equals("*") && value.length() > 0) {
                hidden = true;
            } else if (targetvalue.equals("!*") && value.length() == 0) {
                hidden = true;
            } else if (value.length() > 0 && value.equals(targetvalue)) {
                hidden = true;
            }
        }
        return hidden;
    }

    private static StringBuffer getButtons(PageContext pageContext, String fieldname, String labelsingular, String addMethod, PropertyList topPropertyList, PropertyList propertylist, String direction, boolean full, boolean showCopy, boolean showPaste, TranslationProcessor tp, Browser browser) {
        boolean vertical;
        StringBuffer output = new StringBuffer();
        Button add = new Button(pageContext);
        if (addMethod != null && addMethod.length() > 0) {
            String[] parts = StringUtil.split(addMethod, ";");
            String requestParams = "";
            for (int i = 1; i < parts.length; ++i) {
                String part = parts[i].trim();
                if (part.indexOf("=") == -1) {
                    String value = topPropertyList.getProperty(part);
                    if (value.length() == 0) {
                        value = propertylist.getProperty(part);
                    }
                    if (value.length() <= 0) continue;
                    requestParams = requestParams + "&" + part + "=" + value;
                    continue;
                }
                String[] argVal = StringUtil.split(part, "=");
                int idx1 = argVal[1].indexOf("[");
                int idx2 = argVal[1].indexOf(VARIABLES_SUFFIX);
                if (idx1 > -1 && idx2 > -1) {
                    String property = argVal[1].substring(idx1 + 1, idx2);
                    String value = topPropertyList.getProperty(property);
                    if (value.length() == 0) {
                        value = propertylist.getProperty(property);
                    }
                    if (value.length() <= 0) continue;
                    part = StringUtil.replaceAll(part, "[" + property + VARIABLES_SUFFIX, value);
                    requestParams = requestParams + "&" + part;
                    continue;
                }
                requestParams = requestParams + "&" + part;
            }
            add.setAction(parts[0] + "( '" + fieldname + "', '" + requestParams + "', event );");
        } else {
            add.setAction("addPropertyList( 'addpropertylist', '" + fieldname + "', event)");
            add.setTip("(Ctrl-click for options)");
        }
        if (direction.equals(DIRECTION_GROUPED)) {
            add.setText(tp != null ? tp.translate("Add...") : "Add...");
        } else {
            add.setText(tp != null ? tp.translate("Add " + labelsingular + "...") : "Add " + labelsingular + "...");
        }
        add.setId(fieldname + "__ADDBUTTON");
        output.append("<td>" + add.getHtml() + "</td>");
        boolean bl = vertical = !direction.equals(DIRECTION_ACROSS);
        if (full) {
            Button moveback = new Button(pageContext);
            moveback.setImg(vertical ? "WEB-CORE/images/gif/MoveUp.gif" : "WEB-CORE/images/gif/MoveLeft.gif");
            moveback.setTip("Move selected columns backwards. (Ctrl-click for options)");
            moveback.setAction("movePropertyList( 'moveback', '" + fieldname + "', event );");
            if (!browser.isIE()) {
                moveback.setStyle("height:25px");
            }
            output.append("<td>" + moveback.getHtml() + "</td>");
            Button moveForward = new Button(pageContext);
            moveForward.setImg(vertical ? "WEB-CORE/images/gif/MoveDown.gif" : "WEB-CORE/images/gif/MoveRight.gif");
            moveForward.setAction("movePropertyList( 'moveforward', '" + fieldname + "', event );");
            moveForward.setTip("Move selected columns forward. (Ctrl-click for options)");
            if (!browser.isIE()) {
                moveForward.setStyle("height:25px");
            }
            output.append("<td>" + moveForward.getHtml() + "</td>");
            Button delete = new Button(pageContext);
            delete.setImg("WEB-CORE/images/gif/Delete.gif");
            delete.setTip("Delete the selected items");
            if (!browser.isIE()) {
                delete.setStyle("height:25px");
            }
            delete.setAction("deletePropertyList( '" + fieldname + "', '" + labelsingular + "' );");
            output.append("<td>" + delete.getHtml() + "</td>");
            if (showCopy) {
                Button copy = new Button(pageContext);
                copy.setImg("WEB-CORE/images/gif/Copy.gif");
                copy.setTip("Copy the selected items to the clipboard");
                copy.setAction("copyPropertyList( '" + fieldname + "' );");
                if (showPaste) {
                    copy.setId("hiddencopybutton");
                    copy.setStyle("display: none");
                }
                if (!browser.isIE()) {
                    copy.setStyle("height:25px");
                }
                output.append("<td>" + copy.getHtml() + "</td>");
            }
        }
        if (showPaste) {
            Button paste = new Button(pageContext);
            paste.setImg("WEB-CORE/images/gif/PlateTransfer.gif");
            paste.setTip("Paste items on the clipboard here");
            paste.setId("pastebutton");
            if (!browser.isIE()) {
                paste.setStyle("height:25px");
            }
            paste.setAction("pastePropertyList( '" + fieldname + "' );");
            output.append("<td>" + paste.getHtml() + "</td>");
        }
        return output;
    }

    private static StringBuffer getPropertyList(String fieldname, int i, PropertyListCollection collection, PropertyDefinitionList propertyDefinitionList, String nodeid, PropertyList toppropertylist, PageContext pageContext, String tableextra, PropertyTreeDisplayOptions options, boolean visible) {
        StringBuffer output = new StringBuffer();
        PropertyList subpropertylist = (PropertyList)collection.get(i);
        String subpropertylistid = subpropertylist.getId();
        boolean ancestor = subpropertylist.getPropertyTreeNodeId() == null || !subpropertylist.getPropertyTreeNodeId().equals(nodeid);
        PropertyListEditor editor = new PropertyListEditor();
        if (editor == null) {
            output.append("Property List Editor not found");
        } else {
            output.append("<input type=\"hidden\" name=\"" + fieldname + "_" + i + "__PROPERTYLISTID\" value=\"" + subpropertylistid + "\" />");
            output.append("<input type=\"hidden\" name=\"" + fieldname + "_" + i + "__ANCESTOR\" value=\"" + (ancestor ? "Y" : "N") + "\" />");
            output.append("<input type=\"hidden\" name=\"" + fieldname + "_" + i + "__SEQUENCE\" value=\"" + subpropertylist.getAttribute("sequence") + "\" />");
            if (visible) {
                output.append("<table " + tableextra + " id=\"" + fieldname + "_" + i + "__PROPERTYLIST\" border=\"1\" class=\"propertytable\" cellspacing=\"0\" cellpadding=\"0\">");
            }
            if (options.showDebug) {
                String fromNode = subpropertylist.getPropertyTreeNodeId();
                output.append("propertylistid=" + subpropertylistid + " (from " + fromNode + "), sequence=" + subpropertylist.getAttribute("sequence") + "<br>");
            }
            output.append(editor.getEditor(nodeid, propertyDefinitionList, subpropertylist, toppropertylist, fieldname, i, pageContext, options)).append("\n");
            if (visible) {
                output.append("</table>");
            }
        }
        return output;
    }

    private static String processKey(String inputString, String key, PropertyList props, String token, boolean relaceEmpty) {
        PropertyList target;
        String out = inputString;
        if (key.indexOf(".") > -1) {
            String[] path = StringUtil.split(key, ".");
            target = props;
            if (target != null) {
                for (int k = 0; k < path.length - 1 && (target = target.getPropertyList(path[k])) != null; ++k) {
                }
            }
            key = path[path.length - 1];
        } else {
            target = props;
        }
        if (target != null) {
            if (target.containsKey(key)) {
                Object value = target.get(key);
                if (value != null && value instanceof String) {
                    if (value.toString().length() == 0) {
                        if (relaceEmpty) {
                            out = StringUtil.replaceAll(out, "[" + token + VARIABLES_SUFFIX, "", false);
                        }
                    } else {
                        out = StringUtil.replaceAll(out, "[" + token + VARIABLES_SUFFIX, value.toString(), false);
                    }
                } else if (relaceEmpty) {
                    out = StringUtil.replaceAll(out, "[" + token + VARIABLES_SUFFIX, "", false);
                }
            } else if (relaceEmpty) {
                out = StringUtil.replaceAll(out, "[" + token + VARIABLES_SUFFIX, "", false);
            }
        }
        return out;
    }

    public static String replaceToken(String inputstring, String tokenid, PropertyValue propertyValue) {
        PropertyList parentPropertyList = propertyValue.getParentPropertyList();
        if (parentPropertyList != null) {
            String replace = parentPropertyList.getProperty(tokenid);
            if (replace.length() > 0) {
                return StringUtil.replaceAll(inputstring, "[" + tokenid + VARIABLES_SUFFIX, replace);
            }
            if (parentPropertyList.getParentPropertyValue() != null) {
                inputstring = EditorUtil.replaceToken(inputstring, tokenid, parentPropertyList.getParentPropertyValue());
            }
        }
        return inputstring;
    }

    public static String replaceTokens(String inputString, PropertyList props, PropertyValue propertyValue) {
        String[] tokens;
        String str = EditorUtil.replaceTokens(inputString, props, false);
        if (str.indexOf("[") > -1 && (tokens = StringUtil.getTokens(str)) != null && tokens.length > 0) {
            for (int i = 0; i < tokens.length; ++i) {
                String key = tokens[i];
                if ((str = EditorUtil.replaceToken(str, key, propertyValue)).indexOf("[" + key + VARIABLES_SUFFIX) <= -1) continue;
                return StringUtil.replaceAll(str, "[" + key + VARIABLES_SUFFIX, "");
            }
        }
        return str;
    }

    public static String replaceTokens(String inputstring, PropertyList props) {
        return EditorUtil.replaceTokens(inputstring, props, true);
    }

    public static String replaceTokens(String inputstring, PropertyList props, boolean replaceEmpty) {
        String[] tokens;
        String out = inputstring;
        if (out.indexOf("[") > -1 && (tokens = StringUtil.getTokens(out)) != null && tokens.length > 0) {
            for (int i = 0; i < tokens.length; ++i) {
                String key = tokens[i];
                if (key.indexOf("||") > -1) {
                    String[] keys = StringUtil.split(key, "||");
                    for (int k = 0; k < keys.length; ++k) {
                        out = k == keys.length - 1 ? EditorUtil.processKey(out, keys[k], props, tokens[i], replaceEmpty) : EditorUtil.processKey(out, keys[k], props, tokens[i], false);
                    }
                    continue;
                }
                out = EditorUtil.processKey(out, key, props, tokens[i], replaceEmpty);
            }
        }
        return out;
    }

    public static void loadExtraPropertyLists(ArrayList extraPropertyLists, PageContext pageContext, Document document) throws Exception {
        EditorUtil.loadExtraPropertyLists(extraPropertyLists, pageContext, document, null, null);
    }

    public static void loadExtraPropertyLists(ArrayList extraPropertyLists, PageContext pageContext, Document document, PropertyListCollection collection) throws Exception {
        EditorUtil.loadExtraPropertyLists(extraPropertyLists, pageContext, document, null, collection);
    }

    public static void loadExtraPropertyLists(ArrayList extraPropertyLists, PageContext pageContext, Document document, ScheduleGrid grid) throws Exception {
        EditorUtil.loadExtraPropertyLists(extraPropertyLists, pageContext, document, grid, null);
    }

    public static void loadExtraPropertyLists(ArrayList extraPropertyLists, PageContext pageContext, Document document, ScheduleGrid grid, PropertyListCollection collection) throws Exception {
        block10: {
            String contextType;
            String[] contextParts;
            String[] pastePathParts;
            block14: {
                QueryProcessor qp;
                WebAdminProcessor wp;
                block13: {
                    block12: {
                        block11: {
                            String context = (String)pageContext.getSession().getAttribute(COLLECTION_ITEM_PASTE_CONTEXT);
                            wp = new WebAdminProcessor(pageContext);
                            qp = new QueryProcessor(pageContext);
                            String pastePath = (String)pageContext.getSession().getAttribute(COLLECTION_ITEM_PASTE);
                            if (pastePath == null || pastePath.length() <= 0) break block10;
                            pastePathParts = StringUtil.split(pastePath, ";");
                            if (context == null || context.length() <= 0) break block10;
                            contextParts = StringUtil.split(context, ";");
                            contextType = contextParts[0];
                            if (!contextType.equals(COLLECTION_ITEM_PASTE_CONTEXT_TYPE_MAINTPAGE) || collection == null || collection.size() <= 0) break block11;
                            PropertyList props = collection.getPropertyList(0);
                            for (int i = 1; i < pastePathParts.length; ++i) {
                                PropertyList copyPropertyList = props.findPropertyList(pastePathParts[i]);
                                if (copyPropertyList == null) continue;
                                Element copyPropertyListElement = copyPropertyList.toElement(document);
                                extraPropertyLists.add(copyPropertyListElement);
                            }
                            break block10;
                        }
                        if (!contextType.equals(COLLECTION_ITEM_PASTE_CONTEXT_TYPE_PROPERTYTREE)) break block12;
                        String propertyTreeid = contextParts[1];
                        String nodeid = contextParts[2];
                        PropertyTree tree = wp.getPropertyTree(propertyTreeid);
                        PropertyList props = tree.getNodePropertyList(nodeid, true);
                        for (int i = 1; i < pastePathParts.length; ++i) {
                            PropertyList copyPropertyList = props.findPropertyList(pastePathParts[i]);
                            if (copyPropertyList == null) continue;
                            Element copyPropertyListElement = copyPropertyList.toElement(document);
                            extraPropertyLists.add(copyPropertyListElement);
                        }
                        break block10;
                    }
                    if (!contextType.equals(COLLECTION_ITEM_PASTE_CONTEXT_TYPE_WEBPAGE)) break block13;
                    String propertyTreeid = contextParts[1];
                    String pageid = contextParts[2];
                    String edition = contextParts[3];
                    String elementid = contextParts[4];
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "select extendnodeid from webpagepropertytree where webpageid=" + safeSQL.addVar(pageid) + " and productedition=" + safeSQL.addVar(edition) + " and elementid = " + safeSQL.addVar(elementid) + " and propertytreeid=" + safeSQL.addVar(propertyTreeid);
                    DataSet dsextendnode = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                    if (dsextendnode.getRowCount() != 1) break block10;
                    String extendnodeid = dsextendnode.getString(0, "extendnodeid");
                    Element pagepropertylist = wp.loadPageValues(pageid, edition, propertyTreeid, elementid);
                    Element productpropertylist = wp.loadPageProductValues(pageid, edition, propertyTreeid, elementid);
                    PropertyList pagePropertyList = new PropertyList();
                    pagePropertyList.setUsePropertyValues(true);
                    if (!extendnodeid.equals("__root")) {
                        Node fromptreenode = wp.loadPropertyTreeValues(propertyTreeid);
                        PropertyDefinitionList propertyDefinitionList = wp.getPropertyDefinitionList(propertyTreeid);
                        pagePropertyList.setPropertyTree(fromptreenode, extendnodeid, false, propertyDefinitionList);
                        pagePropertyList.addPropertyList(productpropertylist, true, "product__" + extendnodeid);
                        pagePropertyList.addPropertyList(pagepropertylist, true, extendnodeid);
                        pagePropertyList.setPropertyTreeDefaults(DOMUtil.getChildElement(fromptreenode, "propertydefaultlist"), propertyDefinitionList);
                        for (int i = 1; i < pastePathParts.length; ++i) {
                            PropertyList copyPropertyList = pagePropertyList.findPropertyList(pastePathParts[i]);
                            if (copyPropertyList == null) continue;
                            Element copyPropertyListElement = copyPropertyList.toElement(document);
                            extraPropertyLists.add(copyPropertyListElement);
                        }
                    }
                    break block10;
                }
                if (!contextType.equals(COLLECTION_ITEM_PASTE_CONTEXT_TYPE_SCHEDULEPLANITEM)) break block14;
                String planid = contextParts[1];
                String itemid = contextParts[2];
                SafeSQL safeSQL = new SafeSQL();
                DataSet dsextendnode = qp.getPreparedSqlDataSet("select propertytreeid, scheduletasknodeid from scheduleplanitem where scheduleplanid=" + safeSQL.addVar(planid) + " and scheduleplanitemid=" + safeSQL.addVar(itemid), safeSQL.getValues());
                if (dsextendnode.getRowCount() != 1) break block10;
                String propertytreeid = dsextendnode.getString(0, "propertytreeid");
                String extendnodeid = dsextendnode.getString(0, "scheduletasknodeid");
                SchedulerAdminProcessor sap = new SchedulerAdminProcessor(pageContext);
                Document d = DOMUtil.getNewDocument(sap.loadItemValueTree(planid, itemid), true);
                Element pagepropertylist = d == null ? null : d.getDocumentElement();
                PropertyList planItemPropertyList = new PropertyList();
                planItemPropertyList.setUsePropertyValues(true);
                if (!extendnodeid.equals("__root")) {
                    Node fromptreenode = wp.loadPropertyTreeValues(propertytreeid);
                    PropertyDefinitionList propertyDefinitionList = wp.getPropertyDefinitionList(propertytreeid);
                    planItemPropertyList.setPropertyTree(fromptreenode, extendnodeid, false, propertyDefinitionList);
                    planItemPropertyList.addPropertyList(pagepropertylist, true, extendnodeid);
                    planItemPropertyList.setPropertyTreeDefaults(DOMUtil.getChildElement(fromptreenode, "propertydefaultlist"), propertyDefinitionList);
                    for (int i = 1; i < pastePathParts.length; ++i) {
                        PropertyList copyPropertyList = planItemPropertyList.findPropertyList(pastePathParts[i]);
                        if (copyPropertyList == null) continue;
                        Element copyPropertyListElement = copyPropertyList.toElement(document);
                        extraPropertyLists.add(copyPropertyListElement);
                    }
                }
                break block10;
            }
            if (contextType.equals(COLLECTION_ITEM_PASTE_CONTEXT_TYPE_STABILITYPLANITEM)) {
                PlanItem item;
                String planid = contextParts[1];
                String itemid = contextParts[2];
                ScheduleGrid copyGrid = grid;
                if (!grid.planid.equals(planid)) {
                    copyGrid = new ScheduleGrid(grid.getRakFile(), grid.getConnectionId());
                    copyGrid.retrieve(planid);
                }
                if ((item = copyGrid.planItems.findById(itemid)) != null) {
                    PropertyList planItemPropertyList = item.getCollapsedPropertyList();
                    for (int i = 1; i < pastePathParts.length; ++i) {
                        PropertyList copyPropertyList = planItemPropertyList.findPropertyList(pastePathParts[i]);
                        if (copyPropertyList == null) continue;
                        Element copyPropertyListElement = copyPropertyList.toElement(document);
                        extraPropertyLists.add(copyPropertyListElement);
                    }
                }
            }
        }
    }

    public static String showPasteButton(String fieldname, HashMap attributes, PageContext pageContext) {
        String pasteValue = (String)attributes.get("pastevalue");
        String propertyid = null;
        String path = null;
        if (pasteValue.equals("Y")) {
            propertyid = (String)attributes.get("id");
            path = fieldname.substring(0, fieldname.length() - propertyid.length() - 1);
            path = fieldname.substring(0, path.lastIndexOf(95));
        } else {
            int i;
            String collectionProperty = pasteValue.substring(pasteValue.indexOf(":") + 1).trim();
            if (collectionProperty.length() > 0 && (i = fieldname.indexOf(collectionProperty)) > 0) {
                path = fieldname.substring(0, i + collectionProperty.length());
                int j = fieldname.indexOf("_", i + 2);
                if (j >= 0) {
                    propertyid = fieldname.substring(j + 1);
                    propertyid = propertyid.substring(propertyid.indexOf("_") + 1);
                }
            }
        }
        if (propertyid != null) {
            Button button = new Button(pageContext);
            button.setImg("WEB-CORE/images/gif/Paste.gif");
            button.setStyle("height:9px");
            button.setTip("Paste this value to all selected members of the collection");
            button.setAction("pasteValue( '" + path + "', '" + propertyid + "', '" + fieldname + "' );");
            return "<div style=\"display:inline;position:relative;top:4px\">" + button.getHtml() + "</div>";
        }
        return "";
    }
}


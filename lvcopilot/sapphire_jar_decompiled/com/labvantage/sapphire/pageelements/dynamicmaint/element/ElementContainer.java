/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.element;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.ColumnManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ElementContainer
extends BaseElement {
    PropertyList requestData;

    @Override
    public void setPageContext(PageContext pageContext) {
        super.setPageContext(pageContext);
        this.requestData = (PropertyList)JstlUtil.evaluateExpression("${requestdata}", pageContext);
    }

    @Override
    public String getHtml() {
        Locale userLocale;
        StringBuilder html = new StringBuilder();
        if (this.requestData == null) {
            throw new RuntimeException("Page context is not set.");
        }
        boolean sdiNotesExists = false;
        PropertyList sdiNotesProps = this.requestData.getPropertyList("sdinotes");
        if (sdiNotesProps != null && sdiNotesProps.getProperty("show", "Y").equals("Y")) {
            sdiNotesExists = true;
        }
        boolean lv71Layout = false;
        try {
            PropertyList guiPolicy = this.getConfigurationProcessor().getPolicy("GUIPolicy", "Sapphire Custom");
            if (guiPolicy != null && guiPolicy.size() > 0) {
                lv71Layout = true;
            }
        }
        catch (Exception guiPolicy) {
            // empty catch block
        }
        PropertyList pagetypeProps = this.requestData.getPropertyListNotNull("pagedata");
        String sdcid = pagetypeProps.getProperty("sdcid", "");
        PropertyList layoutProps = this.requestData.getPropertyListNotNull("layout");
        int upperPartTabCount = 0;
        int lowerPartTabCount = 0;
        int upperPartColspan = 1;
        int lowerPartColspan = 1;
        boolean showSingleUpperTab = true;
        boolean showSingleLowerTab = true;
        HashSet<String> elementIds = new HashSet<String>();
        PropertyListCollection elements = pagetypeProps.getCollectionNotNull("elements");
        for (int i = 0; i < elements.size(); ++i) {
            PropertyList elementProps = elements.getPropertyList(i);
            String innerElementId = elementProps.getProperty("elementid");
            String innerElementtype = elementProps.getProperty("elementtype");
            boolean isVisible = elementProps.getProperty("show", "Y").equals("Y");
            PropertyList elementConfig = this.requestData.getPropertyList(innerElementId);
            if (elementConfig == null || !isVisible || elementIds.contains(innerElementId)) continue;
            elementIds.add(innerElementId);
            PropertyList tabConfig = elementConfig.getPropertyListNotNull("tab");
            boolean showTab = tabConfig.getProperty("show", "Y").equals("Y");
            String groupnumber = tabConfig.getProperty("group", "");
            if (groupnumber.equals("")) {
                groupnumber = innerElementtype.equals("dynamicgrid") || innerElementtype.equals("DYM_dynamicgrid") || innerElementtype.equals("DYM_dynamicdatasetgrid") || innerElementtype.equals("dynamicdatasetgrid") ? "bottom" : "top";
            }
            if (!showTab) continue;
            if (groupnumber.equalsIgnoreCase("bottom")) {
                ++lowerPartTabCount;
                if (!tabConfig.getProperty("showsingletabheader", "Y").equals("N")) continue;
                showSingleLowerTab = false;
                continue;
            }
            ++upperPartTabCount;
            if (!tabConfig.getProperty("showsingletabheader", "Y").equals("N")) continue;
            showSingleUpperTab = false;
        }
        StringBuilder upperPartTabs = new StringBuilder();
        StringBuilder lowerPartTabs = new StringBuilder();
        StringBuilder upperPart = new StringBuilder();
        StringBuilder lowerPart = new StringBuilder();
        StringBuilder tabScripts = new StringBuilder();
        StringBuilder upperTabElementGroup = new StringBuilder();
        StringBuilder lowerTabElementGroup = new StringBuilder();
        elementIds.clear();
        for (int i = 0; i < elements.size(); ++i) {
            PropertyList elementProps = elements.getPropertyList(i);
            String innerElementId = elementProps.getProperty("elementid");
            String innerElementtype = elementProps.getProperty("elementtype");
            boolean isVisible = elementProps.getProperty("show", "Y").equals("Y");
            PropertyList elementConfig = this.requestData.getPropertyList(innerElementId);
            if (elementConfig == null || !isVisible || elementIds.contains(innerElementId)) continue;
            elementIds.add(innerElementId);
            PropertyList tabConfig = elementConfig.getPropertyListNotNull("tab");
            boolean showTab = tabConfig.getProperty("show", "Y").equals("Y");
            String tabText = tabConfig.getProperty("text", "");
            String tabTip = tabConfig.getProperty("tip", "");
            String groupnumber = tabConfig.getProperty("group", "");
            if (groupnumber.equals("")) {
                groupnumber = innerElementtype.equals("dynamicgrid") || innerElementtype.equals("DYM_dynamicgrid") || innerElementtype.equals("DYM_dynamicdatasetgrid") || innerElementtype.equals("dynamicdatasetgrid") ? "bottom" : "top";
            }
            if (!showTab) continue;
            String elementHTML = this.getElementHTML(innerElementId, innerElementtype);
            if (groupnumber.equals("top")) {
                upperPart.append(elementHTML);
                if (upperPartTabCount <= 1 && !showSingleUpperTab) continue;
                if (tabText.equals("") && upperPartTabCount == 1) {
                    tabText = this.getTranslationProcessor().translate(pagetypeProps.getProperty("sdcid", ""));
                }
                this.addTabToTabGroup(upperPartTabs, tabScripts, upperTabElementGroup, upperPartColspan, tabText, tabTip, innerElementId, "upper", upperPartTabCount == 1, innerElementtype, lv71Layout);
                ++upperPartColspan;
                continue;
            }
            lowerPart.append(elementHTML);
            if (lowerPartTabCount <= 1 && !showSingleLowerTab) continue;
            this.addTabToTabGroup(lowerPartTabs, tabScripts, lowerTabElementGroup, lowerPartColspan, tabText, tabTip, innerElementId, "lower", lowerPartTabCount == 1, innerElementtype, lv71Layout);
            ++lowerPartColspan;
        }
        if (elements.size() == 0) {
            upperPart.append("<span style='color:red'>");
            upperPart.append(this.getTranslationProcessor().translate("No elements configured to page type"));
            upperPart.append("</span>");
        }
        html.append("<div id='maint_container'>");
        html.append("<table id='content_table' height='100%' width='100%' cellpadding=0 cellspacing=0 border=0 style='min-height:100%;'>");
        html.append("<tr><td id='maint_td' style='vertical-align:top;min-height:100%;width:100%'>");
        html.append("<div id='maint_div' style='overflow-y:auto;'>");
        if (lv71Layout) {
            html.append("<div id='top-component' style='margin-left:6px;margin-top:6px;'>\n");
        } else {
            html.append("<div id='top-component' style='margin-left:9px;margin-top:9px;'>\n");
        }
        html.append("<table id='toptable' cellpadding='0' cellspacing='0' border='0'>");
        if (upperPartTabs.length() > 0) {
            html.append("<tr>");
            html.append("<td><table cellpadding=0 cellspacing=0 style='width:100%' " + (lv71Layout ? "class='tab_modernstandard_bodyheader'" : "") + ">");
            html.append("<tr " + (lv71Layout ? "class='tab_modernstandard _bodyheader_expandable'" : "") + ">");
            html.append((CharSequence)upperPartTabs);
            html.append(" <td class='" + (lv71Layout ? "tab_modernstandard _spacer" : "tab_standard_spacer") + "'><div style='height:1px;overflow:hidden'>&nbsp;</div></td></tr>");
            html.append("</table></td></tr>");
        } else if (!lv71Layout) {
            html.append("<tr><td class='tab_standard_spacer'><div style='height:1px;overflow:hidden'>&nbsp;</div></td></tr>");
        }
        html.append("<tr><td class='" + (lv71Layout ? "tab_modernstandard _body" : "tab_standard_body") + "' " + (lv71Layout ? " style='padding-bottom:12px'" : "") + ">");
        html.append((CharSequence)upperPart);
        html.append("</td></tr></table>");
        html.append("</div>\n");
        if (lowerPart.length() > 0) {
            if (lv71Layout) {
                html.append("<div id='bottom-component' style='margin-left:6px;margin-top:6px;'>\n");
            } else {
                html.append("<div id='bottom-component' style='margin-left:9px;margin-top:9px;'>\n");
            }
            html.append("<table id='bottomtable' cellpadding='0' cellspacing='0' border='0'>");
            if (lowerPartTabs.length() > 0) {
                html.append("<tr>");
                html.append("<td><table cellpadding=0 cellspacing=0 style='width:100%' " + (lv71Layout ? "class='tab_modernstandard_bodyheader'" : "") + ">");
                html.append("<tr " + (lv71Layout ? "class='tab_modernstandard _bodyheader_expandable'" : "") + ">");
                html.append((CharSequence)lowerPartTabs);
                html.append("<td class='" + (lv71Layout ? "tab_modernstandard _spacer" : "tab_standard_spacer") + "'><div style='height:10px;overflow:hidden'>&nbsp;</div></td></tr>");
                html.append("</table></td></tr>");
            } else if (!lv71Layout) {
                html.append("<tr><td class='tab_standard_spacer'><div style='height:1px;overflow:hidden'>&nbsp;</div></td></tr>");
            }
            html.append("<tr><td class='" + (lv71Layout ? "tab_modernstandard _body" : "tab_standard_body") + "'>");
            html.append((CharSequence)lowerPart);
            html.append("</td></tr></table>");
            html.append("</div>\n");
        }
        html.append("</div>");
        html.append("</td>");
        if (sdiNotesExists) {
            this.addSDINotes(sdiNotesProps, html);
        }
        html.append("</tr></table>");
        html.append("</div>");
        if (pagetypeProps.getPropertyListNotNull("templateselector").getProperty("enable", "").equals("Y")) {
            String extendedWhere = pagetypeProps.getPropertyListNotNull("templateselector").getProperty("whereclause", "");
            String lookupPage = pagetypeProps.getPropertyListNotNull("templateselector").getProperty("lookuppage", "");
            String templateTitle = pagetypeProps.getPropertyListNotNull("templateselector").getProperty("title", this.getTranslationProcessor().translate("Template"));
            if (!extendedWhere.equals("")) {
                extendedWhere = extendedWhere + " and ";
            }
            extendedWhere = extendedWhere + this.getSDCProcessor().getSDCProperties(sdcid).get("tableid") + ".templateflag='Y'";
            ColumnManager cm = new ColumnManager(this.getTranslationProcessor(), this.getSDCProcessor());
            cm.setFormPrefix("templateselector");
            PropertyListCollection lookupColCollection = pagetypeProps.getPropertyListNotNull("templateselector").getCollectionNotNull("lookupcolumns");
            html.append(cm.getLookupScript(sdcid, "templateselector", extendedWhere, lookupColCollection, true));
        }
        String pageId = this.requestData.getProperty("page");
        HttpSession session = this.pageContext.getSession();
        PropertyList pageConfig = (PropertyList)session.getAttribute("DYM_" + pageId);
        if (pageConfig == null) {
            pageConfig = new PropertyList();
        }
        pageConfig.setProperty("pagetype", pagetypeProps);
        pageConfig.setProperty("layout", layoutProps);
        session.setAttribute("DYM_" + pageId, (Object)pageConfig);
        String userLocaleStr = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getLocale();
        if (userLocaleStr == null) {
            userLocaleStr = new DateTimeUtil().getLocale().toString();
        }
        if (userLocaleStr.contains("_")) {
            String country = userLocaleStr.substring(0, userLocaleStr.indexOf(95));
            String language = userLocaleStr.substring(userLocaleStr.indexOf(95) + 1);
            userLocale = new Locale(country, language);
        } else {
            userLocale = new Locale(userLocaleStr);
        }
        int dateFormat = 3;
        int dateTimeDateFormat = 3;
        int dateTimeTimeFormat = 3;
        try {
            PropertyList dateFormatPolicy = this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom");
            String defaultdateformat = dateFormatPolicy.getProperty("defaultdateformat", "");
            String defaultdateonlyformat = dateFormatPolicy.getProperty("defaultdateonlyformat", "");
            if (!defaultdateonlyformat.equals("")) {
                int n = defaultdateonlyformat.equals("L") ? 1 : (dateFormat = defaultdateonlyformat.equals("M") ? 2 : 3);
            }
            if (!defaultdateformat.equals("")) {
                String[] formatS = StringUtil.split(defaultdateformat, " ");
                int n = formatS[0].equals("S") ? 3 : (dateTimeDateFormat = formatS[0].equals("L") ? 1 : 2);
                if (formatS.length == 2) {
                    dateTimeTimeFormat = formatS[1].equals("S") ? 3 : (formatS[1].equals("L") ? 1 : 2);
                }
            }
        }
        catch (SapphireException dateFormatPolicy) {
            // empty catch block
        }
        DateFormat df = DateFormat.getDateInstance(dateFormat, userLocale);
        String dateFormatStr = "";
        if (df instanceof SimpleDateFormat) {
            dateFormatStr = ((SimpleDateFormat)df).toPattern();
        }
        DateFormat dtf = DateFormat.getDateTimeInstance(dateTimeDateFormat, dateTimeTimeFormat, userLocale);
        String dateTimeFormatStr = "";
        if (dtf instanceof SimpleDateFormat) {
            dateTimeFormatStr = ((SimpleDateFormat)dtf).toPattern();
        }
        html.append("<script>");
        html.append("dynamicmaint.addFormConfig('locale','").append(userLocaleStr).append("');\n");
        html.append("dynamicmaint.addFormConfig('dateformat',\"").append(dateFormatStr).append("\");\n");
        html.append("dynamicmaint.addFormConfig('datetimeformat',\"").append(dateTimeFormatStr).append("\");\n");
        html.append("dynamicmaint.addFormConfig('tabgroups', {'upper' : [").append((CharSequence)upperTabElementGroup).append("], 'lower' : [").append((CharSequence)lowerTabElementGroup).append("]});\n");
        html.append((CharSequence)tabScripts);
        html.append("</script>");
        return html.toString();
    }

    public String getKeyColumns(String sdcid) {
        String keycol3;
        if (sdcid.equals("")) {
            return "";
        }
        String keyColumns = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        String keycol2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
        if (keycol2 != null && !keycol2.equals("")) {
            keyColumns = keyColumns + ";" + keycol2;
        }
        if ((keycol3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3")) != null && !keycol3.equals("")) {
            keyColumns = keyColumns + ";" + keycol3;
        }
        return keyColumns;
    }

    private String getElementHTML(String innerElementId, String innerElementtype) {
        BaseElement innerElement;
        String sql = "select objectname from propertytree where propertytreetype='Element' and propertytreeid=?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{innerElementtype});
        String className = ds.getString(0, "objectname", "");
        try {
            Class<?> cl = Class.forName(className);
            innerElement = (BaseElement)cl.newInstance();
        }
        catch (ClassNotFoundException e) {
            this.logger.error("Class not found: " + className, e);
            return "<div id='" + this.elementid + "'><span style='color:red'>Element type " + innerElementtype + " not supported. See logs for more information.</span></div>";
        }
        catch (IllegalAccessException e) {
            this.logger.error("Class not accessible: " + className, e);
            return "<div id='" + this.elementid + "'><span style='color:red'>Element type " + innerElementtype + " not supported. See logs for more information.</span></div>";
        }
        catch (InstantiationException e) {
            this.logger.error("Class not instantiable: " + className, e);
            return "<div id='" + this.elementid + "'><span style='color:red'>Element type " + innerElementtype + " not supported. See logs for more information.</span></div>";
        }
        innerElement.setPageContext(this.pageContext);
        innerElement.setElementid(innerElementId);
        innerElement.setConnectionId(this.getConnectionId());
        innerElement.setBrowser(this.browser);
        String elementHTML = innerElement.getHtml();
        return elementHTML;
    }

    public String getActionButtonConfig() {
        StringBuilder actionButtonConfig = new StringBuilder();
        PropertyList toolbarProps = this.requestData.getPropertyListNotNull("advancedtoolbar");
        PropertyListCollection buttons = toolbarProps.getCollectionNotNull("buttons");
        for (int i = 0; i < buttons.size(); ++i) {
            PropertyList button = buttons.getPropertyList(i);
            if (!button.getProperty("buttontype", "").equals("Action")) continue;
            String actionbuttonid = button.getProperty("id", "");
            PropertyList actionButtonProps = button.getPropertyListNotNull("actionbuttonprops");
            String buttonString = actionButtonProps.toJSONObject().toString().replaceAll("\"", "\\\\\"");
            if (actionButtonConfig.length() > 0) {
                actionButtonConfig.append(",");
            }
            actionButtonConfig.append("\"").append(actionbuttonid).append("\":\"").append(buttonString).append("\"");
        }
        return actionButtonConfig.toString();
    }

    private void addSDINotes(PropertyList sdiNotesProps, StringBuilder html) {
        html.append("<td style='width:2px;background:#bfdbff;border:1px solid lightgray'>&nbsp;</td>");
        html.append("<td width='300px' id='notescell' style='border-left:2px solid lightgray; vertical-align:top;'>");
        html.append("<div style='display:hidden; width:100%; height:100%; overflow-y:none; overflow-x:visible' id='notesdiv'>");
        html.append("<div id='sdinotes' style='width:300px; overflow-x:visible;'></div>");
        html.append("<div id='sdinotes_expand' class='notes_titlebar_back' style='display:none;vertical-align:top;width:25px;'>");
        html.append("<a href='JavaScript:dynamicmaint.showNotes();'><img src='WEB-CORE/elements/advancedsearch/images/hidesearchbar.gif' border='0'/></a>");
        html.append("<div id='notespresent'></div>");
        html.append("</div>");
        html.append("</div>");
        html.append("<script>\n");
        String sdinotesScript = "sapphire.gwt.addGWTElement( 'notes', 'sdinotes', " + sdiNotesProps.toJSONString() + ");\n";
        html.append(sdinotesScript);
        html.append("</script>\n");
        html.append("</td>");
    }

    private void addTabToTabGroup(StringBuilder tabs, StringBuilder tabScripts, StringBuilder tabGroup, int colspan, String tabText, String tabTip, String innerElementId, String tabGroupName, boolean isSingle, String innerElementtype, boolean lv71Layout) {
        String tabTextClass;
        String tabClass;
        boolean isMultiMaint;
        String selected = colspan == 1 ? "Y" : "N";
        boolean isInnerPage = innerElementtype.equals("DYM_dynamicinnerpage") || innerElementtype.equals("dynamicinnerpage");
        boolean bl = isMultiMaint = innerElementtype.equals("DYM_dynamicmultimaint") || innerElementtype.equals("dynamicmultimaint");
        if (isSingle) {
            tabClass = lv71Layout ? "tab_modernstandard _tab" : "tab_standard_tab";
            tabTextClass = lv71Layout ? "tab_text _selected" : "";
        } else if (colspan == 1) {
            tabClass = lv71Layout ? "tab_modernstandard _tab_selected" : "tab_standard_tab_selected";
            tabTextClass = lv71Layout ? "tab_text _selected" : "";
        } else {
            tabClass = lv71Layout ? "tab_modernstandard _tab_unselected" : "tab_standard_tab_unselected";
            tabTextClass = lv71Layout ? "tab_text" : "";
        }
        tabs.append("<td nowrap width=1% style='white-space: nowrap;width:1%;' ");
        tabs.append("id='").append(innerElementId).append("_tabheader' ");
        tabs.append("class='").append(tabClass).append("' ");
        tabs.append("title='").append(tabTip).append("' ");
        tabs.append("selected='").append(selected).append("' ");
        if (isSingle) {
            tabs.append("onclick=\"dynamicmaint.showHideForm('").append(innerElementId).append("', " + lv71Layout + ");\" ");
            tabs.append("onmouseover=\"this.className='" + (lv71Layout ? "tab_modernstandard _tab_highlight" : "tab_standard_tab_highlight") + "'; \" ");
            tabs.append("onmouseout=\"this.className='" + (lv71Layout ? "tab_modernstandard _tab" : "tab_standard_tab") + "'; \" ");
        } else {
            tabs.append("onclick=\"dynamicmaint.selectTab('").append(tabGroupName).append("', '").append(innerElementId).append("'," + lv71Layout + ");\" ");
            tabs.append("onmouseover=\"if (sapphire.util.dom.getAttribute(this,'selected') == 'N') this.className='" + (lv71Layout ? "tab_modernstandard _tab_unselected_highlight" : "tab_standard_tab_unselected_highlight") + "';\" ");
            tabs.append("onmouseout=\"if (sapphire.util.dom.getAttribute(this,'selected') == 'N' ) this.className='" + (lv71Layout ? "tab_modernstandard _tab_unselected" : "tab_standard_tab_unselected") + "';\" ");
        }
        if (isInnerPage) {
            tabs.append("ondblclick=\"").append(innerElementId).append("JS.repaintElement();\" ");
        }
        tabs.append(">");
        if (tabGroup.length() > 0) {
            tabGroup.append(",");
        }
        tabGroup.append("\"").append(innerElementId).append("\"");
        if (selected.equals("N")) {
            tabScripts.append("dynamicmaint.unSelectTab('").append(innerElementId).append("', " + lv71Layout + ");");
        }
        tabs.append("<div class='" + tabTextClass + "'>");
        if (isSingle && !isMultiMaint) {
            if (lv71Layout) {
                tabs.append("<img id='tab_").append(innerElementId).append("_img' src='WEB-CORE/elements/images/flat_black_angle_up_arrow.svg' width='8px' height='8px'> ");
            } else {
                tabs.append("<img id='tab_").append(innerElementId).append("_img' src='WEB-CORE/elements/images/minus.gif'> ");
            }
        }
        tabText = tabText + "&nbsp;";
        tabs.append(tabText);
        tabs.append("<span id='").append(innerElementId).append("_tabtitle'></span>");
        tabs.append("</div>");
        tabs.append("</td>");
    }
}


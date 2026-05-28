/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.detailmaint;

import com.labvantage.opal.elements.detailmaint.DBColumn;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.list.ListColumn;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseItem
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90330 $";
    public static final String BUTTONPLACEMENT_TOPLEFT = "topleft";
    public static final String BUTTONPLACEMENT_TOPMIDDLE = "topmiddle";
    public static final String BUTTONPLACEMENT_TOPRIGHT = "topright";
    public static final String BUTTONPLACEMENT_BOTTOMLEFT = "bottomleft";
    public static final String BUTTONPLACEMENT_BOTTOMMIDDLE = "bottommiddle";
    public static final String BUTTONPLACEMENT_BOTTOMRIGHT = "bottomright";
    public static final String BUTTONPLACEMENT_LEFT = "left";
    public static final String BUTTONPLACEMENT_MIDDLE = "middle";
    public static final String BUTTONPLACEMENT_RIGHT = "right";
    public static final String BUTTONPLACEMENT_TOP = "top";
    public static final String BUTTONPLACEMENT_BOTTOM = "bottom";
    protected String _SdcID;
    protected String _KeyID1;
    protected String _KeyID2;
    protected String _KeyID3;
    protected String _SqlQuery;
    protected String _Mode = "Edit";
    protected String _PageScript;
    protected Tab _Tab;
    protected String _TabText;
    protected HashMap _TableMetaData;
    protected PropertyListCollection columns;
    protected HashMap<String, Object> _ColumnMap;
    protected List<String> _ColumnsList;
    protected List<String> _TitleList;
    protected List<String> _WidthList;
    protected List<String> _AlignList;
    protected List<String> _ModeList;
    protected List<String> _LinkHrefList;
    protected List<String> _LinkTargetList;
    protected List<String> _LinkTipList;
    protected List<String> _PseudoList;
    protected List<String> _DisplayValueList;
    protected List<String> _LinkReftypeList;
    protected List<String> _LookupList;
    protected List<String> _DisableOnSaveList;
    protected List<String> _DefaultValueList;
    protected List<Object> _DropDownSQLList;
    protected List<String> _CollapseList;
    protected Set<String> _CustomList;
    protected List<String> _DateFormatList;
    protected List<String> _TranslateList;
    protected List<String> _DynamicLook;
    protected List<String> _DisplayReftype;
    private String __ButtonPlacementTop = "none";
    private String __ButtonPlacementBottom = "none";
    private String __ButtonHTML = null;
    protected static final String CONSTANT_Y = "Y";
    protected String _PropertyHandlerClass;
    protected HashMap<String, String> __PropertyHandlerMap;
    protected int _CurrentRow;
    protected boolean __SaveException;
    public static final String ROW_DISABLED = "_DISABLEDROW";
    protected static List<String> _ReservedFunctionsList = new ArrayList<String>();
    public static List<String> COLUMNS_CORE = new ArrayList<String>();
    private ArrayList<List<String>> __Grid = new ArrayList();
    private static List<String> __IgnoreMasterKeyList = new ArrayList<String>();
    private boolean viewOnly = false;
    protected List<String> timeZoneIndependentColumnList = new ArrayList<String>();
    protected static String currentUser = "";
    protected boolean _IsOra;
    private Map<String, String> refTypeValueMap = new HashMap<String, String>();
    private Map<String, String> reftypeMap = new HashMap<String, String>();

    protected BaseItem() {
        this._ColumnMap = new HashMap();
        this._ColumnsList = new ArrayList<String>();
        this._TitleList = new ArrayList<String>();
        this._WidthList = new ArrayList<String>();
        this._AlignList = new ArrayList<String>();
        this._ModeList = new ArrayList<String>();
        this._LinkHrefList = new ArrayList<String>();
        this._LinkTargetList = new ArrayList<String>();
        this._LinkTipList = new ArrayList<String>();
        this._PseudoList = new ArrayList<String>();
        this._CustomList = new HashSet<String>();
        this._DisplayValueList = new ArrayList<String>();
        this._LinkReftypeList = new ArrayList<String>();
        this._LookupList = new ArrayList<String>();
        this._DefaultValueList = new ArrayList<String>();
        this._DropDownSQLList = new ArrayList<Object>();
        this._DisableOnSaveList = new ArrayList<String>();
        this._CollapseList = new ArrayList<String>();
        this._DateFormatList = new ArrayList<String>();
        this._TranslateList = new ArrayList<String>();
        this._DynamicLook = new ArrayList<String>();
        this._DisplayReftype = new ArrayList<String>();
        this.__PropertyHandlerMap = new HashMap();
        this.putPropertyHandlerKey("edata", "");
        this.putPropertyHandlerKey("eremove", "");
        this.putPropertyHandlerKey("emodified", "N");
    }

    @Override
    public String getHtml() {
        currentUser = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        this._IsOra = this.getConnectionProcessor().isOra();
        if (this.element == null) {
            return BaseItem.toErrorString("Element Error", "No element data found for the element");
        }
        this.setSdcid(this.requestContext.getProperty("sdcid"));
        this.setKeyid1(this.requestContext.getProperty("keyid1"));
        this.setKeyid2(this.requestContext.getProperty("keyid2"));
        this.setKeyid3(this.requestContext.getProperty("keyid3"));
        if (this.getSdcid() == null || this.getKeyid1().equals("(null)")) {
            return BaseItem.toErrorString("Data Error", "No information found for Master in Request.");
        }
        this.putPropertyHandlerKey("sdcid", this.getSdcid());
        this.putPropertyHandlerKey("keyid1", this.getKeyid1());
        this.putPropertyHandlerKey("keyid2", this.getKeyid2());
        this.putPropertyHandlerKey("keyid3", this.getKeyid3());
        String elementmode = this.element.getProperty("mode");
        if (elementmode == null || elementmode.length() == 0) {
            elementmode = "Edit";
        }
        this.setMode(elementmode);
        try {
            this.parseElementColumns();
        }
        catch (SapphireException e) {
            return BaseItem.toErrorString("Element Configuration Error", e.getMessage());
        }
        StringBuilder html = new StringBuilder();
        if (this.pageContext.getAttribute("jsincluded") == null || !this.pageContext.getAttribute("jsincluded").equals(CONSTANT_Y)) {
            html.append(this.getGridJsIncludes());
            this.pageContext.setAttribute("jsincluded", (Object)CONSTANT_Y);
        }
        if (this.pageContext.getAttribute("rendergridscript") == null || this.pageContext.getAttribute("rendergridscript").equals(CONSTANT_Y)) {
            html.append(SDITagUtil.getGrid(new ArrayList(), this.element.getId(), -1, -1, true, this.browser, this.getTranslationProcessor()));
        }
        this.initElement();
        html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, this.pageContext));
        PropertyList tab = this.element.getPropertyList("tab");
        if (tab != null) {
            String tabshow = tab.getProperty("show");
            if (CONSTANT_Y.equals(tabshow)) {
                this._Tab = new Tab();
                this._TabText = OpalUtil.parseRequestString(this.pageContext, tab.getProperty("text"));
                this.setDefaultPropertyValue(tab, "text", this.element.getId());
                this.setDefaultPropertyValue(tab, "expandable", CONSTANT_Y);
                this.setDefaultPropertyValue(tab, "expanded", CONSTANT_Y);
                this.setDefaultPropertyValue(tab, "highlight", CONSTANT_Y);
                this.setDefaultPropertyValue(tab, "id", this.element.getId() + "_tab");
                this._Tab.setId(tab.getProperty("id"));
                this._Tab.setText(this._TabText);
                this._Tab.setBodywidth(tab.getProperty("bodywidth").length() > 0 ? tab.getProperty("bodywidth") : "");
                this._Tab.setWidth(tab.getProperty("width").length() > 0 ? tab.getProperty("width") : "");
                this._Tab.setExpandable(tab.getProperty("expandable").equals(CONSTANT_Y) ? "true" : "false");
                this._Tab.setExpanded(tab.getProperty("expanded").equals(CONSTANT_Y) ? "true" : "false");
                this._Tab.setHighlight(tab.getProperty("highlight").equals(CONSTANT_Y) ? "true" : "false");
                this._Tab.setContent(this.getMainHtml());
                this._Tab.setCollapsedtext(this.getTranslationProcessor().translate("Click the tab to show more information") + ".");
                html.append(this._Tab.getHtml());
            } else {
                html.append(this.getMainHtml());
            }
        } else {
            html.append(this.getMainHtml());
        }
        if (!this.isViewonly()) {
            if (!CONSTANT_Y.equals(this.pageContext.getAttribute("__detailelementcontext"))) {
                html.append("<script>var __griddetailidarray = new Array();</script>");
                this.pageContext.setAttribute("__detailelementcontext", (Object)CONSTANT_Y);
            }
            html.append("<script>__griddetailidarray.push( '").append(this.element.getId()).append("' );</script>");
        }
        PropertyListCollection includes = this.element.getCollectionNotNull("jsincludes");
        html.append("\n<!-- Element Includes Start for element ").append(this.element.getId()).append(" -->");
        for (int i = 0; i < includes.size(); ++i) {
            String src = includes.getPropertyList(i).getProperty("src");
            if (StringUtil.getLen(src) <= 0L) continue;
            html.append("\n<script src='").append(src).append("'></script>");
        }
        html.append("\n<!-- Element Includes End for element ").append(this.element.getId()).append(" -->");
        return html.toString();
    }

    protected abstract String getMainHtml();

    protected void initElement() {
    }

    protected String getElementAppearance() {
        String appearance = this.element.getProperty("appearance");
        if (appearance == null || appearance.length() == 0) {
            appearance = "blue";
        }
        return appearance;
    }

    protected String renderHtml(DataSet ds) {
        String selectorType;
        StringBuilder sb = new StringBuilder();
        boolean parentLocked = this.isParentLocked();
        if (this._Tab != null) {
            if (ds != null) {
                this._Tab.setText(this._TabText + " (" + ds.size() + ")");
            } else {
                this._Tab.setText(this._TabText + " (" + this.getTranslationProcessor().translate("error") + ")");
            }
        }
        if (StringUtil.getLen(selectorType = this.element.getProperty("selectortype")) == 0L) {
            selectorType = "View".equals(this.getMode()) ? "none" : "checkbox";
        }
        sb.append("<script>");
        sb.append("var ").append(this.element.getId()).append("__lockstate = ").append(parentLocked ? "true" : "false").append(";\n");
        sb.append("var ").append(this.element.getId()).append("_selectortype = '").append(selectorType).append("';\n");
        sb.append("var ").append(this.element.getId()).append("_refdisplayicon = new Array();\n");
        sb.append(this.getLinksData(this.element.getId()));
        sb.append(this.getdisplayRefTytpeData(this.element.getId()));
        sb.append(this.getDataScript(ds));
        sb.append(this.getColumnScript());
        sb.append("</script>");
        if (!parentLocked && !this.getButtonPlacement(BUTTONPLACEMENT_TOP).equals("none")) {
            sb.append(this.getButtons(this.element.getId()));
        }
        sb.append("<table><tr><td>");
        sb.append("<table class='maintform_table_").append(this.getElementAppearance()).append("'");
        sb.append(" border=1 cellpadding=2 cellspacing=0 id='").append(this.element.getId()).append("'>");
        sb.append("<thead><tr height='26'><td></td></tr>");
        sb.append("</thead><tbody></tbody></table>");
        sb.append("</td></tr>");
        sb.append("<tr><td>");
        sb.append("<div id='").append(this.element.getId()).append("_norecord' style='display:");
        sb.append(ds == null || ds.size() == 0 ? "block" : "none").append(";'>");
        sb.append("<div width='100%' style='padding:2px;' class='maintform_field_").append(this.getElementAppearance()).append("'>");
        sb.append(this.getTranslationProcessor().translate("No records found"));
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</td></tr></table>");
        if (!parentLocked && !this.getButtonPlacement(BUTTONPLACEMENT_BOTTOM).equals("none")) {
            sb.append(this.getButtons(this.element.getId()));
        }
        PropertyList legend = this.element.getPropertyList("legend");
        sb.append("<table id='").append(this.element.getId()).append("_legendblock' cellpadding=2 cellspacing=0 border=0>");
        sb.append("<tr><td class='legend'>");
        sb.append(legend != null ? legend.getProperty("text") : "");
        sb.append("</td></tr></table>");
        if (!this.getMode().equals("View")) {
            sb.append(this.getLegendScript());
        }
        sb.append(this.getValidationScript());
        return sb.toString();
    }

    protected String getIncludeScripts() {
        StringBuilder sb = new StringBuilder();
        if (!CONSTANT_Y.equals(this.pageContext.getAttribute("detailmaint.jsinclude"))) {
            sb.append("<script language='JavaScript' src='WEB-OPAL/elements/scripts/sdibase.js'></script>");
            sb.append("<script language='JavaScript' src='WEB-OPAL/elements/scripts/dd.js'></script>");
            sb.append("<script language='JavaScript' src='WEB-OPAL/elements/scripts/backcomp.js'></script>");
            this.pageContext.setAttribute("detailmaint.jsinclude", (Object)CONSTANT_Y);
        }
        return sb.toString();
    }

    private String getGridJsIncludes() {
        StringBuilder html = new StringBuilder();
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/scripts/grid.js\"></script>\n");
        html.append("<textarea style=\"display:none;width:0;height:0\" id=\"clipboard\"></textarea>\n");
        return html.toString();
    }

    protected String getLegendScript() {
        StringBuilder sb = new StringBuilder();
        PropertyList legend = this.element.getPropertyList("legend");
        sb.append("<script>");
        sb.append("var ").append(this.element.getId()).append("_newitemsymbol = '").append(legend != null ? legend.getProperty("newitemsymbol") : "").append("';");
        sb.append("var ").append(this.element.getId()).append("_applyworkitemsymbol = '").append(legend != null ? legend.getProperty("applyworkitemsymbol") : "").append("';");
        sb.append("</script>");
        return sb.toString();
    }

    protected String getOperations(String elementType) {
        StringBuilder sb = new StringBuilder();
        sb.append("document.getElementById( '__").append(this.element.getId()).append("_ecolumns' ).value = getColumnIDBuffer( c1_").append(this.element.getId()).append(", '|', false );");
        if (this.doesColumnExists("usersequence")) {
            sb.append("function moveUp").append(this.element.getId()).append("() {\n");
            sb.append("\tvar list = getSelectedRowID( '").append(this.element.getId()).append("');\n");
            sb.append("\tif ( list.length <= 0 ) {\n");
            sb.append("\t\ttop.showMessage(top.selectAtleastOneItemMsg);\n");
            sb.append("\t\treturn false;\n");
            sb.append("\t}\n");
            sb.append("\telse {\n");
            sb.append("    \tif ( moveSelectedRow( '").append(this.element.getId()).append("', a2_").append(this.element.getId()).append(", 'U' ) ) {");
            sb.append("        \tmarkAllRowsEdited( '").append(this.element.getId()).append("' );");
            sb.append("        \tsetChangesMade_this( '").append(this.element.getId()).append("', true );");
            sb.append("    \t}");
            sb.append("\t}\n");
            sb.append("}\n");
            sb.append("function moveDown").append(this.element.getId()).append("() {\n");
            sb.append("\tvar list = getSelectedRowID( '").append(this.element.getId()).append("');\n");
            sb.append("\tif ( list.length <= 0 ) {\n");
            sb.append("\t\ttop.showMessage(top.selectAtleastOneItemMsg);\n");
            sb.append("\t\treturn false;\n");
            sb.append("\t}\n");
            sb.append("\telse {\n");
            sb.append("    \tif ( moveSelectedRow( '").append(this.element.getId()).append("', a2_").append(this.element.getId()).append(", 'D' ) ) {");
            sb.append("        \tmarkAllRowsEdited( '").append(this.element.getId()).append("' );");
            sb.append("        \tsetChangesMade_this( '").append(this.element.getId()).append("', true );");
            sb.append("    \t}");
            sb.append("\t}\n");
            sb.append("}\n");
        }
        sb.append("function reset").append(this.element.getId()).append("() {");
        sb.append("    resetDetailData( '").append(elementType).append("', '").append(this.element.getId()).append("' );");
        sb.append("}\n");
        if (this.pageContext.getAttribute("__DetailMaint_CommonFunction") == null) {
            sb.append("function getParamValue( param ) {");
            sb.append("    if ( param == 'sdcid' ) {");
            sb.append("        return '").append(this.requestContext.getProperty("sdcid")).append("';");
            sb.append("    }");
            sb.append("    if ( param == 'keyid1' ) {");
            sb.append("        return '").append(this.requestContext.getProperty("keyid1")).append("';");
            sb.append("    }");
            sb.append("    if ( param == 'keyid2' ) {");
            sb.append("        return '").append(this.requestContext.getProperty("keyid2")).append("';");
            sb.append("    }");
            sb.append("    if ( param == 'keyid3' ) {");
            sb.append("        return '").append(this.requestContext.getProperty("keyid3")).append("';");
            sb.append("    }");
            sb.append("}");
            this.pageContext.setAttribute("__DetailMaint_CommonAttributes", (Object)"1");
        }
        sb.append("_elementtype = '").append(this.element.getProperty("elementtype")).append("';");
        sb.append("initDetail( '").append(elementType).append("', '").append(this.element.getId()).append("' );");
        return sb.toString();
    }

    protected String convertToPropertyHandlerKey(Object o) {
        StringBuilder sb = new StringBuilder();
        if (o == this._ColumnsList) {
            for (String a_ColumnsList : this._ColumnsList) {
                String columnId = a_ColumnsList;
                if (columnId.startsWith("(")) {
                    int lastindex = columnId.lastIndexOf(")");
                    columnId = columnId.substring(++lastindex).trim();
                }
                sb.append(columnId).append(";");
            }
            if (sb.toString().trim().length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }

    public static String toErrorString(String title, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("<font color='red'>");
        if (title != null) {
            sb.append("<b>").append(title).append("</b><br>");
        }
        sb.append(msg).append("</font>");
        return sb.toString();
    }

    protected void setKeyid1(String keyid1) {
        this._KeyID1 = keyid1;
    }

    protected void setKeyid2(String keyid2) {
        this._KeyID2 = keyid2;
    }

    protected void setKeyid3(String keyid3) {
        this._KeyID3 = keyid3;
    }

    protected void setMode(String s) {
        this._Mode = s;
    }

    protected String getMode() {
        if (this._Mode == null || this._Mode.length() == 0 || this._Mode.equals("null")) {
            return "Edit";
        }
        return this._Mode;
    }

    protected void setPropertyHandlerClass(String propertyHandlerClass) {
        this._PropertyHandlerClass = propertyHandlerClass;
    }

    protected void putPropertyHandlerKey(String key, String value) {
        this.__PropertyHandlerMap.put(key, value);
    }

    protected String getButtons(String id) {
        if (this.__ButtonHTML == null) {
            StringBuilder sb = new StringBuilder();
            PropertyListCollection buttonscollection = this.element.getCollection("buttons");
            sb.append("<table border=0 cellspacing=3 cellpadding=0><tr>");
            StringBuilder function = new StringBuilder();
            for (int i = 0; i < buttonscollection.size(); ++i) {
                PropertyList pl = buttonscollection.getPropertyList(i);
                sb.append("<td>");
                if (pl.getProperty("show").equals(CONSTANT_Y)) {
                    this.setDefaultPropertyValue(pl, "show", CONSTANT_Y);
                    this.setDefaultPropertyValue(pl, "width", "80");
                    this.setDefaultPropertyValue(pl, "appearance", "standard");
                    this.setDefaultPropertyValue(pl, "highlight", CONSTANT_Y);
                    this.setDefaultPropertyValue(pl, "id", this.element.getId() + "_button_" + i);
                    function.setLength(0);
                    function.append(pl.getProperty("js"));
                    String f = function.toString();
                    if (!function.toString().startsWith("fn")) {
                        int commaIndex = function.indexOf("(");
                        if (commaIndex != -1) {
                            function.insert(commaIndex, id);
                        } else {
                            function.append(id).append("()");
                            f = f + "()";
                        }
                    }
                    pl.setProperty("js", "try{" + function.toString() + "}catch(e){console.log(e);try{" + f + "}catch(e){}}");
                    Button button = new Button(this.pageContext);
                    ElementUtil.setButtonProperties(button, pl);
                    sb.append(button.getHtml());
                }
                sb.append("</td>");
            }
            sb.append("</tr></table>");
            this.__ButtonHTML = sb.toString();
        }
        return this.__ButtonHTML;
    }

    protected String getButtonPlacement(String placement) {
        if (this.getMode().equals("View")) {
            return "none";
        }
        String buttonplacement = this.element.getProperty("buttonplacement");
        if (buttonplacement.equals(BUTTONPLACEMENT_TOPLEFT)) {
            this.__ButtonPlacementTop = this.connectionInfo.isRtl() ? BUTTONPLACEMENT_RIGHT : BUTTONPLACEMENT_LEFT;
            this.__ButtonPlacementBottom = "none";
        } else if (buttonplacement.equals(BUTTONPLACEMENT_TOPMIDDLE)) {
            this.__ButtonPlacementTop = BUTTONPLACEMENT_MIDDLE;
            this.__ButtonPlacementBottom = "none";
        } else if (buttonplacement.equals(BUTTONPLACEMENT_TOPRIGHT)) {
            this.__ButtonPlacementTop = this.connectionInfo.isRtl() ? BUTTONPLACEMENT_LEFT : BUTTONPLACEMENT_RIGHT;
            this.__ButtonPlacementBottom = "none";
        } else if (buttonplacement.equals(BUTTONPLACEMENT_BOTTOMLEFT)) {
            this.__ButtonPlacementBottom = this.connectionInfo.isRtl() ? BUTTONPLACEMENT_RIGHT : BUTTONPLACEMENT_LEFT;
            this.__ButtonPlacementTop = "none";
        } else if (buttonplacement.equals(BUTTONPLACEMENT_BOTTOMMIDDLE)) {
            this.__ButtonPlacementBottom = BUTTONPLACEMENT_MIDDLE;
            this.__ButtonPlacementTop = "none";
        } else if (buttonplacement.equals(BUTTONPLACEMENT_BOTTOMRIGHT)) {
            this.__ButtonPlacementBottom = this.connectionInfo.isRtl() ? BUTTONPLACEMENT_LEFT : BUTTONPLACEMENT_RIGHT;
            this.__ButtonPlacementTop = "none";
        } else if (buttonplacement.equals(BUTTONPLACEMENT_LEFT)) {
            this.__ButtonPlacementBottom = this.connectionInfo.isRtl() ? BUTTONPLACEMENT_RIGHT : BUTTONPLACEMENT_LEFT;
            this.__ButtonPlacementTop = this.connectionInfo.isRtl() ? BUTTONPLACEMENT_RIGHT : BUTTONPLACEMENT_LEFT;
        } else if (buttonplacement.equals(BUTTONPLACEMENT_MIDDLE)) {
            this.__ButtonPlacementBottom = BUTTONPLACEMENT_MIDDLE;
            this.__ButtonPlacementTop = BUTTONPLACEMENT_MIDDLE;
        } else if (buttonplacement.equals(BUTTONPLACEMENT_RIGHT)) {
            this.__ButtonPlacementBottom = this.connectionInfo.isRtl() ? BUTTONPLACEMENT_LEFT : BUTTONPLACEMENT_RIGHT;
            String string = this.__ButtonPlacementTop = this.connectionInfo.isRtl() ? BUTTONPLACEMENT_LEFT : BUTTONPLACEMENT_RIGHT;
        }
        if (placement.equals(BUTTONPLACEMENT_TOP)) {
            return this.__ButtonPlacementTop;
        }
        if (placement.equals(BUTTONPLACEMENT_BOTTOM)) {
            return this.__ButtonPlacementBottom;
        }
        return "none";
    }

    protected void setDefaultPropertyValue(PropertyList propertyList, String property, String defaultValue) {
        if (propertyList.getProperty(property).length() == 0) {
            propertyList.setProperty(property, defaultValue);
        }
    }

    protected void parseElementColumns() throws SapphireException {
        this.columns = this.element.getCollection("columns");
        if (this.element.getProperty("sql").length() == 0 && (this.columns == null || this.columns.size() == 0)) {
            throw new SapphireException("No columns defined");
        }
        boolean linkedsdiitemmode = false;
        DataSet columnds = new DataSet();
        String sdcid = this.element.getProperty("sdcid", "").trim();
        if (StringUtil.getLen(sdcid) > 0L) {
            linkedsdiitemmode = true;
            columnds = this.getSDCProcessor().getColumnData(sdcid);
        }
        for (int i = 0; i < this.columns.size(); ++i) {
            String datatype;
            int columnlength;
            int columnrow;
            PropertyList column = this.columns.getPropertyList(i);
            String columnid = column.getProperty("column");
            String columntitle = column.getProperty("title", this.getTranslationProcessor().translate(columnid));
            String columnwidth = column.getProperty("width", "150");
            String columnalign = column.getProperty("align", this.connectionInfo.isRtl() ? BUTTONPLACEMENT_RIGHT : BUTTONPLACEMENT_LEFT);
            String columnmode = column.getProperty("mode");
            if (linkedsdiitemmode && ("input".equals(columnmode) || StringUtil.getLen(columnmode) == 0L) && StringUtil.getLen(columnid) > 0L && columnid.indexOf("=") == -1 && (columnrow = columnds.findRow("columnid", columnid)) != -1 && (columnlength = columnds.getInt(columnrow, "columnlength")) >= 2000 && ("C".equals(datatype = columnds.getString(columnrow, "datatype")) || "B".equals(datatype) || "T".equals(datatype))) {
                columnmode = "textarea";
            }
            String pseudovalue = column.getProperty("pseudo", "");
            String displayvalue = column.getProperty("displayvalue");
            String linkreftypeid = column.getProperty("linkreftypeid");
            String lookuppage = column.getProperty("lookuppage", "");
            String disableonsave = column.getProperty("disableonsave", "N");
            String defaultvalue = column.getProperty("default", "");
            if (defaultvalue.contains("[currentuser]")) {
                defaultvalue = StringUtil.replaceAll(defaultvalue, "[currentuser]", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId());
            } else if (defaultvalue.contains("[currentdate]")) {
                SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
                M18NUtil m18n = new M18NUtil(sapphireConnection);
                defaultvalue = StringUtil.replaceAll(defaultvalue, "[currentdate]", m18n.formatDateOnly(m18n.getNowCalendar()));
            }
            String format = column.getProperty("format", "");
            String elementid = this.element.getProperty("propertytreeid", "");
            if ((!"".equals(elementid) && "linkedsdimaint".equals(elementid) || "detailmaint".equals(elementid) || "sdidetailmaint".equals(elementid)) && columnmode.equals("datelookup") && (format == null || format.length() == 0)) {
                format = this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom").getProperty("defaultdateformat", "");
            }
            String translatevalue = column.getProperty("translatevalue", "");
            String dynamiclookup = column.getPropertyListNotNull("lookuplink").getProperty("enablesuggest");
            String displayreftype = column.getProperty("displayreftype");
            if (columnmode.equalsIgnoreCase("displayicon") && displayreftype.trim().length() == 0) {
                DataSet dsLinkData;
                String tableid = this.element.getProperty("elementtype").toLowerCase();
                DataSet dsTableInfo = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdcid from sdc WHERE tableid = ?", (Object[])new String[]{tableid});
                if (dsTableInfo.getRowCount() > 0 && (dsLinkData = this.getSDCProcessor().getLinksData(dsTableInfo.getValue(0, "sdcid"))) != null && dsLinkData.getRowCount() > 0) {
                    int columnRow;
                    HashMap<String, String> filterRefTypeLinkSDC = new HashMap<String, String>();
                    filterRefTypeLinkSDC.put("linksdcid", "RefType");
                    DataSet dsRefTypeLinks = dsLinkData.getFilteredDataSet(filterRefTypeLinkSDC);
                    if (dsRefTypeLinks.getRowCount() > 0 && (columnRow = dsLinkData.findRow("sdccolumnid", columnid)) > -1) {
                        displayreftype = dsLinkData.getValue(columnRow, "reftypeid");
                    }
                }
            }
            PropertyList dropdowndefinition = column.getPropertyListNotNull("dropdowndefinition");
            String ddsdcid = dropdowndefinition.getProperty("sdcid", "").trim();
            String ddvaluecolumn = dropdowndefinition.getProperty("valuecolumn", "").trim();
            if (StringUtil.getLen(ddsdcid) > 0L && StringUtil.getLen(ddvaluecolumn) > 0L) {
                this._DropDownSQLList.add(i, dropdowndefinition);
            } else {
                this._DropDownSQLList.add(i, column.getProperty("dropdownsql", ""));
            }
            if (displayvalue == null || displayvalue.length() == 0 || displayvalue.indexOf("=") == -1) {
                displayvalue = "";
            }
            if (columnid.contains("[currentuser]")) {
                columnid = StringUtil.replaceAll(columnid, "[currentuser]", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId());
            }
            this._ColumnsList.add(i, columnid);
            this._TitleList.add(i, columntitle);
            this._WidthList.add(i, columnwidth);
            this._AlignList.add(i, columnalign);
            if (COLUMNS_CORE.contains(columnid)) {
                columnmode = "readonly";
            }
            this._ModeList.add(i, columnmode);
            PropertyList linkpropertylist = column.getPropertyList("link");
            String linkhref = "";
            String linktarget = "";
            String linktip = "";
            if (linkpropertylist != null) {
                linkhref = linkpropertylist.getProperty("href");
                linktarget = linkpropertylist.getProperty("target");
                linktip = linkpropertylist.getProperty("tip");
                if (linkhref == null || linkhref.length() == 0) {
                    linkhref = "";
                }
                if (linktarget == null || linktarget.length() == 0) {
                    linktarget = "_blank";
                }
                if (linktip == null || linktip.length() == 0) {
                    linktip = "";
                }
            }
            this._LinkHrefList.add(i, linkhref);
            this._LinkTargetList.add(i, linktarget);
            this._LinkTipList.add(i, linktip);
            this._PseudoList.add(i, pseudovalue);
            this._DisplayValueList.add(i, displayvalue);
            this._LinkReftypeList.add(i, linkreftypeid);
            this._LookupList.add(i, lookuppage);
            this._DisableOnSaveList.add(i, disableonsave);
            this._DefaultValueList.add(i, defaultvalue);
            this._CollapseList.add("false");
            this._DateFormatList.add(format);
            this._TranslateList.add(translatevalue);
            this._DynamicLook.add(dynamiclookup);
            this._DisplayReftype.add(displayreftype);
            if (pseudovalue.length() > 0) {
                this._CustomList.add(columnid);
            }
            if (!columnid.startsWith("(")) continue;
            int lastindex = columnid.lastIndexOf(")");
            this._CustomList.add(columnid.substring(++lastindex).trim());
        }
        this._ColumnMap.put("columnslist", this._ColumnsList);
        this._ColumnMap.put("titlelist", this._TitleList);
        this._ColumnMap.put("widthlist", this._WidthList);
        this._ColumnMap.put("alignlist", this._AlignList);
        this._ColumnMap.put("modelist", this._ModeList);
        this._ColumnMap.put("linkhreflist", this._LinkHrefList);
        this._ColumnMap.put("linktargetlist", this._LinkTargetList);
        this._ColumnMap.put("linktiplist", this._LinkTipList);
        this._ColumnMap.put("pseudolist", this._PseudoList);
        this._ColumnMap.put("customlist", this._CustomList);
        this._ColumnMap.put("displayvaluelist", this._DisplayValueList);
        this._ColumnMap.put("dropdownsqllist", this._DropDownSQLList);
        this._ColumnMap.put("dateformatlist", this._DateFormatList);
        this._ColumnMap.put("translatevaluelist", this._TranslateList);
        this._ColumnMap.put("dynamiclookuplist", this._DynamicLook);
        this._ColumnMap.put("displayreftypelist", this._DisplayReftype);
    }

    protected String getSdcid() {
        return this._SdcID;
    }

    protected void setSdcid(String parSdcid) {
        this._SdcID = parSdcid;
    }

    protected String getKeyid1() {
        if (this._KeyID1 == null || this._KeyID1.equals("null") || this._KeyID1.length() == 0) {
            return "(null)";
        }
        return this._KeyID1;
    }

    protected String getKeyid2() {
        if (this._KeyID2 == null || this._KeyID2.equals("null") || this._KeyID2.length() == 0) {
            return "(null)";
        }
        return this._KeyID2;
    }

    protected String getKeyid3() {
        if (this._KeyID3 == null || this._KeyID3.equals("null") || this._KeyID3.length() == 0) {
            return "(null)";
        }
        return this._KeyID3;
    }

    protected String getDataScript(DataSet ds) {
        String id = this.element.getId();
        StringBuilder sb = new StringBuilder();
        StringBuffer array = new StringBuffer();
        HashSet<String> translationSet = new HashSet<String>();
        TranslationProcessor tp = this.getTranslationProcessor();
        boolean disableCheckFlag = false;
        sb.append(" var a1_").append(id).append(" = new Array();\n");
        sb.append(" var a2_").append(id).append(" = new Array();\n");
        sb.append(" var t1_").append(id).append(" = new Array();\n");
        if (ds != null) {
            if (ds.isValidColumn(ROW_DISABLED)) {
                disableCheckFlag = true;
            }
            this.setDateFormat(this.columns, ds, this.pageContext);
            for (int i = 0; i < ds.size(); ++i) {
                array.delete(0, array.length());
                array.append("['");
                ArrayList<String> arrayList = new ArrayList<String>();
                for (int x = 0; x < this._ColumnsList.size(); ++x) {
                    boolean translate;
                    String colId = this._ColumnsList.get(x);
                    String value = ds.getValue(i, colId);
                    if ("View".equals(this._Mode) || "readonly".equals(this._ModeList.get(x))) {
                        String reftypeid = this._LinkReftypeList.get(x);
                        if (OpalUtil.isEmpty(reftypeid)) {
                            reftypeid = this.getColumnRefType(colId);
                        }
                        if (OpalUtil.isNotEmpty(reftypeid)) {
                            value = this.getRefTypeDisplayValue(reftypeid, value);
                        }
                    }
                    boolean bl = translate = CONSTANT_Y.equals(this._TranslateList.get(x)) && ("readonly".equals(this._ModeList.get(x)) || this._ColumnsList.contains(colId));
                    if (translate) {
                        translationSet.add(value);
                    }
                    if (!colId.equalsIgnoreCase("reflexrule")) {
                        value = ListColumn.sanitizeHTMLValue(value);
                    }
                    value = HttpUtil.encodeURIComponent(value);
                    array.append(value.replaceAll("'", "\\\\'")).append("','");
                    arrayList.add(this.element.getId() + "_" + this._ColumnsList.get(x) + "_" + i);
                }
                this.__Grid.add(arrayList);
                if (disableCheckFlag) {
                    if (ds.getValue(i, ROW_DISABLED).equals(CONSTANT_Y)) {
                        array.append("R']");
                    } else {
                        array.append("S']");
                    }
                } else {
                    array.append("S']");
                }
                sb.append("a1_").append(id).append("[").append(i).append("] = ").append(array.toString()).append(";\n");
            }
            for (Object e : translationSet) {
                String value = (String)e;
                sb.append("\nt1_").append(id).append("['").append(SafeHTML.encodeForJavaScript(value)).append("'] = '").append(SafeHTML.encodeForJavaScript(tp.translate(value))).append("';");
            }
        }
        return sb.toString();
    }

    private String getRefTypeDisplayValue(String reftypeid, String value) {
        String displayvalue = "";
        if (OpalUtil.isNotEmpty(reftypeid) && OpalUtil.isNotEmpty(value)) {
            String key = reftypeid + ":" + value;
            if (!this.refTypeValueMap.containsKey(key)) {
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select coalesce(refdisplayvalue, refvalueid) refvalueid from refvalue where reftypeid = ? and refvalueid = ?", (Object[])new String[]{reftypeid, value});
                displayvalue = ds != null && ds.size() > 0 ? ds.getString(0, "refvalueid", "") : value;
                this.refTypeValueMap.put(key, displayvalue);
            }
            displayvalue = this.refTypeValueMap.get(key);
        }
        return displayvalue;
    }

    private String getColumnRefType(String colId) {
        DataSet dsreftype;
        if (this.reftypeMap == null) {
            this.reftypeMap = new HashMap<String, String>();
        }
        if (OpalUtil.isNotEmpty(colId) && !this.reftypeMap.containsKey(colId) && (dsreftype = this.getQueryProcessor().getPreparedSqlDataSet("select reftypeid from sdclink where sdcid = (select sdc.sdcid from sdc where sdc.tableid = ?) and sdccolumnid = ? and linksdcid = 'RefType'", (Object[])new String[]{this.element.getProperty("tableid").toLowerCase(), colId})) != null && dsreftype.size() > 0) {
            this.reftypeMap.put(colId, dsreftype.getString(0, "reftypeid", ""));
        }
        return this.reftypeMap.containsKey(colId) ? this.reftypeMap.get(colId) : "";
    }

    protected String getColumnScript() {
        StringBuilder sb = new StringBuilder();
        StringBuilder array = new StringBuilder();
        String id = this.element.getId();
        sb.append(" var c1_").append(id).append(" = new Array();\n");
        for (int i = 0; i < this._ColumnsList.size(); ++i) {
            String columnid = this._ColumnsList.get(i);
            String ctype = "S";
            if (this._CustomList.contains(columnid)) {
                ctype = "C";
            }
            String dateformat = this.timeZoneIndependentColumnList.contains(columnid) ? "O" + this._DateFormatList.get(i) : this._DateFormatList.get(i);
            array.setLength(0);
            array.append("[\"");
            array.append(this._ColumnsList.get(i));
            array.append("\", \"");
            array.append(ctype);
            array.append("\", \"");
            array.append(this._WidthList.get(i));
            array.append("\", \"");
            array.append(this._AlignList.get(i));
            array.append("\", \"");
            array.append(this._ModeList.get(i));
            array.append("\", \"");
            array.append(this._LinkHrefList.get(i));
            array.append("\", \"");
            array.append(this._LinkTargetList.get(i));
            array.append("\", \"");
            array.append(this._LinkTipList.get(i));
            array.append("\", \"");
            array.append(this._PseudoList.get(i));
            array.append("\", \"");
            array.append(this._DisplayValueList.get(i));
            array.append("\", \"");
            array.append(this._LookupList.get(i));
            array.append("\", \"");
            array.append(this._DisableOnSaveList.get(i));
            array.append("\", \"");
            array.append(this._DefaultValueList.get(i));
            array.append("\", ");
            array.append(this._CollapseList.get(i));
            array.append(", \"");
            array.append(this._TitleList.get(i));
            array.append("\", \"");
            array.append(dateformat);
            array.append("\", \"");
            array.append(this._TranslateList.get(i));
            array.append("\", \"");
            array.append(this._DynamicLook.get(i));
            array.append("\", \"");
            array.append(this._DisplayReftype.get(i));
            array.append("\"]");
            sb.append("c1_").append(id).append("[").append(i).append("] = ").append(array.toString()).append(";\n");
        }
        return sb.toString();
    }

    protected String getPropertyHandlerFields() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n<input type='hidden' name='__propertyhandler_").append(this.element.getId()).append("' value='").append(this._PropertyHandlerClass).append("'/>\n");
        Set<String> keySet = this.__PropertyHandlerMap.keySet();
        for (String aKeySet : keySet) {
            String key = aKeySet;
            String fieldid = key.equals("edata") || key.equals("eremove") || key.equals("emodified") || key.equals("eunlink") ? "forward_" + key + "_" + this.element.getId() : "__" + this.element.getId() + "_" + key;
            sb.append("<input type='hidden' name='").append(fieldid).append("' id='").append(fieldid);
            sb.append("' value='").append(this.__PropertyHandlerMap.get(key)).append("'>\n");
        }
        return sb.toString();
    }

    protected String getdisplayRefTytpeData(String id) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this._DisplayReftype.size(); ++i) {
            DataSet dsRefData;
            String refTypeId = this._DisplayReftype.get(i);
            if (refTypeId.length() <= 0 || (dsRefData = this.getQueryProcessor().getRefTypeDataSet(refTypeId)).getRowCount() <= 0) continue;
            sb.append("\n " + id + "_refdisplayicon['" + SafeHTML.encodeForJavaScript(refTypeId) + "'] = new Array();");
            for (int r = 0; r < dsRefData.getRowCount(); ++r) {
                String refValue = dsRefData.getValue(r, "refvalueid", dsRefData.getValue(r, "refvaluedesc"));
                String refDispIcon = dsRefData.getValue(r, "refdisplayicon");
                String refDisplayValue = dsRefData.getValue(r, "refdisplayvalue", dsRefData.getValue(r, "refvaluedesc"));
                sb.append("\n " + id + "_refdisplayicon['" + SafeHTML.encodeForJavaScript(refTypeId) + "'].push('" + SafeHTML.encodeForJavaScript(refValue) + ";" + SafeHTML.encodeForJavaScript(refDispIcon) + ";" + SafeHTML.encodeForJavaScript(refDisplayValue) + "');");
            }
            sb.append("\n ");
        }
        return sb.toString();
    }

    protected String getLinksData(String id) {
        StringBuilder sb = new StringBuilder();
        HashMap<String, DataSet> rmap = new HashMap<String, DataSet>();
        ArrayList<String> dropdowncolumnlist = new ArrayList<String>();
        sb.append("function ").append(id).append("_dd_getDDOptions( columnid ) {");
        sb.append("    var arr = new Array();");
        sb.append("    arr.push( ';' );");
        sb.append(this.renderDropDownJavaScript(dropdowncolumnlist, rmap));
        sb.append("    return arr;");
        sb.append("}");
        StringBuilder _sb = new StringBuilder();
        sb.append("function ").append(id).append("_dd_getDisplayValue( columnid, value ) {");
        _sb.append("function ").append(id).append("_dd_getDataValue( columnid, displayvalue ) {");
        Iterator<String> iterator = rmap.keySet().iterator();
        while (iterator.hasNext()) {
            String o;
            String columnid = o = iterator.next();
            sb.append("if ( columnid == '").append(columnid).append("' ) {");
            _sb.append("if ( columnid == '").append(columnid).append("' ) {");
            DataSet ds = rmap.get(columnid);
            for (int i = 0; i < ds.size(); ++i) {
                String r1 = ds.getValue(i, "R1");
                String r2 = ds.getValue(i, "R2");
                if (r2 == null || r2.equals("null") || r2.trim().length() == 0) {
                    r2 = r1;
                }
                r2 = StringUtil.replaceAll(r2, "'", "\\'");
                r1 = StringUtil.replaceAll(r1, "'", "\\'");
                sb.append("if ( value == '").append(r1).append("' ) return '").append(r2).append("';");
                _sb.append("if ( displayvalue == '").append(r2).append("' ) return '").append(r1).append("';");
            }
            sb.append("}");
            _sb.append("}");
        }
        sb.append("return value;");
        _sb.append("return displayvalue;");
        sb.append("}");
        _sb.append("}");
        sb.append(_sb.toString());
        return sb.toString();
    }

    protected String renderDropDownJavaScript(List<String> dropdowncolumnlist, Map<String, DataSet> rmap) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this._ColumnsList.size(); ++i) {
            DataSet keyvalueds;
            boolean translate;
            String columnid = this._ColumnsList.get(i);
            String columnmode = this._ModeList.get(i);
            boolean bl = translate = CONSTANT_Y.equals(this._TranslateList.get(i)) && columnmode.equals("dropdownlist");
            if (!columnmode.startsWith("dropdown")) continue;
            Object ddobject = this._DropDownSQLList.get(i);
            if (ddobject instanceof PropertyList) {
                PropertyList dropdowndefinition = (PropertyList)ddobject;
                String ddsdcid = dropdowndefinition.getProperty("sdcid");
                String ddqueryfrom = dropdowndefinition.getProperty("queryfrom");
                String ddquerywhere = dropdowndefinition.getProperty("querywhere");
                String ddvaluecolumn = dropdowndefinition.getProperty("valuecolumn");
                String dddisplaycolumn = dropdowndefinition.getProperty("displaycolumn");
                SDIRequest sdirequest = new SDIRequest();
                sdirequest.setSDCid(ddsdcid);
                String orderby = "";
                if (StringUtil.getLen(ddqueryfrom) > 0L) {
                    sdirequest.setQueryFrom(ddqueryfrom);
                } else {
                    SDCProcessor sdcProcessor = this.getSDCProcessor();
                    String tableid = (String)sdcProcessor.getSDCProperties(ddsdcid).get("tableid");
                    sdirequest.setQueryFrom(tableid);
                    orderby = (String)sdcProcessor.getSDCProperties(ddsdcid).get("keycolid1");
                }
                sdirequest.setQueryWhere(OpalUtil.parseRequestString(this.requestContext, ddquerywhere, true, this._IsOra));
                sdirequest.setQueryOrderBy(dropdowndefinition.getProperty("queryorderby", orderby));
                if (StringUtil.getLen(ddvaluecolumn) > 0L) {
                    sdirequest.setRequestItem("primary[" + ddvaluecolumn + (StringUtil.getLen(dddisplaycolumn) > 0L ? "," + dddisplaycolumn : "") + "]");
                } else {
                    sdirequest.setRequestItem("primary");
                }
                sdirequest.setShowTemplates(true);
                SDIData sdidata = this.getSDIProcessor().getSDIData(sdirequest);
                DataSet primary = sdidata.getDataset("primary");
                if (primary == null) continue;
                DataSet keyvalueds2 = new DataSet();
                sb.append("    if ( columnid == '").append(columnid).append("' ) {");
                for (int row = 0; row < primary.size(); ++row) {
                    String r1 = primary.getValue(row, ddvaluecolumn, "");
                    String r2 = StringUtil.getLen(dddisplaycolumn) > 0L ? primary.getValue(row, dddisplaycolumn, "") : r1;
                    String string = r2 = translate ? this.getTranslationProcessor().translate(r2) : r2;
                    if (r1.toLowerCase().contains("</script")) continue;
                    r1 = SafeHTML.encodeForJavaScript(r1.replaceAll("'", "\\\\'"));
                    r2 = SafeHTML.encodeForJavaScript(r2.replaceAll("'", "\\\\'"));
                    sb.append("    arr.push( '").append(r1).append(";").append(r2).append("' );");
                    int krow = keyvalueds2.addRow();
                    keyvalueds2.setString(krow, "R1", r1);
                    keyvalueds2.setString(krow, "R2", r2);
                }
                sb.append("}");
                dropdowncolumnlist.add(columnid);
                rmap.put(columnid, keyvalueds2);
                continue;
            }
            if (!(ddobject instanceof String)) continue;
            String dropdownsql = (String)ddobject;
            if (StringUtil.getLen(dropdownsql) > 0L) {
                dropdownsql = OpalUtil.parseRequestString(this.requestContext, dropdownsql);
                DataSet _ds = this.getQueryProcessor().getSqlDataSet(dropdownsql);
                if (_ds == null) continue;
                keyvalueds = new DataSet();
                String dsColumn = _ds.getColumnId(0);
                sb.append("    if ( columnid == '").append(columnid).append("' ) {");
                int columnCount = _ds.getColumnCount();
                for (int x = 0; x < _ds.size(); ++x) {
                    String r2;
                    String r1 = _ds.getValue(x, dsColumn);
                    String displayValue = columnCount >= 2 ? _ds.getValue(x, _ds.getColumnId(1)) : r1;
                    String string = r2 = translate ? this.getTranslationProcessor().translate(displayValue) : displayValue;
                    if (r1.toLowerCase().contains("</script")) continue;
                    r1 = SafeHTML.encodeForJavaScript(r1.replaceAll("'", "\\\\'"));
                    r2 = SafeHTML.encodeForJavaScript(r2.replaceAll("'", "\\\\'"));
                    sb.append("    arr.push( '").append(r1).append(";").append(r2).append("' );");
                    int krow = keyvalueds.addRow();
                    keyvalueds.setString(krow, "R1", r1);
                    keyvalueds.setString(krow, "R2", r2);
                }
                sb.append("}");
                dropdowncolumnlist.add(columnid);
                rmap.put(columnid, keyvalueds);
                continue;
            }
            String linkreftypeid = this._LinkReftypeList.get(i);
            if (StringUtil.getLen(linkreftypeid) <= 0L) continue;
            keyvalueds = new DataSet();
            if (linkreftypeid.indexOf(";") == -1) {
                sb.append("    if ( columnid == '").append(columnid).append("' ) {");
                DataSet reftypeds = this.getQueryProcessor().getRefTypeDataSet(linkreftypeid);
                for (int x = 0; x < reftypeds.size(); ++x) {
                    String r1 = reftypeds.getValue(x, "REFVALUEID");
                    String r2 = reftypeds.getValue(x, "REFDISPLAYVALUE");
                    if (r2 == null || r2.equals("null") || r2.trim().length() == 0) {
                        r2 = r1;
                    }
                    String string = r2 = translate ? this.getTranslationProcessor().translate(r2) : r2;
                    if (r1.toLowerCase().contains("</script")) continue;
                    r1 = SafeHTML.encodeForJavaScript(r1.replaceAll("'", "\\\\'"));
                    r2 = SafeHTML.encodeForJavaScript(r2.replaceAll("'", "\\\\'"));
                    sb.append("    arr.push( '").append(r1).append(";").append(r2).append("' );");
                    int krow = keyvalueds.addRow();
                    keyvalueds.setString(krow, "R1", r1);
                    keyvalueds.setString(krow, "R2", r2);
                }
                sb.append("}");
                rmap.put(columnid, keyvalueds);
            } else {
                String[] r;
                sb.append("    if ( columnid == '").append(columnid).append("' ) {");
                for (String reftype : r = StringUtil.split(linkreftypeid, ";")) {
                    String r2;
                    String r1;
                    if (reftype.indexOf(":") == -1) {
                        r1 = reftype;
                        r2 = reftype;
                    } else {
                        r1 = reftype.substring(0, reftype.indexOf(":"));
                        r2 = reftype.substring(reftype.indexOf(":") + 1);
                    }
                    String string = r2 = translate ? this.getTranslationProcessor().translate(r2) : r2;
                    if (r1.toLowerCase().contains("</script")) continue;
                    r1 = SafeHTML.encodeForJavaScript(r1.replaceAll("'", "\\\\'"));
                    r2 = SafeHTML.encodeForJavaScript(r2.replaceAll("'", "\\\\'"));
                    sb.append("    arr.push( '").append(r1).append(";").append(r2).append("' );");
                    int krow = keyvalueds.addRow();
                    keyvalueds.setString(krow, "R1", r1);
                    keyvalueds.setString(krow, "R2", r2);
                }
                sb.append("}");
                rmap.put(columnid, keyvalueds);
            }
            dropdowncolumnlist.add(columnid);
        }
        return sb.toString();
    }

    protected boolean doesColumnExists(String columnid) {
        return this._TableMetaData == null || this._TableMetaData.isEmpty() || this._TableMetaData.size() == 0 || this._TableMetaData.containsKey(columnid.toLowerCase());
    }

    protected String getColumnDataType(String columnid) {
        return (String)this._TableMetaData.get(columnid.toLowerCase());
    }

    public HashMap<String, Object> getColumnMap() {
        return this._ColumnMap;
    }

    protected void addToColumnCollection(String columnid, String columnMode, String defaultValue) {
        this._ColumnsList.add(columnid);
        this._ModeList.add(columnMode);
        this._TitleList.add("");
        this._WidthList.add("");
        this._AlignList.add("");
        this._LinkHrefList.add("");
        this._LinkTargetList.add("");
        this._LinkTipList.add("");
        this._PseudoList.add("");
        this._CustomList.add("");
        this._DisplayValueList.add("");
        this._LinkReftypeList.add("");
        this._LookupList.add("");
        this._DisableOnSaveList.add("");
        this._DefaultValueList.add(defaultValue);
        this._CollapseList.add("");
        this._DateFormatList.add("");
        this._DropDownSQLList.add("");
        this._TranslateList.add("N");
        this._DynamicLook.add("N");
        this._DisplayReftype.add("");
    }

    protected void putCollectionToPropertyHandler(PropertyListCollection collection, String propertyHandlerKey, String[] collectionKeys) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < collection.size(); ++i) {
            for (String collectionKey : collectionKeys) {
                temp.append(collection.getPropertyList(i).getProperty(collectionKey));
                temp.append("|");
            }
            temp.setLength(temp.length() - "|".length());
            temp.append(";");
        }
        if (temp.length() > 0) {
            temp.setLength(temp.length() - ";".length());
            this.putPropertyHandlerKey(propertyHandlerKey, temp.toString());
        }
    }

    public static String getSelectSQL(List columns, String fromclause, List where, String orderclause, boolean distinct) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        if (distinct) {
            sql.append(" DISTINCT ");
        }
        for (Object column : columns) {
            sql.append(((DBColumn)column).getNvlImpl());
            sql.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" FROM ").append(fromclause);
        sql.append(" WHERE ");
        if (where.size() > 0) {
            sql.append(where.get(0));
            for (int i = 1; i < where.size(); ++i) {
                sql.append(" AND ").append(where.get(i));
            }
        }
        if (orderclause != null) {
            sql.append(" ORDER BY ").append(orderclause);
        }
        return sql.toString();
    }

    public static String getSelectSQL(List columns, String fromclause, String whereclause, String orderclause, boolean distinct) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        if (distinct) {
            sql.append(" DISTINCT ");
        }
        for (Object column : columns) {
            sql.append(((DBColumn)column).getNvlImpl());
            sql.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" FROM ").append(fromclause);
        HashMap<String, String> hmContent = new HashMap<String, String>();
        hmContent.put("currentuser", currentUser);
        whereclause = BaseItem.getSubstitutedContent(whereclause, hmContent);
        sql.append(" WHERE ").append(whereclause);
        if (orderclause != null && orderclause.length() > 0) {
            sql.append(" ORDER BY ").append(orderclause);
        }
        return sql.toString();
    }

    protected void setDateFormat(PropertyListCollection columns, DataSet ds, PageContext pageContext) {
        for (Object column : columns) {
            String columnid = ((PropertyList)column).getProperty("column");
            String formatStr = ((PropertyList)column).getProperty("format");
            if (columnid.length() <= 0) continue;
            if (formatStr.length() > 0) {
                ds.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(pageContext, formatStr, !this.timeZoneIndependentColumnList.contains(columnid)));
                continue;
            }
            if (!this.timeZoneIndependentColumnList.contains(columnid)) continue;
            ds.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(pageContext, "O", false));
        }
    }

    protected boolean validateMasterKeyCollection(PropertyListCollection collection) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < collection.size(); ++i) {
            String value;
            String requestparam;
            PropertyList keylist = collection.getPropertyList(i);
            if (keylist != null) {
                String columnid = keylist.getProperty("columnid");
                requestparam = keylist.getProperty("requestparam");
                value = this.requestContext.getProperty(requestparam);
                if (value == null || value.trim().equals("")) {
                    if (!__IgnoreMasterKeyList.contains(requestparam)) {
                        return false;
                    }
                    value = "(null)";
                }
                if (!this._ColumnsList.contains(columnid)) {
                    this.addToColumnCollection(columnid, "hidden", value);
                } else {
                    this._DefaultValueList.set(this._ColumnsList.indexOf(columnid), value);
                }
            } else {
                return false;
            }
            map.put(requestparam, value);
        }
        this.putPropertyHandlerKey("masterkeyvalue", OpalUtil.map2String(map));
        return true;
    }

    protected String getValidationScript() {
        PropertyListCollection columns = this.element.getCollection("columns");
        StringBuilder html = new StringBuilder();
        html.append("\n<script>\n");
        if (this.pageContext.getAttribute("validationarray") == null) {
            html.append("var detail_validations = new Array();\n");
            this.pageContext.setAttribute("validationarray", (Object)"detail_validations");
        }
        html.append("var ").append(this.element.getId()).append("_validations = new Array();");
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList attributes = columns.getPropertyList(i);
            String validation = attributes.getProperty("validation");
            if (validation == null || validation.trim().length() <= 0) continue;
            html.append("\n");
            html.append(this.element.getId());
            html.append("_validations['");
            html.append(attributes.getProperty("column"));
            html.append("'] = '");
            html.append(validation);
            html.append("';\n");
        }
        html.append("\n</script>\n");
        return html.toString();
    }

    protected boolean isParentLocked() {
        boolean locked = false;
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select t1.connectionid, t2.lockstate, t2.checkedoutbyuserid, t2.checkedoutbydepartmentid");
        sql.append(" from rset t1, rsetitems t2");
        sql.append(" where t2.sdcid = ").append(safeSQL.addVar(this.requestContext.getProperty("sdcid")));
        sql.append(" and t2.keyid1 = ").append(safeSQL.addVar(this.getKeyid1()));
        sql.append(" and t2.keyid2 = ").append(safeSQL.addVar(this.getKeyid2()));
        sql.append(" and t2.keyid3 = ").append(safeSQL.addVar(this.getKeyid3()));
        sql.append(" and t2.rsetid = t1.rsetid");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                if (this.getConnectionId().equals(ds.getString(i, "connectionid"))) continue;
                if ("2".equals(ds.getValue(i, "lockstate"))) {
                    locked = true;
                    break;
                }
                String checkedoutbydepartmentid = ds.getString(i, "checkedoutbydepartmentid", "");
                if ("{none}".equals(checkedoutbydepartmentid)) {
                    checkedoutbydepartmentid = "";
                }
                if (checkedoutbydepartmentid.length() > 0 && !this.connectionInfo.isDepartmentMember(checkedoutbydepartmentid)) {
                    locked = true;
                    break;
                }
                String checkedoutbyuserid = ds.getString(i, "checkedoutbyuserid", "");
                if ("{none}".equals(checkedoutbyuserid)) {
                    checkedoutbyuserid = "";
                }
                if (checkedoutbyuserid.length() <= 0 || this.connectionInfo.getSysuserId().equals(checkedoutbyuserid)) continue;
                locked = true;
                break;
            }
        }
        return locked;
    }

    public boolean isViewonly() {
        return this.viewOnly;
    }

    public void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
    }

    @Override
    public boolean isVisibleInAddMode() {
        return true;
    }

    protected static String getSubstitutedContent(String content, HashMap<String, String> contentMap) {
        String substitutedContent = content;
        int fromIndex = 0;
        int openPerIndex = 0;
        int closePerIndex = 0;
        String placeHolderKey = "";
        String placeHolderValue = "";
        openPerIndex = content.indexOf("[", fromIndex);
        closePerIndex = content.indexOf("]", openPerIndex);
        while (openPerIndex > -1 && closePerIndex > -1) {
            placeHolderKey = content.substring(openPerIndex + 1, closePerIndex).toLowerCase();
            if (contentMap.containsKey(placeHolderKey)) {
                placeHolderValue = contentMap.get(placeHolderKey);
                placeHolderValue = placeHolderValue == null ? "" : placeHolderValue;
                substitutedContent = StringUtil.replaceAll(substitutedContent, "[" + placeHolderKey + "]", placeHolderValue, false);
            }
            fromIndex = openPerIndex + 1;
            openPerIndex = content.indexOf("[", fromIndex);
            closePerIndex = content.indexOf("]", openPerIndex);
        }
        return substitutedContent;
    }

    static {
        _ReservedFunctionsList.add("add");
        _ReservedFunctionsList.add("addQC");
        _ReservedFunctionsList.add("edit");
        _ReservedFunctionsList.add("remove");
        _ReservedFunctionsList.add("moveUp");
        _ReservedFunctionsList.add("moveDown");
        _ReservedFunctionsList.add("reset");
        _ReservedFunctionsList.add("link");
        _ReservedFunctionsList.add("unlink");
        _ReservedFunctionsList.add("addreplicate");
        _ReservedFunctionsList.add("copy");
        _ReservedFunctionsList.add("retest");
        _ReservedFunctionsList.add("remeasure");
        _ReservedFunctionsList.add("cancel");
        _ReservedFunctionsList.add("apply");
        _ReservedFunctionsList.add("addinstance");
        _ReservedFunctionsList.add("newinstance");
        __IgnoreMasterKeyList.add("keyid2");
        __IgnoreMasterKeyList.add("keyid3");
        COLUMNS_CORE.add("createdt");
        COLUMNS_CORE.add("createby");
        COLUMNS_CORE.add("createtool");
        COLUMNS_CORE.add("moddt");
        COLUMNS_CORE.add("modby");
        COLUMNS_CORE.add("modtool");
    }
}


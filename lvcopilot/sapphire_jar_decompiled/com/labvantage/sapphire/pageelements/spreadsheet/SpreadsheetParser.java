/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.spreadsheet;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.FileUploader;
import com.labvantage.sapphire.pageelements.controls.Tree;
import com.labvantage.sapphire.pageelements.forms.PropertyBuilder;
import com.labvantage.sapphire.pageelements.spreadsheet.Spreadsheet;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SpreadsheetParser
extends BaseElement {
    private static final String IMAGE_COLUMNS_C = "WEB-CORE/elements/richtext/images/columns.gif";
    private static final String IMAGE_COLUMNS_E = "WEB-CORE/elements/richtext/images/columns.gif";
    private static final String IMAGE_FIELD = "WEB-CORE/elements/richtext/images/field.gif";
    private static final int STYLE_CUSTOM = 0;
    private static final int STYLE_SIMPLEGRID = 1;
    private static final int STYLE_CROSSTAB = 2;
    public static final String PROPERTY_DATA = "data";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_FIELDIDS = "fieldids";
    public static final String PROPERTY_CALLBACK = "callback";
    public static final String PROPERTY_STYLE = "style";
    public static final String PROPERTY_SHOWBUTTONS = "showbuttons";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_EMBEDDED = "embedded";
    private PropertyList props;
    private DataSet data;
    private int style;
    private PropertyList userConfig;
    private String fieldIds;
    private String callbackFunc;
    private boolean showBtns = true;
    private boolean viewOnly = false;
    private String filetype = "";
    private String delimiter = "";
    private int firstBlankRow = -2;
    private ArrayList<Integer> firstBlankRowArray = new ArrayList();
    private int endRow = -1;
    private int firstBlankCol = -2;
    private ArrayList<Integer> firstBlankColArray = new ArrayList();
    private Mode mode = Mode.PARSER;
    private int endCol = -1;
    private boolean embedded = false;

    public SpreadsheetParser(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            this.setUp(pageproperties, (HttpServletRequest)pageContext.getRequest());
            this.prepareData(this.data);
        }
        catch (Exception e) {
            this.props = null;
            this.data = null;
            this.logger.error("Could not set up spread sheet parser: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void prepareData(DataSet data) {
        if (data != null) {
            String colid;
            int row;
            int col;
            int rowcheck = -1;
            for (int row2 = 0; row2 < data.size(); ++row2) {
                HashMap rowob = (HashMap)data.get(row2);
                if (rowob.size() == 0) {
                    if (this.firstBlankRow == -1) {
                        this.firstBlankRow = row2;
                        this.firstBlankRowArray.add(row2);
                    } else if (this.firstBlankRow > -1 && rowcheck > -1 && row2 == rowcheck + 1) {
                        this.firstBlankRowArray.add(row2);
                    }
                    if (this.endRow != -1) continue;
                    this.endRow = row2;
                    continue;
                }
                rowcheck = row2;
                if (this.firstBlankRow == -2) {
                    this.firstBlankRow = -1;
                }
                this.endRow = -1;
            }
            int colcheck = -1;
            for (col = 0; col < data.getColumnCount(); ++col) {
                String values = data.getColumnValues(data.getColumnId(col), " ").trim();
                if (values.length() == 0) {
                    if (this.firstBlankCol == -1) {
                        this.firstBlankCol = col;
                        this.firstBlankColArray.add(col);
                    } else if (this.firstBlankCol > -1 && colcheck > -1 && col == colcheck + 1) {
                        this.firstBlankColArray.add(col);
                    }
                    if (this.endCol != -1) continue;
                    this.endCol = col;
                    continue;
                }
                colcheck = col;
                if (this.firstBlankCol == -2) {
                    this.firstBlankCol = -1;
                }
                this.endCol = -1;
            }
            if (this.firstBlankRow > -1 && this.firstBlankRow != this.endRow) {
                for (row = 0; row < this.firstBlankRowArray.size(); ++row) {
                    for (int col2 = 0; col2 < data.getColumnCount(); ++col2) {
                        data.setValue(this.firstBlankRowArray.get(row), data.getColumnId(col2), "{[FBR]}");
                    }
                }
            }
            if (this.endRow == -1) {
                this.endRow = data.addRow();
            }
            for (col = 0; col < data.getColumnCount(); ++col) {
                data.setValue(this.endRow, data.getColumnId(col), "{[EOF]}");
            }
            if (this.firstBlankCol > -1 && this.firstBlankCol != this.endCol) {
                for (row = 0; row < this.endRow; ++row) {
                    for (int col3 = 0; col3 < this.firstBlankColArray.size(); ++col3) {
                        String colid2 = data.getColumnId(this.firstBlankColArray.get(col3));
                        data.setValue(row, colid2, "{[FBC]}");
                    }
                }
            }
            if (this.endCol == -1) {
                colid = "eof";
                data.addColumn(colid, 0);
                this.endCol = data.getColumnCount() - 1;
            } else {
                colid = data.getColumnId(this.endCol);
            }
            for (int row3 = 0; row3 < this.endRow + 1; ++row3) {
                data.setValue(row3, colid, "{[EOF]}");
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void setUp(PropertyList pagedata, HttpServletRequest request) throws Exception {
        try {
            this.mode = Mode.valueOf(pagedata.getProperty("mode", Mode.PARSER.toString()).toUpperCase());
        }
        catch (Exception e) {
            this.mode = Mode.PARSER;
        }
        pagedata.setProperty("jsrequest", "exclude=data|properties");
        this.viewOnly = pagedata.getProperty(PROPERTY_VIEWONLY, "Y").equalsIgnoreCase("Y");
        this.embedded = pagedata.getProperty(PROPERTY_EMBEDDED, "Y").equalsIgnoreCase("Y");
        boolean bl = this.showBtns = !pagedata.getProperty(PROPERTY_SHOWBUTTONS, "Y").equalsIgnoreCase("N");
        if (this.mode == Mode.PARSER) {
            String ts;
            String show = pagedata.getProperty("showtitle", "N");
            this.logger.debug("show = " + show);
            PropertyList layout = pagedata.getPropertyList("layout");
            if (layout != null) {
                layout.setProperty("hideshadow", "Y");
                if (show.equalsIgnoreCase("N")) {
                    layout.setProperty("hidetitle", "Y");
                } else {
                    layout.setProperty("hidetitle", "N");
                }
            }
            this.style = (ts = pagedata.getProperty(PROPERTY_STYLE, "")).equalsIgnoreCase("crosstab") || ts.equalsIgnoreCase("c") ? 2 : (ts.equalsIgnoreCase("simplegrid") || ts.equalsIgnoreCase("s") ? 1 : 0);
            String propertylist = pagedata.getProperty(PROPERTY_PROPERTIES, "");
            if (propertylist.length() > 0) {
                this.logger.debug("Propertylist provided.");
                if (propertylist.startsWith("<propertylist")) {
                    this.logger.debug("Propertylist in XML format.");
                    try {
                        this.props = new PropertyList();
                        this.props.setPropertyList(propertylist);
                    }
                    catch (Exception e) {
                        throw new SapphireException("Could not create propertylist(1): " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                    }
                }
            } else {
                this.logger.debug("No propertylist provided...start.");
                this.props = new PropertyList();
            }
            if (this.props == null) throw new SapphireException("Propertylist could not be created.");
            String sdata = pagedata.getProperty(PROPERTY_DATA, "");
            if (sdata.length() == 0) {
                this.logger.debug("No data provided... start.");
                this.data = null;
            } else {
                this.data = new DataSet(sdata);
            }
            this.fieldIds = pagedata.getProperty(PROPERTY_FIELDIDS, "");
            this.callbackFunc = pagedata.getProperty(PROPERTY_CALLBACK, "");
            this.logger.debug("fieldIds = " + this.fieldIds + ", callbackFunc = " + this.callbackFunc);
            if (this.callbackFunc.length() <= 0 && this.fieldIds.length() <= 0) throw new SapphireException("Either a callback function or return field ids are required.");
            if (!this.props.containsKey("fields")) {
                this.logger.debug("Fields collection not found therefore create...");
                this.props.setProperty("fields", new PropertyListCollection());
            }
            this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
            if (this.userConfig != null) return;
            throw new SapphireException("User configuration could not be obtained.");
        }
        this.filetype = pagedata.getProperty("filetype", "");
        this.delimiter = pagedata.getProperty("delimiter", "");
    }

    public static String getObjectsHtml(PropertyList props, String connectionId, TranslationProcessor tp, HttpServletRequest request) {
        String msg;
        RequestContext rc;
        StringBuffer sb = new StringBuffer();
        PropertyList userConfig = null;
        if (request != null && (rc = RequestContext.getInstance(request)) != null) {
            if (connectionId == null || connectionId.length() == 0) {
                connectionId = rc.getConnectionId();
            }
            userConfig = rc.getPropertyList("userconfig");
        }
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            if (props != null && (props.containsKey("fields") || props.containsKey("columns"))) {
                PropertyList tree = new PropertyList();
                PropertyListCollection rootitems = new PropertyListCollection();
                PropertyList fieldrootitem = new PropertyList();
                String text = "Fields";
                fieldrootitem.setProperty("expanded", "Y");
                fieldrootitem.setProperty("translate", "N");
                fieldrootitem.setProperty("showexpandcollapse", "Y");
                fieldrootitem.setProperty("expandedimage", "WEB-CORE/elements/richtext/images/columns.gif");
                fieldrootitem.setProperty("collapsedimage", "WEB-CORE/elements/richtext/images/columns.gif");
                String collectionid = "fields";
                String idprop = "fieldid";
                fieldrootitem.setProperty("prehtml", "<div id=\"ssparser_fi\" itemlocked=false collectionid=\"" + collectionid + "\" itemid=\"\">");
                fieldrootitem.setProperty("posthtml", "</div>");
                PropertyListCollection fields = props.getCollection(collectionid);
                if (tp != null) {
                    text = tp.translate(text);
                }
                String nofieldstext = "No Fields";
                if (tp != null) {
                    nofieldstext = tp.translate(nofieldstext);
                }
                PropertyListCollection subitems = new PropertyListCollection();
                if (fields != null && fields.size() > 0) {
                    fieldrootitem.setProperty("text", text + " (" + fields.size() + ")");
                    for (int i = 0; i < fields.size(); ++i) {
                        PropertyList field = fields.getPropertyList(i);
                        String id = field.getProperty(idprop, "");
                        if (id.length() > 0) {
                            PropertyList subitem = new PropertyList();
                            subitem.setProperty("imageclick", "ssParser.fieldItemClick('" + id + "');");
                            subitem.setProperty("textclick", "ssParser.fieldItemClick('" + id + "');");
                            subitem.setProperty("prehtml", "<div id=\"ssparser_fi_" + id + "\" itemlocked=\"false\" collectionid=\"" + collectionid + "\" itemid=\"" + id + "\">");
                            subitem.setProperty("posthtml", "</div>");
                            subitem.setProperty("text", id);
                            subitem.setProperty("expanded", "N");
                            subitem.setProperty("translate", "N");
                            subitem.setProperty("image", IMAGE_FIELD);
                            subitems.add(subitem);
                            continue;
                        }
                        Logger.logWarn("getObjectsHtml - Field located (" + i + ") with no Id.");
                    }
                } else {
                    fieldrootitem.setProperty("text", text + " (0)");
                    PropertyList subitem = new PropertyList();
                    subitem.setProperty("text", nofieldstext);
                    subitem.setProperty("expanded", "N");
                    subitem.setProperty("translate", "N");
                    subitem.setProperty("image", "");
                    subitems.add(subitem);
                }
                fieldrootitem.setProperty("items", subitems);
                rootitems.add(fieldrootitem);
                tree.setProperty("rootitems", rootitems);
                Tree t = new Tree(connectionId, tp);
                if (userConfig != null) {
                    t.setUserConfig(userConfig);
                }
                t.setId("object_fields");
                t.setElementProperties(tree);
                sb.append(t.getHtml());
            } else {
                msg = "Form properties not correct.";
                if (tp != null) {
                    msg = tp.translate(msg);
                }
                sb.append(msg);
            }
        } else {
            msg = "No connection Id provided.";
            if (tp != null) {
                msg = tp.translate(msg);
            }
            sb.append(msg);
        }
        return sb.toString();
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        if (this.browser.isWebkit()) {
            html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/richtext/stylesheets/formbuilder_webkit.css", this.pageContext) + "\" type=\"text/css\">");
        } else {
            html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/richtext/stylesheets/formbuilder.css", this.pageContext) + "\" type=\"text/css\">");
        }
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/spreadsheet/scripts/spreadsheetparser.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/richtext/scripts/richtexteditor.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/modules/webadmin/scripts/editors.js\"></script>");
        return html;
    }

    private StringBuffer getEndScript(PropertyList props, int style, String fieldIds, String callback, String defaultNoProps, int firstBlankRow, int firstBlankCol, int endRow, int endCol, boolean embedded, boolean viewOnly) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append("ssParser.properties=sapphire.util.propertyList.create(").append(props.toJSONString()).append(");");
        html.append("ssParser.style=").append(style).append(";");
        html.append("ssParser.viewOnly=").append(viewOnly).append(";");
        html.append("ssParser.embedded=").append(embedded).append(";");
        html.append("ssParser.fieldIds='").append(fieldIds).append("';");
        html.append("ssParser.callback='").append(callback).append("';");
        html.append("ssParser.defaultNoProps='").append(defaultNoProps).append("';");
        html.append("ssParser.endRow=").append(endRow).append(";");
        html.append("ssParser.firstBlankRow=").append(firstBlankRow).append(";");
        html.append("ssParser.firstBlankRowArray=").append(new JSONArray(this.firstBlankRowArray)).append(";");
        html.append("ssParser.firstBlankCol=").append(firstBlankCol).append(";");
        html.append("ssParser.firstBlankColArray=").append(new JSONArray(this.firstBlankColArray)).append(";");
        html.append("ssParser.endCol=").append(endCol).append(";");
        html.append("ssParser.restoreMaximise=").append(this.userConfig != null && this.userConfig.getProperty("parser_maximised", "N").equalsIgnoreCase("Y")).append(";");
        html.append("sapphire.ui.resize.setResizeEvent(form_objects_row,ssParser.resizeObjectsEnd);");
        html.append("sapphire.ui.resize.setResizeEvent(form_leftbar,ssParser.resizeWidthEnd);");
        html.append("var richText = new RichTextEditor();");
        html.append("</script>");
        return html;
    }

    private String getSpreadsheetHtml(DataSet data) {
        Spreadsheet ss = new Spreadsheet(this.pageContext);
        ss.setElementid("spreadsheetElement");
        ss.setWidth("100%");
        ss.setHeight("100%");
        ss.setSpreadsheetData(data);
        ss.addEvent("mousedown", "ssParser.cellDown");
        ss.addEvent("mouseover", "ssParser.cellOver");
        ss.addEvent("mouseenter", "ssParser.cellEnter");
        ss.addEvent("mouseleave", "ssParser.cellLeave");
        ss.addEvent("dragstart", "ssParser.cellDrag");
        ss.addEvent("selectstart", "ssParser.cellSelect");
        return ss.getHtml();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.mode == Mode.PARSER) {
            if (this.props != null) {
                html.append(this.getScriptAndStyle());
                html.append(this.getDivsHtml());
                html.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%;height:100%;table-layout:auto;\">");
                html.append("<tbody>");
                html.append("<tr>");
                html.append(this.getLeftBarStart());
                html.append("<table style=\"table-layout:fixed;\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar\">");
                html.append("<tbody>");
                html.append("<tr class=\"layout_sidebar_tab_row\">");
                html.append("<td align=\"left\" class=\"layout_sidebar_tab layout_sidebar_tab_back\" style=\"width:16px;\">");
                html.append("<img src=\"WEB-CORE/elements/richtext/images/objects-h.gif\" width=\"16\" height=\"17\">");
                html.append("</td>");
                html.append("<td align=\"left\" class=\"layout_sidebar_tab layout_sidebar_tab_back\" valign=\"middle\" nowrap>");
                html.append(this.getTranslationProcessor().translate("Objects"));
                html.append("</td>");
                html.append("<td align=right valign=middle class=\"layout_sidebar_expandcollapse layout_sidebar_tab_back\">");
                html.append("<img src=\"WEB-OPAL/layouts/generic/images/sidebar_collapse.gif\" title=\"Collapse\" id=\"form_leftbar_collapse\" style=\"display:block;cursor: pointer;\" onclick=\"ssParser.collapseLeftBar();\">");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr class=\"form_merge\">");
                html.append("<td colspan=\"3\">");
                html.append("</td>");
                html.append("</tr>");
                String rowheight = this.userConfig.getProperty("ssparser_objects", "200px");
                if (!rowheight.endsWith("px")) {
                    rowheight = rowheight + "px";
                }
                html.append("<tr id=\"form_objects_row\" style=\"height:").append(rowheight).append(";\">");
                html.append("<td colspan=\"3\" class=\"form_bar_parentcell\">");
                html.append("<table width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar_childtable\">");
                html.append("<tbody>");
                html.append("<tr>");
                html.append("<td valign=\"top\" align=\"left\" class=\"form_bar_childcell\" id=\"form_objects_content_cell\" nowrap>");
                html.append("<div align=\"left\" id=\"form_objects_content\"  style=\"overflow-y:auto;overflow-x:auto;width:100%;height:").append(rowheight).append(";\">");
                html.append(SpreadsheetParser.getObjectsHtml(this.props, this.getConnectionId(), this.getTranslationProcessor(), (HttpServletRequest)this.pageContext.getRequest()));
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr class=\"layout_sidebar_tab_row\" style=\"cursor:n-resize;\" onmousedown=\"sapphire.ui.resize.start(form_objects_row,[],'s');\">");
                html.append("<td align=\"left\" class=\"layout_sidebar_tab layout_sidebar_tab_back\" style=\"width:16px;\">");
                html.append("<img src=\"WEB-CORE/elements/richtext/images/props-h.gif\" width=\"16\" height=\"17\">");
                html.append("</td>");
                html.append("<td valign=\"middle\" class=\"layout_sidebar_tab layout_sidebar_tab_back\"  nowrap >");
                html.append(this.getTranslationProcessor().translate("Properties"));
                html.append("</td>");
                html.append("<td align=right valign=middle class=\"layout_sidebar_expandcollapse layout_sidebar_tab_back\">");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr class=\"form_merge\">");
                html.append("<td colspan=\"3\">");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr id=\"form_properties_row\" style=\"height:1000px;\">");
                html.append("<td colspan=\"3\" class=\"form_bar_parentcell\">");
                html.append("<table width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"form_bar_childtable\">");
                html.append("<tbody>");
                html.append("<tr >");
                html.append("<td  valign=\"top\" align=\"left\" class=\"form_bar_childcell\" id=\"form_properties_content_cell\" nowrap>");
                html.append("<div align=\"left\" id=\"form_properties_content\"  style=\"overflow-y:auto;overflow-x:auto;width:100%;height:100%;\">");
                String defaultNoProps = SpreadsheetParser.getPropertiesHtml(null, this.props, this.style, this.viewOnly, this.getConnectionId(), this.getTranslationProcessor());
                html.append(defaultNoProps);
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                html.append(this.getLeftBarEnd());
                html.append("<td colspan=\"2\" style=\"vertical-align:top;\">");
                html.append("<div id=\"spreadsheetcontainer\" style=\"width:100%;height:100px;\">");
                html.append("<table style=\"table-layout:fixed;\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
                html.append("<tbody>");
                html.append("<tr class=\"form_shadow\">");
                html.append("<td colspan=\"2\" align=\"left\" class=\"layout_sidebar_tab\" nowrap>");
                html.append("</td>");
                html.append("</tr>");
                html.append("<tr style=\"height:auto;\">");
                html.append("<td colspan=\"2\" style=\"position:relative;\">");
                html.append("<div id=\"ss_loading\" style=\"position:absolute;top:2px;left:2px;\">Loading...</div>");
                html.append("<div id=\"form_richtext_content\"  style=\"display:none;overflow:auto;width:100%;height:100%;position:relative;\">");
                html.append(this.getSpreadsheetHtml(this.data));
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
                if (this.showBtns) {
                    html.append("<tr style=\"height:32px;\">");
                    html.append("<td valign=\"middle\" align=\"left\" class=\"form_buttons\" nowrap>");
                    html.append("<div id=\"form_devbuttons_content\"  style=\"overflow:auto;width:100%;height:100%;\">");
                    html.append(this.getDevButtonsHtml());
                    html.append("</div>");
                    html.append("</td>");
                    html.append("<td valign=\"middle\" align=\"right\" class=\"form_buttons\" nowrap>");
                    html.append("<div id=\"form_buttons_content\"  style=\"overflow:auto;width:100%;height:100%;\">");
                    html.append(this.getButtonsHtml(this.viewOnly));
                    html.append("</div>");
                    html.append("</td>");
                    html.append("</tr>");
                }
                html.append("</tbody>");
                html.append("</table>");
                html.append("</div>");
                html.append("</td>");
                html.append("</tr>");
                html.append("</tbody>");
                html.append("</table>");
                html.append(this.getEndScript(this.props, this.style, this.fieldIds, this.callbackFunc, defaultNoProps, this.firstBlankRow, this.firstBlankCol, this.endRow, this.endCol, this.embedded, this.viewOnly));
            } else {
                html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load editor.")).append("</font>");
            }
        } else {
            FileUploader fileUploader = new FileUploader(this.pageContext);
            fileUploader.setElementid("uploadexamplefile");
            fileUploader.setUploadMultiple(false);
            fileUploader.setMaxFileSize(FileManager.getUploadDFDMaxFileSizeMB(this.getConnectionId()));
            fileUploader.setUploadCallback("window.parent.dataFileDefMaint.uploadCallback");
            fileUploader.setErrorCallback("window.parent.dataFileDefMaint.uploadErrorCallback");
            if (this.filetype.equalsIgnoreCase("excel")) {
                fileUploader.addExtension(".xls");
                fileUploader.addExtension(".xlsx");
            } else {
                fileUploader.addExtension(".txt");
                if (this.delimiter.equalsIgnoreCase(",")) {
                    fileUploader.addExtension(".csv");
                }
            }
            html.append(fileUploader.getHtml());
            if (this.showBtns) {
                Button btn = new Button(this.pageContext);
                btn.setText(this.getTranslationProcessor().translate("Cancel"));
                btn.setId("CancelBtn");
                btn.setAction("parent.dataFileDefMaint.doInline()");
                html.append(btn.getHtml());
            }
        }
        return html.toString();
    }

    public static String getPropertiesHtml(String fieldId, PropertyList props, int style, String connectionId, TranslationProcessor tp) {
        return SpreadsheetParser.getPropertiesHtml(fieldId, props, style, false, connectionId, tp);
    }

    public static String getPropertiesHtml(String fieldId, PropertyList props, int style, boolean viewOnly, String connectionId, TranslationProcessor tp) {
        StringBuffer sb = new StringBuffer();
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            if (props != null && props.containsKey("fields")) {
                if (fieldId != null && fieldId.length() > 0) {
                    PropertyBuilder.renderStart(sb);
                    PropertyListCollection fields = props.getCollection("fields");
                    PropertyList field = fields.find("fieldid", fieldId);
                    if (field != null) {
                        String typestring;
                        String fieldid = field.getProperty("fieldid", "");
                        PropertyBuilder.renderPropTitle(sb, tp, "Field Details");
                        String sql = "select localeid, localedesc from locale order by localedesc";
                        QueryProcessor queryProcessor = new QueryProcessor(connectionId);
                        DataSet locales = queryProcessor.getSqlDataSet(sql);
                        PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "fieldid", "Id", fieldid, false, false, viewOnly);
                        PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "title", "Title", field.getProperty("title"), false, false, viewOnly);
                        PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "datatype", "Data Type", "number;string;date", field.getProperty("datatype"), viewOnly);
                        if (!viewOnly) {
                            PropertyBuilder.renderParserValidationEditor(sb, tp, fieldId, "validationrule", "Validation", field.getProperty("validationrule"), false, false, viewOnly, field.getProperty("datatype"));
                        } else {
                            PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "validationrule", "Validation", field.getProperty("validationrule"), false, false, viewOnly);
                        }
                        PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "errorprefix", "Error Prefix", field.getProperty("errorprefix"), false, false, viewOnly);
                        PropertyBuilder.renderPropTitle(sb, tp, "Data Range");
                        String type = field.getProperty("type");
                        switch (style) {
                            case 2: {
                                typestring = "cell;range;headercolumn;headerrow;input";
                                if (type.equalsIgnoreCase("cell") || type.equalsIgnoreCase("range") || type.equalsIgnoreCase("headercolumn") || type.equalsIgnoreCase("headerrow") || type.equalsIgnoreCase("input")) break;
                                type = "";
                                break;
                            }
                            case 1: {
                                typestring = "cell;column;row;input";
                                if (type.equalsIgnoreCase("cell") || type.equalsIgnoreCase("column") || type.equalsIgnoreCase("row") || type.equalsIgnoreCase("input")) break;
                                type = "";
                                break;
                            }
                            default: {
                                typestring = "cell;column;row;range;headercolumn;headerrow;input";
                            }
                        }
                        PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "type", "Type", typestring, type, viewOnly);
                        if (type.length() > 0) {
                            String startcolumn_num = field.getProperty("startcolumn", "");
                            String startcolumn_char = Spreadsheet.columnNumToChar(startcolumn_num);
                            String endcolumn_num = field.getProperty("endcolumn", "");
                            String endcolumn_char = Spreadsheet.columnNumToChar(endcolumn_num);
                            if (type.equalsIgnoreCase("cell")) {
                                PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startcolumn", "Column", startcolumn_char, false, false, viewOnly);
                                PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startrow", "Row", field.getProperty("startrow"), false, false, viewOnly);
                            } else if (type.equalsIgnoreCase("column")) {
                                PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startcolumn", "Column", startcolumn_char, false, false, viewOnly);
                                String startrowtype = field.getProperty("startrowtype");
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "startrowtype", "Start Row Type", "absolute;relative", startrowtype, viewOnly);
                                if (startrowtype.length() > 0) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startrow", "Start Row", field.getProperty("startrow"), false, false, viewOnly);
                                }
                                String endrowtype = field.getProperty("endrowtype");
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "endrowtype", "End Row Type", "firstblankrow;endoffile;absolute", endrowtype, viewOnly);
                                if (endrowtype.equalsIgnoreCase("absolute")) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "endrow", "End Row", field.getProperty("endrow"), false, false, viewOnly);
                                }
                            } else if (type.equalsIgnoreCase("row")) {
                                PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startrow", "Row", field.getProperty("startrow"), false, false, viewOnly);
                                String startcolumntype = field.getProperty("startcolumntype");
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "startcolumntype", "Start Column Type", "absolute;relative", startcolumntype, viewOnly);
                                if (startcolumntype.length() > 0) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startcolumn", "Start Column", startcolumn_char, false, false, viewOnly);
                                }
                                String endcolumntype = field.getProperty("endcolumntype");
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "endcolumntype", "End Column Type", "firstblankcolumn;endoffile;absolute", endcolumntype, viewOnly);
                                if (endcolumntype.equalsIgnoreCase("absolute")) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "endcolumn", "End Column", endcolumn_char, false, false, viewOnly);
                                }
                            } else if (type.equalsIgnoreCase("range")) {
                                String startcolumntype = field.getProperty("startcolumntype");
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "startcolumntype", "Start Column Type", "absolute;relative", startcolumntype, viewOnly);
                                if (startcolumntype.length() > 0) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startcolumn", "Start Column", startcolumn_char, false, false, viewOnly);
                                }
                                String startrowtype = field.getProperty("startrowtype");
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "startrowtype", "Start Row Type", "absolute;relative", startrowtype, viewOnly);
                                if (startrowtype.length() > 0) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startrow", "Start Row", field.getProperty("startrow"), false, false, viewOnly);
                                }
                                String endcolumntype = field.getProperty("endcolumntype");
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "endcolumntype", "End Column Type", "firstblankcolumn;endoffile;absolute", endcolumntype, viewOnly);
                                if (endcolumntype.equalsIgnoreCase("absolute")) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "endcolumn", "End Column", endcolumn_char, false, false, viewOnly);
                                }
                                String endrowtype = field.getProperty("endrowtype");
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "endrowtype", "End Row Type", "firstblankrow;endoffile;absolute", endrowtype, viewOnly);
                                if (endrowtype.equalsIgnoreCase("absolute")) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "endrow", "End Row", field.getProperty("endrow"), false, false, viewOnly);
                                }
                            } else if (type.equalsIgnoreCase("headercolumn") || type.equalsIgnoreCase("headerrow")) {
                                StringBuffer fieldslist = new StringBuffer();
                                for (int i = 0; i < fields.size(); ++i) {
                                    String currtype;
                                    PropertyList currfield;
                                    String currfieldid;
                                    if (fieldslist.length() > 0) {
                                        fieldslist.append(";");
                                    }
                                    if ((currfieldid = (currfield = fields.getPropertyList(i)).getProperty("fieldid", "")).length() <= 0 || currfieldid.equalsIgnoreCase(fieldid) || !(currtype = currfield.getProperty("type")).equalsIgnoreCase("range")) continue;
                                    fieldslist.append(currfieldid);
                                }
                                if (type.equalsIgnoreCase("headercolumn")) {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startcolumn", "Column", startcolumn_char, false, false, viewOnly);
                                } else {
                                    PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "startrow", "Row", field.getProperty("startrow"), false, false, viewOnly);
                                }
                                PropertyBuilder.renderParserListEditor(sb, tp, fieldId, "target", "Target", fieldslist.toString(), field.getProperty("target"), viewOnly);
                            } else if (type.equalsIgnoreCase("input")) {
                                PropertyBuilder.renderParserStringEditor(sb, tp, fieldId, "defaultvalue", "Default Value", field.getProperty("defaultvalue"), false, false, viewOnly);
                            }
                        }
                    }
                    PropertyBuilder.renderEnd(sb);
                } else {
                    String msg = "No properties.";
                    if (tp != null) {
                        msg = tp.translate(msg);
                    }
                    sb.append(msg);
                }
            } else {
                String msg = "Form properties not correct.";
                if (tp != null) {
                    msg = tp.translate(msg);
                }
                sb.append(msg);
            }
        } else {
            String msg = "No connection Id provided.";
            if (tp != null) {
                msg = tp.translate(msg);
            }
            sb.append(msg);
        }
        return sb.toString();
    }

    private StringBuffer getDivsHtml() {
        int left;
        StringBuffer sb = new StringBuffer();
        String width = this.userConfig.getProperty("ssparser_width", "200");
        try {
            left = Integer.parseInt(width) - 5;
        }
        catch (Exception e) {
            left = 195;
        }
        if (this.userConfig.getProperty("ssparser_left", "Y").equalsIgnoreCase("Y")) {
            if (this.browser.isWebkit()) {
                sb.append("<div id=\"form_leftbar_expand\" style=\"display:none;\" onclick=\"ssParser.expandLeftBar();\"><table bgcolor='E6E6E6')><tr><td><img src=\"WEB-CORE/elements/richtext/images/objects-v.gif\" width=\"17\" height=\"16\"></td><td>").append(this.getTranslationProcessor().translate("Objects")).append("</td></tr></table></div>");
            } else {
                sb.append("<div id=\"form_leftbar_expand\" style=\"display:none;\" onclick=\"ssParser.expandLeftBar();\"><img src=\"WEB-CORE/elements/richtext/images/objects-v.gif\" width=\"17\" height=\"16\">&nbsp;").append(this.getTranslationProcessor().translate("Objects")).append("</div>");
            }
            sb.append("<div id=\"form_leftbar_resize\" style=\"display:block;left:").append(left).append("px;\" onmousedown=\"sapphire.ui.resize.start(form_leftbar, [], 'e');\"></div>");
        } else {
            if (this.browser.isWebkit()) {
                sb.append("<div id=\"form_leftbar_expand\" style=\"display:block;\" onclick=\"ssParser.expandLeftBar();\"><table bgcolor='E6E6E6'><tr><td><img src=\"WEB-CORE/elements/richtext/images/objects-v.gif\" width=\"17\" height=\"16\"></td><td>").append(this.getTranslationProcessor().translate("Objects")).append("</td></tr></table></div>");
            } else {
                sb.append("<div id=\"form_leftbar_expand\" style=\"display:block;\" onclick=\"ssParser.expandLeftBar();\"><img src=\"WEB-CORE/elements/richtext/images/objects-v.gif\" width=\"17\" height=\"16\">&nbsp;").append(this.getTranslationProcessor().translate("Objects")).append("</div>");
            }
            sb.append("<div id=\"form_leftbar_resize\" style=\"display:none;left:").append(left).append("px;\" onmousedown=\"sapphire.ui.resize.start(form_leftbar,[],'e');\"></div>");
        }
        return sb;
    }

    private StringBuffer getLeftBarStart() {
        int width;
        String twidth = this.userConfig.getProperty("ssparser_width", "200");
        try {
            width = Integer.parseInt(twidth);
        }
        catch (Exception e) {
            width = 200;
        }
        StringBuffer sb = new StringBuffer();
        if (this.userConfig.getProperty("ssparser_left", "Y").equalsIgnoreCase("Y")) {
            sb.append("<td  id=\"form_leftbar_collapsed\" style=\"display:none\"></td>");
            sb.append("<td id=\"form_leftbar\" class=\"form_leftbar\" style=\"display:table-cell;width:").append(width).append("px;\">");
        } else {
            sb.append("<td  id=\"form_leftbar_collapsed\" style=\"display:table-cell;\"></td>");
            sb.append("<td id=\"form_leftbar\" class=\"form_leftbar\" style=\"display:none;width:").append(width).append("px;\">");
        }
        return sb;
    }

    private String getButtonsHtml(boolean viewOnly) {
        Button but;
        StringBuffer sb = new StringBuffer();
        if (!viewOnly) {
            but = new Button(this.pageContext);
            but.setText(this.getTranslationProcessor().translate("OK"));
            but.setTip(this.getTranslationProcessor().translate("OK and return"));
            but.setId("ssparser_btn_ok");
            but.setImg("WEB-CORE/images/gif/Confirm.gif");
            but.setAction("ssParser.buttons.doOK()");
            but.setWidth("100");
            sb.append(but.getHtml());
            sb.append("&nbsp;&nbsp;");
        }
        but = new Button(this.pageContext);
        if (viewOnly) {
            but.setText(this.getTranslationProcessor().translate("Close"));
            but.setTip(this.getTranslationProcessor().translate("Close"));
        } else {
            but.setText(this.getTranslationProcessor().translate("Cancel"));
            but.setTip(this.getTranslationProcessor().translate("Cancel and return"));
        }
        but.setId("ssparser_btn_cancel");
        but.setImg("WEB-CORE/images/gif/Cancel.gif");
        but.setAction("ssParser.buttons.doCancel()");
        but.setWidth("100");
        sb.append(but.getHtml());
        return sb.toString();
    }

    private String getDevButtonsHtml() {
        StringBuffer sb = new StringBuffer("&nbsp;");
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(this.pageContext);
            String devMode = config.getSysConfigProperty("devmode", "N");
            if (devMode.equals("Y")) {
                Button but = new Button(this.pageContext);
                but.setTip(this.getTranslationProcessor().translate("Show JS debugger (only available in development mode)"));
                but.setId("ssparser_btn_source");
                but.setImg("WEB-CORE/images/gif/Debug.gif");
                but.setAction("sapphire.debug.show()");
                sb.append(but.getHtml());
            }
        }
        catch (Exception e) {
            this.logger.warn("Could not render dev mode buttons.");
        }
        return sb.toString();
    }

    private String getLeftBarEnd() {
        return "</td>";
    }

    public static enum Mode {
        FILE,
        PARSER;

    }
}


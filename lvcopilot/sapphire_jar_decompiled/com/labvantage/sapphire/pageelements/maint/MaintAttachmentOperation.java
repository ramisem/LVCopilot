/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.actions.AddSDIAttachmentOperation;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintAttachmentOperation
extends BaseElement
implements SDMSConstants {
    public static final String PROPERTYHANDLER = "com.labvantage.sapphire.pageelements.maint.MaintAttachmentOperationHandler";
    public static final String DATAFIELD = "__attachmentoperation_data";
    private boolean ajax = false;
    private boolean ajaxCreate = false;
    private DataSet attachmentoperations = null;
    private String sdcid = "";
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";
    private boolean viewonly = false;
    private StringBuilder script = null;

    public void setOperationData(DataSet dataSet) {
        this.attachmentoperations = dataSet;
    }

    public DataSet getOperationData() {
        return this.attachmentoperations;
    }

    public MaintAttachmentOperation() {
    }

    public MaintAttachmentOperation(PageContext pageContext) {
        this.setPageContext(pageContext);
        this.setAjax(true);
    }

    private void setUpProps() {
        if (this.requestContext != null) {
            this.viewonly = this.requestContext.getProperty("mode").equalsIgnoreCase("view");
        }
        if (this.element.getProperty("readonly", this.element.getProperty("viewonly", "")).length() > 0) {
            this.viewonly = this.element.getProperty("readonly", "N").equalsIgnoreCase("N") ? this.element.getProperty("viewonly", this.viewonly ? "Y" : "N").equalsIgnoreCase("Y") : true;
        }
        if (!this.element.getProperty("sdcid").equalsIgnoreCase("LV_InstrumentModel") && !this.element.getProperty("sdcid").equalsIgnoreCase("Instrument")) {
            this.viewonly = true;
        }
        if (this.element.getProperty("ajax", "N").equalsIgnoreCase("Y")) {
            this.ajax = true;
        }
    }

    public void setPrimary(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2;
        this.keyid3 = keyid3;
    }

    public void setViewOnly(boolean viewOnly) {
        this.viewonly = viewOnly;
    }

    public boolean getViewOnly() {
        return this.viewonly;
    }

    public void setAjax(boolean ajax) {
        this.ajax = ajax;
    }

    public void setAjaxCreate(boolean ajaxCreate) {
        this.ajaxCreate = ajaxCreate;
    }

    private void setUpData() {
        if (this.attachmentoperations == null) {
            if (this.ajax || this.sdiInfo == null) {
                Object[] params;
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT * FROM sdiattachmentoperation WHERE sdcid = ? AND keyid1 = ? ");
                if (this.keyid2.length() > 0) {
                    sql.append("AND keyid2 = ? ");
                    if (this.keyid3.length() > 0) {
                        sql.append("AND keyid3 = ? ");
                        params = new Object[]{this.sdcid, this.keyid1, this.keyid2, this.keyid3};
                    } else {
                        params = new Object[]{this.sdcid, this.keyid1, this.keyid2};
                    }
                } else {
                    params = new Object[]{this.sdcid, this.keyid1};
                }
                this.attachmentoperations = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params, true);
            } else {
                String[] keycols = this.sdiInfo.getKeycols();
                this.setPrimary(this.sdiInfo.getSdcid(), this.sdiInfo.getDataSet("primary").getValue(0, keycols[0], ""), keycols.length > 1 ? this.sdiInfo.getDataSet("primary").getValue(0, this.sdiInfo.getKeycols()[1], "") : "", keycols.length > 2 ? this.sdiInfo.getDataSet("primary").getValue(0, this.sdiInfo.getKeycols()[2], "") : "");
                this.attachmentoperations = this.sdiInfo.getDataSet("attachmentoperation");
                String lockedBy = this.sdiInfo.getDataSet("primary").getValue(0, "__lockedby", "");
                if (lockedBy != null && lockedBy.length() > 0) {
                    this.viewonly = true;
                    this.logger.debug("Locked by " + lockedBy + ".");
                }
            }
            if (this.attachmentoperations != null) {
                if (!this.attachmentoperations.isValidColumn("__rowstatus")) {
                    this.attachmentoperations.addColumn("__rowstatus", 0);
                }
                this.attachmentoperations.setValue(-1, "__rowstatus", "S");
                if (!this.attachmentoperations.isValidColumn("__rowid")) {
                    this.attachmentoperations.addColumn("__rowid", 1);
                }
                for (int i = 0; i < this.attachmentoperations.getRowCount(); ++i) {
                    this.attachmentoperations.setNumber(i, "__rowid", i);
                }
            }
        }
        if (!this.sdcid.equalsIgnoreCase("LV_InstrumentModel") && !this.sdcid.equalsIgnoreCase("Instrument")) {
            this.setViewOnly(true);
        }
    }

    private void renderIcon(StringBuilder html, int rownum, String attachmentoperationid, String attachmentoperationkeyid1, String attachmentclass, String asynchronous, String processinggroupname, String captureopstatus, String rowstatus, String usersequence) {
        if (!rowstatus.equalsIgnoreCase("X") && !rowstatus.equalsIgnoreCase("D")) {
            this.renderRowHead(html, rownum, attachmentoperationid, "icon");
            html.append("<div class=\"icon_txt\">");
            String selectorId = this.elementid + rownum + "_usersequence";
            String selctor = "<input style=\"display:none\" type=\"text\" name=\"" + selectorId + "\" id=\"" + selectorId + "\" class=\"input_field \" onfocus=\"\" oninput=\"sdiSetRowUpdate(event)\" data-columnid=\"usersequence\" data-row=\"" + rownum + "\" value=\"" + usersequence + "\">";
            selctor = selctor + "<input id=\"" + selectorId + "_chx\" name=\"" + selectorId + "_chx\" value=\"Y\" type=\"checkbox\" onchange=\"attachmentOperation.clickSelector(event,this,'" + this.elementid + "')\" oninput=\"sdiSetRowUpdate(event)\" data-columnid=\"usersequence\" data-row=\"" + rownum + "\">";
            html.append(selctor);
            html.append("</div>");
            html.append("<div class=\"icon_txt\">");
            String url = "sapphire.page.list.link.open('" + attachmentoperationkeyid1 + "',{'tip':'Show Details for " + attachmentoperationkeyid1 + "','href':'rc?command=page&page=LV_DataHandlerMaint&sdcid=LV_AttachmentHandler&mode=Edit&keyid1=" + attachmentoperationkeyid1 + "'});void(0);";
            html.append("<a  title=\"Click to view Handler\" href=\"").append("javascript:;").append("\" onclick=\"").append(url).append("\">");
            html.append("").append(this.getShortDesc(attachmentoperationkeyid1));
            html.append("</a>");
            html.append("</div>");
            html.append("<div class=\"icon_txt\">");
            EditorStyleField editorStyleField = new EditorStyleField(this.pageContext);
            PropertyList col = new PropertyList();
            col.setProperty("mode", this.viewonly ? "readonly" : "dropdownlist");
            col.setProperty("reftypeid", "AttachmentClass");
            col.setProperty("extraattributes", "data-columnid=attachmentclass;data-row=" + rownum);
            editorStyleField.setColumn(col);
            editorStyleField.setFieldName(this.elementid + rownum + '_' + "attachmentclass");
            editorStyleField.setDatasetname("attachmentoperation");
            editorStyleField.setFieldValue(attachmentclass);
            if (!this.viewonly) {
                editorStyleField.setChangeEvent("attachmentOperation.change('" + this.elementid + "',event,this)");
            }
            html.append(editorStyleField.getHtml());
            html.append("</div>");
            html.append("<div class=\"icon_txt\">");
            editorStyleField = new EditorStyleField(this.pageContext);
            col = new PropertyList();
            col.setProperty("mode", this.viewonly ? "readonly" : "checkbox");
            if (!this.viewonly) {
                col.setProperty("displayvalue", "Y=No;N=Yes");
            } else {
                col.setProperty("displayvalue", "Y=Yes;N=No");
            }
            editorStyleField.setFieldValue(asynchronous);
            col.setProperty("extraattributes", "data-columnid=synchronousflag;data-row=" + rownum);
            editorStyleField.setColumn(col);
            editorStyleField.setFieldName(this.elementid + rownum + '_' + "synchronousflag");
            editorStyleField.setDatasetname("attachmentoperation");
            if (!this.viewonly) {
                editorStyleField.setChangeEvent("attachmentOperation.synchChange('" + this.elementid + "',event,this)");
            }
            html.append(editorStyleField.getHtml());
            html.append("</div>");
            html.append("<div class=\"icon_txt\">");
            editorStyleField = new EditorStyleField(this.pageContext);
            col = new PropertyList();
            col.setProperty("mode", this.viewonly ? "readonly" : "dropdownlist");
            col.setProperty("dropdownvalues", "ProcessingGroup1;ProcessingGroup2;ProcessingGroup3;ProcessingGroup4;ProcessingGroup5");
            col.setProperty("extraattributes", "data-columnid=processinggroupname;data-row=" + rownum);
            editorStyleField.setColumn(col);
            editorStyleField.setFieldName(this.elementid + rownum + '_' + "processinggroupname");
            editorStyleField.setDatasetname("attachmentoperation");
            editorStyleField.setFieldValue(processinggroupname);
            if (!this.viewonly) {
                editorStyleField.setChangeEvent("attachmentOperation.change('" + this.elementid + "',event,this)");
            }
            html.append(editorStyleField.getHtml());
            html.append("</div>");
            if (this.sdcid.equalsIgnoreCase("LV_DataCapture")) {
                html.append("<div class=\"icon_txt\">");
                editorStyleField = new EditorStyleField(this.pageContext);
                col = new PropertyList();
                col.setProperty("mode", "readonly");
                col.setProperty("extraattributes", "data-columnid=operationstatus;data-row=" + rownum);
                editorStyleField.setColumn(col);
                editorStyleField.setFieldName(this.elementid + rownum + '_' + "operationstatus");
                editorStyleField.setDatasetname("attachmentoperation");
                editorStyleField.setFieldValue(captureopstatus);
                html.append(editorStyleField.getHtml());
                html.append("</div>");
            }
            html.append("<div class=\"icon_txt\">");
            html.append("<input type=\"button\"  value=\"...\" onclick=\"attachmentOperation.openPropertyChooser( " + rownum + "," + this.viewonly + ");\" title=\"Setup Variable\" style=\"vertical-align: top; float:center\">");
            html.append("</div>");
            html.append("</div>");
        }
    }

    private boolean renderIconView(StringBuilder html, DataSet attachmentOperations) {
        boolean rendered = false;
        boolean renderIncludes = true;
        html.append("<div class=\"title\">");
        html.append("<div class=\"title_txt\">");
        String selectorId = this.elementid + "_selectall";
        String selectorALL = "<input id=\"" + selectorId + "\" name=\"" + selectorId + "\" value=\"Y\" type=\"checkbox\" onchange=\"attachmentOperation.clickSelectorAll('" + this.elementid + "'," + attachmentOperations.getRowCount() + ",this)\"  data-columnid=\"usersequence\">";
        html.append(attachmentOperations.getRowCount() > 0 ? selectorALL : "&nbsp;");
        html.append("</div>");
        PropertyListCollection columns = this.element.getCollection("columns");
        if (attachmentOperations.getRowCount() > 0) {
            for (int c = 0; c < columns.size(); ++c) {
                PropertyList col = columns.getPropertyList(c);
                if (col.getProperty("columnid").length() <= 0) continue;
                html.append("<div class=\"title_txt\">");
                html.append(col.getProperty("title"));
                html.append("</div>");
            }
        }
        html.append("</div>");
        for (int i = 0; i < attachmentOperations.getRowCount(); ++i) {
            boolean cont = true;
            if (!cont) continue;
            String attachmentoperationid = attachmentOperations.getValue(i, "attachmentoperationid", "");
            String attachmentoperationkeyid1 = attachmentOperations.getValue(i, "operationkeyid1", "");
            String attachmentclass = attachmentOperations.getValue(i, "attachmentclass", "");
            String asynchronous = attachmentOperations.getValue(i, "synchronousflag", "").equalsIgnoreCase("N") ? "Y" : "N";
            String processinggroupname = attachmentOperations.getValue(i, "processinggroupname", "");
            String status = attachmentOperations.getValue(i, "operationstatus", "ready");
            String rowstatus = attachmentOperations.getValue(i, "__rowstatus", "S");
            String usersequence = attachmentOperations.getValue(i, "usersequence", "");
            this.renderIcon(html, i, attachmentoperationid, attachmentoperationkeyid1, attachmentclass, asynchronous, processinggroupname, status, rowstatus, usersequence);
            if (rowstatus.equalsIgnoreCase("D") || rowstatus.equalsIgnoreCase("X")) continue;
            rendered = true;
        }
        return rendered;
    }

    private String getShortDesc(String desc) {
        String out = desc;
        int l = 22;
        if (desc.length() > l) {
            int m = desc.length() % 2;
            String p1 = desc.substring(0, m == 0 ? desc.length() / 2 : desc.length() / 2 + 1);
            String p2 = desc.substring(m == 0 ? desc.length() / 2 + 1 : desc.length() / 2 + 2);
            if (p1.length() > 8) {
                p1 = p1.substring(0, l / 2 - 1);
            }
            if (p2.length() > 8) {
                p2 = p2.substring(p2.length() - (l / 2 - 1));
            }
            out = p1 + "..." + p2;
        }
        return out;
    }

    private void renderRowHead(StringBuilder html, int row, String attachmentoperationid, String cssclass) {
        html.append("<div data-attachmentoperationrow=\"").append(row).append("\" data-attachmentoperationid=\"").append(attachmentoperationid).append("\" class=\"").append(cssclass).append("\" onmouseout=\"attachmentOperation.mouseout(event, this,'").append(this.elementid).append("')\" ondblclick=\"attachmentOperation.dblClick(event,this,'").append(this.elementid).append("')\" onclick=\"attachmentOperation.click(event,this,'").append(this.elementid).append("')\" onmouseover=\"attachmentOperation.mouseover(event, this,'").append(this.elementid).append("')\">");
    }

    private void checkColumns() {
        PropertyListCollection columns = new PropertyListCollection();
        this.element.setProperty("columns", columns);
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "operationkeyid1");
        column.setProperty("title", this.getTranslationProcessor().translate("Attachment Handler"));
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "attachmentclass");
        column.setProperty("title", this.getTranslationProcessor().translate("Attachment Class"));
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "synchronousflag");
        column.setProperty("title", this.getTranslationProcessor().translate("Run Asynchronous"));
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "processinggroupname");
        column.setProperty("title", this.getTranslationProcessor().translate("Asynchronous Group"));
        columns.add(column);
        if (this.sdcid.equalsIgnoreCase("LV_DataCapture")) {
            column = new PropertyList();
            column.setProperty("columnid", "operationstatus");
            column.setProperty("title", this.getTranslationProcessor().translate("Status"));
            columns.add(column);
        }
        column = new PropertyList();
        column.setProperty("columnid", "propertyclob");
        column.setProperty("title", this.getTranslationProcessor().translate("Properties"));
        columns.add(column);
    }

    public JSONObject getElement() {
        JSONObject out = new JSONObject();
        try {
            out.put("selection", new JSONArray());
        }
        catch (Exception exception) {
            // empty catch block
        }
        return out;
    }

    public String getScript() {
        StringBuilder html = new StringBuilder();
        if (this.script != null) {
            html.append((CharSequence)this.script);
        }
        return html.toString();
    }

    private String getStartScript() {
        StringBuilder html = new StringBuilder();
        if (!this.ajax) {
            html.append("attachmentOperation.sdcid = '").append(this.sdcid).append("';");
            html.append("attachmentOperation.keyid1 = '").append(this.keyid1).append("';");
            html.append("attachmentOperation.keyid2 = '").append(this.keyid2).append("';");
            html.append("attachmentOperation.keyid3 = '").append(this.keyid3).append("';");
            html.append("attachmentOperation.viewonly = ").append(this.viewonly).append(";");
            html.append("attachmentOperation.rowcount = ").append(this.attachmentoperations.getRowCount()).append(";");
            html.append("attachmentOperation.maxusersequence = ").append(AddSDIAttachmentOperation.getMaxUsersequence(this.sdcid, this.keyid1, this.keyid2, this.keyid3, new HashMap(), this.getQueryProcessor())).append(";");
            html.append("attachmentOperation.elementid = '").append(this.elementid).append("';");
        }
        return html.toString();
    }

    public static String getScriptAndStyle() {
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/elements/sdms/scripts/attachmentoperation.js\"></script>");
        html.append("<link href=\"WEB-CORE/elements/sdms/style/attachmentoperation.css\" rel=\"stylesheet\" type=\"text/css\"/>");
        return html.toString();
    }

    @Override
    public String getHtml() {
        Object o;
        this.script = new StringBuilder();
        this.setUpProps();
        this.setUpData();
        if (!this.ajax) {
            this.attachmentoperations.sort("usersequence");
        }
        if ((o = ((HttpServletRequest)this.pageContext.getRequest()).getSession().getAttribute("userconfig")) != null) {
            PropertyList userconfig = (PropertyList)o;
        } else {
            PropertyList userconfig = this.requestContext.getPropertyList("userconfig");
        }
        StringBuilder html = new StringBuilder();
        if (this.attachmentoperations != null) {
            if (!this.ajax) {
                ConfigurationProcessor config = new ConfigurationProcessor(this.pageContext);
                boolean devMode = false;
                try {
                    devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                }
                catch (Exception e) {
                    devMode = false;
                }
                html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, "", devMode, this.pageContext));
                html.append(MaintAttachmentOperation.getScriptAndStyle());
                html.append("<script>");
                html.append(this.getStartScript());
                html.append("attachmentOperation.elements['").append(this.elementid).append("'] = ").append(this.getElement().toString()).append(";");
                html.append("attachmentOperation.elements['").append(this.elementid).append("'].properties = sapphire.util.propertyList.create( ").append(this.element.toJSONString()).append(");");
                html.append("attachmentOperation.elements['").append(this.elementid).append("'].selection = new Array();");
                html.append("attachmentOperation.data = sapphire.util.dataSet.create( ").append(this.attachmentoperations.toJSONString()).append(");");
                html.append("$().ready( function(){");
                html.append("attachmentOperation.onload('").append(this.elementid).append("');");
                html.append("});");
                html.append("</script>");
            }
            if (!this.ajax || this.ajaxCreate) {
                html.append("<div id=\"").append(this.elementid).append("_parent\" class=\"attachmentoperation_parent\">");
                html.append("<div id=\"").append(this.elementid).append("_container\" class=\"attachmentoperation_container").append("").append("\">");
            }
            boolean rendered = false;
            this.checkColumns();
            rendered = this.renderIconView(html, this.attachmentoperations);
            if (!rendered) {
                html.append(this.getTranslationProcessor().translate("No Capture Operations found"));
            }
            if (!this.ajax || this.ajaxCreate) {
                html.append("</div>");
                html.append("</div>");
                if (!this.ajax && !this.ajaxCreate) {
                    html.append("<input type=\"hidden\" name=\"").append("__postpropertyhandler_post_").append(this.elementid).append("\" value=\"").append(PROPERTYHANDLER).append("\"/>");
                    html.append("<input type=\"hidden\" name=\"").append(DATAFIELD).append("\" id=\"").append(DATAFIELD).append("\">");
                }
                html.append(this.getButtons());
            }
        }
        if (!this.ajax) {
            html.append("<script>");
            html.append(this.getScript());
            html.append("</script>");
        }
        return html.toString();
    }

    public String getButtons() {
        StringBuilder html = new StringBuilder();
        html.append("<span id=\"").append(this.elementid).append("_buttons\">");
        PropertyListCollection buttons = new PropertyListCollection();
        if (!this.viewonly) {
            Button btn = new Button(this.pageContext);
            btn.setId("btnAddAttachmenteOperation");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_plus1.svg");
            btn.setText(this.getTranslationProcessor().translate("Add"));
            btn.setAction("attachmentOperation.add('" + this.elementid + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId("btnRemove");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_close_remove2.svg");
            btn.setText(this.getTranslationProcessor().translate("Remove"));
            btn.setAction("attachmentOperation.remove('" + this.elementid + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId("btnMoveUp");
            btn.setImg("WEB-CORE/images/gif/MoveUp.gif");
            btn.setWidth("40");
            btn.setAction("attachmentOperation.moveup('" + this.elementid + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId("btnMoveDown");
            btn.setImg("WEB-CORE/images/gif/MoveDown.gif");
            btn.setWidth("40");
            btn.setAction("attachmentOperation.movedown('" + this.elementid + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;&nbsp;");
        }
        html.append("</span>");
        return html.toString();
    }
}


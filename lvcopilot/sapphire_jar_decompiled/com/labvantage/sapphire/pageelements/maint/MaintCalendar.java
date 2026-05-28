/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintCalendar
extends BaseElement {
    public static final String PROPERTYHANDLER = "com.labvantage.sapphire.pageelements.maint.MaintCalendarPropertyHandler";
    public static final String DATAFIELD = "__calendar_data";
    private boolean ajax = false;
    private boolean ajaxCreate = false;
    private DataSet calendar = null;
    private String sdcid = "";
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";
    private boolean viewonly = false;
    private boolean showPrimaryCalendar = true;
    private StringBuilder script = null;

    public void setCalendarData(DataSet dataSet) {
        this.calendar = dataSet;
    }

    public DataSet getCalendarData() {
        return this.calendar;
    }

    public MaintCalendar() {
    }

    public MaintCalendar(PageContext pageContext) {
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
        if (this.element.getProperty("showprimarycalendar", "Y").equalsIgnoreCase("N")) {
            this.showPrimaryCalendar = false;
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
        if (this.calendar == null) {
            Object[] params;
            StringBuilder sql;
            if (this.ajax || this.sdiInfo == null) {
                sql = new StringBuilder();
                sql.append("SELECT * FROM sdicalendar WHERE sdcid = ? AND keyid1 = ? ");
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
                this.calendar = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params, true);
            } else {
                String[] keycols = this.sdiInfo.getKeycols();
                this.setPrimary(this.sdiInfo.getSdcid(), this.sdiInfo.getDataSet("primary").getValue(0, keycols[0], ""), keycols.length > 1 ? this.sdiInfo.getDataSet("primary").getValue(0, this.sdiInfo.getKeycols()[1], "") : "", keycols.length > 2 ? this.sdiInfo.getDataSet("primary").getValue(0, this.sdiInfo.getKeycols()[2], "") : "");
                this.calendar = this.sdiInfo.getDataSet("calendar");
                String lockedBy = this.sdiInfo.getDataSet("primary").getValue(0, "__lockedby", "");
                if (lockedBy != null && lockedBy.length() > 0) {
                    this.viewonly = true;
                    this.logger.debug("Locked by " + lockedBy + ".");
                }
            }
            if (this.calendar != null) {
                if (!this.calendar.isValidColumn("__rowstatus")) {
                    this.calendar.addColumn("__rowstatus", 0);
                }
                this.calendar.setValue(-1, "__rowstatus", "S");
                if (!this.calendar.isValidColumn("__rowid")) {
                    this.calendar.addColumn("__rowid", 1);
                }
                if (!this.calendar.isValidColumn("__calendardesc")) {
                    this.calendar.addColumn("__calendardesc", 0);
                    sql = new StringBuilder("SELECT calendarid, calendardesc FROM calendar WHERE calendarid IN (SELECT calendarid FROM sdicalendar WHERE sdcid=? AND keyid1=? ");
                    if (this.keyid2.length() > 0) {
                        sql.append(" AND keyid2 = ? ");
                        if (this.keyid3.length() > 0) {
                            sql.append(" AND keyid3 = ? ");
                            params = new Object[]{this.sdcid, this.keyid1, this.keyid2, this.keyid3};
                        } else {
                            params = new Object[]{this.sdcid, this.keyid1, this.keyid2};
                        }
                    } else {
                        params = new Object[]{this.sdcid, this.keyid1};
                    }
                    sql.append(" ) ");
                    DataSet calendardetails = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params);
                    if (calendardetails != null && calendardetails.getRowCount() > 0) {
                        for (int r = 0; r < calendardetails.getRowCount(); ++r) {
                            String calid;
                            String caldesc = calendardetails.getValue(r, "calendardesc", "");
                            if (caldesc.length() <= 0 || (calid = calendardetails.getValue(r, "calendarid", "")).length() <= 0) continue;
                            for (int i = 0; i < this.calendar.getRowCount(); ++i) {
                                if (!this.calendar.getValue(i, "calendarid").equals(calid)) continue;
                                this.calendar.setValue(i, "__calendardesc", caldesc);
                            }
                        }
                    }
                }
                for (int i = 0; i < this.calendar.getRowCount(); ++i) {
                    this.calendar.setNumber(i, "__rowid", i);
                }
            }
        }
    }

    private void renderIcon(StringBuilder html, int rownum, String calendarid, String calendardesc, String rowstatus) {
        if (!rowstatus.equalsIgnoreCase("X") && !rowstatus.equalsIgnoreCase("D")) {
            this.renderCalendarRowHead(html, rownum, calendarid, "icon");
            html.append("<div class=\"icon_view\" onmouseover=\"$(this).addClass('icon_view_over')\" onmouseout=\"$(this).removeClass('icon_view_over')\" onclick=\"calendarElement.");
            if (rownum < 0) {
                html.append("managePrivate('").append(this.elementid).append("',").append("event").append(");\">");
            } else {
                html.append("manageShared('").append(this.elementid).append("','").append(calendarid).append("',event);\">");
            }
            Image viewim = new Image(this.pageContext);
            viewim.setImageId("FlatBlackCalendarPlus");
            viewim.setDimensions(10, 10);
            viewim.setTitle(this.getTranslationProcessor().translate("Manage Calendar"));
            html.append(viewim.getHtml());
            html.append("</div>");
            html.append("<div class=\"icon_img\">");
            Image image = new Image(this.pageContext);
            image.setImageId(rownum < 0 ? "FlatBlackCalendarRange" : "FlatBlackCalendar");
            image.setWidth(64);
            image.setHeight(64);
            image.setTitle(rownum < 0 ? this.getTranslationProcessor().translate("Primary calendar") : calendardesc + " (" + calendarid + ")");
            html.append(image.getHtml());
            html.append("</div>");
            html.append("<div class=\"icon_txt\">");
            html.append("").append(this.getShortDesc(calendardesc.length() > 0 ? calendardesc : calendarid));
            html.append("</div>");
            html.append("</div>");
        }
    }

    private boolean renderIconView(StringBuilder html, DataSet calendar) {
        boolean rendered = false;
        boolean renderIncludes = true;
        if (this.showPrimaryCalendar) {
            this.renderIcon(html, -1, "", "(PRIMARY)", "S");
        }
        for (int i = 0; i < calendar.getRowCount(); ++i) {
            boolean cont = true;
            if (!cont) continue;
            String calendarid = calendar.getValue(i, "calendarid", "");
            String calendardesc = calendar.getValue(i, "__calendardesc", "");
            String rowstatus = calendar.getValue(i, "__rowstatus", "S");
            this.renderIcon(html, i, calendarid, calendardesc, rowstatus);
            if (rowstatus.equalsIgnoreCase("D") || rowstatus.equalsIgnoreCase("X")) continue;
            rendered = true;
        }
        return rendered;
    }

    private String getShortDesc(String desc) {
        String out = desc;
        if (desc.length() > 18) {
            int m = desc.length() % 2;
            String p1 = desc.substring(0, m == 0 ? desc.length() / 2 : desc.length() / 2 + 1);
            String p2 = desc.substring(m == 0 ? desc.length() / 2 + 1 : desc.length() / 2 + 2);
            if (p1.length() > 8) {
                p1 = p1.substring(0, 8);
            }
            if (p2.length() > 8) {
                p2 = p2.substring(p2.length() - 8);
            }
            out = p1 + "..." + p2;
        }
        return out;
    }

    private void renderCalendarRowHead(StringBuilder html, int row, String calendarid, String cssclass) {
        html.append("<div data-calendarrow=\"").append(row).append("\" data-calendarid=\"").append(calendarid).append("\" class=\"").append(cssclass).append("\" onmouseout=\"calendarElement.mouseout(event, this,'").append(this.elementid).append("')\" ondblclick=\"calendarElement.dblClick(event,this,'").append(this.elementid).append("')\" onclick=\"calendarElement.click(event,this,'").append(this.elementid).append("')\" onmouseover=\"calendarElement.mouseover(event, this,'").append(this.elementid).append("')\">");
    }

    private void checkColumns() {
        PropertyListCollection columns = this.element.getCollection("columns");
        if (columns != null) {
            PropertyList pl = columns.find("columnid", "calendarid");
            if (pl != null) {
                pl.setProperty("mode", "readonly");
                pl.setProperty("class", "calendarid");
            }
            if (columns.find("columnid", "calendardesc") == null) {
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "calendardesc");
                column.setProperty("title", this.getTranslationProcessor().translate("Description"));
                column.setProperty("mode", "readonly");
                columns.add(column);
            }
        } else {
            columns = new PropertyListCollection();
            PropertyList column = new PropertyList();
            column.setProperty("columnid", "calendarid");
            column.setProperty("title", this.getTranslationProcessor().translate("Id"));
            column.setProperty("mode", "readonly");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "calendardesc");
            column.setProperty("title", this.getTranslationProcessor().translate("Description"));
            column.setProperty("mode", "readonly");
            columns.add(column);
            this.element.setProperty("columns", columns);
        }
    }

    public JSONObject getCalendarElement() {
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
            html.append("calendarElement.sdcid = '").append(this.sdcid).append("';");
            html.append("calendarElement.keyid1 = '").append(this.keyid1).append("';");
            html.append("calendarElement.keyid2 = '").append(this.keyid2).append("';");
            html.append("calendarElement.keyid3 = '").append(this.keyid3).append("';");
            html.append("calendarElement.viewonly = ").append(this.viewonly).append(";");
        }
        return html.toString();
    }

    public static String getScriptAndStyle() {
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/elements/calendar/scripts/calendar.js\"></script>");
        html.append("<link href=\"WEB-CORE/elements/calendar/style/calendar.css\" rel=\"stylesheet\" type=\"text/css\"/>");
        return html.toString();
    }

    @Override
    public String getHtml() {
        this.script = new StringBuilder();
        this.setUpProps();
        this.setUpData();
        Object o = ((HttpServletRequest)this.pageContext.getRequest()).getSession().getAttribute("userconfig");
        if (o != null) {
            PropertyList userconfig = (PropertyList)o;
        } else {
            PropertyList userconfig = this.requestContext.getPropertyList("userconfig");
        }
        StringBuilder html = new StringBuilder();
        if (this.calendar != null) {
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
                html.append(MaintCalendar.getScriptAndStyle());
                html.append("<script>");
                html.append(this.getStartScript());
                html.append("calendarElement.elements['").append(this.elementid).append("'] = ").append(this.getCalendarElement().toString()).append(";");
                html.append("calendarElement.elements['").append(this.elementid).append("'].properties = sapphire.util.propertyList.create( ").append(this.element.toJSONString()).append(");");
                html.append("calendarElement.elements['").append(this.elementid).append("'].selection = new Array();");
                html.append("calendarElement.data = sapphire.util.dataSet.create( ").append(this.calendar.toJSONString()).append(");");
                html.append("$().ready( function(){");
                html.append("calendarElement.onload('").append(this.elementid).append("');");
                html.append("});");
                html.append("</script>");
            }
            if (!this.ajax || this.ajaxCreate) {
                html.append("<div id=\"").append(this.elementid).append("_parent\" class=\"calendar_parent\">");
                html.append("<div id=\"").append(this.elementid).append("_container\" class=\"calendar_container").append("").append("\">");
            }
            boolean rendered = false;
            this.checkColumns();
            rendered = this.renderIconView(html, this.calendar);
            if (!rendered && !this.showPrimaryCalendar) {
                html.append(this.getTranslationProcessor().translate("No calendars found"));
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
            btn.setId("btnAddCalendar");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_plus1.svg");
            btn.setText(this.getTranslationProcessor().translate("Add"));
            btn.setAction("calendarElement.add('" + this.elementid + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId("btnRemove");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_close_remove2.svg");
            btn.setText(this.getTranslationProcessor().translate("Remove"));
            btn.setAction("calendarElement.remove('" + this.elementid + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId("btnManage");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_calendar_plus.svg");
            btn.setText(this.getTranslationProcessor().translate("Manage"));
            btn.setAction("calendarElement.manage('" + this.elementid + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;");
        }
        html.append("</span>");
        return html.toString();
    }
}


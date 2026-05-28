/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.list.ListView;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintDataCapture
extends BaseElement
implements SDMSConstants {
    public static final String PROPERTYHANDLER = "com.labvantage.sapphire.pageelements.maint.MaintDataCaptureHandler";
    public static final String DATAFIELD = "__datacapture_data";
    private boolean ajax = false;
    private boolean ajaxCreate = false;
    private DataSet datacaptures = null;
    private String sdcid = "";
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";
    private static JSONObject jsonObj;
    private boolean viewonly = false;
    private StringBuilder script = null;

    public void setDataCaptureData(DataSet dataSet) {
        this.datacaptures = dataSet;
    }

    public DataSet getDataCaptureData() {
        return this.datacaptures;
    }

    public MaintDataCapture() {
    }

    public MaintDataCapture(PageContext pageContext) {
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
        this.viewonly = true;
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

    private void setUpSDCLinks() {
        jsonObj = new JSONObject();
        try {
            PropertyList SDMSPolicy = new ConfigurationProcessor(this.getConnectionid()).getPolicy("SDMSPolicy", "Sapphire Custom");
            PropertyListCollection datacapturelinks = SDMSPolicy.getCollectionNotNull("datacapturelinks");
            if (datacapturelinks.size() > 0) {
                for (Object links : datacapturelinks) {
                    PropertyList pl = (PropertyList)links;
                    String linksdcid = pl.getProperty("sdcid");
                    String link = pl.getProperty("link");
                    String target = pl.getProperty("target");
                    jsonObj.put(linksdcid, link);
                    jsonObj.put(linksdcid + "target", target);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpData() {
        if ((this.keyid1 == null || this.keyid1.length() == 0) && this.sdiInfo != null) {
            this.keyid1 = this.sdiInfo.getSDIRequest().getKeyid1List();
        }
        String sql = "(select dc.sdcid,dc.sdcid lvssdcid, dc.keyid1,dc.keyid1 orderkey,  case when dc.sdcid='DataSet' then " + this.concatFields("sd.keyid1", "' ('", "sd.paramlistid", "')'") + " else dc.keyid1 end displaykeyid1, case when dc.sdcid='Sample' then s.sampledesc when dc.sdcid='Batch' then b.batchdesc when dc.sdcid='DataSet' then " + this.concatFields("sd.paramlistid", "' (Ver:'", "paramlistversionid", "') '", "variantid") + " when dc.sdcid='User' then su.sysuserdesc else dc.keyid1 end description from sdidatacapture dc  left join s_sample s on s.s_sampleid=dc.keyid1  left join s_batch b on b.s_batchid=dc.keyid1  left join sdidata sd on sd.sdidataid=dc.keyid1  left join sysuser su on su.sysuserid=dc.keyid1  WHERE datacaptureid=?) union (select  'DataItem' sdcid ,'DataItem' lvssdcid, (select sdidataid from sdidata sd where sd.sdcid=di.sdcid and sd.keyid1=di.keyid1 and sd.keyid2=di.keyid2 and sd.keyid3=di.keyid3 and sd.paramlistid=di.paramlistid and sd.paramlistversionid=di.paramlistversionid and sd.variantid=di.variantid and sd.dataset=di.dataset) keyid1, di.keyid1 orderkey, " + this.concatFields("di.keyid1", "' ('", "di.paramlistid", "') '", "di.paramid") + " displaykeyid1, " + this.concatFields("di.keyid1", "' (PL: '", "di.paramlistid", " ' Ver:'", "di.paramlistversionid", "') '", "di.variantid", "' '", "di.paramid", "' (Type: '", "di.paramtype", "') '", "di.replicateid") + " description  from sdidataitem di  where di.datacaptureid=? ) order by orderkey";
        Object[] params = new Object[]{this.keyid1, this.keyid1};
        this.datacaptures = this.getQueryProcessor().getPreparedSqlDataSet(sql, params, true);
        this.setViewOnly(true);
    }

    private void renderIcon(StringBuilder html, int rownum, String sdcid, String keyid1, String keyid2, String keyid3, String datacaptureid, String rowstatus) {
        if (!rowstatus.equalsIgnoreCase("X") && !rowstatus.equalsIgnoreCase("D")) {
            String href;
            PropertyList link;
            this.renderRowHead(html, rownum, sdcid, keyid1, keyid2, keyid3, datacaptureid, "icon");
            html.append("<div class=\"icon_img\">");
            Image image = new Image(this.pageContext);
            image.setImageId("FlatBlackUpload");
            image.setWidth(16);
            image.setHeight(16);
            image.setTitle(this.sdcid.equalsIgnoreCase("LV_DataCapture") ? sdcid + " " + keyid1 : datacaptureid);
            html.append(image.getHtml());
            html.append("</div>");
            html.append("<div class=\"icon_txt\">");
            if (this.sdcid.equalsIgnoreCase("LV_DataCapture")) {
                html.append("").append(sdcid);
            } else {
                link = this.element.getPropertyList("link");
                String string = href = link != null ? link.getProperty("href") : "";
                if (href.length() > 0) {
                    html.append(ElementUtil.getLink("datacapture", "datacaptureid", this.sdiInfo, link, datacaptureid, 0, true, this.getTranslationProcessor(), false));
                } else {
                    html.append("").append(datacaptureid);
                }
            }
            html.append("</div>");
            if (this.sdcid.equalsIgnoreCase("LV_DataCapture")) {
                html.append("<div class=\"icon_txt\">");
                link = this.element.getPropertyList("link");
                String string = href = link != null ? link.getProperty("href") : "";
                if (href.length() > 0) {
                    html.append(ElementUtil.getLink("datacapture", "keyid1", this.sdiInfo, link, keyid1, 0, true, this.getTranslationProcessor(), false));
                } else {
                    html.append("").append(keyid1);
                }
                html.append("</div>");
            }
            html.append("</div>");
        }
    }

    private boolean renderIconView(StringBuilder html, DataSet datacaptures) {
        boolean rendered = false;
        boolean renderIncludes = true;
        html.append("<div class=\"title\">");
        html.append("<div class=\"title_txt\">");
        html.append("&nbsp;");
        html.append("</div>");
        PropertyListCollection columns = this.element.getCollection("columns");
        if (datacaptures.getRowCount() > 0) {
            for (int c = 0; c < columns.size(); ++c) {
                PropertyList col = columns.getPropertyList(c);
                if (col.getProperty("columnid").length() <= 0) continue;
                html.append("<div class=\"title_txt\">");
                html.append(col.getProperty("title"));
                html.append("</div>");
            }
        }
        html.append("</div>");
        for (int i = 0; i < datacaptures.getRowCount(); ++i) {
            boolean cont = true;
            if (!cont) continue;
            String sdcid = datacaptures.getValue(i, "sdcid", "");
            String keyid1 = datacaptures.getValue(i, "keyid1", "");
            String keyid2 = datacaptures.getValue(i, "keyid2", "");
            String keyid3 = datacaptures.getValue(i, "keyid3", "N");
            String datacaptureid = datacaptures.getValue(i, "datacaptureid", "");
            String rowstatus = datacaptures.getValue(i, "__rowstatus", "S");
            this.renderIcon(html, i, sdcid, keyid1, keyid2, keyid3, datacaptureid, rowstatus);
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

    private void renderRowHead(StringBuilder html, int row, String sdcid, String keyid1, String keyid2, String keyid3, String datacaptureid, String cssclass) {
        html.append("<div data-datacapturerow=\"").append(row).append("\" data-datacaptureid=\"").append(datacaptureid).append("\" class=\"").append(cssclass).append("\" onmouseout=\"dataCapture.mouseout(event, this,'").append(this.elementid).append("')\" ondblclick=\"dataCapture.dblClick(event,this,'").append(this.elementid).append("')\" onclick=\"dataCapture.click(event,this,'").append(this.elementid).append("')\" onmouseover=\"dataCapture.mouseover(event, this,'").append(this.elementid).append("')\">");
    }

    private void checkColumns() {
        PropertyListCollection columns = new PropertyListCollection();
        this.element.setProperty("columns", columns);
        if (this.sdcid.equalsIgnoreCase("LV_DataCapture")) {
            PropertyList column = new PropertyList();
            column.setProperty("columnid", "sdcid");
            column.setProperty("title", "SDC Id");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "keyid1");
            column.setProperty("title", "Id");
            columns.add(column);
        } else {
            PropertyList column = new PropertyList();
            column.setProperty("columnid", "datacaptureid");
            column.setProperty("title", "Data Capture");
            columns.add(column);
        }
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
            html.append("dataCapture.sdcid = '").append(this.sdcid).append("';");
            html.append("dataCapture.keyid1 = '").append(this.keyid1).append("';");
            html.append("dataCapture.keyid2 = '").append(this.keyid2).append("';");
            html.append("dataCapture.keyid3 = '").append(this.keyid3).append("';");
            html.append("dataCapture.viewonly = ").append(this.viewonly).append(";");
        }
        return html.toString();
    }

    public static String getScriptAndStyle() {
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/elements/sdms/scripts/datacapture.js\"></script>");
        html.append("<link href=\"WEB-CORE/elements/sdms/style/datacapture.css\" rel=\"stylesheet\" type=\"text/css\"/>");
        html.append("<script>");
        html.append("var jsonObj=");
        html.append(jsonObj.toString());
        html.append(";");
        html.append("</script>");
        return html.toString();
    }

    @Override
    public String getHtml() {
        this.script = new StringBuilder();
        this.setUpProps();
        this.setUpData();
        this.setUpSDCLinks();
        StringBuilder html = new StringBuilder();
        String listView = this.getListViewSDIDataCapture();
        html.append("<div class=\"datacapture_container\">");
        html.append("<div class=\"datacapture_list\">");
        html.append(listView);
        html.append("</div>");
        html.append("</div>");
        html.append(MaintDataCapture.getScriptAndStyle());
        return html.toString();
    }

    public String getButtons() {
        StringBuilder html = new StringBuilder();
        html.append("<span id=\"").append(this.elementid).append("_buttons\">");
        PropertyListCollection buttons = new PropertyListCollection();
        if (!this.viewonly) {
            // empty if block
        }
        html.append("</span>");
        return html.toString();
    }

    private String getListViewSDIDataCapture() {
        ListView listView = new ListView(this.pageContext, this.datacaptures, this.getConnectionId());
        PropertyList element = new PropertyList();
        PropertyListCollection columns = new PropertyListCollection();
        PropertyList column = new PropertyList();
        column.setProperty("id", "keyid1");
        column.setProperty("title", this.getTranslationProcessor().translate("Key ID"));
        column.setProperty("columnid", "displaykeyid1");
        column.setProperty("mode", "Display Text");
        column.setProperty("width", "150");
        PropertyList link = new PropertyList();
        link.setProperty("href", "javascript:dataCapture.openDataCaptureLink('[lvssdcid]','[keyid1]','[keyid2]','[keyid3]',jsonObj)");
        column.setProperty("link", link);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("id", "sdcid");
        column.setProperty("title", this.getTranslationProcessor().translate("SDC ID"));
        column.setProperty("columnid", "lvssdcid");
        column.setProperty("mode", "Hidden Value");
        column.setProperty("width", "120");
        columns.add(column);
        column = new PropertyList();
        column.setProperty("id", "keyid3");
        column.setProperty("title", this.getTranslationProcessor().translate("Description"));
        column.setProperty("columnid", "description");
        column.setProperty("mode", "Display Text");
        columns.add(column);
        element.setProperty("columns", columns);
        element.setProperty("selectortype", "none");
        PropertyListCollection groupby = new PropertyListCollection();
        PropertyList group = new PropertyList();
        group.setProperty("id", "sdcid");
        group.setProperty("columnid", "sdcid");
        group.setProperty("title", this.getTranslationProcessor().translate("SDC:"));
        groupby.add(group);
        element.setProperty("groupby", groupby);
        PropertyListCollection sortby = new PropertyListCollection();
        PropertyList sort = new PropertyList();
        sort.setProperty("id", "sdcid");
        sort.setProperty("columnid", "sdcid");
        sort.setProperty("asc_desc", "d");
        sortby.add(sort);
        element.setProperty("sortby", sortby);
        element.setProperty("showgroupby", "Y");
        element.setProperty("groupbycolumns", "sdcid");
        element.setProperty("hideselectedcount", "Y");
        element.setProperty("hidegroupingtopheader", "Y");
        listView.setElementid("listview");
        listView.setElementProperties(element);
        return listView.getHtml();
    }

    private String concatFields(String ... fields) {
        String str = "";
        String cFNS = "{fn concat(";
        String cFNE = ")}";
        String specialDelimer = "";
        boolean isOracle = this.connectionInfo.isOracle();
        boolean firstItem = true;
        for (String f : fields) {
            if (firstItem) {
                str = isOracle ? f : "cast(" + f + " as nvarchar(100))";
                firstItem = false;
                continue;
            }
            str = cFNS + cFNS + str + ",'" + specialDelimer + "'" + cFNE + "," + (isOracle ? f : "cast(" + f + " as nvarchar(100))") + cFNE;
        }
        return str;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.SDITag;
import com.labvantage.sapphire.tagext.SDITagUtil;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseTagSupport;
import sapphire.tagext.SDITagInfo;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;

public class SDIInputTag
extends BaseTagSupport {
    private String data = "primary";
    private String columnid = "";
    private String row = "";
    private String find = "";
    private String nullValue = "";
    private String readonly = "false";
    private String wrap = "false";
    private String value = "";
    private String align = "";
    private String mode = "input";
    private String style = "";
    private String size = "";
    private String maxlen = "";
    private String tabindex = "";
    private String rows = "";
    private String cols = "";
    private String sdcid = "";
    private String queryfrom = "(default)";
    private String querywhere = "";
    private String queryorderby = "";
    private String reftypeid = "";
    private String sql = "";
    private String img = "";
    private String imgtext = "";
    private String extraattributes = "";
    private String onchange = "";
    private String onkeydown = "";
    private String onfocus = "";
    private String display = "";
    private PropertyList attributes;

    public void setData(String data) {
        this.data = data;
    }

    public void setColumnid(String columnid) {
        this.columnid = columnid;
    }

    protected String getColumnid() {
        return this.columnid;
    }

    public void setFind(String find) {
        this.find = find;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public void setNullvalue(String nullValue) {
        this.nullValue = nullValue;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setSize(String size) {
        this.size = size;
    }

    protected String getSize() {
        return this.size;
    }

    public void setMaxlength(String maxlen) {
        this.maxlen = maxlen;
    }

    public void setTabindex(String tabindex) {
        this.tabindex = tabindex;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public void setCols(String cols) {
        this.cols = cols;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setReftypeid(String reftypeid) {
        this.reftypeid = reftypeid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public void setQueryfrom(String queryfrom) {
        this.queryfrom = queryfrom;
    }

    public void setQuerywhere(String querywhere) {
        this.querywhere = querywhere;
    }

    public void setQueryorderby(String queryorderby) {
        this.queryorderby = queryorderby;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setImgtext(String imgtext) {
        this.imgtext = imgtext;
    }

    public void setExtraattributes(String extraattributes) {
        this.extraattributes = extraattributes;
    }

    public void setOnchange(String onchange) {
        this.onchange = onchange;
    }

    public void setOnkeydown(String onkeydown) {
        this.onkeydown = onkeydown;
    }

    public void setOnfocus(String onfocus) {
        this.onfocus = onfocus;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public void setWrap(String wrap) {
        this.wrap = wrap;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.attributes = new PropertyList();
        if (this.isControlledPage()) {
            SDITag sditag = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
            if (sditag != null) {
                SDITagInfo sdiInfo = (SDITagInfo)this.pageContext.getAttribute("sdiinfo");
                this.evaluateExpressions();
                this.attributes.setProperty("_prefix", sditag.getId());
                SDITagUtil.setIdentifierAttributes(this.attributes, sdiInfo);
                this.write(SDITagUtil.getInstance(this.pageContext).getInputHtml(this.attributes, sdiInfo));
                sdiInfo.getQueryData(this.data).addGridColumn(this.attributes.getProperty("name"));
            } else {
                this.write("TAG ERROR: sdiinput tag must be nested in a sdi and sdicol tag.");
            }
        } else {
            this.write("TAG ERROR: Request context null");
        }
        return 1;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.data = "primary";
        this.columnid = "";
        this.row = "";
        this.find = "";
        this.nullValue = "";
        this.readonly = "false";
        this.wrap = "false";
        this.value = "";
        this.align = "";
        this.mode = "input";
        this.style = "";
        this.size = "";
        this.maxlen = "";
        this.tabindex = "";
        this.rows = "";
        this.cols = "";
        this.sdcid = "";
        this.queryfrom = "(default)";
        this.querywhere = "";
        this.queryorderby = "";
        this.reftypeid = "";
        this.sql = "";
        this.img = "";
        this.imgtext = "";
        this.extraattributes = "";
        this.onchange = "";
        this.onkeydown = "";
        this.onfocus = "";
        this.display = "";
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this.attributes.setProperty("data", JstlUtil.evaluateExpression(this.data, this.pageContext, "primary").toString());
        this.attributes.setProperty("columnid", JstlUtil.evaluateExpression(this.columnid, this.pageContext, "").toString());
        this.attributes.setProperty("row", JstlUtil.evaluateExpression(this.row, this.pageContext, "").toString());
        this.attributes.setProperty("find", JstlUtil.evaluateExpression(this.find, this.pageContext, "").toString());
        this.attributes.setProperty("value", JstlUtil.evaluateExpression(this.value, this.pageContext, "").toString());
        this.attributes.setProperty("nullvalue", JstlUtil.evaluateExpression(this.nullValue, this.pageContext, "").toString());
        this.attributes.setProperty("display", JstlUtil.evaluateExpression(this.display, this.pageContext, "").toString());
        this.attributes.setProperty("reftypeid", JstlUtil.evaluateExpression(this.reftypeid, this.pageContext, "").toString());
        this.attributes.setProperty("sdcid", JstlUtil.evaluateExpression(this.sdcid, this.pageContext, "").toString());
        this.attributes.setProperty("queryfrom", JstlUtil.evaluateExpression(this.queryfrom, this.pageContext, "").toString());
        this.attributes.setProperty("querywhere", JstlUtil.evaluateExpression(this.querywhere, this.pageContext, "").toString());
        this.attributes.setProperty("queryorderby", JstlUtil.evaluateExpression(this.queryorderby, this.pageContext, "").toString());
        this.attributes.setProperty("sql", JstlUtil.evaluateExpression(this.sql, this.pageContext, "").toString());
        this.attributes.setProperty("mode", JstlUtil.evaluateExpression(this.mode, this.pageContext, "").toString());
        this.attributes.setProperty("align", JstlUtil.evaluateExpression(this.align, this.pageContext, "").toString());
        this.attributes.setProperty("style", JstlUtil.evaluateExpression(this.style, this.pageContext, "").toString());
        this.attributes.setProperty("size", JstlUtil.evaluateExpression(this.size, this.pageContext, "").toString());
        this.attributes.setProperty("maxlen", JstlUtil.evaluateExpression(this.maxlen, this.pageContext, "").toString());
        this.attributes.setProperty("tabindex", JstlUtil.evaluateExpression(this.tabindex, this.pageContext, "").toString());
        this.attributes.setProperty("rows", JstlUtil.evaluateExpression(this.rows, this.pageContext, "").toString());
        this.attributes.setProperty("cols", JstlUtil.evaluateExpression(this.cols, this.pageContext, "").toString());
        this.attributes.setProperty("img", JstlUtil.evaluateExpression(this.img, this.pageContext, "").toString());
        this.attributes.setProperty("imgtext", JstlUtil.evaluateExpression(this.imgtext, this.pageContext, "").toString());
        this.attributes.setProperty("extraattributes", JstlUtil.evaluateExpression(this.extraattributes, this.pageContext, "").toString());
        this.attributes.setProperty("onchange", JstlUtil.evaluateExpression(this.onchange, this.pageContext, "").toString());
        this.attributes.setProperty("onkeydown", JstlUtil.evaluateExpression(this.onkeydown, this.pageContext, "").toString());
        this.attributes.setProperty("onfocus", JstlUtil.evaluateExpression(this.onfocus, this.pageContext, "").toString());
        this.attributes.setProperty("readonly", JstlUtil.evaluateExpression(this.readonly, this.pageContext, "").toString());
        this.attributes.setProperty("wrap", JstlUtil.evaluateExpression(this.wrap, this.pageContext, "").toString());
    }
}


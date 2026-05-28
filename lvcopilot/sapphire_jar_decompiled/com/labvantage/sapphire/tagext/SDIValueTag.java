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

public class SDIValueTag
extends BaseTagSupport {
    private String data = "primary";
    private String columnid = "";
    private String find = "";
    private String row = "";
    private String nullValue = "";
    private String display = "";
    private PropertyList attributes;

    public void setData(String data) {
        this.data = data;
    }

    public void setColumnid(String columnid) {
        this.columnid = columnid;
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

    public void setDisplay(String display) {
        this.display = display;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.attributes = new PropertyList();
        SDITag sdi = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        if (sdi != null) {
            this.evaluateExpressions();
            SDITagInfo sdiInfo = (SDITagInfo)this.pageContext.getAttribute("sdiinfo");
            SDITagUtil.setIdentifierAttributes(this.attributes, sdiInfo);
            this.write(this.attributes.getProperty("value"));
        } else {
            this.write("TAG ERROR: sdivalue tag must be nested in a sdi and sdicol tag.");
        }
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.data = "primary";
        this.columnid = "";
        this.find = "";
        this.row = "";
        this.nullValue = "";
        this.display = "";
        super.doEndTag();
        return 6;
    }

    private void evaluateExpressions() {
        this.attributes.setProperty("data", JstlUtil.evaluateExpression(this.data, this.pageContext, "primary").toString());
        this.attributes.setProperty("columnid", JstlUtil.evaluateExpression(this.columnid, this.pageContext, "").toString());
        this.attributes.setProperty("row", JstlUtil.evaluateExpression(this.row, this.pageContext, "").toString());
        this.attributes.setProperty("find", JstlUtil.evaluateExpression(this.find, this.pageContext, "").toString());
        this.attributes.setProperty("nullvalue", JstlUtil.evaluateExpression(this.nullValue, this.pageContext, "").toString());
        this.attributes.setProperty("display", JstlUtil.evaluateExpression(this.display, this.pageContext, "").toString());
    }
}


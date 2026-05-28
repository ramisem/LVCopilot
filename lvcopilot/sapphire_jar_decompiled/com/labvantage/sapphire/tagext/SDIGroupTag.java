/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;

public class SDIGroupTag
extends BaseBodyTagSupport {
    private QueryData _querydata = null;
    private String _data = "primary";
    private String _error = "";
    private String _sort = "";
    private String _group = "";
    private String _filter = "";

    public void setSort(String sort) {
        this._sort = sort;
    }

    public void setGroup(String group) {
        this._group = group;
    }

    public void setFilter(String filter) {
        this._filter = filter;
    }

    public String getFilter() {
        return this._filter;
    }

    public void setData(String data) {
        this._data = data;
    }

    public String getData() {
        return this._data;
    }

    public int doStartTag() throws JspTagException {
        int rc = 2;
        this.doInit();
        this.evaluateExpressions();
        SDITag sditag = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        if (sditag != null) {
            this._querydata = sditag.getData(this._data, this._filter);
            if (this._group != null && this._group.length() > 0 && (this._sort == null || this._sort.length() == 0)) {
                this._error = this._error + "TAG ERROR: sdigroup tag must have a sort specified when a group attribute is specified";
                rc = 0;
            }
        } else {
            this._error = this._error + "TAG ERROR: sdigroup tag must be nested in a sdi tag";
            rc = 0;
        }
        return rc;
    }

    public void doInitBody() throws JspTagException {
        this._querydata.resetGroup(this._sort, this._group);
    }

    public int doAfterBody() throws JspTagException {
        int rc = 2;
        if (!this._querydata.nextGroup(-1)) {
            this.writeBodyContent();
            rc = 0;
        }
        return rc;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            this.write(this._error);
        }
        this._querydata = null;
        this._data = "primary";
        this._error = "";
        this._sort = "";
        this._group = "";
        this._filter = "";
        super.doEndTag();
        return rc;
    }

    private void evaluateExpressions() {
        this._data = JstlUtil.evaluateExpression(this._data, this.pageContext, "").toString();
        this._sort = JstlUtil.evaluateExpression(this._sort, this.pageContext, "").toString();
        this._group = JstlUtil.evaluateExpression(this._group, this.pageContext, "").toString();
        this._filter = JstlUtil.evaluateExpression(this._filter, this.pageContext, "").toString();
    }
}


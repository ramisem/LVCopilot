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
import com.labvantage.sapphire.tagext.QueryTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;

public class QueryGroupTag
extends BaseBodyTagSupport {
    private QueryData _querydata = null;
    private String _error = "";
    private String _sort = "";
    private String _group = "";

    public void setSort(String sort) {
        this._sort = sort;
    }

    public void setGroup(String group) {
        this._group = group;
    }

    public int doStartTag() throws JspTagException {
        this.evaluateExpressions();
        int rc = 2;
        QueryTag query = (QueryTag)TagSupport.findAncestorWithClass((Tag)this, QueryTag.class);
        if (query != null) {
            this._querydata = query.getQuerydata();
        } else {
            this._error = this._error + "TAG ERROR: querygroup tag must be nested in a query tag";
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
            rc = 5;
            this.write(this._error);
        }
        this._querydata = null;
        this._error = "";
        this._sort = "";
        this._group = "";
        super.doEndTag();
        return rc;
    }

    private void evaluateExpressions() {
        this._sort = JstlUtil.evaluateExpression(this._sort, this.pageContext, "").toString();
        this._group = JstlUtil.evaluateExpression(this._group, this.pageContext, "").toString();
    }
}


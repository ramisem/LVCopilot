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

public class QueryRowTag
extends BaseBodyTagSupport {
    private QueryData _querydata = null;
    private String _error = "";
    private String _sort = "";
    private String _filter = "";
    private int _startrow = -1;
    private int _endrow = -1;
    private String _var = "currentRow";

    public void setSort(String sort) {
        this._sort = sort;
    }

    public void setFilter(String filter) {
        this._filter = filter;
    }

    public void setStartrow(String startrow) {
        this._startrow = Integer.parseInt(JstlUtil.evaluateExpression(startrow, this.pageContext, "").toString()) - 1;
    }

    public void setEndrow(String endrow) {
        this._endrow = Integer.parseInt(JstlUtil.evaluateExpression(endrow, this.pageContext, "").toString()) + 1;
    }

    public void setVar(String var) {
        this._var = var;
    }

    public int doStartTag() throws JspTagException {
        this.evaluateExpressions();
        int rc = 2;
        QueryTag query = (QueryTag)TagSupport.findAncestorWithClass((Tag)this, QueryTag.class);
        if (query != null) {
            this._querydata = query.getQuerydata(this._filter);
        } else {
            this._error = this._error + "TAG ERROR: queryrow tag must be nested in a query tag";
            rc = 0;
        }
        return rc;
    }

    public void doInitBody() throws JspTagException {
        this._querydata.resetRow(this._startrow);
        if (this._sort != null && this._sort.length() > 0) {
            this._querydata.sort(this._sort);
        }
        this._querydata.nextRow(this._endrow);
        if (this._querydata.getCurrentRow() >= 0 && this._querydata.getCurrentRow() < this._querydata.getRowCount()) {
            this.pageContext.setAttribute(this._var, this._querydata.get(this._querydata.getCurrentRow()));
        }
    }

    public int doAfterBody() throws JspTagException {
        int rc = 2;
        if (this._querydata.nextRow(this._endrow)) {
            if (this._querydata.getCurrentRow() >= 0 && this._querydata.getCurrentRow() < this._querydata.getRowCount()) {
                this.pageContext.setAttribute(this._var, this._querydata.get(this._querydata.getCurrentRow()));
            }
        } else {
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
        this._filter = "";
        this._startrow = -1;
        this._endrow = -1;
        this._var = "currentRow";
        super.doEndTag();
        return rc;
    }

    private void evaluateExpressions() {
        this._sort = JstlUtil.evaluateExpression(this._sort, this.pageContext, "").toString();
        this._filter = JstlUtil.evaluateExpression(this._filter, this.pageContext, "").toString();
    }
}


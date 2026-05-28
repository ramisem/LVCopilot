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
import com.labvantage.sapphire.tagext.RefTypeTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseBodyTagSupport;

public class RefTypeRowTag
extends BaseBodyTagSupport {
    private QueryData _querydata = null;
    private String _error = "";
    private String _sort = "";
    private int _startrow = -1;
    private int _endrow = -1;

    public void setSort(String sort) {
        this._sort = sort;
    }

    public void setStartrow(String startrow) {
        this._startrow = Integer.parseInt(startrow) - 1;
    }

    public void setEndrow(String endrow) {
        this._endrow = Integer.parseInt(endrow) + 1;
    }

    public int doStartTag() throws JspTagException {
        int rc = 2;
        RefTypeTag reftype = (RefTypeTag)TagSupport.findAncestorWithClass((Tag)this, RefTypeTag.class);
        if (reftype != null) {
            this._querydata = reftype.getQuerydata();
        } else {
            this._error = this._error + "TAG ERROR: RefTyperow tag must be nested in a RefType tag";
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
    }

    public int doAfterBody() throws JspTagException {
        int rc = 2;
        if (!this._querydata.nextRow(this._endrow)) {
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
        this._startrow = -1;
        this._endrow = -1;
        super.doEndTag();
        return rc;
    }
}


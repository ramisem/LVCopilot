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

public class QueryColTag
extends BaseBodyTagSupport {
    private QueryData _querydata = null;

    public int doStartTag() throws JspTagException {
        int rc = 2;
        QueryTag query = (QueryTag)TagSupport.findAncestorWithClass((Tag)this, QueryTag.class);
        if (query != null) {
            this._querydata = query.getQuerydata();
        } else {
            this.write("TAG ERROR: querycol tag must be nested in a query and queryrow tag.");
            rc = 0;
        }
        return rc;
    }

    public void doInitBody() throws JspTagException {
        this._querydata.nextCol();
    }

    public int doAfterBody() throws JspTagException {
        int rc = 2;
        if (!this._querydata.nextCol()) {
            this.writeBodyContent();
            rc = 0;
        }
        return rc;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this._querydata = null;
        super.doEndTag();
        return 6;
    }
}


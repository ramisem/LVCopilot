/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.QueryColTag;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.QueryTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseTagSupport;

public class QueryHeaderTag
extends BaseTagSupport {
    private QueryData _querydata = null;

    public int doStartTag() throws JspTagException {
        String output = "";
        QueryTag query = (QueryTag)TagSupport.findAncestorWithClass((Tag)this, QueryTag.class);
        if (query != null) {
            this._querydata = query.getQuerydata();
            QueryColTag querycol = (QueryColTag)TagSupport.findAncestorWithClass((Tag)this, QueryColTag.class);
            output = querycol != null ? this._querydata.getColumnId() : "TAG ERROR: queryheader tag must be nested in a querycol tag.";
        } else {
            output = "TAG ERROR: queryheader tag must be nested in a query and querycol tag.";
        }
        this.write(output);
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this._querydata = null;
        super.doEndTag();
        return 6;
    }
}


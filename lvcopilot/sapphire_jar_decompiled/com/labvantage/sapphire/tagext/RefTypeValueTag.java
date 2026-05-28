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
import sapphire.tagext.BaseTagSupport;
import sapphire.util.SafeHTML;

public class RefTypeValueTag
extends BaseTagSupport {
    private QueryData _querydata = null;
    private String _columnid = "refvalueid";

    public void setColumnid(String columnid) {
        this._columnid = columnid;
    }

    public int doStartTag() throws JspTagException {
        String output = "";
        RefTypeTag reftype = (RefTypeTag)TagSupport.findAncestorWithClass((Tag)this, RefTypeTag.class);
        if (reftype != null) {
            this._querydata = reftype.getQuerydata();
            output = this._querydata.getValue(this._columnid, this._querydata.getNullValue());
        } else {
            output = "TAG ERROR: reftypevalue tag must be nested in a reftype tag.";
        }
        this.write(SafeHTML.encodeForHTMLAttribute(output));
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this._querydata = null;
        this._columnid = "refvalueid";
        super.doEndTag();
        return 6;
    }
}


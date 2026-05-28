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
import com.labvantage.sapphire.tagext.SDIRowTag;
import com.labvantage.sapphire.tagext.SDITag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseBodyTagSupport;

public class SDIColTag
extends BaseBodyTagSupport {
    private QueryData _querydata = null;
    private String _data = "primary";

    public void setData(String data) {
        this._data = data;
    }

    public int doStartTag() throws JspTagException {
        int rc = 2;
        SDITag sditag = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        if (sditag != null) {
            this._querydata = sditag.getData(this._data, "");
            SDIRowTag sdirow = (SDIRowTag)TagSupport.findAncestorWithClass((Tag)this, SDIRowTag.class);
            if (sdirow == null) {
                this.write("TAG ERROR: sdicol tag must be nested in a sdirow tag.");
                rc = 0;
            }
        } else {
            this.write("TAG ERROR: sdicol tag must be nested in an sdi and sdirow tag.");
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
        this._data = "primary";
        super.doEndTag();
        return 6;
    }
}


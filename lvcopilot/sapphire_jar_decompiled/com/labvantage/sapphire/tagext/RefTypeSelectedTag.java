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

public class RefTypeSelectedTag
extends BaseTagSupport {
    private QueryData _querydata = null;
    private String _columnid = "refvalueid";
    private String _selectedtext = "SELECTED";

    public void setColumnid(String columnid) {
        this._columnid = columnid;
    }

    public void setSelectedtext(String selectedtext) {
        this._selectedtext = selectedtext;
    }

    public int doStartTag() throws JspTagException {
        String output = "";
        RefTypeTag reftype = (RefTypeTag)TagSupport.findAncestorWithClass((Tag)this, RefTypeTag.class);
        if (reftype != null) {
            this._querydata = reftype.getQuerydata();
            if (reftype.getSelected().equalsIgnoreCase(this._querydata.getValue(this._columnid, this._querydata.getNullValue()))) {
                output = this._selectedtext;
            }
        } else {
            output = "TAG ERROR: reftypeselected tag must be nested in a reftype tag.";
        }
        this.write(output);
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this._querydata = null;
        this._columnid = "refvalueid";
        this._selectedtext = "SELECTED";
        super.doEndTag();
        return 6;
    }
}


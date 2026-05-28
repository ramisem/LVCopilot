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
import com.labvantage.sapphire.tagext.SDIColTag;
import com.labvantage.sapphire.tagext.SDITag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.BaseTagSupport;
import sapphire.util.SDIData;

public class SDINameTag
extends BaseTagSupport {
    private QueryData _querydata = null;
    private String _columnid = "";
    private String _data = "primary";
    private String _find = null;
    private int _row = -1;

    public void setData(String data) {
        this._data = data;
    }

    public void setColumnid(String columnid) {
        this._columnid = columnid;
    }

    public void setFind(String find) {
        this._find = find;
    }

    public void setRow(String row) {
        this._row = Integer.parseInt(row);
    }

    public int doStartTag() throws JspTagException {
        String output = "";
        SDITag sdi = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        if (sdi != null) {
            this._querydata = sdi.getData(this._data);
            SDIColTag sdicol = (SDIColTag)TagSupport.findAncestorWithClass((Tag)this, SDIColTag.class);
            output = this._columnid.length() == 0 ? SDIData.getDatasetCode(this._data) + this._querydata.getRowId(this._querydata.getCurrentRow()) + "_" : (this._row > -1 ? SDIData.getDatasetCode(this._data) + this._querydata.getRowId(this._row) + "_" + this._columnid : (this._find != null && this._find.length() > 0 ? SDIData.getDatasetCode(this._data) + this._querydata.getRowId(this._querydata.findRow(this._find)) + "_" + this._columnid : SDIData.getDatasetCode(this._data) + this._querydata.getRowId(this._querydata.getCurrentRow()) + "_" + this._columnid));
        } else {
            output = "TAG ERROR: sdiname tag must be nested in a sdi and sdicol tag.";
        }
        this.write(output);
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this._querydata = null;
        this._columnid = "";
        this._data = "primary";
        this._find = null;
        this._row = -1;
        super.doEndTag();
        return 6;
    }
}


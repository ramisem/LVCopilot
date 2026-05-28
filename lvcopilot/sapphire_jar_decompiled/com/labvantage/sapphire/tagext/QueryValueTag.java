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
import sapphire.util.JstlUtil;
import sapphire.util.StringUtil;

public class QueryValueTag
extends BaseTagSupport {
    private QueryData _querydata = null;
    private String _columnid = "";
    private String[] _values = null;
    private String[] _displays = null;
    private String _find = null;
    private String _nullvalue = null;
    private int _row = -1;
    private int _col = -1;

    public void setColumnid(String columnid) {
        this._columnid = columnid;
    }

    public void setCol(int col) {
        this._col = col;
    }

    public void setCol(String col) {
        this._col = Integer.parseInt(col);
    }

    public void setNullvalue(String nullvalue) {
        this._nullvalue = nullvalue;
    }

    public void setRow(String row) {
        this._row = Integer.parseInt(row);
    }

    public void setFind(String find) {
        this._find = find;
    }

    public void setDisplay(String display) {
        String[] displays = StringUtil.split(display, ";");
        if (displays != null && displays.length > 0) {
            this._values = new String[displays.length];
            this._displays = new String[displays.length];
            for (int i = 0; i < displays.length; ++i) {
                String[] displayparts = StringUtil.split(displays[i].trim(), "=");
                this._values[i] = displayparts[0].trim();
                this._displays[i] = displayparts.length > 1 ? displayparts[1].trim() : this._querydata.getNullValue();
            }
        }
    }

    public int doStartTag() throws JspTagException {
        this.evaluateExpressions();
        String output = "";
        QueryTag query = (QueryTag)TagSupport.findAncestorWithClass((Tag)this, QueryTag.class);
        if (query != null) {
            this._querydata = query.getQuerydata();
            QueryColTag querycol = (QueryColTag)TagSupport.findAncestorWithClass((Tag)this, QueryColTag.class);
            String nv = this._nullvalue;
            if (nv == null) {
                nv = this._querydata.getNullValue();
            }
            output = this._columnid.length() == 0 ? (querycol == null ? (this._col > 0 ? this._querydata.getValue(this._col - 1, nv) : "TAG ERROR: columnid attribute not specified - queryvalue tag must be nested in a querycol tag.") : this._querydata.getValue(nv)) : (this._row > -1 ? this._querydata.getValue(this._row, this._columnid, nv) : (this._find != null && this._find.length() > 0 ? this._querydata.findColValue(this._find, this._columnid, nv) : this._querydata.getValue(this._columnid, nv)));
            if (this._values != null && this._values.length > 0 && this._displays != null && this._displays.length > 0 && this._values.length == this._displays.length) {
                for (int i = 0; i < this._values.length; ++i) {
                    if (!output.equals(this._values[i])) continue;
                    output = this._displays[i];
                    break;
                }
            }
        } else {
            output = "TAG ERROR: queryvalue tag must be nested in a query and querycol tag.";
        }
        this.write(output);
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this._querydata = null;
        this._columnid = "";
        this._values = null;
        this._displays = null;
        this._find = null;
        this._nullvalue = null;
        this._row = -1;
        this._col = -1;
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this._columnid = JstlUtil.evaluateExpression(this._columnid, this.pageContext, "").toString();
        this._find = JstlUtil.evaluateExpression(this._find, this.pageContext, "").toString();
        this._nullvalue = JstlUtil.evaluateExpression(this._nullvalue, this.pageContext, "").toString();
    }
}


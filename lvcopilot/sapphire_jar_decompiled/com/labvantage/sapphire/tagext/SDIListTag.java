/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.list.List;
import com.labvantage.sapphire.tagext.SDITag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.accessor.SDCProcessor;
import sapphire.tagext.BaseTagSupport;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;

public class SDIListTag
extends BaseTagSupport {
    private String elementid = "";
    private String _sortby = "";
    private String _overridepageorder = "";
    private String _listmode = "";

    public void setElementid(String elementid) {
        this.elementid = elementid;
    }

    public void setOverridepageorder(String overridepageorder) {
        this._overridepageorder = overridepageorder;
    }

    public void setListmode(String listmode) {
        this._listmode = listmode;
    }

    public void setSortby(String sortby) {
        this._sortby = sortby;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        SDITag _sditag = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        if (_sditag != null) {
            SDITagInfo sdiinfo = (SDITagInfo)this.pageContext.getAttribute("sdiinfo");
            SDCProcessor sdcProcessor = new SDCProcessor(this.getConnectionId());
            List list = new List(this.pageContext, sdiinfo, sdcProcessor);
            PropertyList properties = this.requestContext.getPropertyList().getPropertyList(this.elementid);
            list.setBrowser(new Browser(this.pageContext));
            list.setElementProperties(properties);
            if (!"Y".equals(this._overridepageorder)) {
                list.setSortby(this._sortby);
            } else {
                list.setOverridepageorder(this._overridepageorder);
            }
            list.setListmode(this._listmode);
            try {
                this.pageContext.getOut().print(list.getHtml());
            }
            catch (Exception e) {
                this.logError("Stack Trace", e);
                throw new JspTagException(e.getMessage());
            }
        } else {
            throw new JspTagException("Sdilist tag must be used inside sditag.");
        }
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.elementid = "";
        this._sortby = "";
        this._overridepageorder = "";
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this._sortby = JstlUtil.evaluateExpression(this._sortby, this.pageContext, "").toString();
    }
}


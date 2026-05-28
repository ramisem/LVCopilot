/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.maint.Maint;
import com.labvantage.sapphire.tagext.BaseSDIMaintTag;
import com.labvantage.sapphire.tagext.SDIFormTag;
import com.labvantage.sapphire.tagext.SDIRowTag;
import com.labvantage.sapphire.tagext.SDITag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;

public class SDIMaintTag
extends BaseSDIMaintTag {
    private String _style = null;
    private String _viewonly = null;

    public void setStyle(String style) {
        this._style = style;
    }

    public void setViewonly(String viewonly) {
        this._viewonly = viewonly;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void doTag(SDITagInfo sdiInfo) throws JspTagException {
        SDIFormTag _sdiformtag = (SDIFormTag)TagSupport.findAncestorWithClass((Tag)this, SDIFormTag.class);
        SDITag _sditag = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        SDIRowTag _sdirowtag = (SDIRowTag)TagSupport.findAncestorWithClass((Tag)this, SDIRowTag.class);
        if (_sditag == null || _sdirowtag == null) throw new JspTagException("Sdimaint tag must be used inside sdirowtag.");
        this._style = JstlUtil.evaluateExpression(this._style, this.pageContext, "").toString();
        this._viewonly = JstlUtil.evaluateExpression(this._viewonly, this.pageContext, "").toString();
        Maint element = new Maint(this.pageContext, sdiInfo, this.getConnectionId());
        element.setBrowser(new Browser(this.pageContext));
        PropertyList properties = this.requestContext.getPropertyList().getPropertyList(this.elementid);
        if (properties == null) throw new JspTagException("TAG ERROR: sdimaint tag properties not found through id: " + this.id);
        properties.setProperty("_prefix", _sditag.getId());
        element.setElementProperties(properties);
        if (_sdiformtag == null) {
            element.setInsidesdiform(false);
        }
        if (this._viewonly != null && (this._viewonly.equalsIgnoreCase("true") || this._viewonly.equalsIgnoreCase("y") || this._viewonly.equalsIgnoreCase("view"))) {
            properties.setProperty("viewonly", "Y");
        }
        if (this._style != null || this._style.length() > 0) {
            properties.setProperty("style", this._style);
        }
        this.write(element.getHtml());
    }
}


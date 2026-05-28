/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.tagext.SDIFormTag;
import com.labvantage.sapphire.tagext.SDITag;
import com.labvantage.sapphire.util.http.HttpUtil;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.accessor.QueryProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.tagext.BaseTagSupport;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ElementTag
extends BaseTagSupport {
    private String elementid = "";
    private String elementClassname = "";
    private String elementType = "";
    private String oldElementType = "";
    private String gwt = "false";

    public void setElementid(String elementid) {
        this.elementid = elementid;
    }

    public void setClassname(String elementClassname) {
        this.elementClassname = elementClassname;
    }

    public void setType(String elementType) {
        this.oldElementType = this.elementType;
        this.elementType = elementType;
    }

    public void setGwt(String gwt) {
        this.gwt = gwt;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        Trace.setStartCodeBlock("TAG: " + this.elementid);
        PropertyList properties = this.requestContext.getPropertyList(this.elementid);
        if (properties == null) {
            properties = new PropertyList();
        }
        if (this.gwt.equalsIgnoreCase("true") || this.gwt.equalsIgnoreCase("Y")) {
            try {
                this.pageContext.getOut().print(HttpUtil.getGWTElementHTML(this.elementid, this.elementType, properties));
            }
            catch (Exception e) {
                this.logError("ElementTag '" + this.elementid, e);
                throw new JspTagException("ElementTag '" + this.elementid + "' exception: " + e.getMessage());
            }
        }
        if (this.elementClassname.length() == 0 || !this.oldElementType.equals(this.elementType)) {
            if (Trace.on) {
                this.logTrace("Loading element object name for type: " + this.elementType);
            }
            QueryProcessor qp = new QueryProcessor(this.pageContext);
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = qp.getPreparedSqlDataSet("SELECT objectname FROM propertytree WHERE propertytreeid = " + safeSQL.addVar(this.elementType), safeSQL.getValues());
            String string = this.elementClassname = ds != null && ds.size() == 1 ? ds.getString(0, "objectname") : "";
            if (this.elementClassname == null || this.elementClassname.length() == 0) {
                throw new JspTagException("ElementTag exception: class not defined or not found using type '" + this.elementType + "'");
            }
        }
        try {
            if (Trace.on) {
                this.logTrace("Creating element class using: " + this.elementClassname);
            }
            BaseElement element = (BaseElement)Class.forName(this.elementClassname).newInstance();
            element.setElementid(this.elementid);
            element.setElementType(this.elementType);
            element.setElementClass(this.elementClassname);
            element.setPageContext(this.pageContext);
            element.setConnectionId(this.getConnectionId());
            element.setElementProperties(properties);
            if (TagSupport.findAncestorWithClass((Tag)this, SDITag.class) != null) {
                element.setSDIInfo((SDITagInfo)this.pageContext.getAttribute("sdiinfo"));
                SDIFormTag sdiForm = (SDIFormTag)TagSupport.findAncestorWithClass((Tag)this, SDIFormTag.class);
                if (sdiForm != null) {
                    element.setSDIFormId(sdiForm.getId());
                }
            }
            if (Trace.on) {
                this.logTrace("Generating HTML for elementid:" + this.elementid + "...");
            }
            this.pageContext.getOut().print(element.getHtml());
            if (Trace.on) {
                this.logTrace("Done generating HTML for elementid:" + this.elementid);
            }
        }
        catch (Exception e) {
            this.logError("ElementTag '" + this.elementid, e);
            throw new JspTagException("ElementTag '" + this.elementid + "' exception: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        Trace.setEndCodeBlock("TAG: " + this.elementid);
        this.elementid = "";
        this.elementClassname = "";
        this.elementType = "";
        this.oldElementType = "";
        this.gwt = "false";
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this.elementid = JstlUtil.evaluateExpression(this.elementid, this.pageContext, "").toString();
        this.elementClassname = JstlUtil.evaluateExpression(this.elementClassname, this.pageContext, "").toString();
        this.elementType = JstlUtil.evaluateExpression(this.elementType, this.pageContext, "").toString();
        this.gwt = JstlUtil.evaluateExpression(this.gwt, this.pageContext, "").toString();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.tagext.QueryData;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseTagSupport;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;

public class DataViewTag
extends BaseTagSupport {
    private String elementid = "";
    private String data = "";
    private QueryData querydata;

    public void setElementid(String elementid) {
        this.elementid = elementid;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        PropertyList properties = this.requestContext.getPropertyList().getPropertyList(this.elementid);
        Object dataObject = JstlUtil.evaluateExpression(this.data, this.pageContext);
        if (dataObject == null) {
            throw new JspTagException("No data found for the dataview tag");
        }
        if (dataObject instanceof QueryData) {
            this.querydata = (QueryData)dataObject;
        } else if (dataObject instanceof DataSet) {
            this.querydata = new QueryData((DataSet)dataObject);
        } else {
            throw new JspTagException("Cannot handle the data object type for the dataview tag:" + dataObject.getClass().getName());
        }
        DataView dataView = new DataView(this.pageContext, this.querydata, this.getConnectionId());
        dataView.setElementProperties(properties);
        try {
            this.pageContext.getOut().print(dataView.getHtml());
        }
        catch (Exception e) {
            throw new JspTagException(e.getMessage());
        }
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.elementid = "";
        this.data = "";
        this.querydata = null;
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this.elementid = JstlUtil.evaluateExpression(this.elementid, this.pageContext, "").toString();
    }
}


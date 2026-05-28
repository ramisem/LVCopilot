/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.QueryStatus;
import javax.servlet.jsp.JspTagException;
import sapphire.accessor.QueryProcessor;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.tagext.RefTypeTagInfo;
import sapphire.util.JstlUtil;

public class RefTypeTag
extends BaseBodyTagSupport {
    private QueryData _querydata = new QueryData();
    private String _error = "";
    private String _selected = "";
    private String _var = "reftypedata";
    private String _varStatus = "reftypestatus";

    public void setReftypeid(String reftypeid) {
        this.setAttribute("reftypeid", reftypeid);
    }

    public void setSelected(String selected) {
        this._selected = selected;
    }

    public String getSelected() {
        return this._selected;
    }

    public QueryData getQuerydata() {
        return this._querydata;
    }

    public void setVar(String var) {
        this._var = var;
    }

    public void setVarStatus(String varStatus) {
        this._varStatus = varStatus;
    }

    public int doStartTag() throws JspTagException {
        int rc = 0;
        this.doInit();
        this.evaluateExpressions();
        if (this.isControlledPage()) {
            if (this.getRefTypeData() == 1) {
                rc = 2;
            }
        } else {
            this.goErrorPage("RequestContext or controlled page tag does not exist. Tags can only be used via the Request Controller and in a controlled page.");
        }
        return rc;
    }

    public int getRefTypeData() {
        int rc = 2;
        QueryProcessor qp = new QueryProcessor(this.getConnectionId());
        String reftypeid = this.getAttribute("reftypeid");
        if (reftypeid == null || reftypeid.length() == 0) {
            this._error = this._error + "TAG ERROR: Reftypeid not specified in Query tag<br>";
        }
        if (this._error.length() == 0) {
            this._querydata.setQueryData(qp.getRefTypeDataSet(reftypeid));
            if (this._querydata.getQuerydata() != null) {
                this.pageContext.setAttribute("reftypeinfo", (Object)new RefTypeTagInfo(this._querydata));
                this.pageContext.setAttribute(this._var, (Object)this._querydata);
                this.pageContext.setAttribute(this._varStatus, (Object)new QueryStatus(this._querydata));
                rc = 1;
            } else {
                this._error = this._error + "TAG ERROR: Failed to get reftype data";
            }
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        this.writeBodyContent();
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            this.write(this._error);
        }
        this._querydata = new QueryData();
        this._error = "";
        this._selected = "";
        this._var = "reftypedata";
        this._varStatus = "reftypestatus";
        super.doEndTag();
        return rc;
    }

    protected void evaluateExpressions() {
        this.setAttribute("reftypeid", JstlUtil.evaluateExpression(this.getAttribute("reftypeid"), this.pageContext, "").toString());
    }
}


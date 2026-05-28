/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspException
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.PageContext
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 *  org.apache.taglibs.standard.tag.common.core.NullAttributeException
 *  org.apache.taglibs.standard.tag.el.core.ExpressionUtil
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.taglibs.standard.tag.common.core.NullAttributeException;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import sapphire.tagext.BaseBodyTagSupport;

public class SDIIfTag
extends BaseBodyTagSupport {
    private SDITag _sditag = null;
    private String _data = "primary";
    private String _conditionstring = "";
    private String _error = "";

    public void setCondition(String condition) {
        this._conditionstring = condition;
    }

    public void setData(String data) {
        this._data = data;
    }

    public String getCondition() {
        return this._conditionstring;
    }

    public boolean getConditionValue() {
        boolean condition = false;
        try {
            if (this._conditionstring != null && this._conditionstring.length() > 0) {
                Boolean temp = Boolean.valueOf(this._conditionstring);
                condition = temp;
            }
        }
        catch (Exception temp) {
            // empty catch block
        }
        if (!condition) {
            try {
                QueryData qd = this._sditag.getData(this._data);
                if (this._sditag != null && this._conditionstring.equalsIgnoreCase("hasrows")) {
                    if (this._conditionstring.equalsIgnoreCase("hasrows")) {
                        condition = qd != null && qd.getRowCount() > 0;
                    }
                } else if (qd != null && this._sditag != null) {
                    if (this._conditionstring.equalsIgnoreCase("norows")) {
                        condition = qd.getRowCount() == 0;
                    } else if (this._conditionstring.equalsIgnoreCase("retrievedrow")) {
                        condition = qd.getRowStatus(qd.getCurrentRow()).equals("S");
                    } else if (this._conditionstring.equalsIgnoreCase("newrow")) {
                        condition = qd.getRowStatus(qd.getCurrentRow()).equals("I");
                    } else if (this._conditionstring.equalsIgnoreCase("deletedrow")) {
                        condition = qd.getRowStatus(qd.getCurrentRow()).equals("D");
                    } else if (this._conditionstring.equalsIgnoreCase("notdeletedrow")) {
                        condition = !qd.getRowStatus(qd.getCurrentRow()).equals("D");
                    } else if (this._conditionstring.equalsIgnoreCase("modifiedrow")) {
                        condition = qd.getRowStatus(qd.getCurrentRow()).equals("U");
                    } else if (this._conditionstring.equalsIgnoreCase("lockedrow") || this._conditionstring.equalsIgnoreCase("locksuccess")) {
                        condition = qd.getValue("__lockstate", "0").equals("2");
                    } else if (this._conditionstring.equalsIgnoreCase("unlockedrow") || this._conditionstring.equalsIgnoreCase("lockfailure")) {
                        condition = qd.getValue("__lockstate", "0").equals("0");
                    } else if (this._conditionstring.equalsIgnoreCase("haslockedrows")) {
                        condition = this._sditag.getLockoption().length() > 0 ? this._sditag.getRequestStatus() == 2 || this._sditag.getRequestStatus() == 101 : (this._data.equalsIgnoreCase("dataset") || this._data.equalsIgnoreCase("dataitem") || this._data.equalsIgnoreCase("datalimit") || this._data.equalsIgnoreCase("dataapproval") || this._data.equalsIgnoreCase("dataspec") ? this._sditag.getRequestStatus() == 100 || this._sditag.getRequestStatus() == 102 : this._sditag.getRequestStatus() == 100 || this._sditag.getRequestStatus() == 101);
                    }
                } else {
                    condition = true;
                }
            }
            catch (IndexOutOfBoundsException iobe) {
                condition = false;
            }
        }
        return condition;
    }

    public int doStartTag() throws JspTagException {
        int rc = 0;
        this.doInit();
        this._sditag = (SDITag)TagSupport.findAncestorWithClass((Tag)this, SDITag.class);
        try {
            this.evaluateExpressions(this.pageContext);
        }
        catch (Exception e) {
            this.logError("Could not evaluate JSTL expressions", e);
        }
        if (this._sditag != null) {
            if (this.getConditionValue()) {
                rc = 2;
            }
        } else {
            this._error = this._error + "TAG ERROR: sdiif tag must be nested in a sdi tag";
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        this.writeBodyContent();
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        if (this._error.length() != 0) {
            this.write(this._error);
        }
        this._sditag = null;
        this._data = "primary";
        this._conditionstring = "";
        this._error = "";
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions(PageContext pageContext) throws JspException {
        if (this._data.length() > 0) {
            try {
                this._data = ExpressionUtil.evalNotNull((String)"sdiif", (String)"data", (String)this._data, Object.class, (Tag)this, (PageContext)pageContext).toString();
            }
            catch (NullAttributeException ex) {
                this._data = "primary";
            }
        }
        if (this._conditionstring.length() > 0) {
            try {
                this._conditionstring = ExpressionUtil.evalNotNull((String)"sdiif", (String)"condition", (String)this._conditionstring, Object.class, (Tag)this, (PageContext)pageContext).toString();
            }
            catch (NullAttributeException ex) {
                this._conditionstring = "false";
            }
        }
    }
}


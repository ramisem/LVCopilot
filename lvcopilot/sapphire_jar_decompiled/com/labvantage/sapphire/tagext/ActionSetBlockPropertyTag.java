/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.Tag
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.ActionBlockTag;
import com.labvantage.sapphire.tagext.ActionTag;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import sapphire.tagext.BaseTagSupport;
import sapphire.util.JstlUtil;

public class ActionSetBlockPropertyTag
extends BaseTagSupport {
    private String _propertyid = "";
    private String _actionpropertyid = "";
    private String _value = "";

    public void setPropertyid(String id) {
        this._propertyid = id;
    }

    public void setActionpropertyid(String id) {
        this._actionpropertyid = id;
    }

    public void setValue(String value) {
        this._value = value;
    }

    public int doStartTag() throws JspTagException {
        this.evaluateExpressions();
        if (this._propertyid.length() > 0) {
            if (this._value.length() > 0) {
                ActionBlockTag _actionblocktag = (ActionBlockTag)ActionSetBlockPropertyTag.findAncestorWithClass((Tag)this, ActionBlockTag.class);
                if (_actionblocktag == null) {
                    this.write("TAG ERROR: actionblocksetproperty tag must be nested in an actionblock tag.<br>");
                } else {
                    _actionblocktag._actionblock.setBlockProperty(this._propertyid, this._value);
                }
            } else if (this._actionpropertyid.length() > 0) {
                ActionBlockTag _actionblocktag = (ActionBlockTag)ActionSetBlockPropertyTag.findAncestorWithClass((Tag)this, ActionBlockTag.class);
                if (_actionblocktag == null) {
                    this.write("TAG ERROR: actionblocksetproperty tag must be nested in an actionblock tag.<br>");
                } else {
                    ActionTag _actiontag = (ActionTag)ActionSetBlockPropertyTag.findAncestorWithClass((Tag)this, ActionTag.class);
                    if (_actiontag == null) {
                        this.write("TAG ERROR: actionblocksetproperty tag must be nested in an action tag.<br>");
                    } else {
                        String actionid = _actiontag.getName();
                        try {
                            _actionblocktag._actionblock.setActionBlockProperty(actionid, this._propertyid, this._actionpropertyid);
                        }
                        catch (Exception e) {
                            this.write("TAG ERROR: " + e.getMessage() + "<br>");
                        }
                    }
                }
            } else {
                this.write("TAG ERROR: You must specify either an actionpropertyid or a value.<br>");
            }
        } else {
            this.write("TAG ERROR: You must specify a propertyid.<br>");
        }
        return 0;
    }

    protected void evaluateExpressions() {
        this._actionpropertyid = JstlUtil.evaluateExpression(this._actionpropertyid, this.pageContext, "").toString();
        this._propertyid = JstlUtil.evaluateExpression(this._propertyid, this.pageContext, "").toString();
        this._value = JstlUtil.evaluateExpression(this._value, this.pageContext, "").toString();
    }
}


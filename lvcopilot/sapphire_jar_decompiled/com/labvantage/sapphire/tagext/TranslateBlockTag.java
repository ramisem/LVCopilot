/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import java.util.HashMap;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseBodyTagSupport;

public class TranslateBlockTag
extends BaseBodyTagSupport {
    private String _error = "";
    private String _languageid = "";
    private boolean _translate = true;
    private boolean _firstpass = true;
    private HashMap _translationtable = new HashMap();

    public HashMap getTranslationtable() {
        return this._translationtable;
    }

    public void setLanguageid(String languageid) {
        this._languageid = languageid;
    }

    public String getLanguageid() {
        return this._languageid;
    }

    public void setTranslate(String translate) {
        this._translate = translate.equalsIgnoreCase("true");
    }

    public boolean getFirstpass() {
        return this._firstpass;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            rc = 5;
            this.write(this._error);
        } else {
            this.writeBodyContent();
        }
        this._error = "";
        this._languageid = "";
        this._translate = true;
        this._firstpass = true;
        this._translationtable = new HashMap();
        super.doEndTag();
        return rc;
    }
}


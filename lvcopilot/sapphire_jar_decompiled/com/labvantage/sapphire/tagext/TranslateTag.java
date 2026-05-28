/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.tagext.BodyContent
 *  javax.servlet.jsp.tagext.Tag
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.TranslateBlockTag;
import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.BaseBodyTagSupport;

public class TranslateTag
extends BaseBodyTagSupport {
    private String _error = "";
    private TranslateBlockTag _tbt = null;
    private String _languageid = "";

    public void setLanguageid(String languageid) {
        this._languageid = languageid;
    }

    public int doStartTag() throws JspTagException {
        int rc = 2;
        this.doInit();
        if (this._languageid.length() == 0) {
            if (this.requestContext != null) {
                this._tbt = (TranslateBlockTag)TagSupport.findAncestorWithClass((Tag)this, TranslateBlockTag.class);
                if (this._tbt != null) {
                    this._languageid = this._tbt.getLanguageid();
                }
            } else {
                this.goErrorPage("Translations can only be accessed via the RequestController");
            }
            if (this._languageid.length() == 0) {
                RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
                this._languageid = requestContext.getPropertyList().getProperty("language");
            }
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        BodyContent body = this.getBodyContent();
        String text = body.getString();
        String transtext = "";
        TranslationProcessor translator = (TranslationProcessor)this.pageContext.getAttribute("translationprocessor");
        if (translator == null) {
            translator = new TranslationProcessor(this.pageContext);
            this.pageContext.setAttribute("translationprocessor", (Object)translator);
        }
        if (this._languageid.length() > 0) {
            transtext = text.indexOf("{{") < 0 ? translator.translate(text, this._languageid) : translator.translatePartial(text, this._languageid);
        } else if (text.indexOf("{{") >= 0) {
            transtext = translator.translatePartial(text, "(null)");
        }
        try {
            body.clear();
            this.getPreviousOut().print(transtext != null && transtext.length() > 0 ? transtext : text);
            this.writeBodyContent();
        }
        catch (IOException e) {
            throw new JspTagException("Failed to write body: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() > 0) {
            rc = 5;
            this.write(this._error);
        }
        this._error = "";
        this._languageid = "";
        this._tbt = null;
        super.doEndTag();
        return rc;
    }
}


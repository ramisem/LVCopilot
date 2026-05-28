/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.controls.Button;
import javax.servlet.jsp.JspTagException;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.BaseTagSupport;
import sapphire.util.JstlUtil;

public class ButtonTag
extends BaseTagSupport {
    private String id = "";
    private String text = "";
    private String action = "";
    private String img = "";
    private String imgposition = "left";
    private String tip = "";
    private String width = "";
    private String style = "";
    private String appearance = "standard";
    private String highlight = "true";
    private String margin = "";

    public void setId(String id) {
        this.id = id;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

    public void setText(String text) {
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        this.text = tp.translate(text);
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setImgposition(String imgposition) {
        this.imgposition = imgposition;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        this.evaluateExpressions();
        Button button = new Button(this.pageContext);
        button.setAction(this.action);
        if (this.appearance.length() > 0) {
            button.setAppearance(this.appearance);
        }
        if (this.highlight.length() > 0) {
            button.setHighlight(this.highlight);
        }
        button.setId(this.id);
        button.setImg(this.img);
        button.setImgposition(this.imgposition);
        button.setStyle(this.style);
        button.setText(this.text);
        button.setTip(this.tip);
        button.setWidth(this.width);
        button.setMargin(this.margin);
        this.write(button.getHtml());
        return 1;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.id = "";
        this.text = "";
        this.action = "";
        this.img = "";
        this.imgposition = "left";
        this.tip = "";
        this.width = "";
        this.style = "";
        this.appearance = "standard";
        this.highlight = "true";
        this.margin = "";
        super.doEndTag();
        return 6;
    }

    protected void evaluateExpressions() {
        this.id = JstlUtil.evaluateExpression(this.id, this.pageContext, "").toString();
        this.text = JstlUtil.evaluateExpression(this.text, this.pageContext, "").toString();
        this.width = JstlUtil.evaluateExpression(this.width, this.pageContext, "").toString();
        this.action = JstlUtil.evaluateExpression(this.action, this.pageContext, "").toString();
        this.img = JstlUtil.evaluateExpression(this.img, this.pageContext, "").toString();
        this.tip = JstlUtil.evaluateExpression(this.tip, this.pageContext, "").toString();
        this.appearance = JstlUtil.evaluateExpression(this.appearance, this.pageContext, "").toString();
        this.highlight = JstlUtil.evaluateExpression(this.highlight, this.pageContext, "").toString();
        this.style = JstlUtil.evaluateExpression(this.style, this.pageContext, "").toString();
        this.margin = JstlUtil.evaluateExpression(this.margin, this.pageContext, "").toString();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.BaseTagSupport;

public class WizardTag
extends BaseTagSupport {
    private boolean enterNext = true;
    private boolean autoFinish = false;

    public void setEnternext(String enterNext) {
        this.enterNext = enterNext.equalsIgnoreCase("true");
    }

    public void setAutofinish(String autoFinish) {
        this.autoFinish = autoFinish.equalsIgnoreCase("true");
    }

    public int doStartTag() throws JspTagException {
        HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
        this.doInit();
        this.write("<script>");
        this.write("function wizardLoaded() {");
        this.write("\tif ( parent.pageLoaded ) parent.pageLoaded( " + (request.getParameter("wizardindex") == null ? "0" : request.getParameter("wizardindex")) + ");");
        this.write("}");
        if (this.autoFinish) {
            this.write("var autofinish = true;");
            this.write("function doAutoFinish() {");
            this.write("  if ( autofinish ) {");
            this.write("    if ( parent.finish ) parent.finish();");
            this.write("  }");
            this.write("  else {");
            this.write("    wizardLoaded();");
            this.write("  }");
            this.write("}");
            this.write("sapphire.events.attachEvent( window, \"onload\", doAutoFinish );");
        } else {
            this.write("sapphire.events.attachEvent( window, \"onload\", wizardLoaded );");
        }
        if (this.enterNext) {
            this.write("function stepWizard() {");
            this.write("\tif ( parent.next && parent.back && event.keyCode == 13 ) {");
            this.write("\t\tif ( event.shiftKey ) {");
            this.write("\t\t\tparent.back();");
            this.write("\t\t}");
            this.write("\t\telse {");
            this.write("\t\t\tparent.next();");
            this.write("\t\t}");
            this.write("\t}");
            this.write("}");
            this.write("document.body.onkeypress = stepWizard;");
        }
        this.write("</script>");
        return 1;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.enterNext = true;
        this.autoFinish = false;
        super.doEndTag();
        return 6;
    }
}


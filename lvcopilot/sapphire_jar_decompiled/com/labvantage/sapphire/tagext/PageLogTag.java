/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.HashMap;
import javax.servlet.jsp.JspTagException;
import sapphire.SapphireException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.xml.PropertyList;

public class PageLogTag
extends BaseBodyTagSupport {
    private String _error = "";
    private String _title = "";
    private String _tip = "";
    HashMap _pagelogprops = new HashMap();

    public void setTitle(String title) {
        this._title = title;
    }

    public void setTip(String tip) {
        this._tip = tip;
    }

    public void setProperty(String propertyid, String value) {
        this._pagelogprops.put(propertyid, value);
    }

    public int doStartTag() throws JspTagException {
        int rc = 0;
        this.doInit();
        if (this.isControlledPage()) {
            rc = 2;
        } else {
            this.goErrorPage("RequestContext or controlled page tag does not exist. Tags can only be used via the Request Controller and in a controlled page.");
        }
        return rc;
    }

    public int doAfterBody() throws JspTagException {
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        int rc = 6;
        if (this._error.length() == 0) {
            String request = "";
            if (this.requestContext.getProperty("page") != null && this.requestContext.getProperty("page").length() > 0) {
                request = "page=" + this.requestContext.getProperty("page");
            } else if (this.requestContext.getProperty("file") != null && this.requestContext.getProperty("file").length() > 0) {
                request = "file=" + this.requestContext.getProperty("file");
            }
            if (request.length() > 0) {
                try {
                    RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionId());
                    this._pagelogprops.put("currentlayout", this.pageContext.getSession().getAttribute("currentlayout"));
                    this._pagelogprops.put("currentlayoutnode", this.pageContext.getSession().getAttribute("currentlayoutnode"));
                    PropertyList userPreferences = (PropertyList)this.pageContext.getSession().getAttribute("userconfig");
                    this._pagelogprops.put("currentlayouttab", userPreferences.getProperty("genericlayout_lastlinktab"));
                    this._pagelogprops.put("currentlayoutmenu", userPreferences.getProperty("genericlayout_lastlinkmenu"));
                    requestProcessor.logPageAccess(request, this._title, this._tip, this._pagelogprops);
                }
                catch (SapphireException e) {
                    this.logError("Failed to add a new Page Log entry - failed to save log entry", e);
                }
            } else {
                this.logError("Failed to add a new Page Log entry - failed to generate request");
            }
        } else {
            this.write(this._error);
        }
        this._error = "";
        this._title = "";
        this._tip = "";
        super.doEndTag();
        return rc;
    }
}


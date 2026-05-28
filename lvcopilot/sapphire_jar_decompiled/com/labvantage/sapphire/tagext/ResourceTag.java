/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.servlet.command.ResourceRequest;
import java.io.IOException;
import javax.servlet.jsp.JspTagException;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.tagext.BaseTagSupport;
import sapphire.xml.PropertyList;

public class ResourceTag
extends BaseTagSupport {
    private PropertyList include;
    private String type;

    public PropertyList getInclude() {
        return this.include;
    }

    public void setInclude(PropertyList include) {
        this.include = include;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        String connectionId = this.getConnectionId();
        String databaseId = new ConnectionProcessor(connectionId).getConnectionInfo(connectionId).getDatabaseId();
        try {
            String resourceTag = ResourceRequest.getResourceTag(this.pageContext, databaseId, this.include, this.type);
            this.pageContext.getOut().println(resourceTag);
        }
        catch (IOException | SapphireException e) {
            this.logError("ResourceTag '", e);
            throw new JspTagException("ResourceTag '' exception: " + e.getMessage());
        }
        return 0;
    }
}


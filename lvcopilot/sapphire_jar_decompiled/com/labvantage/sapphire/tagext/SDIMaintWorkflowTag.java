/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.pageelements.maint.MaintWorkflow;
import com.labvantage.sapphire.tagext.BaseSDIMaintTag;
import javax.servlet.jsp.JspTagException;
import sapphire.tagext.SDITagInfo;
import sapphire.xml.PropertyList;

public class SDIMaintWorkflowTag
extends BaseSDIMaintTag {
    @Override
    public void doTag(SDITagInfo sdiInfo) throws JspTagException {
        MaintWorkflow element = new MaintWorkflow(this.pageContext, sdiInfo, this.getConnectionId());
        PropertyList properties = this.requestContext.getPropertyList().getPropertyList(this.elementid);
        if (properties != null) {
            element.setElementProperties(properties);
            this.write(element.getHtml());
        } else {
            this.write("TAG ERROR: sdimaintworkflow tag properties not found through id: " + this.id);
        }
    }
}


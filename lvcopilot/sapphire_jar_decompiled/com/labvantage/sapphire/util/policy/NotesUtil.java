/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.util.policy;

import com.labvantage.sapphire.util.groovy.GroovyPolicyUtil;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class NotesUtil {
    private PropertyList element;
    private PropertyList policy;
    private boolean showNotes;
    private boolean showInitially;

    public NotesUtil(PageContext pageContext) {
        this.element = RequestContext.getRequestContext(pageContext).getPropertyList("sdinotes");
        if (this.element == null) {
            this.element = new PropertyList();
        }
        GroovyPolicyUtil groovyPolicyUtil = new GroovyPolicyUtil(pageContext);
        try {
            this.policy = groovyPolicyUtil.getPolicy("NotesPolicy", "Sapphire Custom");
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        this.showNotes = this.element.getProperty("show", "N").equals("Y") && this.policy != null && this.policy.getProperty("enabled", "N").equals("Y");
        this.showInitially = this.element.getProperty("showinitially", "N").equals("Y");
    }

    public boolean showNotes() {
        return this.showNotes;
    }

    public boolean showInitialy() {
        return this.showInitially;
    }

    public void setLinkSDI(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.element.setProperty("linksdinotes", "Y");
        this.element.setProperty("linksdcid", sdcid);
        this.element.setProperty("linkkeyid1", keyid1);
    }

    public String getWidth() {
        return this.element.getProperty("width", "200px");
    }
}


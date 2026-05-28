/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.servlet.jsp.JspTagException;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.xml.PropertyList;

public class SetPropertyListTag
extends BaseBodyTagSupport {
    private String var;
    private String file;
    private boolean updatePropertyTreeData = false;
    private String languageid;

    public void setVar(String var) {
        this.var = var;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setLanguageid(String languageid) {
        this.languageid = languageid;
    }

    public void setUpdate(String update) {
        this.updatePropertyTreeData = update.equals("true");
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        return 2;
    }

    public int doAfterBody() throws JspTagException {
        if (this.requestContext != null) {
            if (this.var != null && this.var.length() > 0) {
                PropertyList dataBlock = this.requestContext.getPropertyList().getPropertyList(this.var);
                if (dataBlock == null) {
                    dataBlock = new PropertyList(this.var);
                }
                this.setPropertyList(dataBlock);
                this.requestContext.getPropertyList().setProperty(this.var, dataBlock);
                this.pageContext.getRequest().setAttribute(this.var, (Object)dataBlock);
            } else {
                this.setPropertyList(this.requestContext.getPropertyList());
            }
            if (this.updatePropertyTreeData) {
                try {
                    RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionId());
                    requestProcessor.addPropertyData(this.requestContext);
                }
                catch (SapphireException e) {
                    throw new JspTagException("Failed to add property data in file tag");
                }
            }
        } else if (this.var != null && this.var.length() > 0) {
            PropertyList propertyList = new PropertyList();
            this.setPropertyList(propertyList);
            this.pageContext.getRequest().setAttribute(this.var, (Object)propertyList);
        } else {
            throw new JspTagException("Var attribute not specified with no RequestContext - can't create an unnamed object in request object.");
        }
        return 0;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.var = null;
        this.file = null;
        this.updatePropertyTreeData = false;
        this.languageid = null;
        super.doEndTag();
        return 6;
    }

    private void setPropertyList(PropertyList target) throws JspTagException {
        try {
            if (Trace.stats) {
                Trace.setStartCodeBlock("TAG.SetPropertyList");
            }
            String xmlString = "";
            if (this.file != null && this.file.length() > 0) {
                String line;
                StringBuffer sb = new StringBuffer();
                URL url = this.pageContext.getServletContext().getResource("/" + this.file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                xmlString = sb.toString();
            } else if (this.bodyContent != null && this.bodyContent.getString().length() > 0) {
                xmlString = this.bodyContent.getString();
            }
            if (xmlString.indexOf("{{") > 0) {
                TranslationProcessor translator;
                if (this.languageid == null || this.languageid.length() == 0) {
                    RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
                    this.languageid = requestContext.getPropertyList().getProperty("language");
                }
                if ((translator = (TranslationProcessor)this.pageContext.getAttribute("translationprocessor")) == null) {
                    translator = new TranslationProcessor(this.pageContext);
                    this.pageContext.setAttribute("translationprocessor", (Object)translator);
                }
                xmlString = translator.translatePartial(xmlString, this.languageid);
            }
            if (xmlString.length() > 0) {
                target.setPropertyList(xmlString);
            }
            if (Trace.stats) {
                Trace.setEndCodeBlock("TAG.SetPropertyList");
            }
        }
        catch (Exception e) {
            this.logError("Stack Trace", e);
            throw new JspTagException(e.getMessage());
        }
    }
}


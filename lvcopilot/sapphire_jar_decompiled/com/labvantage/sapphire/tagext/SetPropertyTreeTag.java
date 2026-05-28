/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspTagException
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.servlet.RequestProcessor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.servlet.jsp.JspTagException;
import sapphire.SapphireException;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;

public class SetPropertyTreeTag
extends BaseBodyTagSupport {
    private String var;
    private String file;
    private String node;
    private String roleList;
    private String moduleList;
    private boolean updatePropertyTreeData = false;
    private boolean dependsfilecommand = false;

    public void setVar(String var) {
        this.var = var;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setRolelist(String roleList) {
        this.roleList = roleList;
    }

    public void setModulelist(String moduleList) {
        this.moduleList = moduleList;
    }

    public void setUpdate(String update) {
        this.updatePropertyTreeData = update.equals("true");
    }

    public void setDependsfilecommand(String dependsfilecommand) {
        this.dependsfilecommand = dependsfilecommand.equals("true");
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        return 2;
    }

    @Override
    public int doEndTag() throws JspTagException {
        this.evaluateExpressions();
        if (this.requestContext != null) {
            if (!this.dependsfilecommand || this.requestContext.getProperty("command").equals("file")) {
                if (this.var != null && this.var.length() > 0) {
                    PropertyList dataBlock = this.requestContext.getPropertyList().getPropertyList(this.var);
                    if (dataBlock == null) {
                        dataBlock = new PropertyList(this.var);
                    }
                    if (this.node != null && this.node.length() > 0) {
                        this.setPropertyTree(dataBlock);
                    }
                    this.requestContext.getPropertyList().setProperty(this.var, dataBlock);
                    this.pageContext.getRequest().setAttribute(this.var, (Object)dataBlock);
                } else if (this.node != null && this.node.length() > 0) {
                    this.setPropertyTree(this.requestContext.getPropertyList());
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
            }
        } else if (this.var != null && this.var.length() > 0) {
            PropertyList propertyList = new PropertyList();
            if (this.node != null && this.node.length() > 0) {
                this.setPropertyTree(propertyList);
            }
            this.pageContext.getRequest().setAttribute(this.var, (Object)propertyList);
        } else {
            throw new JspTagException("Var attribute not specified with no RequestContext - can't create an unnamed object in request object.");
        }
        this.var = null;
        this.file = null;
        this.node = null;
        this.roleList = null;
        this.moduleList = null;
        this.updatePropertyTreeData = false;
        this.dependsfilecommand = false;
        super.doEndTag();
        return 0;
    }

    private void setPropertyTree(PropertyList target) throws JspTagException {
        try {
            if (this.file != null && this.file.length() > 0) {
                URL url = this.pageContext.getServletContext().getResource("/" + this.file);
                StringBuffer xml = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    xml.append(line);
                }
                reader.close();
                target.setPropertyTree(xml.toString(), this.node, null);
            } else if (this.bodyContent != null && this.bodyContent.getString().length() > 0) {
                target.setPropertyTree(this.bodyContent.getString(), this.node, null);
            }
            if (this.roleList != null || this.moduleList != null) {
                String xmlString = target.toXMLString(this.roleList == null || this.roleList.length() == 0 ? "<ALL>" : this.roleList, this.moduleList == null || this.moduleList.length() == 0 ? "<ALL>" : this.moduleList);
                target.clear();
                target.setPropertyList(xmlString);
            }
        }
        catch (Exception se) {
            throw new JspTagException(se.getMessage());
        }
    }

    private void evaluateExpressions() {
        this.roleList = JstlUtil.evaluateExpression(this.roleList, this.pageContext, "").toString();
        this.moduleList = JstlUtil.evaluateExpression(this.moduleList, this.pageContext, "").toString();
    }
}


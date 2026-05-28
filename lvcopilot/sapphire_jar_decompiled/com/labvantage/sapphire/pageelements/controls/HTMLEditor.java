/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class HTMLEditor
extends BaseElement {
    private HTMLEditorControl htmlEditorControl = null;
    private boolean devMode = false;
    private TranslationProcessor tp = null;
    private PropertyList userConfig = null;
    HttpServletRequest request = null;

    public HTMLEditor(PageContext pageContext) {
        this.setPageContext(pageContext);
        this.tp = this.getTranslationProcessor();
        if (this.requestContext == null && pageContext != null && pageContext.getRequest() instanceof HttpServletRequest) {
            this.requestContext = RequestContext.getInstance((HttpServletRequest)pageContext.getRequest());
        }
        this.userConfig = this.requestContext != null ? this.requestContext.getPropertyList("userconfig") : new PropertyList();
        ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
        try {
            this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            this.devMode = false;
        }
        this.htmlEditorControl = new HTMLEditorControl(this.logger);
        this.htmlEditorControl.setDebug(this.devMode);
        this.htmlEditorControl.setDevMode(this.devMode);
        this.htmlEditorControl.setRtl(this.getConnectionProcessor().getSapphireConnection().isRtl());
        this.htmlEditorControl.setUseFullIncludes(this.getConnectionProcessor().getSapphireConnection().getUseFullIncludes());
    }

    public void setEvent(String id, HTMLEditorControl.Events event, String script) {
        this.htmlEditorControl.setEvent(id, event, script);
    }

    public void setButton(String id, String text, String script) {
        this.htmlEditorControl.setButton(id, text, script);
    }

    public void setMenuItem(String id, String text, String menu, String script) {
        this.htmlEditorControl.setMenuItem(id, text, menu, script);
    }

    public void setSDI(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.htmlEditorControl.setSDI(sdcid, keyid1, keyid2, keyid3);
    }

    public void setCanUpload(boolean canUpload) {
        this.htmlEditorControl.setCanUpload(canUpload);
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setShowToolbar(boolean show) {
        this.htmlEditorControl.setShowToolbar(show);
    }

    public void setSourceMode(int sourcemode) {
        this.htmlEditorControl.setSourceMode(sourcemode);
    }

    public int getSourceMode() {
        return this.htmlEditorControl.getSourceMode();
    }

    public void setViewOnly(String viewonly) {
        this.htmlEditorControl.setViewOnly(viewonly);
    }

    public void setViewOnly(boolean viewonly) {
        this.htmlEditorControl.setViewOnly(viewonly);
    }

    public void setId(String id) {
        this.htmlEditorControl.setId(id);
        super.setElementid(id);
    }

    public void setEditorType(HTMLEditorControl.EditorType editorType) {
        this.htmlEditorControl.setEditorType(editorType);
    }

    public void setInline(boolean inline) {
        this.htmlEditorControl.setInline(inline);
    }

    public HTMLEditorControl.Editor getEditor() {
        return this.htmlEditorControl.getEditor();
    }

    @Override
    public void setElementid(String elementid) {
        this.setId(elementid);
    }

    private String getPageHTML(String htmlcontent) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<div sapphire=\"page\" id=\"page001\" class=\"page\" style=\"box-sizing:border-box;");
        buffer.append("width:").append(HTMLEditorControl.PageMode.LETTERPORTRAIT.width).append(";");
        buffer.append("height:").append(HTMLEditorControl.PageMode.LETTERPORTRAIT.height).append(";");
        buffer.append("padding:25.4mm 25.4mm 25.4mm 25.4mm;\" contenteditable=\"true\">");
        buffer.append(htmlcontent);
        buffer.append("</div>");
        return buffer.toString();
    }

    public void setContent(String htmlcontent) {
        this.htmlEditorControl.setContent(htmlcontent);
    }

    public void setWidth(String width) {
        this.htmlEditorControl.setWidth(width);
    }

    public void setHeight(String height) {
        this.htmlEditorControl.setHeight(height);
    }

    private void renderScript(StringBuffer content) {
        HttpServletRequest r;
        Object object = this.request != null ? this.request : (r = this.pageContext != null ? (HttpServletRequest)this.pageContext.getRequest() : null);
        if (r == null || r.getAttribute("htmleditor_rendered_mainscript") == null || r.getAttribute("htmleditor_rendered_mainscript") == Boolean.FALSE) {
            content.append(this.htmlEditorControl.getIncludesHTML((HttpServletRequest)(r != null ? r : null)));
        }
        if (r != null) {
            r.setAttribute("htmleditor_rendered_mainscript", (Object)Boolean.TRUE);
        }
        content.append("<script type=\"text/javascript\">");
        content.append(this.htmlEditorControl.getScript());
        if (this.browser == null || this.browser.isIE() || this.browser.isChrome() || this.browser.isSafari()) {
            content.append("sapphire.events.attachEvent(window,'load',function(){").append(this.htmlEditorControl.getInitScript()).append("});");
        }
        content.append("</script>");
    }

    @Override
    public String getHtml() {
        StringBuffer content = new StringBuffer();
        if (this.htmlEditorControl.getId().length() > 0) {
            this.renderScript(content);
            content.append(this.htmlEditorControl.getHtml());
        } else {
            this.debugErrorMsg = "No Id or ElementId provided.";
        }
        if (this.debugErrorMsg != null && this.debugErrorMsg.length() > 0) {
            return "<font style=\"color:red\">" + this.debugErrorMsg + "</font>";
        }
        return content.toString();
    }
}


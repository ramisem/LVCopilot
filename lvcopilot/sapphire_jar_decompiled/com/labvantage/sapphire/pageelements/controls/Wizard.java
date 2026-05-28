/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 *  org.apache.xpath.XPathAPI
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;

public class Wizard
extends BaseElement {
    private ServletContext servletContext;
    private HttpServletRequest request;
    private String css = "";
    private String backImg = "WEB-CORE/images/gif/Back.gif";
    private String nextImg = "WEB-CORE/images/gif/Forward.gif";
    private String buttonAppearance = "standard";
    private TranslationProcessor tp = null;

    public Wizard(PageContext pageContext) {
        this.pageContext = pageContext;
        this.servletContext = pageContext.getServletContext();
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        this.tp = new TranslationProcessor(requestContext.getConnectionid());
    }

    public Wizard(ServletContext servletContext, HttpServletRequest request) {
        this.servletContext = servletContext;
        this.request = request;
        this.requestContext = (RequestContext)request.getAttribute("RequestContext");
        String cid = this.requestContext.getConnectionId();
        this.tp = new TranslationProcessor(cid);
    }

    public void setCSS(String css) {
        this.css = css;
    }

    public void setBackImg(String backImg) {
        this.backImg = backImg;
    }

    public void setNextImg(String nextImg) {
        this.nextImg = nextImg;
    }

    public void setButtonAppearance(String buttonAppearance) {
        this.buttonAppearance = buttonAppearance;
    }

    @Override
    public String getHtml() {
        if (this.request == null) {
            this.request = (HttpServletRequest)this.pageContext.getRequest();
        }
        if (this.requestContext == null && this.request != null) {
            this.requestContext = RequestContext.getRequestContext(this.request);
        }
        if (this.requestContext != null) {
            this.requestContext.setProperty("html5", "Y");
        }
        StringBuffer html = new StringBuffer();
        html.append("<!DOCTYPE html>");
        html.append("<html class=\"html5body\">\n");
        html.append("<head>\n");
        html.append(HttpUtil.getMetaTags());
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n");
        if (this.pageContext != null) {
            html.append(JavaScriptAPITag.getJavaScriptAPI(this.pageContext, this.requestContext, this.connectionInfo));
        } else {
            html.append(JavaScriptAPITag.getJavaScriptAPI(this.servletContext, this.request, this.requestContext, this.connectionInfo));
        }
        html.append("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n");
        html.append(HttpUtil.getCoreStyleSheets(false, this.pageContext));
        if (this.css.length() > 0) {
            html.append("\t<link rel=\"stylesheet\" href=\"").append(this.css).append("\" type=\"text/css\">\n");
        }
        String wizardtitle = "Wizard";
        StringBuffer temp = new StringBuffer();
        boolean ok = true;
        String filename = "";
        StringBuffer wizXML = new StringBuffer();
        String wizardstylesheet = "";
        boolean progressbar = false;
        boolean progressbarjumping = false;
        boolean progressbarlaststep = false;
        boolean showtitlebar = false;
        try {
            Element step;
            int i;
            Enumeration e = this.request.getParameterNames();
            while (e.hasMoreElements()) {
                String propertyid = (String)e.nextElement();
                if (propertyid.equals("wizardcompletetarget") || propertyid.equals("wizardcomplete") || propertyid.equals("wizardcanceltarget") || propertyid.equals("wizardcancel") || propertyid.equals("wizard") || propertyid.equals("debug") || propertyid.equals("command") || this.request.getParameter(propertyid) == null) continue;
                temp.append("setProperty( '").append(propertyid).append("', '").append(StringUtil.replaceAll(this.request.getParameter(propertyid), "'", "\\'")).append("');\n");
            }
            try {
                URL url = this.servletContext.getResource("/" + this.request.getParameter("wizard"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    wizXML.append(line);
                }
                reader.close();
            }
            catch (Exception e2) {
                this.logger.stackTrace(e2);
            }
            Document document = DOMUtil.getNewDocument(wizXML.toString());
            Element documentelement = null;
            NodeList steps = null;
            if (document != null) {
                documentelement = document.getDocumentElement();
                steps = XPathAPI.selectNodeList((Node)documentelement, (String)"//step");
                wizardstylesheet = documentelement.getAttribute("stylesheet");
                wizardtitle = documentelement.getAttribute("title");
                showtitlebar = !documentelement.getAttribute("showtitlebar").equalsIgnoreCase("false");
                progressbar = documentelement.getAttribute("progressbar").equalsIgnoreCase("true");
                progressbarjumping = progressbar && documentelement.getAttribute("progressbarjumping").equalsIgnoreCase("true");
                progressbarlaststep = progressbar && documentelement.getAttribute("progressbarlaststep").equalsIgnoreCase("true");
                temp.append("var steps = ").append(steps.getLength()).append(";\n");
                for (i = 0; i < steps.getLength(); ++i) {
                    step = (Element)steps.item(i);
                    temp.append("step[").append(i).append("] = \"").append(step.getAttribute("src")).append("\";\n");
                    temp.append("stepindex[\"").append(step.getAttribute("id")).append("\"] = \"").append(i).append("\";\n");
                    String helpText = step.getAttribute("help");
                    temp.append("help[").append(i).append("] = \"").append(this.tp == null ? helpText : this.tp.translate(helpText)).append("\";\n");
                    temp.append("showcancel[").append(i).append("] = ").append(step.getAttribute("cancel").equalsIgnoreCase("false") ? "false" : "true").append(";\n");
                    NodeList returns = XPathAPI.selectNodeList((Node)step, (String)"return");
                    if (returns != null && returns.getLength() > 0) {
                        for (int j = 0; j < returns.getLength(); ++j) {
                            Element ret = (Element)returns.item(j);
                            temp.append("returns[\"").append(i).append("_").append(ret.getAttribute("value")).append("\"] = \"").append(ret.getAttribute("nextstepid")).append("\"\n");
                        }
                    }
                    if (i >= steps.getLength() - 1) continue;
                    String nextstepid = step.getAttribute("nextstepid");
                    temp.append("nextstep[").append(i).append("] = \"").append(nextstepid.length() > 0 ? nextstepid : ((Element)steps.item(i + 1)).getAttribute("id")).append("\";\n");
                }
            } else {
                ok = false;
            }
            html.append("<title>").append(this.tp.translate(wizardtitle)).append("</title>");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("<div style=\"display:none\">\n");
            html.append("<form id=\"forward\" method=\"post\" target=\"wizdetail\">\n");
            html.append("\t<input type=\"hidden\" name=\"wizardindex\"/>\n");
            html.append("\t<input type=\"hidden\" name=\"_nav\" value=\"N\"/>\n");
            html.append("\t<div id=\"formcontents\"></div>\n");
            html.append("</form>\n");
            html.append("<script>\n");
            html.append("document.body.bottomMargin = 0;\n");
            html.append("document.body.scroll = 'no';\n");
            html.append("var CANCELBUTTON = 1;\n");
            html.append("var BACKBUTTON = 2;\n");
            html.append("var NEXTBUTTON = 3;\n");
            html.append("var FINISHBUTTON = 4;\n");
            html.append("var POST = 0;\n");
            html.append("var GET = 1;\n");
            html.append("var step = new Array();\n");
            html.append("var nextstep = new Array();\n");
            html.append("var map = new Object();\n");
            html.append("var help = new Array();\n");
            html.append("var showcancel = new Array();\n");
            html.append("var stepindex = new Array();\n");
            html.append("var returns = new Array();\n");
            html.append("var propertyid = new Array();\n");
            html.append("var propertyvalue = new Array();\n");
            html.append("var returnMethod = GET;");
            html.append("var propertycount = 0;\n");
            html.append("var wizardcomplete = \"").append(this.request.getParameter("wizardcomplete") != null ? SafeHTML.encodeForJavaScript(this.request.getParameter("wizardcomplete")) : "").append("\";\n");
            html.append("var wizardcancel = \"").append(this.request.getParameter("wizardcancel") != null ? SafeHTML.encodeForJavaScript(this.request.getParameter("wizardcancel")) : "").append("\";\n");
            html.append("var wizardcompletetarget = \"").append(this.request.getParameter("wizardcompletetarget") != null ? SafeHTML.encodeForJavaScript(this.request.getParameter("wizardcompletetarget")) : "").append("\";\n");
            html.append("var wizardcanceltarget = \"").append(this.request.getParameter("wizardcanceltarget") != null ? SafeHTML.encodeForJavaScript(this.request.getParameter("wizardcanceltarget")) : "").append("\";\n");
            html.append(temp);
            html.append("var indexorder = new Array();\n");
            html.append("var currentpoint = 0;\n");
            html.append("var currentindex = 0;\n");
            html.append("var lastindex = -1;\n");
            html.append("var finished = false;\n");
            html.append("function setReturnMethod( method ) {\n");
            html.append("\treturnMethod = method;\n");
            html.append("}\n");
            html.append("function setWizardComplete( wc ) {\n");
            html.append("\twizardcomplete = wc;\n");
            html.append("}\n");
            html.append("function getWizardComplete() {\n");
            html.append("\treturn wizardcomplete;\n");
            html.append("}\n");
            html.append("function setWizardCancel( wc ) {\n");
            html.append("\twizardcancel = wc;\n");
            html.append("}\n");
            html.append("function getWizardCancel() {\n");
            html.append("\treturn wizardcancel;\n");
            html.append("}\n");
            html.append("function setWizardCancelTarget( target ) {\n");
            html.append("\twizardcanceltarget = target;\n");
            html.append("}\n");
            html.append("function getWizardCancelTarget() {\n");
            html.append("\treturn wizardcanceltarget;\n");
            html.append("}\n");
            html.append("function setWizardCompleteTarget( target ) {\n");
            html.append("\twizardcompletetarget = target;\n");
            html.append("}\n");
            html.append("function getWizardCompleteTarget() {\n");
            html.append("\treturn wizardcompletetarget;\n");
            html.append("}\n");
            html.append("function setTitle( title ) {\n");
            if (showtitlebar) {
                html.append("\tdocument.getElementById( \"wizardtitle\" ).innerHTML = title;\n");
            }
            html.append("}\n");
            html.append("function setHelpText( helptext ) {\n");
            if (showtitlebar) {
                html.append("\tdocument.getElementById( \"helptext\" ).innerHTML = helptext;\n");
            }
            html.append("}\n");
            html.append("function setButtonText( button, newtext ) {\n");
            html.append("\tvar buttonid = \"\";\n");
            html.append("\tif ( button == CANCELBUTTON ) buttonid = 'buttoncancel';\n");
            html.append("\tif ( button == BACKBUTTON ) buttonid = 'buttonback';\n");
            html.append("\tif ( button == NEXTBUTTON ) buttonid = 'buttonnext';\n");
            html.append("\tif ( button == FINISHBUTTON ) buttonid = 'buttonfinish';\n");
            html.append("\tvar element = document.getElementById( buttonid );\n");
            html.append("\tif ( element != null ) {\n");
            html.append("\t\telement.text = newtext;\n");
            html.append("\t\telement.refresh='Y'\n");
            html.append("\t}\n");
            html.append("}\n");
            html.append("function enableButton( button ) {\n");
            html.append("\tvar buttonid = \"\";\n");
            html.append("\tif ( button == CANCELBUTTON ) buttonid = 'buttoncancel';\n");
            html.append("\tif ( button == BACKBUTTON ) buttonid = 'buttonback';\n");
            html.append("\tif ( button == NEXTBUTTON ) buttonid = 'buttonnext';\n");
            html.append("\tif ( button == FINISHBUTTON ) buttonid = 'buttonfinish';\n");
            html.append("\tvar element = document.getElementById( buttonid );\n");
            html.append("\tif ( element != null ) {\n");
            html.append("\t\telement.disabled = false;\n");
            html.append("\t}\n");
            html.append("}\n");
            html.append("function disableButton( button ) {\n");
            html.append("\tvar buttonid = \"\";\n");
            html.append("\tif ( button == CANCELBUTTON ) buttonid = 'buttoncancel';\n");
            html.append("\tif ( button == BACKBUTTON ) buttonid = 'buttonback';\n");
            html.append("\tif ( button == NEXTBUTTON ) buttonid = 'buttonnext';\n");
            html.append("\tif ( button == FINISHBUTTON ) buttonid = 'buttonfinish';\n");
            html.append("\tvar element = document.getElementById( buttonid );\n");
            html.append("\tif ( element != null ) {\n");
            html.append("\t\telement.disabled = true;\n");
            html.append("\t}\n");
            html.append("}\n");
            html.append("function cancel() {\n");
            html.append("\tvar returnvalue = \"OK\";\n");
            html.append("\tif ( wizdetail.cancel ) returnvalue = wizdetail.cancel();\n");
            html.append("\tif ( returnvalue != null ) {\n");
            html.append("  \tvar express = (wizardcancel.toLowerCase().indexOf('javascript:') == 0 ? wizardcancel.substring('javascript:'.length) :  (( wizardcanceltarget.length > 0 ? ( wizardcanceltarget + '.' ) : '' ) + 'sapphire.page.navigate( wizardcancel )'));\n");
            html.append("  \teval( express );\n");
            html.append("  }\n");
            html.append("}\n");
            html.append("function finish( jump ) {\n");
            html.append("\tif ( jump == null || jump ) {\n");
            html.append("\t\tvar returnvalue = \"OK\";\n");
            html.append("\t\tif ( wizdetail.finish ) returnvalue = wizdetail.finish();\n");
            html.append("\t\tif ( returnvalue != null ) {\n");
            html.append("\t\t  if ( returnMethod == POST ) {\n");
            html.append("  \t    postReturn( wizardcomplete, wizardcompletetarget );\n");
            html.append("  \t  }");
            html.append("  \t  else {");
            html.append("      \tvar express = (wizardcomplete.toLowerCase().indexOf('javascript:') == 0 ? wizardcomplete.substring('javascript:'.length) :  (( wizardcompletetarget.length > 0 ? ( wizardcompletetarget + '.' ) : '' ) + 'sapphire.page.navigate( wizardcomplete )'));\n");
            html.append("  \t    eval( express );\n");
            html.append("  \t  }");
            html.append("  \t}");
            html.append("  }");
            html.append("  else {");
            html.append("    finished = true;\n");
            html.append("    buttonnext.style.display = 'none';\n");
            html.append("    buttonfinish.style.display = 'block';\n");
            html.append("\t  buttonback.disabled = true;\n");
            html.append("\t  buttoncancel.disabled = true;\n");
            html.append("  }");
            html.append("}\n");
            html.append("function postReturn( url, target ) {\n");
            html.append("  var f = document.getElementById( 'wizardreturnform' );\n");
            html.append("  if ( target != null && target.length > 0 ) {\n");
            html.append("    f.target = target;\n");
            html.append("  }\n");
            html.append("  f.action = url;\n");
            html.append(" sapphire.page.submit(f);");
            html.append("}  \n");
            html.append("function setReturnProperty( id, value ) {\n");
            html.append("  var f = document.getElementById( 'wizardreturnform' );\n");
            html.append("\tvar input = top.sapphire.util.dom.createInput(id);\n");
            html.append("  input.value = value;\n");
            html.append("  input.id = id;\n");
            html.append("  f.appendChild( input );\n");
            html.append("}  \n");
            html.append("function getSRC( index ) {\n");
            html.append("\treturn step[index];\n");
            html.append("}\n");
            html.append("function setProperty( id, value ) {\n");
            html.append("\tvar found = false;\n");
            html.append("\tvar index = map[id];\n");
            html.append("\tif ( index != null ) {\n");
            html.append("\t\tpropertyvalue[index] = value;\n");
            html.append("\t\tfound = true;\n");
            html.append("\t}\n");
            html.append("\tif ( !found ) {\n");
            html.append("\t\tpropertyid[propertycount] = id;\n");
            html.append("\t\tpropertyvalue[propertycount] = value;\n");
            html.append("\t\tmap[id] = propertycount;\n");
            html.append("\t\tpropertycount ++;\n");
            html.append("\t}\n");
            html.append("}\n");
            html.append("function getProperty( id ) {\n");
            html.append("\tvar value = '';\n");
            html.append("\tvar index = map[id];\n");
            html.append("\tif ( index != null ) {\n");
            html.append("\t\tvalue = propertyvalue[index];\n");
            html.append("\t}\n");
            html.append("\treturn value;\n");
            html.append("}\n");
            html.append("function resetProperties() {\n");
            html.append("\tpropertyid.length = 0;\n");
            html.append("\tpropertycount = 0;\n");
            html.append("  map = new Object();\n");
            html.append("}\n");
            html.append("function getNextIndex( currentindex, returnvalue ) {\n");
            html.append("\tvar nextindex = -1;\n");
            html.append("\tif ( nextindex == -1 ) {\n");
            html.append("\t\t// Return values\n");
            html.append("\t\tvar nextstepid = returns[currentindex + \"_\" + returnvalue];\n");
            html.append("\t\tif ( nextstepid != null ) {\n");
            html.append("\t\t\tnextindex = stepindex[nextstepid];\n");
            html.append("\t\t}\n");
            html.append("\t}\n");
            html.append("\tif ( nextindex == -1 ) {\n");
            html.append("\t\t// Next step attribute\n");
            html.append("\t\tvar nextstepid = nextstep[currentindex];\n");
            html.append("\t\tif ( nextstepid != null ) {\n");
            html.append("\t\t\tnextindex = stepindex[nextstepid];\n");
            html.append("\t\t}\n");
            html.append("\t}\n");
            html.append("\tif ( nextindex == -1 ) {\n");
            html.append("\t\t// If all else fails move to the next step\n");
            html.append("\t\tnextindex = currentindex + 1;\n");
            html.append("\t}\n");
            html.append("\treturn nextindex;\n");
            html.append("}\n");
            html.append("function getPropertyForm() {\n");
            html.append("\tvar len= propertyid.length;\n");
            html.append("  var oForm = document.createElement('form');sapphire.page.addCSRFToken( oForm )\n");
            html.append("\tfor ( var i = 0; i < len; i ++ ) {\n");
            html.append("\t\tif ( propertyvalue[i] != null && propertyvalue[i].length > 0 ) {\n");
            html.append("\t\t\tvar oIn;\n");
            html.append("\t\t\toIn = top.sapphire.util.dom.createInput(propertyid[i]);\n");
            html.append("\t\t\toIn.type = 'hidden';\n");
            html.append("\t\t\toIn.value = propertyvalue[i];\n");
            html.append("\t\t\toForm.appendChild(oIn);\n");
            html.append("\t\t}\n");
            html.append("\t}\n");
            html.append("\treturn oForm.innerHTML;\n");
            html.append("}\n");
            html.append("function getPropertyTableRows() {\n");
            html.append("\tvar output = \"\";\n");
            html.append("\tvar len= propertyid.length;\n");
            html.append("\tfor ( var i = 0; i < len; i ++ ) {\n");
            html.append("\t\tif ( propertyvalue[i] != null && propertyvalue[i].length > 0 ) {\n");
            html.append("\t\t\toutput += \"<tr><td>\" + propertyid[i] + \"</td><td>\" + propertyvalue[i] + \"</td></tr>\";\n");
            html.append("\t\t}\n");
            html.append("\t}\n");
            html.append("\treturn output;\n");
            html.append("}\n");
            html.append("function getPropertyList() {\n");
            html.append("\tvar output = \"\";\n");
            html.append("\tvar len= propertyid.length;\n");
            html.append("\tfor ( var i = 0; i < len; i ++ ) {\n");
            html.append("\t\tif ( propertyvalue[i] != null && propertyvalue[i].length > 0 ) {\n");
            html.append("\t\t\toutput += propertyid[i] + \"\t\" + propertyvalue[i] + \"\\n\";\n");
            html.append("\t\t}\n");
            html.append("\t}\n");
            html.append("\treturn output;\n");
            html.append("}\n");
            html.append("function pageLoaded( index ) {\n");
            if (showtitlebar) {
                html.append("\tdocument.getElementById( \"helptext\" ).innerHTML = help[index];\n");
            }
            html.append("\tif ( finished || index == steps - 1 ) {\n");
            html.append("\t\tfinish( false );\n");
            html.append("\t}\n");
            html.append("\telse {\n");
            html.append("\t\tbuttonback.disabled = ( index == 0 );\n");
            html.append("\t\tbuttonnext.disabled = ( index >= steps - 1 );\n");
            html.append("\t\tbuttoncancel.disabled = !showcancel[index];\n");
            html.append("\t\tcurrentindex = index;\n");
            html.append("\t\tfor ( var i = 0; i < currentpoint; i ++ ) {\n");
            html.append("\t\t\tif ( indexorder[i] == index ) {\n");
            html.append("\t\t\t\tcurrentpoint = i;\n");
            html.append("\t\t\t\tbreak;\n");
            html.append("  \t\t}\n");
            html.append("\t\t}\n");
            html.append("\t}\n");
            if (progressbar) {
                html.append("if ( lastindex >= 0 ) {");
                html.append("\tdocument.getElementById( \"step_\" + lastindex ).className = \"wizard_progress_visited\";");
                if (progressbarjumping) {
                    html.append("\tdocument.getElementById( \"step_\" + lastindex ).style.cursor= \"pointer\";");
                    html.append("\tdocument.getElementById( \"step_\" + lastindex ).setAttribute( 'visited', 'true' );");
                }
                html.append("\tfor ( var i = 0; i <= currentindex; i ++ ) { ");
                html.append("\t\tdocument.getElementById( \"step_\" + i ).className = \"wizard_progress_visited\";");
                html.append("\t}");
                html.append("}");
                html.append("lastindex = currentindex;\n");
                html.append("var element = document.getElementById( \"step_\" + currentindex ); ");
                html.append("if ( element != null ) element.className = \"wizard_progress_current\";");
            }
            html.append("}\n");
            if (progressbar) {
                html.append("function resetProgressBar() {\n");
                html.append("\tfor ( var i = 0; i < steps ").append(progressbarlaststep ? "" : "- 1").append("; i ++ ) { ");
                html.append("\t\tdocument.getElementById( \"step_\" + i ).style.cursor= \"auto\";");
                html.append("\t\tdocument.getElementById( \"step_\" + i ).className = \"wizard_progress\";");
                html.append("\t\tdocument.getElementById( \"step_\" + i ).setAttribute( \"visited\", \"false\" );");
                html.append("\t}");
                html.append("}");
            }
            html.append("function skip() {\n");
            html.append("\tdoNext( true );\n");
            html.append("}\n");
            html.append("function next() {\n");
            html.append("\tdoNext( false );\n");
            html.append("}\n");
            html.append("function jump( index ) {\n");
            html.append("  buttonnext.style.display = 'block';\n");
            html.append("  buttonfinish.style.display = 'none';\n");
            html.append("\tcurrentindex = index;\n");
            html.append("\tcurrentpoint ++;\n");
            html.append("\tdisplay( false );\n");
            html.append("}\n");
            html.append("function doNext( skipping ) {\n");
            html.append("\tif ( skipping || !buttonnext.disabled ) {\n");
            html.append("\t\tvar returnvalue = \"OK\";\n");
            html.append("\t\tif ( wizdetail.next ) returnvalue = wizdetail.next();\n");
            html.append("\t\tif ( returnvalue != null ) {\n");
            html.append("\t\t\tcurrentindex = getNextIndex( currentindex, returnvalue );\n");
            html.append("\t\t\tvar skiprequired = ( skipping || returnvalue == \"\" );\n");
            html.append("\t\t\tif ( !skiprequired ) currentpoint ++;\n");
            html.append("\t\t\tdisplay( skiprequired )\n");
            html.append("\t\t}\n");
            html.append("\t\treturn ( returnvalue != null );\n");
            html.append("\t}\n");
            html.append("}\n");
            html.append("function startWizard() {\n");
            html.append("\tdisplay( false );\n");
            html.append("}\n");
            html.append("function display( skip )  {\n");
            html.append("\tif ( !skip ) indexorder[currentpoint] = currentindex;\n");
            html.append("\tbuttonback.disabled = true;\n");
            html.append("\tbuttonnext.disabled = true;\n");
            html.append("\tbuttoncancel.disabled = !showcancel[currentindex];\n");
            html.append("\tvar src = getSRC( currentindex );\n");
            html.append("\tvar oForward = document.getElementById('forward');\nsapphire.page.addCSRFToken( oForward );\n");
            html.append("\toForward.action = src;\n");
            html.append("\toForward.wizardindex.value = currentindex;\n");
            html.append("\tformcontents.innerHTML = getPropertyForm();\n");
            html.append("\toForward.submit();\n");
            html.append("}\n");
            html.append("function back() {\n");
            html.append("\tif ( !buttonback.disabled ) {\n");
            html.append("\t\tvar returnvalue = \"OK\";\n");
            html.append("\t\tif ( wizdetail.back ) returnvalue = wizdetail.back();\n");
            html.append("\t\tif ( returnvalue != null ) {\n");
            html.append("\t\t\tcurrentpoint --;\n");
            html.append("\t\t\tcurrentindex = indexorder[currentpoint];\n");
            html.append("\t\t\tdisplay( false )\n");
            html.append("\t\t}\n");
            html.append("\t}\n");
            html.append("}\n");
            html.append("    function wizFrameLoaded(){\n");
            html.append("        console.log(\"LOADED\");\n");
            html.append("        if ( typeof(top.modernLayout) != 'undefined') {\n");
            html.append("            top.modernLayout.loader.hide();\n");
            html.append("        }\n");
            html.append("        else if ( typeof(hideProcessingDiv) != 'undefined'){\n");
            html.append("            hideProcessingDiv();\n");
            html.append("        }\n");
            html.append("    }\n");
            html.append("</script>\n");
            if (wizardstylesheet.length() > 0) {
                html.append("\t<link rel=\"stylesheet\" href=\"").append(wizardstylesheet).append("\" type=\"text/css\">\n");
            }
            html.append("</div>\n");
            if (ok) {
                html.append("<table height=\"100%\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n");
                if (showtitlebar) {
                    html.append("<tr class=\"wizard_body\" height=\"1%\"><td ").append(progressbar ? " colspan=\"2\" " : "").append(" width=\"*\">\n");
                    html.append("\t<table style=\"padding-left: 3px; padding-top: 3px; padding-right: 3px\">\n");
                    html.append("\t<tr valign=\"bottom\">\n");
                    html.append("\t\t<td valign=\"bottom\" class=\"wizard_title\"><span id=\"wizardtitle\">").append(this.tp == null ? wizardtitle : this.tp.translate(wizardtitle)).append("</span></td>\n");
                    html.append("\t\t<td valign=\"bottom\" class=\"wizard_helptext\">&nbsp;<span style=\"white-space:nowrap\" id=\"helptext\"></span></td>\n");
                    html.append("\t</tr>\n");
                    html.append("\t</table>\n");
                    html.append("</td><tr>\n");
                }
                html.append("<tr valign=\"top\" height=\"*\">");
                if (progressbar) {
                    html.append("<td class=\"wizard_body\" width=\"1%\" valign=\"top\" style=\"border-top:solid gray thin; border-bottom:solid gray thin\" >");
                    html.append("<table cellpadding=\"2\" cellspacing=\"0\">");
                    for (i = 0; i < steps.getLength() - (progressbarlaststep ? 0 : 1); ++i) {
                        step = (Element)steps.item(i);
                        html.append("<tr id=\"step_").append(i).append("\" onclick=\"if ( this.getAttribute( 'visited' )=='true' ) jump( ").append(i).append(");\" class=\"wizard_progress\"><td style=\"font-size: 11pt\" >").append(i + 1).append("</td><td nowrap>&nbsp;").append(step.getAttribute("id")).append("</td></tr>\n");
                    }
                    html.append("</table></td>");
                }
                html.append("<td height=\"100%\" width=\"99%\" valign=\"top\">\n");
                Browser b = new Browser(this.pageContext);
                String blankURL = b.isChromium() ? "about:blank" : "WEB-CORE/blank.html";
                html.append("\t<iframe id=\"wizdetail\" name=\"wizdetail\" marginheight=\"0\" frameborder=\"0\" style=\"border-top:solid gray thin; border-bottom:solid gray thin ").append(progressbar ? "; border-left:solid gray thin" : "").append("\" marginwidth=\"0\" height=\"100%\" width=\"100%\" src='" + blankURL + "' onload=\"wizFrameLoaded()\">Your browser does not support IFrames - Oops</iframe>\n");
                html.append("</td></tr>\n");
                Button cancelButton = new Button(this.pageContext);
                cancelButton.setAppearance(this.buttonAppearance);
                cancelButton.setId("buttoncancel");
                cancelButton.setAction("cancel()");
                cancelButton.setText(this.tp == null ? "Cancel" : this.tp.translate("Cancel"));
                cancelButton.setWidth("70");
                Button backButton = new Button(this.pageContext);
                backButton.setId("buttonback");
                backButton.setAppearance(this.buttonAppearance);
                backButton.setAction("back()");
                backButton.setText(this.tp == null ? "Back" : this.tp.translate("Back"));
                backButton.setImg(this.backImg);
                backButton.setWidth("70");
                Button nextButton = new Button(this.pageContext);
                nextButton.setId("buttonnext");
                nextButton.setAppearance(this.buttonAppearance);
                nextButton.setAction("next()");
                nextButton.setText(this.tp == null ? "Next" : this.tp.translate("Next"));
                nextButton.setImg(this.nextImg);
                nextButton.setWidth("70");
                Button finishButton = new Button(this.pageContext);
                finishButton.setId("buttonfinish");
                finishButton.setAppearance(this.buttonAppearance);
                finishButton.setAction("finish( true )");
                finishButton.setText(this.tp == null ? "Finish" : this.tp.translate("Finish"));
                finishButton.setWidth("70");
                finishButton.setStyle("display: none");
                Button valuesButton = new Button(this.pageContext);
                valuesButton.setId("showvalues");
                valuesButton.setAppearance(this.buttonAppearance);
                valuesButton.setAction("sapphire.alert( getPropertyList() )");
                valuesButton.setText("Show values");
                html.append("<tr class=\"wizard_body\" style=\"height:35px;\"><td ").append(progressbar ? " colspan=\"2\" " : "").append(">\n");
                html.append("\t<table cellpadding=\"0\" cellspacing=\"0\" style=\"padding-top: 4px; padding-left: 3px; padding-right: 3px\" >\n");
                html.append("\t<tr valign=\"top\">\n");
                html.append("\t\t<td style=\"width:10px;\">&nbsp;</td>\n");
                html.append("\t\t<td>").append(cancelButton.getHtml()).append("</td>\n");
                html.append("\t\t<td style=\"width:30px;\">&nbsp;</td>\n");
                html.append("\t\t<td>").append(backButton.getHtml()).append("</td>\n");
                html.append("\t\t<td style=\"width:10px;\">&nbsp;</td>\n");
                html.append("\t\t<td>").append(nextButton.getHtml()).append("</td>\n");
                html.append("\t\t<td>").append(finishButton.getHtml()).append("</td>\n");
                if (this.request.getParameter("debug") != null && this.request.getParameter("debug").equals("Y")) {
                    html.append("\t<td width=\"100\">&nbsp;</td>\n");
                    html.append("\t<td>").append(valuesButton.getHtml()).append("</td>\n");
                }
                html.append("\t</tr>\n");
                html.append("\t</table>\n");
                html.append("</td></tr>\n");
                html.append("</table>\n");
                html.append("<form style=\"display:none\" id=\"wizardreturnform\" name=\"wizardreturnform\" method=\"post\"></form>");
                html.append("<script>\n");
                html.append("wizdetail.focus();\n");
                html.append("startWizard();\n");
                html.append("</script>\n");
            } else {
                html.append("<h2>Unable to start wizard ").append(this.request.getParameter("wizardid")).append(" because the wizard driver could not be found in ").append(filename).append("</h2>");
            }
            html.append("</body>\n");
            html.append("</html>\n");
        }
        catch (SapphireException se) {
            Trace.log("WIZARD", "ERROR: Exception thrown to root" + se + " " + se.getMessage());
        }
        catch (TransformerException e) {
            Trace.log("WIZARD", "ERROR: Exception thrown to root" + e + " " + e.getMessage());
        }
        return html.toString();
    }
}


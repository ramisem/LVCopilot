/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.pageelements.lookup;

import com.labvantage.sapphire.util.http.HttpUtil;
import javax.servlet.http.HttpServletRequest;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class AWSLookUp
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    protected String logName = this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1).toUpperCase();
    protected Logger logger = new Logger(new LogContext(this.logName, "(none)"));
    public static final String ACCESSKEYPROP = "accessKey";
    public static final String SECRETACCESSKEYPROP = "secretAccessKey";
    public static final String BUCKETNAMEPROP = "bucketName";
    public static final String REGIONPROP = "region";
    public static final String VERSIONINGPROP = "isVersioningEnabled";
    public static final String PATHPROP = "folderPath";
    public static final String MOVEUPDISABLEDIMAGE = "WEB-CORE/utils/lookup/images/moveup-disabled.gif";
    public static final String MOVEUPIMAGE = "WEB-CORE/utils/lookup/images/moveup.gif";
    private StringBuffer theHTMLBuffer;
    private String accessKey = "accessKey";
    private String secretAccessKey = "secretAccessKey";
    private String bucketName = "bucketName";
    private String region = "region";
    private String folderPath = "folderPath";
    private boolean isVersioningEnabled = false;
    private String[] descsArray = new String[]{"All Files"};
    private String[] extensionsArray = new String[]{"*.*"};
    private String activeExt = "*.*";

    public AWSLookUp() {
        this.theHTMLBuffer = new StringBuffer("");
        this.logger.debug("Class created...");
    }

    @Override
    public String getHtml() {
        String theReturn = "";
        this.logger.debug("getHTMML called...");
        if (this.loadProperties()) {
            this.renderHTML(this.theHTMLBuffer);
        }
        theReturn = this.theHTMLBuffer.toString();
        return theReturn;
    }

    private void renderHTML(StringBuffer theBuffer) {
        try {
            PropertyList userconfig = null;
            RequestContext requestContext = RequestContext.getInstance((HttpServletRequest)this.pageContext.getRequest());
            Object o = ((HttpServletRequest)this.pageContext.getRequest()).getSession().getAttribute("userconfig");
            userconfig = o != null ? (PropertyList)o : requestContext.getPropertyList("userconfig");
            String selectedPath = (String)userconfig.get("browseaws");
            TranslationProcessor tp = this.getTranslationProcessor();
            this.renderHeader(theBuffer);
            theBuffer.append("<table id=oRootTable border=0 style=\"width:100%;height:100%;\" class=\"roottable\" border=0 cellpadding=0 cellspacing=0> ");
            theBuffer.append("<tr height=30> ");
            theBuffer.append("<td width=80 align=\"right\" style=\"padding-bottom:10px;\"> ");
            theBuffer.append(tp.translate("Look in")).append(":");
            theBuffer.append("</td> ");
            theBuffer.append(" <td width=\"10\">&nbsp;</td> ");
            theBuffer.append("<td style='padding-bottom:10px;'> ");
            theBuffer.append("<select id=oFolderSelect style=\"width:80%;\" disabled>");
            theBuffer.append("<option selected value=\"").append(selectedPath != null && selectedPath.length() > 0 && selectedPath.trim().startsWith(this.folderPath.trim()) ? selectedPath.trim() : (this.folderPath.trim().length() > 0 ? this.folderPath.trim() : "/")).append("\">").append(selectedPath != null && selectedPath.length() > 0 && selectedPath.trim().startsWith(this.folderPath.trim()) ? selectedPath.trim() : (this.folderPath.trim().length() > 0 ? this.folderPath.trim() : "/")).append("</option>");
            theBuffer.append("</select>");
            theBuffer.append(" &nbsp; ");
            theBuffer.append("<img width=16 height=16 src=\"").append(MOVEUPIMAGE).append("\" title=\"Up one level\" style=\"cursor: pointer;\" id= \"moveup\" onclick=\"aws.moveUp()\">&nbsp;");
            theBuffer.append("<img width=16 height=16 src=\"").append(MOVEUPDISABLEDIMAGE).append("\" title=\"Up one level\" style=\"cursor: pointer;\" id= \"moveup_disabled\" >&nbsp;");
            theBuffer.append("</td>");
            theBuffer.append("</tr>");
            theBuffer.append("<tr>");
            theBuffer.append("<td colspan=3>");
            theBuffer.append(" <table border=0 cellpadding=0 cellspacing=0 class=\"insidetable\" width=\"100%\" height=\"100%\">");
            theBuffer.append("<tr height=\"100%\">");
            theBuffer.append("<td colspan=\"6\" style=\"overflow:hidden;padding-right:2px;\">");
            theBuffer.append("<div id=\"oFileFrameLoading\" style=\"background-color:white;position:relative;top:100px;left:190px;\">Loading...<img src=\"WEB-CORE/utils/lookup/images/loading.gif\"></div>");
            theBuffer.append("<iframe scrolling=\"no\" class=\"frame\" frameborder=0 src=\"").append(this.browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\" id=oFileFrame name=oFileFrame></iframe>");
            theBuffer.append("</td>");
            theBuffer.append("</tr>");
            theBuffer.append("<tr height=30 style=\"padding-top:10px;\">");
            theBuffer.append("<td width=\"25%\"><span class=\"pad\">" + tp.translate("File name:") + "</span></td>");
            theBuffer.append("<td width=\"65%\" valign=middle>");
            theBuffer.append("<input type=text id=oFileName name=oFileName style='width:80%' value=\"").append("\">");
            if (this.isVersioningEnabled) {
                theBuffer.append("<input type=hidden id=versionid name=versionid style='width:80%' value=\"").append("\">");
            }
            theBuffer.append("<input type=hidden id=accesskey name=accesskey value=\"").append(this.accessKey).append("\">");
            theBuffer.append("<input type=hidden id=secretaccesskey name=secretaccesskey value=\"").append(this.secretAccessKey).append("\">");
            theBuffer.append("<input type=hidden id=bucketname name=bucketname value=\"").append(this.bucketName).append("\">");
            theBuffer.append("<input type=hidden id=region name=bucketname value=\"").append(this.region).append("\">");
            theBuffer.append("<input type=hidden id=folderPath name=folderPath value=\"").append(this.folderPath).append("\">");
            theBuffer.append("</td>");
            theBuffer.append("<td width=\"25%\">&nbsp;</td>");
            theBuffer.append("<td width=\"65%\">&nbsp;</td>");
            theBuffer.append("<td width=\"10%\" align=right><input style=\"width:80px;").append("\" type=button id=oReturnButton name=oReturnButton value=\"").append(tp.translate("Open")).append("\" onclick=\"aws.doOK();\"></td>");
            theBuffer.append("</tr>");
            theBuffer.append("<tr height=30 >");
            theBuffer.append("<td width=\"25%\"><span class=\"pad\">" + tp.translate("Files of type:") + "</span></td>");
            theBuffer.append("<td width=\"65%\" valign=middle><select id=oFileExt name=oFileExt style=\"width:80%;\">");
            this.logger.debug("About to render extensions...");
            for (int index = 0; index < this.extensionsArray.length; ++index) {
                if (this.extensionsArray[index].equalsIgnoreCase(this.activeExt)) {
                    theBuffer.append("<option selected value=\"").append(this.extensionsArray[index]).append("\">").append(this.descsArray[index]).append("</option>");
                    continue;
                }
                theBuffer.append("<option value=\"").append(this.extensionsArray[index]).append("\">").append(this.descsArray[index]).append("</option>");
            }
            this.logger.debug("Extensions rendered.");
            theBuffer.append("</select></td>");
            theBuffer.append("<td width=\"25%\">&nbsp;</td>");
            theBuffer.append("<td width=\"65%\">&nbsp;</td>");
            theBuffer.append("<td width=\"10%\" align=\"right\"><input style=\"width:80px;\" type=button id=oCancelButton name=oCancelButton value=\"" + tp.translate("Cancel") + "\" onclick=\"aws.doCancel();\"></td> ");
            theBuffer.append("</tr>");
            theBuffer.append("</table>   ");
            theBuffer.append("</td>");
            theBuffer.append("</tr>");
            theBuffer.append("</table>");
            theBuffer.append("<iframe frameborder=0 src='WEB-CORE/blank.html' width=1 height=1 id=oValidateFrame name=oValidateFrame style='display:none;'></iframe>");
            this.renderForms(theBuffer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderHeader(StringBuffer theBuffer) {
        theBuffer.append("<link href=\"" + HttpUtil.getCSS("WEB-CORE/utils/lookup/stylesheets/filesystem.css", this.pageContext) + "\" rel=\"stylesheet\" type=\"text/css\">\n");
        theBuffer.append("<script type=\"text/javascript\" src=\"WEB-CORE/utils/lookup/scripts/awss3.js\"></script>\n");
        theBuffer.append("<script type=\"text/javascript\">\n");
        theBuffer.append("document.body.style.overflow = 'hidden';\n");
        theBuffer.append("sapphire.events.attachEvent( window, 'onload', aws.doOnLoad );\n");
        theBuffer.append("</script>\n");
    }

    private void renderForms(StringBuffer theBuffer) {
        this.logger.debug("renderForms called...");
        PropertyList userconfig = null;
        RequestContext requestContext = RequestContext.getInstance((HttpServletRequest)this.pageContext.getRequest());
        Object o = ((HttpServletRequest)this.pageContext.getRequest()).getSession().getAttribute("userconfig");
        userconfig = o != null ? (PropertyList)o : requestContext.getPropertyList("userconfig");
        String selectedPath = (String)userconfig.get("browseaws");
        theBuffer.append("<form id=fileform name=fileform method=post action='rc?command=file&file=WEB-CORE/utils/lookup/awsfileview.jsp' target='oFileFrame' style='display:none;'>");
        theBuffer.append("<input type=hidden name='accessKey' value='").append(this.accessKey).append("'>");
        theBuffer.append("<input type=hidden name='secretAccessKey' value='").append(this.secretAccessKey).append("'>");
        theBuffer.append("<input type=hidden name='bucketName' value='").append(this.bucketName).append("'>");
        theBuffer.append("<input type=hidden name='region' value='").append(this.region).append("'>");
        theBuffer.append("<input type=hidden name='isVersioningEnabled' value='").append(this.isVersioningEnabled).append("'>");
        theBuffer.append("<input type=text name='folderPath' value='").append(this.folderPath).append("'>");
        theBuffer.append("<input type=text name='selectedPath' value='").append(selectedPath != null && selectedPath.length() > 0 && selectedPath.trim().startsWith(this.folderPath.trim()) ? selectedPath.trim() : this.folderPath).append("'>");
        theBuffer.append("<input type=hidden id=sortorder name=sortorder value=\"\">");
        theBuffer.append("<input type=hidden id=sortcolumn name=sortcolumn value=\"\">");
        theBuffer.append("</form>");
    }

    private boolean loadProperties() {
        if (this.pageContext != null && this.requestContext != null && this.element != null) {
            this.accessKey = this.element.getProperty(ACCESSKEYPROP);
            this.secretAccessKey = this.element.getProperty(SECRETACCESSKEYPROP);
            this.bucketName = this.element.getProperty(BUCKETNAMEPROP);
            this.region = this.element.getProperty(REGIONPROP);
            this.isVersioningEnabled = Boolean.valueOf(this.element.getProperty(VERSIONINGPROP));
            this.folderPath = this.element.getProperty(PATHPROP);
        }
        return true;
    }
}


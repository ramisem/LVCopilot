/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.pageelements.lookup;

import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import javax.servlet.http.HttpServletRequest;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class AzureLookUp
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    protected String logName = this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1).toUpperCase();
    protected Logger logger = new Logger(new LogContext(this.logName, "(none)"));
    public static final String ACCOUNTNAMEPROP = "accountName";
    public static final String ACCOUNTKEYPROP = "accountKey";
    public static final String CONTAINERPROP = "container";
    public static final String PATHPROP = "folderPath";
    public static final String VERSIONINGPROP = "isVersioningEnabled";
    public static final String MOVEUPDISABLEDIMAGE = "WEB-CORE/utils/lookup/images/moveup-disabled.gif";
    public static final String MOVEUPIMAGE = "WEB-CORE/utils/lookup/images/moveup.gif";
    private StringBuffer theHTMLBuffer;
    private String accountName = "accountName";
    private String accountKey = "accountKey";
    private String container = "container";
    private String folderPath = "folderPath";
    private boolean isVersioningEnabled = false;
    private String[] descsArray = new String[]{"All Files"};
    private String[] extensionsArray = new String[]{"*.*"};
    private String activeExt = "*.*";
    private int sortCol;
    private String sortDir;

    public AzureLookUp() {
        this.theHTMLBuffer = new StringBuffer("");
        this.logger.debug("Class created...");
    }

    @Override
    public String getHtml() {
        this.logger.debug("getHTML called...");
        String theReturn = "";
        if (this.pageContext != null) {
            if (AzureLookUp.isServerSideBrowsingPermitted(this.connectionInfo.getConnectionId())) {
                if (this.loadProperties()) {
                    this.renderHTML(this.theHTMLBuffer, this.sortCol, this.sortDir);
                    this.logger.debug("HTML rendered.");
                } else {
                    this.logger.error("Could not load required properties.");
                }
            }
        } else {
            this.logger.error("PageContext has not been set.");
        }
        if (this.debugErrorMsg == null || this.debugErrorMsg.length() == 0) {
            if (this.theHTMLBuffer.length() > 0) {
                theReturn = this.theHTMLBuffer.toString();
            }
        } else {
            theReturn = this.getError();
        }
        return theReturn;
    }

    private void renderHTML(StringBuffer theBuffer, int sortCol, String sortDir) {
        try {
            PropertyList userconfig = null;
            RequestContext requestContext = RequestContext.getInstance((HttpServletRequest)this.pageContext.getRequest());
            Object o = ((HttpServletRequest)this.pageContext.getRequest()).getSession().getAttribute("userconfig");
            userconfig = o != null ? (PropertyList)o : requestContext.getPropertyList("userconfig");
            String selectedPath = (String)userconfig.get("browseazure");
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
            theBuffer.append("<img width=16 height=16 src=\"").append(MOVEUPIMAGE).append("\" title=\"Up one level\" style=\"cursor: pointer;\" id= \"moveup\" onclick=\"azure.moveUp()\">&nbsp;");
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
            theBuffer.append("<input type=hidden id=accountName name=accountName value=\"").append(this.accountName).append("\">");
            theBuffer.append("<input type=hidden id=accountKey name=accountKey value=\"").append(this.accountKey).append("\">");
            theBuffer.append("<input type=hidden id=container name=container value=\"").append(this.container).append("\">");
            theBuffer.append("<input type=hidden id=folderPath name=folderPath value=\"").append(this.folderPath).append("\">");
            theBuffer.append("</td>");
            theBuffer.append("<td width=\"25%\">&nbsp;</td>");
            theBuffer.append("<td width=\"65%\">&nbsp;</td>");
            theBuffer.append("<td width=\"10%\" align=right><input style=\"width:80px;").append("\" type=button id=oReturnButton name=oReturnButton value=\"").append(tp.translate("Open")).append("\" onclick=\"azure.doOK();\"></td>");
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
            theBuffer.append("<td width=\"10%\" align=\"right\"><input style=\"width:80px;\" type=button id=oCancelButton name=oCancelButton value=\"" + tp.translate("Cancel") + "\" onclick=\"azure.doCancel();\"></td> ");
            theBuffer.append("</tr>");
            theBuffer.append("</table> ");
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
        theBuffer.append("<script type=\"text/javascript\" src=\"WEB-CORE/utils/lookup/scripts/azureblobstorage.js\"></script>\n");
        theBuffer.append("<script type=\"text/javascript\">\n");
        theBuffer.append("document.body.style.overflow = 'hidden';\n");
        theBuffer.append("sapphire.events.attachEvent( window, 'onload', azure.doOnLoad );\n");
        theBuffer.append("</script>\n");
    }

    private void renderForms(StringBuffer theBuffer) {
        this.logger.debug("renderForms called...");
        PropertyList userconfig = null;
        RequestContext requestContext = RequestContext.getInstance((HttpServletRequest)this.pageContext.getRequest());
        Object o = ((HttpServletRequest)this.pageContext.getRequest()).getSession().getAttribute("userconfig");
        userconfig = o != null ? (PropertyList)o : requestContext.getPropertyList("userconfig");
        String selectedPath = (String)userconfig.get("browseazure");
        theBuffer.append("<form id=fileform name=fileform method=post action='rc?command=file&file=WEB-CORE/utils/lookup/azurefileview.jsp' target='oFileFrame' style='display:none;'>");
        theBuffer.append("<input type=text name='accountName' value='").append(this.accountName).append("'>");
        theBuffer.append("<input type=text name='accountKey' value='").append(this.accountKey).append("'>");
        theBuffer.append("<input type=text name='container' value='").append(this.container).append("'>");
        theBuffer.append("<input type=text name='folderPath' value='").append(this.folderPath).append("'>");
        theBuffer.append("<input type=text name='selectedPath' value='").append(selectedPath != null && selectedPath.length() > 0 && selectedPath.trim().startsWith(this.folderPath.trim()) ? selectedPath.trim() : this.folderPath).append("'>");
        theBuffer.append("<input type=text name='isVersioningEnabled' value='").append(this.isVersioningEnabled).append("'>");
        theBuffer.append("<input type=hidden id=sortorder name=sortorder value=\"\">");
        theBuffer.append("<input type=hidden id=sortcolumn name=sortcolumn value=\"\">");
        theBuffer.append("</form>");
    }

    private boolean loadProperties() {
        if (this.pageContext != null && this.requestContext != null && this.element != null) {
            this.accountName = this.element.getProperty(ACCOUNTNAMEPROP);
            this.accountKey = this.element.getProperty(ACCOUNTKEYPROP);
            this.container = this.element.getProperty(CONTAINERPROP);
            this.folderPath = this.element.getProperty(PATHPROP);
            this.isVersioningEnabled = Boolean.valueOf(this.element.getProperty(VERSIONINGPROP));
        }
        return true;
    }

    public static boolean isServerSideBrowsingPermitted(String connectionId) {
        QueryProcessor qp = new QueryProcessor(connectionId);
        DataSet ds = qp.getSqlDataSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'allowserversidebrowsing'");
        if (ds != null && ds.getRowCount() > 0 && ds.getValue(0, "propertyvalue", "Y").equalsIgnoreCase("N")) {
            return false;
        }
        return SecurityPolicyUtil.isServerSideBrowsingPermitted(connectionId);
    }
}


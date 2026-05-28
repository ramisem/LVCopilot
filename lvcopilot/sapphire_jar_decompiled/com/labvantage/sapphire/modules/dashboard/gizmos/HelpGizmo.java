/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.ajax.operations.HelpGizmoOperations;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.modules.issuemanagement.IssueManagementUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class HelpGizmo
extends MenuGizmo {
    @Override
    public boolean init() {
        super.init();
        return true;
    }

    @Override
    protected void setUpProperties() {
        this.imageTitle = this.getGizmoDefId().length() > 0 ? this.getGizmoDefId() : "Help";
        this.setTitle(this.imageTitle);
        if (this.element == null) {
            this.element = new PropertyList();
        }
        this.element.setProperty("image", this.element.getProperty("image", this.getDefaultImageSrc()));
        PropertyListCollection menu = new PropertyListCollection();
        this.element.setProperty("menu", menu);
        this.element.setProperty("dynamicmenuajaxclass", HelpGizmoOperations.class.getName());
        this.element.setProperty("dynamicmenuajaxprops", "{operation: 'gethelppages', gizmoid: '" + this.getGizmoDefId() + "'}");
    }

    @Override
    public String getDefaultImageSrc() {
        return "FlatWhiteQuestionHelp1";
    }

    @Override
    public String getIconHtml() {
        StringBuilder html = new StringBuilder();
        html.append(super.getIconHtml());
        html.append(IssueManagementUtil.getIssueManagerJSHTML());
        html.append("<script language='JavaScript' src='WEB-CORE/modules/dashboard/scripts/clickhelphandler.js'></script>");
        return html.toString();
    }

    @Override
    public int evalCount() {
        SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
        String sysUserId = sc.getSysuserId();
        DataSet openIssuesDS = this.getQueryProcessor().getPreparedSqlDataSet(20050, new Object[0]);
        if (Arrays.asList(StringUtil.split(sc.getRoleList(), ";")).contains("IssueSubmitter")) {
            if (openIssuesDS.getRowCount() > 0) {
                return openIssuesDS.getRowCount();
            }
            return -1;
        }
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("createby", sysUserId);
        DataSet userIssues = openIssuesDS.getFilteredDataSet(filterMap);
        if (userIssues.getRowCount() > 0) {
            return userIssues.getRowCount();
        }
        return -1;
    }

    public String getHelpRootUrl() {
        String helpurlroot;
        URL rootUrl = null;
        try {
            if (this.pageContext != null) {
                rootUrl = new URL(((HttpServletRequest)this.pageContext.getRequest()).getRequestURL().toString());
                rootUrl = new URL(rootUrl, "../");
            }
        }
        catch (Exception e) {
            rootUrl = null;
            this.logger.warn("Failed to get URL: " + e.getMessage());
        }
        if (rootUrl == null) {
            try {
                Configuration configuration = Configuration.getInstance();
                int port = 0;
                try {
                    port = configuration.getHttpPort().length() > 0 ? Integer.parseInt(configuration.getHttpPort()) : 0;
                }
                catch (Exception exception) {
                    // empty catch block
                }
                rootUrl = new URL("http", configuration.getServerHostName(), port, "");
            }
            catch (SapphireException e) {
                this.logger.error("Failed to get configuration instance: Reason: " + e.getMessage(), e);
            }
            catch (Exception e) {
                this.logger.error("Failed to get URL: " + e.getMessage(), e);
            }
        }
        if ((helpurlroot = this.getElementProperties().getProperty("helpurlroot", "")).length() == 0) {
            if (rootUrl != null) {
                try {
                    return new URL(rootUrl, "labvantagedoc").toString();
                }
                catch (MalformedURLException e) {
                    this.logger.error("Malformed Default URL: " + e.getMessage());
                    return "";
                }
            }
            this.logger.error("No root URL could be found");
            return "";
        }
        if (helpurlroot.toLowerCase().startsWith("http:") || helpurlroot.toLowerCase().startsWith("https:")) {
            try {
                URL out = new URL(helpurlroot);
                return out.toString();
            }
            catch (MalformedURLException e) {
                this.logger.error("Malformed URL: " + e.getMessage());
                return "";
            }
        }
        if (rootUrl != null) {
            try {
                URL out = new URL(rootUrl, helpurlroot);
                return out.toString();
            }
            catch (MalformedURLException e) {
                this.logger.error("Malformed URL: " + e.getMessage());
                return "";
            }
        }
        return "";
    }
}


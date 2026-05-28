/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.HelpGizmo;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class HelpGizmoOperations
extends BaseAjaxRequest {
    protected PageContext pageContext;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
            SapphireConnection sapphireConnection = connectionProcessor.getSapphireConnection();
            TranslationProcessor tp = this.getTranslationProcessor();
            String operation = ajaxResponse.getRequestParameter("operation");
            String menuid = ajaxResponse.getRequestParameter("menuid");
            if (operation.equals("gethelppages")) {
                PropertyList newlink;
                String url;
                String applicationurl;
                String webapp;
                String helpurlroot;
                PropertyListCollection menu = new PropertyListCollection();
                PropertyListCollection helpMenu = new PropertyListCollection();
                PropertyListCollection issueMenu = new PropertyListCollection();
                PropertyListCollection tutorialMenu = new PropertyListCollection();
                String gizmoid = ajaxResponse.getRequestParameter("gizmoid");
                sapphire.pageelements.BaseGizmo gizmo = BaseGizmo.getInstance(this.getConnectionId(), gizmoid, true);
                HelpGizmo helpGizmo = (HelpGizmo)gizmo;
                String menuMode = helpGizmo.getElementProperties().getProperty("menumode", "HI");
                PropertyList tutorialvideo = gizmo.getElementProperties().getPropertyList("tutorialvideos");
                if ("HI".equals(menuMode) || "IO".equals(menuMode)) {
                    String issueLoggerRole = helpGizmo.getElementProperties().getProperty("issueloggerrole");
                    String roleListStr = sapphireConnection.getRoleList();
                    List<String> userRoleList = Arrays.asList(StringUtil.split(roleListStr, ";"));
                    if (issueLoggerRole.length() == 0 || userRoleList.contains(issueLoggerRole)) {
                        PropertyList newlink2 = new PropertyList();
                        newlink2.setProperty("id", menuid + "_logIssue");
                        newlink2.setProperty("text", tp.translate("Log an Issue"));
                        newlink2.setProperty("tip", tp.translate("Log an Issue"));
                        newlink2.setProperty("link", "#");
                        newlink2.setProperty("onclick", "javascript:issueGizmoObj.gizmoPrompt('createIssue')");
                        newlink2.setProperty("releaselocks", "N");
                        issueMenu.add(newlink2);
                        newlink2 = new PropertyList();
                        newlink2.setProperty("id", menuid + "_editRecent");
                        newlink2.setProperty("text", tp.translate("Edit Recent Issue"));
                        newlink2.setProperty("tip", tp.translate("Edit Recent Issue"));
                        newlink2.setProperty("link", "#");
                        newlink2.setProperty("onclick", "javascript:issueGizmoObj.gizmoPrompt('continueIssue')");
                        newlink2.setProperty("releaselocks", "N");
                        issueMenu.add(newlink2);
                        newlink2 = new PropertyList();
                        newlink2.setProperty("id", menuid + "_showIssues");
                        newlink2.setProperty("text", tp.translate("My Issues"));
                        newlink2.setProperty("tip", tp.translate("My Issues"));
                        newlink2.setProperty("link", "#");
                        newlink2.setProperty("onclick", "javascript:issueGizmoObj.gizmoPrompt('showIssue')");
                        newlink2.setProperty("releaselocks", "N");
                        issueMenu.add(newlink2);
                    }
                }
                String webpageid = ajaxResponse.getRequestParameter("webpageid");
                String productedition = ajaxResponse.getRequestParameter("productedition");
                DataSet helppages = this.getQueryProcessor().getPreparedSqlDataSet("SELECT helppage.title, helppage.longdesc, helppage.url, helppage.video FROM helppage, webpagehelppage WHERE helppage.helppageid = webpagehelppage.helppageid AND webpagehelppage.webpageid = ? AND webpagehelppage.productedition = ? ORDER BY CASE typeflag WHEN 'P' THEN 1 WHEN 'C' THEN 2 WHEN 'R' THEN 3 END", new Object[]{webpageid, productedition});
                if ("HI".equals(menuMode) || "HO".equals(menuMode)) {
                    helpurlroot = helpGizmo.getHelpRootUrl();
                    webapp = request.getContextPath();
                    if (webapp.startsWith("/")) {
                        webapp = webapp.substring(1);
                    }
                    applicationurl = (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + webapp + "/";
                    boolean alwaysShowHelp = "N".equals(helpGizmo.getElementProperties().getProperty("hidehelpifnotfound", "N"));
                    if (alwaysShowHelp || this.isHelpURLReachable(helpurlroot, request)) {
                        PropertyList restservices;
                        PropertyList policy;
                        int i;
                        PropertyListCollection additionalHelpPages = gizmo.getElementProperties().getCollection("helppages");
                        if (additionalHelpPages != null) {
                            for (i = 0; i < additionalHelpPages.size(); ++i) {
                                PropertyList helppage = additionalHelpPages.getPropertyList(i);
                                if (!helppage.getProperty("show", "Y").equals("Y")) continue;
                                PropertyList newlink3 = new PropertyList();
                                newlink3.setProperty("id", menuid + "_a_" + i);
                                newlink3.setProperty("text", "<img src='WEB-CORE/imageref/flat/16/flat_black_book_hardcover_open_writing.svg'> " + helppage.getProperty("title"));
                                newlink3.setProperty("tip", helppage.getProperty("tip"));
                                url = helppage.getProperty("url");
                                if (helppage.getProperty("prependhelproot", "Y").equals("Y")) {
                                    url = helpurlroot + (!url.startsWith("/") ? "/" + url : url);
                                }
                                newlink3.setProperty("link", "#");
                                newlink3.setProperty("onclick", "window.open( '" + url + "', 'help', '' );return false;");
                                newlink3.setProperty("releaselocks", "N");
                                helpMenu.add(newlink3);
                            }
                        }
                        for (i = 0; i < helppages.size(); ++i) {
                            if (!"".equals(helppages.getValue(i, "video", ""))) continue;
                            newlink = new PropertyList();
                            newlink.setProperty("id", menuid + i);
                            newlink.setProperty("text", "<img src='WEB-CORE/imageref/flat/16/flat_black_book_hardcover_open_writing.svg'> " + helppages.getValue(i, "title"));
                            newlink.setProperty("tip", helppages.getValue(i, "longdesc"));
                            String url2 = helppages.getValue(i, "url");
                            newlink.setProperty("link", "#");
                            newlink.setProperty("onclick", "window.open( '" + helpurlroot + (!url2.startsWith("/") ? "/" + url2 : url2) + "', 'help', 'width=1000,height=800,directories=no,titlebar=no,toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no' );return false;");
                            newlink.setProperty("releaselocks", "N");
                            helpMenu.add(newlink);
                        }
                        String restAPIRole = gizmo.getElementProperties().getProperty("restapirole");
                        if (sapphireConnection.hasRole(restAPIRole) && (policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(sapphireConnection.getConnectionId()), SecurityService.isVirtualUser(sapphireConnection.getConnectionId()), SecurityService.isPortalUser(sapphireConnection.getConnectionId()))) != null && (restservices = policy.getPropertyList("restservices")).getProperty("enable", "N").equals("Y")) {
                            PropertyList newlink4 = new PropertyList();
                            newlink4.setProperty("id", menuid + "_a_rest");
                            newlink4.setProperty("text", "REST API");
                            newlink4.setProperty("tip", "REST API");
                            newlink4.setProperty("link", "#");
                            newlink4.setProperty("onclick", "window.open( '" + request.getContextPath() + "/rest/api', 'rest', '' );return false;");
                            newlink4.setProperty("releaselocks", "N");
                            helpMenu.add(newlink4);
                        }
                        if (helpMenu.size() == 0) {
                            newlink = new PropertyList();
                            newlink.setProperty("id", menuid + "_help-undefined");
                            newlink.setProperty("text", "<font color=red>" + tp.translate("No Help found.") + "</font>");
                            newlink.setProperty("tip", tp.translate("No Help have been provided for this context. Please contact Administrator"));
                            newlink.setProperty("link", "#");
                            newlink.setProperty("releaselocks", "N");
                            helpMenu.add(newlink);
                        }
                    }
                }
                helpurlroot = helpGizmo.getHelpRootUrl();
                webapp = request.getContextPath();
                if (webapp.startsWith("/")) {
                    webapp = webapp.substring(1);
                }
                if (tutorialvideo.getProperty("show", "Y").equals("Y")) {
                    PropertyList newlink5 = new PropertyList();
                    newlink5.setProperty("id", menuid + "_a_tutorial");
                    newlink5.setProperty("text", "<img src='WEB-CORE/imageref/flat/16/flat_black_arrow6_right_play.svg'>  " + tutorialvideo.getProperty("title"));
                    newlink5.setProperty("tip", tutorialvideo.getProperty("tip"));
                    String url3 = tutorialvideo.getProperty("url");
                    if (url3 == null || url3.equals("")) {
                        String pageDefault = "rc?command=page&page=LV_HelpPagePopup";
                        String urlDefault = (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + webapp + "/";
                        url3 = urlDefault + pageDefault;
                    }
                    newlink5.setProperty("link", "#");
                    newlink5.setProperty("onclick", "sapphire.ui.dialog.open( '','" + url3 + "',true,1024,640);return false;");
                    newlink5.setProperty("releaselocks", "N");
                    tutorialMenu.add(newlink5);
                }
                applicationurl = (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + webapp + "/";
                for (int i = 0; i < helppages.size(); ++i) {
                    if ("".equals(helppages.getValue(i, "video", ""))) continue;
                    String vidId = helppages.getValue(i, "video", "");
                    String titleReplace = helppages.getValue(i, "title", "");
                    newlink = new PropertyList();
                    newlink.setProperty("id", menuid + i);
                    newlink.setProperty("text", "<img src='WEB-CORE/imageref/flat/16/flat_black_arrow6_right_play.svg'>  " + helppages.getValue(i, "title"));
                    newlink.setProperty("tip", helppages.getValue(i, "longdesc"));
                    String baseUrl = "";
                    baseUrl = tutorialvideo.getProperty("basevideourl");
                    baseUrl = baseUrl + helppages.getValue(i, "url");
                    newlink.setProperty("link", "#");
                    url = baseUrl.replace("[videoid]", vidId);
                    url = url.replace("[title]", titleReplace);
                    newlink.setProperty("onclick", "window.open( '" + url + "', 'help', 'noopener,width=1000,height=800,directories=no,titlebar=no,toolbar=no,location=no,status=no,menubar=no,scrollbars=no,resizable=no' );return false;");
                    newlink.setProperty("releaselocks", "N");
                    tutorialMenu.add(newlink);
                }
                if (tutorialMenu.size() == 0) {
                    PropertyList newlink6 = new PropertyList();
                    newlink6.setProperty("id", menuid + "_help-undefined");
                    newlink6.setProperty("text", "<font color=red>" + tp.translate("No Tutorial Videos found.") + "</font>");
                    newlink6.setProperty("tip", tp.translate("No Tutorial Videos have been provided for this context. Please contact Administrator"));
                    newlink6.setProperty("link", "#");
                    newlink6.setProperty("releaselocks", "N");
                    tutorialMenu.add(newlink6);
                }
                if (issueMenu.size() > 0) {
                    menu.addAll(issueMenu);
                }
                if (helpMenu.size() > 0) {
                    if (menu.size() > 0) {
                        PropertyList separatorMenu = new PropertyList();
                        separatorMenu.setProperty("id", menuid + "_menuSeparator1");
                        separatorMenu.setProperty("text", tp.translate("-"));
                        separatorMenu.setProperty("tip", tp.translate(""));
                        separatorMenu.setProperty("link", "#");
                        separatorMenu.setProperty("onclick", "");
                        separatorMenu.setProperty("releaselocks", "N");
                        menu.add(separatorMenu);
                    }
                    menu.addAll(helpMenu);
                }
                if (tutorialMenu.size() > 0) {
                    if (menu.size() > 0) {
                        PropertyList separatorMenu = new PropertyList();
                        separatorMenu.setProperty("id", menuid + "_menuSeparator2");
                        separatorMenu.setProperty("text", tp.translate("-"));
                        separatorMenu.setProperty("tip", tp.translate(""));
                        separatorMenu.setProperty("link", "#");
                        separatorMenu.setProperty("onclick", "");
                        separatorMenu.setProperty("releaselocks", "N");
                        menu.add(separatorMenu);
                    }
                    menu.addAll(tutorialMenu);
                }
                if (menu.size() == 0) {
                    PropertyList newlink7 = new PropertyList();
                    newlink7.setProperty("id", menuid + "_help-nomenu");
                    newlink7.setProperty("text", "<font color=red>" + tp.translate("No Menu Items defined.") + "</font>");
                    newlink7.setProperty("tip", tp.translate("No Menu Items defined. Please contact Administrator"));
                    newlink7.setProperty("link", "#");
                    newlink7.setProperty("releaselocks", "N");
                    menu.add(newlink7);
                }
                ajaxResponse.addCallbackArgument("menuid", menuid);
                ajaxResponse.addCallbackArgument("menu", menu.toJSONString());
            }
        }
        catch (Exception e) {
            this.logError("Failed to get help gizmo data. Reason: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }

    private boolean isHelpURLReachable(String helpRootURL, HttpServletRequest request) {
        block6: {
            try {
                HttpSession session = request.getSession();
                String helpURLFlag = (String)session.getAttribute("helpurlaccessibleflag");
                if (helpURLFlag == null) {
                    URL url = new URL(helpRootURL + "/index.htm");
                    try {
                        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("GET");
                        if (connection.getResponseCode() == 200) {
                            session.setAttribute("helpurlaccessibleflag", (Object)"Y");
                            return true;
                        }
                        session.setAttribute("helpurlaccessibleflag", (Object)"N");
                    }
                    catch (Exception e) {
                        session.setAttribute("helpurlaccessibleflag", (Object)"N");
                        this.logError("Failed to establish connection to help application using " + helpRootURL + "/index.htm", e);
                    }
                    break block6;
                }
                return "Y".equals(helpURLFlag);
            }
            catch (Exception e) {
                this.logError("Exception occurred when trying to establish connection to help application using " + helpRootURL + "/index.htm");
            }
        }
        return false;
    }
}


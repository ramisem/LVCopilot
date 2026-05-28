/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.actions.wap.SetActivityResource;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.ActivityResources;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPage;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceContainer;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageResourceData;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageUtil;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ActivityResourcesAjax
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block60: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            try {
                Mode mode;
                try {
                    mode = Mode.valueOf(ajaxResponse.getRequestParameter("mode").toUpperCase());
                }
                catch (Exception e) {
                    mode = Mode.ADD;
                }
                String displayTZ = ajaxResponse.getRequestParameter("timezone");
                ZoneId displayTimeZone = ZoneOffset.UTC;
                ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                if (displayTZ.length() > 0) {
                    try {
                        try {
                            displayTimeZone = TimeZone.getTimeZone(displayTZ).toZoneId();
                        }
                        catch (Exception e1) {
                            displayTimeZone = I18nUtil.getZoneIdFromString(displayTZ);
                        }
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to find timezone", e);
                    }
                } else if (connectionInfo.getTimeZone() != null && connectionInfo.getTimeZone().length() > 0) {
                    try {
                        try {
                            displayTimeZone = TimeZone.getTimeZone(connectionInfo.getTimeZone()).toZoneId();
                        }
                        catch (Exception e1) {
                            displayTimeZone = I18nUtil.getZoneIdFromString(displayTZ);
                        }
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to find timezone", e);
                    }
                } else {
                    displayTimeZone = TimeZone.getDefault().toZoneId();
                }
                String activityid = ajaxResponse.getRequestParameter("activityid");
                String elementid = ajaxResponse.getRequestParameter("elementid");
                WAPCommands wapCommands = new WAPCommands(this.getConnectionId());
                if (activityid.length() > 0) {
                    PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
                    if (mode == Mode.LOOKUP) {
                        int resourceNum = 0;
                        try {
                            resourceNum = Integer.parseInt(ajaxResponse.getRequestParameter("resourcenum"));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        AssignmentPage.ResourceSDC resourceSDC = AssignmentPage.ResourceSDC.USER;
                        try {
                            resourceSDC = AssignmentPage.ResourceSDC.valueOf(ajaxResponse.getRequestParameter("resourcesdc").toUpperCase());
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        StringBuilder html = new StringBuilder();
                        DataSet resources = wapCommands.getActivityResources(activityid);
                        if (resources != null) {
                            Activity activity = wapCommands.getActivityDetails(activityid);
                            String eid = elementid != null && elementid.length() > 0 ? elementid : "activityresources";
                            html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"WEB-CORE/modules/wap/style/activityresources.css\">");
                            html.append("<script src=\"WEB-CORE/modules/wap/scripts/activityresources.js\" type=\"text/javascript\"></script>");
                            html.append("<div id=\"").append(eid).append("_lookup\">");
                            ActivityClassHandler activityClassHandler = new ActivityClassHandler(this.getConnectionId(), "_dummy", "Activity", "LV_Activity");
                            AssignmentPage.AssignmentPageResourceRequirement resourceRequirement = AssignmentPageUtil.loadResources(activityClassHandler, activityid, null, activity.getTestingDepartmentid(), AssignmentPage.ResourceMode.ALL, this.getConnectionId(), this.getSDIProcessor(), this.getTranslationProcessor(), false, null);
                            AssignmentPageResourceData resourceData = resourceRequirement.getResourceData();
                            resourceData.setResourceRequirements(resources);
                            boolean found = false;
                            for (int i = 0; i < resourceData.getResources().size(); ++i) {
                                AssignmentPageResourceContainer resourceContainer = resourceData.getResources().get(i);
                                if (resourceContainer.getResourceSDC() != resourceSDC || resourceContainer.getNum() != resourceNum) continue;
                                resourceContainer.setShowAll(true);
                                String classN = "";
                                classN = resourceContainer.getResourceSDC() == AssignmentPage.ResourceSDC.INSTRUMENT ? "resource_instrument" : "resource_user";
                                if (resourceContainer.getData() != null && resourceContainer.getData().getRowCount() > 0) {
                                    html.append("<div class=\"").append(classN).append("\">");
                                    AssignmentPageUtil.drawResourceHtml(html, null, null, null, null, null, displayTimeZone, resourceContainer.resourceData, resourceContainer.attachmentData, resourceContainer.getResourceSDC().getName(), resourceContainer, null, 64, "top._activityResources", AssignmentPage.ColorScheme.DEFAULT, this.logger, this.getTranslationProcessor(), pageContext);
                                    html.append("</div>");
                                    found = true;
                                }
                                if (resourceContainer.getWorkareas() == null || resourceContainer.getWorkareas().getRowCount() <= 0) continue;
                                html.append("<div class=\"").append("resource_department").append("\">");
                                AssignmentPageUtil.drawResourceHtml(html, null, null, null, null, null, displayTimeZone, resourceContainer.workareaData, resourceContainer.attachmentData, "Department", resourceContainer, null, 64, "top._activityResources", AssignmentPage.ColorScheme.DEFAULT, this.logger, this.getTranslationProcessor(), pageContext);
                                html.append("</div>");
                                found = true;
                            }
                            if (!found) {
                                html.append("<div id=\"error\">").append(this.getTranslationProcessor().translate("No Resources Found")).append(".</div>");
                            }
                            html.append("</div>");
                            ajaxResponse.addCallbackArgument("html", html.toString());
                            ajaxResponse.addCallbackArgument("resourcesdc", resourceSDC.toString());
                            ajaxResponse.addCallbackArgument("resourcenum", resourceNum);
                        } else {
                            ajaxResponse.addCallbackArgument("html", "No resources found.");
                        }
                        break block60;
                    }
                    if (mode == Mode.ADD) {
                        String sdcid = ajaxResponse.getRequestParameter("sdcid");
                        String itemid = ajaxResponse.getRequestParameter("itemid");
                        String resourcenum = ajaxResponse.getRequestParameter("resourcenum");
                        String resourcesdc = ajaxResponse.getRequestParameter("resourcesdc");
                        if (sdcid.length() > 0 && itemid.length() > 0 && resourcenum.length() > 0 && resourcesdc.length() > 0) {
                            try {
                                String[] sdcids = StringUtil.split(sdcid, ";");
                                String[] itemids = StringUtil.split(itemid, ";");
                                String[] resourcenums = StringUtil.split(resourcenum, ";");
                                String[] resourcesdcs = StringUtil.split(resourcesdc, ";");
                                for (int i = 0; i < resourcenums.length; ++i) {
                                    PropertyList props = new PropertyList();
                                    props.setProperty("activityid", activityid);
                                    props.setProperty("resourcenum", resourcenums[i]);
                                    AssignmentPage.ResourceSDC resourceSDC = AssignmentPage.ResourceSDC.USER;
                                    try {
                                        resourceSDC = AssignmentPage.ResourceSDC.valueOf(resourcesdcs[i].toUpperCase());
                                    }
                                    catch (Exception resourceContainer) {
                                        // empty catch block
                                    }
                                    props.setProperty("resourcekeyid1", itemids[i]);
                                    if (resourceSDC == AssignmentPage.ResourceSDC.USER) {
                                        if (sdcids[i].equalsIgnoreCase(resourceSDC.getName())) {
                                            props.setProperty("resourcesdcid", resourceSDC.getName());
                                        } else {
                                            props.setProperty("resourcesdcid", "Department");
                                        }
                                    } else if (sdcids[i].equalsIgnoreCase(resourceSDC.getName())) {
                                        props.setProperty("resourcesdcid", resourceSDC.getName());
                                    } else {
                                        props.setProperty("resourcesdcid", "Department");
                                    }
                                    try {
                                        this.getActionProcessor().processActionClass(SetActivityResource.class.getName(), props);
                                        continue;
                                    }
                                    catch (SapphireException e) {
                                        throw new SapphireException(e.getMessage(), e);
                                    }
                                }
                                boolean canAdd = ajaxResponse.getRequestParameter("canadd", "Y").equalsIgnoreCase("Y");
                                boolean canRemove = ajaxResponse.getRequestParameter("canremove", "Y").equalsIgnoreCase("Y");
                                DataSet work = wapCommands.getActivityWorkSDIs(activityid);
                                StringBuilder html = new StringBuilder();
                                ActivityResources.renderHtml(html, activityid, elementid, false, canAdd, canRemove, wapCommands, displayTimeZone, this.getConnectionId(), this.getTranslationProcessor(), this.getSDIProcessor(), this.logger, pageContext);
                                ajaxResponse.addCallbackArgument("html", html.toString());
                                ajaxResponse.addCallbackArgument("msg", "");
                            }
                            catch (Exception e) {
                                this.logger.warn(e.getMessage());
                                ajaxResponse.addCallbackArgument("html", "");
                                ajaxResponse.addCallbackArgument("msg", e.getMessage());
                            }
                            break block60;
                        }
                        ajaxResponse.addCallbackArgument("html", "");
                        ajaxResponse.addCallbackArgument("msg", "No resources provided.");
                        break block60;
                    }
                    if (mode != Mode.REMOVE) break block60;
                    String resourcenum = ajaxResponse.getRequestParameter("resourcenum");
                    String resourcesdc = ajaxResponse.getRequestParameter("resourcesdc");
                    String sdcid = ajaxResponse.getRequestParameter("sdcid");
                    if (sdcid.length() > 0 && resourcenum.length() > 0 && resourcesdc.length() > 0) {
                        try {
                            String[] resourcenums = StringUtil.split(resourcenum, ";");
                            String[] resourcesdcs = StringUtil.split(resourcesdc, ";");
                            String[] sdcids = StringUtil.split(sdcid, ";");
                            for (int i = 0; i < resourcenums.length; ++i) {
                                PropertyList props = new PropertyList();
                                props.setProperty("activityid", activityid);
                                props.setProperty("resourcenum", resourcenums[i]);
                                AssignmentPage.ResourceSDC resourceSDC = AssignmentPage.ResourceSDC.USER;
                                try {
                                    resourceSDC = AssignmentPage.ResourceSDC.valueOf(resourcesdcs[i].toUpperCase());
                                }
                                catch (Exception canRemove) {
                                    // empty catch block
                                }
                                props.setProperty("resourcekeyid1", "");
                                if (resourceSDC == AssignmentPage.ResourceSDC.USER) {
                                    if (sdcids[i].equalsIgnoreCase(resourceSDC.getName())) {
                                        props.setProperty("resourcesdcid", resourceSDC.getName());
                                    } else {
                                        props.setProperty("resourcesdcid", "Department");
                                    }
                                } else if (sdcids[i].equalsIgnoreCase(resourceSDC.getName())) {
                                    props.setProperty("resourcesdcid", resourceSDC.getName());
                                } else {
                                    props.setProperty("resourcesdcid", "Department");
                                }
                                try {
                                    this.getActionProcessor().processActionClass(SetActivityResource.class.getName(), props);
                                    continue;
                                }
                                catch (SapphireException e) {
                                    throw new SapphireException(e.getMessage(), e);
                                }
                            }
                            boolean canAdd = ajaxResponse.getRequestParameter("canadd", "Y").equalsIgnoreCase("Y");
                            boolean canRemove = ajaxResponse.getRequestParameter("canremove", "Y").equalsIgnoreCase("Y");
                            DataSet work = wapCommands.getActivityWorkSDIs(activityid);
                            StringBuilder html = new StringBuilder();
                            ActivityResources.renderHtml(html, activityid, elementid, false, canAdd, canRemove, wapCommands, displayTimeZone, this.getConnectionId(), this.getTranslationProcessor(), this.getSDIProcessor(), this.logger, ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                            ajaxResponse.addCallbackArgument("html", html.toString());
                            ajaxResponse.addCallbackArgument("msg", "");
                        }
                        catch (Exception e) {
                            this.logger.warn(e.getMessage());
                            ajaxResponse.addCallbackArgument("html", "");
                            ajaxResponse.addCallbackArgument("msg", e.getMessage());
                        }
                        break block60;
                    }
                    ajaxResponse.addCallbackArgument("html", "");
                    ajaxResponse.addCallbackArgument("msg", "No resources provided.");
                    break block60;
                }
                ajaxResponse.addCallbackArgument("html", "");
                ajaxResponse.addCallbackArgument("msg", "No activity provided.");
            }
            catch (Exception e) {
                this.logger.error(e.getMessage(), e);
            }
            finally {
                ajaxResponse.print();
            }
        }
    }

    public static enum Mode {
        ADD,
        REMOVE,
        LOOKUP;

    }
}


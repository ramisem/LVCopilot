/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.ActivityClassHandler;
import com.labvantage.sapphire.modules.wap.activity.AssignmentPageUtil;
import com.labvantage.sapphire.modules.wap.activity.WAPCommands;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ActivityWorkListAjax
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            Mode mode;
            try {
                mode = Mode.valueOf(ajaxResponse.getRequestParameter("mode").toUpperCase());
            }
            catch (Exception e) {
                mode = Mode.ADD;
            }
            String activityid = ajaxResponse.getRequestParameter("activityid");
            if (activityid.length() > 0) {
                String keyid1 = ajaxResponse.getRequestParameter("keyid1");
                if (keyid1.length() > 0) {
                    keyid1 = StringUtil.replaceAll(keyid1, "%3B", ";");
                    WAPCommands wapCommands = new WAPCommands(this.getConnectionId());
                    Activity activity = wapCommands.getActivityDetails(activityid);
                    PropertyList wapPolicy = AssignmentPageUtil.getWapPolicy(this.getConnectionId());
                    ActivityClassHandler activityClassHandler = ActivityClassHandler.getInstance(this.getConnectionId(), wapPolicy, activity.getActivityClass(), activity.getWorksdcid());
                    try {
                        PropertyList actionProps;
                        if (mode == Mode.ADD) {
                            actionProps = new PropertyList();
                            actionProps.setProperty("activityid", activityid);
                            actionProps.setProperty("worksdcid", activity.getWorksdcid());
                            actionProps.setProperty("workkeyid1", keyid1);
                            this.getActionProcessor().processAction("AddActivityWorkSDI", "1", actionProps);
                        } else if (mode == Mode.REMOVE) {
                            actionProps = new PropertyList();
                            actionProps.setProperty("activityid", activityid);
                            actionProps.setProperty("worksdcid", activity.getWorksdcid());
                            actionProps.setProperty("workkeyid1", keyid1);
                            actionProps.setProperty("setpending", "Y");
                            this.getActionProcessor().processAction("RemoveActivityWorkSDI", "1", actionProps);
                        }
                        DataSet work = wapCommands.getActivityWorkSDIs(activityid);
                        ajaxResponse.addCallbackArgument("pagedir", activityClassHandler.getListPageDirectives(work.getColumnValues("workkeyid1", ";"), this.getSDCProcessor(), this.getTranslationProcessor(), ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response)).toJSONString());
                        ajaxResponse.addCallbackArgument("msg", "");
                    }
                    catch (Exception e) {
                        this.logger.warn(e.getMessage());
                        ajaxResponse.addCallbackArgument("pagedir", "");
                        ajaxResponse.addCallbackArgument("msg", e.getMessage());
                    }
                } else {
                    ajaxResponse.addCallbackArgument("pagedir", "");
                    ajaxResponse.addCallbackArgument("msg", "No work provided.");
                }
            } else {
                ajaxResponse.addCallbackArgument("pagedir", "");
                ajaxResponse.addCallbackArgument("msg", "No activity provided.");
            }
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
        finally {
            ajaxResponse.print();
        }
    }

    public static enum Mode {
        ADD,
        REMOVE;

    }
}


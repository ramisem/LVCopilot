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
package com.labvantage.sapphire.layouts.modern;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.GroupGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GizmoTargetAjaxManager
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 78207 $";
    public static final String USEROVERRIDESID = "__gt_groupgizmo_";

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        changeMade = false;
        ajaxResponse = new AjaxResponse(request, response, "GizmoTargetHandler");
        mode = Mode.REFRESH;
        try {
            mode = Mode.valueOf(ajaxResponse.getRequestParameter("mode", Mode.REFRESH.toString()).toUpperCase());
        }
        catch (Exception e) {
            mode = Mode.REFRESH;
        }
        gizmoLocation = BaseGizmo.GizmoLocation.SIDEBAR;
        s = ajaxResponse.getRequestParameter("gizmolocation", "");
        if (s.length() == 0) {
            sidebar = ajaxResponse.getRequestParameter("sidebar", "Y").equalsIgnoreCase("Y");
            gizmoLocation = sidebar ? BaseGizmo.GizmoLocation.SIDEBAR : BaseGizmo.GizmoLocation.TOPBAR;
        } else {
            try {
                gizmoLocation = BaseGizmo.GizmoLocation.valueOf(s.toUpperCase());
            }
            catch (Exception e1) {
                gizmoLocation = BaseGizmo.GizmoLocation.SIDEBAR;
            }
        }
        groupgizmoid = ajaxResponse.getRequestParameter("groupgizmo", "");
        o = request.getSession().getAttribute("userconfig");
        userconfig = o != null ? (PropertyList)o : RequestContext.getInstance(request).getPropertyList("userconfig");
        gizmos = null;
        pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        saveasuseroverrides = ajaxResponse.getRequestParameter("saveasuseroverrides", "Y").equalsIgnoreCase("Y");
        groupGizmo = GizmoTargetAjaxManager.getGroupGizmo(groupgizmoid, saveasuseroverrides, pageContext);
        gizmos = groupGizmo.getElementProperties().getCollection("gizmos");
        if (gizmos == null) {
            gizmos = new PropertyListCollection();
            groupGizmo.getElementProperties().setProperty("gizmos", gizmos);
        }
        userOverrides = new PropertyList();
        devmode = false;
        cp = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(pageContext);
        try {
            devmode = cp.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception var19_22) {
            // empty catch block
        }
        rawGizmo = null;
        if (!saveasuseroverrides) {
            rawGizmo = BaseGizmo.getGizmoDef(groupGizmo.getGizmoDefId(), this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), this.getConnectionProcessor().getSapphireConnection().getSysuserId(), this.getConnectionId());
            if (rawGizmo != null && rawGizmo.size() > 0) {
                valueTree = rawGizmo.getClob(0, devmode != false ? "productvaluetree" : "valuetree");
                try {
                    rawGizmoPL = new PropertyList();
                    rawGizmoPL.setPropertyList(valueTree);
                    userOverrides = rawGizmoPL;
                }
                catch (Exception e) {
                    this.logger.error("Could not load value tree from raw.");
                }
            } else {
                this.logger.warn("No raw data");
            }
        }
        gizmodefid = "";
        ptreeid = "";
        extendnodeid = "";
        collectionid = "";
        elementid = "";
        windowid = ajaxResponse.getRequestParameter("windowid");
        html = new StringBuffer();
        script = new StringBuffer();
        switch (1.$SwitchMap$com$labvantage$sapphire$layouts$modern$GizmoTargetAjaxManager$Mode[mode.ordinal()]) {
            case 1: {
                gizmodefid = ajaxResponse.getRequestParameter("gizmodefid");
                elementid = ajaxResponse.getRequestParameter("elementid", "");
                if (gizmodefid.length() <= 0) break;
                outHtml = GizmoTargetAjaxManager.getGizmo(elementid + "_preview", gizmodefid, script, BaseGizmo.GizmoStyle.FULL, GizmoType.PREVIEW, this.getConnectionId(), pageContext);
                ajaxResponse.addCallbackArgument("html", outHtml);
                ajaxResponse.addCallbackArgument("script", script.toString());
                break;
            }
            case 2: {
                changeMade = false;
                if (groupgizmoid.length() > 0) {
                    groupGizmo.resetUserOverrides();
                    break;
                }
                gizmodefid = ajaxResponse.getRequestParameter("gizmodefid");
                gizmo = BaseGizmo.getInstance(pageContext, gizmodefid, false);
                gizmo.resetUserOverrides();
                break;
            }
            case 3: {
                order = null;
                try {
                    order = new JSONArray(ajaxResponse.getRequestParameter("order", "[]"));
                }
                catch (Exception var30_36) {
                    // empty catch block
                }
                if (order == null) break;
                out = new PropertyListCollection();
                moveMade = false;
                block73: for (i = 0; i < order.length(); ++i) {
                    try {
                        id = order.getString(i);
                        if (id.length() <= 0) continue;
                        for (c = 0; c < gizmos.size(); ++c) {
                            gizmo = gizmos.getPropertyList(c);
                            if (!gizmo.getProperty("id", gizmo.getId()).equals(id)) continue;
                            out.add(gizmo);
                            if (changeMade) continue block73;
                            moveMade = true;
                            continue block73;
                        }
                        continue;
                    }
                    catch (Exception id) {
                        // empty catch block
                    }
                }
                if (!moveMade) break;
                GizmoTargetAjaxManager.sequenceCollection(out);
                changeMade = true;
                gizmos = out;
                userOverrides.setProperty("gizmos", gizmos);
                break;
            }
            case 4: {
                gizmodefid = ajaxResponse.getRequestParameter("gizmodefid");
                gizmotype = ajaxResponse.getRequestParameter("gizmotype");
                desc = ajaxResponse.getRequestParameter("gizmodefdesc");
                if (gizmodefid.length() > 0 && gizmotype.length() > 0) {
                    if (ajaxResponse.getRequestParameter("gizmoprops", "").length() > 0) {
                        try {
                            pl = new PropertyList(new JSONObject(ajaxResponse.getRequestParameter("gizmoprops", "")));
                        }
                        catch (Exception e) {
                            this.logger.error("Failed to read create properties.");
                            pl = new PropertyList();
                        }
                    } else {
                        pl = new PropertyList();
                    }
                    if (!pl.containsKey("gizmoprops")) {
                        gizmoprops = new PropertyList();
                        gizmoprops.setProperty("title", desc);
                        pl.setProperty("gizmoprops", gizmoprops);
                    } else {
                        pl.getPropertyList("gizmoprops").setProperty("title", desc);
                    }
                    addSDI = new PropertyList();
                    addSDI.setProperty("sdcid", "LV_GizmoDef");
                    addSDI.setProperty("keyid1", gizmodefid);
                    addSDI.setProperty("gizmodefdesc", desc.length() == 0 ? gizmodefid : desc);
                    addSDI.setProperty("propertytreeid", gizmotype);
                    addSDI.setProperty("extendnodeid", ajaxResponse.getRequestParameter("nodeid", "Sapphire Custom"));
                    if (devmode) {
                        addSDI.setProperty("productvaluetree", pl.toXMLString());
                    } else {
                        addSDI.setProperty("valuetree", pl.toXMLString());
                    }
                    try {
                        this.getActionProcessor().processAction("AddSDI", "1", addSDI);
                    }
                    catch (Exception e) {
                        m = "";
                        m = e.getMessage().contains("ORA-00001: unique constraint") != false ? this.getTranslationProcessor().translate("Failed to add due to ") + gizmotype + " " + gizmodefid + " " + this.getTranslationProcessor().translate("already existing.") : this.getTranslationProcessor().translate("Failed to add ") + gizmotype + " " + gizmodefid + ".\nError - " + e.getMessage() + "\n" + this.getTranslationProcessor().translate("Please check log for more information.");
                        ajaxResponse.setError(m);
                        this.logger.error(m);
                        this.logger.error("Failed to create gizmo. " + e.getMessage());
                        gizmodefid = "";
                    }
                } else {
                    e = "Failed to add gizmo. No gizmo or type provided.";
                    ajaxResponse.setError(e);
                    this.logger.error(e);
                    gizmodefid = "";
                }
                ajaxResponse.addCallbackArgument("gizmodefid", gizmodefid);
                break;
            }
            case 5: {
                dashboard = null;
                try {
                    dashboard = new JSONArray(ajaxResponse.getRequestParameter("dashboard", "[]"));
                }
                catch (Exception addSDI) {
                    // empty catch block
                }
                if (dashboard == null) break;
                out = userOverrides;
                out.setProperty("gizmos", gizmos);
                currentHead = groupGizmo.getElementProperties().getProperty("showdashboardheaders", "Y").equalsIgnoreCase("Y");
                thisHead = ajaxResponse.getRequestParameter("showheader", "Y").equalsIgnoreCase("Y");
                if (currentHead != thisHead) {
                    out.setProperty("showdashboardheaders", thisHead != false ? "Y" : "N");
                    changeMade = true;
                }
                if ((currentPublic = groupGizmo.getElementProperties().getPropertyList("groupoptions").getProperty("public", "N").equalsIgnoreCase("Y")) != (thisPublic = ajaxResponse.getRequestParameter("public", "Y").equalsIgnoreCase("Y"))) {
                    if (!out.containsKey("groupoptions")) {
                        out.setProperty("groupoptions", new PropertyList());
                    }
                    out.getPropertyList("groupoptions").setProperty("public", thisPublic != false ? "Y" : "N");
                    changeMade = true;
                }
                if (!(currentTitle = groupGizmo.getTitle()).equalsIgnoreCase(thisTitle = ajaxResponse.getRequestParameter("title", currentTitle))) {
                    if (!out.containsKey("gizmoprops")) {
                        out.setProperty("gizmoprops", new PropertyList());
                    }
                    out.getPropertyList("gizmoprops").setProperty("title", thisTitle);
                    changeMade = true;
                }
                if (!(currentEditing = groupGizmo.getElementProperties().getPropertyList("groupoptions").getProperty("editinglevel", "")).equalsIgnoreCase(thisEditing = ajaxResponse.getRequestParameter("editinglevel", currentEditing))) {
                    if (!out.containsKey("groupoptions")) {
                        out.setProperty("groupoptions", new PropertyList());
                    }
                    out.getPropertyList("groupoptions").setProperty("editinglevel", thisEditing);
                    changeMade = true;
                }
                for (i = 0; i < dashboard.length(); ++i) {
                    try {
                        job = dashboard.getJSONObject(i);
                        id = job.getString("id");
                        find = gizmos.find("id", id);
                        if (find == null) continue;
                        grid = new PropertyList();
                        grid.setProperty("size_x", job.getString("size_x"));
                        grid.setProperty("size_y", job.getString("size_y"));
                        grid.setProperty("row", job.getString("row"));
                        grid.setProperty("col", job.getString("col"));
                        find.setProperty("grid", grid);
                        if (changeMade) continue;
                        changeMade = true;
                        continue;
                    }
                    catch (Exception job) {
                        // empty catch block
                    }
                }
                break;
            }
            case 6: {
                gizmodefidArray = StringUtil.split(ajaxResponse.getRequestParameter("gizmodefid"), "%3B");
                ptreeidArray = StringUtil.split(ajaxResponse.getRequestParameter("ptreeid", ""), "%3B");
                extendnodeidArray = StringUtil.split(ajaxResponse.getRequestParameter("extendnodeid", ""), "%3B");
                try {
                    guiPolicy = new ConfigurationProcessor(pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
                }
                catch (Exception e) {
                    guiPolicy = null;
                }
                gizmoStyle = gizmoLocation == BaseGizmo.GizmoLocation.SIDEBAR ? BaseGizmo.GizmoStyle.getGizmoStyle(guiPolicy != null ? guiPolicy.getProperty("groupgizmostyle", BaseGizmo.GizmoStyle.LARGETEXT.toString()) : BaseGizmo.GizmoStyle.LARGETEXT.toString(), true) : (gizmoLocation == BaseGizmo.GizmoLocation.DASHBOARD ? BaseGizmo.GizmoStyle.LARGETEXT : BaseGizmo.GizmoStyle.SMALL);
                outHtmlList = new ArrayList<String>();
                outScriptList = new ArrayList<String>();
                gizmoprops = null;
                parameters = null;
                tp = ajaxResponse.getRequestParameter("parameters", "");
                if (tp.length() > 0) {
                    try {
                        parameters = new PropertyList(new JSONObject(tp));
                    }
                    catch (Exception i) {
                        // empty catch block
                    }
                }
                if (gizmodefidArray.length > 0) {
                    for (k = 0; k < gizmodefidArray.length; ++k) {
                        gizmodefid = gizmodefidArray[k];
                        if (gizmodefid.length() <= 0 && (ptreeid.length() <= 0 || extendnodeid.length() <= 0) || gizmodefid.length() <= 0) continue;
                        id = "gt_" + new Random().nextInt(1000);
                        outScript = new StringBuffer();
                        outHtml = GizmoTargetAjaxManager.getGizmo(id, gizmodefid, outScript, gizmoStyle, gizmoLocation == BaseGizmo.GizmoLocation.SIDEBAR ? GizmoType.SIDEBAR : (gizmoLocation == BaseGizmo.GizmoLocation.DASHBOARD ? GizmoType.DASHBOARD : GizmoType.TOPBAR), parameters, null, this.getConnectionId(), pageContext, this.getTranslationProcessor());
                        outHtmlList.add(outHtml);
                        outScriptList.add(outScript.toString());
                        newP = new PropertyList();
                        newP.setId(id);
                        newP.setProperty("id", id);
                        newP.setProperty("gizmoid", gizmodefid);
                        newP.setAttribute("fromuser", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                        if (gizmoprops != null) {
                            newP.setProperty("ptreeid", ptreeid);
                            newP.setProperty("extendnodeid", extendnodeid);
                            newP.setProperty("gizmoprops", gizmoprops);
                        }
                        gizmos.add(newP);
                        userOverrides.setProperty("gizmos", gizmos);
                        changeMade = true;
                    }
                }
                if (ptreeidArray.length > 0 && ptreeidArray.length == extendnodeidArray.length) {
                    for (k = 0; k < ptreeidArray.length; ++k) {
                        ptreeid = ptreeidArray[k];
                        extendnodeid = extendnodeidArray[k];
                        if (ptreeid.length() <= 0 || extendnodeid.length() <= 0) continue;
                        id = "gt_" + new Random().nextInt(1000);
                        gizmo = BaseGizmo.getTypeInstance(this.getConnectionId(), pageContext, ptreeid, extendnodeid, parameters, false);
                        if (gizmo != null) {
                            uid = StringUtil.replaceAll(id, " ", "_") + (gizmoLocation == BaseGizmo.GizmoLocation.SIDEBAR ? "s_" : (gizmoLocation == BaseGizmo.GizmoLocation.DASHBOARD ? "d_" : "t_")) + ((int)(Math.random() * 100.0) + 1);
                            gizmo.setElementid(uid);
                            gizmodefid = "__CUSTOM" + id;
                            gizmo.setGizmoDefId(gizmodefid);
                            gizmoprops = gizmo.getElementProperties();
                            gp = ajaxResponse.getRequestParameter("properties", "");
                            if (gp.length() > 0) {
                                try {
                                    job = new JSONObject(gp);
                                    tomerge = new PropertyList(job);
                                    gizmoprops.setPropertyList(tomerge.toXMLString(), true, true);
                                }
                                catch (Exception job) {
                                    // empty catch block
                                }
                            }
                            if (gizmoprops.getPropertyListNotNull("gizmoprops").getProperty("title").length() > 0 && (matcher = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher(gizmoprops.getPropertyListNotNull("gizmoprops").getProperty("title"))).find()) {
                                this.logger.error("Found html tags in title. Add Gizmo aborted.");
                                throw new ServletException("Invalid title provided.");
                            }
                            outScript = new StringBuffer();
                            outHtml = GizmoTargetAjaxManager.renderGizmo(id, gizmo, outScript, gizmoLocation == BaseGizmo.GizmoLocation.SIDEBAR ? GizmoType.SIDEBAR : (gizmoLocation == BaseGizmo.GizmoLocation.DASHBOARD ? GizmoType.DASHBOARD : GizmoType.TOPBAR), gizmoStyle, this.getTranslationProcessor());
                            outHtmlList.add(outHtml);
                            outScriptList.add(outScript.toString());
                            newP = new PropertyList();
                            newP.setId(id);
                            newP.setProperty("id", id);
                            newP.setProperty("gizmoid", gizmodefid);
                            newP.setAttribute("fromuser", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                            if (gizmoprops != null) {
                                newP.setProperty("ptreeid", ptreeid);
                                newP.setProperty("extendnodeid", extendnodeid);
                                newP.setProperty("gizmoprops", gizmoprops);
                            }
                            gizmos.add(newP);
                            userOverrides.setProperty("gizmos", gizmos);
                            changeMade = true;
                            continue;
                        }
                        this.logger.warn("Gizmo for propertytreeid " + ptreeid + " could not be created.");
                    }
                }
                ajaxResponse.addCallbackArgument("html", outHtmlList.size() > 1 ? new JSONArray(outHtmlList).toString() : (outHtmlList.size() == 1 ? outHtmlList.get(0) : ""));
                ajaxResponse.addCallbackArgument("script", outScriptList.size() > 1 ? new JSONArray(outScriptList).toString() : (outScriptList.size() == 1 ? outScriptList.get(0) : ""));
                ajaxResponse.addCallbackArgument("multiple", outHtmlList.size() > 1);
                ajaxResponse.addCallbackArgument("params", parameters == null ? "" : parameters.toJSONString(true));
                GizmoTargetAjaxManager.sequenceCollection(gizmos);
                if (!ajaxResponse.getRequestParameter("savestate", "N").equalsIgnoreCase("Y") || ajaxResponse.getRequestParameter("stateid", "").length() <= 0) break;
                this.logger.debug("Saving state for gizmo...");
                try {
                    updateSql = "UPDATE webpagelog SET logtypeflag='P' WHERE webpagelogid=?";
                    this.getQueryProcessor().execPreparedUpdate(updateSql, new Object[]{ajaxResponse.getRequestParameter("stateid", "")});
                    this.logger.debug("State saved.");
                }
                catch (Exception e) {
                    this.logger.error("Failed to save state for gizmo.");
                }
                break;
            }
            case 7: {
                gizmodefidlist = StringUtil.split(ajaxResponse.getRequestParameter("gizmodefid"), ";");
                elementidlist = StringUtil.split(ajaxResponse.getRequestParameter("elementid", ""), ";");
                if (gizmodefidlist.length <= 0) break;
                v0 = gizmoLocation == BaseGizmo.GizmoLocation.SIDEBAR ? GizmoType.SIDEBAR_CONTENTONLY : (t = gizmoLocation == BaseGizmo.GizmoLocation.DASHBOARD ? GizmoType.DASHBOARD_CONTENTONLY : GizmoType.TOPBAR_CONTENTONLY);
                if (ajaxResponse.getRequestParameter("gizmotype").length() > 0) {
                    try {
                        t = GizmoType.valueOf(ajaxResponse.getRequestParameter("gizmotype").toUpperCase());
                    }
                    catch (Exception uid) {
                        // empty catch block
                    }
                }
                try {
                    guiPolicy = new ConfigurationProcessor(pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
                }
                catch (Exception e) {
                    guiPolicy = null;
                }
                parameters1 = null;
                tp1 = ajaxResponse.getRequestParameter("parameters", "");
                if (tp1.length() > 0) {
                    try {
                        parameters1 = new PropertyList(new JSONObject(tp1));
                    }
                    catch (Exception outScript) {
                        // empty catch block
                    }
                }
                size_x = null;
                if (ajaxResponse.getRequestParameter("size_x").length() > 0) {
                    size_x = StringUtil.split(ajaxResponse.getRequestParameter("size_x"), ";");
                }
                size_y = null;
                if (ajaxResponse.getRequestParameter("size_y").length() > 0) {
                    size_y = StringUtil.split(ajaxResponse.getRequestParameter("size_y"), ";");
                }
                containerwidth = null;
                if (ajaxResponse.getRequestParameter("containerwidth").length() > 0) {
                    containerwidth = StringUtil.split(ajaxResponse.getRequestParameter("containerwidth"), ";");
                }
                gizmoStyle1 = gizmoLocation == BaseGizmo.GizmoLocation.SIDEBAR ? BaseGizmo.GizmoStyle.getGizmoStyle(guiPolicy != null ? guiPolicy.getProperty("groupgizmostyle", BaseGizmo.GizmoStyle.LARGETEXT.toString()) : BaseGizmo.GizmoStyle.LARGETEXT.toString(), true) : (gizmoLocation == BaseGizmo.GizmoLocation.DASHBOARD ? BaseGizmo.GizmoStyle.FULL : BaseGizmo.GizmoStyle.SMALL);
                gizmostyles = null;
                if (ajaxResponse.getRequestParameter("gizmostyle", "").length() > 0) {
                    gizmostyles = StringUtil.split(ajaxResponse.getRequestParameter("gizmostyle").toUpperCase(), ";");
                }
                outHtmlList2 = new ArrayList<String>();
                outScriptList2 = new ArrayList<String>();
                for (c = 0; c < gizmodefidlist.length; ++c) {
                    elementid = elementidlist[c];
                    gizmodefid = gizmodefidlist[c];
                    additionalGizmoProps = new PropertyList();
                    if (size_x != null) {
                        try {
                            sizeX = Integer.parseInt(size_x[c]);
                            if (sizeX > -1) {
                                additionalGizmoProps.setProperty("size_x", "" + sizeX);
                            }
                        }
                        catch (Exception sizeX) {
                            // empty catch block
                        }
                    }
                    if (size_y != null) {
                        try {
                            sizeY = Integer.parseInt(size_y[c]);
                            if (sizeY > -1) {
                                additionalGizmoProps.setProperty("size_y", "" + sizeY);
                            }
                        }
                        catch (Exception sizeY) {
                            // empty catch block
                        }
                    }
                    if (containerwidth != null) {
                        try {
                            d = Double.parseDouble(containerwidth[c]);
                            width = (int)Math.floor(d);
                            if (width > -1) {
                                additionalGizmoProps.setProperty("containerwidth", "" + width);
                            }
                        }
                        catch (Exception d) {
                            // empty catch block
                        }
                    }
                    currentstyle = gizmoStyle1;
                    if (gizmostyles != null && gizmostyles.length > 0) {
                        try {
                            currentstyle = BaseGizmo.GizmoStyle.valueOf(gizmostyles[c]);
                        }
                        catch (Exception var57_153) {
                            // empty catch block
                        }
                    }
                    outHtml = new StringBuffer();
                    outScript = new StringBuffer();
                    if (gizmodefid.length() <= 0) ** GOTO lbl429
                    dynamicProps = null;
                    if (elementid.length() > 0 && ajaxResponse.getRequestParameter(elementid).length() > 0) {
                        try {
                            dynamicProps = new PropertyList(new JSONObject(ajaxResponse.getRequestParameter(elementid)));
                        }
                        catch (Exception var60_158) {
                            // empty catch block
                        }
                    }
                    if ((bg = GizmoTargetAjaxManager.getGizmo(elementid, gizmodefid, outHtml, outScript, currentstyle, t, parameters1, additionalGizmoProps, dynamicProps, this.getConnectionId(), pageContext, this.getTranslationProcessor())) != null || (gizmoProps = groupGizmo.getElementProperties().getCollection("gizmos").find("gizmoid", gizmodefid)) == null) ** GOTO lbl429
                    customProps = gizmoProps.getPropertyList("gizmoprops");
                    if (customProps == null) ** GOTO lbl428
                    ptreeidprop = gizmoProps.getProperty("ptreeid", "");
                    extendnodeidprop = gizmoProps.getProperty("extendnodeid", "");
                    bg = BaseGizmo.getTypeInstance(this.getConnectionId(), pageContext, ptreeidprop, extendnodeidprop, parameters1, false);
                    if (bg != null) {
                        uid = StringUtil.replaceAll(gizmoProps.getProperty("id"), " ", "_") + "_x" + ((int)(Math.random() * 100.0) + 1);
                        bg.setElementid(uid);
                        bg.setGizmoDefId(gizmodefid);
                        currentProps = bg.getElementProperties();
                        try {
                            currentProps.setPropertyList(customProps.toXMLString(), true, true);
                        }
                        catch (Exception var67_166) {
                            // empty catch block
                        }
                        if (additionalGizmoProps.size() > 0 && (gp = currentProps.getPropertyList("gizmoprops")) != null) {
                            try {
                                gp.setPropertyList(additionalGizmoProps.toXMLString(), true);
                            }
                            catch (Exception e) {
                                this.logger.warn("Could not add additional gizmo properties.");
                            }
                        }
                        outHtml.append(GizmoTargetAjaxManager.renderGizmo(gizmoProps.getProperty("id"), bg, outScript, GizmoType.DASHBOARD, currentstyle, this.getTranslationProcessor()));
                    } else {
                        this.logger.warn("Gizmo for propertytreeid " + ptreeid + " could not be created.");
                    }
                    ** GOTO lbl429
lbl428:
                    // 1 sources

                    this.logger.warn("Gizmo properties empty.");
lbl429:
                    // 5 sources

                    outHtmlList2.add(outHtml.toString());
                    outScriptList2.add(outScript.toString());
                }
                ajaxResponse.addCallbackArgument("html", outHtmlList2.size() > 1 ? new JSONArray(outHtmlList2).toString() : (outHtmlList2.size() == 1 ? outHtmlList2.get(0) : ""));
                ajaxResponse.addCallbackArgument("script", outScriptList2.size() > 1 ? new JSONArray(outScriptList2).toString() : (outScriptList2.size() == 1 ? outScriptList2.get(0) : ""));
                ajaxResponse.addCallbackArgument("parameters", parameters1 == null ? "" : parameters1.toJSONString(true));
                break;
            }
            case 8: {
                gizmodefid = ajaxResponse.getRequestParameter("gizmodefid");
                count = -1;
                if (gizmodefid.length() > 0 && (gizmo = BaseGizmo.getInstance(pageContext, gizmodefid, true)) != null) {
                    count = gizmo.getCount();
                }
                ajaxResponse.addCallbackArgument("elementid", ajaxResponse.getRequestParameter("elementid"));
                ajaxResponse.addCallbackArgument("count", count);
                break;
            }
            case 9: {
                gizmodefid = ajaxResponse.getRequestParameter("gizmodefid");
                collectionid = ajaxResponse.getRequestParameter("collectionid");
                elementid = ajaxResponse.getRequestParameter("elementid");
                for (c = 0; c < gizmos.size(); ++c) {
                    gizmo = gizmos.getPropertyList(c);
                    if (!gizmo.getProperty("id", gizmo.getId()).equals(collectionid)) continue;
                    if (!saveasuseroverrides) ** GOTO lbl468
                    rawGizmoPL = new PropertyList();
                    if (rawGizmo == null && (rawGizmo = BaseGizmo.getGizmoDef(groupGizmo.getGizmoDefId(), this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), this.getConnectionProcessor().getSapphireConnection().getSysuserId(), this.getConnectionId())) != null && rawGizmo.size() > 0) {
                        try {
                            valueTree = rawGizmo.getClob(0, "productvaluetree");
                            rawGizmoPL.setPropertyList(valueTree);
                        }
                        catch (Exception valueTree) {
                            // empty catch block
                        }
                    }
                    if (rawGizmoPL.getCollectionNotNull("gizmos").find("id", collectionid) == null && gizmo.getAttribute("fromuser").length() > 0) {
                        gizmos.remove(c);
                    } else {
                        gizmo.setProperty("show", "N");
                    }
                    ** GOTO lbl473
lbl468:
                    // 1 sources

                    if (!devmode && groupGizmo.getElementProperties().getAttribute("productvaluetree").equalsIgnoreCase("Y") || devmode && groupGizmo.getElementProperties().getAttribute("valuetree").equalsIgnoreCase("Y")) {
                        gizmo.setProperty("show", "N");
                    } else {
                        gizmos.remove(c);
                    }
lbl473:
                    // 4 sources

                    changeMade = true;
                    GizmoTargetAjaxManager.sequenceCollection(gizmos);
                    break;
                }
                if (!changeMade) break;
                userOverrides.setProperty("gizmos", gizmos);
                break;
            }
            case 10: {
                gizmodefid = ajaxResponse.getRequestParameter("gizmodefid");
                if (gizmodefid.length() <= 0) ** GOTO lbl524
                sc = this.getConnectionProcessor().getSapphireConnection();
                c = null;
                cachedAssets = c == null || c instanceof PropertyList == false ? new PropertyList() : (PropertyList)c;
                mscript = "";
                mhtml = "";
                if (cachedAssets.containsKey("topmenu")) ** GOTO lbl517
                bg = BaseGizmo.getInstance(pageContext, gizmodefid, true);
                if (bg != null && bg instanceof MenuGizmo) {
                    menuGizmo = (MenuGizmo)bg;
                    menuGizmo.setPageContext(pageContext);
                    menuGizmo.setElementid("topmenu");
                    menuProps = menuGizmo.getElementProperties();
                    menuProps.setProperty("menutype", "navigation");
                    try {
                        guiPolicy = new ConfigurationProcessor(pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
                    }
                    catch (Exception e) {
                        guiPolicy = null;
                    }
                    if (guiPolicy != null && guiPolicy.getProperty("menumode").equalsIgnoreCase("mouseover")) {
                        menuProps.setProperty("click", "Y");
                        menuProps.setProperty("mouseover", "Y");
                    } else {
                        menuProps.setProperty("click", "Y");
                        menuProps.setProperty("mouseover", "N");
                    }
                    menuProps.setProperty("customy", new Browser(pageContext).isIE() != false ? "0" : "1");
                    menuProps.setProperty("ajaxrender", "Y");
                    mhtml = menuGizmo.getHtml();
                    mscript = menuGizmo.getScript();
                    cachedtopmenu = new PropertyList();
                    cachedtopmenu.setProperty("html", mhtml);
                    cachedtopmenu.setProperty("script", mscript);
                    cachedAssets.setProperty("topmenu", cachedtopmenu);
                    CacheUtil.put(sc.getDatabaseId(), "GizmoDefAssets", gizmodefid, cachedAssets);
                }
                ** GOTO lbl520
lbl517:
                // 1 sources

                cachedtopmenu = cachedAssets.getPropertyList("topmenu");
                mhtml = cachedtopmenu.getProperty("html");
                mscript = cachedtopmenu.getProperty("script");
lbl520:
                // 2 sources

                html.append(mhtml);
                script.append(mscript);
lbl524:
                // 2 sources

                if (windowid.length() > 0) {
                    request.getSession().setAttribute("_menucache_" + windowid, (Object)gizmodefid);
                    request.getSession().setAttribute("_menucache_last", (Object)gizmodefid);
                }
                ajaxResponse.addCallbackArgument("html", html.toString());
                ajaxResponse.addCallbackArgument("script", script.toString());
                break;
            }
            case 11: {
                gizmodefid = ajaxResponse.getRequestParameter("gizmodefid");
                gg = null;
                if (gizmodefid.length() > 0) {
                    try {
                        guiPolicy = new ConfigurationProcessor(pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
                    }
                    catch (Exception e) {
                        guiPolicy = null;
                    }
                    gg = GizmoTargetAjaxManager.renderGizmoGroup(html, gizmodefid, GizmoType.SIDEBAR_CONTENTONLY, script, guiPolicy, userconfig, pageContext);
                }
                if (windowid.length() > 0) {
                    request.getSession().setAttribute("_sidebarcache_" + windowid, (Object)gizmodefid);
                    request.getSession().setAttribute("_sidebarcache_last", (Object)gizmodefid);
                }
                ajaxResponse.addCallbackArgument("html", html.toString());
                ajaxResponse.addCallbackArgument("script", script.toString());
                ajaxResponse.addCallbackArgument("groupgizmo", gizmodefid);
                ajaxResponse.addCallbackArgument("title", gg != null ? gg.getTitle() : gizmodefid);
                ajaxResponse.addCallbackArgument("locked", gg != null ? gg.getElementProperties().getProperty("lockgroup", "N") : "N");
                break;
            }
            case 12: {
                try {
                    guiPolicy = new ConfigurationProcessor(pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
                }
                catch (Exception e) {
                    guiPolicy = null;
                }
                GizmoTargetAjaxManager.renderGizmoGroup(html, "", GizmoType.TOPBAR_CONTENTONLY, script, guiPolicy, userconfig, pageContext);
                ajaxResponse.addCallbackArgument("html", html.toString());
                ajaxResponse.addCallbackArgument("script", script.toString());
                break;
            }
            case 13: {
                guimode = ajaxResponse.getRequestParameter("guimode");
                if (guimode.length() > 0) {
                    try {
                        ajaxResponse.getBrowser().setGUIMode(guimode, request);
                        if (ajaxResponse.getBrowser().getGUIMode() == null) {
                            // empty if block
                        }
                    }
                    catch (Exception cachedAssets) {
                        // empty catch block
                    }
                }
                temp = new StringBuffer();
                temp.append("<div>");
                image = new Image(pageContext);
                guiModes = ajaxResponse.getBrowser().getGUIModes();
                guiMode = ajaxResponse.getBrowser().getGUIMode();
                image.setImageId(guiMode != null ? guiMode.getImageRef() : "FlatBlackQuestionHelp1");
                image.setDimensions(16, 16);
                image.setTitle(guiMode.getTitle());
                image.setStyle("display: inline-block;position: relative;top: 3px;");
                temp.append(image.getHtml());
                temp.append("</div>");
                menuGizmo = new MenuGizmo();
                menuGizmo.init();
                menuGizmo.setPageContext(pageContext);
                menuProps = new PropertyList();
                menuProps.setProperty("renderincludes", "N");
                menuProps.setProperty("customhtml", temp.toString());
                devicemenu = new PropertyListCollection();
                for (i = 0; i < guiModes.size(); ++i) {
                    if (guiMode == null || guiMode.getId().equalsIgnoreCase(guiModes.get(i).getId())) continue;
                    deviceitem = new PropertyList();
                    deviceitem.setProperty("text", guiModes.get(i).getTitle().length() > 0 ? guiModes.get(i).getTitle() : guiModes.get(i).getId());
                    deviceitem.setProperty("link", "javascript:modernLayout.changeGUIMode('" + guiModes.get(i).getId() + "')");
                    devicemenu.add(deviceitem);
                }
                menuProps.setProperty("custommenu", devicemenu);
                menuProps.setProperty("customx", "80");
                menuProps.setProperty("menutype", "custom");
                menuProps.setProperty("click", "Y");
                menuProps.setProperty("mouseover", "N");
                menuGizmo.setElementProperties(menuProps);
                menuGizmo.setElementid("devicemenu");
                html.append(menuGizmo.getHtml());
                script.append("" + menuGizmo.getScript() + "");
                ajaxResponse.addCallbackArgument("html", html.toString());
                ajaxResponse.addCallbackArgument("script", script.toString());
                ajaxResponse.addCallbackArgument("guimode", guimode);
                ajaxResponse.addCallbackArgument("ismobile", ajaxResponse.getBrowser().isMobile() != false ? "Y" : "N");
                ajaxResponse.addCallbackArgument("istablet", ajaxResponse.getBrowser().isTablet() != false ? "Y" : "N");
                ajaxResponse.addCallbackArgument("isphone", ajaxResponse.getBrowser().isPhone() != false ? "Y" : "N");
                break;
            }
        }
        if (changeMade) {
            if (!userOverrides.containsKey("showgizmoheaders")) {
                userOverrides.setProperty("showgizmoheaders", groupGizmo.getElementProperties().getProperty("showgizmoheaders"));
            }
            if (saveasuseroverrides) {
                groupGizmo.saveUserOverrides(userOverrides);
            } else {
                groupGizmo.saveGizmoDefinition(userOverrides);
                groupGizmo.resetUserOverrides();
            }
        }
        ajaxResponse.print();
    }

    private static void sequenceCollection(PropertyListCollection collection) {
        for (int i = 0; i < collection.size(); ++i) {
            PropertyList propertyList = collection.getPropertyList(i);
            propertyList.setSequence(100000 + i);
        }
    }

    public static GroupGizmo renderGizmoGroup(StringBuffer html, String gizmogroupid, GizmoType type, StringBuffer script, PropertyList guiPolicy, PropertyList userconfig, PageContext pageContext) {
        GroupGizmo groupGizmo = null;
        if (type == GizmoType.TOPBAR || type == GizmoType.TOPBAR_CONTENTONLY) {
            groupGizmo = new GroupGizmo();
            groupGizmo.setPageContext(pageContext);
            groupGizmo.setElementid("topbarsystemgroup");
            PropertyList groupGizmoProps = new PropertyList();
            PropertyListCollection gizmos = new PropertyListCollection();
            groupGizmoProps.setProperty("gizmos", gizmos);
            if (guiPolicy != null && guiPolicy.getCollection("fixedgizmos") != null) {
                PropertyListCollection targets = guiPolicy.getCollection("fixedgizmos");
                if (gizmos != null) {
                    for (int i = 0; i < targets.size(); ++i) {
                        String gid;
                        PropertyList f = targets.getPropertyList(i);
                        if (!f.getProperty("show", "Y").equals("Y") || (gid = f.getProperty("gizmodefid")).length() <= 0) continue;
                        PropertyList g = new PropertyList();
                        g.setProperty("gizmoid", gid);
                        String id = "f_" + f.getProperty("id", f.getId());
                        if (id.length() == 0) {
                            id = "f_" + gid + ((int)(Math.random() * 100.0) + 1);
                        }
                        g.setProperty("id", id);
                        g.setId(id);
                        gizmos.add(g);
                    }
                }
            }
            groupGizmo.setElementProperties(groupGizmoProps);
            groupGizmoProps.setProperty("gizmotype", type.toString());
        } else {
            groupGizmo = GizmoTargetAjaxManager.getGroupGizmo(gizmogroupid, true, pageContext);
            groupGizmo.setElementid("sidebarsystemgroup");
            groupGizmo.getElementProperties().setProperty("gizmotype", type.toString());
            String gizmoStyle = groupGizmo.getElementProperties().getProperty("gizmostyle", guiPolicy != null ? guiPolicy.getProperty("groupgizmostyle", "Text Only") : "Text Only");
            groupGizmo.getElementProperties().setProperty("gizmostyle", gizmoStyle);
        }
        groupGizmo.getElementProperties().setProperty("layout", "Y");
        if (html != null) {
            html.append(groupGizmo.getHtml());
        }
        if (script != null) {
            script.append(groupGizmo.getScript());
        }
        return groupGizmo;
    }

    private static GroupGizmo getGroupGizmo(String gizmogroupid, boolean applyOverrides, PageContext pageContext) {
        sapphire.pageelements.BaseGizmo baseGizmo;
        GroupGizmo groupGizmo = null;
        if (gizmogroupid.length() > 0 && (baseGizmo = BaseGizmo.getInstance(pageContext, gizmogroupid, applyOverrides)) instanceof GroupGizmo) {
            groupGizmo = (GroupGizmo)baseGizmo;
        }
        if (groupGizmo == null) {
            groupGizmo = new GroupGizmo();
            groupGizmo.setPageContext(pageContext);
        }
        return groupGizmo;
    }

    public static StringBuffer getMissingParameters(BaseGizmo gizmoEl) {
        PropertyListCollection missingParams;
        StringBuffer missingBuffer = new StringBuffer();
        if (gizmoEl.getParameters() != null && gizmoEl.getGizmoDefId().length() > 0 && (missingParams = gizmoEl.getParameters().getCollection("missing")) != null && missingParams.size() > 0) {
            for (Object o : missingParams) {
                PropertyList missing = (PropertyList)o;
                if (!missing.getProperty("gizmodefid").equals(gizmoEl.getGizmoDefId())) continue;
                if (missingBuffer.length() > 0) {
                    missingBuffer.append("<br>");
                }
                missingBuffer.append(missing.getProperty("paramid"));
            }
        }
        return missingBuffer;
    }

    public static String renderGizmo(String collectionid, BaseGizmo gizmoEl, StringBuffer script, GizmoType gizmoType, BaseGizmo.GizmoStyle gizmoStyle) {
        return GizmoTargetAjaxManager.renderGizmo(collectionid, gizmoEl, script, gizmoType, gizmoStyle, null);
    }

    public static String renderGizmo(String collectionid, BaseGizmo gizmoEl, StringBuffer script, GizmoType gizmoType, BaseGizmo.GizmoStyle gizmoStyle, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        gizmoEl.setGizmoLocation(gizmoType == GizmoType.SIDEBAR || gizmoType == GizmoType.SIDEBAR_CONTENTONLY ? BaseGizmo.GizmoLocation.SIDEBAR : (gizmoType == GizmoType.TOPBAR || gizmoType == GizmoType.TOPBAR_CONTENTONLY ? BaseGizmo.GizmoLocation.TOPBAR : BaseGizmo.GizmoLocation.DASHBOARD));
        if (gizmoType == GizmoType.PREVIEW) {
            gizmoEl.setParameters(new PropertyList());
            gizmoEl.setWidth(535);
            gizmoEl.setHeight(460);
            html.append(gizmoEl.getHtml());
            if (script != null) {
                script.append(gizmoEl.getScript());
            }
        } else {
            String notificationsProp;
            if (gizmoType == GizmoType.DASHBOARD || gizmoType == GizmoType.DASHBOARD_CONTENTONLY) {
                if (gizmoEl instanceof GroupGizmo) {
                    gizmoEl.getElementProperties().setProperty("childgroup", "Y");
                }
                gizmoEl.setPreviewJS("groupGizmo.grid.preview(this,'" + gizmoEl.getGizmoDefId() + "')");
            } else {
                gizmoEl.setPreviewJS("top.modernLayout.gizmos.preview('" + gizmoEl.getGizmoDefId() + "','" + gizmoEl.getElementid() + "')");
            }
            String uniId = gizmoEl.getElementid();
            boolean group = gizmoEl instanceof GroupGizmo;
            String groupclass = "";
            if (group) {
                if (gizmoEl.getElementProperties().getProperty("dashboard", "N").equalsIgnoreCase("N")) {
                    // empty if block
                }
                if (gizmoType == GizmoType.TOPBAR) {
                    groupclass = "groupgizmo_container_small";
                } else if (gizmoEl.getElementProperties().getProperty("dashboard", "N").equalsIgnoreCase("Y")) {
                    switch (gizmoStyle) {
                        case LARGE: 
                        case LARGETEXT: {
                            groupclass = "groupgizmo_container_large";
                            break;
                        }
                        case SMALL: {
                            groupclass = "groupgizmo_container_small";
                            break;
                        }
                        case MEDIUM: {
                            groupclass = "groupgizmo_container_medium";
                            break;
                        }
                        case MEDIUMTEXT: {
                            groupclass = "groupgizmo_container_medium";
                            break;
                        }
                        default: {
                            groupclass = "";
                            break;
                        }
                    }
                } else {
                    gizmoEl.getElementProperties().setProperty("inlinegroup", "Y");
                    groupclass = "groupgizmo_container_inline";
                }
            }
            String gizmodefid = gizmoEl.getGizmoDefId();
            int size_x = -1;
            int size_y = -1;
            int containerwidth = -1;
            if (gizmoType == GizmoType.DASHBOARD || gizmoType == GizmoType.DASHBOARD_CONTENTONLY) {
                try {
                    size_x = Integer.parseInt(gizmoEl.getElementProperties().getPropertyList("gizmoprops").getProperty("size_x", "-1"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    size_y = Integer.parseInt(gizmoEl.getElementProperties().getPropertyList("gizmoprops").getProperty("size_y", "-1"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    double d = Double.parseDouble(gizmoEl.getElementProperties().getPropertyList("gizmoprops").getProperty("containerwidth", "-1"));
                    containerwidth = (int)Math.floor(d);
                }
                catch (Exception d) {
                    // empty catch block
                }
                gizmoEl.setWidth(containerwidth > -1 ? containerwidth : (size_x > -1 ? size_x * 100 : 100));
                gizmoEl.setHeight(size_y > -1 ? size_y * 100 : 100);
                if (gizmoEl.getElementProperties().getPropertyList("gizmoprops") == null) {
                    gizmoEl.getElementProperties().setProperty("gizmoprops", new PropertyList());
                }
                gizmoEl.getElementProperties().getPropertyList("gizmoprops").setProperty("width", "" + gizmoEl.getWidth());
                gizmoEl.getElementProperties().getPropertyList("gizmoprops").setProperty("height", "" + gizmoEl.getHeight());
            }
            if (gizmoType.isSortable()) {
                if (gizmoType == GizmoType.DASHBOARD) {
                    int row = -1;
                    int column = -1;
                    try {
                        row = Integer.parseInt(gizmoEl.getElementProperties().getPropertyList("gizmoprops").getProperty("row", "-1"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    try {
                        column = Integer.parseInt(gizmoEl.getElementProperties().getPropertyList("gizmoprops").getProperty("column", "-1"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    html.append("<li data-row=\"").append(row).append("\" data-col=\"").append(column).append("\" data-sizex=\"").append(size_x > -1 ? size_x : 1).append("\" data-sizey=\"").append(size_y > -1 ? size_y : 1).append("\" style=\"overflow:hidden;\" class=\"gs-w\" ");
                    html.append("id=\"ws_").append(gizmoEl.getElementid()).append("\" ");
                    html.append("collectionid=\"").append(collectionid).append("\" ");
                    html.append("elementid=\"").append(gizmoEl.getElementid()).append("\" ");
                    html.append("gizmotype=\"").append(gizmoEl.getElementType()).append("\" ");
                    html.append("gizmodefid=\"").append(gizmoEl.getGizmoDefId()).append("\" ");
                    html.append("gizmostyle=\"").append((Object)gizmoEl.getGizmoStyle()).append("\" ");
                    if (gizmoEl.getElementProperties().getProperty("_dynamicprops", "N").equalsIgnoreCase("Y")) {
                        html.append("_dynamicprops=\"Y\" ");
                        script.append("groupGizmo.dynamic['").append(gizmoEl.getElementid()).append("'] = sapphire.util.propertyList.create(").append(gizmoEl.getElementProperties().toJSONString(false, false)).append(");");
                    }
                    html.append("refreshonresize=\"").append(gizmoEl.getElementProperties().getPropertyList("gizmoprops").getProperty("refreshonresize").toUpperCase()).append("\" ");
                    html.append(">");
                    html.append("<header class=\"gridster_handle\">").append(tp == null ? gizmoEl.getTitle() : tp.translate(gizmoEl.getTitle())).append("</header>");
                    html.append("<div class=\"ws_outer\">");
                    html.append("<div id=\"ws_contents_").append(gizmoEl.getElementid()).append("\" class=\"ws_inner").append(gizmoStyle == BaseGizmo.GizmoStyle.LARGE || gizmoStyle == BaseGizmo.GizmoStyle.LARGETEXT ? " ws_icon" : "").append("\">");
                } else {
                    String t = gizmoEl.getHelpText();
                    html.append("<div ").append(t.length() > 0 ? "title=\"" + t + "\" " : "").append("class=\"ui-state-default ws_sortable_item_").append(gizmoType == GizmoType.TOPBAR || gizmoType == GizmoType.TOPBAR_CONTENTONLY ? "t" : (gizmoType == GizmoType.SIDEBAR || gizmoType == GizmoType.SIDEBAR_CONTENTONLY ? "s" : "f")).append("\" collectionid=\"").append(collectionid).append("\" gizmoid=\"").append(uniId).append("\" gizmodefid=\"").append(gizmoEl.getGizmoDefId()).append("\"").append(gizmoType == GizmoType.TOPBAR || gizmoType == GizmoType.TOPBAR_CONTENTONLY ? " onclick=\"top.modernLayout.gizmos.trickleClick(this,event,'e_" + uniId + "');\"" : "").append(">");
                }
            }
            if (gizmoType.renderContainer) {
                html.append("<div class=\"ws_gizmotarget ws_gizmotarget").append(gizmoType == GizmoType.TOPBAR || gizmoType == GizmoType.TOPBAR_CONTENTONLY ? "_top" : (gizmoType == GizmoType.SIDEBAR || gizmoType == GizmoType.SIDEBAR_CONTENTONLY ? "_side" : "_full")).append(groupclass.length() > 0 ? " " + groupclass : "").append("\" collectionid=\"").append(collectionid).append("\" gizmoid=\"").append(uniId).append("\" gizmodefid=\"").append(gizmodefid).append("\" id=\"ws_gizmotarget_").append(uniId).append("\">");
            }
            boolean bypassscript = false;
            if (gizmoType == GizmoType.TOPBAR || gizmoType == GizmoType.TOPBAR_CONTENTONLY) {
                gizmoEl.setGizmoStyle(BaseGizmo.GizmoStyle.SMALL);
                html.append(gizmoEl.getIconHtml());
            } else if (gizmoType == GizmoType.SIDEBAR_CONTENTONLY || gizmoType == GizmoType.SIDEBAR) {
                gizmoEl.setGizmoStyle(gizmoStyle);
                html.append(gizmoEl.getIconHtml());
            } else if (gizmoType == GizmoType.DASHBOARD || gizmoType == GizmoType.DASHBOARD_CONTENTONLY) {
                gizmoEl.setGizmoStyle(gizmoStyle);
                String htmltemp = gizmoStyle == BaseGizmo.GizmoStyle.FULL ? gizmoEl.getHtml() : gizmoEl.getIconHtml();
                StringBuffer missingBuffer = GizmoTargetAjaxManager.getMissingParameters(gizmoEl);
                if (missingBuffer.length() > 0) {
                    if (gizmoStyle == BaseGizmo.GizmoStyle.FULL) {
                        html.append("<div class=\"dashboard_gizmoerror gizmoerror_large\">");
                        html.append("Gizmo ").append(gizmoEl.getGizmoDefId()).append(" could not be loaded due to it requiring the following parameters:<br>");
                        html.append("<blockquote>").append(missingBuffer).append("</blockquote>");
                        html.append("<br>Try adding a parameter gizmo to the dashboard.");
                        html.append("</div>");
                    } else {
                        html.append("<div class=\"dashboard_gizmoerror gizmoerror_small\" title=\"").append(gizmoEl.getGizmoDefId()).append(" could not be loaded due to it requiring the following parameters: ").append(missingBuffer).append("\">");
                        html.append("<img style=\"width:48px;height:48px;\" src=\"").append("WEB-CORE/images/svg/gizmo-c.svg").append("\">");
                        html.append("<div>Parameter Error</div>");
                        html.append("</div>");
                    }
                    bypassscript = true;
                } else {
                    html.append(htmltemp);
                }
            } else {
                gizmoEl.setGizmoStyle(gizmoStyle);
                if (gizmoStyle == BaseGizmo.GizmoStyle.FULL) {
                    html.append(gizmoEl.getHtml());
                } else {
                    html.append(gizmoEl.getIconHtml());
                }
            }
            PropertyList gizmoProps = gizmoEl.getElementProperties().getPropertyList("gizmoprops");
            String string = notificationsProp = gizmoProps != null ? gizmoProps.getProperty("notifications") : "";
            if (!bypassscript) {
                if (notificationsProp.length() > 0) {
                    PropertyList notificationProps = new PropertyList();
                    try {
                        notificationProps.setPropertyList(notificationsProp);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (notificationProps.getCollection("notifications") != null && notificationProps.getCollection("notifications").size() > 0) {
                        String s = gizmoEl.getScript();
                        if (s != null) {
                            script.append(s);
                        }
                        script.append("sapphire.notification.requestElementNotifications(").append("'").append(uniId).append("',").append("'").append(gizmodefid).append("',").append("'").append(gizmoEl.getElementType()).append("',").append("'").append(gizmoProps != null && gizmoProps.getProperty("flashonupdate").equals("Y") ? "Y" : "N").append("'").append(");").append(s);
                    } else {
                        String s = gizmoEl.getScript();
                        if (s != null) {
                            script.append(s);
                        }
                    }
                } else {
                    String s = gizmoEl.getScript();
                    if (s != null) {
                        script.append(s);
                    }
                }
                if (gizmoType == GizmoType.DASHBOARD_CONTENTONLY && gizmoProps.getProperty("refreshevery").length() > 0) {
                    int timeout = -1;
                    try {
                        timeout = Integer.parseInt(gizmoProps.getProperty("refreshevery", "-1"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (timeout > 0) {
                        script.append("groupGizmo.grid.setRefreshTimeout('").append(collectionid).append("', ").append(timeout * 1000).append(");");
                    }
                }
            }
            if (gizmoType.renderContainer) {
                html.append("</div>");
            }
            if (gizmoType != GizmoType.TOPBAR_CONTENTONLY && gizmoType != GizmoType.SIDEBAR_CONTENTONLY && gizmoType != GizmoType.DASHBOARD_CONTENTONLY) {
                html.append("<div class=\"ws_gizmotarget_cover").append(gizmoType == GizmoType.DASHBOARD || gizmoType == GizmoType.DASHBOARD_CONTENTONLY ? " gridster_handle" : "").append("\"></div>");
            }
            if (gizmoType == GizmoType.DASHBOARD) {
                html.append("<div class=\"ws_gizmotarget_trash").append("\" onclick=\"groupGizmo.grid.remove(this)\"></div>");
            }
            if (gizmoType.isSortable()) {
                if (gizmoType == GizmoType.DASHBOARD) {
                    html.append("</div>");
                    html.append("</div>");
                    html.append("</li>");
                } else {
                    html.append("</div>");
                }
            }
            if (gizmoEl.getTimeout() > 0 && gizmoEl.getCount() > -1 && gizmoType != GizmoType.PREVIEW) {
                script.append("top.modernLayout.gizmos.registerRefreshCount('").append(gizmodefid).append("','").append(uniId).append("', ").append(gizmoEl.getTimeout()).append(");");
            }
        }
        return html.toString();
    }

    public static String getGizmo(String collectionid, String gizmodefid, StringBuffer script, BaseGizmo.GizmoStyle gizmoStyle, GizmoType gizmoType, String connectionId, PageContext pageContext) {
        return GizmoTargetAjaxManager.getGizmo(collectionid, gizmodefid, script, gizmoStyle, gizmoType, null, null, connectionId, pageContext);
    }

    public static String getGizmo(String collectionid, String gizmodefid, StringBuffer script, BaseGizmo.GizmoStyle gizmoStyle, GizmoType gizmoType, PropertyList parameters, PropertyList customProps, String connectionId, PageContext pageContext) {
        return GizmoTargetAjaxManager.getGizmo(collectionid, gizmodefid, script, gizmoStyle, gizmoType, parameters, customProps, connectionId, pageContext, null);
    }

    public static String getGizmo(String collectionid, String gizmodefid, StringBuffer script, BaseGizmo.GizmoStyle gizmoStyle, GizmoType gizmoType, PropertyList parameters, PropertyList customProps, String connectionId, PageContext pageContext, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        GizmoTargetAjaxManager.getGizmo(collectionid, gizmodefid, html, script, gizmoStyle, gizmoType, parameters, customProps, connectionId, pageContext, tp);
        return html.toString();
    }

    public static BaseGizmo getGizmo(String collectionid, String gizmodefid, StringBuffer html, StringBuffer script, BaseGizmo.GizmoStyle gizmoStyle, GizmoType gizmoType, String connectionId, PageContext pageContext) {
        return GizmoTargetAjaxManager.getGizmo(collectionid, gizmodefid, html, script, gizmoStyle, gizmoType, null, null, connectionId, pageContext);
    }

    public static BaseGizmo getGizmo(String collectionid, String gizmodefid, StringBuffer html, StringBuffer script, BaseGizmo.GizmoStyle gizmoStyle, GizmoType gizmoType, PropertyList parameters, PropertyList customGizmoProps, String connectionId, PageContext pageContext) {
        return GizmoTargetAjaxManager.getGizmo(collectionid, gizmodefid, html, script, gizmoStyle, gizmoType, parameters, customGizmoProps, connectionId, null);
    }

    public static BaseGizmo getGizmo(String collectionid, String gizmodefid, StringBuffer html, StringBuffer script, BaseGizmo.GizmoStyle gizmoStyle, GizmoType gizmoType, PropertyList parameters, PropertyList customGizmoProps, String connectionId, PageContext pageContext, TranslationProcessor tp) {
        return GizmoTargetAjaxManager.getGizmo(collectionid, gizmodefid, html, script, gizmoStyle, gizmoType, parameters, customGizmoProps, null, connectionId, pageContext, tp);
    }

    public static BaseGizmo getGizmo(String collectionid, String gizmodefid, StringBuffer html, StringBuffer script, BaseGizmo.GizmoStyle gizmoStyle, GizmoType gizmoType, PropertyList parameters, PropertyList customGizmoProps, PropertyList dynamicProperties, String connectionId, PageContext pageContext, TranslationProcessor tp) {
        sapphire.pageelements.BaseGizmo gizmoEl = null;
        if (gizmodefid.length() > 0 && gizmodefid.length() > 0) {
            String id = StringUtil.replaceAll(collectionid, " ", "_");
            try {
                gizmoEl = BaseGizmo.getInstance(pageContext, gizmodefid, parameters, true, false);
                if (gizmoEl != null) {
                    PropertyList gp;
                    gizmoEl.setPageContext(pageContext);
                    gizmoEl.setRequest((HttpServletRequest)pageContext.getRequest());
                    gizmoEl.setConnectionId(connectionId);
                    String uniId = id + (gizmoType == GizmoType.SIDEBAR || gizmoType == GizmoType.SIDEBAR_CONTENTONLY ? "s_" : (gizmoType == GizmoType.TOPBAR || gizmoType == GizmoType.TOPBAR_CONTENTONLY ? "t_" : "x_")) + ((int)(Math.random() * 100.0) + 1);
                    gizmoEl.setElementid(uniId);
                    gizmoEl.setColor(gizmoType == GizmoType.SIDEBAR || gizmoType == GizmoType.SIDEBAR_CONTENTONLY ? Color.BLACK : (gizmoType == GizmoType.DASHBOARD || gizmoType == GizmoType.DASHBOARD_CONTENTONLY ? Color.BLACK : Color.WHITE));
                    if (dynamicProperties != null && (gp = gizmoEl.getElementProperties()) != null) {
                        gp.setPropertyList(dynamicProperties.toXMLString(), true);
                    }
                    if (customGizmoProps != null && (gp = gizmoEl.getElementProperties().getPropertyList("gizmoprops")) != null) {
                        gp.setPropertyList(customGizmoProps.toXMLString(), true);
                    }
                    gizmoEl.setBaseProperties();
                    html.append(GizmoTargetAjaxManager.renderGizmo(collectionid, gizmoEl, script, gizmoType, gizmoStyle, tp));
                } else {
                    Trace.log("Gizmo " + gizmodefid + " not found or failed to initialize");
                }
            }
            catch (Exception e) {
                Trace.logError("Faied to get gizmo " + gizmodefid + ". Reason: " + e.getMessage(), e);
            }
        }
        return gizmoEl;
    }

    public static enum Mode {
        ADD,
        MOVE,
        DELETE,
        REFRESH,
        REFRESHCOUNT,
        PREVIEW,
        MENU,
        GROUP,
        TOPBAR,
        RESETOVERRIDES,
        DASHBOARDSAVE,
        CREATE,
        GUIMODE;

    }

    public static enum GizmoType {
        SIDEBAR(true, true),
        SIDEBAR_CONTENTONLY(false, false),
        TOPBAR(true, true),
        TOPBAR_CONTENTONLY(false, false),
        PREVIEW(false, true),
        DASHBOARD(true, true),
        DASHBOARD_CONTENTONLY(false, false);

        private boolean renderSortable = true;
        private boolean renderContainer = true;

        private GizmoType(boolean rS, boolean rC) {
            this.renderContainer = rC;
            this.renderSortable = rS;
        }

        public boolean isSortable() {
            return this.renderSortable;
        }

        public boolean isInContainer() {
            return this.renderContainer;
        }
    }
}


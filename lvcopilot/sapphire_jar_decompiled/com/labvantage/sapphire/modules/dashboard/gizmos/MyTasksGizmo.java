/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.http.HttpSender;
import java.util.ArrayList;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.util.DataSet;

public class MyTasksGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String URL_PROPERTY = "url";
    private static final String URL_END = "rc?command=ajax&ajaxclass=com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequestAjaxHandler";
    private static final String COMMANDREQUEST = "{\"command\":\"s\",\"commandhandler\":\"com.labvantage.sapphire.modules.workflow.gwt.server.WorkflowManagerRequest\",\"data\":{\"search\":\"{\\\"searchid\\\":\\\"taskexecution\\\",\\\"linkid\\\":\\\"taskexecution\\\", \\\"title\\\":\\\"My Tasks\\\", \\\"type\\\":\\\"tasks\\\", \\\"LV_TaskDef\\\":{\\\"querywhere\\\":\\\"taskdef.taskdefid = workflowdeftask.taskdefid AND \\\\ntaskdef.taskdefversionid = workflowdeftask.taskdefversionid AND \\\\ntaskdef.taskdefvariantid = workflowdeftask.taskdefvariantid AND \\\\nworkflowdeftask.starttaskflag = 'Y' AND  workflowdeftask.startableflag = 'Y' AND  \\\\n( workflowdeftask.sysuserid = '[currentuser]' OR workflowdeftask.sysuserid IS NULL )\\\", \\\"queryfrom\\\":\\\"taskdef, workflowdeftask\\\", \\\"include\\\":\\\"Y\\\"}, \\\"TaskQueueItem\\\":{\\\"querywhere\\\":\\\"taskqueue.taskdefid = taskdef.taskdefid AND \\\\ntaskqueue.taskdefversionid = taskdef.taskdefversionid AND \\\\ntaskqueue.taskdefvariantid = taskdef.taskdefvariantid AND \\\\nqueuestatus IN ( 'W' , 'S' ) AND  ( taskqueue.assignedanalyst = '[currentuser]' OR taskqueue.assignedanalyst IS NULL )\\\", \\\"queryfrom\\\":\\\"taskqueue,taskdef\\\", \\\"include\\\":\\\"Y\\\"}, \\\"LV_TaskExec\\\":{\\\"querywhere\\\":\\\"taskexec.execstatus='P' AND \\\\n( taskexec.assignedanalyst = '[currentuser]' OR taskexec.assignedanalyst IS NULL )\\\", \\\"queryfrom\\\":\\\"taskexec\\\", \\\"include\\\":\\\"Y\\\"}, \\\"ignorenoqueueitemtasks\\\":\\\"N\\\"}\", \"search_type\":\"PropertyList\", \"sortby\":\"\", \"sortbydir\":\"ASC\", \"groupby\":\"\", \"groupbysortdir\":\"A\", \"queryparams\":\"\", \"postprocessing\":\"T\", \"applyuserrestrictions\":\"Y\", \"wfexecstatus\":\"A\", \"wfeexecstatus\":\"A\",  \"__hostwebpageid\":\"DashboardTasks\"}}";
    private DataSet taskData = null;

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        return true;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionid()) : this.getTranslationProcessor();
        if (this.element != null) {
            String url = this.element.getProperty("mytaskspage") + "&scrolling=N&forceconfig=Y" + (this.element.getProperty("completepage").length() > 0 ? "&completepage=" + (this.element.getProperty("completepage").equals("(self)") ? this.element.getProperty("webpageid") : this.element.getProperty("completepage")) : LABVANTAGE_CVS_ID) + (this.element.getProperty("cancelpage").length() > 0 ? "&cancelpage=" + (this.element.getProperty("cancelpage").equals("(self)") ? this.element.getProperty("webpageid") : this.element.getProperty("cancelpage")) : LABVANTAGE_CVS_ID) + (this.element.getProperty("showdisplayoptions").length() > 0 ? "&showdisplayoptions=" + this.element.getProperty("showdisplayoptions") : LABVANTAGE_CVS_ID) + (this.element.getProperty("displaystyle").length() > 0 ? "&displaystyle=" + this.element.getProperty("displaystyle") : LABVANTAGE_CVS_ID) + (this.element.getProperty("groupby").length() > 0 ? "&groupby=" + this.element.getProperty("groupby") : LABVANTAGE_CVS_ID) + (this.element.getProperty("confirmopenonclick").length() > 0 ? "&confirmopenonclick=" + this.element.getProperty("confirmopenonclick") : LABVANTAGE_CVS_ID);
            html.append(this.opencloseURLResultFrame(url));
        } else {
            html.append("<font size=2>").append(translationProcessor.translate("No element data found.")).append("</font>");
        }
        return html.toString();
    }

    private String opencloseURLResultFrame(String sURL) {
        StringBuffer sb = new StringBuffer(LABVANTAGE_CVS_ID);
        sb.append("<iframe frameborder=0 src=\"").append(sURL).append("\" style=\"width: 100%; height: 100%;\" name=\"").append(this.elementid).append("_iframe\" id=\"").append(this.elementid).append("_iframe\"></iframe>");
        return sb.toString();
    }

    @Override
    public String getScript() {
        return LABVANTAGE_CVS_ID;
    }

    private DataSet getMyTasksData() {
        if (this.taskData == null) {
            DataSet ds = null;
            SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
            if (sc != null) {
                String user = sc.getSysuserId();
                try {
                    JSONObject commandrequest = new JSONObject(COMMANDREQUEST);
                    HttpSender o = new HttpSender();
                    String webapp = this.request.getContextPath();
                    if (webapp.startsWith("/")) {
                        webapp = webapp.substring(1);
                    }
                    String url = (this.request.isSecure() ? "https" : "http") + "://" + this.request.getLocalAddr() + ":" + this.request.getServerPort() + "/" + webapp + "/" + URL_END;
                    url = url + "&connectionid=" + this.getConnectionId();
                    o.doConnect(url, "POST", false);
                    o.doSend("commandrequest=" + commandrequest.toString());
                    String res = o.getResponse(false, false);
                    if (res.length() > 0) {
                        JSONObject commandresponse = new JSONObject(res);
                        if (commandresponse != null && commandresponse.has("responsedata") && commandresponse.getJSONObject("responsedata").has("searchresults")) {
                            ds = new DataSet(new JSONObject(commandresponse.getJSONObject("responsedata").getString("searchresults")));
                        } else {
                            this.logger.warn("Unexpected response from Workflow Command.");
                        }
                    } else {
                        this.logger.warn("Failed to run Ajax Command.");
                    }
                }
                catch (Exception e) {
                    this.logger.error(e.getMessage());
                }
                this.taskData = ds;
                return ds;
            }
            return null;
        }
        return this.taskData;
    }

    @Override
    public String getTitle() {
        DataSet ds = this.getMyTasksData();
        int tcount = 0;
        int wcount = 0;
        if (ds != null) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String wid = ds.getValue(i, "workflowdefid");
                String wv = ds.getValue(i, "workflowdefversionid");
                String wvar = ds.getValue(i, "workflowdefvariantid");
                String k = wid + wv + wvar;
                if (list.size() != 0 && list.contains(k)) continue;
                list.add(k);
            }
            wcount = list.size();
            tcount = this.getCount();
        }
        return tcount + " Tasks from " + wcount + " Workflows";
    }

    @Override
    public String getIcon() {
        return this.getImage("My Tasks", this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return "FlatWhiteUser";
    }

    @Override
    public String getURL() {
        return "rc?command=page&page=UserTasks";
    }

    @Override
    public int getCount() {
        DataSet ds = this.getMyTasksData();
        int count = 0;
        if (ds != null) {
            count = ds.getRowCount();
        }
        return count;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.admin.system.automation.LAMStats;
import com.labvantage.sapphire.admin.system.automation.Server;
import com.labvantage.sapphire.services.AutomationService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetLAMStats
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String databaseid = this.getConnectionProcessor().getSapphireConnection().getDatabaseId();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        TranslationProcessor tp = this.getTranslationProcessor();
        try {
            AutomationService.broadcastServerCommand(databaseid, "RequestLamStats", "");
            List<Server> servers = AutomationService.getFullServerList(databaseid);
            SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy, hh:mm:ss");
            StringBuffer out = new StringBuffer();
            out.append("<table>");
            out.append("<tr>");
            for (Server server : servers) {
                if (server.thisServer) {
                    out.append("<td>" + server.hostid + " " + tp.translate("(Local Time:") + " " + format.format(Calendar.getInstance().getTime()) + ")</td>");
                    continue;
                }
                out.append("<td>" + server.hostid + "</td>");
            }
            out.append("</tr>");
            out.append("<tr>");
            for (Server server : servers) {
                try {
                    if (server.thisServer) {
                        LAM lam = AutomationService.getLAM(databaseid);
                        LAMStats stats = lam.getStats();
                        out.append("<td>" + GetLAMStats.statsToHTML(stats, tp) + "</td>");
                        continue;
                    }
                    String html = AutomationService.getLamStats(databaseid, server.hostid);
                    out.append("<td>" + (html == null ? "Waiting for a response..." : html) + "</td>");
                }
                catch (Exception e) {
                    out.append("<td> ERROR " + e.getMessage() + "</td>");
                }
            }
            out.append("</tr>");
            out.append("</table>");
            ajaxResponse.addCallbackArgument("html", out.toString());
            ajaxResponse.print();
        }
        catch (SapphireException e) {
            ajaxResponse.setError(tp.translate("Failed to load stats"), e);
        }
    }

    public static String statsToHTML(LAMStats stats, TranslationProcessor tp) {
        StringBuffer out = new StringBuffer();
        boolean isTP = tp != null;
        out.append("<table cellspacing=\"0\" border=\"1\">");
        out.append("<tr><td colspan=\"2\" style=\"color:red\">" + (stats.isPrimaryAutomationServer ? (isTP ? tp.translate("PRIMARY AUTOMATION SERVER") : "PRIMARY AUTOMATION SERVER") : (!stats.isAutomationServer ? "AUTOMATION DISABLED" : "&nbsp;")) + "</td></tr>");
        out.append("<tr><td class=\"fieldtitle\">").append(isTP ? tp.translate("ToDoList Entries Processed") : "ToDoList Entries Processed").append("</td><td width=\"60\">" + stats.totalTasksCompleted + "</td></tr>");
        out.append("<tr><td class=\"fieldtitle\">").append(isTP ? tp.translate("Active Thread Count") : "Active Thread Count").append("</td><td>" + stats.activeThreads + "</td></tr>");
        out.append("<tr><td class=\"fieldtitle\">").append(isTP ? tp.translate("Current Pool Size") : "Current Pool Size").append("</td><td>" + stats.poolSize + "</td></tr>");
        out.append("</table>");
        out.append("<br>");
        out.append("<table cellspacing=\"0\" border=\"1\">");
        out.append("<tr class=\"fieldtitle\">");
        out.append("<td>Action</td>");
        out.append("<td>Count</td>");
        out.append("<td>/s (1min)</td>");
        out.append("<td>/s (5min)</td>");
        out.append("<td>/s (15min)</td>");
        out.append("<td>Mean (s)</td>");
        out.append("<td>St Dev (s)</td>");
        out.append("<td>95% (s)</td>");
        out.append("</tr>");
        for (LAMStats.Timer t : stats.timers) {
            out.append("<tr>").append("<td>").append(t.name).append("</td>");
            out.append("<td>" + t.count + "</td><td>" + t.oneRate + "</td><td>" + t.fiveRate + "</td><td>" + t.fifteenRate + "</td><td>" + t.mean + "</td><td>" + t.stdev + "</td><td>" + t.nintyfive + "</td>");
            out.append("</tr>");
        }
        out.append("</table>");
        out.append("<br>");
        out.append("<table cellspacing=\"0\" border=\"1\">");
        out.append("<tr class=\"fieldtitle\">");
        out.append("<td width=\"120\">").append(isTP ? tp.translate("Poller") : "Poller").append("</td>");
        out.append("<td width=\"60\">").append(isTP ? tp.translate("Interval") : "Interval").append("</td>");
        out.append("<td width=\"60\">").append(isTP ? tp.translate("Status") : "Status").append("</td>");
        out.append("<td >").append(isTP ? tp.translate("Last Execution Date") : "Last Execution Date").append("</td>");
        out.append("<td width=\"60\">").append(isTP ? tp.translate("Run Count") : "Run Count").append("</td>");
        out.append("<td >").append(isTP ? tp.translate("Last Error") : "Last Error").append("</td>");
        out.append("</tr>");
        SimpleDateFormat format = new SimpleDateFormat("d MMM, h:mm:ss a");
        for (LAMStats.Poller p : stats.pollers) {
            out.append("<tr><td>").append(isTP ? tp.translate(p.name) : p.name).append("</td>");
            out.append("<td>" + p.interval + "s</td>");
            if (p.status.equals("N/A")) {
                out.append("<td colspan=\"2\">").append(isTP ? tp.translate("(Primary Automation Only)") : "(Primary Automation Only)").append("</td>");
            } else if (p.status.equals("Skipped")) {
                out.append("<td colspan=\"2\">").append(isTP ? tp.translate("(Skipped - No Automation)") : "(Skipped - No Automation)").append("</td>");
            } else {
                out.append("<td>").append(isTP ? tp.translate(p.status) : p.status).append("</td>");
                out.append("<td>" + (p.lastRun == null ? "&nbsp;" : format.format(p.lastRun.getTime())) + "</td>");
            }
            out.append("<td>" + p.runCount + "</td>");
            out.append("<td>" + (p.lastError != null && p.lastError.length() > 0 ? p.lastError : "&nbsp;") + "</td>");
            out.append("</tr>");
        }
        out.append("</table>");
        return out.toString();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.util.diff.DiffConnectionDetails;
import com.labvantage.sapphire.util.diff.DiffProgress;
import com.labvantage.sapphire.util.diff.DiffUtil;
import com.labvantage.sapphire.util.diff.SDIDiff;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;

public class AjaxDiff
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String DIFFSESSION = "AjaxDiffSession";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        String dothis;
        HttpSession session = request.getSession();
        DiffProgress diffProgress = (DiffProgress)session.getAttribute(DIFFSESSION);
        if (diffProgress == null) {
            diffProgress = new DiffProgress();
            session.setAttribute(DIFFSESSION, (Object)diffProgress);
        }
        if ("progress".equals(dothis = (ajaxResponse = new AjaxResponse(request, response, "DiffResults")).getRequestParameter("dothis"))) {
            String currentProgress = diffProgress.getProgress();
            ajaxResponse.addCallbackArgument("html", currentProgress);
        } else if ("cancel".equals(dothis)) {
            diffProgress.setCancelled(true);
            diffProgress.setProgress("");
            diffProgress.setPercentComplete(-1);
        } else {
            String sdcid = ajaxResponse.getRequestParameter("sdcid");
            String sdifilter = ajaxResponse.getRequestParameter("sdifilter");
            boolean includeAuditFields = "Y".equals(ajaxResponse.getRequestParameter("includeauditfields"));
            boolean includedetails = "Y".equals(ajaxResponse.getRequestParameter("includedetails"));
            DiffConnectionDetails source = new DiffConnectionDetails();
            source.setAjaxResponse(ajaxResponse, "source");
            DiffConnectionDetails target = new DiffConnectionDetails();
            target.setAjaxResponse(ajaxResponse, "target");
            diffProgress.setCancelled(false);
            try {
                StringBuffer output = new StringBuffer();
                String connectionid = this.getConnectionId();
                diffProgress.setPercentComplete(-1);
                diffProgress.setProgress("Loading " + sdcid + " data from source");
                SDIData sourceSDIData = DiffUtil.getSDIData(connectionid, sdcid, sdifilter, source, includedetails);
                if (!diffProgress.isCancelled()) {
                    diffProgress.setProgress("Loading " + sdcid + " data from target");
                    SDIData targetSDIData = DiffUtil.getSDIData(connectionid, sdcid, sdifilter, target, includedetails);
                    if (!diffProgress.isCancelled()) {
                        SDIDiff diff = new SDIDiff(this.getConnectionId(), sdcid, sourceSDIData, targetSDIData, diffProgress, includeAuditFields);
                        try {
                            diffProgress.setProgress("Processing " + sdcid);
                            DataSet results = diff.diff();
                            output.append("<div style=\"margin-left: 5px\">");
                            if (results.size() == 0) {
                                output.append("The source and target are identical");
                            } else {
                                output.append(DiffUtil.formatResultsHTML(sdcid, results));
                            }
                            output.append("</div>");
                        }
                        catch (SapphireException e) {
                            output.append("----------<br>");
                            output.append(e.getMessage());
                        }
                        catch (Exception e) {
                            output.append("An error has occurred<br>");
                            StringWriter writer = new StringWriter();
                            e.printStackTrace(new PrintWriter(writer));
                            output.append(writer.toString().replaceAll("\n", "<br/>"));
                        }
                    } else {
                        output = new StringBuffer("Execution Cancelled");
                    }
                } else {
                    output = new StringBuffer("Execution Cancelled");
                }
                ajaxResponse.addCallbackArgument("html", output.toString());
            }
            catch (Exception e) {
                ajaxResponse.setError("Failed to perform diff. Exception: " + e.getMessage(), e);
            }
            diffProgress.setProgress("");
            diffProgress.setPercentComplete(-1);
        }
        ajaxResponse.print();
    }
}


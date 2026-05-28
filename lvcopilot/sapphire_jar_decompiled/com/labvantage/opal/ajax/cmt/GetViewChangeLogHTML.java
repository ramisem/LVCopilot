/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.cmt;

import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.cmt.Snapshot;

public class GetViewChangeLogHTML
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String html = "";
        String changelogid = ajaxResponse.getRequestParameter("changelogid", "");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select changelogid, changelogstatus, originalsnapshot, modifiedsnapshot, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3 from changelog where changelogid=?", (Object[])new String[]{changelogid}, true);
        if (ds != null && ds.size() > 0) {
            SDISnapshot sourceSnapshot = null;
            SDISnapshot targetSnapshot = null;
            try {
                String originalsnapshot = ds.getClob(0, "originalsnapshot", "");
                sourceSnapshot = (SDISnapshot)Snapshot.fromXML(originalsnapshot, this.getConnectionId());
                String modifiedsnapshot = ds.getClob(0, "modifiedsnapshot", "");
                if (modifiedsnapshot.length() == 0) {
                    String sdcid = ds.getString(0, "linksdcid", "");
                    String keyid1 = ds.getString(0, "linkkeyid1", "");
                    String keyid2 = ds.getString(0, "linkkeyid2", "");
                    String keyid3 = ds.getString(0, "linkkeyid3", "");
                    targetSnapshot = new SnapshotFactory(this.getConnectionId()).generateSDISnapshot(sdcid, keyid1, keyid2, keyid3);
                } else {
                    targetSnapshot = (SDISnapshot)Snapshot.fromXML(modifiedsnapshot, this.getConnectionId());
                }
            }
            catch (SapphireException e) {
                message = this.getTranslationProcessor().translate("Error accessing snapshot") + ": " + e.getMessage();
                e.printStackTrace();
            }
            if (sourceSnapshot != null && targetSnapshot != null) {
                try {
                    html = SDISnapshotViewer.getDiffHtml(this.getConnectionProcessor().getSapphireConnection(), sourceSnapshot, targetSnapshot);
                }
                catch (SapphireException e) {
                    message = "Error generating diff html";
                    e.printStackTrace();
                }
            } else {
                message = this.getTranslationProcessor().translate("Error generating snapshots");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("html", html);
        ajaxResponse.print();
    }
}


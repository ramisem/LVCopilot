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

import com.labvantage.sapphire.actions.cmt.UndoCheckOutSDI;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ValidateUndoCheckOutSDI
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        String confirm = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String operation = ajaxResponse.getRequestParameter("operation", "validate");
        if ("validate".equals(operation)) {
            String sdcid = ajaxResponse.getRequestParameter("sdcid");
            String keyid1 = ajaxResponse.getRequestParameter("keyid1");
            String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
            String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
            String propertytreenodeid = ajaxResponse.getRequestParameter("propertytreenodeid", "").trim();
            String changelogid = ajaxResponse.getRequestParameter("changelogid", "").trim();
            try {
                String sql;
                DataSet ds = null;
                if (changelogid.length() > 0) {
                    sql = "select c.changelogid, c.linksdcid, c.linkkeyid1, c.linkkeyid2, c.linkkeyid3, c.propertytreenodeid, c.originalsnapshot from changelog c where c.changelogstatus = 'Checked Out' and c.changelogid = ?";
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{changelogid}, true);
                } else if ("PropertyTree".equals(sdcid)) {
                    if (propertytreenodeid.length() == 0) {
                        throw new SapphireException("MissingInputs", "VALIDATION", this.getTranslationProcessor().translate("PropertyTree Node Id is mandatory for PropertyTree snapshots."));
                    }
                    sql = "select c.changelogid, c.linksdcid, c.linkkeyid1, c.linkkeyid2, c.linkkeyid3, c.propertytreenodeid, c.originalsnapshot from changelog c where c.linksdcid = 'PropertyTree' and c.linkkeyid1 = ? and c.propertytreenodeid = ? and c.changelogstatus = 'Checked Out'";
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyid1, propertytreenodeid}, true);
                } else {
                    String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                    String sql2 = "select c.changelogid, c.linksdcid, c.linkkeyid1, c.linkkeyid2, c.linkkeyid3, c.originalsnapshot from changelog c, rsetitems r where r.sdcid = c.linksdcid and r.keyid1 = c.linkkeyid1 and r.keyid2 = c.linkkeyid2 and r.keyid3 = c.linkkeyid3 and c.changelogstatus = 'Checked Out' and r.rsetid = ?";
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql2, (Object[])new String[]{rsetid}, true);
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                if (ds == null || ds.isEmpty()) {
                    message = this.getTranslationProcessor().translate("Selected item must be Checked Out");
                } else {
                    changelogid = ds.getString(0, "changelogid");
                    String originalsnapshotXML = ds.getValue(0, "originalsnapshot", "");
                    if (originalsnapshotXML.length() == 0) {
                        message = this.getTranslationProcessor().translate("Cannot Undo. SDI is recently created. Use Delete operation to delete the SDI.");
                    } else {
                        String linksdcid = ds.getString(0, "linksdcid", "");
                        String linkkeyid1 = ds.getString(0, "linkkeyid1", "");
                        String linkkeyid2 = ds.getString(0, "linkkeyid2", "");
                        String linkkeyid3 = ds.getString(0, "linkkeyid3", "");
                        String propertyTreeNodeId = ds.getString(0, "propertytreenodeid", "");
                        SDISnapshot currentSnapshot = null;
                        currentSnapshot = "PropertyTree".equals(linksdcid) ? new SnapshotFactory(this.getConnectionId()).generatePropertyTreeSnapshot(linkkeyid1, propertyTreeNodeId) : new SnapshotFactory(this.getConnectionId()).generateSDISnapshot(linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
                        String currentSnapshotXML = currentSnapshot.toXML().trim();
                        if (!currentSnapshotXML.equals(originalsnapshotXML.trim())) {
                            confirm = this.getTranslationProcessor().translate("Changes have been detected.") + "&nbsp;(<a href='#' onclick=\"sapphire.ui.dialog.open('', 'rc?command=page&page=SDISnapshotViewer&keyid1=" + changelogid + "&layoutscrolling=N',true,1024,640)\">" + this.getTranslationProcessor().translate("View changes") + "</a>)<br><br>Would you like the system to attempt to rollback to prior state?<hr>" + this.getTranslationProcessor().translate("Please ensure to review your data after this process.");
                        } else {
                            message = "";
                        }
                    }
                }
            }
            catch (SapphireException e) {
                message = this.getTranslationProcessor().translate("Error in undo check out validation") + ": " + e.getMessage();
                e.printStackTrace();
            }
            ajaxResponse.addCallbackArgument("message", message);
            ajaxResponse.addCallbackArgument("confirm", confirm);
            ajaxResponse.addCallbackArgument("sdcid", sdcid);
            ajaxResponse.addCallbackArgument("changelogid", changelogid);
        } else if ("undocheckout".equals(operation)) {
            String changelogid = ajaxResponse.getRequestParameter("changelogid");
            String changelogstatus = ajaxResponse.getRequestParameter("changelogstatus", "CheckOut Aborted");
            try {
                PropertyList props = new PropertyList();
                props.setProperty("changelogid", changelogid);
                props.setProperty("changelogstatus", changelogstatus);
                this.getActionProcessor().processActionClass(UndoCheckOutSDI.class.getName(), props);
            }
            catch (SapphireException e) {
                message = this.getTranslationProcessor().translate("Undo Checkout Error");
                message = message + "<hr>";
                message = message + this.getTranslationProcessor().translate("Reason: ") + this.getTranslationProcessor().translate(e.getMessage());
                e.printStackTrace();
            }
            ajaxResponse.addCallbackArgument("message", message);
            ajaxResponse.addCallbackArgument("confirm", "");
            ajaxResponse.addCallbackArgument("changelogid", changelogid);
        }
        ajaxResponse.print();
    }
}


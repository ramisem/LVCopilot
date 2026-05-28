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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ValidateChangeLogCompare
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String keyId1List = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid1", ""), "%3B", ";");
        message = ValidateChangeLogCompare.doValidateSelectedEntries(keyId1List, this.getConnectionId());
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private static String doValidateSelectedEntries(String keyId1List, String connectionId) {
        String message = "";
        TranslationProcessor tp = new TranslationProcessor(connectionId);
        if (keyId1List.length() > 0) {
            String[] changelogids = StringUtil.split(keyId1List, ";");
            if (changelogids.length != 2) {
                message = tp.translate("Compare operation needs two items to be selected");
                return message;
            }
            String sql = "SELECT * FROM changelog where changelogid = ? or changelogid = ? order by createdt";
            DataSet ds = new QueryProcessor(connectionId).getPreparedSqlDataSet(sql, new Object[]{changelogids[0], changelogids[1]});
            if (ds != null && ds.getRowCount() == 2) {
                String sdcid;
                if (!ds.getString(0, "linksdcid", "").equals(ds.getString(1, "linksdcid", ""))) {
                    return tp.translate("Cannot compare changelog entries corresponding to different SDCs");
                }
                if (!ds.getString(0, "linkkeyid1", "").equals(ds.getString(1, "linkkeyid1", ""))) {
                    if (!ds.getString(0, "changelogstatus").equals("Checked In") || !ds.getString(1, "changelogstatus").equals("Checked In")) {
                        return tp.translate("Cannot compare changelog entries different SDI's that are not Checked In");
                    }
                } else if (ds.getString(0, "changelogstatus").equals("Deleted") || ds.getString(1, "changelogstatus").equals("Deleted")) {
                    return tp.translate("Cannot compare changelog entry with status as Deleted");
                }
                if ((sdcid = ds.getString(0, "linksdcid")).equals("PropertyTree")) {
                    String propertytreenode1 = ds.getString(0, "propertytreenodeid", "");
                    String propertytreenode2 = ds.getString(1, "propertytreenodeid", "");
                    if (propertytreenode1.length() == 0 && propertytreenode2.length() > 0 || propertytreenode2.length() == 0 && propertytreenode1.length() > 0) {
                        return tp.translate("Both the change log entries should be for the same mode (Full/Definition/Node)");
                    }
                    if (propertytreenode1.equals("__FULL") && !propertytreenode2.equals("__FULL") || propertytreenode2.equals("__FULL") && !propertytreenode1.equals("__FULL")) {
                        return tp.translate("Both the change log entries should be for the same mode (Full/Definition/Node)");
                    }
                    if (propertytreenode1.equals("__DEFINITION") && !propertytreenode2.equals("__DEFINITION") || propertytreenode2.equals("__DEFINITION") && !propertytreenode1.equals("__DEFINITION")) {
                        return tp.translate("Both the change log entries should be for the same mode (Full/Definition/Node)");
                    }
                    if (!propertytreenode1.equals(propertytreenode2)) {
                        if (!ds.getString(0, "changelogstatus").equals("Checked In") || !ds.getString(1, "changelogstatus").equals("Checked In")) {
                            return tp.translate("Cannot compare changelog entries different nodes that are not Checked In");
                        }
                        if (ds.getClob(0, "originalsnapshot", "").length() == 0 && ds.getClob(0, "modifiedsnapshot", "").length() == 0 && ds.getClob(1, "originalsnapshot", "").length() == 0 && ds.getClob(0, "modifiedsnapshot", "").length() == 0) {
                            return tp.translate("Both pre and post images of the changelog entry are empty. Cannot compare.");
                        }
                    }
                }
            } else {
                message = tp.translate("Failed to fetch the changelog entries to compare");
            }
        } else {
            message = tp.translate("Invalid selection for the operation");
        }
        return message;
    }
}


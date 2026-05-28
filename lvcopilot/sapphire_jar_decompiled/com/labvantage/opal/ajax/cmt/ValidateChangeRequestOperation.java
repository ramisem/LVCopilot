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

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateChangeRequestOperation
extends BaseAjaxRequest {
    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String keyid1 = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid1", "").trim(), "%3B", ";");
        String operation = ajaxResponse.getRequestParameter("operation", "");
        if (keyid1.length() > 0 && operation.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select changerequestid, changerequeststatus from changerequest where changerequestid in (" + safeSQL.addIn(keyid1, ";") + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < ds.size(); ++i) {
                    block24: {
                        list.clear();
                        switch (operation) {
                            case "Accept": {
                                list.add("Initial");
                                break;
                            }
                            case "Complete": {
                                list.add("In Progress");
                                break;
                            }
                            case "Approve": {
                                list.add("Completed");
                                break;
                            }
                            case "Export": {
                                list.add("Approved");
                                break;
                            }
                            case "Cancel": {
                                list.add("Accepted");
                                list.add("In Progress");
                                list.add("Completed");
                                break;
                            }
                        }
                        String changerequeststatus = ds.getString(i, "changerequeststatus");
                        if (!list.contains(changerequeststatus)) {
                            if ("Export".equals(operation)) {
                                boolean exportRequireApproval = "Y".equals(CMTPolicy.getPolicy(this.getConnectionId(), "").getPolicyPropertyList().getPropertyListNotNull("changerequest").getProperty("requireapproval"));
                                if (!exportRequireApproval) {
                                    if (!("In Progress".equals(changerequeststatus) || "Completed".equals(changerequeststatus) || "Approved".equals(changerequeststatus))) {
                                        message = this.getTranslationProcessor().translate("Only In Progress, Completed or Approved Change Request can be exported");
                                        break;
                                    }
                                    break block24;
                                } else {
                                    message = this.getTranslationProcessor().translate("Only Approved Change Request can be exported");
                                    break;
                                }
                            }
                            message = this.getTranslationProcessor().translate("To " + operation + ", selected Change Request must be in the status of " + OpalUtil.toDelimitedString(list, ", "));
                            break;
                        }
                    }
                    if (!"Complete".equals(operation)) continue;
                    String changerequestid = ds.getString(i, "changerequestid");
                    try {
                        if (this.getQueryProcessor().getPreparedCount("select count(changelogid) from changelog where changerequestid = ? and changelogstatus = 'Checked Out'", new String[]{changerequestid}) <= 0) continue;
                        message = this.getTranslationProcessor().translate("Found Change Logs that are still Checked Out in Change Request") + " [" + changerequestid + "]";
                        break;
                    }
                    catch (SapphireException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}


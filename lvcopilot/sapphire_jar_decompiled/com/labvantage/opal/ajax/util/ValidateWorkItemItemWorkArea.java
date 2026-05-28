/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;

public class ValidateWorkItemItemWorkArea
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String paramlistid = ajaxResponse.getRequestParameter("paramlistid", "");
        String paramlistversionid = ajaxResponse.getRequestParameter("paramlistversionid", "");
        String variantid = ajaxResponse.getRequestParameter("variantid", "");
        String workitem_testingdepartmentid = ajaxResponse.getRequestParameter("testingdepartmentid", "");
        String workareadepartmentid = ajaxResponse.getRequestParameter("workareadepartmentid", "");
        String autoassignrule = ajaxResponse.getRequestParameter("autoassignrule", "");
        String autoassignanalystid = ajaxResponse.getRequestParameter("autoassignanalystid", "");
        String workitem_testinglabtype = ajaxResponse.getRequestParameter("testinglabtype", "");
        DataSet dsPL = new DataSet();
        dsPL.addColumnValues("paramlistid", 0, paramlistid, ";");
        dsPL.addColumnValues("paramlistversionid", 0, paramlistversionid, ";");
        dsPL.addColumnValues("variantid", 0, variantid, ";");
        dsPL.addColumnValues("workareadepartmentid", 0, workareadepartmentid, ";");
        dsPL.addColumnValues("autoassignrule", 0, autoassignrule, ";");
        dsPL.addColumnValues("autoassignanalystid", 0, autoassignanalystid, ";");
        if (dsPL.getRowCount() > 0) {
            SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
            DBUtil db = new DBUtil();
            db.setConnection(sapphireConnection);
            try {
                dsPL.addColumn("versionstatus", 0);
                for (int i = 0; i < dsPL.getRowCount(); ++i) {
                    String version = dsPL.getValue(i, "paramlistversionid");
                    if (version.equalsIgnoreCase("C")) {
                        dsPL.setString(i, "versionstatus", "C");
                        continue;
                    }
                    dsPL.setString(i, "versionstatus", "");
                }
                this.resolveCurrentVersionAndGetPLData(dsPL, sapphireConnection, db);
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        }
        QueryProcessor qp = this.getQueryProcessor();
        String status = "";
        StringBuffer msg = new StringBuffer();
        DataSet updateWII = new DataSet();
        TranslationProcessor tp = this.getTranslationProcessor();
        StringBuffer msgConfirm = new StringBuffer();
        StringBuffer msgError = new StringBuffer();
        for (int i = 0; i < dsPL.getRowCount(); ++i) {
            String plId = dsPL.getValue(i, "paramlistid");
            String plVerId = dsPL.getValue(i, "paramlistversionid");
            String plVariantId = dsPL.getValue(i, "variantid");
            String wiiWorkArea = dsPL.getValue(i, "workareadepartmentid");
            String wiiAssignRule = dsPL.getValue(i, "autoassignrule");
            String wiiAssignAnalyst = dsPL.getValue(i, "autoassignanalystid");
            String versionStatus = dsPL.getValue(i, "versionstatus");
            DataSet ds = qp.getPreparedSqlDataSet("SELECT * FROM paramlist WHERE paramlistid = ? AND paramlistversionid = ? AND variantid = ?", new Object[]{plId, plVerId, plVariantId});
            if (ds.getRowCount() <= 0) continue;
            String plTestingDepartmentId = ds.getValue(0, "testingdepartmentid");
            String plWorkArea = ds.getValue(0, "workareadepartmentid");
            String plAssignRule = ds.getValue(0, "autoassignrule");
            String plAssignAnalyst = ds.getValue(0, "autoassignanalystid");
            if (workitem_testingdepartmentid.length() > 0 && wiiWorkArea.length() > 0) {
                if (wiiWorkArea.equals(plWorkArea) && !workitem_testingdepartmentid.equals(plTestingDepartmentId)) {
                    status = "ERROR";
                    msgError.append("\n" + plId + " (Ver:" + ("C".equalsIgnoreCase(versionStatus) ? "C" : plVerId) + ") " + plVariantId);
                } else {
                    String parentdepartmentid;
                    DataSet dsDept = qp.getPreparedSqlDataSet("SELECT  parentdepartmentid from department WHERE departmentid = ? ", new Object[]{wiiWorkArea});
                    if (dsDept.getRowCount() > 0 && !(parentdepartmentid = dsDept.getValue(0, "parentdepartmentid")).equals(workitem_testingdepartmentid)) {
                        status = "ERROR";
                        msgError.append("\n" + plId + " (Ver:" + ("C".equalsIgnoreCase(versionStatus) ? "C" : plVerId) + ") " + plVariantId);
                    }
                }
            }
            if (("ERROR".equals(status) || workitem_testinglabtype.length() != 0 || workitem_testingdepartmentid.length() != 0 && !workitem_testingdepartmentid.equals(plTestingDepartmentId) || plWorkArea.equals(wiiWorkArea)) && plAssignRule.equals(wiiAssignRule) && (!"Analyst".equalsIgnoreCase(plAssignRule) || plAssignAnalyst.equals(wiiAssignAnalyst))) continue;
            status = "CONFIRM";
            if (msgConfirm.length() == 0) {
                msgConfirm.append(tp.translate("Following ParamList reference(s) differs in the given fields from the ParamList definition")).append(":\n\n ");
                msgConfirm.append("<table border=1 bordercolor=\"#DCDCDC\" cellspacing=0 ><tr><td><font size=\"1em\">" + tp.translate("ParamList") + "</font></td><td><font size=\"1em\">" + tp.translate("Values") + "</font></td></tr>");
            }
            int r = updateWII.addRow();
            updateWII.setString(r, "paramlistid", plId);
            updateWII.setString(r, "paramlistversionid", plVerId);
            updateWII.setString(r, "variantid", plVariantId);
            updateWII.setString(r, "versionstatus", versionStatus);
            msgConfirm.append("<tr><td valign=\"top\" ><font size=\"1em\" >").append(plId + " (Ver:" + ("C".equalsIgnoreCase(versionStatus) ? "C" : plVerId) + ") " + plVariantId).append("</font></td><td valign=\"top\"><table border=0>");
            if (workitem_testinglabtype.length() == 0 && (workitem_testingdepartmentid.length() == 0 || workitem_testingdepartmentid.equals(plTestingDepartmentId)) && !plWorkArea.equals(wiiWorkArea)) {
                msgConfirm.append("<tr><td  valign=\"top\"><font size=\"1em\" >").append(tp.translate("Work Area") + ": \"").append(plWorkArea).append("\"</font></td></tr>");
                updateWII.setString(r, "workareadepartmentid", plWorkArea);
            } else {
                updateWII.setString(r, "workareadepartmentid", "(null)");
            }
            if (!plAssignRule.equals(wiiAssignRule)) {
                msgConfirm.append("<tr><td  valign=\"top\"><font size=\"1em\" >").append(tp.translate("Auto Assign Rule") + ": \"").append(plAssignRule).append("\"</font></td></tr>");
                updateWII.setString(r, "autoassignrule", plAssignRule);
            } else {
                updateWII.setString(r, "autoassignrule", "(null)");
            }
            if ("Analyst".equalsIgnoreCase(plAssignRule) && !plAssignAnalyst.equals(wiiAssignAnalyst)) {
                msgConfirm.append("<tr><td  valign=\"top\"><font size=\"1em\" >").append(tp.translate("Auto Assign Analyst") + ": \"").append(plAssignAnalyst).append("\"</font></td></tr>");
                updateWII.setString(r, "autoassignanalystid", plAssignAnalyst);
            } else {
                updateWII.setString(r, "autoassignanalystid", "(null)");
            }
            msgConfirm.append("</table></td></tr>");
        }
        if ("ERROR".equals(status)) {
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("testlab", workitem_testingdepartmentid);
            msg.append(tp.translate("Cannot proceed with save operation. Test Method's Testing lab '[testlab]' differs from the parent Testing Lab of the Work Area in the following ParamList reference(s):", token));
            msg.append("\n").append(msgError);
        }
        if ("CONFIRM".equals(status)) {
            msg.setLength(0);
            msg.append(msgConfirm).append("</table>");
            msg = msg.append("\n").append(new StringBuffer(tp.translate("Do you want to update the ParamList reference(s) to include these values?")));
        }
        ajaxResponse.addCallbackArgument("status", status);
        ajaxResponse.addCallbackArgument("message", msg.toString());
        if (updateWII.getRowCount() > 0) {
            ajaxResponse.addCallbackArgument("dsUpdateWII", updateWII);
        }
        ajaxResponse.print();
    }

    private void resolveCurrentVersionAndGetPLData(DataSet plData, SapphireConnection sc, DBAccess database) throws SapphireException {
        for (int i = 0; i < plData.getRowCount(); ++i) {
            String plVersion = plData.getValue(i, "paramlistversionid");
            if (!"c".equalsIgnoreCase(plVersion) && !"".equals(plVersion) && !"null".equals(plVersion)) continue;
            plData.setValue(i, "paramlistversionid", "C");
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("paramlistversionid", "C");
        DataSet currentVersionPL = plData.getFilteredDataSet(filter);
        if (currentVersionPL.getRowCount() > 0) {
            String versionIdList = SdiInfo.getCurrentVersion("ParamList", currentVersionPL.getColumnValues("paramlistid", ";"), currentVersionPL.getColumnValues("variantid", ";"), sc);
            currentVersionPL.addColumnValues("paramlistversionid", 0, versionIdList, ";");
        }
    }
}


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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ValidateVersionedApproval
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 96881 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcId");
        String keyid1 = ajaxResponse.getRequestParameter("keyId1");
        String keyid2 = ajaxResponse.getRequestParameter("keyId2");
        String keyid3 = ajaxResponse.getRequestParameter("keyId3");
        String operation = ajaxResponse.getRequestParameter("operation");
        String versionapproveddt = ajaxResponse.getRequestParameter("versionapproveddt");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(sdcid);
        boolean tripleKey = sdcPropertyList.getProperty("keycolumns").equals("3");
        QueryProcessor qp = this.getQueryProcessor();
        StringBuilder sql = new StringBuilder();
        TranslationProcessor tp = this.getTranslationProcessor();
        String message = "";
        DataSet ds = null;
        String rsetid = "";
        try {
            rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select sdiapprovalstep.approvalflag from sdiapproval, sdiapprovalstep, rsetitems r where sdiapproval.sdcid = sdiapprovalstep.sdcid and sdiapproval.keyid1 = sdiapprovalstep.keyid1 ").append(" and sdiapproval.keyid2 = sdiapprovalstep.keyid2 and sdiapproval.keyid3 = sdiapprovalstep.keyid3 and sdiapproval.approvaltypeid = sdiapprovalstep.approvaltypeid and sdiapproval.approvalfunction =   'Versioned' ").append(" and sdiapprovalstep.sdcid = r.sdcid and sdiapprovalstep.keyid1 = r.keyid1 and sdiapprovalstep.keyid2 = r.keyid2 ");
            if (tripleKey) {
                sql.append("and sdiapprovalstep.keyid3 = r.keyid3");
            }
            sql.append(" and r.rsetid = ").append(safeSQL.addVar(rsetid));
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        catch (SapphireException e) {
            this.logger.info("ValidateVersionedApproval", "Error finding sdiapprovals.");
        }
        finally {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (ds != null && ds.getRowCount() > 0) {
            PropertyList propsFilter = new PropertyList();
            DataSet dsFilteredDS = null;
            if ("SubmitForApproval".equals(operation)) {
                propsFilter.put("approvalflag", "N");
                dsFilteredDS = ds.getFilteredDataSet(propsFilter);
            } else if ("ResetApproval".equals(operation)) {
                propsFilter.put("approvalflag", "P");
                dsFilteredDS = ds.getFilteredDataSet(propsFilter);
                if (dsFilteredDS == null || dsFilteredDS.getRowCount() == 0) {
                    propsFilter.clear();
                    propsFilter.put("approvalflag", "F");
                    dsFilteredDS = ds.getFilteredDataSet(propsFilter);
                }
                if (dsFilteredDS == null || dsFilteredDS.getRowCount() == 0) {
                    propsFilter.clear();
                    propsFilter.put("approvalflag", "U");
                    dsFilteredDS = ds.getFilteredDataSet(propsFilter);
                }
            }
            if (dsFilteredDS.getRowCount() > 0) {
                message = "Success";
            } else if ("SubmitForApproval".equals(operation)) {
                message = tp.translate("SDI already submitted for approval.");
            } else if ("ResetApproval".equals(operation)) {
                message = tp.translate("Cannot Reset Approval. SDI not yet submitted for approval.");
            }
        } else {
            String approvalTypeId;
            if ("SubmitForApproval".equals(operation) && (approvalTypeId = sdcPropertyList.getProperty("versionapprovaltypeid", "")) != null && approvalTypeId.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", keyid1);
                props.setProperty("keyid2", keyid2);
                if (tripleKey) {
                    props.setProperty("keyid3", keyid3);
                }
                props.setProperty("approvaltypeid", approvalTypeId);
                props.setProperty("approvalfunction", "Versioned");
                props.setProperty("addsteps", "Y");
                props.setProperty("ready", "N");
                try {
                    this.getActionProcessor().processAction("AddSDIApproval", "1", props);
                }
                catch (SapphireException e) {
                    this.logger.info("ValidateVersionedApproval", "Not able to Add Version Approvals.");
                }
                message = "Success";
            }
            if ("ResetApproval".equals(operation)) {
                message = "".equals(versionapproveddt) ? tp.translate("Cannot Reset Approval. Approval Date is blank.") : "Success";
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}


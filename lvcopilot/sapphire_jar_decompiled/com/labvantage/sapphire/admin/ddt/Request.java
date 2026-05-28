/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleUtil;
import com.labvantage.sapphire.admin.ddt.RequestManagementUtil;
import com.labvantage.sapphire.admin.ddt.misc.WhoDoneIt;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Request
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "Revision: 1.1 $";
    public static final String SDCID = "Request";
    public static final String COLUMN_KEYID1 = "s_requestid";
    public static final String COLUMN_REQUESTCLASS = "requestclass";
    public static final String REQUESTCLASS_SUBMISSION = "Submission";
    public static final String REQUESTCLASS_PULL = "Pull";
    public static final String REQUESTCLASS_DISPOSE = "Dispose";
    private static WhoDoneIt whoDoneIt = new WhoDoneIt();

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!actionProps.getProperty("templateflag").equalsIgnoreCase("Y") && RequestManagementUtil.isPolicyEnabled(this.getConfigurationProcessor())) {
            this.performOperation(RequestManagementUtil.RequestOperation.ADD, sdiData, actionProps);
            this.resetFieldsWhileCopyingRequest(sdiData);
            this.checkIfSubmitterBelongsToSubmittingDepartment(sdiData);
            this.setSubmitterAndTargetDepartments(sdiData);
        }
    }

    private void resetRequestItem(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        StringBuffer requestItemIds = new StringBuffer();
        StringBuffer appliedFlags = new StringBuffer();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String requestId = primary.getValue(i, COLUMN_KEYID1);
            SDIRequest sdirequest = new SDIRequest();
            sdirequest.setSDCid("LV_RequestItem");
            sdirequest.setQueryFrom("s_requestitem");
            sdirequest.setRequestItem("primary");
            sdirequest.setQueryWhere("requestid='" + SafeSQL.encodeForSQL(requestId, this.database.isOracle()) + "'");
            DataSet dsRequest = this.getSDIProcessor().getSDIData(sdirequest).getDataset("primary");
            for (int j = 0; j < dsRequest.getRowCount(); ++j) {
                requestItemIds.append(";").append(dsRequest.getString(j, "s_requestitemid"));
                appliedFlags.append(";").append("N");
            }
        }
        if (requestItemIds.length() > 0) {
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "LV_RequestItem");
            props.put("keyid1", requestItemIds.substring(1));
            props.put("appliedflag", appliedFlags.substring(1));
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
    }

    private void resetApprovals(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet approvals = sdiData.getDataset("approval");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            HashMap<String, String> props;
            String requestId = primary.getValue(i, COLUMN_KEYID1);
            boolean acceptanceApprovalPresent = false;
            boolean releaseApprovalPresent = false;
            DataSet acceptanceApproval = null;
            DataSet releaseApproval = null;
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("approvalfunction", "Acceptance");
            findMap.put("keyid1", requestId);
            if (approvals != null && approvals.findRow(findMap) >= 0) {
                acceptanceApprovalPresent = true;
                acceptanceApproval = approvals.getFilteredDataSet(findMap);
            }
            findMap.clear();
            findMap.put("approvalfunction", "Release");
            findMap.put("keyid1", requestId);
            if (approvals != null && approvals.findRow(findMap) >= 0) {
                releaseApprovalPresent = true;
                releaseApproval = approvals.getFilteredDataSet(findMap);
            }
            if (acceptanceApprovalPresent) {
                props = new HashMap<String, String>();
                props.put("sdcid", SDCID);
                props.put("keyid1", requestId);
                props.put("ready", "N");
                props.put("approvalfunction", "Acceptance");
                this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
                this.resetApprovalSteps(requestId, acceptanceApproval);
            }
            if (!releaseApprovalPresent) continue;
            props = new HashMap();
            props.put("sdcid", SDCID);
            props.put("keyid1", requestId);
            props.put("ready", "N");
            props.put("approvalfunction", "Release");
            this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
            this.resetApprovalSteps(requestId, releaseApproval);
        }
    }

    private void resetApprovalSteps(String requestid, DataSet approvals) throws SapphireException {
        for (int i = 0; i < approvals.getRowCount(); ++i) {
            String approvaltypeid = approvals.getString(i, "approvaltypeid");
            String sql = "select approvalstep,roleid,assignedto,mandatoryflag,usersequence from sdiapprovalstep where sdcid=? and keyid1=? and approvaltypeid=?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{SDCID, requestid, approvaltypeid});
            for (int j = 0; j < ds.getRowCount(); ++j) {
                String approvalStep = ds.getString(j, "approvalstep");
                String roleid = ds.getString(j, "roleid");
                String assignedto = ds.getString(j, "assignedto");
                String mandatoryflag = ds.getString(j, "mandatoryflag");
                BigDecimal usersequence = ds.getBigDecimal(j, "usersequence");
                PropertyList props = new PropertyList();
                props.put("sdcid", SDCID);
                props.put("keyid1", requestid);
                props.put("approvaltypeid", approvaltypeid);
                props.put("approvalstep", approvalStep);
                props.put("roleid", roleid);
                props.put("assignedto", assignedto);
                props.put("usersequence", usersequence);
                props.put("mandatoryflag", mandatoryflag);
                this.getActionProcessor().processAction("EditSDIApprovalStep", "1", props);
            }
        }
    }

    private void resetFieldsWhileCopyingRequest(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            primary.setString(i, "deniedby", "");
            primary.setDate(i, "denieddt", "(null)");
            primary.setString(i, "rejectionnotes", "");
            primary.setDate(i, "cancelleddt", "(null)");
            primary.setString(i, "cancelledby", "");
            primary.setDate(i, "releaseddt", "(null)");
            primary.setDate(i, "holddt", "(null)");
            primary.setString(i, "holdby", "");
            primary.setDate(i, "completeddt", "(null)");
            primary.setDate(i, "starteddt", "(null)");
            primary.setDate(i, "receiveddt", "(null)");
            primary.setDate(i, "accepteddt", "(null)");
            primary.setDate(i, "acknowledgedflag", "(null)");
        }
    }

    private void checkIfSubmitterBelongsToSubmittingDepartment(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            boolean isTemplate = primary.getString(i, "templateflag", "N").equalsIgnoreCase("Y");
            if (isTemplate) continue;
            String requesterid = primary.getString(i, "requesterid", "(null)");
            String submitbydepartmentid = primary.getString(i, "submitbydepartmentid", "(null)");
            if (requesterid.equalsIgnoreCase("(null)") || submitbydepartmentid.equalsIgnoreCase("(null)") || this.database.getPreparedCount("select count(1) from departmentsysuser where sysuserid=? and departmentid=?", new Object[]{requesterid, submitbydepartmentid}) != 0) continue;
            throw new SapphireException(this.getTranslationProcessor().translate("Selected submitter does not belong to the selected submitting department"));
        }
    }

    private void setSubmitterAndTargetDepartments(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet initialPrimary = this.getBeforeEditImage() == null ? null : this.getBeforeEditImage().getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String sitedepartmentid;
            boolean isTemplate;
            boolean bl = isTemplate = initialPrimary != null ? initialPrimary.getString(i, "templateflag", "N").equalsIgnoreCase("Y") : primary.getString(i, "templateflag", "N").equalsIgnoreCase("Y");
            if (isTemplate) continue;
            String requesterid = primary.getString(i, "requesterid", "(null)");
            String submitbydepartmentid = primary.getString(i, "submitbydepartmentid", "(null)");
            String string = sitedepartmentid = primary.getString(i, "sitedepartmentid", "").length() > 0 ? primary.getString(i, "sitedepartmentid") : "(null)";
            if (initialPrimary == null && requesterid.equalsIgnoreCase("(null)")) {
                requesterid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                primary.setString(i, "requesterid", requesterid);
            }
            if (initialPrimary == null && submitbydepartmentid.equalsIgnoreCase("(null)")) {
                submitbydepartmentid = this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment();
                primary.setString(i, "submitbydepartmentid", submitbydepartmentid);
            }
            if (!OpalUtil.isDeptSecurityEnabled(this.getSDCProcessor(), SDCID)) continue;
            String primarySecurityDepartmentPolicySwitch = RequestManagementUtil.getDepartmentSecuritySwitch(this.getConfigurationProcessor(), "primarysecuritydepartment", "requestdepartmentsecurity");
            if (primarySecurityDepartmentPolicySwitch.equalsIgnoreCase("SD")) {
                if (!requesterid.equalsIgnoreCase("(null)")) {
                    primary.setString(i, "securityuser", requesterid);
                }
                if (submitbydepartmentid.equalsIgnoreCase("(null)")) continue;
                primary.setString(i, "securitydepartment", submitbydepartmentid);
                continue;
            }
            if (!primarySecurityDepartmentPolicySwitch.equalsIgnoreCase("TD")) continue;
            if (!requesterid.equalsIgnoreCase("(null)")) {
                primary.setString(i, "securityuser", requesterid);
            }
            if (sitedepartmentid.equalsIgnoreCase("(null)")) continue;
            primary.setString(i, "securitydepartment", sitedepartmentid);
        }
    }

    private void setAuxillarySecurityDepartments(SDIData sdiData) throws SapphireException {
        if (OpalUtil.isDeptSecurityEnabled(this.getSDCProcessor(), SDCID)) {
            DataSet primary = sdiData.getDataset("primary");
            DataSet initialPrimary = this.getBeforeEditImage() == null ? null : this.getBeforeEditImage().getDataset("primary");
            StringBuffer toAddSdiSecurityDeparmentids = new StringBuffer("");
            StringBuffer toDeleteSdiSecurityDeparmentids = new StringBuffer("");
            StringBuffer keyid1s = new StringBuffer("");
            String auxillarySecurityDepartmentPolicySwitch = RequestManagementUtil.getDepartmentSecuritySwitch(this.getConfigurationProcessor(), "auxillarysecuritydepartment", "requestdepartmentsecurity");
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String toDeleteSdiDepartments;
                boolean isTemplate;
                boolean bl = isTemplate = initialPrimary != null ? initialPrimary.getString(i, "templateflag", "N").equalsIgnoreCase("Y") : primary.getString(i, "templateflag", "N").equalsIgnoreCase("Y");
                if (isTemplate) continue;
                if (auxillarySecurityDepartmentPolicySwitch.equalsIgnoreCase("SD")) {
                    String submitbydepartmentid = primary.getString(i, "submitbydepartmentid", "(null)");
                    keyid1s.append(";").append(primary.getString(i, COLUMN_KEYID1));
                    if (submitbydepartmentid.equalsIgnoreCase("(null)")) {
                        if (initialPrimary == null) {
                            toAddSdiSecurityDeparmentids.append(";").append(this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
                        }
                    } else {
                        toAddSdiSecurityDeparmentids.append(";").append(submitbydepartmentid);
                    }
                    toDeleteSdiDepartments = RequestManagementUtil.fetchDepartmentsToDeleteSDISecurityDepartments(this.getQueryProcessor(), primary.getString(i, COLUMN_KEYID1));
                    if (toAddSdiSecurityDeparmentids.length() <= 0 || toDeleteSdiDepartments.length() <= 0) continue;
                    toDeleteSdiSecurityDeparmentids.append(";").append(toDeleteSdiDepartments);
                    continue;
                }
                if (!auxillarySecurityDepartmentPolicySwitch.equalsIgnoreCase("TD")) continue;
                String sitedepartmentid = primary.getString(i, "sitedepartmentid", "(null)");
                keyid1s.append(";").append(primary.getString(i, COLUMN_KEYID1));
                if (!sitedepartmentid.equalsIgnoreCase("(null)")) {
                    toAddSdiSecurityDeparmentids.append(";").append(sitedepartmentid);
                }
                toDeleteSdiDepartments = RequestManagementUtil.fetchDepartmentsToDeleteSDISecurityDepartments(this.getQueryProcessor(), primary.getString(i, COLUMN_KEYID1));
                if (toAddSdiSecurityDeparmentids.length() <= 0 || toDeleteSdiDepartments.length() <= 0) continue;
                toDeleteSdiSecurityDeparmentids.append(";").append(toDeleteSdiDepartments);
            }
            if (toDeleteSdiSecurityDeparmentids.toString().length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDCID);
                props.setProperty("keyid1", keyid1s.substring(1));
                props.setProperty("departmentid", toDeleteSdiSecurityDeparmentids.substring(1));
                this.getActionProcessor().processAction("DeleteSDISecurityDep", "1", props);
            }
            if (toAddSdiSecurityDeparmentids.toString().length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDCID);
                props.setProperty("keyid1", keyid1s.substring(1));
                props.setProperty("propsmatch", "Y");
                props.setProperty("departmentid", toAddSdiSecurityDeparmentids.substring(1));
                this.getActionProcessor().processAction("AddSDISecurityDept", "1", props);
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!actionProps.getProperty("templateflag").equalsIgnoreCase("Y") && RequestManagementUtil.isPolicyEnabled(this.getConfigurationProcessor())) {
            DataSet primary = sdiData.getDataset("primary");
            String requestTemplate = actionProps.getProperty("templatekeyid1", actionProps.getProperty("templateid", ""));
            if (actionProps.getProperty("excludetemplaterequestitems", "N").equals("N") && requestTemplate.length() > 0) {
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    String requestId = primary.getValue(i, COLUMN_KEYID1, "");
                    this.copyRequestItems(requestTemplate, requestId);
                }
            }
            this.resetApprovals(sdiData);
            this.resetRequestItem(sdiData);
            this.setAuxillarySecurityDepartments(sdiData);
        }
    }

    private void copyRequestItems(String requestTemplate, String requestId) throws ActionException {
        SDIRequest requestTemplateRequestItemsSDIRequest = new SDIRequest();
        requestTemplateRequestItemsSDIRequest.setSDCid("LV_RequestItem");
        requestTemplateRequestItemsSDIRequest.setRequestItem("primary");
        requestTemplateRequestItemsSDIRequest.setQueryFrom("s_requestitem");
        requestTemplateRequestItemsSDIRequest.setQueryWhere("requestid ='" + SafeSQL.encodeForSQL(requestTemplate, this.database.isOracle()) + "'");
        SDIData requestTemplateRequestItemsSDIData = this.getSDIProcessor().getSDIData(requestTemplateRequestItemsSDIRequest);
        DataSet requestTemplateRequestItemsDataSet = requestTemplateRequestItemsSDIData.getDataset("primary");
        for (int i = 0; i < requestTemplateRequestItemsDataSet.getRowCount(); ++i) {
            ActionBlock actionBlock = new ActionBlock();
            PropertyList addRequestItemProps = new PropertyList();
            addRequestItemProps.setProperty("requestid", requestId);
            addRequestItemProps.setProperty("sdcid", "LV_RequestItem");
            addRequestItemProps.setProperty("templatekeyid1", requestTemplateRequestItemsDataSet.getValue(i, "s_requestitemid"));
            addRequestItemProps.setProperty("newkeyid1", "[newrequestitem]");
            actionBlock.setAction("AddRequestItem" + i, "AddSDI", "1", addRequestItemProps);
            String sampleTemplate = requestTemplateRequestItemsDataSet.getValue(i, "templatekeyid1", "");
            if (sampleTemplate.length() > 0) {
                PropertyList editRequestItemProps = new PropertyList();
                editRequestItemProps.setProperty("sdcid", "LV_RequestItem");
                editRequestItemProps.setProperty("keyid1", "[newrequestitem]");
                editRequestItemProps.setProperty("templatekeyid1", sampleTemplate);
                actionBlock.setAction("EditRequestItem" + i, "EditSDI", "1", editRequestItemProps);
            }
            this.getActionProcessor().processActionBlock(actionBlock);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (RequestManagementUtil.isPolicyEnabled(this.getConfigurationProcessor())) {
            RequestManagementUtil.RequestOperation requestOperation = RequestManagementUtil.RequestOperation.getRequestOperation(actionProps.getProperty("operation", ""));
            if (requestOperation != null) {
                this.performOperation(requestOperation, sdiData, actionProps);
            } else {
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    String requestStatus = primary.getValue(i, "requeststatus");
                    DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
                    String previousRequestString = beforeEditImagePrimary.getString(i, "requeststatus", "");
                    RequestManagementUtil.RequestStatus previousRequestStatus = null;
                    if (previousRequestString.length() > 0) {
                        previousRequestStatus = RequestManagementUtil.RequestStatus.getRequestStatus(previousRequestString);
                    }
                    if (!requestStatus.equalsIgnoreCase(RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue()) && !requestStatus.equalsIgnoreCase(RequestManagementUtil.RequestStatus.RELEASED.getStatusValue())) continue;
                    if (beforeEditImagePrimary.getValue(i, "starteddt", "").length() == 0) {
                        primary.setDate(i, "starteddt", DateTimeUtil.getNowTimestamp());
                    }
                    primary.setDate(i, "completeddt", DateTimeUtil.getNowTimestamp());
                    if (!requestStatus.equalsIgnoreCase(RequestManagementUtil.RequestStatus.RELEASED.getStatusValue())) continue;
                    primary.setDate(i, "releaseddt", DateTimeUtil.getNowTimestamp());
                }
            }
            this.checkIfSubmitterBelongsToSubmittingDepartment(sdiData);
            this.setSubmitterAndTargetDepartments(sdiData);
            this.setAuxillarySecurityDepartments(sdiData);
        }
        whoDoneIt.process(primary, this);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.findCandidateForAutoRelease(sdiData);
        ApprovalRuleUtil.checkSDIAutoApprovalRule(primary, actionProps, SDCID, "requeststatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue(), RequestManagementUtil.RequestStatus.RELEASED.getStatusValue(), "releasedby", "releaseddt", this.database, this.connectionInfo, this.logger, this.getSDCProcessor(), true);
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void performOperation(RequestManagementUtil.RequestOperation operation, SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        boolean isAsyncCopySDIDetailsToRequest = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom").getProperty("asynccopysdidetailstorequest", "Y").equals("Y");
        boolean isProcessRequestSamplesAsynchronously = isAsyncCopySDIDetailsToRequest && !this.connectionInfo.getSysuserId().equals("(system)");
        switch (operation) {
            case ADD: {
                String statusInActionProps = actionProps.getProperty("requeststatus", "");
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    String userRequestStatus = primary.getValue(i, "requeststatus", "");
                    RequestManagementUtil.RequestStatus status = RequestManagementUtil.RequestStatus.getRequestStatus(userRequestStatus);
                    if (status != null && statusInActionProps.length() != 0) continue;
                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.DRAFT.getStatusValue());
                }
                break;
            }
            case SENDFORACCEPTANCE: {
                DataSet beforeEditImagePrimary = this.getBeforeEditImage() == null ? null : this.getBeforeEditImage().getDataset("primary");
                DataSet approvals = sdiData.getDataset("approval");
                if (approvals == null) {
                    SDIRequest sdirequest = new SDIRequest();
                    sdirequest.setSDCid(SDCID);
                    sdirequest.setKeyid1List(primary.getColumnValues(COLUMN_KEYID1, ";"));
                    sdirequest.setRequestItem("approval");
                    approvals = this.getSDIProcessor().getSDIData(sdirequest).getDataset("approval");
                }
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    String creationRule = beforeEditImagePrimary != null ? beforeEditImagePrimary.getValue(i, "creationrule", "OnSubmit") : primary.getValue(i, "creationrule", "OnSubmit");
                    String creationClass = beforeEditImagePrimary != null ? beforeEditImagePrimary.getValue(i, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION) : primary.getValue(i, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION);
                    String requestId = primary.getValue(i, COLUMN_KEYID1);
                    boolean acceptanceApprovalPresent = false;
                    boolean releaseApprovalPresent = false;
                    String nextRequestStatus = "";
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("approvalfunction", "Acceptance");
                    findMap.put("keyid1", requestId);
                    if (approvals.findRow(findMap) >= 0) {
                        acceptanceApprovalPresent = true;
                    }
                    findMap.clear();
                    findMap.put("approvalfunction", "Release");
                    findMap.put("keyid1", requestId);
                    if (approvals.findRow(findMap) >= 0) {
                        releaseApprovalPresent = true;
                    }
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("sdcid", SDCID);
                    props.put("keyid1", requestId);
                    props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.PENDINGACCEPTANCE.getStatusValue());
                    props.put("approvalstatuscolumn", "requeststatus");
                    props.put("approvalfunction", "Acceptance");
                    boolean doSubmitForSDIApproval = true;
                    Timestamp now = DateTimeUtil.getNowTimestamp();
                    if (RequestManagementUtil.hasTestingFinishedOnAllChildSample(requestId, this.getQueryProcessor())) {
                        String autoRelease = beforeEditImagePrimary.getValue(i, "autoreleaseflag", "");
                        if (acceptanceApprovalPresent) {
                            props.put("approvalfunction", "Acceptance");
                            props.put("approvalstatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                            if (beforeEditImagePrimary.getValue(i, "starteddt", "").length() == 0) {
                                primary.setDate(i, "starteddt", now);
                            }
                            primary.setDate(i, "completeddt", now);
                            props.put("auditreason", "Samples testing finished.");
                        } else if (autoRelease.trim().equals("Y") && RequestManagementUtil.doAutoRelease(this.getQueryProcessor(), this.getConfigurationProcessor(), requestId)) {
                            doSubmitForSDIApproval = false;
                            primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                            primary.setDate(i, "releaseddt", now);
                            primary.setString(i, "reviewdisposition", "Approved");
                        } else {
                            if (releaseApprovalPresent) {
                                props.put("approvalfunction", "Release");
                            }
                            if (!acceptanceApprovalPresent) {
                                props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                            }
                            props.put("approvalstatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                            if (beforeEditImagePrimary.getValue(i, "starteddt", "").length() == 0) {
                                primary.setDate(i, "starteddt", now);
                            }
                            primary.setDate(i, "completeddt", now);
                            props.put("auditreason", "Samples testing finished.");
                        }
                    } else if (RequestManagementUtil.hasTestingStartedOnAnyChildSample(requestId, this.getQueryProcessor())) {
                        props.put("approvalstatus", RequestManagementUtil.RequestStatus.OPEN.getStatusValue());
                        props.put("auditreason", "Samples testing started.");
                        if (beforeEditImagePrimary.getValue(i, "starteddt", "").length() == 0) {
                            primary.setDate(i, "starteddt", now);
                        }
                    } else if (RequestManagementUtil.isAllSamplesReceived(requestId, this.getQueryProcessor())) {
                        props.put("approvalstatus", RequestManagementUtil.RequestStatus.RECEIVED.getStatusValue());
                        primary.setDate(i, "receiveddt", DateTimeUtil.getNowTimestamp());
                    } else {
                        props.put("approvalstatus", RequestManagementUtil.RequestStatus.INITIAL.getStatusValue());
                        if (creationClass.equals(REQUESTCLASS_SUBMISSION)) {
                            SDIRequest sdirequest = new SDIRequest();
                            sdirequest.setSDCid("LV_RequestItem");
                            sdirequest.setQueryFrom("s_requestitem");
                            sdirequest.setRequestItem("primary");
                            sdirequest.setQueryWhere("requestid='" + SafeSQL.encodeForSQL(requestId, this.database.isOracle()) + "'");
                            DataSet dsRequest = this.getSDIProcessor().getSDIData(sdirequest).getDataset("primary");
                            int copies = 0;
                            boolean isNotApplied = false;
                            for (int j = 0; j < dsRequest.getRowCount(); ++j) {
                                copies += Integer.parseInt(dsRequest.getValue(i, "itemcount", "0"));
                                if (!StringUtil.getYN(dsRequest.getValue(j, "appliedflag"), "N").equals("N") || isNotApplied) continue;
                                isNotApplied = true;
                            }
                            if (!(creationRule.equals("OnAcceptance") && acceptanceApprovalPresent || !isNotApplied)) {
                                props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.PENDINDINGTESTASSOCIATION.getStatusValue());
                                if (dsRequest.getRowCount() > 0 && copies > 0) {
                                    if (acceptanceApprovalPresent) {
                                        if (isProcessRequestSamplesAsynchronously) {
                                            props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.PENDINDINGTESTASSOCIATION.getStatusValue());
                                            nextRequestStatus = RequestManagementUtil.RequestStatus.PENDINGACCEPTANCE.getStatusValue();
                                        } else {
                                            props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.PENDINGACCEPTANCE.getStatusValue());
                                        }
                                    } else if (isProcessRequestSamplesAsynchronously) {
                                        props.put("approvalstatus", RequestManagementUtil.RequestStatus.PENDINDINGTESTASSOCIATION.getStatusValue());
                                        nextRequestStatus = RequestManagementUtil.RequestStatus.INITIAL.getStatusValue();
                                    } else {
                                        props.put("approvalstatus", RequestManagementUtil.RequestStatus.INITIAL.getStatusValue());
                                    }
                                } else {
                                    props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.PENDINGACCEPTANCE.getStatusValue());
                                }
                            }
                        }
                    }
                    if (doSubmitForSDIApproval) {
                        this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
                        primary.setString(i, "deniedby", "");
                        primary.setDate(i, "denieddt", "(null)");
                        primary.setString(i, "rejectionnotes", "");
                    }
                    if (RequestManagementUtil.RequestStatus.getRequestStatus((String)props.get("newstatus")) == RequestManagementUtil.RequestStatus.ACCEPTED) {
                        primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                    }
                    if (creationRule.equals("OnAcceptance") && acceptanceApprovalPresent || !creationClass.equals(REQUESTCLASS_SUBMISSION)) continue;
                    this.addSamples(requestId, null, nextRequestStatus);
                }
                break;
            }
            case ACCEPT: {
                DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
                DataSet approvals = sdiData.getDataset("approval");
                if (approvals == null) {
                    SDIRequest sdirequest = new SDIRequest();
                    sdirequest.setSDCid(SDCID);
                    sdirequest.setKeyid1List(primary.getColumnValues(COLUMN_KEYID1, ";"));
                    sdirequest.setRequestItem("approval");
                    approvals = this.getSDIProcessor().getSDIData(sdirequest).getDataset("approval");
                }
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    SDIRequest sdirequest;
                    String requestId = primary.getValue(i, COLUMN_KEYID1);
                    boolean acceptanceApprovalPresent = false;
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("approvalfunction", "Acceptance");
                    findMap.put("keyid1", requestId);
                    if (approvals.findRow(findMap) >= 0) {
                        acceptanceApprovalPresent = true;
                    }
                    if (actionProps.getProperty("approvalevaluation", "F").equalsIgnoreCase("P")) {
                        String nextRequestStatus = "";
                        if (RequestManagementUtil.hasTestingFinishedOnAllChildSample(requestId, this.getQueryProcessor())) {
                            String autoRelease = beforeEditImagePrimary.getValue(i, "autoreleaseflag", "");
                            Timestamp now = DateTimeUtil.getNowTimestamp();
                            HashMap<String, String> props = new HashMap<String, String>();
                            if (autoRelease.trim().equals("Y") && RequestManagementUtil.doAutoRelease(this.getQueryProcessor(), this.getConfigurationProcessor(), requestId)) {
                                primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                                primary.setDate(i, "releaseddt", now);
                                primary.setString(i, "reviewdisposition", "Approved");
                            } else {
                                props.put("sdcid", SDCID);
                                props.put("keyid1", primary.getValue(i, COLUMN_KEYID1));
                                props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                                props.put("approvalstatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                                props.put("approvalstatuscolumn", "requeststatus");
                                props.put("approvalfunction", "Release");
                                props.put("tracelogid", primary.getValue(i, "tracelogid", ""));
                                props.put("auditreason", "Samples testing finished.");
                                this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
                            }
                            if (beforeEditImagePrimary.getValue(i, "starteddt", "").length() == 0) {
                                primary.setDate(i, "starteddt", now);
                            }
                            primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                            primary.setDate(i, "completeddt", now);
                            if (RequestManagementUtil.RequestStatus.getRequestStatus((String)props.get("newstatus")) == RequestManagementUtil.RequestStatus.RELEASED) {
                                primary.setDate(i, "releaseddt", DateTimeUtil.getNowTimestamp());
                            }
                        } else if (RequestManagementUtil.hasTestingStartedOnAnyChildSample(requestId, this.getQueryProcessor())) {
                            primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.OPEN.getStatusValue());
                            if (beforeEditImagePrimary.getValue(i, "starteddt", "").length() == 0) {
                                primary.setDate(i, "starteddt", DateTimeUtil.getNowTimestamp());
                            }
                            primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                        } else if (RequestManagementUtil.isAllSamplesReceived(requestId, this.getQueryProcessor())) {
                            primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.RECEIVED.getStatusValue());
                            primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                            primary.setDate(i, "receiveddt", DateTimeUtil.getNowTimestamp());
                        } else {
                            int totalNumberOfSamplesCreated;
                            sdirequest = new SDIRequest();
                            sdirequest.setSDCid("LV_RequestItem");
                            sdirequest.setQueryFrom("s_requestitem");
                            sdirequest.setRequestItem("primary");
                            sdirequest.setQueryWhere("requestid='" + SafeSQL.encodeForSQL(requestId, this.database.isOracle()) + "'");
                            DataSet dsRequest = this.getSDIProcessor().getSDIData(sdirequest).getDataset("primary");
                            int copies = 0;
                            for (int j = 0; j < dsRequest.getRowCount(); ++j) {
                                copies += Integer.parseInt(dsRequest.getValue(j, "itemcount", "0"));
                            }
                            if (beforeEditImagePrimary.getValue(i, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION).equals(REQUESTCLASS_SUBMISSION) && beforeEditImagePrimary.getValue(i, "creationrule", "OnSubmit").equals("OnAcceptance") && acceptanceApprovalPresent) {
                                if (dsRequest.getRowCount() > 0 && copies > 0) {
                                    totalNumberOfSamplesCreated = this.getQueryProcessor().getPreparedCount("select count(requestid) totalNumberOfSamples from s_requestitemdetail where requestid=?", new Object[]{requestId});
                                    if (copies != totalNumberOfSamplesCreated) {
                                        if (isProcessRequestSamplesAsynchronously) {
                                            primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.PENDINDINGTESTASSOCIATION.getStatusValue());
                                            nextRequestStatus = RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue();
                                        } else {
                                            primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue());
                                            primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                                        }
                                    } else {
                                        primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue());
                                        primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                                    }
                                } else {
                                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue());
                                    primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                                }
                            } else if (beforeEditImagePrimary.getValue(i, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION).equals(REQUESTCLASS_SUBMISSION) && !beforeEditImagePrimary.getValue(i, "creationrule", "OnSubmit").equals("OnAcceptance") && acceptanceApprovalPresent) {
                                totalNumberOfSamplesCreated = this.getQueryProcessor().getPreparedCount("select count(requestid) totalNumberOfSamples from s_requestitemdetail where requestid=?", new Object[]{requestId});
                                if (copies != totalNumberOfSamplesCreated) {
                                    if (isProcessRequestSamplesAsynchronously) {
                                        primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.PENDINDINGTESTASSOCIATION.getStatusValue());
                                        nextRequestStatus = RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue();
                                    } else {
                                        primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue());
                                        primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                                    }
                                } else {
                                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue());
                                    primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                                }
                            } else {
                                primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue());
                                primary.setDate(i, "accepteddt", DateTimeUtil.getNowTimestamp());
                            }
                        }
                        if (!beforeEditImagePrimary.getValue(i, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION).equals(REQUESTCLASS_SUBMISSION)) continue;
                        this.addSamples(requestId, null, nextRequestStatus);
                        continue;
                    }
                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.DENIED.getStatusValue());
                    primary.setString(i, "deniedby", this.getConnectionInfo().getSysuserId());
                    primary.setDate(i, "denieddt", DateTimeUtil.getNowTimestamp());
                    String rejectionComment = RequestManagementUtil.populateRequestRejectionComments(this.getQueryProcessor(), requestId, "Acceptance");
                    if (rejectionComment != null && rejectionComment.length() > 0) {
                        primary.setString(i, "rejectionnotes", rejectionComment);
                    }
                    if (!actionProps.getProperty("rolldownstatus", "Y").equals("Y") || beforeEditImagePrimary.getValue(i, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION).equals(REQUESTCLASS_SUBMISSION)) continue;
                    sdirequest = new SDIRequest();
                    sdirequest.setSDCid("LV_RequestItem");
                    sdirequest.setQueryFrom("s_requestitem");
                    sdirequest.setRequestItem("primary");
                    sdirequest.setQueryWhere("requestid='" + SafeSQL.encodeForSQL(requestId, this.database.isOracle()) + "'");
                    DataSet dsRequestItem = this.getSDIProcessor().getSDIData(sdirequest).getDataset("primary");
                    if (dsRequestItem == null || dsRequestItem.size() <= 0) continue;
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("sdcid", "LV_RequestItem");
                    props.put("keyid1", dsRequestItem.getColumnValues("s_requestitemid", ";"));
                    props.put("operation", "denyonly");
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                }
                break;
            }
            case RECEIVE: {
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.RECEIVED.getStatusValue());
                    primary.setDate(i, "receiveddt", DateTimeUtil.getNowTimestamp());
                }
                break;
            }
            case OPEN: {
                DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
                Timestamp now = DateTimeUtil.getNowTimestamp();
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.OPEN.getStatusValue());
                    if (beforeEditImagePrimary.getValue(i, "starteddt", "").length() != 0) continue;
                    primary.setDate(i, "starteddt", now);
                }
                break;
            }
            case COMPLETE: {
                DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
                Timestamp now = DateTimeUtil.getNowTimestamp();
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    String requestId = primary.getValue(i, COLUMN_KEYID1);
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("sdcid", SDCID);
                    props.put("keyid1", primary.getValue(i, COLUMN_KEYID1));
                    props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                    String requestApprovalPolicy = policy.getProperty("requestapprovalwithincidents", "Allow");
                    if (requestApprovalPolicy.equalsIgnoreCase("block") || requestApprovalPolicy.equalsIgnoreCase("warn")) {
                        boolean hasOpenIncidents = RequestManagementUtil.hasOpenIncidents(requestId, this.getQueryProcessor());
                        if (hasOpenIncidents) {
                            props.put("approvalstatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                        } else {
                            props.put("approvalstatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                        }
                    } else {
                        props.put("approvalstatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                    }
                    props.put("approvalstatuscolumn", "requeststatus");
                    props.put("approvalfunction", "Release");
                    props.put("tracelogid", primary.getValue(i, "tracelogid", ""));
                    this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
                    if (beforeEditImagePrimary.getValue(i, "starteddt", "").length() == 0) {
                        primary.setDate(i, "starteddt", now);
                    }
                    primary.setDate(i, "completeddt", now);
                    if (RequestManagementUtil.RequestStatus.getRequestStatus((String)props.get("newstatus")) != RequestManagementUtil.RequestStatus.RELEASED) continue;
                    primary.setDate(i, "releaseddt", DateTimeUtil.getNowTimestamp());
                }
                break;
            }
            case RELEASE: {
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    if (actionProps.getProperty("approvalevaluation", "F").equalsIgnoreCase("P")) {
                        primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                        primary.setDate(i, "releaseddt", DateTimeUtil.getNowTimestamp());
                        primary.setString(i, "reviewdisposition", "Approved");
                        continue;
                    }
                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.REJECTED.getStatusValue());
                    primary.setString(i, "reviewdisposition", "Rejected");
                }
                break;
            }
            case CANCEL: {
                DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
                StringBuffer requestsToBeCancelledWithItems = new StringBuffer();
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.CANCELLED.getStatusValue());
                    if (beforeEditImagePrimary.getValue(i, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION).equals(REQUESTCLASS_SUBMISSION)) continue;
                    requestsToBeCancelledWithItems.append(";").append(primary.getValue(i, COLUMN_KEYID1));
                }
                if (requestsToBeCancelledWithItems.length() > 0) {
                    String extendedQueryWhere = "requestitemstatus IN ('" + RequestManagementUtil.RequestItemStatus.PENDING.getStatusValue() + "','" + RequestManagementUtil.RequestItemStatus.INPROGRESS.getStatusValue() + "')";
                    this.doRequestItemOperation(requestsToBeCancelledWithItems.substring(1), "cancelonly", extendedQueryWhere);
                }
                this.cancelUnCancelChildren(primary, true);
                break;
            }
            case UNCANCEL: {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("keyid1", primary.getColumnValues(COLUMN_KEYID1, ";"));
                props.put("sdcid", SDCID);
                props.put("statuscolumn", "requeststatus");
                props.put("validatestatuscolumnvalue", RequestManagementUtil.RequestStatus.CANCELLED.getStatusValue());
                props.put("tracelogid", primary.getValue(0, "tracelogid", ""));
                this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                this.cancelUnCancelChildren(primary, false);
                DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
                StringBuffer requestsToBeUnCancelledWithItems = new StringBuffer();
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    if (beforeEditImagePrimary.getValue(i, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION).equals(REQUESTCLASS_SUBMISSION)) continue;
                    requestsToBeUnCancelledWithItems.append(";").append(primary.getValue(i, COLUMN_KEYID1));
                }
                if (requestsToBeUnCancelledWithItems.length() <= 0) break;
                this.doRequestItemOperation(requestsToBeUnCancelledWithItems.substring(1), "pending", null);
                break;
            }
            case HOLD: {
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.ONHOLD.getStatusValue());
                    primary.setString(i, "holdby", this.getConnectionInfo().getSysuserId());
                    primary.setDate(i, "holddt", DateTimeUtil.getNowTimestamp());
                }
                break;
            }
            case RELEASEHOLD: {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("keyid1", primary.getColumnValues(COLUMN_KEYID1, ";"));
                props.put("sdcid", SDCID);
                props.put("statuscolumn", "requeststatus");
                props.put("validatestatuscolumnvalue", RequestManagementUtil.RequestStatus.ONHOLD.getStatusValue());
                this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    primary.setString(i, "holdby", "");
                    primary.setDate(i, "holddt", "(null)");
                }
                break;
            }
            case AUTORELEASE: {
                Timestamp now = DateTimeUtil.getNowTimestamp();
                StringBuilder requestsWithOpenIncidents = new StringBuilder();
                StringBuilder requestsWithoutIncidents = new StringBuilder();
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    String requestId = primary.getValue(i, COLUMN_KEYID1);
                    String requestApprovalPolicy = policy.getProperty("requestapprovalwithincidents", "Allow");
                    if (requestApprovalPolicy.equalsIgnoreCase("block") || requestApprovalPolicy.equalsIgnoreCase("warn")) {
                        boolean hasOpenIncidents = RequestManagementUtil.hasOpenIncidents(requestId, this.getQueryProcessor());
                        if (hasOpenIncidents) {
                            requestsWithOpenIncidents.append(";").append(requestId);
                            continue;
                        }
                        requestsWithoutIncidents.append(";").append(requestId);
                        continue;
                    }
                    requestsWithoutIncidents.append(";").append(requestId);
                }
                if (requestsWithoutIncidents.length() > 0) {
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("sdcid", SDCID);
                    props.put("keyid1", requestsWithoutIncidents.substring(1));
                    props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                    props.put("approvalstatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                    props.put("approvalstatuscolumn", "requeststatus");
                    props.put("approvalfunction", "Release");
                    props.put("tracelogid", primary.getColumnValues("tracelogid", ";"));
                    this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
                    for (int i = 0; i < primary.getRowCount(); ++i) {
                        primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                        primary.setDate(i, "releaseddt", now);
                        primary.setString(i, "reviewdisposition", "Approved");
                        primary.setDate(i, "completeddt", now);
                    }
                }
                if (requestsWithOpenIncidents.length() <= 0) break;
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("sdcid", SDCID);
                props.put("keyid1", requestsWithOpenIncidents.substring(1));
                props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                props.put("approvalstatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                props.put("approvalstatuscolumn", "requeststatus");
                props.put("approvalfunction", "Release");
                props.put("tracelogid", primary.getColumnValues("tracelogid", ";"));
                this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
                break;
            }
            case CLEARAPPROVAL: {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("keyid1", primary.getColumnValues(COLUMN_KEYID1, ";"));
                props.put("sdcid", SDCID);
                props.put("ready", "Y");
                props.put("approvalfunction", "Acceptance");
                this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.PENDINGACCEPTANCE.getStatusValue());
                    primary.setDate(i, "accepteddt", "(null)");
                }
                break;
            }
            case UNRELEASE: {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("keyid1", primary.getColumnValues(COLUMN_KEYID1, ";"));
                props.put("sdcid", SDCID);
                props.put("statuscolumn", "requeststatus");
                props.put("validatestatuscolumnvalue", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                props.put("defaultvalue", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                props = new HashMap();
                props.put("keyid1", primary.getColumnValues(COLUMN_KEYID1, ";"));
                props.put("sdcid", SDCID);
                props.put("ready", "Y");
                props.put("approvalfunction", "Release");
                this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    primary.setDate(i, "releaseddt", "(null)");
                    primary.setString(i, "reviewdisposition", "");
                }
                break;
            }
            case UNREJECT: {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("keyid1", primary.getColumnValues(COLUMN_KEYID1, ";"));
                props.put("sdcid", SDCID);
                props.put("statuscolumn", "requeststatus");
                props.put("validatestatuscolumnvalue", RequestManagementUtil.RequestStatus.REJECTED.getStatusValue());
                props.put("defaultvalue", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
                this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                props = new HashMap();
                props.put("keyid1", primary.getColumnValues(COLUMN_KEYID1, ";"));
                props.put("sdcid", SDCID);
                props.put("ready", "Y");
                props.put("approvalfunction", "Release");
                this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    primary.setString(i, "reviewdisposition", "");
                }
                break;
            }
            case ADDSAMPLES: {
                DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
                String requestId = actionProps.getProperty("requestid", primary.getValue(0, COLUMN_KEYID1, ""));
                HashSet<String> requestItems = new HashSet<String>(Arrays.asList(StringUtil.split(actionProps.getProperty("s_requestitemid", ""), ";")));
                if (!beforeEditImagePrimary.getValue(0, COLUMN_REQUESTCLASS, REQUESTCLASS_SUBMISSION).equals(REQUESTCLASS_SUBMISSION)) break;
                String nextRequestStatus = "";
                if (isProcessRequestSamplesAsynchronously) {
                    primary.setString(0, "requeststatus", RequestManagementUtil.RequestStatus.PENDINDINGTESTASSOCIATION.getStatusValue());
                    nextRequestStatus = RequestManagementUtil.RequestStatus.DRAFT.getStatusValue();
                } else {
                    primary.setString(0, "requeststatus", RequestManagementUtil.RequestStatus.DRAFT.getStatusValue());
                }
                this.addSamples(requestId, requestItems, nextRequestStatus);
                break;
            }
            case ADDADHOCSAMPLES: {
                this.addAdhocSamples(primary, actionProps);
                this.doUnComplete(primary);
                break;
            }
            case UNCOMPLETE: {
                this.doUnComplete(primary);
            }
        }
    }

    private void doRequestItemOperation(String requestIds, String operation, String extendedQueryWhere) throws SapphireException {
        String queryWhere = "requestid IN ( '" + SafeSQL.convertToSQLInClause(requestIds, ";", this.database.isOracle()) + "')";
        if (extendedQueryWhere != null && extendedQueryWhere.length() > 0) {
            queryWhere = queryWhere + " AND " + extendedQueryWhere;
        }
        SDIRequest sdirequest = new SDIRequest();
        sdirequest.setSDCid("LV_RequestItem");
        sdirequest.setQueryFrom("s_requestitem");
        sdirequest.setRequestItem("primary");
        sdirequest.setQueryWhere(queryWhere);
        DataSet dsRequestItem = this.getSDIProcessor().getSDIData(sdirequest).getDataset("primary");
        if (dsRequestItem.getRowCount() > 0) {
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("operation", operation);
            props.put("sdcid", "LV_RequestItem");
            props.put("keyid1", dsRequestItem.getColumnValues("s_requestitemid", ";"));
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
    }

    private void doUnComplete(DataSet primary) throws SapphireException {
        DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String requestId = primary.getString(i, COLUMN_KEYID1);
            if (!RequestManagementUtil.RequestStatus.getRequestStatus(beforeEditImagePrimary.getValue(i, "requeststatus", "")).equals((Object)RequestManagementUtil.RequestStatus.COMPLETED) && !RequestManagementUtil.RequestStatus.getRequestStatus(beforeEditImagePrimary.getValue(i, "requeststatus", "")).equals((Object)RequestManagementUtil.RequestStatus.RELEASED)) continue;
            if (RequestManagementUtil.hasTestingStartedOnAnyChildSample(requestId, this.getQueryProcessor())) {
                primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.OPEN.getStatusValue());
                continue;
            }
            if (RequestManagementUtil.isAllSamplesReceived(requestId, this.getQueryProcessor())) {
                primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.RECEIVED.getStatusValue());
                continue;
            }
            SDIRequest sdirequest = new SDIRequest();
            sdirequest.setSDCid(SDCID);
            sdirequest.setKeyid1List(primary.getColumnValues(COLUMN_KEYID1, ";"));
            sdirequest.setRequestItem("approval");
            DataSet approvals = this.getSDIProcessor().getSDIData(sdirequest).getDataset("approval");
            boolean acceptanceApprovalPresent = false;
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("approvalfunction", "Acceptance");
            findMap.put("keyid1", requestId);
            if (approvals.findRow(findMap) >= 0) {
                acceptanceApprovalPresent = true;
            }
            if (acceptanceApprovalPresent) {
                primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue());
                continue;
            }
            primary.setString(i, "requeststatus", RequestManagementUtil.RequestStatus.INITIAL.getStatusValue());
        }
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("keyid1", primary.getColumnValues(COLUMN_KEYID1, ";"));
        props.put("sdcid", SDCID);
        props.put("ready", "N");
        props.put("approvalfunction", "Release");
        this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
    }

    private void cancelUnCancelChildren(DataSet primary, boolean isCancel) throws SapphireException {
        String cancelBehavior = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom").getProperty("cancelactionbehavior", "");
        String requests = primary.getColumnValues(COLUMN_KEYID1, "','");
        if (cancelBehavior.equalsIgnoreCase("Cancel all Children") || cancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
            DataSet sdidataDS;
            ActionBlock actionBlock = new ActionBlock();
            StringBuffer dsSql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            dsSql.append("SELECT s_sampleid FROM s_sample WHERE requestid IN (").append(safeSQL.addIn(requests)).append(")");
            if (!isCancel) {
                dsSql.append(" AND samplestatus = 'Cancelled'");
            } else if (cancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                dsSql.append(" AND samplestatus IN ('Initial', 'InProgress', 'Received')");
            }
            DataSet samplesDS = this.getQueryProcessor().getPreparedSqlDataSet(dsSql.toString(), safeSQL.getValues());
            if (samplesDS != null && samplesDS.size() > 0) {
                PropertyList props;
                if (isCancel) {
                    props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", samplesDS.getColumnValues("s_sampleid", ";"));
                    props.setProperty("samplestatus", "Cancelled");
                    props.setProperty("statusrollup", "false");
                    props.setProperty("tracelogid", primary.getValue(0, "tracelogid", ""));
                    actionBlock.setAction("CancelSample", "EditSDI", "1", props);
                } else {
                    props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", samplesDS.getColumnValues("s_sampleid", ";"));
                    props.setProperty("statuscolumn", "samplestatus");
                    props.setProperty("validatestatuscolumnvalue", "Cancelled");
                    props.setProperty("statusrollup", "false");
                    props.setProperty("tracelogid", primary.getValue(0, "tracelogid", ""));
                    actionBlock.setAction("UnCancelSample", "UndoSDIColumnValue", "1", props);
                }
            }
            safeSQL.reset();
            dsSql = new StringBuffer();
            dsSql.append("SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset ");
            dsSql.append(" FROM sdidata WHERE sdcid = 'Request' and keyid1  IN (").append(safeSQL.addIn(requests)).append(")");
            dsSql.append(" AND (sourceworkitemid is null OR sourceworkitemid  = '') ");
            if (!isCancel) {
                dsSql.append(" AND s_datasetstatus = 'Cancelled'");
            }
            if ((sdidataDS = this.getQueryProcessor().getPreparedSqlDataSet(dsSql.toString(), safeSQL.getValues())) != null && sdidataDS.size() > 0) {
                PropertyList props;
                if (isCancel) {
                    props = new PropertyList();
                    props.setProperty("sdcid", sdidataDS.getValue(0, "sdcid", ""));
                    props.setProperty("keyid1", sdidataDS.getColumnValues("keyid1", ";"));
                    props.setProperty("s_datasetstatus", "Cancelled");
                    props.setProperty("paramlistid", sdidataDS.getColumnValues("paramlistid", ";"));
                    props.setProperty("paramlistversionid", sdidataDS.getColumnValues("paramlistversionid", ";"));
                    props.setProperty("variantid", sdidataDS.getColumnValues("variantid", ";"));
                    props.setProperty("dataset", sdidataDS.getColumnValues("dataset", ";"));
                    if (cancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                        props.setProperty("editincompleteonly", "Y");
                    }
                    props.setProperty("tracelogid", primary.getValue(0, "tracelogid", ""));
                    actionBlock.setAction("CancelDataSet", "EditDataSet", "1", props);
                } else {
                    props = new PropertyList();
                    props.setProperty("sdcid", sdidataDS.getValue(0, "sdcid", ""));
                    props.setProperty("keyid1", sdidataDS.getColumnValues("keyid1", ";"));
                    props.setProperty("keyid2", sdidataDS.getColumnValues("keyid2", ";"));
                    props.setProperty("keyid3", sdidataDS.getColumnValues("keyid3", ";"));
                    props.setProperty("statuscolumn", "s_datasetstatus");
                    props.setProperty("validatestatuscolumnvalue", "Cancelled");
                    props.setProperty("paramlistid", sdidataDS.getColumnValues("paramlistid", ";"));
                    props.setProperty("paramlistversionid", sdidataDS.getColumnValues("paramlistversionid", ";"));
                    props.setProperty("variantid", sdidataDS.getColumnValues("variantid", ";"));
                    props.setProperty("dataset", sdidataDS.getColumnValues("dataset", ";"));
                    props.setProperty("propsmatch", "Y");
                    props.setProperty("tracelogid", primary.getValue(0, "tracelogid", ""));
                    actionBlock.setAction("UnCancelDataSet", "UndoSDIColumnValue", "1", props);
                }
            }
            safeSQL.reset();
            dsSql = new StringBuffer();
            dsSql.append("SELECT sdcid, keyid1, keyid2, keyid3,workitemid, workiteminstance ");
            dsSql.append("  FROM sdiworkitem WHERE sdcid = 'Request' and keyid1  IN (").append(safeSQL.addIn(requests)).append(")");
            if (!isCancel) {
                dsSql.append(" AND workitemstatus = 'Cancelled'");
            } else if (cancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                dsSql.append(" AND workitemstatus IN ('Initial', 'InProgress', 'DataEntered', 'Released')");
            }
            DataSet sdiworkitemDS = this.getQueryProcessor().getPreparedSqlDataSet(dsSql.toString(), safeSQL.getValues());
            if (sdiworkitemDS != null && sdiworkitemDS.size() > 0) {
                if (isCancel) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", sdiworkitemDS.getValue(0, "sdcid", ""));
                    props.setProperty("keyid1", sdiworkitemDS.getColumnValues("keyid1", ";"));
                    props.setProperty("workitemid", sdiworkitemDS.getColumnValues("workitemid", ";"));
                    props.setProperty("workiteminstance", sdiworkitemDS.getColumnValues("workiteminstance", ";"));
                    props.setProperty("propsmatch", "Y");
                    props.setProperty("syncsdistatus", "N");
                    props.setProperty("syncsdiworkitemgroupstatus", "N");
                    props.setProperty("tracelogid", primary.getValue(0, "tracelogid", ""));
                    actionBlock.setAction("CancelWorkItem", "CancelSDIWorkItem", "1", props);
                } else {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", sdiworkitemDS.getValue(0, "sdcid", ""));
                    props.setProperty("keyid1", sdiworkitemDS.getColumnValues("keyid1", ";"));
                    props.setProperty("keyid2", sdiworkitemDS.getColumnValues("keyid2", ";"));
                    props.setProperty("keyid3", sdiworkitemDS.getColumnValues("keyid3", ";"));
                    props.setProperty("statuscolumn", "workitemstatus");
                    props.setProperty("validatestatuscolumnvalue", "Cancelled");
                    props.setProperty("workitemid", sdiworkitemDS.getColumnValues("workitemid", ";"));
                    props.setProperty("workiteminstance", sdiworkitemDS.getColumnValues("workiteminstance", ";"));
                    props.setProperty("propsmatch", "Y");
                    props.setProperty("tracelogid", primary.getValue(0, "tracelogid", ""));
                    actionBlock.setAction("UnCancelWorkItem", "UndoSDIColumnValue", "1", props);
                }
            }
            this.getActionProcessor().processActionBlock(actionBlock);
        }
    }

    private void addSamples(String requestId, Set<String> requestItems, String nextRequestStatus) throws SapphireException {
        SDIRequest sdirequest = new SDIRequest();
        sdirequest.setSDCid("LV_RequestItem");
        sdirequest.setQueryFrom("s_requestitem");
        sdirequest.setRequestItem("primary");
        sdirequest.setQueryWhere("requestid='" + SafeSQL.encodeForSQL(requestId, this.database.isOracle()) + "'");
        DataSet dsRequest = this.getSDIProcessor().getSDIData(sdirequest).getDataset("primary");
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select * from s_request where s_requestid=").append(safeSQL.addVar(requestId));
        Calendar requestDueDate = null;
        DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (dataSet != null && !dataSet.isEmpty()) {
            requestDueDate = dataSet.getCalendar(0, "duedt");
        }
        PropertyList editRequestItems = new PropertyList();
        ActionBlock actionBlock = new ActionBlock();
        for (int i = 0; i < dsRequest.getRowCount(); ++i) {
            String product = dsRequest.getValue(i, "productid", "");
            String productVersion = dsRequest.getValue(i, "productversionid", "");
            int copies = Integer.parseInt(dsRequest.getValue(i, "itemcount", "0"));
            String requestItemId = dsRequest.getValue(i, "s_requestitemid", "");
            if (requestItems != null && requestItems.size() != 0 && !requestItems.contains(requestItemId) || !StringUtil.getYN(dsRequest.getValue(i, "appliedflag"), "N").equals("N") || copies <= 0) continue;
            StringBuffer sourceKeyid1 = new StringBuffer();
            StringBuffer sourceKeyid2 = new StringBuffer();
            for (int j = 0; j < copies; ++j) {
                sourceKeyid1.append(";").append(product);
                sourceKeyid2.append(";").append(productVersion);
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", dsRequest.getValue(i, "templatesdcid", "Sample"));
            props.setProperty("templateid", dsRequest.getValue(i, "templatekeyid1", ""));
            props.setProperty("templatekeyid1", dsRequest.getValue(i, "templatekeyid1", ""));
            props.setProperty("templatekeyid2", dsRequest.getValue(i, "templatekeyid2", ""));
            props.setProperty("templatekeyid3", dsRequest.getValue(i, "templatekeyid3", ""));
            props.setProperty("copies", copies + "");
            props.setProperty("productid", product);
            props.setProperty("productversionid", productVersion);
            props.setProperty("requestid", requestId);
            props.setProperty("requestitemid", requestItemId);
            props.setProperty("classification", SDCID);
            props.setProperty("applyworkitems", "Y");
            if (requestDueDate != null) {
                props.setProperty("duedt", new M18NUtil(this.getConnectionInfo()).format(requestDueDate));
            }
            actionBlock.setAction(requestItemId + " AddSDI", "AddSDI", "1", props);
            editRequestItems.setProperty("keyid1", editRequestItems.getProperty("keyid1", "") + ";" + requestItemId);
            editRequestItems.setProperty("appliedflag", editRequestItems.getProperty("appliedflag", "") + ";Y");
        }
        if (editRequestItems.getProperty("keyid1", "").length() > 0) {
            editRequestItems.setProperty("sdcid", "LV_RequestItem");
            editRequestItems.setProperty("keyid1", editRequestItems.getProperty("keyid1", ";").substring(1));
            editRequestItems.setProperty("appliedflag", editRequestItems.getProperty("appliedflag", "").substring(1));
            actionBlock.setAction(requestId + "SetAppliedFlag", "EditSDI", "1", editRequestItems);
        }
        this.getActionProcessor().processActionBlock(actionBlock);
        StringBuffer newSdcIdBuffer = new StringBuffer();
        StringBuffer newKeyid1Buffer = new StringBuffer();
        StringBuffer newKeyid2Buffer = new StringBuffer();
        StringBuffer newKeyid3Buffer = new StringBuffer();
        StringBuffer requestDetailIdBuffer = new StringBuffer();
        String[] appliedRequestItemArray = StringUtil.split(editRequestItems.getProperty("keyid1", ""), ";");
        for (int i = 0; i < appliedRequestItemArray.length; ++i) {
            String requestItemId = appliedRequestItemArray[i];
            if (requestItemId.length() <= 0) continue;
            int itemCount = Integer.parseInt(actionBlock.getActionProperty(requestItemId + " AddSDI", "copies"));
            newKeyid1Buffer.append(";").append(actionBlock.getActionProperty(requestItemId + " AddSDI", "newkeyid1"));
            newKeyid2Buffer.append(";").append(actionBlock.getActionProperty(requestItemId + " AddSDI", "newkeyid2"));
            newKeyid3Buffer.append(";").append(actionBlock.getActionProperty(requestItemId + " AddSDI", "newkeyid3"));
            String sdcId = actionBlock.getActionProperty(requestItemId + " AddSDI", "sdcid");
            for (int j = 0; j < itemCount; ++j) {
                newSdcIdBuffer.append(";").append(sdcId);
            }
            requestDetailIdBuffer.append(";").append(this.getRequestDetails(requestId, requestItemId, itemCount));
        }
        if (requestDetailIdBuffer.length() > 1) {
            PropertyList setRequestDetailsProps = new PropertyList();
            setRequestDetailsProps.setProperty("sdcid", "LV_RequestItemDetail");
            setRequestDetailsProps.setProperty("keyid1", requestDetailIdBuffer.substring(1));
            setRequestDetailsProps.setProperty("linkkeyid1", newKeyid1Buffer.substring(1));
            setRequestDetailsProps.setProperty("linkkeyid2", newKeyid2Buffer.substring(1));
            setRequestDetailsProps.setProperty("linkkeyid3", newKeyid3Buffer.substring(1));
            setRequestDetailsProps.setProperty("linksdcid", newSdcIdBuffer.substring(1));
            setRequestDetailsProps.setProperty("operation", "setsamplefk;copysdidetails");
            setRequestDetailsProps.setProperty("operation_requestid", requestId);
            setRequestDetailsProps.setProperty("nextrequeststatus", nextRequestStatus);
            this.getActionProcessor().processAction("EditSDI", "1", setRequestDetailsProps);
        }
    }

    private String getRequestDetails(String requestId, String requestItemId, int itemCount) throws ActionException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT s_requestitemdetailid,requestid,requestitemid FROM s_requestitemdetail WHERE requestid = " + safeSQL.addVar(requestId) + " AND " + "requestitemid" + " = " + safeSQL.addVar(requestItemId) + " ORDER BY USERSEQUENCE";
        DataSet requestDetailDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        int requestDetailCount = requestDetailDs.getRowCount();
        if (requestDetailCount < itemCount) {
            int copies = itemCount - requestDetailCount;
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_RequestItemDetail");
            props.setProperty("copies", copies + "");
            props.setProperty("requestid", requestId);
            props.setProperty("requestitemid", requestItemId);
            this.getActionProcessor().processAction("AddSDI", "1", props);
        } else if (requestDetailCount > itemCount) {
            int extra = requestDetailCount - itemCount;
            StringBuffer requestDetailToDeleteKeyid1 = new StringBuffer();
            for (int i = 0; i < extra; ++i) {
                requestDetailToDeleteKeyid1.append(";").append(requestDetailDs.getValue(i, "s_requestitemdetailid", ""));
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_RequestItemDetail");
            props.setProperty("keyid1", requestDetailToDeleteKeyid1.substring(1));
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
        requestDetailDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return requestDetailDs.getColumnValues("s_requestitemdetailid", ";");
    }

    private void addAdhocSamples(DataSet primary, PropertyList actionProps) throws SapphireException {
        String product = actionProps.getProperty("productid", "");
        String productVersion = actionProps.getProperty("productversion", "");
        int copies = Integer.parseInt(actionProps.getProperty("itemcount", "1"));
        String templateSdcId = actionProps.getProperty("templatesdcid", "Sample");
        String templateKeyid1 = actionProps.getProperty("templatekeyid1", "");
        String templateKeyid2 = actionProps.getProperty("templatekeyid2", "");
        String templateKeyid3 = actionProps.getProperty("templatekeyid3", "");
        StringBuffer sourceKeyid1 = new StringBuffer();
        StringBuffer sourceKeyid2 = new StringBuffer();
        for (int j = 0; j < copies; ++j) {
            sourceKeyid1.append(";").append(product);
            sourceKeyid2.append(";").append(productVersion);
        }
        ActionBlock actionBlock = new ActionBlock();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String requestId = primary.getValue(i, COLUMN_KEYID1, "");
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", templateSdcId);
            props.setProperty("templateid", templateKeyid1);
            props.setProperty("templatekeyid1", templateKeyid1);
            props.setProperty("templatekeyid2", templateKeyid2);
            props.setProperty("templatekeyid3", templateKeyid3);
            props.setProperty("copies", copies + "");
            props.setProperty("productid", product);
            props.setProperty("productversionid", productVersion);
            props.setProperty("requestid", requestId);
            props.setProperty("classification", SDCID);
            props.setProperty("applyworkitems", "Y");
            props.setProperty("sourcesdcid", "Product");
            props.setProperty("sourcekeyid1", sourceKeyid1.substring(1));
            props.setProperty("sourcekeyid2", sourceKeyid2.substring(1));
            props.setProperty("copydataset", "Y");
            props.setProperty("copyspec", "Y");
            props.setProperty("copyworkitem", "Y");
            props.setProperty("usecurrentversion", "Y");
            props.setProperty("applysourceworkitem", "Y");
            actionBlock.setAction(requestId + i, "CopySDIDetail", "1", props);
        }
        this.getActionProcessor().processActionBlock(actionBlock);
    }

    @Override
    public void postApprove(DataSet approve) throws SapphireException {
        if (approve.getRowCount() > 0) {
            RequestManagementUtil.RequestOperation operation = approve.getValue(0, "approvalfunction", "Acceptance").equalsIgnoreCase("Acceptance") ? RequestManagementUtil.RequestOperation.ACCEPT : RequestManagementUtil.RequestOperation.RELEASE;
            PropertyList props = new PropertyList();
            props.put("sdcid", approve.getValue(0, "sdcid", SDCID));
            props.put("keyid1", approve.getValue(0, "keyid1"));
            props.put("approvalevaluation", approve.getValue(0, "approvalflag", "F"));
            props.put("tracelogid", approve.getValue(0, "tracelogid", ""));
            props.put("operation", operation.getOperationValue());
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
    }

    private void findCandidateForAutoRelease(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String autoRelease;
            String requestStatus = primary.getValue(i, "requeststatus", "");
            if (!requestStatus.equals(RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue()) || !(autoRelease = beforeEditImagePrimary.getValue(i, "autoreleaseflag", "")).trim().equals("Y")) continue;
            primary.setString(i, "skipapprovalrulecheck", "Y");
        }
    }

    static {
        whoDoneIt.addColumnPair("cancelleddt", "cancelledby", "requeststatus", "=", RequestManagementUtil.RequestItemStatus.CANCELLED.getStatusValue());
        whoDoneIt.addColumnPair("cancelleddt", "cancelledby", "requeststatus", "!=", RequestManagementUtil.RequestItemStatus.CANCELLED.getStatusValue());
    }
}


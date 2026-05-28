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
package com.labvantage.sapphire.modules.issuemanagement;

import com.labvantage.sapphire.admin.logfileviewer.LogViewerUtil;
import com.labvantage.sapphire.modules.issuemanagement.IssueManagementUtil;
import com.labvantage.sapphire.platform.Configuration;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.attachment.Attachment;
import sapphire.ext.BaseIssueHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class IssueManagementAjaxUtil
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision:  $";
    public static final String PROPERTY_METHOD = "method";
    public static final String METHOD_GETMANAGERS = "getIssueManagers";
    public static final String METHOD_STORETOSESSION = "storeToSession";
    public static final String METHOD_GENSUBMITID = "generateSubmitId";
    public static final String METHOD_DELETESUBMITID = "doDeleteSubmissionId";
    public static final String METHOD_SUBMITISSUE = "submitIssue";
    public static final String METHOD_CHECKSUBMITPROGRESS = "checkSubmitProgress";
    public static final String METHOD_ATTACHLOG = "atttachLog";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block12: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            String method = ajaxResponse.getRequestParameter(PROPERTY_METHOD);
            try {
                if (METHOD_GETMANAGERS.equalsIgnoreCase(method)) {
                    this.doGetIssueManagers(ajaxResponse, request);
                    break block12;
                }
                if (METHOD_STORETOSESSION.equalsIgnoreCase(method)) {
                    this.doStoreToSession(ajaxResponse, request);
                    break block12;
                }
                if (METHOD_GENSUBMITID.equalsIgnoreCase(method)) {
                    this.doGetSubmissionId(ajaxResponse);
                    break block12;
                }
                if (METHOD_DELETESUBMITID.equalsIgnoreCase(method)) {
                    this.doDeleteSubmissionId(ajaxResponse);
                    break block12;
                }
                if (METHOD_SUBMITISSUE.equalsIgnoreCase(method)) {
                    this.doSubmitIssue(ajaxResponse, request);
                    break block12;
                }
                if (METHOD_CHECKSUBMITPROGRESS.equalsIgnoreCase(method)) {
                    this.doCheckSubmitProgress(ajaxResponse);
                    break block12;
                }
                if (METHOD_ATTACHLOG.equalsIgnoreCase(method)) {
                    this.doAttachLog(ajaxResponse);
                    break block12;
                }
                throw new SapphireException("Method un-defined" + method);
            }
            catch (Exception e) {
                ajaxResponse.setError(e.getMessage());
            }
            finally {
                ajaxResponse.print();
            }
        }
    }

    private void doGetIssueManagers(AjaxResponse ajaxResponse, HttpServletRequest request) throws SapphireException {
        String currentSysUserid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId();
        DataSet repositories = new DataSet();
        repositories = IssueManagementUtil.getAllIssueRepositories(this.getQueryProcessor(), request, currentSysUserid);
        ajaxResponse.addCallbackArgument("issueManagerList", repositories);
    }

    private void doStoreToSession(AjaxResponse ajaxResponse, HttpServletRequest request) throws SapphireException {
        String issueRepositoryId = ajaxResponse.getRequestParameter("issuerepositoryid");
        String userName = ajaxResponse.getRequestParameter("username");
        String password = ajaxResponse.getRequestParameter("password");
        String currentSysUserid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId();
        HttpSession session = request.getSession();
        IssueManagementUtil.setLoginAttributeValue(session, issueRepositoryId, currentSysUserid, userName, password);
        ajaxResponse.addCallbackArgument("message", "");
    }

    private void doGetSubmissionId(AjaxResponse ajaxResponse) throws SapphireException {
        String submissionId = BaseIssueHandler.generateSubmissionId(this.getConnectionId());
        ajaxResponse.addCallbackArgument("submissionId", submissionId);
    }

    private void doDeleteSubmissionId(AjaxResponse ajaxResponse) throws SapphireException {
        String submissionId = ajaxResponse.getRequestParameter("submissionId");
        BaseIssueHandler.deleteSubmissionId(submissionId);
    }

    private void doSubmitIssue(AjaxResponse ajaxResponse, HttpServletRequest request) throws SapphireException {
        String currentSysUserid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId();
        String issueId = ajaxResponse.getRequestParameter("issueid");
        String issueRepositoryidId = ajaxResponse.getRequestParameter("issuerepositoryid");
        String submissionId = ajaxResponse.getRequestParameter("submissionId");
        String credentials = IssueManagementUtil.getCredentialsFromSession(request, issueRepositoryidId, currentSysUserid);
        PropertyList props = new PropertyList();
        props.setProperty("issueid", issueId);
        props.setProperty("issuerepositoryid", issueRepositoryidId);
        props.setProperty("userpassword", credentials);
        props.setProperty("submissionid", submissionId);
        this.getActionProcessor().processAction("SubmitIssue", "1", props);
        String issueRefId = props.getProperty("issuerefid");
        ajaxResponse.addCallbackArgument("issueRefId", issueRefId);
    }

    private void doCheckSubmitProgress(AjaxResponse ajaxResponse) throws SapphireException {
        String submissionId = ajaxResponse.getRequestParameter("submissionid", "");
        ajaxResponse.addCallbackArgument("message", BaseIssueHandler.getSubmitProgress(submissionId));
    }

    private void doAttachLog(AjaxResponse ajaxResponse) throws SapphireException, IOException {
        String issueId = ajaxResponse.getRequestParameter("issueid", "");
        String attPolicyNodeId = ajaxResponse.getRequestParameter("attachmentpolicynodeid", "Sapphire Custom");
        String logSnapshotId = ajaxResponse.getRequestParameter("logsnapshotid", "");
        if (issueId.length() == 0 || logSnapshotId.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Mandatory Property Missing."));
        }
        String hostId = Configuration.getInstance().getHostid();
        String logSnapshotFileName = "";
        String[] dbids = LogViewerUtil.getConfiguredDBList();
        String loogedInDB = LogViewerUtil.fetchDBid(this.getConnectionid());
        boolean found = false;
        for (int len = 0; len < dbids.length; ++len) {
            String dbID = dbids[len];
            if (!loogedInDB.equalsIgnoreCase(dbID.trim())) continue;
            found = true;
            break;
        }
        if (found) {
            loogedInDB = loogedInDB.replaceAll("_", ".").trim();
            logSnapshotFileName = (logSnapshotId + "-" + hostId).toUpperCase() + "-" + loogedInDB + ".zip";
        } else {
            logSnapshotFileName = (logSnapshotId + "-" + hostId).toUpperCase() + ".zip";
        }
        DataSet logSnapshotInfo = LogViewerUtil.getSnapshots(this.getConnectionid(), logSnapshotFileName);
        if (logSnapshotInfo.getRowCount() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Log Snapshot Info. Not found: ") + logSnapshotFileName);
        }
        String absoluteFileName = logSnapshotInfo.getString(0, "absolutefilename", "");
        if (absoluteFileName.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Log Snapshot File Not found: ") + logSnapshotFileName);
        }
        Path filePath = Paths.get(absoluteFileName, new String[0]);
        Attachment attachment = Attachment.getAttachment("LV_Issue", issueId, null, null);
        attachment.setDescription(filePath.getFileName().toString());
        attachment.setAttachmentClass("Issue Log Snapshot");
        attachment.setFilename(filePath.toString());
        attachment.setSourceFilename(filePath.getFileName().toString());
        attachment.setAttachmentType(Attachment.AttachmentType.FILE);
        AttachmentProcessor ap = new AttachmentProcessor(this.getConnectionid());
        ap.addSDIAttachment(attachment, false, false, attPolicyNodeId);
    }
}


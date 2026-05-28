/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 *  org.apache.commons.codec.binary.StringUtils
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Signature
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
        worksheetItemOptions.setIsAlwaysLive(true);
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getHTML(true);
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getHTML(false);
    }

    private String getHTML(boolean export) throws SapphireException {
        String signature;
        String status;
        String source = this.config.getProperty("source", "Control");
        String signformat = this.config.getProperty("contentcompletesignature", "Signed by [username] on [date]: [reason]").trim();
        String signformatnoreason = this.config.getProperty("contentcompletesignaturenoreason", "Signed by [username] on [date]").trim();
        boolean onlyshowsignaturewhenstatuscomplete = this.config.getProperty("onlyshowsignaturewhenstatuscomplete").equals("Y");
        boolean showlineabove = this.config.getProperty("showlineabove").equals("Y");
        boolean showlinebelow = this.config.getProperty("showlinebelow").equals("Y");
        String inprogressmessage = this.config.getProperty("inprogressmessage", this.isTemplate() ? "In Progress Message (Blank)" : "").trim();
        String pendingapprovalmessage = this.config.getProperty("pendingapprovalmessage", this.isTemplate() ? "Pending Approval Message (Blank)" : "").trim();
        String approvalformat = this.config.getProperty("approvalformat").trim();
        Map<String, String> author = this.getWorksheetAuthor();
        DataSet details = null;
        boolean isPendingApproval = false;
        boolean isComplete = false;
        boolean isWSComplete = false;
        String wscompletionby = "";
        String wscompletionbydesc = "";
        if (source.equals("W")) {
            details = this.getQueryProcessor().getPreparedSqlDataSet("SELECT contentcompletedby, contentcompleteddt, contentcompletedreason, worksheetstatus, sysuser.sysuserdesc, tracelog.activity  FROM worksheet LEFT OUTER JOIN sysuser ON sysuser.sysuserid=contentcompletedby LEFT OUTER JOIN tracelog ON contentcompletedtracelogid=tracelog.tracelogid  WHERE worksheetid=? AND worksheetversionid=?", (Object[])new String[]{this.getWorksheetId(), this.getWorksheetVersionId()});
            status = details.getValue(0, "worksheetstatus");
            isComplete = status.equals("Complete");
            isWSComplete = status.equals("Complete");
            if (isWSComplete) {
                wscompletionby = details.getValue(0, "contentcompletedby");
                wscompletionbydesc = details.getValue(0, "sysuserdesc");
            }
            isPendingApproval = status.equals("PendingApproval");
        } else if (source.equals("S")) {
            details = this.getQueryProcessor().getPreparedSqlDataSet("SELECT contentcompletedby, contentcompleteddt, contentcompletedreason, sectionstatus, sectionlevel, sysuser.sysuserdesc, tracelog.activity FROM worksheetsection LEFT OUTER JOIN sysuser ON sysuser.sysuserid=contentcompletedby LEFT OUTER JOIN tracelog ON contentcompletedtracelogid=tracelog.tracelogid  WHERE worksheetsectionid=? AND worksheetsectionversionid=?", (Object[])new String[]{this.getWorksheetSectionId(), this.getWorksheetSectionVersionId()});
            if (details.getValue(0, "sectionlevel").equals("0")) {
                return "ERROR: This control is not in a section.";
            }
            status = details.getValue(0, "sectionstatus");
            isComplete = status.equals("Complete");
            isPendingApproval = status.equals("PendingApproval");
        } else if (source.equalsIgnoreCase("PC") || source.equals("NC")) {
            ArrayList list = null;
            String propertytreeid = this.config.getProperty("propertytreeid");
            if (source.equalsIgnoreCase("NC")) {
                list = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid, usersequence FROM worksheetitem WHERE worksheetsectionid=? AND worksheetsectionversionid=? " + (propertytreeid.length() > 0 ? " AND propertytreeid='" + propertytreeid + "'" : "") + " AND usersequence > ( select usersequence from worksheetitem WHERE worksheetitemid=? AND worksheetitemversionid=?) ORDER BY usersequence", new Object[]{this.getWorksheetSectionId(), this.getWorksheetSectionVersionId(), this.getWorksheetItemId(), this.getWorksheetItemVersionId()});
            } else if (source.equalsIgnoreCase("PC")) {
                list = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid, usersequence FROM worksheetitem WHERE worksheetsectionid=? AND worksheetsectionversionid=? " + (propertytreeid.length() > 0 ? " AND propertytreeid='" + propertytreeid + "'" : "") + " AND usersequence < ( select usersequence from worksheetitem WHERE worksheetitemid=? AND worksheetitemversionid=?) ORDER BY usersequence desc ", new Object[]{this.getWorksheetSectionId(), this.getWorksheetSectionVersionId(), this.getWorksheetItemId(), this.getWorksheetItemVersionId()});
            }
            if (list != null && list.size() > 0) {
                String worksheetitemid = ((DataSet)list).getValue(0, "worksheetitemid");
                String worksheetitemversionid = ((DataSet)list).getValue(0, "worksheetitemversionid");
                details = this.getQueryProcessor().getPreparedSqlDataSet("SELECT contentcompletedby, contentcompleteddt, contentcompletedreason, itemstatus, sysuser.sysuserdesc, tracelog.activity FROM worksheetitem LEFT OUTER JOIN sysuser ON sysuser.sysuserid=contentcompletedby LEFT OUTER JOIN tracelog ON contentcompletedtracelogid=tracelog.tracelogid  WHERE worksheetitemid=? AND worksheetitemversionid=?", (Object[])new String[]{worksheetitemid, worksheetitemversionid});
                String status2 = details.getValue(0, "itemstatus");
                isComplete = status2.equals("Complete");
            } else {
                return "ERROR: Unable to find matching Control";
            }
        }
        if (details == null) {
            return "ERROR: Control requires configuration";
        }
        StringBuilder html = new StringBuilder();
        String by = details.getValue(0, "contentcompletedby");
        String reason = details.getValue(0, "contentcompletedreason");
        String activity = details.getValue(0, "activity");
        String string = signature = reason.length() > 0 ? signformat : signformatnoreason;
        if ((isComplete || isPendingApproval && !onlyshowsignaturewhenstatuscomplete) && (by.length() > 0 || signature.contains("[username]"))) {
            String name = details.getValue(0, "sysuserdesc");
            String sysuserid = details.getValue(0, "contentcompletedby");
            String date = details.getValue(0, "contentcompleteddt");
            signature = StringUtil.replaceAll(signature, "[username]", name);
            signature = StringUtil.replaceAll(signature, "[userid]", sysuserid);
            signature = StringUtil.replaceAll(signature, "[sysuserid]", sysuserid);
            signature = StringUtil.replaceAll(signature, "[date]", date);
            signature = StringUtil.replaceAll(signature, "[reason]", reason);
            signature = StringUtil.replaceAll(signature, "[activity]", activity);
            html.append(SafeHTML.encodeForHTML(signature, true));
        }
        if (html.length() == 0) {
            if (isPendingApproval) {
                html.append(SafeHTML.encodeForHTML(pendingapprovalmessage, true));
            } else if (!isComplete) {
                html.append(SafeHTML.encodeForHTML(inprogressmessage, true));
            }
        }
        boolean showapproversignature = this.config.getProperty("showapproversignature", "N").equalsIgnoreCase("Y");
        boolean showcompletionsignature = this.config.getProperty("showcompletersignature", "N").equalsIgnoreCase("Y");
        boolean showconfirmationsignature = this.config.getProperty("showconfirmationsignature", "N").equalsIgnoreCase("Y");
        boolean showauthorsignature = this.config.getProperty("showauthorsignature", "N").equalsIgnoreCase("Y");
        String reviewedby = "";
        String reviewedbyId = "";
        StringBuilder signatureHTML = new StringBuilder();
        signatureHTML.append("<br><div><table>");
        int signPerRow = 3;
        int signCount = 0;
        if (showauthorsignature) {
            if (signCount == 0 || signCount == signPerRow) {
                signatureHTML.append("<tr>");
            }
            ++signCount;
            String authorid = "";
            String authordes = "";
            for (Map.Entry<String, String> auth : author.entrySet()) {
                authorid = auth.getKey();
                authordes = auth.getValue();
            }
            signatureHTML.append("<td><div id=\"digitalsign_createdby\" style=\"width:200pt; height:50pt;margin-right: 30pt;padding-bottom: 20pt; border:1px solid black; \">Created By - <div>" + authordes + " </div> <div>" + this.getUserSignatureHTML(authorid) + "</div></td></div>");
            if (signCount == signPerRow) {
                signatureHTML.append("</tr>");
            }
        }
        if (source.equals("W") || source.equals("S")) {
            boolean showApprovals = false;
            switch (this.config.getProperty("showapprovals", "N")) {
                case "N": {
                    showApprovals = false;
                    break;
                }
                case "A": {
                    showApprovals = true;
                    break;
                }
                case "S": {
                    showApprovals = isComplete || isPendingApproval;
                    break;
                }
                case "C": {
                    showApprovals = isComplete;
                }
            }
            if (showApprovals) {
                Object[] objectArray;
                QueryProcessor queryProcessor = this.getQueryProcessor();
                if (source.equals("W")) {
                    Object[] objectArray2 = new String[3];
                    objectArray2[0] = "LV_Worksheet";
                    objectArray2[1] = this.getWorksheetId();
                    objectArray = objectArray2;
                    objectArray2[2] = this.getWorksheetVersionId();
                } else {
                    String[] stringArray = new String[3];
                    stringArray[0] = "LV_WorksheetSection";
                    stringArray[1] = this.getWorksheetSectionId();
                    objectArray = stringArray;
                    stringArray[2] = this.getWorksheetSectionVersionId();
                }
                DataSet approvals = queryProcessor.getPreparedSqlDataSet("SELECT sdiapprovalstep.*, sysuser.sysuserdesc FROM sdiapprovalstep LEFT OUTER JOIN sysuser ON sysuser.sysuserid=sdiapprovalstep.reviewedby WHERE sdcid=? AND keyid1=? AND keyid2=? order by sdiapprovalstep.usersequence, sdiapprovalstep.approvalstep", objectArray);
                if (approvals.size() > 0) {
                    html.append("<table style=\"padding-left: 15px\">");
                    for (int i = 0; i < approvals.size(); ++i) {
                        html.append("<tr><td>");
                        String flag = approvals.getValue(i, "approvalflag");
                        String step = approvals.getValue(i, "approvalstep");
                        if (flag.equals("U")) {
                            html.append("Pending approval by " + step);
                        } else if (flag.equals("P")) {
                            reviewedby = approvals.getValue(i, "sysuserdesc");
                            reviewedbyId = approvals.getValue(i, "reviewedby");
                            String message = approvalformat;
                            message = StringUtil.replaceAll(message, "[userid]", approvals.getValue(i, "reviewedby"));
                            message = StringUtil.replaceAll(message, "[sysuserid]", approvals.getValue(i, "reviewedby"));
                            message = StringUtil.replaceAll(message, "[username]", approvals.getValue(i, "sysuserdesc"));
                            message = StringUtil.replaceAll(message, "[date]", approvals.getValue(i, "revieweddt"));
                            message = StringUtil.replaceAll(message, "[step]", step);
                            html.append(SafeHTML.encodeForHTML(message, true));
                            if (showapproversignature) {
                                if (signCount == 0 || signCount == signPerRow) {
                                    signCount = 0;
                                    signatureHTML.append("<tr>");
                                }
                                signatureHTML.append("<td><div style=\"width:200pt; height:50pt;margin-right: 30pt;padding-bottom: 20pt; border:1px solid black; \"> " + step + " Approved By - <div>" + reviewedby + " </div> " + this.getUserSignatureHTML(reviewedbyId) + "</div></td>");
                                if (++signCount == signPerRow) {
                                    signatureHTML.append("</tr>");
                                }
                            }
                        }
                        html.append("</td></tr>");
                    }
                    html.append("</table>");
                }
            }
        }
        if (isWSComplete && showcompletionsignature) {
            if (signCount == 0 || signCount == signPerRow) {
                signCount = 0;
                signatureHTML.append("<tr>");
            }
            signatureHTML.append("<td><div id=\"digitalsign_completedby\" style=\"width:200pt; height:50pt;margin-right: 30pt;padding-bottom: 20pt; border:1px solid black; \">Completed By - <div>" + wscompletionbydesc + " </div> <div>" + this.getUserSignatureHTML(wscompletionby) + "</div></td></div>");
            if (++signCount == signPerRow) {
                signatureHTML.append("</tr>");
            }
        }
        if (showconfirmationsignature && export) {
            if (signCount == 0 || signCount == signPerRow) {
                signCount = 0;
                signatureHTML.append("<tr>");
            }
            signatureHTML.append("<td><div id=\"digitalsign4\" style=\"width:200pt; height:50pt;margin-right: 30pt;padding-bottom: 20pt; border:1px solid black; \">Confirmed By - <div>  [Confirmation_Pending]</br></br></br></br></br></br></div></td></div>");
            if (++signCount == signPerRow) {
                signatureHTML.append("</tr>");
            }
        }
        signatureHTML.append("</table></div>");
        html.append((CharSequence)signatureHTML);
        return html.toString().length() == 0 ? "" : (showlineabove ? "<hr>" : "") + html + (showlinebelow ? "<hr>" : "");
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getViewHTML();
    }

    private Map<String, String> getWorksheetAuthor() throws SapphireException {
        HashMap<String, String> author = new HashMap<String, String>();
        DataSet authords = this.getQueryProcessor().getPreparedSqlDataSet("SELECT authorid,sysuserdesc FROM worksheet, sysuser WHERE worksheetid=? AND worksheetversionid=? AND worksheet.authorid = sysuser.sysuserid", (Object[])new String[]{this.getWorksheetId(), this.getWorksheetVersionId()});
        if (OpalUtil.isNotEmpty(authords)) {
            author.put(authords.getString(0, "authorid"), authords.getString(0, "sysuserdesc"));
        }
        return author;
    }

    private String getUserSignature(String userid) throws SapphireException {
        Attachment signatureAttachment = null;
        String sql = "SELECT attachmentnum FROM sdiattachment WHERE sdcid = 'User' AND keyid1 = ? AND attachmentclass = 'ReportSignature'";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{userid});
        if (ds.getRowCount() > 0) {
            signatureAttachment = Attachment.getAttachment("User", userid, null, null, ds.getInt(0, "attachmentnum"));
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionProcessor().getSapphireConnection().getConnectionId());
            signatureAttachment = attachmentProcessor.getSDIAttachment(signatureAttachment, Attachment.ThumbnailGeneration.DISABLED);
        }
        if (signatureAttachment == null) {
            throw new SapphireException("User Signature attachment is missing.");
        }
        return this.getImageSource(signatureAttachment);
    }

    private String getImageSource(Attachment signatureAttachment) {
        byte[] imageByteArray = signatureAttachment.getData();
        return "data:image/jpeg;base64," + StringUtils.newStringUtf8((byte[])Base64.encodeBase64((byte[])imageByteArray, (boolean)false));
    }

    private String getUserSignatureHTML(String userid) throws SapphireException {
        StringBuilder signHTML = new StringBuilder();
        try {
            signHTML.append("<img style=\"width:120pt; height:40pt;\" src=\"" + this.getUserSignature(userid) + "\"/>");
        }
        catch (SapphireException e) {
            String message = this.getTranslationProcessor().translate("No signature image on file");
            signHTML.append("<p style=\"color:red\">" + message + "</p>");
        }
        return signHTML.toString();
    }
}


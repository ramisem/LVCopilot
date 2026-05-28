/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.xpath.XPathAPI
 *  org.w3c.tidy.Tidy
 */
package com.labvantage.sapphire.modules.documents;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.documents.AutoAttachDocument;
import com.labvantage.sapphire.actions.documents.AutoLinkDocument;
import com.labvantage.sapphire.actions.documents.SetFieldValue;
import com.labvantage.sapphire.modules.documents.DocumentUserException;
import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.FormGroup;
import com.labvantage.sapphire.pageelements.controls.RichTextEditor;
import com.labvantage.sapphire.pageelements.gwt.server.ApprovalStepUtil;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.format.DateFormatter;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Document
implements DocumentConstants {
    public static final String LOGNAME = "DOCUMENT";
    public static final int VIEW_MODE = 0;
    public static final int EDIT_MODE = 1;
    public static final String SEPARATOR = "$SEP$";
    private SapphireConnection sapphireConnection;
    private SDIData documentData;
    private String rsetid;
    private String documentid;
    private String documentversionid = "1";
    private String formid;
    private String formversionid;
    private String documentstatus;
    private String versionstatus;
    private String statusmessage;
    private String documentdesc;
    private String sysuserid1;
    private String sysuserid2;
    private String returnmessage;
    private String priority;
    private String createdt;
    private String moddt;
    private String duedt;
    private boolean hasFieldErrors = false;
    private boolean hasOpenFollowups = false;
    private boolean reconcilable = false;
    private boolean approvable = false;
    private boolean documentManager = false;
    private PropertyList documentObjects;
    private PropertyListCollection fieldValues;
    private HashMap fieldValueMap = new HashMap();
    private HashMap ddefieldValueMap = new HashMap();
    private HashMap identityFieldMap = new HashMap();
    private PropertyListCollection groupValues;
    private PropertyListCollection reviewitems;
    private Form form;
    private int mode = 1;
    private Logger logger;

    private Document() {
    }

    public static Document getInstance(SapphireConnection sapphireConnection, Form form, boolean debug) {
        Document document = new Document();
        document.logger = new Logger(sapphireConnection.getConnectionId());
        document.logger.setLoggerName(LOGNAME);
        document.sapphireConnection = sapphireConnection;
        document.setFormid(form.getFormid());
        document.setFormversionid(form.getFormversionid());
        document.setDocumentdesc(form.getDocumentDescRule());
        document.setDocumentStatus("DR");
        document.form = form;
        return document;
    }

    public static Document getInstance(SapphireConnection sapphireConnection, String rsetid, boolean lock, boolean debug) throws DocumentUserException, SapphireException {
        Document document = new Document();
        document.logger = new Logger(sapphireConnection.getConnectionId());
        document.logger.setLoggerName(LOGNAME);
        SDIProcessor sdiProcessor = new SDIProcessor(sapphireConnection.getConnectionId());
        long start = System.currentTimeMillis();
        document.logger.info("Loading document with rsetid=" + rsetid);
        document.documentData = Document.loadDocument(sdiProcessor, "", "", rsetid, "", lock);
        document.logger.info("Load took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        Document.initDocument(sapphireConnection, document);
        document.logger.info("Processing took " + (System.currentTimeMillis() - start) + "ms");
        return document;
    }

    public static Document getInstance(SapphireConnection sapphireConnection, String documentid, String documentversionid, String restrictivewhere, boolean lock, boolean debug) throws DocumentUserException, SapphireException {
        Document document = new Document();
        document.logger = new Logger(sapphireConnection.getConnectionId());
        document.logger.setLoggerName(LOGNAME);
        SDIProcessor sdiProcessor = new SDIProcessor(sapphireConnection.getConnectionId());
        long start = System.currentTimeMillis();
        if (documentversionid.length() == 0 || documentversionid.equalsIgnoreCase("C")) {
            String sql = "SELECT documentversionid FROM document WHERE documentid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( documentversionid as integer ) DESC";
            QueryProcessor queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
            DataSet documents = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{documentid});
            if (documents.size() > 0) {
                documentversionid = documents.getValue(0, "documentversionid");
            } else {
                throw new SapphireException("Failed to find document '" + documentid + "'");
            }
        }
        document.logger.info("Loading document with documentid=" + documentid + ", documentversionid=" + documentversionid);
        document.documentData = Document.loadDocument(sdiProcessor, documentid, documentversionid, "", restrictivewhere, lock);
        document.logger.info("Load took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        Document.initDocument(sapphireConnection, document);
        document.logger.info("Processing took " + (System.currentTimeMillis() - start) + "ms");
        return document;
    }

    private static void initDocument(SapphireConnection sapphireConnection, Document document) throws SapphireException {
        DataSet primary = document.documentData.getDataset("primary");
        if (document.getLockedby().length() == 0) {
            document.setRsetid(document.documentData.getRsetid());
        } else {
            document.setRsetid("");
            DAMProcessor dam = new DAMProcessor(sapphireConnection.getConnectionId());
            dam.clearRSet(document.documentData.getRsetid());
        }
        document.sapphireConnection = sapphireConnection;
        document.setDocumentid(primary.getValue(0, "documentid"));
        document.setDocumentversionid(primary.getValue(0, "documentversionid"));
        document.setDocumentdesc(primary.getValue(0, "documentdesc"));
        document.setFormid(primary.getValue(0, "formid"));
        document.setFormversionid(primary.getValue(0, "formversionid"));
        document.setDocumentStatus(primary.getValue(0, "documentstatus"));
        document.setVersionStatus(primary.getValue(0, "versionstatus"));
        document.setStatusmessage(primary.getValue(0, "statusmessage"));
        document.setSysuserid1(primary.getValue(0, "sysuserid1"));
        document.setSysuserid2(primary.getValue(0, "sysuserid2"));
        document.createdt = primary.getValue(0, "createdt");
        document.moddt = primary.getValue(0, "moddt");
        document.duedt = primary.getValue(0, "duedt");
        document.priority = primary.getValue(0, "priority");
        document.setDocumentObjects(primary.getValue(0, "documentobjects"));
        PropertyList directives = new PropertyList();
        directives.setProperty("sectionbindings", document.getDocumentObjects().getPropertyList("sections"));
        document.form = Form.getInstance(sapphireConnection, document.getFormid(), document.formversionid, directives);
        document.setDocumentManager(document.form.isDocumentManager(sapphireConnection.getRoleList()));
        DataSet approval = document.documentData.getDataset("approval");
        if (approval != null && approval.size() == 1) {
            DataSet approvalsteps = document.documentData.getDataset("approvalstep");
            approvalsteps.addColumn("stepstatusflag", 0);
            ApprovalStepUtil.checkApprovalSteps(approvalsteps, sapphireConnection.getRoleList(), sapphireConnection.getSysuserId(), approval.getValue(0, "sequenceflag"), approval.getValue(0, "uniquenessflag"), null, null, false);
            for (int i = 0; i < approvalsteps.size(); ++i) {
                if (!document.isDocumentManager() && !approvalsteps.getValue(i, "stepstatusflag").equals("C") || !approvalsteps.getValue(i, "approvalflag").equals("U")) continue;
                document.setApprovable(document.getDocumentStatus().equals("PA"));
                break;
            }
        }
        document.setReconcilable(document.form.getReconciliationroleid().length() > 0 && (";" + sapphireConnection.getRoleList() + ";").indexOf(document.form.getReconciliationroleid()) > -1 || document.form.isDdeuser2recon() && !sapphireConnection.getSysuserId().equals(document.getSysuserid1()) && document.getDocumentStatus().equals("SM"));
        M18NUtil m18n = new M18NUtil(sapphireConnection);
        DataSet fielddata = document.documentData.getDataset("documentfield");
        fielddata.setM18NUtil(m18n);
        DataSet fieldreviewitems = document.documentData.getDataset("documentreviewitem");
        fieldreviewitems.setM18NUtil(m18n);
        fieldreviewitems.sort("reviewitemtype D,createdt A");
        DataSet attachments = document.documentData.getDataset("attachment");
        QueryProcessor qp = new QueryProcessor(sapphireConnection.getConnectionId());
        DataSet reviewUsers = qp.getPreparedSqlDataSet("SELECT distinct sysuserid, sysuserdesc, initials FROM sysuser su, documentreviewitem dr WHERE dr.documentid = ? AND dr.documentversionid = ? AND ( su.sysuserid = dr.createby OR su.sysuserid = dr.resolvedby OR su.sysuserid = dr.reviewitemassignto )", new Object[]{document.getDocumentid(), document.getDocumentversionid()});
        PropertyList userDetails = new PropertyList();
        for (int i = 0; i < reviewUsers.size(); ++i) {
            String sysuserid = reviewUsers.getValue(i, "sysuserid");
            userDetails.setProperty(sysuserid, reviewUsers.getValue(i, "sysuserdesc"));
        }
        HashMap<String, String> fieldFilterMap = new HashMap<String, String>();
        HashMap<String, Object> riFilterMap = new HashMap<String, Object>();
        HashMap<String, String> attachmentFindMap = new HashMap<String, String>();
        PropertyListCollection fields = document.form.getFields();
        PropertyListCollection fieldValues = new PropertyListCollection();
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList field = fields.getPropertyList(i);
            String fieldid = field.getProperty("fieldid");
            PropertyList fieldValue = new PropertyList(fieldid);
            fieldValue.setProperty("fieldid", fieldid);
            PropertyListCollection instances = new PropertyListCollection();
            fieldValue.setProperty("instances", instances);
            fieldFilterMap.put("fieldid", fieldid);
            DataSet fielddatavalues = fielddata.getFilteredDataSet(fieldFilterMap);
            fielddatavalues.sort("fieldinstance");
            if (fielddatavalues.size() > 0) {
                for (int j = 0; j < fielddatavalues.size(); ++j) {
                    PropertyList instance = new PropertyList();
                    instance.setProperty("fieldid", fieldid);
                    instance.setProperty("fieldinstance", fielddatavalues.getValue(j, "fieldinstance"));
                    instance.setProperty("enteredtext", field.getProperty("type").equals("formattedtext") ? fielddatavalues.getValue(j, "clobvalue") : fielddatavalues.getValue(j, "enteredtext"));
                    instance.setProperty("displayvalue", fielddatavalues.getValue(j, "displayvalue", instance.getProperty("enteredtext")));
                    instance.setProperty("datevalue", fielddatavalues.getValue(j, "datevalue"));
                    instance.setProperty("numericvalue", fielddatavalues.getValue(j, "numericvalue"));
                    instance.setProperty("ddeenteredtext", fielddatavalues.getValue(j, "ddeenteredtext"));
                    instance.setProperty("ddedisplayvalue", fielddatavalues.getValue(j, "ddedisplayvalue", instance.getProperty("ddeenteredtext")));
                    instance.setProperty("ddedatevalue", fielddatavalues.getValue(j, "ddedatevalue"));
                    instance.setProperty("ddenumericvalue", fielddatavalues.getValue(j, "ddenumericvalue"));
                    instance.setProperty("fieldstatus", fielddatavalues.getValue(j, "fieldstatus"));
                    if (field.getProperty("datatype").equals("date") || field.getProperty("datatype").equals("dateonly") || field.getProperty("type").equals("date")) {
                        instance.setProperty("enteredtext", instance.getProperty("datevalue"));
                        instance.setProperty("ddeenteredtext", instance.getProperty("ddedatevalue"));
                    }
                    if (field.getProperty("datatype").equals("number")) {
                        instance.setProperty("enteredtext", instance.getProperty("numericvalue"));
                        instance.setProperty("ddeenteredtext", instance.getProperty("ddenumericvalue"));
                    }
                    PropertyList binding = new PropertyList();
                    binding.setPropertyList(fielddatavalues.getValue(j, "binding"));
                    if (binding.size() > 0) {
                        instance.setProperty("binding", binding);
                        instance.setProperty("bound", "Y");
                    }
                    instances.add(instance);
                    if (!instance.getProperty("fieldstatus", "P").equals("P")) {
                        document.hasFieldErrors = true;
                    }
                    riFilterMap.put("reviewitemobjectid", fieldid);
                    riFilterMap.put("reviewiteminstance", fielddatavalues.getBigDecimal(j, "fieldinstance"));
                    DataSet reviewitems = fieldreviewitems.getFilteredDataSet(riFilterMap);
                    if (reviewitems.size() <= 0) continue;
                    PropertyListCollection instanceReviewItems = new PropertyListCollection();
                    for (int k = 0; k < reviewitems.size(); ++k) {
                        PropertyList reviewItem = new PropertyList();
                        reviewItem.setProperty("reviewitemid", reviewitems.getValue(k, "reviewitemid"));
                        reviewItem.setProperty("reviewitemobjectid", reviewitems.getValue(k, "reviewitemobjectid"));
                        reviewItem.setProperty("reviewiteminstance", reviewitems.getValue(k, "reviewiteminstance"));
                        reviewItem.setProperty("reviewitemtype", reviewitems.getValue(k, "reviewitemtype"));
                        reviewItem.setProperty("reviewitemtext", reviewitems.getValue(k, "reviewitemtext"));
                        reviewItem.setProperty("reviewitemstatus", reviewitems.getValue(k, "reviewitemstatus"));
                        reviewItem.setProperty("reviewitemassignto", reviewitems.getValue(k, "reviewitemassignto"));
                        reviewItem.setProperty("reviewitemassigntouser", userDetails.getProperty(reviewitems.getValue(k, "reviewitemassignto")));
                        reviewItem.setProperty("resolvedstatus", reviewitems.getValue(k, "resolvedstatus"));
                        reviewItem.setProperty("resolvedby", reviewitems.getValue(k, "resolvedby"));
                        reviewItem.setProperty("resolvedbyuser", userDetails.getProperty(reviewitems.getValue(k, "resolvedby")));
                        reviewItem.setProperty("resolveddt", reviewitems.getValue(k, "resolveddt"));
                        reviewItem.setProperty("resolvedtext", reviewitems.getValue(k, "resolvedtext"));
                        reviewItem.setProperty("enteredtext", reviewitems.getValue(k, "enteredtext"));
                        reviewItem.setProperty("ddeenteredtext", reviewitems.getValue(k, "ddeenteredtext"));
                        reviewItem.setProperty("annotationtype", reviewitems.getValue(k, "annotationtype"));
                        reviewItem.setProperty("notificationstatus", reviewitems.getValue(k, "notificationstatus"));
                        reviewItem.setProperty("createby", reviewitems.getValue(k, "createby"));
                        reviewItem.setProperty("createbyuser", userDetails.getProperty(reviewitems.getValue(k, "createby")));
                        reviewItem.setProperty("createdt", reviewitems.getValue(k, "createdt"));
                        if (attachments != null) {
                            attachmentFindMap.put("attachmentlabel", reviewitems.getValue(k, "reviewitemobjectid") + "|" + reviewitems.getValue(k, "reviewiteminstance") + "|" + k);
                            int row = attachments.findRow(attachmentFindMap);
                            if (row > -1) {
                                reviewItem.setProperty("attachmentnum", attachments.getValue(row, "attachmentnum"));
                            }
                        }
                        instanceReviewItems.add(reviewItem);
                        if (!reviewItem.getProperty("reviewitemassignto").equals(sapphireConnection.getSysuserId()) || reviewItem.getProperty("resolvedstatus").equals("R")) continue;
                        document.hasOpenFollowups = true;
                    }
                    instance.setProperty("reviewitems", instanceReviewItems);
                }
            }
            document.fieldValueMap.put(fieldid, new Field(field, instances, "enteredtext", m18n));
            document.ddefieldValueMap.put(fieldid, new Field(field, instances, field.getProperty("bindingmode").endsWith("as") ? "ddeenteredtext" : "enteredtext", m18n));
            if (field.getProperty("identityfield", "N").equals("Y")) {
                document.identityFieldMap.put(fieldid, document.fieldValueMap.get(fieldid));
            }
            ProcessingUtil.addSectionInstances(document.fieldValueMap, fieldid);
            ProcessingUtil.addSectionInstances(document.ddefieldValueMap, fieldid);
            fieldValues.add(fieldValue);
        }
        document.setFieldValues(fieldValues);
        PropertyListCollection groupValues = new PropertyListCollection();
        PropertyListCollection groups = document.form.getGroups();
        if (groups != null) {
            for (int i = 0; i < groups.size(); ++i) {
                PropertyList group = groups.getPropertyList(i);
                String groupid = group.getProperty("groupid");
                PropertyList groupValue = new PropertyList(groupid);
                groupValue.setProperty("groupid", groupid);
                riFilterMap.put("reviewitemobjectid", groupid);
                DataSet reviewitems = fieldreviewitems.getFilteredDataSet(riFilterMap);
                if (reviewitems.size() > 0) {
                    PropertyListCollection groupReviewItems = new PropertyListCollection();
                    for (int j = 0; j < reviewitems.size(); ++j) {
                        PropertyList reviewItem = new PropertyList();
                        reviewItem.setProperty("reviewitemid", reviewitems.getValue(j, "reviewitemid"));
                        reviewItem.setProperty("reviewitemobjectid", reviewitems.getValue(j, "reviewitemobjectid"));
                        reviewItem.setProperty("reviewitemtype", reviewitems.getValue(j, "reviewitemtype"));
                        reviewItem.setProperty("reviewitemtext", reviewitems.getValue(j, "reviewitemtext"));
                        reviewItem.setProperty("reviewitemstatus", reviewitems.getValue(j, "reviewitemstatus"));
                        reviewItem.setProperty("reviewitemassignto", reviewitems.getValue(j, "reviewitemassignto"));
                        reviewItem.setProperty("reviewitemassigntouser", userDetails.getProperty(reviewitems.getValue(j, "reviewitemassignto")));
                        reviewItem.setProperty("createby", reviewitems.getValue(j, "createby"));
                        reviewItem.setProperty("createbyuser", userDetails.getProperty(reviewitems.getValue(j, "createby")));
                        reviewItem.setProperty("createdt", reviewitems.getValue(j, "createdt"));
                        groupReviewItems.add(reviewItem);
                        if (!reviewItem.getProperty("reviewitemassignto").equals(sapphireConnection.getSysuserId()) || reviewItem.getProperty("resolvedstatus").equals("R")) continue;
                        document.hasOpenFollowups = true;
                    }
                    groupValue.setProperty("reviewitems", groupReviewItems);
                }
                groupValues.add(groupValue);
            }
        }
        document.setGroupValues(groupValues);
        riFilterMap.remove("reviewiteminstance");
        riFilterMap.put("reviewitemobjectid", "__document");
        DataSet reviewitems = fieldreviewitems.getFilteredDataSet(riFilterMap);
        if (reviewitems.size() > 0) {
            PropertyListCollection documentReviewItems = new PropertyListCollection();
            for (int j = 0; j < reviewitems.size(); ++j) {
                PropertyList reviewItem = new PropertyList();
                reviewItem.setProperty("reviewitemid", reviewitems.getValue(j, "reviewitemid"));
                reviewItem.setProperty("reviewitemobjectid", reviewitems.getValue(j, "reviewitemobjectid"));
                reviewItem.setProperty("reviewitemtype", reviewitems.getValue(j, "reviewitemtype"));
                reviewItem.setProperty("reviewitemtext", reviewitems.getValue(j, "reviewitemtext"));
                reviewItem.setProperty("reviewitemstatus", reviewitems.getValue(j, "reviewitemstatus"));
                reviewItem.setProperty("reviewitemassignto", reviewitems.getValue(j, "reviewitemassignto"));
                reviewItem.setProperty("reviewitemassigntouser", userDetails.getProperty(reviewitems.getValue(j, "reviewitemassignto")));
                reviewItem.setProperty("resolvedstatus", reviewitems.getValue(j, "resolvedstatus"));
                reviewItem.setProperty("resolvedby", reviewitems.getValue(j, "resolvedby"));
                reviewItem.setProperty("resolvedbyuser", userDetails.getProperty(reviewitems.getValue(j, "resolvedby")));
                reviewItem.setProperty("resolveddt", reviewitems.getValue(j, "resolveddt"));
                reviewItem.setProperty("resolvedtext", reviewitems.getValue(j, "resolvedtext"));
                reviewItem.setProperty("annotationtype", reviewitems.getValue(j, "annotationtype"));
                reviewItem.setProperty("notificationstatus", reviewitems.getValue(j, "notificationstatus"));
                reviewItem.setProperty("createby", reviewitems.getValue(j, "createby"));
                reviewItem.setProperty("createbyuser", userDetails.getProperty(reviewitems.getValue(j, "createby")));
                reviewItem.setProperty("createdt", reviewitems.getValue(j, "createdt"));
                if (attachments != null) {
                    attachmentFindMap.put("attachmentlabel", reviewitems.getValue(j, "reviewitemobjectid") + "|-1|" + j);
                    int row = attachments.findRow(attachmentFindMap);
                    if (row > -1) {
                        reviewItem.setProperty("attachmentnum", attachments.getValue(row, "attachmentnum"));
                    }
                }
                documentReviewItems.add(reviewItem);
                if (!reviewItem.getProperty("reviewitemassignto").equals(sapphireConnection.getSysuserId()) || reviewItem.getProperty("resolvedstatus").equals("R")) continue;
                document.hasOpenFollowups = true;
            }
            document.setReviewitems(documentReviewItems);
        }
    }

    private static SDIData loadDocument(SDIProcessor sdiProcessor, String documentid, String documentversionid, String rsetid, String restrictivewhere, boolean lock) throws DocumentUserException, SapphireException {
        SDIData sdiData;
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_Document");
        sdiRequest.setRsetid(rsetid);
        boolean isOracle = new ConnectionProcessor(sdiProcessor.getConnectionid()).isOra();
        if (rsetid.length() == 0) {
            if (restrictivewhere.length() == 0) {
                sdiRequest.setKeyid1List(documentid);
                sdiRequest.setKeyid2List(documentversionid);
            } else {
                sdiRequest.setQueryFrom("document");
                sdiRequest.setQueryWhere("document.documentid = '" + SafeSQL.encodeForSQL(documentid, isOracle) + "' AND document.documentversionid = '" + SafeSQL.encodeForSQL(documentversionid, isOracle) + "' AND ( " + restrictivewhere + " )");
            }
        }
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("documentfield");
        sdiRequest.setRequestItem("documentreviewitem");
        sdiRequest.setRequestItem("approval");
        sdiRequest.setRequestItem("approvalstep");
        sdiRequest.setRequestItem("attachment");
        sdiRequest.setExtendedDataTypes(true);
        sdiRequest.setExtendedAudit(true);
        if (lock) {
            sdiRequest.setPrimaryLockOption("LA");
            sdiRequest.setRetainRsetid(true);
        }
        if ((sdiData = sdiProcessor.getSDIData(sdiRequest)) == null || sdiData.getDataset("primary") == null) {
            throw new SapphireException("Failed to open document '" + documentid + "(" + documentversionid + ")'");
        }
        if (sdiData.getDataset("primary").size() != 1) {
            QueryProcessor qp = new QueryProcessor(sdiProcessor.getConnectionid());
            if (qp.getPreparedSqlDataSet("SELECT documentid FROM document WHERE documentid = ? AND documentversionid = ?", new Object[]{documentid, documentversionid}).size() == 1) {
                throw new DocumentUserException("Failed to open document '" + documentid + "(" + documentversionid + ")' - you may not have privileges to open this document!");
            }
            throw new DocumentUserException("Failed to open document '" + documentid + "(" + documentversionid + ")' - document does not exist!");
        }
        return sdiData;
    }

    public DataSet getAttachments(boolean load) {
        return this.getAttachments(load, "");
    }

    public DataSet getAttachments(boolean load, String tempdocumentid) {
        if (load) {
            QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
            return qp.getPreparedSqlDataSet("SELECT * FROM sdiattachment WHERE sdcid = 'LV_Document' AND keyid1 = ? AND keyid2 = ?", new Object[]{this.getDocumentid().length() > 0 ? this.getDocumentid() : tempdocumentid, this.getDocumentversionid()});
        }
        return this.documentData.getDataset("attachment");
    }

    public boolean save(String operation, PropertyListCollection fieldValues, HashMap fieldMap, PropertyListCollection groupValues, boolean ddedata, boolean validationErrors, boolean unAcknowledgedErrors, PropertyList requestData) throws SapphireException {
        boolean saved;
        block112: {
            block109: {
                PropertyListCollection saveValues;
                block110: {
                    String originalStatus;
                    TranslationProcessor trans;
                    block111: {
                        DataSet existingDocument;
                        SaveProps saveProps = new SaveProps();
                        saveProps.auditreason = requestData.getProperty("auditreason");
                        saveProps.auditactivity = requestData.getProperty("auditactivity");
                        saveProps.auditsigned = requestData.getProperty("auditsigned");
                        saveProps.assignto = requestData.getProperty("assignto");
                        saveProps.assigntodepartment = requestData.getProperty("assigntodepartment");
                        boolean editingDocument = this.getDocumentid().length() > 0 && !"newversion".equalsIgnoreCase(operation);
                        boolean saveDocument = false;
                        saved = false;
                        boolean rejectingData = false;
                        boolean compareData = false;
                        int newreviewitemid = 0;
                        DataSet boundDataSets = null;
                        DataSet documentfielddata = null;
                        HashMap<String, Integer> documentfielddataRowMap = new HashMap<String, Integer>();
                        DataSet reviewitemdata = null;
                        trans = new TranslationProcessor(this.sapphireConnection.getConnectionId());
                        M18NUtil m18n = new M18NUtil(this.sapphireConnection);
                        FormatUtil formatUtil = null;
                        this.logger.info("Saving " + (editingDocument || "newversion".equalsIgnoreCase(operation) ? "document " + this.documentid + "(v" + this.documentversionid + ")" : "new document"));
                        if ("submit".equalsIgnoreCase(operation) || "resubmit".equalsIgnoreCase(operation) || "reconcile".equalsIgnoreCase(operation) || "followup".equalsIgnoreCase(operation) || "documentmgr".equalsIgnoreCase(operation)) {
                            if (!validationErrors || validationErrors && this.form.isSubmitInvalidData() && !unAcknowledgedErrors) {
                                saveDocument = true;
                                if (this.form.isDde() && ddedata) {
                                    compareData = true;
                                }
                            } else {
                                this.setReturnmessage(trans.translate("Failed to submit data because of validation errors - your data has not been saved!"));
                            }
                        } else if ("approve".equalsIgnoreCase(operation)) {
                            if (!validationErrors || validationErrors && this.form.isApproveInvalidData() && !unAcknowledgedErrors) {
                                saveDocument = true;
                            } else {
                                this.setReturnmessage(trans.translate("Failed to approve data due to validation errors - your data has not been saved!"));
                            }
                        } else if ("reject".equalsIgnoreCase(operation)) {
                            saveDocument = true;
                            rejectingData = true;
                        } else if ("pending".equalsIgnoreCase(operation) || "save".equalsIgnoreCase(operation)) {
                            saveDocument = true;
                        } else if ("draft".equalsIgnoreCase(operation) || "copy".equalsIgnoreCase(operation) || "newversion".equalsIgnoreCase(operation)) {
                            if (this.form.isDraftable() && !validationErrors || this.form.isDraftable() && validationErrors && this.form.isSaveInvalidData()) {
                                saveDocument = true;
                            } else {
                                this.setReturnmessage(trans.translate("Failed to draft data due to validation errors - your data has not been saved!"));
                            }
                        } else {
                            throw new SapphireException("Unrecognized operation '" + operation + "'!");
                        }
                        if (!saveDocument) break block109;
                        saveValues = new PropertyListCollection();
                        for (int i = 0; i < fieldValues.size(); ++i) {
                            PropertyListCollection instances = fieldValues.getPropertyList(i).getCollection("instances");
                            for (int j = 0; j < instances.size(); ++j) {
                                saveValues.add(instances.getPropertyList(j));
                            }
                        }
                        saveValues.addAll(groupValues);
                        PropertyList documentValues = new PropertyList();
                        documentValues.setProperty("documentid", "__document");
                        documentValues.setProperty("objecttype", "document");
                        documentValues.setProperty("reviewitems", this.reviewitems);
                        saveValues.add(documentValues);
                        ActionProcessor actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
                        String now = m18n.format(m18n.getNowCalendar());
                        if (editingDocument) {
                            documentfielddata = this.documentData.getDataset("documentfield");
                            documentfielddata.addColumn("itemchecked", 0);
                            reviewitemdata = this.documentData.getDataset("documentreviewitem");
                            newreviewitemid = this.initReviewItemData(reviewitemdata);
                        }
                        PropertyListCollection identityset = new PropertyListCollection();
                        boolean identityFieldsChanged = false;
                        boolean reconErrors = false;
                        boolean unresolvedFollowups = false;
                        PropertyList reconItems = new PropertyList();
                        StringBuffer processingFieldErrors = new StringBuffer();
                        HashMap<String, Object> findMap = new HashMap<String, Object>();
                        for (int i = 0; i < saveValues.size(); ++i) {
                            String objectid;
                            PropertyList fieldValue = saveValues.getPropertyList(i);
                            String objecttype = fieldValue.getProperty("objecttype", "field");
                            String string = objecttype.equals("field") ? fieldValue.getProperty("fieldid") : (objectid = objecttype.equals("group") ? fieldValue.getProperty("groupid") : fieldValue.getProperty("documentid"));
                            String objectinstance = objecttype.equals("field") ? fieldValue.getProperty("fieldinstance") : (objecttype.equals("group") ? "0" : "0");
                            String fieldstatus = "P";
                            String enteredtext = null;
                            String displayvalue = null;
                            String sectionid = null;
                            boolean repeatable = false;
                            boolean savefield = true;
                            boolean clobfield = false;
                            String autosave = "";
                            PropertyListCollection reviewItems = fieldValue.getCollection("reviewitems");
                            if (objecttype.equals("field")) {
                                PropertyList binding;
                                boolean fieldReadonly;
                                boolean fieldVisible;
                                enteredtext = fieldValue.getProperty("enteredtext");
                                Field field = (Field)fieldMap.get(objectid);
                                int instanceint = field.getInstanceIndex(Integer.parseInt(objectinstance));
                                String string2 = displayvalue = field.isRepeatable() ? (String)field.getList("displayvalue").get(instanceint) : field.getProperty("displayvalue");
                                boolean bl = field.isRepeatable() ? !field.getList("visible").get(instanceint).equals("N") : (fieldVisible = !field.getProperty("visible").equals("N"));
                                boolean bl2 = field.isRepeatable() ? !field.getList("readonly").get(instanceint).equals("N") : (fieldReadonly = !field.getProperty("readonly").equals("N"));
                                if (field.getProperty("save", "Y").equals("V") && !fieldVisible || field.getProperty("save", "Y").equals("E") && fieldReadonly) {
                                    savefield = false;
                                }
                                if (ddedata) {
                                    fieldValue.setProperty("ddeenteredtext", enteredtext);
                                    fieldValue.setProperty("ddedisplayvalue", displayvalue);
                                }
                                repeatable = this.form.getField(objectid).getProperty("repeatable", "N").equals("Y");
                                sectionid = this.form.getField(objectid).getProperty("sectionid");
                                autosave = field.getProperty("autosave");
                                clobfield = field.getProperty("type").equals("formattedtext");
                                boolean derivedvalue = this.form.getField(objectid).getProperty("valuerule").length() > 0;
                                PropertyList propertyList = binding = repeatable ? (PropertyList)field.getList("binding").get(instanceint) : (PropertyList)field.get("binding");
                                if (binding != null && (binding.getProperty("dataentrytype").equals("NC") || binding.getProperty("bindingmode").length() > 0 && !binding.getProperty("bindingmode").endsWith("as"))) {
                                    derivedvalue = true;
                                }
                                if (compareData && this.form.getField(objectid).getProperty("reconcile", "Y").equals("Y") && !derivedvalue) {
                                    String reviewitemstatus;
                                    String oldenteredtext;
                                    PropertyList reviewItem = null;
                                    boolean found = false;
                                    int index = 0;
                                    while (!found && index < reviewItems.size()) {
                                        reviewItem = reviewItems.getPropertyList(index);
                                        if (reviewItem.getProperty("reviewitemtype").equals("R")) {
                                            found = true;
                                            continue;
                                        }
                                        ++index;
                                    }
                                    Field oldFormField = (Field)this.fieldValueMap.get(objectid);
                                    String string3 = oldenteredtext = repeatable ? (String)oldFormField.getList("enteredtext").get(instanceint) : (String)oldFormField.get("enteredtext");
                                    if (!oldenteredtext.equals(enteredtext)) {
                                        if (!found) {
                                            reviewItem = new PropertyList();
                                            reviewItem.setProperty("reviewitemtype", "R");
                                            reviewItem.setProperty("reviewitemtext", "DDE field values inconsistent!");
                                            reviewItem.setProperty("reviewitemstatus", "F");
                                            reviewItem.setProperty("resolvedstatus", "");
                                            reviewItem.setProperty("createby", this.sapphireConnection.getSysuserId());
                                            reviewItem.setProperty("enteredtext", oldenteredtext);
                                            reviewItem.setProperty("ddeenteredtext", enteredtext);
                                            reviewItems.add(reviewItem);
                                        } else if (reviewItem.getProperty("reviewitemstatus").equals("R")) {
                                            reconItems.setProperty(objectid, enteredtext);
                                        }
                                    } else if (found && (reviewitemstatus = reviewItem.getProperty("reviewitemstatus")).equals("F")) {
                                        reviewItems.remove(index);
                                    }
                                }
                                if (!rejectingData && !ddedata && this.form.getField(objectid).getProperty("identityfield", "N").equals("Y")) {
                                    PropertyList identity = new PropertyList();
                                    identity.setProperty("fieldid", objectid);
                                    identityset.add(identity);
                                    Field formField = (Field)this.identityFieldMap.get(objectid);
                                    if (formField == null || !enteredtext.equals(formField.getProperty("enteredtext"))) {
                                        identityFieldsChanged = true;
                                    }
                                }
                            } else if (objecttype.equals("group")) {
                                enteredtext = fieldValue.getProperty("value");
                            }
                            if (reviewItems != null && reviewItems.size() > 0) {
                                boolean processingField = objecttype.equals("field") && this.form.getField(objectid).getProperty("processingfield", "N").equals("Y");
                                for (int j = 0; j < reviewItems.size(); ++j) {
                                    PropertyList reviewItem = reviewItems.getPropertyList(j);
                                    String reviewitemstatus = reviewItem.getProperty("reviewitemstatus");
                                    if (!(rejectingData || reviewitemstatus.equals("P") || reviewitemstatus.equals("R"))) {
                                        if (reviewItem.getProperty("reviewitemtype").equals("R") && reviewitemstatus.equals("F")) {
                                            reconErrors = true;
                                            unAcknowledgedErrors = true;
                                        }
                                        fieldstatus = "E";
                                    }
                                    if (processingField && (reviewItem.getProperty("reviewitemtype").equals("V") || reviewItem.getProperty("reviewitemtype").equals("F") && !reviewitemstatus.equals("P") || reviewItem.getProperty("reviewitemtype").equals("R") && !reviewitemstatus.equals("R"))) {
                                        processingFieldErrors.append("<br>&nbsp;").append(this.form.getField(objectid).getProperty("title", this.form.getField(objectid).getProperty("fieldid")));
                                    }
                                    if ("followup".equalsIgnoreCase(operation) && reviewItem.getProperty("reviewitemtype").equals("F") && !reviewItem.getProperty("resolvedstatus").equals("R")) {
                                        unresolvedFollowups = true;
                                    }
                                    if ("copy".equalsIgnoreCase(operation)) {
                                        reviewItem.setProperty("reviewitemid", "");
                                    }
                                    newreviewitemid = this.updateReviewItemPropLists(objectid, objectinstance, reviewItem, reviewitemdata, saveProps, newreviewitemid, now);
                                }
                            }
                            fieldValue.setProperty("fieldstatus", fieldstatus);
                            if (!objecttype.equals("field") && !objecttype.equals("group")) continue;
                            if (editingDocument) {
                                findMap.put("fieldid", objectid);
                                findMap.put("fieldinstance", new BigDecimal(objectinstance));
                                int findRow = documentfielddata.findRow(findMap);
                                if (findRow >= 0) {
                                    documentfielddataRowMap.put(objectid, findRow);
                                    documentfielddata.setValue(findRow, "itemchecked", "Y");
                                }
                            }
                            boolean addfieldrow = false;
                            if (repeatable && editingDocument && sectionid.length() > 0) {
                                addfieldrow = this.fieldValueMap.get(sectionid + "_" + objectinstance) == null;
                            }
                            PropertyList binding = fieldValue.getPropertyList("binding");
                            if (autosave.length() > 0 && !autosave.equals("primary")) {
                                boundDataSets = Document.appendBoundDataSets(binding, boundDataSets);
                            }
                            boolean savenumeric = true;
                            try {
                                m18n.parseBigDecimal(enteredtext);
                                if (formatUtil == null) {
                                    formatUtil = FormatUtil.getInstance(this.sapphireConnection);
                                }
                                if (enteredtext.indexOf(formatUtil.getDecimalSeparator()) > 17 || enteredtext.length() > 18) {
                                    throw new NumberFormatException();
                                }
                            }
                            catch (Exception e) {
                                savenumeric = false;
                            }
                            boolean savedate = true;
                            try {
                                Calendar cal = m18n.parseCalendar(enteredtext);
                                if (cal == null || cal.get(1) < 1900 || cal.get(1) > 2100) {
                                    savedate = false;
                                }
                            }
                            catch (Exception e) {
                                savedate = false;
                            }
                            if (addfieldrow) {
                                saveProps.addfieldidList.append(SEPARATOR).append(objectid);
                                saveProps.addfieldinstanceList.append(SEPARATOR).append(objectinstance);
                                saveProps.addenteredtextList.append(SEPARATOR).append(savefield && !clobfield ? (enteredtext.length() > 4000 ? enteredtext.substring(0, 4000) : enteredtext) : "");
                                saveProps.adddisplayvalueList.append(SEPARATOR).append(savefield && !clobfield ? (displayvalue.length() > 4000 ? displayvalue.substring(0, 4000) : displayvalue) : "");
                                saveProps.addnumericvalueList.append(SEPARATOR).append(savenumeric && savefield && !clobfield ? (enteredtext.length() > 4000 ? enteredtext.substring(0, 4000) : enteredtext) : "");
                                saveProps.adddatevalueList.append(SEPARATOR).append(savefield && !clobfield ? (enteredtext.length() > 4000 ? enteredtext.substring(0, 4000) : enteredtext) : "");
                                saveProps.addclobvalueList.append(SEPARATOR).append(savefield && clobfield ? enteredtext : "");
                                saveProps.addbindingList.append(SEPARATOR).append(binding != null ? binding.toXMLString() : "");
                                saveProps.addfieldcreatebyList.append(SEPARATOR).append(this.sapphireConnection.getSysuserId());
                                saveProps.addfieldcreatedtList.append(SEPARATOR).append(now);
                                saveProps.addfieldcreatetoolList.append(SEPARATOR).append("Document");
                                saveProps.addfieldstatusList.append(SEPARATOR).append(fieldstatus);
                                continue;
                            }
                            saveProps.fieldidList.append(SEPARATOR).append(objectid);
                            saveProps.fieldinstanceList.append(SEPARATOR).append(objectinstance);
                            saveProps.enteredtextList.append(SEPARATOR).append(savefield && !clobfield ? enteredtext : "");
                            saveProps.displayvalueList.append(SEPARATOR).append(savefield && !clobfield ? displayvalue : "");
                            saveProps.numericvalueList.append(SEPARATOR).append(savenumeric && savefield && !clobfield ? enteredtext : "");
                            saveProps.datevalueList.append(SEPARATOR).append(savedate && savefield && !clobfield ? enteredtext : "");
                            saveProps.clobvalueList.append(SEPARATOR).append(savefield && clobfield ? enteredtext : "");
                            saveProps.bindingList.append(SEPARATOR).append(binding != null ? binding.toXMLString() : "");
                            saveProps.fieldcreatebyList.append(SEPARATOR).append(this.sapphireConnection.getSysuserId());
                            saveProps.fieldcreatedtList.append(SEPARATOR).append(now);
                            saveProps.fieldcreatetoolList.append(SEPARATOR).append("Document");
                            saveProps.fieldstatusList.append(SEPARATOR).append(fieldstatus);
                        }
                        this.updateDelFieldInstancePropList(documentfielddata, saveProps);
                        this.updateDelReviewItemPropList(reviewitemdata, saveProps);
                        originalStatus = this.getDocumentStatus();
                        String originalOperation = operation;
                        if ("documentmgr".equalsIgnoreCase(operation) && (this.getDocumentStatus().length() == 0 || this.getDocumentStatus().equals("DR") || this.getDocumentStatus().equals("PD"))) {
                            operation = "submit";
                        }
                        if ("submit".equalsIgnoreCase(operation) || "reconcile".equalsIgnoreCase(operation) || "followup".equalsIgnoreCase(operation)) {
                            if (this.form.isDde()) {
                                if (!ddedata) {
                                    if (!"reconcile".equalsIgnoreCase(operation) && !"followup".equalsIgnoreCase(operation)) {
                                        this.setDocumentStatus("SM");
                                    } else if ("followup".equalsIgnoreCase(operation) && !unresolvedFollowups && this.getDocumentStatus().equals("RJ")) {
                                        this.setDocumentStatus("PA");
                                    }
                                } else if (!"reconcile".equalsIgnoreCase(operation) && reconErrors && this.form.isDdeuser2alert() && unAcknowledgedErrors) {
                                    this.setReturnmessage(trans.translate("Failed to submit data due to reconciliation errors - your data has not been saved!"));
                                    saveDocument = false;
                                } else if (this.form.isApprovable()) {
                                    this.setDocumentStatus("PA");
                                } else if (processingFieldErrors.length() > 0) {
                                    this.setReturnmessage(trans.translate("Failed to submit data because the following processing fields have validation errors, open follow-ups or reconciliation errors (your data has not been saved):") + "<br>" + processingFieldErrors);
                                    saveDocument = false;
                                } else if (this.form.hasProcessing()) {
                                    this.setDocumentStatus("PP");
                                } else {
                                    this.setDocumentStatus(this.form.isLockondone() ? "LK" : "DN");
                                }
                            } else if (!originalStatus.equals("DN")) {
                                if (this.form.isApprovable()) {
                                    this.setDocumentStatus("PA");
                                } else if (processingFieldErrors.length() > 0) {
                                    this.setReturnmessage(trans.translate("Failed to submit data because the following processing fields have errors or open follow-ups (your data has not been saved):") + "<br>" + processingFieldErrors);
                                    if (!editingDocument) {
                                        saveDocument = false;
                                    }
                                } else if (this.form.hasProcessing()) {
                                    this.setDocumentStatus("PP");
                                } else {
                                    this.setDocumentStatus(this.form.isLockondone() ? "LK" : "DN");
                                }
                            }
                        } else if ("approve".equalsIgnoreCase(operation)) {
                            if (processingFieldErrors.length() > 0) {
                                this.setReturnmessage(trans.translate("Failed to approve data because the following processing fields have errors or open follow-ups (your data has not been saved):") + "<br>" + processingFieldErrors);
                            } else {
                                DataSet approvalsteps = this.documentData.getDataset("approvalstep");
                                boolean moreApprovals = false;
                                for (int i = 0; !moreApprovals && i < approvalsteps.size(); ++i) {
                                    if (this.isDocumentManager() || approvalsteps.getValue(i, "stepstatusflag").equals("C") || !approvalsteps.getValue(i, "approvalflag").equals("U")) continue;
                                    moreApprovals = true;
                                }
                                if (!moreApprovals) {
                                    if (this.form.hasProcessing()) {
                                        this.setDocumentStatus("PP");
                                    } else {
                                        this.setDocumentStatus(this.form.isLockondone() ? "LK" : "DN");
                                    }
                                }
                            }
                        } else if ("reject".equalsIgnoreCase(operation)) {
                            this.setDocumentStatus("RJ");
                        } else if ("pending".equalsIgnoreCase(operation)) {
                            this.setDocumentStatus("PD");
                        } else if ("draft".equalsIgnoreCase(operation)) {
                            if (this.form.isDde() && ddedata) {
                                this.setDocumentStatus("DDEDR");
                            } else if (!editingDocument || originalStatus.equals("PD")) {
                                this.setDocumentStatus("DR");
                            }
                        } else if (("documentmgr".equalsIgnoreCase(operation) || "resubmit".equalsIgnoreCase(operation)) && this.getDocumentStatus().equals("ER")) {
                            this.setDocumentStatus("PP");
                        }
                        operation = originalOperation;
                        if (!saveDocument) break block110;
                        if (this.form.getDocumentDescRule() != null && this.form.getDocumentDescRule().length() > 0) {
                            HashMap<String, HashMap> bindingMap = new HashMap<String, HashMap>();
                            bindingMap.put("params", requestData.getPropertyList("paramvalues"));
                            bindingMap.put("fields", fieldMap);
                            bindingMap.put("user", new ConnectionInfo(this.sapphireConnection).getUserAttributeMap());
                            String desc = GroovyUtil.getInstance(this.sapphireConnection).evaluateSecure(this.form.getDocumentDescRule(), bindingMap);
                            this.setDocumentdesc(desc.substring(0, desc.length() > 80 ? 80 : desc.length()));
                        }
                        boolean exists = false;
                        if (identityset.size() > 0 && identityFieldsChanged && (existingDocument = Document.getExistingDocument(this.sapphireConnection, this.formid, this.formversionid, identityset, fieldMap)) != null && existingDocument.size() > 0) {
                            exists = true;
                            String sysuserid1 = existingDocument.getValue(0, "sysuserid1");
                            if (this.form.isDde() && !this.sapphireConnection.getSysuserId().equals(sysuserid1)) {
                                this.setDocumentid(existingDocument.getValue(0, "documentid"));
                                this.setSysuserid1(sysuserid1);
                                this.setDocumentStatus("EXISTS");
                            }
                        }
                        if (exists) break block111;
                        saved = editingDocument ? this.editDocument(operation, fieldMap, ddedata, requestData, saveProps, rejectingData, documentfielddata, documentfielddataRowMap, saveValues, actionProcessor, reconItems) : this.addDocument(operation, fieldMap, requestData, saveProps, boundDataSets, saveValues, actionProcessor);
                        break block112;
                    }
                    if (this.getDocumentStatus().equals("EXISTS")) break block112;
                    this.setDocumentStatus(originalStatus);
                    this.setReturnmessage(trans.translate("Request to " + operation + " the document aborted as it already exists - check identity fields are correct."));
                    break block112;
                }
                for (int i = 0; i < saveValues.size(); ++i) {
                    PropertyList fieldValue = saveValues.getPropertyList(i);
                    PropertyListCollection reviewItems = fieldValue.getCollection("reviewitems");
                    if (reviewItems == null) continue;
                    for (int j = 0; j < reviewItems.size(); ++j) {
                        PropertyList reviewItem = reviewItems.getPropertyList(j);
                        if (!reviewItem.getProperty("new", "N").equals("Y")) continue;
                        reviewItem.remove("reviewitemid");
                        reviewItem.remove("new");
                    }
                }
                break block112;
            }
            if (ddedata) {
                for (int i = 0; i < fieldValues.size(); ++i) {
                    PropertyList fieldValue = fieldValues.getPropertyList(i);
                    PropertyListCollection instances = fieldValue.getCollection("instances");
                    if (instances == null) continue;
                    for (int j = 0; j < instances.size(); ++j) {
                        PropertyList instance = instances.getPropertyList(j);
                        String enteredtext = instance.getProperty("enteredtext");
                        instance.setProperty("ddeenteredtext", enteredtext);
                        instance.setProperty("ddedisplayvalue", enteredtext);
                    }
                }
            }
        }
        return saved;
    }

    private boolean addDocument(String operation, HashMap fieldMap, PropertyList requestData, SaveProps saveProps, DataSet boundDataSets, PropertyListCollection saveValues, ActionProcessor actionProcessor) throws SapphireException {
        HashMap autolinkProps;
        boolean saved = false;
        PropertyList documentObjects = new PropertyList();
        documentObjects.setProperty("multisamplecalcs", requestData.getProperty("multisamplecalcs"));
        documentObjects.setProperty("datasources", requestData.getPropertyList("datasourcebindings"));
        documentObjects.setProperty("sections", requestData.getPropertyList("sectionbindings"));
        ActionBlock ab = new ActionBlock();
        HashMap<String, String> primaryProps = new HashMap<String, String>();
        primaryProps.put("sdcid", "LV_Document");
        String documentid = requestData.getProperty("newdocumentid", "[documentid]");
        if (!documentid.equals("[documentid]")) {
            primaryProps.put("overrideautokey", "Y");
        }
        if (requestData.getProperty("newdocumentversionid").length() > 0) {
            this.setDocumentversionid(requestData.getProperty("newdocumentversionid"));
        }
        String documentversionid = this.getDocumentversionid();
        if ("newversion".equalsIgnoreCase(operation)) {
            documentid = this.getDocumentid();
            primaryProps.put("overrideautokey", "Y");
        }
        primaryProps.put("keyid2", documentversionid);
        primaryProps.put("keyid1", documentid);
        primaryProps.put("keyid2", documentversionid);
        primaryProps.put("formid", this.getFormid());
        primaryProps.put("formversionid", this.getFormversionid());
        primaryProps.put("documentobjects", documentObjects.toXMLString());
        primaryProps.put("ddeflag", this.form.isDde() ? (this.form.isDdeuser2recon() ? "V" : "Y") : "N");
        primaryProps.put("documentdesc", this.getDocumentdesc());
        primaryProps.put("documentstatus", this.getDocumentStatus());
        primaryProps.put("statusmessage", this.getStatusmessage());
        primaryProps.put("thumbnailhtml", requestData.getProperty("pagehtml"));
        primaryProps.put("priority", requestData.getProperty("priority"));
        primaryProps.put("duedt", requestData.getProperty("duedt"));
        primaryProps.put("trainingoverridenflag", requestData.getProperty("trainingoverriden", "N"));
        primaryProps.put("sysuserid1", saveProps.assignto.length() > 0 ? saveProps.assignto : (!this.sapphireConnection.getSysuserId().equals("(system)") ? this.sapphireConnection.getSysuserId() : ""));
        primaryProps.put("assigneddepartment", saveProps.assigntodepartment.length() > 0 ? saveProps.assigntodepartment : "");
        primaryProps.put("auditreason", saveProps.auditreason);
        primaryProps.put("auditactivity", saveProps.auditactivity);
        primaryProps.put("auditsignedflag", saveProps.auditsigned);
        primaryProps.put("newkeyid1", "[documentid]");
        ab.setAction("document", "AddSDI", "1", primaryProps);
        if (saveProps.fieldidList.length() > 0) {
            HashMap<String, String> fieldProps = new HashMap<String, String>();
            fieldProps.put("linkid", "documentfield_link");
            fieldProps.put("sdcid", "LV_Document");
            fieldProps.put("keyid1", documentid);
            fieldProps.put("keyid2", documentversionid);
            fieldProps.put("separator", SEPARATOR);
            fieldProps.put("fieldid", saveProps.fieldidList.substring(SEPARATOR.length()));
            fieldProps.put("fieldinstance", saveProps.fieldinstanceList.substring(SEPARATOR.length()));
            fieldProps.put("createby", saveProps.fieldcreatebyList.substring(SEPARATOR.length()));
            fieldProps.put("createdt", saveProps.fieldcreatedtList.substring(SEPARATOR.length()));
            fieldProps.put("createtool", saveProps.fieldcreatetoolList.substring(SEPARATOR.length()));
            fieldProps.put("modby", saveProps.fieldcreatebyList.substring(SEPARATOR.length()));
            fieldProps.put("moddt", saveProps.fieldcreatedtList.substring(SEPARATOR.length()));
            fieldProps.put("modtool", saveProps.fieldcreatetoolList.substring(SEPARATOR.length()));
            fieldProps.put("enteredtext", saveProps.enteredtextList.substring(SEPARATOR.length()));
            fieldProps.put("displayvalue", saveProps.displayvalueList.substring(SEPARATOR.length()));
            fieldProps.put("numericvalue", saveProps.numericvalueList.substring(SEPARATOR.length()));
            fieldProps.put("datevalue", saveProps.datevalueList.substring(SEPARATOR.length()));
            fieldProps.put("clobvalue", saveProps.clobvalueList.substring(SEPARATOR.length()));
            fieldProps.put("binding", saveProps.bindingList.substring(SEPARATOR.length()));
            fieldProps.put("fieldstatus", saveProps.fieldstatusList.substring(SEPARATOR.length()));
            ab.setAction("documentfield", "AddSDIDetail", "1", fieldProps);
        }
        this.addAddReviewItemAction(documentid, documentversionid, ab, saveProps);
        if (this.form.isApprovable()) {
            HashMap<String, String> approvalProps = new HashMap<String, String>();
            approvalProps.put("sdcid", "LV_Document");
            approvalProps.put("keyid1", documentid);
            approvalProps.put("keyid2", documentversionid);
            approvalProps.put("approvaltypeid", this.form.getApprovaltypeid());
            ab.setAction("sdiapproval", "AddSDIApproval", "1", approvalProps);
        }
        if ((autolinkProps = this.getAutoLinkProps(fieldMap, saveValues, "autolink")).size() > 0) {
            autolinkProps.put("documentid", documentid);
            autolinkProps.put("documentversionid", documentversionid);
            ab.setActionClass("autolink", AutoLinkDocument.class.getName(), autolinkProps);
        }
        if (requestData.getProperty("blockdatasets").equals("Y") && boundDataSets != null && boundDataSets.size() > 0) {
            HashMap<String, String> editDataSetProps = new HashMap<String, String>();
            editDataSetProps.put("propsmatch", "Y");
            editDataSetProps.put("sdcid", boundDataSets.getValue(0, "sdcid"));
            editDataSetProps.put("keyid1", boundDataSets.getColumnValues("keyid1", ";"));
            editDataSetProps.put("keyid2", boundDataSets.getColumnValues("keyid2", ";"));
            editDataSetProps.put("keyid3", boundDataSets.getColumnValues("keyid3", ";"));
            editDataSetProps.put("paramlistid", boundDataSets.getColumnValues("paramlistid", ";"));
            editDataSetProps.put("paramlistversionid", boundDataSets.getColumnValues("paramlistversionid", ";"));
            editDataSetProps.put("variantid", boundDataSets.getColumnValues("variantid", ";"));
            editDataSetProps.put("dataset", boundDataSets.getColumnValues("dataset", ";"));
            editDataSetProps.put("documentid", StringUtil.repeat(";" + documentid, boundDataSets.size()).substring(1));
            editDataSetProps.put("documentversionid", StringUtil.repeat(";" + documentversionid, boundDataSets.size()).substring(1));
            editDataSetProps.put("blockflag", StringUtil.repeat(";Y", boundDataSets.size()).substring(1));
            ab.setAction("editdataset", "EditDataSet", "1", editDataSetProps);
        }
        DBUtil dbu = new DBUtil();
        try {
            HashMap autoattachProps;
            actionProcessor.processActionBlock(ab, !requestData.getProperty("actioncall").equals("Y"));
            this.setDocumentid(ab.getBlockProperty("documentid"));
            if (!requestData.getProperty("actioncall").equals("Y")) {
                DAMProcessor dam = new DAMProcessor(this.sapphireConnection.getConnectionId());
                this.setRsetid(dam.createLockedRSet("LV_Document", this.getDocumentid(), this.getDocumentversionid(), ""));
            }
            if (requestData.getProperty("tempdocumentid").length() > 0) {
                File file;
                boolean fileWasRenamed = false;
                String tempdocumentid = requestData.getProperty("tempdocumentid");
                if (this.form.getAttachmentslocation().length() > 0 && (file = new File(this.form.getAttachmentslocation(), tempdocumentid + "_1")).exists()) {
                    fileWasRenamed = file.renameTo(new File(this.form.getAttachmentslocation(), this.getDocumentid() + "_1"));
                }
                dbu.setConnection(this.sapphireConnection);
                if (fileWasRenamed) {
                    dbu.executePreparedUpdate("UPDATE sdiattachment SET keyid1 = ?, filename = replace( filename, '" + tempdocumentid + "', '" + this.getDocumentid() + "' ) WHERE sdcid = 'LV_Document' AND keyid1 = ?", new Object[]{this.getDocumentid(), tempdocumentid});
                } else {
                    dbu.executePreparedUpdate("UPDATE sdiattachment SET keyid1 = ? WHERE sdcid = 'LV_Document' AND keyid1 = ?", new Object[]{this.getDocumentid(), tempdocumentid});
                }
            }
            if ((autoattachProps = this.getAutoLinkProps(fieldMap, saveValues, "autoattach")).size() > 0) {
                autoattachProps.put("documentid", this.getDocumentid());
                autoattachProps.put("documentversionid", this.getDocumentversionid());
                actionProcessor.processActionClass(AutoAttachDocument.class.getName(), autoattachProps, false);
            }
            this.logger.info("New document saved");
            saved = true;
        }
        catch (Exception e) {
            this.logger.error("Failed to add document. Exception: " + e.getMessage(), e);
            throw new SapphireException("Failed to add document. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
        }
        return saved;
    }

    private boolean editDocument(String operation, HashMap fieldMap, boolean ddedata, PropertyList requestData, SaveProps saveProps, boolean rejectingData, DataSet documentfielddata, HashMap documentfielddataRowMap, PropertyListCollection saveValues, ActionProcessor actionProcessor, PropertyList reconItems) throws SapphireException {
        boolean saved;
        HashMap autoattachProps;
        HashMap autolinkProps;
        int i;
        ActionBlock ab = new ActionBlock();
        HashMap<String, String> primaryProps = new HashMap<String, String>();
        primaryProps.put("sdcid", "LV_Document");
        primaryProps.put("keyid1", this.getDocumentid());
        primaryProps.put("keyid2", this.getDocumentversionid());
        primaryProps.put("documentdesc", this.getDocumentdesc());
        primaryProps.put("documentstatus", this.getDocumentStatus());
        primaryProps.put("statusmessage", this.getStatusmessage());
        primaryProps.put("thumbnailhtml", requestData.getProperty("pagehtml"));
        if (requestData.containsKey("priority")) {
            primaryProps.put("priority", requestData.getProperty("priority"));
        }
        if (requestData.containsKey("duedt")) {
            primaryProps.put("duedt", requestData.getProperty("duedt"));
        }
        if (ddedata) {
            primaryProps.put("sysuserid2", !this.sapphireConnection.getSysuserId().equals("(system)") ? this.sapphireConnection.getSysuserId() : "");
        } else {
            primaryProps.put("sysuserid1", !this.sapphireConnection.getSysuserId().equals("(system)") ? this.sapphireConnection.getSysuserId() : "");
        }
        primaryProps.put("auditreason", saveProps.auditreason);
        primaryProps.put("auditactivity", saveProps.auditactivity);
        primaryProps.put("auditsignedflag", saveProps.auditsigned);
        ab.setAction("document", "EditSDI", "1", primaryProps);
        if (saveProps.delfieldidList.length() > 0) {
            HashMap<String, String> fieldDelProps = new HashMap<String, String>();
            fieldDelProps.put("linkid", "documentfield_link");
            fieldDelProps.put("sdcid", "LV_Document");
            fieldDelProps.put("keyid1", this.documentid);
            fieldDelProps.put("keyid2", this.documentversionid);
            fieldDelProps.put("separator", SEPARATOR);
            fieldDelProps.put("fieldid", saveProps.delfieldidList.substring(SEPARATOR.length()));
            fieldDelProps.put("fieldinstance", saveProps.delfieldinstanceList.substring(SEPARATOR.length()));
            ab.setAction("documentfield_delete", "DeleteSDIDetail", "1", fieldDelProps);
        }
        if (saveProps.addfieldidList.length() > 0) {
            HashMap<String, String> fieldAddProps = new HashMap<String, String>();
            fieldAddProps.put("linkid", "documentfield_link");
            fieldAddProps.put("sdcid", "LV_Document");
            fieldAddProps.put("keyid1", this.getDocumentid());
            fieldAddProps.put("keyid2", this.getDocumentversionid());
            fieldAddProps.put("separator", SEPARATOR);
            fieldAddProps.put("fieldid", saveProps.addfieldidList.substring(SEPARATOR.length()));
            fieldAddProps.put("fieldinstance", saveProps.addfieldinstanceList.substring(SEPARATOR.length()));
            fieldAddProps.put("createby", saveProps.addfieldcreatebyList.substring(SEPARATOR.length()));
            fieldAddProps.put("createdt", saveProps.addfieldcreatedtList.substring(SEPARATOR.length()));
            fieldAddProps.put("createtool", saveProps.addfieldcreatetoolList.substring(SEPARATOR.length()));
            fieldAddProps.put("modby", saveProps.addfieldcreatebyList.substring(SEPARATOR.length()));
            fieldAddProps.put("moddt", saveProps.addfieldcreatedtList.substring(SEPARATOR.length()));
            fieldAddProps.put("modtool", saveProps.addfieldcreatetoolList.substring(SEPARATOR.length()));
            fieldAddProps.put("enteredtext", saveProps.addenteredtextList.substring(SEPARATOR.length()));
            fieldAddProps.put("displayvalue", saveProps.adddisplayvalueList.substring(SEPARATOR.length()));
            fieldAddProps.put("numericvalue", saveProps.addnumericvalueList.substring(SEPARATOR.length()));
            fieldAddProps.put("datevalue", saveProps.adddatevalueList.substring(SEPARATOR.length()));
            fieldAddProps.put("clobvalue", saveProps.addclobvalueList.substring(SEPARATOR.length()));
            fieldAddProps.put("binding", saveProps.addbindingList.substring(SEPARATOR.length()));
            fieldAddProps.put("fieldstatus", saveProps.addfieldstatusList.substring(SEPARATOR.length()));
            ab.setAction("documentfield_add", "AddSDIDetail", "1", fieldAddProps);
        }
        if (saveProps.fieldidList.length() > 0) {
            HashMap<String, String> fieldEditProps = new HashMap<String, String>();
            fieldEditProps.put("linkid", "documentfield_link");
            fieldEditProps.put("sdcid", "LV_Document");
            fieldEditProps.put("keyid1", this.getDocumentid());
            fieldEditProps.put("keyid2", this.getDocumentversionid());
            fieldEditProps.put("separator", SEPARATOR);
            fieldEditProps.put("fieldid", saveProps.fieldidList.substring(SEPARATOR.length()));
            fieldEditProps.put("fieldinstance", saveProps.fieldinstanceList.substring(SEPARATOR.length()));
            fieldEditProps.put("modby", saveProps.fieldcreatebyList.substring(SEPARATOR.length()));
            fieldEditProps.put("moddt", saveProps.fieldcreatedtList.substring(SEPARATOR.length()));
            fieldEditProps.put("modtool", saveProps.fieldcreatetoolList.substring(SEPARATOR.length()));
            fieldEditProps.put(ddedata ? "ddeenteredtext" : "enteredtext", saveProps.enteredtextList.substring(SEPARATOR.length()));
            fieldEditProps.put(ddedata ? "ddedisplayvalue" : "displayvalue", saveProps.displayvalueList.substring(SEPARATOR.length()));
            fieldEditProps.put(ddedata ? "ddenumericvalue" : "numericvalue", saveProps.numericvalueList.substring(SEPARATOR.length()));
            fieldEditProps.put(ddedata ? "ddedatevalue" : "datevalue", saveProps.datevalueList.substring(SEPARATOR.length()));
            fieldEditProps.put("clobvalue", saveProps.clobvalueList.substring(SEPARATOR.length()));
            fieldEditProps.put("binding", saveProps.bindingList.substring(SEPARATOR.length()));
            if (ddedata && reconItems.size() > 0) {
                String[] fields = StringUtil.split(saveProps.fieldidList.substring(SEPARATOR.length()), SEPARATOR);
                StringBuffer enteredtext1 = new StringBuffer();
                StringBuffer numericvalue1 = new StringBuffer();
                StringBuffer datevalue1 = new StringBuffer();
                for (i = 0; i < fields.length; ++i) {
                    if (reconItems.containsKey(fields[i])) {
                        enteredtext1.append(SEPARATOR).append(reconItems.getProperty(fields[i]));
                        numericvalue1.append(SEPARATOR).append(reconItems.getProperty(fields[i]));
                        datevalue1.append(SEPARATOR).append(reconItems.getProperty(fields[i]));
                        continue;
                    }
                    enteredtext1.append(SEPARATOR).append(documentfielddata.getValue((Integer)documentfielddataRowMap.get(fields[i]), "enteredtext"));
                    numericvalue1.append(SEPARATOR).append(documentfielddata.getValue((Integer)documentfielddataRowMap.get(fields[i]), "numericvalue"));
                    datevalue1.append(SEPARATOR).append(documentfielddata.getValue((Integer)documentfielddataRowMap.get(fields[i]), "datevalue"));
                }
                fieldEditProps.put("enteredtext", enteredtext1.substring(SEPARATOR.length()));
                fieldEditProps.put("numericvalue", numericvalue1.substring(SEPARATOR.length()));
                fieldEditProps.put("datevalue", datevalue1.substring(SEPARATOR.length()));
            }
            if (!rejectingData) {
                fieldEditProps.put("fieldstatus", saveProps.fieldstatusList.substring(SEPARATOR.length()));
            }
            ab.setAction("documentfield_edit", "EditSDIDetail", "1", fieldEditProps);
        }
        this.addDeleteReviewItemAction(this.getDocumentid(), this.getDocumentversionid(), ab, saveProps);
        this.addAddReviewItemAction(this.getDocumentid(), this.getDocumentversionid(), ab, saveProps);
        this.addEditReviewItemAction(this.getDocumentid(), this.getDocumentversionid(), ab, saveProps);
        if (this.form.isApprovable() && "approve".equalsIgnoreCase(operation)) {
            StringBuffer approvaltypeList = new StringBuffer();
            StringBuffer approvalstepList = new StringBuffer();
            StringBuffer approvalinstList = new StringBuffer();
            StringBuffer approvalflagList = new StringBuffer();
            DataSet approvalsteps = this.documentData.getDataset("approvalstep");
            for (i = 0; i < approvalsteps.size(); ++i) {
                if (!this.isDocumentManager() && !approvalsteps.getValue(i, "stepstatusflag").equals("C")) continue;
                approvaltypeList.append(";").append(this.form.getApprovaltypeid());
                approvalstepList.append(";").append(approvalsteps.getValue(i, "approvalstep"));
                approvalinstList.append(";").append(approvalsteps.getValue(i, "approvalstepinstance"));
                approvalflagList.append(";").append("P");
            }
            HashMap<String, String> approvalProps = new HashMap<String, String>();
            approvalProps.put("sdcid", "LV_Document");
            approvalProps.put("keyid1", this.getDocumentid());
            approvalProps.put("keyid2", this.getDocumentversionid());
            approvalProps.put("approvaltypeid", approvaltypeList.substring(1));
            approvalProps.put("approvalstep", approvalstepList.substring(1));
            approvalProps.put("approvalstepinstance", approvalinstList.substring(1));
            approvalProps.put("approvalflag", approvalflagList.substring(1));
            ab.setAction("approvesdistep", "ApproveSDIStep", "1", approvalProps);
        }
        if ((autolinkProps = this.getAutoLinkProps(fieldMap, saveValues, "autolink")).size() > 0) {
            autolinkProps.put("documentid", this.getDocumentid());
            autolinkProps.put("documentversionid", this.getDocumentversionid());
            ab.setActionClass("autolink", AutoLinkDocument.class.getName(), autolinkProps);
        }
        if ((autoattachProps = this.getAutoLinkProps(fieldMap, saveValues, "autoattach")).size() > 0) {
            autoattachProps.put("documentid", this.getDocumentid());
            autoattachProps.put("documentversionid", this.getDocumentversionid());
            ab.setActionClass("autoattach", AutoAttachDocument.class.getName(), autoattachProps);
        }
        try {
            actionProcessor.processActionBlock(ab, !requestData.getProperty("actioncall").equals("Y"));
            this.logger.info("Existing document saved");
            saved = true;
        }
        catch (Exception e) {
            this.logger.error("Failed to edit document. Exception: " + e.getMessage(), e);
            throw new SapphireException("Failed to edit document. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
        }
        return saved;
    }

    public static DataSet appendBoundDataSets(PropertyList binding, DataSet boundDataSets) {
        if (binding != null && binding.size() > 0) {
            String[] stringArray;
            String[] stringArray2;
            String[] stringArray3;
            String[] stringArray4;
            String[] stringArray5;
            String[] stringArray6;
            String[] stringArray7;
            String[] stringArray8;
            int binds;
            if (boundDataSets == null) {
                boundDataSets = new DataSet("sdcid;keyid1;keyid2;keyid3;paramlistid;paramlistversionid;variantid;dataset", "|||||||");
                boundDataSets.deleteRow(0);
            }
            if ((binds = Integer.parseInt(binding.getProperty("binds", "1"))) > 1) {
                stringArray8 = StringUtil.split(binding.getProperty("sdcid"), ";");
            } else {
                String[] stringArray9 = new String[1];
                stringArray8 = stringArray9;
                stringArray9[0] = binding.getProperty("sdcid");
            }
            String[] bindsdcid = stringArray8;
            if (binds > 1) {
                stringArray7 = StringUtil.split(binding.getProperty("keyid1"), ";");
            } else {
                String[] stringArray10 = new String[1];
                stringArray7 = stringArray10;
                stringArray10[0] = binding.getProperty("keyid1");
            }
            String[] bindkeyid1 = stringArray7;
            if (binds > 1) {
                stringArray6 = StringUtil.split(binding.getProperty("keyid2"), ";");
            } else {
                String[] stringArray11 = new String[1];
                stringArray6 = stringArray11;
                stringArray11[0] = binding.getProperty("keyid2");
            }
            String[] bindkeyid2 = stringArray6;
            if (binds > 1) {
                stringArray5 = StringUtil.split(binding.getProperty("keyid3"), ";");
            } else {
                String[] stringArray12 = new String[1];
                stringArray5 = stringArray12;
                stringArray12[0] = binding.getProperty("keyid3");
            }
            String[] bindkeyid3 = stringArray5;
            if (binds > 1) {
                stringArray4 = StringUtil.split(binding.getProperty("paramlistid"), ";");
            } else {
                String[] stringArray13 = new String[1];
                stringArray4 = stringArray13;
                stringArray13[0] = binding.getProperty("paramlistid");
            }
            String[] bindparamlistid = stringArray4;
            if (binds > 1) {
                stringArray3 = StringUtil.split(binding.getProperty("paramlistversionid"), ";");
            } else {
                String[] stringArray14 = new String[1];
                stringArray3 = stringArray14;
                stringArray14[0] = binding.getProperty("paramlistversionid");
            }
            String[] bindparamlistversionid = stringArray3;
            if (binds > 1) {
                stringArray2 = StringUtil.split(binding.getProperty("variantid"), ";");
            } else {
                String[] stringArray15 = new String[1];
                stringArray2 = stringArray15;
                stringArray15[0] = binding.getProperty("variantid");
            }
            String[] bindvariantid = stringArray2;
            if (binds > 1) {
                stringArray = StringUtil.split(binding.getProperty("dataset"), ";");
            } else {
                String[] stringArray16 = new String[1];
                stringArray = stringArray16;
                stringArray16[0] = binding.getProperty("dataset");
            }
            String[] binddataset = stringArray;
            for (int j = 0; j < binds; ++j) {
                HashMap<String, String> boundDataSetFilter = new HashMap<String, String>();
                boundDataSetFilter.put("sdcid", bindsdcid[0]);
                boundDataSetFilter.put("keyid1", bindkeyid1[j]);
                boundDataSetFilter.put("keyid2", bindkeyid2[j]);
                boundDataSetFilter.put("keyid3", bindkeyid3[j]);
                boundDataSetFilter.put("paramlistid", bindparamlistid[j]);
                boundDataSetFilter.put("paramlistversionid", bindparamlistversionid[j]);
                boundDataSetFilter.put("variantid", bindvariantid[j]);
                boundDataSetFilter.put("dataset", binddataset[j]);
                if (boundDataSets.findRow(boundDataSetFilter) != -1) continue;
                int row = boundDataSets.addRow();
                boundDataSets.setValue(row, "sdcid", bindsdcid[0]);
                boundDataSets.setValue(row, "keyid1", bindkeyid1[j]);
                boundDataSets.setValue(row, "keyid2", bindkeyid2[j]);
                boundDataSets.setValue(row, "keyid3", bindkeyid3[j]);
                boundDataSets.setValue(row, "paramlistid", bindparamlistid[j]);
                boundDataSets.setValue(row, "paramlistversionid", bindparamlistversionid[j]);
                boundDataSets.setValue(row, "variantid", bindvariantid[j]);
                boundDataSets.setValue(row, "dataset", binddataset[j]);
            }
        }
        return boundDataSets;
    }

    private HashMap getAutoLinkProps(HashMap fieldMap, PropertyListCollection saveValues, String autoProp) {
        HashMap<String, String> autolinkProps = new HashMap<String, String>();
        SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
        StringBuffer fieldidList = new StringBuffer();
        StringBuffer sdcidList = new StringBuffer();
        StringBuffer sdckeycolsList = new StringBuffer();
        StringBuffer actionList = new StringBuffer();
        StringBuffer keyid1List = new StringBuffer();
        StringBuffer keyid2List = new StringBuffer();
        StringBuffer keyid3List = new StringBuffer();
        StringBuffer oldkeyid1List = new StringBuffer();
        StringBuffer oldkeyid2List = new StringBuffer();
        StringBuffer oldkeyid3List = new StringBuffer();
        StringBuffer attachmentnumList = new StringBuffer();
        for (int i = 0; i < saveValues.size(); ++i) {
            String oldKeyid2;
            String oldKeyid1;
            boolean oldvalues;
            Field temp;
            String fieldid;
            PropertyList field;
            PropertyList fieldValue = saveValues.getPropertyList(i);
            if (!fieldValue.getProperty("objecttype", "field").equals("field") || !(field = this.form.getField(fieldid = fieldValue.getProperty("fieldid"))).getProperty(autoProp, "N").equals("Y")) continue;
            if (autoProp.equals("autoattach") && field.getProperty("autosave").length() > 0 && fieldValue.getPropertyList("binding") != null) {
                String autosave = field.getProperty("autosave");
                PropertyList binding = fieldValue.getPropertyList("binding");
                fieldidList.append(";").append(fieldid);
                if (autosave.equals("primary")) {
                    sdcidList.append(";").append(binding.getProperty("sdcid"));
                    sdckeycolsList.append(";").append(binding.getProperty("keyid3").length() > 0 ? "3" : (binding.getProperty("keyid2").length() > 0 ? "2" : "1"));
                    keyid1List.append(";").append(binding.getProperty("keyid1"));
                    keyid2List.append(";").append(binding.getProperty("keyid2").length() > 0 ? binding.getProperty("keyid2") : "");
                    keyid3List.append(";").append(binding.getProperty("keyid3").length() > 0 ? binding.getProperty("keyid3") : "");
                } else {
                    sdcidList.append(";").append(autosave.equals("dataset") ? "DataSet" : "DataItem");
                    sdckeycolsList.append(";").append("1");
                    keyid1List.append(";").append(binding.getProperty(autosave.equals("dataset") ? "sdidataid" : "sdidataitemid"));
                    keyid2List.append(";").append("");
                    keyid3List.append(";").append("");
                }
                oldkeyid1List.replace(0, oldkeyid1List.length(), keyid1List.toString());
                oldkeyid2List.replace(0, oldkeyid2List.length(), keyid2List.toString());
                oldkeyid3List.replace(0, oldkeyid3List.length(), keyid3List.toString());
                StringBuffer numclauseList = new StringBuffer();
                PropertyListCollection reviewItems = fieldValue.getCollection("reviewitems");
                for (int j = 0; j < reviewItems.size(); ++j) {
                    PropertyList reviewItem = reviewItems.getPropertyList(j);
                    numclauseList.append(",'").append(reviewItem.getProperty("attachmentnum")).append("'");
                }
                attachmentnumList.append(";").append(numclauseList.length() > 0 ? numclauseList.substring(1) : "");
                if (numclauseList.length() == 0) {
                    actionList.append(";D");
                    continue;
                }
                actionList.append(";R");
                continue;
            }
            String autolinkSdcid = field.getProperty("sdcid");
            int keycols = Integer.parseInt(sdcProcessor.getProperty(autolinkSdcid, "keycolumns"));
            String autolinkkeyid1 = field.getProperty("keyid1");
            String autolinkkeyid2 = field.getProperty("keyid2");
            String autolinkkeyid3 = field.getProperty("keyid3");
            String keyid1 = fieldValue.getProperty("enteredtext");
            if (!autolinkkeyid1.equals(fieldid) || !fieldValue.getProperty("fieldstatus", "F").equals("P")) {
                keyid1 = "";
            }
            String keyid2 = (temp = (Field)fieldMap.get(autolinkkeyid2)) != null ? temp.getProperty("enteredtext") : "";
            temp = (Field)fieldMap.get(autolinkkeyid3);
            String keyid3 = temp != null ? temp.getProperty("enteredtext") : "";
            Field valueMapField1 = (Field)this.fieldValueMap.get(fieldid);
            Field valueMapField2 = (Field)this.fieldValueMap.get(autolinkkeyid2);
            Field valueMapField3 = (Field)this.fieldValueMap.get(autolinkkeyid3);
            boolean bl = oldvalues = this.fieldValueMap != null && this.fieldValueMap.size() > 0 && valueMapField1 != null;
            String string = oldvalues ? (valueMapField1.isRepeatable() ? (String)valueMapField1.getValueList().get(Integer.parseInt(fieldValue.getProperty("fieldinstance"))) : valueMapField1.getProperty("enteredtext")) : (oldKeyid1 = "");
            String string2 = oldvalues && valueMapField2 != null ? (valueMapField2.isRepeatable() ? (String)valueMapField2.getValueList().get(Integer.parseInt(fieldValue.getProperty("fieldinstance"))) : valueMapField2.getProperty("enteredtext")) : (oldKeyid2 = "");
            String oldKeyid3 = oldvalues && valueMapField3 != null ? (valueMapField3.isRepeatable() ? (String)valueMapField3.getValueList().get(Integer.parseInt(fieldValue.getProperty("fieldinstance"))) : valueMapField3.getProperty("enteredtext")) : "";
            fieldidList.append(";").append(fieldid);
            sdcidList.append(";").append(autolinkSdcid);
            sdckeycolsList.append(";").append(keycols);
            keyid1List.append(";").append(keyid1);
            keyid2List.append(";").append(keyid2);
            keyid3List.append(";").append(keyid3);
            oldkeyid1List.append(";").append(oldKeyid1);
            oldkeyid2List.append(";").append(oldKeyid2);
            oldkeyid3List.append(";").append(oldKeyid3);
            if (autoProp.equals("autoattach")) {
                StringBuffer numclauseList = new StringBuffer();
                PropertyListCollection reviewItems = fieldValue.getCollection("reviewitems");
                for (int j = 0; j < reviewItems.size(); ++j) {
                    PropertyList reviewItem = reviewItems.getPropertyList(j);
                    numclauseList.append(",'").append(reviewItem.getProperty("attachmentnum")).append("'");
                }
                attachmentnumList.append(";").append(numclauseList.length() > 0 ? numclauseList.substring(1) : "");
                if (numclauseList.length() == 0) {
                    actionList.append(";D");
                    continue;
                }
                if (!(keyid1.length() <= 0 || keycols > 1 && keyid2.length() <= 0 || keycols > 2 && keyid3.length() <= 0)) {
                    if (!(!oldvalues || oldKeyid1.length() <= 0 || keycols > 1 && oldKeyid2.length() <= 0 || keycols > 2 && oldKeyid3.length() <= 0)) {
                        actionList.append(";U");
                        continue;
                    }
                    actionList.append(";I");
                    continue;
                }
                if (!(oldKeyid1.length() <= 0 || keycols > 1 && oldKeyid2.length() <= 0 || keycols > 2 && oldKeyid3.length() <= 0)) {
                    actionList.append(";D");
                    continue;
                }
                if (!(keyid1.length() <= 0 || keycols > 1 && keyid2.length() <= 0 || keycols > 2 && keyid3.length() <= 0)) {
                    actionList.append(";R");
                    continue;
                }
                actionList.append(";N");
                continue;
            }
            if (!(keyid1.length() <= 0 || keycols > 1 && keyid2.length() <= 0 || keycols > 2 && keyid3.length() <= 0)) {
                if (!(!oldvalues || oldKeyid1.length() <= 0 || keycols > 1 && oldKeyid2.length() <= 0 || keycols > 2 && oldKeyid3.length() <= 0)) {
                    actionList.append(";U");
                    continue;
                }
                actionList.append(";I");
                continue;
            }
            if (!(oldKeyid1.length() <= 0 || keycols > 1 && oldKeyid2.length() <= 0 || keycols > 2 && oldKeyid3.length() <= 0)) {
                actionList.append(";D");
                continue;
            }
            actionList.append(";N");
        }
        if (fieldidList.length() > 0) {
            autolinkProps.put("fieldid", fieldidList.substring(1));
            autolinkProps.put("sdcid", sdcidList.substring(1));
            autolinkProps.put("sdckeycols", sdckeycolsList.substring(1));
            autolinkProps.put("action", actionList.substring(1));
            autolinkProps.put("keyid1", keyid1List.substring(1));
            autolinkProps.put("keyid2", keyid2List.substring(1));
            autolinkProps.put("keyid3", keyid3List.substring(1));
            autolinkProps.put("oldkeyid1", oldkeyid1List.substring(1));
            autolinkProps.put("oldkeyid2", oldkeyid2List.substring(1));
            autolinkProps.put("oldkeyid3", oldkeyid3List.substring(1));
            if (autoProp.equals("autoattach")) {
                autolinkProps.put("attachmentnum", attachmentnumList.substring(1));
            }
        }
        return autolinkProps;
    }

    public static DataSet getExistingDocument(SapphireConnection sapphireConnection, String formid, String formversionid, PropertyListCollection identityset, HashMap fieldMap) {
        StringBuffer identityFrom = new StringBuffer();
        StringBuffer identityWhere = new StringBuffer();
        ArrayList<String> pl = new ArrayList<String>();
        pl.add(formid);
        pl.add(formversionid);
        for (int i = 0; i < identityset.size(); ++i) {
            PropertyList identity = identityset.getPropertyList(i);
            String fieldid = identity.getProperty("fieldid");
            Field value = (Field)fieldMap.get(fieldid);
            String enteredtext = value != null ? value.toString() : "";
            identityFrom.append(", documentfield df").append(i + 1);
            identityWhere.append(" AND df").append(i + 1).append(".fieldid=? AND df").append(i + 1).append(".enteredtext=?");
            pl.add(fieldid);
            pl.add(enteredtext);
            if (i <= 0) continue;
            identityWhere.append(" AND df").append(i).append(".documentid=df").append(i + 1).append(".documentid");
        }
        QueryProcessor queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
        return queryProcessor.getPreparedSqlDataSet("SELECT d.formid, d.documentid, d.documentversionid, d.sysuserid1, d.documentstatus FROM   document d" + identityFrom + " WHERE  d.formid = ? AND d.formversionid = ?    AND d.documentid = df1.documentid  AND d.documentversionid = df1.documentversionid  " + identityWhere, pl.toArray());
    }

    public boolean saveReviewItems(String reviewitemobjectid, String reviewiteminstance, PropertyListCollection reviewItems) throws SapphireException {
        boolean saved = false;
        SaveProps saveProps = new SaveProps();
        String now = DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[shortdate] [time]");
        DataSet allreviewitemdata = this.documentData.getDataset("documentreviewitem");
        int newreviewitemid = this.initReviewItemData(allreviewitemdata);
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("reviewitemobjectid", reviewitemobjectid);
        DataSet reviewitemdata = allreviewitemdata.getFilteredDataSet(filterMap);
        for (int i = 0; i < reviewItems.size(); ++i) {
            PropertyList reviewItem = reviewItems.getPropertyList(i);
            newreviewitemid = this.updateReviewItemPropLists(reviewitemobjectid, reviewiteminstance, reviewItem, reviewitemdata, saveProps, newreviewitemid, now);
        }
        this.updateDelReviewItemPropList(reviewitemdata, saveProps);
        try {
            ActionBlock ab = new ActionBlock();
            this.addDeleteReviewItemAction(this.getDocumentid(), this.getDocumentversionid(), ab, saveProps);
            this.addAddReviewItemAction(this.getDocumentid(), this.getDocumentversionid(), ab, saveProps);
            this.addEditReviewItemAction(this.getDocumentid(), this.getDocumentversionid(), ab, saveProps);
            ActionProcessor actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
            actionProcessor.processActionBlock(ab);
            saved = true;
        }
        catch (Exception e) {
            Trace.logError("Failed to save reviewitems. Exception: " + e.getMessage(), e);
            throw new SapphireException("Failed to save reviewitems. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
        }
        return saved;
    }

    private int initReviewItemData(DataSet reviewitemdata) {
        int newreviewitemid = 0;
        if (reviewitemdata != null && reviewitemdata.size() > 0) {
            reviewitemdata.addColumn("itemchecked", 0);
            for (int i = 0; i < reviewitemdata.size(); ++i) {
                newreviewitemid = Math.max(newreviewitemid, reviewitemdata.getInt(i, "reviewitemid"));
            }
            ++newreviewitemid;
        }
        return newreviewitemid;
    }

    private int updateReviewItemPropLists(String objectid, String objectinstance, PropertyList reviewItem, DataSet reviewitemdata, SaveProps saveProps, int newreviewitemid, String now) {
        String reviewitemid = reviewItem.getProperty("reviewitemid");
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        if (reviewitemid.length() > 0) {
            int findRow;
            saveProps.editRIobjectidList.append(SEPARATOR).append(objectid);
            saveProps.editRIobjectinstanceList.append(SEPARATOR).append(objectinstance);
            saveProps.editRIidList.append(SEPARATOR).append(reviewitemid);
            saveProps.editRItypeList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemtype"));
            saveProps.editRItextList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemtext"));
            saveProps.editRIstatusList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemstatus"));
            saveProps.editRIassigntoList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemassignto"));
            saveProps.editRIresolvedstatusList.append(SEPARATOR).append(reviewItem.getProperty("resolvedstatus"));
            saveProps.editRIresolvedbyList.append(SEPARATOR).append(reviewItem.getProperty("resolvedby"));
            saveProps.editRIresolveddtList.append(SEPARATOR).append(now);
            reviewItem.setProperty("resolveddt", now);
            saveProps.editRIresolvedtextList.append(SEPARATOR).append(reviewItem.getProperty("resolvedtext"));
            saveProps.editRIannotationtypeList.append(SEPARATOR).append(reviewItem.getProperty("annotationtype"));
            saveProps.editRInotifyList.append(SEPARATOR).append(reviewItem.getProperty("notificationstatus"));
            saveProps.editRIcreatebyList.append(SEPARATOR).append(reviewItem.getProperty("createby"));
            findMap.put("reviewitemid", new BigDecimal(reviewitemid));
            if (reviewitemdata != null && (findRow = reviewitemdata.findRow(findMap)) >= 0) {
                reviewitemdata.setValue(findRow, "itemchecked", "Y");
            }
        } else if (reviewItem.getProperty("reviewitemtype").equals("Y") || reviewItem.getProperty("reviewitemtype").equals("V") || reviewItem.getProperty("reviewitemtype").equals("R")) {
            int findRow = -1;
            if (reviewitemdata != null && reviewitemdata.size() > 0) {
                findMap.put("reviewitemobjectid", objectid);
                findMap.put("reviewiteminstance", new Integer(objectinstance));
                findMap.put("reviewitemtext", reviewItem.getProperty("reviewitemtext"));
                findMap.put("reviewitemtype", reviewItem.getProperty("reviewitemtype"));
                findMap.put("createby", reviewItem.getProperty("createby"));
                findRow = reviewitemdata.findRow(findMap);
            }
            if (findRow >= 0) {
                reviewitemdata.setValue(findRow, "itemchecked", "Y");
            } else {
                saveProps.addRIobjectidList.append(SEPARATOR).append(objectid);
                saveProps.addRIobjectinstanceList.append(SEPARATOR).append(objectinstance);
                reviewItem.setProperty("reviewitemid", String.valueOf(newreviewitemid));
                reviewItem.setProperty("new", "Y");
                saveProps.addRIidList.append(SEPARATOR).append(newreviewitemid++);
                saveProps.addRItypeList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemtype"));
                saveProps.addRItextList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemtext"));
                saveProps.addRIstatusList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemstatus"));
                saveProps.addRIassigntoList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemassignto"));
                saveProps.addRIresolvedstatusList.append(SEPARATOR).append(reviewItem.getProperty("resolvedstatus"));
                saveProps.addRIresolvedbyList.append(SEPARATOR).append(reviewItem.getProperty("resolvedby"));
                saveProps.addRIresolveddtList.append(SEPARATOR).append(reviewItem.getProperty("resolveddt"));
                saveProps.addRIresolvedtextList.append(SEPARATOR).append(reviewItem.getProperty("resolvedtext"));
                saveProps.addRIenteredtextList.append(SEPARATOR).append(reviewItem.getProperty("enteredtext"));
                saveProps.addRIddeenteredtextList.append(SEPARATOR).append(reviewItem.getProperty("ddeenteredtext"));
                saveProps.addRIannotationtypeList.append(SEPARATOR).append(reviewItem.getProperty("annotationtype"));
                saveProps.addRInotifyList.append(SEPARATOR).append("N");
                saveProps.addCreatebyList.append(SEPARATOR).append(this.sapphireConnection.getSysuserId());
                saveProps.addCreatedtList.append(SEPARATOR).append(now);
                reviewItem.setProperty("createdt", now);
            }
        } else {
            saveProps.addRIobjectidList.append(SEPARATOR).append(objectid);
            saveProps.addRIobjectinstanceList.append(SEPARATOR).append(objectid.equals("__document") ? "-1" : objectinstance);
            reviewItem.setProperty("reviewitemid", String.valueOf(newreviewitemid));
            reviewItem.setProperty("new", "Y");
            saveProps.addRIidList.append(SEPARATOR).append(newreviewitemid++);
            saveProps.addRItypeList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemtype"));
            saveProps.addRItextList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemtext"));
            saveProps.addRIstatusList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemstatus"));
            saveProps.addRIassigntoList.append(SEPARATOR).append(reviewItem.getProperty("reviewitemassignto"));
            saveProps.addRIresolvedstatusList.append(SEPARATOR).append(reviewItem.getProperty("resolvedstatus"));
            saveProps.addRIresolvedbyList.append(SEPARATOR).append(reviewItem.getProperty("resolvedby"));
            saveProps.addRIresolveddtList.append(SEPARATOR).append(reviewItem.getProperty("resolveddt"));
            saveProps.addRIresolvedtextList.append(SEPARATOR).append(reviewItem.getProperty("resolvedtext"));
            saveProps.addRIenteredtextList.append(SEPARATOR).append(reviewItem.getProperty("enteredtext"));
            saveProps.addRIddeenteredtextList.append(SEPARATOR).append(reviewItem.getProperty("ddeenteredtext"));
            saveProps.addRIannotationtypeList.append(SEPARATOR).append(reviewItem.getProperty("annotationtype"));
            saveProps.addRInotifyList.append(SEPARATOR).append(reviewItem.getProperty("notificationstatus"));
            saveProps.addCreatebyList.append(SEPARATOR).append(this.sapphireConnection.getSysuserId());
            saveProps.addCreatedtList.append(SEPARATOR).append(now);
            reviewItem.setProperty("createdt", now);
        }
        return newreviewitemid;
    }

    private void updateDelFieldInstancePropList(DataSet documentfielddata, SaveProps saveProps) {
        if (documentfielddata != null) {
            for (int i = 0; i < documentfielddata.size(); ++i) {
                if (!documentfielddata.getValue(i, "itemchecked", "N").equals("N")) continue;
                saveProps.delfieldidList.append(SEPARATOR).append(documentfielddata.getValue(i, "fieldid"));
                saveProps.delfieldinstanceList.append(SEPARATOR).append(documentfielddata.getValue(i, "fieldinstance"));
            }
        }
    }

    private void updateDelReviewItemPropList(DataSet reviewitemdata, SaveProps saveProps) {
        if (reviewitemdata != null) {
            for (int i = 0; i < reviewitemdata.size(); ++i) {
                if (!reviewitemdata.getValue(i, "itemchecked", "N").equals("N")) continue;
                saveProps.delRIidList.append(SEPARATOR).append(reviewitemdata.getValue(i, "reviewitemid"));
            }
        }
    }

    private void addEditReviewItemAction(String documentid, String documentversionid, ActionBlock ab, SaveProps saveProps) throws ActionException {
        if (saveProps.editRIidList.length() > 0) {
            HashMap<String, String> editReviewitemProps = new HashMap<String, String>();
            editReviewitemProps.put("linkid", "documentreviewitem_link");
            editReviewitemProps.put("sdcid", "LV_Document");
            editReviewitemProps.put("keyid1", documentid);
            editReviewitemProps.put("keyid2", documentversionid);
            editReviewitemProps.put("separator", SEPARATOR);
            editReviewitemProps.put("reviewitemid", saveProps.editRIidList.substring(SEPARATOR.length()));
            editReviewitemProps.put("reviewitemobjectid", saveProps.editRIobjectidList.substring(SEPARATOR.length()));
            editReviewitemProps.put("reviewiteminstance", saveProps.editRIobjectinstanceList.substring(SEPARATOR.length()));
            editReviewitemProps.put("reviewitemtype", saveProps.editRItypeList.substring(SEPARATOR.length()));
            editReviewitemProps.put("reviewitemtext", saveProps.editRItextList.substring(SEPARATOR.length()));
            editReviewitemProps.put("reviewitemstatus", saveProps.editRIstatusList.substring(SEPARATOR.length()));
            editReviewitemProps.put("reviewitemassignto", saveProps.editRIassigntoList.substring(SEPARATOR.length()));
            editReviewitemProps.put("resolvedstatus", saveProps.editRIresolvedstatusList.substring(SEPARATOR.length()));
            editReviewitemProps.put("resolvedby", saveProps.editRIresolvedbyList.substring(SEPARATOR.length()));
            editReviewitemProps.put("resolveddt", saveProps.editRIresolveddtList.substring(SEPARATOR.length()));
            editReviewitemProps.put("resolvedtext", saveProps.editRIresolvedtextList.substring(SEPARATOR.length()));
            editReviewitemProps.put("annotationtype", saveProps.editRIannotationtypeList.substring(SEPARATOR.length()));
            editReviewitemProps.put("notificationstatus", saveProps.editRInotifyList.substring(SEPARATOR.length()));
            ab.setAction("documentreviewitem_edit", "EditSDIDetail", "1", editReviewitemProps);
            String[] type = StringUtil.split(saveProps.editRItypeList.toString(), SEPARATOR);
            String[] status = StringUtil.split(saveProps.editRIstatusList.toString(), SEPARATOR);
            String[] resolvedstatus = StringUtil.split(saveProps.editRIresolvedstatusList.toString(), SEPARATOR);
            String[] text = StringUtil.split(saveProps.editRItextList.toString(), SEPARATOR);
            String[] createby = StringUtil.split(saveProps.editRIcreatebyList.toString(), SEPARATOR);
            String[] notify = StringUtil.split(saveProps.editRInotifyList.toString(), SEPARATOR);
            StringBuffer descList = new StringBuffer();
            StringBuffer bodyList = new StringBuffer();
            StringBuffer urlList = new StringBuffer();
            StringBuffer userList = new StringBuffer();
            for (int i = 0; i < type.length; ++i) {
                if (!type[i].equals("F") || !status[i].equals("P") || !resolvedstatus[i].equals("R") || !notify[i].contains("RN")) continue;
                descList.append(";").append("Followup resolved");
                bodyList.append(";").append(text[i]);
                urlList.append(";").append("rc?command=page&page=EFormFollowup&documentid=").append(documentid).append("&documentversionid=").append(documentversionid);
                userList.append(";").append(createby[i]);
            }
            if (descList.length() > 0) {
                HashMap<String, String> followupProps = new HashMap<String, String>();
                followupProps.put("description", descList.substring(1));
                followupProps.put("body", bodyList.substring(1));
                followupProps.put("url", urlList.substring(1));
                followupProps.put("user", userList.substring(1));
                ab.setAction("followupbulletins_edit", "SendBulletin", "1", followupProps);
            }
        }
    }

    private void addAddReviewItemAction(String documentid, String documentversionid, ActionBlock ab, SaveProps saveProps) throws ActionException {
        if (saveProps.addRIidList.length() > 0) {
            HashMap<String, String> addReviewitemProps = new HashMap<String, String>();
            addReviewitemProps.put("linkid", "documentreviewitem_link");
            addReviewitemProps.put("sdcid", "LV_Document");
            addReviewitemProps.put("keyid1", documentid);
            addReviewitemProps.put("keyid2", documentversionid);
            addReviewitemProps.put("separator", SEPARATOR);
            addReviewitemProps.put("reviewitemid", saveProps.addRIidList.substring(SEPARATOR.length()));
            addReviewitemProps.put("reviewitemobjectid", saveProps.addRIobjectidList.substring(SEPARATOR.length()));
            addReviewitemProps.put("reviewiteminstance", saveProps.addRIobjectinstanceList.substring(SEPARATOR.length()));
            addReviewitemProps.put("reviewitemtype", saveProps.addRItypeList.substring(SEPARATOR.length()));
            addReviewitemProps.put("reviewitemtext", saveProps.addRItextList.substring(SEPARATOR.length()));
            addReviewitemProps.put("reviewitemstatus", saveProps.addRIstatusList.substring(SEPARATOR.length()));
            addReviewitemProps.put("reviewitemassignto", saveProps.addRIassigntoList.substring(SEPARATOR.length()));
            addReviewitemProps.put("resolvedstatus", saveProps.addRIresolvedstatusList.substring(SEPARATOR.length()));
            addReviewitemProps.put("resolvedby", saveProps.addRIresolvedbyList.substring(SEPARATOR.length()));
            addReviewitemProps.put("resolveddt", saveProps.addRIresolveddtList.substring(SEPARATOR.length()));
            addReviewitemProps.put("resolvedtext", saveProps.addRIresolvedtextList.substring(SEPARATOR.length()));
            if (saveProps.addRIenteredtextList.length() > 0) {
                addReviewitemProps.put("enteredtext", saveProps.addRIenteredtextList.substring(SEPARATOR.length()));
                addReviewitemProps.put("ddeenteredtext", saveProps.addRIddeenteredtextList.substring(SEPARATOR.length()));
            }
            addReviewitemProps.put("annotationtype", saveProps.addRIannotationtypeList.substring(SEPARATOR.length()));
            addReviewitemProps.put("notificationstatus", saveProps.addRInotifyList.substring(SEPARATOR.length()));
            addReviewitemProps.put("createby", saveProps.addCreatebyList.substring(SEPARATOR.length()));
            addReviewitemProps.put("createdt", saveProps.addCreatedtList.substring(SEPARATOR.length()));
            ab.setAction("documentreviewitem_add", "AddSDIDetail", "1", addReviewitemProps);
            String[] type = StringUtil.split(saveProps.addRItypeList.toString(), SEPARATOR);
            String[] status = StringUtil.split(saveProps.addRIstatusList.toString(), SEPARATOR);
            String[] text = StringUtil.split(saveProps.addRItextList.toString(), SEPARATOR);
            String[] assignto = StringUtil.split(saveProps.addRIassigntoList.toString(), SEPARATOR);
            String[] notify = StringUtil.split(saveProps.addRInotifyList.toString(), SEPARATOR);
            StringBuffer descList = new StringBuffer();
            StringBuffer bodyList = new StringBuffer();
            StringBuffer urlList = new StringBuffer();
            StringBuffer userList = new StringBuffer();
            for (int i = 0; i < type.length; ++i) {
                if (!type[i].equals("F") || !status[i].equals("F") || !notify[i].contains("SN") && !notify[i].contains("RN")) continue;
                descList.append(";").append("Followup requested");
                bodyList.append(";").append(text[i]);
                urlList.append(";").append("rc?command=page&page=EFormFollowup&documentid=").append(documentid).append("&documentversionid=").append(documentversionid);
                userList.append(";").append(assignto[i]);
            }
            if (descList.length() > 0) {
                HashMap<String, String> followupProps = new HashMap<String, String>();
                followupProps.put("description", descList.substring(1));
                followupProps.put("body", bodyList.substring(1));
                followupProps.put("url", urlList.substring(1));
                followupProps.put("user", userList.substring(1));
                ab.setAction("followupbulletins_add", "SendBulletin", "1", followupProps);
            }
        }
    }

    private void addDeleteReviewItemAction(String documentid, String documentversionid, ActionBlock ab, SaveProps saveProps) throws ActionException {
        if (saveProps.delRIidList.length() > 0) {
            HashMap<String, String> delReviewitemProps = new HashMap<String, String>();
            delReviewitemProps.put("linkid", "documentreviewitem_link");
            delReviewitemProps.put("sdcid", "LV_Document");
            delReviewitemProps.put("keyid1", documentid);
            delReviewitemProps.put("keyid2", documentversionid);
            delReviewitemProps.put("separator", SEPARATOR);
            delReviewitemProps.put("reviewitemid", saveProps.delRIidList.substring(SEPARATOR.length()));
            ab.setAction("documentreviewitem_delete", "DeleteSDIDetail", "1", delReviewitemProps);
        }
    }

    public HashMap getDocumentRowMap() {
        return this.documentData != null && this.documentData.getDataset("primary") != null && this.documentData.getDataset("primary").size() == 1 ? (HashMap)this.documentData.getDataset("primary").get(0) : null;
    }

    public PropertyList getDocumentData() {
        if (this.documentData != null && this.documentData.getDataset("primary") != null && this.documentData.getDataset("primary").size() == 1) {
            DataSet primary = this.documentData.getDataset("primary");
            String[] columns = primary.getColumns();
            PropertyList documentData = new PropertyList();
            for (int i = 0; i < columns.length; ++i) {
                documentData.setProperty(columns[i], primary.getValue(0, columns[i]));
            }
            return documentData;
        }
        return null;
    }

    public boolean processScript(Form form, HashMap bindings) throws DocumentUserException, SapphireException {
        ActionProcessor actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
        String processingType = form.getProcessingType();
        String processingRule = form.getProcessingRule();
        boolean processed = false;
        if (form.hasProcessing()) {
            if (this.getDocumentid().length() > 0) {
                if (this.documentData == null) {
                    SDIProcessor sdiProcessor = new SDIProcessor(this.sapphireConnection.getConnectionId());
                    this.documentData = Document.loadDocument(sdiProcessor, "", "", this.rsetid, "", false);
                }
                bindings.put("document", this.getDocumentRowMap());
            } else {
                bindings.put("document", new HashMap());
            }
            String status = "";
            String statusmessage = "";
            String log = "";
            if (processingType.equals("G")) {
                this.logger.info("Processing Groovy script...");
                StringBuffer groovyLog = new StringBuffer();
                try {
                    ProcessingUtil.processScript(this.sapphireConnection, processingRule, bindings, groovyLog, "DOCUMENTPROCESSING");
                    status = form.isLockondone() ? "LK" : "DN";
                    log = groovyLog.toString();
                    processed = true;
                }
                catch (Exception e) {
                    String message = e.getMessage();
                    this.setReturnmessage(groovyLog.toString() + "\nFailed to process script. Reason:\n" + (message.contains("//startinsert") ? message.substring(0, message.indexOf("//startinsert")) + "\n" + message.substring(message.indexOf("//endinsert") + 11) : message));
                    status = "ER";
                    statusmessage = this.getReturnmessage();
                }
            } else {
                this.logger.info("Processing ActionBlock...");
                ActionBlock ab = new ActionBlock("eForm: " + form.getFormid(), processingRule);
                ab.setDebugMode(true);
                try {
                    ab.setGroovyBindings(bindings);
                    ab.setBlockProperties((HashMap)bindings.get("fields"));
                    HashMap groups = (HashMap)bindings.get("groups");
                    for (String groupid : groups.keySet()) {
                        ab.setBlockProperty(groupid, ((FormGroup)groups.get(groupid)).getProperty("value"));
                    }
                    actionProcessor.processActionBlock(ab);
                    HashMap output = (HashMap)bindings.get("output");
                    for (String propertyid : ab.getReturnProperties().keySet()) {
                        output.put(propertyid, ab.getReturnProperties().get(propertyid));
                    }
                    status = form.isLockondone() ? "LK" : "DN";
                    log = ab.getDebugLog();
                    processed = true;
                }
                catch (Exception e) {
                    this.setReturnmessage(actionProcessor.getLastErrorMessage().length() > 0 ? actionProcessor.getLastErrorMessage() : "Failed to process action block. Reason: " + e.getMessage());
                    status = "ER";
                    statusmessage = this.getReturnmessage();
                    log = ab.getDebugLog();
                }
            }
            this.saveDocumentStatus(form, bindings, actionProcessor, status, statusmessage, log);
        }
        return processed;
    }

    private void saveDocumentStatus(Form form, HashMap bindings, ActionProcessor actionProcessor, String status, String statusmessage, String log) throws SapphireException {
        if (this.getDocumentid().length() > 0) {
            ActionBlock ab = new ActionBlock();
            this.setDocumentStatus(status);
            PropertyList setStatusProps = new PropertyList();
            setStatusProps.setProperty("documentid", this.documentid);
            setStatusProps.setProperty("documentversionid", this.documentversionid);
            setStatusProps.setProperty("documentstatus", status);
            setStatusProps.setProperty("statusmessage", statusmessage == null ? "" : (statusmessage.length() > 4000 ? statusmessage.substring(0, 4000) : statusmessage));
            setStatusProps.setProperty("processinglog", log.toString());
            ab.setAction("setstatus", "SetDocumentStatus", "1", setStatusProps);
            if (bindings != null) {
                HashMap output = (HashMap)bindings.get("output");
                StringBuffer fieldidList = new StringBuffer();
                StringBuffer fieldinstanceList = new StringBuffer();
                StringBuffer valueList = new StringBuffer();
                for (String fieldid : output.keySet()) {
                    if (form.getField(fieldid) == null) continue;
                    fieldidList.append(SEPARATOR).append(fieldid);
                    fieldinstanceList.append(SEPARATOR).append("0");
                    valueList.append(SEPARATOR).append((String)output.get(fieldid));
                }
                if (fieldidList.length() > 0) {
                    PropertyList setFieldValueProps = new PropertyList();
                    setFieldValueProps.setProperty("documentid", this.documentid);
                    setFieldValueProps.setProperty("documentversionid", this.documentversionid);
                    setFieldValueProps.setProperty("fieldid", fieldidList.substring(SEPARATOR.length()));
                    setFieldValueProps.setProperty("fieldinstance", fieldinstanceList.substring(SEPARATOR.length()));
                    setFieldValueProps.setProperty("value", valueList.substring(SEPARATOR.length()));
                    ab.setActionClass("setfieldvalues", SetFieldValue.class.getName(), setFieldValueProps);
                }
            }
            actionProcessor.processActionBlock(ab, true);
        }
    }

    public boolean processAutoSave(PropertyListCollection fieldValues, HashMap fieldMap, Form form, String operation, PropertyList requestData) throws SapphireException {
        SaveProps saveProps = new SaveProps();
        saveProps.auditreason = requestData.getProperty("auditreason");
        saveProps.auditactivity = requestData.getProperty("auditactivity");
        saveProps.auditsigned = requestData.getProperty("auditsigned");
        saveProps.assignto = requestData.getProperty("assignto");
        boolean processed = false;
        HashMap<String, PropertyList> sdcDatasources = new HashMap<String, PropertyList>();
        HashMap<String, HashMap<String, String>> prPropsBySDC = new HashMap<String, HashMap<String, String>>();
        HashMap<String, HashMap<String, String>> dsPropsBySDC = new HashMap<String, HashMap<String, String>>();
        HashMap<String, HashMap<String, String>> diPropsBySDC = new HashMap<String, HashMap<String, String>>();
        HashMap<String, HashMap<String, String>> dePropsBySDC = new HashMap<String, HashMap<String, String>>();
        DataSet deSDIData = new DataSet("sdcid;keyid1", "|");
        deSDIData.remove(0);
        HashMap<String, String> deSDIDataFindMap = new HashMap<String, String>();
        DataSet deSDIDataApproval = new DataSet("sdcid;keyid1;keyid2;keyid3;paramlistid;paramlistversionid;variantid;dataset;approvalstep;approvalflag", "|||||||||");
        deSDIDataApproval.remove(0);
        HashMap<String, String> deSDIDataApprovalFindMap = new HashMap<String, String>();
        DataSet drData = new DataSet("sdcid;keyid1;keyid2;keyid3;paramlistid;paramlistversionid;variantid;dataset;relationid;reagenttypeid;reagentlotid;amount;amountunits;containerid", "|||||||||||||");
        drData.deleteRow(0);
        HashMap<String, String> drFindMap = new HashMap<String, String>();
        ArrayList boundDataSets = null;
        for (int i = 0; i < fieldValues.size(); ++i) {
            PropertyList fieldValue = fieldValues.getPropertyList(i);
            Field field = (Field)fieldMap.get(fieldValue.getProperty("fieldid"));
            String autosave = field.getProperty("autosave");
            if (autosave.length() <= 0) continue;
            boolean repeatable = field.isRepeatable();
            int repeats = field.getRepeats();
            for (int repeat = 0; repeat < repeats; ++repeat) {
                int binds;
                PropertyList binding;
                String enteredtext = repeatable ? (String)field.getList("enteredtext").get(repeat) : (String)field.get("enteredtext");
                PropertyList propertyList = binding = repeatable ? (PropertyList)field.getList("binding").get(repeat) : (PropertyList)field.get("binding");
                if (!autosave.equals("primary")) {
                    boundDataSets = Document.appendBoundDataSets(binding, (DataSet)boundDataSets);
                }
                if (binding == null) continue;
                String sdcid = binding.getProperty("sdcid");
                sdcDatasources.put(sdcid, form.getDatasource(field.getProperty("datasourceid")));
                if (autosave.equals("primary")) {
                    HashMap<String, String> prProps = (HashMap<String, String>)prPropsBySDC.get(sdcid);
                    if (prProps == null) {
                        prProps = new HashMap<String, String>();
                        prPropsBySDC.put(sdcid, prProps);
                    }
                    prProps.put("sdcid", sdcid);
                    int pos = -1;
                    binds = Integer.parseInt(binding.getProperty("binds", "1"));
                    enteredtext = StringUtil.repeat(";" + enteredtext, binds).substring(1);
                    if (prProps.containsKey("keyid1")) {
                        String[] keyid1 = StringUtil.split((String)prProps.get("keyid1"), SEPARATOR);
                        for (int j = 0; j < keyid1.length; ++j) {
                            if (!keyid1[j].equals(binding.getProperty("keyid1"))) continue;
                            pos = j;
                            break;
                        }
                        if (pos == -1) {
                            prProps.put("keyid1", prProps.get("keyid1") + SEPARATOR + binding.getProperty("keyid1"));
                            prProps.put("keyid2", prProps.get("keyid2") + SEPARATOR + binding.getProperty("keyid2"));
                            prProps.put("keyid3", prProps.get("keyid3") + SEPARATOR + binding.getProperty("keyid3"));
                            prProps.put(binding.getProperty("columnid"), prProps.get(binding.getProperty("columnid")) + SEPARATOR + enteredtext);
                        } else {
                            int j;
                            String[] newcolvalues = new String[keyid1.length];
                            String columnvalue = (String)prProps.get(binding.getProperty("columnid"));
                            if (columnvalue == null) {
                                for (int j2 = 0; j2 < newcolvalues.length; ++j2) {
                                    newcolvalues[j2] = j2 == pos ? enteredtext : "";
                                }
                            } else {
                                String[] existingcolumnvalues = StringUtil.split(columnvalue, SEPARATOR);
                                for (j = 0; j < newcolvalues.length; ++j) {
                                    newcolvalues[j] = j == pos ? enteredtext : existingcolumnvalues[j];
                                }
                            }
                            StringBuffer newcolvalue = new StringBuffer();
                            for (j = 0; j < newcolvalues.length; ++j) {
                                newcolvalue.append(SEPARATOR).append(newcolvalues[j]);
                            }
                            prProps.put(binding.getProperty("columnid"), newcolvalue.substring(SEPARATOR.length()));
                        }
                    } else {
                        prProps.put("keyid1", binding.getProperty("keyid1"));
                        prProps.put("keyid2", binding.getProperty("keyid2"));
                        prProps.put("keyid3", binding.getProperty("keyid3"));
                        prProps.put(binding.getProperty("columnid"), enteredtext);
                    }
                }
                if (autosave.equals("dataset")) {
                    String columnid = binding.getProperty("columnid");
                    HashMap<String, String> dsProps = (HashMap<String, String>)dsPropsBySDC.get(sdcid + ";" + columnid);
                    if (dsProps == null) {
                        dsProps = new HashMap<String, String>();
                        dsPropsBySDC.put(sdcid + ";" + columnid, dsProps);
                    }
                    dsProps.put("sdcid", sdcid);
                    dsProps.put("keyid1", dsProps.containsKey("keyid1") ? dsProps.get("keyid1") + ";" + binding.getProperty("keyid1") : binding.getProperty("keyid1"));
                    dsProps.put("keyid2", dsProps.containsKey("keyid2") ? dsProps.get("keyid2") + ";" + binding.getProperty("keyid2") : binding.getProperty("keyid2"));
                    dsProps.put("keyid3", dsProps.containsKey("keyid3") ? dsProps.get("keyid3") + ";" + binding.getProperty("keyid3") : binding.getProperty("keyid3"));
                    dsProps.put("paramlistid", dsProps.containsKey("paramlistid") ? dsProps.get("paramlistid") + ";" + binding.getProperty("paramlistid") : binding.getProperty("paramlistid"));
                    dsProps.put("paramlistversionid", dsProps.containsKey("paramlistversionid") ? dsProps.get("paramlistversionid") + ";" + binding.getProperty("paramlistversionid") : binding.getProperty("paramlistversionid"));
                    dsProps.put("variantid", dsProps.containsKey("variantid") ? dsProps.get("variantid") + ";" + binding.getProperty("variantid") : binding.getProperty("variantid"));
                    dsProps.put("dataset", dsProps.containsKey("dataset") ? dsProps.get("dataset") + ";" + binding.getProperty("dataset") : binding.getProperty("dataset"));
                    binds = Integer.parseInt(binding.getProperty("binds", "1"));
                    enteredtext = StringUtil.repeat(";" + enteredtext, binds);
                    dsProps.put(columnid, dsProps.containsKey(binding.getProperty("columnid")) ? dsProps.get(binding.getProperty("columnid")) + enteredtext : enteredtext.substring(1));
                }
                if (autosave.equals("dataitem")) {
                    HashMap<String, String> diProps = (HashMap<String, String>)diPropsBySDC.get(sdcid);
                    if (diProps == null) {
                        diProps = new HashMap<String, String>();
                        diPropsBySDC.put(sdcid, diProps);
                    }
                    diProps.put("sdcid", sdcid);
                    diProps.put("keyid1", diProps.containsKey("keyid1") ? diProps.get("keyid1") + ";" + binding.getProperty("keyid1") : binding.getProperty("keyid1"));
                    diProps.put("keyid2", diProps.containsKey("keyid2") ? diProps.get("keyid2") + ";" + binding.getProperty("keyid2") : binding.getProperty("keyid2"));
                    diProps.put("keyid3", diProps.containsKey("keyid3") ? diProps.get("keyid3") + ";" + binding.getProperty("keyid3") : binding.getProperty("keyid3"));
                    diProps.put("paramlistid", diProps.containsKey("paramlistid") ? diProps.get("paramlistid") + ";" + binding.getProperty("paramlistid") : binding.getProperty("paramlistid"));
                    diProps.put("paramlistversionid", diProps.containsKey("paramlistversionid") ? diProps.get("paramlistversionid") + ";" + binding.getProperty("paramlistversionid") : binding.getProperty("paramlistversionid"));
                    diProps.put("variantid", diProps.containsKey("variantid") ? diProps.get("variantid") + ";" + binding.getProperty("variantid") : binding.getProperty("variantid"));
                    diProps.put("dataset", diProps.containsKey("dataset") ? diProps.get("dataset") + ";" + binding.getProperty("dataset") : binding.getProperty("dataset"));
                    diProps.put("paramid", diProps.containsKey("paramid") ? diProps.get("paramid") + ";" + binding.getProperty("paramid") : binding.getProperty("paramid"));
                    diProps.put("paramtype", diProps.containsKey("paramtype") ? diProps.get("paramtype") + ";" + binding.getProperty("paramtype") : binding.getProperty("paramtype"));
                    diProps.put("replicateid", diProps.containsKey("replicateid") ? diProps.get("replicateid") + ";" + binding.getProperty("replicateid") : binding.getProperty("replicateid"));
                    int binds2 = Integer.parseInt(binding.getProperty("binds", "1"));
                    enteredtext = StringUtil.repeat(";" + enteredtext, binds2);
                    diProps.put(binding.getProperty("columnid"), diProps.containsKey(binding.getProperty("columnid")) ? diProps.get(binding.getProperty("columnid")) + enteredtext : enteredtext.substring(1));
                }
                if (autosave.equals("dataentry")) {
                    HashMap<String, String> deProps = (HashMap<String, String>)dePropsBySDC.get(sdcid);
                    if (deProps == null) {
                        deProps = new HashMap<String, String>();
                        dePropsBySDC.put(sdcid, deProps);
                    }
                    deProps.put("sdcid", sdcid);
                    deProps.put("keyid1", deProps.containsKey("keyid1") ? deProps.get("keyid1") + ";" + binding.getProperty("keyid1") : binding.getProperty("keyid1"));
                    deProps.put("keyid2", deProps.containsKey("keyid2") ? deProps.get("keyid2") + ";" + binding.getProperty("keyid2") : binding.getProperty("keyid2"));
                    deProps.put("keyid3", deProps.containsKey("keyid3") ? deProps.get("keyid3") + ";" + binding.getProperty("keyid3") : binding.getProperty("keyid3"));
                    deProps.put("paramlistid", deProps.containsKey("paramlistid") ? deProps.get("paramlistid") + ";" + binding.getProperty("paramlistid") : binding.getProperty("paramlistid"));
                    deProps.put("paramlistversionid", deProps.containsKey("paramlistversionid") ? deProps.get("paramlistversionid") + ";" + binding.getProperty("paramlistversionid") : binding.getProperty("paramlistversionid"));
                    deProps.put("variantid", deProps.containsKey("variantid") ? deProps.get("variantid") + ";" + binding.getProperty("variantid") : binding.getProperty("variantid"));
                    deProps.put("dataset", deProps.containsKey("dataset") ? deProps.get("dataset") + ";" + binding.getProperty("dataset") : binding.getProperty("dataset"));
                    deProps.put("paramid", deProps.containsKey("paramid") ? deProps.get("paramid") + ";" + binding.getProperty("paramid") : binding.getProperty("paramid"));
                    deProps.put("paramtype", deProps.containsKey("paramtype") ? deProps.get("paramtype") + ";" + binding.getProperty("paramtype") : binding.getProperty("paramtype"));
                    deProps.put("replicateid", deProps.containsKey("replicateid") ? deProps.get("replicateid") + ";" + binding.getProperty("replicateid") : binding.getProperty("replicateid"));
                    deProps.put("enteredtext", deProps.containsKey("enteredtext") ? deProps.get("enteredtext") + ";" + enteredtext : enteredtext);
                    deProps.put("calcexcludeflag", deProps.containsKey("calcexcludeflag") ? deProps.get("calcexcludeflag") + ";" + binding.getProperty("calcexclude") : binding.getProperty("calcexclude"));
                    deProps.put("instrumentid", deProps.containsKey("instrumentid") ? deProps.get("instrumentid") + ";" + binding.getProperty("instrumentid") : binding.getProperty("instrumentid"));
                    deSDIDataFindMap.put("sdcid", sdcid);
                    deSDIDataFindMap.put("keyid1", binding.getProperty("keyid1"));
                    int row = deSDIData.findRow(deSDIDataFindMap);
                    if (row == -1) {
                        row = deSDIData.addRow();
                        deSDIData.setValue(row, "sdcid", sdcid);
                        deSDIData.setValue(row, "keyid1", binding.getProperty("keyid1"));
                    }
                    if (operation.equals("approve")) {
                        deSDIDataApprovalFindMap.put("sdcid", sdcid);
                        deSDIDataApprovalFindMap.put("keyid1", binding.getProperty("keyid1"));
                        deSDIDataApprovalFindMap.put("keyid2", binding.getProperty("keyid2"));
                        deSDIDataApprovalFindMap.put("keyid3", binding.getProperty("keyid3"));
                        deSDIDataApprovalFindMap.put("paramlistid", binding.getProperty("paramlistid"));
                        deSDIDataApprovalFindMap.put("paramlistversionid", binding.getProperty("paramlistversionid"));
                        deSDIDataApprovalFindMap.put("variantid", binding.getProperty("variantid"));
                        deSDIDataApprovalFindMap.put("dataset", binding.getProperty("dataset"));
                        row = deSDIDataApproval.findRow(deSDIDataApprovalFindMap);
                        if (row == -1) {
                            DataSet approvalsteps = this.documentData.getDataset("approvalstep");
                            for (int j = 0; j < approvalsteps.size(); ++j) {
                                if (!this.isDocumentManager() && !approvalsteps.getValue(j, "stepstatusflag").equals("C")) continue;
                                row = deSDIDataApproval.addRow();
                                deSDIDataApproval.setValue(row, "sdcid", sdcid);
                                deSDIDataApproval.setValue(row, "keyid1", binding.getProperty("keyid1"));
                                deSDIDataApproval.setValue(row, "keyid2", binding.getProperty("keyid2"));
                                deSDIDataApproval.setValue(row, "keyid3", binding.getProperty("keyid3"));
                                deSDIDataApproval.setValue(row, "paramlistid", binding.getProperty("paramlistid"));
                                deSDIDataApproval.setValue(row, "paramlistversionid", binding.getProperty("paramlistversionid"));
                                deSDIDataApproval.setValue(row, "variantid", binding.getProperty("variantid"));
                                deSDIDataApproval.setValue(row, "dataset", binding.getProperty("dataset"));
                                deSDIDataApproval.setValue(row, "approvalstep", approvalsteps.getValue(j, "approvalstep"));
                                deSDIDataApproval.setValue(row, "approvalflag", "P");
                            }
                        }
                    }
                }
                if (!autosave.equals("datareagent")) continue;
                drFindMap.put("sdcid", sdcid);
                drFindMap.put("keyid1", binding.getProperty("keyid1"));
                drFindMap.put("keyid2", binding.getProperty("keyid2"));
                drFindMap.put("keyid3", binding.getProperty("keyid3"));
                drFindMap.put("paramlistid", binding.getProperty("paramlistid"));
                drFindMap.put("paramlistversionid", binding.getProperty("paramlistversionid"));
                drFindMap.put("variantid", binding.getProperty("variantid"));
                drFindMap.put("dataset", binding.getProperty("dataset"));
                drFindMap.put("relationid", binding.getProperty("relationid"));
                int row = drData.findRow(drFindMap);
                if (row == -1) {
                    row = drData.addRow();
                    drData.setValue(row, "sdcid", sdcid);
                    drData.setValue(row, "keyid1", binding.getProperty("keyid1"));
                    drData.setValue(row, "keyid2", binding.getProperty("keyid2"));
                    drData.setValue(row, "keyid3", binding.getProperty("keyid3"));
                    drData.setValue(row, "paramlistid", binding.getProperty("paramlistid"));
                    drData.setValue(row, "paramlistversionid", binding.getProperty("paramlistversionid"));
                    drData.setValue(row, "variantid", binding.getProperty("variantid"));
                    drData.setValue(row, "dataset", binding.getProperty("dataset"));
                    drData.setValue(row, "relationid", binding.getProperty("relationid"));
                }
                drData.setValue(row, "reagenttypeid", binding.getProperty("reagenttypeid"));
                String reagentcomponent = binding.getProperty("reagentcomponent");
                if (reagentcomponent.equals("lot")) {
                    drData.setValue(row, "reagentlotid", enteredtext);
                    continue;
                }
                if (reagentcomponent.equals("quantity") || reagentcomponent.equals("defaultquantity")) {
                    drData.setValue(row, "amount", enteredtext);
                    continue;
                }
                if (reagentcomponent.equals("units") || reagentcomponent.equals("defaultunits")) {
                    drData.setValue(row, "amountunits", enteredtext);
                    continue;
                }
                if (!reagentcomponent.equals("container")) continue;
                drData.setValue(row, "containerid", enteredtext);
            }
        }
        if (prPropsBySDC.size() > 0 || dsPropsBySDC.size() > 0 || diPropsBySDC.size() > 0 || dePropsBySDC.size() > 0 || boundDataSets != null && boundDataSets.size() > 0) {
            HashMap<String, String> filterMap;
            ActionProcessor actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
            ActionBlock ab = new ActionBlock();
            ab.setDebugMode(true);
            if (drData.size() > 0) {
                filterMap = new HashMap<String, String>();
                for (String sdcid : sdcDatasources.keySet()) {
                    filterMap.put("sdcid", sdcid);
                    DataSet drFiltered = drData.getFilteredDataSet(filterMap);
                    if (drFiltered.size() <= 0) continue;
                    HashMap<String, String> drProps = new HashMap<String, String>();
                    drProps.put("sdcid", sdcid);
                    if (drFiltered.getRowCount() == 1) {
                        drProps.put("keyid1", drFiltered.getValue(0, "keyid1"));
                        drProps.put("keyid2", drFiltered.getValue(0, "keyid2"));
                        drProps.put("keyid3", drFiltered.getValue(0, "keyid3"));
                        drProps.put("paramlistid", drFiltered.getValue(0, "paramlistid"));
                        drProps.put("paramlistversionid", drFiltered.getValue(0, "paramlistversionid"));
                        drProps.put("variantid", drFiltered.getValue(0, "variantid"));
                        drProps.put("dataset", drFiltered.getValue(0, "dataset"));
                        drProps.put("relationid", drFiltered.getValue(0, "relationid"));
                        drProps.put("reagenttypeid", drFiltered.getValue(0, "reagenttypeid"));
                        drProps.put("reagentlotid", drFiltered.getValue(0, "reagentlotid"));
                        drProps.put("amount", drFiltered.getValue(0, "amount"));
                        drProps.put("amountunits", drFiltered.getValue(0, "amountunits"));
                        drProps.put("trackitemid", drFiltered.getValue(0, "containerid"));
                    } else {
                        drProps.put("keyid1", drFiltered.getColumnValues("keyid1", ";"));
                        drProps.put("keyid2", drFiltered.getColumnValues("keyid2", ";"));
                        drProps.put("keyid3", drFiltered.getColumnValues("keyid3", ";"));
                        drProps.put("paramlistid", drFiltered.getColumnValues("paramlistid", ";"));
                        drProps.put("paramlistversionid", drFiltered.getColumnValues("paramlistversionid", ";"));
                        drProps.put("variantid", drFiltered.getColumnValues("variantid", ";"));
                        drProps.put("dataset", drFiltered.getColumnValues("dataset", ";"));
                        drProps.put("relationid", drFiltered.getColumnValues("relationid", ";"));
                        drProps.put("reagenttypeid", drFiltered.getColumnValues("reagenttypeid", ";"));
                        drProps.put("reagentlotid", drFiltered.getColumnValues("reagentlotid", ";"));
                        drProps.put("amount", drFiltered.getColumnValues("amount", ";"));
                        drProps.put("amountunits", drFiltered.getColumnValues("amountunits", ";"));
                        drProps.put("trackitemid", drFiltered.getColumnValues("containerid", ";"));
                    }
                    ab.setAction("UseReagent" + sdcid, "UseReagent", "1", drProps);
                }
            }
            if (prPropsBySDC.size() > 0) {
                for (String sdcid : prPropsBySDC.keySet()) {
                    HashMap prProps = (HashMap)prPropsBySDC.get(sdcid);
                    for (String key : prProps.keySet()) {
                        prProps.put(key, StringUtil.replaceAll((String)prProps.get(key), SEPARATOR, ";"));
                    }
                    if (!form.isTransientForm()) {
                        prProps.put("applylock", "Y");
                    }
                    prProps.put("auditactivity", saveProps.auditactivity);
                    prProps.put("auditreason", saveProps.auditreason);
                    prProps.put("auditsignedflag", saveProps.auditsigned);
                    ab.setAction("EditSDI" + sdcid, "EditSDI", "1", prProps);
                }
            }
            if (dsPropsBySDC.size() > 0) {
                for (String key : dsPropsBySDC.keySet()) {
                    HashMap dsProps = (HashMap)dsPropsBySDC.get(key);
                    dsProps.put("auditactivity", saveProps.auditactivity);
                    dsProps.put("auditreason", saveProps.auditreason);
                    dsProps.put("auditsignedflag", saveProps.auditsigned);
                    dsProps.put("propsmatch", "Y");
                    ab.setAction("EditDataSet" + key, "EditDataSet", "1", dsProps);
                }
            }
            if (diPropsBySDC.size() > 0) {
                for (String sdcid : diPropsBySDC.keySet()) {
                    HashMap diProps = (HashMap)diPropsBySDC.get(sdcid);
                    diProps.put("auditactivity", saveProps.auditactivity);
                    diProps.put("auditreason", saveProps.auditreason);
                    diProps.put("auditsignedflag", saveProps.auditsigned);
                    diProps.put("propsmatch", "Y");
                    ab.setAction("EditDataItem" + sdcid, "EditDataItem", "1", diProps);
                }
            }
            if (dePropsBySDC.size() > 0) {
                for (String sdcid : dePropsBySDC.keySet()) {
                    HashMap deProps = (HashMap)dePropsBySDC.get(sdcid);
                    PropertyList datasource = (PropertyList)sdcDatasources.get(sdcid);
                    PropertyList statusmgmt = datasource != null ? datasource.getPropertyList("statusmgmt") : new PropertyList();
                    deProps.put("autorelease", statusmgmt.getProperty("autorelease", "Y"));
                    deProps.put("auditactivity", saveProps.auditactivity);
                    deProps.put("auditreason", saveProps.auditreason);
                    deProps.put("auditsignedflag", saveProps.auditsigned);
                    deProps.put("propsmatch", "Y");
                    ab.setAction("EnterDataItem" + sdcid, "EnterDataItem", "1", deProps);
                }
            }
            if (deSDIData.size() > 0) {
                filterMap = new HashMap();
                for (String sdcid : sdcDatasources.keySet()) {
                    PropertyList datasource = (PropertyList)sdcDatasources.get(sdcid);
                    PropertyList statusmgmt = datasource != null ? datasource.getPropertyList("statusmgmt") : new PropertyList();
                    filterMap.put("sdcid", sdcid);
                    DataSet deSDIFiltered = deSDIData.getFilteredDataSet(filterMap);
                    if (deSDIFiltered.size() <= 0) continue;
                    PropertyList updateDataSetProps = new PropertyList();
                    updateDataSetProps.setProperty("sdcid", sdcid);
                    updateDataSetProps.setProperty("keyid1", deSDIFiltered.getColumnValues("keyid1", ";"));
                    updateDataSetProps.setProperty("keyid2", "(null)");
                    updateDataSetProps.setProperty("keyid3", "(null)");
                    updateDataSetProps.put("auditactivity", saveProps.auditactivity);
                    updateDataSetProps.put("auditreason", saveProps.auditreason);
                    updateDataSetProps.put("auditsignedflag", saveProps.auditsigned);
                    ab.setAction("UpdateDataSetStatus" + sdcid, "UpdateDatasetStatus", "1", updateDataSetProps);
                    PropertyList syncSDIWIprops = new PropertyList();
                    syncSDIWIprops.setProperty("sdcid", sdcid);
                    syncSDIWIprops.setProperty("keyid1", deSDIFiltered.getColumnValues("keyid1", ";"));
                    syncSDIWIprops.put("auditactivity", saveProps.auditactivity);
                    syncSDIWIprops.put("auditreason", saveProps.auditreason);
                    syncSDIWIprops.put("auditsignedflag", saveProps.auditsigned);
                    ab.setAction("SyncSDIWIStatus" + sdcid, "SyncSDIWIStatus", "1", syncSDIWIprops);
                    if (statusmgmt.getProperty("syncprimarystatus", "N").equals("Y")) {
                        HashMap<String, String> syncStatusProps = new HashMap<String, String>();
                        syncStatusProps.put("sdcid", sdcid);
                        syncStatusProps.put("keyid1", deSDIFiltered.getColumnValues("keyid1", ";"));
                        syncStatusProps.put("sdistatus", "[sdistatus]");
                        if (statusmgmt.getProperty("primarystatuscolumn").length() > 0) {
                            syncStatusProps.put("statuscolid", statusmgmt.getProperty("primarystatuscolumn"));
                        }
                        syncStatusProps.put("auditactivity", saveProps.auditactivity);
                        syncStatusProps.put("auditreason", saveProps.auditreason);
                        syncStatusProps.put("auditsignedflag", saveProps.auditsigned);
                        ab.setAction("SyncSDIDataSetStatus" + sdcid, "SyncSDIDataSetStatus", "1", syncStatusProps);
                    }
                    if (!statusmgmt.getProperty("syncaqcstatus", "N").equals("Y")) continue;
                    HashMap<String, String> syncAQCStatusProps = new HashMap<String, String>();
                    HashMap deProps = (HashMap)dePropsBySDC.get(sdcid);
                    if (deProps == null || deProps.size() <= 0) continue;
                    String paramListIds = (String)deProps.get("paramlistid");
                    String paramListVersionIds = (String)deProps.get("paramlistversionid");
                    String variantIds = (String)deProps.get("variantid");
                    String dataSets = (String)deProps.get("dataset");
                    String qcBatchIds = QCUtil.getLinkedQCBatchIds(sdcid, deSDIFiltered.getColumnValues("keyid1", ";"), paramListIds, paramListVersionIds, variantIds, dataSets, new QueryProcessor(this.sapphireConnection.getConnectionId()));
                    if (qcBatchIds.length() <= 0) continue;
                    syncAQCStatusProps.put("sdcid", "QCBatch");
                    syncAQCStatusProps.put("keyid1", qcBatchIds);
                    syncAQCStatusProps.put("auditactivity", saveProps.auditactivity);
                    syncAQCStatusProps.put("auditreason", saveProps.auditreason);
                    syncAQCStatusProps.put("auditsignedflag", saveProps.auditsigned);
                    syncAQCStatusProps.put("postdataentry", "Y");
                    ab.setAction("UpdateQCBatchStatus" + sdcid, "UpdateQCBatchStatus", "1", syncAQCStatusProps);
                }
            }
            if (deSDIDataApproval.size() > 0) {
                filterMap = new HashMap();
                for (String sdcid : sdcDatasources.keySet()) {
                    filterMap.put("sdcid", sdcid);
                    DataSet deSDIDataApprFiltered = deSDIDataApproval.getFilteredDataSet(filterMap);
                    if (deSDIDataApprFiltered.size() <= 0) continue;
                    PropertyList editDataApprovalProps = new PropertyList();
                    editDataApprovalProps.setProperty("sdcid", sdcid);
                    editDataApprovalProps.setProperty("keyid1", deSDIDataApprFiltered.getColumnValues("keyid1", ";"));
                    editDataApprovalProps.setProperty("keyid2", deSDIDataApprFiltered.getColumnValues("keyid2", ";"));
                    editDataApprovalProps.setProperty("keyid3", deSDIDataApprFiltered.getColumnValues("keyid3", ";"));
                    editDataApprovalProps.setProperty("paramlistid", deSDIDataApprFiltered.getColumnValues("paramlistid", ";"));
                    editDataApprovalProps.setProperty("paramlistversionid", deSDIDataApprFiltered.getColumnValues("paramlistversionid", ";"));
                    editDataApprovalProps.setProperty("variantid", deSDIDataApprFiltered.getColumnValues("variantid", ";"));
                    editDataApprovalProps.setProperty("dataset", deSDIDataApprFiltered.getColumnValues("dataset", ";"));
                    editDataApprovalProps.setProperty("approvalstep", deSDIDataApprFiltered.getColumnValues("approvalstep", ";"));
                    editDataApprovalProps.setProperty("approvalflag", deSDIDataApprFiltered.getColumnValues("approvalflag", ";"));
                    editDataApprovalProps.put("auditactivity", saveProps.auditactivity);
                    editDataApprovalProps.put("auditreason", saveProps.auditreason);
                    editDataApprovalProps.put("auditsignedflag", saveProps.auditsigned);
                    ab.setAction("EditDataApproval" + sdcid, "EditDataApproval", "1", editDataApprovalProps);
                }
            }
            if (boundDataSets != null && boundDataSets.size() > 0) {
                filterMap = new HashMap();
                for (String sdcid : sdcDatasources.keySet()) {
                    filterMap.put("sdcid", sdcid);
                    DataSet boundDataSetsFiltered = ((DataSet)boundDataSets).getFilteredDataSet(filterMap);
                    if (boundDataSetsFiltered.size() <= 0) continue;
                    HashMap<String, String> unblockProps = new HashMap<String, String>();
                    unblockProps.put("sdcid", sdcid);
                    unblockProps.put("keyid1", boundDataSetsFiltered.getColumnValues("keyid1", ";"));
                    unblockProps.put("keyid2", boundDataSetsFiltered.getColumnValues("keyid2", ";"));
                    unblockProps.put("keyid3", boundDataSetsFiltered.getColumnValues("keyid3", ";"));
                    unblockProps.put("paramlistid", boundDataSetsFiltered.getColumnValues("paramlistid", ";"));
                    unblockProps.put("paramlistversionid", boundDataSetsFiltered.getColumnValues("paramlistversionid", ";"));
                    unblockProps.put("variantid", boundDataSetsFiltered.getColumnValues("variantid", ";"));
                    unblockProps.put("dataset", boundDataSetsFiltered.getColumnValues("dataset", ";"));
                    unblockProps.put("blockflag", StringUtil.repeat(";N", boundDataSetsFiltered.size()).substring(1));
                    ab.setAction("UnblockDataSets" + sdcid, "EditDataSet", "1", unblockProps);
                }
            }
            if (form.isWorksheet() && form.getWorksheettype().equals("qcbatch")) {
                HashMap<String, String> unblockProps = new HashMap<String, String>();
                unblockProps.put("sdcid", "QCBatch");
                unblockProps.put("keyid1", this.getDocumentObjects().getPropertyList("datasources").getPropertyList("QCBatchDatasource").getProperty("keyid1"));
                unblockProps.put("blockflag", "N");
                ab.setAction("UnblockQCBatch", "EditSDI", "1", unblockProps);
            }
            try {
                actionProcessor.processActionBlock(ab);
                processed = true;
            }
            catch (Exception e) {
                this.setReturnmessage("Failed to process action block. Reason: " + e.getMessage());
                this.saveDocumentStatus(form, null, actionProcessor, "ER", this.getReturnmessage(), ab.getDebugLog());
            }
        }
        return processed;
    }

    public void setRsetid(String rsetid) {
        this.rsetid = rsetid;
    }

    public String getRsetid() {
        return this.rsetid != null ? this.rsetid : "";
    }

    public String getDocumentid() {
        return this.documentid != null ? this.documentid : "";
    }

    public void setDocumentid(String documentid) {
        this.documentid = documentid;
    }

    public String getDocumentversionid() {
        return this.documentversionid;
    }

    public void setDocumentversionid(String documentversionid) {
        this.documentversionid = documentversionid;
    }

    public String getDocumentdesc() {
        return this.documentdesc;
    }

    public void setDocumentdesc(String documentdesc) {
        this.documentdesc = documentdesc;
    }

    public PropertyListCollection getFieldValues() {
        return this.fieldValues;
    }

    public void setFieldValues(PropertyListCollection fieldValues) {
        this.fieldValues = fieldValues;
    }

    public HashMap getFieldValueMap(boolean ddedata) {
        return ddedata ? this.ddefieldValueMap : this.fieldValueMap;
    }

    public PropertyListCollection getGroupValues() {
        return this.groupValues;
    }

    public void setGroupValues(PropertyListCollection groupValues) {
        this.groupValues = groupValues;
    }

    public PropertyListCollection getReviewitems() {
        return this.reviewitems;
    }

    public void setReviewitems(PropertyListCollection reviewitems) {
        this.reviewitems = reviewitems;
    }

    public String getFormid() {
        return this.formid;
    }

    public void setFormid(String formid) {
        this.formid = formid;
    }

    public String getFormversionid() {
        return this.formversionid;
    }

    public void setFormversionid(String formversionid) {
        this.formversionid = formversionid;
    }

    public boolean isEditMode() {
        return this.mode == 1;
    }

    public boolean isViewMode() {
        return this.mode == 0;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getVersionStatus() {
        return this.versionstatus;
    }

    public void setVersionStatus(String status) {
        this.versionstatus = status;
    }

    public String getDocumentStatus() {
        return this.documentstatus != null ? this.documentstatus : "";
    }

    public void setDocumentStatus(String status) {
        this.documentstatus = status;
    }

    public String getStatusmessage() {
        return this.statusmessage == null ? "" : (this.statusmessage.length() > 4000 ? this.statusmessage.substring(0, 4000) : this.statusmessage);
    }

    public void setStatusmessage(String statusmessage) {
        this.statusmessage = statusmessage;
    }

    public String getSysuserid1() {
        return this.sysuserid1;
    }

    public void setSysuserid1(String sysuserid1) {
        this.sysuserid1 = sysuserid1;
    }

    public String getSysuserid2() {
        return this.sysuserid2;
    }

    public void setSysuserid2(String sysuserid2) {
        this.sysuserid2 = sysuserid2;
    }

    public PropertyList getDocumentObjects() {
        return this.documentObjects;
    }

    public void setDocumentObjects(String documentObjectsXML) throws SapphireException {
        this.documentObjects = new PropertyList();
        if (documentObjectsXML != null && documentObjectsXML.length() > 0) {
            this.documentObjects.setPropertyList(documentObjectsXML);
        }
    }

    public static PropertyList getDocumentParams(PropertyList documentObjects) {
        if (documentObjects != null && documentObjects.containsKey("datasources")) {
            PropertyList params = new PropertyList();
            PropertyList datasources = documentObjects.getPropertyList("datasources");
            if (datasources != null) {
                for (String datasourceid : datasources.keySet()) {
                    PropertyList datasourceParams = datasources.getPropertyList(datasourceid);
                    for (String paramid : datasourceParams.keySet()) {
                        params.setProperty(paramid, datasourceParams.getProperty(paramid));
                    }
                }
            }
            return params;
        }
        return null;
    }

    public String getReturnmessage() {
        return this.returnmessage != null ? this.returnmessage : "";
    }

    public void setReturnmessage(String returnmessage) {
        this.returnmessage = returnmessage;
    }

    public boolean hasFieldErrors() {
        return this.hasFieldErrors;
    }

    public boolean hasOpenFollowups() {
        return this.hasOpenFollowups;
    }

    public void setReconcilable(boolean reconcilable) {
        this.reconcilable = reconcilable;
    }

    public boolean isReconcilable() {
        return this.reconcilable;
    }

    public void setApprovable(boolean approvable) {
        this.approvable = approvable;
    }

    public boolean isApprovable() {
        return this.approvable;
    }

    public void setDocumentManager(boolean documentManager) {
        this.documentManager = documentManager;
    }

    public boolean isDocumentManager() {
        return this.documentManager;
    }

    public String getLockedby() {
        return this.documentData != null ? this.documentData.getDataset("primary").getValue(0, "__lockedby") : "";
    }

    public PropertyList getDocumentProperties() {
        PropertyList docProps = new PropertyList();
        docProps.setProperty("rsetid", this.getRsetid());
        docProps.setProperty("documentid", this.getDocumentid());
        docProps.setProperty("documentversionid", this.getDocumentversionid());
        docProps.setProperty("documentdesc", this.getDocumentdesc());
        docProps.setProperty("reviewitems", this.getReviewitems());
        docProps.setProperty("formid", this.getFormid());
        docProps.setProperty("formversionid", this.getFormversionid());
        docProps.setProperty("documentstatus", this.getDocumentStatus());
        docProps.setProperty("versionstatus", this.getVersionStatus());
        docProps.setProperty("statusmessage", this.getStatusmessage());
        docProps.setProperty("sysuserid1", this.getSysuserid1());
        docProps.setProperty("sysuserid2", this.getSysuserid2());
        docProps.setProperty("fielderrors", this.hasFieldErrors() ? "Y" : "N");
        docProps.setProperty("openfollowups", this.hasOpenFollowups() ? "Y" : "N");
        docProps.setProperty("reconcilable", this.isReconcilable() ? "Y" : "N");
        docProps.setProperty("approvable", this.isApprovable() ? "Y" : "N");
        docProps.setProperty("documentmanager", this.isDocumentManager() ? "Y" : "N");
        docProps.setProperty("lockedby", this.getLockedby());
        docProps.setProperty("documentobjects", this.getDocumentObjects());
        docProps.setProperty("createdt", this.createdt);
        docProps.setProperty("moddt", this.moddt);
        docProps.setProperty("duedt", this.duedt);
        docProps.setProperty("priority", this.priority);
        return docProps;
    }

    public void sortFieldValues(PropertyListCollection fields) {
        PropertyListCollection docFields = new PropertyListCollection();
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList formField = fields.getPropertyList(i);
            docFields.add(this.fieldValues.getPropertyList(formField.getProperty("fieldid")));
        }
        this.fieldValues = docFields;
    }

    public Form getForm() {
        return this.form;
    }

    public static String getFieldStatusText(String status, TranslationProcessor trans) {
        if (status.equals("P")) {
            return trans.translate("Pass");
        }
        if (status.equals("F")) {
            return trans.translate("Fail");
        }
        if (status.equals("E")) {
            return trans.translate("Error");
        }
        return trans.translate("Unknown");
    }

    public static String getDocumentThumbnail() {
        return "<div id=\"page001\" style=\"padding-bottom: 25.4mm; padding-left: 25.4mm; width: 215.9mm !important; padding-right: 25.4mm; height: 279.4mm !important; padding-top: 25.4mm;\">Document thumbnail not available</div>";
    }

    public static String generatePageThumbnail(SapphireConnection sapphireConnection, PropertyList formProps) {
        try {
            Form form = Form.getInstance(sapphireConnection, formProps);
            PropertyList fieldValues = new PropertyList();
            for (int i = 0; i < form.getFields().size(); ++i) {
                PropertyList field = form.getFields().getPropertyList(i);
                PropertyListCollection instances = new PropertyListCollection();
                PropertyList instance = new PropertyList();
                instance.setProperty("fieldinstance", "0");
                instance.setProperty("enteredtext", "");
                instances.add(instance);
                fieldValues.put(field.getProperty("fieldid"), instances);
            }
            HashMap fieldMap = ProcessingUtil.createFieldMap(sapphireConnection, form, fieldValues, "enteredtext", false);
            return Document.generatePageThumbnail(form, fieldMap);
        }
        catch (SapphireException e) {
            return "";
        }
    }

    public static String generatePageThumbnail(Form form, HashMap fieldMap) {
        String pagehtml = form.getFormLayout();
        try {
            Tidy tidy = new Tidy();
            byte[] htmlbytes = pagehtml.getBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(htmlbytes);
            tidy.setOnlyErrors(true);
            tidy.setErrout(new PrintWriter(new ByteArrayOutputStream()));
            org.w3c.dom.Document doc = tidy.parseDOM((InputStream)byteArrayInputStream, null);
            NodeList divlist = XPathAPI.selectNodeList((Node)doc, (String)"/html/body/div");
            for (int i = 0; i < divlist.getLength(); ++i) {
                Node div = divlist.item(i);
                if (!div.getAttributes().getNamedItem("id").getNodeValue().equals("page001")) continue;
                NodeList spanlist = XPathAPI.selectNodeList((Node)div, (String)"//span");
                for (int j = 0; j < spanlist.getLength(); ++j) {
                    Node span = spanlist.item(j);
                    if (span.getAttributes().getNamedItem("sapphire") == null || !span.getAttributes().getNamedItem("sapphire").getNodeValue().equals("field")) continue;
                    String fieldid = span.getAttributes().getNamedItem("id").getNodeValue();
                    PropertyList fielddef = form.getField(fieldid);
                    String type = fielddef.getProperty("type", "text");
                    Field field = (Field)fieldMap.get(fieldid);
                    if (field == null) continue;
                    if (field.isRepeatable()) {
                        ArrayList arrayList = field.getValueList();
                        continue;
                    }
                    String value = field.toString().equals("null") ? "" : field.toString();
                    Node parent = span.getParentNode();
                    if (type.equals("dropdown")) {
                        Element select = doc.createElement("SELECT");
                        select.setAttribute("style", "width:150px");
                        select.setAttribute("readonly", "true");
                        parent.replaceChild(select, span);
                        Element options = doc.createElement("OPTION");
                        select.appendChild(options);
                        Text optionvalue = doc.createTextNode(value);
                        options.appendChild(optionvalue);
                        continue;
                    }
                    Element input = doc.createElement("INPUT");
                    input.setAttribute("value", value);
                    input.setAttribute("readonly", "true");
                    parent.replaceChild(input, span);
                }
                NodeList buttonlist = XPathAPI.selectNodeList((Node)div, (String)"//button");
                for (int j = 0; j < buttonlist.getLength(); ++j) {
                    Node button = buttonlist.item(j);
                    button.getAttributes().getNamedItem("onclick").setNodeValue("");
                }
                NodeList linklist = XPathAPI.selectNodeList((Node)doc, (String)"//a");
                for (int j = 0; j < linklist.getLength(); ++j) {
                    Node link = linklist.item(j);
                    if (link.getAttributes().getNamedItem("href") == null) continue;
                    Element input = doc.createElement("SPAN");
                    link.getParentNode().replaceChild(input, link);
                }
            }
            pagehtml = RichTextEditor.serializeDocument(doc, false);
        }
        catch (Exception e) {
            pagehtml = "";
        }
        return pagehtml;
    }

    private class SaveProps {
        private StringBuffer fieldidList = new StringBuffer();
        private StringBuffer fieldinstanceList = new StringBuffer();
        private StringBuffer enteredtextList = new StringBuffer();
        private StringBuffer displayvalueList = new StringBuffer();
        private StringBuffer numericvalueList = new StringBuffer();
        private StringBuffer datevalueList = new StringBuffer();
        private StringBuffer clobvalueList = new StringBuffer();
        private StringBuffer bindingList = new StringBuffer();
        private StringBuffer fieldstatusList = new StringBuffer();
        private StringBuffer fieldcreatebyList = new StringBuffer();
        private StringBuffer fieldcreatedtList = new StringBuffer();
        private StringBuffer fieldcreatetoolList = new StringBuffer();
        private StringBuffer addfieldidList = new StringBuffer();
        private StringBuffer addfieldinstanceList = new StringBuffer();
        private StringBuffer addenteredtextList = new StringBuffer();
        private StringBuffer adddisplayvalueList = new StringBuffer();
        private StringBuffer addnumericvalueList = new StringBuffer();
        private StringBuffer adddatevalueList = new StringBuffer();
        private StringBuffer addclobvalueList = new StringBuffer();
        private StringBuffer addbindingList = new StringBuffer();
        private StringBuffer addfieldstatusList = new StringBuffer();
        private StringBuffer addfieldcreatebyList = new StringBuffer();
        private StringBuffer addfieldcreatedtList = new StringBuffer();
        private StringBuffer addfieldcreatetoolList = new StringBuffer();
        private StringBuffer delfieldidList = new StringBuffer();
        private StringBuffer delfieldinstanceList = new StringBuffer();
        private StringBuffer editRIidList = new StringBuffer();
        private StringBuffer editRItypeList = new StringBuffer();
        private StringBuffer editRItextList = new StringBuffer();
        private StringBuffer editRIobjectidList = new StringBuffer();
        private StringBuffer editRIobjectinstanceList = new StringBuffer();
        private StringBuffer editRIstatusList = new StringBuffer();
        private StringBuffer editRIassigntoList = new StringBuffer();
        private StringBuffer editRIresolvedstatusList = new StringBuffer();
        private StringBuffer editRIresolvedbyList = new StringBuffer();
        private StringBuffer editRIresolveddtList = new StringBuffer();
        private StringBuffer editRIresolvedtextList = new StringBuffer();
        private StringBuffer editRIannotationtypeList = new StringBuffer();
        private StringBuffer editRInotifyList = new StringBuffer();
        private StringBuffer editRIcreatebyList = new StringBuffer();
        private StringBuffer addRIidList = new StringBuffer();
        private StringBuffer addRItypeList = new StringBuffer();
        private StringBuffer addRItextList = new StringBuffer();
        private StringBuffer addRIobjectidList = new StringBuffer();
        private StringBuffer addRIobjectinstanceList = new StringBuffer();
        private StringBuffer addRIstatusList = new StringBuffer();
        private StringBuffer addRIassigntoList = new StringBuffer();
        private StringBuffer addRIresolvedstatusList = new StringBuffer();
        private StringBuffer addRIresolvedbyList = new StringBuffer();
        private StringBuffer addRIresolveddtList = new StringBuffer();
        private StringBuffer addRIresolvedtextList = new StringBuffer();
        private StringBuffer addRIenteredtextList = new StringBuffer();
        private StringBuffer addRIddeenteredtextList = new StringBuffer();
        private StringBuffer addRIannotationtypeList = new StringBuffer();
        private StringBuffer addRInotifyList = new StringBuffer();
        private StringBuffer addCreatebyList = new StringBuffer();
        private StringBuffer addCreatedtList = new StringBuffer();
        private StringBuffer delRIidList = new StringBuffer();
        private String auditreason;
        private String auditactivity;
        private String auditsigned;
        private String assignto;
        private String assigntodepartment;

        private SaveProps() {
        }
    }
}


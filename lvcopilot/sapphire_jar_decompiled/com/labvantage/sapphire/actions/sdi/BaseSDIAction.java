/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.actions.CopySDIDetail;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.cmt.CheckInSDI;
import com.labvantage.sapphire.actions.cmt.ImportSnapshot;
import com.labvantage.sapphire.actions.eventplans.AddSDIEventPlan;
import com.labvantage.sapphire.actions.sdi.AddSDIAttribute;
import com.labvantage.sapphire.actions.sdi.CopySDIAttachment;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostEditEventObject;
import com.labvantage.sapphire.modules.sdisecurity.SDISecurity;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.scheduler.SchedulerUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.StringLogger;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.cmt.SnapshotItem;

public class BaseSDIAction
extends BaseAction {
    public static HashMap<String, ArrayList<PropertyList>> getCopyDownPolicy(ArrayList<String> possibleColumns, PropertyList sdc, String[] copyDowns, ConfigurationProcessor cp) {
        PropertyListCollection copyFrom;
        HashMap<String, ArrayList<PropertyList>> out = new HashMap<String, ArrayList<PropertyList>>(copyDowns.length);
        for (String copyDown : copyDowns) {
            out.put(copyDown, new ArrayList());
        }
        PropertyList copyDown = cp.findPolicy("CopyDownPolicy", "sdcid", sdc.getProperty("sdcid"));
        if (copyDown != null && (copyFrom = copyDown.getCollection("copyfrom")) != null) {
            for (int i = 0; i < copyFrom.size(); ++i) {
                int j;
                PropertyList copyFromSDC = copyFrom.getPropertyList(i);
                String copyfromsdcid = copyFromSDC.getProperty("sdcid");
                String fkcolumnid = copyFromSDC.getProperty("fkcolumnid");
                if (copyfromsdcid.length() <= 0) continue;
                PropertyListCollection links = sdc.getCollection("links");
                if (fkcolumnid.length() == 0) {
                    for (j = 0; fkcolumnid.length() == 0 && j < links.size(); ++j) {
                        PropertyList link = links.getPropertyList(j);
                        if (!link.getProperty("linksdcid").equals(copyfromsdcid)) continue;
                        fkcolumnid = link.getProperty("sdccolumnid");
                        copyFromSDC.setProperty("fkcolumnid", fkcolumnid);
                        copyFromSDC.setProperty("fkcolumnid2", link.getProperty("sdccolumnid2", ""));
                        copyFromSDC.setProperty("fkcolumnid3", link.getProperty("sdccolumnid3", ""));
                    }
                } else {
                    for (j = 0; j < links.size(); ++j) {
                        PropertyList link = links.getPropertyList(j);
                        if (!link.getProperty("sdccolumnid", "").equals(fkcolumnid)) continue;
                        copyFromSDC.setProperty("fkcolumnid2", link.getProperty("sdccolumnid2", ""));
                        copyFromSDC.setProperty("fkcolumnid3", link.getProperty("sdccolumnid3", ""));
                    }
                }
                if (possibleColumns != null && possibleColumns.contains(fkcolumnid)) {
                    for (String cd : copyDowns) {
                        if (!copyFromSDC.containsKey("copy" + cd) || !copyFromSDC.getProperty("copy" + cd).equalsIgnoreCase("Y")) continue;
                        out.get(cd).add(copyFromSDC);
                    }
                    continue;
                }
                for (String cd : copyDowns) {
                    if (!copyFromSDC.containsKey("copy" + cd) || !copyFromSDC.getProperty("copy" + cd).equalsIgnoreCase("Y")) continue;
                    out.get(cd).add(copyFromSDC);
                }
            }
        }
        return out;
    }

    /*
     * WARNING - void declaration
     */
    protected void editSDI(String actionid, PropertyList properties) throws SapphireException {
        boolean isTriggerSDCRule;
        boolean applylock;
        String sdcid;
        boolean isImport = false;
        SDISnapshotItem sdiSnapshotItem = null;
        if (properties.get("sdisnapshotitem") != null && properties.get("sdisnapshotitem") instanceof SDISnapshotItem) {
            sdiSnapshotItem = (SDISnapshotItem)properties.get("sdisnapshotitem");
            SDIData importSDIData = sdiSnapshotItem.getSDIData();
            properties.put("sdidata", importSDIData);
            properties.put("sdcid", importSDIData.getSdcid());
            this.writeOutputLogger("Import from SDISnapshot: " + sdiSnapshotItem.toString(true, true), properties);
            this.logger.info("Import from SDISnapshot: " + sdiSnapshotItem.toString(true, true));
        }
        if ((sdcid = properties.getProperty("sdcid")).length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
        }
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcProps = sdcProcessor.getPropertyList(sdcid);
        if (sdcProps == null) {
            throw new SapphireException("INVALID_PROPERTY", "Unrecognized SDC: " + sdcid);
        }
        sdcid = sdcProps.getProperty("sdcid");
        String blockColumnUpdatesMode = properties.getProperty("blockcolumnupdatesmode", "N");
        String blockedColumns = properties.getProperty("blockedcolumns");
        BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, sdcProps, "PreEdit");
        boolean requiresBeforeEditImage = sdcPreRules.requiresBeforeEditImage() || sdcPreRules.customRulesRequiresBeforeEditImage() || blockedColumns.length() > 0;
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        DAMProcessor dam = this.getDAMProcessor();
        String rsetid = "";
        String tableid = sdcProps.getProperty("tableid");
        int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        String keycolid1 = sdcProps.getProperty("keycolid1");
        String keycolid2 = sdcProps.getProperty("keycolid2");
        String keycolid3 = sdcProps.getProperty("keycolid3");
        SDIData importSDIData = null;
        DataSet primaryData = new DataSet(this.connectionInfo);
        if (properties.get("sdidata") != null && properties.get("sdidata") instanceof SDIData) {
            importSDIData = (SDIData)properties.get("sdidata");
            primaryData = importSDIData.getDataset("primary");
            keyid1 = primaryData.getColumnValues(keycolid1, ";");
            keyid2 = primaryData.getColumnValues(keycolid2, ";");
            keyid3 = primaryData.getColumnValues(keycolid3, ";");
            isImport = true;
        }
        if ((applylock = properties.getProperty("applylock").equals("Y")) ? (rsetid = dam.createLockedRSet(sdcid, keyid1, keyid2, keyid3)).length() == 0 : (requiresBeforeEditImage || isImport) && (rsetid = dam.createRSet(sdcid, keyid1, keyid2, keyid3, true, 1)).length() == 0) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
        }
        PropertyListCollection columns = sdcProps.getCollection("columns");
        if (columns == null || columns.size() == 0) {
            throw new SapphireException("SDC properties indicate no columns!");
        }
        this.logger.info("table: " + tableid);
        this.logger.info("Populate dataset with property values");
        DataSet attributeData = new DataSet(this.connectionInfo);
        DataSet eventplanData = null;
        DataSet eventplanitemData = null;
        DataSet eventplanitempropertyData = null;
        int copies = 1;
        if (!isImport) {
            copies = StringUtil.split(properties.getProperty("keyid1"), ";").length;
            for (int i = 0; i < copies; ++i) {
                primaryData.addRow();
            }
        }
        Calendar now = m18n.getNowCalendar();
        primaryData.setString(-1, "modby", this.connectionInfo.getSysuserId());
        primaryData.setString(-1, "modtool", (isImport ? "Import/" : "") + actionid);
        primaryData.setDate(-1, "moddt", now);
        ArrayList<String> addedStringColumns = new ArrayList<String>();
        if (!isImport) {
            int pkcount = 0;
            for (int sdccol = 0; sdccol < columns.size(); ++sdccol) {
                String[] splitvalue;
                String value;
                PropertyList column = columns.getPropertyList(sdccol);
                String columnid = column.getProperty("columnid").toLowerCase();
                String pkflag = column.getProperty("pkflag");
                String datatype = column.getProperty("datatype");
                if (pkflag.equals("Y")) {
                    value = properties.getProperty("keyid" + ++pkcount);
                } else {
                    String string = value = properties.containsKey(columnid) ? properties.getProperty(columnid) : null;
                    if (value != null && value.equalsIgnoreCase("(null)") && (columnid.equalsIgnoreCase("createby") || columnid.equalsIgnoreCase("createtool") || columnid.equalsIgnoreCase("createdt") || columnid.equalsIgnoreCase("modby") || columnid.equalsIgnoreCase("modtool") || columnid.equalsIgnoreCase("moddt"))) {
                        value = null;
                    }
                }
                if (value == null) continue;
                if (copies > 1 && value.indexOf(";") > -1) {
                    splitvalue = StringUtil.split(value, ";");
                } else {
                    splitvalue = new String[copies];
                    for (int i = 0; i < copies; ++i) {
                        splitvalue[i] = value;
                    }
                }
                int values = splitvalue.length;
                if (values > copies) {
                    values = copies;
                }
                boolean date = false;
                boolean dateonly = false;
                if (datatype.equals("C")) {
                    addedStringColumns.add(columnid);
                    primaryData.addColumn(columnid, 0);
                } else if (datatype.equals("T") || datatype.equals("B")) {
                    primaryData.addColumn(columnid, 3);
                } else if (datatype.equals("N") || datatype.equals("R")) {
                    primaryData.addColumn(columnid, 1);
                } else if (datatype.equals("D")) {
                    primaryData.addColumn(columnid, 2);
                    if ("Y".equals(column.getProperty("timezoneindependent"))) {
                        dateonly = true;
                        primaryData.setTimeZoneInsensitive(columnid);
                    } else {
                        date = true;
                    }
                }
                for (int i = 0; i < values; ++i) {
                    if (date || dateonly) {
                        if (splitvalue[i].equalsIgnoreCase("(null)") || splitvalue[i].trim().length() == 0) {
                            primaryData.setDate(i, columnid, (Calendar)null);
                            continue;
                        }
                        if (dateonly) {
                            primaryData.setDate(i, columnid, m18n.parseCalendar(splitvalue[i], false));
                            continue;
                        }
                        primaryData.setDate(i, columnid, m18n.parseCalendar(splitvalue[i]));
                        continue;
                    }
                    if (splitvalue[i].equalsIgnoreCase("(null)")) {
                        splitvalue[i] = "";
                    }
                    primaryData.setValue(i, columnid, splitvalue[i]);
                }
            }
        }
        if (!sdcProps.getProperty("auditedflag").equalsIgnoreCase("N")) {
            String traceLogId = properties.getProperty("tracelogid", "").trim();
            primaryData.addColumn("tracelogid", 0);
            if (traceLogId.length() == 0) {
                String auditReason = properties.getProperty("auditreason", "");
                if (auditReason.length() > 0) {
                    this.logger.info("Generate the tracelog records");
                    String promptflag = sdcProps.getProperty("auditpromptflag");
                    String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
                    AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    try {
                        int tracelogid = Integer.parseInt(audit.addSDITraceLogEntry(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), auditReason, properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"), "Data editing", standard.equals("Y")));
                        properties.setProperty("tracelogid", String.valueOf(tracelogid));
                        for (int i = 0; i < copies; ++i) {
                            primaryData.setString(i, "tracelogid", String.valueOf(tracelogid + i));
                        }
                    }
                    catch (ServiceException e) {
                        throw new SapphireException("Failed to add audit records", e);
                    }
                } else {
                    primaryData.setString(-1, "tracelogid", null);
                }
            } else if (!properties.getProperty("tracelogid").contains(";")) {
                primaryData.setString(-1, "tracelogid", traceLogId);
            }
        }
        SDIData beforeEditImage = new SDIData();
        SDIData sdiData = isImport ? (SDIData)properties.get("sdidata") : new SDIData();
        HashMap<String, ArrayList<PropertyList>> copyDownPolicy = BaseSDIAction.getCopyDownPolicy(addedStringColumns, sdcProps, new String[]{"attributes", "eventplans", "columnvalues", "attachments", "securityset", "sdidetails", "maskinglevel"}, this.getConfigurationProcessor());
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        PostEditEventObject postEdit = new PostEditEventObject(sdcid, sdcProps, sdiData, properties);
        boolean eventPlansRequiresBeforeImage = EventManager.requiresSupplementalData(sapphireConnection, this.getErrorHandler(), postEdit);
        if (requiresBeforeEditImage || eventPlansRequiresBeforeImage || copyDownPolicy.containsKey("eventplans") && copyDownPolicy.get("eventplans").size() > 0 || copyDownPolicy.containsKey("columnvalues") && copyDownPolicy.get("columnvalues").size() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcid);
            if (rsetid.length() == 0) {
                rsetid = dam.createRSet(sdcid, keyid1, keyid2, keyid3);
            }
            sdiRequest.setRsetid(rsetid);
            sdiRequest.setRetainRsetid(true);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setExtendedDataTypes(true);
            if (eventPlansRequiresBeforeImage) {
                sdiRequest.setRequestItem("sdieventplan");
                sdiRequest.setRequestItem("sdieventplanitem");
                sdiRequest.setRequestItem("sdieventplanitemproperty");
            }
            SDIProcessor sdiProcessor = this.getSDIProcessor();
            beforeEditImage = sdiProcessor.getSDIData(sdiRequest);
            postEdit.setSupplementalData(beforeEditImage);
            postEdit.setRsetid(rsetid);
            if (beforeEditImage == null) {
                throw new SapphireException("GET_SDIDATA_FAILED", "Failed to get before edit image");
            }
        }
        sdcPreRules.setBeforeEditImage(beforeEditImage);
        if (!isImport) {
            if (rsetid.length() == 0 && (copyDownPolicy.containsKey("eventplans") && copyDownPolicy.get("eventplans").size() > 0 || sdcProps.getProperty("allowattributesflag").equalsIgnoreCase("Y"))) {
                rsetid = dam.createRSet(sdcid, keyid1, keyid2, keyid3);
            }
            if (copyDownPolicy.containsKey("eventplans") && copyDownPolicy.get("eventplans").size() > 0) {
                eventplanData = new DataSet(this.connectionInfo);
                eventplanitemData = new DataSet(this.connectionInfo);
                eventplanitempropertyData = new DataSet(this.connectionInfo);
                AddSDIEventPlan.copyDownEventPlans(eventplanData, eventplanitemData, eventplanitempropertyData, primaryData, beforeEditImage.getDataset("primary"), sdcProps, copyDownPolicy.get("eventplans"), this.database, this.connectionInfo, this.logger);
                sdiData.setDataset("sdieventplan", eventplanData);
                sdiData.setDataset("sdieventplanitem", eventplanitemData);
                sdiData.setDataset("sdieventplanitemproperty", eventplanitempropertyData);
            }
            if (copyDownPolicy.containsKey("securityset") && copyDownPolicy.get("securityset").size() > 0) {
                SDISecurity.copyDownSecuritySet(sdcProps, primaryData, beforeEditImage.getDataset("primary"), copyDownPolicy.get("securityset"), this.getSDCProcessor(), this.getSDIProcessor(), this.getQueryProcessor(), this.getActionProcessor());
            }
            if (copyDownPolicy.containsKey("columnvalues") && copyDownPolicy.get("columnvalues").size() > 0) {
                SdiInfo.copyDownColumns(sdcProps, primaryData, beforeEditImage.getDataset("primary"), copyDownPolicy.get("columnvalues"), this.getSDCProcessor(), this.getSDIProcessor());
            }
            if ("Y".equals(sdcProps.getProperty("maskableflag")) && copyDownPolicy.containsKey("maskinglevel") && copyDownPolicy.get("maskinglevel").size() > 0) {
                DataMaskUtil.copyDownMaskingLevel(sdcid, primaryData, beforeEditImage.getDataset("primary"), copyDownPolicy, this.getSDCProcessor(), this.getSDIProcessor());
            }
            if (copyDownPolicy != null && copyDownPolicy.containsKey("attachments") && copyDownPolicy.get("attachments").size() > 0) {
                CopySDIAttachment.copyDownSDIAttachment(sdcProps, primaryData, beforeEditImage.getDataset("primary"), copyDownPolicy.get("attachments"), sapphireConnection);
            }
            if (copyDownPolicy.containsKey("sdidetails") && copyDownPolicy.get("sdidetails").size() > 0) {
                CopySDIDetail.copyDownSDIDetails(sdcProps, primaryData, beforeEditImage.getDataset("primary"), (List<PropertyList>)copyDownPolicy.get("sdidetails"), sdcProcessor, this.getActionProcessor(), this.getSDIProcessor());
            }
            if (sdcProps.getProperty("allowattributesflag").equalsIgnoreCase("Y")) {
                HashMap<String, ArrayList<String>> skippedAttributes = new HashMap<String, ArrayList<String>>();
                String[] addedStringCols = new String[addedStringColumns.size()];
                addedStringColumns.toArray(addedStringCols);
                AddSDIAttribute.copyDownAttributes(attributeData, null, primaryData, beforeEditImage.getDataset("primary"), addedStringCols, sdcProps, keycolid1, keycolid2, keycolid3, copyDownPolicy.get("attributes"), skippedAttributes, rsetid, this.getQueryProcessor(), this.getSDIProcessor(), m18n, this.getConfigurationProcessor(), this.getSDCProcessor(), this.getConnectionProcessor(), this.logger);
                AddSDIAttribute.logSkipped(skippedAttributes, sdcid, this.logger);
            }
            attributeData.setString(-1, "createby", this.connectionInfo.getSysuserId());
            attributeData.setString(-1, "createtool", actionid);
            attributeData.setDate(-1, "createdt", now);
            attributeData.setString(-1, "modby", this.connectionInfo.getSysuserId());
            attributeData.setString(-1, "modtool", actionid);
            attributeData.setDate(-1, "moddt", now);
            sdiData.setDataset("primary", primaryData);
            sdiData.setDataset("attribute", attributeData);
        }
        boolean bl = isTriggerSDCRule = isImport ? CMTPolicy.getPolicy(this.getConnectionid(), sdcid).isTriggerBusinessRule() : true;
        if (isImport) {
            sdcPreRules.setCMTImport(isImport);
            sdcPreRules.preCMTImport(sdiData, properties, false);
            for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                customRules.setCMTImport(isImport);
                customRules.preCMTImport(sdiData, properties, false);
            }
        }
        DataSet beforeEditImagePrimary = beforeEditImage.getDataset("primary");
        if (blockedColumns != null && blockedColumns.length() > 0 && beforeEditImagePrimary != null) {
            String[] blockColArray = StringUtil.split(blockedColumns, ";");
            DataSet primary = sdiData.getDataset("primary");
            List<String> primaryCols = Arrays.asList(primary.getColumns());
            for (String columnid : blockColArray) {
                if (columnid.length() <= 0 || !primaryCols.contains(columnid)) continue;
                for (int i = 0; i < primary.size(); ++i) {
                    int oldRow;
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put(keycolid1, primary.getValue(i, keycolid1));
                    if (keycolid2.length() > 0) {
                        findMap.put(keycolid1, primary.getValue(i, keycolid2));
                    }
                    if (keycolid3.length() > 0) {
                        findMap.put(keycolid3, primary.getValue(i, keycolid3));
                    }
                    if ((oldRow = beforeEditImagePrimary.findRow(findMap)) < 0) continue;
                    String preResetValue = primary.getValue(i, columnid);
                    primary.setObject(i, columnid, beforeEditImagePrimary.getObject(oldRow, columnid));
                    String postResetValue = primary.getValue(i, columnid);
                    if (primary.getValue(i, columnid).equals(preResetValue)) continue;
                    if (blockColumnUpdatesMode.equals("E")) {
                        this.logger.error("An attempt was made to modify blocked column " + columnid + " from '" + postResetValue + "' to '" + preResetValue + "'. Throwing Error.");
                        throw new ActionException("An attempt was made to modify blocked column " + columnid + " from '" + postResetValue + "' to '" + preResetValue + "'.");
                    }
                    this.logger.warn("An attempt was made to modify blocked column " + columnid + " from '" + postResetValue + "' to '" + preResetValue + "'. New value ignored.");
                }
            }
        }
        if (isTriggerSDCRule) {
            Trace.startBusinessRule(sdcid + "." + "PreEdit", true);
            sdcPreRules.preEdit(sdiData, properties);
            Trace.endBusinessRule(sdcid + "." + "PreEdit", true);
            Trace.startBusinessRule(sdcid + "." + "PreEdit", false);
            for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                customRules.preEdit(sdiData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PreEdit", false);
            sdcPreRules.endRule();
        } else {
            this.writeOutputLogger("Not trigger preEdit business rule based on transfer option", properties);
        }
        this.logger.info("Issueing update statements");
        try {
            ImportSnapshot.ImportInstructions instructions = (ImportSnapshot.ImportInstructions)properties.get("importInstructions");
            boolean isIgnoreMissingObjects = instructions != null ? instructions.isIgnoreMissingObjects() : false;
            DataSet toDeleteDataSet = new DataSet();
            if (isImport) {
                PropertyListCollection embeddedCollections;
                if (isIgnoreMissingObjects) {
                    String[] sourcecolumns;
                    for (String colid : sourcecolumns = primaryData.getColumns()) {
                        boolean colexist;
                        if (colid.indexOf("__") == 0) continue;
                        boolean bl2 = colexist = sdcProcessor.getSDCColumnProperty(sdcid, colid, "columnid").length() > 0;
                        if (colexist) continue;
                        primaryData.renameColumn(colid, "__" + colid);
                        this.writeOutputLogger("Ignore Missing Object, table " + tableid + " column " + colid, properties);
                    }
                }
                PropertyListCollection propertyListCollection = embeddedCollections = sdiSnapshotItem.getPolicyNodeProps() != null ? sdiSnapshotItem.getPolicyNodeProps().getCollection("sdidatasets") : null;
                if (embeddedCollections != null && embeddedCollections.size() > 0) {
                    HashSet<String> sourceEmbeddedSDISet = new HashSet<String>();
                    List<SnapshotItem> sourceembeddedItems = sdiSnapshotItem.getLinkItems();
                    for (int e = 0; e < sourceembeddedItems.size(); ++e) {
                        SDISnapshotItem item = (SDISnapshotItem)sourceembeddedItems.get(e);
                        CMTPolicy itemPolicy = CMTPolicy.getSDISnapshotItemPolicy(this.connectionInfo.getConnectionId(), item);
                        String sdikey = item.getSDCId() + ";" + item.getKeyId1() + ";" + item.getKeyId2() + ";" + item.getKeyId3();
                        boolean isAutoKey = sdcProcessor.getProperty(item.getSDCId(), "keygenerationrule").length() > 0;
                        boolean isEmbededFlushTarget = itemPolicy.getTransferOption().isFlushTarget();
                        boolean bl3 = itemPolicy.getTransferOption().isRegenerateKey();
                        if (!isEmbededFlushTarget) {
                            String identifycolumn = itemPolicy.getIndentifyColumn();
                            String matchkey = "";
                            if (identifycolumn.length() > 0) {
                                String[] identifycolumns = StringUtil.split(identifycolumn, ",");
                                for (int i = 0; i < identifycolumns.length; ++i) {
                                    SDIData itemSDIData = item.getSDIData();
                                    if (itemSDIData == null) {
                                        itemSDIData = item.getSnapshot().getSnapshotPackage().getSnapshot(item).getSDIData();
                                    }
                                    if (itemSDIData == null) continue;
                                    matchkey = matchkey + ";" + itemSDIData.getDataset("primary").getValue(0, identifycolumns[i].trim());
                                }
                                if (matchkey.length() > 1) {
                                    matchkey = matchkey.substring(1);
                                }
                            } else {
                                matchkey = isAutoKey && !bl3 ? sdikey : (isAutoKey && item.getSDIData() != null && item.getSDIData().getDataset("primary").isValidColumn("uuid") ? item.getSDIData().getDataset("primary").getValue(0, "uuid") : sdikey);
                            }
                            sourceEmbeddedSDISet.add(matchkey);
                            continue;
                        }
                        this.writeOutputLogger("Transfer Option is Flush Target Embedded SDI: " + sdikey, properties);
                    }
                    SnapshotFactory snapshotFactory = new SnapshotFactory(this.getConnectionId(), this.getRakFile());
                    SDISnapshot targetSDISnapshot = null;
                    try {
                        targetSDISnapshot = snapshotFactory.generateSDISnapshot(sdcid, keyid1, keyid2, keyid3, sdiSnapshotItem);
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed generate Target Snapshot for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                    }
                    List<SnapshotItem> targetembeddedItems = targetSDISnapshot.getSnapshotItem().getLinkItems();
                    HashSet<void> targetEmbeddedSDISet = new HashSet<void>();
                    HashMap<void, String> targetKeySDIMap = new HashMap<void, String>();
                    for (int e = 0; e < targetembeddedItems.size(); ++e) {
                        void var60_154;
                        String versionstatus;
                        SDISnapshotItem sDISnapshotItem = (SDISnapshotItem)targetembeddedItems.get(e);
                        CMTPolicy itemPolicy = CMTPolicy.getSDISnapshotItemPolicy(this.connectionInfo.getConnectionId(), sDISnapshotItem);
                        String identifycolumn = itemPolicy.getIndentifyColumn();
                        String sdikey = sDISnapshotItem.getSDCId() + ";" + sDISnapshotItem.getKeyId1() + ";" + sDISnapshotItem.getKeyId2() + ";" + sDISnapshotItem.getKeyId3();
                        boolean isAutoKey = sdcProcessor.getProperty(sDISnapshotItem.getSDCId(), "keygenerationrule").length() > 0;
                        boolean isRegenerateKey = itemPolicy.getTransferOption().isRegenerateKey();
                        String string = "";
                        if (identifycolumn.length() > 0) {
                            void var60_156;
                            String[] identifycolumns = StringUtil.split(identifycolumn, ",");
                            for (int i = 0; i < identifycolumns.length; ++i) {
                                String string2 = (String)var60_156 + ";" + sDISnapshotItem.getSDIData().getDataset("primary").getValue(0, identifycolumns[i].trim());
                            }
                            String string3 = var60_156.substring(1);
                        } else if (isAutoKey && !isRegenerateKey) {
                            String string4 = sdikey;
                        } else if (isAutoKey && sDISnapshotItem.getSDIData() != null && sDISnapshotItem.getSDIData().getDataset("primary").isValidColumn("uuid")) {
                            String string5 = sDISnapshotItem.getSDIData().getDataset("primary").getValue(0, "uuid");
                        } else {
                            String string6 = sdikey;
                        }
                        String string7 = versionstatus = sDISnapshotItem.getSDIData() != null ? sDISnapshotItem.getSDIData().getDataset("primary").getValue(0, "versionstatus") : "";
                        if (!"A".equals(versionstatus) && !"C".equals(versionstatus)) {
                            targetEmbeddedSDISet.add(var60_154);
                        }
                        targetKeySDIMap.put(var60_154, sdikey);
                    }
                    if (targetEmbeddedSDISet.size() > 0) {
                        targetEmbeddedSDISet.removeAll(sourceEmbeddedSDISet);
                        if (targetEmbeddedSDISet.size() > 0) {
                            toDeleteDataSet.addColumn("sdcid", 0);
                            toDeleteDataSet.addColumn("keyid1", 0);
                            toDeleteDataSet.addColumn("keyid2", 0);
                            toDeleteDataSet.addColumn("keyid3", 0);
                            for (String string : targetEmbeddedSDISet) {
                                String sdikey = (String)targetKeySDIMap.get(string);
                                String[] sdi = StringUtil.split(sdikey, ";");
                                int row = toDeleteDataSet.addRow();
                                toDeleteDataSet.setValue(row, "sdcid", sdi[0]);
                                toDeleteDataSet.setValue(row, "keyid1", sdi[1]);
                                toDeleteDataSet.setValue(row, "keyid2", sdi[2]);
                                toDeleteDataSet.setValue(row, "keyid3", sdi[3]);
                            }
                        }
                    }
                }
            }
            DataSetUtil.update(this.database, primaryData, tableid, new String[]{keycolid1, keycolid2, keycolid3});
            if (isImport && toDeleteDataSet.size() > 0) {
                toDeleteDataSet.sort("sdcid");
                ArrayList<DataSet> sdcGroups = toDeleteDataSet.getGroupedDataSets("sdcid");
                for (int g = 0; g < sdcGroups.size(); ++g) {
                    DataSet ds = sdcGroups.get(g);
                    String itemsdcid = ds.getValue(0, "sdcid");
                    String itemkeyid1 = ds.getColumnValues("keyid1", ";");
                    String itemkeyid2 = ds.getColumnValues("keyid2", ";");
                    String itemkeyid3 = ds.getColumnValues("keyid3", ";");
                    this.writeOutputLogger("Delete embedded SDI not exist in import or Import Option is Flush (" + itemsdcid + "," + itemkeyid1 + "," + itemkeyid2 + "," + itemkeyid3 + ")", properties);
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", itemsdcid);
                    props.setProperty("keyid1", itemkeyid1);
                    props.setProperty("keyid2", itemkeyid2);
                    props.setProperty("keyid3", itemkeyid3);
                    this.getActionProcessor().processAction("DeleteSDI", "1", props);
                    this.writeOutputLogger("Done Delete embedded SDIs", properties);
                }
            }
            if (eventplanData != null && eventplanData.size() > 0) {
                DataSetUtil.insert(this.database, eventplanData, "sdieventplan");
                DataSetUtil.insert(this.database, eventplanitemData, "sdieventplanitem");
                DataSetUtil.insert(this.database, eventplanitempropertyData, "sdieventplanitemproperty");
            }
            if (attributeData.size() > 0) {
                this.logger.debug("Updating attributes...");
                attributeData.setString(-1, "sdiattributeid", "");
                DataSetUtil.insert(this.database, attributeData, "sdiattribute");
                this.logger.debug("Attributes updated.");
            }
            if (isImport) {
                String keyclause;
                CharSequence keyclause1;
                SafeSQL safeSQLKeyClause = new SafeSQL();
                if (this.connectionInfo.isOracle()) {
                    keyclause1 = new StringBuffer(keycolid1);
                    StringBuffer keyclause2 = new StringBuffer("keyid1");
                    if (keycols > 1) {
                        ((StringBuffer)keyclause1).append(",").append(keycolid2);
                        keyclause2.append(", keyid2");
                    }
                    if (keycols > 2) {
                        ((StringBuffer)keyclause1).append(",").append(keycolid3);
                        keyclause2.append(", keyid3");
                    }
                    keyclause = "(" + ((StringBuffer)keyclause1).toString() + ") IN (SELECT " + keyclause2.toString() + " FROM rsetitems WHERE rsetid=" + safeSQLKeyClause.addVar(rsetid) + ")";
                } else {
                    keyclause1 = keycolid1 + " IN ( SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQLKeyClause.addVar(rsetid) + " )";
                    if (keycols == 2) {
                        keyclause1 = (String)keyclause1 + " AND " + keycolid1 + " + ';' + " + keycolid2 + " IN ( SELECT keyid1 + ';' + keyid2 FROM rsetitems WHERE rsetid=" + safeSQLKeyClause.addVar(rsetid) + " ) ";
                    } else if (keycols == 3) {
                        keyclause1 = (String)keyclause1 + " AND " + keycolid1 + " + ';' + " + keycolid2 + " + ';' + " + keycolid3 + " IN ( SELECT keyid1 + ';' + keyid2 + ';' + keyid3 FROM rsetitems WHERE rsetid=" + safeSQLKeyClause.addVar(rsetid) + " ) ";
                    }
                    keyclause = "(" + (String)keyclause1 + ")";
                }
                String[] sdixxxDatasetNames = new String[]{"sdiattribute", "attachment", "dataset", "dataitem", "datalimit", "dataapproval", "datarelation", "dataspec", "sdispec", "sdispecrule", "address", "coc", "pricelist", "category", "role", "sdiworkitem", "sdiworkitemitem", "workitemrelation", "approval", "approvalstep", "document", "formrule", "sdieventplan", "sdieventplanitem", "sdieventplanitemproperty", "sdiworkflowrule", "notes", "sdiattribute", "sdialias", "sdiworksheetrule", "sdiresourcerequirement", "sdcjobtypesecurity"};
                PropertyListCollection links = sdcProps.getCollectionNotNull("links");
                PropertyListCollection detailLinks = sdcProcessor.getDetailLinks(sdcid);
                if (detailLinks != null) {
                    links.addAll(detailLinks);
                }
                ArrayList<String> alldetailList = new ArrayList<String>();
                List<String> allsdixxxdetaillist = Arrays.asList(sdixxxDatasetNames);
                alldetailList.addAll(allsdixxxdetaillist);
                for (int i = 0; i < links.size(); ++i) {
                    CMTPolicy targetCMTPolicy;
                    String linktype = links.getPropertyList(i).getProperty("linktype");
                    String string = links.getPropertyList(i).getProperty("linktableid");
                    if ("D".equals(linktype)) {
                        alldetailList.add(string);
                        continue;
                    }
                    if (!"M".equals(linktype) || !(targetCMTPolicy = CMTPolicy.getPolicy(this.getConnectionid(), sdcid)).getDetailDataSetList(false).contains(string)) continue;
                    alldetailList.add(string);
                }
                CMTPolicy sourceCMTPolicy = null;
                if (properties.get("sdisnapshotitem") != null && properties.get("sdisnapshotitem") instanceof SDISnapshotItem) {
                    SDISnapshotItem snapshotItem = (SDISnapshotItem)properties.get("sdisnapshotitem");
                    sourceCMTPolicy = CMTPolicy.getSDISnapshotItemPolicy(this.getConnectionid(), snapshotItem);
                }
                for (int i = 0; i < alldetailList.size(); ++i) {
                    SafeSQL safeSQL;
                    String string = (String)alldetailList.get(i);
                    DataSet dataset = sdiData.getDataset(string);
                    if ("primary".equals(string)) continue;
                    boolean isSDIDetail = allsdixxxdetaillist.contains(string);
                    String tablename = SDIData.getDatasetTablename(string);
                    if (isIgnoreMissingObjects && !this.database.checkPreparedExists("SELECT 1 FROM systable WHERE tableid=?", new Object[]{tablename})) {
                        this.writeOutputLogger("Ignore missing table " + tablename, properties);
                        continue;
                    }
                    if (sourceCMTPolicy != null && !"sdcjobtypesecurity".equals(tablename)) {
                        PropertyList detailTableProps;
                        PropertyList propertyList = detailTableProps = isSDIDetail ? sourceCMTPolicy.getSDIDetailProps(tablename) : sourceCMTPolicy.getDetailProps(tablename);
                        if (detailTableProps == null || detailTableProps.size() == 0 || "N".equals(detailTableProps.getProperty("enabled")) || !"UNDOCHECKOUT".equals(properties.getProperty("__importSource", "")) && "N".equals(detailTableProps.getProperty("includeintransfer"))) {
                            this.logger.info("Skip flushing (and importing) data from table: " + tablename + ". Is un-defined/disabled/non-transferable in source CMTPolicy.");
                            continue;
                        }
                    }
                    if (isIgnoreMissingObjects && dataset != null && dataset.getRowCount() > 0) {
                        String[] sourcecolumns = dataset.getColumns();
                        DataSet targetColumnDs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT columnid FROM syscolumn WHERE tableid=?", new Object[]{tablename});
                        for (String colid : sourcecolumns) {
                            boolean colexist;
                            if (colid.indexOf("__") == 0) continue;
                            boolean bl4 = colexist = targetColumnDs.findRow("columnid", colid) > -1;
                            if (colexist) continue;
                            dataset.renameColumn(colid, "__" + colid);
                            this.writeOutputLogger("Ignore missing column " + colid + " for table " + tablename, properties);
                        }
                    }
                    int count = 0;
                    if ("sdirole".equals(tablename)) {
                        safeSQL = new SafeSQL();
                        String string8 = "DELETE FROM sdirole WHERE " + (this.connectionInfo.getDbms().equals("ORA") ? " ( sdcid, keyid1 ) IN (SELECT sdcid, keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r \tWHERE sdirole.sdcid = r.sdcid AND sdirole.keyid1= r.keyid1 AND r.rsetid = " + safeSQL.addVar(rsetid) + ")");
                        count = this.database.executePreparedUpdate(string8, safeSQL.getValues());
                    } else if ("s_sdicertification".equals(tablename)) {
                        void var60_170;
                        String importedCertificationsLinkCols = "";
                        if (sdiSnapshotItem != null) {
                            SDISnapshot sDISnapshot = sdiSnapshotItem.getSnapshot();
                            CMTPolicy cmtPolicy = CMTPolicy.getPolicy(this.getRakFile(), this.getConnectionid(), sdiSnapshotItem.getSDCId(), sdiSnapshotItem.getPolicyNodeId(), sDISnapshot.getPolicyNodeMap());
                            importedCertificationsLinkCols = CMTPolicy.getCertificationOwnerLinkCol(cmtPolicy.getCertificationsOwner(), sDISnapshot.getSDCId());
                        }
                        if (importedCertificationsLinkCols == null || importedCertificationsLinkCols.length() == 0) {
                            throw new SapphireException("Certifications dataset ownership is not defined.");
                        }
                        String string9 = "";
                        if ("resourcesdc".equals(importedCertificationsLinkCols)) {
                            String string10 = "resource";
                        } else if ("certifiedforsdc".equals(importedCertificationsLinkCols)) {
                            String string11 = "certifiedfor";
                        } else {
                            throw new SapphireException("Invalid Certification Ownership type.");
                        }
                        SafeSQL safeSQL2 = new SafeSQL();
                        String sql2 = "DELETE FROM " + tablename + " WHERE " + (this.connectionInfo.getDbms().equals("ORA") ? "( " + (String)var60_170 + "sdcid, " + (String)var60_170 + "keyid1, " + (String)var60_170 + "keyid2, " + (String)var60_170 + "keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL2.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r \tWHERE " + tablename + "." + (String)var60_170 + "sdcid = r.sdcid AND " + tablename + "." + (String)var60_170 + "keyid1= r.keyid1 AND " + tablename + "." + (String)var60_170 + "keyid2 = r.keyid2 AND " + tablename + "." + (String)var60_170 + "keyid3 = r.keyid3 AND    r.rsetid = " + safeSQL2.addVar(rsetid) + ")");
                        count = this.database.executePreparedUpdate(sql2, safeSQL2.getValues());
                    } else if ("sdiattachment".equalsIgnoreCase(tablename)) {
                        safeSQL = new SafeSQL();
                        String string12 = "SELECT * FROM sdiattachment WHERE attachmentnum != 0 AND " + (this.connectionInfo.getDbms().equals("ORA") ? "( sdcid, keyid1, keyid2, keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r \tWHERE " + tablename + ".sdcid = r.sdcid AND " + tablename + ".keyid1= r.keyid1 AND " + tablename + ".keyid2 = r.keyid2 AND " + tablename + ".keyid3 = r.keyid3 AND    r.rsetid = " + safeSQL.addVar(rsetid) + ")");
                        DataSet currSDIAttRows = this.getQueryProcessor().getPreparedSqlDataSet(string12, safeSQL.getValues());
                        this.deleteSDIAttachments(currSDIAttRows, properties);
                    } else if ("categoryitem".equals(tablename)) {
                        safeSQL = new SafeSQL();
                        String string13 = "DELETE FROM " + tablename + " WHERE " + (this.connectionInfo.getDbms().equals("ORA") ? "( sdcid, keyid1 ) IN (SELECT sdcid, keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r \tWHERE " + tablename + ".sdcid = r.sdcid AND " + tablename + ".keyid1= r.keyid1 AND    r.rsetid = " + safeSQL.addVar(rsetid) + ")");
                        count = this.database.executePreparedUpdate(string13, safeSQL.getValues());
                    } else if ("sdcjobtypesecurity".equals(tablename)) {
                        if ("LV_JobType".equals(sdcid)) {
                            safeSQL = new SafeSQL();
                            String string14 = "DELETE FROM " + tablename + " WHERE jobtypeid IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")";
                            count = this.database.executePreparedUpdate(string14, safeSQL.getValues());
                        }
                    } else if (isSDIDetail) {
                        safeSQL = new SafeSQL();
                        if ("sdiattribute".equals(tablename)) {
                            String[] stringArray = new String[]{"sdidata", "sdiworkitem", "sdidataitem", "sdiattachment"};
                            String[] sdcids = new String[]{"DataSet", "SDIWorkItem", "DataItem", "SDIAttachment"};
                            for (int t = 0; t < stringArray.length; ++t) {
                                String delattrsql = "DELETE FROM sdiattribute WHERE sdcid=" + safeSQL.addVar(sdcids[t]) + " and keyid1 IN (SELECT " + stringArray[t] + "id FROM " + stringArray[t] + " WHERE " + (this.connectionInfo.getDbms().equals("ORA") ? "( sdcid, keyid1, keyid2, keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") )" : " EXISTS ( SELECT null FROM rsetitems r \tWHERE " + stringArray[t] + ".sdcid = r.sdcid AND " + stringArray[t] + ".keyid1= r.keyid1 AND " + stringArray[t] + ".keyid2 = r.keyid2 AND " + stringArray[t] + ".keyid3 = r.keyid3 AND    r.rsetid = " + safeSQL.addVar(rsetid) + ") )");
                                count = this.database.executePreparedUpdate(delattrsql, safeSQL.getValues());
                                safeSQL.reset();
                                if (count <= 0) continue;
                                this.writeOutputLogger("Flush sdiattribute for " + sdcids[t] + " for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ". Deleted Rows: " + count, properties);
                            }
                        }
                        String string15 = "DELETE FROM " + tablename + " WHERE " + (this.connectionInfo.getDbms().equals("ORA") ? "( sdcid, keyid1, keyid2, keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r \tWHERE " + tablename + ".sdcid = r.sdcid AND " + tablename + ".keyid1= r.keyid1 AND " + tablename + ".keyid2 = r.keyid2 AND " + tablename + ".keyid3 = r.keyid3 AND    r.rsetid = " + safeSQL.addVar(rsetid) + ")");
                        count = this.database.executePreparedUpdate(string15, safeSQL.getValues());
                    } else {
                        String sql3 = "DELETE FROM " + tablename + " WHERE " + keyclause;
                        count = this.database.executePreparedUpdate(sql3, safeSQLKeyClause.getValues());
                    }
                    if (count > 0) {
                        this.writeOutputLogger("Flush detail table " + tablename + " for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ". Deleted Rows: " + count, properties);
                    }
                    if (tablename.equals("sdiattribute")) continue;
                    if ("sdiattachment".equalsIgnoreCase(tablename)) {
                        this.addSDIAttachmentsFromSnapshot(dataset, sdiSnapshotItem, properties);
                        continue;
                    }
                    if (dataset == null || dataset.getRowCount() <= 0) continue;
                    this.writeOutputLogger("Insert into detail table " + tablename + " for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3, properties);
                    DataSetUtil.insert(this.database, dataset, tablename);
                    this.writeOutputLogger("Inserted Rows: " + dataset.getRowCount(), properties);
                }
                this.repopulateSDIAttributeKeyid1AndInsert(sdiData, rsetid, properties);
            }
        }
        catch (SapphireException se) {
            if (isImport) {
                this.writeOutputLogger("ERROR:" + se.getMessage(), properties);
            }
            throw new SapphireException("DB_UPDATE_FAILED", "Failed to update records: " + ErrorUtil.extractMessage(se.getMessage(), ErrorUtil.isUserAdmin(this.getConnectionId())), se);
        }
        if (isTriggerSDCRule && !properties.getProperty("__bypasseventplan", "N").equalsIgnoreCase("Y")) {
            EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), postEdit);
        }
        new SchedulerUtil(this.getConnectionId()).performSchedulerUpdates(sdcid, primaryData, beforeEditImage.getDataset("primary"), properties);
        if (isTriggerSDCRule || isImport) {
            BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, sdcProps, "PostEdit");
            if (sdcPostRules.requiresBeforeEditImage() || sdcPostRules.customRulesRequiresBeforeEditImage()) {
                sdcPostRules.setBeforeEditImage(beforeEditImage);
            }
            if (isImport) {
                sdcPostRules.setCMTImport(isImport);
                sdcPostRules.postCMTImport(sdiData, properties, false);
                for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                    customRules.setCMTImport(isImport);
                    customRules.postCMTImport(sdiData, properties, false);
                }
            }
            if (isTriggerSDCRule) {
                Trace.startBusinessRule(sdcid + "." + "PostEdit", true);
                sdcPostRules.postEdit(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PostEdit", true);
                Trace.startBusinessRule(sdcid + "." + "PostEdit", false);
                for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                    customRules.postEdit(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PostEdit", false);
                sdcPostRules.endRule();
            } else {
                this.writeOutputLogger("Not trigger preEdit business rule based on transfer option", properties);
            }
        }
        if ("Y".equals(properties.getProperty("checkinsdiflag"))) {
            PropertyList props = new PropertyList();
            props.setProperty("rsetid", rsetid);
            this.getActionProcessor().processActionClass(CheckInSDI.class.getName(), props);
        }
        if (rsetid != null && rsetid.length() > 0) {
            dam.clearRSet(rsetid);
        }
        if (properties.getProperty("index", "Y").equals("Y")) {
            Indexer.indexSDI(this.connectionInfo, sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
        }
    }

    protected void deleteSDIAttachments(DataSet sdiAttachments, PropertyList actionProps) {
        if (sdiAttachments == null) {
            return;
        }
        this.writeOutputLogger("Delete existing attachment.", actionProps);
        AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getRakFile(), this.getConnectionid());
        for (int i = 0; i < sdiAttachments.getRowCount(); ++i) {
            Attachment attachment = Attachment.getAttachment(sdiAttachments, i, this.getConnectionId());
            attachmentProcessor.deleteSDIAttachment(attachment);
        }
    }

    protected void addSDIAttachmentsFromSnapshot(DataSet attachments, SDISnapshotItem sdiSnapshotItem, PropertyList actionProps) throws SapphireException {
        if (attachments != null) {
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getRakFile(), this.getConnectionid());
            for (int i = 0; i < attachments.getRowCount(); ++i) {
                int attNum = attachments.getInt(i, "attachmentnum");
                Attachment attachment = Attachment.getAttachment(attachments, i, this.getConnectionId());
                this.writeOutputLogger("Processing Attachment Num#" + attNum + ". Type: " + attachment.getType() + ", Size: " + attachments.getValue(i, "attachmentsize", "Unknown"), actionProps);
                InputStream attInStream = null;
                if (attachment != null && !CMTUtil.EXCLUDED_ATTACHMENT_TYPES.contains(attachment.getType())) {
                    attInStream = sdiSnapshotItem.getAttachmentAsStream(attNum);
                }
                if (attInStream != null) {
                    attachment.setInputStream(attInStream);
                }
                attachmentProcessor.addSDIAttachment(attachment, false, false, null);
            }
        }
    }

    protected void setSDIValue(String actionid, String columnid, String value, PropertyList properties) throws SapphireException {
        if (columnid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No columnid specified.");
        }
        if (value.length() > 0) {
            PropertyList newproperties = new PropertyList();
            newproperties.setProperty("sdcid", properties.getProperty("sdcid"));
            newproperties.setProperty("keyid1", properties.getProperty("keyid1"));
            newproperties.setProperty("keyid2", properties.getProperty("keyid2"));
            newproperties.setProperty("keyid3", properties.getProperty("keyid3"));
            newproperties.setProperty("applylock", properties.getProperty("applylock"));
            String[] columnids = StringUtil.split(columnid, ";");
            String[] values = StringUtil.split(value, ";");
            if (columnids.length == values.length) {
                for (int i = 0; i < columnids.length; ++i) {
                    newproperties.setProperty(columnids[i], values[i]);
                }
            } else {
                throw new SapphireException("INVALID_PROPERTY", "Columnids and values do not match.");
            }
            newproperties.setProperty("auditreason", properties.getProperty("auditreason"));
            newproperties.setProperty("auditactivity", properties.getProperty("auditactivity", ""));
            newproperties.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", "N"));
            newproperties.setProperty("auditdt", properties.getProperty("auditdt"));
            this.editSDI(actionid, newproperties);
        }
    }

    public static boolean isValidKey(String keyid) {
        return keyid.indexOf(39) < 0 && keyid.indexOf(34) < 0 && keyid.indexOf(123) < 0 && keyid.indexOf(125) < 0 && keyid.indexOf(91) < 0 && keyid.indexOf(93) < 0 && keyid.indexOf(124) < 0 && keyid.indexOf(59) < 0 && keyid.indexOf(92) < 0 && keyid.indexOf(38) < 0 && !keyid.equalsIgnoreCase("all") && !keyid.equalsIgnoreCase("current") && !keyid.equalsIgnoreCase("date") && !keyid.equalsIgnoreCase("default") && !keyid.equalsIgnoreCase("developer") && !keyid.equalsIgnoreCase("first") && !keyid.equalsIgnoreCase("last") && !keyid.equalsIgnoreCase("none") && !keyid.equalsIgnoreCase("null") && !keyid.equalsIgnoreCase("operator") && !keyid.equalsIgnoreCase("system") && !keyid.equalsIgnoreCase("startup") && !keyid.equalsIgnoreCase("this");
    }

    protected void repopulateSDIAttributeKeyid1AndInsert(SDIData sdiData, String rsetid, PropertyList properties) throws SapphireException {
        DataSet attrforDataSet = null;
        SafeSQL safeSQL = new SafeSQL();
        String sqlWhere = " WHERE " + (this.connectionInfo.getDbms().equals("ORA") ? "( sdcid, keyid1, keyid2, keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : " EXISTS ( SELECT null FROM rsetitems r \tWHERE [tableid].sdcid = r.sdcid AND [tableid].keyid1= r.keyid1 AND [tableid].keyid2 = r.keyid2 AND [tableid].keyid3 = r.keyid3 AND    r.rsetid = " + safeSQL.addVar(rsetid) + ")");
        HashMap<String, String> oldnewMap = new HashMap<String, String>();
        String[] datasetnames = new String[]{"attribute", "datasetattribute", "dataitemattribute", "sdiworkitemattribute", "attachmentattribute"};
        String[] parentdatasetnames = new String[]{"", "dataset", "dataitem", "sdiworkitem", "attachment"};
        String[] idcolumns = new String[]{"", "sdidataid", "sdidataitemid", "sdiworkitemid", "sdiattachmentid"};
        String[] detailsdcids = new String[]{"", "DataSet", "DataItem", "SDIWorkItem", "SDIAttachment"};
        String[] detailtables = new String[]{"", "sdidata", "sdidataitem", "sdiworkitem", "sdiattachment"};
        String[] sqls = new String[]{"", "SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, sdidataid FROM sdidata ", "SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, sdidataitemid FROM sdidataitem ", "SELECT sdcid, keyid1, keyid2, keyid3, workitemid, workitemversionid, workiteminstance, sdiworkitemid FROM sdiworkitem ", "SELECT sdcid, keyid1, keyid2, keyid3, attachmentid, attachmentnum, sdiattachmentid FROM sdiattachment "};
        for (int d = 0; d < datasetnames.length; ++d) {
            int i;
            DataSet sdiattributeDs = sdiData.getDataset(datasetnames[d]);
            if (sdiattributeDs == null || sdiattributeDs.getRowCount() <= 0) continue;
            if (!datasetnames[d].equalsIgnoreCase("attribute")) {
                attrforDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sqls[d] + StringUtil.replaceAll(sqlWhere, "[tableid]", detailtables[d]), safeSQL.getValues());
                for (i = 0; i < attrforDataSet.getRowCount(); ++i) {
                    int foundrow;
                    HashMap rowMap = (HashMap)attrforDataSet.get(i);
                    String newkeyid1 = attrforDataSet.getValue(i, idcolumns[d]);
                    rowMap.remove(idcolumns[d]);
                    DataSet parentDataSet = sdiData.getDataset(parentdatasetnames[d]);
                    String oldkeyid1 = "";
                    if (parentdatasetnames[d].equalsIgnoreCase("sdiworkitem") && rowMap.get("workitemversionid") == null) {
                        parentDataSet.setValue(i, "workitemversionid", (String)rowMap.get("workitemversionid"));
                    }
                    if ((oldkeyid1 = parentDataSet.getValue(foundrow = parentDataSet.findRow(rowMap), "__old" + idcolumns[d])).length() == 0) {
                        oldkeyid1 = parentDataSet.getValue(foundrow, idcolumns[d]);
                    }
                    oldnewMap.put(detailsdcids[d] + ";" + oldkeyid1, newkeyid1);
                }
            }
            for (i = 0; i < sdiattributeDs.getRowCount(); ++i) {
                String newkeyid1 = (String)oldnewMap.get(sdiattributeDs.getValue(i, "sdcid") + ";" + sdiattributeDs.getValue(i, "keyid1"));
                if (newkeyid1 == null) continue;
                sdiattributeDs.setValue(i, "keyid1", newkeyid1);
            }
            this.writeOutputLogger("Insert into detail table sdiattribute for " + datasetnames[d], properties);
            DataSetUtil.insert(this.database, sdiattributeDs, "sdiattribute");
            this.writeOutputLogger("Inserted Rows: " + sdiattributeDs.getRowCount(), properties);
        }
    }

    protected void writeOutputLogger(String message, HashMap actionProps) {
        this.logger.info(message);
        if (actionProps.get("outputLogger") != null) {
            ((StringLogger)actionProps.get("outputLogger")).log(message);
        }
    }
}


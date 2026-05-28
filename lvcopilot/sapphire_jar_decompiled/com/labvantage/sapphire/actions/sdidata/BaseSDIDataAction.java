/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.opal.validation.data.ValidateDataEntered;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import com.labvantage.sapphire.actions.wap.RemoveActivityWorkSDI;
import com.labvantage.sapphire.actions.wap.SetActivityStatus;
import com.labvantage.sapphire.actions.wap.UpdateActivityCompleteCount;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostDataReleaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostEditDataSetEventObject;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.WorkItemItemRuleEvaluator;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.action.BaseSpecRule;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BaseSDIDataAction
extends BaseAction {
    private boolean runOld;
    protected static final String COLUMN_ASSIGNEDANALYST = "s_assignedanalyst";
    protected static final String COLUMN_ASSIGNEDDEPT = "s_assigneddepartment";
    protected static final String COLUMN_CREATEWORKSHEETRULE = "createworksheetrule";
    protected static final String COLUMN_SECURITYUSER = "securityuser";
    protected static final String COLUMN_SECURITYDEPARTMENT = "securitydepartment";
    protected static final String COLUMN_FORMRULE = "formrule";
    protected static final String WS_ONCREATION = "On Creation";
    protected static final String WS_ONASSIGNMENT = "On Assignment";

    protected void editSDIData(PropertyList properties, String tableid) throws SapphireException {
        String preEventType;
        String sdcid = properties.getProperty("sdcid");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcProps = sdcProcessor.getPropertyList(sdcid);
        sdcid = sdcProps.getProperty("sdcid");
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        String editIncompleteDSOnly = properties.getProperty("editincompleteonly", "N");
        boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        boolean sdidata = tableid.equals("sdidata");
        boolean sdidataitem = tableid.equals("sdidataitem");
        boolean sdidataapproval = tableid.equals("sdidataapproval");
        BaseSDCRules sdcPreReleaseRules = null;
        boolean isDataRelease = sdidataitem && properties.getProperty("releasedflag").length() > 0 && this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("releaseeventsonedit").equals("Y");
        boolean requiresDataReleasePrimary = false;
        boolean requiresBeforeDataReleaseImage = false;
        boolean sdiDataApprovalRollback = sdidataitem;
        if (sdiDataApprovalRollback) {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
            sdiDataApprovalRollback = "Y".equals(policy.getProperty("resetapproval", "N"));
        }
        if (isDataRelease) {
            sdcPreReleaseRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PreDataRelease");
            requiresDataReleasePrimary = sdcPreReleaseRules.requiresDataReleasePrimary() || sdcPreReleaseRules.customRulesRequiresDataReleasePrimary();
            boolean bl = requiresBeforeDataReleaseImage = sdcPreReleaseRules.requiresBeforeDataReleaseImage() || sdcPreReleaseRules.customRulesRequiresBeforeDataReleaseImage();
        }
        String string = sdidata ? "PreEditDataSet" : (preEventType = sdidataitem ? "PreEditDataItem" : "PreEditDataApproval");
        String postEventType = sdidata ? "PostEditDataSet" : (sdidataitem ? "PostEditDataItem" : "PostEditDataApproval");
        BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, preEventType);
        String datasetType = "";
        if (sdidata) {
            datasetType = "dataset";
        }
        if (sdidataitem) {
            datasetType = "dataitem";
        }
        if (sdidataapproval) {
            datasetType = "dataapproval";
        }
        SDIData sdiData = new SDIData();
        SDIData beforeEditImage = null;
        boolean requiresEditSDIDataPrimary = sdcPreRules.requiresEditSDIDataPrimary() || sdcPreRules.customRulesRequiresEditSDIDataPrimary() || requiresDataReleasePrimary;
        boolean requiresBeforeEditSDIDataImage = sdcPreRules.requiresBeforeEditSDIDataImage() || sdcPreRules.customRulesRequiresBeforeEditSDIDataImage() || requiresBeforeDataReleaseImage;
        PostEditDataSetEventObject postEditDataSetEventObject = null;
        PostDataReleaseEventObject postDataReleaseEventObject = null;
        boolean requiresSupplementalData = false;
        if (sdidata) {
            postEditDataSetEventObject = new PostEditDataSetEventObject(sdcid, sdcProps, sdiData, properties);
            requiresSupplementalData = EventManager.requiresSupplementalData(sapphireConnection, this.getErrorHandler(), postEditDataSetEventObject);
        } else if (sdidataitem && isDataRelease) {
            postDataReleaseEventObject = new PostDataReleaseEventObject(sdcid, sdcProps, sdiData, properties, properties.getProperty("releasedflag").contains("Y") ? "Y" : "N", false, false);
            requiresSupplementalData = EventManager.requiresSupplementalData(sapphireConnection, this.getErrorHandler(), postDataReleaseEventObject);
        }
        boolean applylock = properties.getProperty("applylock").equals("Y");
        DAMProcessor dam = this.getDAMProcessor();
        String inputKeyid1 = properties.getProperty("keyid1");
        String inputKeyid2 = properties.getProperty("keyid2");
        String inputKeyid3 = properties.getProperty("keyid3");
        String inputParamlistid = properties.getProperty("paramlistid");
        String inputVersion = properties.getProperty("paramlistversionid");
        String inputVariant = properties.getProperty("variantid");
        String inputDataset = properties.getProperty("dataset");
        boolean skipAction = false;
        if (inputParamlistid.length() == 0 || inputVersion.length() == 0 || inputVariant.length() == 0 || inputDataset.length() == 0) {
            SDIRequest request = new SDIRequest();
            request.setSDCid(sdcid);
            request.setKeyid1List(inputKeyid1);
            request.setKeyid2List(inputKeyid2);
            request.setKeyid3List(inputKeyid3);
            request.setRequestItem("dataset");
            DataSet allDataSets = this.getSDIProcessor().getSDIData(request).getDataset("dataset");
            inputKeyid1 = allDataSets.getColumnValues("keyid1", ";");
            inputKeyid2 = allDataSets.getColumnValues("keyid2", ";");
            inputKeyid3 = allDataSets.getColumnValues("keyid3", ";");
            inputParamlistid = allDataSets.getColumnValues("paramlistid", ";");
            inputVersion = allDataSets.getColumnValues("paramlistversionid", ";");
            inputVariant = allDataSets.getColumnValues("variantid", ";");
            inputDataset = allDataSets.getColumnValues("dataset", ";");
            if (allDataSets.size() == 0) {
                skipAction = true;
            }
        }
        String rsetid = "";
        if (!skipAction) {
            rsetid = applylock ? dam.createLockedRSetDS(sdcid, inputKeyid1, inputKeyid2, inputKeyid3, inputParamlistid, inputVersion, inputVariant, inputDataset, propsmatch) : dam.createRSetDS(sdcid, inputKeyid1, inputKeyid2, inputKeyid3, inputParamlistid, inputVersion, inputVariant, inputDataset, propsmatch, requiresEditSDIDataPrimary || requiresBeforeEditSDIDataImage || requiresSupplementalData, false);
        }
        if (rsetid.length() > 0) {
            DataSet tabledef = new DataSet();
            StringBuffer selecttabledef = new StringBuffer("SELECT columnid, datatype FROM syscolumn WHERE\ttableid = ? AND \t\tpkflag='N' AND \t\tcolumnid not in \t\t( ");
            if (sdidataitem) {
                selecttabledef.append("'enteredvalue', 'enteredtext', 'enteredunits', 'transformvalue', 'transformdt', 'transformtext', 'displayvalue','enteredqualifier', 'enteredoperator', 'valuestatus', 'textcolor',");
            }
            selecttabledef.append(DataSetUtil.getStandardSysColsClause()).append(")");
            this.database.createPreparedResultSet(selecttabledef.toString(), new Object[]{tableid});
            tabledef.setResultSet(this.database.getResultSet());
            String[] keyid1prop = StringUtil.split(inputKeyid1, ";");
            String[] keyid2prop = StringUtil.split(inputKeyid2, ";");
            String[] keyid3prop = StringUtil.split(inputKeyid3, ";");
            String[] paramlistidprop = StringUtil.split(inputParamlistid, ";");
            String[] paramlistversionidprop = StringUtil.split(inputVersion, ";");
            String[] variantidprop = StringUtil.split(inputVariant, ";");
            String[] datasetprop = StringUtil.split(inputDataset, ";");
            String[] paramidprop = StringUtil.split(properties.getProperty("paramid"), ";");
            String[] paramtypeprop = StringUtil.split(properties.getProperty("paramtype"), ";");
            String[] replicateidprop = StringUtil.split(properties.getProperty("replicateid"), ";");
            String[] approvalstepprop = StringUtil.split(properties.getProperty("approvalstep"), ";");
            String[][] colvalprop = new String[tabledef.getRowCount()][];
            String formId = properties.getProperty("formid");
            String formVersionId = properties.getProperty("formversionid");
            boolean createWorkSheet = "Y".equals(StringUtil.getYN(properties.getProperty("createworksheet"), "Y"));
            DataSet paramListForms = null;
            DataSet dataForms = new DataSet();
            DataSet sdiDataSets = null;
            if (sdidata && properties.getProperty("assignto").length() > 0) {
                properties.setProperty(COLUMN_ASSIGNEDANALYST, properties.getProperty("assignto"));
            }
            DataSet sdidatatable = new DataSet(this.connectionInfo);
            block8: for (int col = 0; col < tabledef.getRowCount(); ++col) {
                String columnid = tabledef.getString(col, "columnid");
                String value = properties.getProperty(columnid);
                if (!properties.containsKey(columnid)) continue;
                colvalprop[col] = StringUtil.split(value, ";");
                switch (tabledef.getString(col, "datatype").charAt(0)) {
                    case 'C': {
                        sdidatatable.addColumn(columnid, 0);
                        continue block8;
                    }
                    case 'N': {
                        sdidatatable.addColumn(columnid, 1);
                        continue block8;
                    }
                    case 'R': {
                        sdidatatable.addColumn(columnid, 1);
                        continue block8;
                    }
                    case 'D': {
                        sdidatatable.addColumn(columnid, 2);
                        if (!"Y".equals(this.getSDCProcessor().getSDCColumnProperty("DataSet", columnid, "timezoneindependent"))) continue block8;
                        sdidatatable.setTimeZoneInsensitive(columnid);
                    }
                }
            }
            StringBuffer worksheetPresent = new StringBuffer();
            for (int i = 0; i < keyid1prop.length; ++i) {
                String keyid1 = keyid1prop[i];
                String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                for (int pl = 0; pl < paramlistidprop.length; ++pl) {
                    String approvalstep;
                    if (propsmatch) {
                        pl = i;
                    }
                    String paramlistid = paramlistidprop[pl];
                    String paramlistversionid = paramlistversionidprop.length == 0 || paramlistversionidprop.length < paramlistidprop.length || paramlistversionidprop[pl].length() == 0 ? "" : paramlistversionidprop[pl];
                    String variantid = variantidprop.length == 0 || variantidprop.length < paramlistidprop.length || variantidprop[pl].length() == 0 ? "" : variantidprop[pl];
                    String datasetstr = datasetprop.length == 0 || datasetprop.length < paramlistidprop.length || datasetprop[pl].length() == 0 ? "" : datasetprop[pl];
                    String paramid = paramidprop.length == 0 || paramidprop.length < paramlistidprop.length || paramidprop[pl].length() == 0 ? "" : paramidprop[pl];
                    String paramtype = paramtypeprop.length == 0 || paramtypeprop.length < paramlistidprop.length || paramtypeprop[pl].length() == 0 ? "" : paramtypeprop[pl];
                    String replicateid = replicateidprop.length == 0 || replicateidprop.length < paramlistidprop.length || replicateidprop[pl].length() == 0 ? "" : replicateidprop[pl];
                    String string2 = approvalstep = approvalstepprop.length == 0 || approvalstepprop.length < paramlistidprop.length || approvalstepprop[pl].length() == 0 ? "" : approvalstepprop[pl];
                    if (sdidata) {
                        if (paramlistversionid.length() == 0 || variantid.length() == 0 || datasetstr.length() == 0) {
                            throw new SapphireException("INVALID_PROPERTIES", "Property values for sdidata do not match");
                        }
                        if (sdiDataSets == null) {
                            sdiDataSets = this.loadSDIDataSetsFromRsetItemDS(rsetid);
                        }
                    }
                    if (sdidataapproval && (paramlistversionid.length() == 0 || variantid.length() == 0 || datasetstr.length() == 0 || approvalstep.length() == 0)) {
                        throw new SapphireException("INVALID_PROPERTIES", "Property values for sdidataapproval do not match");
                    }
                    if (sdidataitem && (paramlistversionid.length() == 0 || variantid.length() == 0 || datasetstr.length() == 0 || paramid.length() == 0 || paramtype.length() == 0 || replicateid.length() == 0)) {
                        throw new SapphireException("INVALID_PROPERTIES", "Property values for sdidataitem do not match");
                    }
                    String[] datasetStatusArr = StringUtil.split(properties.getProperty("s_datasetstatus"), ";");
                    boolean skipSdiData = false;
                    if (editIncompleteDSOnly.equalsIgnoreCase("Y") && (datasetStatusArr.length > pl ? datasetStatusArr[pl] : datasetStatusArr[datasetStatusArr.length - 1]).equals("Cancelled")) {
                        HashMap<String, Object> findMap = new HashMap<String, Object>();
                        findMap.put("sdcid", sdcid);
                        findMap.put("keyid1", keyid1);
                        findMap.put("keyid2", keyid2);
                        findMap.put("keyid3", keyid3);
                        findMap.put("paramlistid", paramlistid);
                        findMap.put("paramlistversionid", paramlistversionid);
                        findMap.put("variantid", variantid);
                        findMap.put("dataset", new BigDecimal(datasetstr));
                        int dsIndex = sdiDataSets.findRow(findMap);
                        if (sdiDataSets.getValue(dsIndex, "s_datasetstatus").equals("Completed")) {
                            skipSdiData = true;
                        }
                    }
                    if (!skipSdiData) {
                        int newrow = sdidatatable.addRow();
                        sdidatatable.setString(newrow, "sdcid", sdcid);
                        sdidatatable.setString(newrow, "keyid1", keyid1);
                        sdidatatable.setString(newrow, "keyid2", keyid2);
                        sdidatatable.setString(newrow, "keyid3", keyid3);
                        sdidatatable.setString(newrow, "paramlistid", paramlistid);
                        sdidatatable.setString(newrow, "paramlistversionid", paramlistversionid);
                        sdidatatable.setString(newrow, "variantid", variantid);
                        sdidatatable.setNumber(newrow, "dataset", (int)Double.parseDouble(datasetstr));
                        if (sdidataapproval) {
                            sdidatatable.setString(newrow, "approvalstep", approvalstep);
                        }
                        if (sdidataitem) {
                            sdidatatable.setString(newrow, "paramid", paramid);
                            sdidatatable.setString(newrow, "paramtype", paramtype);
                            sdidatatable.setNumber(newrow, "replicateid", Integer.parseInt(replicateid));
                        }
                        sdidatatable.setString(newrow, "modby", this.connectionInfo.getSysuserId());
                        sdidatatable.setString(newrow, "modtool", this.connectionInfo.getTool());
                        for (int col = 0; col < tabledef.getRowCount(); ++col) {
                            String colval = "";
                            String columnid = tabledef.getString(col, "columnid");
                            if (colvalprop[col] == null || colvalprop[col].length <= 0) continue;
                            String string3 = colval = colvalprop[col].length > pl ? colvalprop[col][pl] : colvalprop[col][colvalprop[col].length - 1];
                            if (colval.equals("(null)") || colval.equals("(none)")) {
                                colval = "";
                            }
                            sdidatatable.setValue(newrow, columnid, colval);
                        }
                        if (sdidata && createWorkSheet) {
                            if (paramListForms == null) {
                                paramListForms = BaseSDIDataAction.getParamListForms(properties.getProperty("paramlistid"), properties.getProperty("paramlistversionid"), properties.getProperty("variantid"), this.getQueryProcessor(), this.getDAMProcessor(), this.database, this.connectionInfo, this.logger);
                            }
                            this.prepareDataFormRow(sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, datasetstr, formId, formVersionId, paramListForms, dataForms, properties, worksheetPresent, sdiDataSets, sdidatatable, newrow);
                        }
                    }
                    if (!propsmatch) continue;
                    pl = paramlistidprop.length;
                }
            }
            if (worksheetPresent.length() > 0) {
                this.setInfoError(this.getTranslationProcessor().translate("Worksheet already bound to the following datasets: -") + worksheetPresent.substring(1) + "<br>" + this.getTranslationProcessor().translate("Continuing with assignment."));
            }
            if (properties.getProperty("tracelogid", "").trim().length() == 0) {
                String traceLogId = this.getTracelogid(sdcid, "Edited data in " + tableid, properties.getProperty("auditreason"), properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"));
                sdidatatable.setString(-1, "tracelogid", traceLogId);
                properties.setProperty("tracelogid", traceLogId);
            } else {
                sdidatatable.setString(-1, "tracelogid", properties.getProperty("tracelogid", "").trim());
            }
            sdiData.setDataset(datasetType, sdidatatable);
            boolean bl = requiresBeforeEditSDIDataImage = requiresBeforeEditSDIDataImage || properties.containsKey("s_datasetstatus");
            if (requiresEditSDIDataPrimary || requiresBeforeEditSDIDataImage || requiresSupplementalData || sdiDataApprovalRollback) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setRsetid(rsetid);
                sdiRequest.setRetainRsetid(true);
                if (requiresEditSDIDataPrimary || requiresSupplementalData) {
                    sdiRequest.setRequestItem("primary");
                }
                if (requiresBeforeEditSDIDataImage || requiresSupplementalData || sdiDataApprovalRollback) {
                    sdiRequest.setRequestItem(datasetType);
                    sdiRequest.setRequestItem("dataapproval");
                }
                if (sdiDataApprovalRollback) {
                    sdiRequest.setRequestItem("dataapproval");
                }
                if (requiresSupplementalData) {
                    if (sdidata) {
                        postEditDataSetEventObject.addRequestItems(sdiRequest);
                    } else if (sdidataitem && isDataRelease) {
                        postDataReleaseEventObject.addRequestItems(sdiRequest);
                    }
                }
                BaseSDCRules[] sdiProcessor = this.getSDIProcessor();
                beforeEditImage = sdiProcessor.getSDIData(sdiRequest);
                sdcPreRules.setBeforeEditImage(beforeEditImage);
                sdiData.setDataset("primary", beforeEditImage.getDataset("primary"));
                sdiData.setDataset("dataapproval", beforeEditImage.getDataset("dataapproval"));
                if (sdidata) {
                    postEditDataSetEventObject.setSupplementalData(beforeEditImage);
                    postEditDataSetEventObject.setRsetid(rsetid);
                } else if (sdidataitem && isDataRelease) {
                    postDataReleaseEventObject.getSDIData().setDataset("sdispec", beforeEditImage.getDataset("sdispec"));
                    postDataReleaseEventObject.getSDIData().setDataset("dataspec", beforeEditImage.getDataset("dataspec"));
                    DataSet beforeDataitem = beforeEditImage.getDataset("dataitem");
                    beforeDataitem.sort("sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid");
                    DataSet eventDataitem = postDataReleaseEventObject.getSDIData().getDataset("dataitem");
                    eventDataitem.sort("sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid");
                    String[] columns = beforeDataitem.getColumns();
                    ArrayList<String> missingColumns = new ArrayList<String>();
                    for (int i = 0; i < columns.length; ++i) {
                        if (eventDataitem.getColumnType(columns[i]) != -1) continue;
                        missingColumns.add(columns[i]);
                        eventDataitem.addColumn(columns[i], beforeDataitem.getColumnType(columns[i]));
                    }
                    if (missingColumns.size() > 0) {
                        ArrayList<DataSet> groupedEventDataItemsList = eventDataitem.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3");
                        for (DataSet groupedEventDataItem : groupedEventDataItemsList) {
                            HashMap<String, String> filterBySDI = new HashMap<String, String>();
                            filterBySDI.put("sdcid", groupedEventDataItem.getValue(0, "sdcid"));
                            filterBySDI.put("keyid1", groupedEventDataItem.getValue(0, "keyid1"));
                            filterBySDI.put("keyid2", groupedEventDataItem.getValue(0, "keyid2"));
                            filterBySDI.put("keyid3", groupedEventDataItem.getValue(0, "keyid3"));
                            DataSet filteredBeforeDataitems = beforeDataitem.getFilteredDataSet(filterBySDI);
                            if (filteredBeforeDataitems.size() <= 0) continue;
                            for (int i = 0; i < groupedEventDataItem.size(); ++i) {
                                HashMap<String, Object> findDataItem = new HashMap<String, Object>();
                                findDataItem.put("paramlistid", groupedEventDataItem.getValue(i, "paramlistid"));
                                findDataItem.put("paramlistversionid", groupedEventDataItem.getValue(i, "paramlistversionid"));
                                findDataItem.put("variantid", groupedEventDataItem.getValue(i, "variantid"));
                                findDataItem.put("dataset", groupedEventDataItem.getBigDecimal(i, "dataset"));
                                findDataItem.put("paramid", groupedEventDataItem.getValue(i, "paramid"));
                                findDataItem.put("paramtype", groupedEventDataItem.getValue(i, "paramtype"));
                                findDataItem.put("replicateid", groupedEventDataItem.getBigDecimal(i, "replicateid"));
                                int findRow = filteredBeforeDataitems.findRow(findDataItem);
                                if (findRow <= -1) continue;
                                for (String columnid : missingColumns) {
                                    groupedEventDataItem.setObject(i, columnid, filteredBeforeDataitems.getObject(findRow, columnid));
                                }
                            }
                        }
                    }
                }
            }
            if (sdidataitem) {
                Trace.startBusinessRule(sdcid + "." + "PreEditDataItem", true);
                sdcPreRules.preEditSDIDataItem(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PreEditDataItem", true);
                Trace.startBusinessRule(sdcid + "." + "PreEditDataItem", false);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.preEditSDIDataItem(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PreEditDataItem", false);
                if (isDataRelease) {
                    Trace.startBusinessRule(sdcid + "." + "PreDataRelease", true);
                    sdcPreReleaseRules.preReleaseData(sdidatatable, properties);
                    Trace.endBusinessRule(sdcid + "." + "PreDataRelease", true);
                    Trace.startBusinessRule(sdcid + "." + "PreDataRelease", false);
                    for (BaseSDCRules customRules : sdcPreReleaseRules.getCustomRuleList()) {
                        customRules.preReleaseData(sdidatatable, properties);
                    }
                    Trace.endBusinessRule(sdcid + "." + "PreDataRelease", false);
                    sdcPreRules.endRule();
                }
            } else if (sdidata) {
                DataSet sdiDataSet = sdiData.getDataset("dataset");
                if (sdiDataSet.isValidColumn("s_datasetstatus")) {
                    this.setWapStatusToCancel(sdiDataSet, sdcPreRules);
                    this.setWapStatusToPendingOnUncancel(sdiDataSet, sdcPreRules);
                    this.setCompleteDtCompleteByNull(sdiDataSet, sdcPreRules);
                }
                Trace.startBusinessRule(sdcid + "." + "PreEditDataSet", true);
                sdcPreRules.preEditSDIData(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PreEditDataSet", true);
                Trace.startBusinessRule(sdcid + "." + "PreEditDataSet", false);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.preEditSDIData(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PreEditDataSet", false);
            } else if (sdidataapproval) {
                Trace.startBusinessRule(sdcid + "." + "PreEditDataApproval", true);
                sdcPreRules.preEditSDIDataApproval(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PreEditDataApproval", true);
                Trace.startBusinessRule(sdcid + "." + "PreEditDataApproval", false);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.preEditSDIDataApproval(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PreEditDataApproval", false);
            }
            sdcPreRules.endRule();
            DataSetUtil.update(this.database, sdidatatable, tableid, new SDIData().getKeys(datasetType));
            if (sdiDataApprovalRollback) {
                BaseSDIDataAction.dataApprovalRollback(beforeEditImage.getDataset(datasetType), beforeEditImage.getDataset("dataapproval"), sdidatatable, this.getActionProcessor());
            }
            if (sdidataapproval) {
                DataSet validateforcePeerDataset;
                if (sdidataapproval && (validateforcePeerDataset = this.getQueryProcessor().getPreparedSqlDataSet("select da.approvalstep from sdidataitem di, sdidataapproval da, rsetitemsds rs where di.sdcid=da.sdcid and di.keyid1=da.keyid1 and di.keyid2=da.keyid2 and di.keyid3=da.keyid3 and di.paramlistid=da.paramlistid and di.paramlistversionid=da.paramlistversionid and di.variantid=da.variantid and di.dataset=da.dataset and di.sdcid=rs.sdcid and di.keyid1=rs.keyid1 and di.keyid2=rs.keyid2 and di.keyid3=rs.keyid3 and di.paramlistid=rs.paramlistid and di.paramlistversionid=rs.paramlistversionid and di.variantid=rs.variantid and di.dataset=rs.dataset and da.forcepeerflag='Y' and da.approvalflag in ( 'P', 'F' ) and rs.rsetid=? and di.s_analystid=? ", new Object[]{rsetid, this.connectionInfo.getSysuserId()})).getRowCount() > 0) {
                    String errormsg = this.getTranslationProcessor().translate("The approval step requires peer approval:") + validateforcePeerDataset.getValue(0, "approvalstep");
                    throw new SapphireException(errormsg);
                }
                sdidatatable.sort("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset");
                ArrayList<DataSet> dsGroups = sdidatatable.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset");
                StringBuffer keyid1 = new StringBuffer();
                StringBuffer keyid2 = new StringBuffer();
                StringBuffer keyid3 = new StringBuffer();
                StringBuffer paramlistid = new StringBuffer();
                StringBuffer paramlistversionid = new StringBuffer();
                StringBuffer variantid = new StringBuffer();
                StringBuffer dataset = new StringBuffer();
                StringBuffer approvalflag = new StringBuffer();
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", sdcid);
                for (int ds = 0; ds < dsGroups.size(); ++ds) {
                    DataSet currentds = dsGroups.get(ds);
                    props.setProperty("keyid1", currentds.getValue(0, "keyid1"));
                    props.setProperty("keyid2", currentds.getValue(0, "keyid2"));
                    props.setProperty("keyid3", currentds.getValue(0, "keyid3"));
                    props.setProperty("paramlistid", currentds.getValue(0, "paramlistid"));
                    props.setProperty("paramlistversionid", currentds.getValue(0, "paramlistversionid"));
                    props.setProperty("variantid", currentds.getValue(0, "variantid"));
                    props.setProperty("dataset", currentds.getValue(0, "dataset"));
                    props.setProperty("tracelogid", properties.getProperty("tracelogid"));
                    keyid1.append(";" + currentds.getValue(0, "keyid1"));
                    keyid2.append(";" + currentds.getValue(0, "keyid2"));
                    keyid3.append(";" + currentds.getValue(0, "keyid3"));
                    paramlistid.append(";" + currentds.getValue(0, "paramlistid"));
                    paramlistversionid.append(";" + currentds.getValue(0, "paramlistversionid"));
                    variantid.append(";" + currentds.getValue(0, "variantid"));
                    dataset.append(";" + currentds.getValue(0, "dataset"));
                    this.getActionProcessor().processAction("GetApprovalFlag", "1", props);
                    String flag = props.getProperty("approvalflag");
                    flag = "Pass".equals(flag) ? "P" : ("Fail".equals(flag) ? "F" : "U");
                    approvalflag.append(";" + flag);
                }
                if (keyid1.indexOf(";") == 0) {
                    props.setProperty("keyid1", keyid1.substring(1));
                    props.setProperty("keyid1", keyid1.substring(1));
                    props.setProperty("keyid2", keyid2.substring(1));
                    props.setProperty("keyid3", keyid3.substring(1));
                    props.setProperty("paramlistid", paramlistid.substring(1));
                    props.setProperty("paramlistversionid", paramlistversionid.substring(1));
                    props.setProperty("variantid", variantid.substring(1));
                    props.setProperty("dataset", dataset.substring(1));
                    props.setProperty("approvalflag", approvalflag.substring(1));
                    props.setProperty("propsmatch", properties.getProperty("propsmatch"));
                    props.setProperty("tracelogid", properties.getProperty("tracelogid"));
                    this.getActionProcessor().processAction("EditDataSet", "1", props);
                }
            }
            BaseSDIDataAction.createWorksheet(dataForms, this.getActionProcessor(), this.logger, false, sapphireConnection);
            if (sdidata) {
                EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), postEditDataSetEventObject);
            } else if (sdidataitem && isDataRelease) {
                SafeSQL safeSQL = new SafeSQL();
                boolean[] releasedAndManadatoryReleased = BaseSDIDataEntryAction.getReleasedAndManadatoryReleased(this.database, "WHERE\tsdidataitem.sdcid = rsetitems.sdcid AND sdidataitem.keyid1 = rsetitems.keyid1 AND sdidataitem.keyid2 = rsetitems.keyid2 AND sdidataitem.keyid3 = rsetitems.keyid3 AND rsetid = " + safeSQL.addVar(rsetid), safeSQL);
                postDataReleaseEventObject.setAllDataItemsReleased(releasedAndManadatoryReleased[0]);
                postDataReleaseEventObject.setAllMandatoryDataItemsReleased(releasedAndManadatoryReleased[1]);
                EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), postDataReleaseEventObject);
            }
            BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, postEventType);
            sdcPostRules.setBeforeEditImage(beforeEditImage);
            if (sdidataitem) {
                if (properties.getProperty("releasedflag").length() > 0 && beforeEditImage != null) {
                    WorkItemItemRuleEvaluator ruleProcessor = new WorkItemItemRuleEvaluator();
                    ruleProcessor.evaluateRuleOnDataItemRelease(this.getSDCProcessor(), sdcid, beforeEditImage, sdiData, this.database, this.connectionInfo);
                }
                Trace.startBusinessRule(sdcid + "." + "PostEditDataItem", true);
                sdcPreRules.postEditSDIDataItem(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PostEditDataItem", true);
                Trace.startBusinessRule(sdcid + "." + "PostEditDataItem", false);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.postEditSDIDataItem(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PostEditDataItem", false);
                if (isDataRelease) {
                    Trace.startBusinessRule(sdcid + "." + "PostDataRelease", true);
                    sdcPreReleaseRules.postReleaseData(sdidatatable, properties);
                    Trace.endBusinessRule(sdcid + "." + "PostDataRelease", true);
                    Trace.startBusinessRule(sdcid + "." + "PostDataRelease", false);
                    for (BaseSDCRules customRules : sdcPreReleaseRules.getCustomRuleList()) {
                        customRules.postReleaseData(sdidatatable, properties);
                    }
                    Trace.endBusinessRule(sdcid + "." + "PostDataRelease", false);
                    sdcPreRules.endRule();
                }
            } else if (sdidata) {
                if (beforeEditImage != null) {
                    WorkItemItemRuleEvaluator ruleProcessor = new WorkItemItemRuleEvaluator();
                    if (properties.containsKey("s_datasetstatus")) {
                        ruleProcessor.evaluateRuleOnDataSetStatusUpdate(this.getSDCProcessor(), sdcid, beforeEditImage, sdiData, this.database, this.connectionInfo);
                    }
                    this.autoStartStopWapActivity(sdiData.getDataset("dataset"), this.database, sdcPostRules);
                }
                if (properties.containsKey("s_datasetstatus") || properties.containsKey("s_remeasuredflag") || properties.containsKey("s_retestedflag")) {
                    this.evaluateSDISpecRules(sdiData, sdcPostRules, properties.containsKey("s_remeasuredflag"), properties.containsKey("s_retestedflag"));
                    if (properties.containsKey("s_datasetstatus")) {
                        this.triggerRedoCalc(sdiData, sdcPostRules);
                    }
                }
                if (beforeEditImage != null && properties.containsKey("availabilityflag")) {
                    HashMap<String, String> availabilityYes = new HashMap<String, String>();
                    availabilityYes.put("availabilityflag", "Y");
                    DataSet datasetDataMadeAvailable = sdiData.getDataset("dataset").getFilteredDataSet(availabilityYes);
                    DataSet beforeEditSDIDataNotAvailable = beforeEditImage.getDataset("dataset").getFilteredDataSet(availabilityYes, true);
                    DataSet qcBatchDS = new DataSet();
                    for (int k = 0; k < datasetDataMadeAvailable.getRowCount(); ++k) {
                        String qcBatchId;
                        HashMap<String, Object> findDS = new HashMap<String, Object>();
                        findDS.put("sdcid", sdcid);
                        findDS.put("keyid1", datasetDataMadeAvailable.getValue(k, "keyid1"));
                        findDS.put("keyid2", datasetDataMadeAvailable.getValue(k, "keyid2"));
                        findDS.put("keyid3", datasetDataMadeAvailable.getValue(k, "keyid3"));
                        findDS.put("paramlistid", datasetDataMadeAvailable.getValue(k, "paramlistid"));
                        findDS.put("paramlistversionid", datasetDataMadeAvailable.getValue(k, "paramlistversionid"));
                        findDS.put("variantid", datasetDataMadeAvailable.getValue(k, "variantid"));
                        findDS.put("dataset", datasetDataMadeAvailable.getBigDecimal(k, "dataset"));
                        int datasetRow = beforeEditSDIDataNotAvailable.findRow(findDS);
                        if (datasetRow <= -1 || (qcBatchId = beforeEditSDIDataNotAvailable.getValue(datasetRow, "s_qcbatchid")).length() <= 0) continue;
                        qcBatchDS.copyRow(beforeEditSDIDataNotAvailable, datasetRow, 1);
                    }
                    if (qcBatchDS.getRowCount() > 0) {
                        try {
                            QCUtil.syncQCBatchReagentInstrument(qcBatchDS, this.getActionProcessor());
                        }
                        catch (SapphireException e) {
                            this.logger.error("WARNING: FAILED TO SYNC AQCREAGENTUSE (" + e.getMessage() + ")", e);
                        }
                    }
                }
                Trace.startBusinessRule(sdcid + "." + "PostEditDataSet", true);
                sdcPreRules.postEditSDIData(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PostEditDataSet", true);
                Trace.startBusinessRule(sdcid + "." + "PostEditDataSet", false);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.postEditSDIData(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PostEditDataSet", false);
            } else if (sdidataapproval) {
                Trace.startBusinessRule(sdcid + "." + "PostEditDataApproval", true);
                sdcPreRules.postEditSDIDataApproval(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PostEditDataApproval", true);
                Trace.startBusinessRule(sdcid + "." + "PostEditDataApproval", false);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.postEditSDIDataApproval(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PostEditDataApproval", false);
            }
            sdcPostRules.endRule();
            dam.clearRSet(rsetid);
        } else {
            this.logger.error("Failed to create dataset rset.");
        }
    }

    private void setCompleteDtCompleteByNull(DataSet sdidata, BaseSDCRules sdcPreRules) throws SapphireException {
        for (int i = 0; i < sdidata.getRowCount(); ++i) {
            String oldStatus = sdcPreRules.getOldSDIDataValue(sdidata, i, "s_datasetstatus");
            String dsStatus = sdidata.getValue(i, "s_datasetstatus");
            if (!sdcPreRules.hasSDIDataValueChanged(sdidata, i, "s_datasetstatus") || !"Completed".equalsIgnoreCase(oldStatus) || !"DataEntered".equalsIgnoreCase(dsStatus) && !"InProgress".equalsIgnoreCase(dsStatus) && !"Initial".equalsIgnoreCase(dsStatus)) continue;
            sdidata.setString(i, "completeddt", null);
            sdidata.setString(i, "completedby", "");
        }
    }

    private void setWapStatusToPendingOnUncancel(DataSet sdiDataset, BaseSDCRules sdcPreRules) throws SapphireException {
        for (int i = 0; i < sdiDataset.getRowCount(); ++i) {
            String oldWapStatus = sdcPreRules.getOldSDIDataValue(sdiDataset, i, "wapstatus");
            String oldDatasetStatus = sdcPreRules.getOldSDIDataValue(sdiDataset, i, "s_datasetstatus");
            if (!sdcPreRules.hasSDIDataValueChanged(sdiDataset, i, "s_datasetstatus") || !"Cancelled".equalsIgnoreCase(oldDatasetStatus) || !"Cancelled".equalsIgnoreCase(oldWapStatus)) continue;
            sdiDataset.setString(i, "wapstatus", "Pending");
        }
    }

    private void setWapStatusToCancel(DataSet sdidata, BaseSDCRules sdcPreRules) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select w.activityid from activityworksdi w, sdidata s ").append(" where s.sdcid = ? and s.keyid1 = ? and s.keyid2 = ? and s.keyid3 = ?").append(" and s.paramlistid = ? and s.paramlistversionid = ? and s.variantid = ? and s.dataset = ? and w.worksdcid = 'DataSet' and w.workkeyid1 = s.sdidataid");
        PreparedStatement getSDIDataActivity = this.database.prepareStatement("getsdidataactivity", sql.toString());
        try {
            for (int i = 0; i < sdidata.getRowCount(); ++i) {
                String oldWapStatus = sdcPreRules.getOldSDIDataValue(sdidata, i, "wapstatus");
                String dsStatus = sdidata.getValue(i, "s_datasetstatus");
                if (!sdcPreRules.hasSDIDataValueChanged(sdidata, i, "s_datasetstatus") || !"Cancelled".equalsIgnoreCase(dsStatus) || !"Pending".equalsIgnoreCase(oldWapStatus)) continue;
                getSDIDataActivity.setString(1, sdidata.getValue(i, "sdcid"));
                getSDIDataActivity.setString(2, sdidata.getValue(i, "keyid1"));
                getSDIDataActivity.setString(3, sdidata.getValue(i, "keyid2"));
                getSDIDataActivity.setString(4, sdidata.getValue(i, "keyid3"));
                getSDIDataActivity.setString(5, sdidata.getValue(i, "paramlistid"));
                getSDIDataActivity.setString(6, sdidata.getValue(i, "paramlistversionid"));
                getSDIDataActivity.setString(7, sdidata.getValue(i, "variantid"));
                getSDIDataActivity.setString(8, sdidata.getValue(i, "dataset"));
                DataSet ds = new DataSet(getSDIDataActivity.executeQuery());
                if (ds.getRowCount() != 0) continue;
                sdidata.setString(i, "wapstatus", "Cancelled");
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            this.database.closeStatement("getsdidataactivity");
        }
    }

    private void autoStartStopWapActivity(DataSet sdidata, DBAccess database, BaseSDCRules sdcPostRules) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select distinct a.activityid, a.activitystatus, s.sdidataid from activity a, activityworksdi w, sdidata s ").append(" where s.sdcid = ? and s.keyid1 = ? and s.keyid2 = ? and s.keyid3 = ?").append(" and s.paramlistid = ? and s.paramlistversionid = ? and s.variantid = ? and s.dataset = ? and w.worksdcid = 'DataSet' and w.workkeyid1 = s.sdidataid and a.activityid = w.activityid ");
        PreparedStatement getSDIDataActivity = database.prepareStatement("getsdidataactivity", sql.toString());
        StringBuffer startActivities = new StringBuffer();
        StringBuffer updateCompleteCountActivities = new StringBuffer();
        DataSet deleteActivityWorkSDIs = new DataSet();
        ArrayList<String> listStartActivities = new ArrayList<String>();
        ArrayList<String> listCompleteActivities = new ArrayList<String>();
        try {
            for (int r = 0; r < sdidata.getRowCount(); ++r) {
                boolean wapStatusPendingOrAssigned;
                String startedDt = sdidata.getValue(r, "starteddt");
                boolean started = startedDt.length() > 0 && sdcPostRules.hasSDIDataValueChanged(sdidata, r, "starteddt");
                String datasetStatus = sdidata.getValue(r, "s_datasetstatus");
                String wapStatus = sdidata.getValue(r, "wapstatus");
                String oldwapStatus = sdcPostRules.getOldSDIDataValue(sdidata, r, "wapstatus");
                boolean cancelled = sdcPostRules.hasSDIDataValueChanged(sdidata, r, "s_datasetstatus") && "Cancelled".equalsIgnoreCase(datasetStatus);
                boolean uncancelled = sdcPostRules.hasSDIDataValueChanged(sdidata, r, "s_datasetstatus") && "Cancelled".equalsIgnoreCase(sdcPostRules.getOldSDIDataValue(sdidata, r, "s_datasetstatus"));
                boolean completedorcancelled = sdcPostRules.hasSDIDataValueChanged(sdidata, r, "s_datasetstatus") && ("Cancelled".equalsIgnoreCase(datasetStatus) || "Completed".equalsIgnoreCase(datasetStatus));
                boolean bl = wapStatusPendingOrAssigned = !"Cancelled".equalsIgnoreCase(wapStatus) && ("Pending".equalsIgnoreCase(oldwapStatus) || "Assigned".equalsIgnoreCase(oldwapStatus));
                if (!wapStatusPendingOrAssigned || !started && !completedorcancelled && !uncancelled) continue;
                getSDIDataActivity.setString(1, sdidata.getValue(r, "sdcid"));
                getSDIDataActivity.setString(2, sdidata.getValue(r, "keyid1"));
                getSDIDataActivity.setString(3, sdidata.getValue(r, "keyid2"));
                getSDIDataActivity.setString(4, sdidata.getValue(r, "keyid3"));
                getSDIDataActivity.setString(5, sdidata.getValue(r, "paramlistid"));
                getSDIDataActivity.setString(6, sdidata.getValue(r, "paramlistversionid"));
                getSDIDataActivity.setString(7, sdidata.getValue(r, "variantid"));
                getSDIDataActivity.setString(8, sdidata.getValue(r, "dataset"));
                DataSet ds = new DataSet(getSDIDataActivity.executeQuery());
                if (ds.getRowCount() <= 0) continue;
                String activityid = ds.getValue(0, "activityid");
                String activityStatus = ds.getValue(0, "activitystatus");
                String sdidataid = ds.getValue(0, "sdidataid");
                if (started && "Activated".equalsIgnoreCase(activityStatus) && !listStartActivities.contains(activityid)) {
                    listStartActivities.add(activityid);
                }
                if ("Draft".equalsIgnoreCase(activityStatus) && cancelled) {
                    deleteActivityWorkSDIs.copyRow(ds, -1, 1);
                    continue;
                }
                if (!completedorcancelled && !uncancelled || listCompleteActivities.contains(activityid)) continue;
                listCompleteActivities.add(activityid);
                updateCompleteCountActivities.append(";").append(activityid);
            }
            for (int i = 0; i < listStartActivities.size(); ++i) {
                if (listCompleteActivities.contains(listStartActivities.get(i))) continue;
                startActivities.append(";").append(listStartActivities.get(i));
            }
            if (startActivities.length() > 0) {
                PropertyList setstatus = new PropertyList();
                setstatus.setProperty("activityid", startActivities.substring(1));
                setstatus.setProperty("status", "In Progress");
                this.getActionProcessor().processActionClass(SetActivityStatus.class.getName(), setstatus);
            }
            if (updateCompleteCountActivities.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("activityid", updateCompleteCountActivities.substring(1));
                this.getActionProcessor().processActionClass(UpdateActivityCompleteCount.class.getName(), props);
            }
            if (deleteActivityWorkSDIs.getRowCount() > 0) {
                deleteActivityWorkSDIs.sort("activityid");
                ArrayList<DataSet> activityGrps = deleteActivityWorkSDIs.getGroupedDataSets("activityid");
                for (int g = 0; g < activityGrps.size(); ++g) {
                    DataSet activityWorkSDIs = activityGrps.get(g);
                    PropertyList props = new PropertyList();
                    props.setProperty("activityid", activityWorkSDIs.getValue(0, "activityid"));
                    props.setProperty("worksdcid", "DataSet");
                    props.setProperty("workkeyid1", activityWorkSDIs.getColumnValues("sdidataid", ";"));
                    props.setProperty("setcancelled", "Y");
                    this.getActionProcessor().processActionClass(RemoveActivityWorkSDI.class.getName(), props);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            database.closeStatement("getsdidataactivity");
        }
    }

    private void triggerRedoCalc(SDIData sdiData, BaseSDCRules sdcRule) throws SapphireException {
        boolean excludeCancelledDatasets = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("excludecancelleddataset", "Y").equals("Y");
        DataSet datasetData = sdiData.getDataset("dataset");
        HashSet<String> uniqueKeys = new HashSet<String>();
        for (int r = 0; r < datasetData.getRowCount(); ++r) {
            if (excludeCancelledDatasets && sdcRule.hasSDIDataValueChanged(datasetData, r, "s_datasetstatus") && "Cancelled".equalsIgnoreCase(datasetData.getValue(r, "s_datasetstatus"))) {
                uniqueKeys.add(datasetData.getValue(r, "keyid1"));
                continue;
            }
            if (!sdcRule.hasSDIDataValueChanged(datasetData, r, "s_datasetstatus") || !"Cancelled".equalsIgnoreCase(sdcRule.getOldSDIDataValue(datasetData, r, "s_datasetstatus"))) continue;
            uniqueKeys.add(datasetData.getValue(r, "keyid1"));
        }
        if (uniqueKeys.size() > 0) {
            String keyids = String.join((CharSequence)";", uniqueKeys);
            String sdcid = datasetData.getValue(0, "sdcid");
            String tracelogid = datasetData.getValue(0, "tracelogid");
            String auditreason = datasetData.getValue(0, "auditreason");
            String auditactivity = datasetData.getValue(0, "auditactivity");
            String auditsignedflag = datasetData.getValue(0, "auditsignedflag");
            PropertyList newProps = new PropertyList();
            newProps.setProperty("sdcid", sdcid);
            newProps.setProperty("keyid1", keyids);
            newProps.setProperty("tracelogid", tracelogid);
            newProps.setProperty("auditreason", auditreason);
            newProps.setProperty("auditactivity", auditactivity);
            newProps.setProperty("auditsignedflag", auditsignedflag);
            newProps.setProperty("_fireSyncActions", "N");
            this.getActionProcessor().processAction("RedoCalculations", "1", newProps);
        }
    }

    private void evaluateSDISpecRules(SDIData sdiData, BaseSDCRules sdcRule, boolean remeasured, boolean retested) throws SapphireException {
        PropertyList specRuleEvalPolicyPL;
        DataSet datasetData = sdiData.getDataset("dataset");
        DataSet specEvalDS = new DataSet();
        for (int r = 0; r < datasetData.getRowCount(); ++r) {
            if (sdcRule.hasSDIDataValueChanged(datasetData, r, "s_datasetstatus") && ("Cancelled".equalsIgnoreCase(datasetData.getValue(r, "s_datasetstatus")) || "Cancelled".equalsIgnoreCase(sdcRule.getOldSDIDataValue(datasetData, r, "s_datasetstatus")))) {
                specEvalDS.copyRow(datasetData, r, 1);
                continue;
            }
            if (remeasured) {
                specEvalDS.copyRow(datasetData, r, 1);
                continue;
            }
            if (!retested) continue;
            specEvalDS.copyRow(datasetData, r, 1);
        }
        if (specEvalDS.getRowCount() > 0 && this.isSpecFilterRequired(specRuleEvalPolicyPL = this.getSpecRulesPolicy())) {
            String rsetid = this.getRSet(specEvalDS.getValue(0, "sdcid"), specEvalDS.getColumnValues("keyid1", ";"), specEvalDS.getColumnValues("keyid2", ";"), specEvalDS.getColumnValues("keyid3", ";"), false);
            String selectsdispec = "SELECT sdispec.* FROM sdispec, rsetitems WHERE sdispec.sdcid = rsetitems.sdcid AND sdispec.keyid1 = rsetitems.keyid1  AND sdispec.keyid2 = rsetitems.keyid2 AND sdispec.keyid3 = rsetitems.keyid3 AND rsetitems.rsetid = ?";
            this.database.createPreparedResultSet("selectsdispec", selectsdispec, new Object[]{rsetid});
            DataSet sdispec = new DataSet(this.database.getResultSet("selectsdispec"));
            if (sdispec.getRowCount() > 0) {
                String selectdispec = "SELECT sdidataitemspec.* FROM sdidataitemspec, rsetitems WHERE sdidataitemspec.sdcid = rsetitems.sdcid AND sdidataitemspec.keyid1 = rsetitems.keyid1  AND sdidataitemspec.keyid2 = rsetitems.keyid2 AND sdidataitemspec.keyid3 = rsetitems.keyid3 AND rsetitems.rsetid = ?";
                this.database.createPreparedResultSet("sdidataitemsspec", selectdispec, new Object[]{rsetid});
                DataSet sdidataitemspec = new DataSet(this.database.getResultSet("sdidataitemsspec"));
                String selectdi = "SELECT sdidataitem.* FROM sdidataitem, rsetitems WHERE sdidataitem.sdcid = rsetitems.sdcid AND sdidataitem.keyid1 = rsetitems.keyid1  AND sdidataitem.keyid2 = rsetitems.keyid2 AND sdidataitem.keyid3 = rsetitems.keyid3 AND rsetitems.rsetid = ?";
                this.database.createPreparedResultSet("sdidataitems", selectdi, new Object[]{rsetid});
                DataSet sdidataitems = new DataSet(this.database.getResultSet("sdidataitems"));
                String selectds = "SELECT sdidata.* FROM sdidata, rsetitems WHERE sdidata.sdcid = rsetitems.sdcid AND sdidata.keyid1 = rsetitems.keyid1  AND sdidata.keyid2 = rsetitems.keyid2 AND sdidata.keyid3 = rsetitems.keyid3 AND rsetitems.rsetid = ? ";
                this.database.createPreparedResultSet("sdidata", selectds, new Object[]{rsetid});
                DataSet sdidata = new DataSet(this.database.getResultSet("sdidata"));
                DataSet filteredSpecRuleEvalDataItems = this.filterSpecDataItemBasedOnPolicy(specRuleEvalPolicyPL, sdidata, sdidataitems, sdidataitemspec);
                this.checkSpecRules(this.database, sdispec, filteredSpecRuleEvalDataItems, sdidataitems);
                this.logger.info("Saving changes to sdispec");
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("_modifiedtotal", "Y");
                DataSet updatesdispec = sdispec.getFilteredDataSet(filter);
                DataSetUtil.update(this.database, updatesdispec, "sdispec", new SDIData().getKeys("sdispec"));
            }
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    protected DataSet getSpecRules(String specid, String specversionid) throws SapphireException {
        DataSet specrules = (DataSet)CacheUtil.get(this.connectionInfo.getDatabaseId(), "SpecRules", specid + ";" + specversionid);
        if (specrules == null) {
            specrules = new DataSet();
            String sql = "SELECT ruledef FROM specrule WHERE specid=? AND specversionid=? ORDER BY ruleno";
            this.database.createPreparedResultSet("specrules", sql, new String[]{specid, specversionid});
            specrules.setResultSet(this.database.getResultSet("specrules"));
            CacheUtil.put(this.connectionInfo.getDatabaseId(), "SpecRules", specid + ";" + specversionid, specrules);
        }
        return specrules;
    }

    protected PropertyList getSpecRulesPolicy() throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
        PropertyList specRuleEvalPolicyPL = policy.getPropertyListNotNull("specruleevaluationoptions");
        return specRuleEvalPolicyPL;
    }

    protected boolean isSpecFilterRequired(PropertyList specRuleEvalPolicyPL) throws SapphireException {
        boolean excludeCancelledDatasetsFromCalcs = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("excludecancelleddataset", "Y").equals("Y");
        boolean excludeOutlier = "N".equalsIgnoreCase(specRuleEvalPolicyPL.getProperty("includeoutlierdataitems", "Y"));
        boolean excludeCancelledDataSet = "N".equalsIgnoreCase(specRuleEvalPolicyPL.getProperty("includecancelleddatasets", "Y"));
        boolean excludeRemeasuredDataSet = "N".equalsIgnoreCase(specRuleEvalPolicyPL.getProperty("includeremeasureddatasets", "Y"));
        boolean excludeRetestedDataSet = "N".equalsIgnoreCase(specRuleEvalPolicyPL.getProperty("includeretesteddatasets", "Y"));
        if (excludeCancelledDatasetsFromCalcs && !excludeCancelledDataSet) {
            throw new SapphireException("Cancelled datasets cannot be included in spec evaluation when they are excluded from calculations. Please check your policy settings.");
        }
        return excludeOutlier || excludeCancelledDataSet || excludeRemeasuredDataSet || excludeRetestedDataSet;
    }

    protected void checkSpecRules(DBAccess database, DataSet sdispec, DataSet sdidataitemspec, DataSet sdidataitems) throws SapphireException {
        sdispec.sort("sdcid, keyid1, keyid2, keyid3, specid, specversionid");
        int rows = sdispec.getRowCount();
        for (int row = 0; row < rows; ++row) {
            String sdcid = sdispec.getString(row, "sdcid");
            String keyid1 = sdispec.getString(row, "keyid1");
            String keyid2 = sdispec.getString(row, "keyid2");
            String keyid3 = sdispec.getString(row, "keyid3");
            String specid = sdispec.getString(row, "specid");
            String specversionid = sdispec.getString(row, "specversionid");
            String specCondition = sdispec.getValue(row, "condition");
            HashMap<String, String> filterspec = new HashMap<String, String>();
            filterspec.put("sdcid", sdcid);
            filterspec.put("keyid1", keyid1);
            filterspec.put("keyid2", keyid2);
            filterspec.put("keyid3", keyid3);
            DataSet sdi_dataitems = sdidataitems.getFilteredDataSet(filterspec);
            filterspec.put("specid", specid);
            filterspec.put("specversionid", specversionid);
            DataSet spec_sdidataitemspec = sdidataitemspec.getFilteredDataSet(filterspec);
            if (spec_sdidataitemspec.getRowCount() == 0 && !sdispec.getValue(row, "waivedflag").equals("Y")) {
                if (specCondition.length() <= 0) continue;
                sdispec.setValue(row, "condition", null);
                sdispec.setValue(row, "waivedflag", "N");
                sdispec.setString(row, "_modifiedtotal", "Y");
                continue;
            }
            if (spec_sdidataitemspec.getRowCount() > 0 && !sdispec.getValue(row, "waivedflag").equals("Y")) {
                boolean evalCondNotExists = true;
                for (int count = 0; count < spec_sdidataitemspec.getRowCount(); ++count) {
                    String condition = spec_sdidataitemspec.getValue(count, "condition", "");
                    if (!OpalUtil.isNotEmpty(condition)) continue;
                    evalCondNotExists = false;
                    break;
                }
                if (OpalUtil.isNotEmpty(specCondition) && evalCondNotExists) {
                    sdispec.setValue(row, "condition", null);
                    sdispec.setValue(row, "waivedflag", "N");
                    sdispec.setString(row, "_modifiedtotal", "Y");
                }
            }
            boolean hasEnteredText = false;
            HashSet<String> plSet = new HashSet<String>();
            for (int di = 0; di < spec_sdidataitemspec.getRowCount(); ++di) {
                String plId = spec_sdidataitemspec.getValue(di, "paramlistid");
                String plVersionId = spec_sdidataitemspec.getValue(di, "paramlistversionid");
                String plVariant = spec_sdidataitemspec.getValue(di, "variantid");
                if (plSet.contains(plId + ";" + plVersionId + ";" + plVariant)) continue;
                HashMap<String, String> findDS = new HashMap<String, String>();
                findDS.put("paramlistid", plId);
                findDS.put("paramlistversionid", plVersionId);
                findDS.put("variantid", plVariant);
                DataSet dataitems = sdi_dataitems.getFilteredDataSet(findDS);
                for (int diRow = 0; diRow < dataitems.getRowCount(); ++diRow) {
                    boolean bl = hasEnteredText = dataitems.getValue(diRow, "enteredtext").length() > 0;
                    if (hasEnteredText) break;
                }
                if (hasEnteredText) break;
                plSet.add(plId + ";" + plVersionId + ";" + plVariant);
            }
            if (!hasEnteredText) continue;
            try {
                DataSet specrules;
                String sql = "SELECT spec.specid, spec.ruletypeflag, spec.oosgeneratingflag, spec.specusetype FROM spec, specrule WHERE spec.specid = specrule.specid AND spec.specversionid = specrule.specversionid AND spec.specid = ? AND spec.specversionid = ?";
                database.createPreparedResultSet("verifyspecexists", sql, new String[]{specid, specversionid});
                if (!database.getNext("verifyspecexists")) continue;
                String spectype = database.getString("verifyspecexists", "ruletypeflag");
                if (spectype == null || spectype.equals("E")) {
                    throw new SapphireException("SPEC_RULES_FAILURE", "Expression based spec rules not supported on this platform");
                }
                if (spectype.equals("B")) {
                    specrules = this.getSpecRules(specid, specversionid);
                    this.checkBandedSpecRules(sdcid, keyid1, keyid2, keyid3, specid, specversionid, sdispec, specrules, spec_sdidataitemspec);
                    continue;
                }
                if (spectype.equals("S")) {
                    specrules = this.getSpecRules(specid, specversionid);
                    this.checkStructuredSpecRules(sdcid, keyid1, keyid2, keyid3, specid, specversionid, sdispec, specrules, spec_sdidataitemspec, sdidataitems);
                    continue;
                }
                if (!spectype.equals("C")) continue;
                specrules = this.getSpecRules(specid, specversionid);
                this.checkCustomSpecRules(sdcid, keyid1, keyid2, keyid3, specid, specversionid, sdispec, specrules, spec_sdidataitemspec, sdidataitems);
                continue;
            }
            catch (Exception e) {
                throw new SapphireException("SPEC_RULES_FAILURE", "Exception found trying to process spec rules: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }

    protected void checkBandedSpecRules(String sdcid, String keyid1, String keyid2, String keyid3, String specid, String specversionid, DataSet sdispec, DataSet specrules, DataSet sdidataitemspec) {
        boolean match = false;
        HashMap<String, String> specFindMap = new HashMap<String, String>();
        specFindMap.put("sdcid", sdcid);
        specFindMap.put("keyid1", keyid1);
        specFindMap.put("keyid2", keyid2);
        specFindMap.put("keyid3", keyid3);
        specFindMap.put("specid", specid);
        specFindMap.put("specversionid", specversionid);
        int sdispecRow = sdispec.findRow(specFindMap);
        if (sdispecRow >= 0) {
            for (int i = 0; !match && i < specrules.size(); ++i) {
                String ruledef = specrules.getValue(i, "ruledef");
                String[] parts = StringUtil.split(ruledef, ";");
                if (parts.length != 2) continue;
                if (parts[1].equals("(none)")) {
                    parts[1] = "";
                }
                if (i < specrules.size() - 1) {
                    specFindMap.put("condition", parts[0]);
                    boolean bl = match = sdidataitemspec.findRow(specFindMap) >= 0;
                }
                if (!match && i != specrules.size() - 1 || sdispec.getValue(sdispecRow, "waivedflag").equals("Y") || sdispec.getValue(sdispecRow, "condition").equals(parts[1])) continue;
                sdispec.setValue(sdispecRow, "condition", parts[1]);
                sdispec.setValue(sdispecRow, "waivedflag", "N");
                sdispec.setString(sdispecRow, "_modifiedtotal", "Y");
            }
        }
    }

    protected void checkCustomSpecRules(String sdcid, String keyid1, String keyid2, String keyid3, String specid, String specversionid, DataSet sdispec, DataSet specrules, DataSet sdidataitemspec, DataSet sdidataitems) {
        HashMap<String, String> specFilterMap = new HashMap<String, String>();
        specFilterMap.put("specid", specid);
        specFilterMap.put("specversionid", specversionid);
        specFilterMap.put("sdcid", sdcid);
        specFilterMap.put("keyid1", keyid1);
        specFilterMap.put("keyid2", keyid2);
        specFilterMap.put("keyid3", keyid3);
        DataSet filteredsdispec = sdispec.getFilteredDataSet(specFilterMap);
        if (filteredsdispec.size() == 1) {
            DataSet filteredsdidataitem = sdidataitems.getFilteredDataSet(specFilterMap);
            DataSet filteredsdidataitemspec = sdidataitemspec.getFilteredDataSet(specFilterMap);
            String condition = null;
            try {
                for (int i = 0; condition == null && i < specrules.size(); ++i) {
                    String ruledef = specrules.getValue(i, "ruledef");
                    Class<?> c = Class.forName(ruledef);
                    BaseSpecRule baseRequest = (BaseSpecRule)c.newInstance();
                    baseRequest.setConnectionId(this.connectionInfo.getConnectionId());
                    condition = baseRequest.getSpecCondition(sdcid, keyid1, keyid2, keyid3, specid, specversionid, filteredsdidataitemspec, filteredsdidataitem);
                    if (condition == null || filteredsdispec.getValue(0, "waivedflag").equals("Y") || filteredsdispec.getValue(0, "condition").equals(condition)) continue;
                    filteredsdispec.setValue(0, "condition", condition);
                    filteredsdispec.setValue(0, "waivedflag", "N");
                    filteredsdispec.setString(0, "_modifiedtotal", "Y");
                }
            }
            catch (Exception e) {
                this.logger.error("Failed to checkspecrule for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText() + " and specid '" + specid + "'. Exception: " + e.getMessage(), e);
            }
        }
    }

    protected void checkStructuredSpecRules(String sdcid, String keyid1, String keyid2, String keyid3, String specid, String specversionid, DataSet sdispec, DataSet specrules, DataSet sdidataitemspec, DataSet sdidataitems) {
        boolean match = false;
        HashMap<String, String> thisSDISpecMap = new HashMap<String, String>();
        thisSDISpecMap.put("sdcid", sdcid);
        thisSDISpecMap.put("keyid1", keyid1);
        thisSDISpecMap.put("keyid2", keyid2);
        thisSDISpecMap.put("keyid3", keyid3);
        thisSDISpecMap.put("specid", specid);
        thisSDISpecMap.put("specversionid", specversionid);
        int sdispecRow = sdispec.findRow(thisSDISpecMap);
        if (sdispecRow >= 0) {
            Boolean allResults = null;
            Boolean allMandatoryResults = null;
            boolean mandatoryFlagAdded = false;
            DataSet theseDataItemSpecs = sdidataitemspec.getFilteredDataSet(thisSDISpecMap);
            for (int specRule = 0; !match && specRule < specrules.size(); ++specRule) {
                int i;
                String ruledef = specrules.getValue(specRule, "ruledef");
                String[] parts = StringUtil.split(ruledef, ";");
                if (parts.length != 6) continue;
                String speccondition = parts[0];
                String qualifier = parts[1];
                String quantity = parts[2];
                String mandatory = parts[3];
                String arenot = parts[4];
                String itemcondition = parts[5];
                if (speccondition.equals("(none)")) {
                    speccondition = "";
                }
                if (itemcondition.equals("(none)")) {
                    itemcondition = "";
                }
                if ((mandatory.equals("Y") || qualifier.equals("M")) && !mandatoryFlagAdded) {
                    sdidataitemspec.addColumn("_mandatoryflag", 0);
                    HashMap<String, Object> dataitemSearch = new HashMap<String, Object>();
                    for (i = 0; i < theseDataItemSpecs.size(); ++i) {
                        dataitemSearch.put("sdcid", sdcid);
                        dataitemSearch.put("keyid1", keyid1);
                        dataitemSearch.put("keyid2", keyid2);
                        dataitemSearch.put("keyid3", keyid3);
                        dataitemSearch.put("paramlistid", theseDataItemSpecs.getObject(i, "paramlistid"));
                        dataitemSearch.put("paramlistversionid", theseDataItemSpecs.getObject(i, "paramlistversionid"));
                        dataitemSearch.put("variantid", theseDataItemSpecs.getObject(i, "variantid"));
                        dataitemSearch.put("dataset", theseDataItemSpecs.getObject(i, "dataset"));
                        dataitemSearch.put("paramid", theseDataItemSpecs.getObject(i, "paramid"));
                        dataitemSearch.put("paramtype", theseDataItemSpecs.getObject(i, "paramtype"));
                        dataitemSearch.put("replicateid", theseDataItemSpecs.getObject(i, "replicateid"));
                        int dataItemRow = sdidataitems.findRow(dataitemSearch);
                        if (dataItemRow < 0) continue;
                        theseDataItemSpecs.setString(i, "_mandatoryflag", sdidataitems.getValue(dataItemRow, "mandatoryflag"));
                    }
                    mandatoryFlagAdded = true;
                }
                boolean check = true;
                if (qualifier.equals("A")) {
                    if (allResults == null) {
                        for (i = 0; i < theseDataItemSpecs.size() && allResults == null; ++i) {
                            if (!theseDataItemSpecs.isNull(i, "condition")) continue;
                            allResults = false;
                        }
                        if (allResults == null) {
                            allResults = true;
                        }
                    }
                    check = allResults;
                } else if (qualifier.equals("M")) {
                    if (allMandatoryResults == null) {
                        for (i = 0; i < theseDataItemSpecs.size() && allMandatoryResults == null; ++i) {
                            if (!theseDataItemSpecs.isNull(i, "condition") || !theseDataItemSpecs.getValue(i, "_mandatoryflag").equals("Y")) continue;
                            allMandatoryResults = false;
                        }
                    }
                    if (allMandatoryResults == null) {
                        allMandatoryResults = true;
                    }
                    check = allMandatoryResults;
                }
                if (!check) continue;
                if (itemcondition.length() == 0) {
                    match = true;
                } else {
                    int count = 0;
                    int mandatoryitems = 0;
                    int totalitems = 0;
                    for (int i2 = 0; i2 < theseDataItemSpecs.size(); ++i2) {
                        if (mandatory.equals("Y")) {
                            ++totalitems;
                            if (!theseDataItemSpecs.getValue(i2, "_mandatoryflag").equals("Y")) continue;
                            ++mandatoryitems;
                            if (!theseDataItemSpecs.getValue(i2, "condition").equals(itemcondition)) continue;
                            ++count;
                            continue;
                        }
                        ++totalitems;
                        if (!theseDataItemSpecs.getValue(i2, "condition").equals(itemcondition)) continue;
                        ++count;
                    }
                    if (arenot.equals("N")) {
                        int n = count = mandatory.equals("Y") ? mandatoryitems - count : totalitems - count;
                    }
                    if (quantity.equals("1")) {
                        boolean bl = match = count > 0;
                    }
                    if (quantity.equals("2")) {
                        boolean bl = match = count >= 2;
                    }
                    if (quantity.equals("3")) {
                        boolean bl = match = count >= 3;
                    }
                    if (quantity.equals("4")) {
                        boolean bl = match = count >= 4;
                    }
                    if (quantity.equals("5")) {
                        boolean bl = match = count >= 5;
                    }
                    if (quantity.equals("A")) {
                        boolean bl = mandatory.equals("Y") ? count == mandatoryitems : (match = count == totalitems);
                    }
                }
                if (!match && specRule == specrules.size() - 1) {
                    speccondition = "";
                    match = true;
                }
                if (!match || sdispec.getValue(sdispecRow, "waivedflag").equals("Y") || sdispec.getValue(sdispecRow, "condition").equals(speccondition)) continue;
                sdispec.setValue(sdispecRow, "condition", speccondition);
                sdispec.setValue(sdispecRow, "waivedflag", "N");
                sdispec.setString(sdispecRow, "_modifiedtotal", "Y");
            }
        }
    }

    protected DataSet filterSpecDataItemBasedOnPolicy(PropertyList specRuleEvalPolicyPL, DataSet sdidata, DataSet sdidataitems, DataSet sdidataitemspec) throws SapphireException {
        if (sdidataitemspec.getRowCount() > 0) {
            sdidataitemspec.setString(-1, "__excludespecruleeval", "N");
            DataSet diExclude = new DataSet();
            DataSet dsExclude = new DataSet();
            boolean excludeOutlier = "N".equalsIgnoreCase(specRuleEvalPolicyPL.getProperty("includeoutlierdataitems", "Y"));
            boolean excludeCancelledDataSet = "N".equalsIgnoreCase(specRuleEvalPolicyPL.getProperty("includecancelleddatasets", "Y"));
            boolean excludeRemeasuredDataSet = "N".equalsIgnoreCase(specRuleEvalPolicyPL.getProperty("includeremeasureddatasets", "Y"));
            boolean excludeRetestedDataSet = "N".equalsIgnoreCase(specRuleEvalPolicyPL.getProperty("includeretesteddatasets", "Y"));
            sdidata.setString(-1, "__excludespecruleeval", "N");
            for (int i = 0; i < sdidata.getRowCount(); ++i) {
                if (!(excludeCancelledDataSet && "Cancelled".equalsIgnoreCase(sdidata.getValue(i, "s_datasetstatus")) || excludeRemeasuredDataSet && "Y".equalsIgnoreCase(sdidata.getValue(i, "s_remeasuredflag"))) && (!excludeRetestedDataSet || !"Y".equalsIgnoreCase(sdidata.getValue(i, "s_retestedflag")))) continue;
                sdidata.setString(i, "__excludespecruleeval", "Y");
            }
            HashMap<String, String> excludeDSFilter = new HashMap<String, String>();
            excludeDSFilter.put("__excludespecruleeval", "Y");
            dsExclude = sdidata.getFilteredDataSet(excludeDSFilter);
            HashMap<String, Object> dsExcludeFilter = new HashMap<String, Object>();
            for (int i = 0; i < dsExclude.getRowCount(); ++i) {
                dsExcludeFilter.put("sdcid", dsExclude.getValue(i, "sdcid"));
                dsExcludeFilter.put("keyid1", dsExclude.getValue(i, "keyid1"));
                dsExcludeFilter.put("keyid2", dsExclude.getValue(i, "keyid2"));
                dsExcludeFilter.put("keyid3", dsExclude.getValue(i, "keyid3"));
                dsExcludeFilter.put("paramlistid", dsExclude.getValue(i, "paramlistid"));
                dsExcludeFilter.put("paramlistversionid", dsExclude.getValue(i, "paramlistversionid"));
                dsExcludeFilter.put("variantid", dsExclude.getValue(i, "variantid"));
                dsExcludeFilter.put("dataset", dsExclude.getBigDecimal(i, "dataset"));
                DataSet dsSpecExlude = sdidataitemspec.getFilteredDataSet(dsExcludeFilter);
                dsSpecExlude.setString(-1, "__excludespecruleeval", "Y");
                if (sdidataitemspec.findRow("__excludespecruleeval", "N") < 0) break;
            }
            dsExcludeFilter.clear();
            dsExcludeFilter.put("__excludespecruleeval", "N");
            DataSet dsSpecNotRemoved = sdidataitemspec.getFilteredDataSet(dsExcludeFilter);
            if (dsSpecNotRemoved.getRowCount() > 0) {
                if (excludeOutlier) {
                    HashMap<String, String> calcExcludeFilter = new HashMap<String, String>();
                    calcExcludeFilter.put("calcexcludeflag", "Y");
                    diExclude = sdidataitems.getFilteredDataSet(calcExcludeFilter);
                }
                HashMap<String, Object> diExcludeFilter = new HashMap<String, Object>();
                for (int i = 0; i < diExclude.getRowCount(); ++i) {
                    diExcludeFilter.put("sdcid", diExclude.getValue(i, "sdcid"));
                    diExcludeFilter.put("keyid1", diExclude.getValue(i, "keyid1"));
                    diExcludeFilter.put("keyid2", diExclude.getValue(i, "keyid2"));
                    diExcludeFilter.put("keyid3", diExclude.getValue(i, "keyid3"));
                    diExcludeFilter.put("paramlistid", diExclude.getValue(i, "paramlistid"));
                    diExcludeFilter.put("paramlistversionid", diExclude.getValue(i, "paramlistversionid"));
                    diExcludeFilter.put("variantid", diExclude.getValue(i, "variantid"));
                    diExcludeFilter.put("dataset", diExclude.getBigDecimal(i, "dataset"));
                    diExcludeFilter.put("paramid", diExclude.getValue(i, "paramid"));
                    diExcludeFilter.put("paramtype", diExclude.getValue(i, "paramtype"));
                    diExcludeFilter.put("replicateid", diExclude.getBigDecimal(i, "replicateid"));
                    DataSet dsSpecExlude = dsSpecNotRemoved.getFilteredDataSet(diExcludeFilter);
                    dsSpecExlude.setString(-1, "__excludespecruleeval", "Y");
                    if (sdidataitemspec.findRow("__excludespecruleeval", "N") < 0) break;
                }
            }
        }
        HashMap<String, String> specIncludeMap = new HashMap<String, String>();
        specIncludeMap.put("__excludespecruleeval", "N");
        return sdidataitemspec.getFilteredDataSet(specIncludeMap);
    }

    protected void setSDIDataValue(PropertyList properties, String tableid) throws SapphireException {
        String columnid = properties.getProperty("columnid");
        String value = properties.getProperty("value");
        if (columnid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "Columnid property not defined");
        }
        if (value.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "Value property not defined");
        }
        PropertyList newproperties = new PropertyList();
        newproperties.setProperty("sdcid", properties.getProperty("sdcid"));
        newproperties.setProperty("keyid1", properties.getProperty("keyid1"));
        newproperties.setProperty("keyid2", properties.getProperty("keyid2"));
        newproperties.setProperty("keyid3", properties.getProperty("keyid3"));
        newproperties.setProperty("paramlistid", properties.getProperty("paramlistid"));
        newproperties.setProperty("paramlistversionid", properties.getProperty("paramlistversionid"));
        newproperties.setProperty("variantid", properties.getProperty("variantid"));
        newproperties.setProperty("dataset", properties.getProperty("dataset"));
        if (tableid.equals("sdidataitem")) {
            newproperties.setProperty("paramid", properties.getProperty("paramid"));
            newproperties.setProperty("paramtype", properties.getProperty("paramtype"));
            newproperties.setProperty("replicateid", properties.getProperty("replicateid"));
        }
        newproperties.setProperty("auditreason", properties.getProperty("auditreason"));
        newproperties.setProperty("auditactivity", properties.getProperty("auditactivity", ""));
        newproperties.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", "N"));
        newproperties.setProperty("auditdt", properties.getProperty("auditdt"));
        newproperties.setProperty("propsmatch", properties.getProperty("propsmatch"));
        newproperties.setProperty(columnid.toLowerCase(), value);
        this.editSDIData(newproperties, tableid);
    }

    protected void loadSDIDataSets(String rsetid, DataSet datasets) throws SapphireException {
        String selectDatasets = "SELECT\tsdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset, sdidata.usersequence, sdidata.documentid, sdidata.s_datasetstatus  FROM\tsdidata, rsetitems WHERE\trsetitems.rsetid = ? AND \t\trsetitems.sdcid  = sdidata.sdcid AND \t\trsetitems.keyid1 = sdidata.keyid1 AND \t\trsetitems.keyid2 = sdidata.keyid2 AND \t\trsetitems.keyid3 = sdidata.keyid3 ORDER BY keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset desc";
        this.database.createPreparedResultSet("SelectDataSets", selectDatasets, new Object[]{rsetid});
        datasets.setResultSet(this.database.getResultSet("SelectDataSets"));
    }

    protected DataSet loadSDIDataSetsFromRsetItemDS(String rsetid) throws SapphireException {
        String selectDatasets = "SELECT\tsdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset, sdidata.usersequence, sdidata.documentid, sdidata.s_datasetstatus FROM\tsdidata, rsetitemsds WHERE\trsetitemsds.rsetid = ? AND \t\trsetitemsds.sdcid  = sdidata.sdcid AND \t\trsetitemsds.keyid1 = sdidata.keyid1 AND \t\trsetitemsds.keyid2 = sdidata.keyid2 AND \t\trsetitemsds.keyid3 = sdidata.keyid3 AND \t\trsetitemsds.paramlistid = sdidata.paramlistid AND \t\trsetitemsds.paramlistversionid = sdidata.paramlistversionid AND \t\trsetitemsds.variantid = sdidata.variantid AND \t\trsetitemsds.dataset = sdidata.dataset ";
        this.database.createPreparedResultSet("SelectDataSetsFromRsetitemds", selectDatasets, new Object[]{rsetid});
        DataSet dataSet = new DataSet();
        dataSet.setResultSet(this.database.getResultSet("SelectDataSetsFromRsetitemds"));
        return dataSet;
    }

    protected void loadParamList(String paramlistid, String paramlistversionid, String variantid, DataSet paramlist, DataSet paramlistitem, DataSet paramlimits, DataSet approvalsteps, DataSet attributes, DataSet paramlistcrosssdicalcs, String sdcid) throws SapphireException {
        this.logger.info("Loading paramlist details: " + paramlistid + "|" + paramlistversionid + "|" + variantid);
        boolean qcTransferFlagExist = this.checkQCTransferFlag(sdcid, paramlistid, paramlistversionid, variantid);
        String selectparamlist = "SELECT\tparamlist.*, sequenceflag, uniquenessflag, passrule FROM\tparamlist LEFT OUTER JOIN approvaltype ON paramlist.approvaltypeid = approvaltype.approvaltypeid WHERE\tparamlistid = ? AND \t\tparamlistversionid = ? AND \t\tvariantid = ?";
        String selectparamlistitems = "SELECT\t* FROM paramlistitem WHERE\tparamlistid = ? AND \t\tparamlistversionid = ? AND \t\tvariantid = ?" + (qcTransferFlagExist ? " and\tqctransferflag = 'Y'" : "");
        String selectparamlimits = "SELECT\t* FROM paramlimits WHERE\tparamlistid = ? AND \t\tparamlistversionid = ? AND \t\tvariantid = ?";
        String selectapprovalsteps = "SELECT\t* FROM approvaltypestep WHERE\tapprovaltypeid IN ( \t\tSELECT\tapprovaltypeid FROM paramlist \t\tWHERE\tparamlistid = ? AND \t\t\t\tparamlistversionid = ? AND \t\t\t\tvariantid = ? )";
        String selectattributes = "SELECT\t* FROM sdiattribute WHERE\tsdcid = 'ParamList' AND keyid1 =? AND keyid2 =? AND keyid3 = ?";
        String selectparamlistcrosssdicalc = "SELECT\t* FROM paramlistcrosssdicalc WHERE\tparamlistid = ? AND \t\tparamlistversionid = ? AND \t\tvariantid = ?";
        Object[] paramlistBindVars = new String[]{paramlistid, paramlistversionid, variantid};
        this.database.createPreparedResultSet("SelectParamList", selectparamlist, paramlistBindVars);
        paramlist.setResultSet(this.database.getResultSet("SelectParamList"));
        if (paramlist.size() == 1) {
            this.database.createPreparedResultSet("SelectParamListItems", selectparamlistitems, paramlistBindVars);
            paramlistitem.setResultSet(this.database.getResultSet("SelectParamListItems"));
            if (qcTransferFlagExist) {
                this.flushingCalculationForReagentAttribute(paramlistitem);
            }
            this.database.createPreparedResultSet("SelectParamLimits", selectparamlimits, paramlistBindVars);
            paramlimits.setResultSet(this.database.getResultSet("SelectParamLimits"));
            this.database.createPreparedResultSet("SelectApprovalSteps", selectapprovalsteps, paramlistBindVars);
            approvalsteps.setResultSet(this.database.getResultSet("SelectApprovalSteps"));
            if (attributes != null) {
                this.database.createPreparedResultSet("SelectAttributes", selectattributes, paramlistBindVars);
                attributes.setResultSet(this.database.getResultSet("SelectAttributes"));
            }
            this.database.createPreparedResultSet("SelectsdidataCrosssdiCalcRules", selectparamlistcrosssdicalc, paramlistBindVars);
            paramlistcrosssdicalcs.setResultSet(this.database.getResultSet("SelectsdidataCrosssdiCalcRules"));
        }
    }

    private void flushingCalculationForReagentAttribute(DataSet paramlistitem) {
        for (int row = 0; row < paramlistitem.size(); ++row) {
            String datatype = paramlistitem.getString(row, "datatypes", "");
            String calcrule = paramlistitem.getString(row, "calcrule", "");
            if (datatype.equalsIgnoreCase("NC")) {
                paramlistitem.setString(row, "datatypes", "N");
            } else if (datatype.equalsIgnoreCase("TC")) {
                paramlistitem.setString(row, "datatypes", "T");
            } else if (datatype.equalsIgnoreCase("DC")) {
                paramlistitem.setString(row, "datatypes", "D");
            } else if (datatype.equalsIgnoreCase("OC")) {
                paramlistitem.setString(row, "datatypes", "O");
            }
            if (calcrule.length() <= 0) continue;
            paramlistitem.setString(row, "calcrule", "");
        }
    }

    private boolean checkQCTransferFlag(String sdcid, String paramlistid, String paramlistversionid, String variantid) throws SapphireException {
        boolean qcTransferFlagExist = false;
        if ("LV_ReagentLot".equalsIgnoreCase(sdcid)) {
            String selectparamlistitems = "SELECT\tqctransferflag FROM paramlistitem WHERE\tparamlistid = ? AND \t\tparamlistversionid = ? AND \t\tvariantid = ?\tand\tqctransferflag = 'Y'";
            this.database.createPreparedResultSet("qctransferflag", selectparamlistitems, new String[]{paramlistid, paramlistversionid, variantid});
            if (this.database.getNext("qctransferflag")) {
                qcTransferFlagExist = true;
            }
        }
        return qcTransferFlagExist;
    }

    protected void addDataitems(DataSet dataitem, DataSet datalimits, DataSet paramlistitem, DataSet paramlimits, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, int datasetnum, String paramidlist, String paramtypelist, Calendar now, HashMap replicateMap, boolean enterDefaultValue) throws SapphireException {
        String[] paramids = StringUtil.split(paramidlist, ";");
        String[] paramtypes = StringUtil.split(paramtypelist, ";");
        boolean params = false;
        if (paramids.length > 0 && paramids[0].length() > 0 && paramtypes.length > 0 && paramids.length == paramtypes.length) {
            this.logger.info("Adding specified dataitems from paramlist: " + paramlistid + ": " + paramids.toString());
            params = true;
        } else {
            this.logger.info("Adding all dataitems from paramlist: " + paramlistid + " (Ver:" + paramlistversionid + "), " + variantid + " DataSet: " + datasetnum);
        }
        HashMap<String, String> limitfilter = new HashMap<String, String>();
        limitfilter.put("paramlistid", paramlistid);
        limitfilter.put("paramlistversionid", paramlistversionid);
        limitfilter.put("variantid", variantid);
        for (int pli = 0; pli < paramlistitem.size(); ++pli) {
            String repid;
            String paramid = paramlistitem.getString(pli, "paramid");
            String paramtype = paramlistitem.getString(pli, "paramtype");
            int reps = 0;
            if (params) {
                for (int i = 0; i < paramids.length && reps == 0; ++i) {
                    if (!paramids[i].equals(paramid) || !paramtypes[i].equals(paramtype)) continue;
                    reps = paramlistitem.getInt(pli, "numreplicates");
                }
            } else {
                reps = paramlistitem.getInt(pli, "numreplicates");
            }
            boolean itemNotExist = true;
            if (replicateMap != null && (repid = (String)replicateMap.get(keyid1 + keyid2 + keyid3 + paramlistid + paramlistversionid + variantid + datasetnum + paramid + paramtype)) != null && repid.length() > 0) {
                itemNotExist = false;
            }
            if (!itemNotExist) continue;
            for (int rep = 0; rep < reps; ++rep) {
                int newdi = dataitem.addRow();
                dataitem.setString(newdi, "sdcid", sdcid);
                dataitem.setString(newdi, "keyid1", keyid1);
                dataitem.setString(newdi, "keyid2", keyid2);
                dataitem.setString(newdi, "keyid3", keyid3);
                dataitem.setString(newdi, "paramlistid", paramlistid);
                dataitem.setString(newdi, "paramlistversionid", paramlistversionid);
                dataitem.setString(newdi, "variantid", variantid);
                dataitem.setNumber(newdi, "dataset", datasetnum);
                dataitem.setString(newdi, "paramid", paramid);
                dataitem.setString(newdi, "paramtype", paramtype);
                dataitem.setNumber(newdi, "replicateid", rep + 1);
                dataitem.setString(newdi, "aliasid", paramlistitem.getString(pli, "aliasid"));
                dataitem.setString(newdi, "datatypes", paramlistitem.getString(pli, "datatypes"));
                dataitem.setString(newdi, "mandatoryflag", paramlistitem.getString(pli, "mandatoryflag"));
                dataitem.setString(newdi, "displayunits", paramlistitem.getString(pli, "displayunits"));
                dataitem.setString(newdi, "displayformat", paramlistitem.getString(pli, "displayformat"));
                dataitem.setString(newdi, "operatorrule", paramlistitem.getString(pli, "operatorrule"));
                dataitem.setString(newdi, "transformdeferflag", paramlistitem.getString(pli, "transformdeferflag", "N"));
                dataitem.setString(newdi, "transformrule", paramlistitem.getString(pli, "transformrule"));
                dataitem.setString(newdi, "entrysdcid", paramlistitem.getString(pli, "entrysdcid"));
                dataitem.setString(newdi, "entryreftypeid", paramlistitem.getString(pli, "entryreftypeid"));
                dataitem.setString(newdi, "calcrule", paramlistitem.getString(pli, "calcrule"));
                dataitem.setNumber(newdi, "usersequence", paramlistitem.getBigDecimal(pli, "usersequence"));
                dataitem.setString(newdi, "releasedflag", "N");
                dataitem.setString(newdi, "measurementactionid", paramlistitem.getString(pli, "measurementactionid"));
                dataitem.setString(newdi, "instrumentfieldid", paramlistitem.getString(pli, "instrumentfieldid"));
                dataitem.setString(newdi, "uncertaintyfunction", paramlistitem.getString(pli, "uncertaintyfunction"));
                dataitem.setString(newdi, "uncertaintydisplayformat", paramlistitem.getString(pli, "uncertaintydisplayformat"));
                dataitem.setString(newdi, "uncertaintyfunctionupper", paramlistitem.getString(pli, "uncertaintyfunctionupper"));
                dataitem.setString(newdi, "uncertaintydisplayformatupper", paramlistitem.getString(pli, "uncertaintydisplayformatupper"));
                dataitem.setString(newdi, "uncertaintyasymmetricflag", paramlistitem.getString(pli, "uncertaintyasymmetricflag"));
                dataitem.setString(newdi, "reportflag", paramlistitem.getString(pli, "reportflag"));
                dataitem.setString(newdi, "qctransferflag", paramlistitem.getString(pli, "qctransferflag"));
                dataitem.setDate(newdi, "createdt", now);
                dataitem.setString(newdi, "createby", this.connectionInfo.getSysuserId());
                dataitem.setString(newdi, "createtool", this.connectionInfo.getTool());
                dataitem.setDate(newdi, "moddt", now);
                dataitem.setString(newdi, "modby", this.connectionInfo.getSysuserId());
                dataitem.setString(newdi, "modtool", this.connectionInfo.getTool());
                String defaultvalue = paramlistitem.getString(pli, "defaultvalue");
                dataitem.setString(newdi, "defaultvalue", defaultvalue);
                if (enterDefaultValue && defaultvalue != null && defaultvalue.length() > 0) {
                    dataitem.setString(newdi, "enteredtext", defaultvalue);
                    String datatypes = paramlistitem.getString(pli, "datatypes");
                    if (datatypes.equals("A")) {
                        try {
                            FormatUtil formatutil = FormatUtil.getInstance(this.connectionInfo);
                            BigDecimal test = formatutil.parseBigDecimal(defaultvalue);
                            datatypes = "N";
                        }
                        catch (NumberFormatException nfe) {
                            DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
                            Calendar c = dtu.getCalendar(defaultvalue);
                            String string = datatypes = c != null ? "D" : "T";
                        }
                    }
                    if (datatypes.equals("N")) {
                        if (paramlistitem.getString(pli, "transformrule") == null || paramlistitem.getString(pli, "transformrule").length() == 0) {
                            String numtext = defaultvalue;
                            try {
                                dataitem.setString(newdi, "displayvalue", defaultvalue);
                                dataitem.setString(newdi, "enteredunits", paramlistitem.getString(pli, "displayunits"));
                                BigDecimal value = FormatUtil.getInstance().parseBigDecimal(defaultvalue);
                                dataitem.setNumber(newdi, "enteredvalue", value);
                                dataitem.setNumber(newdi, "transformvalue", value);
                            }
                            catch (Exception e) {
                                switch (defaultvalue.charAt(0)) {
                                    case '<': 
                                    case '>': {
                                        if (defaultvalue.length() > 1 && defaultvalue.charAt(1) == '=') {
                                            numtext = defaultvalue.substring(2);
                                            break;
                                        }
                                        numtext = defaultvalue.substring(1);
                                        break;
                                    }
                                    case '=': {
                                        numtext = defaultvalue.substring(1);
                                    }
                                }
                                try {
                                    dataitem.setNumber(newdi, "enteredvalue", FormatUtil.getInstance().parseBigDecimal(numtext));
                                    dataitem.setNumber(newdi, "transformvalue", FormatUtil.getInstance().parseBigDecimal(numtext));
                                }
                                catch (NumberFormatException numberFormatException) {}
                            }
                        }
                    } else if (datatypes.equals("T")) {
                        dataitem.setString(newdi, "transformtext", defaultvalue);
                        dataitem.setString(newdi, "displayvalue", defaultvalue);
                    } else if (datatypes.equals("R") || datatypes.equals("V") || datatypes.equals("S")) {
                        dataitem.setString(newdi, "transformtext", defaultvalue);
                        dataitem.setString(newdi, "displayvalue", defaultvalue);
                        dataitem.setString(newdi, "enteredunits", paramlistitem.getString(pli, "displayunits"));
                    } else if (datatypes.equals("D") || datatypes.equals("O")) {
                        try {
                            Calendar c = new DateTimeUtil().getCalendar(defaultvalue);
                            if (c != null) {
                                dataitem.setDate(newdi, "transformdt", c);
                                dataitem.setString(newdi, "displayvalue", defaultvalue);
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                limitfilter.put("paramid", paramid);
                limitfilter.put("paramtype", paramtype);
                DataSet filteredlimits = paramlimits.getFilteredDataSet(limitfilter);
                this.addDataitemLimits(datalimits, filteredlimits, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, datasetnum, paramid, paramtype, rep + 1);
            }
        }
    }

    protected void addDataitemLimits(DataSet datalimits, DataSet limits, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, int datasetnum, String paramid, String paramtype, int rep) {
        if (limits.getRowCount() > 0) {
            this.logger.debug("Adding datalimit details...");
            for (int i = 0; i < limits.size(); ++i) {
                int newRow = datalimits.addRow();
                datalimits.setString(newRow, "sdcid", sdcid);
                datalimits.setString(newRow, "keyid1", keyid1);
                datalimits.setString(newRow, "keyid2", keyid2);
                datalimits.setString(newRow, "keyid3", keyid3);
                datalimits.setString(newRow, "paramlistid", paramlistid);
                datalimits.setString(newRow, "paramlistversionid", paramlistversionid);
                datalimits.setString(newRow, "variantid", variantid);
                datalimits.setNumber(newRow, "dataset", datasetnum);
                datalimits.setString(newRow, "paramid", paramid);
                datalimits.setString(newRow, "paramtype", paramtype);
                datalimits.setNumber(newRow, "replicateid", rep);
                datalimits.setString(newRow, "limittypeid", limits.getString(i, "limittypeid"));
                datalimits.setString(newRow, "operator", limits.getString(i, "operator"));
                datalimits.setString(newRow, "value1", limits.getString(i, "value1"));
                datalimits.setString(newRow, "value2", limits.getString(i, "value2"));
                datalimits.setNumber(newRow, "value1num", limits.getBigDecimal(i, "value1num"));
                datalimits.setNumber(newRow, "value2num", limits.getBigDecimal(i, "value2num"));
                datalimits.setString(newRow, "unitsid", limits.getString(i, "unitsid"));
                datalimits.setString(newRow, "limitfailedactionid", limits.getString(i, "limitfailedactionid"));
            }
        }
    }

    protected String getRSet(String sdcid, String keyid1, String keyid2, String keyid3, boolean applylock) throws SapphireException {
        String rsetid = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, applylock);
        return rsetid;
    }

    public static String createRSet(String sdcid, String keyid1, String keyid2, String keyid3, DBAccess database, ConnectionInfo connectionInfo, boolean applylock) throws SapphireException {
        String rsetid = "";
        try {
            SapphireConnection sapphireConnection = new SapphireConnection(database.getConnection(), connectionInfo);
            DataAccessService das = new DataAccessService(sapphireConnection);
            boolean isDataSetSecurity = "D".equals(new DDTService(sapphireConnection).getSDCProperties("DataSet").getProperty("accesscontrolledflag"));
            RSet rset = das.createRSet(sdcid, keyid1, keyid2, keyid3, false, isDataSetSecurity ? 1 : 0);
            if (applylock) {
                rset.setRSet(das.lockRSet(rset, "DA", 3));
            }
            rsetid = rset.getRsetid();
        }
        catch (ServiceException se) {
            Trace.logError("Failed to createRset for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3, se);
            throw new SapphireException(ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())));
        }
        return rsetid;
    }

    public static String createBypassSecurityRSet(String sdcid, String keyid1, String keyid2, String keyid3, DBAccess database, ConnectionInfo connectionInfo, boolean applylock) throws SapphireException {
        String rsetid = "";
        try {
            SapphireConnection sapphireConnection = new SapphireConnection(database.getConnection(), connectionInfo);
            DataAccessService das = new DataAccessService(sapphireConnection);
            RSet rset = das.createRSet(sdcid, keyid1, keyid2, keyid3, false, 1);
            if (applylock) {
                rset.setRSet(das.lockRSet(rset, "DA", 3));
            }
            rsetid = rset.getRsetid();
        }
        catch (ServiceException se) {
            Trace.logError("Failed to createRset for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3, se);
            throw new SapphireException(ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())));
        }
        return rsetid;
    }

    protected String getTracelogid(String sdcid, String desc, String auditReason, String auditActivity, String auditSignedFlag, String auditDt) throws SapphireException {
        String tracelogid = null;
        if (auditReason.length() > 0) {
            this.logger.info("Generate the tracelog record");
            PropertyList tracelogprops = new PropertyList();
            tracelogprops.setProperty("sdcid", sdcid);
            tracelogprops.setProperty("description", desc);
            tracelogprops.setProperty("auditreason", auditReason);
            tracelogprops.setProperty("auditactivity", auditActivity);
            tracelogprops.setProperty("auditsignedflag", auditSignedFlag);
            tracelogprops.setProperty("auditdt", auditDt);
            ActionProcessor ap = this.getActionProcessor();
            ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
            tracelogid = (String)tracelogprops.get("tracelogid");
        }
        return tracelogid;
    }

    protected String getAllDSRSet(String sdcid, String keyid1, String keyid2, String keyid3, boolean applylock) throws SapphireException {
        DAMProcessor dam = this.getDAMProcessor();
        String rsetid = dam.createRSetDS(sdcid, keyid1, keyid2, keyid3, "", "", "", "", false, true, false);
        if (rsetid.length() == 0) {
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create rset for " + new SDI(sdcid, keyid1, keyid2, keyid3).getKeyText() + " and it's datasets");
        }
        if (applylock) {
            rsetid = dam.lockRSet(rsetid);
        }
        return rsetid;
    }

    protected void deleteSDIData(PropertyList properties, String rsetid, boolean deleteds, boolean deletedi, boolean deletedl, boolean deleteda, String tracelogtable) throws SapphireException {
        int maxprops;
        boolean forcedelete = !properties.getProperty("forcedelete").equals("N");
        String old = properties.getProperty("old");
        this.runOld = "Y".equalsIgnoreCase(old);
        String[] paramlistidprop = StringUtil.split(properties.getProperty("paramlistid"), ";");
        String[] paramlistversionidprop = StringUtil.split(properties.getProperty("paramlistversionid"), ";");
        String[] variantidprop = StringUtil.split(properties.getProperty("variantid"), ";");
        String[] datasetprop = StringUtil.split(properties.getProperty("dataset"), ";");
        String[] paramidprop = StringUtil.split(properties.getProperty("paramid"), ";");
        String[] paramtypeprop = StringUtil.split(properties.getProperty("paramtype"), ";");
        String[] replicateidprop = StringUtil.split(properties.getProperty("replicateid"), ";");
        String[] limittypeidprop = StringUtil.split(properties.getProperty("limittypeid"), ";");
        String[] approvalstepprop = StringUtil.split(properties.getProperty("approvalstep"), ";");
        String reason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity", "");
        String auditSignedFlag = properties.getProperty("auditsignedflag", "N");
        String auditdt = properties.getProperty("auditdt");
        String tracelogid = properties.getProperty("tracelogid", "").trim();
        if (tracelogid.length() == 0 && reason.length() > 0) {
            this.logger.info("Generate the tracelog record");
            PropertyList tracelogprops = new PropertyList();
            tracelogprops.setProperty("sdcid", properties.getProperty("sdcid"));
            tracelogprops.setProperty("keyid1", properties.getProperty("keyid1"));
            tracelogprops.setProperty("keyid2", properties.getProperty("keyid2"));
            tracelogprops.setProperty("keyid3", properties.getProperty("keyid3"));
            tracelogprops.setProperty("description", "Deleted data from " + tracelogtable);
            tracelogprops.setProperty("auditreason", reason);
            tracelogprops.setProperty("auditactivity", auditActivity);
            tracelogprops.setProperty("auditsignedflag", auditSignedFlag);
            tracelogprops.setProperty("auditdt", auditdt);
            ActionProcessor ap = this.getActionProcessor();
            ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
            tracelogid = (String)tracelogprops.get("tracelogid");
            properties.setProperty("tracelogid", tracelogid);
        }
        if ((maxprops = Math.max(paramlistidprop.length, Math.max(paramlistversionidprop.length, Math.max(variantidprop.length, Math.max(datasetprop.length, Math.max(paramidprop.length, Math.max(paramtypeprop.length, Math.max(replicateidprop.length, Math.max(limittypeidprop.length, approvalstepprop.length))))))))) > 0) {
            this.logger.info("Deleting specified data...");
            for (int i = 0; i < maxprops; ++i) {
                SafeSQL dldeleteSafeSQL = new SafeSQL();
                String dldelete = "DELETE FROM sdidataitemlimits WHERE " + this.getSDIClause("sdidataitemlimits", rsetid, dldeleteSafeSQL);
                SafeSQL dideleteSafeSQL = new SafeSQL();
                String didelete = "DELETE FROM sdidataitem WHERE " + this.getSDIClause("sdidataitem", rsetid, dideleteSafeSQL);
                SafeSQL dadeleteSafeSQL = new SafeSQL();
                String dadelete = "DELETE FROM sdidataapproval WHERE " + this.getSDIClause("sdidataapproval", rsetid, dadeleteSafeSQL);
                SafeSQL dsdeleteSafeSQL = new SafeSQL();
                String dsdelete = "DELETE FROM sdidata WHERE " + this.getSDIClause("sdidata", rsetid, dsdeleteSafeSQL);
                SafeSQL drdeleteSafeSQL = new SafeSQL();
                String drdelete = "DELETE FROM sdidatarelation WHERE " + this.getSDIClause("sdidatarelation", rsetid, drdeleteSafeSQL);
                SafeSQL dispecdeleteSafeSQL = new SafeSQL();
                String dispecdelete = "DELETE FROM sdidataitemspec WHERE " + this.getSDIClause("sdidataitemspec", rsetid, dispecdeleteSafeSQL);
                StringBuffer deletedlbuff = new StringBuffer(dldelete);
                StringBuffer deletedibuff = new StringBuffer(didelete);
                StringBuffer deletedabuff = new StringBuffer(dadelete);
                StringBuffer deletedsbuff = new StringBuffer(dsdelete);
                StringBuffer deletedrbuff = new StringBuffer(drdelete);
                StringBuffer deletedispecbuff = new StringBuffer(dispecdelete);
                if (i < paramlistidprop.length && paramlistidprop[i].length() > 0 || paramlistidprop.length == 1 && paramlistidprop[0].length() > 0) {
                    if (deletedl) {
                        deletedlbuff.append(" AND paramlistid = ").append(dldeleteSafeSQL.addVar(paramlistidprop[paramlistidprop.length == 1 ? 0 : i]));
                    }
                    if (deletedi) {
                        deletedibuff.append(" AND paramlistid = ").append(dideleteSafeSQL.addVar(paramlistidprop[paramlistidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteda) {
                        deletedabuff.append(" AND paramlistid = ").append(dadeleteSafeSQL.addVar(paramlistidprop[paramlistidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteds) {
                        deletedsbuff.append(" AND paramlistid = ").append(dsdeleteSafeSQL.addVar(paramlistidprop[paramlistidprop.length == 1 ? 0 : i]));
                        deletedrbuff.append(" AND paramlistid = ").append(drdeleteSafeSQL.addVar(paramlistidprop[paramlistidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteds || deletedi) {
                        deletedispecbuff.append(" AND paramlistid = ").append(dispecdeleteSafeSQL.addVar(paramlistidprop[paramlistidprop.length == 1 ? 0 : i]));
                    }
                }
                if (i < paramlistversionidprop.length && paramlistversionidprop[i].length() > 0 || paramlistversionidprop.length == 1 && paramlistversionidprop[0].length() > 0) {
                    if (deletedl) {
                        deletedlbuff.append(" AND paramlistversionid = ").append(dldeleteSafeSQL.addVar(paramlistversionidprop[paramlistversionidprop.length == 1 ? 0 : i]));
                    }
                    if (deletedi) {
                        deletedibuff.append(" AND paramlistversionid = ").append(dideleteSafeSQL.addVar(paramlistversionidprop[paramlistversionidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteda) {
                        deletedabuff.append(" AND paramlistversionid = ").append(dadeleteSafeSQL.addVar(paramlistversionidprop[paramlistversionidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteds) {
                        deletedsbuff.append(" AND paramlistversionid = ").append(dsdeleteSafeSQL.addVar(paramlistversionidprop[paramlistversionidprop.length == 1 ? 0 : i]));
                        deletedrbuff.append(" AND paramlistversionid = ").append(drdeleteSafeSQL.addVar(paramlistversionidprop[paramlistversionidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteds || deletedi) {
                        deletedispecbuff.append("  AND paramlistversionid = ").append(dispecdeleteSafeSQL.addVar(paramlistversionidprop[paramlistversionidprop.length == 1 ? 0 : i]));
                    }
                }
                if (i < variantidprop.length && variantidprop[i].length() > 0 || variantidprop.length == 1 && variantidprop[0].length() > 0) {
                    if (deletedl) {
                        deletedlbuff.append(" AND variantid = ").append(dldeleteSafeSQL.addVar(variantidprop[variantidprop.length == 1 ? 0 : i]));
                    }
                    if (deletedi) {
                        deletedibuff.append(" AND variantid = ").append(dideleteSafeSQL.addVar(variantidprop[variantidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteda) {
                        deletedabuff.append(" AND variantid = ").append(dadeleteSafeSQL.addVar(variantidprop[variantidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteds) {
                        deletedsbuff.append(" AND variantid = ").append(dsdeleteSafeSQL.addVar(variantidprop[variantidprop.length == 1 ? 0 : i]));
                        deletedrbuff.append(" AND variantid = ").append(drdeleteSafeSQL.addVar(variantidprop[variantidprop.length == 1 ? 0 : i]));
                    }
                    if (deleteds || deletedi) {
                        deletedispecbuff.append(" AND variantid = ").append(dispecdeleteSafeSQL.addVar(variantidprop[variantidprop.length == 1 ? 0 : i]));
                    }
                }
                if (i < datasetprop.length && datasetprop[i].length() > 0 || datasetprop.length == 1 && datasetprop[0].length() > 0) {
                    if (deletedl) {
                        deletedlbuff.append(" AND dataset = ").append(dldeleteSafeSQL.addVar(datasetprop[datasetprop.length == 1 ? 0 : i]));
                    }
                    if (deletedi) {
                        deletedibuff.append(" AND dataset = ").append(dideleteSafeSQL.addVar(datasetprop[datasetprop.length == 1 ? 0 : i]));
                    }
                    if (deleteda) {
                        deletedabuff.append(" AND dataset = ").append(dadeleteSafeSQL.addVar(datasetprop[datasetprop.length == 1 ? 0 : i]));
                    }
                    if (deleteds) {
                        deletedsbuff.append(" AND dataset = ").append(dsdeleteSafeSQL.addVar(datasetprop[datasetprop.length == 1 ? 0 : i]));
                        deletedrbuff.append(" AND dataset = ").append(drdeleteSafeSQL.addVar(datasetprop[datasetprop.length == 1 ? 0 : i]));
                    }
                    if (deleteds || deletedi) {
                        deletedispecbuff.append(" AND dataset = ").append(dispecdeleteSafeSQL.addVar(datasetprop[datasetprop.length == 1 ? 0 : i]));
                    }
                }
                if (i < paramidprop.length && paramidprop[i].length() > 0) {
                    if (deletedl) {
                        deletedlbuff.append(" AND paramid = ").append(dldeleteSafeSQL.addVar(paramidprop[i]));
                    }
                    if (deletedi) {
                        deletedibuff.append(" AND paramid = ").append(dideleteSafeSQL.addVar(paramidprop[i]));
                        deletedispecbuff.append(" AND paramid = ").append(dispecdeleteSafeSQL.addVar(paramidprop[i]));
                    }
                }
                if (i < paramtypeprop.length && paramtypeprop[i].length() > 0) {
                    if (deletedl) {
                        deletedlbuff.append(" AND paramtype = ").append(dldeleteSafeSQL.addVar(paramtypeprop[i]));
                    }
                    if (deletedi) {
                        deletedibuff.append(" AND paramtype = ").append(dideleteSafeSQL.addVar(paramtypeprop[i]));
                        deletedispecbuff.append(" AND paramtype = ").append(dispecdeleteSafeSQL.addVar(paramtypeprop[i]));
                    }
                }
                if (i < replicateidprop.length && replicateidprop[i].length() > 0) {
                    if (deletedl) {
                        deletedlbuff.append(" AND replicateid = ").append(dldeleteSafeSQL.addVar(replicateidprop[i]));
                    }
                    if (deletedi) {
                        deletedibuff.append(" AND replicateid = ").append(dideleteSafeSQL.addVar(replicateidprop[i]));
                        deletedispecbuff.append(" AND replicateid = ").append(dispecdeleteSafeSQL.addVar(replicateidprop[i]));
                    }
                }
                if (i < limittypeidprop.length && limittypeidprop[i].length() > 0 && deletedl) {
                    deletedlbuff.append(" AND limittypeid = ").append(dldeleteSafeSQL.addVar(limittypeidprop[i]));
                }
                if (i < approvalstepprop.length && approvalstepprop[i].length() > 0 && deleteda) {
                    deletedabuff.append(" AND approvalstep = ").append(dadeleteSafeSQL.addVar(approvalstepprop[i]));
                }
                if (deleteds && !forcedelete) {
                    String dsCheck = "SELECT enteredtext FROM sdidataitem " + deletedibuff.substring(deletedibuff.indexOf("WHERE")) + " AND enteredtext is not null AND enteredtext != '(null)'";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(dsCheck.toString(), dideleteSafeSQL.getValues());
                    ValidateDataEntered.removeBlankEnteredtextRows(ds);
                    if (ds != null && ds.size() > 0) {
                        throw new SapphireException("DATASET_HAS_RESULTS", "One or more of the datasets marked for deletion contains data.");
                    }
                }
                if (!deleteds) {
                    deletedsbuff.delete(0, deletedsbuff.length());
                    deletedrbuff.delete(0, deletedrbuff.length());
                }
                if (!deletedi) {
                    deletedibuff.delete(0, deletedibuff.length());
                }
                if (!deletedl) {
                    deletedlbuff.delete(0, deletedlbuff.length());
                }
                if (!deleteda) {
                    deletedabuff.delete(0, deletedabuff.length());
                }
                if (!deleteds && !deletedi) {
                    deletedispecbuff.delete(0, deletedispecbuff.length());
                }
                this.deleteTableData(deletedsbuff.toString(), dsdeleteSafeSQL, deletedrbuff.toString(), drdeleteSafeSQL, deletedibuff.toString(), dideleteSafeSQL, deletedlbuff.toString(), dldeleteSafeSQL, deletedabuff.toString(), dadeleteSafeSQL, deletedispecbuff.toString(), dispecdeleteSafeSQL, tracelogid);
            }
        } else {
            this.logger.info("Deleting all data...");
            SafeSQL dldeleteSafeSQL = new SafeSQL();
            String dldelete = "DELETE FROM sdidataitemlimits WHERE " + this.getSDIClause("sdidataitemlimits", rsetid, dldeleteSafeSQL);
            SafeSQL dideleteSafeSQL = new SafeSQL();
            String didelete = "DELETE FROM sdidataitem WHERE " + this.getSDIClause("sdidataitem", rsetid, dideleteSafeSQL);
            SafeSQL dadeleteSafeSQL = new SafeSQL();
            String dadelete = "DELETE FROM sdidataapproval WHERE " + this.getSDIClause("sdidataapproval", rsetid, dadeleteSafeSQL);
            SafeSQL dsdeleteSafeSQL = new SafeSQL();
            String dsdelete = "DELETE FROM sdidata WHERE " + this.getSDIClause("sdidata", rsetid, dsdeleteSafeSQL);
            SafeSQL drdeleteSafeSQL = new SafeSQL();
            String drdelete = "DELETE FROM sdidatarelation WHERE " + this.getSDIClause("sdidatarelation", rsetid, drdeleteSafeSQL);
            SafeSQL dispecdeleteSafeSQL = new SafeSQL();
            String dispecdelete = "DELETE FROM sdidataitemspec WHERE " + this.getSDIClause("sdidataitemspec", rsetid, dispecdeleteSafeSQL);
            if (!deleteds) {
                dsdelete = "";
                drdelete = "";
            }
            if (!deletedi) {
                didelete = "";
            }
            if (!deletedl) {
                dldelete = "";
            }
            if (!deleteda) {
                dadelete = "";
            }
            if (!deleteds && !deletedi) {
                dispecdelete = "";
            }
            this.deleteTableData(dsdelete, dsdeleteSafeSQL, drdelete, drdeleteSafeSQL, didelete, dideleteSafeSQL, dldelete, dldeleteSafeSQL, dadelete, dadeleteSafeSQL, dispecdelete, dispecdeleteSafeSQL, tracelogid);
        }
    }

    private void deleteTableData(String deleteds, SafeSQL deletedsSafeSQL, String deletedr, SafeSQL deletedrSafeSQL, String deletedi, SafeSQL deletediSafeSQL, String deletedl, SafeSQL deletedlSafeSQL, String deleteda, SafeSQL deletedaSafeSQL, String deletedispec, SafeSQL deletedispecSafeSQL, String tracelogid) throws SapphireException {
        ArrayList<Object> varList;
        String str;
        Object[] bindvars;
        if (deletedl.length() > 0 && this.database.executePreparedUpdate(deletedl, bindvars = deletedlSafeSQL.getValues()) > 0 && tracelogid.length() > 0) {
            String str2 = deletedl.substring(deletedl.indexOf(" WHERE "));
            str2 = str2.replaceAll("sdidataitemlimits", "a_sdidataitemlimits");
            ArrayList<Object> varList2 = new ArrayList<Object>();
            if (tracelogid.length() > 0) {
                varList2.add(tracelogid);
            }
            varList2.addAll(Arrays.asList(deletedlSafeSQL.getValues()));
            varList2.addAll(Arrays.asList(deletedlSafeSQL.getValues()));
            Object[] vars = varList2.toArray();
            this.database.executePreparedUpdate("UPDATE a_sdidataitemlimits SET tracelogid = ?" + str2 + " AND tracelogid = 'DELETED' AND auditsequence = ( SELECT max( auditsequence ) FROM a_sdidataitemlimits " + str2 + " AND tracelogid = 'DELETED' )", varList2.toArray());
        }
        if (deletedi.length() > 0 && this.database.executePreparedUpdate(deletedi, deletediSafeSQL.getValues()) > 0) {
            str = deletedi.substring(deletedi.indexOf(" WHERE "));
            str = str.replaceAll("sdidataitem", "a_sdidataitem");
            varList = new ArrayList<Object>();
            if (tracelogid.length() > 0) {
                varList.add(tracelogid);
            }
            varList.addAll(Arrays.asList(deletediSafeSQL.getValues()));
            varList.addAll(Arrays.asList(deletediSafeSQL.getValues()));
            Object[] vars = varList.toArray();
            this.database.executePreparedUpdate("UPDATE a_sdidataitem SET modtool = 'DeleteDataItem'" + (tracelogid.length() > 0 ? ", tracelogid = ?" : "") + str + " AND tracelogid = 'DELETED' AND auditsequence = ( SELECT max( auditsequence ) FROM a_sdidataitem " + str + " AND tracelogid = 'DELETED' )", varList.toArray());
        }
        if (deleteda.length() > 0 && this.database.executePreparedUpdate(deleteda, deletedaSafeSQL.getValues()) > 0) {
            str = deleteda.substring(deleteda.indexOf(" WHERE "));
            str = str.replaceAll("sdidataapproval", "a_sdidataapproval");
            varList = new ArrayList();
            if (tracelogid.length() > 0) {
                varList.add(tracelogid);
            }
            varList.addAll(Arrays.asList(deletedaSafeSQL.getValues()));
            varList.addAll(Arrays.asList(deletedaSafeSQL.getValues()));
            this.database.executePreparedUpdate("UPDATE a_sdidataapproval SET modtool = 'DeleteDataApproval'" + (tracelogid.length() > 0 ? ", tracelogid = ?" : "") + str + " AND tracelogid = 'DELETED' AND auditsequence = ( SELECT max( auditsequence ) FROM a_sdidataapproval " + str + " AND tracelogid = 'DELETED' )", varList.toArray());
        }
        if (deletedr.length() > 0) {
            this.database.executePreparedUpdate(deletedr, deletedrSafeSQL.getValues());
        }
        if (deleteds.length() > 0) {
            StringBuffer selParentSdiWiiSQL = new StringBuffer();
            selParentSdiWiiSQL.append(" SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid,").append(" dataset, sourceworkitemid, sourceworkiteminstance FROM  sdidata ").append(deleteds.substring(deleteds.indexOf(" WHERE "))).append(" AND sourceworkitemid is not null ");
            DataSet dsSDIData = this.getQueryProcessor().getPreparedSqlDataSet(selParentSdiWiiSQL.toString(), deletedsSafeSQL.getValues());
            StringBuffer selQCDataSets = new StringBuffer();
            selQCDataSets.append(" SELECT s_qcbatchid, s_qcbatchitemid FROM  sdidata ").append(deleteds.substring(deleteds.indexOf(" WHERE "))).append(" AND s_qcbatchid is not null");
            DataSet dsQCDataSets = this.getQueryProcessor().getPreparedSqlDataSet(selQCDataSets.toString(), deletedsSafeSQL.getValues());
            StringBuffer selActivityWorkSDIs = new StringBuffer();
            selActivityWorkSDIs.append("select * from activityworksdi WHERE worksdcid = 'DataSet' AND workkeyid1 IN ").append("(SELECT sdidataid FROM sdidata ").append(deleteds.substring(deleteds.indexOf(" WHERE "))).append(")");
            DataSet dsActivityWorkSDIS = this.getQueryProcessor().getPreparedSqlDataSet(selActivityWorkSDIs.toString(), deletedsSafeSQL.getValues());
            if (this.database.executePreparedUpdate(deleteds, deletedsSafeSQL.getValues()) > 0) {
                String str3 = deleteds.substring(deleteds.indexOf(" WHERE "));
                str3 = str3.replaceAll("sdidata", "a_sdidata");
                ArrayList<Object> varList3 = new ArrayList<Object>();
                if (tracelogid.length() > 0) {
                    varList3.add(tracelogid);
                }
                varList3.addAll(Arrays.asList(deletedsSafeSQL.getValues()));
                varList3.addAll(Arrays.asList(deletedsSafeSQL.getValues()));
                this.database.executePreparedUpdate("UPDATE a_sdidata SET modtool = 'DeleteDataSet'" + (tracelogid.length() > 0 ? ", tracelogid = ?" : "") + str3 + " AND tracelogid = 'DELETED' AND auditsequence = ( SELECT max( auditsequence ) FROM a_sdidata " + str3 + " AND tracelogid = 'DELETED' )", varList3.toArray());
                try {
                    PreparedStatement delSDIWII = this.database.prepareStatement(" DELETE FROM sdiworkitemitem  WHERE sdcid=? AND keyid1=? AND  keyid2=? AND keyid3=? AND workitemid=?   AND itemsdcid='ParamList' AND itemkeyid1=? AND itemkeyid2=? AND itemkeyid3=? AND iteminstance=? ");
                    for (int k = 0; k < dsSDIData.getRowCount(); ++k) {
                        delSDIWII.setString(1, dsSDIData.getString(k, "sdcid"));
                        delSDIWII.setString(2, dsSDIData.getString(k, "keyid1"));
                        delSDIWII.setString(3, dsSDIData.getString(k, "keyid2"));
                        delSDIWII.setString(4, dsSDIData.getString(k, "keyid3"));
                        delSDIWII.setString(5, dsSDIData.getString(k, "sourceworkitemid"));
                        delSDIWII.setString(6, dsSDIData.getString(k, "paramlistid"));
                        delSDIWII.setString(7, dsSDIData.getString(k, "paramlistversionid"));
                        delSDIWII.setString(8, dsSDIData.getString(k, "variantid"));
                        delSDIWII.setInt(9, dsSDIData.getInt(k, "dataset"));
                        delSDIWII.executeUpdate();
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to delete sdiworkitemitem reference of the dataset", e);
                }
                try {
                    if (dsSDIData.getRowCount() > 0) {
                        ActionProcessor ap = this.getActionProcessor();
                        PreparedStatement getSDIWINoSDIWII = this.database.prepareStatement(" SELECT sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance FROM sdiworkitem  WHERE sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? AND NOT EXISTS (SELECT 1 FROM sdiworkitemitem WHERE  sdiworkitemitem.sdcid = sdiworkitem.sdcid AND sdiworkitemitem.keyid1 = sdiworkitem.keyid1 AND sdiworkitemitem.keyid2 = sdiworkitem.keyid2 AND sdiworkitemitem.keyid3 = sdiworkitem.keyid3  AND sdiworkitemitem.workitemid = sdiworkitem.workitemid AND sdiworkitemitem.workiteminstance = sdiworkitem.workiteminstance  )");
                        DataSet deleteSDIWI = new DataSet();
                        for (int k = 0; k < dsSDIData.getRowCount(); ++k) {
                            String sdiwi_sdcId = dsSDIData.getString(k, "sdcid");
                            String sdiwi_keyid1 = dsSDIData.getString(k, "keyid1");
                            String sdiwi_keyid2 = dsSDIData.getString(k, "keyid2");
                            String sdiwi_keyid3 = dsSDIData.getString(k, "keyid3");
                            String sdiwi_workitemid = dsSDIData.getString(k, "sourceworkitemid");
                            getSDIWINoSDIWII.setString(1, sdiwi_sdcId);
                            getSDIWINoSDIWII.setString(2, sdiwi_keyid1);
                            getSDIWINoSDIWII.setString(3, sdiwi_keyid2);
                            getSDIWINoSDIWII.setString(4, sdiwi_keyid3);
                            getSDIWINoSDIWII.setString(5, sdiwi_workitemid);
                            DataSet dsSDIWI = new DataSet(getSDIWINoSDIWII.executeQuery());
                            for (int d = 0; d < dsSDIWI.getRowCount(); ++d) {
                                HashMap<String, Object> find = new HashMap<String, Object>();
                                find.put("sdcid", dsSDIWI.getValue(d, "sdcid"));
                                find.put("keyid1", dsSDIWI.getValue(d, "keyid1"));
                                find.put("keyid2", dsSDIWI.getValue(d, "keyid2"));
                                find.put("keyid3", dsSDIWI.getValue(d, "keyid3"));
                                find.put("workitemid", dsSDIWI.getValue(d, "workitemid"));
                                find.put("workiteminstance", new BigDecimal(dsSDIWI.getValue(d, "workiteminstance")));
                                if (deleteSDIWI.findRow(find) >= 0) continue;
                                deleteSDIWI.copyRow(dsSDIWI, d, 1);
                            }
                        }
                        if (deleteSDIWI.getRowCount() > 0) {
                            PropertyList delSDIWI = new PropertyList();
                            delSDIWI.setProperty("sdcid", deleteSDIWI.getValue(0, "sdcid"));
                            delSDIWI.setProperty("keyid1", deleteSDIWI.getColumnValues("keyid1", ";"));
                            delSDIWI.setProperty("keyid2", deleteSDIWI.getColumnValues("keyid2", ";"));
                            delSDIWI.setProperty("keyid3", deleteSDIWI.getColumnValues("keyid2", ";"));
                            delSDIWI.setProperty("workitemid", deleteSDIWI.getColumnValues("workitemid", ";"));
                            delSDIWI.setProperty("workiteminstance", deleteSDIWI.getColumnValues("workiteminstance", ";"));
                            ap.processAction("DeleteSDIWorkItem", "1", delSDIWI);
                        }
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to delete sdiworkitem reference when the last dataset deleted", e);
                }
                PreparedStatement delQCBI = this.database.prepareStatement("DELETE FROM s_qcbatchitem WHERE s_qcbatchitem.s_qcbatchid = ?  and s_qcbatchitem.s_qcbatchitemid = ?  AND NOT EXISTS(SELECT 1 FROM sdidata WHERE sdidata.s_qcbatchid = s_qcbatchitem.s_qcbatchid AND sdidata.s_qcbatchitemid = s_qcbatchitem.s_qcbatchitemid )");
                StringBuffer qcBatches = new StringBuffer();
                try {
                    for (int k = 0; k < dsQCDataSets.getRowCount(); ++k) {
                        String qcBatchId = dsQCDataSets.getString(k, "s_qcbatchid", "");
                        delQCBI.setString(1, qcBatchId);
                        delQCBI.setString(2, dsQCDataSets.getString(k, "s_qcbatchitemid", ""));
                        if (delQCBI.executeUpdate() <= 0) continue;
                        qcBatches.append(";").append(qcBatchId);
                    }
                    if (qcBatches.length() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("qcbatchid", qcBatches.substring(1));
                        props.setProperty("tracelogid", tracelogid);
                        this.getActionProcessor().processAction("UpdateQCBatchStatus", "1", props);
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to delete qcbatchitem reference of the dataset", e);
                }
                if (dsActivityWorkSDIS.getRowCount() > 0) {
                    dsActivityWorkSDIS.sort("activityid");
                    ArrayList<DataSet> activityGrps = dsActivityWorkSDIS.getGroupedDataSets("activityid");
                    for (int g = 0; g < activityGrps.size(); ++g) {
                        DataSet activityWorkSDIs = activityGrps.get(g);
                        PropertyList props = new PropertyList();
                        props.setProperty("activityid", activityWorkSDIs.getValue(0, "activityid"));
                        props.setProperty("worksdcid", "DataSet");
                        props.setProperty("workkeyid1", activityWorkSDIs.getColumnValues("workkeyid1", ";"));
                        this.getActionProcessor().processActionClass(RemoveActivityWorkSDI.class.getName(), props);
                    }
                }
            }
        }
        if (deletedispec.length() > 0 && this.database.executePreparedUpdate(deletedispec, deletedispecSafeSQL.getValues()) > 0) {
            str = deletedispec.substring(deletedispec.indexOf(" WHERE "));
            str = str.replaceAll("sdidataitemspec", "a_sdidataitemspec");
            varList = new ArrayList();
            if (tracelogid.length() > 0) {
                varList.add(tracelogid);
            }
            varList.addAll(Arrays.asList(deletedispecSafeSQL.getValues()));
            varList.addAll(Arrays.asList(deletedispecSafeSQL.getValues()));
            this.database.executePreparedUpdate("UPDATE a_sdidataitemspec SET modtool = 'DeleteDataItemSpec'" + (tracelogid.length() > 0 ? ", tracelogid = ?" : "") + str + " AND tracelogid = 'DELETED' AND auditsequence = ( SELECT max( auditsequence ) FROM a_sdidataitemspec " + str + " AND tracelogid = 'DELETED' )", varList.toArray());
        }
    }

    private String getSDIClause(String tname, String rsetid, SafeSQL safeSQL) {
        String sdiclause = "";
        sdiclause = this.runOld ? (this.connectionInfo.isOracle() ? "( sdcid, keyid1, keyid2, keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ")" : "( \tsdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") AND \tkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(rsetid) + ") )") : (this.connectionInfo.isOracle() ? "( sdcid, keyid1, keyid2, keyid3 ) IN ( SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + ") " : " EXISTS ( \tSELECT null FROM rsetitems r WHERE    r.rsetid = " + safeSQL.addVar(rsetid) + " AND    r.sdcid = " + tname + ".sdcid AND \tr.keyid1 = " + tname + ".keyid1 AND \tr.keyid2 = " + tname + ".keyid2 AND \tr.keyid3 = " + tname + ".keyid3 ) ");
        return sdiclause;
    }

    public static DataSet resolveCurrentPL(String paramListIdProp, String paramListVersionIdProp, String variantIdProp, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        DataSet allPropsParamLists = new DataSet();
        allPropsParamLists.addColumnValues("paramlistid", 0, paramListIdProp, ";");
        allPropsParamLists.addColumnValues("paramlistversionid", 0, paramListVersionIdProp, ";");
        allPropsParamLists.addColumnValues("variantid", 0, variantIdProp, ";");
        if (paramListVersionIdProp.indexOf("C") > -1) {
            boolean cached = true;
            SapphireConnection sapphireConnection = new SapphireConnection(database.getConnection(), connectionInfo);
            for (int i = 0; i < allPropsParamLists.getRowCount(); ++i) {
                String paramlistid = allPropsParamLists.getValue(i, "paramlistid");
                String variantid = allPropsParamLists.getValue(i, "variantid");
                String currentversionid = (String)CacheUtil.get(connectionInfo.getDatabaseId(), "ParamList_CurrentVersion", paramlistid + ";" + variantid);
                if (currentversionid == null || currentversionid.length() <= 0) {
                    cached = false;
                    break;
                }
                allPropsParamLists.setValue(i, "paramlistversionid", currentversionid);
            }
            if (!cached) {
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("paramlistversionid", "C");
                DataSet allCurrentPropsParamLists = allPropsParamLists.getFilteredDataSet(filterMap);
                DAMProcessor dam = new DAMProcessor(connectionInfo.getConnectionId());
                String paramListRset = dam.createRSet("ParamList", allCurrentPropsParamLists.getColumnValues("paramlistid", ";"), allCurrentPropsParamLists.getColumnValues("paramlistversionid", ";"), allCurrentPropsParamLists.getColumnValues("variantid", ";"));
                StringBuffer qry = new StringBuffer();
                qry.append("SELECT p.paramlistid, p.paramlistversionid, p.variantid, p.versionstatus").append(" FROM paramlist p, rsetitems r").append(" WHERE ").append(" p.paramlistid = r.keyid1").append(" AND p.variantid = r.keyid3").append(" AND ( p.versionstatus='P' OR p.versionstatus='C' )").append(" AND r.rsetid = ? ");
                QueryProcessor qp = new QueryProcessor(connectionInfo.getConnectionId());
                allCurrentPropsParamLists = qp.getPreparedSqlDataSet(qry.toString(), new Object[]{paramListRset});
                dam.clearRSet(paramListRset);
                int rowCount = allPropsParamLists.getRowCount();
                String versionId = "";
                for (int i = 0; i < rowCount; ++i) {
                    if (!"C".equals(allPropsParamLists.getString(i, "paramlistversionid", ""))) continue;
                    filterMap.clear();
                    filterMap.put("paramlistid", allPropsParamLists.getString(i, "paramlistid"));
                    filterMap.put("variantid", allPropsParamLists.getString(i, "variantid"));
                    filterMap.put("versionstatus", "C");
                    DataSet paramListDS = allCurrentPropsParamLists.getFilteredDataSet(filterMap);
                    if (paramListDS.getRowCount() > 0) {
                        versionId = paramListDS.getString(0, "paramlistversionid");
                    } else {
                        filterMap.put("versionstatus", "P");
                        paramListDS = allCurrentPropsParamLists.getFilteredDataSet(filterMap);
                        paramListDS.sort("paramlistversionid D");
                        versionId = paramListDS.getString(0, "paramlistversionid", "1");
                    }
                    filterMap.clear();
                    allPropsParamLists.setString(i, "paramlistversionid", versionId);
                    CacheUtil.put(connectionInfo.getDatabaseId(), "ParamList_CurrentVersion", allPropsParamLists.getString(i, "paramlistid") + ";" + allPropsParamLists.getString(i, "variantid"), versionId);
                }
            }
        }
        return allPropsParamLists;
    }

    public static DataSet getParamListForms(String paramListIdProp, String paramListVersionIdProp, String variantIdProp, QueryProcessor qp, DAMProcessor dam, DBAccess database, ConnectionInfo connectionInfo, Logger logger) throws SapphireException {
        DataSet paramListDefaultForms = new DataSet();
        DataSet resolvedPL = BaseSDIDataAction.resolveCurrentPL(paramListIdProp, paramListVersionIdProp, variantIdProp, database, connectionInfo);
        String[] plIdArr = StringUtil.split(resolvedPL.getColumnValues("paramlistid", ";"), ";");
        String[] plVerIdArr = StringUtil.split(resolvedPL.getColumnValues("paramlistversionid", ";"), ";");
        String[] variantIdArr = StringUtil.split(resolvedPL.getColumnValues("variantid", ";"), ";");
        StringBuffer sql = new StringBuffer();
        String rsetid = null;
        SafeSQL safeSQL = new SafeSQL();
        if (plIdArr.length >= 50) {
            rsetid = dam.createRSet("ParamList", paramListIdProp, paramListVersionIdProp, variantIdProp);
            sql.append("SELECT paramlistid, paramlistversionid, variantid, createworksheetrule, formid, formversionid, formrule ").append(" FROM paramlist p, rsetitems r, sdiformrule sfr").append(" WHERE ").append(" r.sdcid = 'ParamList'").append(" AND r.keyid1 = p.paramlistid").append(" AND r.keyid2 = p.paramlistversionid").append(" AND r.keyid3 = p.variantid").append(" AND r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" AND sfr.sdcid = 'ParamList'").append(" AND sfr.keyid1 = p.paramlistid").append(" AND sfr.keyid2 = p.paramlistversionid").append(" AND sfr.keyid3 = p.variantid");
        } else {
            sql.append("SELECT paramlistid, paramlistversionid, variantid").append(", createworksheetrule").append(", formid, formversionid, formrule").append(" FROM paramlist, sdiformrule").append(" WHERE").append(" ( ");
            for (int i = 0; i < plIdArr.length; ++i) {
                if (i != 0) {
                    sql.append(" OR ");
                }
                sql.append(" ( ").append(" paramlistid = ").append(safeSQL.addVar(plIdArr[i])).append(" AND paramlistversionid = ").append(safeSQL.addVar(plVerIdArr[i])).append(" AND variantid = ").append(safeSQL.addVar(variantIdArr[i]));
                sql.append(" ) ");
            }
            sql.append(" ) ");
            sql.append(" AND sdiformrule.sdcid = 'ParamList'").append(" AND sdiformrule.keyid1 = paramlist.paramlistid").append(" AND sdiformrule.keyid2 = paramlist.paramlistversionid").append(" AND sdiformrule.keyid3 = paramlist.variantid");
        }
        logger.info("ParamList Forms sql: " + sql.toString());
        paramListDefaultForms = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (rsetid != null) {
            dam.clearRSet(rsetid);
        }
        return paramListDefaultForms;
    }

    public static void createWorksheet(DataSet dataForms, ActionProcessor ap, Logger logger, boolean asynchronous, SapphireConnection sapphireConnection) throws SapphireException {
        logger.info("Start creating worksheet.");
        if (dataForms.getRowCount() > 0) {
            dataForms.sort("formid,formversionid,s_assignedanalyst,s_assigneddepartment,s_assigneddepartment");
            ArrayList<DataSet> groupedDataForms = dataForms.getGroupedDataSets("formid,formversionid,s_assignedanalyst,s_assigneddepartment,s_assigneddepartment");
            for (int i = 0; i < groupedDataForms.size(); ++i) {
                DataSet tempDataSet = (DataSet)groupedDataForms.get(i);
                PropertyList createWSProps = new PropertyList();
                createWSProps.setProperty("sdcid", tempDataSet.getString(0, "sdcid"));
                createWSProps.setProperty("keyid1", tempDataSet.getColumnValues("keyid1", ";"));
                createWSProps.setProperty("keyid2", tempDataSet.getColumnValues("keyid2", ";"));
                createWSProps.setProperty("keyid3", tempDataSet.getColumnValues("keyid3", ";"));
                createWSProps.setProperty("paramlistid", tempDataSet.getColumnValues("paramlistid", ";"));
                createWSProps.setProperty("paramlistversionid", tempDataSet.getColumnValues("paramlistversionid", ";"));
                createWSProps.setProperty("variantid", tempDataSet.getColumnValues("variantid", ";"));
                createWSProps.setProperty("dataset", tempDataSet.getColumnValues("dataset", ";"));
                createWSProps.setProperty("formid", tempDataSet.getString(0, "formid", ""));
                createWSProps.setProperty("formversionid", tempDataSet.getString(0, "formversionid", ""));
                if (dataForms.isValidColumn(COLUMN_ASSIGNEDANALYST)) {
                    createWSProps.setProperty("assignto", tempDataSet.getString(0, COLUMN_ASSIGNEDANALYST, ""));
                }
                if (dataForms.isValidColumn(COLUMN_ASSIGNEDDEPT)) {
                    createWSProps.setProperty("assigntodepartment", tempDataSet.getString(0, COLUMN_ASSIGNEDDEPT, ""));
                }
                if (asynchronous) {
                    createWSProps.setProperty("actionid", "CreateWorksheet");
                    createWSProps.setProperty("actionversionid", "1");
                    createWSProps.setProperty("processassysuserid", sapphireConnection.getSysuserId());
                    ap.processAction("AddToDoListEntry", "1", createWSProps);
                    continue;
                }
                ap.processAction("CreateWorksheet", "1", createWSProps, false, true);
                tempDataSet.addColumnValues("documentid", 0, createWSProps.getProperty("documentid"), ";");
                tempDataSet.addColumnValues("documentversionid", 0, createWSProps.getProperty("documentversionid"), ";");
            }
        } else {
            logger.info("No Data Forms present.");
        }
        logger.info("Finish creating worksheet.");
    }

    public static int addDataForms(DataSet dataForms, String sdcId, String keyId1, String keyId2, String keyId3, String paramListId, String paramListVersionId, String variantId, String dataSet, String formId, String formVersionId, String assignedAnalyst, String assignedDept, TranslationProcessor tp) throws SapphireException {
        if (formId == null || formId.length() == 0) {
            throw new SapphireException("GENERAL_ERROR", tp.translate("No default/supplied Form found for") + " " + paramListId + "|" + paramListVersionId + "|" + variantId);
        }
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("sdcid", sdcId);
        findMap.put("keyid1", keyId1);
        findMap.put("keyid2", keyId2);
        findMap.put("keyid3", keyId3);
        findMap.put("paramlistid", paramListId);
        findMap.put("paramlistversionid", paramListVersionId);
        findMap.put("variantid", variantId);
        findMap.put("dataset", new BigDecimal(dataSet));
        int newRow = dataForms.findRow(findMap);
        if (newRow == -1) {
            newRow = dataForms.addRow();
            dataForms.setString(newRow, "sdcid", sdcId);
            dataForms.setString(newRow, "keyid1", keyId1);
            dataForms.setString(newRow, "keyid2", keyId2);
            dataForms.setString(newRow, "keyid3", keyId3);
            dataForms.setString(newRow, "paramlistid", paramListId);
            dataForms.setString(newRow, "paramlistversionid", paramListVersionId);
            dataForms.setString(newRow, "variantid", variantId);
            dataForms.setNumber(newRow, "dataset", dataSet);
            dataForms.setString(newRow, "formid", formId);
            dataForms.setString(newRow, "formversionid", formVersionId);
            if (assignedAnalyst != null && assignedAnalyst.length() > 0) {
                dataForms.setString(newRow, COLUMN_ASSIGNEDANALYST, assignedAnalyst);
            }
            if (assignedDept != null && assignedDept.length() > 0) {
                dataForms.setString(newRow, COLUMN_ASSIGNEDDEPT, assignedDept);
            }
        }
        return newRow;
    }

    private void prepareDataFormRow(String sdcId, String keyId1, String keyId2, String keyId3, String paramListId, String paramListVersionId, String variantId, String dataset, String formId, String formVersionId, DataSet paramListForms, DataSet dataForms, PropertyList properties, StringBuffer worksheetPresent, DataSet sdiDataSets, DataSet sdiDataTable, int currentRow) throws SapphireException {
        if (sdiDataTable.getString(currentRow, COLUMN_ASSIGNEDANALYST, "").length() > 0 || sdiDataTable.getString(currentRow, COLUMN_ASSIGNEDDEPT, "").length() > 0) {
            boolean isWorksheetAlreadyPresent;
            HashMap<String, Object> filterMap = new HashMap<String, Object>();
            filterMap.put("sdcid", sdcId);
            filterMap.put("keyid1", keyId1);
            filterMap.put("keyid2", keyId2);
            filterMap.put("keyid3", keyId3);
            filterMap.put("paramlistid", paramListId);
            filterMap.put("paramlistversionid", paramListVersionId);
            filterMap.put("variantid", variantId);
            filterMap.put("dataset", new BigDecimal(dataset));
            int dataSetRow = sdiDataSets.findRow(filterMap);
            boolean bl = isWorksheetAlreadyPresent = dataSetRow > -1 && sdiDataSets.getString(dataSetRow, "documentid", "").length() > 0;
            if (!isWorksheetAlreadyPresent) {
                filterMap.clear();
                filterMap.put("paramlistid", paramListId);
                filterMap.put("paramlistversionid", paramListVersionId);
                filterMap.put("variantid", variantId);
                DataSet paramListForm = paramListForms.getFilteredDataSet(filterMap);
                if (WS_ONASSIGNMENT.equalsIgnoreCase(paramListForm.getString(0, COLUMN_CREATEWORKSHEETRULE))) {
                    formId = properties.getProperty("formid");
                    formVersionId = properties.getProperty("formversionid");
                    filterMap.put(COLUMN_FORMRULE, "default");
                    int row = paramListForm.findRow(filterMap);
                    if (formId.length() == 0 && row > -1) {
                        formId = paramListForm.getString(row, "formid");
                        formVersionId = paramListForm.getString(row, "formversionid");
                    }
                    int n = BaseSDIDataAction.addDataForms(dataForms, sdcId, keyId1, keyId2, keyId3, paramListId, paramListVersionId, variantId, dataset, formId, formVersionId, sdiDataTable.getString(currentRow, COLUMN_ASSIGNEDANALYST), sdiDataTable.getString(currentRow, COLUMN_ASSIGNEDDEPT), this.getTranslationProcessor());
                }
            } else {
                worksheetPresent.append(",<br>&nbsp;&nbsp;").append(sdcId).append("|").append(keyId1).append("|").append(keyId2).append("|").append(keyId3).append("|").append(paramListId).append("|").append(paramListVersionId).append("|").append(variantId).append("|").append(dataset);
            }
        }
    }

    public static void dataApprovalRollback(DataSet beforeEditDataItems, DataSet dataapproval, DataSet dataItems, ActionProcessor ap) throws ActionException {
        HashSet unReleasedDataSet = new HashSet();
        PropertyList actionprops = new PropertyList();
        StringBuffer keyid1Buffer = new StringBuffer();
        StringBuffer keyid2Buffer = new StringBuffer();
        StringBuffer keyid3Buffer = new StringBuffer();
        StringBuffer paramlistidBuffer = new StringBuffer();
        StringBuffer paramlistversionidBuffer = new StringBuffer();
        StringBuffer variantidBuffer = new StringBuffer();
        StringBuffer datasetBuffer = new StringBuffer();
        StringBuffer approvalstepBuffer = new StringBuffer();
        StringBuffer approvalflagBuffer = new StringBuffer();
        for (int i = 0; i < dataItems.getRowCount(); ++i) {
            String releaseFlagPrev;
            String releaseFlag = dataItems.getValue(i, "releasedflag", "");
            if (!StringUtil.getYN(releaseFlag, "").equals("N")) continue;
            HashMap<String, Object> find = new HashMap<String, Object>();
            find.put("sdcid", dataItems.getValue(i, "sdcid"));
            find.put("keyid1", dataItems.getValue(i, "keyid1"));
            find.put("keyid2", dataItems.getValue(i, "keyid2"));
            find.put("keyid3", dataItems.getValue(i, "keyid3"));
            find.put("paramlistid", dataItems.getValue(i, "paramlistid"));
            find.put("paramlistversionid", dataItems.getValue(i, "paramlistversionid"));
            find.put("variantid", dataItems.getValue(i, "variantid"));
            find.put("dataset", dataItems.getBigDecimal(i, "dataset"));
            if (unReleasedDataSet.contains(find)) continue;
            HashMap<String, Object> findDataItem = new HashMap<String, Object>();
            findDataItem.put("sdcid", dataItems.getValue(i, "sdcid"));
            findDataItem.put("keyid1", dataItems.getValue(i, "keyid1"));
            findDataItem.put("keyid2", dataItems.getValue(i, "keyid2"));
            findDataItem.put("keyid3", dataItems.getValue(i, "keyid3"));
            findDataItem.put("paramlistid", dataItems.getValue(i, "paramlistid"));
            findDataItem.put("paramlistversionid", dataItems.getValue(i, "paramlistversionid"));
            findDataItem.put("variantid", dataItems.getValue(i, "variantid"));
            findDataItem.put("dataset", dataItems.getBigDecimal(i, "dataset"));
            findDataItem.put("paramid", dataItems.getValue(i, "paramid"));
            findDataItem.put("paramtype", dataItems.getValue(i, "paramtype"));
            findDataItem.put("replicateid", dataItems.getBigDecimal(i, "replicateid"));
            int row = beforeEditDataItems.findRow(findDataItem);
            boolean unReleased = true;
            if (row >= 0 && (releaseFlagPrev = beforeEditDataItems.getValue(row, "releasedflag", "N")).equals(releaseFlag)) {
                unReleased = false;
            }
            if (!unReleased) continue;
            unReleasedDataSet.add(find);
            DataSet filteredDataApproval = dataapproval.getFilteredDataSet(find);
            if (filteredDataApproval.getRowCount() <= 0) continue;
            actionprops.setProperty("sdcid", (String)find.get("sdcid"));
            keyid1Buffer.append(";" + filteredDataApproval.getColumnValues("keyid1", ";"));
            keyid2Buffer.append(";" + filteredDataApproval.getColumnValues("keyid2", ";"));
            keyid3Buffer.append(";" + filteredDataApproval.getColumnValues("keyid3", ";"));
            paramlistidBuffer.append(";" + filteredDataApproval.getColumnValues("paramlistid", ";"));
            paramlistversionidBuffer.append(";" + filteredDataApproval.getColumnValues("paramlistversionid", ";"));
            variantidBuffer.append(";" + filteredDataApproval.getColumnValues("variantid", ";"));
            datasetBuffer.append(";" + filteredDataApproval.getColumnValues("dataset", ";"));
            approvalstepBuffer.append(";" + filteredDataApproval.getColumnValues("approvalstep", ";"));
            approvalflagBuffer.append(";" + filteredDataApproval.getColumnValues("approvalflag", ";"));
        }
        if (actionprops.getProperty("sdcid").length() > 0 && (approvalflagBuffer.indexOf("P") >= 0 || approvalflagBuffer.indexOf("F") >= 0)) {
            actionprops.setProperty("keyid1", keyid1Buffer.substring(1));
            actionprops.setProperty("keyid2", keyid2Buffer.substring(1));
            actionprops.setProperty("keyid3", keyid3Buffer.substring(1));
            actionprops.setProperty("paramlistid", paramlistidBuffer.substring(1));
            actionprops.setProperty("paramlistversionid", paramlistversionidBuffer.substring(1));
            actionprops.setProperty("variantid", variantidBuffer.substring(1));
            actionprops.setProperty("dataset", datasetBuffer.substring(1));
            actionprops.setProperty("approvalstep", approvalstepBuffer.substring(1));
            String approvalFlags = approvalflagBuffer.substring(1);
            approvalFlags = approvalFlags.replaceAll("P|F", "U");
            actionprops.setProperty("approvalflag", approvalFlags);
            actionprops.setProperty("propsmatch", "Y");
            ap.processAction("EditDataApproval", "1", actionprops);
        }
    }
}


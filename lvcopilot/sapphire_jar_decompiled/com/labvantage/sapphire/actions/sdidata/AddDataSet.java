/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddDataSetEventObject;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.WorkItemItemRuleEvaluator;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddDataSet
extends BaseSDIDataAction
implements sapphire.action.AddDataSet {
    private static HashMap<String, HashMap> transactionCache = new HashMap();
    private HashMap cache = null;
    private String transactionid = "";
    private HashSet<String> processedSDIParamListSet = null;
    private HashMap<String, String> limitRuleCache = new HashMap();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean isDSecEnabled;
        if (properties.getProperty("__transactionid").length() > 0) {
            this.transactionid = properties.getProperty("__transactionid");
            this.cache = transactionCache.get(this.transactionid);
            if (this.cache != null) {
                this.processedSDIParamListSet = (HashSet)this.cache.get("processedSDIParamListSet");
            }
        }
        if (this.cache == null) {
            this.cache = new HashMap();
            this.processedSDIParamListSet = new HashSet();
        }
        boolean applyworkitem = "Y".equals(properties.getProperty("applyworkitem"));
        boolean useQuickInsertSQL4SDIDataItemLimit = true;
        useQuickInsertSQL4SDIDataItemLimit = this.determineQuickInsertionNeeded(properties, "usequickinsertsql4sdidataitemlimits");
        boolean useQuickInsertSQL4SDIDataItem = true;
        useQuickInsertSQL4SDIDataItem = this.determineQuickInsertionNeeded(properties, "usequickinsertsql4sdidataitem");
        DataSet sdidataColList = (DataSet)this.cache.get("sdidataColList");
        if (sdidataColList == null) {
            sdidataColList = WorkItemUtil.getEditableColumnList(this.database, this.logger, "sdidata");
            sdidataColList = WorkItemUtil.filterColumnIdsWithPropList(this.logger, sdidataColList, properties);
            this.cache.put("sdidataColList", sdidataColList);
        }
        boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        this.logger.info("PropsMatch=" + propsmatch);
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        String sdcid = properties.getProperty("sdcid");
        PropertyList sdcProps = (PropertyList)this.cache.get("sdcProps");
        PropertyList datasetSDCProps = (PropertyList)this.cache.get("datasetSDCProps");
        if (sdcProps == null) {
            sdcProps = this.getSDCProcessor().getPropertyList(sdcid);
            this.cache.put("sdcProps", sdcProps);
            sdcid = sdcProps.getProperty("sdcid");
            datasetSDCProps = this.getSDCProcessor().getPropertyList("DataSet");
            this.cache.put("datasetSDCProps", datasetSDCProps);
        }
        boolean bl = isDSecEnabled = datasetSDCProps.getProperty("accesscontrolledflag", "").equals("D") || datasetSDCProps.getProperty("accesscontrolledflag", "").equals("B");
        if (propsmatch) {
            StringBuffer groupByCols = new StringBuffer("usersequence, paramlistid, paramlistversionid, variantid");
            DataSet propsDS = new DataSet(this.connectionInfo);
            propsDS.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), ";");
            propsDS.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), ";", "(null)");
            propsDS.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), ";", "(null)");
            propsDS.addColumnValues("paramlistid", 0, properties.getProperty("paramlistid"), ";");
            propsDS.addColumnValues("paramlistversionid", 0, properties.getProperty("paramlistversionid"), ";");
            propsDS.addColumnValues("variantid", 0, properties.getProperty("variantid"), ";");
            propsDS.padColumns();
            WorkItemUtil.fillDSWithProps(this.getSDCProcessor(), this.getTranslationProcessor(), sdidataColList, properties, propsDS, "DataSet", false, 0, 0, ";", true, "");
            for (int i = 0; i < sdidataColList.size(); ++i) {
                groupByCols.append(", ").append(sdidataColList.getString(i, "columnid"));
            }
            propsDS.sort(groupByCols.toString());
            ArrayList<DataSet> groups = propsDS.getGroupedDataSets(groupByCols.toString());
            PropertyList pl = new PropertyList();
            pl.putAll(properties);
            pl.setProperty("rsetid", "");
            pl.setProperty("propsmatch", "N");
            pl.setProperty("__propsmatch", "Y");
            SDIData sdiData = new SDIData();
            DataSet newDSInstances = new DataSet(this.connectionInfo);
            for (DataSet dataSet : groups) {
                if (dataSet.size() <= 0) continue;
                pl.setProperty("keyid1", dataSet.getColumnValues("keyid1", ";"));
                pl.setProperty("keyid2", dataSet.getColumnValues("keyid2", ";"));
                pl.setProperty("keyid3", dataSet.getColumnValues("keyid3", ";"));
                pl.setProperty("paramlistid", dataSet.getString(0, "paramlistid"));
                pl.setProperty("paramlistversionid", dataSet.getString(0, "paramlistversionid"));
                pl.setProperty("variantid", dataSet.getString(0, "variantid"));
                for (int i = 0; i < sdidataColList.size(); ++i) {
                    String columnId = sdidataColList.getString(i, "columnid");
                    pl.setProperty(columnId, dataSet.getValue(0, columnId));
                }
                this.processAction(pl);
                String tempXML = pl.getProperty("newdatasetinstancexml");
                DataSet tempDS = new DataSet(tempXML);
                for (int i = 0; i < tempDS.size(); ++i) {
                    newDSInstances.copyRow(tempDS, i, 1);
                }
                SDIData callSDIData = (SDIData)pl.get("__sdidata");
                Set datasets = callSDIData.getDatasets();
                for (String dataset : datasets) {
                    DataSet callDataSet = callSDIData.getDataset(dataset);
                    if (callDataSet == null) continue;
                    DataSet ds = sdiData.getDataset(dataset);
                    if (ds == null) {
                        sdiData.setDataset(dataset, callDataSet);
                        continue;
                    }
                    ds.copyRow(callDataSet, -1, 1);
                }
            }
            properties.setProperty("newdatasetinstancexml", newDSInstances.toXML());
            EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), new PostAddDataSetEventObject(sdcid, sdcProps, sdiData, properties));
        } else {
            boolean propsMatchCall = properties.getProperty("__propsmatch").equals("Y");
            DataSet newDataSetInstances = new DataSet(this.connectionInfo);
            boolean wapStatusInactionInput = properties.containsKey("wapstatus");
            boolean deleterset = false;
            String rsetid = properties.getProperty("rsetid");
            if (rsetid.length() == 0) {
                rsetid = AddDataSet.createBypassSecurityRSet(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), this.database, this.connectionInfo, false);
                deleterset = true;
            }
            if (rsetid.length() > 0) {
                DataSet dsSDIWI;
                DataSet sdidatasets;
                String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
                String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
                String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
                String[] paramlistidprop = StringUtil.split(properties.getProperty("paramlistid"), ";");
                String[] paramlistversionidprop = StringUtil.split(properties.getProperty("paramlistversionid"), ";");
                String[] variantidprop = StringUtil.split(properties.getProperty("variantid"), ";");
                String[] datasetprop = StringUtil.split(properties.getProperty("dataset"), ";");
                String[] addnewonlyprop = StringUtil.split(properties.getProperty("addnewonly"), ";");
                String[] availabilityflagprop = StringUtil.split(properties.getProperty("available"), ";");
                String[] sourcewiinstanceprop = StringUtil.split(properties.getProperty("sourceworkiteminstance"), ";");
                SDCProcessor sdcProcessor = this.getSDCProcessor();
                DataSet sdiRows = new DataSet();
                String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer selectSDIs = new StringBuffer("select s.* from " + tableid + " s, rsetitems WHERE rsetitems.rsetid=" + safeSQL.addVar(rsetid) + "  AND rsetitems.sdcid =" + safeSQL.addVar(sdcid) + " AND s." + keycolid1 + " = rsetitems.keyid1");
                if (keycolid2.length() > 0) {
                    selectSDIs.append(" AND s." + keycolid2 + " = rsetitems.keyid2");
                }
                if (keycolid3.length() > 0) {
                    selectSDIs.append(" AND s." + keycolid3 + " = rsetitems.keyid3");
                }
                this.database.createPreparedResultSet(selectSDIs.toString(), safeSQL.getValues());
                sdiRows.setResultSet(this.database.getResultSet());
                boolean sdidataDirty = false;
                if (this.transactionid.length() > 0) {
                    block9: for (int s = 0; s < keyid1prop.length; ++s) {
                        String sdiKey = keyid1prop[s] + ";" + (keyid2prop.length < keyid1prop.length || keyid2prop[s].length() == 0 ? "(null)" : keyid2prop[s]) + ";" + (keyid3prop.length < keyid1prop.length || keyid3prop[s].length() == 0 ? "(null)" : keyid3prop[s]);
                        for (int i = 0; i < paramlistidprop.length; ++i) {
                            String sdiparamlistKey = sdiKey + ";" + paramlistidprop[i] + ";" + paramlistversionidprop[i] + ";" + variantidprop[i];
                            if (this.processedSDIParamListSet.contains(sdiparamlistKey)) {
                                sdidataDirty = true;
                                continue block9;
                            }
                            this.processedSDIParamListSet.add(sdiparamlistKey);
                        }
                    }
                }
                if ((sdidatasets = (DataSet)this.cache.get("sdidatasets")) == null || sdidataDirty || this.transactionid.length() == 0) {
                    sdidatasets = new DataSet(this.connectionInfo);
                    HashMap<String, BigDecimal> sdiparamlistkeyMaxDataset = new HashMap<String, BigDecimal>();
                    HashMap sdiMaxUserSequence = new HashMap();
                    this.loadSDIDataSets(rsetid, sdidatasets);
                    String previousSDIParamListKey = "";
                    for (int i = 0; i < sdidatasets.getRowCount(); ++i) {
                        String currentKey = sdidatasets.getValue(i, "keyid1") + ";" + sdidatasets.getValue(i, "keyid2") + ";" + sdidatasets.getValue(i, "keyid3") + ";" + sdidatasets.getValue(i, "paramlistid") + ";" + sdidatasets.getValue(i, "paramlistversionid") + ";" + sdidatasets.getValue(i, "variantid");
                        if (currentKey.equals(previousSDIParamListKey)) continue;
                        sdiparamlistkeyMaxDataset.put(currentKey, sdidatasets.getBigDecimal(i, "dataset"));
                        previousSDIParamListKey = currentKey;
                    }
                    if (this.transactionid.length() > 0) {
                        this.cache.put("sdidatasets", sdidatasets);
                        this.cache.put("sdiparamlistkeyMaxDataset", sdiparamlistkeyMaxDataset);
                        this.cache.put("sdiMaxUserSequence", sdiMaxUserSequence);
                    }
                    if (sdidataDirty) {
                        this.processedSDIParamListSet.clear();
                    }
                }
                Calendar now = DateTimeUtil.getNowCalendar();
                SDIRequest primaryReq = new SDIRequest();
                primaryReq.setSDCid(sdcid);
                primaryReq.setRsetid(rsetid);
                primaryReq.setRetainRsetid(true);
                primaryReq.setRequestItem("primary");
                SDIProcessor sdiProcessor = this.getSDIProcessor();
                SDIData beforeEditImage = sdiProcessor.getSDIData(primaryReq);
                DataSet primaryImage = beforeEditImage.getDataset("primary");
                boolean addInstrToCalibrationDS = "Y".equals(this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("autoaddinstrumementtocalibrationdataset"));
                DataSet dataset = new DataSet(this.connectionInfo);
                DataSet attributeData = new DataSet(this.connectionInfo);
                DataSet dataitem = new DataSet(this.connectionInfo);
                DataSet datalimits = new DataSet(this.connectionInfo);
                DataSet dataapproval = new DataSet(this.connectionInfo);
                DataSet datarelations = new DataSet(this.connectionInfo);
                DataSet dataForms = new DataSet(this.connectionInfo);
                DataSet sdidatacrosssdicalc = new DataSet(this.connectionInfo);
                DataSet paramlist = null;
                DataSet paramlistitem = null;
                DataSet paramlimits = null;
                DataSet approvalsteps = null;
                DataSet paramlistAttributes = null;
                DataSet paramlistcrosssdicalcs = null;
                DataSet paramListReagentTypes = this.getParamListReagentTypes(properties.getProperty("paramlistid"), properties.getProperty("paramlistversionid"), properties.getProperty("variantid"));
                DataSet paramListDefaultForms = null;
                HashMap<String, Object> findmap = new HashMap<String, Object>();
                HashMap<String, String> filtermap = new HashMap<String, String>();
                HashMap<String, String> approvalstepfilter = new HashMap<String, String>();
                String currentdsprop = "";
                StringBuffer output_sdidataid = new StringBuffer();
                String[] paramprop = properties.getProperty("param") == null ? null : StringUtil.getTokens(properties.getProperty("param"));
                for (int pl = 0; pl < paramlistidprop.length; ++pl) {
                    String[] pseudokeys;
                    int i;
                    String isVersionProtectEnabled;
                    PropertyList versionprotection;
                    String availabilityflag;
                    boolean createWorksheet = false;
                    String formId = "";
                    String formVersionId = "";
                    String paramlistid = paramlistidprop[pl];
                    BaseSDCRules[] paramlistversionid = paramlistversionidprop.length == 0 || paramlistversionidprop.length < paramlistidprop.length || paramlistversionidprop[pl].length() == 0 ? "1" : paramlistversionidprop[pl];
                    String variantid = variantidprop.length == 0 || variantidprop.length < paramlistidprop.length || variantidprop[pl].length() == 0 ? "" : variantidprop[pl];
                    String datasetstr = datasetprop.length == 0 || datasetprop.length < paramlistidprop.length || datasetprop[pl].length() == 0 ? "1" : datasetprop[pl];
                    String string = availabilityflag = availabilityflagprop.length < paramlistidprop.length ? availabilityflagprop[0] : availabilityflagprop[pl];
                    if ("C".equals(paramlistversionid)) {
                        String sql = "SELECT paramlistversionid FROM paramlist WHERE paramlistid=? and variantid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (paramlistversionid as integer) desc";
                        this.database.createPreparedResultSet("CurrentVersion", sql, new Object[]{paramlistid, variantid});
                        if (this.database.getNext("CurrentVersion")) {
                            paramlistversionid = this.database.getString("CurrentVersion", "paramlistversionid");
                        }
                    }
                    boolean addnewonly = true;
                    addnewonly = addnewonlyprop.length == 1 ? addnewonlyprop[0].equals("Y") : addnewonlyprop.length == 0 || addnewonlyprop.length < paramlistidprop.length || addnewonlyprop[pl].length() == 0 || addnewonlyprop[pl].equals("Y");
                    findmap.put("paramlistid", paramlistid);
                    findmap.put("paramlistversionid", paramlistversionid);
                    findmap.put("variantid", variantid);
                    String paramlistKey = paramlistid + ";" + (String)paramlistversionid + ";" + variantid;
                    if (this.cache.get(paramlistKey + "_paramlist") == null) {
                        paramlist = new DataSet(this.connectionInfo);
                        paramlistitem = new DataSet(this.connectionInfo);
                        paramlimits = new DataSet(this.connectionInfo);
                        approvalsteps = new DataSet(this.connectionInfo);
                        paramlistAttributes = new DataSet(this.connectionInfo);
                        paramlistcrosssdicalcs = new DataSet(this.connectionInfo);
                        this.loadParamList(paramlistid, (String)paramlistversionid, variantid, paramlist, paramlistitem, paramlimits, approvalsteps, paramlistAttributes, paramlistcrosssdicalcs, sdcid);
                        this.logger.info("ParamList definition loaded");
                        this.cache.put(paramlistKey + "_paramlist", paramlist);
                        this.cache.put(paramlistKey + "_paramlistitem", paramlistitem);
                        this.cache.put(paramlistKey + "_paramlimits", paramlimits);
                        this.cache.put(paramlistKey + "_approvalsteps", approvalsteps);
                        this.cache.put(paramlistKey + "_paramlistAttributes", paramlistAttributes);
                        this.cache.put(paramlistKey + "_paramlistcrosssdicalcs", paramlistcrosssdicalcs);
                    } else {
                        this.logger.info("ParamList definition from cache");
                    }
                    paramlist = (DataSet)this.cache.get(paramlistKey + "_paramlist");
                    paramlistitem = (DataSet)this.cache.get(paramlistKey + "_paramlistitem");
                    paramlimits = (DataSet)this.cache.get(paramlistKey + "_paramlimits");
                    approvalsteps = (DataSet)this.cache.get(paramlistKey + "_approvalsteps");
                    paramlistAttributes = (DataSet)this.cache.get(paramlistKey + "_paramlistAttributes");
                    paramlistcrosssdicalcs = (DataSet)this.cache.get(paramlistKey + "_paramlistcrosssdicalcs");
                    PropertyList paramListPolicy = this.getConfigurationProcessor().getPolicy("ParamListPolicy", "Sapphire Custom");
                    if (paramListPolicy != null && (versionprotection = paramListPolicy.getPropertyListNotNull("expireddataprotection")) != null && versionprotection.size() > 0 && (isVersionProtectEnabled = versionprotection.getProperty("ParamList".toLowerCase(), "")).equalsIgnoreCase("Y")) {
                        this.validateParamListExpiry(paramlist);
                    }
                    if (useQuickInsertSQL4SDIDataItemLimit) {
                        paramlimits = new DataSet(this.connectionInfo);
                    }
                    if (useQuickInsertSQL4SDIDataItem) {
                        paramlistitem = new DataSet(this.connectionInfo);
                    }
                    if (paramprop != null && paramprop.length == paramlistidprop.length && !paramprop[pl].equals("all")) {
                        String param;
                        String paramids = paramprop[pl];
                        Object[] paramidarray = StringUtil.split(paramids, ";");
                        Arrays.sort(paramidarray);
                        ArrayList toberemoved = new ArrayList();
                        ArrayList toberemovedlimits = new ArrayList();
                        for (i = 0; i < paramlistitem.getRowCount(); ++i) {
                            param = paramlistitem.getString(i, "paramid") + "|" + paramlistitem.getString(i, "paramtype");
                            if (Arrays.binarySearch(paramidarray, param) >= 0) continue;
                            toberemoved.add(paramlistitem.get(i));
                        }
                        for (i = 0; i < paramlimits.getRowCount(); ++i) {
                            param = paramlimits.getString(i, "paramid") + "|" + paramlimits.getString(i, "paramtype");
                            if (Arrays.binarySearch(paramidarray, param) >= 0) continue;
                            toberemovedlimits.add(paramlimits.get(i));
                        }
                        paramlistitem.removeAll(toberemoved);
                        paramlimits.removeAll(toberemovedlimits);
                        this.cache.remove(paramlistKey + "_paramlist");
                        this.cache.remove(paramlistKey + "_paramlistitem");
                        this.cache.remove(paramlistKey + "_paramlimits");
                        this.cache.remove(paramlistKey + "_approvalsteps");
                        this.cache.remove(paramlistKey + "_paramlistAttributes");
                    }
                    approvalstepfilter.put("approvaltypeid", paramlist.getString(0, "approvaltypeid"));
                    DataSet filteredapprovalsteps = approvalsteps.getFilteredDataSet(approvalstepfilter);
                    createWorksheet = "Y".equals(properties.getProperty("createworksheet", "Y"));
                    String createWorkSheetRule = "";
                    if (createWorksheet) {
                        HashMap<String, Object> formFilterMap = new HashMap<String, Object>();
                        formFilterMap.put("paramlistid", paramlistid);
                        formFilterMap.put("paramlistversionid", paramlistversionid);
                        formFilterMap.put("variantid", variantid);
                        if (paramListDefaultForms == null) {
                            paramListDefaultForms = AddDataSet.getParamListForms(properties.getProperty("paramlistid"), properties.getProperty("paramlistversionid"), properties.getProperty("variantid"), this.getQueryProcessor(), this.getDAMProcessor(), this.database, this.connectionInfo, this.logger);
                            this.logger.info("paramListDefaultForms loaded");
                        }
                        DataSet paramListDefaultForm = paramListDefaultForms.getFilteredDataSet(formFilterMap);
                        createWorkSheetRule = paramListDefaultForm.getString(0, "createworksheetrule", "");
                        formId = properties.getProperty("formid");
                        formVersionId = properties.getProperty("formversionid");
                        formFilterMap.put("formrule", "default");
                        paramListDefaultForm = paramListDefaultForm.getFilteredDataSet(formFilterMap);
                        if (formId.length() == 0) {
                            formId = paramListDefaultForm.getString(0, "formid");
                            formVersionId = paramListDefaultForm.getString(0, "formversionid");
                        }
                    }
                    if (output_sdidataid.length() > 0) {
                        output_sdidataid.append("|");
                    }
                    if ((pseudokeys = ((DBUtil)this.database).getUUIDList(keyid1prop.length)) == null || pseudokeys.length != keyid1prop.length) {
                        throw new SapphireException("Unable to generate sdidataid.");
                    }
                    StringBuffer curr_sdidataid = new StringBuffer();
                    for (i = 0; i < keyid1prop.length; ++i) {
                        BigDecimal datasetsequenceBigDecimal;
                        int datasetnum;
                        String sdiAssignedAnalyst = "";
                        String sdiAssignedDepartment = "";
                        String sdiTestingDepartmentId = "";
                        String sdiWorkareaDepartmentId = "";
                        String sdiSiteId = "";
                        String sdiWapStatus = "";
                        String keyid1 = keyid1prop[i];
                        String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                        String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                        String sdiKey = keyid1 + ";" + keyid2 + ";" + keyid3;
                        String sdiParamListKey = sdiKey + ";" + paramlistKey;
                        String sourcewiinstance = sourcewiinstanceprop.length == 0 || sourcewiinstanceprop.length < keyid1prop.length || sourcewiinstanceprop[i].length() == 0 ? "" : sourcewiinstanceprop[i];
                        String sdidataid = pseudokeys[i];
                        boolean copyWapStatus = false;
                        if (curr_sdidataid.length() > 0) {
                            curr_sdidataid.append(";");
                        }
                        curr_sdidataid.append(sdidataid);
                        SDI thisSDI = new SDI(sdcid, keyid1, keyid2, keyid3);
                        HashMap<String, String> findSDI = new HashMap<String, String>();
                        findSDI.put(keycolid1, thisSDI.getKeyid1());
                        if (keycolid2.length() > 0) {
                            findSDI.put(keycolid2, thisSDI.getKeyid2());
                        }
                        if (keycolid3.length() > 0) {
                            findSDI.put(keycolid3, thisSDI.getKeyid3());
                        }
                        int sdiRow = sdiRows.findRow(findSDI);
                        if ("Sample".equals(sdcid) && sdiRow > -1) {
                            sdiAssignedAnalyst = sdiRows.getValue(sdiRow, "assignedanalystid");
                            sdiAssignedDepartment = sdiRows.getValue(sdiRow, "assigneddepartmentid");
                            sdiWapStatus = sdiRows.getValue(sdiRow, "wapstatus");
                            sdiSiteId = sdiRows.getValue(sdiRow, "sitedepartmentid");
                            sdiTestingDepartmentId = sdiRows.getValue(sdiRow, "testingdepartmentid");
                            sdiWorkareaDepartmentId = sdiRows.getValue(sdiRow, "workareadepartmentid");
                            if (!"Y".equalsIgnoreCase(sdiRows.getValue(sdiRow, "templateflag")) && sdiWapStatus.length() == 0) {
                                copyWapStatus = true;
                            }
                        }
                        try {
                            datasetnum = Integer.parseInt(datasetstr);
                        }
                        catch (Exception e) {
                            datasetnum = 1;
                        }
                        dataset.sort("keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset d");
                        findmap.put("keyid1", keyid1);
                        findmap.put("keyid2", keyid2);
                        findmap.put("keyid3", keyid3);
                        filtermap.put("keyid1", keyid1);
                        filtermap.put("keyid2", keyid2);
                        filtermap.put("keyid3", keyid3);
                        DataSet subsdidatasets = (DataSet)this.cache.get(sdiKey);
                        if (subsdidatasets == null || subsdidatasets.getRowCount() == 0 || sdidataDirty || this.transactionid.length() == 0) {
                            subsdidatasets = sdidatasets.getFilteredDataSet(filtermap);
                            this.cache.put(sdiKey, subsdidatasets);
                        }
                        int datasetsequence = -1;
                        if (properties.getProperty("matchusersequence", "N").equals("Y")) {
                            int j;
                            for (j = 0; j < subsdidatasets.getRowCount(); ++j) {
                                if (!subsdidatasets.getString(j, "paramlistid").equals(paramlistid) || !subsdidatasets.getString(j, "paramlistversionid").equals(paramlistversionid) || !subsdidatasets.getString(j, "variantid").equals(variantid)) continue;
                                datasetsequence = subsdidatasets.getInt(j, "usersequence") - 1;
                            }
                            subsdidatasets = dataset.getFilteredDataSet(filtermap);
                            for (j = 0; j < subsdidatasets.getRowCount(); ++j) {
                                if (!subsdidatasets.getString(j, "paramlistid").equals(paramlistid) || !subsdidatasets.getString(j, "paramlistversionid").equals(paramlistversionid) || !subsdidatasets.getString(j, "variantid").equals(variantid)) continue;
                                datasetsequence = subsdidatasets.getInt(j, "usersequence") - 1;
                            }
                        }
                        if (datasetsequence == -1 && this.transactionid.length() > 0 && (datasetsequenceBigDecimal = (BigDecimal)((HashMap)this.cache.get("sdiMaxUserSequence")).get(sdiKey)) != null) {
                            datasetsequence = datasetsequenceBigDecimal.intValue();
                        }
                        if (datasetsequence == -1) {
                            for (int j = 0; j < subsdidatasets.getRowCount(); ++j) {
                                if (subsdidatasets.getInt(j, "usersequence") <= datasetsequence) continue;
                                datasetsequence = subsdidatasets.getInt(j, "usersequence");
                            }
                            DataSet newsubsdidatasets = dataset.getFilteredDataSet(filtermap);
                            for (int j = 0; j < newsubsdidatasets.getRowCount(); ++j) {
                                if (newsubsdidatasets.getInt(j, "usersequence") <= datasetsequence) continue;
                                datasetsequence = newsubsdidatasets.getInt(j, "usersequence");
                            }
                        }
                        int existingdatasetNo = -1;
                        if (this.transactionid.length() > 0) {
                            BigDecimal datasetBigDecimal = (BigDecimal)((HashMap)this.cache.get("sdiparamlistkeyMaxDataset")).get(sdiParamListKey);
                            if (datasetBigDecimal != null) {
                                existingdatasetNo = datasetBigDecimal.intValue();
                            }
                        } else {
                            int findrow = sdidatasets.findRow(findmap);
                            if (findrow >= 0) {
                                existingdatasetNo = sdidatasets.getBigDecimal(findrow, "dataset").intValue();
                            }
                        }
                        boolean allCancelled = true;
                        for (int j = 0; j < subsdidatasets.getRowCount(); ++j) {
                            if (subsdidatasets.getString(j, "s_datasetstatus", "").equalsIgnoreCase("Cancelled") || !subsdidatasets.getString(j, "paramlistid").equals(paramlistid) || !subsdidatasets.getString(j, "paramlistversionid").equals(paramlistversionid) || !subsdidatasets.getString(j, "variantid").equals(variantid)) continue;
                            allCancelled = false;
                        }
                        int newfindrow = dataset.findRow(findmap);
                        if (!addnewonly || addnewonly & (existingdatasetNo < 0 || allCancelled)) {
                            HashMap<String, String> filterMap;
                            int newds = dataset.addRow();
                            if (existingdatasetNo >= 0 || newfindrow >= 0) {
                                if (newfindrow >= 0) {
                                    datasetnum = dataset.getBigDecimal(newfindrow, "dataset").intValue() + 1;
                                } else if (datasetnum <= existingdatasetNo) {
                                    datasetnum = existingdatasetNo + 1;
                                }
                            }
                            if (this.cache.get("sdiparamlistkeyMaxDataset") != null) {
                                ((HashMap)this.cache.get("sdiparamlistkeyMaxDataset")).put(sdiParamListKey, new BigDecimal(datasetnum));
                            }
                            if ((currentdsprop = properties.getProperty("newds" + String.valueOf(i))).length() > 0) {
                                properties.setProperty("newds" + String.valueOf(i), currentdsprop + ";" + String.valueOf(datasetnum));
                            } else {
                                properties.setProperty("newds" + String.valueOf(i), String.valueOf(datasetnum));
                            }
                            WorkItemUtil.fillDSWithProps(this.getSDCProcessor(), this.getTranslationProcessor(), sdidataColList, properties, dataset, "DataSet", true, newds, pl, "", false, isDSecEnabled ? "" : "securityuser;securitydepartment");
                            this.logger.info("Adding dataset row for: " + keyid1 + ", " + keyid2 + ", " + keyid3 + "(" + sdidataid + ")");
                            dataset.setString(newds, "sdcid", sdcid);
                            dataset.setString(newds, "keyid1", keyid1);
                            dataset.setString(newds, "keyid2", keyid2);
                            dataset.setString(newds, "keyid3", keyid3);
                            dataset.setString(newds, "sdidataid", sdidataid);
                            dataset.setString(newds, "paramlistid", paramlistid);
                            dataset.setString(newds, "paramlistversionid", (String)paramlistversionid);
                            dataset.setString(newds, "variantid", variantid);
                            dataset.setNumber(newds, "dataset", datasetnum);
                            dataset.setNumber(newds, "sourceworkiteminstance", sourcewiinstance);
                            if (sourcewiinstance == null || sourcewiinstance.length() == 0) {
                                String sdidataset_workareaDepartment;
                                String testingDepartmentId = paramlist.getValue(0, "testingdepartmentid");
                                String testinglabType = paramlist.getValue(0, "testinglabtype");
                                String workareaType = paramlist.getValue(0, "workareatype");
                                String workareaDepartmentId = paramlist.getValue(0, "workareadepartmentid");
                                String autoassignRule = paramlist.getValue(0, "autoassignrule");
                                String createActivityRule = paramlist.getValue(0, "createactivityrule");
                                String autoAssignAnalystId = paramlist.getValue(0, "autoassignanalystid");
                                if (dataset.getValue(newds, "testingdepartmentid").length() == 0) {
                                    if (testingDepartmentId.length() > 0) {
                                        dataset.setString(newds, "testingdepartmentid", testingDepartmentId);
                                        if (dataset.getValue(newds, "workareadepartmentid").length() == 0 && workareaDepartmentId.length() > 0) {
                                            dataset.setString(newds, "workareadepartmentid", workareaDepartmentId);
                                        }
                                    } else if (testinglabType.length() > 0 && sdiSiteId.length() > 0) {
                                        this.database.createPreparedResultSet("gettestdept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND testinglabtype = ?", new String[]{sdiSiteId, testinglabType});
                                        DataSet testDept = new DataSet(this.database.getResultSet("gettestdept"));
                                        if (testDept.getRowCount() > 0) {
                                            dataset.setString(newds, "testingdepartmentid", testDept.getValue(0, "departmentid"));
                                        }
                                    } else if (sdiTestingDepartmentId.length() > 0) {
                                        dataset.setString(newds, "testingdepartmentid", sdiTestingDepartmentId);
                                        if (dataset.getValue(newds, "workareadepartmentid").length() == 0 && sdiWorkareaDepartmentId.length() > 0) {
                                            dataset.setString(newds, "workareadepartmentid", sdiWorkareaDepartmentId);
                                        }
                                    }
                                }
                                String sdidataset_testingDepartment = dataset.getValue(newds, "testingdepartmentid");
                                if (dataset.getValue(newds, "workareadepartmentid").length() == 0 && workareaType.length() > 0 && sdidataset_testingDepartment.length() > 0) {
                                    this.database.createPreparedResultSet("getworkareadept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND workareatype = ?", new String[]{sdidataset_testingDepartment, workareaType});
                                    DataSet woDept = new DataSet(this.database.getResultSet("getworkareadept"));
                                    if (woDept.getRowCount() > 0 && woDept.getValue(0, "departmentid").length() > 0) {
                                        dataset.setString(newds, "workareadepartmentid", woDept.getValue(0, "departmentid"));
                                    }
                                }
                                if ((sdidataset_workareaDepartment = dataset.getValue(newds, "workareadepartmentid")).length() > 0 && "Workarea".equalsIgnoreCase(autoassignRule) && dataset.getValue(newds, "s_assigneddepartment").length() == 0) {
                                    dataset.setString(newds, "s_assigneddepartment", sdidataset_workareaDepartment);
                                }
                                if (autoAssignAnalystId.length() > 0 && "Analyst".equalsIgnoreCase(autoassignRule) && dataset.getValue(newds, "s_assignedanalyst").length() == 0) {
                                    dataset.setString(newds, "s_assignedanalyst", autoAssignAnalystId);
                                }
                                if (copyWapStatus && "On Demand".equalsIgnoreCase(createActivityRule) && !wapStatusInactionInput) {
                                    dataset.setString(newds, "wapstatus", "Pending");
                                }
                                if (sdiAssignedAnalyst.length() > 0 && dataset.getValue(newds, "s_assignedanalyst").length() == 0) {
                                    dataset.setString(newds, "s_assignedanalyst", sdiAssignedAnalyst);
                                }
                                if (sdiAssignedDepartment.length() > 0 && dataset.getValue(newds, "s_assigneddepartment").length() == 0) {
                                    dataset.setString(newds, "s_assigneddepartment", sdiAssignedDepartment);
                                }
                            } else if (!copyWapStatus) {
                                dataset.setValue(newds, "wapstatus", "");
                            }
                            dataset.setString(newds, "s_datasetstatus", "Initial");
                            dataset.setString(newds, "modifiableflag", paramlist.getString(0, "modifiableflag"));
                            dataset.setString(newds, "s_cancellableflag", paramlist.getString(0, "s_cancellableflag"));
                            dataset.setString(newds, "accreditedflag", paramlist.getString(0, "accreditedflag"));
                            dataset.setString(newds, "limitruleid", paramlist.getString(0, "limitruleid"));
                            String limitRuleId = paramlist.getValue(0, "limitruleid");
                            String limitRuleVersion = paramlist.getValue(0, "limitruleversionid");
                            if (limitRuleId.length() > 0 && (limitRuleVersion.length() == 0 || "C".equalsIgnoreCase(limitRuleVersion))) {
                                if (this.limitRuleCache.get(limitRuleId) == null) {
                                    String limitRuleCurrentVersion = SdiInfo.getCurrentVersion("LimitRule", limitRuleId, null, new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                                    this.limitRuleCache.put(limitRuleId, limitRuleCurrentVersion);
                                    limitRuleVersion = this.limitRuleCache.get(limitRuleId);
                                } else {
                                    limitRuleVersion = this.limitRuleCache.get(limitRuleId);
                                }
                            }
                            dataset.setString(newds, "limitruleversionid", limitRuleVersion);
                            dataset.setString(newds, "availabilityflag", StringUtil.getYN(availabilityflag, "Y"));
                            dataset.setNumber(newds, "usersequence", datasetsequence + 1);
                            if (this.cache.get("sdiMaxUserSequence") != null && this.transactionid.length() > 0) {
                                ((HashMap)this.cache.get("sdiMaxUserSequence")).put(sdiKey, new BigDecimal(datasetsequence + 1));
                            }
                            dataset.setString(newds, "approvalsequenceflag", paramlist.getString(0, "sequenceflag"));
                            dataset.setString(newds, "uniquenessflag", paramlist.getString(0, "uniquenessflag"));
                            dataset.setString(newds, "approvalpassrule", paramlist.getString(0, "passrule"));
                            dataset.setDate(newds, "createdt", now);
                            dataset.setString(newds, "createby", this.connectionInfo.getSysuserId());
                            dataset.setString(newds, "createtool", this.connectionInfo.getTool());
                            dataset.setDate(newds, "moddt", now);
                            dataset.setString(newds, "modby", this.connectionInfo.getSysuserId());
                            dataset.setString(newds, "modtool", this.connectionInfo.getTool());
                            if (StringUtil.getYN(properties.getProperty("adddataitems"), "Y").equals("Y")) {
                                if (this.cache.get("enterDefaultValue") == null) {
                                    this.cache.put("enterDefaultValue", this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("enterdefaultvaluesonadd").equals("Y"));
                                }
                                boolean enterDefaultValue = (Boolean)this.cache.get("enterDefaultValue");
                                this.addDataitems(dataitem, datalimits, paramlistitem, paramlimits, sdcid, keyid1, keyid2, keyid3, paramlistid, (String)paramlistversionid, variantid, datasetnum, properties.getProperty("paramid"), properties.getProperty("paramtype"), now, null, enterDefaultValue);
                            }
                            if (OpalUtil.isNotEmpty(paramlistcrosssdicalcs)) {
                                this.addSDIDataCrossSDICalcRules(sdidatacrosssdicalc, paramlistcrosssdicalcs, sdcid, keyid1, keyid2, keyid3, paramlistid, (String)paramlistversionid, variantid, datasetnum, now);
                            }
                            this.addApprovalsteps(dataapproval, filteredapprovalsteps, sdcid, keyid1, keyid2, keyid3, paramlistid, (String)paramlistversionid, variantid, datasetnum, now);
                            this.addReagentRelations(datarelations, paramlistid, (String)paramlistversionid, variantid, datasetnum, sdcid, keyid1, keyid2, keyid3, paramListReagentTypes);
                            String assignedAnalyst = dataset.getString(newds, "s_assignedanalyst", "");
                            String assignedDept = dataset.getString(newds, "s_assigneddepartment", "");
                            createWorksheet = "On Creation".equalsIgnoreCase(createWorkSheetRule) || "On Assignment".equalsIgnoreCase(createWorkSheetRule) && (assignedAnalyst.length() > 0 || assignedDept.length() > 0);
                            if (createWorksheet) {
                                AddDataSet.addDataForms(dataForms, sdcid, keyid1, keyid2, keyid3, paramlistid, (String)paramlistversionid, variantid, String.valueOf(datasetnum), formId, formVersionId, assignedAnalyst, assignedDept, this.getTranslationProcessor());
                            }
                            int keyCols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                            String plInstrType = paramlist.getValue(0, "s_instrumenttype");
                            String plInstrModel = paramlist.getValue(0, "s_instrumentmodel");
                            if (sdcid.equals("Sample") && addInstrToCalibrationDS && primaryImage != null && primaryImage.getRowCount() > 0 && (plInstrType.length() > 0 || plInstrModel.length() > 0)) {
                                int findRow;
                                filterMap = new HashMap();
                                filterMap.put(sdcProps.getProperty("keycolid1"), keyid1);
                                if (keyCols > 1) {
                                    filterMap.put(sdcProps.getProperty("keycolid2"), keyid2);
                                }
                                if (keyCols > 2) {
                                    filterMap.put(sdcProps.getProperty("keycolid3"), keyid3);
                                }
                                if ((findRow = primaryImage.findRow(filterMap)) > -1) {
                                    String instrumentId = primaryImage.getValue(findRow, "instrumentid");
                                    String classification = primaryImage.getValue(findRow, "classification");
                                    if (instrumentId.length() > 0 && classification.equalsIgnoreCase("Certification") && this.isInstrumentMeetsModelTypeOfParamList(instrumentId, plInstrType, plInstrModel)) {
                                        dataset.setString(newds, "s_instrumentid", instrumentId);
                                    }
                                }
                            }
                            if (isDSecEnabled) {
                                filterMap = new HashMap<String, String>();
                                filterMap.put(sdcProps.getProperty("keycolid1"), keyid1);
                                if (keyCols > 1) {
                                    filterMap.put(sdcProps.getProperty("keycolid2"), keyid2);
                                }
                                if (keyCols > 2) {
                                    filterMap.put(sdcProps.getProperty("keycolid3"), keyid3);
                                }
                                String securityDepartment = dataset.getString(newds, "securitydepartment", "");
                                String securityUser = dataset.getString(newds, "securityuser", "");
                                if (securityDepartment.length() == 0) {
                                    int findRow;
                                    securityDepartment = paramlist.getString(0, "securitydepartment", "");
                                    if (securityDepartment.length() == 0) {
                                        securityDepartment = this.connectionInfo.getDefaultDepartment();
                                    }
                                    if ((securityDepartment == null || securityDepartment.length() == 0) && primaryImage != null && primaryImage.getRowCount() > 0 && (findRow = primaryImage.findRow(filterMap)) > -1) {
                                        securityDepartment = primaryImage.getValue(findRow, "securitydepartment");
                                    }
                                }
                                if (securityUser.length() == 0) {
                                    String sysUserId = this.connectionInfo.getSysuserId();
                                    if (sysUserId == null || sysUserId.length() == 0 || "(system)".equalsIgnoreCase(sysUserId)) {
                                        int findRow;
                                        if (primaryImage != null && primaryImage.getRowCount() > 0 && (findRow = primaryImage.findRow(filterMap)) > -1) {
                                            securityUser = primaryImage.getValue(findRow, "securityuser");
                                        }
                                    } else {
                                        securityUser = this.connectionInfo.getSysuserId();
                                    }
                                }
                                dataset.setString(newds, "securitydepartment", securityDepartment);
                                dataset.setString(newds, "securityuser", securityUser);
                            }
                            HashMap<String, ArrayList<String>> hmskipped = new HashMap<String, ArrayList<String>>();
                            BaseSDIAttributeAction.coreCopyDownAttributes(attributeData, paramlistAttributes, datasetSDCProps, sdidataid, "", "", hmskipped, new M18NUtil(this.connectionInfo), this.logger);
                            BaseSDIAttributeAction.logSkipped(hmskipped, "DataSet", this.logger);
                        } else if (newfindrow >= 0) {
                            currentdsprop = properties.getProperty("newds" + String.valueOf(i));
                            if (currentdsprop.length() > 0) {
                                properties.setProperty("newds" + String.valueOf(i), currentdsprop + ";" + dataset.getValue(newfindrow, "dataset"));
                            } else {
                                properties.setProperty("newds" + String.valueOf(i), dataset.getValue(newfindrow, "dataset"));
                            }
                        } else if (existingdatasetNo >= 0) {
                            currentdsprop = properties.getProperty("newds" + String.valueOf(i));
                            if (currentdsprop.length() > 0) {
                                properties.setProperty("newds" + String.valueOf(i), currentdsprop + ";" + existingdatasetNo);
                            } else {
                                properties.setProperty("newds" + String.valueOf(i), "" + existingdatasetNo);
                            }
                        }
                        int newDSRow = newDataSetInstances.addRow();
                        newDataSetInstances.setString(newDSRow, "sdcid", sdcid);
                        newDataSetInstances.setString(newDSRow, "paramlistid", paramlistidprop[pl]);
                        newDataSetInstances.setString(newDSRow, "paramlistversionid", paramlistversionidprop[pl]);
                        newDataSetInstances.setString(newDSRow, "variantid", variantidprop[pl]);
                        newDataSetInstances.setString(newDSRow, "keyid1", keyid1);
                        newDataSetInstances.setString(newDSRow, "keyid2", keyid2);
                        newDataSetInstances.setString(newDSRow, "keyid3", keyid3);
                        newDataSetInstances.setString(newDSRow, "dataset", String.valueOf(datasetnum));
                        newDataSetInstances.setString(newDSRow, "sdidataid", sdidataid);
                    }
                    output_sdidataid.append(curr_sdidataid);
                    properties.setProperty("newdatasetinstancexml", newDataSetInstances.toXML());
                }
                properties.setProperty("sdidataid", output_sdidataid.toString());
                String traceLogId = properties.getProperty("tracelogid", "").trim();
                if (traceLogId.length() == 0) {
                    traceLogId = this.getTracelogid(sdcid, "Added datasets", properties.getProperty("auditreason"), properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"));
                }
                dataset.setString(-1, "tracelogid", traceLogId);
                dataitem.setString(-1, "tracelogid", traceLogId);
                dataapproval.setString(-1, "tracelogid", traceLogId);
                datalimits.setString(-1, "tracelogid", traceLogId);
                sdidatacrosssdicalc.setString(-1, "tracelogid", traceLogId);
                BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PreAddDataSet");
                SDIData sdiData = new SDIData();
                sdiData.setDataset("dataset", dataset);
                sdiData.setDataset("dataitem", dataitem);
                sdiData.setDataset("dataapproval", dataapproval);
                sdiData.setDataset("datalimit", datalimits);
                sdiData.setDataset("attribute", attributeData);
                sdiData.setDataset("sdidatacrosssdicalrule", sdidatacrosssdicalc);
                PostAddDataSetEventObject postAddDataSetEventObject = new PostAddDataSetEventObject(sdcid, sdcProps, sdiData, properties);
                boolean requiresSupplementalData = EventManager.requiresSupplementalData(sapphireConnection, this.getErrorHandler(), postAddDataSetEventObject);
                if (sdcPreRules.requiresAddDataSetPrimary() || sdcPreRules.customRulesRequiresAddDataSetPrimary() || requiresSupplementalData) {
                    sdcPreRules.setBeforeEditImage(beforeEditImage);
                    sdiData.setDataset("primary", beforeEditImage.getDataset("primary"));
                    postAddDataSetEventObject.setSupplementalData(beforeEditImage);
                    postAddDataSetEventObject.setRsetid(rsetid);
                }
                Trace.startBusinessRule(sdcid + "." + "PreAddDataSet", true);
                sdcPreRules.preAddDataSet(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PreAddDataSet", true);
                Trace.startBusinessRule(sdcid + "." + "PreAddDataSet", false);
                for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                    customRules.preAddDataSet(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PreAddDataSet", false);
                sdcPreRules.endRule();
                String evaluatedWorkitemitemid = properties.getProperty("__evaluatedworkitemitemidrule");
                if (evaluatedWorkitemitemid.length() > 0 && OpalUtil.isNotEmpty(dsSDIWI = this.getQueryProcessor().getPreparedSqlDataSet("select * from sdiworkitem where KEYID1 = ? and KEYID2 = ? and KEYID3 = ? and SDCID = ? and workitemid = ? and workiteminstance = ?", (Object[])new String[]{dataset.getString(0, "keyid1"), dataset.getString(0, "keyid2"), dataset.getString(0, "keyid3"), dataset.getString(0, "sdcid"), dataset.getString(0, "sourceworkitemid"), dataset.getValue(0, "sourceworkiteminstance")}))) {
                    WorkItemUtil.updateSDIDataWapColumns(dsSDIWI, dataset, this.database, this.getActionProcessor(), this.connectionInfo);
                }
                DataSetUtil.insert(this.database, dataset, "sdidata");
                this.logger.info("Done insert sdidata");
                if (evaluatedWorkitemitemid.length() > 0) {
                    String sourceWorkItemId = properties.getProperty("sourceworkitemid");
                    String sourceWorkItemInstance = properties.getProperty("sourceworkiteminstance");
                    WorkItemItemRuleEvaluator.updateSDIWorkItemItem(newDataSetInstances.toXML(), sourceWorkItemId, sourceWorkItemInstance, evaluatedWorkitemitemid, this.database, this.getActionProcessor(), this.connectionInfo, this.getSDCProcessor(), this.logger);
                    WorkItemItemRuleEvaluator.updateSDIDataUserSequence(newDataSetInstances.toXML(), sourceWorkItemId, sourceWorkItemInstance, this.database);
                    if (!properties.getProperty("skipqcbatchreagentsync", "N").equalsIgnoreCase("Y") && properties.getProperty("s_qcbatchid").length() > 0) {
                        QCUtil.postAddDataSetSyncQCBatchReagentInstrument(newDataSetInstances.toXML(), this.getActionProcessor(), this.getQueryProcessor());
                    }
                }
                if (attributeData.getRowCount() > 0) {
                    DataSetUtil.insert(this.database, attributeData, "sdiattribute");
                }
                DataSet rsetItemsDS = new DataSet(this.connectionInfo);
                if (useQuickInsertSQL4SDIDataItem) {
                    if (OpalUtil.isEmpty(rsetItemsDS)) {
                        this.createRsetDS(dataset, rsetItemsDS, rsetid);
                    }
                    this.logger.info("Before insert sdidataitem - Prepared RsetItemDS. sdidataitem count: " + dataitem.getRowCount());
                    String currentdt = this.getConnectionProcessor().isOra() ? "SYSDATE" : "GetDate()";
                    String currentUser = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                    StringBuffer quickInsertSQL = new StringBuffer();
                    quickInsertSQL.append("Insert into sdidataitem (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, ").append("paramid, paramtype, replicateid,aliasid, datatypes, mandatoryflag, displayunits, displayformat, operatorrule, ").append("transformdeferflag, transformrule,entrysdcid, entryreftypeid, calcrule, usersequence, releasedflag, measurementactionid, ").append("instrumentfieldid,uncertaintyfunction, uncertaintydisplayformat, uncertaintyfunctionupper, uncertaintydisplayformatupper, ").append("uncertaintyasymmetricflag, reportflag, qctransferflag, createdt, createby, createtool, moddt, modby, modtool )");
                    quickInsertSQL.append("select r.sdcid, r.keyid1, r.keyid2, r.keyid3, pli.paramlistid, pli.paramlistversionid, pli.variantid, r.dataset, ").append("pli.paramid, pli.paramtype, 1, pli.aliasid, pli.datatypes, pli.mandatoryflag, pli.displayunits, pli.displayformat, pli.operatorrule, ").append("coalesce(pli. Transformdeferflag,'N'), pli.transformrule,pli.entrysdcid, pli.entryreftypeid, pli.calcrule, pli.usersequence, 'N', pli.measurementactionid, ").append("pli.instrumentfieldid,pli.uncertaintyfunction, pli.uncertaintydisplayformat, pli.uncertaintyfunctionupper, pli.uncertaintydisplayformatupper, ").append("pli.uncertaintyasymmetricflag, pli.reportflag, pli.qctransferflag, ").append(currentdt).append(", ").append("'" + currentUser + "'").append(" , 'AddDataset', ").append(currentdt).append(", ").append("'" + currentUser + "'").append(" , 'AddDataset' ").append(" from paramlistitem pli inner join rsetitemsds r on pli.paramlistid = r.paramlistid and pli.paramlistversionid = r.paramlistversionid and pli.variantid = r.variantid where r.rsetid= ?");
                    int numRows = this.database.executePreparedUpdate(quickInsertSQL.toString(), new Object[]{rsetid});
                    this.logger.info("Done insert sdidataitem - Quick Insert. No of Rows Added: " + numRows);
                    HashMap<String, BigDecimal> filterMap = new HashMap<String, BigDecimal>();
                    filterMap.put("replicateid", new BigDecimal("1"));
                    DataSet newDS = dataitem.getFilteredDataSet(filterMap, true);
                    DataSetUtil.insert(this.database, newDS, "sdidataitem");
                    this.logger.info("Done insert sdidataitem");
                } else {
                    DataSetUtil.insert(this.database, dataitem, "sdidataitem");
                    this.logger.info("Done insert sdidataitem");
                }
                if (useQuickInsertSQL4SDIDataItemLimit) {
                    if (OpalUtil.isEmpty(rsetItemsDS)) {
                        this.createRsetDS(dataset, rsetItemsDS, rsetid);
                    }
                    this.logger.info("Before insert sdidataitemlimits - Prepared RsetItemDS. sdidataitemlimits count: " + datalimits.getRowCount());
                    String quickInsertSQL = "INSERT INTO sdidataitemlimits (       sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid, usersequence    , limittypeid, operator, value1, value2, value1num, value2num, unitsid    , limitfailedactionid ) SELECT sdi.sdcid, sdi.keyid1, sdi.keyid2, sdi.keyid3, sdi.paramlistid, sdi.paramlistversionid, sdi.variantid, sdi.dataset, sdi.paramid, sdi.paramtype, sdi.replicateid, pli.usersequence    , pli.limittypeid, pli.operator, pli.value1, pli.value2, pli.value1num, pli.value2num, pli.unitsid    , pli.limitfailedactionid FROM paramlimits pli     INNER JOIN sdidataitem sdi on pli.paramlistid = sdi.paramlistid and pli.variantid = sdi.variantid and pli.paramlistversionid = sdi.paramlistversionid and pli.paramid = sdi.paramid and pli.paramtype = sdi.paramtype    INNER JOIN rsetitemsds r on sdi.sdcid = r.sdcid and sdi.keyid1 = r.keyid1 and sdi.keyid2=r.keyid2 and sdi.keyid3=r.keyid3 and sdi.paramlistid = r.paramlistid and sdi.paramlistversionid = r.paramlistversionid and sdi.variantid = r.variantid and sdi.dataset = r.dataset WHERE r.rsetid= ? ";
                    int numRows = this.database.executePreparedUpdate(quickInsertSQL, new Object[]{rsetid});
                    this.logger.info("Done insert sdidataitemlimits - Quick Insert. No of Rows Added: " + numRows);
                } else {
                    DataSetUtil.insert(this.database, datalimits, "sdidataitemlimits");
                    this.logger.info("Done insert sdidataitemlimits");
                }
                if (OpalUtil.isNotEmpty(rsetItemsDS)) {
                    this.database.executePreparedUpdate("DELETE rsetitemsds WHERE rsetid = ?", new Object[]{rsetid});
                }
                DataSetUtil.insert(this.database, dataapproval, "sdidataapproval");
                this.logger.info("Done insert sdidataapproval");
                DataSetUtil.insert(this.database, sdidatacrosssdicalc, "sdidatacrosssdicalc");
                this.logger.info("Done insert sdidatacrosssdicalc");
                StringBuffer sql = new StringBuffer("INSERT INTO sdidataitemspec (sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, ");
                sql.append(" variantid, dataset, paramid, paramtype, replicateid, specid, specversionid, usersequence, reportflag ) ");
                sql.append(" ( ");
                sql.append(" SELECT sdidataitem.sdcid, sdidataitem.keyid1,sdidataitem.keyid2, sdidataitem.keyid3, ");
                sql.append(" sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, ");
                sql.append(" sdidataitem.dataset, sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.replicateid, ");
                sql.append(" sdispec.specid, sdispec.specversionid, specparamitems.usersequence, specparamitems.reportflag  ");
                sql.append(" FROM sdidataitem, sdispec, specparamitems ");
                sql.append(" WHERE sdidataitem.sdcid = '").append(sdcid).append("' ");
                sql.append(" AND sdidataitem.keyid1=? ");
                sql.append(" AND sdidataitem.keyid2=? ");
                sql.append(" AND sdidataitem.keyid3=? ");
                sql.append(" AND sdidataitem.paramlistid=? ");
                sql.append(" AND sdidataitem.paramlistversionid=? ");
                sql.append(" AND sdidataitem.variantid=? ");
                sql.append(" AND sdidataitem.dataset=? ");
                sql.append(" AND sdispec.sdcid  = sdidataitem.sdcid ");
                sql.append(" AND sdispec.keyid1 = sdidataitem.keyid1 ");
                sql.append(" AND sdispec.keyid2 = sdidataitem.keyid2 ");
                sql.append(" AND sdispec.keyid3 = sdidataitem.keyid3 AND coalesce( sdispec.appliedflag, 'Y') = 'Y'");
                sql.append(" AND specparamitems.specid = sdispec.specid ");
                sql.append(" AND specparamitems.specversionid = sdispec.specversionid ");
                sql.append(" AND sdidataitem.paramid = specparamitems.paramid ");
                sql.append(" AND sdidataitem.paramtype = specparamitems.paramtype  ");
                sql.append(" AND ");
                sql.append(" (  ");
                sql.append(" ( ");
                sql.append(" specparamitems.allowanyparamlistflag = 'Y'  ");
                sql.append(" ) ");
                sql.append(" OR ");
                sql.append(" ( ");
                sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
                sql.append(" AND sdidataitem.paramlistversionid = specparamitems.paramlistversionid ");
                sql.append(" AND sdidataitem.variantid = specparamitems.variantid ");
                sql.append(" AND (specparamitems.allowanyparamlistflag = 'N' OR specparamitems.allowanyparamlistflag is null OR specparamitems.allowanyparamlistflag = '') ");
                sql.append(" ) ");
                sql.append(" OR ");
                sql.append(" (  ");
                sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
                sql.append(" AND sdidataitem.variantid = specparamitems.variantid ");
                sql.append(" AND specparamitems.allowanyparamlistflag = 'V' ");
                sql.append(" ) ");
                sql.append(" OR ");
                sql.append(" (  ");
                sql.append(" sdidataitem.paramlistid = specparamitems.paramlistid ");
                sql.append(" AND specparamitems.allowanyparamlistflag = 'A' ");
                sql.append(" ) ");
                sql.append(" ) ");
                sql.append(" ) ");
                Trace.logDebug("sql = " + sql.toString());
                try {
                    PreparedStatement ps = this.database.prepareStatement(sql.toString());
                    int rows = dataset.getRowCount();
                    for (int row = 0; row < rows; ++row) {
                        ps.setString(1, dataset.getString(row, "keyid1"));
                        ps.setString(2, dataset.getString(row, "keyid2"));
                        ps.setString(3, dataset.getString(row, "keyid3"));
                        ps.setString(4, dataset.getString(row, "paramlistid"));
                        ps.setString(5, dataset.getString(row, "paramlistversionid"));
                        ps.setString(6, dataset.getString(row, "variantid"));
                        ps.setBigDecimal(7, dataset.getBigDecimal(row, "dataset"));
                        ps.executeUpdate();
                    }
                    this.database.closeStatement();
                }
                catch (SQLException e) {
                    throw new SapphireException("RECONCILE_DI_SPECS", "Exception generated trying to add dataitem spec information", e);
                }
                this.logger.info("Done insert sdidataitemspec");
                if (datarelations.getRowCount() > 0) {
                    PropertyList dataRelationProps = new PropertyList();
                    dataRelationProps.setProperty("sdcid", datarelations.getString(0, "sdcid", sdcid));
                    dataRelationProps.setProperty("keyid1", datarelations.getColumnValues("keyid1", ";"));
                    dataRelationProps.setProperty("keyid2", datarelations.getColumnValues("keyid2", ";"));
                    dataRelationProps.setProperty("keyid3", datarelations.getColumnValues("keyid3", ";"));
                    dataRelationProps.setProperty("paramlistid", datarelations.getColumnValues("paramlistid", ";"));
                    dataRelationProps.setProperty("paramlistversionid", datarelations.getColumnValues("paramlistversionid", ";"));
                    dataRelationProps.setProperty("variantid", datarelations.getColumnValues("variantid", ";"));
                    dataRelationProps.setProperty("dataset", datarelations.getColumnValues("dataset", ";"));
                    dataRelationProps.setProperty("relationtype", datarelations.getColumnValues("relationtype", ";"));
                    dataRelationProps.setProperty("relationfunction", datarelations.getColumnValues("relationfunction", ";"));
                    dataRelationProps.setProperty("requiredamount", datarelations.getColumnValues("requiredamount", ";"));
                    dataRelationProps.setProperty("requiredamountunits", datarelations.getColumnValues("requiredamountunits", ";"));
                    dataRelationProps.setProperty("requiredamountunitstype", datarelations.getColumnValues("requiredamountunitstype", ";"));
                    dataRelationProps.setProperty("mandatoryflag", datarelations.getColumnValues("mandatoryflag", ";"));
                    dataRelationProps.setProperty("sourcesdcid", datarelations.getColumnValues("sourcesdcid", ";"));
                    dataRelationProps.setProperty("sourcekeyid1", datarelations.getColumnValues("sourcekeyid1", ";"));
                    dataRelationProps.setProperty("sourcekeyid2", datarelations.getColumnValues("sourcekeyid2", ";"));
                    this.getActionProcessor().processAction("AddSDIDataRelation", "1", dataRelationProps);
                }
                AddDataSet.createWorksheet(dataForms, this.getActionProcessor(), this.logger, true, sapphireConnection);
                if (propsMatchCall) {
                    properties.put("__sdidata", sdiData);
                } else {
                    EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), postAddDataSetEventObject);
                }
                BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PostAddDataSet");
                sdcPostRules.setBeforeEditImage(beforeEditImage);
                Trace.startBusinessRule(sdcid + "." + "PostAddDataSet", true);
                sdcPostRules.postAddDataSet(sdiData, properties);
                Trace.endBusinessRule(sdcid + "." + "PostAddDataSet", true);
                Trace.startBusinessRule(sdcid + "." + "PostAddDataSet", false);
                for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                    customRules.postAddDataSet(sdiData, properties);
                }
                Trace.endBusinessRule(sdcid + "." + "PostAddDataSet", false);
                sdcPostRules.endRule();
                if (deleterset) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            } else {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET whilst adding datasets");
            }
        }
    }

    private void validateParamListExpiry(DataSet paramList) throws SapphireException {
        StringBuffer message = new StringBuffer();
        for (int pl = 0; pl < paramList.size(); ++pl) {
            String sql;
            String paramlistid = paramList.getString(pl, "paramlistid");
            String paramlistversionid = paramList.getString(pl, "paramlistversionid");
            String variantid = paramList.getString(pl, "variantid");
            if ("C".equals(paramlistversionid)) {
                sql = "SELECT paramlistid, paramlistversionid FROM paramlist WHERE paramlistid=? and variantid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (paramlistversionid as integer) desc";
                this.database.createPreparedResultSet("CurrentVersion", sql, new Object[]{paramlistid, variantid});
                if (this.database.getNext("CurrentVersion")) {
                    paramlistversionid = this.database.getString("CurrentVersion", "paramlistversionid");
                    continue;
                }
                if (message.length() > 0) {
                    message.append(", ").append("<br>");
                }
                message.append("ParamList ").append("-").append(paramlistid).append(";").append(paramlistversionid).append(";").append(variantid).append(" is Expired");
                continue;
            }
            sql = "SELECT paramlistid, paramlistversionid FROM paramlist WHERE paramlistid=? and variantid=? and paramlistversionid=? and ( versionstatus='E' ) order by versionstatus, cast (paramlistversionid as integer) desc";
            this.database.createPreparedResultSet("ExpiredVersion", sql, new Object[]{paramlistid, variantid, paramlistversionid});
            DataSet ds = new DataSet(this.database.getResultSet("ExpiredVersion"));
            if (ds.size() <= 0) continue;
            for (int i = 0; i < ds.size(); ++i) {
                if (message.length() > 0) {
                    message.append(", ").append("<br>");
                }
                message.append("ParamList ").append("-").append(paramlistid).append(";").append(paramlistversionid).append(";").append(variantid).append(" is Expired");
            }
        }
        if (message.length() > 0) {
            throw new SapphireException("Failed to add dataSet due to expired ParamList.", message.toString());
        }
    }

    private void addSDIDataCrossSDICalcRules(DataSet sdidatacrosssdicalc, DataSet paramlistcrosssdicalcs, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, int datasetnum, Calendar now) {
        this.logger.info("Adding SDI Data CrossSDI Calculation rule  details...");
        for (int i = 0; i < paramlistcrosssdicalcs.size(); ++i) {
            int newRow = sdidatacrosssdicalc.addRow();
            sdidatacrosssdicalc.setString(newRow, "sdcid", sdcid);
            sdidatacrosssdicalc.setString(newRow, "keyid1", keyid1);
            sdidatacrosssdicalc.setString(newRow, "keyid2", keyid2);
            sdidatacrosssdicalc.setString(newRow, "keyid3", keyid3);
            sdidatacrosssdicalc.setString(newRow, "paramlistid", paramlistid);
            sdidatacrosssdicalc.setString(newRow, "paramlistversionid", paramlistversionid);
            sdidatacrosssdicalc.setString(newRow, "variantid", variantid);
            sdidatacrosssdicalc.setNumber(newRow, "dataset", datasetnum);
            sdidatacrosssdicalc.setString(newRow, "crosssdicalcdefid", paramlistcrosssdicalcs.getValue(i, "crosssdicalcdefid"));
            sdidatacrosssdicalc.setDate(newRow, "createdt", now);
            sdidatacrosssdicalc.setString(newRow, "createby", this.connectionInfo.getSysuserId());
            sdidatacrosssdicalc.setString(newRow, "createtool", this.connectionInfo.getTool());
            sdidatacrosssdicalc.setDate(newRow, "moddt", now);
            sdidatacrosssdicalc.setString(newRow, "modby", this.connectionInfo.getSysuserId());
            sdidatacrosssdicalc.setString(newRow, "modtool", this.connectionInfo.getTool());
        }
    }

    private void addApprovalsteps(DataSet dataapproval, DataSet approvalsteps, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, int datasetnum, Calendar now) {
        this.logger.info("Adding dataapproval details...");
        for (int i = 0; i < approvalsteps.size(); ++i) {
            int newRow = dataapproval.addRow();
            dataapproval.setString(newRow, "sdcid", sdcid);
            dataapproval.setString(newRow, "keyid1", keyid1);
            dataapproval.setString(newRow, "keyid2", keyid2);
            dataapproval.setString(newRow, "keyid3", keyid3);
            dataapproval.setString(newRow, "paramlistid", paramlistid);
            dataapproval.setString(newRow, "paramlistversionid", paramlistversionid);
            dataapproval.setString(newRow, "variantid", variantid);
            dataapproval.setNumber(newRow, "dataset", datasetnum);
            dataapproval.setString(newRow, "approvalstep", approvalsteps.getString(i, "approvalstep"));
            dataapproval.setString(newRow, "approvalflag", "U");
            dataapproval.setString(newRow, "mandatoryflag", approvalsteps.getString(i, "mandatoryflag"));
            dataapproval.setString(newRow, "forcepeerflag", approvalsteps.getString(i, "forcepeerflag"));
            dataapproval.setString(newRow, "roleid", approvalsteps.getString(i, "roleid"));
            dataapproval.setNumber(newRow, "usersequence", approvalsteps.getBigDecimal(i, "usersequence"));
            dataapproval.setDate(newRow, "createdt", now);
            dataapproval.setString(newRow, "createby", this.connectionInfo.getSysuserId());
            dataapproval.setString(newRow, "createtool", this.connectionInfo.getTool());
            dataapproval.setDate(newRow, "moddt", now);
            dataapproval.setString(newRow, "modby", this.connectionInfo.getSysuserId());
            dataapproval.setString(newRow, "modtool", this.connectionInfo.getTool());
        }
    }

    private void createRsetDS(DataSet dataset, DataSet rsetItemsDS, String rsetid) throws SapphireException {
        for (int i = 0; i < dataset.getRowCount(); ++i) {
            int newRow = rsetItemsDS.addRow();
            rsetItemsDS.setString(newRow, "rsetid", rsetid);
            rsetItemsDS.setString(newRow, "sdcid", dataset.getString(i, "sdcid"));
            rsetItemsDS.setString(newRow, "keyid1", dataset.getString(i, "keyid1"));
            rsetItemsDS.setString(newRow, "keyid2", dataset.getString(i, "keyid2", ""));
            rsetItemsDS.setString(newRow, "keyid3", dataset.getString(i, "keyid3", ""));
            rsetItemsDS.setString(newRow, "paramlistid", dataset.getString(i, "paramlistid"));
            rsetItemsDS.setString(newRow, "paramlistversionid", dataset.getString(i, "paramlistversionid"));
            rsetItemsDS.setString(newRow, "variantid", dataset.getString(i, "variantid"));
            rsetItemsDS.setNumber(newRow, "dataset", dataset.getInt(i, "dataset"));
        }
        DataSetUtil.insert(this.database, rsetItemsDS, "rsetitemsds");
    }

    private boolean determineQuickInsertionNeeded(PropertyList properties, String key) throws SapphireException {
        boolean quickInsertNeeded = properties.getProperty(key, "").length() > 0 ? "Y".equals(StringUtil.getYN(properties.getProperty(key, "Y"), "Y")) : "Y".equals(this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty(key, "Y"));
        quickInsertNeeded = quickInsertNeeded && (properties.getProperty("param", "").length() == 0 || "all".equalsIgnoreCase(properties.getProperty("param", ""))) && properties.getProperty("paramid", "").length() == 0 && properties.getProperty("paramtype", "").length() == 0;
        return quickInsertNeeded;
    }

    private void addReagentRelations(DataSet datarelations, String paramlistid, String paramlistversionid, String variantid, int datasetnum, String sdcid, String keyid1, String keyid2, String keyid3, DataSet paramListReagentTypes) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("paramlistid", paramlistid);
        filterMap.put("paramlistversionid", paramlistversionid);
        filterMap.put("variantid", variantid);
        DataSet filteredDS = paramListReagentTypes.getFilteredDataSet(filterMap);
        int size = filteredDS.getRowCount();
        for (int i = 0; i < size; ++i) {
            int newRow = datarelations.addRow();
            datarelations.setString(newRow, "sdcid", sdcid);
            datarelations.setString(newRow, "keyid1", keyid1);
            datarelations.setString(newRow, "keyid2", keyid2);
            datarelations.setString(newRow, "keyid3", keyid3);
            datarelations.setString(newRow, "paramlistid", paramlistid);
            datarelations.setString(newRow, "paramlistversionid", paramlistversionid);
            datarelations.setString(newRow, "variantid", variantid);
            datarelations.setNumber(newRow, "dataset", datasetnum);
            datarelations.setString(newRow, "relationtype", filteredDS.getString(i, "reagenttypeid"));
            datarelations.setString(newRow, "requiredamount", filteredDS.getValue(i, "amount"));
            datarelations.setString(newRow, "requiredamountunits", filteredDS.getString(i, "amountunits"));
            datarelations.setString(newRow, "requiredamountunitstype", filteredDS.getString(i, "amountunitstype"));
            datarelations.setString(newRow, "mandatoryflag", filteredDS.getString(i, "mandatoryflag", ""));
            datarelations.setString(newRow, "sourcesdcid", "LV_ReagentType");
            datarelations.setString(newRow, "sourcekeyid1", filteredDS.getString(i, "reagenttypeid"));
            datarelations.setString(newRow, "sourcekeyid2", filteredDS.getString(i, "reagenttypeversionid"));
            datarelations.setString(newRow, "relationfunction", "Reagent");
        }
    }

    private DataSet getParamListReagentTypes(String paramListIdProp, String paramListVersionIdProp, String variantIdProp) throws SapphireException {
        DataSet resolvedPL = AddDataSet.resolveCurrentPL(paramListIdProp, paramListVersionIdProp, variantIdProp, this.database, this.connectionInfo);
        String[] plIdArr = StringUtil.split(resolvedPL.getColumnValues("paramlistid", ";"), ";");
        String[] plVerIdArr = StringUtil.split(resolvedPL.getColumnValues("paramlistversionid", ";"), ";");
        String[] variantIdArr = StringUtil.split(resolvedPL.getColumnValues("variantid", ";"), ";");
        StringBuffer sql = new StringBuffer();
        String rsetid = null;
        SafeSQL safeSQL = new SafeSQL();
        if (plIdArr.length >= 50) {
            rsetid = this.getDAMProcessor().createRSet("ParamList", paramListIdProp, paramListVersionIdProp, variantIdProp);
            sql.append("SELECT paramlistid, paramlistversionid, variantid, reagenttypeid, reagenttypeversionid,amount,amountunits,amountunitstype,mandatoryflag ").append(" FROM paramlistreagenttype p, rsetitems r").append(" WHERE ").append(" r.sdcid = 'ParamList'").append(" AND r.keyid1 = p.paramlistid").append(" AND r.keyid2 = p.paramlistversionid").append(" AND r.keyid3 = p.variantid").append(" AND r.rsetid = ").append(safeSQL.addVar(rsetid));
        } else {
            sql.append("SELECT paramlistid, paramlistversionid, variantid, reagenttypeid, reagenttypeversionid,amount,amountunits,amountunitstype,mandatoryflag ").append(" FROM paramlistreagenttype").append(" WHERE ").append(" ( ");
            for (int i = 0; i < plIdArr.length; ++i) {
                if (i != 0) {
                    sql.append(" OR ");
                }
                sql.append(" ( ").append(" paramlistid = ").append(safeSQL.addVar(plIdArr[i])).append(" AND paramlistversionid = ").append(safeSQL.addVar(plVerIdArr[i])).append(" AND variantid = ").append(safeSQL.addVar(variantIdArr[i]));
                sql.append(" ) ");
            }
            sql.append(" ) ");
        }
        this.logger.info("ParamList Reagent Types sql: " + sql.toString());
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (rsetid != null) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        return ds;
    }

    boolean isInstrumentMeetsModelTypeOfParamList(String instrumentId, String plInstrType, String plInstrModel) throws SapphireException {
        DataSet dsInstr = (DataSet)this.cache.get("Instrument_" + instrumentId);
        if (dsInstr == null) {
            this.database.createPreparedResultSet("instrumentdetail", "SELECT instrumenttype, instrumentmodelid FROM instrument WHERE instrumentid = ?", new String[]{instrumentId});
            dsInstr = new DataSet(this.database.getResultSet("instrumentdetail"));
            this.database.closeResultSet("instrumentdetail");
            this.cache.put("Instrument_" + instrumentId, dsInstr);
        }
        if (plInstrModel.length() > 0) {
            return plInstrModel.equals(dsInstr.getValue(0, "instrumentmodelid")) && plInstrType.equals(dsInstr.getValue(0, "instrumenttype"));
        }
        return plInstrType.equals(dsInstr.getValue(0, "instrumenttype"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void startTransactionCache(String transactionid) {
        HashMap<String, HashMap> hashMap = transactionCache;
        synchronized (hashMap) {
            if (transactionCache.size() > 100) {
                transactionCache.clear();
            }
            HashMap cache = new HashMap();
            transactionCache.put(transactionid, cache);
            HashSet processedSDIParamListSet = new HashSet();
            cache.put("processedSDIParamListSet", processedSDIParamListSet);
        }
    }

    public static void endTransactionCache(String transactionid) {
        transactionCache.remove(transactionid);
    }
}


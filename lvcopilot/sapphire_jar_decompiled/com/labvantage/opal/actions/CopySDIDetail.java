/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.util.SDIProps;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.actions.sdidata.AddDataSet;
import com.labvantage.sapphire.actions.spec.AddSDISpec;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import com.labvantage.sapphire.ajax.operations.GetForceNewPolicy;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CopySDIDetail
extends BaseAction
implements sapphire.action.CopySDIDetail {
    private Map<String, String> productItemMap = new HashMap<String, String>();

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String primarySpecs;
        String primaryDatasets;
        String primaryWorkitems;
        String addnewonly;
        String sourcekeyid3;
        String sourcekeyid2;
        String sourcekeyid1;
        String sourcesdcid;
        String targetkeyid3;
        String targetkeyid2;
        String targetkeyid1;
        String targetsdcid;
        block72: {
            targetsdcid = props.getProperty("sdcid");
            targetkeyid1 = props.getProperty("keyid1");
            targetkeyid2 = props.getProperty("keyid2");
            targetkeyid3 = props.getProperty("keyid3");
            sourcesdcid = props.getProperty("sourcesdcid");
            sourcekeyid1 = props.getProperty("sourcekeyid1");
            sourcekeyid2 = props.getProperty("sourcekeyid2");
            sourcekeyid3 = props.getProperty("sourcekeyid3");
            addnewonly = props.getProperty("addnewonly");
            targetkeyid2 = StringUtil.getLen(targetkeyid2) == 0L ? "(null)" : targetkeyid2;
            targetkeyid3 = StringUtil.getLen(targetkeyid3) == 0L ? "(null)" : targetkeyid3;
            sourcekeyid2 = StringUtil.getLen(sourcekeyid2) == 0L ? "(null)" : sourcekeyid2;
            String string = sourcekeyid3 = StringUtil.getLen(sourcekeyid3) == 0L ? "(null)" : sourcekeyid3;
            if (targetkeyid1 == null || targetkeyid1.length() == 0) {
                try {
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    targetkeyid1 = props.getProperty("newkeyid1");
                }
                catch (ActionException e) {
                    ErrorHandler err = e.getErrorHandler();
                    if (err == null || !err.hasErrors()) break block72;
                    this.setErrors(err);
                }
            }
        }
        if (StringUtil.getLen(primaryWorkitems = props.getProperty("primaryWorkitems")) > 0L) {
            String[] productdArray;
            sourcesdcid = "";
            sourcekeyid1 = "";
            HashMap<String, Map<String, String>> productMap = new HashMap<String, Map<String, String>>();
            for (String productworkitem : productdArray = StringUtil.split(primaryWorkitems, "|")) {
                Map<String, String> workitemMap;
                String product = productworkitem.substring(0, productworkitem.indexOf("%3B"));
                String[] workitem = StringUtil.split(productworkitem.substring(productworkitem.indexOf("%3B") + 3), ";");
                String workitemid = workitem[0];
                String workitemversionid = workitem[1];
                if (productMap.containsKey(product)) {
                    workitemMap = (Map)productMap.get(product);
                    workitemMap.put("workitemid", (String)workitemMap.get("workitemid") + ";" + workitemid);
                    workitemMap.put("workitemversionid", (String)workitemMap.get("workitemversionid") + ";" + workitemversionid);
                    productMap.put(product, workitemMap);
                    continue;
                }
                workitemMap = new HashMap();
                workitemMap.put("workitemid", workitemid);
                workitemMap.put("workitemversionid", workitemversionid);
                productMap.put(product, workitemMap);
            }
            if (productMap.size() > 0) {
                PropertyList actionProps = new PropertyList();
                for (String product : productMap.keySet()) {
                    actionProps.clear();
                    String sampleid = this.getProductItems(product, targetkeyid1);
                    String workitemid = ((Map)productMap.get(product)).get("workitemid").toString();
                    String workitemversionid = ((Map)productMap.get(product)).get("workitemversionid").toString();
                    if (StringUtil.getLen(sampleid) <= 0L || StringUtil.getLen(workitemid) <= 0L) continue;
                    actionProps.setProperty("sdcid", "Sample");
                    actionProps.setProperty("keyid1", sampleid);
                    actionProps.setProperty("workitemid", workitemid);
                    actionProps.setProperty("workitemversionid", workitemversionid);
                    actionProps.setProperty("applyworkitem", "Y");
                    try {
                        this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), actionProps);
                    }
                    catch (ActionException e) {
                        this.logger.error("Action Error", e);
                    }
                }
            }
        }
        if (StringUtil.getLen(primaryDatasets = props.getProperty("primaryDatasets")) > 0L) {
            String[] productdArray;
            sourcesdcid = "";
            sourcekeyid1 = "";
            HashMap<String, Map<String, String>> productMap = new HashMap<String, Map<String, String>>();
            for (String productDataset : productdArray = StringUtil.split(primaryDatasets, "|")) {
                Map<String, String> map;
                String product = productDataset.substring(0, productDataset.indexOf("%3B"));
                String[] dataset = StringUtil.split(productDataset.substring(productDataset.indexOf("%3B") + 3), ";");
                String paramlistid = dataset[0];
                String paramlistversionid = dataset[1];
                String variantid = dataset[2];
                if (productMap.containsKey(product)) {
                    map = (Map)productMap.get(product);
                    map.put("paramlistid", (String)map.get("paramlistid") + ";" + paramlistid);
                    map.put("paramlistversionid", (String)map.get("paramlistversionid") + ";" + paramlistversionid);
                    map.put("variantid", (String)map.get("variantid") + ";" + variantid);
                    productMap.put(product, map);
                    continue;
                }
                map = new HashMap();
                ((HashMap)map).put("paramlistid", paramlistid);
                ((HashMap)map).put("paramlistversionid", paramlistversionid);
                ((HashMap)map).put("variantid", variantid);
                productMap.put(product, map);
            }
            if (productMap.size() > 0) {
                PropertyList actionProps = new PropertyList();
                for (String product : productMap.keySet()) {
                    actionProps.clear();
                    String sampleid = this.getProductItems(product, targetkeyid1);
                    String paramlistid = (String)((Map)productMap.get(product)).get("paramlistid");
                    String paramlistversionid = (String)((Map)productMap.get(product)).get("paramlistversionid");
                    String variantid = (String)((Map)productMap.get(product)).get("variantid");
                    if (StringUtil.getLen(sampleid) <= 0L || StringUtil.getLen(paramlistid) <= 0L) continue;
                    actionProps.setProperty("sdcid", "Sample");
                    actionProps.setProperty("keyid1", sampleid);
                    actionProps.setProperty("paramlistid", paramlistid);
                    actionProps.setProperty("paramlistversionid", paramlistversionid);
                    actionProps.setProperty("variantid", variantid);
                    try {
                        this.getActionProcessor().processActionClass(AddDataSet.class.getName(), actionProps);
                    }
                    catch (ActionException e) {
                        this.logger.error("Action Error", e);
                    }
                }
            }
        }
        if (StringUtil.getLen(primarySpecs = props.getProperty("primarySpecs")) > 0L) {
            String[] productdArray;
            sourcesdcid = "";
            sourcekeyid1 = "";
            HashMap<String, Map<String, String>> productMap = new HashMap<String, Map<String, String>>();
            for (String productSpec : productdArray = StringUtil.split(primarySpecs, "|")) {
                Map<String, String> map;
                String autoapplyFlag;
                String product = productSpec.substring(0, productSpec.indexOf("%3B"));
                String[] dataset = StringUtil.split(productSpec.substring(productSpec.indexOf("%3B") + 3), ";");
                String specid = dataset[0];
                String specversionid = dataset[1];
                String oosGeneratingFlag = dataset.length > 2 ? dataset[2] : "";
                String applyFlag = "Y".equalsIgnoreCase(oosGeneratingFlag) ? "Y" : "";
                String string = autoapplyFlag = dataset.length > 3 && dataset[3] != null && dataset[3].length() > 0 ? dataset[3] : applyFlag;
                if (productMap.containsKey(product)) {
                    map = (Map)productMap.get(product);
                    map.put("specid", (String)map.get("specid") + ";" + specid);
                    map.put("specversionid", (String)map.get("specversionid") + ";" + specversionid);
                    map.put("oosgeneratingflag", (String)map.get("oosgeneratingflag") + ";" + oosGeneratingFlag);
                    map.put("autoapplyflag", (String)map.get("autoapplyflag") + ";" + autoapplyFlag);
                    productMap.put(product, map);
                    continue;
                }
                map = new HashMap();
                ((HashMap)map).put("specid", specid);
                ((HashMap)map).put("specversionid", specversionid);
                ((HashMap)map).put("oosgeneratingflag", oosGeneratingFlag);
                ((HashMap)map).put("autoapplyflag", autoapplyFlag);
                productMap.put(product, map);
            }
            if (productMap.size() > 0) {
                PropertyList actionProps = new PropertyList();
                for (String product : productMap.keySet()) {
                    actionProps.clear();
                    String sampleid = this.getProductItems(product, targetkeyid1);
                    String specid = (String)((Map)productMap.get(product)).get("specid");
                    String specversionid = (String)((Map)productMap.get(product)).get("specversionid");
                    String autoapplyFlag = (String)((Map)productMap.get(product)).get("autoapplyflag");
                    String oosGeneratingFlag = (String)((Map)productMap.get(product)).get("oosgeneratingflag");
                    if (StringUtil.getLen(sampleid) <= 0L || StringUtil.getLen(specid) <= 0L) continue;
                    actionProps.setProperty("sdcid", "Sample");
                    actionProps.setProperty("keyid1", sampleid);
                    actionProps.setProperty("specid", specid);
                    actionProps.setProperty("specversionid", specversionid);
                    actionProps.setProperty("applyspec", autoapplyFlag);
                    actionProps.setProperty("autoapplyflag", autoapplyFlag);
                    actionProps.setProperty("oosgeneratingflag", oosGeneratingFlag);
                    try {
                        this.getActionProcessor().processActionClass(AddSDISpec.class.getName(), actionProps);
                    }
                    catch (ActionException e) {
                        this.logger.error("Action Error", e);
                    }
                }
            }
        }
        if (StringUtil.getLen(sourcesdcid) == 0L) {
            return;
        }
        if (StringUtil.getLen(sourcekeyid1) == 0L) {
            return;
        }
        if (sourcekeyid1.indexOf(";") >= 0) {
            String[] sourceKeyid1s;
            boolean hasSourceKeyid1 = false;
            for (String k : sourceKeyid1s = StringUtil.split(sourcekeyid1, ";")) {
                if (k == null || k.trim().length() <= 0) continue;
                hasSourceKeyid1 = true;
            }
            if (!hasSourceKeyid1) {
                return;
            }
        }
        boolean copyDatasetFlag = "Y".equalsIgnoreCase(props.getProperty("copydataset", "N"));
        boolean copySpecFlag = "Y".equalsIgnoreCase(props.getProperty("copyspec", "N"));
        boolean applySpecFlag = !"Never".equalsIgnoreCase(props.getProperty("specapplyflag", "")) && !"N".equalsIgnoreCase(props.getProperty("specapplyflag", ""));
        boolean copyWorkitemFlag = "Y".equalsIgnoreCase(props.getProperty("copyworkitem", "N"));
        boolean copyCurrentVersionFlag = "Y".equalsIgnoreCase(props.getProperty("usecurrentversion", "N"));
        ArrayList<SDIProps> sourceKeyList = new ArrayList<SDIProps>();
        String[] sourcekeyarray = StringUtil.split(sourcekeyid1, ";");
        String[] sourcekey2array = StringUtil.split(sourcekeyid2, ";");
        String[] sourcekey3array = StringUtil.split(sourcekeyid3, ";");
        String[] targetKeyArray = StringUtil.split(targetkeyid1, ";");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String sourceTableid = sdcProcessor.getProperty(sourcesdcid, "tableid");
        String sourceKeycolid1 = sdcProcessor.getProperty(sourcesdcid, "keycolid1");
        String sourceKeycolid2 = sdcProcessor.getProperty(sourcesdcid, "keycolid2");
        boolean sourcesdc_versioned = "Y".equalsIgnoreCase(sdcProcessor.getProperty(sourcesdcid, "versionedflag"));
        String sql = "SELECT " + sourceKeycolid1 + " itemid," + sourceKeycolid2 + " itemversion FROM " + sourceTableid + " WHERE " + sourceKeycolid1 + " = ?  AND ( versionstatus = 'P' or versionstatus = 'C' ) order by versionstatus, cast (" + sourceKeycolid2 + " as numeric) desc";
        PreparedStatement psmtGetCurrentVersion = this.database.prepareStatement("GetCurrentVersion", sql);
        if (sourcekeyarray.length == targetKeyArray.length) {
            try {
                for (int i = 0; i < sourcekeyarray.length; ++i) {
                    SQLGenerator __SqlGenerator;
                    String sourceKey3;
                    SDI _sdi;
                    SDIProps sdiProps;
                    String sourceKey2;
                    String string = sourceKey2 = sourcekey2array.length <= i || sourcekey2array[i] == null || sourcekey2array[i].length() == 0 || sourcekey2array[i].equals("(null)") ? null : sourcekey2array[i];
                    if (sourceKey2 == null && sourcesdc_versioned) {
                        psmtGetCurrentVersion.setString(1, sourcekeyarray[i]);
                        DataSet dsCurrentVersion = new DataSet(psmtGetCurrentVersion.executeQuery());
                        if (dsCurrentVersion.getRowCount() > 0) {
                            sourceKey2 = dsCurrentVersion.getString(0, "itemversion");
                        }
                    }
                    if (!sourceKeyList.contains(sdiProps = new SDIProps(this.database, _sdi = new SDI(sourcesdcid, sourcekeyarray[i], sourceKey2, sourceKey3 = sourcekey3array.length <= i || sourcekey3array[i] == null || sourcekey3array[i].length() == 0 || sourcekey3array[i].equals("(null)") ? null : sourcekey3array[i]), __SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra()), this.getSDCProcessor(), copyDatasetFlag, copySpecFlag, copyWorkitemFlag))) {
                        try {
                            sdiProps.addKey(targetKeyArray[i]);
                            sdiProps.init();
                            sourceKeyList.add(sdiProps);
                        }
                        catch (SapphireException e) {
                            this.logger.error("sdiProps error", e);
                        }
                        continue;
                    }
                    for (SDIProps sdi : sourceKeyList) {
                        if (!sdi.equals(sdiProps)) continue;
                        sdi.addKey(targetKeyArray[i]);
                    }
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        } else {
            this.setError("");
        }
        this.database.closeStatement("GetCurrentVersion");
        PropertyList actionprops = new PropertyList();
        DataSet sdiattributesToAdd = new DataSet();
        DataSet sdiattributesToEdit = new DataSet();
        DataSet allExistingTargetAttributes = new DataSet();
        for (SDIProps sdiProps : sourceKeyList) {
            actionprops.clear();
            if (copyDatasetFlag) {
                this.logger.debug("Copying DataSet(s)...");
                String paramListId = sdiProps.getParamListID();
                if (paramListId != null) {
                    String paramlistVersionId = sdiProps.getParamListVersionID();
                    String paramlistVariantId = sdiProps.getVariantID();
                    if (paramListId.contains("ProductInformation")) {
                        StringBuilder plId = new StringBuilder();
                        StringBuilder plVerId = new StringBuilder();
                        StringBuilder plVarId = new StringBuilder();
                        String[] plArray = StringUtil.split(paramListId, ";");
                        String[] plVerArray = StringUtil.split(paramListId, ";");
                        String[] plVarArray = StringUtil.split(paramListId, ";");
                        for (int i = 0; i < plArray.length; ++i) {
                            String pl = plArray[i];
                            if ("ProductInformation".equals(pl)) continue;
                            plId.append(";").append(pl);
                            plVerId.append(";").append(plVerArray[i]);
                            plVarId.append(";").append(plVarArray[i]);
                        }
                        if (plId.length() > 0) {
                            paramListId = plId.substring(1);
                            paramlistVersionId = plVerId.substring(1);
                            paramlistVariantId = plVarId.substring(1);
                        } else {
                            paramListId = "";
                            paramlistVersionId = "";
                            paramlistVariantId = "";
                        }
                    }
                    if (paramListId.length() > 0) {
                        if (copyCurrentVersionFlag) {
                            int paramListCount = StringUtil.split(paramListId, ";").length;
                            String newParamListVersionId = StringUtil.repeat(";C", paramListCount);
                            paramlistVersionId = newParamListVersionId.substring(1);
                        }
                        try {
                            actionprops.setProperty("sdcid", targetsdcid);
                            actionprops.setProperty("keyid1", sdiProps.getKey());
                            actionprops.setProperty("keyid2", targetkeyid2);
                            actionprops.setProperty("keyid3", targetkeyid3);
                            actionprops.setProperty("paramlistid", paramListId);
                            actionprops.setProperty("paramlistversionid", paramlistVersionId);
                            actionprops.setProperty("variantid", paramlistVariantId);
                            actionprops.setProperty("addnewonly", addnewonly);
                            this.getActionProcessor().processActionClass(AddDataSet.class.getName(), actionprops);
                        }
                        catch (ActionException e) {
                            this.logger.error("Exception caught while copying datasets. ", e);
                        }
                    }
                }
            }
            if (copySpecFlag) {
                this.logger.debug("Copying Spec(s)...");
                String specid = sdiProps.getSpecID();
                String specAutoApplyFlag = sdiProps.getSpecAppliedFlag();
                String specOOSGeneratingApplyFlag = sdiProps.getSpecOOSGeneratingFlag();
                if (specid != null) {
                    String specversionid;
                    if (copyCurrentVersionFlag) {
                        int specCount = StringUtil.split(specid, ";").length;
                        String newSpecversionId = StringUtil.repeat(";C", specCount);
                        specversionid = newSpecversionId.substring(1);
                    } else {
                        specversionid = sdiProps.getSpecVersionID();
                    }
                    try {
                        actionprops.setProperty("sdcid", targetsdcid);
                        actionprops.setProperty("keyid1", sdiProps.getKey());
                        actionprops.setProperty("keyid2", targetkeyid2);
                        actionprops.setProperty("keyid3", targetkeyid3);
                        actionprops.setProperty("specid", specid);
                        actionprops.setProperty("specversionid", specversionid);
                        int specIdCount = StringUtil.split(specid, ";").length;
                        if (specAutoApplyFlag == null || specAutoApplyFlag.length() == 0) {
                            StringBuffer autoApplySpec = new StringBuffer();
                            for (int c = 0; c < specIdCount; ++c) {
                                autoApplySpec.append(";Y");
                            }
                            specAutoApplyFlag = autoApplySpec.substring(1);
                        }
                        if (specOOSGeneratingApplyFlag == null || specOOSGeneratingApplyFlag.length() == 0) {
                            StringBuffer oosFlag = new StringBuffer();
                            for (int c = 0; c < specIdCount; ++c) {
                                oosFlag.append(";Y");
                            }
                            specOOSGeneratingApplyFlag = oosFlag.substring(1);
                        }
                        if (applySpecFlag) {
                            actionprops.setProperty("applyspec", specAutoApplyFlag);
                        } else {
                            StringBuffer applySpec = new StringBuffer();
                            for (int c = 0; c < specIdCount; ++c) {
                                applySpec.append(";N");
                            }
                            actionprops.setProperty("applyspec", applySpec.substring(1));
                        }
                        actionprops.setProperty("autoapplyflag", specAutoApplyFlag);
                        actionprops.setProperty("oosgeneratingflag", specOOSGeneratingApplyFlag);
                        this.getActionProcessor().processActionClass(AddSDISpec.class.getName(), actionprops);
                    }
                    catch (ActionException e) {
                        this.logger.debug("Exception caught while copying specifications. ", e);
                    }
                }
            }
            if (!copyWorkitemFlag) continue;
            this.logger.debug("Copying Workitem(s)...");
            if (StringUtil.getLen(sdiProps.getWorkitemID()) <= 0L) continue;
            DataSet sdiWorkItems = new DataSet();
            sdiWorkItems.addColumnValues("workitemid", 0, sdiProps.getWorkitemID(), ";");
            sdiWorkItems.addColumnValues("workitemversionid", 0, sdiProps.getWorkitemVersionID(), ";");
            sdiWorkItems.addColumnValues("workitemtypeflag", 0, sdiProps.getWorkitemTypeFlag(), ";");
            sdiWorkItems.addColumnValues("reflexrule", 0, sdiProps.getWorkitemReflexRule(), ";");
            HashMap<String, String> filterWI = new HashMap<String, String>();
            filterWI.put("workitemtypeflag", "P");
            DataSet pkgSdiWorkitems = sdiWorkItems.getFilteredDataSet(filterWI);
            filterWI.put("workitemtypeflag", "W");
            DataSet nonpkgSdiWorkitems = sdiWorkItems.getFilteredDataSet(filterWI);
            boolean nonPkgWIRuleExists = false;
            if ("Y".equalsIgnoreCase(props.getProperty("applysourceworkitem", "N"))) {
                for (int n = 0; n < nonpkgSdiWorkitems.getRowCount(); ++n) {
                    String reflexRule = nonpkgSdiWorkitems.getValue(n, "reflexrule");
                    if (reflexRule.length() <= 0 || "null".equalsIgnoreCase(reflexRule)) continue;
                    nonPkgWIRuleExists = true;
                    break;
                }
            }
            try {
                DataSet dsInstance;
                String forceNew = GetForceNewPolicy.analyzeForceNewPolicy("Y", this.getConfigurationProcessor());
                if (nonPkgWIRuleExists) {
                    StringBuffer workItemIds = new StringBuffer();
                    StringBuffer workItemVersionIds = new StringBuffer();
                    workItemIds.append("_Reflex");
                    workItemVersionIds.append("1");
                    workItemIds.append(pkgSdiWorkitems.getRowCount() > 0 ? ";" + pkgSdiWorkitems.getColumnValues("workitemid", ";") : "");
                    workItemVersionIds.append(pkgSdiWorkitems.getRowCount() > 0 ? ";" + pkgSdiWorkitems.getColumnValues("workitemversionid", ";") : "");
                    actionprops.clear();
                    actionprops.setProperty("sdcid", targetsdcid);
                    actionprops.setProperty("keyid1", sdiProps.getKey());
                    actionprops.setProperty("keyid2", targetkeyid2);
                    actionprops.setProperty("keyid3", targetkeyid3);
                    actionprops.setProperty("applyworkitem", props.getProperty("applysourceworkitem", "N"));
                    actionprops.setProperty("forcenew", forceNew);
                    actionprops.setProperty("workitemid", workItemIds.toString());
                    actionprops.setProperty("workitemversionid", workItemVersionIds.toString());
                    actionprops.setProperty("__sourcesdcid", sdiProps.getSDI().getSdcid());
                    actionprops.setProperty("__sourcekeyids", sdiProps.getSDI().getKeyid1() + ";" + sdiProps.getSDI().getKeyid2() + ";" + sdiProps.getSDI().getKeyid3());
                    this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), actionprops);
                } else {
                    actionprops.clear();
                    actionprops.setProperty("sdcid", targetsdcid);
                    actionprops.setProperty("keyid1", sdiProps.getKey());
                    actionprops.setProperty("keyid2", targetkeyid2);
                    actionprops.setProperty("keyid3", targetkeyid3);
                    actionprops.setProperty("applyworkitem", props.getProperty("applysourceworkitem", "N"));
                    actionprops.setProperty("forcenew", forceNew);
                    actionprops.setProperty("workitemid", sdiProps.getWorkitemID());
                    actionprops.setProperty("workitemversionid", sdiProps.getWorkitemVersionID());
                    actionprops.setProperty("reflexrule", sdiProps.getWorkitemReflexRule());
                    this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), actionprops);
                }
                if ((dsInstance = new DataSet(actionprops.getProperty("newworkiteminstancexml"))).getRowCount() <= 0) continue;
                SDI sourceSDI = sdiProps.getSDI();
                String sourceSDCId = sourceSDI.sdcid;
                String source_keyid1 = sourceSDI.keyid1;
                String source_keyid2 = sourceSDI.keyid2;
                String source_keyid3 = sourceSDI.keyid3;
                QueryProcessor qp = this.getQueryProcessor();
                DataSet dsSourceSDIWIAttributes = qp.getPreparedSqlDataSet("select a.*, w.workitemid, w.workitemversionid from sdiattribute a, sdiworkitem w  where a.sdcid = ? and  a.keyid1 = w.sdiworkitemid and  w.sdcid = ? and w.keyid1 = ? and w.keyid2 = ? and w.keyid3 = ? ", (Object[])new String[]{"SDIWorkItem", sourceSDCId, source_keyid1, source_keyid2, source_keyid3}, true);
                if (nonPkgWIRuleExists) {
                    SafeSQL safeSQL = new SafeSQL();
                    dsInstance = qp.getPreparedSqlDataSet("select * from sdiworkitem sw1, sdiworkitem sw2 where sw1.sdcid = sw2.sdcid and sw1.keyid1 = sw2.keyid1 and sw1.keyid2 = sw2.keyid2 and sw1.keyid3 = sw2.keyid3  and sw1.groupid = sw2.groupid and sw1.groupinstance = sw2.groupinstance and sw1.workitemtypeflag = " + safeSQL.addVar("W") + " and sw2.sdiworkitemid in(" + safeSQL.addIn(dsInstance.getColumnValues("sdiworkitemid", "','")) + ")", safeSQL.getValues());
                }
                if (dsSourceSDIWIAttributes.getRowCount() <= 0) continue;
                CopySDIDetail.copyDownSourceAttributes(dsSourceSDIWIAttributes, dsInstance, qp, this.getSDCProcessor(), allExistingTargetAttributes, new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.logger, new M18NUtil(this.connectionInfo), sdiattributesToAdd, sdiattributesToEdit);
            }
            catch (SapphireException e) {
                this.logger.error(" Exception caught while copying workitems. ", e);
            }
        }
        if (sdiattributesToAdd.getRowCount() > 0 || sdiattributesToEdit.getRowCount() > 0) {
            CopySDIDetail.addEditAttributes(sdiattributesToAdd, sdiattributesToEdit, allExistingTargetAttributes, this.logger, this.database);
        }
    }

    private String getProductItems(String product, String keyid1) throws SapphireException {
        if (!this.productItemMap.containsKey(product)) {
            DataSet ds;
            String[] p = StringUtil.split(product, ";");
            String productid = p[0];
            String productversionid = "1";
            try {
                productversionid = p[1];
            }
            catch (Exception exception) {
                // empty catch block
            }
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            String sampleRsetId = this.getDAMProcessor().createRSet("Sample", keyid1, null, null);
            sql.append("select s.s_sampleid from s_sample s, rsetitems r where s.s_sampleid = r.keyid1 and r.rsetid = ").append(safeSQL.addVar(sampleRsetId));
            if (!"(blank)".equals(productid)) {
                sql.append(" and productid = ").append(safeSQL.addVar(productid)).append(" and productversionid = ").append(safeSQL.addVar(productversionid));
            }
            if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null) {
                this.productItemMap.put(product, ds.getColumnValues("s_sampleid", ";"));
            }
        }
        return this.productItemMap.get(product);
    }

    public static void copyDownSDIDetails(PropertyList toSDCProps, DataSet toPrimary, DataSet toPrimaryBeforeEditImage, List<PropertyList> copyDownSDIDetailsList, SDCProcessor sdcProcessor, ActionProcessor actionProcessor, SDIProcessor sdiProcessor) throws ActionException {
        if (toSDCProps == null) {
            throw new IllegalArgumentException("To SDC props is null");
        }
        if (toPrimary == null) {
            throw new IllegalArgumentException("To primary is null");
        }
        if (copyDownSDIDetailsList == null) {
            throw new IllegalArgumentException("Copy down SDI details list is null");
        }
        if (sdcProcessor == null) {
            throw new IllegalArgumentException("SDC processor is null");
        }
        if (actionProcessor == null) {
            throw new IllegalArgumentException("Action processor is null");
        }
        if (sdiProcessor == null) {
            throw new IllegalArgumentException("SDI processor is null");
        }
        String toSdcId = toSDCProps.getProperty("sdcid");
        String toKeyColumn1 = toSDCProps.getProperty("keycolid1");
        String toKeyColumn2 = toSDCProps.getProperty("keycolid2");
        String toKeyColumn3 = toSDCProps.getProperty("keycolid3");
        for (int i = 0; i < toPrimary.getRowCount(); ++i) {
            for (PropertyList copyDownSDIDetails : copyDownSDIDetailsList) {
                boolean templateSDI;
                String fromSdcId = copyDownSDIDetails.getProperty("sdcid");
                String toForeignKeyColumn1 = copyDownSDIDetails.getProperty("fkcolumnid");
                String toForeignKeyColumn2 = copyDownSDIDetails.getProperty("fkcolumnid2");
                String toForeignKeyColumn3 = copyDownSDIDetails.getProperty("fkcolumnid3");
                PropertyList fromSDCProps = sdcProcessor.getProperties(fromSdcId);
                String fromKeyColumn1 = fromSDCProps.getProperty("keycolid1");
                String fromKeyColumn2 = fromSDCProps.getProperty("keycolid2");
                String fromKeyColumn3 = fromSDCProps.getProperty("keycolid3");
                String fromKeyId1 = toPrimary.getString(i, toForeignKeyColumn1, "");
                String fromKeyId2 = "";
                String fromKeyId3 = "";
                if (!toForeignKeyColumn2.isEmpty()) {
                    fromKeyId2 = toPrimary.getString(i, toForeignKeyColumn2, "");
                    if (!toForeignKeyColumn3.isEmpty()) {
                        fromKeyId3 = toPrimary.getString(i, toForeignKeyColumn3, "");
                    }
                }
                boolean isEdit = toPrimaryBeforeEditImage != null;
                boolean isFromKeyChanged = false;
                if (isEdit) {
                    String oldFromKeyId1 = SdiInfo.getOldPrimaryValue(toSDCProps, toKeyColumn1, toKeyColumn2, toKeyColumn3, toPrimary, toPrimaryBeforeEditImage, i, toForeignKeyColumn1, "");
                    String oldFromKeyId2 = "";
                    String oldFromKeyId3 = "";
                    if (!toForeignKeyColumn2.isEmpty()) {
                        oldFromKeyId2 = SdiInfo.getOldPrimaryValue(toSDCProps, toKeyColumn1, toKeyColumn2, toKeyColumn3, toPrimary, toPrimaryBeforeEditImage, i, toForeignKeyColumn2, "");
                        if (!toForeignKeyColumn3.isEmpty()) {
                            oldFromKeyId3 = SdiInfo.getOldPrimaryValue(toSDCProps, toKeyColumn1, toKeyColumn2, toKeyColumn3, toPrimary, toPrimaryBeforeEditImage, i, toForeignKeyColumn3, "");
                        }
                    }
                    if (!(oldFromKeyId1.equals(fromKeyId1) && oldFromKeyId2.equals(fromKeyId2) && oldFromKeyId3.equals(fromKeyId3))) {
                        isFromKeyChanged = true;
                    }
                }
                if (fromKeyId1.isEmpty() || isEdit && !isFromKeyChanged) continue;
                SDIRequest request = new SDIRequest();
                request.setSDIList(fromSdcId, fromKeyId1, fromKeyId2.isEmpty() ? "" : fromKeyId2, fromKeyId3.isEmpty() ? "" : fromKeyId3);
                request.setRequestItem("primary");
                DataSet fromPrimary = sdiProcessor.getSDIData(request).getDataset("primary");
                if (fromPrimary.getRowCount() <= 0) continue;
                PropertyList copySDIDetailProps = new PropertyList(copyDownSDIDetails.getPropertyListNotNull("copysdidetailsdetails"));
                copySDIDetailProps.setProperty("sdcid", toSdcId);
                copySDIDetailProps.setProperty("keyid1", toPrimary.getString(i, toKeyColumn1, ""));
                if (!toKeyColumn2.isEmpty()) {
                    copySDIDetailProps.setProperty("keyid2", toPrimary.getString(i, toKeyColumn2, ""));
                    if (!toKeyColumn3.isEmpty()) {
                        copySDIDetailProps.setProperty("keyid3", toPrimary.getString(i, toKeyColumn3, ""));
                    }
                }
                copySDIDetailProps.setProperty("sourcesdcid", fromSdcId);
                copySDIDetailProps.setProperty("sourcekeyid1", fromPrimary.getString(0, fromKeyColumn1, ""));
                if (!toForeignKeyColumn2.isEmpty()) {
                    copySDIDetailProps.setProperty("sourcekeyid2", fromPrimary.getString(0, fromKeyColumn2, ""));
                    if (!toForeignKeyColumn3.isEmpty()) {
                        copySDIDetailProps.setProperty("sourcekeyid3", fromPrimary.getString(0, fromKeyColumn3, ""));
                    }
                }
                if (templateSDI = "Y".equalsIgnoreCase(toPrimary.getString(i, "templateflag", ""))) {
                    copySDIDetailProps.setProperty("specapplyflag", "N");
                }
                actionProcessor.processAction("CopySDIDetail", "1", copySDIDetailProps);
            }
        }
    }

    public static void copyDownSourceAttributes(DataSet dsSourceSDIWIAttributes, DataSet dsInstance, QueryProcessor qp, SDCProcessor sdcp, DataSet allExistingTargetAttributes, SapphireConnection sc, Logger logger, M18NUtil m18N, DataSet sdiattributesToAdd, DataSet sdiattributesToEdit) throws SapphireException {
        dsSourceSDIWIAttributes.sort("workitemid, workitemversionid");
        ArrayList<DataSet> wiSourceAttributeGroups = dsSourceSDIWIAttributes.getGroupedDataSets("workitemid, workitemversionid");
        for (int i = 0; i < wiSourceAttributeGroups.size(); ++i) {
            DataSet wiSourceAttributes = wiSourceAttributeGroups.get(i);
            String workitemid = wiSourceAttributes.getValue(0, "workitemid");
            String workitemversionid = wiSourceAttributes.getValue(0, "workitemversionid", "C");
            if ("C".equalsIgnoreCase(workitemversionid)) {
                workitemversionid = SdiInfo.getCurrentVersion("WorkItem", workitemid, null, sc);
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("workitemid", workitemid);
            filter.put("workitemversionid", workitemversionid);
            DataSet dsFilterSDIWI = dsInstance.getFilteredDataSet(filter);
            if (dsFilterSDIWI.getRowCount() <= 0) continue;
            for (int k = 0; k < dsFilterSDIWI.getRowCount(); ++k) {
                String sdiworkitemid = dsFilterSDIWI.getValue(k, "sdiworkitemid");
                DataSet targetExistingAttributes = qp.getPreparedSqlDataSet("select a.* from sdiattribute a where a.sdcid = ? and  a.keyid1 = ?  ", (Object[])new String[]{"SDIWorkItem", sdiworkitemid}, true);
                allExistingTargetAttributes.copyRow(targetExistingAttributes, -1, 1);
                DataSet sourceLinkAttributes = new DataSet();
                DataSet sourceAdhocAttributes = new DataSet();
                wiSourceAttributes.sort("attributeid, attributeinstance");
                for (int w = 0; w < wiSourceAttributes.getRowCount(); ++w) {
                    Object v;
                    String attributeid = wiSourceAttributes.getValue(w, "attributeid");
                    String attributesourcetype = wiSourceAttributes.getValue(w, "attributesourcetype");
                    BigDecimal attributeinstance = wiSourceAttributes.getBigDecimal(w, "attributeinstance");
                    String attributesdcid = wiSourceAttributes.getValue(w, "attributesdcid");
                    String sdcid = wiSourceAttributes.getValue(w, "sdcid");
                    HashMap<String, Object> findAttribute = new HashMap<String, Object>();
                    findAttribute.put("attributeid", attributeid);
                    findAttribute.put("attributeinstance", attributeinstance);
                    findAttribute.put("sdcid", sdcid);
                    findAttribute.put("attributesdcid", attributesdcid);
                    if (targetExistingAttributes.findRow(findAttribute) < 0) {
                        if ("Adhoc".equalsIgnoreCase(attributesourcetype)) {
                            sourceAdhocAttributes.copyRow(wiSourceAttributes, w, 1);
                            continue;
                        }
                        sourceLinkAttributes.copyRow(wiSourceAttributes, w, 1);
                        continue;
                    }
                    String atdatatype = wiSourceAttributes.getValue(w, "datatype", "S");
                    boolean editRow = false;
                    if (atdatatype.equalsIgnoreCase("n")) {
                        v = wiSourceAttributes.getBigDecimal(w, "numericvalue");
                        if (v != null) {
                            editRow = true;
                        }
                    } else if (atdatatype.equalsIgnoreCase("d") || atdatatype.equalsIgnoreCase("o")) {
                        v = wiSourceAttributes.getCalendar(w, "datevalue");
                        if (v != null) {
                            editRow = true;
                        }
                    } else if (atdatatype.equalsIgnoreCase("c")) {
                        v = wiSourceAttributes.getClob(w, "clobvalue");
                        if (v != null && ((String)v).length() > 0) {
                            editRow = true;
                        }
                    } else {
                        v = wiSourceAttributes.getString(w, "textvalue");
                        if (v != null && ((String)v).length() > 0) {
                            editRow = true;
                        }
                    }
                    if (!editRow) continue;
                    sdiattributesToEdit.copyRow(wiSourceAttributes, w, 1);
                    sdiattributesToEdit.setString(sdiattributesToEdit.getRowCount() - 1, "keyid1", sdiworkitemid);
                }
                if (sourceLinkAttributes.getRowCount() > 0) {
                    HashMap<String, ArrayList<String>> hmSkipped = new HashMap<String, ArrayList<String>>();
                    BaseSDIAttributeAction.coreCopyDownAttributes(sdiattributesToAdd, sourceLinkAttributes, sdcp.getPropertyList("SDIWorkItem"), sdiworkitemid, "", "", hmSkipped, m18N, logger, BaseSDIAttributeAction.AttributeType.link, true);
                    BaseSDIAttributeAction.logSkipped(hmSkipped, "SDIWorkItem", logger);
                }
                if (sourceAdhocAttributes.getRowCount() <= 0) continue;
                HashMap<String, ArrayList<String>> hmSkipped = new HashMap<String, ArrayList<String>>();
                BaseSDIAttributeAction.coreCopyDownAttributes(sdiattributesToAdd, sourceAdhocAttributes, sdcp.getPropertyList("SDIWorkItem"), sdiworkitemid, "", "", hmSkipped, m18N, logger, BaseSDIAttributeAction.AttributeType.adhoc, true);
                BaseSDIAttributeAction.logSkipped(hmSkipped, "SDIWorkItem", logger);
            }
        }
    }

    public static void addEditAttributes(DataSet sdiattributesToAdd, DataSet sdiattributesToEdit, DataSet allExistingTargetAttributes, Logger logger, DBAccess database) throws SapphireException {
        if (sdiattributesToAdd.getRowCount() > 0) {
            sdiattributesToAdd.removeColumn("workitemid");
            sdiattributesToAdd.removeColumn("workitemversionid");
            sdiattributesToAdd.sort("keyid1,attributeid");
            ArrayList<DataSet> attributeGroups = sdiattributesToAdd.getGroupedDataSets("keyid1,attributeid");
            for (int a = 0; a < attributeGroups.size(); ++a) {
                DataSet dsSDIWIAddAttrib = attributeGroups.get(a);
                String attributeid = dsSDIWIAddAttrib.getValue(0, "attributeid");
                String sdiwi_id = dsSDIWIAddAttrib.getValue(0, "keyid1");
                HashMap<String, String> findExisting = new HashMap<String, String>();
                findExisting.put("keyid1", sdiwi_id);
                findExisting.put("attributeid", attributeid);
                DataSet dsExistingAttributes = allExistingTargetAttributes.getFilteredDataSet(findExisting);
                if (dsExistingAttributes.getRowCount() <= 0) continue;
                dsExistingAttributes.sort("attributeinstance D");
                BigDecimal max_instance = dsExistingAttributes.getBigDecimal(0, "attributeinstance");
                for (int k = 0; k < dsSDIWIAddAttrib.getRowCount(); ++k) {
                    dsSDIWIAddAttrib.setNumber(k, "attributeinstance", max_instance.add(new BigDecimal(k + 1)));
                }
            }
            logger.info("Processing the sdiattributes inserts: \n" + sdiattributesToAdd);
            DataSetUtil.insert(database, sdiattributesToAdd, "sdiattribute");
        }
        if (sdiattributesToEdit.getRowCount() > 0) {
            sdiattributesToEdit.removeColumn("workitemid");
            sdiattributesToEdit.removeColumn("workitemversionid");
            logger.info("Processing update on the existing sdiattributes with values obtained from source sdiworkitem: \n" + sdiattributesToEdit);
            DataSetUtil.update(database, sdiattributesToEdit, "sdiattribute", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "attributeid", "attributesdcid", "attributeinstance"});
        }
    }
}


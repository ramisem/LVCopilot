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

import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.util.UnitsUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddAdhocConsumableEquipment
extends BaseAjaxRequest {
    private final String SDIDataRelation = "sdidatarelation";
    private final String SDIWorkItemRelation = "sdiworkitemrelation";
    private final String ReagentLotRecipe = "reagentlotrecipe";
    private final String QCBatchInstrument = "s_qcbatchinstrument";
    private final String QCBatchReagent = "s_qcbatchreagent";
    private final String TransferExecutionReagent = "transferexecutionreagent";
    private char decimalSeperator;
    private FormatUtil formatUtil;
    private final String specialDelimer = "^^^";
    private boolean isDataRelation;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
        ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
        this.formatUtil = FormatUtil.getInstance(connectionInfo);
        this.decimalSeperator = this.formatUtil.getDecimalSeparator();
        String tablename = ajaxResponse.getRequestParameter("tablename", "");
        this.isDataRelation = tablename.equalsIgnoreCase("sdidatarelation");
        if (this.isDataRelation || tablename.equalsIgnoreCase("sdiworkitemrelation")) {
            this.addSDIRelation(ajaxResponse);
        } else if (tablename.equalsIgnoreCase("reagentlotrecipe")) {
            this.addReagentLotRecipe(ajaxResponse);
        } else if (tablename.equalsIgnoreCase("s_qcbatchinstrument") || tablename.equalsIgnoreCase("s_qcbatchreagent")) {
            this.addQCBatchInstrumentReagent(ajaxResponse);
        } else if (tablename.equalsIgnoreCase("transferexecutionreagent")) {
            this.addTransferExecutionReagent(ajaxResponse);
        }
    }

    private void addSDIRelation(AjaxResponse ajaxResponse) {
        String tablename = ajaxResponse.getRequestParameter("tablename", "sdidatarelation");
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "Sample");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
        String paramlistid = ajaxResponse.getRequestParameter("paramlistid", "");
        String paramlistversionid = ajaxResponse.getRequestParameter("paramlistversionid", "");
        String variantid = ajaxResponse.getRequestParameter("variantid", "");
        String dataset = ajaxResponse.getRequestParameter("dataset", "");
        String workitemid = ajaxResponse.getRequestParameter("workitemid", "");
        String workiteminstance = ajaxResponse.getRequestParameter("workiteminstance", "");
        String relationfunction = ajaxResponse.getRequestParameter("relationfunction", "Reagent");
        boolean isReagent = relationfunction.equalsIgnoreCase("Reagent");
        String sourcesdcid = "LV_ReagentType";
        String sourcekeyid1 = ajaxResponse.getRequestParameter("sourcekeyid1", "");
        String sourcekeyid2 = ajaxResponse.getRequestParameter("sourcekeyid2", "");
        String sourcekeyid3 = ajaxResponse.getRequestParameter("sourcekeyid3", "");
        String relationtype = "";
        boolean managed = true;
        String instModel = sourcekeyid2;
        if (!isReagent) {
            sourcesdcid = sourcekeyid3.trim().length() > 0 ? "Instrument" : (sourcekeyid2.trim().length() > 0 ? "LV_InstrumentModel" : "LV_InstrumentType");
            SafeSQL safeSQL = new SafeSQL();
            String sql = sourcekeyid3.trim().length() > 0 ? "select instrumenttype instrumenttypeid,instrumentmodelid,'N' unmanagedflag  from instrument where instrumentid=" + safeSQL.addVar(sourcekeyid3) : (sourcekeyid2.trim().length() > 0 ? "select instrumenttypeid,instrumentmodelid,unmanagedflag from instrumentmodel where instrumentmodelid=" + safeSQL.addVar(sourcekeyid2) : "select instrumenttypeid,'' instrumentmodelid,unmanagedflag from instrumenttype where instrumenttypeid=" + safeSQL.addVar(sourcekeyid1));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds != null && ds.getRowCount() > 0) {
                sourcekeyid1 = ds.getString(0, "instrumenttypeid", "");
                sourcekeyid2 = ds.getString(0, "instrumentmodelid", "");
                managed = ds.getString(0, "unmanagedflag", "N").equalsIgnoreCase("N");
            }
            instModel = sourcekeyid2;
            relationtype = sourcekeyid1;
            if (sourcekeyid3.trim().length() > 0) {
                sourcekeyid1 = sourcekeyid3;
                sourcekeyid2 = "";
                sourcekeyid3 = "";
            } else if (sourcekeyid2.trim().length() > 0) {
                String sourcekeyid1Temp = sourcekeyid1;
                sourcekeyid1 = sourcekeyid2;
                sourcekeyid2 = sourcekeyid1Temp;
            }
        }
        String originalreagenttypeid = ajaxResponse.getRequestParameter("originalreagenttypeid", "");
        String originalreagenttypeversionid = ajaxResponse.getRequestParameter("originalreagenttypeversionid", "");
        if (isReagent && originalreagenttypeid.trim().length() == 0) {
            originalreagenttypeid = sourcekeyid1;
            originalreagenttypeversionid = sourcekeyid2;
        }
        if (isReagent && sourcekeyid1.trim().length() == 0) {
            sourcekeyid1 = originalreagenttypeid;
            sourcekeyid2 = originalreagenttypeversionid;
        }
        relationtype = isReagent ? originalreagenttypeid : relationtype;
        String errorMsg = "";
        if (relationtype.trim().length() > 0) {
            String tokeyid1 = ajaxResponse.getRequestParameter("tokeyid1", "");
            String tosdcid = tokeyid1.length() > 0 ? (isReagent ? "LV_ReagentLot" : "Instrument") : "";
            String refkeyid1 = ajaxResponse.getRequestParameter("refkeyid1", "");
            String refsdcid = refkeyid1.length() > 0 && isReagent ? "TrackItemSDC" : "";
            String amount = ajaxResponse.getRequestParameter("amount", "");
            String amountunit = "";
            String amountunittype = "";
            double amountD = 0.0;
            double totalUsedAmount = 0.0;
            int rowCount = 1;
            if (!isReagent && managed) {
                try {
                    rowCount = Integer.parseInt(ajaxResponse.getRequestParameter("amount", "1"));
                }
                catch (NumberFormatException e) {
                    rowCount = 1;
                }
            }
            if (isReagent) {
                amountD = (amount = UnitsUtil.convertToLocateSeperated(amount, "" + this.decimalSeperator)).trim().length() > 0 ? this.formatUtil.parseBigDecimal(amount).doubleValue() : 0.0;
                amountunit = ajaxResponse.getRequestParameter("amountunit", "");
                amountunittype = "";
                if (amountunit.equalsIgnoreCase("(Containers)")) {
                    amountunit = "";
                    amountunittype = "C";
                } else if (amountunit.trim().length() > 0) {
                    amountunittype = "U";
                }
            }
            ArrayList<String> uniqueRelation = new ArrayList<String>();
            String[] keyid1Arr = StringUtil.split(keyid1, ";");
            String[] keyid2Arr = StringUtil.split(keyid2, ";");
            String[] keyid3Arr = StringUtil.split(keyid3, ";");
            String[] paramlistidArr = StringUtil.split(paramlistid, ";");
            String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
            String[] variantidArr = StringUtil.split(variantid, ";");
            String[] datasetArr = StringUtil.split(dataset, ";");
            String[] workitemidArr = StringUtil.split(workitemid, ";");
            String[] workiteminstanceArr = StringUtil.split(workiteminstance, ";");
            String keyid1s = "";
            String keyid2s = "";
            String keyid3s = "";
            String paramlistids = "";
            String paramlistversionids = "";
            String variantids = "";
            String datasets = "";
            String workitemids = "";
            String workiteminstances = "";
            String relationfunctions = "";
            String relationtypes = "";
            String relationinstances = "";
            String sourcesdcids = "";
            String sourcekeyid1s = "";
            String sourcekeyid2s = "";
            String tosdcids = "";
            String tokeyid1s = "";
            String refsdcids = "";
            String refkeyid1s = "";
            String amounts = "";
            String amountunits = "";
            String amountunittypes = "";
            String originalreagenttypeids = "";
            String originalreagenttypeversionids = "";
            boolean isFirstRow = true;
            boolean checkDuplicateRelation = !managed || isReagent;
            HashMap<Object, Object> existingRelationtypes = new HashMap();
            if (checkDuplicateRelation) {
                existingRelationtypes = this.getExistingRealtions(ajaxResponse);
            }
            for (int i = 0; i < keyid1Arr.length; ++i) {
                String relationKey = keyid1Arr[i] + "^^^" + keyid2Arr[i] + "^^^" + keyid3Arr[i];
                relationKey = this.isDataRelation ? relationKey + "^^^" + paramlistidArr[i] + "^^^" + paramlistversionidArr[i] + "^^^" + variantidArr[i] + "^^^" + datasetArr[i] : relationKey + "^^^" + workitemidArr[i] + "^^^" + workiteminstanceArr[i];
                if (uniqueRelation.contains(relationKey)) continue;
                if (checkDuplicateRelation) {
                    String existingRelationtype = (String)existingRelationtypes.get(relationKey);
                    String value = "^^^" + relationtype;
                    if (!isReagent) {
                        value = value + "||" + instModel;
                    }
                    if (existingRelationtype != null && existingRelationtype.contains(value)) continue;
                }
                uniqueRelation.add(relationKey);
                for (int c = 0; c < rowCount; ++c) {
                    keyid1s = keyid1s + this.delimiter(isFirstRow) + keyid1Arr[i];
                    keyid2s = keyid2s + this.delimiter(isFirstRow) + keyid2Arr[i];
                    keyid3s = keyid3s + this.delimiter(isFirstRow) + keyid3Arr[i];
                    paramlistids = paramlistids + this.delimiter(isFirstRow) + paramlistidArr[i];
                    paramlistversionids = paramlistversionids + this.delimiter(isFirstRow) + paramlistversionidArr[i];
                    variantids = variantids + this.delimiter(isFirstRow) + variantidArr[i];
                    datasets = datasets + this.delimiter(isFirstRow) + datasetArr[i];
                    workitemids = workitemids + this.delimiter(isFirstRow) + workitemidArr[i];
                    workiteminstances = workiteminstances + this.delimiter(isFirstRow) + workiteminstanceArr[i];
                    relationfunctions = relationfunctions + this.delimiter(isFirstRow) + relationfunction;
                    relationtypes = relationtypes + this.delimiter(isFirstRow) + relationtype;
                    relationinstances = relationinstances + this.delimiter(isFirstRow) + (c + 1);
                    sourcesdcids = sourcesdcids + this.delimiter(isFirstRow) + sourcesdcid;
                    sourcekeyid1s = sourcekeyid1s + this.delimiter(isFirstRow) + sourcekeyid1;
                    sourcekeyid2s = sourcekeyid2s + this.delimiter(isFirstRow) + sourcekeyid2;
                    tosdcids = tosdcids + this.delimiter(isFirstRow) + tosdcid;
                    tokeyid1s = tokeyid1s + this.delimiter(isFirstRow) + tokeyid1;
                    refsdcids = refsdcids + this.delimiter(isFirstRow) + refsdcid;
                    refkeyid1s = refkeyid1s + this.delimiter(isFirstRow) + refkeyid1;
                    amounts = amounts + this.delimiter(isFirstRow) + amount;
                    if (isReagent) {
                        originalreagenttypeids = originalreagenttypeids + this.delimiter(isFirstRow) + originalreagenttypeid;
                        originalreagenttypeversionids = originalreagenttypeversionids + this.delimiter(isFirstRow) + originalreagenttypeversionid;
                        amountunits = amountunits + this.delimiter(isFirstRow) + amountunit;
                        amountunittypes = amountunittypes + this.delimiter(isFirstRow) + amountunittype;
                        if (amountD > 0.0) {
                            totalUsedAmount += amountD;
                        }
                    }
                    isFirstRow = false;
                }
            }
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1s);
            props.setProperty("keyid2", keyid2s);
            props.setProperty("keyid3", keyid3s);
            if (this.isDataRelation) {
                props.setProperty("paramlistid", paramlistids);
                props.setProperty("paramlistversionid", paramlistversionids);
                props.setProperty("variantid", variantids);
                props.setProperty("dataset", datasets);
            } else {
                props.setProperty("workitemid", workitemids);
                props.setProperty("workiteminstance", workiteminstances);
            }
            props.setProperty("relationtype", relationtypes);
            props.setProperty("relationfunction", relationfunctions);
            props.setProperty("tosdcid", tosdcids);
            props.setProperty("tokeyid1", tokeyid1s);
            props.setProperty("refsdcid", refsdcids);
            props.setProperty("refkeyid1", refkeyid1s);
            props.setProperty("amount", amounts);
            if (isReagent) {
                props.setProperty("amountunits", amountunits);
                props.setProperty("amountunitstype", amountunittypes);
                props.setProperty("originalreagenttypeid", originalreagenttypeids);
                props.setProperty("originalreagenttypeversionid", originalreagenttypeversionids);
            }
            props.setProperty("sourcesdcid", sourcesdcids);
            props.setProperty("sourcekeyid1", sourcekeyid1s);
            props.setProperty("sourcekeyid2", sourcekeyid2s);
            props.setProperty("relationinstance", relationinstances);
            try {
                if (this.isDataRelation) {
                    this.getActionProcessor().processAction("AddSDIDataRelation", "1", props);
                } else {
                    this.getActionProcessor().processAction("AddSDIWorkItemRelation", "1", props);
                }
                if (isReagent) {
                    this.adjustTrackItemInv(refkeyid1, totalUsedAmount, amountunit, amountunittype);
                }
            }
            catch (Exception e) {
                errorMsg = "Failed to add SDI Relation:" + e.getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("errormsg", errorMsg);
        ajaxResponse.print();
    }

    private void addReagentLotRecipe(AjaxResponse ajaxResponse) {
        String errorMsg = "";
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String recipeitemtype = ajaxResponse.getRequestParameter("recipeitemtype", "Reagent");
        boolean isReagent = recipeitemtype.equalsIgnoreCase("Reagent");
        String containerreagenttypeid = ajaxResponse.getRequestParameter("containerreagenttypeid", "");
        String containerreagenttypeversionid = ajaxResponse.getRequestParameter("containerreagenttypeversionid", "");
        String reagenttypeid = ajaxResponse.getRequestParameter("reagenttypeid", "");
        String reagenttypeversionid = ajaxResponse.getRequestParameter("reagenttypeversionid", "");
        String instrumenttype = ajaxResponse.getRequestParameter("instrumenttype", "");
        String instrumentmodelid = ajaxResponse.getRequestParameter("instrumentmodelid", "");
        String instrumentid = ajaxResponse.getRequestParameter("instrumentid", "");
        String reagentlotstageid = ajaxResponse.getRequestParameter("reagentlotstageid", "");
        if (isReagent && reagenttypeid.trim().length() == 0) {
            reagenttypeid = containerreagenttypeid;
            reagenttypeversionid = containerreagenttypeversionid;
        }
        if (isReagent && containerreagenttypeid.trim().length() == 0) {
            containerreagenttypeid = reagenttypeid;
            containerreagenttypeversionid = reagenttypeversionid;
        }
        if (reagenttypeid.trim().length() > 0 || instrumenttype.trim().length() > 0 || instrumentmodelid.trim().length() > 0 || instrumentid.trim().length() > 0) {
            String amount = ajaxResponse.getRequestParameter("amount", "");
            double amountD = 0.0;
            String amountunit = "";
            String amountunittype = "";
            String trackitemid = "";
            try {
                if (isReagent) {
                    String trackitemreagentlotid = ajaxResponse.getRequestParameter("reagentlotid", "");
                    trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
                    amountD = (amount = UnitsUtil.convertToLocateSeperated(amount, "" + this.decimalSeperator)).trim().length() > 0 ? this.formatUtil.parseBigDecimal(amount).doubleValue() : 0.0;
                    amountunit = ajaxResponse.getRequestParameter("amountunit", "");
                    amountunittype = "";
                    if (amountunit.equalsIgnoreCase("(Containers)")) {
                        amountunit = "";
                        amountunittype = "C";
                    } else if (amountunit.trim().length() > 0) {
                        amountunittype = "U";
                    }
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_ReagentLot");
                    props.setProperty("linkid", "ReagentLotRecipe");
                    props.setProperty("keyid1", keyid1);
                    props.setProperty("reagentlotrecipeitemid", ReagentUtil.getMaxRecipeItemId(keyid1, recipeitemtype, this.getQueryProcessor()));
                    props.setProperty("recipeitemtype", recipeitemtype);
                    props.setProperty("amount", amount);
                    props.setProperty("includereagenttypeid", containerreagenttypeid);
                    props.setProperty("includereagenttypeversionid", containerreagenttypeversionid);
                    props.setProperty("originalreagenttypeid", reagenttypeid);
                    props.setProperty("originalreagenttypeversionid", reagenttypeversionid);
                    props.setProperty("trackitemid", trackitemid);
                    props.setProperty("trackitemreagentlotid", trackitemreagentlotid);
                    props.setProperty("amounttext", amount);
                    props.setProperty("amountunits", amountunit);
                    props.setProperty("amountunitstype", amountunittype);
                    props.setProperty("reagentlotstageid", reagentlotstageid);
                    this.getActionProcessor().processAction("AddSDIDetail", "1", props);
                    this.adjustTrackItemInv(trackitemid, amountD, amountunit, amountunittype);
                } else {
                    DataSet ds;
                    String sql = "";
                    SafeSQL safeSQL = new SafeSQL();
                    if (instrumenttype.trim().length() == 0 && (instrumentid.trim().length() > 0 || instrumentmodelid.trim().length() > 0)) {
                        sql = instrumentid.trim().length() > 0 ? "select instrumenttype from instrument where instrumentid=" + safeSQL.addVar(instrumentid) : "select instrumenttypeid instrumenttype from instrumentmodel where instrumentmodelid=" + safeSQL.addVar(instrumentmodelid);
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                        if (ds != null && ds.getRowCount() > 0) {
                            instrumenttype = ds.getString(0, "instrumenttype", "");
                        }
                    }
                    safeSQL.reset();
                    sql = instrumentmodelid.trim().length() > 0 ? "select unmanagedflag from instrumentmodel where instrumentmodelid=" + safeSQL.addVar(instrumentmodelid) : "select unmanagedflag from instrumenttype where instrumenttypeid=" + safeSQL.addVar(instrumenttype);
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    String unmanagedflag = "N";
                    if (ds != null && ds.getRowCount() > 0) {
                        unmanagedflag = ds.getString(0, "unmanagedflag", "N");
                    }
                    int insCount = 1;
                    String recommCount = "";
                    if (unmanagedflag.equalsIgnoreCase("Y")) {
                        if (ReagentUtil.isInstrumentExist(keyid1, instrumenttype, instrumentmodelid, this.getQueryProcessor())) {
                            insCount = -1;
                        } else {
                            recommCount = ajaxResponse.getRequestParameter("amount", "1");
                            recommCount = UnitsUtil.convertToPointSeperated(recommCount);
                            recommCount = "" + (int)Math.floor(Double.parseDouble(recommCount));
                        }
                    } else {
                        String count = ajaxResponse.getRequestParameter("amount", "1");
                        count = UnitsUtil.convertToPointSeperated(count);
                        insCount = (int)Math.floor(Double.parseDouble(count));
                    }
                    for (int c = 1; c <= insCount; ++c) {
                        String recipeitemid = ReagentUtil.getMaxRecipeItemId(keyid1, recipeitemtype, this.getQueryProcessor());
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_ReagentLot");
                        props.setProperty("linkid", "ReagentLotRecipe");
                        props.setProperty("keyid1", keyid1);
                        props.setProperty("reagentlotrecipeitemid", recipeitemid);
                        props.setProperty("recipeitemtype", recipeitemtype);
                        props.setProperty("amount", recommCount);
                        props.setProperty("instrumenttype", instrumenttype);
                        props.setProperty("instrumentmodelid", instrumentmodelid);
                        props.setProperty("instrumentid", instrumentid);
                        props.setProperty("reagentlotstageid", reagentlotstageid);
                        props.setProperty("usersequence", recipeitemid.substring(2));
                        this.getActionProcessor().processAction("AddSDIDetail", "1", props);
                    }
                }
            }
            catch (Exception e) {
                errorMsg = "Failed to add Recipe:" + e.getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("errormsg", errorMsg);
        ajaxResponse.print();
    }

    private void addQCBatchInstrumentReagent(AjaxResponse ajaxResponse) {
        String errorMsg = "";
        String tablename = ajaxResponse.getRequestParameter("tablename", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String instrumenttype = ajaxResponse.getRequestParameter("instrumenttype", "");
        String instrumentmodelid = ajaxResponse.getRequestParameter("instrumentmodelid", "");
        String instrumentid = ajaxResponse.getRequestParameter("instrumentid", "");
        boolean isReagent = tablename.equalsIgnoreCase("s_qcbatchreagent");
        String containerreagenttypeid = ajaxResponse.getRequestParameter("containerreagenttypeid", "");
        String containerreagenttypeversionid = ajaxResponse.getRequestParameter("containerreagenttypeversionid", "");
        String reagenttypeid = ajaxResponse.getRequestParameter("reagenttypeid", "");
        String reagenttypeversionid = ajaxResponse.getRequestParameter("reagenttypeversionid", "");
        if (isReagent && reagenttypeid.trim().length() == 0) {
            reagenttypeid = containerreagenttypeid;
            reagenttypeversionid = containerreagenttypeversionid;
        }
        if (isReagent && containerreagenttypeid.trim().length() == 0) {
            containerreagenttypeid = reagenttypeid;
            containerreagenttypeversionid = reagenttypeversionid;
        }
        if (reagenttypeid.trim().length() > 0 || instrumenttype.trim().length() > 0 || instrumentmodelid.trim().length() > 0 || instrumentid.trim().length() > 0) {
            String amount = ajaxResponse.getRequestParameter("amount", "");
            double amountD = 0.0;
            String amountunit = "";
            String amountunittype = "";
            String trackitemid = "";
            try {
                int maxUsersequence = this.getMaxUserSequence(tablename, "qcbatchid", keyid1, this.getQueryProcessor());
                if (isReagent) {
                    String trackitemreagentlotid = ajaxResponse.getRequestParameter("reagentlotid", "");
                    trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
                    amountD = (amount = UnitsUtil.convertToLocateSeperated(amount, "" + this.decimalSeperator)).trim().length() > 0 ? this.formatUtil.parseBigDecimal(amount).doubleValue() : 0.0;
                    amountunit = ajaxResponse.getRequestParameter("amountunit", "");
                    amountunittype = "";
                    if (amountunit.equalsIgnoreCase("(Containers)")) {
                        amountunit = "";
                        amountunittype = "C";
                    } else if (amountunit.trim().length() > 0) {
                        amountunittype = "U";
                    }
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_QCBatchReagent");
                    props.setProperty("qcbatchid", keyid1);
                    props.setProperty("reagenttypeid", containerreagenttypeid);
                    props.setProperty("reagenttypeversionid", containerreagenttypeversionid);
                    props.setProperty("originalreagenttypeid", reagenttypeid);
                    props.setProperty("originalreagenttypeversionid", reagenttypeversionid);
                    props.setProperty("trackitemid", trackitemid);
                    props.setProperty("reagentlotid", trackitemreagentlotid);
                    props.setProperty("amount", amount);
                    props.setProperty("amounttext", amount);
                    props.setProperty("amountunits", amountunit);
                    props.setProperty("amountunitstype", amountunittype);
                    props.setProperty("usersequence", Integer.toString(++maxUsersequence));
                    this.getActionProcessor().processAction("AddSDI", "1", props);
                    this.adjustTrackItemInv(trackitemid, amountD, amountunit, amountunittype);
                } else {
                    DataSet ds;
                    String sql = "";
                    SafeSQL safeSQL = new SafeSQL();
                    if (instrumenttype.trim().length() == 0 && (instrumentid.trim().length() > 0 || instrumentmodelid.trim().length() > 0)) {
                        sql = instrumentid.trim().length() > 0 ? "select instrumenttype from instrument where instrumentid=" + safeSQL.addVar(instrumentid) : "select instrumenttypeid instrumenttype from instrumentmodel where instrumentmodelid=" + safeSQL.addVar(instrumentmodelid);
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                        if (ds != null && ds.getRowCount() > 0) {
                            instrumenttype = ds.getString(0, "instrumenttype", "");
                        }
                    }
                    safeSQL.reset();
                    sql = instrumentmodelid.trim().length() > 0 ? "select unmanagedflag from instrumentmodel where instrumentmodelid=" + safeSQL.addVar(instrumentmodelid) : "select unmanagedflag from instrumenttype where instrumenttypeid=" + safeSQL.addVar(instrumenttype);
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    String unmanagedflag = "N";
                    if (ds != null && ds.getRowCount() > 0) {
                        unmanagedflag = ds.getString(0, "unmanagedflag", "N");
                    }
                    int insCount = 1;
                    String count = "1";
                    if (unmanagedflag.equalsIgnoreCase("Y")) {
                        count = ajaxResponse.getRequestParameter("amount", "1");
                        count = UnitsUtil.convertToPointSeperated(count);
                        count = "" + (int)Math.floor(Double.parseDouble(count));
                    } else {
                        count = ajaxResponse.getRequestParameter("amount", "1");
                        count = UnitsUtil.convertToPointSeperated(count);
                        insCount = (int)Math.floor(Double.parseDouble(count));
                    }
                    DataSet dsInsert = new DataSet();
                    for (int c = 1; c <= insCount; ++c) {
                        int rowid = dsInsert.addRow();
                        dsInsert.setString(rowid, "instrumenttype", instrumenttype);
                        dsInsert.setString(rowid, "instrumentmodelid", instrumentmodelid);
                        dsInsert.setString(rowid, "instrumentid", instrumentid);
                        if (unmanagedflag.equalsIgnoreCase("Y")) {
                            dsInsert.setString(rowid, "requiredinstrumentcount", count);
                            dsInsert.setString(rowid, "instrumentcount", count);
                        } else {
                            dsInsert.setNumber(rowid, "instrumentinstance", c);
                        }
                        dsInsert.setNumber(rowid, "usersequence", ++maxUsersequence);
                    }
                    if (dsInsert.getRowCount() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_QCBatchInstrument");
                        props.setProperty("qcbatchid", keyid1);
                        props.setProperty("instrumenttypeid", dsInsert.getColumnValues("instrumenttype", ";"));
                        props.setProperty("instrumentmodelid", dsInsert.getColumnValues("instrumentmodelid", ";"));
                        props.setProperty("instrumentid", dsInsert.getColumnValues("instrumentid", ";"));
                        if (unmanagedflag.equalsIgnoreCase("Y")) {
                            props.setProperty("requiredinstrumentcount", dsInsert.getColumnValues("requiredinstrumentcount", ";"));
                            props.setProperty("instrumentcount", dsInsert.getColumnValues("instrumentcount", ";"));
                        } else {
                            props.setProperty("instrumentinstance", dsInsert.getColumnValues("instrumentinstance", ";"));
                        }
                        props.setProperty("usersequence", dsInsert.getColumnValues("usersequence", ";"));
                        props.setProperty("copies", "" + dsInsert.getRowCount());
                        this.getActionProcessor().processAction("AddSDI", "1", props);
                    }
                }
            }
            catch (Exception e) {
                errorMsg = "Failed to add Recipe:" + e.getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("errormsg", errorMsg);
        ajaxResponse.print();
    }

    private void addTransferExecutionReagent(AjaxResponse ajaxResponse) {
        String errorMsg = "";
        String tablename = ajaxResponse.getRequestParameter("tablename", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String containerreagenttypeid = ajaxResponse.getRequestParameter("containerreagenttypeid", "");
        String containerreagenttypeversionid = ajaxResponse.getRequestParameter("containerreagenttypeversionid", "");
        String reagenttypeid = ajaxResponse.getRequestParameter("reagenttypeid", "");
        String reagenttypeversionid = ajaxResponse.getRequestParameter("reagenttypeversionid", "");
        if (reagenttypeid.trim().length() == 0) {
            reagenttypeid = containerreagenttypeid;
            reagenttypeversionid = containerreagenttypeversionid;
        }
        if (containerreagenttypeid.trim().length() == 0) {
            containerreagenttypeid = reagenttypeid;
            containerreagenttypeversionid = reagenttypeversionid;
        }
        if (reagenttypeid.trim().length() > 0) {
            String amount = ajaxResponse.getRequestParameter("amount", "");
            double amountD = 0.0;
            String amountunit = "";
            String amountunittype = "";
            String trackitemid = "";
            try {
                String trackitemreagentlotid = ajaxResponse.getRequestParameter("reagentlotid", "");
                trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
                amount = UnitsUtil.convertToLocateSeperated(amount, "" + this.decimalSeperator);
                amountD = amount.trim().length() > 0 ? this.formatUtil.parseBigDecimal(amount).doubleValue() : 0.0;
                amountunit = ajaxResponse.getRequestParameter("amountunit", "");
                amountunittype = "";
                if (amountunit.equalsIgnoreCase("(Containers)")) {
                    amountunit = "";
                    amountunittype = "C";
                } else if (amountunit.trim().length() > 0) {
                    amountunittype = "U";
                }
                int maxUsersequence = this.getMaxUserSequence(tablename, "transferexecutionid", keyid1, this.getQueryProcessor());
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_TransferExecution");
                props.setProperty("linkid", "Transfer Execution Reagents");
                props.setProperty("keyid1", keyid1);
                props.setProperty("reagenttypeid", containerreagenttypeid);
                props.setProperty("reagenttypeversionid", containerreagenttypeversionid);
                props.setProperty("originalreagenttypeid", reagenttypeid);
                props.setProperty("originalreagenttypeversionid", reagenttypeversionid);
                props.setProperty("trackitemid", trackitemid);
                props.setProperty("reagentlotid", trackitemreagentlotid);
                props.setProperty("useamount", amount);
                props.setProperty("useamountunits", amountunit);
                props.setProperty("useamountunitstype", amountunittype);
                props.setProperty("usersequence", Integer.toString(++maxUsersequence));
                this.getActionProcessor().processAction("AddSDIDetail", "1", props);
                this.adjustTrackItemInv(trackitemid, amountD, amountunit, amountunittype);
            }
            catch (Exception e) {
                errorMsg = "Failed to add Recipe:" + e.getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("errormsg", errorMsg);
        ajaxResponse.print();
    }

    private void adjustTrackItemInv(String trackitemid, double amount, String unit, String unittype) throws Exception {
        if (trackitemid.length() > 0 && amount > 0.0) {
            PropertyList props = new PropertyList();
            props.setProperty("trackitemid", trackitemid);
            props.setProperty("quantity", "" + (0.0 - amount));
            props.setProperty("quantityunit", unit);
            props.setProperty("quantitytype", unittype);
            this.getActionProcessor().processAction("AdjustTrackItemInv", "1", props);
        }
    }

    private String delimiter(boolean isFirstRow) {
        return isFirstRow ? "" : ";";
    }

    private HashMap<String, String> getExistingRealtions(AjaxResponse ajaxResponse) {
        HashMap<String, String> hm = new HashMap<String, String>();
        String tablename = ajaxResponse.getRequestParameter("tablename", "sdidatarelation");
        String relationfunction = ajaxResponse.getRequestParameter("relationfunction", "Reagent");
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "Sample");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String paramlistid = ajaxResponse.getRequestParameter("paramlistid", "");
        String paramlistversionid = ajaxResponse.getRequestParameter("paramlistversionid", "");
        String variantid = ajaxResponse.getRequestParameter("variantid", "");
        String dataset = ajaxResponse.getRequestParameter("dataset", "");
        String workitemid = ajaxResponse.getRequestParameter("workitemid", "");
        String workiteminstance = ajaxResponse.getRequestParameter("workiteminstance", "");
        boolean isInstrument = relationfunction.equalsIgnoreCase("Instrument");
        SafeSQL safeSQL = new SafeSQL();
        String selectClause = "sdcid,keyid1,keyid2,keyid3,relationtype";
        selectClause = this.isDataRelation ? selectClause + ",paramlistid,paramlistversionid,variantid,dataset" : selectClause + ",workitemid,workiteminstance";
        if (isInstrument) {
            selectClause = this.isDataRelation ? selectClause + ",(CASE WHEN sdidatarelation.sourcesdcid='Instrument' THEN (select instrument.instrumentmodelid  from instrument where  instrument.instrumentid=sdidatarelation.sourcekeyid1 ) WHEN sdidatarelation.sourcesdcid='LV_InstrumentModel' THEN sdidatarelation.sourcekeyid1 WHEN sdidatarelation.sourcesdcid='LV_InstrumentType' THEN '' END) instrmodel" : selectClause + ",(CASE WHEN sdiworkitemrelation.sourcesdcid='Instrument' THEN (select instrument.instrumentmodelid  from instrument where  instrument.instrumentid=sdiworkitemrelation.sourcekeyid1 ) WHEN sdiworkitemrelation.sourcesdcid='LV_InstrumentModel' THEN sdiworkitemrelation.sourcekeyid1 WHEN sdiworkitemrelation.sourcesdcid='LV_InstrumentType' THEN '' END) instrmodel";
        }
        StringBuffer sql = new StringBuffer();
        sql.append("select " + selectClause + " from ").append(tablename);
        sql.append(" where sdcid=").append(safeSQL.addVar(sdcid));
        sql.append(" and keyid1 in (").append(safeSQL.addIn(keyid1, ";")).append(")");
        if (tablename.equalsIgnoreCase("sdiworkitemrelation")) {
            sql.append(" and relationfunction=").append(safeSQL.addVar(relationfunction));
            sql.append(" and workitemid in (").append(safeSQL.addIn(workitemid, ";")).append(")");
            sql.append(" and workiteminstance in (").append(safeSQL.addIn(workiteminstance, ";")).append(")");
        } else {
            sql.append(" and relationfunction=").append(safeSQL.addVar(relationfunction));
            sql.append(" and paramlistid in (").append(safeSQL.addIn(paramlistid, ";")).append(")");
            sql.append(" and paramlistversionid in (").append(safeSQL.addIn(paramlistversionid, ";")).append(")");
            sql.append(" and variantid in (").append(safeSQL.addIn(variantid, ";")).append(")");
            sql.append(" and dataset in (").append(safeSQL.addIn(dataset, ";")).append(")");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            for (int r = 0; r < ds.size(); ++r) {
                String currkeyid3;
                String currkeyid2;
                String relationtype = ds.getString(r, "relationtype", "");
                String currkeyid1 = ds.getString(r, "keyid1", "");
                String key = "";
                if (this.isDataRelation) {
                    currkeyid2 = ds.getString(r, "keyid2", "");
                    currkeyid3 = ds.getString(r, "keyid3", "");
                    String currparamlistid = ds.getString(r, "paramlistid", "");
                    String currparamlistversionid = ds.getString(r, "paramlistversionid", "");
                    String currvariantid = ds.getString(r, "variantid", "");
                    String currdataset = ds.getValue(r, "dataset", "");
                    key = currkeyid1 + "^^^" + currkeyid2 + "^^^" + currkeyid3 + "^^^" + currparamlistid + "^^^" + currparamlistversionid + "^^^" + currvariantid + "^^^" + currdataset;
                } else if (tablename.equalsIgnoreCase("sdiworkitemrelation")) {
                    currkeyid2 = ds.getString(r, "keyid2", "");
                    currkeyid3 = ds.getString(r, "keyid3", "");
                    String currworkitemid = ds.getString(r, "workitemid", "");
                    String currworkiteminstance = ds.getValue(r, "workiteminstance", "");
                    key = currkeyid1 + "^^^" + currkeyid2 + "^^^" + currkeyid3 + "^^^" + currworkitemid + "^^^" + currworkiteminstance;
                } else if (tablename.equalsIgnoreCase("reagentlotrecipe")) {
                    key = currkeyid1;
                }
                String value = relationtype;
                if (isInstrument) {
                    value = value + "||" + ds.getString(r, "instrmodel", "");
                }
                if (hm.containsKey(key)) {
                    String keyValue = hm.get(key);
                    hm.put(key, keyValue + "^^^" + value);
                    continue;
                }
                hm.put(key, "^^^" + value);
            }
        }
        return hm;
    }

    private int getMaxUserSequence(String tableid, String keycolname, String keyid1, QueryProcessor queryProcessor) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select max(usersequence) usersequence from ").append(tableid);
        sql.append(" WHERE " + keycolname + " = " + safeSQL.addVar(keyid1));
        int max = 0;
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            max = ds.getInt(0, "usersequence", 0);
        }
        return max;
    }
}


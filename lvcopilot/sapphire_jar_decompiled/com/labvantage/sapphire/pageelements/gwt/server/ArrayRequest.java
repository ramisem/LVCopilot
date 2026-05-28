/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.array.ArraysPolicy;
import com.labvantage.sapphire.array.EvaluateArrayLayoutRules;
import com.labvantage.sapphire.array.TransferMap;
import com.labvantage.sapphire.array.WellValues;
import com.labvantage.sapphire.array.transfer.TransferOperationUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.pageelements.gwt.server.JSONSaveRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIDataItemTable;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaintRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.Table;
import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import com.labvantage.sapphire.pageelements.search.SearchUtil;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.servlet.command.TagRequestPropertyHandler;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.array.ArrayUtil;
import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.SDIProcessor;
import sapphire.error.ErrorHandler;
import sapphire.servlet.RequestContext;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ArrayRequest
extends SDIMaintRequest
implements ArrayConstants {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    float[] hues = new float[]{0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 0.03f, 0.13f, 0.23f, 0.33f, 0.43f, 0.53f, 0.63f, 0.73f, 0.83f, 0.93f, 0.05f, 0.15f, 0.25f, 0.35f, 0.45f, 0.55f, 0.65f, 0.75f, 0.85f, 0.95f, 0.08f, 0.18f, 0.28f, 0.38f, 0.48f, 0.58f, 0.68f, 0.78f, 0.88f, 0.98f};
    float[] sats = new float[]{0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        if (command.equalsIgnoreCase("retrievearraypolicy")) {
            policynode = commandRequest.getString("policynode");
            policy = this.getConfigurationProcessor().getPolicy("ArraysPolicy", policynode);
            policyDef = new ArraysPolicy(policy);
            commandResponse.set("altkeycolumn", policyDef.getAlternateKeyColumn());
            commandResponse.set("primaryaliastype", policyDef.getPropertyAliasType());
            commandResponse.set("ColumnList", policyDef.getColumnList());
            commandResponse.set("ContentPageMapping", policyDef.getContentPage());
            sql = "SELECT s_sampletypeid FROM s_sampletype order by s_sampletypeid";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[0]);
            commandResponse.set("sampletypedata", ds);
        } else if (command.equalsIgnoreCase("retrievesearchelement")) {
            searchsdcid = commandRequest.getString("selectedsdcid");
            arraymethodqueryid = commandRequest.getString("queryid");
            queryparams = commandRequest.getString("queryparams", "");
            retrievelimit = commandRequest.getString("retrievelimit", "1000");
            requestContext = new RequestContext(new PropertyList());
            requestProcessor = new RequestProcessor(this.getConnectionInfo().getConnectionId());
            pagedata = requestProcessor.getWebPageProperties("ArrayLoadingMaint", requestContext);
            currentQueryid = "";
            basicSearchQueryWhere = "";
            if (searchsdcid.equals("TrackItemSDC")) {
                querysDataSet = this.getQueriesDataSet(searchsdcid, pagedata.getPropertyList("searchreagents"), arraymethodqueryid, retrievelimit);
                currentQueryid = this.getDefaultQueryId(pagedata.getPropertyList("searchreagents"), arraymethodqueryid);
            } else if (searchsdcid.startsWith("LV_Array")) {
                searchsdcid = "LV_Array";
                querysDataSet = this.getQueriesDataSet(searchsdcid, pagedata.getPropertyList("searcharrays"), arraymethodqueryid, retrievelimit);
                currentQueryid = this.getDefaultQueryId(pagedata.getPropertyList("searcharrays"), arraymethodqueryid);
            } else {
                querysDataSet = this.getQueriesDataSet(searchsdcid, pagedata.getPropertyList("searchsamples"), arraymethodqueryid, retrievelimit);
                currentQueryid = this.getDefaultQueryId(pagedata.getPropertyList("searchsamples"), arraymethodqueryid);
            }
            basicSearchQueryWhere = pagedata.getPropertyListNotNull("searchsamples").getPropertyListNotNull("basicsearch").getProperty("whereclause", "").trim();
            queryArgsDataSet = this.getQueryArgDataSet(searchsdcid, querysDataSet, queryparams);
            commandResponse.set("querydataset", querysDataSet.toJSONString());
            commandResponse.set("queryargsdataset", queryArgsDataSet.toJSONString());
            commandResponse.set("queryid", currentQueryid);
            commandResponse.set("queryparams", queryparams);
            commandResponse.set("basicsearchquerywhere", basicSearchQueryWhere);
            restrictivewhere = commandRequest.getString("restrictivewhere");
            params = null;
            if (queryparams.length() > 0) {
                if (queryparams.startsWith(";")) {
                    queryparams = queryparams.substring(1);
                }
                params = StringUtil.split(queryparams, "; ");
            }
        } else if (command.equalsIgnoreCase("retrievedata")) {
            arraytypeid = commandRequest.getString("arraytypeid");
            arraytypeversionid = commandRequest.getString("arraytypeversionid");
            sdiMaint = this.fetchArrayTypeData(arraytypeid, arraytypeversionid);
            commandResponse.set("primarydata", sdiMaint);
        } else if (command.equalsIgnoreCase("retrievesdilist")) {
            sdcid = commandRequest.getString("sdcid");
            if (sdcid.equals("LV_ArrayItem")) {
                sdcid = "LV_Array";
            }
            queryid = commandRequest.getString("queryid", "");
            queryparams = commandRequest.getString("queryparams", "");
            restrictivewhere = commandRequest.getString("restrictivewhere");
            searchText = commandRequest.getString("searchtext", "");
            retrievelimit = commandRequest.getString("retrievelimit", "1000");
            sdiList = new SDIList();
            sdiList.setSdcid(sdcid);
            if (queryid.length() > 0) {
                params = null;
                if (queryparams.length() > 0) {
                    params = StringUtil.split(queryparams, ";");
                    for (i = 0; i < params.length; ++i) {
                        if (!params[i].contains("]:")) continue;
                        params[i] = params[i].substring(params[i].indexOf(":") + 1);
                    }
                }
                sdiRequest = new SDIRequest();
                sdiRequest.setRequestItem("primary");
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setQueryid(queryid);
                sdiRequest.setQueryParams(params);
                sdiRequest.setRetrieveLimit(Integer.parseInt(retrievelimit));
                sdcPrps = this.getSDCProcessor().getSDCProperties(sdcid);
                if (restrictivewhere != null && restrictivewhere.length() > 0) {
                    sdiRequest.setQueryWhere(restrictivewhere);
                }
                if ((sdiData = this.getSDIProcessor().getSDIData(sdiRequest)) != null && (primary = sdiData.getDataset("primary")) != null) {
                    for (i = 0; i < primary.getRowCount(); ++i) {
                        sdiList.addSDI(primary.getValue(i, sdcPrps.get("keycolid1").toString()));
                    }
                }
            } else {
                sdcProps = this.getSDCProcessor().getSDCProperties(sdcid);
                sdiRequest = new SDIRequest();
                sdiRequest.setRequestItem("primary");
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setQueryFrom(sdcProps.get("tableid").toString());
                isOracle = this.getConnectionProcessor().isOra();
                sdiRequest.setQueryWhere(sdcProps.get("keycolid1").toString() + " like '%" + SafeSQL.encodeForSQL(searchText, isOracle) + "%'");
                sdiRequest.setRetrieveLimit(Integer.parseInt(retrievelimit));
                if (restrictivewhere != null && restrictivewhere.length() > 0) {
                    sdiRequest.setQueryWhere(restrictivewhere);
                } else {
                    sdiRequest.setQueryWhere("(" + sdcProps.get("keycolid1").toString() + " like '%" + SafeSQL.encodeForSQL(searchText, isOracle) + "%' )");
                }
                sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                primary = sdiData.getDataset("primary");
                if (primary != null) {
                    for (i = 0; i < primary.getRowCount(); ++i) {
                        sdiList.addSDI(primary.getValue(i, sdcProps.get("keycolid1").toString()));
                    }
                }
            }
            commandResponse.set("sdilist", sdiList);
        } else if (command.equalsIgnoreCase("retrievetreatments")) {
            querywhere = commandRequest.getString("querywhere");
            sql = "SELECT s_treatmenttypeid FROM s_treatmenttype WHERE " + querywhere;
            ds = this.getQueryProcessor().getSqlDataSet(sql);
            sdiList = new SDIList();
            sdiList.setSdcid("LV_Treatment");
            for (i = 0; i < ds.getRowCount(); ++i) {
                sdiList.addSDI(ds.getString(i, "s_treatmenttypeid"));
            }
            commandResponse.set("sdilist", sdiList);
        } else if (command.equalsIgnoreCase("retrievearraydata")) {
            arrayid = commandRequest.getString("arrayid");
            fetchitemDS = commandRequest.getString("fetchitemds");
            fetchzoneDS = commandRequest.getString("fetchzoneds");
            fetchdetails = commandRequest.getString("fetcharraydetails");
            altkeycolumn = commandRequest.getString("altkeycolumn");
            primaryaliastype = commandRequest.getString("primaryaliastype");
            if (arrayid == null || arrayid.length() == 0) {
                commandResponse.setStatus("fail", "Array ID was not specified in the request.");
                return true;
            }
            sdiProcessor = this.getSDIProcessor();
            sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("LV_Array");
            sdiRequest.setKeyid1List(arrayid);
            sdiRequest.setRequestItem("primary");
            if ("Y".equalsIgnoreCase(commandRequest.getString("lockarray"))) {
                sdiRequest.setRetainRsetid(true);
                sdiRequest.setLockOption("LA");
            }
            if ("Y".equalsIgnoreCase(fetchdetails)) {
                sdiRequest.setRequestItem("arrayparamitem");
                sdiRequest.setRequestItem("arrayarraymethoditem");
            }
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            sdiMaint = new SDIMaint(this.getSDCProcessor().getPropertyList(sdiRequest.getSDCid()), sdiData);
            commandResponse.set("arraydata", sdiMaint);
            arrayitemMaint = this.fetchArrayItemData(arrayid, "Y".equalsIgnoreCase(fetchitemDS));
            arrayzoneMaint = this.fetchArrayZoneData(arrayid, "Y".equalsIgnoreCase(fetchzoneDS));
            sourceAIDS = this.fetchSourceArrayItemData(arrayitemMaint);
            if (sourceAIDS != null) {
                commandResponse.set("sourcearrayitemdata", sourceAIDS);
            }
            commandResponse.set("arrayitemdata", arrayitemMaint);
            commandResponse.set("arrayzonedata", arrayzoneMaint);
            primaryDS = sdiMaint.getDataset("primary");
            arraytypeid = primaryDS.getValue(0, "arraytypeid");
            arraytypeversionid = primaryDS.getValue(0, "arraytypeversionid", "1");
            arraytypeMaint = this.fetchArrayTypeData(arraytypeid, arraytypeversionid);
            commandResponse.set("arraytypedata", arraytypeMaint);
            filter = new HashMap<String, String>();
            filter.put("contentsdcid", "Sample");
            samplecontentitems = arrayitemMaint.getDataset("arrayitemcontent").getFilteredDataSet(filter);
            if (altkeycolumn.length() > 0 && samplecontentitems.getRowCount() > 0) {
                rsetid = this.getDAMProcessor().createRSet("Sample", samplecontentitems.getColumnValues("contentkeyid1", ";"), null, null);
                sampleidentificationDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_sampleid, " + altkeycolumn + " FROM s_sample WHERE s_sampleid IN (  SELECT keyid1 FROM rsetitems where rsetid = ? )", new Object[]{rsetid});
                commandResponse.set("sampledetails", sampleidentificationDS);
            } else if (primaryaliastype.length() > 0 && samplecontentitems.getRowCount() > 0) {
                rsetid = this.getDAMProcessor().createRSet("Sample", samplecontentitems.getColumnValues("contentkeyid1", ";"), null, null);
                sql = "select s_sampleid, aliasid from s_sample, sdialias where s_sample.s_sampleid = sdialias.keyid1 and sdcid = 'Sample' and aliastype = ? and s_sampleid IN (  SELECT keyid1 FROM rsetitems where rsetid = ? )";
                sampleidentificationDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{primaryaliastype, rsetid});
                commandResponse.set("sampledetails", sampleidentificationDS);
            } else if (samplecontentitems.getRowCount() > 0) {
                commandResponse.set("sampledetails", new DataSet());
            }
        } else if (command.equalsIgnoreCase("retrievesourcedata")) {
            dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            sdiList = new SDIList();
            try {
                sdcid = commandRequest.getString("sourcesdc");
                queryfrom = commandRequest.getString("queryfrom").trim();
                querywhere = commandRequest.getString("querywhere").trim();
                contentqualifiervalue = commandRequest.getString("contentqualifiervalue").trim();
                querywhere = StringUtil.replaceAll(querywhere, "[contentqualifiervalue]", contentqualifiervalue, false);
                useQuery = querywhere.length() > 0 && queryfrom.length() > 0;
                sdcProcessor = this.getSDCProcessor();
                sdcProperties = sdcProcessor.getSDCProperties(sdcid);
                aliasTableid = fromClause = sdcProperties.get("tableid").toString();
                if (queryfrom.trim().length() > 0) {
                    aliasTableid = this.findAlias(queryfrom, fromClause);
                }
                noOfKeyCols = Integer.valueOf(sdcProperties.get("keycolumns").toString());
                selectClause = "";
                keyid1 = "";
                keyid2 = "";
                keyid3 = "";
                for (i = 1; i <= noOfKeyCols; ++i) {
                    colid = sdcProperties.get("keycolid" + i).toString();
                    switch (i) {
                        case 1: {
                            keyid1 = colid;
                            break;
                        }
                        case 2: {
                            keyid2 = colid;
                            break;
                        }
                        case 3: {
                            keyid3 = colid;
                        }
                    }
                    selectClause = selectClause + "," + colid;
                }
                selectClause = "select " + selectClause.substring(1);
                where = aliasTableid + ".templateflag = 'N'";
                sql = selectClause + " from " + (useQuery != false ? queryfrom : fromClause) + " where " + (useQuery != false ? "( " + querywhere + " ) and " : "") + where;
                dbu.setConnection(this.sapphireConnection);
                dbu.createResultSet(sql);
                sdiList.setSdcid(sdcid);
                while (dbu.getNext()) {
                    sdiList.addSDI(dbu.getValue(keyid1), keyid2.equals("") != false ? "(null)" : dbu.getValue(keyid2), keyid3.equals("") != false ? "(null)" : dbu.getValue(keyid3));
                }
            }
            finally {
                dbu.releaseConnection();
            }
            commandResponse.set("listdata", sdiList);
        } else if (command.equalsIgnoreCase("layoutdata")) {
            contentBound = commandRequest.getString("contentbound");
            repeatCount = commandRequest.getString("repeatcount");
            dilutionSteps = commandRequest.getString("dilutionsteps");
            dilutionFactor = commandRequest.getString("dilutionfactor");
            diluteFirst = commandRequest.getBoolean("dilutefirstflag");
            treatmentCount = commandRequest.getString("treatmentcount");
            horizontalPriority = commandRequest.getString("loadingpriorityhorizontal");
            verticalPriority = commandRequest.getString("loadingpriorityvertical");
            availableCells = commandRequest.getString("availablecells");
            zoneid = commandRequest.getString("arraylayoutzone");
            if (!horizontalPriority.contains("C") && !verticalPriority.contains("C")) {
                horizontalPriority = "C";
            }
            jsonObject = new JSONableMap();
            evalRules = new EvaluateArrayLayoutRules(contentBound != null && contentBound.length() > 0 ? Integer.parseInt(contentBound) : 1, repeatCount != null && repeatCount.length() > 0 ? Integer.parseInt(repeatCount) : 1, dilutionSteps != null && dilutionSteps.length() > 0 ? Integer.parseInt(dilutionSteps) : 1);
            try {
                wellValues = evalRules.plateArray(availableCells, contentBound != null && contentBound.length() > 0 ? Integer.parseInt(contentBound) : 1, repeatCount != null && repeatCount.length() > 0 ? Integer.parseInt(repeatCount) : 1, dilutionSteps != null && dilutionSteps.length() > 0 ? Integer.parseInt(dilutionSteps) : 1, dilutionFactor != null && dilutionFactor.length() > 0 ? Float.parseFloat(dilutionFactor) : 0.0f, diluteFirst, treatmentCount != null && treatmentCount.length() > 0 ? Integer.parseInt(treatmentCount) : 1, horizontalPriority, verticalPriority);
                for (h = 0; h < wellValues.length; ++h) {
                    for (v = 0; v < wellValues[0].length; ++v) {
                        if (wellValues[h][v] == null) continue;
                        key = h + "," + v;
                        val = wellValues[h][v].sample + ";" + wellValues[h][v].repeat + ";" + wellValues[h][v].treatment + ";" + wellValues[h][v].dilutionfactor + ";" + wellValues[h][v].dilution;
                        jsonObject.put(key, val);
                    }
                }
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
            commandResponse.set("layoutdata", jsonObject);
        } else if (command.equalsIgnoreCase("retrievearraylayoutzone")) {
            arraylayoutid = commandRequest.getString("arraylayoutid");
            arraylayoutversionid = commandRequest.getString("arraylayoutversionid");
            sdiProcessor = this.getSDIProcessor();
            sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("LV_ArrayLayout");
            sdiRequest.setKeyid1List(arraylayoutid);
            sdiRequest.setKeyid2List(arraylayoutversionid);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("arraylayoutzone");
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            sdiMaint = new SDIMaint(this.getSDCProcessor().getPropertyList(sdiRequest.getSDCid()), sdiData);
            commandResponse.set("arraylayoutzonedata", sdiMaint);
            sql = "SELECT maxvolume, maxvolumeunits FROM   arraytype t       JOIN arraylayout l         ON t.arraytypeid = l.arraytypeid            AND t.arraytypeversionid = l.arraytypeversionid            AND arraylayoutid = ?             AND arraylayoutversionid = ? ";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{arraylayoutid, arraylayoutversionid});
            commandResponse.set("maxvolume", ds.getValue(0, "maxvolume"));
            commandResponse.set("maxvolumeunits", ds.getValue(0, "maxvolumeunits"));
        } else if (command.equalsIgnoreCase("loadarray")) {
            sampleids = commandRequest.getString("sampleids", "");
            trackitemids = commandRequest.getString("trackitemids", "");
            altkeycolumn = commandRequest.getString("altkeycolumn", "");
            primaryaliastype = commandRequest.getString("primaryaliastype", "");
            fetchsampletype = "Y".equalsIgnoreCase(commandRequest.getString("fetchsampletype"));
            if (altkeycolumn.length() > 0 && sampleids.length() > 0) {
                rsetid = this.getDAMProcessor().createRSet("Sample", sampleids, null, null);
                sampleidentificationDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_sampleid, " + altkeycolumn + " FROM s_sample WHERE s_sampleid IN (  SELECT keyid1 FROM rsetitems where rsetid = ? )", new Object[]{rsetid});
                commandResponse.set("sampledetails", sampleidentificationDS);
            } else if (primaryaliastype.length() > 0 && sampleids.length() > 0) {
                rsetid = this.getDAMProcessor().createRSet("Sample", sampleids, null, null);
                sampleidentificationDS = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid, aliasid from s_sample, sdialias where s_sample.s_sampleid = sdialias.keyid1 and sdcid = 'Sample' and aliastype =? and s_sampleid IN (  SELECT keyid1 FROM rsetitems where rsetid = ? )", new Object[]{primaryaliastype, rsetid});
                commandResponse.set("sampledetails", sampleidentificationDS);
            } else {
                commandResponse.set("sampledetails", new DataSet());
            }
            if (fetchsampletype && sampleids.trim().length() > 0) {
                sampledata = this.getSampleType(sampleids);
                commandResponse.set("sampletypedata", sampledata);
            }
            if (trackitemids != null && trackitemids.trim().length() > 0) {
                commandResponse.set("trackdata", this.fetchTrackData(trackitemids));
            }
        } else if (command.equalsIgnoreCase("evalcontentload")) {
            this.evaluateContentPosition(commandRequest, commandResponse, false);
        } else {
            if (command.equalsIgnoreCase("save")) {
                errorHandler = new ErrorHandler();
                try {
                    super.processCommand("savesdimaint", commandRequest, commandResponse);
                    if (commandResponse.get("ERRORHANDLER") != null) {
                        errorHandler = (ErrorHandler)commandResponse.get("ERRORHANDLER");
                    }
                    if (!errorHandler.hasErrors()) {
                        reloaditem = commandRequest.getString("reloaditem", "");
                        if (reloaditem != null && reloaditem.length() > 0) {
                            arrayitemMaint = this.fetchArrayItemData(commandRequest.getString("arrayid"), false);
                            commandResponse.set(reloaditem, arrayitemMaint);
                        }
                        ArrayUtil.updateTrackItems(commandRequest.getDataSet("trackdata"), this.getActionProcessor());
                        commandResponse.set("ERRORHANDLER", errorHandler);
                    }
                    return true;
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to save SDIMaint data. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            }
            if (command.equalsIgnoreCase("retrieveunitsdc")) {
                sql = "select categoryid, keyid1 from categoryitem where sdcid = 'Units' and ( categoryid = 'ConcUnits' or categoryid = 'VolumeUnits' )";
                unitsDS = this.getQueryProcessor().getSqlDataSet(sql);
                commandResponse.set("unitsdc", unitsDS);
            } else if (command.equalsIgnoreCase("retrievereftype")) {
                reftype = commandRequest.getString("reftype");
                sdiProcessor = this.getSDIProcessor();
                sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("RefType");
                sdiRequest.setKeyid1List(reftype);
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRequestItem("refvalue");
                sdiData = sdiProcessor.getSDIData(sdiRequest);
                dataset = sdiData.getDataset("refvalue");
                data = "";
                if (dataset != null && !dataset.isEmpty()) {
                    for (i = 0; i < dataset.size(); ++i) {
                        refdisplayvalue = dataset.getValue(i, "refdisplayvalue");
                        data = data + ";" + dataset.getValue(i, "refvalueid") + (refdisplayvalue.length() > 0 ? "=" + refdisplayvalue : "");
                    }
                }
                commandResponse.set("reftypevalues", data.length() > 0 ? data.substring(1) : data);
            } else if (command.equalsIgnoreCase("retrievequeryids")) {
                columnid = commandRequest.getString("columnid");
                tableid = commandRequest.getString("tableid");
                whereclause = commandRequest.getString("whereclause");
                sql = "select " + columnid + " from " + tableid + " where " + whereclause + " order by " + columnid;
                queryids = this.getQueryProcessor().getSqlDataSet(sql);
                commandResponse.set("queryids", queryids.getColumnValues(columnid, ";"));
            } else if (command.equalsIgnoreCase("retrievetrackitems")) {
                fetchunits = commandRequest.getString("fetchunits");
                if ("Y".equalsIgnoreCase(fetchunits)) {
                    unitsql = "select categoryid, keyid1 from categoryitem where sdcid = 'Units' and ( categoryid = 'ConcUnits' or categoryid = 'VolumeUnits' )";
                    unitsDS = this.getQueryProcessor().getSqlDataSet(unitsql);
                    commandResponse.set("unitsdc", unitsDS);
                }
                sdcid = commandRequest.getString("selectedsdcid");
                selectedcontents = commandRequest.getString("selectedcontents");
                tovolunit = commandRequest.getString("tovolunit");
                toconcunit = commandRequest.getString("toconcunit");
                rsetid = this.getDAMProcessor().createRSet(sdcid, selectedcontents, "", "");
                if (sdcid.equalsIgnoreCase("TrackItemSDC")) {
                    sql = "SELECT r.keyid1,        trackitemid,        linksdcid,        linkkeyid1,        linkkeyid2,        linkkeyid3,        containertypeid,        qtycurrent,        qtyunits FROM   (SELECT *         FROM   rsetitems         WHERE  rsetitems.rsetid = ? ) r        LEFT OUTER JOIN trackitem                     ON trackitem.trackitemid = r.keyid1 ";
                    trackitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
                } else {
                    sql = "SELECT r.keyid1,        trackitemid,        linksdcid,        linkkeyid1,        linkkeyid2,        linkkeyid3,        containertypeid,        qtycurrent,        qtyunits FROM   (SELECT *         FROM   rsetitems         WHERE  rsetitems.rsetid = ? ) r        LEFT OUTER JOIN trackitem                     ON trackitem.linkkeyid1 = r.keyid1                        AND trackitem.linksdcid = r.sdcid ";
                    trackitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
                }
                formatUtil = FormatUtil.getInstance(this.connectionInfo);
                this.volumeUnitConversion(tovolunit, trackitems, formatUtil);
                commandResponse.set("return_trackitems", trackitems);
                samples = null;
                if (sdcid.equalsIgnoreCase("Sample")) {
                    if (commandRequest.getString("altkeycolumn").length() > 0) {
                        sql = "SELECT s_sampleid, " + commandRequest.getString("altkeycolumn") + ",        concentration,        concentrationunits FROM   s_sample,        rsetitems WHERE  s_sample.s_sampleid = rsetitems.keyid1        AND rsetitems.rsetid = ?";
                        samples = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
                    } else if (commandRequest.getString("primaryaliastype").length() > 0) {
                        safeSQL = new SafeSQL();
                        sql = "select s_sampleid, aliasid from s_sample, sdialias where s_sample.s_sampleid = sdialias.keyid1 and sdcid = 'Sample' and aliastype = " + safeSQL.addVar(commandRequest.getString("primaryaliastype")) + " and s_sampleid IN (  SELECT keyid1 FROM rsetitems where rsetid = " + safeSQL.addVar(rsetid) + ")";
                        samples = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    } else {
                        sql = "SELECT s_sampleid,        concentration,        concentrationunits FROM   s_sample,        rsetitems WHERE  s_sample.s_sampleid = rsetitems.keyid1        AND rsetitems.rsetid = ? ";
                        samples = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
                    }
                    this.concUnitConversion(toconcunit, formatUtil, samples);
                    commandResponse.set("return_samples", samples);
                }
                this.evaluateContentPosition(commandRequest, commandResponse, false);
            } else if (command.equalsIgnoreCase("convertvalue")) {
                qtycurrent = commandRequest.getString("value");
                fromunit = commandRequest.getString("fromunit");
                tounit = commandRequest.getString("tounit");
                formatUtil = FormatUtil.getInstance(this.connectionInfo);
                dqty2 = formatUtil.parseBigDecimal(qtycurrent).setScale(3, 4).doubleValue();
                newqty2 = UnitsUtil.getConvertedValue(this.getQueryProcessor(), fromunit, tounit, Double.toString(dqty2));
                if (newqty2 != null) {
                    dqty2 = Double.parseDouble(newqty2.replace(new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator(), '.'));
                }
                commandResponse.set("convertedvalue", formatUtil.format(formatUtil.parseBigDecimal(String.valueOf(dqty2))));
            } else if (command.equalsIgnoreCase("retrievesourcearraydata")) {
                queryid = commandRequest.getString("queryid");
                basedonid = commandRequest.getString("querybasedonid");
                queryparams = commandRequest.getString("queryparams");
                params = StringUtil.split(queryparams, ";", true);
                parameters = new String[params.length];
                if (params.length > 0) {
                    for (i = 0; i < params.length; ++i) {
                        param = params[i];
                        argValuePair = StringUtil.split(param, ":", true);
                        parameters[i] = argValuePair.length == 2 ? argValuePair[1] : "";
                    }
                }
                sdip = this.getSDIProcessor();
                sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(basedonid);
                sdiRequest.setQueryid(queryid);
                sdiRequest.setQueryParams((String[])(queryparams.trim().length() > 0 ? parameters : null));
                sdiRequest.setRequestItem("primary");
                primary = sdip.getSDIData(sdiRequest).getDataset("primary");
                commandResponse.set("arrayds", primary);
            } else if (command.equalsIgnoreCase("savesdimaint")) {
                try {
                    super.processCommand("savesdimaint", commandRequest, commandResponse);
                    if (!commandResponse.get("sdcid").equals("LV_ArrayTransferMethod")) ** GOTO lbl734
                    sdiMaint = (SDIMaint)commandResponse.get("transfermethod");
                    sdiRequest = new SDIRequest();
                    sdiRequest.setSDIList("LV_ArrayTransferMethod", sdiMaint.getKeyid1(), sdiMaint.getKeyid2(), "");
                    sdiRequest.setRequestItem("primary");
                    sdiRequest.setRequestItem("category");
                    sdiRequest.setExtendedDataTypes(true);
                    sdiRequest.setPrimaryLockOption("LA");
                    sdiRequest.setRetainRsetid(true);
                    sdiProcessor = this.getSDIProcessor();
                    sdiData = sdiProcessor.getSDIData(sdiRequest);
                    if (sdiData == null) {
                        throw sdiProcessor.getLastException();
                    }
                    commandResponse.set("transfermethod", new SDIMaint(this.getSDCProcessor().getPropertyList("LV_ArrayTransferMethod"), (SDIData)sdiData));
                }
                catch (Throwable e) {
                    throw new SapphireException("Failed to save Transfer method. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            } else if (command.equalsIgnoreCase("loadsdimaint")) {
                super.processCommand(command, commandRequest, commandResponse);
                util = new TransferOperationUtil();
                allOperations = util.getAllOperations();
                operations = "";
                for (String allOperation : allOperations) {
                    operations = operations + ";" + allOperation;
                }
                commandResponse.set("transferoperations", operations.length() > 0 ? operations.substring(1) : operations);
            } else if (command.equalsIgnoreCase("retrievesuboperations")) {
                operation = commandRequest.getString("transferoperation");
                util = new TransferOperationUtil();
                allOperations = util.getAllSubOperations(operation);
                suboperations = "";
                for (String allOperation : allOperations) {
                    suboperations = suboperations + ";" + allOperation;
                }
                commandResponse.set("suboperations", suboperations.length() > 0 ? suboperations.substring(1) : suboperations);
            } else if (command.equalsIgnoreCase("retrievesubopprops")) {
                transferoperation = commandRequest.getString("transferoperation");
                suboperation = commandRequest.getString("suboperation");
                algorithmrule = commandRequest.getString("algorithmrule");
                util = new TransferOperationUtil();
                subprops = util.getSubOperation(transferoperation, suboperation);
                subOperationProperties = new PropertyList();
                if (subprops != null) {
                    algorule = new PropertyList();
                    algorule.setPropertyList(algorithmrule);
                    subOperationProperties = subprops.getSubOperationProperties(algorithmrule.trim().length() > 0 ? algorule : null, this.getTranslationProcessor());
                    subOperationProperties.setProperty("formcols", "1");
                }
                commandResponse.set("subprops", subOperationProperties);
            } else if (command.equalsIgnoreCase("generatemap")) {
                sourceType = commandRequest.getJSONableMap("opProps");
                map = (HashMap)sourceType.toMap();
                for (String key : map.keySet()) {
                    val = (String)map.get(key);
                    if ("N".equalsIgnoreCase(val)) {
                        map.put(key, "false");
                        continue;
                    }
                    if (!"Y".equalsIgnoreCase(val)) continue;
                    map.put(key, "true");
                }
                transferoperation = (String)map.get("transferoperation");
                suboperation = (String)map.get("suboperation");
                util = new TransferOperationUtil();
                subprops = util.getSubOperation(transferoperation, suboperation);
                transferDS = new DataSet();
                transferDS.addColumn("sourcearrayindex", 1);
                transferDS.addColumn("sourcerowindex", 1);
                transferDS.addColumn("sourcecolindex", 1);
                transferDS.addColumn("targetarrayindex", 1);
                transferDS.addColumn("targetrowindex", 1);
                transferDS.addColumn("targetcolindex", 1);
                try {
                    colormap = "";
                    if (subprops != null) {
                        transferMap = subprops.generateMap(map, this.getQueryProcessor());
                        for (TransferMap transMap : transferMap) {
                            row = transferDS.addRow();
                            transferDS.setNumber(row, "sourcearrayindex", transMap.sourcearray);
                            transferDS.setNumber(row, "sourcerowindex", transMap.sourcerowindex);
                            transferDS.setNumber(row, "sourcecolindex", transMap.sourcecolindex);
                            transferDS.setNumber(row, "targetarrayindex", transMap.targetarray);
                            transferDS.setNumber(row, "targetrowindex", transMap.targetrowindex);
                            transferDS.setNumber(row, "targetcolindex", transMap.targetcolindex);
                        }
                        colormap = subprops.generateColorMap((String)map.get("sourcearraytypeid"), (String)map.get("targetarraytypeid"), this.getQueryProcessor());
                    }
                    commandResponse.set("transfermap", transferDS);
                    commandResponse.set("colormap", colormap);
                }
                catch (SapphireException e) {
                    commandResponse.setStatus("fail", e.getMessage());
                }
            } else if (command.equalsIgnoreCase("calcnumsrctrg")) {
                transferoperation = commandRequest.getString("transferoperation");
                suboperation = commandRequest.getString("suboperation");
                sourcearraytypeid = commandRequest.getString("sourcearraytypeid");
                targetarraytypeid = commandRequest.getString("targetarraytypeid");
                util = new TransferOperationUtil();
                subprops = util.getSubOperation(transferoperation, suboperation);
                if (subprops != null) {
                    numOfSourceArrays = subprops.getNumOfSourceArrays(sourcearraytypeid, targetarraytypeid, this.getQueryProcessor());
                    numOfTargetArrays = subprops.getNumOfTargetArrays(sourcearraytypeid, targetarraytypeid, this.getQueryProcessor());
                    commandResponse.set("numsourcearrays", String.valueOf(numOfSourceArrays));
                    commandResponse.set("numtargetarrays", String.valueOf(numOfTargetArrays));
                } else {
                    commandResponse.set("numsourcearrays", "");
                    commandResponse.set("numtargetarrays", "");
                }
            } else if (command.equalsIgnoreCase("generatecolor")) {
                colors = this.generateColors(this.hues.length);
                colours = new HashSet<Color>();
                colours.addAll(Arrays.asList(colors));
                revCols = new ArrayList<Color>();
                revCols.addAll(colours);
                cols = "";
                for (i = revCols.size() - 1; i >= 0; --i) {
                    color = (Color)revCols.get(i);
                    cols = cols + ";" + this.rgbToHex(color.getRed(), color.getGreen(), color.getBlue());
                }
                commandResponse.set("colors", cols.substring(1));
            } else if (command.equalsIgnoreCase("createchildsamples")) {
                parentSampleids = new StringBuilder();
                childSampleids = new StringBuilder();
                policy = this.getConfigurationProcessor().getPolicy("ArraysPolicy", "Sapphire Custom");
                childsampleiddirection = policy.getProperty("childsampleiddirection", "Parent Sample ID");
                policyDef = new ArraysPolicy(policy);
                childsamplestoragestatus = policyDef.getChildSampleStorageStatus();
                childsamplestatus = policyDef.getChildSampleStatus();
                arrayid = commandRequest.getString("arrayid");
                arraymethodid = commandRequest.getString("arraymethodid");
                arraymethodversionid = commandRequest.getString("arraymethodversionid");
                createSingleChild = "S".equals(OpalUtil.getColumnValues(this.getQueryProcessor(), "arraymethod", "createchildsampleflag", "arraymethodid = ? and arraymethodversionid = ?", new String[]{arraymethodid, arraymethodversionid}));
                zoneMap = new HashMap<String, DataSet>();
                try {
                    arraydata = new JSONArray(commandRequest.getString("arraydata"));
                    for (i = 0; i < arraydata.length(); ++i) {
                        o = arraydata.getJSONObject(i);
                        arrayzoneid = o.getString("arrayzoneid");
                        if (!zoneMap.containsKey(arrayzoneid)) {
                            zoneMap.put(arrayzoneid, new DataSet());
                        }
                        ds = (DataSet)zoneMap.get(arrayzoneid);
                        row = ds.addRow();
                        ds.setString(row, "parentid", o.has("parentid") != false ? o.getString("parentid") : "");
                        ds.setString(row, "arrayitemid", o.has("arrayitemid") != false ? o.getString("arrayitemid") : "");
                        ds.setString(row, "contentdirection", o.has("contentdirection") != false ? o.getString("contentdirection") : "");
                        ds.setString(row, "childsampletypeid", o.has("childsampletypeid") != false ? o.getString("childsampletypeid") : "");
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                if (zoneMap.size() > 1) {
                    sortedZoneMap = new LinkedHashMap<String, V>();
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select az.arrayzoneid, az.zone from arrayzone az, array a where az.arrayid = a.arrayid and a.arrayid = ? order by az.usersequence", (Object[])new String[]{arrayid});
                    if (ds != null && ds.size() > 0) {
                        for (i = 0; i < ds.size(); ++i) {
                            arrayzoneid = ds.getString(i, "arrayzoneid", "");
                            if (!zoneMap.containsKey(arrayzoneid)) continue;
                            sortedZoneMap.put(arrayzoneid, zoneMap.get(arrayzoneid));
                        }
                        if (sortedZoneMap.size() == zoneMap.size()) {
                            zoneMap.clear();
                            zoneMap.putAll(sortedZoneMap);
                        }
                    }
                }
                if ((aliquot = commandRequest.getString("Aliquot")).trim().length() > 0) {
                    aliquotSamplesList = OpalUtil.toUniqueList(commandRequest.getString("Aliquot_Sampleids"), ";");
                    for (String zone : zoneMap.keySet()) {
                        zoneDS = (DataSet)zoneMap.get(zone);
                        ds = new DataSet();
                        parentSampleSet = new HashSet<String>();
                        for (i = 0; i < zoneDS.size(); ++i) {
                            parentid = zoneDS.getString(i, "parentid");
                            if (!aliquotSamplesList.contains(parentid)) continue;
                            if (!createSingleChild) {
                                ds.copyRow(zoneDS, i, 1);
                                continue;
                            }
                            if (parentSampleSet.contains(parentid)) continue;
                            ds.copyRow(zoneDS, i, 1);
                            parentSampleSet.add(parentid);
                        }
                        if (ds.size() <= 0) continue;
                        if ("Parent Sample ID".equals(childsampleiddirection)) {
                            ds.sort("parentid");
                        } else if ("Layout Zone Loading Direction".equals(childsampleiddirection)) {
                            for (i = 0; i < ds.size(); ++i) {
                                arrayitemid = ds.getString(i, "arrayitemid");
                                temp = StringUtil.split(arrayitemid.substring(arrayitemid.indexOf("_") + 1), "_");
                                row = StringUtil.padLeft(temp[0], 4, '0');
                                col = StringUtil.padLeft(temp[1], 4, '0');
                                ds.setNumber(i, "hordirection", row + col);
                                ds.setNumber(i, "verdirection", col + row);
                            }
                            contentdirection = ds.getString(0, "contentdirection", "H");
                            if ("H".equals(contentdirection)) {
                                ds.sort("hordirection");
                            } else {
                                ds.sort("verdirection");
                            }
                        } else {
                            throw new SapphireException(this.getTranslationProcessor().translate("Unsupported Child Sample loading direction") + " (" + childsampleiddirection + ")");
                        }
                        actionProps = new PropertyList();
                        actionProps.setProperty("parent_sampleid", ds.getColumnValues("parentid", ";"));
                        actionProps.setProperty("child_copies", StringUtil.repeat("1", ds.size(), ";"));
                        actionProps.setProperty("mode", "Aliquot");
                        if (!childsamplestoragestatus.equals("Use Biobanking Policy")) {
                            actionProps.setProperty("child_storagestatus", StringUtil.repeat(childsamplestoragestatus, ds.size(), ";"));
                            actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
                            actionProps.setProperty("childcolumn_receiverequiredflag", "N");
                        }
                        if (childsamplestatus != null) {
                            if (!childsamplestatus.equals("Inherit from Parent")) {
                                actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(childsamplestatus, ds.size(), ";"));
                            } else {
                                actionProps.setProperty("childsamplestatus", StringUtil.repeat("inherit", ds.size(), ";"));
                            }
                        }
                        actionProps.setProperty("propsmatch", "Y");
                        this.getActionProcessor().processAction("MultiSampleChild", "1", actionProps);
                        childSampleids.append(childSampleids.length() > 0 ? ";" : "").append(actionProps.getProperty("newkeyid1"));
                        parentSampleids.append(parentSampleids.length() > 0 ? ";" : "").append(actionProps.getProperty("parentsampleid"));
                    }
                }
                derivative = commandRequest.getString("Derivative");
                childsampletypes = new StringBuilder();
                if (derivative.trim().length() > 0) {
                    derivativeSamplesList = OpalUtil.toUniqueList(commandRequest.getString("Derivative_Sampleids"), ";");
                    for (String zone : zoneMap.keySet()) {
                        zoneDS = (DataSet)zoneMap.get(zone);
                        ds = new DataSet();
                        parentSampleSet = new HashSet<String>();
                        for (i = 0; i < zoneDS.size(); ++i) {
                            parentid = zoneDS.getString(i, "parentid");
                            if (!derivativeSamplesList.contains(parentid)) continue;
                            if (!createSingleChild) {
                                ds.copyRow(zoneDS, i, 1);
                                continue;
                            }
                            if (parentSampleSet.contains(parentid)) continue;
                            ds.copyRow(zoneDS, i, 1);
                            parentSampleSet.add(parentid);
                        }
                        if (ds.size() <= 0) continue;
                        if ("Parent Sample ID".equals(childsampleiddirection)) {
                            ds.sort("parentid");
                        } else if ("Layout Zone Loading Direction".equals(childsampleiddirection)) {
                            for (i = 0; i < ds.size(); ++i) {
                                arrayitemid = ds.getString(i, "arrayitemid");
                                temp = StringUtil.split(arrayitemid.substring(arrayitemid.indexOf("_") + 1), "_");
                                row = StringUtil.padLeft(temp[0], 2, '0');
                                col = StringUtil.padLeft(temp[1], 2, '0');
                                ds.setNumber(i, "hordirection", row + col);
                                ds.setNumber(i, "verdirection", col + row);
                            }
                            contentdirection = ds.getString(0, "contentdirection", "H");
                            if ("H".equals(contentdirection)) {
                                ds.sort("hordirection");
                            } else {
                                ds.sort("verdirection");
                            }
                        } else {
                            throw new SapphireException(this.getTranslationProcessor().translate("Unsupported Child Sample loading direction") + " (" + childsampleiddirection + ")");
                        }
                        actionProps = new PropertyList();
                        actionProps.setProperty("parent_sampleid", ds.getColumnValues("parentid", ";"));
                        actionProps.setProperty("child_copies", StringUtil.repeat("1", ds.size(), ";"));
                        actionProps.setProperty("mode", "Derivative");
                        actionProps.setProperty("child_sampletypeid", ds.getColumnValues("childsampletypeid", ";"));
                        if (!childsamplestoragestatus.equals("Use Biobanking Policy")) {
                            actionProps.setProperty("child_storagestatus", StringUtil.repeat(childsamplestoragestatus, ds.size(), ";"));
                            actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
                            actionProps.setProperty("childcolumn_receiverequiredflag", "N");
                        }
                        if (childsamplestatus != null && !childsamplestatus.equals("Inherit from Parent")) {
                            actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(childsamplestatus, ds.size(), ";"));
                        }
                        actionProps.setProperty("propsmatch", "Y");
                        this.getActionProcessor().processAction("MultiSampleChild", "1", actionProps);
                        childSampleids.append(childSampleids.toString().trim().length() > 0 ? ";" : "").append(actionProps.getProperty("newkeyid1"));
                        parentSampleids.append(parentSampleids.toString().trim().length() > 0 ? ";" : "").append(actionProps.getProperty("parentsampleid"));
                        childsampletypes.append(childsampletypes.toString().trim().length() > 0 ? ";" : "").append(actionProps.getProperty("child_sampletypeid"));
                    }
                }
                commandResponse.set("parentsampleids", parentSampleids.toString());
                commandResponse.set("childsampleids", childSampleids.toString());
                commandResponse.set("childsampletypeids", childsampletypes.toString());
            }
        }
lbl734:
        // 32 sources

        if (command.equalsIgnoreCase("approveversion")) {
            keyid1 = commandRequest.getString("keyid1");
            keyid2 = commandRequest.getString("keyid2");
            props = new HashMap<String, String>();
            props.put("sdcid", commandRequest.getString("sdcid"));
            props.put("keyid1", keyid1);
            props.put("keyid2", keyid2);
            props.put("keyid3", commandRequest.getString("keyid3"));
            props.put("versionstatus", "C");
            extra = commandRequest.getPropertyList("__pr_extraprops");
            if (extra != null) {
                props.putAll(extra);
            }
            props.put("applylock", "Y");
            try {
                this.getActionProcessor().processAction("SetSDIVersionStatus", "1", props);
                commandResponse.setStatus("ok", keyid1 + "|" + keyid2 + " was successfully Approved");
            }
            catch (ActionException e) {
                commandResponse.setStatus("fail", keyid1 + "|" + keyid2 + " could not be Approved");
            }
        }
        return true;
    }

    private JSONableMap evaluateContentPosition(CommandRequest commandRequest, CommandResponse commandResponse, boolean ignoreArrangementRules) {
        String contentBound = commandRequest.getString("contentbound");
        String repeatCount = commandRequest.getString("repeatcount");
        String dilutionSteps = commandRequest.getString("dilutionsteps");
        String dilutionFactor = commandRequest.getString("dilutionfactor");
        boolean diluteFirst = commandRequest.getBoolean("dilutefirstflag");
        String treatmentCount = commandRequest.getString("treatmentcount");
        String horizontalPriority = commandRequest.getString("loadingpriorityhorizontal");
        String verticalPriority = commandRequest.getString("loadingpriorityvertical");
        String availableCells = commandRequest.getString("availablecells");
        String emptyCells = commandRequest.getString("emptycells");
        String selectedItems = commandRequest.getString("selectedsamples");
        String targetselectedcells = commandRequest.getString("targetselectedcells");
        JSONArray selectedCells = null;
        try {
            selectedCells = new JSONArray(targetselectedcells);
        }
        catch (JSONException e) {
            Trace.logError("Selected cells invalid");
        }
        String loadingDirection = commandRequest.getString("loadingdirection");
        if (!horizontalPriority.contains("C") && !verticalPriority.contains("C")) {
            horizontalPriority = "C";
        }
        if (ignoreArrangementRules) {
            contentBound = "1";
            repeatCount = "1";
            dilutionSteps = "1";
            dilutionFactor = "1";
            treatmentCount = "1";
        }
        JSONableMap jsonObject = new JSONableMap();
        EvaluateArrayLayoutRules evalRules = new EvaluateArrayLayoutRules(contentBound != null && contentBound.length() > 0 ? Integer.parseInt(contentBound) : 1, repeatCount != null && repeatCount.length() > 0 ? Integer.parseInt(repeatCount) : 1, dilutionSteps != null && dilutionSteps.length() > 0 ? Integer.parseInt(dilutionSteps) : 1);
        try {
            WellValues[][] wellValues = evalRules.plateArray(availableCells, contentBound != null && contentBound.length() > 0 ? Integer.parseInt(contentBound) : 1, repeatCount != null && repeatCount.length() > 0 ? Integer.parseInt(repeatCount) : 1, dilutionSteps != null && dilutionSteps.length() > 0 ? Integer.parseInt(dilutionSteps) : 1, dilutionFactor != null && dilutionFactor.length() > 0 ? Float.parseFloat(dilutionFactor) : 0.0f, diluteFirst, treatmentCount != null && treatmentCount.length() > 0 ? Integer.parseInt(treatmentCount) : 1, horizontalPriority, verticalPriority);
            WellValues[][] loadedWellValues = evalRules.applyLoadingDirection(selectedItems, selectedCells, emptyCells, loadingDirection, wellValues);
            for (int h = 0; h < loadedWellValues.length; ++h) {
                for (int v = 0; v < loadedWellValues[0].length; ++v) {
                    String key = h + "," + v;
                    if (loadedWellValues[h][v] == null) continue;
                    String val = loadedWellValues[h][v].sample + ";" + loadedWellValues[h][v].repeat + ";" + loadedWellValues[h][v].treatment + ";" + loadedWellValues[h][v].dilutionfactor + ";" + loadedWellValues[h][v].dilution + ";" + loadedWellValues[h][v].sampleid;
                    jsonObject.put(key, val);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            commandResponse.setStatus("fail", "Unknown exception:" + e.getMessage());
            return jsonObject;
        }
        if (jsonObject.isEmpty()) {
            jsonObject = this.evaluateContentPosition(commandRequest, commandResponse, true);
        }
        commandResponse.set("loadeddata", jsonObject);
        return jsonObject;
    }

    private DataSet fetchTrackData(String trackitemids) throws SapphireException {
        String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemids, "", "");
        String sql = "SELECT r.keyid1,        reagentlotid,        reagenttypeid FROM   (SELECT *         FROM   rsetitems         WHERE  rsetitems.rsetid = ? ) r        LEFT OUTER JOIN trackitem                     ON trackitem.trackitemid = r.keyid1        LEFT OUTER JOIN reagentlot                     ON trackitem.linkkeyid1 = reagentlot.reagentlotid";
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
    }

    private DataSet getSampleType(String selectedItems) {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setKeyid1List(selectedItems);
        sdiRequest.setSDCid("Sample");
        sdiRequest.setRequestItem("primary");
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        return sdiData.getDataset("primary");
    }

    private DataSet fetchReagentInfo(SDIMaint arrayitemMaint) throws SapphireException {
        DataSet arrayitemcontent = arrayitemMaint.getDataset("arrayitemcontent");
        if (!arrayitemcontent.isEmpty()) {
            HashMap<String, String> filtermap = new HashMap<String, String>();
            filtermap.put("contentsdcid", "TrackItemSDC");
            DataSet filDS = arrayitemcontent.getFilteredDataSet(filtermap);
            if (!filDS.isEmpty()) {
                HashSet<String> trackitemIDs = new HashSet<String>();
                for (int i = 0; i < filDS.size(); ++i) {
                    trackitemIDs.add(filDS.getValue(i, "contentkeyid1"));
                }
                String rsetid = this.getDAMProcessor().createRSet("LV_ArrayItem", this.getDelimitedString(trackitemIDs, ";", ""), "", "");
                String sql = "SELECT track.trackitemid trackid,        reagentlotid,        reagenttypeid,        reagenttypeversionid FROM   reagentlot        JOIN (SELECT trackitemid,                     linkkeyid1              FROM   trackitem                     JOIN rsetitems                       ON linksdcid = 'LV_ReagentLot'                          AND trackitem.trackitemid = rsetitems.keyid1                          AND rsetitems.rsetid = ? ) track          ON track.linkkeyid1 = reagentlotid ";
                return this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
            }
        }
        return null;
    }

    private DataSet fetchSourceArrayItemData(SDIMaint arrayitemMaint) throws SapphireException {
        DataSet arrayitemcontent = arrayitemMaint.getDataset("arrayitemcontent");
        if (!arrayitemcontent.isEmpty()) {
            HashMap<String, String> filtermap = new HashMap<String, String>();
            filtermap.put("contentsdcid", "LV_ArrayItem");
            DataSet filDS = arrayitemcontent.getFilteredDataSet(filtermap);
            if (!filDS.isEmpty()) {
                HashSet<String> sourceArrayItemID = new HashSet<String>();
                for (int i = 0; i < filDS.size(); ++i) {
                    sourceArrayItemID.add(filDS.getValue(i, "contentkeyid1"));
                }
                String rsetid = this.getDAMProcessor().createRSet("LV_ArrayItem", this.getDelimitedString(sourceArrayItemID, ";", ""), "", "");
                String sql = "SELECT arrayitemid,        itemlabel FROM   arrayitem ai        JOIN (SELECT keyid1              FROM   rsetitems              WHERE  rsetid = ? ) rs          ON rs.keyid1 = ai.arrayitemid";
                DataSet sqlDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
                return sqlDataSet;
            }
        }
        return null;
    }

    private void concUnitConversion(String toconcunit, FormatUtil formatUtil, DataSet samples) throws SapphireException {
        if (samples != null && !samples.isEmpty()) {
            samples.addColumn("toconcentration", 0);
            samples.addColumn("toconcentrationunits", 0);
            for (int i = 0; i < samples.size(); ++i) {
                String concentration = samples.getValue(i, "concentration");
                String fromunit = samples.getValue(i, "concentrationunits");
                String toconcun = toconcunit;
                if (toconcun.trim().length() == 0) {
                    toconcun = fromunit;
                }
                if (concentration == null || concentration.length() <= 0) continue;
                double dqty2 = formatUtil.parseBigDecimal(concentration).setScale(3, 4).doubleValue();
                String newqty2 = UnitsUtil.getConvertedValue(this.getQueryProcessor(), fromunit, toconcun, Double.toString(dqty2));
                if (newqty2 != null) {
                    dqty2 = Double.parseDouble(newqty2.replace(new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator(), '.'));
                }
                samples.setString(i, "toconcentration", formatUtil.format(formatUtil.parseBigDecimal(String.valueOf(dqty2))));
                samples.setString(i, "toconcentrationunits", toconcun);
            }
        }
    }

    private void volumeUnitConversion(String tounit, DataSet trackitems, FormatUtil formatUtil) throws SapphireException {
        if (trackitems != null && !trackitems.isEmpty()) {
            trackitems.addColumn("toqtycurrent", 0);
            trackitems.addColumn("toqtyunits", 0);
            for (int i = 0; i < trackitems.size(); ++i) {
                String qtycurrent = trackitems.getValue(i, "qtycurrent");
                String fromunit = trackitems.getValue(i, "qtyunits");
                if (qtycurrent == null || qtycurrent.length() <= 0) continue;
                double dqty2 = formatUtil.parseBigDecimal(qtycurrent).setScale(3, 4).doubleValue();
                String newqty2 = UnitsUtil.getConvertedValue(this.getQueryProcessor(), fromunit, tounit, Double.toString(dqty2));
                if (newqty2 != null) {
                    dqty2 = Double.parseDouble(newqty2.replace(new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator(), '.'));
                }
                trackitems.setString(i, "toqtycurrent", formatUtil.format(formatUtil.parseBigDecimal(String.valueOf(dqty2))));
                trackitems.setString(i, "toqtyunits", tounit);
            }
        }
    }

    private SDIMaint fetchArrayZoneData(String arrayid, boolean fetchDS) throws SapphireException {
        SDIMaint arrayzoneMaint;
        String sql = "select arrayzoneid from arrayzone where arrayid = ? order by usersequence ";
        DataSet arrayzoneDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arrayid});
        if (!arrayzoneDS.isEmpty()) {
            String arrayzoneids = arrayzoneDS.getColumnValues("arrayzoneid", ";");
            SDIProcessor sdiProcessor = this.getSDIProcessor();
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setKeyid1List(arrayzoneids);
            if (fetchDS) {
                sdiRequest.setRequestItem("dataset");
                sdiRequest.setRequestItem("dataitem");
            }
            sdiRequest.setSDCid("LV_ArrayZone");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("arrayzonecontent");
            sdiRequest.setRequestItem("arrayitemarrayzone");
            sdiRequest.setExtendedDataTypes(true);
            SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
            arrayzoneMaint = new SDIMaint(this.getSDCProcessor().getPropertyList(sdiRequest.getSDCid()), sdiData);
        } else {
            arrayzoneMaint = new SDIMaint(this.getSDCProcessor().getPropertyList("LV_ArrayZone"));
        }
        return arrayzoneMaint;
    }

    private Collection getZoneIdCollection(DataSet arrayzoneDS) {
        HashSet<String> zoneidCollection = new HashSet<String>();
        for (int i = 0; i < arrayzoneDS.size(); ++i) {
            zoneidCollection.add(arrayzoneDS.getValue(i, "arrayzoneid"));
        }
        return zoneidCollection;
    }

    private String getDelimitedString(Collection data, String delimiter, String wrapper) {
        String delimitedstr = "";
        for (Object value : data) {
            delimitedstr = delimitedstr + delimiter + wrapper + value + wrapper;
        }
        return delimitedstr.substring(1);
    }

    private SDIMaint fetchArrayItemData(String arrayid, boolean fetchDS) throws SapphireException {
        String sql = "select arrayitemid from arrayitem where arrayid = ?";
        DataSet arrayitemDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{arrayid});
        String arrayitemids = arrayitemDS.getColumnValues("arrayitemid", ";");
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setKeyid1List(arrayitemids);
        if (fetchDS) {
            sdiRequest.setRequestItem("dataset");
            sdiRequest.setRequestItem("dataitem");
        }
        sdiRequest.setSDCid("LV_ArrayItem");
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("arrayitemcontent");
        sdiRequest.setRequestItem("arrayitemarrayzone");
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            BigDecimal totalVol = primary.getBigDecimal(i, "totalvolume");
            if (totalVol == null) continue;
            primary.setValue(i, "totalvolume", m18n.format(totalVol));
        }
        DataSet dataitem = sdiData.getDataset("dataitem");
        if (dataitem != null && dataitem.size() > 0) {
            I18nUtil.localizeDisplayValues(dataitem, this.connectionInfo);
        }
        DataSet arrayitemcontent = sdiData.getDataset("arrayitemcontent");
        arrayitemcontent.sort("arrayitemid,arrayitemcontentid,usersequence");
        sdiData.setDataset("arrayitemcontent", arrayitemcontent);
        return new SDIMaint(this.getSDCProcessor().getPropertyList(sdiRequest.getSDCid()), sdiData);
    }

    private String findAlias(String queryfrom, String fromClause) {
        String[] split;
        String alias = fromClause;
        for (String s : split = StringUtil.split(queryfrom, ",")) {
            String ali;
            String trimmedali;
            String str = s.trim();
            if (!str.toLowerCase().startsWith(fromClause) || (trimmedali = (ali = str.substring(fromClause.length())).trim()).length() <= 0) continue;
            alias = trimmedali;
        }
        return alias;
    }

    private SDIMaint fetchArrayTypeData(String arraytypeid, String arraytypeversionid) {
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_ArrayType");
        sdiRequest.setKeyid1List(arraytypeid);
        sdiRequest.setKeyid2List(arraytypeversionid);
        sdiRequest.setRequestItem("primary");
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        return new SDIMaint(this.getSDCProcessor().getPropertyList(sdiRequest.getSDCid()), sdiData);
    }

    public Color[] generateColors(int n) {
        Color[] cols = new Color[n];
        for (int i = 0; i < n; ++i) {
            cols[i] = Color.getHSBColor(this.hues[i], this.sats[i], 1.0f);
        }
        return cols;
    }

    public String rgbToHex(int R, int G, int B) {
        return "#" + Integer.toHexString(R) + Integer.toHexString(G) + Integer.toHexString(B);
    }

    public boolean processCommand(CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        ActionService actionService = new ActionService(this.sapphireConnection);
        AuditService auditService = new AuditService(this.sapphireConnection);
        ErrorHandler errorHandler = new ErrorHandler();
        HashMap extraProps = new HashMap();
        String traceLogIdStr = "";
        PropertyList extra = commandRequest.getPropertyList("__pr_extraprops");
        if (extra != null) {
            extraProps.putAll(extra);
        }
        try {
            for (String name : commandRequest.keySet()) {
                Object value = commandRequest.get(name);
                if (!(value instanceof SDIMaint)) continue;
                SDIMaint sdiMaint = (SDIMaint)value;
                sdiMaint.setSDCProps(this.getSDCProcessor().getPropertyList(sdiMaint.getSdcid()));
                this.logInfo("Saving SDIMaint for " + new SDI(sdiMaint.getSdcid(), sdiMaint.getKeyid1(), sdiMaint.getKeyid2(), sdiMaint.getKeyid3()).toString());
                traceLogIdStr = this.save(sdiMaint, actionService, auditService, errorHandler, this, extraProps, this.getSDCProcessor().getPropertyList(sdiMaint.getSdcid()), traceLogIdStr);
                commandResponse.set(name, sdiMaint);
                commandResponse.set("sdcid", sdiMaint.getSdcid());
            }
            commandResponse.set("ERRORHANDLER", errorHandler);
            boolean it = true;
            return it;
        }
        catch (ServiceException se) {
            commandResponse.set("ERRORHANDLER", errorHandler);
            boolean bl = true;
            return bl;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to save SDIMaint data. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        finally {
            if (traceLogIdStr.length() > 0) {
                try {
                    auditService.removeTracelogIdFromDBSession();
                }
                catch (ServiceException serviceException) {}
            }
        }
    }

    public String save(SDIMaint sdiMaint, ActionService actionService, AuditService auditService, ErrorHandler errorHandler, PropertyHandler logger, HashMap extraProps, PropertyList sdcProps, String traceLogIdStr) throws SapphireException, ServiceException {
        HashMap<String, String> sdiprops = new HashMap<String, String>();
        PropertyList props = new PropertyList();
        PropertyList editprops = new PropertyList();
        PropertyList delprops = new PropertyList();
        PropertyList addprops = new PropertyList();
        PropertyList returnkeylist = new PropertyList();
        String sdcid = sdiMaint.getSdcid();
        String separator = ";";
        for (Object key : extraProps.keySet()) {
            String value = (String)extraProps.get(key);
            String ky = (String)key;
            props.setProperty(ky, value);
            sdiprops.put(ky, value);
        }
        try {
            if (separator == null || separator.length() == 0) {
                separator = ";";
            }
        }
        catch (Exception e) {
            separator = ";";
        }
        Set datasets = sdiMaint.getDatasets();
        for (String datasetname : datasets) {
            Table dataset = (Table)sdiMaint.getDataset(datasetname);
            if (dataset == null || "dataitem".equals(datasetname) && (!"dataitem".equals(datasetname) || ((SDIDataItemTable)sdiMaint.getDataset("dataitem")).getDataentry() != null)) continue;
            String[] keyCols = sdiMaint.getKeys(datasetname);
            String linkid = sdiMaint.getLinkid(datasetname);
            String detailLinkId = "";
            if (linkid == null || linkid.length() == 0) {
                linkid = sdiMaint.getDetailLinkid(datasetname);
                detailLinkId = sdiMaint.getDetailDetailLinkid(datasetname);
            }
            dataset.getSaveProps(sdcid, datasetname.equals("primary"), linkid, detailLinkId, keyCols, editprops, delprops, addprops, separator);
            traceLogIdStr = TagRequestPropertyHandler.saveDataset(sdiprops, errorHandler, traceLogIdStr, auditService, returnkeylist, props, editprops, delprops, addprops, null, linkid, detailLinkId, sdcid, sdcProps.getProperty("auditedflag"), sdcProps.getProperty("auditpromptflag"), actionService, linkid != null && linkid.length() > 0, detailLinkId != null && detailLinkId.length() > 0, datasetname, keyCols, separator, logger);
            addprops.clear();
            editprops.clear();
            delprops.clear();
        }
        if (sdiMaint.getDataset("dataitem") != null && ((SDIDataItemTable)sdiMaint.getDataset("dataitem")).getDataentry() != null) {
            try {
                ActionBlock deActionBlock = JSONSaveRequest.buildDataEntryActionBlock(sdiMaint, extraProps, actionService.getConnectionId());
                if (deActionBlock != null) {
                    actionService.processActionBlock(deActionBlock);
                }
            }
            catch (Exception e) {
                throw new ServiceException(e);
            }
        }
        return traceLogIdStr;
    }

    private DataSet getQueriesDataSet(String sdcid, PropertyList advancedSearchProperties, String arraymethodqueryid, String retrievelimit) {
        if (arraymethodqueryid == null || arraymethodqueryid.length() == 0) {
            String queryqueryfrom = "query";
            String queryquerywhere = "query.autoquoteflag='N' and query.basedonid='" + sdcid + "' ";
            PropertyList contentProperties = advancedSearchProperties.getPropertyList("querysearch");
            String categorylist = contentProperties.getProperty("category");
            String filterlist = contentProperties.getProperty("filter");
            if (categorylist != null && categorylist.trim().length() > 0) {
                queryqueryfrom = queryqueryfrom + ",categoryitem";
                queryquerywhere = queryquerywhere + " and query.queryid=categoryitem.keyid1 and categoryitem.sdcid='Query' and categoryid in " + SearchUtil.toQueryInClause(categorylist);
            }
            if (filterlist != null && filterlist.trim().length() > 0) {
                queryquerywhere = queryquerywhere + " and query.queryid in " + SearchUtil.toQueryInClause(filterlist);
            }
            SDIProcessor sdip = this.getSDIProcessor();
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setQueryFrom(queryqueryfrom);
            sdiRequest.setQueryWhere(queryquerywhere);
            sdiRequest.setQueryOrderBy("query.usersequence, querylabel, queryid");
            sdiRequest.setRequestItem("primary[queryid,querydesc,querylabel]");
            sdiRequest.setSDCid("Query");
            sdiRequest.setRetrieve(true);
            sdiRequest.setRetrieveLimit(Integer.parseInt(retrievelimit));
            DataSet queryids = sdip.getSDIData(sdiRequest).getDataset("primary");
            return queryids;
        }
        String queryqueryfrom = "query";
        String queryquerywhere = "query.autoquoteflag='N' and query.basedonid='" + sdcid + "' and query.queryid='" + arraymethodqueryid + "'";
        SDIProcessor sdip = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setQueryFrom(queryqueryfrom);
        sdiRequest.setQueryWhere(queryquerywhere);
        sdiRequest.setQueryOrderBy("query.usersequence, querylabel, queryid");
        sdiRequest.setRequestItem("primary[queryid,querydesc,querylabel]");
        sdiRequest.setSDCid("Query");
        sdiRequest.setRetrieve(true);
        sdiRequest.setRetrieveLimit(Integer.parseInt(retrievelimit));
        DataSet queryidds = sdip.getSDIData(sdiRequest).getDataset("primary");
        return queryidds;
    }

    DataSet getQueryArgDataSet(String sdcid, DataSet queriesDataSet, String queryparams) {
        String queryidlist = "";
        for (int i = 0; i < queriesDataSet.getRowCount(); ++i) {
            String queryid = queriesDataSet.getString(i, "queryid");
            queryidlist = queryidlist + ";" + queryid;
        }
        if (queryidlist.length() > 0) {
            queryidlist = queryidlist.substring(1);
        }
        SafeSQL safeSQL = new SafeSQL();
        String selectArgs = "SELECT query.queryid, query.basedonid, query.cascadedargflag, query.querydesc, queryarg.argid, queryarg.usersequence, queryarg.argdesc, queryarg.argtype, queryarg.sdcid, queryarg.reftypeid, queryarg.argdata, queryarg.defaultvalue, queryarg.arginto, queryarg.weblookupurl, queryarg.mandatoryflag, queryarg.editorstyleid, queryarg.useeditorstyleflag FROM query, queryarg WHERE query.queryid=queryarg.queryid  AND query.basedonid=queryarg.basedonid AND query.queryid in (" + safeSQL.addIn(queryidlist, ";") + ") and query.basedonid=" + safeSQL.addVar(sdcid) + " order by query.queryid, queryarg.usersequence";
        DataSet queryargs = this.getQueryProcessor().getPreparedSqlDataSet(selectArgs, safeSQL.getValues());
        String[] queryparamlist = StringUtil.split(queryparams, ";");
        for (int i = 0; i < queryargs.getRowCount(); ++i) {
            queryargs.setValue(i, "argdesc", this.getTranslationProcessor().translate(queryargs.getValue(i, "argdesc").length() == 0 ? queryargs.getString(i, "argid") : queryargs.getValue(i, "argdesc")));
            if (queryparamlist.length <= i) continue;
            queryargs.setValue(i, "defaultvalue", queryparamlist[i]);
        }
        return queryargs;
    }

    private String getDefaultQueryId(PropertyList advancedSearchProperties, String queryid) {
        if (queryid != null && queryid.length() > 0) {
            return queryid;
        }
        PropertyList contentProperties = advancedSearchProperties.getPropertyList("querysearch");
        return contentProperties.getProperty("default");
    }
}


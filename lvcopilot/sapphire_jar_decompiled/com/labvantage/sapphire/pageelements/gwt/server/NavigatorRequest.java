/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteria;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteriaArg;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteriaArgGroup;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPropertyHandler;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryRequest;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class NavigatorRequest
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static final String POLICY_ID = "NavigatorPolicy";
    public static final String PROPERTY_TREE_NODES = "treenodes";
    public static final String PROPERTY_CHILD_NODES = "childnodes";
    public static final String NEW_NODE_PREFIX = "(newnode)_";
    private int maxRows = 2500;
    private static String[] _datasetnames = new String[]{"primary", "attachment", "dataset", "dataitem", "datalimit", "dataapproval", "datarelation", "dataspec", "sdispec", "sdispecrule", "address", "coc", "pricelist", "category", "role", "sdiworkitem", "sdiworkitemitem", "approval", "approvalstep", "document", "formrule", "pricelistitem", "chargelistitem", "workgroupitem", "workgroupparamlist", "trackitem"};
    private static String[][] defaultDataSetColumns = new String[][]{{"keyid1", "keyid2", "keyid3"}, {"attachmentnum"}, {"paramlistid", "paramlistversionid", "variantid", "dataset"}, {"paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"}, {"paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid", "limittypeid"}, {"paramlistid", "paramlistversionid", "variantid", "dataset", "approvalstep"}, {"paramlistid", "paramlistversionid", "variantid", "dataset", "relationid"}, {"paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid", "specid", "specversionid"}, {"specid", "specversionid"}, {"specid", "specversionid", "ruleno"}, {"addressid", "addresstype", "contactfunction"}, {"cocid"}, {"pricelistid"}, {"categoryid"}, {"roleid", "privid"}, {"workitemid", "workiteminstance"}, {"workitemid", "workiteminstance", "workitemitemid"}, {"approvaltypeid"}, {"approvaltypeid", "approvalstep", "approvalstepinstance"}, {"documentid", "documentversionid"}, {"formid", "forminstance"}, {"pricelistid", "pricelistitemid"}, {"chargelistid", "chargelistitemid"}, {"workgroupid", "workgroupitemid"}, {"workgroupid", "paramlistid", "paramlistversionid", "variantid"}, {"trackitemid"}};
    static HashMap datasetReturnColumnsMap = new HashMap();

    public void processAddNewNode(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        HashMap<String, String> requestMap = new HashMap<String, String>();
        Enumeration enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String key = (String)enumeration.nextElement();
            requestMap.put(key, request.getParameter(key));
        }
        String operation = request.getParameter("operation");
        String nodepropertylistStr = request.getParameter("nodepropertylist");
        try {
            JSONObject nodeJSON = new JSONObject(nodepropertylistStr);
            PropertyList nodepropertylist = new PropertyList(nodeJSON);
            PropertyListCollection operations = nodepropertylist.getCollection("operations");
            PropertyList operationPL = null;
            for (int i = 0; i < operations.size(); ++i) {
                if (!operations.getPropertyList(i).getProperty("name").equals(operation)) continue;
                operationPL = operations.getPropertyList(i);
                break;
            }
            if (operationPL != null) {
                String processingscript = operationPL.getPropertyList("addnewnodeprops").getProperty("processingscript");
                ActionBlock ab = new ActionBlock(processingscript);
                ab.setBlockProperties(requestMap);
                ActionProcessor actionProcessor = this.getActionProcessor();
                actionProcessor.processActionBlock(ab);
            }
            response.getWriter().write(nodepropertylist.toJSONString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block26: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            String connectionid = this.getConnectionId();
            sapphire.util.ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(connectionid);
            String mode = request.getParameter("mode");
            if ("addnewnode".equals(mode)) {
                this.processAddNewNode(request, response, servletContext);
            } else {
                String groovyscript = ajaxResponse.getRequestParameter("validationscript");
                String sdcid = request.getParameter("sdcid");
                String keyid1 = request.getParameter("keyid1");
                String keyid2 = request.getParameter("keyid2");
                String keyid3 = request.getParameter("keyid3");
                try {
                    if (groovyscript != null && groovyscript.length() > 0 || "evaluategroovyexpression".equals(mode)) {
                        String nodepropertylist = request.getParameter("nodepropertylist");
                        HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
                        bindMap.put("user", connectionInfo.getUserAttributeMap());
                        if (sdcid != null && sdcid.length() > 0) {
                            bindMap.put("sdc", this.getSDCProcessor().getPropertyList(sdcid));
                        }
                        if (nodepropertylist == null || nodepropertylist.length() == 0) {
                            sdcid = ajaxResponse.getRequestParameter("sdcid");
                            keyid1 = ajaxResponse.getRequestParameter("keyid1");
                            keyid2 = ajaxResponse.getRequestParameter("keyid2");
                            keyid3 = ajaxResponse.getRequestParameter("keyid3");
                            bindMap.putAll(this.getBindMap(sdcid, keyid1, keyid2, keyid3, null));
                            try {
                                String validationMessage = GroovyUtil.getInstance(connectionInfo).evaluateSecure(groovyscript, bindMap);
                                if (validationMessage.length() > 0) {
                                    validationMessage = this.getTranslationProcessor().translate(validationMessage);
                                }
                                ajaxResponse.addCallbackArgument("message", validationMessage);
                                ajaxResponse.print();
                            }
                            catch (Throwable t) {
                                ajaxResponse.addCallbackArgument("message", t.getMessage());
                                ajaxResponse.print();
                            }
                        } else {
                            PropertyList pl = new PropertyList(new JSONObject(nodepropertylist));
                            String rowData = request.getParameter("rowdata");
                            if (keyid1.indexOf(";") < 0) {
                                if (rowData != null && rowData.length() > 0) {
                                    bindMap.put("primary", new PropertyList(new JSONObject(rowData)));
                                } else {
                                    bindMap.putAll(this.getBindMap(sdcid, keyid1, keyid2, keyid3, pl));
                                }
                                pl = this.evaluationNodeOperationShowGroovy(connectionInfo, pl, bindMap);
                                response.getWriter().write(pl.toJSONString());
                            } else {
                                String groovyExpression = request.getParameter("groovy");
                                SDIData sdiData = this.getSDIData(sdcid, keyid1, keyid2, keyid3, pl);
                                String[] keyid1s = StringUtil.split(keyid1, ";");
                                String[] keyid2s = StringUtil.split(keyid2, ";");
                                String[] keyid3s = StringUtil.split(keyid3, ";");
                                StringBuffer sb = new StringBuffer();
                                for (int i = 0; i < keyid1s.length; ++i) {
                                    HashMap databindMap = this.getBindMap(sdcid, keyid1s[i], keyid2s.length > i ? keyid2s[i] : "", keyid3s.length > i ? keyid3s[i] : "", pl, sdiData);
                                    bindMap.putAll(databindMap);
                                    String show = GroovyUtil.getInstance(connectionInfo).evaluateSecure(groovyExpression, bindMap);
                                    if (!"false".equals(show) && !"N".equals(show)) continue;
                                    sb.append(";" + keyid1s[i]);
                                    if (keyid2s.length > i && keyid2s[i].length() > 0) {
                                        sb.append(";" + keyid2s[i]);
                                    }
                                    if (keyid3s.length <= i || keyid3s[i].length() <= 0) continue;
                                    sb.append(";" + keyid3s[i]);
                                }
                                response.getWriter().write(sb.length() > 0 ? sb.substring(1) : "");
                            }
                        }
                        break block26;
                    }
                    String navigatornodeid = request.getParameter("navigatornodeid");
                    String pageid = request.getParameter("pageid");
                    boolean isExpandNode = "expandnode".equals(mode);
                    RequestProcessor requestProcessor = new RequestProcessor(connectionid);
                    SDCProcessor sdcProcessor = this.getSDCProcessor();
                    SDIProcessor sdiProcessor = new SDIProcessor(connectionid);
                    PropertyList element = null;
                    ConfigurationProcessor configProcessor = new ConfigurationProcessor(connectionid);
                    WebAdminProcessor webAdminProcessor = new WebAdminProcessor(connectionid);
                    boolean isEditSet = "EditSet".equals(request.getParameter("mode"));
                    element = isEditSet ? NavigatorRequest.getEditSetPropertyList(connectionInfo, navigatornodeid, sdcid, pageid, configProcessor, webAdminProcessor) : NavigatorRequest.getNavigatorPolicy(connectionInfo, navigatornodeid, sdcid, configProcessor, webAdminProcessor);
                    if (element != null && element.getProperty("maxrows").length() > 0) {
                        try {
                            this.maxRows = Integer.parseInt(element.getProperty("maxrows"));
                        }
                        catch (Exception i) {
                            // empty catch block
                        }
                    }
                    JSONObject jsonResponseObj = null;
                    if (isExpandNode) {
                        sdcid = request.getParameter("parentsdcid");
                        keyid1 = request.getParameter("parentkeyid1");
                        keyid2 = request.getParameter("parentkeyid2");
                        keyid3 = request.getParameter("parentkeyid3");
                        String nodepropertylistStr = request.getParameter("nodepropertylist");
                        JSONObject nodeJSON = new JSONObject(nodepropertylistStr);
                        PropertyList nodepropertylist = new PropertyList(nodeJSON);
                        jsonResponseObj = this.buildExpandNodeJSONResponseObject(sdcid, keyid1, keyid2, keyid3, nodepropertylist, sdcProcessor, sdiProcessor, element);
                    } else {
                        if (element == null || element.size() == 0) {
                            throw new Exception("Cannot retrieve NavigatorPolicy property for node " + navigatornodeid + " or " + navigatornodeid + " Custom");
                        }
                        jsonResponseObj = this.buildInitialJSONResponseObject(sdcid, keyid1, keyid2, keyid3, element, sdcProcessor, sdiProcessor, requestProcessor);
                    }
                    if (jsonResponseObj != null) {
                        String jsonString = jsonResponseObj.toString();
                        response.getWriter().write(jsonString);
                        break block26;
                    }
                    throw new Exception("Cannot Contruct Response");
                }
                catch (Throwable e) {
                    try {
                        Trace.logError("Error Navigator Response", e);
                        response.getWriter().print("Error:" + e.getMessage());
                    }
                    catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }
    }

    public JSONObject buildExpandNodeJSONResponseObject(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList nodePropertyList, SDCProcessor sdcProcessor, SDIProcessor sdiProcessor, PropertyList element) throws Exception {
        NavigatorRequest.findAndMergeRefNodePL(nodePropertyList, element, 0);
        PropertyList childDataPL = nodePropertyList.getPropertyList("childdata");
        PropertyList parentDataPL = nodePropertyList.getPropertyList("parentdata");
        String show = nodePropertyList.getProperty("show");
        if (show.indexOf("$G{") == 0) {
            parentDataPL.put("primary", parentDataPL.copy());
            show = GroovyUtil.getInstance(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId())).evaluateSecure(show, parentDataPL);
        }
        if ("N".equals(show) || "false".equals(show)) {
            JSONObject jsonResponseObj = new JSONObject();
            jsonResponseObj.put("show", "N");
            return jsonResponseObj;
        }
        DataSet contextDataSet = null;
        SDIData sdidata = null;
        String childsql = childDataPL != null ? childDataPL.getProperty("sql").trim() : "";
        boolean isDetail = false;
        SafeSQL safeSQL = new SafeSQL();
        if (childsql.length() > 0) {
            if (childsql.indexOf("[sdcid]") >= 0) {
                childsql = SafeSQL.replaceAllWithVars(childsql, "'[sdcid]'", sdcid, safeSQL);
                childsql = StringUtil.replaceAll(childsql, "[sdcid]", sdcid);
            }
            if (childsql.indexOf("[keyid1]") >= 0) {
                childsql = SafeSQL.replaceAllWithVars(childsql, "'[keyid1]'", keyid1, safeSQL);
                childsql = StringUtil.replaceAll(childsql, "[keyid1]", keyid1);
            }
            if (childsql.indexOf("[keyid2]") >= 0) {
                childsql = SafeSQL.replaceAllWithVars(childsql, "'[keyid2]'", keyid2, safeSQL);
                childsql = StringUtil.replaceAll(childsql, "[keyid2]", keyid2);
            }
            if (childsql.indexOf("[keyid3]") >= 0) {
                childsql = SafeSQL.replaceAllWithVars(childsql, "'[keyid3]'", keyid3, safeSQL);
                childsql = StringUtil.replaceAll(childsql, "[keyid3]", keyid3);
            }
            childsql = this.replaceCurrentUser(childsql, safeSQL);
            String[] tokens = StringUtil.getTokens(childsql);
            for (int i = 0; i < tokens.length; ++i) {
                childsql = SafeSQL.replaceAllWithVars(childsql, "'[" + tokens[i] + "]'", parentDataPL.getProperty(tokens[i]), safeSQL);
                childsql = StringUtil.replaceAll(childsql, "[" + tokens[i] + "]", parentDataPL.getProperty(tokens[i]));
            }
            contextDataSet = this.getQueryProcessor().getPreparedSqlDataSet(childsql, safeSQL.getValues());
            if (contextDataSet == null) {
                throw new Exception("Error retrieving data set using: " + childsql);
            }
        } else if (nodePropertyList.getProperty("sdcid").length() > 0) {
            String queryfrom = childDataPL.getProperty("queryfrom");
            String querysdcid = childDataPL.getProperty("sdcid");
            String querywhere = childDataPL.getProperty("querywhere");
            String queryorderby = childDataPL.getProperty("queryorderby");
            SDIRequest sdiRequest = this.buildSDIRequest(nodePropertyList);
            if (queryfrom.length() == 0) {
                queryfrom = this.getSDCProcessor().getProperty(querysdcid, "tableid");
            }
            boolean isOracle = this.getConnectionProcessor().isOra();
            sdiRequest.setQueryFrom(queryfrom);
            querywhere = StringUtil.replaceAll(querywhere, "[sdcid]", SafeSQL.encodeForSQL(sdcid, isOracle));
            querywhere = StringUtil.replaceAll(querywhere, "[keyid1]", SafeSQL.encodeForSQL(keyid1, isOracle));
            querywhere = StringUtil.replaceAll(querywhere, "[keyid2]", SafeSQL.encodeForSQL(keyid2, isOracle));
            querywhere = StringUtil.replaceAll(querywhere, "[keyid3]", SafeSQL.encodeForSQL(keyid3, isOracle));
            querywhere = this.replaceCurrentUser(querywhere);
            String[] tokens = StringUtil.getTokens(querywhere);
            for (int i = 0; i < tokens.length; ++i) {
                querywhere = StringUtil.replaceAll(querywhere, "[" + tokens[i] + "]", SafeSQL.encodeForSQL(parentDataPL.getProperty(tokens[i]), isOracle));
            }
            sdiRequest.setQueryWhere(querywhere);
            if (queryorderby.length() > 0) {
                sdiRequest.setQueryOrderBy(queryorderby);
                sdiRequest.setUseRSetOrderBy(true);
            }
            if ((sdidata = sdiProcessor.getSDIData(sdiRequest)) == null) {
                throw new Exception("Error retrieving data set using \nsdcid " + sdiRequest.getSDCid() + "\nquery from: " + sdiRequest.getQueryFrom() + "\nquery where: " + sdiRequest.getQueryWhere() + "\nquery order by:" + sdiRequest.getQueryOrderBy());
            }
            contextDataSet = sdidata.getDataset("primary");
        } else if (nodePropertyList.getProperty("detailname").length() > 0) {
            String detailname = nodePropertyList.getProperty("detailname");
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcid);
            sdiRequest.setKeyid1List(keyid1);
            if (keyid2 != null && keyid2.length() > 0 && keyid2.indexOf("(null)") < 0) {
                sdiRequest.setKeyid2List(keyid2);
            }
            if (keyid3 != null && keyid3.length() > 0 && keyid3.indexOf("(null)") < 0) {
                sdiRequest.setKeyid3List(keyid3);
            }
            sdiRequest.setRequestItem(detailname);
            if ("dataset".equals(detailname) && parentDataPL.getProperty("workitemid").length() > 0) {
                sdiRequest.setRequestItem("sdiworkitemitem");
            }
            sdidata = sdiProcessor.getSDIData(sdiRequest);
            contextDataSet = sdidata.getDataset(detailname);
            if ("dataset".equals(detailname) && parentDataPL.getProperty("workitemid").length() > 0) {
                HashMap<String, Object> filter = new HashMap<String, Object>();
                String workitemid = parentDataPL.getProperty("workitemid");
                String workiteminstance = parentDataPL.getProperty("workiteminstance");
                filter.put("sourceworkitemid", workitemid);
                filter.put("sourceworkiteminstance", new BigDecimal(workiteminstance));
                contextDataSet = contextDataSet.getFilteredDataSet(filter);
                if (contextDataSet.size() == 0) {
                    DataSet sdiwiiDs = sdidata.getDataset("sdiworkitemitem");
                    filter = new HashMap();
                    filter.put("itemsdcid", "ParamList");
                    filter.put("workitemid", workitemid);
                    filter.put("workiteminstance", new BigDecimal(workiteminstance));
                    DataSet plInWI = sdiwiiDs.getFilteredDataSet(filter);
                    DataSet wholeDs = sdidata.getDataset(detailname);
                    for (int p = 0; p < plInWI.size(); ++p) {
                        filter = new HashMap();
                        filter.put("paramlistid", plInWI.getValue(p, "itemkeyid1"));
                        filter.put("paramlistversionid", plInWI.getValue(p, "itemkeyid2"));
                        filter.put("variantid", plInWI.getValue(p, "itemkeyid3"));
                        filter.put("dataset", new BigDecimal(plInWI.getValue(p, "iteminstance")));
                        DataSet datasetFromWII = wholeDs.getFilteredDataSet(filter);
                        contextDataSet.addAll(datasetFromWII);
                    }
                }
            }
            if ("dataset".equals(detailname)) {
                contextDataSet.sort("usersequence, dataset");
            } else if ("sdiworkitem".equals(detailname)) {
                contextDataSet.addColumn("sortsequence", 1);
                contextDataSet.addColumn("workitemtypeflagdisplay", 0);
                for (int i = 0; i < contextDataSet.getRowCount(); ++i) {
                    String groupid = contextDataSet.getValue(i, "groupid");
                    BigDecimal groupinstance = contextDataSet.getBigDecimal(i, "groupinstance");
                    HashMap<String, Object> findMap = new HashMap<String, Object>();
                    findMap.put("workitemid", groupid);
                    findMap.put("groupinstance", groupinstance);
                    if (groupid.length() > 0) {
                        int parentrow = contextDataSet.findRow(findMap);
                        if (parentrow < 0) continue;
                        contextDataSet.setNumber(i, "sortsequence", contextDataSet.getBigDecimal(parentrow, "usersequence"));
                        contextDataSet.setValue(i, "workitemtypeflagdisplay", "P".equals(contextDataSet.getValue(i, "workitemtypeflag")) ? "Parent" : "Child");
                        continue;
                    }
                    contextDataSet.setNumber(i, "sortsequence", contextDataSet.getBigDecimal(i, "usersequence"));
                    contextDataSet.setValue(i, "workitemtypeflagdisplay", "Orphan");
                }
                contextDataSet.sort("sortsequence, groupid, groupinstance, usersequence, workitemtypeflag");
                String i = "";
            }
            isDetail = true;
        } else {
            throw new Exception("Child Data retrieval not defined.");
        }
        if (contextDataSet != null && contextDataSet.getRowCount() > this.maxRows) {
            throw new Exception("Maximum " + this.maxRows + " records exceeded.");
        }
        JSONObject jsonResponseObj = new JSONObject();
        String nodeorsdcid = nodePropertyList.getProperty("sdcid").length() == 0 ? nodePropertyList.getProperty("nodeid") : nodePropertyList.getProperty("sdcid");
        String sdcids = StringUtil.repeat(nodeorsdcid, contextDataSet.getColumnCount(), ",");
        jsonResponseObj.put("contextdata", JSONUtil.toJSONObject(contextDataSet));
        jsonResponseObj.put("nodePropertyList", nodePropertyList.toJSONObject(false, false));
        jsonResponseObj.put("contextsdcids", sdcids);
        if (sdidata != null && !isDetail) {
            this.addEntrySDIDataWithDetails(jsonResponseObj, sdidata);
        }
        return jsonResponseObj;
    }

    public JSONObject buildInitialJSONResponseObject(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList element, SDCProcessor sdcProcessor, SDIProcessor sdiProcessor, RequestProcessor requestProcessor) throws Exception {
        long starttime = System.currentTimeMillis();
        JSONObject jsonResponseObj = new JSONObject();
        if (sdcid != null && sdcid.length() > 0) {
            try {
                PropertyList addnewoperationPL;
                SDIRequest sdiRequest = null;
                boolean isTreeDataSetDefined = false;
                DataSet contextDataSet = null;
                PropertyList contextdatasetPL = element.getPropertyList("contextdataset");
                PropertyListCollection treenodes = element.getCollection(PROPERTY_TREE_NODES);
                int currentnodeindex = this.getCurrentNodeIndex(sdcid, treenodes);
                PropertyList currentNode = null;
                if (currentnodeindex >= 0) {
                    currentNode = treenodes.getPropertyList(currentnodeindex);
                }
                SafeSQL safeSQL = new SafeSQL();
                if (contextdatasetPL != null && (contextdatasetPL.getProperty("contextsql").length() > 0 || contextdatasetPL.getCollection("topsqls") != null && contextdatasetPL.getCollection("topsqls").size() > 0)) {
                    String contextsql = contextdatasetPL.getProperty("contextsql");
                    String topsql = "";
                    String sql = contextsql;
                    boolean isContextSql = true;
                    String topkeyid1 = "";
                    String topkeyid2 = "";
                    String topkeyid3 = "";
                    if (contextsql.trim().length() == 0 || contextsql.indexOf("[topkeyid1]") > 0) {
                        isContextSql = contextsql.trim().length() > 0;
                        PropertyListCollection topsqls = contextdatasetPL.getCollection("topsqls");
                        if (topsqls != null) {
                            for (int t = 0; t < topsqls.size(); ++t) {
                                if (!sdcid.equals(topsqls.getPropertyList(t).getProperty("sdcid"))) continue;
                                topsql = topsqls.getPropertyList(t).getProperty("topsql");
                                break;
                            }
                        }
                        if ("[keyid1]".equals(topsql)) {
                            topkeyid1 = keyid1;
                            topkeyid2 = keyid2;
                            topkeyid3 = keyid3;
                        } else if (topsql.trim().length() > 0) {
                            SafeSQL topsafeSQL = new SafeSQL();
                            topsql = StringUtil.replaceAll(topsql, "'[keyid1]'", topsafeSQL.addIn(keyid1, ";"));
                            topsql = StringUtil.replaceAll(topsql, "'[keyid2]'", topsafeSQL.addIn(keyid2, ";"));
                            topsql = StringUtil.replaceAll(topsql, "'[keyid3]'", topsafeSQL.addIn(keyid3, ";"));
                            topsql = this.replaceCurrentUser(topsql, topsafeSQL);
                            DataSet topDataSet = this.getQueryProcessor().getPreparedSqlDataSet(topsql, topsafeSQL.getValues());
                            if (topDataSet != null && topDataSet.getRowCount() > 0) {
                                topkeyid1 = topDataSet.getColumnValues(topDataSet.getColumnId(0), ";");
                                if (topDataSet.getColumnCount() > 1) {
                                    topkeyid2 = topDataSet.getColumnValues(topDataSet.getColumnId(1), ";");
                                }
                                if (topDataSet.getColumnCount() > 2) {
                                    topkeyid3 = topDataSet.getColumnValues(topDataSet.getColumnId(2), ";");
                                }
                            } else {
                                throw new Exception("No top sdi retrieved using:" + topsql);
                            }
                            jsonResponseObj.put("topkeyid1", topkeyid1);
                            jsonResponseObj.put("topkeyid2", topkeyid2);
                            jsonResponseObj.put("topkeyid3", topkeyid3);
                        } else {
                            topkeyid1 = keyid1;
                            topkeyid2 = keyid2;
                            topkeyid3 = keyid3;
                        }
                        sql = SafeSQL.replaceAllWithInVars(sql, "'[topkeyid1]'", topkeyid1, ";", safeSQL);
                        sql = SafeSQL.replaceAllWithInVars(sql, "'[topkeyid2]'", topkeyid2, ";", safeSQL);
                        sql = SafeSQL.replaceAllWithInVars(sql, "'[topkeyid3]'", topkeyid3, ";", safeSQL);
                    }
                    sql = SafeSQL.replaceAllWithInVars(sql, "'[keyid1]'", keyid1, ";", safeSQL);
                    sql = SafeSQL.replaceAllWithInVars(sql, "'[keyid2]'", keyid2, ";", safeSQL);
                    sql = SafeSQL.replaceAllWithInVars(sql, "'[keyid3]'", keyid3, ";", safeSQL);
                    sql = this.replaceCurrentUser(sql, safeSQL);
                    if (isContextSql) {
                        contextDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                        for (int t = 0; t < treenodes.size(); ++t) {
                            PropertyList nodePropertyList = treenodes.getPropertyList(t);
                            String nodelabel = nodePropertyList.getProperty("label");
                            String[] tokens = StringUtil.getTokens(nodelabel);
                            for (int tn = 0; tn < tokens.length; ++tn) {
                                if (contextDataSet.isValidColumn(tokens[tn])) continue;
                                throw new Exception("[" + tokens[tn] + "] used in the node label of " + nodePropertyList.getProperty("nodeid") + " but it is not retrieve in the Tree Dataset SQL.");
                            }
                            String nodeimage = nodePropertyList.getProperty("image");
                            tokens = StringUtil.getTokens(nodeimage);
                            for (int tn = 0; tn < tokens.length; ++tn) {
                                if (contextDataSet.isValidColumn(tokens[tn])) continue;
                                throw new Exception("[" + tokens[tn] + "] used in the node image of " + nodePropertyList.getProperty("nodeid") + " but it is not retrieve in the Tree Dataset SQL.");
                            }
                            PropertyListCollection columns = nodePropertyList.getCollection("columns");
                            for (int tn = 0; tn < columns.size(); ++tn) {
                                String colid = columns.getPropertyList(tn).getProperty("columnid");
                                if (colid.length() <= 0 || contextDataSet.isValidColumn(colid)) continue;
                                throw new Exception("Column ID '" + colid + "' defined in the node of " + nodePropertyList.getProperty("nodeid") + " but it is not retrieve in the Tree Dataset SQL.");
                            }
                        }
                        isTreeDataSetDefined = true;
                        if (contextDataSet == null || contextDataSet.getRowCount() == 0) {
                            throw new Exception("No Context Tree Data Retrieved using:" + sql);
                        }
                    } else if (topkeyid1.length() > 0) {
                        keyid1 = topkeyid1;
                        keyid2 = topkeyid2;
                        keyid3 = topkeyid3;
                        isTreeDataSetDefined = false;
                    }
                }
                boolean isSelfLink = false;
                String linkcolumnid = "";
                String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                String desccolid = sdcProcessor.getProperty(sdcid, "desccol");
                String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                if (currentNode != null) {
                    isSelfLink = "Y".equals(currentNode.getProperty("selflink"));
                    if (!isTreeDataSetDefined) {
                        sdiRequest = this.buildSDIRequest(currentNode);
                    }
                    linkcolumnid = currentNode.getProperty("parentlinkid");
                    if (currentNode.getProperty("sdcid").length() > 0 && !currentNode.getProperty("sdcid").equals(sdcid)) {
                        sdcid = currentNode.getProperty("sdcid");
                        keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                        keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                        keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                        desccolid = sdcProcessor.getProperty(sdcid, "desccol");
                        tableid = sdcProcessor.getProperty(sdcid, "tableid");
                        currentNode.setProperty("keycolid1", keycolid1);
                        currentNode.setProperty("keycolid2", keycolid2);
                        currentNode.setProperty("keycolid3", keycolid3);
                    }
                } else {
                    throw new Exception("Could not find a tree node for entry sdc:" + sdcid + ". Please specify correct sdcid in the ref nodes");
                }
                SDIData sdidata = null;
                sapphire.util.ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(sdiProcessor.getConnectionid());
                if (isSelfLink) {
                    if (!isTreeDataSetDefined) {
                        String querywhere;
                        boolean isMSS = connectionInfo.isSqlServer();
                        String string = querywhere = !isMSS ? this.getConnectByOracle(tableid, keycolid1, linkcolumnid, keyid1, 1000) : this.getConnectByMSS(tableid, keycolid1, linkcolumnid, keyid1, 1000);
                        if (isMSS) {
                            DataSet keyid1DS = this.getQueryProcessor().getSqlDataSet(querywhere);
                            String connectedkeyid1 = keyid1DS.getColumnValues(keycolid1, ";");
                            sdiRequest.setKeyid1List(connectedkeyid1);
                        } else {
                            sdiRequest.setQueryWhere(querywhere);
                            sdiRequest.setQueryFrom(tableid);
                        }
                        String orderby = "1";
                        if ("storageunit".equals(tableid)) {
                            orderby = "storageunitindex";
                        }
                        sdiRequest.setQueryOrderBy(orderby);
                        sdidata = sdiProcessor.getSDIData(sdiRequest);
                        contextDataSet = sdidata.getDataset("primary");
                    }
                    jsonResponseObj.put("contextdata", JSONUtil.toJSONObject(contextDataSet));
                    jsonResponseObj.put("parentcolid", linkcolumnid);
                    jsonResponseObj.put("keycolid1", keycolid1);
                    jsonResponseObj.put("sdcid", sdcid);
                } else {
                    String sortby = "";
                    String contextSdcids = "";
                    String hidetoolbarlist = "";
                    if (!isTreeDataSetDefined) {
                        if (sdiRequest != null) {
                            sdiRequest.setSDCid(sdcid);
                            sdiRequest.setKeyid1List(keyid1);
                            sdiRequest.setKeyid2List(keyid2 != null ? keyid2 : "");
                            sdiRequest.setKeyid3List(keyid3 != null ? keyid3 : "");
                            sdidata = sdiProcessor.getSDIData(sdiRequest);
                            contextDataSet = sdidata.getDataset("primary");
                            PropertyListCollection operations = currentNode.getCollectionNotNull("operations");
                            HashMap bindMap = null;
                            block9: for (int i = 0; i < operations.size(); ++i) {
                                PropertyList operation = operations.getPropertyList(i);
                                String script = operation.getProperty("show");
                                if (operation == null || "N".equals(operation.getProperty("applytoset")) || script.indexOf("$G") != 0) continue;
                                for (int row = 0; row < contextDataSet.getRowCount(); ++row) {
                                    if (bindMap == null) {
                                        bindMap = new HashMap();
                                        bindMap.put("user", connectionInfo.getUserAttributeMap());
                                        if (sdcid != null && sdcid.length() > 0) {
                                            bindMap.put("sdc", this.getSDCProcessor().getPropertyList(sdcid));
                                        }
                                        bindMap.put("primarydataset", contextDataSet);
                                    }
                                    bindMap.put("primary", contextDataSet.get(row));
                                    String show = GroovyUtil.getInstance(connectionInfo).evaluateSecure(script, bindMap);
                                    if (!"N".equals(show) && !"false".equals(show)) continue;
                                    hidetoolbarlist = hidetoolbarlist + ";" + operation.getProperty("name");
                                    continue block9;
                                }
                            }
                        } else if (linkcolumnid.length() > 0 || currentnodeindex == 0) {
                            int c;
                            AdhocQueryRequest adhocRequest = this.buildNavigatorAdhocRequest(sdcid, keycolid1, keyid1);
                            for (int i = 0; i <= currentnodeindex; ++i) {
                                String prefix = "";
                                if (i < currentnodeindex) {
                                    for (int tn = currentnodeindex; tn > i; --tn) {
                                        String parentlinkid = treenodes.getPropertyList(tn).getProperty("parentlinkid");
                                        prefix = prefix + (prefix.length() > 0 ? "." : "") + parentlinkid;
                                    }
                                }
                                PropertyList tempNode = treenodes.getPropertyList(i);
                                PropertyListCollection currentColumns = tempNode.getCollection("columns");
                                String tempsdcid = tempNode.getProperty("sdcid");
                                if (currentColumns == null) continue;
                                for (c = 0; c < currentColumns.size(); ++c) {
                                    String vcolid = (prefix.length() > 0 ? prefix + "." : "") + currentColumns.getPropertyList(c).getProperty("columnid");
                                    adhocRequest.addViewArg(vcolid);
                                    adhocRequest.addOrderbyArg(vcolid);
                                    sortby = sortby + "," + vcolid;
                                    contextSdcids = contextSdcids + "," + tempsdcid;
                                }
                            }
                            if (!adhocRequest.containViewArg(keycolid1)) {
                                adhocRequest.addViewArg(keycolid1);
                                sortby = sortby + "," + keycolid1;
                                contextSdcids = contextSdcids + "," + sdcid;
                                adhocRequest.addOrderbyArg(keycolid1);
                            }
                            if (!adhocRequest.containViewArg(desccolid)) {
                                adhocRequest.addViewArg(desccolid);
                                contextSdcids = contextSdcids + "," + sdcid;
                            }
                            HashMap<String, Object> props = new HashMap<String, Object>();
                            int maxResults = 500;
                            adhocRequest.setMaxResults(maxResults);
                            props.put("mode", "adhocrequest");
                            props.put("adhocrequest", adhocRequest);
                            try {
                                HashMap returnProps = requestProcessor.processRequest(AdhocQueryPropertyHandler.class.getName(), props);
                                contextDataSet = (DataSet)returnProps.get("dataset");
                                String[] cols = contextDataSet.getColumns();
                                DataSet copyds = new DataSet(connectionInfo);
                                for (c = 0; c < cols.length; ++c) {
                                    String colid = cols[c];
                                    if (colid.indexOf(".") > 0) {
                                        colid = colid.substring(colid.lastIndexOf(".") + 1);
                                    }
                                    copyds.addColumnValues(colid, contextDataSet.getColumnType(cols[c]), contextDataSet.getColumnValues(cols[c], ";"), ";");
                                }
                                contextDataSet = copyds;
                                String errormessage = (String)returnProps.get("errormessage");
                                if (errormessage != null && errormessage.length() > 0) {
                                    throw new Exception(errormessage);
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (sortby.length() > 0) {
                        contextDataSet.sort(sortby.substring(1));
                    }
                    if (contextDataSet != null && contextDataSet.getRowCount() > this.maxRows) {
                        throw new Exception("Maximum " + this.maxRows + " records exceeded.");
                    }
                    jsonResponseObj.put("contextdata", JSONUtil.toJSONObject(contextDataSet));
                    if (hidetoolbarlist.length() > 0) {
                        jsonResponseObj.put("hidetoolbarlist", hidetoolbarlist.substring(1));
                    }
                    if (contextSdcids.length() > 0) {
                        jsonResponseObj.put("contextsdcids", contextSdcids.substring(1));
                    } else {
                        HashMap<String, String> columnidNodeidMap = new HashMap<String, String>();
                        for (int i = 0; i < treenodes.size(); ++i) {
                            String nodeid = treenodes.getPropertyList(i).getProperty("nodeid");
                            PropertyListCollection columns = treenodes.getPropertyList(i).getCollection("columns");
                            if (columns == null) continue;
                            for (int c = 0; c < columns.size(); ++c) {
                                String columnid = columns.getPropertyList(c).getProperty("columnid");
                                if (columnid.length() <= 0 || !contextDataSet.isValidColumn(columnid)) continue;
                                columnidNodeidMap.put(columnid, nodeid);
                            }
                        }
                        StringBuffer contextnodeids = new StringBuffer();
                        String[] columnids = contextDataSet.getColumns();
                        for (int c = 0; c < columnids.length; ++c) {
                            contextnodeids.append("," + (columnidNodeidMap.get(columnids[c]) == null ? "" : (String)columnidNodeidMap.get(columnids[c])));
                        }
                        jsonResponseObj.put("contextsdcids", contextnodeids.substring(1));
                    }
                    Trace.log("Context data retrieved in " + (System.currentTimeMillis() - starttime) + "ms");
                    if (sdiRequest != null && sdidata == null) {
                        sdiRequest.setKeyid1List(keyid1);
                        sdiRequest.setKeyid2List(keyid2 != null ? keyid2 : "");
                        sdiRequest.setKeyid3List(keyid3 != null ? keyid3 : "");
                        sdidata = sdiProcessor.getSDIData(sdiRequest);
                        Trace.log("Entry Primary " + sdcid + " Retrieved in " + (System.currentTimeMillis() - starttime) + "ms");
                    }
                }
                if (currentNode != null && currentNode.getCollection("operations") != null && (addnewoperationPL = this.getAddNewNodeOperation(currentNode)) != null) {
                    int newrow = contextDataSet.addRow();
                    contextDataSet.setValue(newrow, keycolid1, NEW_NODE_PREFIX + addnewoperationPL.getProperty("name"));
                    jsonResponseObj.put("contextdata", JSONUtil.toJSONObject(contextDataSet));
                }
                this.addEntrySDIDataWithDetails(jsonResponseObj, sdidata);
                Trace.log("Navigator Intial Time:" + (System.currentTimeMillis() - starttime) + "ms");
            }
            catch (Exception e) {
                Trace.logError("Error contruct navigator response", e);
                throw new Exception(e.getMessage());
            }
        }
        return jsonResponseObj;
    }

    private PropertyList getAddNewNodeOperation(PropertyList currentNode) {
        if (currentNode != null && currentNode.getCollection("operations") != null) {
            PropertyListCollection operations = currentNode.getCollection("operations");
            for (int i = 0; i < operations.size(); ++i) {
                if (!"Y".equals(operations.getPropertyList(i).getProperty("isaddnodeoperation"))) continue;
                return operations.getPropertyList(i);
            }
        }
        return null;
    }

    private PropertyList evaluationNodeOperationShowGroovy(ConnectionInfo connectionInfo, PropertyList pl, HashMap bindMap) throws Exception {
        PropertyListCollection operations = pl.getCollection("operations");
        String sdcid = pl.getProperty("sdcid");
        ArrayList<PropertyList> removeList = new ArrayList<PropertyList>();
        for (int i = 0; i < operations.size(); ++i) {
            PropertyList operation = operations.getPropertyList(i);
            if (operation.getProperty("show").indexOf("$G") != 0) continue;
            String groovyscript = operation.getProperty("show");
            String show = GroovyUtil.getInstance(connectionInfo).evaluateSecure(groovyscript, bindMap);
            if ("false".equals(show)) {
                show = "N";
            } else if ("true".equals(show)) {
                show = "Y";
            }
            if ("N".equals(show)) {
                removeList.add(operation);
                continue;
            }
            operation.setProperty("show", show);
        }
        if (removeList.size() > 0) {
            operations.removeAll(removeList);
        }
        return pl;
    }

    private SDIData getSDIData(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList pl) {
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = null;
        if (pl != null) {
            sdiRequest = this.buildSDIRequest(pl);
        } else {
            sdiRequest = new SDIRequest();
            sdiRequest.setRequestItem("primary");
        }
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setKeyid1List(keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            sdiRequest.setKeyid2List(keyid2);
            if (keyid3 != null && keyid3.length() > 0) {
                sdiRequest.setKeyid3List(keyid3);
            }
        }
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        return sdiData;
    }

    private HashMap getBindMap(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList pl) {
        return this.getBindMap(sdcid, keyid1, keyid2, keyid3, pl, null);
    }

    private HashMap getBindMap(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList pl, SDIData sdiData) {
        if (sdiData == null) {
            sdiData = this.getSDIData(sdcid, keyid1, keyid2, keyid3, pl);
        }
        HashMap<String, DataSet> bindMap = new HashMap<String, DataSet>();
        DataSet primaryDataset = sdiData.getDataset("primary");
        bindMap.put("primarydataset", primaryDataset);
        if (primaryDataset.getRowCount() == 1) {
            bindMap.put("primary", (DataSet)primaryDataset.get(0));
        } else if (primaryDataset.getRowCount() > 1) {
            int row;
            String[] keycolids = sdiData.getKeys("primary");
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put(keycolids[0], keyid1);
            if (keycolids.length > 1 && keycolids[1].length() > 0) {
                findMap.put(keycolids[1], keyid2);
            }
            if (keycolids.length > 2 && keycolids[2].length() > 3) {
                findMap.put(keycolids[2], keyid3);
            }
            if ((row = primaryDataset.findRow(findMap)) >= 0) {
                bindMap.put("primary", (DataSet)primaryDataset.get(row));
            }
        }
        return bindMap;
    }

    private int getCurrentNodeIndex(String sdcid, PropertyListCollection treenodes) {
        for (int i = 0; i < treenodes.size(); ++i) {
            PropertyList tempPropertyList = treenodes.getPropertyList(i);
            String tempsdcid = tempPropertyList.getProperty("sdcid");
            if (!sdcid.equals(tempsdcid)) continue;
            return i;
        }
        return treenodes.size() - 1;
    }

    private void addEntrySDIDataWithDetails(JSONObject jsonResponseObj, SDIData sdidata) throws Exception {
        if (sdidata != null) {
            JSONObject sdidataObject = new JSONObject();
            HashMap<String, String> filterColumnidMap = new HashMap<String, String>();
            HashMap<String, String[]> columnToReturnMap = new HashMap<String, String[]>();
            String[] primarykeys = sdidata.getKeys("primary");
            NavigatorRequest.addSDIData(sdidataObject, sdidata, primarykeys[0], filterColumnidMap, columnToReturnMap);
            Trace.log("Primary sdidata added:" + new Date(System.currentTimeMillis()));
            jsonResponseObj.put("sdidata", sdidataObject);
        } else {
            Trace.log("No primary sdidata retrieved:" + new Date(System.currentTimeMillis()));
            jsonResponseObj.put("sdidata", new JSONObject());
        }
    }

    private SDIRequest buildSDIRequest(PropertyList currentNode) {
        SDIRequest sdiRequest = new SDIRequest();
        String sdcid = currentNode.getProperty("sdcid");
        StringBuffer columnsToRetrieve = new StringBuffer();
        if (currentNode.getCollection("columns") != null) {
            PropertyListCollection columns = currentNode.getCollection("columns");
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            for (int c = 0; c < columns.size(); ++c) {
                String requestcolumn = columns.getPropertyList(c).getProperty("columnid");
                if (requestcolumn.length() <= 0) continue;
                if (requestcolumn != null && (requestcolumn.indexOf("[keycolid1]") >= 0 || requestcolumn.indexOf("[primarytable]") >= 0) || requestcolumn.indexOf("[sdcid]") >= 0) {
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[sdcid]", sdcid);
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[keycolid1]", sdcProcessor.getProperty(sdcid, "keycolid1"));
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[keycolid2]", sdcProcessor.getProperty(sdcid, "keycolid2"));
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[keycolid3]", sdcProcessor.getProperty(sdcid, "keycolid3"));
                    requestcolumn = StringUtil.replaceAll(requestcolumn, "[primarytable]", sdcProcessor.getProperty(sdcid, "tableid"));
                }
                columnsToRetrieve.append((c > 0 ? "," : "") + requestcolumn);
            }
        }
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setRequestItem("primary" + (columnsToRetrieve.length() > 0 ? "[" + columnsToRetrieve + "]" : ""));
        return sdiRequest;
    }

    private String getConnectByOracle(String tableid, String keycolid1, String linkcolumnid, String keyid1, int rowlimit) {
        if (keyid1.indexOf(";") > 0) {
            keyid1 = SafeSQL.convertToSQLInClause(keyid1, ";", true);
        }
        return keycolid1 + " in (\nselect " + keycolid1 + " from " + tableid + " connect by prior " + linkcolumnid + "=" + keycolid1 + " start with \n" + keycolid1 + " in ('" + keyid1 + "') )";
    }

    private String getConnectByMSS(String tableid, String keycolid1, String linkcolumnid, String keyid1, int rowlimit) {
        if (keyid1.indexOf(";") > 0) {
            keyid1 = SafeSQL.convertToSQLInClause(keyid1, ";", false);
        }
        String temptableid = tableid + "table";
        return " WITH " + temptableid + " (" + keycolid1 + ", " + linkcolumnid + ", Level ) AS ( SELECT su." + keycolid1 + ", su." + linkcolumnid + ", 0 AS Level FROM " + tableid + " AS su WHERE su." + keycolid1 + " in ('" + keyid1 + "') UNION ALL SELECT su." + keycolid1 + ", su." + linkcolumnid + ", Level + 1 FROM " + tableid + " AS su INNER JOIN " + temptableid + " AS d ON su." + keycolid1 + " = d." + linkcolumnid + ") SELECT " + keycolid1 + " FROM " + temptableid + " WHERE Level >= 0";
    }

    private static PropertyList buildNavigatorPropertyList(String sdcid, String defaultpageid, WebAdminProcessor webAdminProcessor) {
        PropertyList rootpl = new PropertyList();
        rootpl.setProperty("width", "200");
        SDCProcessor sdcProcessor = new SDCProcessor(webAdminProcessor.getConnectionid());
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        PropertyListCollection treenodes = new PropertyListCollection();
        PropertyList sdctreenode = new PropertyList();
        sdctreenode.setProperty("keycolid1", keycolid1);
        sdctreenode.setProperty("keycolid2", keycolid2);
        sdctreenode.setProperty("keycolid3", keycolid3);
        sdctreenode.setProperty("nodeid", sdcid);
        treenodes.add(sdctreenode);
        rootpl.setProperty(PROPERTY_TREE_NODES, treenodes);
        PropertyListCollection refnodes = new PropertyListCollection();
        PropertyList refnode = new PropertyList();
        refnode.setProperty("nodeid", sdcid);
        refnode.setProperty("sdcid", sdcid);
        refnodes.add(refnode);
        rootpl.setProperty("refnodes", refnodes);
        PropertyListCollection columns = new PropertyListCollection();
        PropertyList column = new PropertyList();
        column.setProperty("columnid", keycolid1);
        columns.add(column);
        refnode.setProperty("columns", columns);
        if (keycolid2.length() > 0) {
            column = new PropertyList();
            column.setProperty("columnid", keycolid2);
            columns.add(column);
            refnode.setProperty("label", "[" + keycolid1 + "]([" + keycolid2 + "])");
            if (keycolid3.length() > 0) {
                column = new PropertyList();
                column.setProperty("columnid", keycolid3);
                refnode.setProperty("label", "[" + keycolid1 + "]([" + keycolid2 + "], [" + keycolid3 + "])");
                columns.add(column);
            }
        }
        PropertyListCollection operations = new PropertyListCollection();
        PropertyList operation = new PropertyList();
        operation.setProperty("name", "Edit");
        operation.setProperty("image", "WEB-CORE/images/gif/Edit.gif");
        PropertyList url = new PropertyList();
        if (defaultpageid == null) {
            defaultpageid = sdcid + "Maint";
        }
        if (defaultpageid.indexOf("[sdcid]") >= 0) {
            defaultpageid = StringUtil.replaceAll(defaultpageid, "[sdcid]", sdcid);
        }
        url.setProperty("href", "rc?command=page&page=" + defaultpageid);
        TranslationProcessor tp = new TranslationProcessor(webAdminProcessor.getConnectionid());
        url.setProperty("tip", tp.translate("Edit"));
        operation.setProperty("url", url);
        operation.setProperty("applytoset", "Y");
        operations.add(operation);
        refnode.setProperty("operations", operations);
        sdctreenode.putAll(refnode);
        return rootpl;
    }

    public static PropertyList getEditSetPropertyList(ConnectionInfo connectionInfo, String navigatornodeid, String sdcid, String defaultpageid, ConfigurationProcessor configProcessor, WebAdminProcessor webamin) throws Exception {
        PropertyList pl = null;
        pl = navigatornodeid == null || navigatornodeid.length() == 0 ? NavigatorRequest.buildNavigatorPropertyList(sdcid, defaultpageid, webamin) : NavigatorRequest.getNavigatorPolicy(connectionInfo, navigatornodeid, sdcid, configProcessor, webamin, defaultpageid);
        return pl;
    }

    public static PropertyList getNavigatorPolicy(ConnectionInfo connectionInfo, String navigatornodeid, String sdcid, ConfigurationProcessor configProcessor, WebAdminProcessor webamin) throws Exception {
        return NavigatorRequest.getNavigatorPolicy(connectionInfo, navigatornodeid, sdcid, configProcessor, webamin, null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static PropertyList getNavigatorPolicy(ConnectionInfo connectionInfo, String navigatornodeid, String sdcid, ConfigurationProcessor configProcessor, WebAdminProcessor webamin, String defaultpageid) throws Exception {
        boolean isEditSet;
        if (navigatornodeid == null || navigatornodeid.length() == 0) {
            navigatornodeid = NavigatorRequest.getDefaultPolicyId(sdcid, webamin);
        }
        if (navigatornodeid == null || navigatornodeid.length() == 0) {
            throw new Exception("Cannot find appropriate Navigator Policy Node for " + sdcid);
        }
        PropertyList element = configProcessor.getPolicy(POLICY_ID, navigatornodeid + " Custom");
        if (element == null || element.size() == 0) {
            element = configProcessor.getPolicy(POLICY_ID, navigatornodeid);
        }
        if (element == null || element.size() <= 0) throw new Exception("Cannot retrieve NavigatorPolicy using nodeid:" + navigatornodeid + " or sdcid:" + sdcid);
        PropertyListCollection treenodes = element.getCollection(PROPERTY_TREE_NODES);
        if (treenodes != null && treenodes.size() > 0) {
            ArrayList<PropertyList> toRemoveList = new ArrayList<PropertyList>();
            for (int i = 0; i < treenodes.size(); ++i) {
                if (treenodes.getPropertyList(i).getProperty("nodeid").length() != 0) continue;
                toRemoveList.add(treenodes.getPropertyList(i));
            }
            treenodes.removeAll(toRemoveList);
        }
        if ((isEditSet = "Y".equals(element.getProperty("iseditset"))) || treenodes == null || treenodes.size() == 0) {
            treenodes = new PropertyListCollection();
            PropertyList tnode = new PropertyList();
            tnode.setProperty("nodeid", sdcid);
            treenodes.add(tnode);
            element.setProperty(PROPERTY_TREE_NODES, treenodes);
        }
        String connectionid = configProcessor.getConnectionid();
        SDCProcessor sdcProcessor = new SDCProcessor(connectionid);
        if (treenodes == null) throw new Exception("No tree nodes define at NavigatorPolicy node: " + navigatornodeid);
        for (int i = 0; i < treenodes.size(); ++i) {
            String nodesdcid;
            PropertyList nodePL = treenodes.getPropertyList(i);
            NavigatorRequest.findAndMergeRefNodePL(nodePL, element, 0);
            PropertyListCollection operations = nodePL.getCollection("operations");
            HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
            bindMap.put("user", new ConnectionProcessor(connectionid).getConnectionInfo(connectionid).getUserAttributeMap());
            PropertyList sdcProps = null;
            if (sdcid != null && sdcid.length() > 0) {
                sdcProps = sdcProcessor.getPropertyList(sdcid);
                bindMap.put("sdc", sdcProps);
            }
            if (operations != null) {
                for (int p = 0; p < operations.size(); ++p) {
                    PropertyList urlPL;
                    PropertyList operationPL = operations.getPropertyList(p);
                    if (defaultpageid != null && defaultpageid.length() > 0 && (urlPL = operationPL.getPropertyList("url")) != null && urlPL.getProperty("href").trim().length() == 0) {
                        urlPL.setProperty("href", "rc?command=page&page=" + defaultpageid + "&mode=Edit");
                    }
                    if (operationPL.getProperty("applytoset").indexOf("$G{") != 0) continue;
                    String v = GroovyUtil.getInstance(connectionInfo).evaluateSecure(operationPL.getProperty("applytoset"), bindMap);
                    v = "true".equals(v) ? "Y" : ("false".equals(v) ? "N" : v);
                    operationPL.setProperty("applytoset", v);
                }
            }
            if ((nodesdcid = nodePL.getProperty("sdcid")).length() <= 0) continue;
            String keycolid1 = sdcProcessor.getProperty(nodesdcid, "keycolid1");
            String keycolid2 = sdcProcessor.getProperty(nodesdcid, "keycolid2");
            String keycolid3 = sdcProcessor.getProperty(nodesdcid, "keycolid3");
            nodePL.setProperty("keycolid1", keycolid1);
            nodePL.setProperty("keycolid2", keycolid2);
            nodePL.setProperty("keycolid3", keycolid3);
            PropertyListCollection columns = nodePL.getCollection("columns");
            if (columns == null || columns.size() == 0) {
                columns = new PropertyListCollection();
                PropertyList column = new PropertyList();
                column.setProperty("columnid", keycolid1);
                columns.add(column);
                if (keycolid2.length() > 0) {
                    column = new PropertyList();
                    column.setProperty("columnid", keycolid2);
                    columns.add(column);
                    if (keycolid3.length() > 0) {
                        column = new PropertyList();
                        column.setProperty("columnid", keycolid2);
                        columns.add(column);
                    }
                    if (keycolid2.indexOf("versionid") >= 0) {
                        column = new PropertyList();
                        column.setProperty("columnid", "versionstatus");
                        columns.add(column);
                    }
                }
                nodePL.setProperty("columns", columns);
            } else {
                ElementUtil.setColumnDisplayValue(nodePL.getCollection("columns"), sdcProps, new TranslationProcessor(connectionid), new QueryProcessor(connectionid), true);
            }
            String nodelabel = nodePL.getProperty("label");
            if (nodelabel.length() <= 0 || nodelabel.indexOf("[") < 0) continue;
            nodelabel = StringUtil.replaceAll(nodelabel, "[sdcid]", nodesdcid);
            nodelabel = nodelabel.replaceAll("keycolid1", keycolid1);
            nodelabel = nodelabel.replaceAll("keycolid2", keycolid2);
            nodelabel = nodelabel.replaceAll("keycolid3", keycolid3);
            nodelabel = nodelabel.replaceAll("desccol", sdcProcessor.getProperty(nodesdcid, "desccol"));
            nodePL.setProperty("label", nodelabel);
        }
        return element;
    }

    private static void findAndMergeRefNodePL(PropertyList nodePL, PropertyList element, int level) throws Exception {
        PropertyListCollection refnodes = element.getCollection("refnodes");
        String nodeid = nodePL.getProperty("nodeid");
        if (nodeid.length() != 0) {
            PropertyList refNodePL = null;
            PropertyList anySDCPL = null;
            for (int r = 0; r < refnodes.size(); ++r) {
                if (nodeid.equals(refnodes.getPropertyList(r).getProperty("nodeid"))) {
                    refNodePL = refnodes.getPropertyList(r).copy();
                    break;
                }
                if (!"".equals(refnodes.getPropertyList(r).getProperty("sdcid"))) continue;
                anySDCPL = refnodes.getPropertyList(r).copy();
                anySDCPL.setProperty("sdcid", nodeid);
            }
            if (refNodePL == null) {
                refNodePL = anySDCPL;
            }
            if (refNodePL == null) {
                throw new Exception("Cannot find reference node " + nodeid);
            }
            NavigatorRequest.removeInactive(refNodePL.getCollection("operations"));
            NavigatorRequest.removeInactive(refNodePL.getCollection(PROPERTY_CHILD_NODES));
            refNodePL.putAll(nodePL);
            nodePL.putAll(refNodePL);
        }
        if (++level > 4) {
            return;
        }
        if (nodePL.getCollection(PROPERTY_CHILD_NODES) != null) {
            PropertyListCollection childNodes = nodePL.getCollection(PROPERTY_CHILD_NODES);
            for (int c = 0; c < childNodes.size(); ++c) {
                PropertyList childPL = childNodes.getPropertyList(c);
                NavigatorRequest.findAndMergeRefNodePL(childPL, element, level);
            }
        }
    }

    private static String getDefaultPolicyId(String sdcid, WebAdminProcessor wap) throws Exception {
        PropertyTree navigatorPolicy = wap.getPropertyTree(POLICY_ID);
        ArrayList nodelist = navigatorPolicy.getAllNodes();
        for (int i = 0; i < nodelist.size(); ++i) {
            Node node = (Node)nodelist.get(i);
            PropertyList nodePL = node.getPropertyList();
            PropertyListCollection treenodes = nodePL.getCollection(PROPERTY_TREE_NODES);
            if (treenodes == null) continue;
            for (int n = 0; n < treenodes.size(); ++n) {
                if (!treenodes.getPropertyList(n).getProperty("nodeid").equals(sdcid)) continue;
                String nodeid = ((Node)nodelist.get(i)).getNodeId();
                if (nodeid.indexOf(" Product") > 0) {
                    nodeid = StringUtil.replaceAll(nodeid, " Product", " Custom");
                }
                return nodeid;
            }
        }
        return null;
    }

    private static void removeInactive(PropertyListCollection nodes) {
        if (nodes != null) {
            HashSet<PropertyList> inactiveNodes = new HashSet<PropertyList>();
            for (int i = 0; i < nodes.size(); ++i) {
                if (!"N".equals(nodes.getPropertyList(i).getProperty("show"))) continue;
                inactiveNodes.add(nodes.getPropertyList(i));
            }
            nodes.removeAll(inactiveNodes);
        }
    }

    private AdhocQueryRequest buildNavigatorAdhocRequest(String sdcid, String keycolid1, String keyid1) {
        keyid1 = StringUtil.replaceAll(keyid1, ";", "|");
        AdhocQueryRequest adhocRequest = new AdhocQueryRequest();
        adhocRequest.setSdcid(sdcid);
        AdhocCriteria adhocGriteria = new AdhocCriteria();
        AdhocCriteriaArgGroup argGroup = new AdhocCriteriaArgGroup();
        AdhocCriteriaArg arg = new AdhocCriteriaArg();
        arg.setColumnid(keycolid1);
        arg.setOperator("in");
        arg.setValueObject(keyid1);
        argGroup.addCriteriaArg(arg);
        adhocGriteria.addCriteriaArgCroup(argGroup);
        adhocRequest.setCriteria(adhocGriteria);
        return adhocRequest;
    }

    private static void addSDIData(JSONObject jsonObject, SDIData sdidata, String keycolid1, HashMap<String, String> filterColumnMap, HashMap<String, String[]> columnToReturnMap) {
        try {
            String keyid1List = sdidata.getDataset("primary").getColumnValues(keycolid1, ";");
            String[] keyid1s = StringUtil.split(keyid1List, ";");
            for (int i = 0; i < keyid1s.length; ++i) {
                JSONObject sdidatajsonObj = new JSONObject();
                String keyid1 = keyid1s[i];
                Set ds = sdidata.getDatasets();
                Iterator it = ds.iterator();
                HashMap<String, String> filterMap = new HashMap<String, String>();
                while (it.hasNext()) {
                    String name = it.next().toString();
                    DataSet dataset = sdidata.getDataset(name);
                    filterMap.clear();
                    String[] columnsToReturn = (String[])datasetReturnColumnsMap.get(name);
                    if (filterColumnMap.get(name) != null) {
                        filterMap.put(filterColumnMap.get(name), keyid1);
                        columnsToReturn = columnToReturnMap.get(name);
                    } else if ("trackitem".equals(name)) {
                        filterMap.put("linkkeyid1", keyid1);
                    } else {
                        filterMap.put("keyid1", keyid1);
                    }
                    if ("primary".equals(name)) continue;
                    dataset = dataset.getFilteredDataSet(filterMap);
                    sdidatajsonObj.put(name, JSONUtil.toJSONObject(dataset, columnsToReturn, false));
                }
                jsonObject.put(keyid1, sdidatajsonObj);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String replaceCurrentUser(String sql) {
        if (sql.indexOf("[currentuser]") >= 0) {
            sql = StringUtil.replaceAll(sql, "[currentuser]", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId());
        }
        return sql;
    }

    private String replaceCurrentUser(String sql, SafeSQL safeSQL) {
        if (sql.indexOf("[currentuser]") >= 0) {
            sql = SafeSQL.replaceAllWithVars(sql, "'[currentuser]'", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId(), safeSQL);
        }
        return sql;
    }

    static {
        for (int i = 0; i < _datasetnames.length; ++i) {
            datasetReturnColumnsMap.put(_datasetnames[i], defaultDataSetColumns[i]);
        }
        datasetReturnColumnsMap.put("commands", new String[]{"commandid"});
    }
}


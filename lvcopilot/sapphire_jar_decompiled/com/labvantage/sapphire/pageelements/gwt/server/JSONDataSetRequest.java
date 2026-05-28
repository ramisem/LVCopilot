/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.admin.system.SQLRegister;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.json.JSONUtil;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class JSONDataSetRequest
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String connectionid = requestContext.getConnectionId();
            ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
            boolean isOracle = "ORA".equals(connectionInfo.getDbms());
            String sysuserid = connectionInfo.getSysuserId();
            String reftypeid = request.getParameter("reftypeid");
            String sdcid = request.getParameter("sdcid");
            String queryfrom = request.getParameter("queryfrom");
            String instrumentid = request.getParameter("instrumentid");
            String dataitemkey = request.getParameter("dataitemkey");
            boolean isKeyRequest = "Y".equals(request.getParameter("keyrequest"));
            String sql = request.getParameter("sql");
            String sqlcode = request.getParameter("sqlcode");
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            String dynamicsqlcode = ajaxResponse.getRequestParameter("dynamicsqlcode");
            String categoryid = request.getParameter("categoryid");
            JSONObject jsonRequestObj = new JSONObject();
            jsonRequestObj.put("reftypeid", request.getParameter("reftypeid"));
            jsonRequestObj.put("sdcid", request.getParameter("sdcid"));
            boolean usedatatypes = "Y".equals(request.getParameter("usedatatypes"));
            String querySdcId = request.getParameter("querySdcId");
            String querywhere = request.getParameter("querywhere");
            if (OpalUtil.isNotEmpty(request.getParameter("linksdcid")) && request.getParameter("linksdcid").equalsIgnoreCase("AdhocQuery")) {
                querywhere = "basedonsdcid='" + querySdcId + "' AND ( createby='[currentuser]' or shareableflag in ('Y','L') )";
            }
            String queryorderby = request.getParameter("queryorderby");
            if (querywhere != null && querywhere.indexOf("[currentuser]") >= 0) {
                querywhere = StringUtil.replaceAll(querywhere, "[currentuser]", sysuserid);
            }
            if (sql == null && querywhere != null && querywhere.toUpperCase().indexOf("SELECT") == 0) {
                sql = querywhere;
            }
            DataSet ds = null;
            if (reftypeid != null && reftypeid.length() > 0) {
                ds = new QueryProcessor(connectionid).getRefTypeDataSet(reftypeid);
                DataSet reftypeDs = new DataSet();
                reftypeDs.addColumn("refvalueid", 0);
                reftypeDs.addColumn("refdisplayvalue", 0);
                TranslationProcessor tp = null;
                if (connectionInfo.getLanguage() != null && connectionInfo.getLanguage().length() > 0) {
                    tp = new TranslationProcessor(connectionid);
                    tp.setTextType(reftypeid);
                    reftypeDs.addColumn("translatedvalue", 0);
                }
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    int row = reftypeDs.addRow();
                    reftypeDs.setValue(row, "refvalueid", ds.getValue(i, "refvalueid"));
                    reftypeDs.setValue(row, "refdisplayvalue", ds.getValue(i, "refdisplayvalue").length() == 0 ? ds.getValue(i, "refvalueid") : ds.getValue(i, "refdisplayvalue"));
                    reftypeDs.setValue(i, "translatedvalue", tp != null ? tp.translate(reftypeDs.getValue(i, "refdisplayvalue")) : reftypeDs.getValue(i, "refdisplayvalue"));
                }
                ds = reftypeDs;
            } else if (sql != null && sql.length() > 0) {
                if ("GET".equals(request.getMethod())) {
                    throw new Exception("Invalid Request");
                }
                if (sql.indexOf("{|}") == 0 || sql.indexOf("{@}") == 0) {
                    sql = EncryptDecrypt.unobfsql(sql);
                    sql = StringUtil.replaceAll(sql, "[currentuser]", sysuserid);
                    sql = StringUtil.replaceAll(sql, "[%currentuser%]", sysuserid);
                    ds = new QueryProcessor(connectionid).getSqlDataSet(sql);
                } else {
                    if (!SecurityPolicyUtil.isUnregisteredSQLPermitted(this.getConnectionid(), "ajax", "getSQLDataSet", sql)) throw new Exception("Failed to perform ajax request to execute SQL:" + sql + ". Reason: Allow unregistered SQL is set to No in security policy");
                    sql = StringUtil.replaceAll(sql, "[currentuser]", sysuserid);
                    sql = StringUtil.replaceAll(sql, "[%currentuser%]", sysuserid);
                    ds = new QueryProcessor(connectionid).getSqlDataSet(sql);
                }
            } else if (sqlcode != null && sqlcode.length() > 0) {
                QueryProcessor queryProcessor = this.getQueryProcessor();
                ds = ajaxResponse.getRegisteredSQLDataSet(queryProcessor, HttpUtil.getRequestMap((ServletRequest)request));
                if (sqlcode.equals("20000")) {
                    RequestProcessor requestProcessor;
                    PropertyList pagedata;
                    PropertyListCollection searchablesdcs;
                    ds.addColumn("labeltrans", 0);
                    TranslationProcessor tp = new TranslationProcessor(connectionid);
                    String adhocquerypageid = request.getParameter("adhocquerypageid");
                    HashMap<String, String> titlemap = new HashMap<String, String>();
                    if (adhocquerypageid != null && adhocquerypageid.length() > 0 && (searchablesdcs = (pagedata = (requestProcessor = new RequestProcessor(connectionid)).getWebPageProperties(adhocquerypageid, (RequestContext)request.getAttribute("RequestContext"))).getPropertyListNotNull("pagedata").getCollection("searchablesdcs")) != null) {
                        for (int s = 0; s < searchablesdcs.size(); ++s) {
                            PropertyListCollection columns;
                            if (searchablesdcs.getPropertyList(s) == null || (columns = searchablesdcs.getPropertyList(s).getCollection("searchablecolumns")) == null) continue;
                            for (int c = 0; c < columns.size(); ++c) {
                                if (columns.getPropertyList(c) == null || columns.getPropertyList(c).getProperty("title").length() <= 0) continue;
                                titlemap.put(columns.getPropertyList(c).getProperty("columnid"), columns.getPropertyList(c).getProperty("title"));
                            }
                        }
                    }
                    for (int i = 0; i < ds.getRowCount(); ++i) {
                        String columnid = ds.getValue(i, "columnid");
                        if (titlemap.get(columnid) != null) {
                            ds.setValue(i, "labeltrans", (String)titlemap.get(columnid));
                            continue;
                        }
                        ds.setValue(i, "labeltrans", tp.translate(ds.getValue(i, "label")));
                    }
                }
            } else if (sdcid != null && sdcid.length() > 0 || queryfrom != null && queryfrom.length() > 0) {
                boolean isSingleKeyRequest = "Y".equals(request.getParameter("singlekeyrequest"));
                boolean isActionPropertyRequest = "Y".equals(request.getParameter("isActionPropertyRequest"));
                SDCProcessor sdcProcessor = new SDCProcessor(connectionid);
                String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                String desccol = sdcProcessor.getProperty(sdcid, "desccol");
                String qFrom = "";
                String qWhere = "";
                String qOrderBy = "";
                ArrayList<String> requestItemList = new ArrayList<String>();
                if (categoryid != null && categoryid.length() > 0 && categoryid.indexOf("(All") != 0) {
                    qFrom = tableid + ",categoryitem";
                    qWhere = tableid + "." + keycolid1 + "=categoryitem.keyid1 and categoryid in ('" + StringUtil.replaceAll(categoryid, ";", "','") + "')";
                    request.getSession().setAttribute(sdcid + "_lastcategoryid", (Object)categoryid);
                } else {
                    qFrom = tableid;
                    qWhere = querywhere == null ? "" : querywhere;
                    String string = qOrderBy = queryorderby == null ? keycolid1 : queryorderby;
                }
                if ("Action".equals(sdcid)) {
                    if (isActionPropertyRequest) {
                        requestItemList.add("actionproperty");
                    }
                    requestItemList.add("primary[actionid]");
                    qOrderBy = "actionid";
                    qWhere = qWhere.length() > 0 ? "((" + qWhere + ") AND action.clientflag='N')" : "action.clientflag='N'";
                } else if ("AdhocQuery".equals(sdcid)) {
                    requestItemList.add("primary[adhocqueryid, adhocquerydesc, shareableflag]");
                } else if ("Instrument".equals(sdcid)) {
                    qFrom = "instrument";
                    qWhere = JSONDataSetRequest.getInstrumentCertWhereClause(qWhere, isOracle);
                    if (dataitemkey != null && dataitemkey.length() > 0) {
                        String datasetTestingDept;
                        String[] itemkeys = StringUtil.split(dataitemkey, ";");
                        if ("Sample".equals(itemkeys[0])) {
                            qWhere = "((" + qWhere + ") OR ( instrumentid IN ( SELECT s.instrumentid from s_sample s WHERE s.s_sampleid='" + itemkeys[1] + "' AND s.classification='Certification' )))";
                        }
                        if ((datasetTestingDept = this.getQueryProcessor().getPreparedSqlDataSet("SELECT testingdepartmentid FROM sdidata WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? AND paramlistid=? AND paramlistversionid=? AND variantid=? AND dataset=?", new Object[]{itemkeys[0], itemkeys[1], itemkeys[2], itemkeys[3], itemkeys[4], itemkeys[5], itemkeys[6], itemkeys[7]}).getValue(0, "testingdepartmentid")).length() > 0) {
                            qWhere = qWhere + " AND ( instrument.testingdepartmentid is null OR instrument.testingdepartmentid='' OR instrument.testingdepartmentid='" + datasetTestingDept + "' )";
                        }
                    }
                    requestItemList.add("primary[" + keycolid1 + "," + desccol + "]");
                } else if (isSingleKeyRequest) {
                    requestItemList.add("primary[" + keycolid1 + "]");
                } else if (isKeyRequest) {
                    requestItemList.add("primary[" + keycolid1 + "," + keycolid2 + "," + keycolid3 + "]");
                } else {
                    String requestcolumn = request.getParameter("requestcolumn");
                    requestItemList.add(requestcolumn == null ? "primary" : "primary[" + requestcolumn + "]");
                }
                SDIData sdidata = JSONDataSetRequest.getSDIData(connectionid, sdcid, qFrom, qWhere, qOrderBy, requestItemList);
                if (isActionPropertyRequest) {
                    ds = sdidata.getDataset("actionproperty");
                    if (connectionInfo.getLanguage() != null && connectionInfo.getLanguage().length() > 0) {
                        TranslationProcessor tp = new TranslationProcessor(connectionid);
                        tp.setTextType("Action");
                        for (int i = 0; i < ds.getRowCount(); ++i) {
                            ds.setValue(i, "propertytitle", tp.translate(ds.getValue(i, "propertytitle")));
                        }
                    }
                } else if ("Instrument".equals(sdcid)) {
                    ds = sdidata.getDataset("primary");
                    for (int i = 0; i < ds.getRowCount(); ++i) {
                        String instrumentdesc = ds.getValue(i, "instrumentdesc");
                        if (instrumentdesc.length() > 40) {
                            instrumentdesc = instrumentdesc.substring(0, 40) + "...";
                        }
                        ds.setValue(i, "instrumentdesc", ds.getValue(i, keycolid1) + (instrumentdesc.length() > 0 ? " - " + instrumentdesc : ""));
                    }
                } else {
                    ds = sdidata.getDataset("primary");
                    if (isSingleKeyRequest) {
                        DataSet keyRequestDs = new DataSet();
                        keyRequestDs.addColumn(keycolid1, 0);
                        for (int i = 0; i < ds.getRowCount(); ++i) {
                            int row = keyRequestDs.addRow();
                            keyRequestDs.setValue(row, keycolid1, ds.getValue(i, keycolid1));
                        }
                        ds = keyRequestDs;
                    } else if ("User".equals(sdcid)) {
                        ds.removeColumn("password");
                    }
                }
            } else if (instrumentid != null && instrumentid.length() > 0) {
                ds = new QueryProcessor(connectionid).getPreparedSqlDataSet("select commandid, commandlabel, defaultcommandflag, returnflag from instrumentmodelcommand where instrumenttypeid in (select instrumenttype from instrument where instrumentid=? ) AND instrumentmodelid in (select instrumentmodelid from instrument where instrumentid=? ) order by usersequence", new Object[]{instrumentid, instrumentid});
            } else if (dataitemkey != null && dataitemkey.length() > 0) {
                String assignedInstrid;
                String[] itemkeys = StringUtil.split(dataitemkey, ";");
                SafeSQL safeSQL = new SafeSQL();
                String assignedInstrSQL = "select ds.s_instrumentid, pl.s_instrumenttype, pl.s_instrumentmodel from sdidataitem di, sdidata ds, paramlist pl where (di.sdcid=" + safeSQL.addVar(itemkeys[0]) + " and di.keyid1=" + safeSQL.addVar(itemkeys[1]) + " and di.keyid2=" + safeSQL.addVar(itemkeys[2]) + " and di.keyid3=" + safeSQL.addVar(itemkeys[3]) + " and di.paramlistid=" + safeSQL.addVar(itemkeys[4]) + " and di.paramlistversionid=" + safeSQL.addVar(itemkeys[5]) + " and di.variantid=" + safeSQL.addVar(itemkeys[6]) + " and di.dataset=" + safeSQL.addVar(itemkeys[7]) + " and di.paramid=" + safeSQL.addVar(itemkeys[8]) + " and di.paramtype=" + safeSQL.addVar(itemkeys[9]) + " and di.replicateid=" + safeSQL.addVar(itemkeys[10]) + " and di.sdcid=ds.sdcid and di.keyid1=ds.keyid1 and di.keyid2=ds.keyid2 and di.keyid3=ds.keyid3 and di.paramlistid=ds.paramlistid and di.paramlistversionid=ds.paramlistversionid and di.variantid=ds.variantid and di.dataset=ds.dataset and ds.paramlistid=pl.paramlistid and ds.paramlistversionid=pl.paramlistversionid and ds.variantid=pl.variantid)";
                ds = new QueryProcessor(connectionid).getPreparedSqlDataSet(assignedInstrSQL, safeSQL.getValues());
                if (ds != null && ds.size() == 1 && (assignedInstrid = ds.getValue(0, "s_instrumentid")).length() > 0) {
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid("Instrument");
                    sdiRequest.setQueryFrom("instrument");
                    sdiRequest.setQueryWhere(JSONDataSetRequest.getInstrumentCertWhereClause("instrumentid='" + SafeSQL.encodeForSQL(assignedInstrid, isOracle) + "'", isOracle));
                    sdiRequest.setRequestItem("primary[instrumentid]");
                    sdiRequest.setRetrieve(true);
                    SDIProcessor sdiProcessor = new SDIProcessor(connectionid);
                    SDIData sdidata = sdiProcessor.getSDIData(sdiRequest);
                    if (sdidata.getDataset("primary") == null || sdidata.getDataset("primary").size() == 0) {
                        ds.setValue(0, "s_instrumentid", "");
                    }
                }
            }
            if (ds == null) {
                ds = new DataSet();
            }
            if (dynamicsqlcode != null && dynamicsqlcode.length() > 0) {
                Map params = ajaxResponse.getRequestParameters();
                String columnids = ajaxResponse.getRequestParameter("columnids");
                String columnvalues = ajaxResponse.getRequestParameter("columnvalues");
                String fieldid = ajaxResponse.getRequestParameter("fieldid");
                String[] ids = StringUtil.split(columnids, ";");
                String[] values = StringUtil.split(columnvalues, ";");
                for (int i = 0; i < ids.length && i < values.length; ++i) {
                    params.put(ids[i], values[i]);
                }
                Object sqlObject = SQLRegister.getDynamicSQL(dynamicsqlcode);
                JSONObject responseJSONObject = new JSONObject();
                if (sqlObject instanceof String && ((String)sqlObject).length() > 0 || sqlObject instanceof PropertyList) {
                    ds = ajaxResponse.getRegisteredSQLDataSet(this.getQueryProcessor(), params);
                    boolean translateValue = true;
                    if (translateValue || SQLRegister.getDynamicSQL(dynamicsqlcode) instanceof PropertyList) {
                        String valuecolumn = ds.getColumnId(0);
                        int colcount = ds.getColumnCount();
                        String displaycolumn = colcount > 1 ? ds.getColumnId(1) : valuecolumn;
                        String ddsdcid = "";
                        if (SQLRegister.getDynamicSQL(dynamicsqlcode) instanceof PropertyList) {
                            PropertyList dropdowndefinition = (PropertyList)SQLRegister.getDynamicSQL(dynamicsqlcode);
                            ddsdcid = dropdowndefinition.getProperty("sdcid");
                            if (dropdowndefinition.getProperty("valuecolumn").length() > 0 && (valuecolumn = dropdowndefinition.getProperty("valuecolumn")).indexOf(".") > 0) {
                                valuecolumn = valuecolumn.substring(valuecolumn.indexOf(".") + 1);
                            }
                            String string = displaycolumn = dropdowndefinition.getProperty("displaycolumn").length() == 0 ? valuecolumn : dropdowndefinition.getProperty("displaycolumn");
                            if (displaycolumn.indexOf(".") > 0) {
                                displaycolumn = displaycolumn.substring(displaycolumn.indexOf(".") + 1);
                            }
                        }
                        DataSet processedDs = new DataSet();
                        processedDs.addColumn("valuecolumn", 0);
                        processedDs.addColumn("displaycolumn", 0);
                        processedDs.addColumn("translatedvalue", 0);
                        TranslationProcessor tp = null;
                        if (connectionInfo.getLanguage() != null && connectionInfo.getLanguage().length() > 0) {
                            tp = new TranslationProcessor(connectionid);
                            tp.setTextType(ddsdcid);
                        }
                        for (int i = 0; i < ds.getRowCount(); ++i) {
                            int row = processedDs.addRow();
                            processedDs.setValue(row, "valuecolumn", ds.getValue(i, valuecolumn));
                            String displayvalue = ds.getValue(i, displaycolumn).length() > 0 ? ds.getValue(i, displaycolumn) : ds.getValue(i, valuecolumn);
                            processedDs.setValue(row, "displaycolumn", displayvalue);
                            processedDs.setValue(row, "translatedvalue", tp != null ? tp.translate(displayvalue) : displayvalue);
                        }
                        ds = processedDs;
                    }
                    responseJSONObject.put("dataset", JSONUtil.toJSONObject(ds));
                    responseJSONObject.put("dynamicsqlcode", ajaxResponse.getRequestParameter("dynamicsqlcode"));
                }
                responseJSONObject.put("fieldid", fieldid);
                responseJSONObject.put("columnvalues", columnvalues);
                ajaxResponse.addCallbackArgument("data", responseJSONObject);
                ajaxResponse.addCallbackArgument("infieldid", fieldid);
                ajaxResponse.print();
                return;
            } else {
                JSONObject jsonResponseObj = JSONUtil.toJSONObject(ds, usedatatypes);
                jsonResponseObj.write(response.getWriter());
            }
            return;
        }
        catch (Exception e) {
            throw new ServletException((Throwable)e);
        }
    }

    public static SDIData getSDIData(String connectionid, String sdcid, String queryfrom, String querywhere, String queryorderby, ArrayList<String> queryItemList) {
        return JSONDataSetRequest.getSDIData(connectionid, sdcid, queryfrom, querywhere, queryorderby, queryItemList, -1);
    }

    public static SDIData getSDIData(String connectionid, String sdcid, String queryfrom, String querywhere, String queryorderby, ArrayList<String> queryItemList, int retrieveLimit) {
        SDIProcessor sdiProcessor = new SDIProcessor(connectionid);
        SDIRequest sdiRequest = new SDIRequest();
        if (sdcid == null && sdcid.length() == 0) {
            SafeSQL safeSQL = new SafeSQL();
            sdcid = new QueryProcessor(connectionid).getPreparedSqlDataSet("select sdcid from sdc where tableid=" + safeSQL.addVar(queryfrom), safeSQL.getValues()).getString(0, "sdcid");
        }
        if (queryfrom == null || queryfrom.length() == 0) {
            String tableid;
            SDCProcessor sdcProcessor = new SDCProcessor(connectionid);
            queryfrom = tableid = sdcProcessor.getProperty(sdcid, "tableid");
        }
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setQueryFrom(queryfrom);
        sdiRequest.setQueryWhere(querywhere);
        sdiRequest.setQueryOrderBy(queryorderby);
        if (queryItemList != null) {
            for (int i = 0; i < queryItemList.size(); ++i) {
                sdiRequest.setRequestItem(queryItemList.get(i));
            }
        } else {
            sdiRequest.setRequestItem("primary");
        }
        if (retrieveLimit > 0) {
            sdiRequest.setRetrieveLimit(retrieveLimit);
        }
        sdiRequest.setRetrieve(true);
        SDIData sdidata = sdiProcessor.getSDIData(sdiRequest);
        return sdidata;
    }

    private static String getInstrumentCertWhereClause(String queryWhere, boolean isOracle) {
        return (queryWhere != null && queryWhere.length() > 0 ? "(" + queryWhere + ") AND " : "") + "  (instrument.instrumentstatus != 'Unavailable' ) AND (instrument.inserviceflag is null OR instrument.inserviceflag!='N') AND ( instrument.certificationreqflag is null OR instrument.certificationreqflag='N' OR (instrument.certificationreqflag='P' AND instrument.instrumentstatus='Available') OR EXISTS (SELECT S_SDICERTIFICATION.RESOURCEKEYID1 FROM s_sdicertification \n  WHERE S_SDICERTIFICATION.CERTIFICATIONTYPE = 'Instrument'\n     AND S_SDICERTIFICATION.CERTIFICATIONSTATUS = 'Valid'\n     AND S_SDICERTIFICATION.RESOURCESDCID = 'Instrument' \n     AND S_SDICERTIFICATION.RESOURCEKEYID1 = INSTRUMENT.INSTRUMENTID \n     AND (S_SDICERTIFICATION.EXPIRATIONDT IS NULL  \n        OR " + (isOracle ? "( SYSDATE < DECODE(S_SDICERTIFICATION.GRACEPERIODUNITS, 'Days', S_SDICERTIFICATION.EXPIRATIONDT+NVL(S_SDICERTIFICATION.GRACEPERIOD,0), \n                        'Weeks', S_SDICERTIFICATION.EXPIRATIONDT+7*NVL(S_SDICERTIFICATION.GRACEPERIOD,0),\n                        'Months', ADD_MONTHS(S_SDICERTIFICATION.EXPIRATIONDT, NVL(S_SDICERTIFICATION.GRACEPERIOD,0)), \n                        'Years', ADD_MONTHS(S_SDICERTIFICATION.EXPIRATIONDT, 12*NVL(S_SDICERTIFICATION.GRACEPERIOD,0)),\n                        S_SDICERTIFICATION.EXPIRATIONDT)\n            )\n" : "            ( GETDATE() <  CASE  S_SDICERTIFICATION.GRACEPERIODUNITS\n                                WHEN 'Days' THEN DATEADD( DAY, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)    \n                                WHEN 'Weeks' THEN DATEADD( WEEK, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)    \n                                WHEN 'Months' THEN DATEADD( MONTH, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)    \n                                WHEN 'Years' THEN DATEADD( YEAR, ISNULL(S_SDICERTIFICATION.GRACEPERIOD,0),S_SDICERTIFICATION.EXPIRATIONDT)    \n                                ELSE S_SDICERTIFICATION.EXPIRATIONDT        \n                            END            \n            )    \n") + "    ) ) )";
    }
}


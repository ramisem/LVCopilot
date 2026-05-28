/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import com.labvantage.sapphire.pageelements.dynamicmaint.util.ExtraColumnUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.FilterUtil;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.SpecLimitHandler;
import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.action.ActionConstants;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SearchSdidataGroup
extends BaseAjaxRequest
implements ActionConstants {
    private DataSet sdiworkitem;
    private DataSet sdidata;
    private DataSet sdidataitem;
    private DataSet sdidataapproval;
    private DataSet sdispec;

    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        int i;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String inputWorkitemid = ar.getRequestParameter("workitemid", "");
        String inputParamlistid = ar.getRequestParameter("paramlistid", "");
        String mastersdcid = ar.getRequestParameter("mastersdcid", "Product");
        String masterkeyid1 = ar.getRequestParameter("masterkeyid1", "");
        String masterkeyid2 = ar.getRequestParameter("masterkeyid2", "1");
        String inputTestsetid = ar.getRequestParameter("testsetid", "");
        String pageId = ar.getRequestParameter("pageid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "[keyid1]");
        String keyid2 = ar.getRequestParameter("keyid2", "(null)");
        String keyid3 = ar.getRequestParameter("keyid3", "(null)");
        HttpSession session = req.getSession();
        PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
        boolean useTestLevel = this.checkUseTestLevel();
        boolean hasParentProduct = this.checkHasParentProduct();
        if (keyid1.equals("*")) {
            keyid1 = "[keyid1]";
        }
        String testsetid = null;
        if (!inputWorkitemid.equals("")) {
            this.searchByWorkitem(inputWorkitemid);
        } else if (!inputParamlistid.equals("")) {
            this.searchByParamlist(inputParamlistid);
        } else if (!masterkeyid1.equals("")) {
            String temp;
            String parentProductid = "";
            String parentProductversionid = "";
            if (mastersdcid.equals("Product") && hasParentProduct && (temp = this.getParentProduct(masterkeyid1, masterkeyid2)).contains(";")) {
                parentProductid = temp.substring(0, temp.indexOf(59));
                parentProductversionid = temp.substring(temp.indexOf(59) + 1);
            }
            if (!inputTestsetid.equals("")) {
                testsetid = inputTestsetid;
            } else if (useTestLevel) {
                String sql1 = "SELECT levelid FROM s_spdetail spd, s_product p WHERE p.s_productid=? AND p.s_productversionid=? AND spd.s_samplingplanid=p.embeddedsamplingplanid AND spd.s_samplingplanversionid=p.embeddedsamplingplanversionid ORDER BY spd.usersequence";
                Object[] params1 = parentProductid.equals("") ? new String[]{masterkeyid1, masterkeyid2} : new String[]{parentProductid, parentProductversionid};
                DataSet ds1 = this.getQueryProcessor().getPreparedSqlDataSet(sql1, params1);
                testsetid = ds1 == null || ds1.getRowCount() == 0 ? null : (ds1.getRowCount() == 1 ? ds1.getString(0, "levelid", "") : this.getDefaultTestSetId(mastersdcid, masterkeyid1, masterkeyid2));
            } else {
                testsetid = null;
            }
            if (testsetid != null) {
                if (!parentProductid.equals("")) {
                    this.searchByProductAndSamplingPlan(parentProductid, parentProductversionid, testsetid);
                } else {
                    this.searchByProductAndSamplingPlan(masterkeyid1, masterkeyid2, testsetid);
                }
            } else if (!parentProductid.equals("")) {
                this.searchByMasterSdc(mastersdcid, parentProductid, parentProductversionid);
            } else {
                this.searchByMasterSdc(mastersdcid, masterkeyid1, masterkeyid2);
            }
            if (this.sdispec != null && this.sdispec.getRowCount() > 0) {
                String specId = this.sdispec.getString(0, "specid", "");
                String[] specVersionId = this.sdispec.getString(0, "specversionid", "");
                if (!parentProductid.equals("")) {
                    new SpecLimitHandler(this.getQueryProcessor()).injectSpecLimits(this.sdidataitem, specId, (String)specVersionId);
                } else {
                    new SpecLimitHandler(this.getQueryProcessor()).injectSpecLimits(this.sdidataitem, specId, (String)specVersionId);
                }
            }
        } else {
            this.sdiworkitem = new DataSet();
            this.sdidata = new DataSet();
            this.sdidataapproval = new DataSet();
            this.sdidataitem = new DataSet();
            this.sdispec = new DataSet();
        }
        this.duplicateReplicateRows();
        Utils.fillKeyids(this.sdiworkitem, keyid1, keyid2, keyid3);
        Utils.fillKeyids(this.sdidata, keyid1, keyid2, keyid3);
        Utils.fillKeyids(this.sdidataapproval, keyid1, keyid2, keyid3);
        Utils.fillKeyids(this.sdidataitem, keyid1, keyid2, keyid3);
        Utils.fillKeyids(this.sdispec, keyid1, keyid2, keyid3);
        this.sdidataitem.sort("usersequence, replicateid");
        if (pageProps != null) {
            String[] elementIdArr;
            ExtraColumnUtil extraColumnUtil = new ExtraColumnUtil(this.getQueryProcessor(), this.logger);
            String elementIds = ar.getRequestParameter("elementid", "");
            for (String elementId : elementIdArr = elementIds.split(";")) {
                Map<String, List<String>> extraColumns = extraColumnUtil.parseExtraColumns(pageProps, elementId);
                for (Map.Entry<String, List<String>> entry : extraColumns.entrySet()) {
                    String datasource = entry.getKey();
                    List<String> extracolumnList = entry.getValue();
                    if (datasource.equals("sdiworkitem")) {
                        extraColumnUtil.fillExtraColumnData(extracolumnList, this.sdiworkitem, "sdiworkitem");
                        continue;
                    }
                    if (datasource.equals("sdidata")) {
                        extraColumnUtil.fillExtraColumnData(extracolumnList, this.sdidata, "sdidata");
                        continue;
                    }
                    if (!datasource.equals("sdidataitem")) continue;
                    extraColumnUtil.fillExtraColumnData(extracolumnList, this.sdidataitem, "sdidataitem");
                }
            }
        }
        Map<String, List<String>> translateColumns = Utils.getTranslateColumns(pageProps);
        Utils.translateData(this.sdiworkitem, translateColumns.get("sdiworkitem"), this.getConnectionProcessor().getLanguage(), "WorkItem", this.getTranslationProcessor());
        Utils.translateData(this.sdidata, translateColumns.get("sdidata"), this.getConnectionProcessor().getLanguage(), "ParamList", this.getTranslationProcessor());
        Utils.translateData(this.sdidataitem, translateColumns.get("sdidataitem"), this.getConnectionProcessor().getLanguage(), "Param", this.getTranslationProcessor());
        Utils.translateData(this.sdidataapproval, translateColumns.get("sdidataapproval"), this.getConnectionProcessor().getLanguage(), "W", this.getTranslationProcessor());
        FilterUtil filterUtil = new FilterUtil(pageProps);
        filterUtil.filterDataset(this.sdiworkitem, "sdiworkitem");
        filterUtil.filterDataset(this.sdidata, "sdidata");
        filterUtil.filterDataset(this.sdidata, "sdidataapproval");
        filterUtil.filterDataset(this.sdidataitem, "sdidataitem");
        this.sdidata.addColumn("availabilityflag", 0);
        HashSet<String> preparationWorkItems = new HashSet<String>();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            if (!this.sdidata.getString(i, "s_paramlisttype", "").equals("Preparation")) continue;
            preparationWorkItems.add(this.sdidata.getString(i, "sourceworkitemid"));
        }
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            if (!this.sdidata.getString(i, "s_paramlisttype", "Procedural").equals("Procedural") || !preparationWorkItems.contains(this.sdidata.getString(i, "sourceworkitemid"))) continue;
            this.sdidata.setString(i, "availabilityflag", "N");
        }
        PropertyList data = new PropertyList();
        data.setProperty("sdiworkitem", Utils.dsToCollection(this.sdiworkitem));
        data.setProperty("sdidata", Utils.dsToCollection(this.sdidata));
        data.setProperty("sdidataapproval", Utils.dsToCollection(this.sdidataapproval));
        data.setProperty("sdidataitem", Utils.dsToCollection(this.sdidataitem));
        data.setProperty("sdispec", Utils.dsToCollection(this.sdispec));
        ar.addCallbackArgument("data", data.toJSONObject());
        ar.addCallbackArgument("elementid", ar.getRequestParameter("elementid", ""));
        ar.addCallbackArgument("focusfieldid", ar.getRequestParameter("focusfieldid", ""));
        ar.addCallbackArgument("testsetid", Utils.notNull(testsetid));
        ar.addCallbackArgument("testsetidfield", ar.getRequestParameter("testsetidfield", ""));
        ar.addCallbackArgument("ignoreduplicates", ar.getRequestParameter("ignoreduplicates", "N"));
        ar.addCallbackArgument("detailcollectionid", ar.getRequestParameter("detailcollectionid", "N"));
        ar.print();
    }

    private boolean checkHasParentProduct() {
        String sql = "SELECT columnid FROM syscolumn WHERE tableid='s_product' AND columnid='crl_mainproduct'";
        return this.getQueryProcessor().getSqlDataSet(sql).getRowCount() > 0;
    }

    private boolean checkUseTestLevel() {
        String sql = "SELECT columnid FROM syscolumn WHERE tableid='s_product' AND columnid='crl_defaulttestinglevelid'";
        return this.getQueryProcessor().getSqlDataSet(sql).getRowCount() > 0;
    }

    private String getParentProduct(String productid, String productversionid) {
        String retval = "";
        String sql2 = "SELECT crl_mainproduct, crl_mainproductversionid FROM s_product WHERE s_productid=? AND s_productversionid=?";
        Object[] params2 = new String[]{productid, productversionid};
        DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql2, params2);
        if (ds2 != null && ds2.getRowCount() == 1 && !ds2.getString(0, "crl_mainproduct", "").equals("")) {
            retval = ds2.getString(0, "crl_mainproduct", "") + ";" + ds2.getString(0, "crl_mainproductversionid", "1");
        }
        return retval;
    }

    private String getDefaultTestSetId(String sdcid, String keyid1, String keyid2) {
        String retval = "";
        if (sdcid.equals("Product")) {
            String sql = "SELECT crl_defaulttestinglevelid FROM s_product WHERE s_productid=? AND s_productversionid=?";
            Object[] params = new String[]{keyid1, keyid2};
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
            if (ds != null && ds.getRowCount() == 1) {
                retval = ds.getString(0, "crl_defaulttestinglevelid", "");
            } else if (ds == null) {
                this.logger.error("Invalid query: " + sql);
            }
        }
        return retval;
    }

    private void searchByParamlist(String paramlistids) {
        int i;
        String[] arrParamlistids;
        ArrayList<String> params = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int semicolonPos = paramlistids.indexOf(";");
        int pipePos = paramlistids.indexOf("|");
        if (semicolonPos > 0 && pipePos > 0 && semicolonPos < pipePos) {
            arrParamlistids = paramlistids.split("\\|");
            if (arrParamlistids.length == 3) {
                String[] paramlistidArr = arrParamlistids[0].split(";");
                String[] paramlistversionidArr = arrParamlistids[1].split(";");
                String[] variantidArr = arrParamlistids[2].split(";");
                if (paramlistidArr.length == paramlistversionidArr.length && paramlistidArr.length == variantidArr.length) {
                    for (int i2 = 0; i2 < paramlistidArr.length; ++i2) {
                        String paramlistid = paramlistidArr[i2];
                        String paramlistversionid = paramlistversionidArr[i2];
                        String variantid = variantidArr[i2];
                        if (paramlistversionid.equals("C")) {
                            paramlistversionid = this.getCurrentParamlistVersion(paramlistid, variantid);
                        }
                        sb.append(" OR (paramlistid=? and paramlistversionid=? and variantid=?) ");
                        params.add(paramlistid);
                        params.add(paramlistversionid);
                        params.add(variantid);
                    }
                }
            }
        } else {
            for (String id : arrParamlistids = paramlistids.split(";")) {
                String[] arr = id.split("\\|");
                if (arr.length > 2) {
                    String paramlistid = arr[0];
                    String paramlistversionid = arr[1];
                    String variantid = arr[2];
                    if (paramlistversionid.equals("C")) {
                        paramlistversionid = this.getCurrentParamlistVersion(paramlistid, variantid);
                    }
                    sb.append(" OR (paramlistid=? and paramlistversionid=? and variantid=?) ");
                    params.add(paramlistid);
                    params.add(paramlistversionid);
                    params.add(variantid);
                    continue;
                }
                sb.append(" OR paramlistid=? ");
                params.add(id);
            }
        }
        String sqlSdidata = "SELECT i.paramlistid, i.paramlistversionid, i.variantid FROM paramlist i WHERE (" + sb.substring(4) + ") ORDER BY i.usersequence";
        this.sdiworkitem = new DataSet();
        this.sdidata = this.getQueryProcessor().getPreparedSqlDataSet(sqlSdidata, (Object[])params.toArray(new String[params.size()]));
        if (this.sdidata == null) {
            this.sdidata = new DataSet();
            this.logger.error("Invalid query: " + sqlSdidata);
        }
        this.sortSDIData(paramlistids);
        this.sdidataapproval = new DataSet();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            this.getSdidataapprovalByParamlist(this.sdidata.getString(i, "paramlistid"), this.sdidata.getString(i, "paramlistversionid"), this.sdidata.getString(i, "variantid"), this.sdidataapproval);
        }
        this.sdidataitem = new DataSet();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            this.getSdidataitemByParamlist(this.sdidata.getString(i, "paramlistid"), this.sdidata.getString(i, "paramlistversionid"), this.sdidata.getString(i, "variantid"), this.sdidata.getInt(i, "__dataset", 0), this.sdidataitem);
        }
        this.sdispec = new DataSet();
    }

    private void sortSDIData(String paramlistids) {
        if (!paramlistids.isEmpty()) {
            ArrayList paramListKeyList = new ArrayList();
            String[] paramListKeyParts = paramlistids.split("\\|");
            if (paramListKeyParts.length > 0) {
                String[] paramListIds = paramListKeyParts[0].split(";");
                String[] paramListVersionIds = paramListKeyParts[1].split(";");
                String[] variantIds = paramListKeyParts[2].split(";");
                int paramListCount = 0;
                if (paramListIds.length == paramListVersionIds.length && paramListIds.length == variantIds.length) {
                    paramListCount = paramListIds.length;
                }
                for (int i = 0; i < paramListCount; ++i) {
                    ArrayList<String> paramListKey = new ArrayList<String>();
                    paramListKey.add(paramListIds[i]);
                    paramListKey.add(paramListVersionIds[i]);
                    paramListKey.add(variantIds[i]);
                    paramListKeyList.add(paramListKey);
                }
            }
            for (int i = 0; i < this.sdidata.getRowCount(); ++i) {
                String paramListId = this.sdidata.getString(i, "paramlistid", "");
                String paramListVersionId = this.sdidata.getString(i, "paramlistversionid", "");
                String variantId = this.sdidata.getString(i, "variantid", "");
                ArrayList<String> paramListKey = new ArrayList<String>();
                paramListKey.add(paramListId);
                paramListKey.add(paramListVersionId);
                paramListKey.add(variantId);
                int userSequence = paramListKeyList.indexOf(paramListKey);
                this.sdidata.setNumber(i, "usersequence", userSequence);
            }
            this.sdidata.sort("usersequence, paramlistid");
        }
    }

    private void searchByWorkitem(String workitemids) {
        int i;
        String[] workitemidArr;
        String[] arrWorkitemIds;
        ArrayList<String> params = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean currentVersion = false;
        int semicolonPos = workitemids.indexOf(";");
        int pipePos = workitemids.indexOf("|");
        if (semicolonPos > 0 && pipePos > 0 && semicolonPos < pipePos) {
            String[] workitemversionidArr;
            arrWorkitemIds = workitemids.split("\\|");
            if (arrWorkitemIds.length == 2 && (workitemidArr = arrWorkitemIds[0].split(";")).length == (workitemversionidArr = arrWorkitemIds[1].split(";")).length) {
                for (i = 0; i < workitemidArr.length; ++i) {
                    String workitemid = workitemidArr[i];
                    String workitemversionid = workitemversionidArr[i];
                    if (workitemversionid.equals("C")) {
                        workitemversionid = this.getCurrentWorkitemVersion(workitemid);
                        currentVersion = true;
                    }
                    sb.append(" OR (wi.workitemid=? and wi.workitemversionid=?) ");
                    params.add(workitemid);
                    params.add(workitemversionid);
                }
            }
        } else {
            workitemidArr = arrWorkitemIds = workitemids.split(";");
            int workitemversionidArr = workitemidArr.length;
            for (i = 0; i < workitemversionidArr; ++i) {
                String id = workitemidArr[i];
                String[] arr = id.split("\\|");
                if (arr.length > 1) {
                    String workitemid = arr[0];
                    String workitemversionid = arr[1];
                    if (workitemversionid.equals("C")) {
                        workitemversionid = this.getCurrentWorkitemVersion(workitemid);
                        currentVersion = true;
                    }
                    sb.append(" OR (wi.workitemid=? and wi.workitemversionid=?) ");
                    params.add(workitemid);
                    params.add(workitemversionid);
                    continue;
                }
                sb.append(" OR wi.workitemid=? ");
                params.add(id);
            }
        }
        int arrSize = params.size();
        for (int i2 = 0; i2 < arrSize; ++i2) {
            params.add((String)params.get(i2));
        }
        String sqlSdiworkitem = "SELECT workitemid, workitemversionid, workitemtypeflag, usersequence, 'A' \"__rowstatus\", null \"__virtual\", null workitemgroup FROM workitem wi WHERE (" + sb.substring(4) + ") UNION ALL SELECT w.workitemid, w.workitemversionid, w.workitemtypeflag, wi.usersequence, 'V' \"__rowstatus\", 'Y' \"__virtual\", wi.workitemid workitemgroup FROM workitem w, workitemitem wi WHERE (" + sb.substring(4) + ") AND wi.sdcid='WorkItem' AND wi.keyid1=w.workitemid AND (  wi.keyid2=w.workitemversionid OR   (wi.keyid2='C' AND (    (w.workitemversionid=(SELECT max(x.workitemversionid) FROM workitem x     WHERE x.workitemid=wi.keyid1 AND x.versionstatus='C'))   OR     (w.workitemversionid=(SELECT max(x.workitemversionid) FROM workitem x     WHERE x.workitemid=wi.keyid1 AND x.versionstatus='P')     AND NOT EXISTS (SELECT 1 FROM workitem z WHERE z.workitemid=wi.keyid1 AND z.versionstatus='C')) ))) ORDER BY usersequence";
        this.sdiworkitem = this.getQueryProcessor().getPreparedSqlDataSet(sqlSdiworkitem, (Object[])params.toArray(new String[params.size()]));
        if (this.sdiworkitem == null) {
            this.sdiworkitem = new DataSet();
            this.logger.error("Invalid query: " + sqlSdiworkitem);
        }
        this.sortSDIWorkItems(workitemids);
        this.sdiworkitem.addColumn("__workiteminstance", 1);
        HashMap<String, Integer> instances = new HashMap<String, Integer>();
        for (i = this.sdiworkitem.getRowCount() - 1; i >= 0; --i) {
            Integer instance = (Integer)instances.get(this.sdiworkitem.getString(i, "workitemid"));
            instance = instance == null ? Integer.valueOf(0) : Integer.valueOf(instance + 1);
            this.sdiworkitem.setNumber(i, "__workiteminstance", instance);
            instances.put(this.sdiworkitem.getString(i, "workitemid"), instance);
        }
        if (currentVersion) {
            for (i = 0; i < this.sdiworkitem.getRowCount(); ++i) {
                this.sdiworkitem.setValue(i, "workitemversionid", "C");
            }
        }
        this.sdidata = new DataSet();
        for (i = 0; i < this.sdiworkitem.getRowCount(); ++i) {
            this.getSdidataByWorkitem(this.sdiworkitem.getString(i, "workitemid"), this.sdiworkitem.getString(i, "workitemversionid"), this.sdiworkitem.getInt(i, "__workiteminstance"), this.sdidata);
        }
        this.sdidataapproval = new DataSet();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            this.getSdidataapprovalByParamlist(this.sdidata.getString(i, "paramlistid"), this.sdidata.getString(i, "paramlistversionid"), this.sdidata.getString(i, "variantid"), this.sdidataapproval);
        }
        this.sdidataitem = new DataSet();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            this.getSdidataitemByParamlist(this.sdidata.getString(i, "paramlistid"), this.sdidata.getString(i, "paramlistversionid"), this.sdidata.getString(i, "variantid"), this.sdidata.getInt(i, "__dataset", 0), this.sdidataitem);
        }
        this.sdispec = new DataSet();
    }

    private void sortSDIWorkItems(String workitemids) {
        if (!workitemids.isEmpty()) {
            List<String> workItemIdList = Arrays.asList(workitemids.split("\\|")[0].split(";"));
            for (int i = 0; i < this.sdiworkitem.getRowCount(); ++i) {
                int sequence;
                String workItemId = this.sdiworkitem.getString(i, "workitemid", "");
                BigDecimal userSequence = this.sdiworkitem.getBigDecimal(i, "usersequence");
                if (userSequence == null) {
                    sequence = workItemIdList.indexOf(workItemId) * 1000;
                } else {
                    String workItemGroup = this.sdiworkitem.getString(i, "workitemgroup", "");
                    sequence = workItemIdList.indexOf(workItemGroup) * 1000 + userSequence.intValue();
                }
                this.sdiworkitem.setNumber(i, "usersequence", sequence);
            }
            this.sdiworkitem.sort("usersequence, workitemid");
        }
    }

    private String getCurrentWorkitemVersion(String workitemid) {
        String getVersionInfoSql = "SELECT workitemid, workitemversionid, versionstatus FROM workitem WHERE workitemid = ? ORDER BY workitemid, CAST(workitemversionid AS int), versionstatus";
        Object[] params = new String[]{workitemid};
        DataSet getVersionInfoDs = this.getQueryProcessor().getPreparedSqlDataSet(getVersionInfoSql, params);
        String versionid = "";
        HashMap<String, String> currentFilter = new HashMap<String, String>();
        currentFilter.put("workitemid", workitemid);
        currentFilter.put("versionstatus", "C");
        DataSet filteredCurrentDs = getVersionInfoDs.getFilteredDataSet(currentFilter);
        if (filteredCurrentDs.getRowCount() > 0) {
            versionid = filteredCurrentDs.getString(0, "workitemversionid");
        } else {
            HashMap<String, String> provisionalFilter = new HashMap<String, String>();
            provisionalFilter.put("workitemid", workitemid);
            provisionalFilter.put("versionstatus", "P");
            DataSet filteredProvisionalDs = getVersionInfoDs.getFilteredDataSet(provisionalFilter);
            if (filteredProvisionalDs.getRowCount() > 0) {
                versionid = filteredProvisionalDs.getString(filteredProvisionalDs.getRowCount() - 1, "workitemversionid");
            }
        }
        return versionid;
    }

    private String getCurrentParamlistVersion(String paramlistid, String variantid) {
        String getVersionInfoSql = "SELECT paramlistid, variantid, paramlistversionid, versionstatus FROM paramlist WHERE  paramlistid = ? and variantid = ? ORDER BY paramlistid, variantid, CAST(paramlistversionid AS int), versionstatus";
        Object[] params = new String[]{paramlistid, variantid};
        DataSet getVersionInfoDs = this.getQueryProcessor().getPreparedSqlDataSet(getVersionInfoSql, params);
        String versionid = "";
        HashMap<String, String> currentFilter = new HashMap<String, String>();
        currentFilter.put("paramlistid", paramlistid);
        currentFilter.put("variantid", variantid);
        currentFilter.put("versionstatus", "C");
        DataSet filteredCurrentDs = getVersionInfoDs.getFilteredDataSet(currentFilter);
        if (filteredCurrentDs.getRowCount() > 0) {
            versionid = filteredCurrentDs.getString(0, "paramlistversionid");
        } else {
            HashMap<String, String> provisionalFilter = new HashMap<String, String>();
            provisionalFilter.put("paramlistid", paramlistid);
            provisionalFilter.put("variantid", variantid);
            provisionalFilter.put("versionstatus", "P");
            DataSet filteredProvisionalDs = getVersionInfoDs.getFilteredDataSet(provisionalFilter);
            if (filteredProvisionalDs.getRowCount() > 0) {
                versionid = filteredProvisionalDs.getString(filteredProvisionalDs.getRowCount() - 1, "paramlistversionid");
            }
        }
        return versionid;
    }

    private void searchByMasterSdc(String sdcid, String keyid1, String keyid2) {
        int i;
        String sqlSdiworkitem = "SELECT wi.workitemid, wi.workitemversionid, wi.workitemtypeflag, swi.usersequence, 'V' \"__rowstatus\", 'Y' \"__virtual\" FROM workitem wi, sdiworkitem swi WHERE swi.sdcid=? AND swi.keyid1=? AND swi.keyid2=? AND swi.workitemid=wi.workitemid AND swi.workitemversionid=wi.workitemversionid ORDER BY swi.usersequence ";
        Object[] params = new String[]{sdcid, keyid1, keyid2};
        this.sdiworkitem = this.getQueryProcessor().getPreparedSqlDataSet(sqlSdiworkitem, params);
        if (this.sdiworkitem == null) {
            this.sdiworkitem = new DataSet();
            this.logger.error("Invalid query: " + sqlSdiworkitem);
        }
        this.sdidata = new DataSet();
        for (i = 0; i < this.sdiworkitem.getRowCount(); ++i) {
            this.getSdidataByWorkitem(this.sdiworkitem.getString(i, "workitemid"), this.sdiworkitem.getString(i, "workitemversionid"), 0, this.sdidata);
        }
        this.sdidataapproval = new DataSet();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            this.getSdidataapprovalByParamlist(this.sdidata.getString(i, "paramlistid"), this.sdidata.getString(i, "paramlistversionid"), this.sdidata.getString(i, "variantid"), this.sdidataapproval);
        }
        this.sdidataitem = new DataSet();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            this.getSdidataitemByParamlist(this.sdidata.getString(i, "paramlistid"), this.sdidata.getString(i, "paramlistversionid"), this.sdidata.getString(i, "variantid"), this.sdidata.getInt(i, "__dataset", 0), this.sdidataitem);
        }
        if (sdcid.equals("Product")) {
            String specSql = "SELECT s.specid, s.specversionid, 'V' \"__rowstatus\", 'Y' \"__virtual\" FROM s_product p JOIN spec s ON p.embeddedspecid=s.specid AND p.embeddedspecversionid=s.specversionid WHERE s_productid=? AND s_productversionid=? ";
            Object[] specParams = new String[]{keyid1, keyid2};
            this.sdispec = this.getQueryProcessor().getPreparedSqlDataSet(specSql, specParams);
            if (this.sdispec == null) {
                this.logger.debug("Invalid query: " + specSql);
            }
        } else {
            String specSql = "SELECT s.specid, s.specversionid FROM sdispec s WHERE s.sdcid=? AND s.keyid1=? AND s.keyid2=?";
            Object[] specParams = new String[]{sdcid, keyid1, keyid2};
            this.sdispec = this.getQueryProcessor().getPreparedSqlDataSet(specSql, specParams);
            if (this.sdispec == null) {
                this.logger.debug("Invalid query: " + specSql);
            }
        }
    }

    private void searchByProductAndSamplingPlan(String keyid1, String keyid2, String testsetid) {
        int i;
        String sqlSdiworkitem = "SELECT wi.workitemid, wi.workitemversionid, wi.workitemtypeflag, swi.usersequence, 'V' \"__rowstatus\", 'Y' \"__virtual\" FROM s_product p, workitem wi, sdiworkitem swi, s_spitem spi, s_spdetail spd, s_spdetailitem spdi WHERE p.s_productid=? AND p.s_productversionid=? AND swi.sdcid='Product' AND swi.keyid1=p.s_productid AND swi.keyid2=p.s_productversionid AND swi.workitemid=wi.workitemid AND swi.workitemversionid=wi.workitemversionid AND spd.s_samplingplanid=spi.s_samplingplanid AND spd.s_samplingplanversionid=spi.s_samplingplanversionid AND spd.levelid=? AND spi.s_samplingplanid=p.embeddedsamplingplanid AND spi.s_samplingplanversionid=p.embeddedsamplingplanversionid AND spdi.s_samplingplandetailno=spd.s_samplingplandetailno AND spdi.s_samplingplanitemno=spi.s_samplingplanitemno AND spdi.s_samplingplanid=spi.s_samplingplanid AND spdi.s_samplingplanversionid=spi.s_samplingplanversionid AND spi.itemsdcid='WorkItem' AND spi.itemkeyid1=swi.workitemid AND spi.itemkeyid2=swi.workitemversionid ORDER BY swi.usersequence ";
        Object[] params = new String[]{keyid1, keyid2, testsetid};
        this.sdiworkitem = this.getQueryProcessor().getPreparedSqlDataSet(sqlSdiworkitem, params);
        if (this.sdiworkitem == null) {
            this.sdiworkitem = new DataSet();
            this.logger.error("Invalid query: " + sqlSdiworkitem);
        }
        this.sdidata = new DataSet();
        for (i = 0; i < this.sdiworkitem.getRowCount(); ++i) {
            this.getSdidataByWorkitem(this.sdiworkitem.getString(i, "workitemid"), this.sdiworkitem.getString(i, "workitemversionid"), 0, this.sdidata);
        }
        this.sdidataapproval = new DataSet();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            this.getSdidataapprovalByParamlist(this.sdidata.getString(i, "paramlistid"), this.sdidata.getString(i, "paramlistversionid"), this.sdidata.getString(i, "variantid"), this.sdidataapproval);
        }
        this.sdidataitem = new DataSet();
        for (i = 0; i < this.sdidata.getRowCount(); ++i) {
            this.getSdidataitemByParamlist(this.sdidata.getString(i, "paramlistid"), this.sdidata.getString(i, "paramlistversionid"), this.sdidata.getString(i, "variantid"), this.sdidata.getInt(i, "__dataset", 0), this.sdidataitem);
        }
        String specSql = "SELECT s.specid, s.specversionid, 'V' \"__rowstatus\", 'Y' \"__virtual\" FROM s_product p JOIN spec s ON p.embeddedspecid=s.specid and p.embeddedspecversionid=s.specversionid WHERE s_productid=? AND s_productversionid=? ";
        Object[] specParams = new String[]{keyid1, keyid2};
        this.sdispec = this.getQueryProcessor().getPreparedSqlDataSet(specSql, specParams);
        if (this.sdispec == null) {
            this.logger.debug("Invalid query: " + specSql);
        }
    }

    private void getSdidataByWorkitem(String workitemid, String workitemversionid, int workItemInstance, DataSet addToDataset) {
        String defaultForceNewFlag = "coalesce(wii.forcenewflag,'N')";
        try {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
            if (policy.getPropertyListNotNull("workitemforcenew").getProperty("alwaysforcenew").equals("Y")) {
                defaultForceNewFlag = "'Y'";
            }
        }
        catch (Exception e) {
            this.logger.error("", e);
        }
        String sqlSdidata = "SELECT pl.paramlistid, pl.paramlistversionid, pl.variantid, pl.s_paramlisttype, wii.workitemid as sourceworkitemid, wii.usersequence workitem_userseq, pl.usersequence paramlist_userseq, " + defaultForceNewFlag + " forcenewflag, wii.repeatcount, " + workItemInstance + " \"__workiteminstance\" FROM paramlist pl, workitemitem wii WHERE wii.workitemid=? AND wii.workitemversionid=? AND wii.sdcid='ParamList' AND wii.keyid1=pl.paramlistid AND (  wii.keyid2=pl.paramlistversionid OR   (wii.keyid2='C' AND     (       (pl.paramlistversionid=(SELECT max(x.paramlistversionid) FROM paramlist x        WHERE x.paramlistid=wii.keyid1 AND variantid=wii.keyid3 AND versionstatus='C'))     OR       (pl.paramlistversionid=(SELECT max(x.paramlistversionid) FROM paramlist x       WHERE x.paramlistid=wii.keyid1 AND x.variantid=wii.keyid3 AND x.versionstatus='P')       AND NOT EXISTS (SELECT 1 FROM paramlist z WHERE z.paramlistid=wii.keyid1 AND z.variantid=wii.keyid3 AND z.versionstatus='C')) ))) AND wii.keyid3=pl.variantid ORDER BY wii.usersequence ";
        Object[] params = new String[]{workitemid, workitemversionid};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sqlSdidata, params);
        DataSet ds2 = new DataSet();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            int repeatCount = ds.getInt(i, "repeatcount", 1);
            for (int j = 0; j < repeatCount; ++j) {
                ds2.copyRow(ds, i, 1);
            }
        }
        this.appendDatasetToAnother(addToDataset, ds2);
        if (!addToDataset.isValidColumn("__dataset")) {
            addToDataset.addColumn("__dataset", 1);
        }
        HashMap<String, Integer> datasets = new HashMap<String, Integer>();
        for (int i = 0; i < addToDataset.getRowCount(); ++i) {
            Integer dataset = (Integer)datasets.get(addToDataset.getString(i, "paramlistid") + ";" + addToDataset.getString(i, "variantid"));
            dataset = dataset == null ? Integer.valueOf(0) : Integer.valueOf(dataset + 1);
            addToDataset.setNumber(i, "__dataset", dataset);
            datasets.put(addToDataset.getString(i, "paramlistid") + ";" + addToDataset.getString(i, "variantid"), dataset);
        }
    }

    private void getSdidataapprovalByParamlist(String paramlistid, String paramlistversionid, String variantid, DataSet addToDataset) {
        String sqlSdidataitem = "SELECT pl.paramlistid, pl.paramlistversionid, pl.variantid, sda.approvalstep, sda.roleid, sda.mandatoryflag, 'U' approvalflag, sda.forcepeerflag FROM approvaltypestep sda, paramlist pl WHERE pl.paramlistid=? AND pl.paramlistversionid=? AND pl.variantid=? AND sda.approvaltypeid=pl.approvaltypeid ORDER BY sda.usersequence ";
        Object[] params = new String[]{paramlistid, paramlistversionid, variantid};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sqlSdidataitem, params);
        this.appendDatasetToAnother(addToDataset, ds);
    }

    private void getSdidataitemByParamlist(String paramlistid, String paramlistversionid, String variantid, int dataset, DataSet addToDataset) {
        String sqlSdidataitem = "SELECT sdi.paramlistid, sdi.paramlistversionid, sdi.variantid, " + dataset + " \"__dataset\", sdi.paramid, sdi.paramtype, sdi.displayunits, sdi.datatypes, sdi.displayformat, sdi.entryreftypeid, sdi.entrysdcid, sdi.transformrule, sdi.calcrule, sdi.displaywidth, sdi.usersequence, sdi.numreplicates, 1 AS replicateid, sdi.defaultvalue, 'N' AS releasedflag, sdi.editstyleflag, sdi.weblookupurl, sdi.extendedsql FROM paramlistitem sdi WHERE sdi.paramlistid=? AND sdi.paramlistversionid=? AND sdi.variantid=? ";
        Object[] params = new String[]{paramlistid, paramlistversionid, variantid};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sqlSdidataitem, params);
        this.appendDatasetToAnother(addToDataset, ds);
    }

    private void appendDatasetToAnother(DataSet targetDataset, DataSet sourceDataset) {
        if (sourceDataset != null) {
            for (int sourceRow = 0; sourceRow < sourceDataset.getRowCount(); ++sourceRow) {
                int targetRow = targetDataset.addRow();
                for (int col = 0; col < sourceDataset.getColumnCount(); ++col) {
                    String columnId = sourceDataset.getColumnId(col);
                    int columnType = sourceDataset.getColumnType(columnId);
                    if (columnType == 0) {
                        targetDataset.setString(targetRow, columnId, sourceDataset.getString(sourceRow, columnId));
                        continue;
                    }
                    if (columnType == 1) {
                        targetDataset.setNumber(targetRow, columnId, sourceDataset.getBigDecimal(sourceRow, columnId));
                        continue;
                    }
                    if (columnType != 2) continue;
                    targetDataset.setDate(targetRow, columnId, sourceDataset.getCalendar(sourceRow, columnId));
                }
            }
        }
    }

    private void duplicateReplicateRows() {
        int originalRowCount = this.sdidataitem.getRowCount();
        for (int i = 0; i < originalRowCount; ++i) {
            int numReplicates = this.sdidataitem.getInt(i, "numreplicates", 1);
            if (numReplicates <= 1) continue;
            for (int j = 1; j < numReplicates; ++j) {
                int newrow = this.sdidataitem.addRow();
                this.sdidataitem.setNumber(newrow, "replicateid", j + 1);
                for (int k = 0; k < this.sdidataitem.getColumnCount(); ++k) {
                    String columnid = this.sdidataitem.getColumnId(k);
                    if (columnid.equals("replicateid")) continue;
                    int columntype = this.sdidataitem.getColumnType(columnid);
                    if (columntype == 0) {
                        this.sdidataitem.setString(newrow, columnid, this.sdidataitem.getString(i, columnid));
                        continue;
                    }
                    if (columntype != 1) continue;
                    this.sdidataitem.setNumber(newrow, columnid, this.sdidataitem.getBigDecimal(i, columnid));
                }
            }
        }
    }
}


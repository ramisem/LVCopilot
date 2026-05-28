/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;

public class SdcInfo {
    public static String getTableId(String sdcid, QueryProcessor queryprocessor) {
        String tableId = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT TABLEID FROM SDC WHERE SDCID = ").append("?").append("");
        DataSet dataset = queryprocessor.getPreparedSqlDataSet(sql.toString(), new Object[]{sdcid});
        if (dataset.getRowCount() > 0) {
            tableId = dataset.getValue(0, "tableid");
        }
        return tableId;
    }

    public static String getSdcType(String sdcid, QueryProcessor queryprocessor) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SDCTYPE FROM SDC WHERE SDCID = ").append("?").append("");
        DataSet dataset = queryprocessor.getPreparedSqlDataSet(sql.toString(), new Object[]{sdcid});
        if (dataset.getRowCount() > 0) {
            return dataset.getValue(0, "SDCTYPE");
        }
        return "";
    }

    public static ArrayList getPrimaryKeys(String sdcid, QueryProcessor queryprocessor) {
        StringBuffer sql = new StringBuffer();
        ArrayList<String> keys = new ArrayList<String>();
        sql.append("SELECT T1.COLUMNID FROM SYSREFCOLUMN T1 WHERE T1.REFID = ").append(" (SELECT T2.REFID FROM SYSREF T2 WHERE T2.TABLEID = ").append(" (SELECT T3.TABLEID FROM SDC T3 WHERE T3.SDCID = ").append("?").append(") AND T2.REFTYPEFLAG = 'P')").append(" ORDER BY T1.COLUMNSEQUENCE");
        DataSet dataset = queryprocessor.getPreparedSqlDataSet(sql.toString(), new Object[]{sdcid});
        for (int i = 0; i < dataset.getRowCount(); ++i) {
            keys.add(i, dataset.getValue(i, "columnid"));
        }
        return keys;
    }

    public static boolean isAccessControlled(String sdcid, QueryProcessor queryprocessor) {
        DataSet ds = queryprocessor.getPreparedSqlDataSet("SELECT ACCESSCONTROLLEDFLAG FROM SDC WHERE SDCID = ?", new Object[]{sdcid});
        if (ds.getRowCount() > 0) {
            return ds.getValue(0, "accesscontrolledflag").equals("Y");
        }
        return false;
    }

    public static boolean isTemplatable(String sdcid, QueryProcessor queryprocessor) {
        DataSet ds = queryprocessor.getPreparedSqlDataSet("SELECT TEMPLATABLEFLAG FROM SDC WHERE SDCID = ?", new Object[]{sdcid});
        if (ds.getRowCount() > 0) {
            return ds.getValue(0, "TEMPLATABLEFLAG").equals("Y");
        }
        return false;
    }

    public static int getSDICount(String keycolid1, String tableid, QueryProcessor queryprocessor, boolean withtemplates) {
        StringBuffer sql = new StringBuffer();
        if (withtemplates) {
            sql.append("SELECT COUNT(").append(keycolid1).append(") SDICOUNT FROM ").append(tableid).append(" WHERE TEMPLATEFLAG NOT IN ( 'Y' )");
        } else {
            sql.append("SELECT COUNT(").append(keycolid1).append(") SDICOUNT FROM ").append(tableid);
        }
        DataSet ds = queryprocessor.getSqlDataSet(sql.toString());
        return ds.getInt(0, "SDICOUNT");
    }

    public static String getKeyidColumn(int seq, String sdcId, QueryProcessor qp) {
        String columnId = "";
        ArrayList alKeys = SdcInfo.getPrimaryKeys(sdcId, qp);
        for (int i = 0; i < alKeys.size(); ++i) {
            if (i != seq - 1) continue;
            columnId = (String)alKeys.get(i);
        }
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getKeyidColumn, sdcid=" + sdcId + " , GOT columnId=" + columnId);
        return columnId;
    }

    public static String getColumnNameInPrimaryForLinkedSdc(String sdcId, String linkedSdc, QueryProcessor qp) {
        String columnName = "";
        String sql = "";
        DataSet ds = new DataSet();
        sql = "select sdccolumnid from sdclink where sdcid=? and linksdcid=? and linktype='F'";
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getColumnNameInPrimaryForLinkedSdc, Get columnName sql=" + sql);
        ds = qp.getPreparedSqlDataSet(sql, new Object[]{sdcId, linkedSdc});
        if (ds.getRowCount() >= 1) {
            columnName = ds.getValue(0, "sdccolumnid");
        }
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getColumnNameInPrimaryForLinkedSdc, Got columnname=" + columnName);
        ds = null;
        return columnName;
    }

    public static String getMaintFormForSdi(String sdcId, String keyid1, String keyid2, String keyid3, String pageType, boolean useSdcDefaultIfNotFound, QueryProcessor qp) {
        DataSet dsSdiForm = new DataSet();
        String webPage = "";
        webPage = "wzDfltSmplTmpltMaint";
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getMaintFormForSdi, Got sdi form=" + webPage);
        dsSdiForm = null;
        return webPage;
    }

    public static String getDefaultMaintPageForSdc(String sdcId, String type, QueryProcessor qp) {
        String maintPage = "";
        DataSet dsSdcForm = new DataSet();
        maintPage = "wzDfltSmplTmpltMaint";
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getDefaultMaintPageForSdc, Got default sdc form=" + maintPage);
        dsSdcForm = null;
        return maintPage;
    }

    public static String getDefaultMaintPageForSdc(String sdcId, QueryProcessor qp) {
        String type = "MaintWithoutLayout";
        return SdcInfo.getDefaultMaintPageForSdc(sdcId, type, qp);
    }

    public static String getTemplateStringForSdc(String sdcId, PageContext pageContext) {
        String templates = "";
        QueryProcessor qp = new QueryProcessor(pageContext);
        HashMap<String, Object> hmSdi = new HashMap<String, Object>();
        String tableId = SdcInfo.getTableId(sdcId, qp);
        hmSdi.put("sdcid", sdcId);
        hmSdi.put("queryfrom", tableId);
        String queryWhere = "templateflag = 'Y'";
        if (sdcId.equals("Sample")) {
            queryWhere = queryWhere + " AND (scheduletemplateflag = 'N' OR scheduletemplateflag IS NULL)";
        }
        hmSdi.put("querywhere", queryWhere);
        hmSdi.put("request", "primary");
        hmSdi.put("showtemplates", new Boolean(true));
        String orderBy = SdcInfo.getOrderByClause(sdcId, pageContext);
        if (orderBy.length() > 0) {
            hmSdi.put("queryorderby", orderBy.toString());
        }
        templates = SdcInfo.getTemplateString(sdcId, hmSdi, pageContext);
        qp = null;
        return templates;
    }

    public static String getTemplateStringForSdc(String sdcId, PageContext pageContext, String restrictiveWhere) {
        String templates = "";
        QueryProcessor qp = new QueryProcessor(pageContext);
        HashMap<String, Object> hmSdi = new HashMap<String, Object>();
        String tableId = SdcInfo.getTableId(sdcId, qp);
        hmSdi.put("sdcid", sdcId);
        hmSdi.put("queryfrom", tableId);
        String queryWhere = "templateflag = 'Y'";
        if (sdcId.equals("Sample")) {
            queryWhere = queryWhere + " AND (scheduletemplateflag = 'N' OR scheduletemplateflag IS NULL)";
        }
        if (restrictiveWhere != null && !restrictiveWhere.trim().isEmpty()) {
            queryWhere = queryWhere + " AND " + restrictiveWhere;
        }
        hmSdi.put("querywhere", queryWhere);
        hmSdi.put("request", "primary");
        hmSdi.put("showtemplates", new Boolean(true));
        String orderBy = SdcInfo.getOrderByClause(sdcId, pageContext);
        if (orderBy.length() > 0) {
            hmSdi.put("queryorderby", orderBy.toString());
        }
        templates = SdcInfo.getTemplateString(sdcId, hmSdi, pageContext);
        qp = null;
        return templates;
    }

    public static String getTemplateStringForSdc(String sdcId, String queryid, PageContext pageContext) {
        String templates = "";
        HashMap<String, Object> hmSdi = new HashMap<String, Object>();
        hmSdi.put("sdcid", sdcId);
        if (!queryid.equalsIgnoreCase("")) {
            hmSdi.put("queryid", queryid);
        }
        hmSdi.put("request", "primary");
        hmSdi.put("showtemplates", new Boolean(true));
        templates = SdcInfo.getTemplateString(sdcId, hmSdi, pageContext);
        return templates;
    }

    public static String getTemplateStringForSdc(String sdcId, String keyid1, String keyid2, String keyid3, String queryfrom, String querywhere, String queryorderby, PageContext pageContext) {
        QueryProcessor qp = new QueryProcessor(pageContext);
        HashMap<String, Object> hmSdi = new HashMap<String, Object>();
        String tableId = SdcInfo.getTableId(sdcId, qp);
        String orderby = SdcInfo.getOrderByClause(sdcId, pageContext);
        hmSdi.put("sdcid", sdcId);
        if (!queryfrom.equalsIgnoreCase("")) {
            hmSdi.put("queryfrom", tableId + ", " + queryfrom);
        } else {
            hmSdi.put("queryfrom", tableId);
        }
        if (OpalUtil.isNotEmpty(querywhere)) {
            if (querywhere.contains("[sdcid]")) {
                querywhere = StringUtil.replaceAll(querywhere, "[sdcid]", sdcId);
            }
            if (querywhere.contains("[keyid1]")) {
                querywhere = StringUtil.replaceAll(querywhere, "[keyid1]", StringUtil.replaceAll(keyid1, ";", "','"));
            }
            if (querywhere.contains("[keyid2]")) {
                querywhere = StringUtil.replaceAll(querywhere, "[keyid2]", StringUtil.replaceAll(keyid2, ";", "','"));
            }
            if (querywhere.contains("[keyid3]")) {
                querywhere = StringUtil.replaceAll(querywhere, "[keyid3]", StringUtil.replaceAll(keyid3, ";", "','"));
            }
            hmSdi.put("querywhere", querywhere + " and templateflag='Y'");
        } else {
            hmSdi.put("querywhere", "templateflag='Y'");
        }
        if (!queryorderby.equalsIgnoreCase("")) {
            hmSdi.put("queryorderby", orderby + ", " + queryorderby);
        } else {
            hmSdi.put("queryorderby", orderby);
        }
        hmSdi.put("request", "primary");
        hmSdi.put("showtemplates", Boolean.TRUE);
        return SdcInfo.getTemplateString(sdcId, hmSdi, pageContext);
    }

    public static String getTemplateString(String sdcId, HashMap hmSdi, PageContext pageContext) {
        String templates = "";
        DataSet ds = new DataSet();
        QueryProcessor qp = new QueryProcessor(pageContext);
        ds = SdcInfo.getDataSetObjectForSdi(hmSdi, pageContext);
        int keyColumns = Integer.parseInt(SdcInfo.getSDCProps(sdcId, pageContext, "keycolumns"));
        String columnId1 = "";
        String columnId2 = "";
        String columnId3 = "";
        columnId1 = SdcInfo.getKeyidColumn(1, sdcId, qp);
        if (keyColumns > 1) {
            columnId2 = SdcInfo.getKeyidColumn(2, sdcId, qp);
        }
        if (keyColumns > 2) {
            columnId3 = SdcInfo.getKeyidColumn(3, sdcId, qp);
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            StringBuffer template = new StringBuffer();
            template.append(ds.getValue(i, columnId1));
            if (keyColumns > 1) {
                template.append("|").append(ds.getValue(i, columnId2));
            }
            if (keyColumns > 2) {
                template.append("|").append(ds.getValue(i, columnId3));
            }
            templates = templates + ";" + template.toString();
        }
        if (templates.length() > 0) {
            templates = templates.substring(1);
        }
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getTemplateString, Got Templates=" + templates);
        ds = null;
        qp = null;
        return templates;
    }

    public static DataSet getDataSetObjectForSdi(HashMap hmSdiTagAttributes, PageContext pageContext) {
        DataSet dsSdiData = new DataSet();
        SDIProcessor sdiProc = new SDIProcessor(pageContext);
        try {
            String sdcId = "";
            String keyid1 = "";
            String queryfrom = "";
            String querywhere = "";
            String queryorderby = "";
            String queryid = "";
            String param1 = "";
            String param2 = "";
            String param3 = "";
            String param4 = "";
            String param5 = "";
            String param6 = "";
            String param7 = "";
            String param8 = "";
            String param9 = "";
            String param10 = "";
            String param11 = "";
            String param12 = "";
            String paramlistidlist = "";
            String paramlistversionidlist = "";
            String variantidlist = "";
            String datasetlist = "";
            String request = "";
            boolean blnShowTemplates = true;
            if (hmSdiTagAttributes.get("sdcid") != null) {
                sdcId = (String)hmSdiTagAttributes.get("sdcid");
            }
            if (hmSdiTagAttributes.get("keyid1") != null) {
                keyid1 = (String)hmSdiTagAttributes.get("keyid1");
            }
            if (hmSdiTagAttributes.get("queryfrom") != null) {
                queryfrom = (String)hmSdiTagAttributes.get("queryfrom");
            }
            if (hmSdiTagAttributes.get("querywhere") != null) {
                querywhere = (String)hmSdiTagAttributes.get("querywhere");
            }
            if (hmSdiTagAttributes.get("queryorderby") != null) {
                queryorderby = (String)hmSdiTagAttributes.get("queryorderby");
            }
            if (hmSdiTagAttributes.get("queryid") != null) {
                queryid = (String)hmSdiTagAttributes.get("queryid");
            }
            if (hmSdiTagAttributes.get("param1") != null) {
                param1 = (String)hmSdiTagAttributes.get("param1");
            }
            if (hmSdiTagAttributes.get("param2") != null) {
                param2 = (String)hmSdiTagAttributes.get("param2");
            }
            if (hmSdiTagAttributes.get("param3") != null) {
                param3 = (String)hmSdiTagAttributes.get("param3");
            }
            if (hmSdiTagAttributes.get("param4") != null) {
                param4 = (String)hmSdiTagAttributes.get("param4");
            }
            if (hmSdiTagAttributes.get("param5") != null) {
                param5 = (String)hmSdiTagAttributes.get("param5");
            }
            if (hmSdiTagAttributes.get("param6") != null) {
                param6 = (String)hmSdiTagAttributes.get("param6");
            }
            if (hmSdiTagAttributes.get("param7") != null) {
                param7 = (String)hmSdiTagAttributes.get("param7");
            }
            if (hmSdiTagAttributes.get("param8") != null) {
                param8 = (String)hmSdiTagAttributes.get("param8");
            }
            if (hmSdiTagAttributes.get("param9") != null) {
                param9 = (String)hmSdiTagAttributes.get("param9");
            }
            if (hmSdiTagAttributes.get("param10") != null) {
                param10 = (String)hmSdiTagAttributes.get("param10");
            }
            if (hmSdiTagAttributes.get("param11") != null) {
                param11 = (String)hmSdiTagAttributes.get("param11");
            }
            if (hmSdiTagAttributes.get("param12") != null) {
                param12 = (String)hmSdiTagAttributes.get("param12");
            }
            if (hmSdiTagAttributes.get("paramlistidlist") != null) {
                paramlistidlist = (String)hmSdiTagAttributes.get("paramlistidlist");
            }
            if (hmSdiTagAttributes.get("paramlistversionidlist") != null) {
                paramlistversionidlist = (String)hmSdiTagAttributes.get("paramlistversionidlist");
            }
            if (hmSdiTagAttributes.get("variantidlist") != null) {
                variantidlist = (String)hmSdiTagAttributes.get("variantidlist");
            }
            if (hmSdiTagAttributes.get("datasetlist") != null) {
                datasetlist = (String)hmSdiTagAttributes.get("datasetlist");
            }
            if (hmSdiTagAttributes.get("request") != null) {
                request = (String)hmSdiTagAttributes.get("request");
            }
            if (hmSdiTagAttributes.get("showtemplates") != null) {
                blnShowTemplates = (Boolean)hmSdiTagAttributes.get("showtemplates");
            }
            if (request.equalsIgnoreCase("")) {
                request = "primary";
            }
            if (queryfrom.equalsIgnoreCase("")) {
                // empty if block
            }
            String[] arrparams = new String[]{param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12};
            SDIRequest sdiReq = new SDIRequest();
            sdiReq.setRequestItem(request);
            sdiReq.setSDCid(sdcId);
            sdiReq.setQueryid(queryid);
            sdiReq.setQueryFrom(queryfrom);
            sdiReq.setQueryWhere(querywhere);
            sdiReq.setQueryParams(arrparams);
            sdiReq.setQueryOrderBy(queryorderby);
            sdiReq.setKeyid1List(keyid1);
            sdiReq.setParamlistidList(paramlistidlist);
            sdiReq.setParamlistversionidList(paramlistversionidlist);
            sdiReq.setVariantidList(variantidlist);
            sdiReq.setDatasetList(datasetlist);
            sdiReq.setShowTemplates(blnShowTemplates);
            SDIData sdiData = sdiProc.getSDIData(sdiReq);
            dsSdiData = sdiData.getDataset(request);
            Trace.logDebug("OPAL_INFO: Inside SdcInfo.getDataSetObjectForSdi, Sdi tag attribute values hashmap :" + hmSdiTagAttributes + ",  showTemplates=" + blnShowTemplates + ",  request=" + request);
            Trace.logDebug("OPAL_INFO: Inside SdcInfo.getDataSetObjectForSdi, Got the requested dataset :" + dsSdiData);
        }
        catch (Exception ex) {
            Trace.logError("OPAL_ERR: Inside SdcInfo.getDataSetObjectForSdi, Got an error. Message:" + ex + sdiProc.getErrorStack(), ex);
        }
        return dsSdiData;
    }

    public static HashMap getMaintFormsForTemplates(String sdcId, PageContext pageContext) {
        HashMap<String, String> hmTemplateForms = new HashMap<String, String>();
        QueryProcessor qp = new QueryProcessor(pageContext);
        String templates = SdcInfo.getTemplateStringForSdc(sdcId, pageContext);
        String[] arrTemplates = StringUtil.split(templates, ";");
        for (int i = 0; i < arrTemplates.length; ++i) {
            hmTemplateForms.put(arrTemplates[i], SdcInfo.getMaintFormForSdi(sdcId, arrTemplates[i], "", "", "Template", true, qp));
        }
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getMaintFormsForTemplates, Got template forms=" + hmTemplateForms);
        return hmTemplateForms;
    }

    public static String getLinkedColumnIdByLinkId(String sdcId, String linkId, QueryProcessor qp) {
        String columnName = "";
        String sql = "";
        DataSet ds = new DataSet();
        sql = "select sdccolumnid from sdclink where sdcid=? and linkid=?";
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getLinkedColumnIdByLinkId, Get columnName sql=" + sql);
        ds = qp.getPreparedSqlDataSet(sql, new Object[]{sdcId, linkId});
        if (ds.getRowCount() >= 1) {
            columnName = ds.getValue(0, "sdccolumnid");
        }
        Trace.logDebug("OPAL_INFO: Inside SdcInfo.getLinkedColumnIdByLinkId, Got columnname=" + columnName);
        ds = null;
        return columnName;
    }

    public static boolean isCOCAble(String sdcid, QueryProcessor qp) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT NVL(COCABLEFLAG, 'N') COCABLEFLAG FROM SDC");
        sb.append(" WHERE SDCID = ");
        sb.append("?");
        sb.append("");
        DataSet ds = qp.getPreparedSqlDataSet(sb.toString(), new Object[]{sdcid});
        if (ds.getRowCount() > 0) {
            return ds.getValue(0, "COCABLEFLAG").equals("Y");
        }
        return false;
    }

    public static String getColumnDataType(String sdcid, String columnid, QueryProcessor qp) {
        StringBuffer sb = new StringBuffer();
        String columnDataType = null;
        sb.append("SELECT COLUMNID, DATATYPE, COLUMNLENGTH FROM SYSCOLUMN");
        sb.append(" WHERE TABLEID = (SELECT TABLEID FROM SDC WHERE SDCID = ");
        sb.append("?");
        sb.append(" AND COLUMNID = ");
        sb.append("?");
        sb.append(")");
        DataSet ds = qp.getPreparedSqlDataSet(sb.toString(), new Object[]{sdcid, columnid});
        if (ds.getRowCount() > 0) {
            columnDataType = ds.getValue(0, "DATATYPE");
        }
        return columnDataType;
    }

    public static String getSDCProps(String sdcId, PageContext pageContext, String propertyName) {
        SDCProcessor sdcProcessor = new SDCProcessor(pageContext);
        return sdcProcessor.getProperty(sdcId, propertyName, "");
    }

    private static String getOrderByClause(String sdcId, PageContext pageContext) {
        int keyColumn = Integer.parseInt(SdcInfo.getSDCProps(sdcId, pageContext, "keycolumns"));
        QueryProcessor qp = new QueryProcessor(pageContext);
        StringBuffer orderBy = new StringBuffer();
        String tableId = SdcInfo.getSDCProps(sdcId, pageContext, "tableid");
        orderBy.append(tableId).append(".").append(SdcInfo.getSDCProps(sdcId, pageContext, "keycolid1"));
        if (keyColumn > 1) {
            orderBy.append(", ").append(tableId).append(".").append(SdcInfo.getSDCProps(sdcId, pageContext, "keycolid2"));
        }
        if (keyColumn > 2) {
            orderBy.append(", ").append(tableId).append(".").append(SdcInfo.getSDCProps(sdcId, pageContext, "keycolid3"));
        }
        return orderBy.toString();
    }
}


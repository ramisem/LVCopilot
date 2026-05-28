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

import com.labvantage.sapphire.I18nUtil;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import sapphire.action.ActionConstants;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LoadSDI
extends BaseAjaxRequest
implements ActionConstants {
    private Set<String> excludedFields = new HashSet<String>(Arrays.asList("createdt", "createby", "createtool", "moddt", "modby", "modtool", "auditsequence", "auditdeferflag", "tracelogid", "templateflag"));
    private StringBuilder rsetids = new StringBuilder();
    private boolean loadTemplate = false;
    private HashMap<String, PropertyList> sdcCache = new HashMap();

    private PropertyList getSDCProps(String sdcId) {
        PropertyList sdcProps = this.sdcCache.get(sdcId);
        if (sdcProps == null) {
            sdcProps = this.getSDCProcessor().getProperties(sdcId);
            this.sdcCache.put(sdcId, sdcProps);
        }
        return sdcProps;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        String title;
        String dataSource;
        AjaxResponse ar = new AjaxResponse(req, resp);
        long startTime = System.currentTimeMillis();
        String sdcId = ar.getRequestParameter("sdcid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "");
        String keyid2 = ar.getRequestParameter("keyid2", "");
        String keyid3 = ar.getRequestParameter("keyid3", "");
        String secondaryKeyId1 = ar.getRequestParameter("secondarykeyid1", "");
        String secondaryKeyId2 = ar.getRequestParameter("secondarykeyid2", "");
        String secondaryKeyId3 = ar.getRequestParameter("secondarykeyid3", "");
        String mode = ar.getRequestParameter("mode", "edit");
        String templateId = ar.getRequestParameter("templateid", "");
        String pageId = ar.getRequestParameter("pageid", "");
        String sRequestProps = ar.getRequestParameter("requestdata");
        ReturnCode returnCode = ReturnCode.OK;
        String lockedBy = "";
        ExtraColumnUtil extraColumnUtil = new ExtraColumnUtil(this.getQueryProcessor(), this.logger);
        HttpSession session = req.getSession(true);
        PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
        if (pageProps == null) {
            returnCode = ReturnCode.ERROR;
            ar.addCallbackArgument("formdata", "");
            ar.addCallbackArgument("returncode", (Object)returnCode);
            ar.addCallbackArgument("lockedby", lockedBy);
            ar.addCallbackArgument("rsetid", this.rsetids);
            ar.print();
            return;
        }
        String parentSdcId = pageProps.getPropertyListNotNull("pagetype").getPropertyListNotNull("parent").getProperty("parentsdcid", "");
        String parentLinkId = pageProps.getPropertyListNotNull("pagetype").getPropertyListNotNull("parent").getProperty("parentlinkid", "");
        boolean sdcIsVersioned = this.getSDCProps(sdcId).getProperty("versionedflag", "N").equals("Y");
        Set<String> detailDatasets = this.getDetailDatasets(pageProps);
        Map<String, List<String>> extraColumns = extraColumnUtil.parseExtraColumns(pageProps);
        PropertyList formData = new PropertyList();
        DataSet parent = null;
        if (!templateId.equals("")) {
            keyid1 = templateId;
            this.loadTemplate = true;
        }
        if (sdcId.equals("") || keyid1.equals("")) {
            ar.addCallbackArgument("formdata", new JSONObject());
            ar.addCallbackArgument("returncode", "ERROR");
            ar.addCallbackArgument("lockedby", "");
            ar.addCallbackArgument("rsetid", "");
            ar.print();
            return;
        }
        if (keyid2.equalsIgnoreCase("C") && sdcIsVersioned) {
            keyid2 = Utils.getLatestVersion(keyid1, this.getSDCProps(sdcId), this.getQueryProcessor(), this.getConnectionProcessor().isOra());
        }
        SDIRequest r = this.createSdiRequest(sdcId, keyid1, keyid2, keyid3, mode, detailDatasets);
        SDIData sdi = this.getSDIProcessor().getSDIData(r);
        DataSet primary = sdi.getDataset("primary");
        DataSet sdidata = sdi.getDataset("dataset");
        DataSet sdidataitem = sdi.getDataset("dataitem");
        DataSet sdidataapproval = sdi.getDataset("dataapproval");
        DataSet sdispec = sdi.getDataset("sdispec");
        DataSet sdiworkitem = sdi.getDataset("sdiworkitem");
        DataSet sdiattachment = sdi.getDataset("attachment");
        DataSet sdirole = sdi.getDataset("role");
        DataSet category = sdi.getDataset("category");
        DataSet sdiaddress = sdi.getDataset("address");
        DataSet attribute = sdi.getDataset("attribute");
        if (sdcIsVersioned) {
            this.fillMaxVersionId(sdcId, primary);
        }
        String primaryRsetid = sdi.getRsetid();
        this.rsetids.append(primaryRsetid);
        if (primary == null) {
            primary = new DataSet();
            returnCode = ReturnCode.ERROR;
        } else if (primary.getRowCount() == 0) {
            returnCode = ReturnCode.NOTFOUND;
        }
        FilterUtil filterUtil = new FilterUtil(pageProps);
        filterUtil.filterDataset(sdiworkitem, "sdiworkitem");
        filterUtil.filterDataset(sdidata, "sdidata");
        filterUtil.filterDataset(sdidataitem, "sdidataitem");
        Map<String, List<String>> translateColumns = Utils.getTranslateColumns(pageProps);
        for (String string : detailDatasets) {
            void var48_54;
            if (string.startsWith("sdclink:") || string.startsWith("reversefk:")) {
                dataSource = string.substring(string.indexOf("###") + 3);
                String string2 = string.substring(0, string.indexOf("###"));
                List<String> extracolumnList = extraColumns.get(dataSource);
                DataSet dataSet = this.getLinkData(sdcId, keyid1, keyid2, keyid3, string2, mode.equals("edit"), extracolumnList);
                if (dataSet != null) {
                    filterUtil.filterDataset(dataSet, dataSource);
                    if (dataSet.getColumnType("usersequence") == 1) {
                        dataSet.sort("usersequence");
                    }
                    Utils.translateData(dataSet, translateColumns.get(dataSource), this.getConnectionProcessor().getLanguage(), sdcId, this.getTranslationProcessor());
                }
            } else if (string.startsWith("sql:")) {
                dataSource = string.substring(string.indexOf("###") + 3);
                String sql = string.substring(4, string.indexOf("###"));
                sql = this.addKeysToSql(sql, sdcId, keyid1, keyid2, keyid3);
                DataSet dataSet = this.getQueryProcessor().getSqlDataSet(sql);
                Utils.translateData(dataSet, translateColumns.get(dataSource), this.getConnectionProcessor().getLanguage(), "W", this.getTranslationProcessor());
            } else {
                if (!string.startsWith("sdicontrolcard")) continue;
                dataSource = "sdicontrolcard";
                String elementId = string.substring(15);
                StringBuilder sExtraCols = new StringBuilder();
                List<String> extraColumnList = extraColumns.get(elementId);
                for (String s : extraColumnList) {
                    sExtraCols.append(",").append(s);
                }
                String sql = "SELECT spc_sdicontrolcard.* " + sExtraCols + " FROM spc_sdicontrolcard WHERE sdc=? and key1=? ";
                ArrayList<String> params = new ArrayList<String>();
                params.add(sdcId);
                params.add(keyid1);
                if (!keyid2.equals("")) {
                    sql = sql + "and key2=? ";
                    params.add(keyid2);
                }
                if (!keyid3.equals("")) {
                    sql = sql + "and key3=? ";
                    params.add(keyid3);
                }
                DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])params.toArray(new String[params.size()]));
                Utils.translateData(dataSet, translateColumns.get(dataSource), this.getConnectionProcessor().getLanguage(), "SPC_ControlCard", this.getTranslationProcessor());
            }
            if (var48_54 == null) continue;
            this.addFormDataItem(formData, (DataSet)var48_54, dataSource);
        }
        boolean hasWarnings = false;
        for (Map.Entry<String, List<String>> entry : extraColumns.entrySet()) {
            DataSet ds;
            String[] keyColumns;
            dataSource = entry.getKey();
            List<String> extraColumnList = entry.getValue();
            if (dataSource.equals("primary")) {
                keyColumns = this.getKeyColumnsBySdc(sdcId);
                if (this.fillPrimaryExtraColumnData(extraColumnList, sdcId, keyid1, keyid2, keyid3, primary, keyColumns)) continue;
                hasWarnings = true;
                continue;
            }
            if (!dataSource.equals("sdiworkitem") && !dataSource.equals("sdidata") && !dataSource.equals("sdidataitem") && !dataSource.equals("sdiaddress")) continue;
            keyColumns = this.getKeyColumns(dataSource);
            HashMap<String, String> searchValues = new HashMap<String, String>();
            searchValues.put("sdcid", sdcId);
            searchValues.put("keyid1", keyid1);
            searchValues.put("keyid2", keyid2);
            searchValues.put("keyid3", keyid3);
            if (dataSource.equals("sdiworkitem")) {
                ds = sdiworkitem;
            } else if (dataSource.equals("sdidata")) {
                ds = sdidata;
            } else if (dataSource.equals("sdidataitem")) {
                ds = sdidataitem;
            } else {
                if (!dataSource.equals("sdiaddress")) continue;
                ds = sdiaddress;
            }
            extraColumnUtil.fillExtraColumnData(extraColumnList, searchValues, dataSource, ds, keyColumns);
        }
        if (primary.getRowCount() > 0 && !parentSdcId.equals("")) {
            parent = this.createParentDataset(sdcId, parentSdcId, parentLinkId, primary, mode);
        }
        if (sdiworkitem != null) {
            sdiworkitem.sort("keyid1,usersequence");
        }
        if (sdidata != null) {
            sdidata.sort("keyid1,usersequence");
        }
        if (sdidataitem != null) {
            sdidataitem.sort("keyid1,__sdidata_usersequence,usersequence,replicateid");
        }
        if (sdiaddress != null) {
            sdiaddress.sort("keyid1,usersequence");
        }
        if (sdidataitem != null && sdispec != null && sdispec.getRowCount() > 0) {
            String string = sdispec.getString(0, "specid", "");
            String string3 = sdispec.getString(0, "specversionid", "");
            new SpecLimitHandler(this.getQueryProcessor()).injectSpecLimits(sdidataitem, string, string3);
        }
        HashMap<Object[], Integer> hashMap = new HashMap<Object[], Integer>();
        HashMap<Object[], String> hashMap2 = new HashMap<Object[], String>();
        HashMap<Object[], String> webLookupUrls = new HashMap<Object[], String>();
        HashMap<Object[], String> extendedSQLs = new HashMap<Object[], String>();
        if (sdidataitem != null) {
            sdidataitem.addColumn("displaywidth", 1);
            sdidataitem.addColumn("editstyleflag", 0);
            sdidataitem.addColumn("weblookupurl", 0);
            sdidataitem.addColumn("extendedsql", 0);
            for (int i = 0; i < sdidataitem.getRowCount(); ++i) {
                String paramlistid = sdidataitem.getString(i, "paramlistid");
                String paramlistversionid = sdidataitem.getString(i, "paramlistversionid");
                String variantid = sdidataitem.getString(i, "variantid");
                String paramid = sdidataitem.getString(i, "paramid");
                String paramtype = sdidataitem.getString(i, "paramtype");
                Object[] params = new String[]{paramlistid, paramlistversionid, variantid, paramid, paramtype};
                Integer displayWidth = (Integer)hashMap.get(params);
                String editStyleFlag = (String)hashMap2.get(params);
                String webLookupUrl = (String)webLookupUrls.get(params);
                String extendedSQL = (String)extendedSQLs.get(params);
                if (!hashMap.containsKey(params)) {
                    String sql = "SELECT displaywidth, editstyleflag, weblookupurl, extendedsql FROM paramlistitem WHERE paramlistid=? and paramlistversionid=? and variantid=? and paramid=? and paramtype=?";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                    displayWidth = ds.getInt(0, "displaywidth", 0);
                    hashMap.put(params, displayWidth);
                    editStyleFlag = ds.getString(0, "editstyleflag", "");
                    hashMap2.put(params, editStyleFlag);
                    webLookupUrl = ds.getString(0, "weblookupurl", "");
                    webLookupUrls.put(params, webLookupUrl);
                    extendedSQL = ds.getString(0, "extendedsql", "");
                    extendedSQLs.put(params, extendedSQL);
                }
                if (displayWidth != 0) {
                    sdidataitem.setNumber(i, "displaywidth", displayWidth);
                }
                if (!editStyleFlag.equals("")) {
                    sdidataitem.setString(i, "editstyleflag", editStyleFlag);
                }
                if (!webLookupUrl.equals("")) {
                    sdidataitem.setString(i, "weblookupurl", webLookupUrl);
                }
                if (extendedSQL.equals("")) continue;
                sdidataitem.setString(i, "extendedsql", extendedSQL);
            }
        }
        if (sdidataitem != null) {
            I18nUtil.localizeDisplayValues(sdidataitem, this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            String userLocaleStr = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getLocale();
            if (userLocaleStr != null) {
                Locale userLocale;
                if (userLocaleStr.contains("_")) {
                    String country = userLocaleStr.substring(0, userLocaleStr.indexOf(95));
                    String language = userLocaleStr.substring(userLocaleStr.indexOf(95) + 1);
                    userLocale = new Locale(country, language);
                } else {
                    userLocale = new Locale(userLocaleStr);
                }
                M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                for (int i = 0; i < sdidataitem.getRowCount(); ++i) {
                    String uncertaintyDisplayValue = sdidataitem.getValue(i, "uncertaintydisplayvalue", "");
                    BigDecimal uncertaintyValue = sdidataitem.getBigDecimal(i, "uncertaintyvalue");
                    String uncertaintyDisplayFormat = sdidataitem.getValue(i, "uncertaintydisplayformat", "");
                    String formattedValue = I18nUtil.formatDataEntryDisplay(uncertaintyDisplayValue, "N", uncertaintyValue, null, uncertaintyDisplayFormat, null, userLocale, null, m18NUtil);
                    sdidataitem.setValue(i, "uncertaintydisplayvalue", formattedValue);
                }
            }
            HashMap<String, String> refDisplayValues = new HashMap<String, String>();
            for (int i = 0; i < sdidataitem.getRowCount(); ++i) {
                String dataTypes = sdidataitem.getValue(i, "datatypes", "");
                String enteredText = sdidataitem.getValue(i, "enteredtext", "");
                String refTypeId = sdidataitem.getValue(i, "entryreftypeid", "");
                if (!dataTypes.equals("V") || enteredText.equals("") || refTypeId.equals("")) continue;
                String displayValue = (String)refDisplayValues.get(refTypeId + ";" + enteredText);
                if (displayValue == null) {
                    String sql = "SELECT refdisplayvalue FROM refvalue WHERE ( activeflag='Y' OR activeflag is null) AND reftypeid=? AND refvalueid=?";
                    Object[] params = new String[]{refTypeId, enteredText};
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                    displayValue = ds.getString(0, "refdisplayvalue", "");
                    refDisplayValues.put(refTypeId + ";" + enteredText, displayValue);
                }
                sdidataitem.setString(i, "displayvalue", displayValue);
            }
        }
        if (!templateId.equals("")) {
            this.handleTemplateData(templateId, sdcId, parentSdcId, parentLinkId, primary, parent, sdiworkitem, sdidata, sdidataitem, sdispec, sdiaddress);
        }
        Utils.translateData(primary, translateColumns.get("primary"), this.getConnectionProcessor().getLanguage(), sdcId, this.getTranslationProcessor());
        Utils.translateData(parent, translateColumns.get("parent"), this.getConnectionProcessor().getLanguage(), parentSdcId, this.getTranslationProcessor());
        if (sdcId.equals("User")) {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                primary.setString(i, "password", "******");
            }
        }
        this.addFormDataItem(formData, primary, "primary");
        this.addFormDataItem(formData, parent, "parent");
        this.addFormDataItem(formData, sdiattachment, "sdiattachment");
        this.addFormDataItem(formData, attribute, "attribute");
        if (detailDatasets.contains("sdidata")) {
            Utils.translateData(sdidata, translateColumns.get("sdidata"), this.getConnectionProcessor().getLanguage(), "ParamList", this.getTranslationProcessor());
            this.addFormDataItem(formData, sdidata, "sdidata");
        }
        if (detailDatasets.contains("sdidataitem")) {
            Utils.translateData(sdidataitem, translateColumns.get("sdidataitem"), this.getConnectionProcessor().getLanguage(), "Param", this.getTranslationProcessor());
            this.addFormDataItem(formData, sdidataitem, "sdidataitem");
        }
        if (detailDatasets.contains("sdidataapproval")) {
            Utils.translateData(sdidataapproval, translateColumns.get("sdidataapproval"), this.getConnectionProcessor().getLanguage(), "W", this.getTranslationProcessor());
            this.addFormDataItem(formData, sdidataapproval, "sdidataapproval");
        }
        if (detailDatasets.contains("sdispec")) {
            Utils.translateData(sdispec, translateColumns.get("sdispec"), this.getConnectionProcessor().getLanguage(), "Spec", this.getTranslationProcessor());
            this.addFormDataItem(formData, sdispec, "sdispec");
        }
        if (detailDatasets.contains("sdiworkitem")) {
            Utils.translateData(sdiworkitem, translateColumns.get("sdiworkitem"), this.getConnectionProcessor().getLanguage(), "WorkItem", this.getTranslationProcessor());
            this.addFormDataItem(formData, sdiworkitem, "sdiworkitem");
        }
        if (detailDatasets.contains("sdirole")) {
            Utils.translateData(sdirole, translateColumns.get("sdirole"), this.getConnectionProcessor().getLanguage(), "Role", this.getTranslationProcessor());
            this.addFormDataItem(formData, sdirole, "sdirole");
        }
        if (detailDatasets.contains("category")) {
            Utils.translateData(category, translateColumns.get("category"), this.getConnectionProcessor().getLanguage(), "Category", this.getTranslationProcessor());
            this.addFormDataItem(formData, category, "category");
        }
        if (detailDatasets.contains("sdiaddress")) {
            Utils.translateData(sdiaddress, translateColumns.get("sdiaddress"), this.getConnectionProcessor().getLanguage(), "Address", this.getTranslationProcessor());
            this.addFormDataItem(formData, sdiaddress, "sdiaddress");
        }
        this.getSecondarySDIs(sdcId, pageProps, formData, secondaryKeyId1, secondaryKeyId2, secondaryKeyId3);
        this.setDatasourceRowids(formData);
        this.refreshConnection();
        boolean doLog = true;
        PropertyList layoutProps = pageProps.getPropertyListNotNull("layout");
        if (layoutProps.getProperty("propertytreeid", "").equals("GenericPopup") || layoutProps.getProperty("showhistory", "").equals("N")) {
            doLog = false;
        }
        if ((title = pageProps.getPropertyListNotNull("pagetype").getProperty("pagetitle", "")) != null && title.length() > 0 && doLog) {
            title = title.replaceAll("\\[sdcid\\]", sdcId);
            title = title.replaceAll("\\[keyid1\\]", keyid1);
            title = title.replaceAll("\\[keyid2\\]", keyid2);
            title = title.replaceAll("\\[keyid3\\]", keyid3);
            title = title.replaceAll("\\[pagemode\\]", mode);
            title = title.replaceAll("\\[mode\\]", mode);
            try {
                HashMap<String, Object> requestProps = new HashMap<String, Object>();
                requestProps.put("sdcid", sdcId);
                requestProps.put("keyid1", keyid1);
                requestProps.put("keyid2", keyid2);
                requestProps.put("keyid3", keyid3);
                requestProps.put("currentlayout", req.getSession().getAttribute("currentlayout"));
                requestProps.put("currentlayoutnode", req.getSession().getAttribute("currentlayoutnode"));
                PropertyList userPreferences = (PropertyList)req.getSession().getAttribute("userconfig");
                requestProps.put("currentlayouttab", userPreferences.getProperty("genericlayout_lastlinktab"));
                requestProps.put("currentlayoutmenu", userPreferences.getProperty("genericlayout_lastlinkmenu"));
                JSONObject jProps = new JSONObject(sRequestProps);
                requestProps.put("returntolistpage", jProps.optString("returntolistpage"));
            }
            catch (Exception e) {
                this.logger.info("Failed to log page access.");
            }
        }
        if (returnCode == ReturnCode.OK && hasWarnings) {
            returnCode = ReturnCode.WARNING;
        }
        ar.addCallbackArgument("formdata", formData.toJSONObject());
        ar.addCallbackArgument("returncode", (Object)returnCode);
        ar.addCallbackArgument("lockedby", lockedBy);
        ar.addCallbackArgument("rsetid", this.rsetids);
        long endTime = System.currentTimeMillis();
        String logText = "LoadSDI for " + sdcId + " '" + keyid1 + "'  took " + (endTime - startTime) + " milliseconds.";
        this.logger.info(logText);
        ar.print();
    }

    private void setDatasourceRowids(PropertyList formData) {
        for (String datasource : formData.keySet()) {
            PropertyListCollection coll = formData.getCollectionNotNull(datasource);
            if (datasource.equals("primary")) {
                datasource = "pr";
            } else if (datasource.equals("sdiattachment")) {
                datasource = "attachment";
            }
            for (int i = 0; i < coll.size(); ++i) {
                PropertyList a = coll.getPropertyList(i);
                a.setProperty("__rowid", datasource + i);
            }
        }
    }

    private void getSecondarySDIs(String sdcId, PropertyList pageProps, PropertyList formData, String secondaryKeyId1, String secondaryKeyId2, String secondaryKeyId3) {
        for (Object key : pageProps.keySet()) {
            String elementId = (String)key;
            PropertyList elementConfig = pageProps.getPropertyList(elementId);
            if (elementConfig == null || !elementConfig.getProperty("type", "").equals("multimaint")) continue;
            PropertyList collectionConfig = elementConfig.getPropertyListNotNull("detailcollection");
            String detailCollectionType = collectionConfig.getProperty("detailcollectiontype", "sdidetail");
            String detailLink = collectionConfig.getProperty("detaillink", "");
            String detailCollectionItem = collectionConfig.getProperty("detailcollectionitem", "(none)");
            String retrieveMode = collectionConfig.getProperty("retrievemode", "");
            String restrictiveWhere = collectionConfig.getProperty("restrictivewhere", "");
            PropertyListCollection extraColumns = collectionConfig.getCollectionNotNull("secondarycolumns");
            String detailSdcId = "";
            String secondaryTableId = "";
            String secondaryKeyCol1 = "";
            String secondaryKeyCol2 = "";
            String secondaryKeyCol3 = "";
            DataSet ds = null;
            PropertyListCollection elementData = null;
            if (detailCollectionType.equals("many-to-many")) {
                String sql = "SELECT sdclink.linksdcid, sdc.tableid FROM sdclink JOIN sdc on sdclink.linksdcid=sdc.sdcid WHERE sdclink.sdcid=? and sdclink.linkid=?";
                Object[] params = new String[]{sdcId, detailLink};
                DataSet linkDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                detailSdcId = linkDs.getString(0, "linksdcid", "");
                secondaryTableId = linkDs.getString(0, "tableid", "");
                secondaryKeyCol1 = this.getSDCProps(detailSdcId).getProperty("keycolid1", "");
                secondaryKeyCol2 = this.getSDCProps(detailSdcId).getProperty("keycolid2", "");
                elementData = formData.getCollectionNotNull(elementId + "m2m");
            } else if (!detailCollectionItem.equals("(none)") && !detailCollectionItem.equals("category")) {
                detailSdcId = "";
                secondaryTableId = "";
                if (detailCollectionItem.equals("sdiworkitem")) {
                    detailSdcId = "WorkItem";
                    secondaryTableId = "workitem";
                    secondaryKeyCol1 = "workitemid";
                    secondaryKeyCol2 = "workitemversionid";
                } else if (detailCollectionItem.equals("sdidata") || detailCollectionItem.equals("sdidataitem")) {
                    detailSdcId = "ParamList";
                    secondaryTableId = "paramlist";
                    secondaryKeyCol1 = "paramlistid";
                    secondaryKeyCol2 = "paramlistversionid";
                    secondaryKeyCol3 = "variantid";
                } else if (detailCollectionItem.equals("sdispec")) {
                    detailSdcId = "SpecSDC";
                    secondaryTableId = "spec";
                    secondaryKeyCol1 = "specid";
                    secondaryKeyCol2 = "specversionid";
                } else if (detailCollectionItem.equals("sdirole")) {
                    detailSdcId = "Role";
                    secondaryTableId = "role";
                    secondaryKeyCol1 = "roleid";
                }
                elementData = formData.getCollectionNotNull(detailCollectionItem);
            } else if (detailCollectionItem.equals("category")) {
                detailSdcId = "Category";
                secondaryTableId = "category";
                secondaryKeyCol1 = "categoryid";
                elementData = formData.getCollectionNotNull("category");
            }
            if (retrieveMode.equals("All SDIs")) {
                SDIData sdiData;
                SDIRequest r;
                if (detailCollectionType.equals("many-to-many")) {
                    r = new SDIRequest();
                    r.setSDCid(detailSdcId);
                    if (secondaryKeyId1 != null && !secondaryKeyId1.isEmpty()) {
                        r.setKeyid1List(secondaryKeyId1);
                        if (!secondaryKeyCol2.isEmpty() && secondaryKeyId2 != null && !secondaryKeyId2.isEmpty()) {
                            r.setKeyid2List(secondaryKeyId2);
                        }
                        if (!secondaryKeyCol3.isEmpty() && secondaryKeyId3 != null && !secondaryKeyId3.isEmpty()) {
                            r.setKeyid3List(secondaryKeyId3);
                        }
                    } else {
                        r.setQueryFrom(secondaryTableId);
                        r.setQueryWhere(restrictiveWhere);
                    }
                    r.setRequestItem("primary");
                    r.setRetainRsetid(false);
                    sdiData = this.getSDIProcessor().getSDIData(r);
                    ds = sdiData.getDataset("primary");
                } else if (!detailCollectionItem.equals("(none)") && !detailCollectionItem.equals("category")) {
                    r = new SDIRequest();
                    r.setSDCid(detailSdcId);
                    r.setQueryFrom(secondaryTableId);
                    r.setQueryWhere(restrictiveWhere);
                    r.setRequestItem("primary");
                    r.setRetainRsetid(false);
                    sdiData = this.getSDIProcessor().getSDIData(r);
                    ds = sdiData.getDataset("primary");
                } else if (detailCollectionItem.equals("category")) {
                    Object[] params;
                    String sql;
                    String viewHidden = this.getConfigurationProcessor().getProfileProperty("viewhidden", "N");
                    if (viewHidden.equals("Y")) {
                        sql = "SELECT * FROM category WHERE sdcid=?";
                        params = new String[]{sdcId};
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                    } else {
                        sql = "SELECT * FROM category WHERE sdcid=? AND coalesce(activeflag,'Y')='Y'";
                        params = new String[]{sdcId};
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
                    }
                }
                if (ds != null && elementData != null) {
                    int i;
                    HashSet<String> validKeys = new HashSet<String>();
                    for (i = 0; i < ds.getRowCount(); ++i) {
                        validKeys.add(ds.getString(i, secondaryKeyCol1, "") + ";" + ds.getString(i, secondaryKeyCol2, "") + ";" + ds.getString(i, secondaryKeyCol3, ""));
                    }
                    for (i = elementData.size() - 1; i >= 0; --i) {
                        PropertyList rowData = elementData.getPropertyList(i);
                        String rowKey = rowData.getProperty(secondaryKeyCol1, "") + ";" + rowData.getProperty(secondaryKeyCol2, "") + ";" + rowData.getProperty(secondaryKeyCol3, "");
                        if (validKeys.contains(rowKey)) continue;
                        elementData.remove(i);
                    }
                    for (i = 0; i < ds.getRowCount(); ++i) {
                        PropertyList collection = new PropertyList();
                        collection.setProperty(secondaryKeyCol1, ds.getValue(i, secondaryKeyCol1));
                        if (!secondaryKeyCol2.equals("")) {
                            collection.setProperty(secondaryKeyCol2, ds.getValue(i, secondaryKeyCol2));
                        }
                        if (!secondaryKeyCol3.equals("")) {
                            collection.setProperty(secondaryKeyCol3, ds.getValue(i, secondaryKeyCol3));
                        }
                        if (detailCollectionItem.equals("sdidata")) {
                            collection.setProperty("dataset", "1");
                        }
                        if (detailCollectionItem.equals("sdiworkitem")) {
                            collection.setProperty("workiteminstance", "1");
                        }
                        elementData.add(i, collection);
                    }
                }
            }
            StringBuilder extraFieldSelect = new StringBuilder();
            HashSet<String> translateExtraFields = new HashSet<String>();
            for (int i = 0; i < extraColumns.size(); ++i) {
                PropertyList extraColProps = extraColumns.getPropertyList(i);
                String columnId = extraColProps.getProperty("columnid", "");
                if (!columnId.equals("")) {
                    extraFieldSelect.append(",").append(columnId);
                }
                if (!extraColProps.getProperty("translate", "").equals("Y")) continue;
                if (columnId.contains(")")) {
                    translateExtraFields.add(columnId.substring(columnId.indexOf(41) + 1).trim());
                    continue;
                }
                translateExtraFields.add(columnId);
            }
            if (extraFieldSelect.length() <= 0 || elementData == null || elementData.size() <= 0) continue;
            HashSet<String> detailKeySet = new HashSet<String>();
            for (int i = 0; i < elementData.size(); ++i) {
                PropertyList row = elementData.getPropertyList(i);
                String detailKey = secondaryKeyCol1 + "='" + row.getProperty(secondaryKeyCol1) + "'";
                if (!secondaryKeyCol2.equals("")) {
                    detailKey = detailKey + " and " + secondaryKeyCol2 + "='" + row.getProperty(secondaryKeyCol2) + "'";
                }
                if (!secondaryKeyCol3.equals("")) {
                    detailKey = detailKey + " and " + secondaryKeyCol3 + "='" + row.getProperty(secondaryKeyCol3) + "'";
                }
                detailKeySet.add(detailKey);
            }
            String detailKeys = Utils.setToString(detailKeySet, ") or (");
            String extraColumnSql = "select " + secondaryKeyCol1 + " ";
            if (!secondaryKeyCol2.equals("")) {
                extraColumnSql = extraColumnSql + "," + secondaryKeyCol2;
            }
            if (!secondaryKeyCol3.equals("")) {
                extraColumnSql = extraColumnSql + "," + secondaryKeyCol3;
            }
            extraColumnSql = extraColumnSql + extraFieldSelect.toString();
            extraColumnSql = extraColumnSql + " from " + secondaryTableId + " where (" + detailKeys + ")";
            DataSet extraColumnDs = this.getQueryProcessor().getSqlDataSet(extraColumnSql);
            if (extraColumnDs == null) {
                return;
            }
            for (int i = 0; i < elementData.size(); ++i) {
                PropertyList row = elementData.getPropertyList(i);
                for (int j = 0; j < extraColumnDs.getRowCount(); ++j) {
                    if (!extraColumnDs.getString(j, secondaryKeyCol1, "").equals(row.getProperty(secondaryKeyCol1, "")) || !extraColumnDs.getString(j, secondaryKeyCol2, "").equals(row.getProperty(secondaryKeyCol2, "")) || !extraColumnDs.getString(j, secondaryKeyCol3, "").equals(row.getProperty(secondaryKeyCol3, ""))) continue;
                    for (int k = 0; k < extraColumnDs.getColumnCount(); ++k) {
                        String columnId = extraColumnDs.getColumnId(k);
                        String value = extraColumnDs.getValue(j, columnId, "");
                        if (columnId.equals(secondaryKeyCol1) || columnId.equals(secondaryKeyCol2) || columnId.equals(secondaryKeyCol3) || value.equals("")) continue;
                        if (translateExtraFields.contains(columnId)) {
                            row.setProperty(columnId, this.getTranslationProcessor().translate(value, this.getConnectionProcessor().getLanguage(), detailSdcId));
                            continue;
                        }
                        row.setProperty(columnId, value);
                    }
                }
            }
        }
    }

    private DataSet getSampleRequestItems(String templateid) {
        int i;
        String sql = "SELECT ri.itemcount, s.* FROM s_sample s JOIN s_requestitem ri ON ri.templatesdcid='Sample' and ri.templatekeyid1=s.s_sampleid WHERE ri.s_requestid=? ORDER BY ri.usersequence";
        Object[] params = new String[]{templateid};
        DataSet dsOrig = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        DataSet ds = new DataSet();
        for (i = 0; i < dsOrig.getColumnCount(); ++i) {
            String columnId = dsOrig.getColumnId(i);
            if (columnId.equals("itemcount") || this.excludedFields.contains(columnId)) continue;
            ds.addColumn(columnId, dsOrig.getColumnType(columnId));
        }
        ds.addColumn("__rowstatus", 0);
        ds.addColumn("templateid__", 0);
        ds.addColumn("requestid__", 0);
        ds.addColumn("usersequence__", 1);
        for (i = 0; i < dsOrig.getRowCount(); ++i) {
            int itemCount = dsOrig.getInt(i, "itemcount", 1);
            for (int j = 0; j < itemCount; ++j) {
                int rowNum = ds.addRow();
                for (int k = 0; k < ds.getColumnCount(); ++k) {
                    String columnId = ds.getColumnId(k);
                    ds.setObject(rowNum, columnId, dsOrig.getObject(i, columnId));
                }
                ds.setString(rowNum, "__rowstatus", "A");
                ds.setString(rowNum, "templateid__", dsOrig.getString(i, "s_sampleid"));
                ds.setString(rowNum, "requestid__", "[parentkeyid1]");
                ds.setNumber(rowNum, "usersequence", rowNum + 1);
                ds.setNumber(rowNum, "usersequence__", rowNum + 1);
            }
        }
        return ds;
    }

    private void fillMaxVersionId(String sdcid, DataSet primary) {
        primary.addColumn("__maxversion", 0);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String keyid1Field = this.getSDCProps(sdcid).getProperty("keycolid1");
            String keyid1 = primary.getString(i, keyid1Field);
            String lastVersion = Utils.getLatestVersion(keyid1, this.getSDCProps(sdcid), this.getQueryProcessor(), this.getConnectionProcessor().isOra());
            primary.setString(i, "__maxversion", lastVersion);
        }
    }

    private void refreshConnection() {
        String currDate = this.getConnectionProcessor().isOra() ? "sysdate" : "getDate()";
        String sql = "update connection set lastaccesseddt=" + currDate + " where connectionid=?";
        this.getQueryProcessor().execPreparedUpdate(sql, new Object[]{this.getConnectionId()});
    }

    /*
     * WARNING - void declaration
     */
    private void handleTemplateData(String templateid, String sdcid, String parentsdcid, String parentlinkid, DataSet primary, DataSet parent, DataSet sdiworkitem, DataSet sdidata, DataSet sdidataitem, DataSet sdispec, DataSet sdiaddress) {
        PropertyList props = this.getSDCProps(sdcid);
        String keyid1field = (String)props.get("keycolid1");
        DataSet linkData = this.getSDCProcessor().getLinksData(sdcid);
        String linkColumnId = "";
        String linkColumnId2 = "";
        for (int i = 0; i < linkData.getRowCount(); ++i) {
            if (!linkData.getString(i, "linkid", "").equalsIgnoreCase(parentlinkid)) continue;
            linkColumnId = linkData.getString(i, "sdccolumnid", "");
            linkColumnId2 = linkData.getString(i, "sdccolumnid2", "");
        }
        ArrayList<String> primaryColumns = new ArrayList<String>();
        for (int i = 0; i < primary.getColumnCount(); ++i) {
            String string = primary.getColumnId(i);
            if (this.excludedFields.contains(string)) continue;
            primaryColumns.add(string);
        }
        for (String string : primaryColumns) {
            primary.addColumn(string + "__", primary.getColumnType(string));
        }
        primary.addColumn("__rowstatus", 0);
        primary.addColumn("templateid__", 0);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            primary.setValue(i, "__rowstatus", "A");
            primary.setValue(i, keyid1field, "");
            for (String excludedColumn : this.excludedFields) {
                primary.setValue(i, excludedColumn, "");
            }
            if (sdcid.equals("Sample")) {
                primary.setValue(i, "samplestatus", "");
            }
            if (!linkColumnId.equals("")) {
                primary.setValue(i, linkColumnId, "");
            }
            if (!linkColumnId2.equals("")) {
                primary.setValue(i, linkColumnId2, "");
            }
            primary.setValue(i, "templateid__", templateid);
        }
        if (parent != null) {
            void var19_29;
            ArrayList<String> parentColumns = new ArrayList<String>();
            boolean bl = false;
            while (var19_29 < parent.getColumnCount()) {
                String columnid = parent.getColumnId((int)var19_29);
                if (!this.excludedFields.contains(columnid)) {
                    parentColumns.add(columnid);
                }
                ++var19_29;
            }
            for (String columnid : parentColumns) {
                parent.addColumn(columnid + "__", parent.getColumnType(columnid));
            }
            PropertyList propertyList = this.getSDCProps(parentsdcid);
            String parentKeyid1Field = (String)propertyList.get("keycolid1");
            parent.addColumn("__rowstatus", 0);
            for (int i = 0; i < parent.getRowCount(); ++i) {
                parent.setValue(i, "__rowstatus", "A");
                parent.setValue(i, parentKeyid1Field, "");
                for (String excludedColumn : this.excludedFields) {
                    parent.setValue(i, excludedColumn, "");
                }
                for (String columnid : parentColumns) {
                    if (columnid.endsWith("status")) {
                        parent.setValue(i, columnid, "");
                        continue;
                    }
                    parent.setValue(i, columnid + "__", parent.getValue(i, columnid, ""));
                }
            }
        }
        this.clearKeys(sdiworkitem);
        this.clearKeys(sdidata);
        this.clearKeys(sdidataitem);
        this.clearKeys(sdispec);
        this.clearKeys(sdiaddress);
    }

    private void clearKeys(DataSet ds) {
        if (ds != null) {
            ds.addColumn("__rowstatus", 0);
            ds.addColumn("__virtual", 0);
            for (int i = 0; i < ds.getRowCount(); ++i) {
                ds.setValue(i, "keyid1", "[keyid1]");
                ds.setValue(i, "keyid2", "[keyid2]");
                ds.setValue(i, "keyid3", "[keyid3]");
                ds.setValue(i, "__rowstatus", "V");
                ds.setValue(i, "__virtual", "Y");
            }
        }
    }

    private SDIRequest createSdiRequest(String sdcid, String keyid1, String keyid2, String keyid3, String mode, Set<String> detailDatasets) {
        SDIRequest r = new SDIRequest();
        r.setSDCid(sdcid);
        r.setKeyid1List(keyid1);
        if (!keyid2.equals("")) {
            r.setKeyid2List(keyid2);
        }
        if (!keyid3.equals("")) {
            r.setKeyid3List(keyid3);
        }
        r.setRequestItem("primary");
        r.setRequestItem("attribute");
        if (detailDatasets.contains("sdidata")) {
            r.setRequestItem("dataset");
        }
        if (detailDatasets.contains("sdidataitem")) {
            r.setRequestItem("dataitem");
        }
        if (detailDatasets.contains("sdispec")) {
            r.setRequestItem("sdispec");
            r.setRequestItem("dataspec");
        }
        if (detailDatasets.contains("sdiworkitem")) {
            r.setRequestItem("sdiworkitem");
            r.setRequestItem("sdiworkitemitem");
        }
        if (detailDatasets.contains("attachment")) {
            r.setRequestItem("attachment");
        }
        if (detailDatasets.contains("sdirole")) {
            r.setRequestItem("role");
        }
        if (detailDatasets.contains("category")) {
            r.setRequestItem("category");
        }
        if (detailDatasets.contains("sdiaddress")) {
            r.setRequestItem("address");
        }
        if (detailDatasets.contains("sdidataapproval")) {
            r.setRequestItem("dataapproval");
        }
        if (mode.equals("edit")) {
            r.setDataLockOption("LA");
            r.setLockOption("LA");
            r.setPrimaryLockOption("LA");
            r.setRetainRsetid(true);
        } else {
            r.setRetainRsetid(false);
        }
        return r;
    }

    private String addKeysToSql(String sql, String sdcid, String keyid1, String keyid2, String keyid3) {
        sql = sql.replaceAll("\\[sdcid\\]", sdcid);
        sql = sql.replaceAll("\\[keyid1\\]", keyid1);
        sql = sql.replaceAll("\\[keyid2\\]", keyid2);
        sql = sql.replaceAll("\\[keyid3\\]", keyid3);
        sql = sql.replaceAll("\\[%currentuser%\\]", this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId());
        return sql;
    }

    private DataSet getLinkData(String primarysdcid, String primarykeyid1, String primarykeyid2, String primarykeyid3, String detaildataset, boolean lock, List<String> extraColumnList) {
        DataSet retval = null;
        if (detaildataset.startsWith("reversefk:")) {
            String reverselink = detaildataset.substring(10);
            String[] temp = reverselink.split(";");
            String sdcid = temp[0];
            String linkid = temp[1];
            retval = this.fetchReverseFKLinkData(sdcid, linkid, primarykeyid1, primarykeyid2, lock, extraColumnList);
        } else if (detaildataset.startsWith("sdclink:")) {
            String linkid = detaildataset.substring(8);
            retval = this.fetchLinkData(primarysdcid, linkid, primarykeyid1, primarykeyid2, primarykeyid3, extraColumnList);
        }
        return retval;
    }

    private DataSet fetchReverseFKLinkData(String sdcid, String linkid, String primarykeyid1, String primarykeyid2, boolean lock, List<String> extraColumnList) {
        PropertyList props = this.getSDCProps(sdcid);
        String tableid = (String)props.get("tableid");
        DataSet linkData = this.getSDCProcessor().getLinksData(sdcid);
        String linkcolumnid = "";
        String linkcolumnid2 = "";
        for (int i = 0; i < linkData.getRowCount(); ++i) {
            if (!linkData.getString(i, "linkid", "").equalsIgnoreCase(linkid)) continue;
            linkcolumnid = linkData.getString(i, "sdccolumnid", "");
            linkcolumnid2 = linkData.getString(i, "sdccolumnid2", "");
            break;
        }
        boolean isOracle = this.getConnectionProcessor().isOra();
        String queryWhere = linkcolumnid + "='" + SafeSQL.encodeForSQL(primarykeyid1, isOracle) + "'";
        if (!linkcolumnid2.equals("")) {
            queryWhere = queryWhere + " and " + linkcolumnid2 + "='" + SafeSQL.encodeForSQL(primarykeyid2, isOracle) + "'";
        }
        SDIRequest r = new SDIRequest();
        r.setSDCid(sdcid);
        r.setQueryFrom(tableid);
        r.setQueryWhere(queryWhere);
        r.setShowTemplates(true);
        r.setRequestItem("primary");
        if (lock) {
            r.setDataLockOption("LA");
            r.setLockOption("LA");
            r.setPrimaryLockOption("LA");
            r.setRetainRsetid(true);
        } else {
            r.setRetainRsetid(false);
        }
        SDIData sdi = this.getSDIProcessor().getSDIData(r);
        DataSet primary = sdi.getDataset("primary");
        HashMap<String, String> searchValues = new HashMap<String, String>();
        searchValues.put(linkcolumnid, primarykeyid1);
        if (!linkcolumnid2.equals("")) {
            searchValues.put(linkcolumnid2, primarykeyid2);
        }
        String[] keyColumns = this.getKeyColumns(tableid);
        new ExtraColumnUtil(this.getQueryProcessor(), this.logger).fillExtraColumnData(extraColumnList, searchValues, tableid, primary, keyColumns);
        if (this.loadTemplate && sdcid.equals("Sample") && linkid.equals("RequestId")) {
            primary.addColumn("__rowstatus", 0);
            primary.addColumn("templateid__", 0);
            primary.addColumn("requestid__", 0);
            primary.addColumn("collectiondt__", 0);
            primary.addColumn("samplestatus__", 0);
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (!primary.getString(i, "templateflag", "N").equals("Y")) continue;
                primary.setString(i, "templateflag", "N");
                primary.setString(i, "__rowstatus", "A");
                primary.setString(i, "templateid__", primary.getString(i, "s_sampleid"));
                primary.setString(i, "requestid__", "[parentkeyid1]");
                primary.setString(i, "s_sampleid", "");
                primary.setValue(i, "collectiondt", "");
                primary.setValue(i, "collectiondt__", "");
                primary.setValue(i, "samplestatus", "");
                primary.setValue(i, "samplestatus__", "");
                primary.setValue(i, "createdt", "");
            }
        }
        this.rsetids.append("|").append(sdi.getRsetid());
        return primary;
    }

    private DataSet fetchLinkData(String sdcid, String linkid, String primarykeyid1, String primarykeyid2, String primarykeyid3, List<String> extraColumnList) {
        PropertyList props = this.getSDCProps(sdcid);
        String primarykeycolid1 = (String)props.get("keycolid1");
        String primarykeycolid2 = (String)props.get("keycolid2");
        String primarykeycolid3 = (String)props.get("keycolid3");
        if (primarykeycolid2 == null) {
            primarykeycolid2 = "";
        }
        if (primarykeycolid3 == null) {
            primarykeycolid3 = "";
        }
        DataSet linkData = this.getSDCProcessor().getLinksData(sdcid);
        String linktableid = "";
        for (int i = 0; i < linkData.getRowCount(); ++i) {
            if (!linkData.getString(i, "linkid", "").equalsIgnoreCase(linkid)) continue;
            linktableid = linkData.getString(i, "linktableid", "");
            break;
        }
        if (linktableid.equals("")) {
            return new DataSet();
        }
        String[] keyid1Arr = primarykeyid1.split(";");
        String[] keyid2Arr = primarykeyid2.split(";");
        String[] keyid3Arr = primarykeyid3.split(";");
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < keyid1Arr.length; ++i) {
            if (whereClause.length() > 0) {
                whereClause.append(") or (");
            }
            whereClause.append(primarykeycolid1).append("='").append(keyid1Arr[i]).append("'");
            if (!primarykeyid2.equals("") && keyid2Arr.length > i) {
                whereClause.append(" and ").append(primarykeycolid2).append("='").append(keyid2Arr[i]).append("'");
            }
            if (primarykeyid3.equals("") || keyid3Arr.length <= i) continue;
            whereClause.append(" and ").append(primarykeycolid3).append("='").append(keyid3Arr[i]).append("'");
        }
        String keyFields = ", " + primarykeycolid1 + " as keyid1 ";
        if (!primarykeycolid2.equals("")) {
            keyFields = keyFields + ", " + primarykeycolid2 + " as keyid2 ";
        }
        if (!primarykeycolid3.equals("")) {
            keyFields = keyFields + ", " + primarykeycolid3 + " as keyid3 ";
        }
        String sql = "SELECT " + linktableid + ".* " + keyFields + " FROM " + linktableid + " WHERE (" + whereClause.toString() + ")";
        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        HashMap<String, String> searchValues = new HashMap<String, String>();
        searchValues.put(primarykeycolid1, primarykeyid1);
        if (!primarykeyid2.equals("")) {
            searchValues.put(primarykeycolid2, primarykeyid2);
        }
        String[] keyColumns = this.getKeyColumns(linktableid);
        new ExtraColumnUtil(this.getQueryProcessor(), this.logger).fillExtraColumnData(extraColumnList, searchValues, linktableid, ds, keyColumns);
        return ds;
    }

    private String[] getKeyColumns(String tableid) {
        String sql = "SELECT columnid FROM syscolumn WHERE pkflag='Y' AND tableid=?";
        Object[] params = new String[]{tableid};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        if (ds != null) {
            return ds.getColumnValues("columnid", ";").split(";");
        }
        return new String[0];
    }

    private String[] getKeyColumnsBySdc(String sdcid) {
        String sql = "SELECT columnid FROM syscolumn, sdc WHERE sdcid=? AND syscolumn.tableid=sdc.tableid AND pkflag='Y' ORDER BY columnsequence";
        Object[] params = new String[]{sdcid};
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        if (ds != null) {
            return ds.getColumnValues("columnid", ";").split(";");
        }
        return new String[0];
    }

    private Set<String> getDetailDatasets(PropertyList pageConfig) {
        HashSet<String> detailDatasets = new HashSet<String>();
        for (Object key : pageConfig.keySet()) {
            String elementId = (String)key;
            PropertyList elementConfig = pageConfig.getPropertyList(elementId);
            if (elementConfig == null) continue;
            String elementtype = elementConfig.getProperty("type", "");
            if (elementtype.equals("datasetgrid")) {
                detailDatasets.add("sdiworkitem");
                detailDatasets.add("sdidata");
                detailDatasets.add("sdidataitem");
                detailDatasets.add("sdispec");
                detailDatasets.add("sdidataapproval");
            }
            if (elementtype.equals("multimaint")) {
                String detailLink;
                PropertyList collectionConfig = elementConfig.getPropertyListNotNull("detailcollection");
                String detailcollection = collectionConfig.getProperty("detailcollectionitem", "(none)");
                if (!detailcollection.equals("(none)")) {
                    detailDatasets.add(detailcollection);
                }
                if (!(detailLink = collectionConfig.getProperty("detaillink", "")).equals("")) {
                    detailDatasets.add("sdclink:" + detailLink + "###" + elementId + "m2m");
                }
            }
            if (!elementtype.equals("grid")) continue;
            String linktype = elementConfig.getProperty("linktype", "");
            String datasource = elementConfig.getProperty("datasource", "");
            String linkSql = elementConfig.getProperty("linksql", "");
            if (linktype.startsWith("sdidetail:")) {
                if (datasource.equals("sdicontrolcard")) {
                    detailDatasets.add(datasource + ";" + elementId);
                    continue;
                }
                detailDatasets.add(datasource);
                continue;
            }
            if (linktype.startsWith("sql")) {
                detailDatasets.add(linktype + ":" + linkSql + "###" + datasource);
                continue;
            }
            detailDatasets.add(linktype + "###" + datasource);
        }
        return detailDatasets;
    }

    private void addFormDataItem(PropertyList formData, DataSet ds, String datasourceid) {
        if (ds == null) {
            return;
        }
        formData.setProperty(datasourceid, Utils.dsToCollection(ds));
    }

    private DataSet createParentDataset(String sdcid, String parentsdcid, String parentlinkid, DataSet primary, String mode) {
        DataSet linkData = this.getSDCProcessor().getLinksData(sdcid);
        String parentkeyid1 = "";
        String parentkeyid2 = "";
        String linkcolumnid = "";
        for (int i = 0; i < linkData.getRowCount(); ++i) {
            if (!linkData.getString(i, "linkid", "").equalsIgnoreCase(parentlinkid)) continue;
            linkcolumnid = linkData.getString(i, "sdccolumnid", "");
            String linkcolumnid2 = linkData.getString(i, "sdccolumnid2", "");
            parentkeyid1 = primary.getColumnValues(linkcolumnid, ";");
            parentkeyid2 = primary.getColumnValues(linkcolumnid2, ";");
            break;
        }
        if (parentkeyid1.equals("")) {
            return null;
        }
        SDIRequest r = new SDIRequest();
        r.setSDCid(parentsdcid);
        r.setKeyid1List(parentkeyid1);
        if (!parentkeyid2.equals("")) {
            r.setKeyid2List(parentkeyid2);
        }
        r.setRequestItem("primary");
        if (mode.equals("edit")) {
            r.setRetainRsetid(true);
            r.setLockOption("LA");
            r.setPrimaryLockOption("LA");
        } else {
            r.setRetainRsetid(false);
        }
        SDIData parentSDI = this.getSDIProcessor().getSDIData(r);
        DataSet parent = parentSDI.getDataset("primary");
        String parentRsetid = parentSDI.getRsetid();
        this.rsetids.append("|").append(parentRsetid);
        int originalColumnCount = parent.getColumnCount();
        for (int i = 0; i < originalColumnCount; ++i) {
            String columnid = parent.getColumnId(i);
            if (this.excludedFields.contains(columnid)) continue;
            int columntype = parent.getColumnType(columnid);
            parent.addColumn(linkcolumnid + "_" + columnid, columntype);
            parent.setObject(0, linkcolumnid + "_" + columnid, parent.getObject(0, columnid));
        }
        return parent;
    }

    private boolean fillPrimaryExtraColumnData(List<String> extraColumns, String sdcid, String keyid1, String keyid2, String keyid3, DataSet primary, String[] keyColumns) {
        boolean querySuccess;
        if (primary == null || primary.getRowCount() == 0) {
            return true;
        }
        PropertyList props = this.getSDCProps(sdcid);
        String tableid = (String)props.get("tableid");
        String keyid1field = (String)props.get("keycolid1");
        String keyid2field = (String)props.get("keycolid2");
        String keyid3field = (String)props.get("keycolid3");
        HashSet<String> linkedTables = new HashSet<String>();
        StringBuilder sqlSelect = new StringBuilder();
        StringBuilder sqlFrom = new StringBuilder();
        for (String keycol : keyColumns) {
            sqlSelect.append(",").append(keycol);
        }
        HashMap<String, String> actualColumnIds = new HashMap<String, String>();
        int colnum = 0;
        for (String extracolumn : extraColumns) {
            if (extracolumn.trim().startsWith("(")) {
                sqlSelect.append(",").append(extracolumn);
                continue;
            }
            if (!extracolumn.contains(".")) continue;
            String linkid = extracolumn.substring(0, extracolumn.indexOf("."));
            String linkcolumnid = extracolumn.substring(extracolumn.indexOf(".") + 1);
            String sql = "SELECT linksdcid, sdccolumnid2 FROM sdclink WHERE sdcid=? AND sdccolumnid=? ORDER BY coalesce(sdccolumnid2,'1')";
            Object[] params = new String[]{sdcid, linkid};
            DataSet linkDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
            if (linkDs == null || linkDs.getRowCount() <= 0) continue;
            String linksdcid = linkDs.getString(0, "linksdcid", "");
            String linkedcolumn2 = linkDs.getString(0, "sdccolumnid2", "");
            PropertyList linktableprops = this.getSDCProps(linksdcid);
            String linktableid = (String)linktableprops.get("tableid");
            String linktablekeycolumn1 = (String)linktableprops.get("keycolid1");
            String linktablekeycolumn2 = (String)linktableprops.get("keycolid2");
            String tempcolumnid = "extracol" + ++colnum;
            actualColumnIds.put(tempcolumnid, linkid + "_" + linkcolumnid);
            sqlSelect.append(",").append(linktableid).append(".").append(linkcolumnid);
            sqlSelect.append(" AS ").append(tempcolumnid);
            if (linkedTables.contains(linktableid)) continue;
            sqlFrom.append(" LEFT OUTER JOIN ").append(linktableid).append(" ON ");
            sqlFrom.append(linktableid).append(".").append(linktablekeycolumn1).append("=").append(tableid).append(".").append(linkid);
            if (!linkedcolumn2.equals("")) {
                sqlFrom.append(" AND ").append(linktableid).append(".").append(linktablekeycolumn2).append("=").append(tableid).append(".").append(linkedcolumn2);
            }
            linkedTables.add(linktableid);
        }
        StringBuilder sqlWhere = new StringBuilder();
        String[] keyid1Arr = keyid1.split(";");
        String[] keyid2Arr = keyid2.split(";");
        String[] keyid3Arr = keyid3.split(";");
        for (int i = 0; i < keyid1Arr.length; ++i) {
            if (sqlWhere.length() > 0) {
                sqlWhere.append(" OR ");
            }
            sqlWhere.append("(");
            sqlWhere.append(keyid1field).append("='").append(keyid1Arr[i]).append("'");
            if (keyid2field != null && !keyid2field.equals("") && keyid2Arr.length > i) {
                sqlWhere.append(" AND ").append(keyid2field).append("='").append(keyid2Arr[i]).append("'");
            }
            if (keyid3field != null && !keyid3field.equals("") && keyid3Arr.length > i) {
                sqlWhere.append(" AND ").append(keyid3field).append("='").append(keyid3Arr[i]).append("'");
            }
            sqlWhere.append(")");
        }
        String sql = "SELECT " + sqlSelect.substring(1) + " FROM " + tableid + sqlFrom + " WHERE " + sqlWhere;
        DataSet dsExtra = this.getQueryProcessor().getSqlDataSet(sql);
        if (dsExtra != null) {
            new ExtraColumnUtil(this.getQueryProcessor(), this.logger).injectExtraColumnsToDataset(primary, dsExtra, actualColumnIds, keyColumns);
            querySuccess = true;
        } else {
            this.logger.error("Invalid query: " + sql);
            querySuccess = false;
        }
        return querySuccess;
    }

    private static enum ReturnCode {
        OK,
        NOTFOUND,
        ERROR,
        LOCKED,
        WARNING;

    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPageUtil;
import com.labvantage.sapphire.modules.adhocbrowser.CriteriaEditor;
import com.labvantage.sapphire.tagext.SDITagUtil;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DataEntryCriteriaEditor
extends CriteriaEditor {
    @Override
    public PropertyList getEditorProperty(String sdcid, PropertyList column, AdhocMetaData adhocmetadata, SDCProcessor sdcProcessor, SDITagUtil sdiTagUtil, TranslationProcessor tp, PropertyList pagedata) {
        String columnid = column.getProperty("columnid");
        String realsdcid = sdcid;
        PropertyList dataentryPL = AdhocQueryPageUtil.getDataEntryPropertyList(sdcid, pagedata);
        if (columnid.indexOf(".sdidataitem[") > 0) {
            String tableid = adhocmetadata.getTableid(sdcid);
            String realtableid = "";
            try {
                realtableid = AdhocMetaData.getReferenceEntityName(sdcProcessor.getConnectionid(), tableid, columnid.substring(0, columnid.lastIndexOf(".")));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (realtableid != null && realtableid.length() > 0) {
                realsdcid = adhocmetadata.getSdcId(realtableid);
            }
        }
        String[] dataitem = StringUtil.getTokens(columnid);
        String[] dataitemKeys = StringUtil.split(dataitem[dataitem.length - 1], "|");
        String paramlistid = "";
        String paramlistversionid = "";
        String variantid = "";
        String paramid = "";
        String paramtype = "";
        boolean ignoreParamlistversionid = false;
        if (dataitemKeys.length == 5) {
            paramlistid = dataitemKeys[0];
            paramlistversionid = dataitemKeys[1];
            variantid = dataitemKeys[2];
            paramid = dataitemKeys[3];
            paramtype = dataitemKeys[4];
        } else if (dataitemKeys.length == 4) {
            ignoreParamlistversionid = true;
            paramlistid = dataitemKeys[0];
            variantid = dataitemKeys[1];
            paramid = dataitemKeys[2];
            paramtype = dataitemKeys[3];
        } else {
            paramid = dataitemKeys[0];
            paramtype = dataitemKeys[1];
        }
        String title = paramid + "(" + paramtype + ")";
        QueryProcessor qp = new QueryProcessor(sdcProcessor.getConnectionid());
        if (dataitemKeys.length == 5 || dataitemKeys.length == 4) {
            if (dataentryPL != null && dataentryPL.getProperty("criteriatitle").length() > 0) {
                String criteriatitle;
                title = criteriatitle = dataentryPL.getProperty("criteriatitle");
                title = StringUtil.replaceAll(title, "[paramlistid]", paramlistid);
                title = StringUtil.replaceAll(title, "[paramlistversionid]", paramlistversionid);
                title = StringUtil.replaceAll(title, "[variantid]", variantid);
                title = StringUtil.replaceAll(title, "[paramid]", paramid);
                title = StringUtil.replaceAll(title, "[paramtype]", paramtype);
                String[] customtokens = StringUtil.getTokens(title);
                for (int i = 0; i < customtokens.length; ++i) {
                    String value;
                    if (customtokens[i].indexOf("param.") == 0) {
                        value = qp.getPreparedSqlDataSet("select " + customtokens[i] + " from param where paramid=?", new Object[]{paramid}).getValue(0, customtokens[i].substring(6), paramid);
                        title = StringUtil.replaceAll(title, "[" + customtokens[i] + "]", value);
                        continue;
                    }
                    if (customtokens[i].indexOf("paramlist.") != 0) continue;
                    value = qp.getPreparedSqlDataSet("select " + customtokens[i] + " from paramlist where paramlistid=? and variantid=?", new Object[]{paramlistid, variantid}).getValue(0, customtokens[i].substring(10), paramlistid);
                    title = StringUtil.replaceAll(title, "[" + customtokens[i] + "]", value);
                }
            } else {
                title = paramlistid + "(" + (dataitemKeys.length == 4 ? "" : paramlistversionid + ",") + variantid + ")<br/>" + paramid + "(" + paramtype + ")";
            }
        }
        ConnectionInfo connectionInfo = new ConnectionProcessor(sdcProcessor.getConnectionid()).getConnectionInfo(sdcProcessor.getConnectionid());
        String dbms = connectionInfo.getDbms();
        DataSet dataitemDs = null;
        String sql = "";
        String selectcolumnsfromjoin = "sdidataitem.datatypes, sdidataitem.entryreftypeid, sdidataitem.entrysdcid, paramlistitem.weblookupurl from sdidataitem left outer join paramlistitem on sdidataitem.paramlistid=paramlistitem.paramlistid and sdidataitem.paramlistversionid=paramlistitem.paramlistversionid and sdidataitem.variantid=paramlistitem.variantid and sdidataitem.paramid=paramlistitem.paramid and sdidataitem.paramtype=paramlistitem.paramtype";
        if ("MSS".equals(dbms)) {
            sql = "select top 1 " + selectcolumnsfromjoin + "  where sdidataitem.sdcid=? and sdidataitem.paramlistid=?" + (ignoreParamlistversionid ? "" : " and sdidataitem.paramlistversionid=?") + " and sdidataitem.variantid=? and sdidataitem.paramid=? and sdidataitem.paramtype=?";
            if (dataitemKeys.length == 2) {
                sql = "select top 1 " + selectcolumnsfromjoin + " where sdidataitem.sdcid=? and sdidataitem.paramid=? and sdidataitem.paramtype=?";
            }
        } else {
            sql = "select " + selectcolumnsfromjoin + " where sdidataitem.sdcid=? and sdidataitem.paramlistid=?" + (ignoreParamlistversionid ? "" : " and sdidataitem.paramlistversionid=?") + " and sdidataitem.variantid=? and sdidataitem.paramid=? and sdidataitem.paramtype=? and rownum=1";
            if (dataitemKeys.length == 2) {
                sql = "select " + selectcolumnsfromjoin + " where sdidataitem.sdcid=? and sdidataitem.paramid=? and sdidataitem.paramtype=? and rownum=1";
            }
        }
        dataitemDs = dataitemKeys.length == 2 ? qp.getPreparedSqlDataSet(sql, new Object[]{realsdcid, paramid, paramtype}) : (ignoreParamlistversionid ? qp.getPreparedSqlDataSet(sql, new Object[]{realsdcid, paramlistid, variantid, paramid, paramtype}) : qp.getPreparedSqlDataSet(sql, new Object[]{realsdcid, paramlistid, paramlistversionid, variantid, paramid, paramtype}));
        String datatype = dataitemDs.getString(0, "datatypes");
        String reftypeid = dataitemDs.getString(0, "entryreftypeid");
        column.setProperty("name", DataEntryCriteriaEditor.getUniqueId());
        if ("N".equals(datatype) || "NC".equals(datatype)) {
            datatype = "N";
            column.setProperty("onblur", "validateValue( 'Number', this )");
            column.setProperty("mode", "input");
            column.setProperty("size", "10");
        } else if ("D".equals(datatype) || "O".equals(datatype)) {
            column.setProperty("onblur", "validateValue( 'Date', this )");
            column.setProperty("mode", "datelookup");
            if ("O".equals(datatype)) {
                column.setProperty("format", "O");
            }
            column.setProperty("size", "14");
            column.setProperty("img", "WEB-CORE/elements/images/lookup_date.gif");
        } else if ("S".equals(datatype)) {
            column.setProperty("mode", "lookup");
            column.setProperty("img", "WEB-CORE/elements/images/lookup.gif");
            String entrysdcid = dataitemDs.getValue(0, "entrysdcid", "");
            column.setProperty("sdcid", entrysdcid);
            column.setProperty("linksdcid", entrysdcid);
            String weblookupurl = dataitemDs.getValue(0, "weblookupurl");
            if (weblookupurl.length() > 0) {
                PropertyList lookuplink = new PropertyList();
                lookuplink.setProperty("href", weblookupurl);
                column.setProperty("lookuplink", lookuplink);
            }
            DataEntryCriteriaEditor.setDefaultLookupLink(column, entrysdcid, qp);
        } else if ("R".equals(datatype) || "V".equals(datatype)) {
            column.setProperty("mode", "dropdownlist");
            column.setProperty("reftypeid", reftypeid);
        } else {
            column.setProperty("mode", "input");
            column.setProperty("size", "40");
        }
        column.setProperty("label", title);
        column.setProperty("columntype", datatype);
        return column;
    }
}


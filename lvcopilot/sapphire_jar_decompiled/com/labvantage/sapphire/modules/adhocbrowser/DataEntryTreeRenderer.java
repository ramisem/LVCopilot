/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPageUtil;
import com.labvantage.sapphire.modules.adhocbrowser.DetailTreeRenderer;
import com.labvantage.sapphire.modules.adhocbrowser.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DataEntryTreeRenderer
extends DetailTreeRenderer {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private PropertyList dataentryPL;
    private String filtertext;
    private boolean ignoreParamList = false;
    private boolean ignoreParamlistversion = false;
    private String softlinktable;

    public DataEntryTreeRenderer(String sdcid, String detailname, PropertyList pagedata, AdhocMetaData adhocmetadata, QueryProcessor queryProcessor, SDCProcessor sdcProcessor, TranslationProcessor tp) {
        super(sdcid, detailname, pagedata, adhocmetadata, queryProcessor, sdcProcessor, tp);
    }

    public void init(String sdcid, String filtertext, String softlinktable) {
        this.dataentryPL = AdhocQueryPageUtil.getDataEntryPropertyList(sdcid, this.pagedata);
        this.filtertext = filtertext;
        this.softlinktable = softlinktable;
        if (this.dataentryPL != null) {
            this.ignoreParamList = "Y".equals(this.dataentryPL.getProperty("ignoreparamlist"));
            this.ignoreParamlistversion = "Y".equals(this.dataentryPL.getProperty("ignoreparamlistversion"));
        }
    }

    @Override
    protected ArrayList getChildNodes() {
        ArrayList nodes = null;
        if (this.linkcolumnid.indexOf("sdidata[") >= 0) {
            nodes = this.getParamListChildNodes(this.getFilterWhereClause());
        } else if ("dataentryroot".equals(this.linkcolumnid) || this.linkcolumnid.indexOf(".dataentryroot") > 0) {
            String filterclause;
            String string = filterclause = this.filtertext != null ? " lower( sdidataitem.paramlistid ) like '%" + this.filtertext.toLowerCase().replaceAll("'", "''") + "%'" : "";
            if (this.dataentryPL != null) {
                String paramlistdisplay = this.dataentryPL.getProperty("paramlistdisplay");
                String paramdisplay = this.dataentryPL.getProperty("paramdisplay");
                String dataitemtitle = this.dataentryPL.getProperty("dataitemtitle");
                String criteriatitle = this.dataentryPL.getProperty("criteriatitle");
                filterclause = this.getFilterWhereClause();
                nodes = this.ignoreParamList || "N".equals(this.dataentryPL.getProperty("groupbyparamlist")) ? this.getDataEntryParamNodes(filterclause, paramdisplay, criteriatitle, this.filtertext) : this.getDataEntryParamListNodes(filterclause, paramlistdisplay, this.filtertext);
            } else {
                nodes = this.getDataEntryParamListNodes(filterclause, "", this.filtertext);
            }
        }
        return nodes;
    }

    private String getFilterWhereClause() {
        String filterclause = "";
        if (this.dataentryPL != null) {
            String filterwhereclause = this.dataentryPL.getProperty("filterwhereclause");
            filterclause = filterwhereclause.length() > 0 ? StringUtil.replaceAll(filterwhereclause, "[]", this.filtertext) : "";
        }
        return filterclause;
    }

    protected ArrayList getDataEntryParamListNodes(String filterclause, String paramlistdisplay, String filtertext) {
        String objectprefix = "";
        if (this.linkcolumnid != null && this.linkcolumnid.indexOf(".") > 0) {
            objectprefix = this.linkcolumnid.substring(0, this.linkcolumnid.indexOf("dataentryroot"));
        }
        String[] tokens = StringUtil.getTokens(paramlistdisplay);
        String paramlistSelect = "";
        for (int i = 0; i < tokens.length; ++i) {
            if (tokens[i].indexOf("paramlist.") != 0) continue;
            paramlistSelect = paramlistSelect + "," + tokens[i];
        }
        String sql = "";
        sql = this.softlinktable.length() > 0 ? "Select distinct sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid" + (paramlistSelect.length() > 0 ? paramlistSelect : "") + " from sdidataitem " + (paramlistSelect.length() > 0 ? " left outer join paramlist on paramlist.paramlistid=sdidataitem.paramlistid and paramlist.paramlistversionid=sdidataitem.paramlistversionid and paramlist.variantid=sdidataitem.variantid" : "") + (this.softlinktable.length() > 0 ? "," + this.softlinktable : "") + " where sdidataitem.sdcid='" + this.sdcid + "' " + (filterclause.length() > 0 ? " and (" + filterclause + ")" : "") + (this.softlinktable.length() > 0 ? " and ( sdidataitem.sdcid=" + this.softlinktable + ".sdcid and sdidataitem.keyid1=" + this.softlinktable + ".keyid1 )" : "") + " order by sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid" : "select paramlistid, paramlistversionid,variantid" + (paramlistSelect.length() > 0 ? paramlistSelect : "") + " from paramlist where exists (select paramlistid from sdidataitem  where sdidataitem.sdcid='" + this.sdcid + "' and paramlist.paramlistid = sdidataitem.paramlistid and paramlist.paramlistversionid = sdidataitem.paramlistversionid and paramlist.variantid = sdidataitem.variantid " + (filterclause.length() > 0 ? " and (" + filterclause + ")" : "") + " ) order by paramlistid, paramlistversionid, variantid";
        DataSet ds = this.queryProcessor.getSqlDataSet(sql);
        if (filterclause.length() == 0 && filtertext != null && filtertext.length() > 0) {
            ds.addColumn("match", 0);
            filtertext = filtertext.toLowerCase();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (!ds.getValue(i, "paramlistid").toLowerCase().contains(filtertext)) continue;
                ds.setValue(i, "match", "Y");
            }
            HashMap<String, String> filtermap = new HashMap<String, String>();
            filtermap.put("match", "Y");
            ds = ds.getFilteredDataSet(filtermap);
        }
        ds = this.filterParamListDsSecurity(ds);
        return this.getParamListNodesCommon(ds, paramlistdisplay, objectprefix);
    }

    private DataSet filterParamListDsSecurity(DataSet ds) {
        String accessflag = this.sdcProcessor.getProperty("ParamList", "accesscontrolledflag");
        if (!"N".equals(accessflag)) {
            StringBuilder paramlistids = new StringBuilder();
            StringBuilder paramlistversionids = new StringBuilder();
            StringBuilder variantids = new StringBuilder();
            HashSet<String> set = new HashSet<String>();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String idversion = ds.getValue(i, "paramlistid") + ";" + ds.getValue(i, "paramlistversionid") + ";" + ds.getValue(i, "variantid");
                if (set.contains(idversion)) continue;
                set.add(idversion);
                paramlistids.append(";" + ds.getValue(i, "paramlistid"));
                paramlistversionids.append(";" + ds.getValue(i, "paramlistversionid"));
                variantids.append(";" + ds.getValue(i, "variantid"));
            }
            if (paramlistids.length() > 0) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("ParamList");
                sdiRequest.setKeyid1List(paramlistids.substring(1));
                sdiRequest.setKeyid2List(paramlistversionids.substring(1));
                sdiRequest.setKeyid3List(variantids.substring(1));
                sdiRequest.setRequestItem("primary[paramlistid, paramlistversionid, variantid]");
                SDIData sdiData = new SDIProcessor(this.queryProcessor.getConnectionid()).getSDIData(sdiRequest);
                DataSet paramlistDs = sdiData.getDataset("primary");
                set.clear();
                for (int i = 0; i < paramlistDs.getRowCount(); ++i) {
                    set.add(paramlistDs.getValue(i, "paramlistid") + ";" + paramlistDs.getValue(i, "paramlistversionid") + ";" + paramlistDs.getValue(i, "variantid"));
                }
                ArrayList removeList = new ArrayList();
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    if (set.contains(ds.getValue(i, "paramlistid") + ";" + ds.getValue(i, "paramlistversionid") + ";" + ds.getValue(i, "variantid"))) continue;
                    removeList.add(ds.get(i));
                }
                ds.removeAll(removeList);
            }
        }
        return ds;
    }

    protected ArrayList getParamListNodesCommon(DataSet ds, String paramlistdisplay, String objectprefix) {
        ArrayList<TreeNode> paramListNodes = new ArrayList<TreeNode>();
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String nodeid = objectprefix + "sdidata[" + ds.getString(i, "paramlistid") + (this.ignoreParamlistversion ? "" : "|" + ds.getString(i, "paramlistversionid")) + "|" + ds.getString(i, "variantid") + "]";
            if (set.contains(nodeid)) continue;
            set.add(nodeid);
            String nodelabel = ds.getString(i, "paramlistid") + "(" + (this.ignoreParamlistversion ? "" : "v:" + ds.getString(i, "paramlistversionid") + " ") + "var:" + ds.getString(i, "variantid") + ")";
            if (paramlistdisplay != null && paramlistdisplay.length() > 0) {
                nodelabel = paramlistdisplay;
                nodelabel = DataEntryTreeRenderer.evalLabelExpression(nodelabel, ds, i);
            }
            TreeNode node = new TreeNode(nodeid, nodelabel, "WEB-CORE/images/gif/Tests.gif", true);
            node.setDragable(false);
            paramListNodes.add(node);
        }
        Collections.sort(paramListNodes);
        return paramListNodes;
    }

    protected ArrayList getDataEntryParamNodes(String filterclause, String paramdisplay, String criteriatitle, String filtertext) {
        String objectprefix = "";
        if (this.linkcolumnid != null && this.linkcolumnid.indexOf(".") > 0) {
            objectprefix = this.linkcolumnid.substring(0, this.linkcolumnid.indexOf(".") + 1);
        }
        String[] tokens = StringUtil.getTokens(paramdisplay);
        String paramSelect = "";
        String paramlistSelect = "";
        for (int i = 0; i < tokens.length; ++i) {
            if (tokens[i].indexOf("param.") == 0) {
                paramSelect = paramSelect + "," + tokens[i];
                continue;
            }
            if (tokens[i].indexOf("paramlist.") != 0) continue;
            paramlistSelect = paramlistSelect + "," + tokens[i];
        }
        String sql = "Select distinct sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.datatypes " + (paramSelect.length() > 0 ? paramSelect : "") + (paramlistSelect.length() > 0 ? paramlistSelect : "") + " from sdidataitem" + (paramSelect.length() > 0 ? " left outer join param on param.paramid=sdidataitem.paramid" : "") + (paramlistSelect.length() > 0 ? " left outer join paramlist on paramlist.paramlistid=sdidataitem.paramlistid and paramlist.paramlistversionid=sdidataitem.paramlistversionid and paramlist.variantid=sdidataitem.variantid" : "") + (this.softlinktable.length() > 0 ? "," + this.softlinktable : "") + " where sdidataitem.sdcid='" + this.sdcid.replaceAll("'", "''") + "' " + (filterclause.length() > 0 ? " and (" + filterclause + ")" : "") + (this.softlinktable.length() > 0 ? " and (sdidataitem.sdcid=" + this.softlinktable + ".sdcid and sdidataitem.keyid1=" + this.softlinktable + ".keyid1) " : "") + " order by " + (!this.ignoreParamList ? "sdidataitem.paramlistid," : "") + " sdidataitem.paramid";
        DataSet ds = this.queryProcessor.getSqlDataSet(sql);
        if (filterclause.length() == 0 && filtertext != null && filtertext.length() > 0) {
            ds.addColumn("match", 0);
            filtertext = filtertext.toLowerCase();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (!ds.getValue(i, "paramid").toLowerCase().contains(filtertext)) continue;
                ds.setValue(i, "match", "Y");
            }
            HashMap<String, String> filtermap = new HashMap<String, String>();
            filtermap.put("match", "Y");
            ds = ds.getFilteredDataSet(filtermap);
        }
        ds = this.filterParamListDsSecurity(ds);
        return this.getParamNodesCommon(ds, paramdisplay, criteriatitle, objectprefix);
    }

    private ArrayList getParamNodesCommon(DataSet ds, String paramdisplay, String criteriatitle, String objectprefix) {
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String paramlistid = ds.getString(i, "paramlistid");
            String paramlistversionid = ds.getString(i, "paramlistversionid");
            String variantid = ds.getString(i, "variantid");
            String paramid = ds.getString(i, "paramid");
            String paramdesc = ds.getValue(i, "paramdesc").length() > 0 ? ds.getValue(i, "paramdesc") : paramid;
            String paramtype = ds.getString(i, "paramtype");
            String nodeid = objectprefix + "sdidataitem[";
            if (!this.ignoreParamList) {
                nodeid = nodeid + paramlistid + (this.ignoreParamlistversion ? "" : "|" + paramlistversionid) + "|" + variantid + "|";
            }
            if (set.contains(nodeid = nodeid + paramid + "|" + paramtype + "]")) continue;
            set.add(nodeid);
            String nodelabel = paramdisplay;
            nodelabel = nodelabel != null && nodelabel.length() > 0 ? DataEntryTreeRenderer.evalLabelExpression(nodelabel, ds, i) : paramid + "(" + paramtype + ")";
            String datatype = ds.getString(i, "datatypes");
            String nodeimage = "WEB-CORE/imageref/flat/16/flat_black_type_bit.svg";
            if ("V".equals(datatype) || "R".equals(datatype)) {
                nodeimage = "WEB-CORE/imageref/flat/16/flat_black_sort_down_dropdown.svg";
            } else if ("D".equals(datatype) || "O".equals(datatype)) {
                nodeimage = "WEB-CORE/imageref/flat/16/flat_black_calendar2.svg";
            } else if ("S".equals(datatype)) {
                nodeimage = "WEB-CORE/imageref/flat/16/flat_black_external_lookup1.svg";
            } else if ("T".equals(datatype) || "A".equals(datatype)) {
                nodeimage = "WEB-CORE/imageref/flat/16/flat_black_page.svg";
            }
            TreeNode node = new TreeNode(nodeid, nodelabel, nodeimage, false);
            node.setIsCriteriaOnly(this.isCriteriaOnly);
            nodes.add(node);
            String columntitle = criteriatitle;
            columntitle = columntitle != null && columntitle.length() > 0 ? DataEntryTreeRenderer.evalLabelExpression(columntitle, ds, i) : (this.ignoreParamList ? paramid + "(" + paramtype + ")" : paramlistid + "(" + paramid + ")");
            node.setColumntitle(columntitle);
        }
        Collections.sort(nodes);
        return nodes;
    }

    protected ArrayList getParamListChildNodes(String filterclause) {
        String paramdisplay = "";
        String criteriatitle = "";
        if (this.dataentryPL != null) {
            paramdisplay = this.dataentryPL.getProperty("paramdisplay");
            criteriatitle = this.dataentryPL.getProperty("criteriatitle");
        }
        String[] customtokens = StringUtil.getTokens(paramdisplay);
        String paramSelect = "";
        String paramlistSelect = "";
        for (int i = 0; i < customtokens.length; ++i) {
            if (customtokens[i].indexOf("param.") == 0) {
                paramSelect = paramSelect + "," + customtokens[i];
                continue;
            }
            if (customtokens[i].indexOf("paramlist.") != 0) continue;
            paramlistSelect = paramlistSelect + "," + customtokens[i];
        }
        String objectprefix = "";
        if (this.linkcolumnid.indexOf(".sdidata[") > 0) {
            objectprefix = this.linkcolumnid.substring(0, this.linkcolumnid.indexOf("sdidata["));
        }
        String[] tokens = StringUtil.getTokens(this.linkcolumnid);
        DataSet ds = null;
        if (tokens.length > 0) {
            String paramlist = tokens[tokens.length - 1];
            String[] paramlistKey = StringUtil.split(paramlist, "|");
            String sql = "Select distinct sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.datatypes" + (paramSelect.length() > 0 ? paramSelect : "") + (paramlistSelect.length() > 0 ? paramlistSelect : "") + " from sdidataitem" + (paramSelect.length() > 0 ? " left outer join param on param.paramid=sdidataitem.paramid" : "") + (paramlistSelect.length() > 0 ? " left outer join paramlist on paramlist.paramlistid=sdidataitem.paramlistid and paramlist.paramlistversionid=sdidataitem.paramlistversionid and paramlist.variantid=sdidataitem.variantid" : "") + " where sdcid='" + this.sdcid + (this.ignoreParamlistversion ? "' and paramlistid='" + paramlistKey[0].replaceAll("'", "''") + "' and variantid='" + paramlistKey[1].replaceAll("'", "''") + "'" : "' and paramlistid='" + paramlistKey[0].replaceAll("'", "''") + "' and paramlistversionid='" + paramlistKey[1].replaceAll("'", "''") + "' and variantid='" + paramlistKey[2].replaceAll("'", "''") + "'") + (filterclause.length() > 0 ? " AND (" + filterclause + ")" : "");
            ds = this.queryProcessor.getSqlDataSet(sql);
        }
        return this.getParamNodesCommon(ds, paramdisplay, criteriatitle, objectprefix);
    }

    private static String evalLabelExpression(String nodelabel, DataSet sdidataitem, int row) {
        nodelabel = StringUtil.replaceAll(nodelabel, "[paramlistid]", sdidataitem.getValue(row, "paramlistid"));
        nodelabel = StringUtil.replaceAll(nodelabel, "[paramlistversionid]", sdidataitem.getValue(row, "paramlistversionid"));
        nodelabel = StringUtil.replaceAll(nodelabel, "[variantid]", sdidataitem.getValue(row, "variantid"));
        nodelabel = StringUtil.replaceAll(nodelabel, "[paramid]", sdidataitem.getValue(row, "paramid"));
        nodelabel = StringUtil.replaceAll(nodelabel, "[paramtype]", sdidataitem.getValue(row, "paramtype"));
        String[] customtokens = StringUtil.getTokens(nodelabel);
        String colid = "";
        for (int t = 0; t < customtokens.length; ++t) {
            if (customtokens[t].indexOf("param.") == 0) {
                colid = customtokens[t].substring(6);
                nodelabel = StringUtil.replaceAll(nodelabel, "[" + customtokens[t] + "]", sdidataitem.getValue(row, colid, sdidataitem.getValue(row, "paramid")));
                continue;
            }
            if (customtokens[t].indexOf("paramlist.") != 0) continue;
            colid = customtokens[t].substring(10);
            nodelabel = StringUtil.replaceAll(nodelabel, "[" + customtokens[t] + "]", sdidataitem.getValue(row, colid, sdidataitem.getValue(row, "paramlistid")));
        }
        return nodelabel;
    }
}


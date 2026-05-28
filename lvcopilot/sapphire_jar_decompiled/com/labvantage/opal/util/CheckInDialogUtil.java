/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.format.RelativeDateFormat;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CheckInDialogUtil {
    private PageTagInfo pageinfo;
    private SDCProcessor sdcProcessor;
    private QueryProcessor queryProcessor;
    private TranslationProcessor translationProcessor;
    private DAMProcessor damProcessor;
    private PropertyList pagedata;
    private RelativeDateFormat relativeDateFormat;
    private Set<String> excludeChangeLogSet;
    private boolean isCMTAdmin;
    private boolean isRemoteChangeControl;
    private static Map<String, String> viewWebPageMap = new HashMap<String, String>();
    private static List<String> disableViewLinkSDCList = new ArrayList<String>();

    public CheckInDialogUtil(PageTagInfo pageinfo, PageContext pagecontext) {
        this.pageinfo = pageinfo;
        this.sdcProcessor = pageinfo.getSDCProcessor();
        this.queryProcessor = pageinfo.getQueryProcessor();
        this.translationProcessor = new TranslationProcessor(pagecontext);
        this.damProcessor = new DAMProcessor(pagecontext);
        this.pagedata = pageinfo.getPropertyList("pagedata");
        try {
            this.relativeDateFormat = new RelativeDateFormat(false, new ConfigurationProcessor(pagecontext).getPolicy("DateFormatPolicy", "Sapphire Custom", false), this.translationProcessor);
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        this.excludeChangeLogSet = new HashSet<String>();
        CMTPolicy cmtPolicy = CMTPolicy.getPolicy(pageinfo.getConnectionId(), "");
        this.isCMTAdmin = pageinfo.getConnectionProcessor().getSapphireConnection().hasRole(cmtPolicy.getCMTAdminRoleID());
        this.isRemoteChangeControl = cmtPolicy.isMasterRepositoryEnabled();
    }

    public SDCProcessor getSDCProcessor() {
        return this.sdcProcessor;
    }

    public QueryProcessor getQueryProcessor() {
        return this.queryProcessor;
    }

    public TranslationProcessor getTranslationProcessor() {
        return this.translationProcessor;
    }

    public DAMProcessor getDAMProcessor() {
        return this.damProcessor;
    }

    public String getHTML() {
        String sdcid = this.pagedata.getProperty("sdcid");
        String keyid1 = this.pagedata.getProperty("keyid1");
        String keyid2 = this.pagedata.getProperty("keyid2");
        String keyid3 = this.pagedata.getProperty("keyid3");
        String propertyTreeNodeId = this.pagedata.getProperty("propertytreenodeid");
        StringBuilder html = new StringBuilder();
        if ("LV_ChangeLog".equalsIgnoreCase(sdcid)) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet changeLogDS = this.getQueryProcessor().getPreparedSqlDataSet("select changelogid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, propertytreenodeid from changelog where changelogid in (" + safeSQL.addIn(keyid1, ";") + ")", safeSQL.getValues());
            if (changeLogDS != null && changeLogDS.size() > 0) {
                changeLogDS.sort("linksdcid");
                ArrayList<DataSet> dsList = changeLogDS.getGroupedDataSets("linksdcid");
                for (DataSet ds : dsList) {
                    sdcid = ds.getString(0, "linksdcid");
                    int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                    keyid1 = ds.getColumnValues("linkkeyid1", ";");
                    keyid2 = keycolumns > 1 ? ds.getColumnValues("linkkeyid2", ";") : "";
                    keyid3 = keycolumns > 2 ? ds.getColumnValues("linkkeyid3", ";") : "";
                    propertyTreeNodeId = ds.getColumnValues("propertytreenodeid", ";");
                    html.append(this.getPrimarySDIHTML(sdcid, keyid1, keyid2, keyid3, propertyTreeNodeId));
                }
            }
        } else {
            html.append(this.getPrimarySDIHTML(sdcid, keyid1, keyid2, keyid3, propertyTreeNodeId));
        }
        if (this.excludeChangeLogSet.size() > 0) {
            String otheritems;
            if (!"Y".equals(this.pagedata.getProperty("save")) && (otheritems = this.getOtherSDIHTML()).length() > 0) {
                HashMap<String, String> tokenMap = new HashMap<String, String>();
                tokenMap.put("size", String.valueOf(StringUtil.split(otheritems, ";").length));
                html.append("<div style='padding-left:10px;padding-right:10px;padding-bottom:10px;'>");
                html.append("<a href='javascript:showOtherItems();' id='showotheritemlink'>");
                html.append(this.getTranslationProcessor().translate("Found [size] other checked out items. Click here to view.", tokenMap));
                html.append("</a>");
                html.append("<div id='otheritemsdiv'></div>");
                html.append("<script type='text/javascript'>");
                html.append("var otheritemids = \"").append(this.getOtherSDIHTML()).append("\";");
                html.append("</script>");
                html.append("</div>");
            }
        } else {
            html.append("<script type='text/javascript'>");
            html.append("handleNoCheckOutItemFound();");
            html.append("</script>");
        }
        html.append("<script>disableDeletedChangeLogRows();</script>");
        return html.toString();
    }

    private DataSet getUserCheckedOutDataSet() {
        SapphireConnection sapphireConnection = this.pageinfo.getConnectionProcessor().getSapphireConnection();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select changelogid, changelogstatus, changerequestid, checkedoutbyuserid, checkedoutdt, checkedoutbydepartmentid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3 from changelog where changelogstatus = 'Checked Out' and (checkedoutbyuserid = " + safeSQL.addVar(sapphireConnection.getSysuserId()) + " or checkedoutbydepartmentid in (" + safeSQL.addIn(sapphireConnection.getDepartmentList(), ";") + "))";
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
    }

    private String getPrimarySDIHTML(String sdcid, String keyid1, String keyid2, String keyid3, String propertyTreeNodeId) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='padding-left:10px;padding-right:10px;padding-bottom:10px;'>");
        html.append("<table cellpadding=2 class='maintform_table' style='width:100%'>");
        String sysuserid = this.pageinfo.getConnectionProcessor().getSapphireConnection().getSysuserId();
        DataSet requestedSDIs = new DataSet();
        requestedSDIs.addColumn("linksdcid", 0);
        requestedSDIs.addColumn("linkkeyid1", 0);
        requestedSDIs.addColumn("linkkeyid2", 0);
        requestedSDIs.addColumn("linkkeyid3", 0);
        requestedSDIs.addColumn("propertytreenodeid", 0);
        requestedSDIs.addColumnValues("linksdcid", 0, sdcid, ";");
        requestedSDIs.addColumnValues("linkkeyid1", 0, keyid1, ";");
        requestedSDIs.addColumnValues("linkkeyid2", 0, keyid2, ";");
        requestedSDIs.addColumnValues("linkkeyid3", 0, keyid3, ";");
        requestedSDIs.addColumnValues("propertytreenodeid", 0, propertyTreeNodeId, ";");
        requestedSDIs.padColumn("linksdcid");
        int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
        html.append(CheckInDialogUtil.renderHeaderRow(this.getSDCProcessor(), this.getTranslationProcessor(), sdcid, true, true, "Y".equals(this.pagedata.getProperty("save"))));
        try {
            String checkInMessage;
            int i;
            String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
            String sql = "select changelog.changelogid, changelog.changelogstatus, changelog.changerequestid, changelog.checkedoutbyuserid, changelog.checkedoutdt, changelog.checkedoutbydepartmentid, changelog.linksdcid, changelog.linkkeyid1, changelog.linkkeyid2, changelog.linkkeyid3, changelog.propertytreenodeid from changelog, rsetitems where changelog.linksdcid = rsetitems.sdcid and changelog.linkkeyid1 = rsetitems.keyid1";
            if (keycolumns > 1) {
                sql = sql + " and changelog.linkkeyid2 = rsetitems.keyid2";
            }
            if (keycolumns > 2) {
                sql = sql + " and changelog.linkkeyid3 = rsetitems.keyid3";
            }
            sql = sql + " and rsetitems.rsetid = ?";
            sql = sql + " and changelog.changelogstatus = 'Checked Out'";
            DataSet checkedOutSDIs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
            if ("PropertyTree".equals(sdcid) && checkedOutSDIs.getRowCount() > 0) {
                DataSet selectedKeyIds = new DataSet();
                selectedKeyIds.addColumn("propertytreeid", 0);
                selectedKeyIds.addColumn("propertytreenodeid", 0);
                selectedKeyIds.addColumnValues("propertytreeid", 0, keyid1, ";");
                selectedKeyIds.addColumnValues("propertytreenodeid", 0, propertyTreeNodeId, ";");
                HashMap<String, String> findMap = new HashMap<String, String>();
                for (i = checkedOutSDIs.getRowCount() - 1; i >= 0; --i) {
                    String linkSDCId = checkedOutSDIs.getString(i, "linksdcid");
                    String linkKeyId1 = checkedOutSDIs.getString(i, "linkkeyid1");
                    String linkTreeNodeId = checkedOutSDIs.getString(i, "propertytreenodeid", "");
                    if (!"PropertyTree".equals(linkSDCId)) continue;
                    if (linkTreeNodeId.length() == 0) {
                        for (int j = 0; j < selectedKeyIds.getRowCount(); ++j) {
                            if (linkKeyId1.equals(selectedKeyIds.getString(j, "propertytreeid")) && selectedKeyIds.getString(j, "propertytreenodeid", "").length() == 0) continue;
                            checkedOutSDIs.deleteRow(i);
                        }
                        continue;
                    }
                    findMap.clear();
                    findMap.put("propertytreeid", linkKeyId1);
                    findMap.put("propertytreenodeid", linkTreeNodeId);
                    if (selectedKeyIds.findRow(findMap) != -1) continue;
                    checkedOutSDIs.deleteRow(i);
                }
            }
            boolean checkedInRowsRendered = false;
            boolean notCheckedOutRowsRendered = false;
            if (checkedOutSDIs != null && checkedOutSDIs.size() > 0) {
                for (i = 0; i < checkedOutSDIs.size(); ++i) {
                    String changelogid = checkedOutSDIs.getString(i, "changelogid", "");
                    this.excludeChangeLogSet.add(changelogid);
                    String checkedoutbyuserid = checkedOutSDIs.getString(i, "checkedoutbyuserid", "");
                    checkInMessage = "";
                    if (!sysuserid.equals(checkedoutbyuserid)) {
                        String checkedoutbydepartmentid = checkedOutSDIs.getString(0, "checkedoutbydepartmentid", "");
                        if (checkedoutbydepartmentid.length() > 0) {
                            if (!(";" + this.pageinfo.getConnectionProcessor().getSapphireConnection().getDepartmentList() + ";").contains(";" + checkedoutbydepartmentid + ";")) {
                                checkInMessage = this.isCMTAdmin ? "CMTAdmin;Department;" + checkedoutbydepartmentid : this.getTranslationProcessor().translate("Not allowed to Check In. Checked out to another department.");
                            }
                        } else {
                            checkInMessage = this.isCMTAdmin ? "CMTAdmin;User;" + checkedoutbyuserid : this.getTranslationProcessor().translate("Not allowed to Check In. Checked out by another user.");
                        }
                    }
                    html.append(CheckInDialogUtil.renderDataRow(this.getSDCProcessor(), this.getQueryProcessor(), this.getTranslationProcessor(), this.relativeDateFormat, sdcid, changelogid, checkedOutSDIs, i, checkInMessage, true, "Y".equals(this.pagedata.getProperty("save"))));
                    checkedInRowsRendered = true;
                }
            }
            if (this.isRemoteChangeControl) {
                HashMap<String, String> findMap = new HashMap<String, String>();
                for (int i2 = 0; i2 < requestedSDIs.getRowCount(); ++i2) {
                    String requestedSDCId = requestedSDIs.getString(0, "linksdcid");
                    findMap.clear();
                    findMap.put("linksdcid", requestedSDCId);
                    findMap.put("linkkeyid1", requestedSDIs.getString(i2, "linkkeyid1"));
                    if (keycolumns > 1) {
                        findMap.put("linkkeyid2", requestedSDIs.getString(i2, "linkkeyid2"));
                        if (keycolumns > 2) {
                            findMap.put("linkkeyid3", requestedSDIs.getString(i2, "linkkeyid3"));
                        }
                    }
                    if ("PropertyTree".equals(requestedSDCId)) {
                        findMap.put("propertytreenodeid", requestedSDIs.getString(i2, "propertytreenodeid"));
                    }
                    if (checkedOutSDIs.findRow(findMap) != -1) continue;
                    checkInMessage = "";
                    checkInMessage = checkedInRowsRendered ? "Item not Checked Out. Please Check Out OR Check In separately for newly created SDIs." : "<font color='black'><I>Item not Checked Out. SDI will be exported directly to Master Repository.</I></font>";
                    DataSet requestedSDIs2 = new DataSet();
                    requestedSDIs2.copyRow(requestedSDIs, i2, 1);
                    html.append(CheckInDialogUtil.renderDataRow(this.getSDCProcessor(), this.getQueryProcessor(), this.getTranslationProcessor(), this.relativeDateFormat, requestedSDCId, "", requestedSDIs2, i2, checkInMessage, true, false, true));
                    notCheckedOutRowsRendered = true;
                }
            }
            if (!checkedInRowsRendered) {
                if (this.isRemoteChangeControl) {
                    html.append("\n<script>var remotecheckinnotcheckedoutitems=true;</script>");
                } else {
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(sdcid);
                    sdiRequest.setKeyid1List(keyid1);
                    sdiRequest.setKeyid2List(keyid2);
                    sdiRequest.setKeyid3List(keyid3);
                    sdiRequest.setRequestItem("primary");
                    SDIProcessor sdiProcessor = new SDIProcessor(this.getSDCProcessor().getConnectionid());
                    DataSet primary = sdiProcessor.getSDIData(sdiRequest).getDataset("primary");
                    if (primary != null && primary.size() > 0) {
                        html.append("<tr>");
                        html.append("<td class='maintform_field' colspan=6 style='color:red'>");
                        html.append(this.getTranslationProcessor().translate("None of the selected item is checked out."));
                        html.append("</td>");
                        html.append("</tr>");
                    } else {
                        html.append("<tr>");
                        html.append("<td class='maintform_field' colspan=6 style='color:red'>");
                        html.append(this.getTranslationProcessor().translate("User is not authorized to perform this Check In Operation. Please check if User has proper Role and/or access level."));
                        html.append("</td>");
                        html.append("</tr>");
                    }
                }
            }
        }
        catch (Exception e) {
            html.append("Exception: ").append(e.getMessage());
        }
        html.append("</table></div>");
        return html.toString();
    }

    public static String renderDataRow(SDCProcessor sdcProcessor, QueryProcessor queryProcessor, TranslationProcessor translationProcessor, RelativeDateFormat relativeDateFormat, String sdcid, String changelogid, DataSet ds, int row, String checkInMessage, boolean primary, boolean isSaveOperation) {
        return CheckInDialogUtil.renderDataRow(sdcProcessor, queryProcessor, translationProcessor, relativeDateFormat, sdcid, changelogid, ds, row, checkInMessage, primary, isSaveOperation, false);
    }

    private static String renderDataRow(SDCProcessor sdcProcessor, QueryProcessor queryProcessor, TranslationProcessor translationProcessor, RelativeDateFormat relativeDateFormat, String sdcid, String changelogid, DataSet requestedSDIs, int row, String checkInMessage, boolean primary, boolean isSaveOperation, boolean isRemoteChangeControlRow) {
        String[] tokens;
        int keycolumns = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        String keyDisplayFormat = "";
        String keyDisplayLink_href = "";
        String keyDisplayLink_tip = "";
        if ("LV_Worksheet".equals(sdcid)) {
            keyDisplayFormat = "[" + keycolid1 + "], [" + keycolid2 + "]";
            keyDisplayLink_href = "rc?command=page&page=WorksheetPopup&wsmmode=WorksheetViewer&sdcid=LV_Worksheet&keyid1=[linkkeyid1]&keyid2=[linkkeyid2]";
            keyDisplayLink_tip = "View Worksheet";
        } else if ("Language".equals(sdcid)) {
            keyDisplayFormat = "[" + keycolid1 + "]";
            keyDisplayLink_href = "rc?command=page&page=LV_LanguageView&mode=View&sdcid=Language&keyid1=[linkkeyid1]";
            keyDisplayLink_tip = "View Language";
        } else if ("Study".equals(sdcid)) {
            keyDisplayFormat = "[" + keycolid1 + "]";
            keyDisplayLink_href = "rc?command=page&page=LV_StudyView&mode=View&sdcid=Study&keyid1=[linkkeyid1]";
            keyDisplayLink_tip = "View Study";
        } else if ("LV_SAPMsgType".equals(sdcid)) {
            keyDisplayFormat = "[" + keycolid1 + "]";
            keyDisplayLink_href = "rc?command=page&page=SAPMsgTypeView&mode=View&sdcid=LV_SAPMsgType&keyid1=[linkkeyid1]";
            keyDisplayLink_tip = "View Message Type";
        } else if ("LV_Calendar".equals(sdcid)) {
            keyDisplayFormat = "[" + keycolid1 + "]";
            keyDisplayLink_href = "rc?command=page&page=Calendar&dialog=Y&viewonly=Y&sdcid=LV_Calendar&keyid1=[linkkeyid1]";
            keyDisplayLink_tip = "View Calendar";
        } else {
            String viewPage;
            if (!disableViewLinkSDCList.contains(sdcid) && (viewPage = CheckInDialogUtil.getViewWebPage(sdcid, queryProcessor)).length() > 0) {
                keyDisplayLink_href = "rc?command=page&page=" + viewPage + "&mode=view&sdcid=" + sdcid + "&keyid1=[linkkeyid1]";
                if (keycolumns > 1) {
                    keyDisplayLink_href = keyDisplayLink_href + "&keyid2=[linkkeyid2]";
                }
                if (keycolumns > 2) {
                    keyDisplayLink_href = keyDisplayLink_href + "&keyid3=[linkkeyid3]";
                }
                keyDisplayLink_tip = "View [singular]";
            }
            if (OpalUtil.isEmpty(keyDisplayFormat = sdcProcessor.getProperty(sdcid, "itemdisplay"))) {
                keyDisplayFormat = "[" + keycolid1 + "]";
                if (keycolumns > 1) {
                    keyDisplayFormat = keyDisplayFormat + ", [" + keycolid2 + "]";
                }
                if (keycolumns > 2) {
                    keyDisplayFormat = keyDisplayFormat + ", [" + keycolid3 + "]";
                }
            }
        }
        StringBuilder html = new StringBuilder();
        if (isRemoteChangeControlRow) {
            html.append("<tr class='datarow otherdatarow' changerequestid=\"").append(requestedSDIs.getString(row, "changerequestid", "")).append("\">");
            html.append("<td class='maintform_field'><input type='checkbox' checked='true' disabled></td>");
        } else if (primary) {
            html.append("<tr class='datarow primarydatarow' changerequestid=\"").append(requestedSDIs.getString(row, "changerequestid", "")).append("\">");
            html.append("<td class='maintform_field'>");
            if (checkInMessage.length() == 0 || checkInMessage.startsWith("CMTAdmin")) {
                html.append("<input type='checkbox' checked class='selector' changelogid='").append(changelogid).append("'").append(isSaveOperation ? " disabled" : "").append(">");
            } else {
                html.append("<input type='checkbox' disabled>");
            }
            html.append("</td>");
        } else {
            html.append("<tr class='datarow otherdatarow' changerequestid=\"").append(requestedSDIs.getString(row, "changerequestid", "")).append("\">");
            html.append("<td class='maintform_field'><input type='checkbox' class='selector' changelogid='").append(changelogid).append("'></td>");
        }
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        tokenMap.put("singular", sdcProcessor.getProperty(sdcid, "singular"));
        tokenMap.put("plural", sdcProcessor.getProperty(sdcid, "plural"));
        html.append("<td class='maintform_field'>");
        String sql = "";
        ArrayList dataset = null;
        if (changelogid.length() > 0) {
            sql = "select changelog.linksdcid, changelog.linkkeyid1, changelog.linkkeyid2, changelog.linkkeyid3,";
            sql = sql + " (select count(" + tableid + "." + keycolid1 + ") from " + tableid + " where " + tableid + "." + keycolid1 + " = changelog.linkkeyid1";
            sql = sql + (keycolumns > 1 ? " and " + tableid + "." + keycolid2 + " = changelog.linkkeyid2" : "");
            sql = sql + (keycolumns > 2 ? " and " + tableid + "." + keycolid3 + " = changelog.linkkeyid3" : "");
            sql = sql + " ) sdicount,";
            sql = sql + " " + tableid + ".*";
            sql = sql + " from changelog left outer join " + tableid;
            sql = sql + " on changelog.linksdcid = '" + sdcid + "'";
            sql = sql + " and changelog.linkkeyid1 = " + tableid + "." + keycolid1;
            sql = sql + (keycolumns > 1 ? " and changelog.linkkeyid2 = " + tableid + "." + keycolid2 : "");
            sql = sql + (keycolumns > 2 ? " and changelog.linkkeyid3 = " + tableid + "." + keycolid3 : "");
            sql = sql + " where changelog.changelogid = ?";
            dataset = queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{changelogid});
        } else if (isRemoteChangeControlRow) {
            sql = "SELECT * FROM " + tableid + " WHERE " + keycolid1 + " = '" + requestedSDIs.getString(0, "linkkeyid1") + "'" + (keycolumns > 1 ? " AND " + keycolid2 + " = '" + requestedSDIs.getString(0, "linkkeyid2") + "'" : "") + (keycolumns > 2 ? " AND " + keycolid3 + " = '" + requestedSDIs.getString(0, "linkkeyid3") + "'" : "");
            dataset = queryProcessor.getSqlDataSet(sql);
        }
        if (dataset != null && dataset.size() > 0) {
            tokens = StringUtil.getTokens(keyDisplayFormat);
            boolean sdiexists = ((DataSet)dataset).getInt(0, "sdicount", 1) > 0;
            for (String colId : requestedSDIs.getColumns()) {
                tokenMap.put(colId, requestedSDIs.getValue(0, colId, ""));
            }
            for (String colId : ((DataSet)dataset).getColumns()) {
                tokenMap.put(colId, ((DataSet)dataset).getValue(0, colId, ""));
            }
            String[] stringArray = tokens;
            int n = stringArray.length;
            for (int i = 0; i < n; ++i) {
                String token = stringArray[i];
                String value = tokenMap.getOrDefault(token, "");
                if (value.length() == 0) {
                    if (token.equals(keycolid1)) {
                        value = tokenMap.getOrDefault("linkkeyid1", tokenMap.getOrDefault(keycolid1, ""));
                    } else if (token.equals(keycolid2)) {
                        value = tokenMap.getOrDefault("linkkeyid2", tokenMap.getOrDefault(keycolid2, ""));
                    } else if (token.equals(keycolid3)) {
                        value = tokenMap.getOrDefault("linkkeyid3", tokenMap.getOrDefault(keycolid3, ""));
                    }
                    keyDisplayFormat = StringUtil.replaceAll(keyDisplayFormat, "[" + token + "]", value);
                    if (OpalUtil.isEmpty(keyDisplayFormat)) {
                        keyDisplayFormat = ((DataSet)dataset).getValue(0, "linkkeyid1");
                        keyDisplayFormat = keyDisplayFormat + (keycolumns > 1 ? " (" + ((DataSet)dataset).getValue(0, "linkkeyid2") + ")" : "");
                        keyDisplayFormat = keyDisplayFormat + (keycolumns > 2 ? " (" + ((DataSet)dataset).getValue(0, "linkkeyid3") + ")" : "");
                    }
                    if (sdiexists) continue;
                    keyDisplayFormat = "<span class='cldeleted' title='" + translationProcessor.translate("Deleted") + "'>" + keyDisplayFormat + "</span>";
                    continue;
                }
                keyDisplayFormat = StringUtil.replaceAll(keyDisplayFormat, "[" + token + "]", value);
            }
            if ("PropertyTree".equals(sdcid) && tokenMap.getOrDefault("propertytreenodeid", "").length() > 0) {
                keyDisplayFormat = keyDisplayFormat + ", " + tokenMap.getOrDefault("propertytreenodeid", "");
            }
        }
        if (!keyDisplayFormat.contains("class='cldeleted'") && !disableViewLinkSDCList.contains(sdcid) && keyDisplayLink_href.length() > 0) {
            for (String token : tokens = StringUtil.getTokens(keyDisplayLink_href)) {
                keyDisplayLink_href = CheckInDialogUtil.substituteToken(keycolumns, keyDisplayLink_href, token, tokenMap);
            }
            if (keyDisplayLink_tip.length() > 0) {
                tokens = StringUtil.getTokens(keyDisplayLink_tip);
            }
            for (String token : tokens) {
                keyDisplayLink_tip = CheckInDialogUtil.substituteToken(keycolumns, keyDisplayLink_tip, token, tokenMap);
            }
            html.append("<a href=\"javascript:viewHrefLink('").append(keyDisplayLink_href).append("');\" title=\"").append(keyDisplayLink_tip).append("\">");
            html.append(keyDisplayFormat);
            html.append("</a>");
        } else {
            html.append(keyDisplayFormat);
        }
        if (checkInMessage.length() > 0) {
            if (checkInMessage.startsWith("CMTAdmin")) {
                html.append("<div style='color:#e67e22;padding-top:2px'>");
                if (checkInMessage.contains(";Department;")) {
                    html.append(translationProcessor.translate("Checked out to other department")).append(" (").append(checkInMessage.substring(checkInMessage.lastIndexOf(";") + 1)).append(")");
                } else {
                    html.append(translationProcessor.translate("Checked out to other user")).append(" (").append(checkInMessage.substring(checkInMessage.lastIndexOf(";") + 1)).append(")");
                }
                html.append("</div>");
            } else {
                html.append("<div style='color:red;padding-top:2px'>").append(checkInMessage).append("</div>");
            }
        }
        html.append("</td>");
        if (isRemoteChangeControlRow) {
            html.append("<td class='maintform_field'>&nbsp;</td>");
            html.append("<td class='maintform_field'>&nbsp;</td>");
            html.append("<td class='maintform_field'>&nbsp;</td>");
        } else {
            html.append("<td class='maintform_field'>").append(relativeDateFormat.format(requestedSDIs.getCalendar(row, "checkedoutdt").getTime())).append("</td>");
            html.append("<td class='maintform_field'>");
            String changerequestid = requestedSDIs.getValue(row, "changerequestid", "");
            if (changerequestid.length() > 0) {
                html.append("<a href=\"javascript:top.sapphire.cmt.viewChangeRequest('").append(changerequestid).append("');\">").append(changerequestid).append("</a>");
            } else {
                html.append("&nbsp;");
            }
            html.append("</td>");
            html.append("<td class='maintform_field' style='width:20px;'>");
            html.append("<img title='").append(translationProcessor.translate("View Changes")).append("' src='rc?command=image&image=Elements&size=32' width='20' height='20' style='cursor:pointer' onclick=\"top.sapphire.cmt.viewChangeLogChanges('").append(requestedSDIs.getString(row, "changelogid")).append("')\">");
        }
        html.append("</th>");
        html.append("</tr>");
        return html.toString();
    }

    public static String renderHeaderRow(SDCProcessor sdcProcessor, TranslationProcessor translationProcessor, String sdcid, boolean primary, boolean isCheckIn, boolean isSaveOperation) {
        int keycolumns = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
        StringBuilder html = new StringBuilder();
        html.append("<tr>");
        html.append("<th class='maintform_fieldtitle' style='width:20px'>");
        if (isCheckIn) {
            html.append("<input type='checkbox'").append(primary ? " checked" : "").append(" primary='").append(primary ? "Y" : "N").append("'");
            if (isSaveOperation) {
                html.append(" disabled");
            }
            html.append(" class='selectall'>");
        }
        html.append("</th>");
        if (isCheckIn) {
            PropertyList keyColumnProps = new PropertyList();
            keyColumnProps.setProperty("title", "[singular]");
            keyColumnProps.setProperty("displayformat", "[keycolumn1label][has2keys|, ][keycolumn2label][has3keys|, ][keycolumn3label]");
            String keyDisplayTitle = keyColumnProps.getProperty("title");
            HashMap<String, String> sdcTokenMap = new HashMap<String, String>();
            sdcTokenMap.put("sdcid", sdcProcessor.getProperty(sdcid, "sdcid"));
            sdcTokenMap.put("singular", sdcProcessor.getProperty(sdcid, "singular"));
            sdcTokenMap.put("plural", sdcProcessor.getProperty(sdcid, "plural"));
            sdcTokenMap.put("tableid", sdcProcessor.getProperty(sdcid, "tableid"));
            sdcTokenMap.put("keycolumn1label", sdcProcessor.getSDCColumnProperty(sdcid, sdcProcessor.getProperty(sdcid, "keycolid1"), "columnlabel"));
            sdcTokenMap.put("keycolumn2label", sdcProcessor.getSDCColumnProperty(sdcid, sdcProcessor.getProperty(sdcid, "keycolid2"), "columnlabel"));
            sdcTokenMap.put("keycolumn3label", sdcProcessor.getSDCColumnProperty(sdcid, sdcProcessor.getProperty(sdcid, "keycolid3"), "columnlabel"));
            String[] tokens = StringUtil.getTokens(keyDisplayTitle);
            if (tokens.length > 0) {
                for (String token : tokens) {
                    keyDisplayTitle = CheckInDialogUtil.substituteToken(keycolumns, keyDisplayTitle, token, sdcTokenMap);
                }
            }
            html.append("<th class='maintform_fieldtitle' style='text-align:left;' width='*'>").append(keyDisplayTitle).append("</th>");
        } else {
            html.append("<th class='maintform_fieldtitle' style='text-align:left;'>");
            html.append(translationProcessor.translate("Change Log"));
            html.append("</th>");
            html.append("<th class='maintform_fieldtitle' style='width:120px;'>");
            html.append(translationProcessor.translate("Status"));
            html.append("</th>");
        }
        html.append("<th class='maintform_fieldtitle' style='width:220px'>").append(translationProcessor.translate("Checked Out On")).append("</th>");
        html.append("<th class='maintform_fieldtitle' style='width:120px'>").append(translationProcessor.translate("Change Request")).append("</th>");
        if (isCheckIn) {
            html.append("<th class='maintform_fieldtitle' style='width:26px'>&nbsp;</th>");
        }
        html.append("</tr>");
        return html.toString();
    }

    private String getOtherSDIHTML() {
        StringBuilder html = new StringBuilder();
        SapphireConnection sapphireConnection = this.pageinfo.getConnectionProcessor().getSapphireConnection();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select changelog.changelogid, changelog.changelogstatus, changelog.changerequestid, changelog.checkedoutbyuserid, changelog.checkedoutdt, changelog.checkedoutbydepartmentid, changelog.linksdcid, changelog.linkkeyid1, changelog.linkkeyid2, changelog.linkkeyid3, changelog.propertytreenodeid from changelog where changelog.changelogstatus = 'Checked Out' and changelog.changelogid not in (" + safeSQL.addIn(this.excludeChangeLogSet) + ") and (changelog.checkedoutbyuserid = " + safeSQL.addVar(sapphireConnection.getSysuserId()) + " or checkedoutbydepartmentid in (" + safeSQL.addIn(sapphireConnection.getDepartmentList(), ";") + "))";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            html.append(ds.getColumnValues("changelogid", ";"));
        }
        return html.toString();
    }

    private static String substituteToken(int keycolumns, String expression, String token, Map<String, String> tokenMap) {
        boolean substitute = true;
        String value = "";
        String prefix = "";
        if (token.startsWith("has2keys|")) {
            if (keycolumns > 1) {
                prefix = "has2keys|";
                token = token.substring(9);
            } else {
                substitute = false;
                value = "";
            }
        } else if (token.startsWith("has3keys|")) {
            if (keycolumns > 2) {
                prefix = "has3keys|";
                token = token.substring(9);
            } else {
                substitute = false;
                value = "";
            }
        }
        if (substitute) {
            value = tokenMap.containsKey(token) ? tokenMap.get(token) : token;
        }
        return StringUtil.replaceAll(expression, "[" + prefix + token + "]", value);
    }

    private static String getViewWebPage(String sdcid, QueryProcessor queryProcessor) {
        if (!viewWebPageMap.containsKey(sdcid)) {
            String sql = "select webpageid from webpage where webpageid = ? or webpageid = ?";
            DataSet dsWebPage = queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{sdcid + "View", "LV_" + sdcid + "View"});
            if (dsWebPage != null && dsWebPage.size() > 0) {
                viewWebPageMap.put(sdcid, dsWebPage.getString(0, "webpageid", ""));
            } else {
                viewWebPageMap.put(sdcid, "");
            }
        }
        return viewWebPageMap.getOrDefault(sdcid, "");
    }

    static {
        disableViewLinkSDCList.add("WebPage");
        disableViewLinkSDCList.add("PropertyTree");
        disableViewLinkSDCList.add("LV_WorkflowDef");
    }
}


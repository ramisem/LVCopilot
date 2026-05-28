/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.services;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.License;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.BaseService;
import com.labvantage.sapphire.services.DDTConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.util.ArrayList;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DDTService
extends BaseService
implements CacheNames,
DDTConstants {
    public static final String LOGNAME = "DDTService";

    public DDTService(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.logName = LOGNAME;
    }

    public PropertyList getSDCProperties(String sdcid) throws ServiceException {
        this.logDebug("Getting properties for sdcid '" + sdcid + "'");
        if (sdcid == null || sdcid.length() == 0) {
            throw new ServiceException("INVALID_PARAMETER", "SDCID not defined");
        }
        PropertyList sdcPropertyList = (PropertyList)CacheUtil.get(this.connectionInfo.getDatabaseId(), "SDC", sdcid.toLowerCase());
        if (sdcPropertyList == null) {
            this.retrieveProperties(sdcid);
            sdcPropertyList = (PropertyList)CacheUtil.get(this.connectionInfo.getDatabaseId(), "SDC", sdcid.toLowerCase());
        }
        return sdcPropertyList;
    }

    public DataSet getReverseLinksData(String sdcid) throws ServiceException {
        this.logDebug("Getting reverse links data for sdcid '" + sdcid + "'");
        PropertyList sdcPropertyList = this.getSDCProperties(sdcid);
        DataSet reverseLinksData = new DataSet();
        PropertyListCollection reverseLinks = sdcPropertyList.getCollection("reverselinks");
        for (int i = 0; i < reverseLinks.size(); ++i) {
            PropertyList reverseLink = reverseLinks.getPropertyList(i);
            int newrow = reverseLinksData.addRow();
            reverseLinksData.setString(newrow, "tableid", reverseLink.getProperty("tableid"));
            reverseLinksData.setString(newrow, "sdcid", reverseLink.getProperty("sdcid"));
            reverseLinksData.setString(newrow, "linksdcid", reverseLink.getProperty("linksdcid"));
            reverseLinksData.setString(newrow, "linksdccolumnid", reverseLink.getProperty("linksdccolumnid"));
            reverseLinksData.setString(newrow, "linkid", reverseLink.getProperty("linkid"));
            reverseLinksData.setString(newrow, "linktype", reverseLink.getProperty("linktype"));
            reverseLinksData.setString(newrow, "linktableid", reverseLink.getProperty("linktableid"));
            reverseLinksData.setString(newrow, "linksequence", reverseLink.getProperty("linksequence"));
            reverseLinksData.setString(newrow, "reftypeid", reverseLink.getProperty("reftypeid"));
            reverseLinksData.setString(newrow, "sdccolumnid", reverseLink.getProperty("sdccolumnid"));
            reverseLinksData.setString(newrow, "sdccolumnid2", reverseLink.getProperty("sdccolumnid2"));
            reverseLinksData.setString(newrow, "sdccolumnid3", reverseLink.getProperty("sdccolumnid3"));
            reverseLinksData.setString(newrow, "deleteflag", reverseLink.getProperty("deleteflag"));
            reverseLinksData.setString(newrow, "loadflag", reverseLink.getProperty("loadflag"));
            reverseLinksData.setString(newrow, "usersequence", reverseLink.getProperty("usersequence"));
            reverseLinksData.setString(newrow, "auditsequence", reverseLink.getProperty("auditsequence"));
            reverseLinksData.setString(newrow, "lookuppageid", reverseLink.getProperty("lookuppageid"));
            reverseLinksData.setString(newrow, "userflag", reverseLink.getProperty("userflag"));
        }
        return reverseLinksData;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void retrieveProperties(String sdcid) throws ServiceException {
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            DataSet tableLabelInfo;
            String linktype;
            dbu.setConnection(this.sapphireConnection);
            dbu.createPreparedResultSet("SELECT * FROM sdc, systable WHERE systable.tableid = sdc.tableid and lower( sdcid ) = ?", new Object[]{sdcid.toLowerCase()});
            boolean pseudocolumnSDC = DDTService.isPseudocolumnSDC(sdcid);
            PropertyList sdc = new PropertyList();
            try {
                if (!dbu.getNext()) {
                    throw new ServiceException("INVALID_PARAMETER", "Unrecognized sdcid '" + sdcid + "'");
                }
                sdc.setProperty("sdcid", dbu.getString("sdcid"));
                sdc.setProperty("description", dbu.getString("sdcdesc"));
                sdc.setProperty("tableid", dbu.getString("tableid"));
                sdc.setProperty("sdctype", dbu.getString("sdctype"));
                sdc.setProperty("singular", dbu.getString("singular"));
                sdc.setProperty("plural", dbu.getString("plural"));
                sdc.setProperty("tablelabel", dbu.getString("tablelabel"));
                sdc.setProperty("itemdisplay", dbu.getString("itemdisplay"));
                if (pseudocolumnSDC) {
                    String sdikeyid1 = dbu.getString("sdikeyid");
                    if (sdikeyid1 != null && sdikeyid1.length() > 0) {
                        sdc.setProperty("pseudokeycolumn", sdikeyid1);
                        sdc.setProperty("keycolumns", "1");
                    } else {
                        sdc.setProperty("pseudokeycolumn", "");
                        sdc.setProperty("keycolumns", dbu.getString("keycolumns"));
                    }
                } else {
                    sdc.setProperty("pseudokeycolumn", "");
                    sdc.setProperty("keycolumns", dbu.getString("keycolumns"));
                }
                sdc.setProperty("standardmaintflag", dbu.getString("standardmaintflag"));
                sdc.setProperty("versionedflag", dbu.getString("versionedflag"));
                sdc.setProperty("auditedflag", dbu.getString("auditedflag"));
                sdc.setProperty("auditpromptflag", dbu.getString("auditpromptflag"));
                sdc.setProperty("notesflag", dbu.getString("notesflag"));
                sdc.setProperty("accesscontrolledflag", dbu.getString("accesscontrolledflag"));
                sdc.setProperty("changecontrolledflag", dbu.getString("changecontrolledflag"));
                sdc.setProperty("attachmentsflag", dbu.getString("attachmentsflag"));
                sdc.setProperty("keygenerationrule", dbu.getString("keygenerationrule"));
                sdc.setProperty("addressesflag", dbu.getString("addressesflag"));
                sdc.setProperty("workflowflag", dbu.getString("workflowflag"));
                sdc.setProperty("dataentryflag", dbu.getString("dataentryflag"));
                sdc.setProperty("securityflag", dbu.getString("securityflag"));
                sdc.setProperty("chargeoptionflag", dbu.getString("chargeoptionflag"));
                sdc.setProperty("categoriesflag", dbu.getString("categoriesflag"));
                sdc.setProperty("pricelistflag", dbu.getString("pricelistflag"));
                sdc.setProperty("orderableflag", dbu.getString("orderableflag"));
                sdc.setProperty("cocableflag", dbu.getString("cocableflag"));
                sdc.setProperty("specflag", dbu.getString("specflag"));
                sdc.setProperty("templatableflag", dbu.getString("templatableflag"));
                sdc.setProperty("workitemflag", dbu.getString("workitemflag"));
                sdc.setProperty("aliasableflag", dbu.getString("aliasableflag"));
                sdc.setProperty("reftypeid", dbu.getString("reftypeid"));
                sdc.setProperty("searchableflag", dbu.getString("searchableflag"));
                sdc.setProperty("allowattributesflag", dbu.getString("allowattributesflag"));
                sdc.setProperty("coordinatableflag", dbu.getString("coordinatableflag"));
                sdc.setProperty("requiredattributesflag", "Y");
                sdc.setProperty("scheduleableflag", dbu.getString("scheduleableflag"));
                sdc.setProperty("keyidusablesize", dbu.getString("keyidusablesize"));
                sdc.setProperty("activeableflag", dbu.getString("activeableflag"));
                sdc.setProperty("compcode", dbu.getString("compcode"));
                sdc.setProperty("versionuseeffectivedtflag", dbu.getString("versionuseeffectivedtflag"));
                sdc.setProperty("versionapprovaltypeid", dbu.getString("versionapprovaltypeid"));
                sdc.setProperty("sensitivedataflag", dbu.getString("sensitivedataflag"));
                sdc.setProperty("maskableflag", dbu.getString("maskableflag"));
                sdc.setProperty("uuidflag", dbu.getString("uuidflag"));
                sdc.setProperty("maskdatavisibilityrule", dbu.getString("maskdatavisibilityrule"));
                sdc.setProperty("maskdatavisibilitylink", dbu.getString("maskdatavisibilitylink"));
                sdc.setProperty("defaultmaskinglevel", dbu.getString("defaultmaskinglevel"));
                if (sdcid.equalsIgnoreCase("SDC") || sdcid.equalsIgnoreCase("Action") || sdcid.equalsIgnoreCase("RefType") || sdcid.equalsIgnoreCase("WebPage") || sdcid.equalsIgnoreCase("PropertyTree") || sdcid.equalsIgnoreCase("Query") || sdcid.equalsIgnoreCase("Task") || sdcid.equalsIgnoreCase("LV_TaskDef") || sdcid.equalsIgnoreCase("LV_GizmoDef") || sdcid.equalsIgnoreCase("LV_DataFileDef") || sdcid.equalsIgnoreCase("LV_WorkflowDef") || sdcid.equalsIgnoreCase("LV_App") || sdcid.equalsIgnoreCase("LV_Portal")) {
                    sdc.setProperty("componentableflag", "Y");
                }
            }
            finally {
                dbu.closeResultSet();
            }
            sdcid = sdc.getProperty("sdcid");
            String tableid = sdc.getProperty("tableid");
            dbu.createPreparedResultSet("SELECT propertyid, propertyvalue FROM sdcproperty WHERE sdcid = ?", sdcid);
            try {
                while (dbu.getNext()) {
                    sdc.setProperty(dbu.getString("propertyid"), dbu.getString("propertyvalue"));
                }
            }
            finally {
                dbu.closeResultSet();
            }
            dbu.createPreparedResultSet("SELECT sdcid, tableidmap, keymap1 FROM syssdcmap WHERE sdcid = ?", sdcid);
            try {
                while (dbu.getNext()) {
                    sdc.setProperty("keymap1", dbu.getString("keymap1"));
                    sdc.setProperty("tableidmap", dbu.getString("tableidmap"));
                }
            }
            finally {
                dbu.closeResultSet();
            }
            if (sdc.getProperty("pseudokeycolumn", "").length() > 0) {
                sdc.setProperty("keycolid1", sdc.getProperty("pseudokeycolumn", ""));
            } else {
                String[] keyCols = DDTService.getKeyColumns(dbu, tableid);
                for (int i = 0; i < keyCols.length; ++i) {
                    sdc.setProperty("keycolid" + (i + 1), keyCols[i]);
                }
            }
            sdc.setProperty("desccol", DDTService.getDescCol(sdcid, sdc.getProperty("sdctype"), sdc.getProperty("compcode"), tableid, sdc.getProperty("keycolid1")));
            PropertyListCollection columnData = new PropertyListCollection();
            dbu.createPreparedResultSet("SELECT ss.columnid, ss.datatype, ss.columnlength, ss.pkflag, ss.columndesc, ss.columnlabel, ss.columndoc, ss.searchableflag,   sl.linkid, sl.linktype, sl.linksdcid, sl.reftypeid, sl.sdccolumnid, sl.sdccolumnid2,   sl.sdccolumnid3, ss.sdcid, ss.tableid, ss.columnsequence   FROM sdclink sl RIGHT OUTER JOIN    (    SELECT  s.sdcid, sc.tableid, sc.columnid, sc.datatype, sc.columnlength,        'Y' pkflag, sc.columndesc, sc.columnlabel, sc.columndoc, src.columnsequence, sc.searchableflag     FROM   syscolumn sc, sdc s, sysref sr,sysrefcolumn src     WHERE lower( s.sdcid ) = ?     AND s.tableid = sc.tableid     AND sr.tableid = s.tableid     AND sr.reftypeflag = 'P'     AND src.refid = sr.refid     AND src.columnid = sc.columnid     UNION     SELECT  s.sdcid, sc.tableid, sc.columnid, sc.datatype, sc.columnlength,       'N', sc.columndesc, sc.columnlabel, sc.columndoc, sc.columnsequence + 100, sc.searchableflag     FROM   syscolumn sc, sdc s     WHERE lower( s.sdcid ) = ?     AND s.tableid = sc.tableid     AND NOT EXISTS      (      SELECT null       FROM sysref sr, sysrefcolumn src       WHERE sr.tableid = s.tableid       AND sr.reftypeflag = 'P'       AND src.refid = sr.refid       AND src.columnid = sc.columnid      )    ) ss   ON sl.sdcid = ss.sdcid   AND sl.sdccolumnid = ss.columnid   ORDER BY ss.columnsequence, sl.linksequence", new Object[]{sdcid.toLowerCase(), sdcid.toLowerCase()});
            try {
                HashSet<String> addedColumnids = new HashSet<String>();
                while (dbu.getNext()) {
                    PropertyList column = new PropertyList(dbu.getString("columnid"));
                    String columnid = dbu.getString("columnid");
                    if (addedColumnids.contains(columnid)) continue;
                    addedColumnids.add(columnid);
                    String datatype = dbu.getString("datatype");
                    column.setProperty("columnid", columnid);
                    column.setProperty("columndesc", dbu.getString("columndesc"));
                    column.setProperty("columnlabel", dbu.getString("columnlabel"));
                    column.setProperty("columndoc", dbu.getString("columndoc"));
                    column.setProperty("datatype", datatype);
                    String keyidusablesize = sdc.getProperty("keyidusablesize");
                    if (keyidusablesize.length() == 0) {
                        keyidusablesize = "20";
                    }
                    if (columnid.equals(sdc.getProperty("keycolid2")) || columnid.indexOf("versionid") > 0) {
                        column.setProperty("columnlength", "20");
                    } else if (columnid.equals(sdc.getProperty("keycolid1")) || columnid.equals(sdc.getProperty("keycolid2")) || columnid.equals(sdc.getProperty("keycolid3"))) {
                        column.setProperty("columnlength", keyidusablesize);
                    } else {
                        column.setProperty("columnlength", dbu.getString("columnlength"));
                    }
                    column.setProperty("pkflag", dbu.getString("pkflag"));
                    column.setProperty("searchableflag", dbu.getString("searchableflag"));
                    PropertyList editorstyleprops = new PropertyList();
                    String columnreftypeid = "";
                    String columnsdcid = "";
                    if (dbu.getString("linkid") != null && dbu.getString("linkid").length() > 0) {
                        PropertyList link = new PropertyList(dbu.getString("linkid"));
                        String linktype2 = dbu.getString("linktype");
                        link.setProperty("id", dbu.getString("linkid"));
                        link.setProperty("type", linktype2);
                        link.setProperty("linksdcid", dbu.getString("linksdcid"));
                        link.setProperty("reftypeid", dbu.getString("reftypeid"));
                        link.setProperty("sdccolumnid", dbu.getString("sdccolumnid"));
                        link.setProperty("sdccolumnid2", dbu.getString("sdccolumnid2"));
                        link.setProperty("sdccolumnid3", dbu.getString("sdccolumnid3"));
                        link.setProperty("versionedflag", "N");
                        dbu.createPreparedResultSet("versionedlinkedsdcid", "SELECT versionedflag FROM sdc WHERE sdcid = ?", new Object[]{dbu.getString("linksdcid")});
                        if (dbu.getNext("versionedlinkedsdcid")) {
                            link.setProperty("versionedflag", dbu.getString("versionedlinkedsdcid", "versionedflag"));
                        }
                        column.setProperty("link", link);
                        if ("F".equals(linktype2)) {
                            columnreftypeid = dbu.getString("linksdcid");
                            if ("40".equals(dbu.getString("columnlength"))) {
                                String linksdcusablekeysize;
                                dbu.createPreparedResultSet("linksdcusablekeysize", "SELECT keyidusablesize FROM sdc WHERE sdcid = ?", new Object[]{dbu.getString("linksdcid")});
                                if (dbu.getNext("linksdcusablekeysize") && (linksdcusablekeysize = dbu.getString("linksdcusablekeysize", "keyidusablesize")) != null && linksdcusablekeysize.length() > 0) {
                                    column.setProperty("columnlength", linksdcusablekeysize);
                                }
                            }
                            editorstyleprops.setProperty("mode", "lookup");
                            editorstyleprops.setProperty("linksdcid", dbu.getValue("linksdcid"));
                        } else if ("R".equals(linktype2)) {
                            columnreftypeid = dbu.getValue("reftypeid");
                            if ("YesNo".equals(dbu.getValue("reftypeid"))) {
                                editorstyleprops.setProperty("mode", "checkbox");
                                editorstyleprops.setProperty("reftype", "YesNo");
                            } else {
                                editorstyleprops.setProperty("mode", "dropdownlist");
                                editorstyleprops.setProperty("reftypeid", dbu.getValue("reftypeid"));
                            }
                        } else if ("V".equals(linktype2)) {
                            columnreftypeid = dbu.getValue("reftypeid");
                            editorstyleprops.setProperty("mode", "dropdowncombo");
                            editorstyleprops.setProperty("reftypeid", dbu.getString("reftypeid"));
                        }
                    } else if (datatype.equals("D")) {
                        editorstyleprops.setProperty("mode", "datelookup");
                    } else if (columnid.equalsIgnoreCase("createby") || columnid.equalsIgnoreCase("modby")) {
                        editorstyleprops.setProperty("mode", "lookup");
                        editorstyleprops.setProperty("linksdcid", "User");
                    } else if (columnid.equalsIgnoreCase("sdcid")) {
                        editorstyleprops.setProperty("mode", "dropdownlist");
                        editorstyleprops.setProperty("sql", "SELECT sdcid FROM sdc ORDER BY 1");
                    }
                    dbu.createPreparedResultSet("syscolumnproperty", "SELECT propertyid, propertyvalue FROM syscolumnproperty WHERE tableid = ? AND columnid = ?", new Object[]{tableid, column.getProperty("columnid")});
                    try {
                        while (dbu.getNext("syscolumnproperty")) {
                            String propertyid = dbu.getString("syscolumnproperty", "propertyid");
                            String value = dbu.getValue("syscolumnproperty", "propertyvalue");
                            column.setProperty(propertyid, value);
                            if (!propertyid.equals("editorstyleid") || value.length() <= 0) continue;
                            editorstyleprops = EditorStyleField.getEditorStyleProperties(value, columnsdcid, columnreftypeid, this.sapphireConnection);
                        }
                    }
                    finally {
                        dbu.closeResultSet("syscolumnproperty");
                    }
                    column.setProperty("editorstyleprops", editorstyleprops.toXMLString());
                    columnData.add(column);
                }
                sdc.setProperty("columns", columnData);
            }
            finally {
                dbu.closeResultSet();
            }
            PropertyListCollection detaillinkData = new PropertyListCollection();
            dbu.createPreparedResultSet("SELECT sdc.tableid, sdcdetaillink.sdcid, sdcdetaillink.linksdcid, sdcdetaillink.linkid, sdcdetaillink.detaillinkid, sdcdetaillink.linktype, sdcdetaillink.linktableid, sdcdetaillink.linksequence, sdcdetaillink.reftypeid, \tsdcdetaillink.sdccolumnid, sdcdetaillink.sdccolumnid2, sdcdetaillink.sdccolumnid3, sdcdetaillink.userflag , sdcdetaillink.deleteflag FROM\tsdcdetaillink LEFT OUTER JOIN sdc ON sdcdetaillink.linksdcid = sdc.sdcid WHERE sdcdetaillink.sdcid = ? ORDER BY linksequence ASC", sdcid);
            try {
                while (dbu.getNext()) {
                    PropertyList detaillink = new PropertyList(dbu.getString("linkid") + ";" + dbu.getString("detaillinkid"));
                    linktype = dbu.getString("linktype");
                    detaillink.setProperty("sdcid", dbu.getString("sdcid"));
                    detaillink.setProperty("linkid", dbu.getString("linkid"));
                    detaillink.setProperty("detaillinkid", dbu.getString("detaillinkid"));
                    detaillink.setProperty("linksdcid", dbu.getString("linksdcid"));
                    detaillink.setProperty("linktype", dbu.getString("linktype"));
                    detaillink.setProperty("linktableid", dbu.getString("linktableid"));
                    detaillink.setProperty("linksequence", dbu.getString("linksequence"));
                    detaillink.setProperty("reftypeid", dbu.getString("reftypeid"));
                    detaillink.setProperty("sdccolumnid", dbu.getString("sdccolumnid"));
                    detaillink.setProperty("sdccolumnid2", dbu.getString("sdccolumnid2"));
                    detaillink.setProperty("sdccolumnid3", dbu.getString("sdccolumnid3"));
                    detaillink.setProperty("deleteflag", dbu.getString("deleteflag"));
                    detaillink.setProperty("tableid", dbu.getString("tableid"));
                    String detailtableid = dbu.getString("tableid");
                    String detaillinktableid = dbu.getString("linktableid");
                    if (detailtableid != null && detailtableid.length() > 0 && (tableLabelInfo = this.getSysTableProperties(dbu, detailtableid)) != null) {
                        detaillink.setProperty("tablelabel", tableLabelInfo.getValue(0, "tablelabel", ""));
                        detaillink.setProperty("itemdisplay", tableLabelInfo.getValue(0, "itemdisplay", ""));
                    }
                    PropertyListCollection detaillinkColumns = new PropertyListCollection();
                    int keycols = 0;
                    if (detaillinktableid != null && detaillinktableid.length() > 0 && linktype.equalsIgnoreCase("D")) {
                        keycols = this.getDetailTableKeyCols(dbu, detaillinktableid, detaillink, detaillinkColumns);
                        this.getDetailTableCols(dbu, detaillinktableid, detaillink, detaillinkColumns, detaillinkData);
                    }
                    detaillink.setProperty("keycolcount", String.valueOf(keycols));
                    detaillink.setProperty("linkcolumns", detaillinkColumns);
                    detaillinkData.add(detaillink);
                }
                sdc.setProperty("detaillinks", detaillinkData);
            }
            finally {
                dbu.closeResultSet();
            }
            PropertyListCollection linkData = new PropertyListCollection();
            dbu.createPreparedResultSet("SELECT sdc.tableid, sdclink.sdcid, sdclink.linksdcid, sdclink.linksdccolumnid, sdclink.linkid, sdclink.linktype, sdclink.linktableid, sdclink.linksequence, sdclink.reftypeid, \tsdclink.sdccolumnid, sdclink.sdccolumnid2, sdclink.sdccolumnid3, sdclink.usersequence, sdclink.auditsequence, sdclink.lookuppageid, sdclink.userflag, sdclink.deleteflag, sdclink.loadflag FROM sdclink LEFT OUTER JOIN sdc ON sdclink.linksdcid = sdc.sdcid WHERE sdclink.sdcid = ? ORDER BY linksequence ASC", sdcid);
            try {
                while (dbu.getNext()) {
                    linktype = dbu.getString("linktype");
                    PropertyList link = new PropertyList(dbu.getString("linkid"));
                    link.setProperty("sdcid", dbu.getString("sdcid"));
                    link.setProperty("linkid", dbu.getString("linkid"));
                    link.setProperty("linksdcid", dbu.getString("linksdcid"));
                    link.setProperty("linktype", linktype);
                    link.setProperty("linktableid", dbu.getString("linktableid"));
                    link.setProperty("linksequence", dbu.getString("linksequence"));
                    link.setProperty("reftypeid", dbu.getString("reftypeid"));
                    link.setProperty("sdccolumnid", dbu.getString("sdccolumnid"));
                    link.setProperty("sdccolumnid2", dbu.getString("sdccolumnid2"));
                    link.setProperty("sdccolumnid3", dbu.getString("sdccolumnid3"));
                    link.setProperty("loadflag", dbu.getString("loadflag"));
                    link.setProperty("deleteflag", dbu.getString("deleteflag"));
                    link.setProperty("lookuppageid", dbu.getString("lookuppageid"));
                    link.setProperty("tableid", dbu.getString("tableid"));
                    link.setProperty("linksdccolumnid", dbu.getString("linksdccolumnid"));
                    link.setProperty("hasusersequence", "N");
                    String linktableid = dbu.getString("linktableid");
                    if (linktableid != null && linktableid.length() > 0 && (tableLabelInfo = this.getSysTableProperties(dbu, linktableid)) != null) {
                        link.setProperty("linktablelabel", tableLabelInfo.getValue(0, "tablelabel", ""));
                        link.setProperty("linkitemdisplay", tableLabelInfo.getValue(0, "itemdisplay", ""));
                    }
                    PropertyListCollection linkColumns = new PropertyListCollection();
                    int keycols = 0;
                    if (linktableid != null && linktableid.length() > 0 && (linktype.equalsIgnoreCase("D") || linktype.equalsIgnoreCase("M"))) {
                        keycols = this.getDetailTableKeyCols(dbu, linktableid, link, linkColumns);
                        this.getDetailTableCols(dbu, linktableid, link, linkColumns, detaillinkData);
                    }
                    link.setProperty("keycolcount", String.valueOf(keycols));
                    link.setProperty("linkcolumns", linkColumns);
                    linkData.add(link);
                }
                sdc.setProperty("links", linkData);
            }
            finally {
                dbu.closeResultSet();
            }
            PropertyListCollection tableData = new PropertyListCollection();
            PropertyList primary = new PropertyList(tableid);
            primary.setProperty("tableid", tableid);
            primary.setProperty("parenttableid", "");
            primary.setProperty("keycolumns", sdc.getProperty("keycolumns"));
            primary.setProperty("keycolid1", sdc.getProperty("keycolid1"));
            primary.setProperty("keycolid2", sdc.getProperty("keycolid2"));
            primary.setProperty("keycolid3", sdc.getProperty("keycolid3"));
            primary.setProperty("columns", columnData);
            tableData.add(primary);
            for (int i = 0; i < linkData.size(); ++i) {
                PropertyList link = linkData.getPropertyList(i);
                if (!link.getProperty("linktype").equals("D") && !link.getProperty("linktype").equals("M")) continue;
                PropertyList detailtable = new PropertyList(link.getProperty("linktableid"));
                detailtable.setProperty("tableid", link.getProperty("linktableid"));
                detailtable.setProperty("parenttableid", tableid);
                int keycols = Integer.parseInt(link.getProperty("keycolcount"));
                detailtable.setProperty("keycolumns", String.valueOf(keycols));
                for (int j = 1; j <= keycols; ++j) {
                    detailtable.setProperty("keycolid" + j, link.getProperty("keycolid" + j));
                }
                detailtable.setProperty("columns", link.getCollection("linkcolumns"));
                tableData.add(detailtable);
            }
            dbu.createPreparedResultSet("SELECT sdcid, linksdcid, linkid, linktableid, linksequence, parenttableid FROM\tsdcdetaillink WHERE sdcid = ? AND linktype = 'D' ORDER BY linksequence ASC", sdcid);
            try {
                while (dbu.getNext()) {
                    PropertyList detailtable = new PropertyList(dbu.getString("linktableid"));
                    detailtable.setProperty("tableid", dbu.getString("linktableid"));
                    detailtable.setProperty("parenttableid", dbu.getString("parenttableid"));
                    PropertyListCollection detailtablecolumns = new PropertyListCollection();
                    String linktableid = dbu.getString("linktableid");
                    int keycols = 0;
                    if (linktableid != null && linktableid.length() > 0) {
                        keycols = this.getDetailTableKeyCols(dbu, linktableid, detailtable, detailtablecolumns);
                        this.getDetailTableCols(dbu, linktableid, detailtable, detailtablecolumns, detaillinkData);
                    }
                    detailtable.setProperty("keycolumns", String.valueOf(keycols));
                    detailtable.setProperty("columns", detailtablecolumns);
                    tableData.add(detailtable);
                }
                sdc.setProperty("tables", tableData);
            }
            finally {
                dbu.closeResultSet();
            }
            dbu.createPreparedResultSet("SELECT sdc.tableid, sdclink.sdcid, sdclink.linksdcid, sdclink.linksdccolumnid, sdclink.linkid, sdclink.linktype, sdclink.linktableid, sdclink.linksequence, sdclink.reftypeid, \t\tsdclink.sdccolumnid, sdclink.sdccolumnid2, sdclink.sdccolumnid3, sdclink.usersequence, sdclink.auditsequence, sdclink.lookuppageid, sdclink.userflag, sdclink.deleteflag, sdclink.loadflag FROM\tsdclink LEFT OUTER JOIN sdc ON sdclink.linksdcid = sdc.sdcid WHERE  sdclink.linksdcid = ? ORDER BY linksequence ASC", sdcid);
            try {
                PropertyListCollection reverseLinks = new PropertyListCollection();
                while (dbu.getNext()) {
                    String revlinkid = dbu.getString("linkid");
                    String linktableid = dbu.getString("linktableid");
                    String linktype3 = dbu.getString("linktype");
                    String revSDCId = dbu.getString("sdcid");
                    String sdccol = dbu.getString("sdccolumnid");
                    PropertyList reverseLink = new PropertyList(revlinkid + ";" + revSDCId);
                    reverseLink.setProperty("tableid", dbu.getString("tableid"));
                    reverseLink.setProperty("sdcid", revSDCId);
                    reverseLink.setProperty("linksdcid", dbu.getString("linksdcid"));
                    reverseLink.setProperty("linksdccolumnid", dbu.getString("linksdccolumnid"));
                    reverseLink.setProperty("linkid", revlinkid);
                    reverseLink.setProperty("linktype", linktype3);
                    reverseLink.setProperty("linktableid", linktableid);
                    reverseLink.setProperty("linksequence", dbu.getString("linksequence"));
                    reverseLink.setProperty("reftypeid", dbu.getString("reftypeid"));
                    reverseLink.setProperty("sdccolumnid", sdccol);
                    reverseLink.setProperty("sdccolumnid2", dbu.getString("sdccolumnid2"));
                    reverseLink.setProperty("sdccolumnid3", dbu.getString("sdccolumnid3"));
                    reverseLink.setProperty("usersequence", dbu.getString("usersequence"));
                    reverseLink.setProperty("auditsequence", dbu.getString("auditsequence"));
                    reverseLink.setProperty("lookuppageid", dbu.getString("lookuppageid"));
                    reverseLink.setProperty("userflag", dbu.getString("userflag"));
                    reverseLink.setProperty("deleteflag", dbu.getString("deleteflag"));
                    reverseLink.setProperty("loadflag", dbu.getString("loadflag"));
                    reverseLinks.add(reverseLink);
                }
                sdc.setProperty("reverselinks", reverseLinks);
            }
            finally {
                dbu.closeResultSet();
            }
            dbu.createPreparedResultSet("SELECT sdc.tableid, sdcdetaillink.sdcid, sdcdetaillink.linksdcid, sdcdetaillink.linkid, sdcdetaillink.linktype, sdcdetaillink.linktableid, sdcdetaillink.linksequence, sdcdetaillink.reftypeid, \t\tsdcdetaillink.sdccolumnid, sdcdetaillink.sdccolumnid2, sdcdetaillink.sdccolumnid3, sdcdetaillink.userflag, sdcdetaillink.deleteflag FROM\tsdcdetaillink LEFT OUTER JOIN sdc ON sdcdetaillink.linksdcid = sdc.sdcid WHERE  sdcdetaillink.linksdcid = ? ORDER BY linksequence ASC", sdcid);
            try {
                PropertyListCollection reverseDetailLinks = new PropertyListCollection();
                while (dbu.getNext()) {
                    String revdetlink = dbu.getString("linkid");
                    String revSDCId = dbu.getString("sdcid");
                    PropertyList reverseLink = new PropertyList(revdetlink + ";" + revSDCId);
                    reverseLink.setProperty("tableid", dbu.getString("tableid"));
                    reverseLink.setProperty("sdcid", dbu.getString("sdcid"));
                    reverseLink.setProperty("linksdcid", dbu.getString("linksdcid"));
                    reverseLink.setProperty("linkid", revdetlink);
                    reverseLink.setProperty("linktype", dbu.getString("linktype"));
                    reverseLink.setProperty("linktableid", dbu.getString("linktableid"));
                    reverseLink.setProperty("linksequence", dbu.getString("linksequence"));
                    reverseLink.setProperty("reftypeid", dbu.getString("reftypeid"));
                    reverseLink.setProperty("sdccolumnid", dbu.getString("sdccolumnid"));
                    reverseLink.setProperty("sdccolumnid2", dbu.getString("sdccolumnid2"));
                    reverseLink.setProperty("sdccolumnid3", dbu.getString("sdccolumnid3"));
                    reverseLink.setProperty("userflag", dbu.getString("userflag"));
                    reverseLink.setProperty("deleteflag", dbu.getString("deleteflag"));
                    reverseDetailLinks.add(reverseLink);
                }
                sdc.setProperty("reversedetaillinks", reverseDetailLinks);
            }
            finally {
                dbu.closeResultSet();
            }
            dbu.createPreparedResultSet("SELECT attributedef.attributedefid, attributedef.attributetitle, attributedef.helptext, attributedef.editorstyleid, attributedef.editsdcid, attributedef.editreftypeid, attributedef.allowduplicatesflag, attributedef.alwaysaddflag, attributedef.alwaysaddcount, attributedef.datatype, attributedef.defaulttextvalue, attributedef.defaultnumericvalue, attributedef.defaultdatevalue, attributedef.defaultclobvalue, attributedef.attributegroup, attributedef.instructiontext, attributedef.instructionflag FROM\tattributedef  WHERE  attributedef.basedonid = ? ORDER BY attributedef.usersequence ASC", sdcid);
            try {
                PropertyListCollection attributes = new PropertyListCollection();
                while (dbu.getNext()) {
                    String attributeid = dbu.getString("attributedefid");
                    PropertyList attribute = new PropertyList(attributeid);
                    attribute.setProperty("attributeid", attributeid);
                    attribute.setProperty("attributetitle", dbu.getString("attributetitle"));
                    attribute.setProperty("helptext", dbu.getString("helptext"));
                    attribute.setProperty("editorstyleid", dbu.getString("editorstyleid"));
                    attribute.setProperty("editsdcid", dbu.getString("editsdcid"));
                    attribute.setProperty("editreftypeid", dbu.getString("editreftypeid"));
                    attribute.setProperty("allowduplicatesflag", dbu.getString("allowduplicatesflag"));
                    attribute.setProperty("alwaysaddflag", dbu.getString("alwaysaddflag"));
                    attribute.setProperty("alwaysaddcount", dbu.getString("alwaysaddcount"));
                    attribute.setProperty("datatype", dbu.getString("datatype"));
                    attribute.setProperty("defaulttextvalue", dbu.getString("defaulttextvalue"));
                    attribute.setProperty("defaultnumericvalue", dbu.getString("defaultnumericvalue"));
                    attribute.setProperty("defaultdatevalue", dbu.getString("defaultdatevalue"));
                    attribute.setProperty("defaultclobvalue", dbu.getString("defaultclobvalue"));
                    attribute.setProperty("attributegroup", dbu.getString("attributegroup"));
                    attribute.setProperty("instructiontext", dbu.getString("instructiontext"));
                    attribute.setProperty("instructionflag", dbu.getString("instructionflag"));
                    attributes.add(attribute);
                }
                sdc.setProperty("attributes", attributes);
            }
            finally {
                dbu.closeResultSet();
            }
            License license = Configuration.getInstance().getLicense(this.sapphireConnection.getDatabaseId());
            if (license != null) {
                sdc.setProperty("licencemax", license.getProperty("sdc_" + sdcid));
            }
            CacheUtil.put(this.connectionInfo.getDatabaseId(), "SDC", sdcid.toLowerCase(), sdc);
        }
        catch (SapphireException se) {
            throw new ServiceException("DB_ACTION_FAILED", "Error getting sdc properties. Exception: " + se.getMessage(), se);
        }
        catch (Exception e) {
            throw new ServiceException("DB_ACTION_FAILED", "Error getting sdc properties. Exception: " + e.getMessage(), e);
        }
        finally {
            dbu.reset();
        }
    }

    private void addDetailTableCols(DBUtil dbu, PropertyList link, PropertyListCollection linkColumns, PropertyListCollection detaillinkData) throws SapphireException {
        while (dbu.getNext("LinkTableColumns")) {
            PropertyList linkCol = new PropertyList(dbu.getString("LinkTableColumns", "columnid"));
            linkCol.setProperty("linktableid", dbu.getString("LinkTableColumns", "tableid"));
            String currentcol = dbu.getString("LinkTableColumns", "columnid");
            linkCol.setProperty("linkcolumnid", currentcol);
            linkCol.setProperty("linkdatatype", dbu.getString("LinkTableColumns", "datatype"));
            linkCol.setProperty("linkcolumnlength", "" + dbu.getInt("LinkTableColumns", "columnlength"));
            linkCol.setProperty("linkcolumnlabel", "" + dbu.getString("LinkTableColumns", "columnlabel"));
            linkCol.setProperty("linkpkflag", "N");
            if (detaillinkData != null) {
                for (int i = 0; i < detaillinkData.size(); ++i) {
                    String sdccolumnid;
                    PropertyList linklist = detaillinkData.getPropertyList(i);
                    String linkid = linklist.getProperty("linkid", "");
                    String sdcid = linklist.getProperty("sdcid", "");
                    if (linkid.length() <= 0 || !linkid.equals(link.getProperty("linkid", "")) || sdcid.length() <= 0 || !sdcid.equals(link.getProperty("sdcid")) || !(sdccolumnid = linklist.getProperty("sdccolumnid")).equalsIgnoreCase(currentcol)) continue;
                    linkCol.setProperty("linklink", i + "");
                    break;
                }
            }
            linkColumns.add(linkCol);
            if (!dbu.getString("LinkTableColumns", "columnid").equalsIgnoreCase("usersequence")) continue;
            link.setProperty("hasusersequence", "Y");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void getDetailTableCols(DBUtil dbu, String linktableid, PropertyList link, PropertyListCollection linkColumns, PropertyListCollection detaillinkData) throws SapphireException {
        if (linktableid != null && linktableid.length() > 0) {
            dbu.createPreparedResultSet("LinkTableColumns", "SELECT syscolumn.tableid, syscolumn.columnid, syscolumn.datatype, syscolumn.columnlabel, syscolumn.columnlength, syscolumn.columnsequence, \t\tsyscolumn.columntype, syscolumn.columndesc, syscolumn.pkflag, syscolumn.nnflag FROM\tsystable, syscolumn WHERE\tsystable.tableid = ? AND \t\tsystable.tableid = syscolumn.tableid AND        syscolumn.pkflag = 'N' ORDER BY syscolumn.columnsequence", new Object[]{linktableid});
            try {
                this.addDetailTableCols(dbu, link, linkColumns, detaillinkData);
            }
            finally {
                dbu.closeResultSet("LinkTableColumns");
            }
        }
    }

    private int addDetailTableKeyCols(DBUtil dbu, PropertyList link, PropertyListCollection linkColumns) throws SapphireException {
        int keycols = 0;
        while (dbu.getNext("LinkTableKeys")) {
            ++keycols;
            PropertyList linkCol = new PropertyList(dbu.getString("LinkTableKeys", "columnid"));
            linkCol.setProperty("linktableid", dbu.getString("LinkTableKeys", "tableid"));
            linkCol.setProperty("linkcolumnid", dbu.getString("LinkTableKeys", "columnid"));
            linkCol.setProperty("linkdatatype", dbu.getString("LinkTableKeys", "datatype"));
            linkCol.setProperty("linkcolumnlength", "" + dbu.getInt("LinkTableKeys", "columnlength"));
            linkCol.setProperty("linkcolumnlabel", dbu.getString("LinkTableKeys", "columnlabel"));
            linkCol.setProperty("linkpkflag", "Y");
            link.setProperty("keycolid" + String.valueOf(keycols), dbu.getString("LinkTableKeys", "columnid"));
            linkColumns.add(linkCol);
        }
        return keycols;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int getDetailTableKeyCols(DBUtil dbu, String linktableid, PropertyList link, PropertyListCollection linkColumns) throws SapphireException {
        int keycols = 0;
        if (linktableid != null && linktableid.length() > 0) {
            dbu.createPreparedResultSet("LinkTableKeys", "SELECT syscolumn.tableid, syscolumn.columnid, syscolumn.datatype, syscolumn.columnlength, syscolumn.columnlabel, sysrefcolumn.columnsequence,        syscolumn.columntype, syscolumn.columndesc, syscolumn.pkflag, syscolumn.nnflag FROM   syscolumn,  sysrefcolumn, sysref WHERE  sysrefcolumn.columnid = syscolumn.columnid AND        sysrefcolumn.refid = sysref.refid AND        syscolumn.tableid = sysref.tableid AND        lower( syscolumn.tableid ) = ? AND        sysref.reftypeflag = 'P' ORDER BY sysrefcolumn.columnsequence", new Object[]{linktableid.toLowerCase()});
            try {
                keycols = this.addDetailTableKeyCols(dbu, link, linkColumns);
            }
            finally {
                dbu.closeResultSet("LinkTableKeys");
            }
        }
        return keycols;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DataSet getSysTableProperties(DBUtil dbu, String linktableid) throws SapphireException {
        DataSet ret = null;
        if (linktableid != null && linktableid.length() > 0) {
            dbu.createPreparedResultSet("LinkTableDisplay", "SELECT tablelabel, tableid, itemdisplay FROM systable WHERE lower( systable.tableid ) = ? ", new Object[]{linktableid.toLowerCase()});
            try {
                ret = new DataSet();
                while (dbu.getNext("LinkTableDisplay")) {
                    int row = ret.addRow();
                    ret.setString(row, "tableid", dbu.getString("LinkTableDisplay", "tableid"));
                    ret.setString(row, "tablelabel", dbu.getString("LinkTableDisplay", "tablelabel"));
                    ret.setString(row, "itemdisplay", dbu.getString("LinkTableDisplay", "itemdisplay"));
                }
            }
            finally {
                dbu.closeResultSet("LinkTableDisplay");
            }
        }
        return ret;
    }

    public static String[] getKeyColumns(DBAccess database, String tableid) throws SapphireException {
        database.createPreparedResultSet("keys", "SELECT sysrefcolumn.columnid, sysrefcolumn.columnsequence FROM\tsysref, sysrefcolumn WHERE\tsysref.tableid = ? AND \t\tsysrefcolumn.refid = sysref.refid AND \t\tsysref.reftypeflag = 'P' ORDER BY sysrefcolumn.columnsequence ASC ", new Object[]{tableid});
        ArrayList<String> keylist = new ArrayList<String>();
        while (database.getNext("keys")) {
            keylist.add(database.getString("keys", "columnid"));
        }
        return keylist.toArray(new String[keylist.size()]);
    }

    public static String getDescCol(String sdcid, String sdctype, String compcode, String tableid, String keycolid1) {
        if ("QCMethodSampleType".equalsIgnoreCase(sdcid)) {
            return "qcmethoditemsampledesc";
        }
        if (sdctype.equals("U") || tableid.substring(1, 2).equals("_")) {
            return tableid.substring(2) + "desc";
        }
        if (compcode.length() > 0 && tableid.toLowerCase().startsWith(compcode.toLowerCase() + "_")) {
            return tableid.substring(compcode.length() + 1) + "desc";
        }
        if (DDTService.isPseudocolumnSDC(sdcid)) {
            return keycolid1;
        }
        return tableid + "desc";
    }

    public static boolean isPseudocolumnSDC(String sdcid) {
        return sdcid.equalsIgnoreCase("dataset") || sdcid.equalsIgnoreCase("dataitem") || sdcid.equalsIgnoreCase("sdiworkitem") || sdcid.equalsIgnoreCase("sdinote") || sdcid.equalsIgnoreCase("taskqueueitem") || sdcid.equalsIgnoreCase("lv_arrayitem") || sdcid.equalsIgnoreCase("sdiattachment");
    }

    public PropertyListCollection getTableColumns(String tableid) throws ServiceException {
        PropertyListCollection columns = (PropertyListCollection)CacheUtil.get(this.sapphireConnection.getDatabaseId(), "TableColumns", tableid.toLowerCase());
        if (columns == null) {
            columns = new PropertyListCollection();
            DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
            try {
                dbu.setConnection(this.sapphireConnection);
                dbu.createPreparedResultSet("SELECT * FROM syscolumn WHERE tableid = ? ORDER BY columnsequence", new Object[]{tableid});
                while (dbu.getNext()) {
                    PropertyList column = new PropertyList(dbu.getString("columnid"));
                    String columnLength = dbu.getString("columnlength");
                    if (columnLength == null || columnLength.equals("") || columnLength.equals("null")) {
                        columnLength = "0";
                    }
                    column.setProperty("columnid", dbu.getString("columnid"));
                    column.setProperty("columndesc", dbu.getString("columndesc"));
                    column.setProperty("columnlabel", dbu.getString("columnlabel"));
                    column.setProperty("columndoc", dbu.getString("columndoc"));
                    column.setProperty("datatype", dbu.getString("datatype"));
                    column.setProperty("pkflag", dbu.getString("pkflag"));
                    column.setProperty("nnflag", dbu.getString("nnflag"));
                    column.setProperty("columnlength", columnLength);
                    column.setProperty("columntype", dbu.getString("columntype"));
                    columns.add(column);
                }
                CacheUtil.put(this.sapphireConnection.getDatabaseId(), "TableColumns", tableid.toLowerCase(), columns);
            }
            catch (Exception e) {
                throw new ServiceException("DB_ACTION_FAILED", "Error getting table columns. Exception: " + e.getMessage(), e);
            }
            finally {
                dbu.reset();
            }
        }
        return columns;
    }

    public String getColumnProperty(String tableid, String columnid, String propertyid) throws ServiceException {
        PropertyListCollection columns = this.getTableColumns(tableid);
        PropertyList column = columns != null ? columns.getPropertyList(columnid) : null;
        return column != null ? column.getProperty("columnlength") : "";
    }
}


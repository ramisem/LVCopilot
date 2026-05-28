/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDCRO
extends BaseSDCRO {
    private HashMap currentSDCProperties = null;
    private DataSet currentLinksData;
    public DataSet currentCols;
    public DataSet currentDetailsCols;
    public DataSet currentIndexInfo;
    public DataSet currentAttributes;
    public DataSet currentOperations;
    public DataSet currentTableDoc;
    public static final String SEPARATOR = "|!|";

    public void initialize(SapphireConnection connection) throws SapphireException {
        super.initialize("SDC", connection);
    }

    @Override
    public void setCurrentSDIData(SDIData sdiData) throws SapphireException {
        super.setCurrentSDIData(sdiData);
        this.currentSDCProperties = this.getSDCProcessor().getSDCProperties(this.currentSDI.getKeyid1());
        this.currentLinksData = this.getSDCProcessor().getLinksData(this.currentSDI.getKeyid1());
        this.currentCols = this.getColumnData(this.currentSDI.getKeyid1());
        this.currentDetailsCols = this.getDetailsColumnData(this.currentSDI.getKeyid1());
        this.currentIndexInfo = this.getIndexInfoFromDB();
        this.currentAttributes = this.getSDCAttributes(this.currentSDI.getKeyid1());
        this.currentTableDoc = this.getTableDoc();
        this.currentOperations = sdiData.getDataset("sdcoperations");
    }

    @Override
    public void startSection() {
        if (!this.dataSource.equals("XMLREPORT")) {
            this.currentSDCProperties = this.getSDCProcessor().getSDCProperties(this.currentSDI.getKeyid1());
            this.currentLinksData = this.getSDCProcessor().getLinksData(this.currentSDI.getKeyid1());
            this.currentCols = this.getColumnData(this.currentSDI.getKeyid1());
            this.currentDetailsCols = this.getDetailsColumnData(this.currentSDI.getKeyid1());
            this.currentIndexInfo = this.getIndexInfoFromDB();
            this.currentAttributes = this.getSDCAttributes(this.currentSDI.getKeyid1());
            this.currentOperations = this.getSDCOperations(this.currentSDI.getKeyid1());
            this.currentTableDoc = this.getTableDoc();
        }
    }

    public String getSDCPropertiesXML() {
        PropertyList pl = new PropertyList();
        Object[] keyes = this.currentSDCProperties.keySet().toArray();
        for (int i = 0; i < keyes.length; ++i) {
            String currPropVal = (String)this.currentSDCProperties.get(keyes[i].toString());
            pl.setProperty(keyes[i].toString(), currPropVal);
        }
        return pl.toXMLString();
    }

    @Override
    public int gotoSection(SDI sdi) {
        for (int i = 0; i < this.sdiList.size(); ++i) {
            SDI curr = (SDI)this.sdiList.get(i);
            if (!curr.getKeyid1().equalsIgnoreCase(sdi.getKeyid1())) continue;
            this.currentSDI = sdi;
            if (this.dataSource.equals("XMLREPORT")) {
                this.currentSDIData = this.getSDIDataFromXMLReport(this.refReportFolder);
                this.currentSDCProperties = this.getSDCPropertiesFromXMLReport(this.refReportFolder);
                this.currentLinksData = this.getLinksDataFromXMLReport(this.refReportFolder);
                this.currentCols = this.getColsFromXMLReport(this.refReportFolder);
                this.currentDetailsCols = this.getDetailsColsFromXMLReport(this.refReportFolder);
                this.currentIndexInfo = this.getIndexInfoFromXMLReport(this.refReportFolder);
                this.currentAttributes = this.getSDCAttributesFromXMLReport(this.refReportFolder);
                this.currentTableDoc = this.getTableDocFromXMLReport(this.refReportFolder);
                this.currentOperations = this.currentSDIData.getDataset("sdcoperation");
                return i;
            }
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("SDC");
            sdiRequest.setQueryFrom((String)this.getSDCProperties().get("tableid"));
            String whereClause = this.getKeyColId1() + "='" + this.currentSDI.getKeyid1() + "'";
            if (this.getKeyColCount() > 1 && !this.currentSDI.getKeyid2().equals("*")) {
                whereClause = whereClause + " AND " + this.getKeyColId2() + "='" + this.currentSDI.getKeyid2() + "'";
            }
            if (this.getKeyColCount() == 3 && !this.currentSDI.getKeyid3().equals("*")) {
                whereClause = whereClause + " AND " + this.getKeyColId2() + "='" + this.currentSDI.getKeyid2() + "'";
            }
            sdiRequest.setQueryWhere(whereClause);
            sdiRequest.setRequestItem("all");
            sdiRequest.setExtendedDataTypes(true);
            sdiRequest.setOverrideLoadFlag(true);
            sdiRequest.setQueryOrderBy("usersequence");
            this.currentSDIData = this.getSDIProcessor().getSDIData(sdiRequest);
            this.currentSDCProperties = this.getSDCProcessor().getSDCProperties(this.currentSDI.getKeyid1());
            this.currentLinksData = this.getSDCProcessor().getLinksData(this.currentSDI.getKeyid1());
            this.currentCols = this.getColumnData(this.currentSDI.getKeyid1());
            this.currentDetailsCols = this.getDetailsColumnData(this.currentSDI.getKeyid1());
            this.currentIndexInfo = this.getIndexInfoFromDB();
            this.currentAttributes = this.getSDCAttributes(this.currentSDI.getKeyid1());
            this.currentTableDoc = this.getTableDoc();
            this.currentOperations = this.getSDCOperations(this.currentSDI.getKeyid1());
            return i;
        }
        this.currentSDI = null;
        this.currentSDIData = null;
        this.currentSDCProperties = null;
        return -1;
    }

    public String getCurrentSDCDescription() {
        return (String)this.currentSDCProperties.get("description");
    }

    public String getCurrentSDCName() {
        return (String)this.currentSDCProperties.get("sdcid");
    }

    public String getCurrentKeyColId1() {
        return (String)this.currentSDCProperties.get("keycolid1");
    }

    public String getCurrentKeyColId2() {
        return (String)this.currentSDCProperties.get("keycolid2");
    }

    public String getCurrentKeyColId3() {
        return (String)this.currentSDCProperties.get("keycolid3");
    }

    public String getCurrentSDCSingular() {
        return (String)this.currentSDCProperties.get("singular");
    }

    public String getCurrentSDCPlural() {
        return (String)this.currentSDCProperties.get("plural");
    }

    public String getCurrentSDCType() {
        String colValue = (String)this.currentSDCProperties.get("sdctype");
        if ("C".equals(colValue)) {
            return "Core";
        }
        if ("S".equals(colValue)) {
            return "System";
        }
        if ("D".equals(colValue)) {
            return "Data";
        }
        return colValue;
    }

    public String getCurrentTableName() {
        if (this.currentSDCProperties.get("tableid") != null) {
            return this.currentSDCProperties.get("tableid").toString();
        }
        return "";
    }

    public String getCurrentVersioned() {
        Object flag = this.currentSDCProperties.get("versionedflag");
        if (flag == null) {
            return "Yes";
        }
        if ("N".equals(flag.toString())) {
            return "No";
        }
        return "Yes";
    }

    public String getCurrentAccessControl() {
        Object flag = this.currentSDCProperties.get("accesscontrolledflag");
        if (flag == null) {
            return "Not Implemented";
        }
        if ("D".equals(flag.toString())) {
            return "Departmental";
        }
        if ("Y".equals(flag.toString())) {
            return "Role Level";
        }
        return "Not Implemented";
    }

    public String getCurrentCOC() {
        Object flag = this.currentSDCProperties.get("cocableflag");
        if (flag != null && "N".equals(flag.toString())) {
            return "No";
        }
        return "Yes";
    }

    public String getCurrentAuditMethod() {
        Object flag = this.currentSDCProperties.get("auditedflag");
        if (flag == null) {
            return "";
        }
        if ("N".equals(flag.toString())) {
            return "None";
        }
        if ("B".equals(flag.toString())) {
            return "Background";
        }
        if ("P".equals(flag.toString())) {
            return "Prompt";
        }
        if ("W".equals(flag.toString())) {
            return "Prompt with Password";
        }
        return flag.toString();
    }

    public String getCurrentAuditPromptOptions() {
        Object flag = this.currentSDCProperties.get("auditpromptflag");
        if (flag == null) {
            return "";
        }
        if ("R".equals(flag.toString())) {
            return "Standard reason required";
        }
        if ("F".equals(flag.toString())) {
            return "Free text reason required";
        }
        if ("S".equals(flag.toString())) {
            return "Standard reason optional";
        }
        if ("O".equals(flag.toString())) {
            return "Optional reason";
        }
        if ("B".equals(flag.toString())) {
            return "Free text or Optional reason";
        }
        if ("T".equals(flag.toString())) {
            return "Text or reason";
        }
        return flag.toString();
    }

    public String getCurrentReasonReferenceType() {
        Object flag = this.currentSDCProperties.get("reftypeid");
        if (flag == null) {
            flag = "";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowCategories() {
        Object flag = this.currentSDCProperties.get("categoriesflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowDataEntry() {
        Object flag = this.currentSDCProperties.get("dataentryflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowSpecifications() {
        Object flag = this.currentSDCProperties.get("specflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowWorkflows() {
        Object flag = this.currentSDCProperties.get("workflowflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowWorkitems() {
        Object flag = this.currentSDCProperties.get("workitemflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowContacts() {
        Object flag = this.currentSDCProperties.get("addressesflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowAttachments() {
        Object flag = this.currentSDCProperties.get("attachmentsflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowNotes() {
        Object flag = this.currentSDCProperties.get("notesflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowTemplates() {
        Object flag = this.currentSDCProperties.get("templatableflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowAliases() {
        Object flag = this.currentSDCProperties.get("aliasableflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowAttributes() {
        Object flag = this.currentSDCProperties.get("allowattributesflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowActivateDeavtivate() {
        Object flag = this.currentSDCProperties.get("activeableflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentMayBeScheduled() {
        Object flag = this.currentSDCProperties.get("scheduleableflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public String getCurrentAllowAdhocSearching() {
        Object flag = this.currentSDCProperties.get("searchableflag");
        if (flag == null) {
            flag = "N";
        }
        return SDCRO.yesorno(flag.toString());
    }

    public static String yesorno(String flag) {
        if ("Y".equals(flag)) {
            return "Yes";
        }
        return "No";
    }

    public DataSet getDTypeTables() {
        DataSet sdclinks = this.currentLinksData;
        if (sdclinks == null) {
            return new DataSet();
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("linktype", "D");
        return sdclinks.getFilteredDataSet(filter);
    }

    private DataSet getTableDoc() {
        DataSet dtypeLinks = this.getDTypeTables();
        String dTypeTables = dtypeLinks.getColumnValues("linktableid", ";");
        if (this.currentSDCProperties != null && this.currentSDCProperties.get("tableid") != null) {
            String cuurentTablename = this.currentSDCProperties.get("tableid").toString();
            String tableids = dTypeTables + ";" + cuurentTablename;
            String sql = "SELECT * FROM systable WHERE tableid in('" + tableids.replaceAll(";", "','") + "')";
            DataSet tabledocDS = this.getQueryProcessor().getSqlDataSet(sql);
            return tabledocDS;
        }
        return new DataSet();
    }

    public HashMap getTableDocHM() {
        HashMap<String, String> tableDocHM = new HashMap<String, String>();
        DataSet tableDocDS = this.currentTableDoc;
        if (tableDocDS != null) {
            for (int i = 0; i < tableDocDS.size(); ++i) {
                tableDocHM.put(tableDocDS.getString(i, "tableid"), tableDocDS.getString(i, "tabledoc"));
            }
        }
        return tableDocHM;
    }

    public DataSet getFTypeTables() {
        DataSet sdclinks = this.getSDCProcessor().getLinksData(this.currentSDI.getKeyid1());
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("linktype", "F");
        return sdclinks.getFilteredDataSet(filter);
    }

    public DataSet getLinksInfo() {
        return this.currentLinksData;
    }

    public DataSet getAttributesInfo() {
        return this.currentAttributes;
    }

    public DataSet getTableDocInfo() {
        return this.currentTableDoc;
    }

    public DataSet getOperationsInfo() {
        return this.currentOperations;
    }

    public DataSet getIndexInfoFromDB() {
        String sdcid = this.currentSDI.getKeyid1();
        DataSet indexes = this.getQueryProcessor().getPreparedSqlDataSet("SELECT tableid, reftypeflag, sysref.refid, columnid, columnsequence, refindexid FROM   sysrefcolumn,sysref WHERE  sysrefcolumn.refid = sysref.refid AND sysref.reftypeflag <> 'F' AND sysref.tableid IN (       SELECT tableid FROM sdc WHERE sdcid = ?        UNION        SELECT linktableid FROM sdc, sdclink, systable WHERE sdc.sdcid = sdclink.sdcid AND sdclink.linktableid = systable.tableid AND sdc.sdcid = ? AND linktype IN ( 'D', 'M' )        UNION        SELECT linktableid FROM sdcdetaillink, systable WHERE sdcdetaillink.linktableid = systable.tableid AND sdcid = ? AND linktype IN ( 'D', 'M' ) ) ORDER BY tableid, sysref.refid DESC, columnsequence", new Object[]{sdcid, sdcid, sdcid});
        if (indexes == null) {
            return new DataSet();
        }
        return indexes;
    }

    public DataSet getColumnsInfo(String table, boolean reportSDCRelationshipModelOnly) {
        DataSet cols = this.currentCols;
        if (cols == null) {
            return new DataSet();
        }
        if (table != null && table.length() > 0) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("tableid", table);
            cols = cols.getFilteredDataSet(filter);
        }
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Column Name", 0);
        ret.addColumn("Column Label", 0);
        ret.addColumn("Data Type", 0);
        ret.addColumn("Length", 0);
        if (!reportSDCRelationshipModelOnly) {
            ret.addColumn("Time Zone Ind.", 0);
            ret.addColumn("Searchable", 0);
        }
        ret.addColumn("Documentation", 0);
        for (int i = 0; i < cols.size(); ++i) {
            String label = this.currentCols.getString(i, "columnlabel", "");
            ret.addRow();
            if ("Y".equals(cols.getString(i, "pkflag"))) {
                ret.setString(i, "Column Name", cols.getString(i, "columnid") + " (key)");
            } else {
                ret.setString(i, "Column Name", cols.getString(i, "columnid"));
            }
            ret.setString(i, "Column Label", label);
            String datatype = cols.getString(i, "datatype");
            String val = "Character";
            if ("C".equals(datatype)) {
                val = "Character";
            } else if ("D".equals(datatype)) {
                val = "Date";
            } else if ("N".equals(datatype)) {
                val = "Numeric";
            }
            ret.setString(i, "Data Type", val);
            ret.setString(i, "Length", cols.getValue(i, "columnlength"));
            if (!reportSDCRelationshipModelOnly) {
                String tz = this.currentCols.getString(i, "timezoneindependent", "");
                val = "Y".equals(tz) ? "Yes" : "No";
                ret.setString(i, "Time Zone Ind.", val);
                String searchable = this.currentCols.getString(i, "searchableflag", "");
                val = "Y".equals(searchable) ? "Yes" : "No";
                ret.setString(i, "Searchable", val);
            }
            ret.setString(i, "Documentation", this.currentCols.getString(i, "columndoc", ""));
        }
        return ret;
    }

    public DataSet getDetailsColumnsInfo(String table) {
        DataSet ret = new DataSet();
        if (this.currentDetailsCols != null) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("tableid", table);
            DataSet columnData = this.currentDetailsCols.getFilteredDataSet(filter);
            ret.setColidCaseSensitive(true);
            ret.addColumn("Column Name", 0);
            ret.addColumn("Column Label", 0);
            ret.addColumn("Data Type", 0);
            ret.addColumn("Length", 0);
            ret.addColumn("Time Zone Ind.", 0);
            ret.addColumn("Searchable", 0);
            ret.addColumn("Documentation", 0);
            for (int i = 0; i < columnData.size(); ++i) {
                String label = columnData.getString(i, "columnlabel", "");
                ret.addRow();
                if ("Y".equals(columnData.getString(i, "pkflag"))) {
                    ret.setString(i, "Column Name", columnData.getString(i, "columnid") + " (key)");
                } else {
                    ret.setString(i, "Column Name", columnData.getString(i, "columnid"));
                }
                ret.setString(i, "Column Label", label);
                String datatype = columnData.getString(i, "datatype");
                String val = "Character";
                if ("C".equals(datatype)) {
                    val = "Character";
                } else if ("D".equals(datatype)) {
                    val = "Date";
                } else if ("N".equals(datatype)) {
                    val = "Numeric";
                }
                ret.setString(i, "Data Type", val);
                ret.setString(i, "Length", columnData.getValue(i, "columnlength"));
                String tz = columnData.getString(i, "timezoneindependent", "");
                val = "Y".equals(tz) ? "Yes" : "No";
                ret.setString(i, "Time Zone Ind.", val);
                String searchable = columnData.getString(i, "searchableflag", "");
                val = "Y".equals(searchable) ? "Yes" : "No";
                ret.setString(i, "Searchable", val);
                ret.setString(i, "Documentation", columnData.getString(i, "columndoc", ""));
            }
        }
        return ret;
    }

    public String[] getDetailNames() {
        String sql = "SELECT linktableid from sdclink where sdcid='SDC' and linktype='D' ";
        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        if (ds != null && ds.getRowCount() > 0) {
            String vals = ds.getColumnValues("linktableid", ";");
            return StringUtil.split(vals, ";");
        }
        return null;
    }

    public String getKeyGeneration() {
        Object keygen = this.currentSDCProperties.get("keygenerationrule");
        if (keygen == null) {
            return "";
        }
        return keygen.toString();
    }

    public DataSet getReferredBySDCs() {
        if (!this.dataSource.equals("XMLREPORT")) {
            String sql = "SELECT linksdcid, tableid, sdcdesc, linkid, linktype, linktableid, sdc.sdcid, sdccolumnid, sdccolumnid2 \nFROM sdclink, sdc WHERE sdc.sdcid = sdclink.sdcid and linksdcid='" + this.getCurrentSDCName() + "'";
            DataSet ret = this.getQueryProcessor().getSqlDataSet(sql);
            if (ret == null) {
                return new DataSet();
            }
            return ret;
        }
        return this.getReferredBySdcsFromXMLReport(this.refReportFolder);
    }

    public DataSet getReferredBySDCDetails() {
        String sdc = this.getCurrentSDCName();
        if (!this.dataSource.equals("XMLREPORT")) {
            String sql = "SELECT sdcdesc, tableid, detaillinkid, linktype, linktableid, sdc.sdcid, sdccolumnid, sdccolumnid2 FROM sdcdetaillink, sdc WHERE sdc.sdcid=sdcdetaillink.sdcid and  linksdcid='" + sdc + "'";
            DataSet ret = this.getQueryProcessor().getSqlDataSet(sql);
            if (ret == null) {
                ret = new DataSet();
            }
            return ret;
        }
        return this.getReferredBySdcDetailsFromXMLReport(this.refReportFolder);
    }

    public DataSet getRefersToSDCs() {
        String sdc = this.getCurrentSDCName();
        if (!this.dataSource.equals("XMLREPORT")) {
            String sql = "SELECT sdcdesc, linkid, linktype, tableid, linksdcid, linktableid, sdccolumnid, sdccolumnid2 FROM sdclink, sdc WHERE sdc.sdcid = sdclink.linksdcid and sdclink.sdcid='" + sdc + "' and linktype in ('F', 'M') ";
            DataSet ret = this.getQueryProcessor().getSqlDataSet(sql);
            if (ret == null) {
                ret = new DataSet();
            }
            return ret;
        }
        return this.getRefersToSdcsFromXMLReport(this.refReportFolder);
    }

    private PropertyList getSDCPropertiesFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI);
        refSDIFileName = refSDIFileName.replace(".xml", "_sdcprops.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList pl = new PropertyList();
                pl.setPropertyList(xml, false, false);
                return pl;
            }
            f = new File(refSDIFileName = refSDIFileName.replace(".xml", "_sdc_props.xml"));
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList pl = new PropertyList();
                pl.setPropertyList(xml, false, false);
                return pl;
            }
        }
        catch (SapphireException e) {
            Trace.log("Error reading SDC xml report file." + e.getMessage());
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getLinksDataFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_linksdata.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getColsFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_cols.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getDetailsColsFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_detailscols.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("details cols does not exist in the ref report");
        }
        return null;
    }

    private DataSet getSDCAttributesFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_attrsdata.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getTableDocFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_tabledoc.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getSDCOperationsFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_operationsdata.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getIndexInfoFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_indexinfo.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getRefersToSdcsFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_refersto.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return new DataSet();
    }

    private DataSet getReferredBySdcsFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_referredby.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return new DataSet();
    }

    private DataSet getReferredBySdcDetailsFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI).replace(".xml", "_referredbydetails.xml");
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return new DataSet();
    }

    private SDIData getSDIDataFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI);
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                SDIData sdiData = new SDIData(xml);
                sdiData.setSDIData(xml);
                return sdiData;
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private DataSet getColumnData(String sdcid) {
        String sql = "SELECT * from syscolumn where tableid in ( SELECT tableid from sdc where sdcid = ? )";
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcid});
    }

    private DataSet getDetailsColumnData(String sdcid) {
        String sql = "SELECT * from syscolumn where tableid in ( SELECT linktableid from sdclink where sdcid = ? and linktype='D')";
        DataSet ret = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{sdcid});
        if (ret == null) {
            return new DataSet();
        }
        return ret;
    }

    private DataSet getSDCAttributes(String sdcid) {
        String sql = "SELECT * from attributedef where  basedonid ='" + sdcid + "'";
        return this.getQueryProcessor().getSqlDataSet(sql);
    }

    private DataSet getSDCOperations(String sdcid) {
        String accessControl = this.getPrimaryValue("accesscontrolledflag");
        if ("D".equals(accessControl)) {
            String sql = "SELECT * from sdcoperation where  sdcid ='" + sdcid + "'";
            return this.getQueryProcessor().getSqlDataSet(sql);
        }
        return new DataSet();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class BaseSDCRO
extends BaseRO {
    protected ArrayList sdiList = null;
    public SDI currentSDI = null;
    public SDIData currentSDIData = null;
    public int currentSDIPosition = 0;
    private HashMap sdcProperties = null;
    private DataSet columnData = null;
    private String sdcId = "";

    public void setCurrentSDIData(SDIData sdiData) throws SapphireException {
        SDI sdi = BaseSDCRO.getSDI(sdiData);
        ArrayList<SDI> sdiArrayList = new ArrayList<SDI>();
        sdiArrayList.add(sdi);
        this.sdiList = sdiArrayList;
        this.currentSDIData = sdiData;
        this.currentSDI = sdi;
        this.sdcId = this.currentSDI.getSdcid();
        this.sdcProperties = this.getSDCProcessor().getSDCProperties(this.sdcId);
        this.columnData = this.getSDCProcessor().getColumnData(this.sdcId);
    }

    public void setSDIList(ArrayList sdiList) {
        this.sdiList = sdiList;
    }

    public DataSet getColumnData() {
        return this.columnData;
    }

    public HashMap getSDCProperties() {
        return this.sdcProperties;
    }

    @Override
    public void startChapter() throws SapphireException {
        this.sdcId = this.chapterName;
        if (this.dataSource.equals("DATABASE")) {
            this.sdcProperties = this.getSDCProcessor().getSDCProperties(this.sdcId);
            this.columnData = this.getSDCProcessor().getColumnData(this.sdcId);
        } else {
            this.sdcProperties = this.getSDCPropertiesFromXMLReport(this.refReportFolder);
            this.columnData = this.getColumnDataFromXMLReport(this.refReportFolder);
        }
        this.currentSDIPosition = 0;
    }

    public boolean hasNextSection() {
        return this.currentSDIPosition < this.sdiList.size();
    }

    public void reset() {
        this.currentSDIPosition = 0;
    }

    public void nextSection() throws SapphireException {
        if (!this.hasNextSection()) {
            throw new SapphireException("Reached end of sdiList");
        }
        this.currentSDI = (SDI)this.sdiList.get(this.currentSDIPosition);
        if (this.dataSource.equals("DATABASE")) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(this.sdcId);
            sdiRequest.setQueryFrom((String)this.sdcProperties.get("tableid"));
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
        } else {
            this.currentSDIData = this.getSDIDataFromXMLReport(this.refReportFolder);
        }
        if (this.currentSDIData == null) {
            throw new SapphireException("Failed to get sdidata for sdis ( " + this.currentSDI.getKeyid1() + "),(" + this.currentSDI.getKeyid2() + "),(" + this.currentSDI.getKeyid3() + ")");
        }
        ++this.currentSDIPosition;
    }

    public int gotoSection(SDI sdi) {
        this.currentSDI = null;
        this.currentSDIData = null;
        for (int i = 0; i < this.sdiList.size(); ++i) {
            SDI curr = (SDI)this.sdiList.get(i);
            if (!curr.getKeyid1().equalsIgnoreCase(sdi.getKeyid1()) || curr.getKeyid2() != null && !curr.getKeyid2().equalsIgnoreCase(sdi.getKeyid2()) || curr.getKeyid3() != null && !curr.getKeyid3().equalsIgnoreCase(sdi.getKeyid3())) continue;
            this.currentSDI = sdi;
            if (this.dataSource.equals("XMLREPORT")) {
                this.currentSDIData = this.getSDIDataFromXMLReport(this.refReportFolder);
                this.sdcProperties = this.getSDCPropertiesFromXMLReport(this.refReportFolder);
            } else {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(this.sdcId);
                sdiRequest.setQueryFrom((String)this.sdcProperties.get("tableid"));
                String whereClause = this.getKeyColId1() + "='" + this.currentSDI.getKeyid1() + "'";
                if (this.getKeyColCount() > 1 && !this.currentSDI.getKeyid2().equals("*")) {
                    whereClause = whereClause + " AND " + this.getKeyColId2() + "='" + this.currentSDI.getKeyid2() + "'";
                }
                if (this.getKeyColCount() == 3 && !this.currentSDI.getKeyid3().equals("*")) {
                    whereClause = whereClause + " AND " + this.getKeyColId2() + "='" + this.currentSDI.getKeyid2() + "'";
                }
                sdiRequest.setQueryWhere(whereClause);
                sdiRequest.setShowTemplates("true");
                sdiRequest.setRequestItem("all");
                sdiRequest.setExtendedDataTypes(true);
                sdiRequest.setOverrideLoadFlag(true);
                sdiRequest.setQueryOrderBy("usersequence");
                this.currentSDIData = this.getSDIProcessor().getSDIData(sdiRequest);
                this.sdcProperties = this.getSDCProcessor().getSDCProperties(this.sdcId);
            }
            this.currentSDIPosition = i;
            return i;
        }
        this.currentSDI = null;
        this.currentSDIData = null;
        this.currentSDIPosition = -1;
        return -1;
    }

    private SDIData getSDIDataFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.generateSDISectionXMLFileName(this.currentSDI);
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f, "UTF-8");
                SDIData sdiData = new SDIData(this.currentSDI.getSdcid());
                sdiData.setSDIData(xml);
                return sdiData;
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    private PropertyList getSDCPropertiesFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.sdcId + "_sdc_props.xml";
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f, "UTF-8");
                PropertyList pl = new PropertyList();
                pl.setPropertyList(xml, false, false);
                return pl;
            }
            if (this.sdcId.equals("SDC") && (f = new File(refSDIFileName = refReportFolder + "/xmlreport/SDC_sdc_sdcprops.xml")).exists()) {
                String xml = FileUtil.getFileString(f, "UTF-8");
                PropertyList pl = new PropertyList();
                pl.setPropertyList(xml, false, false);
                return pl;
            }
        }
        catch (SapphireException e) {
            Trace.log("Could not read sdcprops from xml report." + e.getMessage());
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return null;
    }

    protected DataSet getColumnDataFromXMLReport(String refReportFolder) {
        String refSDIFileName = refReportFolder + "/xmlreport/" + this.sdcId + "_column_data.xml";
        File f = new File(refSDIFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f, "UTF-8");
                DataSet ds = new DataSet(xml);
                return ds;
            }
        }
        catch (IOException e) {
            Trace.log("Column data does not exist in the ref report");
        }
        return null;
    }

    public String generateSDISectionXMLFileName(SDI currentSDI) {
        if (currentSDI != null) {
            String sdcId = currentSDI.getSdcid();
            String keyid1 = currentSDI.getKeyid1().trim();
            String keyid2 = currentSDI.getKeyid2().trim();
            String keyid3 = currentSDI.getKeyid3().trim();
            String sectionFileName = sdcId + "_" + keyid1;
            if (keyid2.length() > 0 && !"(null)".equals(keyid2)) {
                sectionFileName = sectionFileName + "_" + keyid2;
            }
            if (keyid3.length() > 0 && !"(null)".equals(keyid3)) {
                sectionFileName = sectionFileName + "_" + keyid3;
            }
            sectionFileName = sectionFileName.replaceAll(" ", "_");
            sectionFileName = ConfigReportContent.removeIllegalChars(sectionFileName);
            return sectionFileName + ".xml";
        }
        return "";
    }

    public int getKeyColCount() {
        try {
            return Integer.parseInt((String)this.sdcProperties.get("keycolumns"));
        }
        catch (Exception e) {
            return 1;
        }
    }

    public String getSDCDescription() {
        return (String)this.sdcProperties.get("description");
    }

    public String getSDCName() {
        return (String)this.sdcProperties.get("sdcid");
    }

    public String getKeyColId1() {
        return (String)this.sdcProperties.get("keycolid1");
    }

    public String getKeyColId2() {
        return (String)this.sdcProperties.get("keycolid2");
    }

    public String getKeyColId3() {
        return (String)this.sdcProperties.get("keycolid3");
    }

    public String getSDCSingular() {
        return (String)this.sdcProperties.get("singular");
    }

    public String getSDCPlural() {
        return (String)this.sdcProperties.get("plural");
    }

    public String getAccessControl() {
        return (String)this.sdcProperties.get("accesscontrolledflag");
    }

    public int getSDICount() {
        if (this.sdiList != null) {
            return this.sdiList.size();
        }
        return 0;
    }

    public String getKeyid1() {
        if (this.currentSDI != null) {
            return this.currentSDI.getKeyid1();
        }
        return "";
    }

    public String getKeyid2() {
        if (this.currentSDI != null) {
            return this.currentSDI.getKeyid2();
        }
        return "";
    }

    public String getKeyid3() {
        if (this.currentSDI != null) {
            return this.currentSDI.getKeyid3();
        }
        return "";
    }

    public String getDescription() {
        Object desc;
        if (this.sdcProperties != null && (desc = this.sdcProperties.get("desccol")) != null) {
            DataSet ds = this.getDataSet("primary");
            if (ds == null) {
                return "";
            }
            return ds.getValue(0, desc.toString(), "");
        }
        return "";
    }

    public String getTemplateFlag() {
        DataSet ds = this.getDataSet("primary");
        if (ds == null) {
            return "N";
        }
        return ds.getValue(0, "templateflag", "N");
    }

    public String[] getDataSetKeyCols(String dsName) {
        return this.currentSDIData.getLinkTableKeys(dsName);
    }

    public DataSet getDataSet(String dsName) {
        if (this.currentSDIData == null) {
            return new DataSet();
        }
        DataSet ds = this.currentSDIData.getDataset(dsName);
        if (ds != null && this.sdcId.equals("Profile") && dsName.equals("profileproperty")) {
            DataSet filtered = new DataSet();
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String currprop = ds.getString(i, "propertyid", "");
                if (currprop.contains("userconfig_")) continue;
                filtered.copyRow(ds, i, 1);
            }
            ds = filtered;
        }
        if (ds != null && ds.isValidColumn("usersequence")) {
            ds.sort("usersequence");
        }
        return ds;
    }

    public String getPrimaryValue(String columnName) {
        return this.getDataSet("primary").getValue(0, columnName);
    }

    public ArrayList getPrimaryColumns() {
        String[] cols = this.getDataSet("primary").getColumns();
        ArrayList<String> ret = new ArrayList<String>();
        boolean j = false;
        for (int i = 0; i < cols.length; ++i) {
            if (cols[i].startsWith("__") || cols[i].equals("createdt") || cols[i].equals("moddt") || cols[i].equals("modby") || cols[i].equals("createby") || cols[i].equals("createtool") || cols[i].equals("modtool") || cols[i].equals("auditsequence") || cols[i].equals("usersequence") || cols[i].equals("tracelogid")) continue;
            ret.add(cols[i]);
        }
        return ret;
    }

    public ArrayList getPrimaryColumnLabels() {
        ArrayList cols = this.getPrimaryColumns();
        ArrayList<String> ret = new ArrayList<String>();
        for (int i = 0; i < cols.size(); ++i) {
            DataSet currCol;
            HashMap filter = new HashMap();
            filter.put("columnid", cols.get(i));
            String currLabel = cols.get(i).toString();
            if (this.columnData != null && (currCol = this.columnData.getFilteredDataSet(filter)).getRowCount() > 0) {
                currLabel = currCol.getString(0, "columnlabel", cols.get(i).toString());
            }
            ret.add(currLabel);
        }
        return ret;
    }

    public String[] getDetailTables() {
        return this.currentSDIData.getLinkTables();
    }

    public String getLinkid(String detailtableid) {
        return this.currentSDIData.getLinkid(detailtableid);
    }

    public String[] getDetailLinkTables() {
        return this.currentSDIData.getDetailLinkTables();
    }

    public String[] getDetailLinkTableKeys(String detail) {
        return this.currentSDIData.getDetailLinkTableKeys(detail);
    }

    public ArrayList getDetailColumns(String tableName) {
        DataSet tableDetails = this.getDataSet(tableName);
        ArrayList<String> ret = new ArrayList<String>();
        if (tableDetails != null) {
            String[] cols = tableDetails.getColumns();
            boolean j = false;
            for (int i = 0; i < cols.length; ++i) {
                if (cols[i].startsWith("__") || cols[i].equals("createdt") || cols[i].equals("moddt") || cols[i].equals("modby") || cols[i].equals("createby") || cols[i].equals("createtool") || cols[i].equals("modtool") || cols[i].equals("auditsequence") || cols[i].equals("usersequence") || cols[i].equals("tracelogid")) continue;
                ret.add(cols[i]);
            }
        }
        return ret;
    }

    public DataSet getCategories() {
        DataSet ds = this.getDataSet("category");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Category ID", 0);
        ret.addColumnValues("Category ID", 0, ds.getColumnValues("categoryid", ";"), ";");
        return ret;
    }

    public DataSet getRoleMatrix() {
        DataSet roleMatrix = new DataSet();
        if (this.dataSource.equals("DATABASE")) {
            boolean atleastOneRoleFound = false;
            for (int i = 0; i < this.sdiList.size(); ++i) {
                SDI tempSDI = (SDI)this.sdiList.get(i);
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT roleid, keyid1 FROM sdirole WHERE sdcid = " + safeSQL.addVar(this.sdcId) + " AND keyid1=" + safeSQL.addVar(tempSDI.getKeyid1());
                DataSet currSDIRoles = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                roleMatrix.addRow();
                roleMatrix.setString(i, this.getKeyColId1(), tempSDI.getKeyid1());
                if (this.getKeyColCount() > 1) {
                    roleMatrix.setString(i, this.getKeyColId2(), tempSDI.getKeyid2());
                }
                if (this.getKeyColCount() > 2) {
                    roleMatrix.setString(i, this.getKeyColId3(), tempSDI.getKeyid3());
                }
                if (currSDIRoles == null) continue;
                for (int currRole = 0; currRole < currSDIRoles.getRowCount(); ++currRole) {
                    atleastOneRoleFound = true;
                    String currRoleId = currSDIRoles.getString(currRole, "roleid");
                    String includeImg = "<img src=\"" + this.folder + "/images/WEB-CORE/images/gif/Confirm.gif\" title=\"" + currRoleId + "\">";
                    roleMatrix.setString(i, currRoleId, includeImg);
                }
            }
            if (!atleastOneRoleFound) {
                return new DataSet();
            }
        } else {
            roleMatrix = this.getRoleMatrixFromXMLReport();
        }
        return roleMatrix;
    }

    public PropertyList getCurrentSDCProperties() {
        PropertyList pl = new PropertyList();
        if (this.sdcProperties != null) {
            Object[] keyes = this.sdcProperties.keySet().toArray();
            for (int i = 0; i < keyes.length; ++i) {
                String currPropVal = (String)this.sdcProperties.get(keyes[i].toString());
                pl.setProperty(keyes[i].toString(), currPropVal);
            }
        }
        return pl;
    }

    private DataSet getRoleMatrixFromXMLReport() {
        String fileName = this.refReportFolder + "/xmlreport/" + this.sdcId + "_sdirolematrix.xml";
        File f = new File(fileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f, "UTF-8");
                return new DataSet(xml);
            }
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
        }
        return new DataSet();
    }

    protected static SDI getSDI(SDIData sdiData) {
        String sdcid = sdiData.getSdcid();
        String[] keyes = sdiData.getKeys("primary");
        DataSet primary = sdiData.getDataset("primary");
        String keyid1 = primary.getValue(0, keyes[0]);
        String keyid2 = keyes.length > 1 ? primary.getValue(0, keyes[1]) : "";
        String keyid3 = keyes.length > 2 ? primary.getValue(0, keyes[2]) : "";
        return new SDI(sdcid, keyid1, keyid2, keyid3);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.XSS;
import com.labvantage.sapphire.gwt.shared.constants.DatasetNameConstants;
import com.labvantage.sapphire.xml.SDIDataHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDIData
implements Serializable,
DatasetNameConstants {
    private static String[] _datasettables = new String[]{"primary", "sdiattachment", "sdidata", "sdidataitem", "sdidataitemlimits", "sdidataapproval", "sdidatarelation", "sdidataitemspec", "sdispec", "sdispecrule", "sdiaddress", "sdicoc", "sdipricelist", "categoryitem", "sdirole", "sdiworkitem", "sdiworkitemitem", "sdiworkitemrelation", "sdiapproval", "sdiapprovalstep", "sdidocument", "sdiformrule", "sdieventplan", "sdieventplanitem", "sdieventplanitemproperty", "sdiworkflowrule", "sdinote", "pricelistitem", "chargelistitem", "workgroupitem", "workgroupparamlist", "sdiattribute", "sdialias", "sdicalendar", "sdiworksheetrule", "sdiattachmentoperation", "sdidatacapture", "sdiresourcerequirement", "scheduleplanitemexec", "sdidatacrosssdicalc"};
    private static String[] _datasetnames = new String[]{"primary", "attachment", "dataset", "dataitem", "datalimit", "dataapproval", "datarelation", "dataspec", "sdispec", "sdispecrule", "address", "coc", "pricelist", "category", "role", "sdiworkitem", "sdiworkitemitem", "workitemrelation", "approval", "approvalstep", "document", "formrule", "sdieventplan", "sdieventplanitem", "sdieventplanitemproperty", "sdiworkflowrule", "notes", "pricelistitem", "chargelistitem", "workgroupitem", "workgroupparamlist", "attribute", "sdialias", "calendar", "sdiworksheetrule", "attachmentoperation", "datacapture", "sdiresourcerequirement", "scheduleplanitemexec", "sdidatacrosssdicalc"};
    private static String[] _datasetcodes = new String[]{"pr", "at", "ds", "di", "dl", "dv", "dr", "dp", "sp", "sr", "ad", "cc", "pl", "ct", "rl", "wi", "wii", "wr", "ap", "as", "doc", "fr", "ep", "epi", "epip", "wfr", "nt", "pli", "cli", "wgi", "wgp", "atb", "al", "cal", "wsr", "sco", "sdc", "srr", "spe"};
    private String[][] _datasetkeys = new String[][]{{"keyid1", "keyid2", "keyid3"}, {"sdcid", "keyid1", "keyid2", "keyid3", "attachmentnum"}, {"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset"}, {"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid", "limittypeid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "approvalstep"}, {"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "relationid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid", "specid", "specversionid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "specid", "specversionid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "specid", "specversionid", "ruleno"}, {"sdcid", "keyid1", "keyid2", "keyid3", "addressid", "addresstype", "contactfunction"}, {"sdcid", "keyid1", "keyid2", "keyid3", "cocid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "pricelistid"}, {"sdcid", "keyid1", "categoryid"}, {"sdcid", "keyid1", "roleid", "privid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "workitemid", "workiteminstance"}, {"sdcid", "keyid1", "keyid2", "keyid3", "workitemid", "workiteminstance", "workitemitemid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "workitemid", "workiteminstance", "relationid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "approvaltypeid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "approvaltypeid", "approvalstep", "approvalstepinstance"}, {"sdcid", "keyid1", "keyid2", "keyid3", "documentid", "documentversionid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "formid", "forminstance"}, {"sdcid", "keyid1", "keyid2", "keyid3", "eventplanid", "eventplanversionid", "eventplaninstance"}, {"sdcid", "keyid1", "keyid2", "keyid3", "eventplanid", "eventplanversionid", "eventplaninstance", "eventplanitemid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "eventplanid", "eventplanversionid", "eventplaninstance", "eventplanitemid", "propertyid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "workflowdefid", "workflowdefversionid", "workflowdefvariantid", "taskdefitemid", "ioitemid", "workflowexecid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "notenum"}, {"pricelistid", "pricelistitemid"}, {"chargelistid", "chargelistitemid"}, {"workgroupid", "workgroupitemid"}, {"workgroupid", "paramlistid", "paramlistversionid", "variantid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "attributeid", "attributesdcid", "attributeinstance"}, {"sdcid", "keyid1", "keyid2", "keyid3", "aliasid", "aliastype"}, {"sdcid", "keyid1", "keyid2", "keyid3", "calendarid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "worksheetid", "worksheetinstance"}, {"sdcid", "keyid1", "keyid2", "keyid3", "attachmentoperationid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "datacaptureid"}, {"sdcid", "keyid1", "keyid2", "keyid3", "resourcenum"}, {"scheduleplanid", "scheduleplanitemid"}};
    private String sdcid;
    private String[] linkIds = null;
    private String[] linkTables = null;
    private String[][] linkTableKeys = null;
    private String[] detailLinkIds = null;
    private String[] detailDetailLinkIds = null;
    private String[] detailLinkTables = null;
    private String[][] detailLinkTableKeys = null;
    protected LinkedHashMap sdiData = new LinkedHashMap();
    protected LinkedHashMap linkedSDIData = new LinkedHashMap();
    private String rsetId = "";
    private String linkid;
    private int requestStatus;
    private int qualifiedRows;
    private LinkedHashMap<String, String> primaryfkrset = new LinkedHashMap();

    public SDIData() {
    }

    public SDIData(String sdcid) {
        this.sdcid = sdcid;
    }

    public SDIData(String keycolid1, String keycolid2, String keycolid3) {
        this.setPrimaryKeyCols(keycolid1, keycolid2, keycolid3);
    }

    public SDIData(String sdcid, String keycolid1, String keycolid2, String keycolid3) {
        this.sdcid = sdcid;
        this.setPrimaryKeyCols(keycolid1, keycolid2, keycolid3);
    }

    public SDIData(String[] primarykeycols) {
        for (int i = 0; i < 3; ++i) {
            this._datasetkeys[0][i] = primarykeycols[i];
        }
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public void setPrimaryKeyCols(String keycolid1, String keycolid2, String keycolid3) {
        this._datasetkeys[0][0] = keycolid1;
        this._datasetkeys[0][1] = keycolid2;
        this._datasetkeys[0][2] = keycolid3;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public boolean setSDIData(String xmlString) {
        boolean rc = true;
        try {
            SDIDataHandler handler = new SDIDataHandler(this);
            handler.setXMLString(xmlString);
            SaxUtil.parseString(handler);
        }
        catch (Exception e) {
            rc = false;
        }
        return rc;
    }

    public void setLinks(String[] linkids, String[] linktables) {
        this.linkIds = linkids;
        this.linkTables = linktables;
        this.linkTableKeys = new String[this.linkTables.length][];
    }

    public void setDetailLinks(String[] linkIds, String[] detailLinkIds, String[] linktables) {
        this.detailLinkIds = linkIds;
        this.detailDetailLinkIds = detailLinkIds;
        this.detailLinkTables = linktables;
        this.detailLinkTableKeys = new String[this.detailLinkTables.length][];
    }

    public void setLinkTableKeys(String linktable, String[] keys) {
        int index = this.findLinkTableIndex(linktable);
        if (index >= 0) {
            this.linkTableKeys[index] = keys;
        }
    }

    public void setDetailLinkTableKeys(String linktable, String[] keys) {
        int index = this.findDetailLinkTableIndex(linktable);
        if (index >= 0) {
            this.detailLinkTableKeys[index] = keys;
        }
    }

    public String getLinkid(String linktable) {
        int index = this.findLinkTableIndex(linktable);
        return index >= 0 ? this.linkIds[index] : null;
    }

    public String getDetailLinkid(String linktable) {
        int index = this.findDetailLinkTableIndex(linktable);
        return index >= 0 ? this.detailLinkIds[index] : null;
    }

    public String getDetailDetailLinkid(String linktable) {
        int index = this.findDetailLinkTableIndex(linktable);
        return index >= 0 ? this.detailDetailLinkIds[index] : null;
    }

    public String[] getLinkTableKeys(String linktable) {
        int index = this.findLinkTableIndex(linktable);
        return index >= 0 ? this.linkTableKeys[index] : null;
    }

    public String[] getDetailLinkTableKeys(String linktable) {
        int index = this.findDetailLinkTableIndex(linktable);
        return index >= 0 ? this.detailLinkTableKeys[index] : null;
    }

    public String[] getLinkTables() {
        ArrayList<String> linktables = new ArrayList<String>();
        if (this.linkTables != null) {
            for (int i = 0; i < this.linkTables.length; ++i) {
                if (this.linkTables[i].length() <= 0) continue;
                linktables.add(this.linkTables[i]);
            }
        }
        return linktables.size() > 0 ? linktables.toArray(new String[linktables.size()]) : null;
    }

    public String[] getDetailLinkTables() {
        ArrayList<String> linktables = new ArrayList<String>();
        if (this.detailLinkTables != null) {
            for (int i = 0; i < this.detailLinkTables.length; ++i) {
                if (this.detailLinkTables[i].length() <= 0) continue;
                linktables.add(this.detailLinkTables[i]);
            }
        }
        return linktables.size() > 0 ? linktables.toArray(new String[linktables.size()]) : null;
    }

    public String[] getKeys(String datasetname) {
        if (!"SpecSDC".equals(this.sdcid) && "spec".equals(datasetname)) {
            datasetname = "sdispec";
        } else if (!"SpecSDC".equals(this.sdcid) && "specrule".equals(datasetname)) {
            datasetname = "sdispecrule";
        } else if (!"WorkItem".equals(this.sdcid) && "workitem".equals(datasetname)) {
            datasetname = "sdiworkitem";
        } else if (!"WorkItem".equals(this.sdcid) && "workitemitem".equals(datasetname)) {
            datasetname = "sdiworkitemitem";
        }
        String[] keys = null;
        int index = this.findDatasetIndex(datasetname);
        if (index >= 0) {
            keys = this._datasetkeys[index];
        } else {
            index = this.findLinkTableIndex(datasetname);
            if (index >= 0) {
                keys = this.linkTableKeys[index];
            } else {
                index = this.findDetailLinkTableIndex(datasetname);
                if (index >= 0) {
                    keys = this.detailLinkTableKeys[index];
                }
            }
        }
        return keys;
    }

    public static String getDatasetCode(String datasetname) {
        int index = -1;
        for (int i = 0; i < _datasetnames.length && index == -1; ++i) {
            if (!datasetname.equalsIgnoreCase(_datasetnames[i])) continue;
            index = i;
        }
        return index >= 0 ? _datasetcodes[index] : datasetname;
    }

    public static String getDatasetName(String datasetcode) {
        int index = -1;
        for (int i = 0; i < _datasetcodes.length && index == -1; ++i) {
            if (!datasetcode.equalsIgnoreCase(_datasetcodes[i])) continue;
            index = i;
        }
        return index >= 0 ? _datasetnames[index] : datasetcode;
    }

    public static String getDatasetNameByTableName(String tablename) {
        int index = -1;
        for (int i = 0; i < _datasettables.length && index == -1; ++i) {
            if (!tablename.equalsIgnoreCase(_datasettables[i])) continue;
            index = i;
        }
        return index >= 0 ? _datasetnames[index] : tablename;
    }

    public static String getDatasetTablename(String datasetname) {
        int index = -1;
        for (int i = 0; i < _datasetnames.length && index == -1; ++i) {
            if (!datasetname.equalsIgnoreCase(_datasetnames[i])) continue;
            index = i;
        }
        return index >= 0 ? _datasettables[index] : datasetname;
    }

    public static String[] getDatasetCodes() {
        return _datasetcodes;
    }

    public static String[] getDatasetNames() {
        return _datasetnames;
    }

    public static String[] getDatasetTables() {
        return _datasettables;
    }

    public String[] getDataSetKeys(String datasetName) {
        return this._datasetkeys[this.findDatasetIndex(datasetName)];
    }

    public void setDataset(String dataset, ResultSet rs) {
        this.sdiData.put(dataset.toLowerCase(), new DataSet(rs));
    }

    public void setDataset(String dataset, DataSet ds) {
        this.sdiData.put(dataset.toLowerCase(), ds);
        if (ds != null && XSS.isMock()) {
            String[] keys = this.getKeys(dataset.toLowerCase());
            if (keys == null) {
                keys = this.getLinkTableKeys(dataset);
            }
            if (keys == null) {
                keys = this.getDetailLinkTableKeys(dataset);
            }
            List<String> keyList = keys == null ? null : Arrays.asList(keys);
            String[] cols = ds.getColumns();
            for (int row = 0; row < ds.getRowCount(); ++row) {
                for (int col = 0; col < cols.length; ++col) {
                    String columnid = cols[col];
                    if (!columnid.endsWith("id") || ds.getColumnType(columnid) != 0 || XSS.isExcludedColumn(columnid) || keyList != null && keyList.contains(columnid)) continue;
                    ds.setString(row, columnid, XSS.mock(ds.getValue(row, columnid), columnid));
                }
            }
        }
    }

    public void removeDataset(String datasetname) {
        this.sdiData.remove(datasetname.toLowerCase());
    }

    public void setSDIData(String dataname, SDIData sdiData) {
        this.linkedSDIData.put(dataname, sdiData);
    }

    public DataSet getDataset(String datasetname) {
        return (DataSet)this.sdiData.get(datasetname.toLowerCase());
    }

    public SDIData getSDIData(String sdidataname) {
        return (SDIData)this.linkedSDIData.get(sdidataname);
    }

    public Set getSDIData() {
        return this.linkedSDIData.keySet();
    }

    public Set getDatasets() {
        return this.sdiData.keySet();
    }

    public void setPrimaryFKRsetid(String primaryfkcolumnid, String rsetId) {
        if (this.primaryfkrset.containsKey(primaryfkcolumnid)) {
            String rset = this.primaryfkrset.get(primaryfkcolumnid);
            if (rset.length() > 0) {
                this.primaryfkrset.put(primaryfkcolumnid, rset + "|" + rsetId);
            } else {
                this.primaryfkrset.put(primaryfkcolumnid, rsetId);
            }
        } else {
            this.primaryfkrset.put(primaryfkcolumnid, rsetId);
        }
    }

    public String getPrimaryFKRsetid(String primaryfkcolumnid) {
        if (this.primaryfkrset.containsKey(primaryfkcolumnid)) {
            return this.primaryfkrset.get(primaryfkcolumnid);
        }
        return "";
    }

    public void setRsetid(String rsetid) {
        this.rsetId = rsetid;
    }

    public String getRsetid() {
        StringBuffer rsetout = new StringBuffer(this.rsetId);
        if (this.primaryfkrset.size() > 0) {
            for (String v : this.primaryfkrset.values()) {
                if (rsetout.length() > 0) {
                    rsetout.append("|");
                }
                rsetout.append(v);
            }
        }
        return rsetout.toString();
    }

    public String getPrimaryRsetid() {
        return this.rsetId;
    }

    public void setQualifiedRows(int rows) {
        this.qualifiedRows = rows;
    }

    public int getQualifiedRows() {
        return this.qualifiedRows;
    }

    public void setRequestStatus(int status) {
        this.requestStatus = status;
    }

    public int getRequestStatus() {
        return this.requestStatus;
    }

    public void sanitizeDataset(String datasetname, String sysuserid, String tool, Calendar now) {
        this.sanitizeDataset(datasetname, sysuserid, tool, now, null);
    }

    public void sanitizeDataset(String datasetname, String sysuserid, String tool, Calendar now, PropertyList props) {
        DataSet ds = (DataSet)this.sdiData.get(datasetname.toLowerCase());
        if (ds != null) {
            String currentValue;
            int i;
            if (ds.isValidColumn("createby")) {
                ds.setString(-1, "createby", sysuserid);
            }
            if (ds.isValidColumn("createtool")) {
                ds.setString(-1, "createtool", tool);
            }
            if (ds.isValidColumn("createdt")) {
                ds.setDate(-1, "createdt", now);
            }
            if (ds.isValidColumn("modby")) {
                ds.setString(-1, "modby", sysuserid);
            }
            if (ds.isValidColumn("modtool")) {
                ds.setString(-1, "modtool", tool);
            }
            if (ds.isValidColumn("moddt")) {
                ds.setDate(-1, "moddt", now);
            }
            Calendar nullCalendar = null;
            BigDecimal nullBigDecimal = null;
            if (datasetname.equals("dataset")) {
                ds.setString(-1, "notes", null);
                ds.setString(-1, "tracelogid", null);
                ds.setString(-1, "s_qcbatchid", null);
                ds.setString(-1, "s_qcbatchitemid", null);
                ds.setString(-1, "documentid", null);
                ds.setString(-1, "documentversionid", null);
                ds.setString(-1, "blockflag", null);
                ds.setString(-1, "sdidataid", null);
                ds.setString(-1, "s_datasetstatus", "Initial");
                ds.setString(-1, "completeddt", null);
                ds.setString(-1, "completedby", null);
                ds.setString(-1, "cancelledby", null);
                ds.setString(-1, "cancelleddt", null);
                ds.setString(-1, "starteddt", null);
                ds.setString(-1, "startedby", null);
            }
            if (datasetname.equals("dataitem")) {
                ds.setString(-1, "enteredtext", null);
                ds.setString(-1, "enteredunits", null);
                ds.setNumber(-1, "enteredvalue", nullBigDecimal);
                ds.setNumber(-1, "transformvalue", nullBigDecimal);
                ds.setDate(-1, "transformdt", nullCalendar);
                ds.setString(-1, "transformtext", null);
                ds.setString(-1, "displayvalue", null);
                ds.setString(-1, "enteredqualifier", null);
                ds.setString(-1, "enteredoperator", null);
                ds.setString(-1, "releasedflag", null);
                ds.setString(-1, "valuestatus", null);
                ds.setString(-1, "notes", null);
                ds.setString(-1, "textcolor", null);
                ds.setString(-1, "sdidataitemid", null);
                ds.setString(-1, "tracelogid", null);
                ds.setString(-1, "s_analystid", null);
                ds.setString(-1, "calcexcludeflag", null);
            }
            if (datasetname.equals("sdiworkitem")) {
                ds.setString(-1, "sdiworkitemid", null);
                ds.setString(-1, "workitemstatus", "Initial");
                ds.setString(-1, "tracelogid", null);
                ds.setString(-1, "completeddt", null);
                ds.setString(-1, "completedby", null);
                ds.setString(-1, "applieddt", null);
                ds.setString(-1, "appliedby", null);
                ds.setString(-1, "cancelledby", null);
                ds.setString(-1, "cancelleddt", null);
                ds.setString(-1, "starteddt", null);
                ds.setString(-1, "startedby", null);
            }
            if (datasetname.equals("datalimit")) {
                ds.setString(-1, "statusflag", null);
                ds.setString(-1, "tracelogid", null);
            }
            if (datasetname.equals("dataapproval")) {
                ds.setString(-1, "approvalflag", "U");
                ds.setString(-1, "notes", null);
                ds.setString(-1, "tracelogid", null);
            }
            if (datasetname.equals("dataspec")) {
                ds.setString(-1, "condition", null);
                ds.setString(-1, "tracelogid", null);
                ds.setString(-1, "checkedvalue", null);
                ds.setString(-1, "checkedtext", null);
            }
            if (datasetname.equals("notes")) {
                ds.setString(-1, "sdinoteid", null);
                ds.setString(-1, "resolvedflag", "N");
                ds.setString(-1, "resolveddt", null);
                ds.setString(-1, "resolvedby", null);
                ds.setString(-1, "resolvednote", null);
            }
            if (datasetname.equals("sdispec")) {
                ds.setString(-1, "condition", null);
            }
            if (datasetname.equals("sdispecrule")) {
                ds.setString(-1, "rulevalue", null);
            }
            if (datasetname.equals("approval")) {
                for (i = 0; i < ds.size(); ++i) {
                    currentValue = ds.getValue(i, "approvalflag");
                    if (currentValue.equals("N")) continue;
                    ds.setString(i, "approvalflag", "U");
                }
            }
            if (datasetname.equals("approvalstep")) {
                for (i = 0; i < ds.size(); ++i) {
                    currentValue = ds.getValue(i, "approvalflag");
                    if (currentValue.equals("N")) continue;
                    ds.setString(i, "approvalflag", "U");
                }
                ds.setString(-1, "notes", null);
                ds.setString(-1, "reviewedby", null);
                ds.setString(-1, "revieweddt", null);
            }
            if (datasetname.equals("sdidatacrosssdicalc")) {
                ds.setString(-1, "tracelogid", null);
            }
        }
    }

    public void setKeys(String datasetname, String keyid1, String keyid2, String keyid3) {
        DataSet ds = (DataSet)this.sdiData.get(datasetname.toLowerCase());
        if (ds != null) {
            if (datasetname.equals("pricelistitem")) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    ds.setString(i, "pricelistid", keyid1);
                    ds.setString(i, "pricelistitemid", String.valueOf(i));
                }
            } else if (datasetname.equals("chargelistitem")) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    ds.setString(i, "chargelistid", keyid1);
                    ds.setString(i, "chargelistitemid", String.valueOf(i));
                }
            } else if (datasetname.equals("workitemitem")) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    ds.setString(i, "workitemid", keyid1);
                }
            } else if (datasetname.equals("workgroupitem") || datasetname.equals("workgroupparamlist")) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    ds.setString(i, "workgroupid", keyid1);
                }
            } else if (datasetname.equals("role")) {
                if (keyid1 != null && keyid1.length() > 0) {
                    ds.setString(-1, "keyid1", keyid1);
                }
            } else if (datasetname.equals("primary")) {
                if (ds.isValidColumn(this._datasetkeys[0][0])) {
                    ds.setString(-1, this._datasetkeys[0][0], keyid1);
                }
                if (ds.isValidColumn(this._datasetkeys[0][1])) {
                    ds.setString(-1, this._datasetkeys[0][1], keyid2);
                }
                if (ds.isValidColumn(this._datasetkeys[0][2])) {
                    ds.setString(-1, this._datasetkeys[0][2], keyid3);
                }
            } else {
                if (ds.isValidColumn("keyid1")) {
                    ds.setString(-1, "keyid1", keyid1);
                }
                if (ds.isValidColumn("keyid2")) {
                    ds.setString(-1, "keyid2", keyid2);
                }
                if (ds.isValidColumn("keyid3")) {
                    ds.setString(-1, "keyid3", keyid3);
                }
            }
        }
    }

    private int findDatasetIndex(String datasetname) {
        int index = -1;
        for (int i = 0; i < _datasetnames.length && index == -1; ++i) {
            if (!datasetname.equalsIgnoreCase(_datasetnames[i])) continue;
            index = i;
        }
        return index;
    }

    private int findLinkTableIndex(String linktablename) {
        int index = -1;
        if (this.linkTables != null) {
            for (int i = 0; i < this.linkTables.length && index == -1; ++i) {
                if (!linktablename.equalsIgnoreCase(this.linkTables[i])) continue;
                index = i;
            }
        }
        return index;
    }

    private int findDetailLinkTableIndex(String linktablename) {
        int index = -1;
        if (this.detailLinkTables != null) {
            for (int i = 0; i < this.detailLinkTables.length && index == -1; ++i) {
                if (!linktablename.equalsIgnoreCase(this.detailLinkTables[i])) continue;
                index = i;
            }
        }
        return index;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("rsetId", this.getRsetid());
            jsonObj.put("sdcid", this.getSdcid());
            Set ds = this.getDatasets();
            Iterator it = ds.iterator();
            JSONObject datasets = new JSONObject();
            JSONObject keys = new JSONObject();
            JSONObject types = new JSONObject();
            while (it.hasNext()) {
                String name = it.next().toString();
                JSONObject dataset = this.getDataset(name).toJSONObject(true, true);
                datasets.put(name, dataset);
                keys.put(name, this.getKeys(name));
                String type = name.equalsIgnoreCase("primary") ? "primary" : (this.findDatasetIndex(name) >= 0 ? "sdixxx" : (this.findLinkTableIndex(name) >= 0 ? "detail" : "unknown"));
                types.put(name, type);
            }
            jsonObj.put("datasets", datasets);
            jsonObj.put("keys", keys);
            jsonObj.put("datasettype", types);
            JSONObject linkedSDIDataArray = new JSONObject();
            for (String linkedSDIDataName : this.getSDIData()) {
                linkedSDIDataArray.put(linkedSDIDataName, this.getSDIData(linkedSDIDataName).toJSONObject());
            }
            jsonObj.put("linkedsdidatas", linkedSDIDataArray);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return jsonObj;
    }

    public String toJSONString() {
        return this.toJSONObject().toString();
    }

    public String toXML() {
        return this.toXML(0, false);
    }

    public String toXML(int indentFactor, boolean ignoreEmpty) {
        return this.toXML(indentFactor, ignoreEmpty, false);
    }

    public String toXML(int indentFactor, boolean ignoreEmpty, boolean forceISOFormat) {
        int indentNum = indentFactor;
        StringBuffer output = new StringBuffer();
        String indentStr = "  ";
        output.append(StringUtil.repeat(indentStr, indentNum) + "<sdidata sdcid=\"" + this.sdcid + "\" >\n");
        output.append(StringUtil.repeat(indentStr, ++indentNum) + "<md>\n");
        this.serializeVariable(output, indentStr, indentNum, "_datasetkeys", this._datasetkeys);
        this.serializeVariable(output, indentStr, indentNum, "linkIds", this.linkIds);
        this.serializeVariable(output, indentStr, indentNum, "linkTables", this.linkTables);
        this.serializeVariable(output, indentStr, indentNum, "linkTableKeys", this.linkTableKeys);
        this.serializeVariable(output, indentStr, indentNum, "detailLinkIds", this.detailLinkIds);
        this.serializeVariable(output, indentStr, indentNum, "detailDetailLinkIds", this.detailDetailLinkIds);
        this.serializeVariable(output, indentStr, indentNum, "detailLinkTables", this.detailLinkTables);
        this.serializeVariable(output, indentStr, indentNum, "detailLinkTableKeys", this.detailLinkTableKeys);
        output.append(StringUtil.repeat(indentStr, indentNum--) + "</md>\n");
        Object[] keyes = this.sdiData.keySet().toArray();
        output.append(StringUtil.repeat(indentStr, ++indentNum) + "<datasetcollection>\n");
        for (int i = 0; i < keyes.length; ++i) {
            DataSet currDS = (DataSet)this.sdiData.get(keyes[i]);
            if (ignoreEmpty && currDS.getRowCount() == 0) continue;
            output.append(StringUtil.repeat(indentStr, ++indentNum) + "<datasetitem>\n");
            output.append(StringUtil.repeat(indentStr, ++indentNum) + "<datasetname>" + keyes[i] + "</datasetname>\n");
            --indentNum;
            currDS.setCdataEscape("@]@]@>");
            if (forceISOFormat) {
                currDS.setForceISOFormat(true);
            }
            String xml = currDS.toXML(true);
            if (forceISOFormat) {
                currDS.setForceISOFormat(false);
            }
            String dsXMLCDataEscape = "!]!]!>";
            int temp = 0;
            while (xml.indexOf(dsXMLCDataEscape) > -1) {
                dsXMLCDataEscape = "!]!" + ++temp + "]!>";
            }
            xml = xml.replaceAll("]]>", dsXMLCDataEscape);
            output.append(StringUtil.repeat(indentStr, ++indentNum) + "<datasetval cdataescape= \"" + dsXMLCDataEscape + "\"><![CDATA[" + xml + "]]></datasetval>\n");
            int n = --indentNum;
            --indentNum;
            output.append(StringUtil.repeat(indentStr, n) + "</datasetitem>\n");
        }
        output.append(StringUtil.repeat(indentStr, indentNum--) + "</datasetcollection>\n");
        output.append(StringUtil.repeat(indentStr, ++indentNum) + "<linkedsdidatacollection>\n");
        for (String linkedSDIDataName : this.getSDIData()) {
            output.append(StringUtil.repeat(indentStr, ++indentNum) + "<linkedsdidataitem>\n");
            output.append(StringUtil.repeat(indentStr, ++indentNum) + "<linkedsdidataname>" + linkedSDIDataName);
            output.append(StringUtil.repeat(indentStr, indentNum--) + "</linkedsdidataname>\n");
            SDIData linkedSDIDataTemp = this.getSDIData(linkedSDIDataName);
            output.append(StringUtil.repeat(indentStr, ++indentNum) + "<linkedsdidatavalue>\n");
            output.append(linkedSDIDataTemp.toXML(indentNum, ignoreEmpty, forceISOFormat));
            output.append(StringUtil.repeat(indentStr, indentNum--) + "</linkedsdidatavalue>\n");
            output.append(StringUtil.repeat(indentStr, indentNum--) + "</linkedsdidataitem>\n");
        }
        output.append(StringUtil.repeat(indentStr, indentNum--) + "</linkedsdidatacollection>\n");
        output.append(StringUtil.repeat(indentStr, indentNum) + "</sdidata>\n");
        return output.toString();
    }

    private void serializeVariable(StringBuffer output, String indentStr, int indentNum, String variableName, Object variable) {
        if (variable == null) {
            return;
        }
        if (variable instanceof String[]) {
            output.append(StringUtil.repeat(indentStr, ++indentNum) + "<mdi varname='" + variableName + "' dimension='1'>\n");
            String[] var = (String[])variable;
            ++indentNum;
            for (int i = 0; i < var.length; ++i) {
                output.append(StringUtil.repeat(indentStr, indentNum) + "<mdiv ri='" + i + "'>" + var[i] + "</mdiv>\n");
            }
            output.append(StringUtil.repeat(indentStr, --indentNum) + "</mdi>\n");
        } else if (variable instanceof String[][]) {
            output.append(StringUtil.repeat(indentStr, ++indentNum) + "<mdi varname='" + variableName + "' dimension='2'>\n");
            String[][] var = (String[][])variable;
            ++indentNum;
            for (int i = 0; i < var.length; ++i) {
                if (var[i] != null && var[i].length > 0) {
                    for (int j = 0; j < var[i].length; ++j) {
                        output.append(StringUtil.repeat(indentStr, indentNum) + "<mdiv ri='" + i + "' ci='" + j + "'>" + var[i][j] + "</mdiv>\n");
                    }
                    continue;
                }
                output.append(StringUtil.repeat(indentStr, indentNum) + "<mdiv ri='" + i + "' ci='-1'></mdiv>\n");
            }
            output.append(StringUtil.repeat(indentStr, --indentNum) + "</mdi>\n");
        }
    }

    public void setDataSetKeys(String[][] dataSetKeys) {
        this._datasetkeys = dataSetKeys;
    }

    public void setLinkid(String linkid) {
        this.linkid = linkid;
    }

    public String getLinkid() {
        return this.linkid;
    }
}


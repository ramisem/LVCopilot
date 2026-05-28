/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateUtil;
import com.labvantage.sapphire.modules.adhocbrowser.TableObject;
import com.labvantage.sapphire.modules.adhocbrowser.TableProperty;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.ServiceException;
import java.util.ArrayList;
import java.util.HashSet;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DetailTable
extends TableObject {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static final String SDIROLE = "sdirole";
    public static final String ROLE = "role";
    public static final String SYSUSERROLE = "sysuserrole";
    public static final String SDCSECURITY = "sdcsecurity";
    public static final String SECURITYSETSDC = "securitysetsdc";
    public static final String SECURITYSETITEM = "securitysetitem";
    public static final String SDISECURITYDEPARTMENT = "sdisecuritydepartment";
    public static final String SDISECURITYSET = "sdisecurityset";
    public static final String DEPARTMENTSYSUSER = "departmentsysuser";
    public static final String SDIALIAS = "sdialias";
    public static final String RSETITEMS = "rsetitems";
    public static final String RSETITEMSDS = "rsetitemsds";
    public static final String RSETITEMSNL = "rsetitemsnl";
    public static final String DOCUMENTFIELD = "documentfield";
    public static final String SDIATTRIBUTE = "sdiattribute";
    public static final String SDIDATA = "sdidata";
    public static final String SDIDATAAPPROVAL = "sdidataapproval";
    public static final String SDIDATARELATION = "sdidatarelation";
    public static final String SDIDATAITEM = "sdidataitem";
    public static final String SDIDATAITEMSPEC = "sdidataitemspec";
    public static final String SDIDATAITEMLIMITS = "sdidataitemlimits";
    public static final String SDIDATACAPTURE = "sdidatacapture";
    public static final String SDIWORKITEM = "sdiworkitem";
    public static final String SDIWORKITEMITEM = "sdiworkitemitem";
    public static final String SDISPEC = "sdispec";
    public static final String SDIATTACHMENT = "sdiattachment";
    public static final String SDIAPPROVAL = "sdiapproval";
    public static final String SDIAPPROVALSTEP = "sdiapprovalstep";
    public static final String SDIADDRESS = "sdiaddress";
    public static final String CATEGORYITEM = "categoryitem";
    private static HashSet detailTableSet = new HashSet();

    protected DetailTable(String tableid, String idProperty) {
        super(tableid, idProperty);
        DetailTable.addToDetailTableSet(tableid);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addToDetailTableSet(String tableid) {
        HashSet hashSet = detailTableSet;
        synchronized (hashSet) {
            detailTableSet.add(tableid);
        }
    }

    public static DetailTable getInstance(String tableid, QueryService queryService) {
        if (SDIALIAS.equals(tableid)) {
            return DetailTable.getSDIAliasTable();
        }
        if (SDCSECURITY.equals(tableid)) {
            return DetailTable.getSDCSecurityTable();
        }
        if (SECURITYSETSDC.equals(tableid)) {
            return DetailTable.getSecuritySetSDCTable();
        }
        if (SECURITYSETITEM.equals(tableid)) {
            return DetailTable.getSecuritySetItemTable();
        }
        if (SDIROLE.equals(tableid)) {
            return DetailTable.getSDIRoleTable();
        }
        if (ROLE.equals(tableid)) {
            return DetailTable.getRoleTable();
        }
        if (SYSUSERROLE.equals(tableid)) {
            return DetailTable.getSysuserRoleTable();
        }
        if (DOCUMENTFIELD.equals(tableid)) {
            return DetailTable.getDocumentFieldTable();
        }
        if (SDIATTRIBUTE.equals(tableid)) {
            return DetailTable.getSDIAttributeTable();
        }
        if (SDIDATACAPTURE.equals(tableid)) {
            return DetailTable.getSDIDataCaptureTable();
        }
        if ("v_worksheetmetadata".equals(tableid)) {
            return DetailTable.getWorksheetMetadataView();
        }
        if (RSETITEMS.equals(tableid)) {
            return DetailTable.getRsetitemsTable();
        }
        if (RSETITEMSDS.equals(tableid)) {
            return DetailTable.getRsetitemsdsTable();
        }
        if (RSETITEMSDS.equals(tableid)) {
            return DetailTable.getRsetitemsnlTable();
        }
        try {
            SafeSQL safeSQL = new SafeSQL();
            DataSet columnDs = queryService.getPreparedSqlDataSet("SELECT columnid, datatype from syscolumn WHERE tableid=" + safeSQL.addVar("worksheetsdi_Sample".equals(tableid) ? "s_sample" : tableid) + " order by columnsequence", safeSQL.getValues());
            if ("worksheetsdi_Sample".equals(tableid)) {
                int row = columnDs.addRow();
                columnDs.setValue(row, "columnid", "worksheetid");
                columnDs.setValue(row, "datatype", "C");
                row = columnDs.addRow();
                columnDs.setValue(row, "columnid", "worksheetversionid");
                columnDs.setValue(row, "datatype", "C");
                row = columnDs.addRow();
                columnDs.setValue(row, "columnid", "sdcid");
                columnDs.setValue(row, "datatype", "C");
                return DetailTable.getDetailTable(tableid, columnDs, true);
            }
            return DetailTable.getDetailTable(tableid, columnDs);
        }
        catch (ServiceException se) {
            throw new RuntimeException("Unsupported Detail Table Error:" + tableid);
        }
    }

    private static DetailTable getDetailTable(String tableid, DataSet columnDs) {
        return DetailTable.getDetailTable(tableid, columnDs, false);
    }

    private static DetailTable getDetailTable(String tableid, DataSet columnDs, boolean includeFirstColumn) {
        int startIndex;
        DetailTable detailTable = new DetailTable(tableid, columnDs.getValue(0, "columnid"));
        for (int c = startIndex = includeFirstColumn ? 0 : 1; c < columnDs.getRowCount(); ++c) {
            String columntype = columnDs.getValue(c, "datatype");
            if ("C".equals(columntype)) {
                detailTable.addTableProperty(new TableProperty(columnDs.getValue(c, "columnid"), "string"));
                continue;
            }
            if ("D".equals(columntype)) {
                detailTable.addTableProperty(new TableProperty(columnDs.getValue(c, "columnid"), "timestamp"));
                continue;
            }
            if (!"N".equals(columntype)) continue;
            detailTable.addTableProperty(new TableProperty(columnDs.getValue(c, "columnid"), "big_decimal"));
        }
        return detailTable;
    }

    private static DetailTable getRoleTable() {
        DetailTable detailTable = new DetailTable(ROLE, "roleid");
        detailTable.addTableProperty(new TableProperty("roledesc", "string"));
        detailTable.addTableProperty(new TableProperty("activeflag", "string"));
        return detailTable;
    }

    private static DetailTable getSDIRoleTable() {
        DetailTable detailTable = new DetailTable(SDIROLE, "roleid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("keyid1", "string"));
        return detailTable;
    }

    private static DetailTable getSysuserRoleTable() {
        DetailTable detailTable = new DetailTable(SYSUSERROLE, "sysuserid");
        detailTable.addTableProperty(new TableProperty("roleid", "string"));
        return detailTable;
    }

    private static DetailTable getSDCSecurityTable() {
        DetailTable detailTable = new DetailTable(SDCSECURITY, "sdcid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("operationid", "string"));
        detailTable.addTableProperty(new TableProperty("sysuserid", "string"));
        detailTable.addTableProperty(new TableProperty("accesstype", "string"));
        return detailTable;
    }

    private static DetailTable getSecuritySetSDCTable() {
        DetailTable detailTable = new DetailTable(SECURITYSETSDC, "securitysetid");
        detailTable.addTableProperty(new TableProperty("securitysetid", "string"));
        detailTable.addTableProperty(new TableProperty("securitysetsdcid", "string"));
        return detailTable;
    }

    private static DetailTable getSecuritySetItemTable() {
        DetailTable detailTable = new DetailTable(SECURITYSETITEM, "securitysetid");
        detailTable.addTableProperty(new TableProperty("securitysetid", "string"));
        detailTable.addTableProperty(new TableProperty("securitysetsdcid", "string"));
        detailTable.addTableProperty(new TableProperty("operationid", "string"));
        detailTable.addTableProperty(new TableProperty("securitysetitemid", "string"));
        detailTable.addTableProperty(new TableProperty("itemtypeflag", "string"));
        return detailTable;
    }

    private static DetailTable getSDIAliasTable() {
        DetailTable detailTable = new DetailTable(SDIALIAS, "sdcid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("keyid1", "string"));
        detailTable.addTableProperty(new TableProperty("keyid2", "string"));
        detailTable.addTableProperty(new TableProperty("keyid3", "string"));
        detailTable.addTableProperty(new TableProperty("aliasid", "string"));
        detailTable.addTableProperty(new TableProperty("aliastype", "string"));
        return detailTable;
    }

    private static DetailTable getSDIDataCaptureTable() {
        DetailTable detailTable = new DetailTable(SDIDATACAPTURE, "sdcid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("keyid1", "string"));
        detailTable.addTableProperty(new TableProperty("keyid2", "string"));
        detailTable.addTableProperty(new TableProperty("keyid3", "string"));
        detailTable.addTableProperty(new TableProperty("datacaptureid", "string"));
        return detailTable;
    }

    private static DetailTable getDocumentFieldTable() {
        DetailTable detailTable = new DetailTable(DOCUMENTFIELD, "documentid");
        detailTable.addTableProperty(new TableProperty("documentid", "string"));
        detailTable.addTableProperty(new TableProperty("documentversionid", "big_decimal"));
        detailTable.addTableProperty(new TableProperty("fieldid", "string"));
        detailTable.addTableProperty(new TableProperty("fieldinstance", "string"));
        detailTable.addTableProperty(new TableProperty("fieldstatus", "string"));
        detailTable.addTableProperty(new TableProperty("enteredtext", "string"));
        detailTable.addTableProperty(new TableProperty("datevalue", "timestamp"));
        detailTable.addTableProperty(new TableProperty("numericvalue", "big_decimal"));
        return detailTable;
    }

    private static DetailTable getSDIAttributeTable() {
        DetailTable detailTable = new DetailTable(SDIATTRIBUTE, "attributeid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("keyid1", "string"));
        detailTable.addTableProperty(new TableProperty("keyid2", "string"));
        detailTable.addTableProperty(new TableProperty("keyid3", "string"));
        detailTable.addTableProperty(new TableProperty("attributeid", "string"));
        detailTable.addTableProperty(new TableProperty("attributesdcid", "string"));
        detailTable.addTableProperty(new TableProperty("attributeinstance", "big_decimal"));
        detailTable.addTableProperty(new TableProperty("datatype", "string"));
        detailTable.addTableProperty(new TableProperty("textvalue", "string"));
        detailTable.addTableProperty(new TableProperty("datevalue", "timestamp"));
        detailTable.addTableProperty(new TableProperty("numericvalue", "big_decimal"));
        return detailTable;
    }

    private static DetailTable getWorksheetMetadataView() {
        DetailTable detailTable = new DetailTable("v_worksheetmetadata", "worksheetid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("keyid1", "string"));
        detailTable.addTableProperty(new TableProperty("keyid2", "string"));
        detailTable.addTableProperty(new TableProperty("keyid3", "string"));
        detailTable.addTableProperty(new TableProperty("attributeid", "string"));
        detailTable.addTableProperty(new TableProperty("attributesdcid", "string"));
        detailTable.addTableProperty(new TableProperty("attributeinstance", "big_decimal"));
        detailTable.addTableProperty(new TableProperty("datatype", "string"));
        detailTable.addTableProperty(new TableProperty("textvalue", "string"));
        detailTable.addTableProperty(new TableProperty("datevalue", "timestamp"));
        detailTable.addTableProperty(new TableProperty("numericvalue", "big_decimal"));
        return detailTable;
    }

    private static DetailTable getRsetitemsTable() {
        DetailTable detailTable = new DetailTable(RSETITEMS, "rsetid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("keyid1", "string"));
        detailTable.addTableProperty(new TableProperty("keyid2", "string"));
        detailTable.addTableProperty(new TableProperty("keyid3", "string"));
        return detailTable;
    }

    private static DetailTable getRsetitemsnlTable() {
        DetailTable detailTable = new DetailTable(RSETITEMSNL, "rsetid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("keyid1", "string"));
        detailTable.addTableProperty(new TableProperty("keyid2", "string"));
        detailTable.addTableProperty(new TableProperty("keyid3", "string"));
        return detailTable;
    }

    private static DetailTable getRsetitemsdsTable() {
        DetailTable detailTable = new DetailTable(RSETITEMSDS, "rsetid");
        detailTable.addTableProperty(new TableProperty("sdcid", "string"));
        detailTable.addTableProperty(new TableProperty("keyid1", "string"));
        detailTable.addTableProperty(new TableProperty("keyid2", "string"));
        detailTable.addTableProperty(new TableProperty("keyid3", "string"));
        detailTable.addTableProperty(new TableProperty("paramlistid", "string"));
        detailTable.addTableProperty(new TableProperty("paramlistversionid", "big_decimal"));
        detailTable.addTableProperty(new TableProperty("variantid", "string"));
        detailTable.addTableProperty(new TableProperty("dataset", "big_decimal"));
        return detailTable;
    }

    public static String getDetailJoinClause(String detailtableid, String joinalias, PropertyList sdcPropertyList) {
        String sdcid = sdcPropertyList.getProperty("sdcid");
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        String prefix = "";
        prefix = "trackitem".equals(detailtableid) ? "link" : "";
        boolean isMtoMDetail = detailtableid.indexOf("sdi") != 0 && !detailtableid.equals("trackitem") && !detailtableid.equals(DOCUMENTFIELD) && !detailtableid.equals(CATEGORYITEM);
        return " left join " + tableid + "." + detailtableid + " as " + joinalias + (isMtoMDetail ? "" : " with " + joinalias + "." + prefix + "sdcid='" + sdcid + "' ");
    }

    public static String getDetailJoinWhereClause(String detailtableid, String joinalias, PropertyList sdcPropertyList) {
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        String prefix = "";
        prefix = "trackitem".equals(detailtableid) ? "link" : "";
        PropertyList detailtablePL = null;
        PropertyListCollection tables = sdcPropertyList.getCollection("tables");
        for (Object p : tables) {
            if (p == null || !(p instanceof PropertyList) || !detailtableid.equals(((PropertyList)p).getProperty("tableid"))) continue;
            detailtablePL = (PropertyList)p;
            break;
        }
        boolean isMtoMDetail = false;
        String detailkeycolid2 = prefix + "keyid2";
        String detailkeycolid3 = prefix + "keyid3";
        if (detailtablePL != null) {
            detailkeycolid2 = detailtablePL.getProperty("keycolid2");
            detailkeycolid3 = detailtablePL.getProperty("keycolid3");
        } else {
            boolean bl = isMtoMDetail = detailtableid.indexOf("sdi") != 0 && !detailtableid.equals("trackitem") && !detailtableid.equals(DOCUMENTFIELD) && !detailtableid.equals(CATEGORYITEM);
        }
        return isMtoMDetail ? "" : (keycolid2.length() > 0 ? " (" + joinalias + "." + detailkeycolid2 + " is null or " + joinalias + "." + detailkeycolid2 + "=" + tableid + "." + keycolid2 + ")" : "") + (keycolid3.length() > 0 ? " and (" + joinalias + "." + detailkeycolid3 + " is null or " + joinalias + "." + detailkeycolid3 + "=" + tableid + "." + keycolid3 + ")" : "");
    }

    public static boolean isDetailColumnWithQualifier(String columndef) {
        if (columndef.indexOf(".") > 0) {
            String qualifier = columndef.substring(0, columndef.indexOf("."));
            return DetailTable.isDetailTable(qualifier);
        }
        return false;
    }

    public static boolean isDetailTable(String tableid) {
        return detailTableSet.contains(tableid);
    }

    public static void getKeyColumnSelect(String tableid, StringBuffer selectbuffer, ArrayList selectList, SapphireHibernateUtil shu) {
        if (SDIDATAITEM.equals(tableid) || SDIDATA.equals(tableid)) {
            String keyid1syntax = tableid + ".keyid1";
            String fkReftableid = shu.getReferenceEntityName(tableid, "keyid1");
            if (selectList.size() == 0 || fkReftableid != null && fkReftableid.length() > 0) {
                if (fkReftableid != null && fkReftableid.length() > 0) {
                    keyid1syntax = tableid + ".keyid1_column";
                }
                selectbuffer.append(tableid + ".sdcid,").append(keyid1syntax + ",").append(tableid + ".keyid2,").append(tableid + ".keyid3,").append(tableid + ".paramlistid,").append(tableid + ".paramlistversionid,").append(tableid + ".variantid,").append(tableid + ".dataset");
                selectList.add("sdcid");
                selectList.add("keyid1");
                selectList.add("keyid2");
                selectList.add("keyid3");
                selectList.add("paramlistid");
                selectList.add("paramlistversionid");
                selectList.add("variantid");
                selectList.add("dataset");
                if (SDIDATAITEM.equals(tableid)) {
                    selectbuffer.append("," + tableid + ".paramid_column").append("," + tableid + ".paramtype").append("," + tableid + ".replicateid").append("," + tableid + ".sdidataitemid");
                    selectList.add("paramid");
                    selectList.add("paramtype");
                    selectList.add("replicateid");
                    selectList.add("sdidataitemid");
                }
                if (SDIDATA.equals(tableid)) {
                    selectbuffer.append("," + tableid + ".sdidataid");
                    selectList.add("sdidataid");
                }
            }
        }
    }

    static {
        detailTableSet.add("trackitem");
        detailTableSet.add(SDIDATA);
        detailTableSet.add(SDIDATAAPPROVAL);
        detailTableSet.add(SDIDATARELATION);
        detailTableSet.add(SDIDATAITEM);
        detailTableSet.add(SDIDATAITEMSPEC);
        detailTableSet.add(SDIDATAITEMLIMITS);
        detailTableSet.add(SDIALIAS);
        detailTableSet.add(SDIWORKITEM);
        detailTableSet.add(SDIWORKITEMITEM);
        detailTableSet.add(SDIAPPROVAL);
        detailTableSet.add(SDIAPPROVALSTEP);
        detailTableSet.add(SDIADDRESS);
        detailTableSet.add(CATEGORYITEM);
        detailTableSet.add(SDISPEC);
        detailTableSet.add(SDIATTACHMENT);
        detailTableSet.add(SDIDATACAPTURE);
    }
}


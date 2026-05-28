/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ChildSampleUtil {
    public static final String CHILDSAMPLEPOLICY = "ChildSamplePolicy";
    public static final String CHILDTYPE_ALIQUOT = "aliquot";
    public static final String CHILDTYPE_DERIVATIVE = "derivative";
    public static final String CHILDTYPE_POOL = "pool";
    public static final String CHILDTYPE_CHILDSTUDY = "childstudy";
    private static List<String> sampleExcludeColumns = new ArrayList<String>();
    private static List<String> trackitemExcludeColumns = new ArrayList<String>();
    private static List<String> familyExcludeColumns = new ArrayList<String>();

    public static Map<String, String> getCopyDownValues(ConfigurationProcessor configurationProcessor, QueryProcessor queryProcessor, String sdcid, String parentsampleid, String childtype, String additionalparentcolumns) {
        HashMap<String, String> map;
        block20: {
            DataSet ds;
            Set<String> columnSet;
            map = new HashMap<String, String>();
            if (!OpalUtil.isNotEmpty(parentsampleid) || (columnSet = ChildSampleUtil.getCopyDownColumns(configurationProcessor, childtype, sdcid, additionalparentcolumns)) == null || columnSet.size() <= 0) break block20;
            StringBuilder sql = new StringBuilder();
            sql.append("select ").append(OpalUtil.toDelimitedString(columnSet, ","));
            SafeSQL safeSQL = new SafeSQL();
            if ("Sample".equals(sdcid)) {
                sql.append(" from s_sample");
                sql.append(" where s_sampleid in (").append(safeSQL.addIn(parentsampleid, ";")).append(")");
            } else if ("TrackItemSDC".equals(sdcid)) {
                sql.append(" from trackitem");
                sql.append(" where linksdcid = 'Sample' and linkkeyid1 in (").append(safeSQL.addIn(parentsampleid, ";")).append(")");
            } else if ("LV_SampleFamily".equals(sdcid)) {
                sql.append(" from s_samplefamily");
                sql.append(" where s_samplefamilyid in ( select s.samplefamilyid from s_sample s where s.s_sampleid in (").append(safeSQL.addIn(parentsampleid, ";")).append("))");
            } else {
                sdcid = null;
            }
            if (sdcid != null && (ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null) {
                if (ds.size() == 1) {
                    for (String columnid : columnSet) {
                        String value = ds.getValue(0, columnid, "");
                        if (!OpalUtil.isNotEmpty(value)) continue;
                        map.put(columnid, value);
                    }
                } else {
                    for (String columnid : columnSet) {
                        boolean copyvalue = true;
                        String columnvalue = "";
                        if (CHILDTYPE_POOL.equals(childtype)) {
                            boolean ignorenull = "Y".equals(ChildSampleUtil.getChildSamplePolicy(configurationProcessor).getPropertyListNotNull("copydowncolumns").getPropertyListNotNull(CHILDTYPE_POOL).getProperty("ignorenull", "N"));
                            for (int i = 0; i < ds.size(); ++i) {
                                if (!copyvalue) continue;
                                String dscolumnvalue = ds.getValue(i, columnid);
                                if (OpalUtil.isEmpty(dscolumnvalue)) {
                                    if (ignorenull) continue;
                                    copyvalue = false;
                                    break;
                                }
                                if (columnvalue.length() == 0) {
                                    columnvalue = dscolumnvalue;
                                }
                                if (dscolumnvalue.equals(columnvalue)) continue;
                                copyvalue = false;
                            }
                        } else {
                            columnvalue = ds.getValue(0, columnid, "");
                            if (OpalUtil.isNotEmpty(columnvalue)) {
                                for (int i = 1; i < ds.size(); ++i) {
                                    if (!copyvalue || ds.getValue(i, columnid).equals(columnvalue)) continue;
                                    copyvalue = false;
                                }
                            } else {
                                copyvalue = false;
                            }
                        }
                        if (!copyvalue) continue;
                        map.put(columnid, columnvalue);
                    }
                }
            }
        }
        return map;
    }

    public static PropertyList getChildSamplePolicy(ConfigurationProcessor configurationProcessor) {
        try {
            return configurationProcessor.getPolicy(CHILDSAMPLEPOLICY, "Sapphire Custom");
        }
        catch (SapphireException e) {
            return new PropertyList();
        }
    }

    public static Set<String> getCopyDownColumns(ConfigurationProcessor configurationProcessor, String childtype, String sdcid, String additionalparentcolumns) {
        PropertyListCollection columns;
        HashSet<String> columnSet = new HashSet<String>();
        List<String> excludeColumnList = null;
        String collectionname = "";
        if ("Sample".equals(sdcid)) {
            collectionname = "samplecolumns";
            excludeColumnList = sampleExcludeColumns;
        } else if ("TrackItemSDC".equals(sdcid)) {
            collectionname = "trackitemcolumns";
            excludeColumnList = trackitemExcludeColumns;
        } else if ("LV_SampleFamily".equals(sdcid)) {
            collectionname = "samplefamilycolumns";
            excludeColumnList = familyExcludeColumns;
        }
        if (excludeColumnList != null && (columns = ChildSampleUtil.getChildSamplePolicy(configurationProcessor).getPropertyListNotNull("copydowncolumns").getPropertyListNotNull(childtype.toLowerCase()).getCollectionNotNull(collectionname)) != null) {
            for (int i = 0; i < columns.size(); ++i) {
                String columnid;
                PropertyList pl = columns.getPropertyList(i);
                if ("N".equals(pl.getProperty("copydown")) || !OpalUtil.isNotEmpty(columnid = pl.getProperty("columnid")) || excludeColumnList.contains(columnid)) continue;
                columnSet.add(columnid);
            }
            if (OpalUtil.isNotEmpty(additionalparentcolumns)) {
                String[] additionalColumns;
                for (String additionalColumn : additionalColumns = StringUtil.split(additionalparentcolumns, ";")) {
                    String columnid = additionalColumn;
                    if (!OpalUtil.isNotEmpty(columnid) || excludeColumnList.contains(columnid = columnid.toLowerCase())) continue;
                    columnSet.add(columnid);
                }
            }
        }
        return columnSet;
    }

    static {
        sampleExcludeColumns.add("activeflag");
        sampleExcludeColumns.add("auditsequence");
        sampleExcludeColumns.add("createby");
        sampleExcludeColumns.add("createdt");
        sampleExcludeColumns.add("createtool");
        sampleExcludeColumns.add("glpflag");
        sampleExcludeColumns.add("modby");
        sampleExcludeColumns.add("moddt");
        sampleExcludeColumns.add("modtool");
        sampleExcludeColumns.add("pooledflag");
        sampleExcludeColumns.add("previousstoragestatus");
        sampleExcludeColumns.add("s_sampleid");
        sampleExcludeColumns.add("samplestatus");
        sampleExcludeColumns.add("samplefamilyid");
        sampleExcludeColumns.add("securitydepartment");
        sampleExcludeColumns.add("securityset");
        sampleExcludeColumns.add("securityuser");
        sampleExcludeColumns.add("sourcesdiworkitemid");
        sampleExcludeColumns.add("specimentype");
        sampleExcludeColumns.add("sstudyid");
        sampleExcludeColumns.add("storagestatus");
        sampleExcludeColumns.add("templateflag");
        sampleExcludeColumns.add("tracelogid");
        sampleExcludeColumns.add("usersequence");
        trackitemExcludeColumns.add("activeflag");
        trackitemExcludeColumns.add("auditsequence");
        trackitemExcludeColumns.add("containertypeid");
        trackitemExcludeColumns.add("createby");
        trackitemExcludeColumns.add("createdt");
        trackitemExcludeColumns.add("createtool");
        trackitemExcludeColumns.add("currentstorageunitid");
        trackitemExcludeColumns.add("custodialdepartmentid");
        trackitemExcludeColumns.add("custodialuserid");
        trackitemExcludeColumns.add("custodytakendt");
        trackitemExcludeColumns.add("freezethawcount");
        trackitemExcludeColumns.add("freezethawcountmax");
        trackitemExcludeColumns.add("freezethawcountwarn");
        trackitemExcludeColumns.add("freezethawflag");
        trackitemExcludeColumns.add("linkkeyid1");
        trackitemExcludeColumns.add("linkkeyid2");
        trackitemExcludeColumns.add("linkkeyid3");
        trackitemExcludeColumns.add("linksdcid");
        trackitemExcludeColumns.add("modby");
        trackitemExcludeColumns.add("moddt");
        trackitemExcludeColumns.add("modtool");
        trackitemExcludeColumns.add("reservesotageunitid");
        trackitemExcludeColumns.add("templateflag");
        trackitemExcludeColumns.add("tracelogid");
        trackitemExcludeColumns.add("trackitemid");
        trackitemExcludeColumns.add("usersequence");
        familyExcludeColumns.add("activeflag");
        familyExcludeColumns.add("auditsequence");
        familyExcludeColumns.add("createby");
        familyExcludeColumns.add("createdt");
        familyExcludeColumns.add("createtool");
        familyExcludeColumns.add("modby");
        familyExcludeColumns.add("moddt");
        familyExcludeColumns.add("modtool");
        familyExcludeColumns.add("s_samplefamilyid");
        familyExcludeColumns.add("templateflag");
        familyExcludeColumns.add("tracelogid");
        familyExcludeColumns.add("usersequence");
    }
}


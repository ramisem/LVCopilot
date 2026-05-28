/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDCExpiryValidationUtil {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public static void validateExpiry(String sdcid, String linkSDC, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, StringBuffer message, SDCProcessor sdcProcessor) {
        boolean isDetailLink;
        HashMap linkKeyCols = new HashMap();
        HashMap<String, String> detailLinkProps = new HashMap<String, String>();
        HashMap errors = new HashMap();
        String keyid1 = keyCol.get("keyid1");
        String keyid2 = keyCol.get("keyid2");
        String keyid3 = keyCol.get("keyid3");
        String keyid1Values = keyColValues.get(keyid1);
        String keyid2Values = keyColValues.get(keyid2);
        String keyid3Values = keyColValues.get(keyid3);
        boolean isReverseLink = SDCExpiryValidationUtil.checkLinkData(sdcid, linkSDC, detailLinkProps, sdcProcessor);
        if (isReverseLink) {
            DataSet linkds = SDCExpiryValidationUtil.getLinkData(linkSDC, keyCol, keyColValues, "E", sdcProcessor);
            if (linkds != null && !linkds.isEmpty()) {
                errors = SDCExpiryValidationUtil.getErrorCollection(linkSDC, linkds, sdcProcessor);
                SDCExpiryValidationUtil.addToErrorMessage(linkSDC, message, errors);
            }
            HashMap keyColsValuesForCurrentVerCheck = new HashMap();
            keyColsValuesForCurrentVerCheck = (HashMap)keyColValues.clone();
            keyColsValuesForCurrentVerCheck.put(keyCol.get("keyid2"), "C");
            linkds = SDCExpiryValidationUtil.getLinkData(linkSDC, keyCol, keyColsValuesForCurrentVerCheck, "E", sdcProcessor);
            if (linkds != null && !linkds.isEmpty()) {
                errors = SDCExpiryValidationUtil.getErrorCollection(linkSDC, linkds, sdcProcessor);
                SDCExpiryValidationUtil.addToErrorMessage(linkSDC, message, errors);
            }
        }
        if (isDetailLink = SDCExpiryValidationUtil.checkDetailLinkData(linkSDC, sdcid, detailLinkProps, sdcProcessor)) {
            errors = SDCExpiryValidationUtil.validateExpiryWithFkDetailLinkData(linkSDC, keyCol, keyColValues, detailLinkProps, sdcProcessor);
            SDCExpiryValidationUtil.addToErrorMessage(linkSDC, message, errors);
        }
    }

    public static String getActualVersionId(String sdcid, LinkedHashMap<String, String> keyCols, HashMap<String, String> keyColValues, SDCProcessor sdcProcessor) throws SapphireException, SQLException {
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(sdcid);
        String tableid = sdcPropertyList.getProperty("tableid");
        Integer keycolCount = Integer.parseInt(sdcPropertyList.getProperty("keycolumns"));
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        String primarykeyid1 = keyCols.get("keyid1");
        String primarykeyid2 = keyCols.get("keyid2");
        String primarykeyid3 = keyCols.get("keyid3");
        String primarykeyid1Value = keyColValues.get(primarykeyid1);
        String primarykeyid2Value = keyColValues.get(primarykeyid2);
        String primarykeyid3Value = keyColValues.get(primarykeyid3);
        String actualprimarykeyid2Value = "";
        SapphireConnection con = new ConnectionProcessor(sdcProcessor.getConnectionid()).getSapphireConnection();
        QueryProcessor queryProcessor = new QueryProcessor(sdcProcessor.getConnectionid());
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ").append(keycolid1).append(", ").append(keycolid2).append(OpalUtil.isNotEmpty(keycolid3) ? "," + keycolid3 : "").append(" From " + tableid).append(" WHERE ").append(primarykeyid1).append("= '").append(SafeSQL.encodeForSQL(primarykeyid1Value, con.isOracle())).append("' ");
        if (OpalUtil.isNotEmpty(primarykeyid3Value)) {
            sql.append(" AND ").append(primarykeyid3).append("= '").append(SafeSQL.encodeForSQL(primarykeyid3Value, con.isOracle())).append("'");
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
            if (ds != null && !ds.isEmpty()) {
                actualprimarykeyid2Value = ds.getValue(0, keycolid2, primarykeyid2Value);
            }
        }
        return actualprimarykeyid2Value;
    }

    public static void validateExpiryWithTableName(String sdcid, String linkSDC, String linkTable, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, StringBuffer message, SDCProcessor sdcProcessor) {
        HashMap linkKeyCols = new HashMap();
        ArrayList detailLinkProps = new ArrayList();
        HashMap errors = new HashMap();
        String keyid1 = keyCol.get("keyid1");
        String keyid2 = keyCol.get("keyid2");
        String keyid3 = keyCol.get("keyid3");
        String keyid1Values = keyColValues.get(keyid1);
        String keyid2Values = keyColValues.get(keyid2);
        String keyid3Values = keyColValues.get(keyid3);
        boolean linkexist = SDCExpiryValidationUtil.fetchLinkData(linkSDC, sdcid, linkTable, detailLinkProps, sdcProcessor);
        if (linkexist) {
            List<DataSet> linkds = SDCExpiryValidationUtil.getLinkTableDataSet(linkSDC, keyCol, keyColValues, detailLinkProps, sdcProcessor, true);
            if (linkds != null && !linkds.isEmpty()) {
                for (int k = 0; k < linkds.size(); ++k) {
                    if (linkds.get(k).size() <= 0) continue;
                    errors = SDCExpiryValidationUtil.getErrorCollection(linkSDC, linkds.get(k), sdcProcessor);
                    SDCExpiryValidationUtil.addToErrorMessage(linkSDC, message, errors);
                }
            }
        } else {
            PropertyList linksdcPropertyList = sdcProcessor.getPropertyList(linkSDC);
            Predicate<PropertyList> linksdccheck = e -> e.getProperty("sdcid").equalsIgnoreCase(linkSDC);
            Predicate<PropertyList> linkTablecheck = e -> e.getProperty("linktableid").equalsIgnoreCase(linkTable);
            PropertyListCollection detailLink = linksdcPropertyList.getCollection("links");
            boolean isLinked = detailLink.stream().anyMatch(linksdccheck.and(linkTablecheck));
            if (isLinked) {
                detailLink.stream().filter(linksdccheck.and(linkTablecheck)).collect(Collectors.toList());
                PropertyList detaillinkProps = (PropertyList)((ArrayList)detailLink.stream().filter(linksdccheck.and(linkTablecheck)).collect(Collectors.toList())).get(0);
                String linkkeyid1 = detaillinkProps.getProperty("keycolid1");
                String linkkeyid2 = detaillinkProps.getProperty("keycolid2");
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT ").append(linkkeyid1).append(OpalUtil.isNotEmpty(linkkeyid2) ? "," + linkkeyid2 : "").append(" From " + linkTable).append(" WHERE ");
                QueryProcessor queryProcessor = new QueryProcessor(sdcProcessor.getConnectionid());
                StringBuffer sqlwhere = new StringBuffer();
                sqlwhere.append(keyid1).append("= '").append(keyid1Values).append("'");
                sqlwhere.append(" AND ").append(keyid2).append("= '").append(keyid2Values).append("'");
                if (OpalUtil.isNotEmpty(keyid3Values)) {
                    sqlwhere.append(" AND ").append(keyid3).append("= '").append(keyid3Values).append("'");
                }
                sql.append(sqlwhere);
                DataSet linkedDs = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
                if (linkedDs.size() > 0 && linkedDs != null) {
                    errors = SDCExpiryValidationUtil.getErrorCollection(linkSDC, linkedDs, sdcProcessor);
                    SDCExpiryValidationUtil.addToErrorMessage(linkSDC, message, errors);
                }
            }
        }
    }

    private static List<DataSet> getLinkTableDataSet(String linksdcid, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, ArrayList detailLinkProps, SDCProcessor sdcProcessor, boolean existCheck) {
        HashMap errorcol = new HashMap();
        ArrayList<DataSet> dsList = new ArrayList<DataSet>();
        if (detailLinkProps.size() > 0) {
            for (int i = 0; i < detailLinkProps.size(); ++i) {
                HashMap detailLinkData = (HashMap)detailLinkProps.get(i);
                String linksdc = detailLinkData.get("linksdcid").toString();
                String linktableid = detailLinkData.get("linktableid") != null ? detailLinkData.get("linktableid").toString() : "";
                String sdccolumnid = detailLinkData.get("sdccolumnid") != null ? detailLinkData.get("sdccolumnid").toString() : "";
                String sdccolumnid2 = detailLinkData.get("sdccolumnid2") != null ? detailLinkData.get("sdccolumnid2").toString() : "";
                String sdccolumnid3 = detailLinkData.get("sdccolumnid3") != null ? detailLinkData.get("sdccolumnid3").toString() : "";
                PropertyList sdcPropertyList = sdcProcessor.getPropertyList(linksdcid);
                String tableid = sdcPropertyList.getProperty("tableid");
                Integer keycolCount = Integer.parseInt(sdcPropertyList.getProperty("keycolumns"));
                String keycolid1 = sdcPropertyList.getProperty("keycolid1");
                String keycolid2 = sdcPropertyList.getProperty("keycolid2");
                String keycolid3 = sdcPropertyList.getProperty("keycolid3");
                String primarykeyid1 = keyCol.get("keyid1");
                String primarykeyid2 = keyCol.get("keyid2");
                String primarykeyid3 = keyCol.get("keyid3");
                String primarykeyid1Value = keyColValues.get(primarykeyid1);
                String primarykeyid2Value = keyColValues.get(primarykeyid2);
                String primarykeyid3Value = keyColValues.get(primarykeyid3);
                SapphireConnection con = new ConnectionProcessor(sdcProcessor.getConnectionid()).getSapphireConnection();
                QueryProcessor queryProcessor = new QueryProcessor(sdcProcessor.getConnectionid());
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT ").append(keycolid1).append(OpalUtil.isNotEmpty(keycolid2) ? "," + keycolid2 : "").append(OpalUtil.isNotEmpty(keycolid3) ? "," + keycolid3 : "").append(" From " + linktableid).append(" WHERE ").append(primarykeyid1).append("= '").append(SafeSQL.encodeForSQL(primarykeyid1Value, con.isOracle())).append("' AND ").append(primarykeyid2).append("= '").append(SafeSQL.encodeForSQL(primarykeyid2Value, con.isOracle())).append("'");
                if (!OpalUtil.isEmpty(primarykeyid3Value)) {
                    sql.append(" AND ").append(primarykeyid3).append("= '").append(SafeSQL.encodeForSQL(primarykeyid3Value, con.isOracle())).append("'");
                }
                DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
                dsList.add(ds);
            }
        }
        return dsList;
    }

    private static boolean fetchLinkData(String SDC2, String toCheckSDC, String linkTable, List linkDetailPropList, SDCProcessor sdcProcessor) {
        boolean linkfound = false;
        PropertyListCollection detailLink = sdcProcessor.getDetailLinks(SDC2);
        if (detailLink != null && detailLink.size() > 0) {
            for (int i = 0; i < detailLink.size(); ++i) {
                PropertyList detailProp = (PropertyList)detailLink.get(i);
                if (detailProp.getProperty("linksdcid") == null || detailProp.getProperty("linktableid") == null || detailProp.getProperty("sdccolumnid") == null || !detailProp.getProperty("linksdcid").equalsIgnoreCase(toCheckSDC) || !detailProp.getProperty("linktableid").equalsIgnoreCase(linkTable)) continue;
                linkfound = true;
                HashMap<String, String> linkDetailProps = new HashMap<String, String>();
                linkDetailProps.put("sdccolumnid", detailProp.getProperty("sdccolumnid"));
                linkDetailProps.put("sdccolumnid2", detailProp.getProperty("sdccolumnid2"));
                linkDetailProps.put("sdccolumnid3", detailProp.getProperty("sdccolumnid3"));
                linkDetailProps.put("linktableid", detailProp.getProperty("linktableid"));
                linkDetailProps.put("linkid", detailProp.getProperty("linkid"));
                linkDetailProps.put("linksdcid", detailProp.getProperty("linksdcid"));
                linkDetailPropList.add(linkDetailProps);
            }
        }
        return linkfound;
    }

    public static void validateExpiryWithRefTable(String sdcid, String linkSDC, String refSDC, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, StringBuffer message, SDCProcessor sdcProcessor) {
        HashMap<String, String> refDetailLinkProps;
        boolean isPrimaryLink;
        DataSet linkds;
        HashMap linkKeyCols = new HashMap();
        HashMap<String, String> detailLinkProps = new HashMap<String, String>();
        HashMap errors = new HashMap();
        String keyid1 = keyCol.get("keyid1");
        String keyid2 = keyCol.get("keyid2");
        String keyid3 = keyCol.get("keyid3");
        String keyid1Values = keyColValues.get(keyid1);
        String keyid2Values = keyColValues.get(keyid2);
        String keyid3Values = keyColValues.get(keyid3);
        boolean isReverseLink = SDCExpiryValidationUtil.checkLinkData(sdcid, linkSDC, detailLinkProps, sdcProcessor);
        if (isReverseLink && (linkds = SDCExpiryValidationUtil.getLinkData(linkSDC, keyCol, keyColValues, null, sdcProcessor)) != null && !linkds.isEmpty() && (isPrimaryLink = SDCExpiryValidationUtil.checkLinkData(refSDC, linkSDC, refDetailLinkProps = new HashMap<String, String>(), sdcProcessor))) {
            LinkedHashMap<String, String> refKeyCols = new LinkedHashMap<String, String>();
            refKeyCols.put("keyid1", refDetailLinkProps.get("sdccolumnid"));
            if (refDetailLinkProps.get("sdccolumnid2") != null) {
                refKeyCols.put("keyid2", refDetailLinkProps.get("sdccolumnid2"));
            }
            if (refDetailLinkProps.get("sdccolumnid3") != null) {
                refKeyCols.put("keyid2", refDetailLinkProps.get("sdccolumnid3"));
            }
            for (int k = 0; k < linkds.size(); ++k) {
                DataSet refLinkds;
                LinkedHashMap<String, String> refkeyColsValues = new LinkedHashMap<String, String>();
                refkeyColsValues.put(refDetailLinkProps.get("sdccolumnid"), linkds.getValue(k, refDetailLinkProps.get("sdccolumnid")));
                if (refDetailLinkProps.get("sdccolumnid2") != null) {
                    refkeyColsValues.put(refDetailLinkProps.get("sdccolumnid2"), linkds.getValue(k, refDetailLinkProps.get("sdccolumnid2")));
                }
                if (refDetailLinkProps.get("sdccolumnid3") != null) {
                    refkeyColsValues.put(refDetailLinkProps.get("sdccolumnid3"), linkds.getValue(k, refDetailLinkProps.get("sdccolumnid3")));
                }
                if ((refLinkds = SDCExpiryValidationUtil.getRefLinkData(refSDC, refKeyCols, refkeyColsValues, "E", sdcProcessor)).size() <= 0) continue;
                errors = SDCExpiryValidationUtil.getErrorCollection(linkSDC, linkds, sdcProcessor);
                SDCExpiryValidationUtil.addToErrorMessage(linkSDC, message, errors);
            }
        }
    }

    private static DataSet getRefLinkData(String linkSDC, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, String versionstatus, SDCProcessor sdcProcessor) {
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(linkSDC);
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        String primarykeyid1 = keyCol.get("keyid1");
        String primarykeyid2 = keyCol.get("keyid2");
        String primarykeyid3 = keyCol.get("keyid3");
        String primarykeyid1Value = keyColValues.get(primarykeyid1);
        String primarykeyid2Value = keyColValues.get(primarykeyid2);
        String primarykeyid3Value = keyColValues.get(primarykeyid3);
        SapphireConnection con = new ConnectionProcessor(sdcProcessor.getConnectionid()).getSapphireConnection();
        StringBuffer sqlwhere = new StringBuffer();
        sqlwhere.append(keycolid1).append("= '").append(SafeSQL.encodeForSQL(primarykeyid1Value, con.isOracle())).append("'");
        if (OpalUtil.isNotEmpty(primarykeyid2Value)) {
            sqlwhere.append(" AND ").append(keycolid2).append("= '").append(SafeSQL.encodeForSQL(primarykeyid2Value, con.isOracle())).append("'");
        }
        if (OpalUtil.isNotEmpty(primarykeyid3Value)) {
            sqlwhere.append(" AND ").append(keycolid3).append("= '").append(SafeSQL.encodeForSQL(primarykeyid3Value, con.isOracle())).append("'");
        }
        if (OpalUtil.isNotEmpty(versionstatus)) {
            sqlwhere.append(" AND VERSIONSTATUS <> '").append(SafeSQL.encodeForSQL(versionstatus, con.isOracle())).append("'");
        }
        DataSet linkedDs = SDCExpiryValidationUtil.fetchLinkedSDI(linkSDC, "", "", "", "primary", tableid, sqlwhere.toString(), sdcProcessor);
        return linkedDs;
    }

    private static DataSet getLinkData(String linkSDC, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, String versionstatus, SDCProcessor sdcProcessor) {
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(linkSDC);
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        String primarykeyid1 = keyCol.get("keyid1");
        String primarykeyid2 = keyCol.get("keyid2");
        String primarykeyid3 = keyCol.get("keyid3");
        String primarykeyid1Value = keyColValues.get(primarykeyid1);
        String primarykeyid2Value = keyColValues.get(primarykeyid2);
        String primarykeyid3Value = keyColValues.get(primarykeyid3);
        SapphireConnection con = new ConnectionProcessor(sdcProcessor.getConnectionid()).getSapphireConnection();
        StringBuffer sqlwhere = new StringBuffer();
        sqlwhere.append(primarykeyid1).append("= '").append(SafeSQL.encodeForSQL(primarykeyid1Value, con.isOracle())).append("'");
        if (OpalUtil.isNotEmpty(primarykeyid2Value)) {
            sqlwhere.append(" AND ").append(primarykeyid2).append("= '").append(SafeSQL.encodeForSQL(primarykeyid2Value, con.isOracle())).append("'");
        }
        if (OpalUtil.isNotEmpty(primarykeyid3Value)) {
            sqlwhere.append(" AND ").append(primarykeyid3).append("= '").append(SafeSQL.encodeForSQL(primarykeyid3Value, con.isOracle())).append("'");
        }
        if (OpalUtil.isNotEmpty(versionstatus)) {
            sqlwhere.append(" AND VERSIONSTATUS <> '").append(SafeSQL.encodeForSQL(versionstatus, con.isOracle())).append("'");
        }
        DataSet linkedDs = SDCExpiryValidationUtil.fetchLinkedSDI(linkSDC, "", "", "", "primary", tableid, sqlwhere.toString(), sdcProcessor);
        return linkedDs;
    }

    private static HashMap validateExpiryWithFkDetailLinkData(String sdcid, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, HashMap detailLinkData, SDCProcessor sdcProcessor) {
        HashMap<String, ArrayList> errorcol = new HashMap<String, ArrayList>();
        String linksdcid = detailLinkData.get("linksdcid").toString();
        String linktableid = detailLinkData.get("linktableid") != null ? detailLinkData.get("linktableid").toString() : "";
        String sdccolumnid = detailLinkData.get("sdccolumnid") != null ? detailLinkData.get("sdccolumnid").toString() : "";
        String sdccolumnid2 = detailLinkData.get("sdccolumnid2") != null ? detailLinkData.get("sdccolumnid2").toString() : "";
        String sdccolumnid3 = detailLinkData.get("sdccolumnid3") != null ? detailLinkData.get("sdccolumnid3").toString() : "";
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(sdcid);
        String tableid = sdcPropertyList.getProperty("tableid");
        Integer keycolCount = Integer.parseInt(sdcPropertyList.getProperty("keycolumns"));
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        String primarykeyid1 = keyCol.get("keyid1");
        String primarykeyid2 = keyCol.get("keyid2");
        String primarykeyid3 = keyCol.get("keyid3");
        String primarykeyid1Value = keyColValues.get(primarykeyid1);
        String primarykeyid2Value = keyColValues.get(primarykeyid2);
        String primarykeyid3Value = keyColValues.get(primarykeyid3);
        SapphireConnection con = new ConnectionProcessor(sdcProcessor.getConnectionid()).getSapphireConnection();
        QueryProcessor queryProcessor = new QueryProcessor(sdcProcessor.getConnectionid());
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ").append(keycolid1).append(OpalUtil.isNotEmpty(keycolid2) ? "," + keycolid2 : "").append(OpalUtil.isNotEmpty(keycolid3) ? "," + keycolid3 : "").append(" From " + linktableid).append(" WHERE ").append(primarykeyid1).append("= '").append(SafeSQL.encodeForSQL(primarykeyid1Value, con.isOracle())).append("' AND ").append(primarykeyid2).append("= '").append(SafeSQL.encodeForSQL(primarykeyid2Value, con.isOracle())).append("' AND ").append(primarykeyid3).append("= '").append(SafeSQL.encodeForSQL(primarykeyid3Value, con.isOracle())).append("'");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
        if (ds != null && !ds.isEmpty()) {
            for (int l = 0; l < ds.size(); ++l) {
                String queryWhere = "VERSIONSTATUS NOT IN ('E','H')";
                DataSet linkedDs = SDCExpiryValidationUtil.fetchLinkedSDI(sdcid, ds.getValue(l, keycolid1), ds.getValue(l, keycolid2), ds.getValue(l, keycolid3), "primary", null, queryWhere, sdcProcessor);
                if (linkedDs == null) continue;
                ArrayList error = SDCExpiryValidationUtil.getErrorMsg(keycolid1, keycolid2, keycolid3, linkedDs);
                errorcol.put(sdcid, error);
            }
        }
        return errorcol;
    }

    private static DataSet fetchLinkedSDI(String sdcId, String keyid1, String keyid2, String keyid3, String requestItem, String queryFrom, String querywhere, SDCProcessor sdcProcessor) {
        SDIRequest sdireq = new SDIRequest();
        sdireq.setSDCid(sdcId);
        sdireq.setRequestItem(requestItem);
        if (keyid1 != null) {
            sdireq.setKeyid1List(keyid1);
        }
        if (keyid2 != null) {
            sdireq.setKeyid2List(keyid2);
        }
        if (keyid3 != null) {
            sdireq.setKeyid3List(keyid3);
        }
        if (queryFrom != null) {
            sdireq.setQueryFrom(queryFrom);
        }
        if (querywhere != null) {
            sdireq.setQueryWhere(querywhere);
        }
        SDIProcessor sdiProcessor = new SDIProcessor(sdcProcessor.getConnectionid());
        DataSet ds = sdiProcessor.getSDIData(sdireq).getDataset(requestItem);
        return ds;
    }

    private static boolean checkDetailLinkData(String SDC2, String toCheckSDC, HashMap<String, String> linkDetailProps, SDCProcessor sdcProcessor) {
        boolean linkfound = false;
        PropertyListCollection detailLink = sdcProcessor.getDetailLinks(SDC2);
        PropertyList detailProp = detailLink.find("linksdcid", toCheckSDC);
        if (detailProp != null && detailProp.size() > 0 && detailProp.getProperty("linksdcid") != null && detailProp.getProperty("linktableid") != null && detailProp.getProperty("sdccolumnid") != null) {
            linkfound = true;
            linkDetailProps.put("sdccolumnid", detailProp.getProperty("sdccolumnid"));
            linkDetailProps.put("sdccolumnid2", detailProp.getProperty("sdccolumnid2"));
            linkDetailProps.put("sdccolumnid3", detailProp.getProperty("sdccolumnid3"));
            linkDetailProps.put("linktableid", detailProp.getProperty("linktableid"));
            linkDetailProps.put("linkid", detailProp.getProperty("linkid"));
            linkDetailProps.put("linksdcid", detailProp.getProperty("linksdcid"));
        }
        return linkfound;
    }

    private static boolean checkLinkData(String SDC2, String toCheckSDC, HashMap<String, String> keycols, SDCProcessor sdcProcessor) {
        boolean linkfound = false;
        DataSet dt = sdcProcessor.getLinksData(toCheckSDC);
        if (dt.size() > 0) {
            for (int k = 0; k < dt.size(); ++k) {
                HashMap link = (HashMap)dt.get(k);
                if (link.get("linksdcid") == null || !((String)link.get("linksdcid")).equalsIgnoreCase(SDC2) || link.get("linktype") == null || !((String)link.get("linktype")).equalsIgnoreCase("F")) continue;
                linkfound = true;
                if (link.get("sdccolumnid") != null) {
                    keycols.put("sdccolumnid", (String)link.get("sdccolumnid"));
                }
                if (link.get("sdccolumnid2") != null) {
                    keycols.put("sdccolumnid2", (String)link.get("sdccolumnid2"));
                }
                if (link.get("sdccolumnid3") != null) {
                    keycols.put("sdccolumnid3", (String)link.get("sdccolumnid3"));
                }
                if (link.get("linktableid") != null) {
                    keycols.put("linktableid", (String)link.get("linktableid"));
                }
                if (link.get("linkid") != null) {
                    keycols.put("linkid", (String)link.get("linkid"));
                }
                if (link.get("linksdcid") == null) break;
                keycols.put("linksdcid", (String)link.get("sdccolumnid3"));
                break;
            }
        }
        return linkfound;
    }

    public static void validateSamplingPlanReference(String sdcid, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, StringBuffer message, SDCProcessor sdcProcessor) {
        HashMap errors = new HashMap();
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(sdcid);
        String tableid = sdcPropertyList.getProperty("tableid");
        Integer keycolCount = Integer.parseInt(sdcPropertyList.getProperty("keycolumns"));
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        String primarykeyid1 = keyCol.get("keyid1");
        String primarykeyid2 = keyCol.get("keyid2");
        String primarykeyid3 = keyCol.get("keyid3");
        String primarykeyid1Value = keyColValues.get(primarykeyid1);
        String primarykeyid2Value = keyColValues.get(primarykeyid2);
        String primarykeyid3Value = keyColValues.get(primarykeyid3);
        SapphireConnection con = new ConnectionProcessor(sdcProcessor.getConnectionid()).getSapphireConnection();
        StringBuffer sqlQueryFrom = new StringBuffer();
        sqlQueryFrom.append("s_samplingplan,s_spitem");
        StringBuffer sqlwhere = new StringBuffer();
        sqlwhere.append("s_spitem.ITEMSDCID").append("= '").append(SafeSQL.encodeForSQL(sdcid, con.isOracle())).append("'");
        sqlwhere.append(" AND s_spitem.ITEMKEYID1").append("= '").append(SafeSQL.encodeForSQL(primarykeyid1Value, con.isOracle())).append("'");
        sqlwhere.append(" AND s_spitem.ITEMKEYID2").append("= '").append(SafeSQL.encodeForSQL(primarykeyid2Value, con.isOracle())).append("'");
        sqlwhere.append(" AND s_spitem.S_SAMPLINGPLANID").append("= ").append("s_samplingplan.S_SAMPLINGPLANID");
        sqlwhere.append(" AND s_spitem.S_SAMPLINGPLANVERSIONID").append("= ").append("s_samplingplan.S_SAMPLINGPLANVERSIONID");
        sqlwhere.append(" AND s_samplingplan.VERSIONSTATUS <> '").append(SafeSQL.encodeForSQL("E", con.isOracle())).append("'");
        DataSet linkedDs = SDCExpiryValidationUtil.fetchLinkedSDI("LV_SamplingPlan", "", "", "", "primary", sqlQueryFrom.toString(), sqlwhere.toString(), sdcProcessor);
        if (linkedDs.size() > 0 && linkedDs != null) {
            errors = SDCExpiryValidationUtil.getErrorCollection("LV_SamplingPlan", linkedDs, sdcProcessor);
            SDCExpiryValidationUtil.addToErrorMessage("LV_SamplingPlan", message, errors);
        }
    }

    public static void validateRequestTemplateReference(String sdcid, HashMap<String, String> keyCol, HashMap<String, String> keyColValues, StringBuffer message, SDCProcessor sdcProcessor) {
        String refsdcid = new String();
        switch (sdcid) {
            case "SpecSDC": {
                refsdcid = "SDISpec";
                break;
            }
            case "WorkItem": {
                refsdcid = "SDIWorkItem";
            }
        }
        HashMap errors = new HashMap();
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(sdcid);
        String tableid = sdcPropertyList.getProperty("tableid");
        Integer keycolCount = Integer.parseInt(sdcPropertyList.getProperty("keycolumns"));
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        String primarykeyid1 = keyCol.get("keyid1");
        String primarykeyid2 = keyCol.get("keyid2");
        String primarykeyid3 = keyCol.get("keyid3");
        String primarykeyid1Value = keyColValues.get(primarykeyid1);
        String primarykeyid2Value = keyColValues.get(primarykeyid2);
        String primarykeyid3Value = keyColValues.get(primarykeyid3);
        PropertyList refSdcPropertyList = sdcProcessor.getPropertyList(refsdcid);
        String refTableid = refSdcPropertyList.getProperty("tableid");
        SapphireConnection con = new ConnectionProcessor(sdcProcessor.getConnectionid()).getSapphireConnection();
        StringBuffer sqlQueryFrom = new StringBuffer();
        sqlQueryFrom.append(refTableid);
        StringBuffer sqlwhere = new StringBuffer();
        sqlwhere.append("SDCID").append("= '").append("LV_RequestItem").append("' ");
        if (primarykeyid1Value != null && !OpalUtil.isEmpty(primarykeyid1Value)) {
            sqlwhere.append("AND ").append(primarykeyid1).append("= '").append(SafeSQL.encodeForSQL(primarykeyid1Value, con.isOracle())).append("' ");
        }
        if (primarykeyid2Value != null && !OpalUtil.isEmpty(primarykeyid2Value)) {
            sqlwhere.append("AND ").append(primarykeyid2).append("= '").append(SafeSQL.encodeForSQL(primarykeyid2Value, con.isOracle())).append("' ");
        }
        if (primarykeyid3Value != null && !OpalUtil.isEmpty(primarykeyid3Value)) {
            sqlwhere.append("AND ").append(primarykeyid3).append("= '").append(SafeSQL.encodeForSQL(primarykeyid3Value, con.isOracle())).append("' ");
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ").append(" keyid1 ").append(" From " + sqlQueryFrom).append(" WHERE ").append(sqlwhere.toString());
        QueryProcessor queryProcessor = new QueryProcessor(sdcProcessor.getConnectionid());
        DataSet requestItemds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
        if (requestItemds.size() > 0) {
            sqlQueryFrom = new StringBuffer();
            sqlQueryFrom.append("s_requestitem");
            for (int k = 0; k < requestItemds.size(); ++k) {
                if (requestItemds.getString(k, "keyid1", "").equalsIgnoreCase("")) continue;
                sqlwhere = new StringBuffer();
                sqlwhere.append("s_requestitemid").append("= '").append(SafeSQL.encodeForSQL(requestItemds.getString(k, "keyid1", ""), con.isOracle())).append("'");
                DataSet requestItemsDS = SDCExpiryValidationUtil.fetchLinkedSDI("LV_RequestItem", "", "", "", "primary", sqlQueryFrom.toString(), sqlwhere.toString(), sdcProcessor);
                if (requestItemsDS.size() <= 0) continue;
                String requestId = requestItemsDS.getString(0, "requestid", "");
                String sqlWhereRequest = "S_REQUESTID = '" + requestId + "' AND TEMPLATEFLAG= 'Y' ";
                sql = new StringBuffer();
                sql.append("SELECT ").append(" * ").append(" From s_request").append(" WHERE ").append(sqlWhereRequest.toString());
                DataSet requestDS = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[0]);
                if (requestDS == null) continue;
                errors = SDCExpiryValidationUtil.getErrorCollection("Request", requestDS, sdcProcessor);
                SDCExpiryValidationUtil.addToErrorMessage("Request", message, errors);
            }
        }
    }

    private static HashMap getErrorCollection(String linkSDC, DataSet linkedDs, SDCProcessor sdcProcessor) {
        HashMap<String, ArrayList> errorcol = new HashMap<String, ArrayList>();
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(linkSDC);
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        ArrayList error = SDCExpiryValidationUtil.getErrorMsg(keycolid1, keycolid2, keycolid3, linkedDs);
        errorcol.put(linkSDC, error);
        return errorcol;
    }

    private static void addToErrorMessage(String linkSDC, StringBuffer message, HashMap errors) {
        if (errors.size() > 0 && errors.get(linkSDC) != null) {
            if (message.length() > 0) {
                message.append(", ");
            }
            message.append("<br>").append(linkSDC).append("(s)").append("<br>");
            ArrayList errorItems = (ArrayList)errors.get(linkSDC);
            Iterator i = errorItems.iterator();
            while (i.hasNext()) {
                message.append(i.next()).append("<br>");
            }
        }
    }

    private static ArrayList getErrorMsg(String sdccolumnid, String sdccolumnid2, String sdccolumnid3, DataSet linkedDs) {
        ArrayList<String> error = new ArrayList<String>();
        for (int k = 0; k < linkedDs.size(); ++k) {
            StringBuffer errMsg = new StringBuffer();
            errMsg.append(linkedDs.getString(k, sdccolumnid));
            if (linkedDs.getString(k, sdccolumnid2) != null) {
                errMsg.append("(").append(linkedDs.getString(k, sdccolumnid2)).append(")");
            }
            if (linkedDs.getString(k, sdccolumnid3) != null) {
                errMsg.append("(").append(linkedDs.getString(k, sdccolumnid3)).append(")");
            }
            if (linkedDs.getString(k, "VERSIONSTATUS") != null) {
                String versionStatus;
                errMsg.append(" || Status : ");
                switch (versionStatus = linkedDs.getString(k, "VERSIONSTATUS")) {
                    case "C": {
                        errMsg.append("Current");
                        break;
                    }
                    case "A": {
                        errMsg.append("Active");
                        break;
                    }
                    case "P": {
                        errMsg.append("Provisional");
                        break;
                    }
                    default: {
                        errMsg.append("(null)");
                    }
                }
            }
            error.add(errMsg.toString());
        }
        return error;
    }
}


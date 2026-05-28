/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import java.util.ArrayList;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RedoCalculations
extends BaseSDIDataEntryAction
implements sapphire.action.RedoCalculations {
    String redoCalcSDIs = "";
    private boolean isfireSyncActions = true;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String rsetid;
        String sdcid = properties.getProperty("sdcid");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        String auditreason = properties.getProperty("auditreason");
        String auditactivity = properties.getProperty("auditactivity");
        String auditsignedflag = properties.getProperty("auditsignedflag");
        String tracelogid = properties.getProperty("tracelogid").trim();
        this.redoCalcSDIs = properties.getProperty("REDOCALC_SDIS");
        this.isfireSyncActions = properties.getProperty("_fireSyncActions", "Y").equalsIgnoreCase("Y");
        String linktype = properties.getProperty("reverselinktype", "current");
        if (linktype.equals("current")) {
            this.redoThese(sdcid, keyid1, keyid2, keyid3, auditreason, auditsignedflag, auditactivity, tracelogid, properties);
        } else if (linktype.equals("reversefk") || linktype.equals("reversefksdiworkitem") || linktype.equals("reversefksdidata") || linktype.equals("reversefksdidataitem")) {
            String rsetid2;
            String reversesdcid = properties.getProperty("reversefksdcid");
            String fkcolumnorlinkid = properties.getProperty("fkcolumnorlinkid");
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            if (reversesdcid.length() > 0 && (rsetid2 = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false)) != null && rsetid2.length() > 0) {
                PropertyList reverseSdcProps;
                String reverseTableid = "";
                String reverseKeycolid1 = "";
                String reverseKeycolid2 = "";
                String reverseKeycolid3 = "";
                if (linktype.equals("reversefksdiworkitem")) {
                    reversesdcid = "SDIWorkItem";
                    reverseTableid = "sdiworkitem";
                    reverseKeycolid1 = "keyid1";
                    reverseKeycolid2 = "keyid2";
                    reverseKeycolid3 = "keyid3";
                    reverseSdcProps = sdcProcessor.getPropertyList(reversesdcid);
                    reversesdcid = properties.getProperty("reversefksdcid");
                } else if (linktype.equals("reversefksdidata")) {
                    reversesdcid = "DataSet";
                    reverseTableid = "sdidata";
                    reverseKeycolid1 = "keyid1";
                    reverseKeycolid2 = "keyid2";
                    reverseKeycolid3 = "keyid3";
                    reverseSdcProps = sdcProcessor.getPropertyList(reversesdcid);
                    reversesdcid = properties.getProperty("reversefksdcid");
                } else if (linktype.equals("reversefksdidataitem")) {
                    reversesdcid = "DataItem";
                    reverseTableid = "sdidataitem";
                    reverseKeycolid1 = "keyid1";
                    reverseKeycolid2 = "keyid2";
                    reverseKeycolid3 = "keyid3";
                    reverseSdcProps = sdcProcessor.getPropertyList(reversesdcid);
                    reversesdcid = properties.getProperty("reversefksdcid");
                } else {
                    reverseSdcProps = sdcProcessor.getPropertyList(reversesdcid);
                    reverseTableid = reverseSdcProps.getProperty("tableid");
                    reverseKeycolid1 = reverseSdcProps.getProperty("keycolid1");
                    reverseKeycolid2 = reverseSdcProps.getProperty("keycolid2");
                    reverseKeycolid3 = reverseSdcProps.getProperty("keycolid3");
                }
                PropertyListCollection links = reverseSdcProps.getCollectionNotNull("links");
                PropertyList link = new PropertyList();
                if (fkcolumnorlinkid.length() > 0) {
                    link = links.find("sdccolumnid", fkcolumnorlinkid);
                    if (link == null) {
                        link = links.find("linkid", fkcolumnorlinkid);
                    }
                } else {
                    Iterator iterator = links.iterator();
                    while (iterator.hasNext() && !(link = (PropertyList)iterator.next()).getProperty("linksdcid").equals(sdcid)) {
                    }
                }
                if (link != null && link.size() > 0) {
                    String linkkeycolid1 = link.getProperty("sdccolumnid");
                    String linkkeycolid2 = link.getProperty("sdccolumnid2");
                    String linkkeycolid3 = link.getProperty("sdccolumnid3");
                    String sql = "SELECT " + reverseTableid + "." + reverseKeycolid1 + (reverseKeycolid2.length() > 0 ? "," + reverseTableid + "." + reverseKeycolid2 : "") + (reverseKeycolid3.length() > 0 ? "," + reverseTableid + "." + reverseKeycolid3 : "") + " FROM " + reverseTableid + ", rsetitems  WHERE rsetitems.rsetid = ?  AND rsetitems.sdcid = ?  AND " + reverseTableid + "." + linkkeycolid1 + "= rsetitems.keyid1" + (linkkeycolid2.length() > 0 ? " AND " + reverseTableid + "." + linkkeycolid2 + "= rsetitems.keyid2" : "") + (linkkeycolid3.length() > 0 ? " AND " + reverseTableid + "." + linkkeycolid3 + "= rsetitems.keyid3" : "");
                    this.database.createPreparedResultSet(sql, new Object[]{rsetid2, sdcid});
                    DataSet results = new DataSet();
                    results.setResultSet(this.database.getResultSet());
                    if (results.size() > 0) {
                        this.redoThese(reversesdcid, this.validateKeyColValue(results.getColumnValues(reverseKeycolid1, ";")), this.validateKeyColValue(results.getColumnValues(reverseKeycolid2, ";")), this.validateKeyColValue(results.getColumnValues(reverseKeycolid3, ";")), auditreason, auditsignedflag, auditactivity, tracelogid, properties);
                    }
                    properties.setProperty("reversefklinkresolved", "Y");
                }
                this.getDAMProcessor().clearRSet(rsetid2);
                if (link == null && linktype.equals("reversefk")) {
                    String[] otherSDCLinks = new String[]{"reversefksdiworkitem", "reversefksdidata", "reversefksdidataitem"};
                    for (int k = 0; k < otherSDCLinks.length; ++k) {
                        properties.setProperty("reverselinktype", otherSDCLinks[k]);
                        this.processAction(properties);
                        if (!"Y".equals(properties.getProperty("reversefklinkresolved"))) {
                            continue;
                        }
                        break;
                    }
                }
            }
        } else if (linktype.equals("fk")) {
            String rsetid3;
            String fksdcid = properties.getProperty("fksdcid");
            String fkcolumnorlinkid = properties.getProperty("fkcolumnorlinkid");
            if (fksdcid.length() > 0 && (rsetid3 = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false)) != null && rsetid3.length() > 0) {
                PropertyList fkSdcProps = this.getSDCProcessor().getPropertyList(fksdcid);
                String fkTableid = fkSdcProps.getProperty("tableid");
                String fkKeycolid1 = fkSdcProps.getProperty("keycolid1");
                String fkKeycolid2 = fkSdcProps.getProperty("keycolid2");
                String fkKeycolid3 = fkSdcProps.getProperty("keycolid3");
                PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcid);
                String tableid = sdcProps.getProperty("tableid");
                String keycolid1 = sdcProps.getProperty("keycolid1");
                String keycolid2 = sdcProps.getProperty("keycolid2");
                String keycolid3 = sdcProps.getProperty("keycolid3");
                PropertyListCollection links = sdcProps.getCollectionNotNull("links");
                PropertyList link = new PropertyList();
                if (fkcolumnorlinkid.length() > 0) {
                    link = links.find("sdccolumnid", fkcolumnorlinkid);
                    if (link == null) {
                        link = links.find("linkid", fkcolumnorlinkid);
                    }
                } else {
                    Iterator iterator = links.iterator();
                    while (iterator.hasNext() && !(link = (PropertyList)iterator.next()).getProperty("linksdcid").equals(fksdcid)) {
                    }
                }
                if (link != null && link.size() > 0) {
                    String linkkeycolid1 = link.getProperty("sdccolumnid");
                    String linkkeycolid2 = link.getProperty("sdccolumnid2");
                    String linkkeycolid3 = link.getProperty("sdccolumnid3");
                    String sql = "SELECT r." + fkKeycolid1 + (fkKeycolid2.length() > 0 ? ", r." + fkKeycolid2 : "") + (fkKeycolid3.length() > 0 ? ", r." + fkKeycolid3 : "") + " FROM " + fkTableid + " r, rsetitems, " + tableid + " s WHERE rsetitems.rsetid = ?  AND rsetitems.sdcid = ?  AND s." + keycolid1 + " = rsetitems.keyid1 " + (keycolid2.length() > 0 ? " AND s." + keycolid2 + " = rsetitems.keyid2" : "") + (keycolid3.length() > 0 ? " AND s." + keycolid3 + " = rsetitems.keyid3" : "") + " AND r." + fkKeycolid1 + " = s." + linkkeycolid1 + (fkKeycolid2.length() > 0 ? " AND r." + fkKeycolid2 + " = s." + linkkeycolid2 : "") + (fkKeycolid3.length() > 0 ? " AND r." + fkKeycolid3 + " = s." + linkkeycolid3 : "");
                    this.database.createPreparedResultSet(sql, new Object[]{rsetid3, sdcid});
                    DataSet results = new DataSet();
                    results.setResultSet(this.database.getResultSet());
                    if (results.size() > 0) {
                        this.redoThese(fksdcid, this.validateKeyColValue(results.getColumnValues(fkKeycolid1, ";")), this.validateKeyColValue(results.getColumnValues(fkKeycolid2, ";")), this.validateKeyColValue(results.getColumnValues(fkKeycolid3, ";")), auditreason, auditsignedflag, auditactivity, tracelogid, properties);
                    }
                }
                this.getDAMProcessor().clearRSet(rsetid3);
            }
        } else if (linktype.equals("reversesdidatarelation") || linktype.equals("reversesdiworkitemrelation")) {
            String reverseSdcid = properties.getProperty("reversesdcid", properties.getProperty("reversefksdcid"));
            if (reverseSdcid.length() > 0) {
                String rsetid4 = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false);
                int keyCount = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                if (rsetid4 != null && rsetid4.length() > 0) {
                    String relationType = properties.getProperty("relationtype", properties.getProperty("reverserelationtype"));
                    String sql = "";
                    if (linktype.equals("reversesdidatarelation")) {
                        sql = "SELECT DISTINCT dr.sdcid, dr.keyid1, dr.keyid2, dr.keyid3  FROM sdidatarelation dr, rsetitems  WHERE rsetitems.rsetid = ? AND dr.sdcid = ? AND rsetitems.sdcid = dr.tosdcid AND rsetitems.keyid1 = dr.tokeyid1";
                        if (keyCount > 1) {
                            sql = sql + " AND rsetitems.keyid2 = dr.tokeyid2";
                        }
                        if (keyCount > 2) {
                            sql = sql + " AND rsetitems.keyid3 = dr.tokeyid3";
                        }
                        sql = sql + (relationType.length() > 0 ? " AND dr.relationtype=?" : "");
                    } else if (linktype.equals("reversesdiworkitemrelation")) {
                        sql = "SELECT DISTINCT dr.sdcid, dr.keyid1, dr.keyid2, dr.keyid3  FROM sdiworkitemrelation dr, rsetitems  WHERE rsetitems.rsetid = ? AND dr.sdcid = ? AND rsetitems.sdcid = dr.tosdcid AND rsetitems.keyid1 = dr.tokeyid1";
                        if (keyCount > 1) {
                            sql = sql + " AND rsetitems.keyid2 = dr.tokeyid2";
                        }
                        if (keyCount > 2) {
                            sql = sql + " AND rsetitems.keyid3 = dr.tokeyid3";
                        }
                        sql = sql + (relationType.length() > 0 ? " AND dr.relationtype=?" : "");
                    }
                    ArrayList<String> params = new ArrayList<String>();
                    params.add(rsetid4);
                    params.add(reverseSdcid);
                    if (relationType.length() > 0) {
                        params.add(relationType);
                    }
                    this.database.createPreparedResultSet(sql, params.toArray());
                    DataSet results = new DataSet();
                    results.setResultSet(this.database.getResultSet());
                    if (results.size() > 0) {
                        this.redoThese(results.getString(0, "sdcid"), this.validateKeyColValue(results.getColumnValues("keyid1", ";")), this.validateKeyColValue(results.getColumnValues("keyid2", ";")), this.validateKeyColValue(results.getColumnValues("keyid3", ";")), auditreason, auditsignedflag, auditactivity, tracelogid, properties);
                    }
                    this.getDAMProcessor().clearRSet(rsetid4);
                }
            }
        } else if (linktype.equals("reversesdirelation") || linktype.equals("sdirelation")) {
            String reverseSdcid = properties.getProperty("reversesdcid", properties.getProperty("reversefksdcid"));
            if (reverseSdcid.length() > 0) {
                String rsetid5 = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false);
                int keyCount = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                if (rsetid5 != null && rsetid5.length() > 0) {
                    String relationType = properties.getProperty("relationtype", properties.getProperty("reverserelationtype"));
                    String redoSDI = linktype.equals("reversesdirelation") ? "from" : "to";
                    String providedSDI = linktype.equals("reversesdirelation") ? "to" : "from";
                    String sql = "SELECT DISTINCT sdir." + redoSDI + "sdcid, sdir." + redoSDI + "keyid1, sdir." + redoSDI + "keyid2, sdir." + redoSDI + "keyid3  FROM sdirelation sdir, rsetitems  WHERE rsetitems.rsetid = ? AND sdir." + redoSDI + "sdcid = ? AND rsetitems.sdcid = sdir." + providedSDI + "sdcid AND rsetitems.keyid1 = sdir." + providedSDI + "keyid1";
                    if (keyCount > 1) {
                        sql = sql + " AND rsetitems.keyid2 = sdir." + providedSDI + "keyid2";
                    }
                    if (keyCount > 2) {
                        sql = sql + " AND rsetitems.keyid3 = sdir." + providedSDI + "keyid3";
                    }
                    sql = sql + (relationType.length() > 0 ? " AND sdir.relationtype=?" : "");
                    ArrayList<String> params = new ArrayList<String>();
                    params.add(rsetid5);
                    params.add(reverseSdcid);
                    if (relationType.length() > 0) {
                        params.add(relationType);
                    }
                    this.database.createPreparedResultSet(sql, params.toArray());
                    DataSet results = new DataSet();
                    results.setResultSet(this.database.getResultSet());
                    if (results.size() > 0) {
                        this.redoThese(results.getString(0, redoSDI + "sdcid"), this.validateKeyColValue(results.getColumnValues(redoSDI + "keyid1", ";")), this.validateKeyColValue(results.getColumnValues(redoSDI + "keyid2", ";")), this.validateKeyColValue(results.getColumnValues(redoSDI + "keyid3", ";")), auditreason, auditsignedflag, auditactivity, tracelogid, properties);
                    }
                    this.getDAMProcessor().clearRSet(rsetid5);
                }
            }
        } else if (linktype.equals("reversechildsample")) {
            String rsetid6 = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false);
            if (rsetid6 != null && rsetid6.length() > 0) {
                String sql = "select sourcesampleid from s_samplemap, rsetitems WHERE rsetitems.rsetid = ? and rsetitems.sdcid = ? and s_samplemap.destsampleid = rsetitems.keyid1";
                this.database.createPreparedResultSet(sql, new Object[]{rsetid6, sdcid});
                DataSet results = new DataSet();
                results.setResultSet(this.database.getResultSet());
                if (results.size() > 0) {
                    this.redoThese("Sample", this.validateKeyColValue(results.getColumnValues("sourcesampleid", ";")), "", "", auditreason, auditsignedflag, auditactivity, tracelogid, properties);
                }
                this.getDAMProcessor().clearRSet(rsetid6);
            }
        } else if (linktype.equals("reverseparentsample")) {
            String rsetid7 = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false);
            if (rsetid7 != null && rsetid7.length() > 0) {
                String sql = "select destsampleid from s_samplemap, rsetitems WHERE rsetitems.rsetid = ? and rsetitems.sdcid = ? and s_samplemap.sourcesampleid = rsetitems.keyid1";
                this.database.createPreparedResultSet(sql, new Object[]{rsetid7, sdcid});
                DataSet results = new DataSet();
                results.setResultSet(this.database.getResultSet());
                if (results.size() > 0) {
                    this.redoThese("Sample", this.validateKeyColValue(results.getColumnValues("destsampleid", ";")), "", "", auditreason, auditsignedflag, auditactivity, tracelogid, properties);
                }
                this.getDAMProcessor().clearRSet(rsetid7);
            }
        } else if (linktype.equals("reverseancestorsample")) {
            String rsetid8 = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false);
            if (rsetid8 != null && rsetid8.length() > 0) {
                String sql = "";
                sql = this.database.isOracle() ? "SELECT destsampleid from s_samplemap, rsetitems WHERE rsetitems.rsetid = ? and rsetitems.sdcid = ? START WITH s_samplemap.sourcesampleid = rsetitems.keyid1 CONNECT BY PRIOR destsampleid = sourcesampleid" : "WITH n(sourcesampleid, destsampleid) AS (SELECT sourcesampleid, destsampleid FROM s_samplemap, rsetitems WHERE rsetitems.rsetid = ? and rsetitems.sdcid = ? and sourcesampleid = rsetitems.keyid1 UNION ALL SELECT np.sourcesampleid, np.destsampleid FROM s_samplemap as np, n WHERE n.destsampleid = np.sourcesampleid ) SELECT destsampleid FROM n ";
                this.database.createPreparedResultSet(sql, new Object[]{rsetid8, sdcid});
                DataSet results = new DataSet();
                results.setResultSet(this.database.getResultSet());
                if (results.size() > 0) {
                    this.redoThese("Sample", this.validateKeyColValue(results.getColumnValues("destsampleid", ";")), "", "", auditreason, auditsignedflag, auditactivity, tracelogid, properties);
                }
                this.getDAMProcessor().clearRSet(rsetid8);
            }
        } else if (linktype.equals("reversedescendantsample") && (rsetid = BaseSDIDataAction.createRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false)) != null && rsetid.length() > 0) {
            String sql = "";
            sql = this.database.isOracle() ? "SELECT sourcesampleid from s_samplemap, rsetitems WHERE rsetitems.rsetid = ? and rsetitems.sdcid = ? START WITH s_samplemap.destsampleid = rsetitems.keyid1 CONNECT BY PRIOR sourcesampleid = destsampleid" : "WITH n(sourcesampleid, destsampleid) AS (SELECT sourcesampleid, destsampleid FROM s_samplemap, rsetitems WHERE rsetitems.rsetid = ? and rsetitems.sdcid = ? and destsampleid = rsetitems.keyid1 UNION ALL SELECT np.sourcesampleid, np.destsampleid FROM s_samplemap as np, n WHERE n.sourcesampleid = np.destsampleid ) SELECT sourcesampleid FROM n ";
            this.database.createPreparedResultSet(sql, new Object[]{rsetid, sdcid});
            DataSet results = new DataSet();
            results.setResultSet(this.database.getResultSet());
            if (results.size() > 0) {
                this.redoThese("Sample", this.validateKeyColValue(results.getColumnValues("sourcesampleid", ";")), "", "", auditreason, auditsignedflag, auditactivity, tracelogid, properties);
            }
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    private void redoThese(String sdcid, String keyid1, String keyid2, String keyid3, String auditReason, String auditSignedFlag, String auditActivity, String traceLogId, PropertyList properties) throws SapphireException {
        PropertyList targetItems = new PropertyList();
        targetItems.setProperty("sdcid", sdcid);
        targetItems.setProperty("keyid1", keyid1);
        targetItems.setProperty("keyid2", keyid2);
        targetItems.setProperty("keyid3", keyid3);
        targetItems.setProperty("auditreason", auditReason);
        targetItems.setProperty("auditsignedflag", auditSignedFlag);
        targetItems.setProperty("auditactivity", auditActivity);
        targetItems.setProperty("tracelogid", traceLogId);
        targetItems.setProperty("REDOCALC_SDIS", this.redoCalcSDIs);
        targetItems.put("islivelimitchecking", properties.getProperty("islivelimitchecking"));
        targetItems.put("dataentrypage_allsdis", properties.get("dataentrypage_allsdis"));
        targetItems.put("livelimitcheckmode", properties.get("livelimitcheckmode"));
        targetItems.put("crosssdi_all_modifieddataitems", properties.get("crosssdi_all_modifieddataitems"));
        this.dataEntry(targetItems, false, false, true);
        if (targetItems.getProperty("updateddataitems").length() > 0) {
            properties.setProperty("updateddataitems", targetItems.getProperty("updateddataitems"));
        }
        if (properties.getProperty("islivelimitchecking").equalsIgnoreCase("Y")) {
            this.isfireSyncActions = false;
        }
        if (this.isfireSyncActions) {
            ActionProcessor ap = this.getActionProcessor();
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            props.setProperty("keyid2", keyid2);
            props.setProperty("keyid3", keyid3);
            props.setProperty("auditreason", auditReason);
            props.setProperty("auditsignedflag", auditSignedFlag);
            props.setProperty("auditactivity", auditActivity);
            props.setProperty("tracelogid", traceLogId);
            ap.processAction("UpdateDatasetStatus", "1", props);
            props.clear();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            props.setProperty("keyid2", keyid2);
            props.setProperty("keyid3", keyid3);
            props.setProperty("auditreason", auditReason);
            props.setProperty("auditsignedflag", auditSignedFlag);
            props.setProperty("auditactivity", auditActivity);
            props.setProperty("tracelogid", traceLogId);
            ap.processAction("SyncSDIWIStatus", "1", props);
            if ("Sample".equalsIgnoreCase(sdcid)) {
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", keyid1);
                props.setProperty("keyid2", keyid2);
                props.setProperty("keyid3", keyid3);
                props.setProperty("auditreason", auditReason);
                props.setProperty("auditsignedflag", auditSignedFlag);
                props.setProperty("auditactivity", auditActivity);
                props.setProperty("tracelogid", traceLogId);
                ap.processAction("SyncSDIDataSetStatus", "1", props);
            }
        }
    }

    private String validateKeyColValue(String value) {
        String retVal = value.trim().equals(StringUtil.repeat(";", value.length()).trim()) ? "" : value;
        return retVal;
    }
}


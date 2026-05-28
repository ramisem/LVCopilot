/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.LV_BatchStage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BatchLifeCycleUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 91326 $";
    public static final String STATE_INITIAL = "initial";
    public static final String STATE_RECEIVED = "received";
    public static final String STATE_ACTIVE = "active";
    public static final String STATE_PENDINGRELEASE = "pendingrelease";
    public static final String STATE_RELEASED = "released";
    public static final String STATE_REJECTED = "rejected";
    public static final String STATE_ONHOLD = "onhold";
    public static final String STATE_PRELIMINARYRELEASE = "preliminaryrelease";
    public static final String STATE_CANCELLED = "Cancelled";
    private static PropertyList statechart;

    public static boolean isStateTransitionValid(String fromState, String toState) {
        PropertyList state;
        PropertyListCollection next;
        PropertyListCollection states = statechart.getCollection("states");
        PropertyList stateParent = states.find("state", fromState);
        boolean found = false;
        if (stateParent != null && (next = (state = stateParent.getPropertyList("state")).getCollection("nextstate")).find("states", toState) != null) {
            found = true;
        }
        return found;
    }

    public static String getBatchStateId(String stateDisplayValue) {
        String stateid = null;
        ListIterator statesIter = statechart.getCollection("states").listIterator();
        block0: while (statesIter.hasNext()) {
            PropertyList pl = (PropertyList)statesIter.next();
            Collection state = pl.values();
            for (Object object : state) {
                PropertyList stateDetails;
                if (!(object instanceof PropertyList) || !(stateDetails = (PropertyList)object).getProperty("displayvalue", "").equals(stateDisplayValue)) continue;
                stateid = stateDetails.getProperty("stateid");
                continue block0;
            }
        }
        return stateid;
    }

    public static String getBatchStateDisplayValue(String stateid) {
        PropertyListCollection states = statechart.getCollection("states");
        PropertyList stateParent = states.find("state", stateid);
        String stateDisplayValue = null;
        if (stateParent != null) {
            PropertyList state = stateParent.getPropertyList("state");
            stateDisplayValue = state.getProperty("displayvalue");
        }
        return stateDisplayValue;
    }

    public static PropertyList getBatchStateDetails(String stateid) {
        PropertyListCollection states = statechart.getCollection("states");
        PropertyList stateParent = states.find("state", stateid);
        PropertyList state = null;
        if (stateParent != null) {
            state = stateParent.getPropertyList("state");
        }
        return state;
    }

    public static PropertyList getStatechart() {
        return statechart;
    }

    public static void setStatechart(PropertyList statechart) {
        BatchLifeCycleUtil.statechart = statechart;
    }

    public static boolean hasAllTestsFinished(String batchid, QueryProcessor qp) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT COUNT(*) samplesinbatch, ");
        sql.append(" SUM(CASE ");
        sql.append(" WHEN s.samplestatus = '").append("Completed").append("' AND (s.reviewrequiredflag='N' OR s.reviewrequiredflag IS NULL) THEN 1 ");
        sql.append(" WHEN s.disposalstatus='Retained' OR s.samplestatus IN ('").append("Reviewed").append("', '").append("Reported").append("', '").append(STATE_CANCELLED).append("', '").append("Disposed").append("') THEN 1");
        sql.append(" ELSE 0 ");
        sql.append(" END) NumberOfFinishedSampleInBatch ");
        sql.append(" FROM s_batch b, s_sample s ");
        sql.append(" WHERE b.s_batchid = ").append(safeSQL.addVar(batchid));
        sql.append(" AND s.batchid = b.s_batchid ");
        sql.append(" AND s.classification = '").append("QM").append("'");
        sql.append(" GROUP BY b.s_batchid");
        DataSet pendingRelease = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        int numberOfFinishedSampleInBatch = pendingRelease.getInt(0, "NumberOfFinishedSampleInBatch");
        int samplesInBatch = pendingRelease.getInt(0, "samplesinbatch");
        return numberOfFinishedSampleInBatch == samplesInBatch;
    }

    public static int isParentBatchesReleased(String batchid, QueryProcessor qp, PropertyList batchSamplePolicy) {
        DataSet dsReleaseFlag = qp.getPreparedSqlDataSet("getsampletypereleaserule", "select s.batchreleaserulesflag, b.batchmode   from s_sampletype s, s_batch b where s.s_sampletypeid = b.batchtype and b.s_batchid = ?", new String[]{batchid});
        String releaseFlag = "";
        String batchMode = "";
        if (dsReleaseFlag != null && dsReleaseFlag.getRowCount() > 0) {
            releaseFlag = dsReleaseFlag.getValue(0, "batchreleaserulesflag", "");
            batchMode = dsReleaseFlag.getValue(0, "batchmode", "");
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT 'nonreleasedparent', COUNT(bg.s_batchid) AS counter FROM s_batch b, s_batchgenealogy bg WHERE ");
        sql.append(" bg.s_batchid = ").append(safeSQL.addVar(batchid));
        sql.append(" AND b.s_batchid = bg.parentbatchid AND b.batchstatus != 'Released'");
        sql.append(" UNION ALL");
        sql.append(" SELECT 'nonassignedcount', COUNT(bg2.s_batchid) AS counter FROM s_batchgenealogy bg2 WHERE");
        sql.append(" bg2.s_batchid = ").append(safeSQL.addVar(batchid));
        sql.append(" AND (bg2.parentbatchid IS NULL OR bg2.parentbatchid = '')");
        DataSet nonReleasedParent = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (nonReleasedParent != null) {
            if (releaseFlag.trim().length() > 0 && batchMode.trim().equalsIgnoreCase("formulation")) {
                if ("R".equalsIgnoreCase(releaseFlag) && nonReleasedParent.getInt(0, "counter", 0) > 0) {
                    return 1;
                }
                if (!"S".equalsIgnoreCase(releaseFlag) && nonReleasedParent.getInt(1, "counter", 0) > 0) {
                    return 2;
                }
                return 0;
            }
            boolean isParentBatchReleaseCheckRequired = batchSamplePolicy.getProperty("releasebatch", "Only if ParentBatch is Released").equalsIgnoreCase("Only if ParentBatch is Released");
            if (isParentBatchReleaseCheckRequired && nonReleasedParent.getInt(0, "counter", 0) > 0) {
                return 1;
            }
            boolean isParentBatchNotRequired = batchSamplePolicy.getProperty("releasebatch", "Only if ParentBatch is Released").equalsIgnoreCase("Regardless if ParentBatch provided");
            if (!isParentBatchNotRequired && nonReleasedParent.getInt(1, "counter", 0) > 0) {
                return 2;
            }
        }
        return 0;
    }

    public static boolean isChildStagesReleased(String batchid, QueryProcessor qp, String syncBatchReleaseWithStage) {
        DataSet nonReleasedChildStages;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT bs.s_batchstageid FROM s_batchstage bs");
        sql.append(" WHERE bs.batchid = ").append(safeSQL.addVar(batchid));
        sql.append(" AND bs.batchstagestatus != '").append(LV_BatchStage.BATCHSTAGE_RELEASED).append("'");
        sql.append(" AND bs.batchstagestatus != '").append(LV_BatchStage.BATCHSTAGE_INITIAL).append("'");
        sql.append(" AND bs.batchstagestatus != '").append(LV_BatchStage.BATCHSTAGE_CANCELLED).append("'");
        if (syncBatchReleaseWithStage.equalsIgnoreCase("ARS")) {
            sql.append(" AND bs.batchstagestatus != '").append(LV_BatchStage.BATCHSTAGE_REJECTED).append("'");
        }
        if ((nonReleasedChildStages = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && nonReleasedChildStages.getRowCount() > 0) {
            return false;
        }
        safeSQL.reset();
        sql = new StringBuffer("SELECT s.s_sampleid FROM s_batchstage bs, s_sample s");
        sql.append(" WHERE bs.batchid = ").append(safeSQL.addVar(batchid));
        sql.append(" AND bs.batchstagestatus = 'Initial'");
        sql.append(" AND s.batchstageid = bs.s_batchstageid");
        DataSet samplesForInitialStages = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        return samplesForInitialStages == null || samplesForInitialStages.getRowCount() <= 0;
    }

    public static DataSet getReleaseableStages(String rsetId, QueryProcessor qp) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer str = new StringBuffer("SELECT s_batchstageid FROM");
        str.append(" (SELECT b.s_batchstageid, COUNT(*) samplesinbatchstage, ");
        str.append(" SUM(CASE");
        str.append(" WHEN s.samplestatus = '").append("Completed").append("' AND (s.reviewrequiredflag='N' OR s.reviewrequiredflag IS NULL) THEN 1");
        str.append(" WHEN s.disposalstatus = 'Retained' OR s.samplestatus IN ( '").append("Reviewed").append("', '").append("Reported").append("', '").append(STATE_CANCELLED).append("', '").append("Disposed").append("' ) THEN 1");
        str.append(" ELSE 0");
        str.append(" END) numfinishedsamples");
        str.append(" FROM s_sample s, s_batchstage b, rsetitems rs");
        str.append(" WHERE  rs.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND rs.keyid1 = b.s_batchstageid AND rs.sdcid='LV_BatchStage'");
        str.append(" AND s.batchstageid = b.s_batchstageid");
        str.append(" GROUP BY b.s_batchstageid) BATCHSTAGESAMPLES");
        str.append(" WHERE samplesinbatchstage = numfinishedsamples");
        DataSet pendingRelease = qp.getPreparedSqlDataSet(str.toString(), safeSQL.getValues());
        return pendingRelease;
    }

    public static boolean hasOpenIncidents(String batchid, QueryProcessor qp) throws SapphireException {
        boolean hasOpenIncidents = false;
        String numberOfOpenIncidentsInBatch = "SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3  FROM incidentitem ii, incident i  WHERE ii.sourcesdcid = 'Batch'  AND ii.sourcekeyid1 = ?  AND i.incidentid = ii.incidentid  AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed') ";
        DataSet ds = qp.getPreparedSqlDataSet(numberOfOpenIncidentsInBatch, (Object[])new String[]{batchid});
        if (ds.getRowCount() > 0) {
            hasOpenIncidents = true;
        } else {
            String numberOfOpenIncidentsInBatchStage = "SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3  FROM incidentitem ii, incident i  WHERE ii.sourcesdcid = 'LV_BatchStage'  AND ii.sourcekeyid1 in (select s_batchstageid from s_batchstage where batchid=?)  AND i.incidentid = ii.incidentid  AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed') ";
            DataSet openincidentsinbatchStageDs = qp.getPreparedSqlDataSet(numberOfOpenIncidentsInBatchStage, (Object[])new String[]{batchid});
            if (openincidentsinbatchStageDs.getRowCount() > 0) {
                hasOpenIncidents = true;
            } else {
                String numberOfOpenIncidentsInChildSamplesSql = "SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3  FROM incidentitem ii, incident i, s_sample s  WHERE s.batchid = ? AND ii.sourcesdcid = 'Sample' AND ii.sourcekeyid1 = s.s_sampleid AND i.incidentid = ii.incidentid AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed')";
                DataSet numberOfOpenIncidentsInChildSamples = qp.getPreparedSqlDataSet(numberOfOpenIncidentsInChildSamplesSql, (Object[])new String[]{batchid});
                if (numberOfOpenIncidentsInChildSamples.getRowCount() > 0) {
                    hasOpenIncidents = true;
                }
            }
        }
        return hasOpenIncidents;
    }

    public static boolean isToBeAutoReleased(String batchid, PropertyList policy, QueryProcessor qp, ConfigurationProcessor cp) throws SapphireException {
        String skipLevel = policy.getProperty("skiplevel", "");
        boolean returnval = false;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT COUNT(*) autoreleaseflag FROM s_product, s_batch WHERE ");
        sql.append(" s_batch.s_batchid = ").append(safeSQL.addVar(batchid));
        sql.append(" AND s_product.s_productid = s_batch.productid ");
        sql.append(" AND s_product.s_productversionid = s_batch.productversionid ");
        sql.append(" AND s_product.autoreleaseflag = 'Y' ");
        sql.append(" AND (s_batch.levelid IS NULL OR s_batch.levelid != ").append(safeSQL.addVar(skipLevel)).append(")");
        DataSet autoReleaseReqBatchDS = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (autoReleaseReqBatchDS != null && autoReleaseReqBatchDS.getInt(0, "autoreleaseflag") != 0) {
            PropertyListCollection specInterpretation = cp.getPolicy("DataEntryPolicy", "Sapphire Custom").getCollection("SpecConditions");
            Iterator iter = specInterpretation.iterator();
            String passCondition = "Pass";
            while (iter.hasNext()) {
                PropertyList condition = (PropertyList)iter.next();
                if (!condition.getProperty("interpretation").equals(passCondition)) continue;
                passCondition = condition.getProperty("SpecCond");
                break;
            }
            safeSQL.reset();
            sql = new StringBuffer("SELECT condition FROM sdispec WHERE sdcid = 'Sample' ");
            sql.append(" AND  keyid1 IN ( ");
            sql.append(" SELECT s_sampleid FROM s_sample WHERE batchid = " + safeSQL.addVar(batchid) + ")");
            DataSet specs = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("condition", passCondition);
            DataSet passedSpecs = specs.getFilteredDataSet(filter);
            if (specs.getRowCount() > 0 && specs.getRowCount() == passedSpecs.getRowCount()) {
                returnval = true;
            }
        }
        return returnval;
    }

    static {
        String statechartXml = "<propertylist id=\"statechart\">\n            <property id=\"states\" type=\"collection\">\n              <collection>\n                <propertylist id=\"initial\">\n                  <property id=\"state\" type=\"propertylist\">\n                    <propertylist id=\"initial\">\n                      <property id=\"stateid\" type=\"simple\"><![CDATA[initial]]></property>\n                      <property id=\"displayvalue\" type=\"simple\"><![CDATA[Initial]]></property>\n                      <property id=\"nextstate\" type=\"collection\">\n                        <collection>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[received]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[initial]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[rejected]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[onhold]]></property>\n                          </propertylist>\n                        </collection>\n                      </property>\n                    </propertylist>\n                  </property>\n                </propertylist>\n                <propertylist id=\"received\">\n                  <property id=\"state\" type=\"propertylist\">\n                    <propertylist id=\"received\">\n                      <property id=\"stateid\" type=\"simple\"><![CDATA[received]]></property>\n                      <property id=\"displayvalue\" type=\"simple\"><![CDATA[Received]]></property>\n                      <property id=\"nextstate\" type=\"collection\">\n                        <collection>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[active]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[rejected]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[onhold]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[initial]]></property>\n                          </propertylist>\n                        </collection>\n                      </property>\n                    </propertylist>\n                  </property>\n                </propertylist>\n                <propertylist id=\"active\">\n                  <property id=\"state\" type=\"propertylist\">\n                    <propertylist id=\"active\">\n                      <property id=\"stateid\" type=\"simple\"><![CDATA[active]]></property>\n                      <property id=\"displayvalue\" type=\"simple\"><![CDATA[Active]]></property>\n                      <property id=\"nextstate\" type=\"collection\">\n                        <collection>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[pendingrelease]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[rejected]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[onhold]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[preliminaryrelease]]></property>\n                          </propertylist>\n                        </collection>\n                      </property>\n                    </propertylist>\n                  </property>\n                </propertylist>\n                <propertylist id=\"pendingrelease\">\n                  <property id=\"state\" type=\"propertylist\">\n                    <propertylist id=\"pendingrelease\">\n                      <property id=\"stateid\" type=\"simple\"><![CDATA[pendingrelease]]></property>\n                      <property id=\"displayvalue\" type=\"simple\"><![CDATA[Pending Release]]></property>\n                      <property id=\"nextstate\" type=\"collection\">\n                        <collection>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[released]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[rejected]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[preliminaryrelease]]></property>\n                          </propertylist>\n                        </collection>\n                      </property>\n                    </propertylist>\n                  </property>\n                </propertylist>\n                <propertylist id=\"released\">\n                  <property id=\"state\" type=\"propertylist\">\n                    <propertylist id=\"released\">\n                      <property id=\"stateid\" type=\"simple\"><![CDATA[released]]></property>\n                      <property id=\"displayvalue\" type=\"simple\"><![CDATA[Released]]></property>\n                      <property id=\"nextstate\" type=\"collection\">\n                        <collection>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[pendingrelease]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[preliminaryrelease]]></property>\n                          </propertylist>\n                        </collection>\n                      </property>\n                    </propertylist>\n                  </property>\n                </propertylist>\n                <propertylist id=\"rejected\">\n                  <property id=\"state\" type=\"propertylist\">\n                    <propertylist id=\"rejected\">\n                      <property id=\"stateid\" type=\"simple\"><![CDATA[rejected]]></property>\n                      <property id=\"displayvalue\" type=\"simple\"><![CDATA[Rejected]]></property>\n                      <property id=\"nextstate\" type=\"collection\">\n                        <collection>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[initial]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[received]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[active]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[pendingrelease]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[onhold]]></property>\n                          </propertylist>\n                        </collection>\n                      </property>\n                    </propertylist>\n                  </property>\n                </propertylist>\n                <propertylist id=\"onhold\">\n                  <property id=\"state\" type=\"propertylist\">\n                    <propertylist id=\"onhold\">\n                      <property id=\"stateid\" type=\"simple\"><![CDATA[onhold]]></property>\n                      <property id=\"displayvalue\" type=\"simple\"><![CDATA[OnHold]]></property>\n                      <property id=\"nextstate\" type=\"collection\">\n                        <collection>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[initial]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[received]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[active]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[rejected]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[preliminaryrelease]]></property>\n                          </propertylist>\n                        </collection>\n                      </property>\n                    </propertylist>\n                  </property>\n                </propertylist>\n                <propertylist id=\"preliminaryrelease\">\n                  <property id=\"state\" type=\"propertylist\">\n                    <propertylist id=\"preliminaryrelease\">\n                      <property id=\"stateid\" type=\"simple\"><![CDATA[preliminaryrelease]]></property>\n                      <property id=\"displayvalue\" type=\"simple\"><![CDATA[Preliminary Release]]></property>\n                      <property id=\"nextstate\" type=\"collection\">\n                        <collection>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[released]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[onhold]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[active]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[rejected]]></property>\n                          </propertylist>\n                          <propertylist>\n                            <property id=\"states\" type=\"simple\"><![CDATA[pendingrelease]]></property>\n                          </propertylist>\n                        </collection>\n                      </property>\n                    </propertylist>\n                  </property>\n                </propertylist>\n              </collection>\n            </property>\n          </propertylist>";
        try {
            statechart = new PropertyList();
            statechart.setPropertyList(statechartXml);
        }
        catch (SapphireException e) {
            Trace.logError("Failed to parse the batch state property list");
        }
    }
}


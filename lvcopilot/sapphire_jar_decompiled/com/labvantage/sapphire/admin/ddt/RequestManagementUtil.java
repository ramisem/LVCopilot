/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RequestManagementUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90651 $";

    public static boolean isPolicyEnabled(ConfigurationProcessor configurationProcessor) throws SapphireException {
        PropertyList policy = configurationProcessor.getPolicy("BatchSamplePolicy", "Sapphire Custom");
        return policy != null && policy.getProperty("syncrequeststate", "N").equalsIgnoreCase("Y");
    }

    public static RequestStatus getSyncedRequestStatus(String requestId, QueryProcessor queryProcessor) throws SapphireException {
        boolean accepted = false;
        boolean received = false;
        boolean opened = false;
        boolean completed = true;
        RequestStatus syncedStatus = null;
        DataSet sisterSamples = queryProcessor.getPreparedSqlDataSet("getsistersamples", "SELECT s_sampleid, samplestatus FROM s_sample WHERE s_sample.requestid = ?", new Object[]{requestId});
        for (int i = 0; i < sisterSamples.getRowCount(); ++i) {
            String sampleStatus = sisterSamples.getValue(i, "samplestatus", "");
            if (sampleStatus.equalsIgnoreCase("Initial")) {
                accepted = true;
                received = false;
                opened = false;
                completed = false;
                continue;
            }
            if (sampleStatus.equalsIgnoreCase("Received")) {
                accepted = true;
                received = true;
                opened = false;
                completed = false;
                continue;
            }
            if (!sampleStatus.equalsIgnoreCase("InProgress")) continue;
            accepted = true;
            received = true;
            opened = true;
            completed = false;
        }
        if (completed) {
            syncedStatus = RequestManagementUtil.doAutoRelease(queryProcessor, new ConfigurationProcessor(queryProcessor.getConnectionid()), requestId) ? RequestStatus.RELEASED : RequestStatus.COMPLETED;
        } else if (opened) {
            syncedStatus = RequestStatus.OPEN;
        } else if (received) {
            syncedStatus = RequestStatus.RECEIVED;
        } else if (accepted) {
            syncedStatus = RequestStatus.ACCEPTED;
        }
        return syncedStatus;
    }

    public static boolean isAllSamplesReceived(String requestId, QueryProcessor queryProcessor) throws SapphireException {
        boolean isAllSamplesReceived = false;
        DataSet getReceivedSampleCountDataSet = queryProcessor.getPreparedSqlDataSet("getReceivedDataSet", "select count(s_sampleid) allsamplecount,sum(case samplestatus when 'Received' then 1 else 0 end) receivedsamplecount from s_sample where requestid=?", new Object[]{requestId});
        if (getReceivedSampleCountDataSet != null && getReceivedSampleCountDataSet.getRowCount() > 0) {
            int allsamplecount = getReceivedSampleCountDataSet.getInt(0, "allsamplecount", 0);
            int receivedsamplecount = getReceivedSampleCountDataSet.getInt(0, "receivedsamplecount", 0);
            if (allsamplecount > 0 && allsamplecount == receivedsamplecount) {
                isAllSamplesReceived = true;
            }
        }
        return isAllSamplesReceived;
    }

    public static boolean hasTestingStartedOnAnyChildSample(String requestId, QueryProcessor queryProcessor) throws SapphireException {
        boolean hasTestingStartedOnAnyChildSample = false;
        int getCountOfInProgressSamples = queryProcessor.getPreparedCount("SELECT count(*) FROM s_sample where requestid=? and samplestatus not in ('Initial','Received','Cancelled')", new Object[]{requestId});
        if (getCountOfInProgressSamples > 0) {
            hasTestingStartedOnAnyChildSample = true;
        }
        return hasTestingStartedOnAnyChildSample;
    }

    public static boolean hasTestingCompletedForAllChildSample(String requestId, QueryProcessor queryProcessor) throws SapphireException {
        int completedSampleCount;
        int allsamplecount;
        boolean hasTestingCompletedForAllChildSample = false;
        String sql = "SELECT count(sample.s_sampleid) allsamplecount,  sum(case when sample.samplestatus='Completed' then 1 else 0 end) completedsamplecount  FROM s_request request, s_sample sample  where requestid=?  and request.s_requestid=sample.requestid ";
        DataSet hasTestingCompletedForAllChildSampleDataSet = queryProcessor.getPreparedSqlDataSet("hasTestingCompletedForAllChildSample", sql, new Object[]{requestId});
        if (hasTestingCompletedForAllChildSampleDataSet != null && hasTestingCompletedForAllChildSampleDataSet.getRowCount() > 0 && (allsamplecount = hasTestingCompletedForAllChildSampleDataSet.getInt(0, "allsamplecount", 0)) == (completedSampleCount = hasTestingCompletedForAllChildSampleDataSet.getInt(0, "completedsamplecount", 0))) {
            hasTestingCompletedForAllChildSample = true;
        }
        return hasTestingCompletedForAllChildSample;
    }

    public static boolean hasTestingFinishedOnAllChildSample(String requestId, QueryProcessor queryProcessor) throws SapphireException {
        boolean hasTestingFinishedOnAllChildSample = false;
        String sql = "select count(sample.s_sampleid) allsamplecount, sum(case when sample.samplestatus='Completed' and (reviewrequiredflag='N' or reviewrequiredflag is null)  then 1 when sample.disposalstatus='Retained' or sample.samplestatus in ('Reviewed', 'Reported', 'Disposed', 'Cancelled') then 1 else 0 end) finishedsamplecount from s_request request, s_sample sample where request.s_requestid=sample.requestid and request.s_requestid=?";
        DataSet hasTestingFinishedOnAllChildSampleDataSet = queryProcessor.getPreparedSqlDataSet("hasTestingFinishedOnAllChildSample", sql, new Object[]{requestId});
        if (hasTestingFinishedOnAllChildSampleDataSet != null && hasTestingFinishedOnAllChildSampleDataSet.getRowCount() > 0) {
            int allsamplecount = hasTestingFinishedOnAllChildSampleDataSet.getInt(0, "allsamplecount", 0);
            int finishedSampleCount = hasTestingFinishedOnAllChildSampleDataSet.getInt(0, "finishedsamplecount", 0);
            if (allsamplecount > 0 && allsamplecount == finishedSampleCount) {
                hasTestingFinishedOnAllChildSample = true;
            }
        }
        return hasTestingFinishedOnAllChildSample;
    }

    public static RequestOperation getRequestOperation(PropertyList properties, QueryProcessor queryProcessor) throws SapphireException {
        RequestOperation operation = null;
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(queryProcessor.getConnectionid());
        String sampleId = properties.getProperty("sampleid");
        String requestId = properties.getProperty("requestid", "");
        String requestStatusString = properties.getProperty("requeststatus", "");
        String autoRelease = properties.getProperty("autoreleaseflag", "");
        if (requestId.length() == 0 && sampleId.length() > 0) {
            requestId = queryProcessor.getPreparedSqlDataSet("getrequestid", "SELECT s_sampleid, requestid FROM s_sample WHERE s_sampleid=?", new Object[]{sampleId}).getValue(0, "requestid", "");
            properties.setProperty("requestid", requestId);
        }
        if (requestId.length() == 0) {
            return null;
        }
        if (requestStatusString.length() == 0 || autoRelease.length() == 0) {
            DataSet requestData = queryProcessor.getPreparedSqlDataSet("getrequeststatus", "SELECT s_requestid, requeststatus, autoreleaseflag FROM s_request WHERE s_requestid=?", new Object[]{requestId});
            requestStatusString = requestData.getValue(0, "requeststatus", "");
            autoRelease = requestData.getValue(0, "autoreleaseflag", "N");
            properties.setProperty("requeststatus", requestStatusString);
        }
        RequestStatus currentRequestStatus = RequestStatus.getRequestStatus(requestStatusString);
        String previousSampleStatus = properties.getProperty("previoussamplestatus");
        String currentSampleStatus = properties.getProperty("currentsamplestatus");
        if (currentSampleStatus.equals("Received")) {
            if (currentRequestStatus.equals((Object)RequestStatus.ACCEPTED) || currentRequestStatus.equals((Object)RequestStatus.INITIAL)) {
                boolean isAllSamplesReceived = RequestManagementUtil.isAllSamplesReceived(requestId, queryProcessor);
                if (isAllSamplesReceived) {
                    operation = RequestOperation.RECEIVE;
                }
            } else if (currentRequestStatus == RequestStatus.COMPLETED || currentRequestStatus == RequestStatus.RELEASED) {
                operation = RequestOperation.UNCOMPLETE;
            }
        } else if (currentSampleStatus.equals("Initial")) {
            if (!(currentSampleStatus.equals(previousSampleStatus) || currentRequestStatus != RequestStatus.COMPLETED && currentRequestStatus != RequestStatus.RELEASED)) {
                operation = RequestOperation.UNCOMPLETE;
            }
        } else if (currentSampleStatus.equals("InProgress")) {
            if (currentRequestStatus.isBefore(RequestStatus.OPEN) && currentRequestStatus.isAfter(RequestStatus.PENDINGACCEPTANCE)) {
                operation = RequestOperation.OPEN;
            } else if (currentRequestStatus == RequestStatus.COMPLETED || currentRequestStatus == RequestStatus.RELEASED) {
                operation = RequestOperation.UNCOMPLETE;
            }
        } else if ((currentSampleStatus.equalsIgnoreCase("Completed") || currentSampleStatus.equalsIgnoreCase("Reviewed") || currentSampleStatus.equalsIgnoreCase("Reported") || currentSampleStatus.equalsIgnoreCase("Disposed") || currentSampleStatus.equalsIgnoreCase("Cancelled")) && currentRequestStatus.isAfter(RequestStatus.PENDINGACCEPTANCE)) {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT COUNT(*) samplesinrequest,");
            sql.append(" SUM(CASE");
            sql.append(" WHEN s.samplestatus = '").append("Completed").append("' AND (s.reviewrequiredflag='N' OR s.reviewrequiredflag IS NULL) THEN 1");
            sql.append(" WHEN s.disposalstatus='Retained' OR s.samplestatus IN ('").append("Reviewed").append("', '").append("Reported").append("', '").append("Disposed").append("', '").append("Cancelled").append("') THEN 1");
            sql.append(" ELSE 0");
            sql.append(" END) finishedsamples");
            sql.append(" FROM s_sample s, s_request r");
            sql.append(" WHERE");
            sql.append(" s.requestid=? ");
            sql.append(" AND s.requestid = r.s_requestid");
            DataSet completeRequest = queryProcessor.getPreparedSqlDataSet("completeRequest", sql.toString(), new Object[]{requestId});
            if (completeRequest.getInt(0, "samplesinrequest", 0) == completeRequest.getInt(0, "finishedsamples", 0) && currentRequestStatus.isBefore(RequestStatus.COMPLETED)) {
                operation = autoRelease.equalsIgnoreCase("Y") && RequestManagementUtil.doAutoRelease(queryProcessor, configurationProcessor, requestId) ? RequestOperation.AUTORELEASE : RequestOperation.COMPLETE;
            } else if (!(currentSampleStatus.equalsIgnoreCase("Reviewed") || currentSampleStatus.equalsIgnoreCase("Completed") || currentSampleStatus.equalsIgnoreCase("Disposed") || currentSampleStatus.equalsIgnoreCase("Cancelled") || currentRequestStatus != RequestStatus.COMPLETED)) {
                operation = RequestOperation.UNCOMPLETE;
            } else if (completeRequest.getInt(0, "samplesinrequest", 0) != completeRequest.getInt(0, "finishedsamples", 0) && currentRequestStatus.isBefore(RequestStatus.COMPLETED) && currentSampleStatus.equalsIgnoreCase("Completed")) {
                operation = RequestOperation.OPEN;
            } else if (currentSampleStatus.equalsIgnoreCase("Completed") && previousSampleStatus.equalsIgnoreCase("Reviewed") && currentRequestStatus.isAfter(RequestStatus.COMPLETED)) {
                operation = RequestOperation.UNRELEASE;
            }
        }
        return operation;
    }

    public static boolean hasOpenIncidents(String requestId, QueryProcessor queryProcessor) throws SapphireException {
        boolean hasOpenIncidents = false;
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder numberOfOpenIncidentsInRequestSql = new StringBuilder("SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3 ");
        numberOfOpenIncidentsInRequestSql.append(" FROM incidentitem ii, incident i ");
        numberOfOpenIncidentsInRequestSql.append(" WHERE ii.sourcesdcid = 'Request' ");
        numberOfOpenIncidentsInRequestSql.append(" AND ii.sourcekeyid1 = ").append(safeSQL.addVar(requestId));
        numberOfOpenIncidentsInRequestSql.append(" AND i.incidentid = ii.incidentid ");
        numberOfOpenIncidentsInRequestSql.append(" AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed') ");
        DataSet numberOfOpenIncidentsInRequest = queryProcessor.getPreparedSqlDataSet(numberOfOpenIncidentsInRequestSql.toString(), safeSQL.getValues());
        if (numberOfOpenIncidentsInRequest.getRowCount() > 0) {
            hasOpenIncidents = true;
        } else {
            safeSQL.reset();
            StringBuilder numberOfOpenIncidentsInChildSamplesSql = new StringBuilder(" SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3 ");
            numberOfOpenIncidentsInChildSamplesSql.append(" FROM incidentitem ii, incident i, s_sample s ");
            numberOfOpenIncidentsInChildSamplesSql.append(" WHERE s.requestid = ").append(safeSQL.addVar(requestId));
            numberOfOpenIncidentsInChildSamplesSql.append(" AND ii.sourcesdcid = 'Sample' AND ii.sourcekeyid1 = s.s_sampleid ");
            numberOfOpenIncidentsInChildSamplesSql.append(" AND i.incidentid = ii.incidentid AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed') ");
            DataSet numberOfOpenIncidentsInChildSamples = queryProcessor.getPreparedSqlDataSet(numberOfOpenIncidentsInChildSamplesSql.toString(), safeSQL.getValues());
            if (numberOfOpenIncidentsInChildSamples.getRowCount() > 0) {
                hasOpenIncidents = true;
            }
        }
        return hasOpenIncidents;
    }

    public static boolean doAutoRelease(QueryProcessor queryProcessor, ConfigurationProcessor configurationProcessor, String requestId) throws SapphireException {
        boolean autoRelease = true;
        DataSet specs = queryProcessor.getPreparedSqlDataSet("specconditions", "SELECT spec.condition FROM s_sample sample, sdispec spec WHERE sample.requestid = ? AND spec.sdcid='Sample' AND sample.s_sampleid = spec.keyid1", new Object[]{requestId});
        HashMap<String, String> filter = new HashMap<String, String>();
        PropertyListCollection specInterpretation = configurationProcessor.getPolicy("DataEntryPolicy", "Sapphire Custom").getCollection("SpecConditions");
        Iterator iter = specInterpretation.iterator();
        String passCondition = "Pass";
        while (iter.hasNext()) {
            PropertyList condition = (PropertyList)iter.next();
            if (!condition.getProperty("interpretation").equals(passCondition)) continue;
            passCondition = condition.getProperty("SpecCond");
            break;
        }
        filter.put("condition", passCondition);
        DataSet passedSpecs = specs.getFilteredDataSet(filter);
        if (specs.getRowCount() == 0 || specs.getRowCount() != passedSpecs.getRowCount()) {
            autoRelease = false;
        }
        return autoRelease;
    }

    public static String populateRequestRejectionComments(QueryProcessor queryProcessor, String requestId, String function) {
        String sql = "select sds.rejectionreason, sds.notes from sdiapproval sa, sdiapprovalstep sds where sa.approvaltypeid = sds.approvaltypeid and sa.sdcid = sds.sdcid and sa.keyid1 = sds.keyid1 and sa.keyid2 = sds.keyid2 and sa.keyid3 = sds.keyid3 and sa.sdcid = 'Request' and sa.keyid1= ? and sa.approvalfunction=? order by sds.createdt desc";
        DataSet rejectionReasonDs = queryProcessor.getPreparedSqlDataSet("rejectionreason", sql.toString(), new Object[]{requestId, function});
        StringBuffer rejectionComment = new StringBuffer("");
        for (int i = 0; i < rejectionReasonDs.getRowCount(); ++i) {
            String rejectionReason = rejectionReasonDs.getString(i, "rejectionreason", "").trim();
            String comment = rejectionReasonDs.getString(i, "notes", "").trim();
            if (rejectionReason.length() <= 0 || comment.length() <= 0) continue;
            if (rejectionReason.trim().equals(comment.trim())) {
                rejectionComment.append("Reason: " + comment + " \n");
                continue;
            }
            rejectionComment.append("Reason: (" + rejectionReason + "): " + comment + " \n");
        }
        return rejectionComment.toString().trim();
    }

    public static String getDepartmentSecuritySwitch(ConfigurationProcessor configurationProcessor, String propertyId, String propertyListId) throws SapphireException {
        PropertyList batchSamplePolicy = configurationProcessor.getPolicy("BatchSamplePolicy", "Sapphire Custom");
        PropertyList departmentSecurityPL = batchSamplePolicy.getPropertyListNotNull(propertyListId);
        String securityDepartmentValue = departmentSecurityPL.getProperty(propertyId, "");
        return securityDepartmentValue;
    }

    public static String fetchDepartmentsToDeleteSDISecurityDepartments(QueryProcessor queryProcessor, String requestId) {
        String sdiDepartments = "";
        String sql = "select securitydepartment from sdisecuritydepartment where sdcid='Request' and keyid1=?";
        DataSet sdiDepartmentDs = queryProcessor.getPreparedSqlDataSet("sdidepartments", sql, new Object[]{requestId});
        if (sdiDepartmentDs.getRowCount() > 0) {
            sdiDepartments = sdiDepartmentDs.getString(0, "securitydepartment", "");
        }
        return sdiDepartments;
    }

    public static enum RequestItemDetailStatus {
        PENDING("Pending"),
        DONE("Done"),
        CANCELLED("Cancelled");

        private String statusValue;

        private RequestItemDetailStatus(String statusValue) {
            this.statusValue = statusValue;
        }

        public String getStatusValue() {
            return this.statusValue;
        }

        public static RequestItemDetailStatus getRequestItemDetailStatus(String statusValue) {
            if (statusValue != null) {
                for (RequestItemDetailStatus requestItemDetailStatus : RequestItemDetailStatus.values()) {
                    if (!statusValue.equalsIgnoreCase(requestItemDetailStatus.statusValue)) continue;
                    return requestItemDetailStatus;
                }
            }
            return null;
        }

        public boolean isBefore(RequestItemStatus requestItemStatus) {
            return this.ordinal() < requestItemStatus.ordinal();
        }

        public boolean isAfter(RequestItemStatus requestItemStatus) {
            return this.ordinal() > requestItemStatus.ordinal();
        }
    }

    public static enum RequestItemStatus {
        PENDING("Pending"),
        INPROGRESS("InProgress"),
        DONE("Done"),
        DENIED("Denied"),
        CANCELLED("Cancelled");

        private String statusValue;

        private RequestItemStatus(String statusValue) {
            this.statusValue = statusValue;
        }

        public String getStatusValue() {
            return this.statusValue;
        }

        public static RequestItemStatus getRequestItemStatus(String statusValue) {
            if (statusValue != null) {
                for (RequestItemStatus requestItemStatus : RequestItemStatus.values()) {
                    if (!statusValue.equalsIgnoreCase(requestItemStatus.statusValue)) continue;
                    return requestItemStatus;
                }
            }
            return null;
        }

        public boolean isBefore(RequestItemStatus requestItemStatus) {
            return this.ordinal() < requestItemStatus.ordinal();
        }

        public boolean isAfter(RequestItemStatus requestItemStatus) {
            return this.ordinal() > requestItemStatus.ordinal();
        }
    }

    public static enum RequestOperation {
        ADD("add"),
        SENDFORACCEPTANCE("sendforacceptance"),
        ACCEPT("accept"),
        RECEIVE("receive"),
        UNRECEIVE("unreceive"),
        OPEN("open"),
        COMPLETE("complete"),
        UNCOMPLETE("uncomplete"),
        HOLD("hold"),
        RELEASEHOLD("releasehold"),
        CANCEL("cancel"),
        CLEARAPPROVAL("clearapproval"),
        RELEASE("release"),
        UNCANCEL("uncancel"),
        UNRELEASE("unrelease"),
        UNREJECT("unreject"),
        AUTORELEASE("autorelease"),
        ADDSAMPLES("addsamples"),
        ADDADHOCSAMPLES("addadhocsamples");

        private String operationValue;

        private RequestOperation(String operationValue) {
            this.operationValue = operationValue;
        }

        public String getOperationValue() {
            return this.operationValue;
        }

        public static RequestOperation getRequestOperation(String operationValue) {
            if (operationValue != null) {
                for (RequestOperation requestOperation : RequestOperation.values()) {
                    if (!operationValue.equalsIgnoreCase(requestOperation.operationValue)) continue;
                    return requestOperation;
                }
            }
            return null;
        }
    }

    public static enum RequestStatus {
        DRAFT("Draft"),
        PENDINDINGTESTASSOCIATION("CreatingSamples"),
        PENDINGACCEPTANCE("PendingAcceptance"),
        ACCEPTED("Accepted"),
        DENIED("Denied"),
        INITIAL("Initial"),
        ONHOLD("OnHold"),
        RECEIVED("Received"),
        OPEN("Open"),
        COMPLETED("Completed"),
        RELEASED("Released"),
        REJECTED("Rejected"),
        CLOSED("Closed"),
        CANCELLED("Cancelled");

        private String statusValue;

        private RequestStatus(String statusValue) {
            this.statusValue = statusValue;
        }

        public String getStatusValue() {
            return this.statusValue;
        }

        public static RequestStatus getRequestStatus(String statusValue) {
            if (statusValue != null) {
                for (RequestStatus requestStatus : RequestStatus.values()) {
                    if (!statusValue.equalsIgnoreCase(requestStatus.statusValue)) continue;
                    return requestStatus;
                }
            }
            return null;
        }

        public boolean isBefore(RequestStatus requestStatus) {
            return this.ordinal() < requestStatus.ordinal();
        }

        public boolean isAfter(RequestStatus requestStatus) {
            return this.ordinal() > requestStatus.ordinal();
        }
    }
}


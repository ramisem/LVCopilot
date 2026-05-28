/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddEventDef
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 104027 $";
    private static ArrayList __NonUpdatableCols = new ArrayList();
    private QueryProcessor __QueryProcessor;
    private ActionProcessor __ActionProcessor;
    private SDCProcessor __SdcProcessor;
    public static final String ID = "AddEventDef";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_CLINICALPROTOCOLID = "clinicalprotocolid";
    public static final String PROPERTY_CLINICALPROTOCOLVERSIONID = "clinicalprotocolversionid";
    public static final String PROPERTY_CLINICALPROTOCOLREVISION = "clinicalprotocolrevision";
    public static final String PROPERTY_COHORTID = "cohortid";
    public static final String PROPERTY_PARENTEVENTDEFID = "parenteventdefid";
    public static final String PROPERTY_S_SAMPLETYPEID = "s_sampletypeid";
    public static final String PROPERTY_CONTAINERTYPEID = "containertypeid";
    public static final String RETURN_MSG = "actionresponse";
    public static final String PROPERTY_EVENTTYPE = "eventdeftype";
    public static final String PROPERTY_TEMPLATEID = "templateid";
    public static final String PROPERTY_EVENTLABEL = "eventdeflabel";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String resp = "";
        this.__ActionProcessor = this.getActionProcessor();
        this.__QueryProcessor = this.getQueryProcessor();
        this.__SdcProcessor = this.getSDCProcessor();
        String sampleTypeIds = properties.getProperty(PROPERTY_S_SAMPLETYPEID, "");
        String protocolId = properties.getProperty(PROPERTY_CLINICALPROTOCOLID, "");
        String versionId = properties.getProperty(PROPERTY_CLINICALPROTOCOLVERSIONID, "");
        String revision = properties.getProperty(PROPERTY_CLINICALPROTOCOLREVISION, "");
        String cohortIds = properties.getProperty(PROPERTY_COHORTID, "");
        String eventtype = properties.getProperty(PROPERTY_EVENTTYPE, "");
        TranslationProcessor tp = this.getTranslationProcessor();
        int numberOfCopy = 0;
        if (protocolId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Clinical Protocol Id not passed into the action."));
        }
        if (versionId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Clinical Protocol Version Id not passed into the action."));
        }
        if (revision.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Clinical Protocol Revision not passed into the action."));
        }
        String sourceEventId = properties.getProperty(PROPERTY_TEMPLATEID, "");
        try {
            String copies = VERSIONID;
            numberOfCopy = 1;
            if (!cohortIds.equalsIgnoreCase("")) {
                String[] cohortIdsArr = StringUtil.split(cohortIds, ";");
                numberOfCopy = cohortIdsArr.length;
                copies = Integer.toString(numberOfCopy);
            }
            String newkeyid1 = "";
            properties.setProperty("sdcid", "LV_EventDef");
            properties.setProperty("copies", copies);
            if (sourceEventId.length() > 0) {
                String[] eventIds = StringUtil.split(sourceEventId, ";");
                String childEventSQL = "SELECT * FROM s_eventdef WHERE parenteventdefid = ? order by parenteventdefid, usersequence ";
                PreparedStatement childEventsPsmt = this.database.prepareStatement("childEvents", childEventSQL);
                for (int i = 0; i < eventIds.length; ++i) {
                    properties.setProperty("templatekeyid1", eventIds[i]);
                    this.__ActionProcessor.processAction("AddSDI", VERSIONID, properties);
                    newkeyid1 = (String)properties.get("newkeyid1");
                    childEventsPsmt.setString(1, eventIds[i]);
                    DataSet dsChildEvents = new DataSet(childEventsPsmt.executeQuery());
                    this.logger.debug("Start Copying all child records and child events from source event to target event.");
                    if (dsChildEvents != null && dsChildEvents.getRowCount() > 0) {
                        this.copyChildEventsDef(newkeyid1, cohortIds, protocolId, versionId, revision, dsChildEvents);
                    }
                    this.logger.debug("End Copying all child records and child events from source event to target event.");
                    if (sampleTypeIds.equalsIgnoreCase("")) continue;
                    sampleTypeIds = this.getExtraSampleTypes(sampleTypeIds, newkeyid1);
                }
                this.database.closeStatement("childEvents");
            } else {
                this.__ActionProcessor.processAction("AddSDI", VERSIONID, properties);
                newkeyid1 = (String)properties.get("newkeyid1");
            }
            if (!sampleTypeIds.equalsIgnoreCase("")) {
                String[] newkeyid1Arr = StringUtil.split(newkeyid1, ";");
                String[] sampletypeIdsArr = StringUtil.split(sampleTypeIds, ";");
                SafeSQL safeSQL = new SafeSQL();
                String specimentTypeSQL = "SELECT s_assaytypeid, s_sampletypeid,specimentype,arrivalorder FROM s_cpassaytypesampletype WHERE s_clinicalprotocolid=" + safeSQL.addVar(protocolId) + "  AND s_clinicalprotocolversionid=" + safeSQL.addVar(versionId) + "  AND s_clinicalprotocolrevision=" + safeSQL.addVar(revision) + "  AND s_sampletypeid in (" + safeSQL.addIn(sampleTypeIds, ";") + ") AND ('" + eventtype + "'='Visit' or '" + eventtype + "'='Timepoint')  order by s_sampletypeid,specimentype,arrivalorder desc";
                DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(specimentTypeSQL, safeSQL.getValues());
                DataSet assaytypeIdDS = new DataSet();
                assaytypeIdDS.copyRow(ds, -1, 1);
                DataSet specimentTypeDS = new DataSet();
                ArrayList<DataSet> list = ds.getGroupedDataSets("s_sampletypeid,specimentype");
                for (DataSet tempDS : list) {
                    if (tempDS.size() > 1) {
                        if (tempDS.getValue(0, "arrivalorder").equalsIgnoreCase("All")) {
                            specimentTypeDS.copyRow(tempDS, 1, 1);
                            continue;
                        }
                        specimentTypeDS.copyRow(tempDS, 0, 1);
                        continue;
                    }
                    specimentTypeDS.copyRow(tempDS, 0, 1);
                }
                HashMap<String, String> hp = new HashMap<String, String>();
                StringBuffer newkeyids = new StringBuffer();
                StringBuffer newkeyidsForSpecimenDef = new StringBuffer();
                StringBuffer sampleTypeIds1 = new StringBuffer();
                StringBuffer sampleTypeIdsForSpecimenDef = new StringBuffer();
                StringBuffer specimenTypes = new StringBuffer();
                StringBuffer specimenTypesForAssaytype = new StringBuffer();
                StringBuffer quantity = new StringBuffer();
                StringBuffer arrivalorderForAssaytype = new StringBuffer();
                StringBuffer usersequences = new StringBuffer();
                StringBuffer specimendefids = new StringBuffer();
                StringBuffer newkeyidsForAssaytype = new StringBuffer();
                StringBuffer sampletypeidsForAssaytype = new StringBuffer();
                StringBuffer assaytypeids = new StringBuffer();
                StringBuffer mapid = new StringBuffer();
                StringBuffer defaultworkitemflag = new StringBuffer();
                StringBuffer usersequencesForAssaytype = new StringBuffer();
                StringBuffer usersequencesForSpecimenDef = new StringBuffer();
                for (int i = 0; i < newkeyid1Arr.length; ++i) {
                    int maxsequence = 0;
                    int maxsequenceForAssaytype = 0;
                    for (int j = 0; j < sampletypeIdsArr.length; ++j) {
                        DataSet dsAssayTypes;
                        int maxsequenceForSpecimenDef = 0;
                        newkeyids.append(";" + newkeyid1Arr[i]);
                        sampleTypeIds1.append(";" + sampletypeIdsArr[j]);
                        usersequences.append(";" + Integer.toString(++maxsequence));
                        hp.clear();
                        hp.put(PROPERTY_S_SAMPLETYPEID, sampletypeIdsArr[j]);
                        DataSet dsSpecimenType = specimentTypeDS.getFilteredDataSet(hp);
                        if (dsSpecimenType != null && dsSpecimenType.size() > 0) {
                            for (int at = 0; at < dsSpecimenType.size(); ++at) {
                                newkeyidsForSpecimenDef.append(";" + newkeyid1Arr[i]);
                                sampleTypeIdsForSpecimenDef.append(";" + sampletypeIdsArr[j]);
                                specimenTypes.append(";" + dsSpecimenType.getValue(at, "specimentype"));
                                quantity.append(";" + dsSpecimenType.getValue(at, "arrivalorder"));
                                specimendefids.append(";" + (at + 1));
                                usersequencesForSpecimenDef.append(";" + Integer.toString(++maxsequenceForSpecimenDef));
                            }
                        }
                        if ((dsAssayTypes = assaytypeIdDS.getFilteredDataSet(hp)) == null || dsAssayTypes.size() <= 0) continue;
                        for (int at = 0; at < dsAssayTypes.size(); ++at) {
                            String assayTypeId = dsAssayTypes.getValue(at, "s_assaytypeid");
                            String specimenTypeid = dsAssayTypes.getValue(at, "specimentype");
                            String arrivalorder = dsAssayTypes.getValue(at, "arrivalorder");
                            newkeyidsForAssaytype.append(";" + newkeyid1Arr[i]);
                            sampletypeidsForAssaytype.append(";" + sampletypeIdsArr[j]);
                            assaytypeids.append(";" + assayTypeId);
                            mapid.append(";1");
                            specimenTypesForAssaytype.append(";" + specimenTypeid);
                            arrivalorderForAssaytype.append(";" + arrivalorder);
                            defaultworkitemflag.append(";Y");
                            usersequencesForAssaytype.append(";" + Integer.toString(++maxsequenceForAssaytype));
                        }
                    }
                }
                this.logger.info("Start Populating EventDef SampleType details.");
                PropertyList detailProps = new PropertyList();
                detailProps.setProperty("linkid", "sampletype");
                detailProps.setProperty("sdcid", "LV_EventDef");
                detailProps.setProperty("keyid1", newkeyids.substring(1));
                detailProps.setProperty("s_eventdefid", newkeyids.substring(1));
                detailProps.setProperty(PROPERTY_S_SAMPLETYPEID, sampleTypeIds1.substring(1));
                detailProps.setProperty("usersequence", usersequences.substring(1));
                this.__ActionProcessor.processAction("AddSDIDetail", VERSIONID, detailProps);
                this.logger.info("End Populating EventDef SampleType details.");
                if (specimenTypes.length() > 0) {
                    detailProps.clear();
                    this.logger.info("Start Populating EventDef SampleType SpecimenDef details.");
                    detailProps.setProperty("linkid", "sampletype");
                    detailProps.setProperty("detaillinkid", "specimendef");
                    detailProps.setProperty("sdcid", "LV_EventDef");
                    detailProps.setProperty("keyid1", newkeyidsForSpecimenDef.substring(1));
                    detailProps.setProperty("s_eventdefid", newkeyidsForSpecimenDef.substring(1));
                    detailProps.setProperty(PROPERTY_S_SAMPLETYPEID, sampleTypeIdsForSpecimenDef.substring(1));
                    detailProps.setProperty("s_specimendefid", specimendefids.substring(1));
                    detailProps.setProperty("specimentype", specimenTypes.substring(1));
                    detailProps.setProperty("quantity", quantity.substring(1));
                    detailProps.setProperty("usersequence", usersequencesForSpecimenDef.substring(1));
                    this.__ActionProcessor.processAction("AddSDIDetail", VERSIONID, detailProps);
                    this.logger.info("End Populating EventDef SampleType SpecimenDef details.");
                }
                if (assaytypeids.length() > 0) {
                    detailProps.clear();
                    this.logger.info("Start Populating EventDef SampleType AssayType WorkItem Map details.");
                    detailProps.setProperty("linkid", "sampletype");
                    detailProps.setProperty("detaillinkid", "assaytype");
                    detailProps.setProperty("sdcid", "LV_EventDef");
                    detailProps.setProperty("keyid1", newkeyidsForAssaytype.substring(1));
                    detailProps.setProperty("s_eventdefid", newkeyidsForAssaytype.substring(1));
                    detailProps.setProperty(PROPERTY_S_SAMPLETYPEID, sampletypeidsForAssaytype.substring(1));
                    detailProps.setProperty("s_assaytypeid", assaytypeids.substring(1));
                    detailProps.setProperty("s_mapid", mapid.substring(1));
                    detailProps.setProperty("specimentype", specimenTypesForAssaytype.substring(1));
                    detailProps.setProperty("arrivalorder", arrivalorderForAssaytype.substring(1));
                    detailProps.setProperty("defaultworkitemflag", defaultworkitemflag.substring(1));
                    detailProps.setProperty("usersequence", usersequencesForAssaytype.substring(1));
                    this.__ActionProcessor.processAction("AddSDIDetail", VERSIONID, detailProps);
                    this.logger.info("End Populating EventDef SampleType AssayType WorkItem Map details.");
                }
            }
            resp = tp.translate("Save Operation Successful");
        }
        catch (Exception e) {
            resp = e.getMessage();
            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Could not process action:") + " " + ID + "=>" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            properties.setProperty(RETURN_MSG, resp);
        }
    }

    private void copyChildEventsDef(String newEventIds, String cohortIds, String protocolId, String versionId, String revision, DataSet childEvents) throws SapphireException {
        Calendar now = Calendar.getInstance();
        String[] newEventDefIds = StringUtil.split(newEventIds, ";");
        childEvents.setDate(-1, "createdt", now);
        childEvents.setString(-1, "createtool", this.connectionInfo.getTool());
        childEvents.setString(-1, "createby", this.connectionInfo.getSysuserId());
        String[] cohortIdsArr = StringUtil.split(cohortIds, ";");
        PropertyList actionProps = new PropertyList();
        for (int j = 0; j < newEventDefIds.length; ++j) {
            for (int k = 0; k < childEvents.size(); ++k) {
                actionProps.setProperty("sdcid", "LV_EventDef");
                DataSet columnsDS = this.__SdcProcessor.getColumnData("LV_EventDef");
                for (int col = 0; col < columnsDS.getRowCount(); ++col) {
                    String colid = columnsDS.getString(col, "columnid");
                    if (__NonUpdatableCols.contains(colid)) continue;
                    actionProps.setProperty(colid, childEvents.getValue(k, colid));
                }
                actionProps.setProperty(PROPERTY_CLINICALPROTOCOLID, protocolId);
                actionProps.setProperty(PROPERTY_CLINICALPROTOCOLVERSIONID, versionId);
                actionProps.setProperty(PROPERTY_CLINICALPROTOCOLREVISION, revision);
                actionProps.setProperty(PROPERTY_PARENTEVENTDEFID, newEventDefIds[j]);
                actionProps.setProperty(PROPERTY_COHORTID, cohortIdsArr[j]);
                actionProps.setProperty(PROPERTY_TEMPLATEID, childEvents.getValue(k, "s_eventdefid"));
                this.__ActionProcessor.processAction("AddSDI", VERSIONID, actionProps);
                actionProps.clear();
            }
        }
    }

    private String getExtraSampleTypes(String sampleTypeIDs, String eventDefIds) {
        StringBuffer extraSampleTypes = new StringBuffer();
        String[] sampletypeIdsArr = StringUtil.split(sampleTypeIDs, ";");
        SafeSQL safeSQL = new SafeSQL();
        String sampleTypesSQL = "select s_sampletypeid from s_eventdefsampletype where s_eventdefid in (" + safeSQL.addIn(eventDefIds, ";") + ")";
        DataSet sampleTypesDS = this.__QueryProcessor.getPreparedSqlDataSet(sampleTypesSQL, safeSQL.getValues());
        HashMap<String, String> hp = new HashMap<String, String>();
        for (int i = 0; i < sampletypeIdsArr.length; ++i) {
            hp.clear();
            hp.put(PROPERTY_S_SAMPLETYPEID, sampletypeIdsArr[i]);
            int rowid = sampleTypesDS.findRow(hp);
            if (rowid != -1) continue;
            extraSampleTypes.append(";" + sampletypeIdsArr[i]);
        }
        if (extraSampleTypes.length() > 0) {
            return extraSampleTypes.substring(1);
        }
        return "";
    }

    static {
        __NonUpdatableCols.add("moddt");
        __NonUpdatableCols.add("modby");
        __NonUpdatableCols.add("modtool");
        __NonUpdatableCols.add("eventstatus");
        __NonUpdatableCols.add("auditsequence");
        __NonUpdatableCols.add("tracelogid");
        __NonUpdatableCols.add("collectiondt");
        __NonUpdatableCols.add("verifiedby");
        __NonUpdatableCols.add("verifieddt");
        __NonUpdatableCols.add("approvedby");
        __NonUpdatableCols.add("approveddt");
        __NonUpdatableCols.add("eventdt");
        __NonUpdatableCols.add("receiveddt");
    }
}


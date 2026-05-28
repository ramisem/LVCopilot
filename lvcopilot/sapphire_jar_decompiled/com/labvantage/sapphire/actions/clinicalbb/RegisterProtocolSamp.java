/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.clinicalbb.AddProtocolTest;
import com.labvantage.sapphire.actions.clinicalbb.AddTimepoint;
import com.labvantage.sapphire.actions.clinicalbb.AddUpdProtocolSample;
import com.labvantage.sapphire.actions.clinicalbb.AddUpdateParticipant;
import com.labvantage.sapphire.actions.clinicalbb.AddVisit;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RegisterProtocolSamp
extends BaseAction
implements sapphire.action.RegisterProtocolSamp {
    static final String LABVANTAGE_CVS_ID = "$Revision: 57207 $";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String suppressError = actionProps.getProperty("suppresserror", "Y");
        String participantIds = "";
        String visitEventIds = "";
        if (actionProps.containsKey("participantid") || actionProps.containsKey("externalparticipantid")) {
            participantIds = this.callAddUpdateParticipantAction(actionProps, suppressError);
            visitEventIds = this.callAddVisitAction(actionProps, participantIds, suppressError);
        }
        if (actionProps.getProperty("timepointlabel", "").length() > 0 || actionProps.getProperty("timepointdefid", "").length() > 0) {
            String timepointIds = this.callAddTimepointAction(actionProps, visitEventIds, suppressError);
            String sampleIds = this.callAddUpdProtocolSampleAction(actionProps, timepointIds, participantIds, suppressError);
            this.callAddProtocolTest(actionProps, sampleIds, suppressError);
        } else {
            String sampleIds = this.callAddUpdProtocolSampleAction(actionProps, visitEventIds, participantIds, suppressError);
            this.callAddProtocolTest(actionProps, sampleIds, suppressError);
        }
    }

    private void callAddProtocolTest(PropertyList actionProps, String sampleIds, String suppressError) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sampleid", sampleIds);
        props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
        props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
        props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", "N"));
        try {
            boolean executeAction = true;
            if (props.getProperty("sampleid").length() == 0) {
                executeAction = false;
            }
            if (executeAction) {
                this.getActionProcessor().processActionClass(AddProtocolTest.class.getName(), props);
            }
        }
        catch (SapphireException e) {
            if (suppressError.equalsIgnoreCase("N")) {
                this.logger.error("Error in processing AddProtocolTest action " + e);
                throw new SapphireException(e);
            }
            this.logger.warn("Error in processing AddProtocolTest action " + e);
        }
    }

    private String callAddUpdateParticipantAction(PropertyList actionProps, String suppressError) throws SapphireException {
        String participantId = "";
        try {
            PropertyList props = new PropertyList();
            props.setProperty("studyid", actionProps.getProperty("studyid", ""));
            props.setProperty("studycode", actionProps.getProperty("studycode", ""));
            props.setProperty("siteid", actionProps.getProperty("enrollingsiteid", ""));
            props.setProperty("departmentid", actionProps.getProperty("enrollingdepartmentid", ""));
            props.setProperty("protocolrevision", actionProps.getProperty("protocolrevision", ""));
            props.setProperty("cohortid", actionProps.getProperty("cohortid", ""));
            props.setProperty("cohortid", actionProps.getProperty("cohortid", ""));
            props.setProperty("participantid", actionProps.getProperty("participantid", ""));
            props.setProperty("externalparticipantid", actionProps.getProperty("externalparticipantid", ""));
            props.setProperty("subjectid", actionProps.getProperty("subjectid", ""));
            props.setProperty("participantenrollmentdt", actionProps.getProperty("participantenrollmentdt", ""));
            props.setProperty("autopartenrollment", actionProps.getProperty("autopartenrollment", ""));
            props.setProperty("sitename", actionProps.getProperty("enrollingsitename", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", "N"));
            this.setExtraColsBefCallingAction(actionProps, props);
            boolean executeAction = this.validateInputParams(props);
            if (executeAction) {
                if (props.getProperty("participantid").length() == 0 && props.getProperty("externalparticipantid").length() == 0 && props.getProperty("subjectid").length() == 0) {
                    executeAction = false;
                }
                if (props.getProperty("studyid").length() == 0 && props.getProperty("studycode").length() == 0) {
                    executeAction = false;
                }
                if (props.getProperty("siteid").length() == 0 && props.getProperty("sitename").length() == 0) {
                    executeAction = false;
                }
            }
            if (executeAction) {
                this.getActionProcessor().processActionClass(AddUpdateParticipant.class.getName(), props);
                participantId = props.getProperty("newparticipantid");
            }
        }
        catch (SapphireException e) {
            if (suppressError.equalsIgnoreCase("N")) {
                this.logger.error("Error in processing AddUpdateParticipant action " + e);
                throw new SapphireException(e);
            }
            this.logger.warn("Error in processing AddUpdateParticipant action " + e);
        }
        return participantId.length() == 0 ? actionProps.getProperty("participantid", "") : participantId;
    }

    private void setExtraColsBefCallingAction(PropertyList actionProps, PropertyList props) {
        String[] colPrefix;
        for (String aColPrefix : colPrefix = new String[]{"participant_", "subject_", "samplefamily_", "visit_", "timepoint_"}) {
            for (Object o : actionProps.keySet()) {
                String param = (String)o;
                if (!param.contains(aColPrefix)) continue;
                props.setProperty(param, actionProps.getProperty(param, ""));
            }
        }
    }

    private String callAddVisitAction(PropertyList actionProps, String participantId, String suppressError) throws SapphireException {
        String visitId = "";
        try {
            PropertyList props = new PropertyList();
            String visitedSite = actionProps.getProperty("visitedsiteid", "");
            String visitedSiteDesc = actionProps.getProperty("visitedsitename", "");
            String visitedDept = actionProps.getProperty("visiteddepartmentid", "");
            if (visitedSite.length() == 0 && visitedSiteDesc.length() == 0 && visitedDept.length() == 0) {
                props.setProperty("visitedsite", actionProps.getProperty("enrollingsiteid", ""));
                props.setProperty("visiteddepartment", actionProps.getProperty("enrollingdepartmentid", ""));
                props.setProperty("visitedsitename", actionProps.getProperty("enrollingsitename", ""));
            } else {
                props.setProperty("visitedsite", actionProps.getProperty("visitedsiteid", ""));
                props.setProperty("visiteddepartment", actionProps.getProperty("visiteddepartmentid", ""));
                props.setProperty("visitedsitename", actionProps.getProperty("visitedsitename", ""));
            }
            props.setProperty("participantid", participantId);
            props.setProperty("eventdt", actionProps.getProperty("eventdt", ""));
            props.setProperty("eventlabel", actionProps.getProperty("eventlabel", ""));
            props.setProperty("eventdefid", actionProps.getProperty("eventdefid", ""));
            props.setProperty("eventstatus", actionProps.getProperty("eventstatus", ""));
            props.setProperty("adhocevtflag", actionProps.getProperty("adhocevtflag", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", "N"));
            if (actionProps.containsKey("addeventpolicy")) {
                props.setProperty("addeventpolicy", actionProps.getProperty("addeventpolicy"));
            }
            this.setExtraColsBefCallingAction(actionProps, props);
            boolean executeAction = this.validateInputParams(props);
            if (executeAction) {
                String participantid = props.getProperty("participantid");
                if (participantid.length() == 0) {
                    executeAction = false;
                } else {
                    String eventLabel = props.getProperty("eventlabel", "");
                    String eventDefId = props.getProperty("eventdefid", "");
                    if (eventLabel.length() == 0 && eventDefId.length() == 0) {
                        executeAction = false;
                    }
                }
            }
            if (executeAction) {
                this.getActionProcessor().processActionClass(AddVisit.class.getName(), props);
                visitId = (String)props.get("neweventid");
            }
        }
        catch (SapphireException e) {
            if (suppressError.equalsIgnoreCase("N")) {
                this.logger.error("Error in processing AddVisit action " + e);
                throw new SapphireException(e);
            }
            this.logger.warn("Error in processing AddVisit action " + e);
        }
        return visitId;
    }

    private String callAddTimepointAction(PropertyList actionProps, String visitEventIds, String suppressError) throws SapphireException {
        String timepointIds = "";
        try {
            PropertyList props = new PropertyList();
            props.setProperty("visiteventid", visitEventIds);
            props.setProperty("eventlabel", actionProps.getProperty("timepointlabel", ""));
            props.setProperty("eventdefid", actionProps.getProperty("timepointdefid", ""));
            props.setProperty("eventstatus", actionProps.getProperty("eventstatus", ""));
            props.setProperty("eventdt", actionProps.getProperty("eventdt", ""));
            props.setProperty("adhocevtflag", actionProps.getProperty("adhocevtflag", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", "N"));
            if (actionProps.containsKey("addeventpolicy")) {
                props.setProperty("addeventpolicy", actionProps.getProperty("addeventpolicy"));
            }
            this.setExtraColsBefCallingAction(actionProps, props);
            boolean executeAction = this.validateInputParams(props);
            if (executeAction) {
                if (props.getProperty("visiteventid", "").length() == 0) {
                    executeAction = false;
                }
                if ("eventlabel".length() == 0 && "eventdefid".length() == 0) {
                    executeAction = false;
                }
            }
            if (executeAction) {
                this.getActionProcessor().processActionClass(AddTimepoint.class.getName(), props);
                timepointIds = (String)props.get("neweventid");
                Trace.logInfo("Executed AddTimepoint action. Returned timepoint IDs: " + timepointIds);
                ArrayList<String> timepointList = new ArrayList<String>();
                String[] timepointArray = StringUtil.split(timepointIds, ";");
                String[] visitArray = StringUtil.split(visitEventIds, ";");
                int i = 0;
                for (String timepoint : timepointArray) {
                    if (OpalUtil.isEmpty(timepoint)) {
                        timepointList.add(visitArray.length > i ? visitArray[i] : "");
                    } else {
                        timepointList.add(timepoint);
                    }
                    ++i;
                }
                timepointIds = OpalUtil.toDelimitedString(timepointList, ";");
            }
        }
        catch (SapphireException e) {
            if (suppressError.equalsIgnoreCase("N")) {
                this.logger.error("Error in processing AddTimepoint action " + e);
                throw new SapphireException(e);
            }
            this.logger.warn("Error in processing AddTimepoint action " + e);
        }
        return timepointIds;
    }

    private String callAddUpdProtocolSampleAction(PropertyList actionProps, String eventIds, String participantIds, String suppressError) throws SapphireException {
        String sampleIds = "";
        String defaultClinicalEvent = actionProps.getProperty("eventlabel");
        if (actionProps.getProperty("timepointlabel").length() > 0) {
            DataSet ds = new DataSet();
            ds.addColumnValues("eventlabel", 0, actionProps.getProperty("eventlabel"), ";");
            ds.addColumnValues("tplabel", 0, actionProps.getProperty("timepointlabel"), ";");
            ds.padColumns();
            defaultClinicalEvent = ds.getValue(0, "eventlabel") + " " + ds.getValue(0, "tplabel");
            for (int i = 1; i < ds.size(); ++i) {
                defaultClinicalEvent = defaultClinicalEvent + ";" + ds.getValue(i, "eventlabel") + " " + ds.getValue(i, "tplabel");
            }
        }
        try {
            PropertyList props = new PropertyList();
            props.setProperty("eventid", eventIds);
            props.setProperty("eventlabel", actionProps.getProperty("clinicalevent", defaultClinicalEvent));
            props.setProperty("sampleid", actionProps.getProperty("sampleid", ""));
            props.setProperty("externalsampleid", actionProps.getProperty("externalsampleid", ""));
            props.setProperty("participantid", participantIds);
            props.setProperty("externalparticipantid", actionProps.getProperty("externalparticipantid", ""));
            props.setProperty("collectiondate", actionProps.getProperty("collectiondate", ""));
            props.setProperty("sampletypeid", actionProps.getProperty("sampletypeid", ""));
            props.setProperty("auditreason", actionProps.getProperty("auditreason", ""));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", "N"));
            props.setProperty("kitid", actionProps.getProperty("kitid", ""));
            props.setProperty("kittrackitemid", actionProps.getProperty("kittrackitemid", ""));
            props.setProperty("specimentype", actionProps.getProperty("specimentype", ""));
            props.setProperty("restrictclassid", actionProps.getProperty("restrictclassid", ""));
            this.setExtraColsBefCallingAction(actionProps, props);
            boolean executeAction = this.validateInputParams(props);
            if (executeAction) {
                String participantid = props.getProperty("participantid");
                if (participantid.length() == 0) {
                    executeAction = false;
                } else if (props.getProperty("sampleid").length() == 0 && props.getProperty("externalsampleid").length() == 0) {
                    executeAction = false;
                } else {
                    ArrayList<String> studylist = new ArrayList<String>();
                    ArrayList<String> sitelist = new ArrayList<String>();
                    DataSet ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), "LV_Participant", "select s_participantid, sstudyid, studysiteid from s_participant where s_participantid in ([])", participantid);
                    if (ds != null && ds.size() > 0) {
                        String[] participantArray;
                        for (String participant : participantArray = StringUtil.split(participantid, ";")) {
                            if (OpalUtil.isNotEmpty(participant)) {
                                int row = ds.findRow("s_participantid", participant);
                                if (row != -1) {
                                    studylist.add(ds.getString(row, "sstudyid", ""));
                                    sitelist.add(ds.getString(row, "studysiteid", ""));
                                    continue;
                                }
                                studylist.add("");
                                sitelist.add("");
                                continue;
                            }
                            studylist.add("");
                            sitelist.add("");
                        }
                    }
                    props.setProperty("visitedsiteid", OpalUtil.toDelimitedString(sitelist, ";"));
                    props.setProperty("studyid", OpalUtil.toDelimitedString(studylist, ";"));
                }
            }
            if (executeAction) {
                this.getActionProcessor().processActionClass(AddUpdProtocolSample.class.getName(), props);
                sampleIds = (String)props.get("newsampleid");
            }
        }
        catch (SapphireException e) {
            if (suppressError.equalsIgnoreCase("N")) {
                this.logger.error("Error in processing AddUpdProtocolSample action " + e);
                throw new SapphireException(e);
            }
            this.logger.warn("Error in processing AddUpdProtocolSample action " + e);
        }
        return sampleIds;
    }

    private boolean validateInputParams(PropertyList actionProps) {
        boolean valid = true;
        int maxlength = this.findMaxLength(actionProps);
        for (Object o : actionProps.keySet()) {
            String param = (String)o;
            String value = actionProps.getProperty(param);
            int length = StringUtil.split(value, ";").length;
            if (maxlength <= length || length <= 1) continue;
            valid = false;
            break;
        }
        return valid;
    }

    private int findMaxLength(PropertyList actionProps) {
        int maxlength = 0;
        for (Object o : actionProps.keySet()) {
            String param = (String)o;
            String value = actionProps.getProperty(param);
            int l = StringUtil.split(value, ";").length;
            if (l <= maxlength) continue;
            maxlength = l;
        }
        return maxlength;
    }
}


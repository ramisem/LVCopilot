/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BoxStateRule
extends BaseBioBankRule {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 86098 $";
    public static final String STATE_PACKAGE = "Package";
    public static final String STATE_PHYSICALSTORE = "PhysicalStore";
    public static final String STATE_CUSTODIAN = "Custodian";
    private String tracelogid = "";
    private String auditreason = "";
    private String auditactivity = "";
    private String auditsignedflag = "";

    public BoxStateRule(DBAccess database, ConnectionInfo connectionInfo, String tracelogid, String auditreason, String auditactivity, String auditsignedflag) {
        super(database, connectionInfo);
        this.tracelogid = tracelogid;
        this.auditreason = auditreason;
        this.auditactivity = auditactivity;
        this.auditsignedflag = auditsignedflag;
    }

    @Override
    public String getRuleId() {
        return "BoxStateRule";
    }

    public void processRule(String boxid, boolean forceUpdate) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (boxid != null && boxid.length() > 0) {
            String trackitemid = this.getSDITrackItemID("LV_Box", boxid);
            String custodianid = this.getColumnValue("TrackItemSDC", trackitemid, "custodialuserid");
            String boxCD = this.getColumnValue("TrackItemSDC", trackitemid, "custodialdepartmentid");
            String boxCurrentStorageUnitId = this.getColumnValue("TrackItemSDC", trackitemid, "currentstorageunitid");
            String physicalStore = "";
            String packageid = "";
            String packagestatus = "";
            String boxState = null;
            if (trackitemid != null && trackitemid.trim().length() > 0) {
                String boxStorageUnitId = this.getSDIStorageUnitID("LV_Box", boxid);
                if (StringUtil.getLen(boxCurrentStorageUnitId) > 0L) {
                    String boxCurrentSDC = this.getColumnValue("StorageUnitSDC", boxCurrentStorageUnitId, "linksdcid");
                    if ("LV_Package".equalsIgnoreCase(boxCurrentSDC)) {
                        boxState = STATE_PACKAGE;
                        packageid = this.getColumnValue("StorageUnitSDC", boxCurrentStorageUnitId, "linkkeyid1");
                        packagestatus = this.getColumnValue("LV_Package", packageid, "packagestatus");
                    } else {
                        physicalStore = this.getParentSDIBySDC(boxStorageUnitId, STATE_PHYSICALSTORE);
                        if (StringUtil.getLen(physicalStore) > 0L) {
                            boxState = STATE_PHYSICALSTORE;
                        } else if (StringUtil.getLen(custodianid) > 0L) {
                            boxState = STATE_CUSTODIAN;
                        }
                    }
                }
                if (StringUtil.getLen(boxState) > 0L) {
                    if (custodianid == null) {
                        custodianid = "";
                    }
                    if (boxCD == null) {
                        boxCD = "";
                    }
                    String targetCustodian = custodianid;
                    String targetCD = boxCD;
                    if (STATE_PACKAGE.equals(boxState)) {
                        if ("Shipped".equals(packagestatus)) {
                            targetCustodian = "";
                            targetCD = "Transit";
                        } else if ("Received".equals(packagestatus) || "On Hold".equals(packagestatus)) {
                            targetCD = this.getColumnValue("LV_Package", packageid, "recipientdepartmentid");
                        } else if ("Created".equals(packagestatus)) {
                            targetCustodian = "";
                            targetCD = this.getColumnValue("LV_Package", packageid, "senderdepartmentid");
                        } else if ("Cancelled".equals(packagestatus)) {
                            targetCustodian = this.connectionInfo.getSysuserId();
                            targetCD = this.getDefaultDepartment();
                        } else if (packagestatus.equals("Emptied")) {
                            targetCustodian = "";
                            targetCD = this.getColumnValue("LV_Package", packageid, "recipientdepartmentid");
                        }
                    } else if (STATE_PHYSICALSTORE.equals(boxState)) {
                        targetCD = this.getColumnValue(STATE_PHYSICALSTORE, physicalStore, "departmentid");
                        String physicalstoreclass = this.getColumnValue(STATE_PHYSICALSTORE, physicalStore, "storageclass");
                        if (!"Temporary".equals(physicalstoreclass)) {
                            targetCustodian = "";
                        }
                        if (StringUtil.getLen(targetCD) == 0L) {
                            throw new SapphireException("BoxStateRule", "VALIDATION", this.getTranslationProcessor().translate("Corrupted data: Physical Store is not in any Custodial Department") + " (" + physicalStore + ")");
                        }
                    } else if (STATE_CUSTODIAN.equals(boxState)) {
                        if (StringUtil.getLen(boxCD) > 0L) {
                            if (!OpalUtil.getUserDepartments(this.getQueryProcessor(), custodianid).contains(boxCD)) {
                                throw new SapphireException("BoxStateRule", "VALIDATION", this.getTranslationProcessor().translate("Box must be in one of the custodian's Custodial Departments"));
                            }
                        } else {
                            targetCD = this.getDefaultDepartment(custodianid);
                        }
                    } else {
                        targetCD = this.getDefaultDepartment(custodianid);
                    }
                    if (targetCD == null) {
                        targetCD = "";
                    }
                    if (!custodianid.equalsIgnoreCase(targetCustodian) || !boxCD.equals(targetCD)) {
                        boolean setCustodyDetails = false;
                        PropertyList editTrackItemProps = new PropertyList();
                        if (!custodianid.equalsIgnoreCase(targetCustodian)) {
                            setCustodyDetails = true;
                            editTrackItemProps.setProperty("custodialuserid", targetCustodian);
                        }
                        if (!boxCD.equals(targetCD)) {
                            setCustodyDetails = true;
                            editTrackItemProps.setProperty("custodialdepartmentid", targetCD);
                        }
                        if (setCustodyDetails) {
                            editTrackItemProps.setProperty("sdcid", "TrackItemSDC");
                            editTrackItemProps.setProperty("keyid1", trackitemid);
                            editTrackItemProps.setProperty("propsmatch", "Y");
                            editTrackItemProps.setProperty("__sdcruleignore", "Y");
                            editTrackItemProps.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                            editTrackItemProps.setProperty("tracelogid", this.tracelogid);
                            editTrackItemProps.setProperty("auditreason", this.auditreason);
                            editTrackItemProps.setProperty("auditactivity", this.auditactivity);
                            editTrackItemProps.setProperty("auditsignedflag", this.auditsignedflag);
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), editTrackItemProps);
                        }
                    }
                }
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.LV_Package;
import com.labvantage.sapphire.admin.ddt.PhysicalStore;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.TrackItemSDC;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.admin.ddt.rules.SMSUser;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PlateStateRule
extends BaseRule {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54975 $";
    public static final String STATE_PACKAGE = "Package";
    public static final String STATE_PHYSICALSTORE = "PhysicalStore";
    public static final String STATE_CUSTODIAN = "Custodian";

    public PlateStateRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public String getRuleId() {
        return "PlateStateRule";
    }

    public void processRule(String plateid, boolean forceUpdate) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (plateid != null && plateid.length() > 0) {
            String trackitemid = "";
            String physicalStore = "";
            String custodianid = "";
            String packageid = "";
            String packagestatus = null;
            String plateCD = "";
            String plateCurrentStorageUnitId = "";
            String plateCurrentSDC = "";
            DataSet trackitemdetails = TrackItemSDC.getTIDetailsByLinkKeyid(this.database, "Plate", plateid, false);
            if (trackitemdetails != null && trackitemdetails.size() > 0) {
                trackitemid = trackitemdetails.getValue(0, "trackitemid");
                custodianid = trackitemdetails.getValue(0, "custodialuserid");
                plateCD = trackitemdetails.getValue(0, "custodialdepartmentid");
                plateCurrentStorageUnitId = trackitemdetails.getValue(0, "currentstorageunitid");
            }
            if (trackitemid != null && trackitemid.trim().length() > 0) {
                String plateState;
                String plateStorageUnitId = StorageUnitSDC.getStorageUntitidByLinkKeyid(this.getQueryProcessor(), "Plate", plateid);
                String physicalStoreStorageUnitId = StorageUnitSDC.getStorageNodeBySDC(this.getQueryProcessor(), plateStorageUnitId, STATE_PHYSICALSTORE);
                if (physicalStoreStorageUnitId != null && physicalStoreStorageUnitId.trim().length() > 0) {
                    physicalStore = StorageUnitSDC.getLinkKeyid1ByStorageUnitId(this.getQueryProcessor(), physicalStoreStorageUnitId);
                }
                if (plateCurrentStorageUnitId != null && plateCurrentStorageUnitId.trim().length() > 0) {
                    plateCurrentSDC = StorageUnitSDC.getLinkSDCIdByStorageUnitId(this.getQueryProcessor(), plateCurrentStorageUnitId);
                }
                if ("LV_Package".equalsIgnoreCase(plateCurrentSDC)) {
                    packageid = StorageUnitSDC.getLinkKeyid1ByStorageUnitId(this.getQueryProcessor(), plateCurrentStorageUnitId);
                    packagestatus = LV_Package.getStatus(this.getQueryProcessor(), packageid);
                }
                if (packagestatus == null) {
                    packagestatus = "";
                }
                if (StringUtil.getLen(plateState = this.getPlateState(packageid, physicalStore, custodianid)) > 0L) {
                    if (custodianid == null) {
                        custodianid = "";
                    }
                    if (plateCD == null) {
                        plateCD = "";
                    }
                    String targetCustodian = custodianid;
                    String targetCD = plateCD;
                    if (plateState.equals(STATE_PACKAGE)) {
                        if (packagestatus == null) {
                            packagestatus = "";
                        } else if (packagestatus.equals("Shipped")) {
                            targetCustodian = "";
                            targetCD = "Transit";
                        } else if (packagestatus.equals("Received") || packagestatus.equals("On Hold")) {
                            targetCD = LV_Package.getRecipientDepartmentId(this.database, packageid);
                        } else if (packagestatus.equals("Created")) {
                            targetCustodian = "";
                        } else if (packagestatus.equals("Cancelled")) {
                            targetCustodian = this.connectionInfo.getSysuserId();
                            targetCD = this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment();
                        } else if (packagestatus.equals("Emptied")) {
                            targetCustodian = "";
                            targetCD = LV_Package.getRecipientDepartmentId(this.database, packageid);
                        }
                    } else if (plateState.equals(STATE_PHYSICALSTORE)) {
                        targetCD = PhysicalStore.getCustodialDepartmentId(this.getQueryProcessor(), physicalStore);
                        targetCustodian = "";
                        if (StringUtil.getLen(targetCD) == 0L) {
                            throw new SapphireException("PlateStateRule", "VALIDATION", this.getTranslationProcessor().translate("Corrupted data: Physical Store is not in any Custodial Department") + " (" + physicalStore + ")");
                        }
                    } else if (plateState.equals(STATE_CUSTODIAN)) {
                        if (StringUtil.getLen(plateCD) > 0L) {
                            if (!OpalUtil.getUserDepartments(this.getQueryProcessor(), custodianid).contains(plateCD)) {
                                throw new SapphireException("PlateStateRule", "VALIDATION", this.getTranslationProcessor().translate("Plate must be in one of the custodian's Custodial Departments"));
                            }
                        } else {
                            targetCD = SMSUser.getDefaultDepartment(this.getQueryProcessor(), custodianid);
                        }
                    } else {
                        targetCD = this.connectionInfo.getDefaultDepartment();
                    }
                    if (targetCD == null) {
                        targetCD = "";
                    }
                    if (!custodianid.equalsIgnoreCase(targetCustodian) || !plateCD.equals(targetCD)) {
                        PropertyList editTrackItemProps = new PropertyList();
                        if (!custodianid.equalsIgnoreCase(targetCustodian)) {
                            editTrackItemProps.put("custodialuserid", targetCustodian);
                        }
                        if (!plateCD.equals(targetCD)) {
                            editTrackItemProps.put("custodialdepartmentid", targetCD);
                        }
                        editTrackItemProps.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                        TrackItemSDC.setCustodyDetails(this.connectionInfo, trackitemid, editTrackItemProps);
                    }
                }
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private String getPlateState(String packageid, String physicalStoreid, String custodianid) {
        String plateState = null;
        if (StringUtil.getLen(packageid) > 0L) {
            plateState = STATE_PACKAGE;
        } else if (StringUtil.getLen(physicalStoreid) > 0L) {
            plateState = STATE_PHYSICALSTORE;
        } else if (StringUtil.getLen(custodianid) > 0L) {
            plateState = STATE_CUSTODIAN;
        }
        return plateState;
    }
}


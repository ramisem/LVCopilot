/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.TrackItemSDC;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.List;
import sapphire.SapphireException;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;

public class BoxDisposeRule
extends BaseRule {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 54967 $";
    private ErrorHandler ruleErrorHandler = new ErrorHandler();

    public BoxDisposeRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public ErrorHandler getErrorHandler() {
        return this.ruleErrorHandler;
    }

    public String getRuleId() {
        return "BoxDisposeRule";
    }

    public void processRule(List boxids) throws SapphireException {
        if (boxids == null || boxids.size() == 0) {
            return;
        }
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        for (Object boxid1 : boxids) {
            try {
                DataSet trackItemsInBox;
                String currentUser;
                String boxid = (String)boxid1;
                if (boxid == null || boxid.trim().length() == 0) continue;
                String custodianid = "";
                DataSet trackitemdetails = TrackItemSDC.getTIDetailsByLinkKeyid(this.database, "LV_Box", boxid, false);
                if (trackitemdetails != null && trackitemdetails.size() > 0) {
                    custodianid = trackitemdetails.getValue(0, "custodialuserid");
                }
                if ((currentUser = this.connectionInfo.getSysuserId()) != null && !currentUser.equals(custodianid)) {
                    this.ruleErrorHandler.add("", "", this.getClass().getName(), "VALIDATION", "You are not the custodian of the box " + boxid);
                }
                if ((trackItemsInBox = TrackItemSDC.getTrackItemsInSU(this.getQueryProcessor(), "LV_Box", boxid)) != null && trackItemsInBox.size() > 0) {
                    boolean hasTrackItems = false;
                    StringBuilder trackItems = new StringBuilder();
                    for (int trackItemCount = 0; trackItemCount < trackItemsInBox.size(); ++trackItemCount) {
                        String linkkeyid1 = trackItemsInBox.getValue(trackItemCount, "linkkeyid1");
                        if (linkkeyid1.equalsIgnoreCase(boxid)) continue;
                        hasTrackItems = true;
                        trackItems.append(", ").append(trackItemsInBox.getValue(trackItemCount, "trackitemid"));
                    }
                    if (hasTrackItems) {
                        this.ruleErrorHandler.add("", "", this.getClass().getName(), "VALIDATION", "Box " + boxid + " is not empty. Box has following trackitems - " + trackItems.substring(1));
                    }
                }
                String boxStorageUnitIds = StorageUnitSDC.getAllStorageUnits(this.getQueryProcessor(), this.connectionInfo.isOracle(), StorageUnitSDC.getStorageUnitId(this.getQueryProcessor(), "LV_Box", boxid));
                StorageUnitSDC.clearReservedStorageUnits(this.database, boxStorageUnitIds);
            }
            catch (SapphireException saphEx) {
                this.ruleErrorHandler.add("", "", this.getClass().getName(), "VALIDATION", "");
            }
        }
        if (this.ruleErrorHandler.size() > 0) {
            throw new SapphireException(this.getClass().getName() + " Failed.");
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }
}


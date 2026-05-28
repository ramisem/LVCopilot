/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sms;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.admin.ddt.TrackItemSDC;
import java.util.ArrayList;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ReceivePackage
extends BaseAction
implements sapphire.action.ReceivePackage {
    @Override
    public void processAction(PropertyList actionProps) {
        String packageId = actionProps.getProperty("packageid");
        String takeCustody = actionProps.getProperty("takecustody", "N");
        String sdcRuleConfirm = actionProps.getProperty("__sdcruleconfirm", "N");
        try {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Package");
            props.setProperty("keyid1", packageId);
            props.setProperty("packagestatus", "Received");
            props.setProperty("__sdcruleconfirm", sdcRuleConfirm);
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            if ("Y".equals(takeCustody)) {
                this.takecustody(packageId, sdcRuleConfirm);
            } else {
                props.clear();
                props.setProperty("sdcid", "LV_Package");
                props.setProperty("keyid1", packageId);
                props.setProperty("currentstorageunitid", "");
                props.setProperty("__sdcruleconfirm", sdcRuleConfirm);
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
            }
            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
    }

    private void takecustody(String packageids, String sdcRuleConfirm) throws ActionException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select s_package.s_packageid, s_package.recipientdepartmentid, storageunit.storageunitid, trackitem.trackitemid, trackitem.currentstorageunitid from s_package, storageunit, trackitem where storageunit.linksdcid = 'LV_Package' and storageunit.linkkeyid1 = s_package.s_packageid and trackitem.linksdcid = 'LV_Package' and trackitem.linkkeyid1 = s_package.s_packageid and s_package.s_packageid in (" + safeSQL.addIn(packageids, ";") + ")";
        ArrayList<String> list = new ArrayList<String>();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String packageid = ds.getString(i, "s_packageid");
                if (!OpalUtil.isNotEmpty(packageid)) continue;
                DataSet tids = TrackItemSDC.getTrackItemsInSU(this.getQueryProcessor(), "LV_Package", packageid);
                if (tids != null && tids.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("trackitemid", tids.getColumnValues("trackitemid", ";"));
                    props.setProperty("currentstorageunitid", "(null)");
                    props.setProperty("__sdcruleconfirm", sdcRuleConfirm);
                    this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
                }
                if (ds.getString(i, "currentstorageunitid", "").length() <= 0) continue;
                list.add(ds.getString(i, "trackitemid"));
            }
        }
        if (list.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", OpalUtil.toDelimitedString(list, ";"));
            props.setProperty("currentstorageunitid", "");
            props.setProperty("__sdcruleignore", "Y");
            props.setProperty("__sdcruleconfirm", sdcRuleConfirm);
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }
}


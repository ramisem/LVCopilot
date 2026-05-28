/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GrantCustody
extends BaseAction {
    public static final String PROPERTY_ID = "GrantCustody";
    public static final String PROPERTY_VERSION = "1";
    public static final String PROPERTY_FROM_DEPARTMENTID = "fromdepartmentid";
    public static final String PROPERTY_FROM_SYSUSERID = "fromsysuserid";
    public static final String PROPERTY_TO_DEPARTMENTID = "todepartmentid";
    public static final String PROPERTY_TO_SYSUSERID = "tosysuserid";
    public static final String PROPERTY_REASON = "reason";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_TRACKITEMID = "trackitemid";
    public static final String AUDITACTIVITY = "GrantCustody";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        SafeSQL safeSQL;
        String fromdepartmenid = actionProps.getProperty(PROPERTY_FROM_DEPARTMENTID, "");
        String fromsysuserid = actionProps.getProperty(PROPERTY_FROM_SYSUSERID, "");
        String todepartmentid = actionProps.getProperty(PROPERTY_TO_DEPARTMENTID, "");
        String tosysuserid = actionProps.getProperty(PROPERTY_TO_SYSUSERID, "");
        String reason = actionProps.getProperty(PROPERTY_REASON, "");
        String sdcid = actionProps.getProperty(PROPERTY_SDCID, "");
        String keyid1 = actionProps.getProperty(PROPERTY_KEYID1, "");
        String trackitemid = actionProps.getProperty(PROPERTY_TRACKITEMID, "");
        if (trackitemid.length() == 0 && sdcid.length() > 0 && keyid1.length() > 0) {
            safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid from trackitem where linksdcid = " + safeSQL.addVar(sdcid) + " and linkkeyid1 in (" + safeSQL.addIn(keyid1, ";") + ")", safeSQL.getValues());
            if (OpalUtil.isNotEmpty(ds)) {
                trackitemid = ds.getColumnValues(PROPERTY_TRACKITEMID, ";");
            }
        }
        if (trackitemid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Missing Input"), "VALIDATION", this.getTranslationProcessor().translate("No items passed to grant custody"));
        }
        if (fromdepartmenid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Missing Input"), "VALIDATION", this.getTranslationProcessor().translate("Missing Department of the User granting custody"));
        }
        if (fromsysuserid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Missing Input"), "VALIDATION", this.getTranslationProcessor().translate("Missing User ID of the User granting custody"));
        }
        if (!this.database.checkPreparedExists("select sysuserid from departmentsysuser where sysuserid = ? and departmentid = ?", new String[]{fromsysuserid, fromdepartmenid})) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid Input"), "VALIDATION", this.getTranslationProcessor().translate("From User is not member of From Department"));
        }
        safeSQL = new SafeSQL();
        if (!this.database.checkPreparedExists("select trackitemid from trackitem where trackitemid in (" + safeSQL.addIn(trackitemid, ";") + ") and custodialuserid = " + safeSQL.addVar(fromsysuserid), safeSQL.getValues())) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid Input"), "VALIDATION", this.getTranslationProcessor().translate("User does not have custody of one or more Items"));
        }
        if (todepartmentid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Missing Input"), "VALIDATION", this.getTranslationProcessor().translate("Missing Department of the User taking custody"));
        }
        if (tosysuserid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Missing Input"), "VALIDATION", this.getTranslationProcessor().translate("Missing User ID of the User taking custody"));
        }
        if (!this.database.checkPreparedExists("select sysuserid from departmentsysuser where sysuserid = ? and departmentid = ?", new String[]{tosysuserid, todepartmentid})) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid Input"), "VALIDATION", this.getTranslationProcessor().translate("To User is not member of To Department"));
        }
        PropertyList props = new PropertyList();
        props.setProperty(PROPERTY_SDCID, "TrackItemSDC");
        props.setProperty(PROPERTY_KEYID1, trackitemid);
        props.setProperty("custodialuserid", tosysuserid);
        props.setProperty("custodytakendt", "n");
        props.setProperty("currentstorageunitid", "(null)");
        if (!todepartmentid.equals(fromdepartmenid)) {
            props.setProperty("custodialdepartmentid", todepartmentid);
        }
        props.setProperty("__bypasscustodyrules", "Y");
        props.setProperty("__takecustodyoperation", "Y");
        props.setProperty("auditsignedflag", "Y");
        props.setProperty("auditreason", reason);
        props.setProperty("auditactivity", "GrantCustody");
        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        actionProps.setProperty("tracelogid", props.getProperty("tracelogid"));
    }
}


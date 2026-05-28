/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.pkg.ValidateCDTPacking;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.AddSDISecurityDept;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.sms.CreateStorageUnit;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PerformCDT
extends BaseAction
implements sapphire.action.PerformCDT {
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        String sdcid = actionProps.getProperty("sdcid");
        String keyid1 = actionProps.getProperty("keyid1");
        String keyid2 = actionProps.getProperty("keyid2");
        String keyid3 = actionProps.getProperty("keyid3");
        String destination = actionProps.getProperty("destination");
        String transitlocation = actionProps.getProperty("transitlocation");
        String sdcruleconfirm = actionProps.getProperty("__sdcruleconfirm");
        String auditreason = actionProps.getProperty("auditreason");
        String auditactivity = actionProps.getProperty("auditactivity", "");
        String auditsignedflag = actionProps.getProperty("auditsignedflag", "N");
        this.validateInput("sdcid", sdcid);
        this.validateInput("keyid1", keyid1);
        this.validateInput("destination", destination);
        keyid1 = StringUtil.replaceAll(keyid1, "%3B", ";");
        if (OpalUtil.isNotEmpty(transitlocation) && !this.validateTransitLocation(transitlocation)) {
            throw new SapphireException(tp.translate("CDT Validation failed"), "VALIDATION", tp.translate("Unable to perform CDT transfer") + "<br>" + tp.translate("No space available in transit location") + " " + transitlocation);
        }
        String defauldepartmentid = this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment();
        if (StringUtil.getLen(defauldepartmentid) == 0L) {
            throw new SapphireException(tp.translate("CDT Validation failed"), "VALIDATION", tp.translate("Unable to perform CDT transfer") + "<br>" + tp.translate("User does not have a default department"));
        }
        if ("Sample".equals(sdcid) && this.connectionInfo.hasModule("SMS") && !this.validateSamples(keyid1)) {
            throw new SapphireException(tp.translate("Invalid Sample Status"), "VALIDATION", tp.translate("Unable to perform CDT transfer on sample(s)") + "<br>-" + keyid1 + "<br>" + tp.translate("Samples must have one of the following status") + "<br>1. " + "Received" + "<br>2. " + "In Circulation" + "<br>3. " + "Temporary In Lab");
        }
        try {
            this.validateCDTPacking(sdcid, keyid1);
        }
        catch (SapphireException e) {
            throw new SapphireException(tp.translate("CDT Validation failed"), "VALIDATION", e.getMessage());
        }
        try {
            ActionProcessor actionProcessor = this.getActionProcessor();
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Package");
            props.setProperty("packagetype", "CDT");
            props.setProperty("packagestatus", "Created");
            props.setProperty("recipientdepartmentid", destination);
            props.setProperty("notes", actionProps.getProperty("notes"));
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditsignedflag", auditsignedflag);
            props.setProperty("__sdcruleconfirm", sdcruleconfirm);
            actionProcessor.processActionClass(AddSDI.class.getName(), props);
            ErrorHandler errorHandler = actionProcessor.getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(actionProcessor.getErrorHandler());
            }
            String packageid = props.getProperty("newkeyid1");
            props.clear();
            props.setProperty("createsu", "Y");
            props.setProperty("newkeyid1", packageid);
            props.setProperty("linksdcid", "LV_Package");
            props.setProperty("linkpropnodeid", "No Layout|Package");
            props.setProperty("nodeid", "Package");
            props.setProperty("propertytreeid", "No Layout");
            props.setProperty("size", "1");
            props.setProperty("maxtiallowed", "-1");
            props.setProperty("moveableflag", "Y");
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditsignedflag", auditsignedflag);
            props.setProperty("__sdcruleconfirm", sdcruleconfirm);
            actionProcessor.processActionClass(CreateStorageUnit.class.getName(), props);
            errorHandler = actionProcessor.getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(actionProcessor.getErrorHandler());
            }
            String storageunitid = props.getProperty("storageunitid");
            props.clear();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            props.setProperty("keyid2", keyid2);
            props.setProperty("keyid3", keyid3);
            props.setProperty("currentstorageunitid", storageunitid);
            props.setProperty("custodialuserid", this.connectionInfo.getSysuserId());
            props.setProperty("custodytakendt", "n");
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditsignedflag", auditsignedflag);
            props.setProperty("__sdcruleconfirm", sdcruleconfirm);
            actionProcessor.processActionClass(EditTrackItem.class.getName(), props);
            errorHandler = actionProcessor.getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(actionProcessor.getErrorHandler());
            }
            props.clear();
            props.setProperty("sdcid", "LV_Package");
            props.setProperty("keyid1", packageid);
            props.setProperty("packagestatus", "Shipped");
            props.setProperty("auditreason", auditreason);
            props.setProperty("auditactivity", auditactivity);
            props.setProperty("auditsignedflag", auditsignedflag);
            props.setProperty("__sdcruleconfirm", sdcruleconfirm);
            actionProcessor.processActionClass(EditSDI.class.getName(), props);
            errorHandler = actionProcessor.getErrorHandler();
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setErrors(actionProcessor.getErrorHandler());
            }
            if ("D".equals(this.getSDCProcessor().getProperty(sdcid, "accesscontrolledflag"))) {
                int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                props.clear();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", keyid1);
                if (keycolumns > 1) {
                    props.setProperty("keyid2", keyid2);
                    if (keycolumns > 2) {
                        props.setProperty("keyid3", keyid3);
                    }
                }
                props.setProperty("departmentid", destination);
                props.setProperty("operationid", "list");
                this.getActionProcessor().processActionClass(AddSDISecurityDept.class.getName(), props);
            }
            actionProps.setProperty("newkeyid1", packageid);
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
    }

    private boolean validateTransitLocation(String transitlocation) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select spaceavailflag from storageunit where linksdcid = 'PhysicalStore' and linkkeyid1 = ?", (Object[])new String[]{transitlocation});
        return ds != null && ds.size() > 0 && "Y".equals(ds.getValue(0, "spaceavailflag"));
    }

    private void validateInput(String propertyid, String propertyvalue) throws ActionException {
        if (StringUtil.getLen(propertyvalue) == 0L) {
            throw new ActionException("No value found for input property " + propertyid);
        }
    }

    private void validateCDTPacking(String sdcid, String keyid1) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select trackitemid from trackitem where linksdcid = " + safeSQL.addVar(sdcid) + " and linkkeyid1 in (" + safeSQL.addIn(keyid1, ";") + ")";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            ValidateCDTPacking.validateCDTPacking(this.getQueryProcessor(), this.getTranslationProcessor(), ds.getColumnValues("trackitemid", ";"), this.connectionInfo.getSysuserId());
        }
    }

    private boolean validateSamples(String sampleid) {
        boolean validation = true;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select storagestatus from s_sample where s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ")";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String storagestatus = ds.getValue(i, "storagestatus");
                if ("In Circulation".equals(storagestatus) || "Temporary In Lab".equals(storagestatus) || "Received".equals(storagestatus)) continue;
                validation = false;
                break;
            }
        }
        return validation;
    }
}


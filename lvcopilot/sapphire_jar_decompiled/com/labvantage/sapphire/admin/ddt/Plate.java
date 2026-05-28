/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.admin.ddt.rules.DisposeRule;
import com.labvantage.sapphire.admin.ddt.rules.PlateStateRule;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class Plate
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53966 $";
    public static final String SDC_PLATE = "Plate";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) {
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (this.connectionInfo.hasModule("ASL") || this.connectionInfo.hasModule("SMS")) {
            this.addPlateTrackItem(primary, actionProps);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__securitydepartmentedit"))) {
            return;
        }
        DataSet primary = sdiData.getDataset("primary");
        boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        if (this.connectionInfo.hasModule("ASL")) {
            this.checkPlateStateRule(primary, forceUpdate);
        }
        this.checkPlateDisposeRule(primary);
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        try {
            PropertyList props;
            DataSet ds;
            StringBuffer sql = new StringBuffer();
            if (!"Y".equals(actionProps.getProperty("__sudeleteflag"))) {
                sql.append("select storageunitid from storageunit");
                sql.append(" where linksdcid = '").append(SDC_PLATE).append("'");
                sql.append(" and linkkeyid1 in ( select r.keyid1 from rsetitems r where r.rsetid = '").append(rsetid).append("' )");
                ds = this.getQueryProcessor().getSqlDataSet(sql.toString());
                if (ds != null && ds.size() > 0) {
                    props = new PropertyList();
                    props.setProperty("sdcid", "StorageUnitSDC");
                    props.setProperty("keyid1", ds.getColumnValues("storageunitid", ";"));
                    props.setProperty("_deletelinksdi", "N");
                    this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
                }
            }
            sql.setLength(0);
            sql.append("select trackitemid from trackitem");
            sql.append(" where linksdcid = '").append(SDC_PLATE).append("'");
            sql.append(" and linkkeyid1 in ( select r.keyid1 from rsetitems r where r.rsetid = '").append(rsetid).append("' )");
            ds = this.getQueryProcessor().getSqlDataSet(sql.toString());
            if (ds != null && ds.size() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", ds.getColumnValues("trackitemid", ";"));
                this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
            }
            if (this.getActionProcessor().hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
    }

    private void checkPlateDisposeRule(DataSet primary) throws SapphireException {
        if (primary == null || primary.size() == 0 || !primary.isValidColumn("activeflag")) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        for (int count = 0; count < primary.size(); ++count) {
            String plateid = primary.getValue(count, "s_plateid");
            if (!this.hasPrimaryValueChanged(primary, count, "activeflag") || !"N".equalsIgnoreCase(primary.getValue(count, "activeflag"))) continue;
            sb.append(";").append(plateid);
        }
        if (sb.length() > 0) {
            DisposeRule rule = new DisposeRule(this.database, this.connectionInfo);
            rule.processRule(SDC_PLATE, sb.substring(1));
        }
    }

    private boolean addPlateTrackItem(DataSet primary, PropertyList actionProps) {
        boolean flag = true;
        TranslationProcessor tp = this.getTranslationProcessor();
        String forceUpdate = actionProps.getProperty("__sdcruleconfirm");
        String auditReason = actionProps.getProperty("auditreason");
        String auditActivity = actionProps.getProperty("auditactivity", "");
        String auditSignedFlag = actionProps.getProperty("auditsignedflag", "N");
        int plateCount = 0;
        StringBuffer linkSDCId = new StringBuffer(8 * primary.size());
        StringBuffer linkKeyid1 = new StringBuffer(21 * primary.size());
        for (int count = 0; count < primary.size(); ++count) {
            String keyid1 = primary.getValue(count, "s_plateid");
            if (keyid1.trim().length() <= 0) continue;
            ++plateCount;
            linkSDCId.append(SDC_PLATE).append(";");
            linkKeyid1.append(keyid1).append(";");
        }
        if (plateCount > 0) {
            String currentStorageLocation = actionProps.getProperty("currentstoragelocation");
            PropertyList trackitemProps = new PropertyList();
            trackitemProps.setProperty("sdcid", linkSDCId.substring(0, linkSDCId.length() - 1));
            trackitemProps.setProperty("keyid1", linkKeyid1.substring(0, linkKeyid1.length() - 1));
            trackitemProps.setProperty("numoftrackitems", Integer.toString(plateCount));
            trackitemProps.setProperty("__sdcruleconfirm", forceUpdate);
            trackitemProps.setProperty("auditreason", auditReason);
            trackitemProps.setProperty("auditactivity", auditActivity);
            trackitemProps.setProperty("auditsignedflag", auditSignedFlag);
            trackitemProps.setProperty("location", currentStorageLocation);
            try {
                this.getActionProcessor().processAction("AddTrackItem", "1", trackitemProps);
            }
            catch (Exception ex) {
                flag = false;
                StringBuffer msg = new StringBuffer();
                msg.append("Failed to create trackitems for the new Plates");
                this.setError("Plate Track Item Rule", "VALIDATION", tp.translate(msg.toString()));
            }
        }
        return flag;
    }

    public void checkPlateStateRule(DataSet primary, boolean forceUpdate) {
        PlateStateRule rule = new PlateStateRule(this.database, this.connectionInfo);
        for (int count = 0; count < primary.size(); ++count) {
            String plateid = primary.getValue(count, "s_plateid");
            try {
                rule.processRule(plateid, forceUpdate);
                continue;
            }
            catch (ActionException aex) {
                this.setErrors(aex.getErrorHandler());
                continue;
            }
            catch (SapphireException saphEx) {
                this.setError(rule.getClass().getName(), "VALIDATION", saphEx.getMessage());
            }
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.platform.Configuration;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_DataFileDef
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String datafiledefid = primary.getString(i, "datafiledefid");
            String datafiledefversionid = primary.getString(i, "datafiledefversionid");
            if (!this.isCMTImport()) {
                String sql = "SELECT count(*) FROM MessageType WHERE messagetypeid = ?";
                if (this.getQueryProcessor().getPreparedCount(sql, new Object[]{datafiledefid}) > 0) {
                    throw new SapphireException("Invalid datafiledefid, messagetype already exists.");
                }
                this.createMessageType(datafiledefid, datafiledefversionid);
            }
            primary.setString(i, "coreflag", "N");
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            this.checkCoreType(rsetid, "You cannot delete system data file definitions.");
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            this.checkCoreType(sdiData);
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            this.checkCoreType(sdiData);
        }
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!Configuration.isDevmode(this.connectionInfo.getDatabaseId())) {
            this.checkCoreType(sdiData);
        }
    }

    private void checkCoreType(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (primary != null) {
            for (int i = 0; i < primary.size(); ++i) {
                if (!primary.getString(i, "coreflag", "").equals("Y")) continue;
                throw new SapphireException("You cannot modify 'Core' data file definitions");
            }
        }
    }

    private void checkCoreType(String rsetid, String message) throws SapphireException {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT coreflag FROM datafiledef, rsetitems WHERE datafiledef.datafiledefid = rsetitems.keyid1 AND rsetid = ?", new Object[]{rsetid});
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String coreflag = ds.getString(i, "coreflag", "");
            if (!"Y".equals(coreflag)) continue;
            throw new SapphireException(message);
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String dfdList = actionProps.getProperty("keyid1");
        PropertyList deleteProps = new PropertyList();
        deleteProps.setProperty("sdcid", "LV_MessageType");
        deleteProps.setProperty("keyid1", dfdList);
        this.getActionProcessor().processAction("DeleteSDI", "1", deleteProps);
    }

    private void createMessageType(String datafiledefid, String datafiledefversionid) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_MessageType");
        props.setProperty("keyid1", datafiledefid);
        props.setProperty("directionflag", "I");
        props.setProperty("messageclass", "DATAFILE");
        props.setProperty("messagetypedesc", "Created automatically. DO NOT DELETE");
        props.setProperty("processactionid", "ImportDataFile");
        props.setProperty("processactionversionid", "1");
        props.setProperty("allowreprocessflag", "Y");
        props.setProperty("allowlogflag", "Y");
        props.setProperty("processactionflag", "S");
        props.setProperty("definitionsdcid", "LV_DataFileDef");
        props.setProperty("definitionkeyid1", datafiledefid);
        props.setProperty("definitionkeyid2", datafiledefversionid);
        this.getActionProcessor().processAction("AddSDI", "1", props);
    }
}


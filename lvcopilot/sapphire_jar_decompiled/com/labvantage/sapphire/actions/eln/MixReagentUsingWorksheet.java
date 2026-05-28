/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class MixReagentUsingWorksheet
extends BaseSDIAction
implements sapphire.action.MixReagentUsingWorksheet {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String reagenttypeid = properties.getProperty("reagenttypeid", "");
        String reagenttypeversionid = properties.getProperty("reagenttypeversionid", "");
        String wsTemplateid = "";
        String wsTemplateVersionid = "";
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT worksheetid,worksheetversionid FROM sdiworksheetrule ");
        sql.append(" WHERE sdcid = 'LV_ReagentType'");
        sql.append(" AND keyid1 =").append(safeSQL.addVar(reagenttypeid));
        sql.append(" AND keyid2 =").append(safeSQL.addVar(reagenttypeversionid));
        sql.append(" AND worksheetrule ='default'");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds == null || ds.size() <= 0) {
            throw new SapphireException("Worksheet Template is not defined in Consumable Type");
        }
        wsTemplateid = ds.getValue(0, "worksheetid");
        wsTemplateVersionid = BaseELNAction.resolveVersion(this.getQueryProcessor(), ds.getValue(0, "worksheetid"), ds.getValue(0, "worksheetversionid"), "worksheet");
        this.mixReagentLot(properties);
        String newreagentlot = properties.get("newkeyid1").toString();
        this.generateWorsheet(properties, newreagentlot, wsTemplateid, wsTemplateVersionid);
    }

    private void generateWorsheet(PropertyList properties, String reagentlotid, String wsTemplateid, String wsTemplateVersionid) throws SapphireException {
        PropertyList props = new PropertyList();
        try {
            props.setProperty("reagentlotid", reagentlotid);
            props.setProperty("templateid", wsTemplateid);
            props.setProperty("templateversionid", wsTemplateVersionid);
            this.getActionProcessor().processAction("GenerateReagentWorksheet", "1", props);
            properties.setProperty("worksheetid", props.getProperty("worksheetid"));
        }
        catch (Exception e) {
            throw new SapphireException("Consumable Lot Creation failed");
        }
    }

    private void mixReagentLot(PropertyList properties) throws SapphireException {
        String reagenttypeid = properties.getProperty("reagenttypeid", "");
        String reagenttypeversionid = properties.getProperty("reagenttypeversionid", "");
        String reagentlotdesc = properties.getProperty("reagentlotdesc", "");
        String amountinitial = properties.getProperty("amountinitial", "");
        String amountinitialunits = properties.getProperty("amountinitialunits", "");
        String amountinitialunitstype = properties.getProperty("amountinitialunitstype", "");
        String containertypeid = properties.getProperty("containertypeid", "");
        String amountinitialmax = properties.getProperty("amountinitialmax", "");
        String amountinitialmin = properties.getProperty("amountinitialmin", "");
        String contentflag = properties.getProperty("contentflag", "");
        String reagentclass = properties.getProperty("reagentclass", "");
        String containersinitial = properties.getProperty("containersinitial", properties.containsKey("containersinitial") ? "(null)" : "");
        String purchasedflag = properties.getProperty("purchasedflag", "");
        String managecontainerinventoryflag = properties.getProperty("managecontainerinventoryflag", "");
        String qualitysamplereqflag = properties.getProperty("qualitysamplereqflag", "");
        String trackitemexpiryperiodreqflag = properties.getProperty("trackitemexpiryperiodreqflag", "");
        PropertyList rgntLotPropertyList = new PropertyList();
        try {
            rgntLotPropertyList.setProperty("sdcid", "LV_ReagentLot");
            rgntLotPropertyList.setProperty("reagentlotdesc", reagentlotdesc);
            rgntLotPropertyList.setProperty("reagenttypeid", reagenttypeid);
            rgntLotPropertyList.setProperty("reagenttypeversionid", reagenttypeversionid);
            rgntLotPropertyList.setProperty("amountinitial", amountinitial);
            rgntLotPropertyList.setProperty("amountinitialunits", amountinitialunits);
            rgntLotPropertyList.setProperty("amountinitialunitstype", amountinitialunitstype);
            rgntLotPropertyList.setProperty("containertypeid", containertypeid);
            rgntLotPropertyList.setProperty("amountinitialmax", amountinitialmax);
            rgntLotPropertyList.setProperty("amountinitialmin", amountinitialmin);
            rgntLotPropertyList.setProperty("contentflag", contentflag);
            rgntLotPropertyList.setProperty("reagentclass", reagentclass);
            rgntLotPropertyList.setProperty("containersinitial", containersinitial);
            rgntLotPropertyList.setProperty("purchasedflag", purchasedflag);
            rgntLotPropertyList.setProperty("managecontainerinventoryflag", managecontainerinventoryflag);
            rgntLotPropertyList.setProperty("qualitysamplereqflag", qualitysamplereqflag);
            rgntLotPropertyList.setProperty("trackitemexpiryperiodreqflag", trackitemexpiryperiodreqflag);
            this.getActionProcessor().processAction("AddSDI", "1", rgntLotPropertyList);
            properties.setProperty("newkeyid1", rgntLotPropertyList.get("newkeyid1").toString());
        }
        catch (Exception e) {
            throw new SapphireException("Consumable Lot Creation failed");
        }
    }
}


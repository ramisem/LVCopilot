/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.reagent;

import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ReserveKitLot
extends BaseAction {
    public static final String ID = "ReserveKitLot";
    public static final String VERSION = "1";
    public static final String PROPERTY_KITLOTID = "kitlotid";
    public static final String PROPERTY_REQUESTID = "requestid";
    public static final String PROPERTY_REQUESTITEMID = "requestitemid";
    public static final String PROPERTY_RESERVEQUANTITY = "reservequantity";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String kitlotid = actionProps.getProperty(PROPERTY_KITLOTID);
        String requestid = actionProps.getProperty(PROPERTY_REQUESTID);
        String requestitemid = actionProps.getProperty(PROPERTY_REQUESTITEMID);
        int reservequantity = 0;
        try {
            reservequantity = Integer.parseInt(actionProps.getProperty(PROPERTY_RESERVEQUANTITY, "0"));
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (reservequantity > 0) {
            if (kitlotid.length() == 0) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing action input") + " :" + PROPERTY_KITLOTID);
            }
            if (requestid.length() == 0) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing action input") + " :" + PROPERTY_REQUESTID);
            }
            if (requestitemid.length() == 0) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing action input") + " :" + PROPERTY_REQUESTITEMID);
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid from trackitem where linksdcid = 'LV_ReagentLot' and linkkeyid1 = ? and trackitemstatus  = 'Valid' order by trackitemid", (Object[])new String[]{kitlotid});
            if (ds.size() > 0) {
                String trackitemid = "";
                if (reservequantity > 0 && reservequantity < ds.size()) {
                    DataSet d = new DataSet();
                    for (int i = 0; i < reservequantity; ++i) {
                        d.copyRow(ds, i, 1);
                    }
                    trackitemid = d.getColumnValues("trackitemid", ";");
                } else {
                    trackitemid = ds.getColumnValues("trackitemid", ";");
                }
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_RequestItemDetail");
                props.setProperty(PROPERTY_REQUESTID, requestid);
                props.setProperty(PROPERTY_REQUESTITEMID, requestitemid);
                props.setProperty("linksdcid", "TrackItemSDC");
                props.setProperty("linkkeyid1", trackitemid);
                props.setProperty("copies", String.valueOf(ds.size()));
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                props.clear();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", trackitemid);
                props.setProperty("trackitemstatus", "Reserved");
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
    }
}


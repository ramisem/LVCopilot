/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_ProductInstrument
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77330 $";
    public static final String SDCID = "LV_ProductInstrument";
    public static final String TABLEID = "s_productinstrument";
    public static final String COLUMN_KEYID1 = "s_productid";
    public static final String COLUMN_KEYID2 = "s_productversionid";
    public static final String COLUMN_KEYID3 = "s_productinstrumentid";
    public static final String COLUMN_INSTRUMENTMODELID = "instrumentmodelid";
    public static final String COLUMN_INSTRUMENTTYPEID = "instrumenttypeid";
    public static final String COLUMN_INSTRUMENTID = "instrumentid";
    public static final String COLUMN_PRODUCTSTAGEID = "productstageid";
    public static final String COLUMN_PRODUCTINSTRUMENTDESC = "productinstrumentdesc";

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        if (!"Y".equalsIgnoreCase(actionProps.getProperty("overrideautokey"))) {
            this.generateKeys(primary);
        }
    }

    private void generateKeys(DataSet primary) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String now = sdf.format(cal.getTime());
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String keyid1 = primary.getValue(i, COLUMN_KEYID1, "");
            String keyid2 = primary.getValue(i, COLUMN_KEYID2, "");
            String keyid3 = primary.getValue(i, COLUMN_KEYID3, "");
            int sequence = this.getSequenceProcessor().getSequence(SDCID, keyid1 + keyid2);
            if (keyid3.length() != 0) continue;
            String id = "PI-" + now + "-" + sequence;
            primary.setString(i, COLUMN_KEYID3, id);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet dsPrimary = sdiData.getDataset("primary");
        boolean isPromoteOperation = actionProps.getProperty("operation", "").equals("promoteformulation");
        if (!isPromoteOperation) {
            this.callRedoCalcOnProduct(dsPrimary);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet dsPrimary = sdiData.getDataset("primary");
        this.callRedoCalcOnProduct(dsPrimary);
    }

    private void callRedoCalcOnProduct(DataSet dsPrimary) throws SapphireException {
        DataSet dsRedo = new DataSet();
        PreparedStatement checkProduct = this.database.prepareStatement("checkProduct", "select 1 from s_product  where s_productid = ? and s_productversionid = ? and formulationiterationflag = 'Y' and templateflag != 'Y'");
        try {
            for (int i = 0; i < dsPrimary.getRowCount(); ++i) {
                if (!this.hasPrimaryValueChanged(dsPrimary, i, "cost")) continue;
                String productId = dsPrimary.getString(i, COLUMN_KEYID1);
                String productVersionId = dsPrimary.getString(i, COLUMN_KEYID2);
                checkProduct.setString(1, productId);
                checkProduct.setString(2, productVersionId);
                ResultSet rs = checkProduct.executeQuery();
                if (!rs.next()) continue;
                dsRedo.copyRow(dsPrimary, i, 1);
            }
            if (dsRedo.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Product");
                props.setProperty("keyid1", dsRedo.getColumnValues(COLUMN_KEYID1, ";"));
                props.setProperty("keyid2", dsRedo.getColumnValues(COLUMN_KEYID2, ";"));
                this.getActionProcessor().processAction("RedoCalculations", "1", props);
            }
        }
        catch (Exception e) {
            this.logger.error("Error in executing callRedoCalcOnProduct method:", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("checkProduct");
        }
    }
}


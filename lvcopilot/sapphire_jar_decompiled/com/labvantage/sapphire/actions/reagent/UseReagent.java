/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.reagent;

import java.math.BigDecimal;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class UseReagent
extends BaseAction
implements sapphire.action.UseReagent {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54053 $";
    private static final String PROPERTY_AMOUNTUNITSTYPE = "amountunitstype";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String separator = properties.getProperty("separator", ";");
        String sdcId = properties.getProperty("sdcid", "");
        if (sdcId.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "VALIDATION", "SDCID not specified");
        }
        String keyId1 = properties.getProperty("keyid1", "");
        if (keyId1.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "VALIDATION", "KeyId1 not specified");
        }
        String paramListId = properties.getProperty("paramlistid", "");
        String paramListVersionId = properties.getProperty("paramlistversionid", "");
        String variantId = properties.getProperty("variantid", "");
        String dataset = properties.getProperty("dataset", "");
        if (paramListId.length() == 0 || paramListVersionId.length() == 0 || variantId.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "VALIDATION", "ParamList details not specified");
        }
        DataSet inputData = new DataSet();
        inputData.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), separator);
        inputData.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), separator);
        inputData.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), separator, "(null)");
        inputData.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), separator, "(null)");
        inputData.addColumnValues("paramlistid", 0, properties.getProperty("paramlistid"), separator);
        inputData.addColumnValues("paramlistversionid", 0, properties.getProperty("paramlistversionid"), separator);
        inputData.addColumnValues("variantid", 0, properties.getProperty("variantid"), separator);
        inputData.addColumnValues("dataset", 0, properties.getProperty("dataset"), separator);
        inputData.addColumnValues("relationid", 0, properties.getProperty("relationid"), separator);
        inputData.addColumnValues("reagenttypeid", 0, properties.getProperty("reagenttypeid"), separator);
        inputData.addColumnValues("reagentlotid", 0, properties.getProperty("reagentlotid"), separator);
        inputData.addColumnValues("trackitemid", 0, properties.getProperty("trackitemid"), separator);
        inputData.addColumnValues("amount", 1, properties.getProperty("amount"), separator);
        inputData.addColumnValues("amountunits", 0, properties.getProperty("amountunits"), separator);
        inputData.padColumn("sdcid");
        inputData.padColumn("keyid1");
        inputData.padColumn("keyid2");
        inputData.padColumn("keyid3");
        DataSet addRelation = new DataSet();
        DataSet editRelation = new DataSet();
        DataSet trackItemData = new DataSet();
        this.prepareDataSets(inputData, addRelation, editRelation, trackItemData);
        if (addRelation != null && addRelation.size() > 0) {
            this.logger.info("AddRelation: \n" + addRelation.toString());
            this.getActionProcessor().processAction("AddSDIDataRelation", "1", this.getActionProps(addRelation, "Add"));
        }
        if (editRelation != null && editRelation.size() > 0) {
            this.logger.info("EditRelation: \n" + editRelation.toString());
            this.getActionProcessor().processAction("EditSDIDataRelation", "1", this.getActionProps(editRelation, "Edit"));
        }
        if (trackItemData != null && trackItemData.size() > 0) {
            this.logger.info("TrackItemData: \n" + trackItemData.toString());
            this.getActionProcessor().processAction("AdjustTrackItemInv", "1", this.getTrackItemProps(trackItemData));
        }
    }

    private void prepareDataSets(DataSet inputData, DataSet addRelation, DataSet editRelation, DataSet trackItemData) {
        int newRow;
        int rowCount = inputData.getRowCount();
        for (int i = 0; i < rowCount; ++i) {
            DataSet holderDS;
            String units = inputData.getString(i, "amountunits", "");
            if ("(containers)".equalsIgnoreCase(units)) {
                inputData.setString(i, PROPERTY_AMOUNTUNITSTYPE, "C");
                inputData.setString(i, "amountunits", "");
            } else {
                inputData.setString(i, PROPERTY_AMOUNTUNITSTYPE, "U");
            }
            String relationId = inputData.getString(i, "relationid", "");
            if (relationId.length() == 0) {
                holderDS = addRelation;
                newRow = holderDS.addRow();
            } else {
                holderDS = editRelation;
                newRow = holderDS.addRow();
                holderDS.setString(newRow, "relationid", relationId);
            }
            this.populateDataSet(holderDS, newRow, inputData, i);
        }
        DataSet oldAmounts = null;
        if (editRelation.getRowCount() > 0) {
            oldAmounts = this.getExistingAmount(editRelation.getColumnValues("relationid", ";"));
        }
        for (int i = 0; i < inputData.getRowCount(); ++i) {
            double adjustAmount;
            String relationId = inputData.getString(i, "relationid", "");
            String trackItemId = inputData.getString(i, "trackitemid", "");
            double newAmount = inputData.getBigDecimal(i, "amount", new BigDecimal(0)).doubleValue();
            String amountUnits = inputData.getString(i, "amountunits", "");
            String amountUnitsType = inputData.getString(i, PROPERTY_AMOUNTUNITSTYPE, "");
            if (relationId.length() == 0) {
                adjustAmount = -newAmount;
            } else {
                int oldAmountRow = oldAmounts.findRow("relationid", relationId);
                double oldAmount = oldAmountRow > -1 ? oldAmounts.getBigDecimal(oldAmountRow, "amount", new BigDecimal(0)).doubleValue() : 0.0;
                adjustAmount = oldAmount - newAmount;
            }
            if (trackItemId.length() <= 0 || adjustAmount == 0.0) continue;
            newRow = trackItemData.addRow();
            trackItemData.setString(newRow, "trackitemid", trackItemId);
            trackItemData.setNumber(newRow, "quantity", adjustAmount);
            trackItemData.setString(newRow, "quantityunit", amountUnits);
            trackItemData.setString(newRow, "quantitytype", amountUnitsType);
        }
    }

    private void populateDataSet(DataSet target, int targetRow, DataSet source, int sourceRow) {
        target.setString(targetRow, "sdcid", source.getString(sourceRow, "sdcid"));
        target.setString(targetRow, "keyid1", source.getString(sourceRow, "keyid1"));
        target.setString(targetRow, "keyid2", source.getString(sourceRow, "keyid2"));
        target.setString(targetRow, "keyid3", source.getString(sourceRow, "keyid3"));
        target.setString(targetRow, "paramlistid", source.getString(sourceRow, "paramlistid"));
        target.setString(targetRow, "paramlistversionid", source.getString(sourceRow, "paramlistversionid"));
        target.setString(targetRow, "variantid", source.getString(sourceRow, "variantid"));
        target.setString(targetRow, "dataset", source.getString(sourceRow, "dataset"));
        target.setString(targetRow, "relationtype", source.getString(sourceRow, "reagenttypeid"));
        target.setString(targetRow, "relationfunction", "Reagent");
        target.setString(targetRow, "tosdcid", "LV_ReagentLot");
        target.setString(targetRow, "tokeyid1", source.getString(sourceRow, "reagentlotid"));
        target.setString(targetRow, "tokeyid2", "(null)");
        target.setString(targetRow, "tokeyid3", "(null)");
        target.setString(targetRow, "refsdcid", "TrackItemSDC");
        target.setString(targetRow, "refkeyid1", source.getString(sourceRow, "trackitemid"));
        target.setString(targetRow, "refkeyid2", "(null)");
        target.setString(targetRow, "refkeyid3", "(null)");
        target.setNumber(targetRow, "amount", source.getBigDecimal(sourceRow, "amount"));
        target.setString(targetRow, "amountunits", source.getString(sourceRow, "amountunits"));
        target.setString(targetRow, PROPERTY_AMOUNTUNITSTYPE, source.getString(sourceRow, PROPERTY_AMOUNTUNITSTYPE));
    }

    private DataSet getExistingAmount(String relationIds) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT relationid, amount, amountunits, amountunitstype FROM sdidatarelation").append(" WHERE relationid in (").append(safeSQL.addIn(relationIds, ";")).append(")");
        DataSet oldAmounts = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        return oldAmounts;
    }

    private PropertyList getTrackItemProps(DataSet ds) {
        PropertyList props = new PropertyList();
        props.setProperty("trackitemid", ds.getColumnValues("trackitemid", ";"));
        props.setProperty("quantity", ds.getColumnValues("quantity", ";"));
        props.setProperty("quantityunit", ds.getColumnValues("quantityunit", ";"));
        props.setProperty("quantitytype", ds.getColumnValues("quantitytype", ";"));
        return props;
    }

    private PropertyList getActionProps(DataSet ds, String mode) {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", ds.getValue(0, "sdcid"));
        props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
        props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
        props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
        props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
        props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
        props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
        props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
        if ("edit".equalsIgnoreCase(mode)) {
            props.setProperty("relationid", ds.getColumnValues("relationid", ";"));
        }
        props.setProperty("relationfunction", ds.getColumnValues("relationfunction", ";"));
        props.setProperty("relationtype", ds.getColumnValues("relationtype", ";"));
        props.setProperty("tosdcid", ds.getColumnValues("tosdcid", ";"));
        props.setProperty("tokeyid1", ds.getColumnValues("tokeyid1", ";"));
        props.setProperty("tokeyid2", ds.getColumnValues("tokeyid2", ";"));
        props.setProperty("tokeyid3", ds.getColumnValues("tokeyid3", ";"));
        props.setProperty("refsdcid", ds.getColumnValues("refsdcid", ";"));
        props.setProperty("refkeyid1", ds.getColumnValues("refkeyid1", ";"));
        props.setProperty("refkeyid2", ds.getColumnValues("refkeyid2", ";"));
        props.setProperty("refkeyid3", ds.getColumnValues("refkeyid3", ";"));
        props.setProperty("amount", ds.getColumnValues("amount", ";"));
        props.setProperty("amountunits", ds.getColumnValues("amountunits", ";"));
        props.setProperty(PROPERTY_AMOUNTUNITSTYPE, ds.getColumnValues(PROPERTY_AMOUNTUNITSTYPE, ";"));
        return props;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class LV_ProductIngredient
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 99485 $";
    public static final String SDCID = "LV_ProductIngredient";
    public static final String TABLEID = "s_productformulation";
    public static final String COLUMN_KEYID1 = "s_productid";
    public static final String COLUMN_KEYID2 = "s_productversionid";
    public static final String COLUMN_KEYID3 = "s_productitemid";
    public static final String COLUMN_PARENTPRODUCTID = "parentproductid";
    public static final String COLUMN_PARENTPRODUCTVERSIONID = "parentproductversionid";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_AMOUNTTEXT = "amounttext";
    public static final String COLUMN_AMOUNTUNITS = "amountunits";
    private static final String[] amountDataItemParamTypes = new String[]{"Part", "Quantity"};

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        if (!"Y".equalsIgnoreCase(actionProps.getProperty("overrideautokey"))) {
            this.generateKeys(primary);
        }
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String templateKeyId1 = actionProps.getProperty("templatekeyid1", "");
        boolean isPromoteOperation = actionProps.getProperty("operation", "").equals("promoteformulation");
        DataSet primary = sdiData.getDataset("primary");
        this.setNullToCurrentProductVersion(primary);
        if (templateKeyId1.length() > 0) {
            String templateKeyId2 = actionProps.getProperty("templatekeyid2", "");
            String templateKeyId3 = actionProps.getProperty("templatekeyid3", "");
            if (isPromoteOperation) {
                this.copyAmountFields(sdiData, templateKeyId1, templateKeyId2, templateKeyId3);
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String templateKeyId1;
        DataSet primary = sdiData.getDataset("primary");
        boolean isPromoteOperation = actionProps.getProperty("operation", "").equals("promoteformulation");
        if (actionProps.getProperty("donotcopyformulationmethoddatasets", "N").equals("N") && !isPromoteOperation) {
            this.createFormulationDataSets(primary);
        }
        if ((templateKeyId1 = actionProps.getProperty("templatekeyid1", "")).length() > 0) {
            String templateKeyId2 = actionProps.getProperty("templatekeyid2", "");
            String templateKeyId3 = actionProps.getProperty("templatekeyid3", "");
            this.copyResults(sdiData, templateKeyId1, templateKeyId2, templateKeyId3, isPromoteOperation);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setNullToCurrentProductVersion(primary);
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

    private void setNullToCurrentProductVersion(DataSet primary) {
        for (int i = 0; i < primary.size(); ++i) {
            String prodVersion;
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_PARENTPRODUCTVERSIONID) || !"C".equalsIgnoreCase(prodVersion = primary.getValue(i, COLUMN_PARENTPRODUCTVERSIONID))) continue;
            primary.setValue(i, COLUMN_PARENTPRODUCTVERSIONID, null);
        }
    }

    private void createFormulationDataSets(DataSet primary) throws SapphireException {
        DataSet dsProps = new DataSet();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String productId = primary.getValue(i, COLUMN_KEYID1);
            String productVerId = primary.getValue(i, COLUMN_KEYID2);
            String productItemId = primary.getValue(i, COLUMN_KEYID3);
            this.database.createPreparedResultSet("getingredparamlist", "select pl.paramlistid, pl.paramlistversionid, pl.variantid, pl.targetuse  from formulationmethodparamlist pl,formulationprojectmethod fm, s_product p  where pl.formulationmethodid = fm.formulationmethodid and pl.formulationmethodversionid = fm.formulationmethodversionid and fm.formulationprojectid = p.formulationprojectid  and p.s_productid = ? and p.s_productversionid = ? and p.templateflag != 'Y' and pl.targetuse = 'I'", new String[]{productId, productVerId});
            DataSet ds = new DataSet(this.database.getResultSet("getingredparamlist"));
            if (ds.getRowCount() <= 0) continue;
            for (int k = 0; k < ds.getRowCount(); ++k) {
                int r = dsProps.addRow();
                dsProps.setString(r, "keyid1", productId);
                dsProps.setString(r, "keyid2", productVerId);
                dsProps.setString(r, "keyid3", productItemId);
                dsProps.setString(r, "paramlistid", ds.getValue(k, "paramlistid"));
                dsProps.setString(r, "paramlistversionid", ds.getValue(k, "paramlistversionid"));
                dsProps.setString(r, "variantid", ds.getValue(k, "variantid"));
            }
        }
        if (dsProps.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", dsProps.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", dsProps.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", dsProps.getColumnValues("keyid3", ";"));
            props.setProperty("paramlistid", dsProps.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", dsProps.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", dsProps.getColumnValues("variantid", ";"));
            props.setProperty("addnewonly", "Y");
            props.setProperty("propsmatch", "Y");
            this.getActionProcessor().processAction("AddDataSet", "1", props);
        }
    }

    private void copyAmountFields(SDIData sdiData, String templateKeyId1, String templateKeyId2, String templateKeyId3) throws ActionException {
        DataSet primary = sdiData.getDataset("primary");
        SDIRequest templateRequest = new SDIRequest();
        templateRequest.setSDCid(SDCID);
        templateRequest.setKeyid1List(templateKeyId1);
        templateRequest.setKeyid2List(templateKeyId2);
        templateRequest.setKeyid3List(templateKeyId3);
        templateRequest.setRequestItem("dataitem");
        SDIData templateData = this.getSDIProcessor().getSDIData(templateRequest);
        DataSet templateDataItems = templateData.getDataset("dataitem");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            for (int j = 0; j < amountDataItemParamTypes.length; ++j) {
                HashMap<String, String> find = new HashMap<String, String>();
                find.put("paramtype", amountDataItemParamTypes[j]);
                int index = templateDataItems.findRow(find);
                if (index < 0) continue;
                String transformValue = templateDataItems.getValue(index, "transformvalue", "");
                String displayValue = templateDataItems.getValue(index, "displayvalue", "");
                String displayUnits = templateDataItems.getValue(index, "displayunits", "");
                primary.setValue(i, COLUMN_AMOUNT, transformValue);
                primary.setValue(i, COLUMN_AMOUNTTEXT, displayValue);
                primary.setValue(i, COLUMN_AMOUNTUNITS, displayUnits);
            }
        }
    }

    private void copyResults(SDIData sdiData, String templateKeyId1, String templateKeyId2, String templateKeyId3, boolean isPromoteOperation) throws ActionException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet dataItem = sdiData.getDataset("dataitem");
        SDIRequest templateRequest = new SDIRequest();
        templateRequest.setSDCid(SDCID);
        templateRequest.setKeyid1List(templateKeyId1);
        templateRequest.setKeyid2List(templateKeyId2);
        templateRequest.setKeyid3List(templateKeyId3);
        templateRequest.setRequestItem("dataitem");
        SDIData templateData = this.getSDIProcessor().getSDIData(templateRequest);
        DataSet templateDataItems = templateData.getDataset("dataitem");
        boolean callEnterDataItem = false;
        for (int i = 0; i < primary.getRowCount(); ++i) {
            for (int j = 0; j < dataItem.getRowCount(); ++j) {
                String paramDataType = dataItem.getValue(j, "datatypes", "");
                if (!paramDataType.equals("NC")) {
                    HashMap<String, Object> find = new HashMap<String, Object>();
                    find.put("paramlistid", dataItem.getValue(j, "paramlistid"));
                    find.put("paramlistversionid", dataItem.getValue(j, "paramlistversionid"));
                    find.put("variantid", dataItem.getValue(j, "variantid"));
                    find.put("dataset", dataItem.getBigDecimal(j, "dataset"));
                    find.put("paramid", dataItem.getValue(j, "paramid"));
                    find.put("paramtype", dataItem.getValue(j, "paramtype"));
                    find.put("replicateid", dataItem.getBigDecimal(j, "replicateid"));
                    int index = templateDataItems.findRow(find);
                    if (index < 0) continue;
                    String enteredText = templateDataItems.getValue(index, "enteredtext", "");
                    if (enteredText.length() > 0) {
                        dataItem.setValue(j, "enteredtext", enteredText);
                        callEnterDataItem = true;
                        continue;
                    }
                    dataItem.setValue(j, "enteredtext", "");
                    continue;
                }
                dataItem.setValue(j, "enteredtext", "");
            }
        }
        if (callEnterDataItem) {
            HashMap<String, String> find = new HashMap<String, String>();
            find.put("enteredtext", "");
            DataSet dataEnteredDataSet = dataItem.getFilteredDataSet(find, true);
            if (dataEnteredDataSet.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDCID);
                props.setProperty("keyid1", dataEnteredDataSet.getColumnValues("keyid1", ";"));
                props.setProperty("keyid2", dataEnteredDataSet.getColumnValues("keyid2", ";"));
                props.setProperty("keyid3", dataEnteredDataSet.getColumnValues("keyid3", ";"));
                props.setProperty("paramlistid", dataEnteredDataSet.getColumnValues("paramlistid", ";"));
                props.setProperty("paramlistversionid", dataEnteredDataSet.getColumnValues("paramlistversionid", ";"));
                props.setProperty("variantid", dataEnteredDataSet.getColumnValues("variantid", ";"));
                props.setProperty("dataset", dataEnteredDataSet.getColumnValues("dataset", ";"));
                props.setProperty("paramid", dataEnteredDataSet.getColumnValues("paramid", ";"));
                props.setProperty("paramtype", dataEnteredDataSet.getColumnValues("paramtype", ";"));
                props.setProperty("replicateid", dataEnteredDataSet.getColumnValues("replicateid", ";"));
                props.setProperty("enteredtext", dataEnteredDataSet.getColumnValues("enteredtext", ";"));
                props.setProperty("propsmatch", "Y");
                if (isPromoteOperation) {
                    props.setProperty("ispromotoperation", "Y");
                }
                this.getActionProcessor().processAction("EnterDataItem", "1", props);
            }
        }
    }

    @Override
    public boolean requiresDataEntryPrimary() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}


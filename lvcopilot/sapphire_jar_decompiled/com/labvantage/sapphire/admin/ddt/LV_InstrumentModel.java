/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.admin.ddt.InstrumentUtil;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_InstrumentModel
extends BaseSDCRules {
    protected final String cFNS = "{fn concat(";
    protected final String cFNE = ")}";
    protected String specialDelimer = "^^^";

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.deleteCaptureOperation(rsetid);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String templateKeyId = actionProps.getProperty("templateid", actionProps.getProperty("templatekeyid1", ""));
        if (templateKeyId.length() > 0) {
            InstrumentUtil.copyCaptureOperationPropertiesFromTemplate(primary, actionProps, "LV_InstrumentModel", this.getQueryProcessor(), this.database);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.syncInstrumentModelAndInstrumentsForSDMS(primary);
        String[] cols = new String[]{"instrumentmodelid", "collectorpropertytreeid", "collectorextendnodeid", "collectorvaluetree"};
        HashSet<String> checkInstruments = new HashSet<String>();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            for (String columnid : cols) {
                if (!this.hasPrimaryValueChanged(primary, i, columnid)) continue;
                checkInstruments.add(primary.getValue(i, "instrumentid"));
            }
        }
        if (checkInstruments.size() > 0) {
            PropertyList checkRebootProps = new PropertyList();
            checkRebootProps.setProperty("instrumentid", String.join((CharSequence)";", checkInstruments));
            this.getActionProcessor().processAction("CheckSDMSRebootFlag", "1", checkRebootProps);
        }
    }

    private void syncInstrumentModelAndInstrumentsForSDMS(DataSet primary) throws SapphireException {
        DataSet editInstrument = new DataSet();
        editInstrument.addColumn("instrumentid", 0);
        editInstrument.addColumn("collectorpropertytreeid", 0);
        editInstrument.addColumn("collectorextendnodeid", 0);
        editInstrument.addColumn("postponeprocessingflag", 0);
        DataSet instrumentDS = this.getInstruments(primary);
        HashMap<String, String> hm = new HashMap<String, String>();
        if (instrumentDS != null && instrumentDS.size() > 0) {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "collectorpropertytreeid") && !this.hasPrimaryValueChanged(primary, i, "collectorextendnodeid") && !this.hasPrimaryValueChanged(primary, i, "postponeprocessingflag")) continue;
                hm.clear();
                hm.put("instrumentmodelid", primary.getString(i, "instrumentmodelid", ""));
                hm.put("instrumenttype", primary.getString(i, "instrumenttypeid", ""));
                DataSet instDS = instrumentDS.getFilteredDataSet(hm);
                if (instDS == null || instDS.size() <= 0) continue;
                for (int insRow = 0; insRow < instDS.getRowCount(); ++insRow) {
                    int rowid = editInstrument.addRow();
                    editInstrument.setString(rowid, "instrumentid", instDS.getString(insRow, "instrumentid", ""));
                    editInstrument.setString(rowid, "collectorpropertytreeid", this.getCorrectValue(primary, i, "collectorpropertytreeid"));
                    editInstrument.setString(rowid, "collectorextendnodeid", this.getCorrectValue(primary, i, "collectorextendnodeid"));
                    editInstrument.setString(rowid, "postponeprocessingflag", this.getCorrectValue(primary, i, "postponeprocessingflag"));
                }
            }
        }
        if (editInstrument.size() > 0) {
            ActionProcessor ap = this.getActionProcessor();
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Instrument");
            props.setProperty("keyid1", editInstrument.getColumnValues("instrumentid", ";"));
            props.setProperty("collectorpropertytreeid", editInstrument.getColumnValues("collectorpropertytreeid", ";"));
            props.setProperty("collectorextendnodeid", editInstrument.getColumnValues("collectorextendnodeid", ";"));
            props.setProperty("postponeprocessingflag", editInstrument.getColumnValues("postponeprocessingflag", ";"));
            ap.processAction("EditSDI", "1", props);
        }
    }

    private DataSet getInstruments(DataSet primary) {
        DataSet ds = new DataSet();
        if (primary.size() > 0) {
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select instrumentid,instrumentmodelid,instrumenttype");
            sql.append(" from instrument");
            sql.append(" where ").append(this.concatFields("instrumentmodelid", "instrumenttype")).append(" IN (" + safeSQL.addIn(this.getSQLWhereClauseforInstrument(primary), ";") + ")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        return ds;
    }

    private String getSQLWhereClauseforInstrument(DataSet primary) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String value = primary.getString(i, "instrumentmodelid", "") + this.specialDelimer + primary.getString(i, "instrumenttypeid", "");
            str.append(i == 0 ? value : ";" + value);
        }
        return str.toString();
    }

    private String getCorrectValue(DataSet primary, int rowid, String columnid) {
        if (primary.isValidColumn(columnid)) {
            return primary.getString(rowid, columnid, "");
        }
        return this.getOldPrimaryValue(primary, rowid, columnid);
    }

    private void deleteCaptureOperation(String rsetid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sdiCOSql = new StringBuffer();
        sdiCOSql.append("DELETE from sdiattachmentoperation ");
        sdiCOSql.append(" where sdcid='LV_InstrumentModel'");
        sdiCOSql.append(" and ").append(this.concatFields("keyid1", "keyid2"));
        sdiCOSql.append(" IN ( SELECT ").append(this.concatFields("keyid1", "keyid2")).append(" from rsetitems WHERE rsetid = ").append(safeSQL.addVar(rsetid) + " )");
        this.database.executePreparedUpdate(sdiCOSql.toString(), safeSQL.getValues());
    }

    protected String concatFields(String ... fields) {
        String str = "";
        boolean firstItem = true;
        for (String f : fields) {
            if (firstItem) {
                str = this.database.isOracle() ? f : "cast(" + f + " as nvarchar(100))";
                firstItem = false;
                continue;
            }
            str = "{fn concat({fn concat(" + str + ",'" + this.specialDelimer + "'" + ")}" + "," + (this.database.isOracle() ? f : "cast(" + f + " as nvarchar(100))") + ")}";
        }
        return str;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}


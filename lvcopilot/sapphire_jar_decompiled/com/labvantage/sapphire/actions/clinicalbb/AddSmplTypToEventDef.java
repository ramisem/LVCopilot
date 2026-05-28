/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSmplTypToEventDef
extends BaseAction
implements sapphire.action.AddSmplTypToEventDef {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77312 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String eventDefId = properties.getProperty("eventdefid", "");
        String sampleTypeIds = properties.getProperty("sampletypeids", "");
        int rowInd = 0;
        int maxUserSequence = 0;
        Calendar now = Calendar.getInstance();
        TranslationProcessor tp = this.getTranslationProcessor();
        if (eventDefId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("EventDefId not passed."));
        }
        if (sampleTypeIds.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("SampleTypeIds not passed."));
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select max(usersequence) maxus from s_eventdefsampletype where s_eventdefid=" + safeSQL.addVar(eventDefId);
        DataSet userSequenceData = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (userSequenceData != null && userSequenceData.getRowCount() > 0) {
            maxUserSequence = userSequenceData.getInt(0, "maxus", 0);
        }
        String[] arrSampleType = StringUtil.split(sampleTypeIds, ";");
        String sampleTypeInClause = "";
        String sampleTypeId = "";
        String specimenType = "";
        String quantity = "";
        DataSet eventDefSampleTypeData = new DataSet();
        eventDefSampleTypeData.addColumn("s_eventdefid", 0);
        eventDefSampleTypeData.addColumn("s_sampletypeid", 0);
        eventDefSampleTypeData.addColumn("usersequence", 1);
        for (int si = 0; si < arrSampleType.length; ++si) {
            sampleTypeId = arrSampleType[si];
            rowInd = eventDefSampleTypeData.addRow();
            eventDefSampleTypeData.setValue(rowInd, "s_eventdefid", eventDefId);
            eventDefSampleTypeData.setValue(rowInd, "s_sampletypeid", sampleTypeId);
            eventDefSampleTypeData.setValue(rowInd, "usersequence", String.valueOf(++maxUserSequence));
            if (sampleTypeInClause.length() > 0) {
                sampleTypeInClause = sampleTypeInClause + ",";
            }
            sampleTypeInClause = sampleTypeInClause + "'" + arrSampleType[si] + "'";
        }
        if (eventDefSampleTypeData.getRowCount() > 0) {
            Trace.logInfo("AddSmplTypToEventDef", "Adding Sample Types (" + sampleTypeIds + ") to EventDef " + eventDefId);
            eventDefSampleTypeData.setDate(-1, "createdt", now);
            eventDefSampleTypeData.setString(-1, "createtool", this.connectionInfo.getTool());
            eventDefSampleTypeData.setString(-1, "createby", this.connectionInfo.getSysuserId());
            DataSetUtil.insert(this.database, eventDefSampleTypeData, "s_eventdefsampletype");
        }
        maxUserSequence = 0;
        try {
            safeSQL.reset();
            String specimentDefSQL = "SELECT cp.s_sampletypeid, cp.specimentype, cp.arrivalorder FROM s_cpassaytypesampletype cp, s_eventdef ed WHERE ed.s_eventdefid = " + safeSQL.addVar(eventDefId) + " AND cp.s_clinicalprotocolid = ed.clinicalprotocolid AND cp.s_clinicalprotocolversionid = ed.clinicalprotocolversionid AND cp.s_clinicalprotocolrevision = ed.clinicalprotocolrevision AND cp.s_sampletypeid in (" + safeSQL.addIn(sampleTypeInClause) + ") ORDER BY cp.s_sampletypeid, cp.specimentype,cp.arrivalorder DESC";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(specimentDefSQL, safeSQL.getValues());
            DataSet specimentDefData = new DataSet();
            ArrayList<DataSet> list = ds.getGroupedDataSets("s_sampletypeid,specimentype");
            for (DataSet tempDS : list) {
                if (tempDS.size() > 1) {
                    if (tempDS.getValue(0, "arrivalorder").equalsIgnoreCase("All")) {
                        specimentDefData.copyRow(tempDS, 1, 1);
                        continue;
                    }
                    specimentDefData.copyRow(tempDS, 0, 1);
                    continue;
                }
                specimentDefData.copyRow(tempDS, 0, 1);
            }
            Trace.logInfo("AddSmplTypToEventDef", "Adding specimendef information to s_eventdefstspecimendef.");
            PreparedStatement insertSpecimenDef = this.database.prepareStatement("insertCPEventSampletType", "INSERT INTO s_eventdefstspecimendef (s_eventdefid, s_sampletypeid, s_specimendefid, specimentype, quantity, usersequence, createdt, createtool, createby) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (int di = 0; di < specimentDefData.getRowCount(); ++di) {
                sampleTypeId = specimentDefData.getValue(di, "s_sampletypeid", "");
                specimenType = specimentDefData.getValue(di, "specimentype", "");
                String string = quantity = specimentDefData.getValue(di, "arrivalorder", "").equalsIgnoreCase("All") ? "" : specimentDefData.getValue(di, "arrivalorder", "");
                if (quantity != null && quantity.length() == 0) {
                    quantity = null;
                }
                insertSpecimenDef.setString(1, eventDefId);
                insertSpecimenDef.setString(2, sampleTypeId);
                insertSpecimenDef.setString(3, String.valueOf(di + 1));
                insertSpecimenDef.setString(4, specimenType);
                insertSpecimenDef.setString(5, quantity);
                insertSpecimenDef.setString(6, String.valueOf(++maxUserSequence));
                insertSpecimenDef.setTimestamp(7, DateTimeUtil.getNowTimestamp());
                insertSpecimenDef.setString(8, this.connectionInfo.getTool());
                insertSpecimenDef.setString(9, this.connectionInfo.getSysuserId());
                insertSpecimenDef.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new SapphireException("AddSmplTypToEventDef", tp.translate("Error adding specimendef:") + " " + tp.translate(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId()))));
        }
        finally {
            this.database.closeStatement("insertCPEventSampletType");
        }
        maxUserSequence = 0;
        QueryProcessor queryProcessor = this.getQueryProcessor();
        safeSQL.reset();
        String assayTypeSQL = "select ed.s_eventdefid, cp.s_sampletypeid, cp.s_assaytypeid, '1' s_mapid, cp.specimentype, cp.arrivalorder, '' workitemid, 'Y' defaultworkitemflag, '1' usersequence from s_cpassaytypesampletype cp, s_eventdef ed where cp.s_sampletypeid in (" + safeSQL.addIn(sampleTypeInClause) + ") and ed.s_eventdefid = " + safeSQL.addVar(eventDefId) + " and cp.s_clinicalprotocolid = ed.clinicalprotocolid and cp.s_clinicalprotocolversionid = ed.clinicalprotocolversionid and cp.s_clinicalprotocolrevision = ed.clinicalprotocolrevision";
        DataSet assaytypeData = queryProcessor.getPreparedSqlDataSet(assayTypeSQL, safeSQL.getValues());
        Trace.logInfo("AddSmplTypToEventDef", "Adding assaytype information to s_eventdefstatmap.");
        if (assaytypeData.getRowCount() > 0) {
            assaytypeData.setDate(-1, "createdt", now);
            assaytypeData.setString(-1, "createtool", this.connectionInfo.getTool());
            assaytypeData.setString(-1, "createby", this.connectionInfo.getSysuserId());
            DataSetUtil.insert(this.database, assaytypeData, "s_eventdefstatmap");
        }
    }
}


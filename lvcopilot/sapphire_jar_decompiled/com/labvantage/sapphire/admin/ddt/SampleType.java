/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SampleType
extends BaseSDCRules {
    public static String LABVANTAGE_CVS_ID = "$Revision: 54307 $";
    public static final String COLUMN_PARENTSAMPLETYPEID = "parentsampletypeid";
    public static final String COLUMN_SUBTYPEFLAG = "subtypeflag";
    public static final String COLUMN_S_SAMPLETYPEID = "s_sampletypeid";

    @Override
    public void preAdd(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.validateAndSetSubType(primary);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList list) throws SapphireException {
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.validateAndSetSubType(primary);
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList list) throws SapphireException {
    }

    @Override
    public void preDelete(String string, PropertyList list) throws SapphireException {
    }

    @Override
    public void postDelete(String rsetid, PropertyList list) throws SapphireException {
        String keyid1 = list.getProperty("keyid1");
        if (keyid1.length() > 0) {
            String key = StringUtil.replaceAll(keyid1, ";", "','");
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("delete from s_preptypesampletypemap");
            sql.append(" where ( sourcesampletypeid in ( ").append(safeSQL.addIn(key)).append(" ) or destsampletypeid in ( ").append(safeSQL.addIn(key)).append(" ) )");
            this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
        }
    }

    private void validateAndSetSubType(DataSet primary) throws SapphireException {
        try {
            PreparedStatement subTypeStatement = this.database.prepareStatement("subtypes", "SELECT s_sampletypeid FROM s_sampletype WHERE parentsampletypeid = ?");
            PreparedStatement isSubTypeStatement = this.database.prepareStatement("issubtype", "SELECT subtypeflag FROM s_sampletype WHERE s_sampletypeid = ?");
            for (int i = 0; i < primary.size(); ++i) {
                String sampleTypeId;
                String parentSampleTypeId = primary.getValue(i, COLUMN_PARENTSAMPLETYPEID, "");
                if (parentSampleTypeId.equals(sampleTypeId = primary.getValue(i, COLUMN_S_SAMPLETYPEID, ""))) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Parent Sample Type ID cannot be same as Sample Type ID"));
                }
                if (parentSampleTypeId.length() > 0) {
                    isSubTypeStatement.setString(1, parentSampleTypeId);
                    DataSet subTypeFlag = new DataSet(isSubTypeStatement.executeQuery());
                    if (subTypeFlag.getRowCount() > 0 && subTypeFlag.getValue(0, COLUMN_SUBTYPEFLAG, "N").equals("Y")) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Select a Parent SampleType which is not a SubType. Following Parent SampleType is defined as SubType") + " : " + parentSampleTypeId);
                    }
                    subTypeStatement.setString(1, sampleTypeId);
                    DataSet subTypes = new DataSet(subTypeStatement.executeQuery());
                    if (subTypes.getRowCount() > 0) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Cannot set this SampleType as SubType as it is defined as Parent SampleType of the following SubTypes") + " : " + subTypes.getColumnValues(COLUMN_S_SAMPLETYPEID, ", "));
                    }
                    primary.setString(i, COLUMN_SUBTYPEFLAG, "Y");
                    continue;
                }
                primary.setString(i, COLUMN_SUBTYPEFLAG, "N");
            }
            isSubTypeStatement.close();
            subTypeStatement.close();
        }
        catch (SQLException e) {
            this.logger.error("Failed to retrieve data from s_sampletype", e);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDIWorkItemRelation
extends BaseAction
implements sapphire.action.AddSDIWorkItemRelation {
    private static final String PROPERTY_SEPARATOR = ";";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty("sdcid");
        String[] keyId1 = StringUtil.split(properties.getProperty("keyid1"), PROPERTY_SEPARATOR);
        String[] keyId2 = StringUtil.split(properties.getProperty("keyid2"), PROPERTY_SEPARATOR);
        String[] keyId3 = StringUtil.split(properties.getProperty("keyid3"), PROPERTY_SEPARATOR);
        String[] workItemId = StringUtil.split(properties.getProperty("workitemid"), PROPERTY_SEPARATOR);
        String[] workItemInstance = StringUtil.split(properties.getProperty("workiteminstance"), PROPERTY_SEPARATOR);
        if (keyId1.length != workItemId.length) {
            throw new SapphireException("Number of sdcid's does not match workitemid's");
        }
        if (keyId1.length != workItemInstance.length) {
            throw new SapphireException("Number of sdcid's does not match workiteminstance's");
        }
        String[] relationType = StringUtil.split(properties.getProperty("relationtype"), PROPERTY_SEPARATOR);
        String[] relationFunction = StringUtil.split(properties.getProperty("relationfunction"), PROPERTY_SEPARATOR);
        String[] mandatoryFlag = StringUtil.split(properties.getProperty("mandatoryflag"), PROPERTY_SEPARATOR);
        String[] requiredAmount = StringUtil.split(properties.getProperty("requiredamount"), PROPERTY_SEPARATOR);
        String[] requiredAmountUnits = StringUtil.split(properties.getProperty("requiredamountunits"), PROPERTY_SEPARATOR);
        String[] requiredAmountUnitsType = StringUtil.split(properties.getProperty("requiredamountunitstype"), PROPERTY_SEPARATOR);
        String[] relationinstance = StringUtil.split(properties.getProperty("relationinstance"), PROPERTY_SEPARATOR);
        String[] toSdcId = StringUtil.split(properties.getProperty("tosdcid"), PROPERTY_SEPARATOR);
        String[] toKeyId1 = StringUtil.split(properties.getProperty("tokeyid1"), PROPERTY_SEPARATOR);
        String[] toKeyId2 = StringUtil.split(properties.getProperty("tokeyid2"), PROPERTY_SEPARATOR);
        String[] toKeyId3 = StringUtil.split(properties.getProperty("tokeyid3"), PROPERTY_SEPARATOR);
        if (toSdcId.length != toKeyId1.length) {
            throw new SapphireException("tosdcid's do not match tokeyid1's");
        }
        String[] refSdcId = StringUtil.split(properties.getProperty("refsdcid"), PROPERTY_SEPARATOR);
        String[] refKeyId1 = StringUtil.split(properties.getProperty("refkeyid1"), PROPERTY_SEPARATOR);
        String[] refKeyId2 = StringUtil.split(properties.getProperty("refkeyid2"), PROPERTY_SEPARATOR);
        String[] refKeyId3 = StringUtil.split(properties.getProperty("refkeyid3"), PROPERTY_SEPARATOR);
        if (refSdcId.length != refKeyId1.length) {
            throw new SapphireException("refsdcid's do not match refkeyid1's");
        }
        String[] sourceSdcId = StringUtil.split(properties.getProperty("sourcesdcid"), PROPERTY_SEPARATOR);
        String[] sourceKeyId1 = StringUtil.split(properties.getProperty("sourcekeyid1"), PROPERTY_SEPARATOR);
        String[] sourceKeyId2 = StringUtil.split(properties.getProperty("sourcekeyid2"), PROPERTY_SEPARATOR);
        String[] sourceKeyId3 = StringUtil.split(properties.getProperty("sourcekeyid3"), PROPERTY_SEPARATOR);
        String[] originalReagentType = StringUtil.split(properties.getProperty("originalreagenttypeid"), PROPERTY_SEPARATOR);
        String[] originalReagentTypeVersion = StringUtil.split(properties.getProperty("originalreagenttypeversionid"), PROPERTY_SEPARATOR);
        if (sourceSdcId.length != sourceKeyId1.length) {
            throw new SapphireException("sourcesdcid's do not match sourcekeyid1's");
        }
        String[] amount = StringUtil.split(properties.getProperty("amount"), PROPERTY_SEPARATOR);
        String[] amountUnits = StringUtil.split(properties.getProperty("amountunits"), PROPERTY_SEPARATOR);
        String[] amountUnitsType = StringUtil.split(properties.getProperty("amountunitstype"), PROPERTY_SEPARATOR);
        DataSet preProcessed = new DataSet(this.connectionInfo);
        if (sdcId == null || keyId1.length == 0) {
            throw new SapphireException("sdcid is invalid");
        }
        SDCProcessor sdcProc = this.getSDCProcessor();
        String keyColumnCount = sdcProc.getProperty(sdcId, "keycolumns");
        for (int item = 0; item < keyId1.length; ++item) {
            boolean isReagent = false;
            String trackitemid = "";
            if (Integer.parseInt(keyColumnCount) >= 1 && (keyId1[item] == null || keyId1[item].length() == 0)) {
                this.logger.info("Ignore item, keyId1 not specified for " + sdcId);
                continue;
            }
            if (Integer.parseInt(keyColumnCount) >= 2 && (keyId2[item] == null || keyId2[item].length() == 0)) {
                this.logger.info("Ignore item, keyId2 not specified for " + sdcId);
                continue;
            }
            if (Integer.parseInt(keyColumnCount) == 3 && (keyId3[item] == null || keyId3[item].length() == 0)) {
                this.logger.info("Ignore item, keyId3 not specified for " + sdcId);
                continue;
            }
            if (workItemId[item] == null || workItemId[item].length() == 0 || workItemInstance[item] == null || workItemInstance[item].length() == 0) {
                this.logger.info("Ignore item,Workitem keys are not valid ");
                continue;
            }
            int newRow = preProcessed.addRow();
            preProcessed.setString(newRow, "sdcid", sdcId);
            preProcessed.setString(newRow, "keyid1", keyId1[item]);
            if (Integer.parseInt(keyColumnCount) == 1) {
                preProcessed.setString(newRow, "keyid2", "(null)");
            } else {
                preProcessed.setString(newRow, "keyid2", keyId2[item]);
            }
            if (Integer.parseInt(keyColumnCount) != 3) {
                preProcessed.setString(newRow, "keyid3", "(null)");
            } else {
                preProcessed.setString(newRow, "keyid3", keyId3[item]);
            }
            preProcessed.setString(newRow, "workitemid", workItemId[item]);
            preProcessed.setString(newRow, "workiteminstance", workItemInstance[item]);
            if (relationType.length > item) {
                preProcessed.setString(newRow, "relationtype", relationType[item]);
            }
            if (relationFunction.length > item) {
                preProcessed.setString(newRow, "relationfunction", relationFunction[item]);
                isReagent = relationFunction[item].equalsIgnoreCase("Reagent");
            } else if (relationFunction.length == 1) {
                isReagent = relationFunction[0].equalsIgnoreCase("Reagent");
            }
            if (mandatoryFlag.length > item) {
                preProcessed.setString(newRow, "mandatoryflag", mandatoryFlag[item]);
            }
            if (requiredAmount.length > item && requiredAmount[item].length() > 0) {
                preProcessed.setNumber(newRow, "requiredamount", requiredAmount[item]);
            }
            if (requiredAmountUnits.length > item) {
                preProcessed.setString(newRow, "requiredamountunits", requiredAmountUnits[item]);
            }
            if (requiredAmountUnitsType.length > item) {
                preProcessed.setString(newRow, "requiredamountunitstype", requiredAmountUnitsType[item]);
            }
            if (relationinstance.length > item && relationinstance[item].length() > 0) {
                preProcessed.setNumber(newRow, "relationinstance", relationinstance[item]);
            }
            if (toSdcId.length > item) {
                preProcessed.setString(newRow, "tosdcid", toSdcId[item]);
            }
            if (toKeyId1.length > item) {
                preProcessed.setString(newRow, "tokeyid1", toKeyId1[item]);
            }
            if (toKeyId2.length > item) {
                preProcessed.setString(newRow, "tokeyid2", toKeyId2[item]);
            }
            if (toKeyId3.length > item) {
                preProcessed.setString(newRow, "tokeyid3", toKeyId3[item]);
            }
            if (refSdcId.length > item) {
                preProcessed.setString(newRow, "refsdcid", refSdcId[item]);
            }
            if (refKeyId1.length > item) {
                preProcessed.setString(newRow, "refkeyid1", refKeyId1[item]);
                if (refKeyId1[item].trim().length() > 0) {
                    trackitemid = refKeyId1[item];
                }
            }
            if (refKeyId2.length > item) {
                preProcessed.setString(newRow, "refkeyid2", refKeyId2[item]);
            }
            if (refKeyId3.length > item) {
                preProcessed.setString(newRow, "refkeyid3", refKeyId3[item]);
            }
            if (sourceSdcId.length > item) {
                preProcessed.setString(newRow, "sourcesdcid", sourceSdcId[item]);
            }
            if (sourceKeyId1.length > item) {
                preProcessed.setString(newRow, "sourcekeyid1", sourceKeyId1[item]);
                preProcessed.setString(newRow, "originalreagenttypeid", isReagent ? sourceKeyId1[item] : "");
            }
            if (sourceKeyId2.length > item) {
                preProcessed.setString(newRow, "sourcekeyid2", sourceKeyId2[item]);
                preProcessed.setString(newRow, "originalreagenttypeversionid", isReagent ? sourceKeyId2[item] : "");
            }
            if (isReagent && originalReagentType.length > item && originalReagentType[item].trim().length() > 0) {
                preProcessed.setString(newRow, "originalreagenttypeid", originalReagentType[item]);
                if (originalReagentTypeVersion.length > item) {
                    preProcessed.setString(newRow, "originalreagenttypeversionid", originalReagentTypeVersion[item]);
                }
            }
            if (sourceKeyId3.length > item) {
                preProcessed.setString(newRow, "sourcekeyid3", sourceKeyId3[item]);
            }
            if (amount.length > item && amount[item].length() > 0) {
                preProcessed.setNumber(newRow, "amount", amount[item]);
            }
            if (amountUnits.length > item) {
                preProcessed.setString(newRow, "amountunits", amountUnits[item]);
            }
            if (amountUnitsType.length > item) {
                preProcessed.setString(newRow, "amountunitstype", amountUnitsType[item]);
            }
            preProcessed.setString(newRow, "usedexpiredconsumableflag", ReagentUtil.getUsedExpiredConsumableFlag(trackitemid, this.getQueryProcessor()));
        }
        int startId = 0;
        if (preProcessed.getRowCount() > 0) {
            SequenceProcessor sp = this.getSequenceProcessor();
            startId = sp.getSequence("sdiworkitemrelation", "relationid", preProcessed.getRowCount());
        }
        for (int i = 0; i < preProcessed.getRowCount(); ++i) {
            preProcessed.setString(i, "relationid", Integer.toString(startId + i));
        }
        if (preProcessed.getRowCount() > 0) {
            String insertSQL = "INSERT INTO sdiworkitemrelation  (sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance, relationid, relationtype, relationfunction, tosdcid, tokeyid1, tokeyid2, tokeyid3, refsdcid, refkeyid1, refkeyid2, refkeyid3, sourcesdcid, sourcekeyid1, sourcekeyid2, sourcekeyid3, createdt, createby, mandatoryflag, requiredamount, requiredamountunits, requiredamountunitstype,relationinstance,originalreagenttypeid,originalreagenttypeversionid,amountadjusted,amount, amountunits, amountunitstype,usedexpiredconsumableflag) VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )";
            PreparedStatement ps = this.database.prepareStatement(insertSQL);
            try {
                for (int row = 0; row < preProcessed.getRowCount(); ++row) {
                    ps.setString(1, preProcessed.getString(row, "sdcid"));
                    ps.setString(2, preProcessed.getString(row, "keyid1"));
                    ps.setString(3, preProcessed.getString(row, "keyid2"));
                    ps.setString(4, preProcessed.getString(row, "keyid3"));
                    ps.setString(5, preProcessed.getString(row, "workitemid"));
                    ps.setString(6, preProcessed.getString(row, "workiteminstance"));
                    ps.setString(7, preProcessed.getString(row, "relationid"));
                    ps.setString(8, preProcessed.getString(row, "relationtype"));
                    ps.setString(9, preProcessed.getString(row, "relationfunction"));
                    ps.setString(10, preProcessed.getString(row, "tosdcid"));
                    ps.setString(11, preProcessed.getString(row, "tokeyid1"));
                    ps.setString(12, preProcessed.getString(row, "tokeyid2"));
                    ps.setString(13, preProcessed.getString(row, "tokeyid3"));
                    ps.setString(14, preProcessed.getString(row, "refsdcid"));
                    ps.setString(15, preProcessed.getString(row, "refkeyid1"));
                    ps.setString(16, preProcessed.getString(row, "refkeyid2"));
                    ps.setString(17, preProcessed.getString(row, "refkeyid3"));
                    ps.setString(18, preProcessed.getString(row, "sourcesdcid"));
                    ps.setString(19, preProcessed.getString(row, "sourcekeyid1"));
                    ps.setString(20, preProcessed.getString(row, "sourcekeyid2"));
                    ps.setString(21, preProcessed.getString(row, "sourcekeyid3"));
                    ps.setTimestamp(22, DateTimeUtil.getNowTimestamp());
                    ps.setString(23, this.connectionInfo.getSysuserId());
                    ps.setString(24, preProcessed.getString(row, "mandatoryflag"));
                    BigDecimal reqdAmt = preProcessed.getBigDecimal(row, "requiredamount");
                    if (reqdAmt != null) {
                        ps.setBigDecimal(25, reqdAmt);
                    } else {
                        ps.setNull(25, 2);
                    }
                    ps.setString(26, preProcessed.getString(row, "requiredamountunits"));
                    ps.setString(27, preProcessed.getString(row, "requiredamountunitstype"));
                    BigDecimal ins = preProcessed.getBigDecimal(row, "relationinstance");
                    if (ins != null) {
                        ps.setBigDecimal(28, ins);
                    } else {
                        ps.setNull(28, 2);
                    }
                    ps.setString(29, preProcessed.getString(row, "originalreagenttypeid"));
                    ps.setString(30, preProcessed.getString(row, "originalreagenttypeversionid"));
                    if (reqdAmt != null) {
                        ps.setBigDecimal(31, reqdAmt);
                    } else {
                        ps.setNull(31, 2);
                    }
                    BigDecimal preAmt = preProcessed.getBigDecimal(row, "amount");
                    if (preAmt != null) {
                        ps.setBigDecimal(32, preAmt);
                    } else {
                        ps.setNull(32, 2);
                    }
                    ps.setString(33, preProcessed.getString(row, "amountunits"));
                    ps.setString(34, preProcessed.getString(row, "amountunitstype"));
                    ps.setString(35, preProcessed.getString(row, "usedexpiredconsumableflag"));
                    ps.executeUpdate();
                }
            }
            catch (SQLException ex) {
                this.database.closeStatement();
                throw new SapphireException(ex);
            }
            this.database.closeStatement();
        }
    }
}


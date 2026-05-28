/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.action;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.simplespec.action.AutomaticVersioning;
import com.labvantage.sapphire.pageelements.simplespec.action.SpecHelper;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class DeleteSpecParam
extends BaseAction {
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_PARAMLISTID = "paramlistid";
    public static final String PROPERTY_PARAMLISTVERSIONID = "paramlistversionid";
    public static final String PROPERTY_PARAMID = "paramid";
    public static final String PROPERTY_VARIANTID = "variantid";
    public static final String PROPERTY_PARAMTYPE = "paramtype";
    public static final String PROPERTY_AUTOMATICVERSIONING = "automaticversioning";
    public static final String PROPERTY_SPECIDCOLUMN = "specidcolumn";
    public static final String PROPERTY_SPECVERSIONIDCOLUMN = "specversionidcolumn";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty(PROPERTY_SDCID);
        String keyId1 = properties.getProperty(PROPERTY_KEYID1);
        String keyId2 = properties.getProperty(PROPERTY_KEYID2);
        String keyId3 = properties.getProperty(PROPERTY_KEYID3);
        String paramListIds = properties.getProperty(PROPERTY_PARAMLISTID);
        String paramListVersionIds = properties.getProperty(PROPERTY_PARAMLISTVERSIONID);
        String variantIds = properties.getProperty(PROPERTY_VARIANTID);
        String paramIds = properties.getProperty(PROPERTY_PARAMID);
        String paramTypes = properties.getProperty(PROPERTY_PARAMTYPE);
        AutomaticVersioning automaticVersioning = AutomaticVersioning.fromString(properties.getProperty(PROPERTY_AUTOMATICVERSIONING));
        String specIdColumn = properties.getProperty(PROPERTY_SPECIDCOLUMN);
        String specVersionIdColumn = properties.getProperty(PROPERTY_SPECVERSIONIDCOLUMN);
        if (sdcId.isEmpty() || sdcId.contains(";")) {
            throw new SapphireException("Missing or invalid mandatory input property SDC ID: " + sdcId);
        }
        if (keyId1.isEmpty() || keyId1.contains(";")) {
            throw new SapphireException("Missing or invalid mandatory input property Key ID1: " + keyId1);
        }
        if (paramListIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property parameter list ID");
        }
        if (paramListVersionIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property parameter list version ID");
        }
        if (variantIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property variant ID");
        }
        if (paramIds.isEmpty()) {
            throw new SapphireException("Missing mandatory input property parameter ID");
        }
        if (paramTypes.isEmpty()) {
            throw new SapphireException("Missing mandatory input property parameter type");
        }
        if (specIdColumn.isEmpty()) {
            throw new SapphireException("Missing mandatory input property spec ID column");
        }
        if (specVersionIdColumn.isEmpty()) {
            throw new SapphireException("Missing mandatory input property spec version ID column");
        }
        SpecHelper specHelper = new SpecHelper(this.getConnectionId());
        DataSet getSpecDs = specHelper.getSpec(sdcId, keyId1, keyId2, keyId3, specIdColumn, specVersionIdColumn);
        String specId = getSpecDs.getString(0, "specid", "");
        String specVersionId = getSpecDs.getString(0, "specversionid", "");
        if (!specId.isEmpty()) {
            if (automaticVersioning == AutomaticVersioning.ALWAYS || automaticVersioning == AutomaticVersioning.ONLY_REFERENCED && specHelper.checkSpecReference(specId, specVersionId)) {
                PropertyList addSpecVersionProps = specHelper.createNewSpecVersion(specId, specVersionId);
                specId = addSpecVersionProps.getProperty("newkeyid1");
                specVersionId = addSpecVersionProps.getProperty("newkeyid2");
                specHelper.addSpecToSDI(sdcId, keyId1, keyId2, keyId3, specId, specVersionId, specIdColumn, specVersionIdColumn);
            }
            PropertyList deleteSpecParamItemsProps = new PropertyList();
            deleteSpecParamItemsProps.setProperty(PROPERTY_SDCID, "SpecSDC");
            deleteSpecParamItemsProps.setProperty(PROPERTY_KEYID1, specId);
            deleteSpecParamItemsProps.setProperty(PROPERTY_KEYID2, specVersionId);
            deleteSpecParamItemsProps.setProperty("linkid", "Param Items");
            deleteSpecParamItemsProps.setProperty(PROPERTY_PARAMLISTID, paramListIds);
            deleteSpecParamItemsProps.setProperty(PROPERTY_PARAMLISTVERSIONID, paramListVersionIds);
            deleteSpecParamItemsProps.setProperty(PROPERTY_VARIANTID, variantIds);
            deleteSpecParamItemsProps.setProperty(PROPERTY_PARAMID, paramIds);
            deleteSpecParamItemsProps.setProperty(PROPERTY_PARAMTYPE, paramTypes);
            this.getActionProcessor().processAction("DeleteSDIDetail", "1", deleteSpecParamItemsProps);
            int limitCount = 8;
            List<String> paramListIdList = Arrays.asList(StringUtil.split(paramListIds, ";"));
            List<String> paramListVersionIdList = Arrays.asList(StringUtil.split(paramListVersionIds, ";"));
            List<String> variantIdList = Arrays.asList(StringUtil.split(variantIds, ";"));
            List<String> paramIdList = Arrays.asList(StringUtil.split(paramIds, ";"));
            List<String> paramTypeList = Arrays.asList(paramTypes.split(";"));
            PropertyList deleteSpecParamLimitsProps = new PropertyList();
            deleteSpecParamLimitsProps.setProperty(PROPERTY_SDCID, "SpecSDC");
            deleteSpecParamLimitsProps.setProperty(PROPERTY_KEYID1, specId);
            deleteSpecParamLimitsProps.setProperty(PROPERTY_KEYID2, specVersionId);
            deleteSpecParamLimitsProps.setProperty("linkid", "Param Limits");
            for (int i = 0; i < limitCount; ++i) {
                for (int j = 0; j < paramListIdList.size(); ++j) {
                    deleteSpecParamLimitsProps.setProperty(PROPERTY_PARAMLISTID, paramListIdList.get(j));
                    deleteSpecParamLimitsProps.setProperty(PROPERTY_PARAMLISTVERSIONID, paramListVersionIdList.get(j));
                    deleteSpecParamLimitsProps.setProperty(PROPERTY_VARIANTID, variantIdList.get(j));
                    deleteSpecParamLimitsProps.setProperty(PROPERTY_PARAMID, paramIdList.get(j));
                    deleteSpecParamLimitsProps.setProperty(PROPERTY_PARAMTYPE, paramTypeList.get(j));
                    deleteSpecParamLimitsProps.setProperty("limittypesequence", Integer.toString(i + 1));
                    this.getActionProcessor().processAction("DeleteSDIDetail", "1", deleteSpecParamLimitsProps);
                }
            }
            specHelper.addSpecToSDI(sdcId, keyId1, keyId2, keyId3, specId, specVersionId, specIdColumn, specVersionIdColumn);
            specHelper.syncVersionStatusWithPrimary(specId, specVersionId, sdcId, keyId1, keyId2, keyId3);
        }
    }
}


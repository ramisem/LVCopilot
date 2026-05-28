/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.coc;

public class SDICOCItem {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __SdcID;
    private String __KeyID1;
    private String __CurrentCustodian;
    private String __LastCustodian;
    private boolean __CocStarted;
    private boolean __ControlSubstance;

    public SDICOCItem(String keyid1) {
        this.__KeyID1 = keyid1;
    }

    public SDICOCItem(String sdcid, String keyid1) {
        this.__SdcID = sdcid;
        this.__KeyID1 = keyid1;
    }

    public String getSdcid() {
        return this.__SdcID;
    }

    public void setSdcid(String __sdcid) {
        this.__SdcID = __sdcid;
    }

    public String getKeyid1() {
        return this.__KeyID1;
    }

    public void setKeyid1(String __keyid1) {
        this.__KeyID1 = __keyid1;
    }

    public String getCurrentCustodian() {
        return this.__CurrentCustodian;
    }

    public void setCurrentCustodian(String __currentCustodian) {
        this.__CurrentCustodian = __currentCustodian;
    }

    public boolean isCocStarted() {
        return this.__CocStarted;
    }

    public void setCocStarted(boolean __cocStarted) {
        this.__CocStarted = __cocStarted;
    }

    public String geLastCustodian() {
        return this.__LastCustodian;
    }

    public void setLastCustodian(String __lastCustodian) {
        this.__LastCustodian = __lastCustodian;
    }

    public boolean isControlsubstance() {
        return this.__ControlSubstance;
    }

    public void setControlsubstance(boolean __controlsubstance) {
        this.__ControlSubstance = __controlsubstance;
    }
}


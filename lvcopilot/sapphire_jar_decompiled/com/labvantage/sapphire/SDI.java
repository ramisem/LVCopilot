/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import sapphire.util.DataSet;

public class SDI {
    static final String LABVANTAGE_CVS_ID = "$Revision: 62367 $";
    public String sdcid = "";
    public String keyid1 = "";
    public String keyid2 = "(null)";
    public String keyid3 = "(null)";

    public String getSdcid() {
        return this.sdcid != null && this.sdcid.length() > 0 ? this.sdcid : "";
    }

    public String getKeyid1() {
        return this.keyid1 != null ? this.keyid1 : "";
    }

    public String getKeyid2() {
        return this.keyid2 != null && this.keyid2.length() > 0 ? this.keyid2 : "(null)";
    }

    public String getKeyid3() {
        return this.keyid3 != null && this.keyid3.length() > 0 ? this.keyid3 : "(null)";
    }

    public String getKeyText() {
        return this.getSdcid() + " (" + this.getKeyid1() + (this.getKeyid2().equals("(null)") ? "" : ", " + this.getKeyid2()) + (this.getKeyid3().equals("(null)") ? "" : ", " + this.getKeyid3()) + ")";
    }

    public String toString() {
        return this.getKeyText();
    }

    public boolean isValid() {
        return this.getSdcid().length() > 0 && this.getKeyid1().length() > 0 && this.getKeyid2().length() > 0 && this.getKeyid3().length() > 0;
    }

    public void setSdi(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2 != null && keyid2.length() > 0 ? keyid2 : "(null)";
        this.keyid3 = keyid3 != null && keyid3.length() > 0 ? keyid3 : "(null)";
    }

    public SDI() {
        this.sdcid = "";
        this.keyid1 = "";
        this.keyid2 = "(null)";
        this.keyid3 = "(null)";
    }

    public SDI(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.setSdi(sdcid, keyid1, keyid2, keyid3);
    }

    public DataSet toDataSet() {
        DataSet ds = new DataSet();
        ds.addColumnValues("keyid1", 0, this.getKeyid1(), ";");
        ds.addColumnValues("keyid2", 0, this.getKeyid2(), ";");
        ds.addColumnValues("keyid3", 0, this.getKeyid3(), ";");
        ds.setString(-1, "sdcid", this.getSdcid());
        return ds;
    }
}


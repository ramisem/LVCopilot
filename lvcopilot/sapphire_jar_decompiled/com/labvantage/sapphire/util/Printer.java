/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import java.math.BigDecimal;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class Printer {
    private final String addressId;
    private final String addressType;
    private String printerName;
    private BigDecimal height;
    private BigDecimal width;
    private boolean rotateAutomatically = false;

    public Printer(String addressid, String addresstype, QueryProcessor qp) throws SapphireException {
        this.addressId = addressid;
        this.addressType = addresstype;
        this.printerName = "";
        if (addressid.length() > 0 && addresstype.length() > 0) {
            try {
                DataSet printerDs = qp.getPreparedSqlDataSet("SELECT printerid, printerautorotateflag, height, width FROM address WHERE addressid = ? AND addresstype = ?", new Object[]{addressid, addresstype});
                if (printerDs.getRowCount() > 0) {
                    this.printerName = printerDs.getString(0, "printerid");
                    this.rotateAutomatically = printerDs.getValue(0, "printerautorotateflag", "N").equals("Y");
                    this.height = printerDs.getBigDecimal(0, "height");
                    this.width = printerDs.getBigDecimal(0, "width");
                }
                if (this.printerName == null || this.printerName.length() == 0) {
                    throw new SapphireException("PROCESSACTION_FAILED", "Could not determine the report printer using addressid " + addressid + " and addresstype " + addresstype);
                }
            }
            catch (SapphireException e) {
                throw new SapphireException("PROCESSACTION_FAILED", "Error found looking up printer for addressid " + addressid + " and addresstype " + addresstype, e);
            }
        }
    }

    public String getAddressId() {
        return this.addressId;
    }

    public String getAddressType() {
        return this.addressType;
    }

    public String getPrinterName() {
        return this.printerName;
    }

    public boolean isRotateAutomatically() {
        return this.rotateAutomatically;
    }

    public BigDecimal getHeight() {
        return this.height;
    }

    public BigDecimal getWidth() {
        return this.width;
    }
}


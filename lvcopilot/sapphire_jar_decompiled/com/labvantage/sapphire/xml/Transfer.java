/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import java.util.ArrayList;

public class Transfer {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String TYPE_EXPORT = "export";
    public static final String TYPE_IMPORT = "import";
    private String id;
    private ArrayList transfers = new ArrayList();
    private String type = "export";

    public Transfer(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addTransfer(Object transfer) {
        this.transfers.add(transfer);
    }

    public Object getLatestTransfer() {
        return this.transfers.size() > 0 ? this.transfers.get(this.transfers.size() - 1) : null;
    }

    public Object getTransfer(int i) {
        return this.transfers.get(i);
    }

    public ArrayList getTransfers() {
        return this.transfers;
    }
}


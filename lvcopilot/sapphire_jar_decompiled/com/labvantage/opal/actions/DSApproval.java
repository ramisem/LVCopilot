/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

public class DSApproval {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private boolean mandatory;
    private boolean approved;

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public boolean isApproved() {
        return this.approved;
    }

    public String toString() {
        return "DSApproval{mandatory=" + this.mandatory + ", approved=" + this.approved + "}";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql;

public interface DBColumn {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public void setDateColumn(boolean var1);

    public void setPrefix(String var1);

    public String getNvlImpl();

    public String getLengthImpl();
}

